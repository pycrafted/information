package com.newsplatform.service;

import com.newsplatform.dto.request.LoginRequest;
import com.newsplatform.dto.response.AuthResponse;
import com.newsplatform.entity.AuthToken;
import com.newsplatform.entity.RefreshToken;
import com.newsplatform.entity.User;
import com.newsplatform.exception.BusinessException;
import com.newsplatform.exception.ValidationException;
import com.newsplatform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

/**
 * Service d'authentification REST.
 * Couche Service : Logique métier pour l'authentification via API REST
 * Gère les 3 rôles utilisateur selon le cahier des charges : VISITEUR, EDITEUR, ADMINISTRATEUR.
 */
@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(TokenService tokenService, 
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Authentification utilisateur via API REST
     * Logique métier : Validation credentials et génération des jetons JWT
     * 
     * @param loginRequest Données de connexion
     * @param request Requête HTTP pour IP et User-Agent
     * @return Réponse d'authentification avec jetons JWT
     * @throws ValidationException si les paramètres sont invalides
     * @throws BusinessException si l'authentification échoue
     */
    public AuthResponse authenticateUser(LoginRequest loginRequest, HttpServletRequest request) {
        try {
            // Validation des paramètres d'entrée
            validateLoginRequest(loginRequest);

            // Recherche de l'utilisateur par nom d'utilisateur ou email
            Optional<User> userOpt = findUserByUsernameOrEmail(loginRequest.getUsername());
            
            if (userOpt.isEmpty()) {
                logger.warn("🔍 UTILISATEUR NON TROUVÉ - Username: '{}'", loginRequest.getUsername());
                return AuthResponse.failure("Identifiants invalides");
            }

            User user = userOpt.get();
            logger.info("👤 UTILISATEUR TROUVÉ - Username: '{}' - Rôle: {} - Actif: {}", 
                       user.getUsername(), user.getRole(), user.getActive());

            // Vérification du mot de passe
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                logger.warn("🔒 MOT DE PASSE INCORRECT - Username: '{}'", loginRequest.getUsername());
                return AuthResponse.failure("Identifiants invalides");
            }

            // Vérification que l'utilisateur est actif
            if (!user.getActive()) {
                logger.warn("🚫 COMPTE DÉSACTIVÉ - Username: '{}' - Rôle: {}", 
                           user.getUsername(), user.getRole());
                return AuthResponse.failure("Compte utilisateur désactivé");
            }

            // Extraction des informations de la requête HTTP
            String clientIp = extractClientIp(request);
            String userAgent = extractUserAgent(request);

            logger.info("🔑 GÉNÉRATION DES JETONS - Username: '{}' - Rôle: {} - IP: {}", 
                       user.getUsername(), user.getRole(), clientIp);

            // Génération des jetons JWT
            AuthToken accessToken = tokenService.generateAccessToken(user, clientIp, userAgent);
            RefreshToken refreshToken = tokenService.generateRefreshToken(user, clientIp, userAgent);

            // Mise à jour de la dernière connexion
            user.updateLastLogin();
            userRepository.save(user);

            logger.info("🎉 AUTHENTIFICATION RÉUSSIE - Username: '{}' - Rôle: {} - IP: {} - Dernière connexion: {}", 
                       user.getUsername(), user.getRole(), clientIp, user.getLastLogin());

            // Création de la réponse d'authentification
            return AuthResponse.success(
                accessToken.getTokenValue(),
                refreshToken.getTokenValue(),
                accessToken.getExpiresAt(),
                refreshToken.getExpiresAt(),
                user
            );

        } catch (ValidationException e) {
            logger.warn("⚠️ ERREUR DE VALIDATION - Username: '{}' - Erreur: {}", 
                       loginRequest.getUsername(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("💥 ERREUR SYSTÈME - Username: '{}' - Erreur: {}", 
                        loginRequest.getUsername(), e.getMessage(), e);
            throw new BusinessException("Erreur lors de l'authentification", e);
        }
    }

    /**
     * Déconnexion utilisateur
     * Révoque tous les jetons actifs de l'utilisateur
     * 
     * @param userId ID de l'utilisateur à déconnecter
     * @return Message de confirmation
     */
    public AuthResponse logoutUser(String userId) {
        try {
            // Recherche de l'utilisateur par ID
            Optional<User> userOpt = userRepository.findById(UUID.fromString(userId));
            if (userOpt.isEmpty()) {
                logger.warn("🔍 UTILISATEUR NON TROUVÉ POUR DÉCONNEXION - UserID: {}", userId);
                return AuthResponse.failure("Utilisateur non trouvé");
            }
            
            User user = userOpt.get();
            logger.info("🚪 DÉCONNEXION UTILISATEUR - Username: '{}' - Rôle: {} - UserID: {}", 
                       user.getUsername(), user.getRole(), userId);
            
            // Révocation de tous les jetons de l'utilisateur
            tokenService.revokeAllUserTokens(user);
            
            logger.info("✅ JETONS RÉVOQUÉS - Username: '{}' - UserID: {}", user.getUsername(), userId);
            
            return new AuthResponse(true, "Déconnexion réussie");
        } catch (Exception e) {
            logger.error("💥 ERREUR DÉCONNEXION - UserID: {} - Erreur: {}", userId, e.getMessage(), e);
            throw new BusinessException("Erreur lors de la déconnexion", e);
        }
    }

    /**
     * Rafraîchissement du jeton d'accès
     * 
     * @param refreshTokenValue Jeton de rafraîchissement
     * @param request Requête HTTP pour IP et User-Agent
     * @return Nouveaux jetons d'authentification
     */
    public AuthResponse refreshAccessToken(String refreshTokenValue, HttpServletRequest request) {
        try {
            logger.info("🔄 VALIDATION REFRESH TOKEN - Token: {}...", 
                       refreshTokenValue.substring(0, Math.min(10, refreshTokenValue.length())));
            
            // Validation du jeton de rafraîchissement
            Optional<RefreshToken> refreshTokenOpt = tokenService.validateRefreshToken(refreshTokenValue);
            
            if (refreshTokenOpt.isEmpty()) {
                logger.warn("❌ REFRESH TOKEN INVALIDE - Token: {}...", 
                           refreshTokenValue.substring(0, Math.min(10, refreshTokenValue.length())));
                return AuthResponse.failure("Jeton de rafraîchissement invalide");
            }

            RefreshToken refreshToken = refreshTokenOpt.get();
            User user = refreshToken.getUser();
            
            logger.info("👤 UTILISATEUR REFRESH - Username: '{}' - Rôle: {} - Actif: {}", 
                       user.getUsername(), user.getRole(), user.getActive());
            
            // Vérification que l'utilisateur est toujours actif
            if (!user.getActive()) {
                logger.warn("🚫 COMPTE DÉSACTIVÉ PENDANT REFRESH - Username: '{}'", user.getUsername());
                return AuthResponse.failure("Compte utilisateur désactivé");
            }

            // Extraction des informations de la requête HTTP
            String clientIp = extractClientIp(request);
            String userAgent = extractUserAgent(request);

            logger.info("🔑 GÉNÉRATION NOUVEAU ACCESS TOKEN - Username: '{}' - IP: {}", 
                       user.getUsername(), clientIp);

            // Génération d'un nouveau jeton d'accès
            AuthToken newAccessToken = tokenService.generateAccessToken(user, clientIp, userAgent);

            logger.info("✅ REFRESH TOKEN RÉUSSI - Username: '{}' - Rôle: {} - IP: {}", 
                       user.getUsername(), user.getRole(), clientIp);

            // Création de la réponse avec le nouveau jeton
            return AuthResponse.success(
                newAccessToken.getTokenValue(),
                refreshToken.getTokenValue(), // Même refresh token
                newAccessToken.getExpiresAt(),
                refreshToken.getExpiresAt(),
                user
            );

        } catch (Exception e) {
            logger.error("💥 ERREUR REFRESH TOKEN - Erreur: {}", e.getMessage(), e);
            throw new BusinessException("Erreur lors du rafraîchissement du jeton", e);
        }
    }

    /**
     * Validation des données de connexion
     * 
     * @param loginRequest Données à valider
     * @throws ValidationException si les données sont invalides
     */
    private void validateLoginRequest(LoginRequest loginRequest) {
        if (loginRequest == null) {
            throw new ValidationException("Les données de connexion sont obligatoires");
        }
        
        if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
            throw new ValidationException("Le nom d'utilisateur est obligatoire");
        }
        
        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            throw new ValidationException("Le mot de passe est obligatoire");
        }
    }

    /**
     * Recherche d'un utilisateur par nom d'utilisateur ou email
     * 
     * @param usernameOrEmail Nom d'utilisateur ou email
     * @return Utilisateur trouvé (optionnel)
     */
    private Optional<User> findUserByUsernameOrEmail(String usernameOrEmail) {
        // Recherche d'abord par nom d'utilisateur
        Optional<User> userByUsername = userRepository.findByUsername(usernameOrEmail);
        if (userByUsername.isPresent()) {
            return userByUsername;
        }
        
        // Si pas trouvé, recherche par email
        return userRepository.findByEmail(usernameOrEmail);
    }

    /**
     * Extraction de l'adresse IP du client
     * 
     * @param request Requête HTTP
     * @return Adresse IP du client
     */
    private String extractClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("X-Real-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }

    /**
     * Extraction du User-Agent
     * 
     * @param request Requête HTTP
     * @return User-Agent du client
     */
    private String extractUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    /**
     * Validation d'un jeton d'accès JWT
     * 
     * @param tokenValue Jeton JWT à valider
     * @return Réponse de validation
     */
    public AuthResponse validateToken(String tokenValue) {
        try {
            logger.info("🔍 VALIDATION ACCESS TOKEN - Token: {}...", 
                       tokenValue.substring(0, Math.min(10, tokenValue.length())));
            
            // Validation du jeton via le service de tokens
            Optional<AuthToken> tokenOpt = tokenService.validateAccessToken(tokenValue);
            
            if (tokenOpt.isEmpty()) {
                logger.warn("❌ ACCESS TOKEN INVALIDE - Token: {}...", 
                           tokenValue.substring(0, Math.min(10, tokenValue.length())));
                return AuthResponse.failure("Jeton invalide ou expiré");
            }

            AuthToken token = tokenOpt.get();
            User user = token.getUser();
            
            logger.info("👤 UTILISATEUR TOKEN VALIDE - Username: '{}' - Rôle: {} - Actif: {}", 
                       user.getUsername(), user.getRole(), user.getActive());
            
            // Vérification que l'utilisateur est toujours actif
            if (!user.getActive()) {
                logger.warn("🚫 COMPTE DÉSACTIVÉ PENDANT VALIDATION - Username: '{}'", user.getUsername());
                return AuthResponse.failure("Compte utilisateur désactivé");
            }

            logger.info("✅ TOKEN VALIDE - Username: '{}' - Rôle: {} - Expire: {}", 
                       user.getUsername(), user.getRole(), token.getExpiresAt());

            // Retourner les informations utilisateur si le jeton est valide
            return AuthResponse.success(
                token.getTokenValue(),
                null, // Pas de refresh token pour la validation
                token.getExpiresAt(),
                null,
                user
            );

        } catch (Exception e) {
            logger.error("💥 ERREUR VALIDATION TOKEN - Erreur: {}", e.getMessage(), e);
            throw new BusinessException("Erreur lors de la validation du jeton", e);
        }
    }
}
