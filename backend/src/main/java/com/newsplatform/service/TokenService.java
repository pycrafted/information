package com.newsplatform.service;

import com.newsplatform.entity.AuthToken;
import com.newsplatform.entity.RefreshToken;
import com.newsplatform.entity.User;
import com.newsplatform.exception.BusinessException;
import com.newsplatform.repository.AuthTokenRepository;
import com.newsplatform.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service pour la gestion des jetons d'authentification JWT.
 * Couche Service : Logique métier pour l'authentification sécurisée
 * Implémente la sécurisation des services SOAP et REST selon le cahier des charges.
 */
@Service
@Transactional
public class TokenService {

    private final AuthTokenRepository authTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenCleanupService tokenCleanupService;
    private SecretKey secretKey;
    
    // Configuration JWT depuis application.yml
    @Value("${jwt.secret:defaultSecretKeyForDevelopmentOnly1234567890}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:86400000}") // 24 heures par défaut (en millisecondes)
    private long accessTokenExpiration;
    
    @Value("${jwt.refresh-expiration:604800000}") // 7 jours par défaut (en millisecondes)
    private long refreshTokenExpiration;

    @Autowired
    public TokenService(AuthTokenRepository authTokenRepository, 
                       RefreshTokenRepository refreshTokenRepository,
                       TokenCleanupService tokenCleanupService) {
        this.authTokenRepository = authTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenCleanupService = tokenCleanupService;
    }

    /**
     * Initialisation de la clé secrète JWT après injection des propriétés
     * Couche Service : Configuration sécurisée post-injection
     */
    @PostConstruct
    private void initializeSecretKey() {
        // Génération d'une clé sécurisée pour JWT après injection des propriétés
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            jwtSecret = "defaultSecretKeyForDevelopmentOnly1234567890";
        }
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Génère un jeton d'accès JWT pour un utilisateur
     * Logique métier : Création du jeton selon le rôle utilisateur
     * 
     * @param user Utilisateur pour lequel générer le jeton
     * @param clientIp Adresse IP du client (sécurité)
     * @param userAgent User Agent du client (sécurité)
     * @return Jeton d'accès généré
     * @throws BusinessException si la génération échoue
     */
    public AuthToken generateAccessToken(User user, String clientIp, String userAgent) {
        try {
            // Validation métier : l'utilisateur doit être actif
            if (!user.getActive()) {
                throw new BusinessException("Impossible de générer un jeton pour un utilisateur inactif");
            }

            // Génération du JWT avec les claims utilisateur
            String jwt = Jwts.builder()
                    .setSubject(user.getId().toString())
                    .claim("username", user.getUsername())
                    .claim("email", user.getEmail())
                    .claim("role", user.getRole().name())
                    .claim("roleDescription", user.getRole().getDescription())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                    .signWith(secretKey, SignatureAlgorithm.HS512)
                    .compact();

            // Création et sauvegarde de l'entité AuthToken
            AuthToken authToken = new AuthToken(
                jwt, 
                AuthToken.TokenType.ACCESS, 
                user, 
                LocalDateTime.now().plusSeconds(accessTokenExpiration / 1000)
            );
            authToken.setClientIp(clientIp);
            authToken.setUserAgent(userAgent);

            AuthToken savedToken = authTokenRepository.save(authToken);
            tokenCleanupService.cleanupDuplicateTokensForValue(savedToken.getTokenValue());
            return savedToken;

        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la génération du jeton d'accès", e);
        }
    }

