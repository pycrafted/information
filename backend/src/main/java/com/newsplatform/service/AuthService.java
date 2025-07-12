package com.newsplatform.service;

import com.newsplatform.dto.request.LoginRequest;
import com.newsplatform.dto.response.AuthResponse;
import com.newsplatform.entity.AuthToken;
import com.newsplatform.entity.RefreshToken;
import com.newsplatform.entity.User;
import com.newsplatform.exception.BusinessException;
import com.newsplatform.exception.ValidationException;
import com.newsplatform.repository.UserRepository;
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
                return AuthResponse.failure("Identifiants invalides");
            }

            User user = userOpt.get();

            // Vérification du mot de passe
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return AuthResponse.failure("Identifiants invalides");
            }

            // Vérification que l'utilisateur est actif
            if (!user.getActive()) {
                return AuthResponse.failure("Compte utilisateur désactivé");
            }

            // Extraction des informations de la requête HTTP
            String clientIp = extractClientIp(request);
            String userAgent = extractUserAgent(request);

            // Génération des jetons JWT
            AuthToken accessToken = tokenService.generateAccessToken(user, clientIp, userAgent);
            RefreshToken refreshToken = tokenService.generateRefreshToken(user, clientIp, userAgent);

            // Mise à jour de la dernière connexion
            user.updateLastLogin();
            userRepository.save(user);

            // Création de la réponse d'authentification
            return AuthResponse.success(
                accessToken.getTokenValue(),
                refreshToken.getTokenValue(),
                accessToken.getExpiresAt(),
                refreshToken.getExpiresAt(),
                user
            );

        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
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
                return AuthResponse.failure("Utilisateur non trouvé");
            }
            
            // Révocation de tous les jetons de l'utilisateur
            tokenService.revokeAllUserTokens(userOpt.get());
            
            return new AuthResponse(true, "Déconnexion réussie");
        } catch (Exception e) {
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
            // Validation du jeton de rafraîchissement
            Optional<RefreshToken> refreshTokenOpt = tokenService.validateRefreshToken(refreshTokenValue);
            
            if (refreshTokenOpt.isEmpty()) {
                return AuthResponse.failure("Jeton de rafraîchissement invalide");
            }

            RefreshToken refreshToken = refreshTokenOpt.get();
            User user = refreshToken.getUser();
            
            // Vérification que l'utilisateur est toujours actif
            if (!user.getActive()) {
                return AuthResponse.failure("Compte utilisateur désactivé");
            }

            // Extraction des informations de la requête HTTP
            String clientIp = extractClientIp(request);
            String userAgent = extractUserAgent(request);

            // Génération d'un nouveau jeton d'accès
            AuthToken newAccessToken = tokenService.generateAccessToken(user, clientIp, userAgent);

            // Création de la réponse avec le nouveau jeton
            return AuthResponse.success(
                newAccessToken.getTokenValue(),
                refreshToken.getTokenValue(), // Même refresh token
                newAccessToken.getExpiresAt(),
                refreshToken.getExpiresAt(),
                user
            );

        } catch (Exception e) {
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
}
