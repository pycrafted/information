package com.newsplatform.service;

import com.newsplatform.dto.soap.LoginSoapRequest;
import com.newsplatform.dto.soap.LoginSoapResponse;
import com.newsplatform.dto.soap.LogoutSoapRequest;
import com.newsplatform.dto.soap.LogoutSoapResponse;
import com.newsplatform.entity.AuthToken;
import com.newsplatform.entity.RefreshToken;
import com.newsplatform.entity.User;
import com.newsplatform.exception.BusinessException;
import com.newsplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service d'authentification SOAP.
 * Couche Service : Logique métier pour l'authentification via services SOAP
 * Utilise TokenService pour la gestion des jetons JWT selon le cahier des charges.
 */
@Service
@Transactional
public class AuthSoapService {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthSoapService(TokenService tokenService, 
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Authentification via service SOAP
     * Logique métier : Authentification et génération des jetons JWT
     * 
     * @param request Requête de connexion SOAP
     * @return Réponse d'authentification avec jetons JWT
     */
    public LoginSoapResponse authenticateUser(LoginSoapRequest request) {
        try {
            // Validation des paramètres d'entrée
            if (request == null || request.getUsername() == null || request.getPassword() == null) {
                return LoginSoapResponse.failure("Nom d'utilisateur et mot de passe requis");
            }

            // Recherche de l'utilisateur par nom d'utilisateur ou email
            Optional<User> userOpt = findUserByUsernameOrEmail(request.getUsername());
            
            if (userOpt.isEmpty()) {
                return LoginSoapResponse.failure("Identifiants invalides");
            }

            User user = userOpt.get();

            // Vérification du mot de passe
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return LoginSoapResponse.failure("Identifiants invalides");
            }

            // Vérification que l'utilisateur est actif
            if (!user.getActive()) {
                return LoginSoapResponse.failure("Compte utilisateur désactivé");
            }

            // Génération des jetons JWT
            AuthToken accessToken = tokenService.generateAccessToken(
                user, 
                request.getClientIp(), 
                request.getUserAgent()
            );

            RefreshToken refreshToken = tokenService.generateRefreshToken(
                user, 
                request.getClientIp(), 
                request.getUserAgent()
            );

            // Mise à jour de la dernière connexion
            user.updateLastLogin();
            userRepository.save(user);

            // Création des informations utilisateur pour la réponse
            LoginSoapResponse.UserInfoSoap userInfo = new LoginSoapResponse.UserInfoSoap(
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getRole().getDescription()
            );

            return LoginSoapResponse.success(
                accessToken.getTokenValue(),
                refreshToken.getTokenValue(),
                accessToken.getExpiresAt(),
                refreshToken.getExpiresAt(),
                userInfo
            );

        } catch (Exception e) {
            return LoginSoapResponse.failure("Erreur lors de l'authentification : " + e.getMessage());
        }
    }

    /**
     * Déconnexion via service SOAP
     * Logique métier : Révocation des jetons selon le type de déconnexion
     * 
     * @param request Requête de déconnexion SOAP
     * @return Réponse de déconnexion avec nombre de jetons révoqués
     */
    public LogoutSoapResponse logoutUser(LogoutSoapRequest request) {
        try {
            // Validation des paramètres d'entrée
            if (request == null || request.getAccessToken() == null) {
                return LogoutSoapResponse.failure("Jeton d'accès requis pour la déconnexion");
            }

            // Validation du jeton d'accès
            Optional<AuthToken> authTokenOpt = tokenService.validateAccessToken(request.getAccessToken());
            
            if (authTokenOpt.isEmpty()) {
                return LogoutSoapResponse.failure("Jeton d'accès invalide ou expiré");
            }

            AuthToken authToken = authTokenOpt.get();
            User user = authToken.getUser();
            int tokensRevoked = 0;

            if (request.isGlobalLogout()) {
                // Déconnexion globale : révoquer tous les jetons de l'utilisateur
                tokensRevoked = tokenService.revokeAllUserTokens(user);
                return LogoutSoapResponse.success("Déconnexion globale réussie", tokensRevoked);
            } else {
                // Déconnexion simple : révoquer le jeton d'accès
                authToken.revoke();
                tokensRevoked++;

                // Révoquer aussi le refresh token si fourni
                if (request.getRefreshToken() != null) {
                    Optional<RefreshToken> refreshTokenOpt = tokenService.validateRefreshToken(request.getRefreshToken());
                    if (refreshTokenOpt.isPresent()) {
                        refreshTokenOpt.get().revoke();
                        tokensRevoked++;
                    }
                }

                return LogoutSoapResponse.success(tokensRevoked);
            }

        } catch (Exception e) {
            return LogoutSoapResponse.failure("Erreur lors de la déconnexion : " + e.getMessage());
        }
    }

    /**
     * Validation d'un jeton pour l'accès aux services SOAP
     * Logique métier : Vérification des autorisations selon les rôles
     * 
     * @param accessToken Jeton d'accès à valider
     * @return Utilisateur associé au jeton si valide
     * @throws BusinessException si le jeton est invalide
     */
    public User validateTokenForSoapAccess(String accessToken) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new BusinessException("Jeton d'accès requis pour accéder aux services SOAP");
        }

        Optional<AuthToken> authTokenOpt = tokenService.validateAccessToken(accessToken);
        
        if (authTokenOpt.isEmpty()) {
            throw new BusinessException("Jeton d'accès invalide ou expiré");
        }

        User user = authTokenOpt.get().getUser();
        
        if (!user.getActive()) {
            throw new BusinessException("Compte utilisateur désactivé");
        }

        return user;
    }

    /**
     * Vérification des autorisations administrateur pour les services SOAP
     * Logique métier : Contrôle d'accès selon les rôles du cahier des charges
     * 
     * @param accessToken Jeton d'accès
     * @return Utilisateur administrateur
     * @throws BusinessException si l'utilisateur n'est pas administrateur
     */
    public User validateAdminAccess(String accessToken) {
        User user = validateTokenForSoapAccess(accessToken);
        
        if (!user.isAdministrator()) {
            throw new BusinessException("Accès administrateur requis pour cette opération SOAP");
        }

        return user;
    }

    /**
     * Vérification des autorisations éditeur pour les services SOAP
     * Logique métier : Contrôle d'accès selon les rôles du cahier des charges
     * 
     * @param accessToken Jeton d'accès
     * @return Utilisateur éditeur ou administrateur
     * @throws BusinessException si l'utilisateur n'a pas les droits d'édition
     */
    public User validateEditorAccess(String accessToken) {
        User user = validateTokenForSoapAccess(accessToken);
        
        if (!user.isEditor()) {
            throw new BusinessException("Accès éditeur requis pour cette opération SOAP");
        }

        return user;
    }

    /**
     * Recherche un utilisateur par nom d'utilisateur ou email
     * Logique métier privée : Support de l'authentification flexible
     * 
     * @param usernameOrEmail Nom d'utilisateur ou email
     * @return Utilisateur trouvé
     */
    private Optional<User> findUserByUsernameOrEmail(String usernameOrEmail) {
        // Essayer d'abord par nom d'utilisateur
        Optional<User> userOpt = userRepository.findByUsername(usernameOrEmail);
        
        // Si pas trouvé, essayer par email
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(usernameOrEmail);
        }
        
        return userOpt;
    }
} 