    /**
     * Génère un jeton de rafraîchissement pour un utilisateur
     * Logique métier : Création du refresh token sécurisé
     * 
     * @param user Utilisateur pour lequel générer le jeton
     * @param clientIp Adresse IP du client (sécurité)
     * @param userAgent User Agent du client (sécurité)
     * @return Jeton de rafraîchissement généré
     * @throws BusinessException si la génération échoue
     */
    public RefreshToken generateRefreshToken(User user, String clientIp, String userAgent) {
        try {
            // Validation métier : l'utilisateur doit être actif
            if (!user.getActive()) {
                throw new BusinessException("Impossible de générer un refresh token pour un utilisateur inactif");
            }

            // Génération d'un token unique et sécurisé
            String tokenValue = UUID.randomUUID().toString() + "-" + System.currentTimeMillis();

            // Création et sauvegarde de l'entité RefreshToken
            RefreshToken refreshToken = new RefreshToken(
                tokenValue,
                user,
                LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000)
            );
            refreshToken.setClientIp(clientIp);
            refreshToken.setUserAgent(userAgent);

            RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
            tokenCleanupService.cleanupDuplicateTokensForValue(savedToken.getTokenValue());
            return savedToken;

        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la génération du refresh token", e);
        }
    }

    /**
     * Valide un jeton d'accès JWT
     * Logique métier : Vérification de la validité et extraction des claims
     * 
     * @param tokenValue Valeur du jeton JWT
     * @return Optional contenant l'AuthToken si valide
     */
    @Transactional(readOnly = true)
    public Optional<AuthToken> validateAccessToken(String tokenValue) {
        try {
            // Vérification de la signature et de l'expiration JWT
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseClaimsJws(tokenValue)
                    .getPayload();

            // Recherche du jeton en base de données avec utilisateur chargé
            Optional<AuthToken> authTokenOpt = authTokenRepository.findByTokenValueWithUser(tokenValue);
            
            if (authTokenOpt.isPresent()) {
                AuthToken authToken = authTokenOpt.get();
                
                // Validation métier : le jeton doit être valide
                if (authToken.isValid()) {
                    return Optional.of(authToken);
                } else {
                    // Jeton en base mais invalide (révoqué ou expiré)
                    return Optional.empty();
                }
            }

            return Optional.empty();

        } catch (JwtException | IllegalArgumentException e) {
            // Jeton JWT malformé ou signature invalide
            return Optional.empty();
        } catch (Exception e) {
            // Gestion des doublons - essayer de nettoyer et récupérer
            if (e.getMessage() != null && e.getMessage().contains("Query did not return a unique result")) {
                return handleDuplicateTokens(tokenValue);
            }
            return Optional.empty();
        }
    }

    /**
     * Gère les tokens en double en nettoyant les doublons
     */
    @Transactional
    private Optional<AuthToken> handleDuplicateTokens(String tokenValue) {
        try {
            List<AuthToken> duplicateTokens = authTokenRepository.findAllByTokenValueWithUser(tokenValue);
            
            if (duplicateTokens.isEmpty()) {
                return Optional.empty();
            }
            
            // Garder le token le plus récent et valide
            AuthToken validToken = duplicateTokens.stream()
                .filter(AuthToken::isValid)
                .max((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()))
                .orElse(null);
            
            if (validToken == null) {
                // Aucun token valide trouvé, supprimer tous les doublons
                authTokenRepository.deleteAll(duplicateTokens);
                return Optional.empty();
            }
            
            // Supprimer les autres tokens (doublons)
            List<AuthToken> tokensToDelete = duplicateTokens.stream()
                .filter(token -> !token.getId().equals(validToken.getId()))
                .toList();
            
            if (!tokensToDelete.isEmpty()) {
                authTokenRepository.deleteAll(tokensToDelete);
            }
            
            return Optional.of(validToken);
            
        } catch (Exception e) {
            // En cas d'erreur, supprimer tous les tokens problématiques
            try {
                List<AuthToken> allTokens = authTokenRepository.findAllByTokenValue(tokenValue);
                authTokenRepository.deleteAll(allTokens);
            } catch (Exception cleanupError) {
                // Ignorer les erreurs de nettoyage
            }
            return Optional.empty();
        }
    }

    /**
     * Valide un jeton de rafraîchissement
     * Logique métier : Vérification de la validité du refresh token
     * 
     * @param tokenValue Valeur du refresh token
     * @return Optional contenant le RefreshToken si valide
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> validateRefreshToken(String tokenValue) {
        try {
            Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByTokenValue(tokenValue);
            
            if (refreshTokenOpt.isPresent()) {
                RefreshToken refreshToken = refreshTokenOpt.get();
                
                // Validation métier : le jeton doit être valide
                if (refreshToken.isValid()) {
                    return Optional.of(refreshToken);
                }
            }

            return Optional.empty();

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Renouvelle un jeton d'accès à partir d'un refresh token
     * Logique métier : Renouvellement sécurisé selon le cahier des charges
     * 
     * @param refreshTokenValue Valeur du refresh token
     * @param clientIp Adresse IP du client (sécurité)
     * @param userAgent User Agent du client (sécurité)
     * @return Nouveau jeton d'accès
     * @throws BusinessException si le renouvellement échoue
     */
    public AuthToken renewAccessToken(String refreshTokenValue, String clientIp, String userAgent) {
        Optional<RefreshToken> refreshTokenOpt = validateRefreshToken(refreshTokenValue);
        
        if (refreshTokenOpt.isEmpty()) {
            throw new BusinessException("Refresh token invalide ou expiré");
        }

        RefreshToken refreshToken = refreshTokenOpt.get();
        
        // Validation métier : l'utilisateur doit toujours être actif
        if (!refreshToken.getUser().getActive()) {
            refreshToken.revoke();
            refreshTokenRepository.save(refreshToken);
            throw new BusinessException("Utilisateur inactif - refresh token révoqué");
        }

        // Marquer le refresh token comme utilisé
        refreshToken.markAsUsed();
        refreshTokenRepository.save(refreshToken);

        // Nettoyer les tokens en double pour ce refresh token
        tokenCleanupService.cleanupDuplicateTokensForValue(refreshTokenValue);

        // Générer un nouveau jeton d'accès
        return generateAccessToken(refreshToken.getUser(), clientIp, userAgent);
    }

    /**
     * Révoque tous les jetons d'un utilisateur
     * Logique métier : Déconnexion complète selon les rôles
     * 
     * @param user Utilisateur dont les jetons doivent être révoqués
     * @return Nombre total de jetons révoqués
     */
    public int revokeAllUserTokens(User user) {
        try {
            // Révocation des access tokens
            int revokedAccessTokens = authTokenRepository.revokeAllActiveTokensForUser(
                user, AuthToken.TokenStatus.REVOKED);
            
            // Révocation des refresh tokens
            int revokedRefreshTokens = refreshTokenRepository.revokeAllValidTokensForUser(user);

            return revokedAccessTokens + revokedRefreshTokens;

        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la révocation des jetons utilisateur", e);
        }
    }

    /**
     * Nettoie les jetons expirés (tâche de maintenance)
     * Logique métier : Maintenance de la sécurité
     * 
     * @return Nombre de jetons supprimés
     */
    public int cleanupExpiredTokens() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            
            int deletedAuthTokens = authTokenRepository.deleteOldExpiredTokens(cutoffDate);
            int deletedRefreshTokens = refreshTokenRepository.deleteOldTokens(cutoffDate);

            return deletedAuthTokens + deletedRefreshTokens;

        } catch (Exception e) {
            throw new BusinessException("Erreur lors du nettoyage des jetons expirés", e);
        }
    }

    /**
     * Extrait l'ID utilisateur d'un jeton JWT
     * Logique métier : Extraction des claims utilisateur
     * 
     * @param tokenValue Valeur du jeton JWT
     * @return UUID de l'utilisateur
     * @throws BusinessException si l'extraction échoue
     */
    @Transactional(readOnly = true)
    public UUID extractUserIdFromToken(String tokenValue) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseClaimsJws(tokenValue)
                    .getPayload();

            return UUID.fromString(claims.getSubject());

        } catch (Exception e) {
            throw new BusinessException("Impossible d'extraire l'ID utilisateur du jeton", e);
        }
    }

    /**
     * Extrait le rôle utilisateur d'un jeton JWT
     * Logique métier : Vérification des autorisations selon le cahier des charges
     * 
     * @param tokenValue Valeur du jeton JWT
     * @return Rôle de l'utilisateur
     * @throws BusinessException si l'extraction échoue
     */
    @Transactional(readOnly = true)
    public User.UserRole extractUserRoleFromToken(String tokenValue) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseClaimsJws(tokenValue)
                    .getPayload();

            String roleName = claims.get("role", String.class);
            return User.UserRole.valueOf(roleName);

        } catch (Exception e) {
            throw new BusinessException("Impossible d'extraire le rôle utilisateur du jeton", e);
        }
    }

    /**
     * Vérifie si un jeton appartient à un administrateur
     * Logique métier : Autorisation selon les rôles du cahier des charges
     * 
     * @param tokenValue Valeur du jeton JWT
     * @return true si le jeton appartient à un administrateur
     */
    @Transactional(readOnly = true)
    public boolean isAdminToken(String tokenValue) {
        try {
            User.UserRole role = extractUserRoleFromToken(tokenValue);
            return User.UserRole.ADMINISTRATEUR.equals(role);
        } catch (BusinessException e) {
            return false;
        }
    }

    /**
     * Vérifie si un jeton appartient à un éditeur ou plus
     * Logique métier : Autorisation selon les rôles du cahier des charges
     * 
     * @param tokenValue Valeur du jeton JWT
     * @return true si le jeton appartient à un éditeur ou administrateur
     */
    @Transactional(readOnly = true)
    public boolean isEditorToken(String tokenValue) {
        try {
            User.UserRole role = extractUserRoleFromToken(tokenValue);
            return User.UserRole.EDITEUR.equals(role) || User.UserRole.ADMINISTRATEUR.equals(role);
        } catch (BusinessException e) {
            return false;
        }
    }

    /**
     * Obtient les statistiques des jetons pour un utilisateur
     * Logique métier : Monitoring et sécurité selon le cahier des charges
     * 
     * @param user Utilisateur
     * @return Statistiques des jetons
     */
    @Transactional(readOnly = true)
    public TokenStats getUserTokenStats(User user) {
        try {
            long activeAccessTokens = authTokenRepository.countByUserAndStatus(
                user, AuthToken.TokenStatus.ACTIVE);
            long validRefreshTokens = refreshTokenRepository.countByUserAndRevoked(user, false);
            
            List<RefreshToken> recentTokens = refreshTokenRepository.findRecentlyUsedTokens(
                LocalDateTime.now().minusDays(1));
            long recentUsage = recentTokens.stream()
                .filter(token -> token.getUser().getId().equals(user.getId()))
                .count();

            return new TokenStats(activeAccessTokens, validRefreshTokens, recentUsage);

        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la récupération des statistiques", e);
        }
    }

    /**
     * Classe interne pour les statistiques des jetons
     */
    public static class TokenStats {
        private final long activeAccessTokens;
        private final long validRefreshTokens;
        private final long recentUsage;

        public TokenStats(long activeAccessTokens, long validRefreshTokens, long recentUsage) {
            this.activeAccessTokens = activeAccessTokens;
            this.validRefreshTokens = validRefreshTokens;
            this.recentUsage = recentUsage;
        }

        // Getters
        public long getActiveAccessTokens() { return activeAccessTokens; }
        public long getValidRefreshTokens() { return validRefreshTokens; }
        public long getRecentUsage() { return recentUsage; }
    }
} 