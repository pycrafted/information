package com.newsplatform.controller.rest;

import com.newsplatform.dto.request.LoginRequest;
import com.newsplatform.dto.response.AuthResponse;
import com.newsplatform.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour l'authentification.
 * Couche Contrôle : Orchestration et validation des requêtes d'authentification
 * Gère l'authentification des 3 rôles selon le cahier des charges : VISITEUR, EDITEUR, ADMINISTRATEUR.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification", description = "API d'authentification JWT pour les 3 rôles utilisateur")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authentification utilisateur
     * Endpoint public pour la connexion avec génération de jetons JWT
     * 
     * @param loginRequest Données de connexion
     * @param request Requête HTTP pour extraction IP/User-Agent
     * @return Réponse d'authentification avec jetons JWT
     */
    @PostMapping("/login")
    @Operation(
        summary = "Connexion utilisateur",
        description = """
            **Authentification avec génération de jetons JWT**
            
            Cet endpoint permet l'authentification des utilisateurs selon leurs rôles :
            - **VISITEUR** : Lecture des articles publiés
            - **EDITEUR** : CRUD Articles + Catégories  
            - **ADMINISTRATEUR** : CRUD Utilisateurs + gestion jetons
            
            **Utilisateurs de test :**
            - `admin` / `OusmaneSonko@2029` - Administrateur complet
            - `editeur` / `OusmaneSonko@2029` - Éditeur articles/catégories
            - `visiteur` / `OusmaneSonko@2029` - Lecture uniquement
            
            **Retourne :**
            - `accessToken` : Jeton d'accès JWT (1h)
            - `refreshToken` : Jeton de rafraîchissement (7j)
            - Informations utilisateur avec rôle
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authentification réussie",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "401", description = "Identifiants incorrects"),
        @ApiResponse(responseCode = "403", description = "Compte désactivé")
    })
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody 
            @Parameter(description = "Données de connexion", required = true)
            LoginRequest loginRequest,
            HttpServletRequest request) {
        
        // Log de la tentative de connexion
        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        logger.info("🔐 TENTATIVE DE CONNEXION - Utilisateur: '{}' - IP: {} - User-Agent: {}", 
                   loginRequest.getUsername(), clientIp, userAgent);
        
        try {
            AuthResponse response = authService.authenticateUser(loginRequest, request);
            
            if (response.isSuccess()) {
                // Log de connexion réussie
                logger.info("✅ CONNEXION RÉUSSIE - Utilisateur: '{}' - Rôle: {} - IP: {}", 
                           loginRequest.getUsername(), 
                           response.getUser() != null ? response.getUser().getRole() : "N/A",
                           clientIp);
                return ResponseEntity.ok(response);
            } else {
                // Log d'échec de connexion
                logger.warn("❌ ÉCHEC DE CONNEXION - Utilisateur: '{}' - Raison: {} - IP: {}", 
                           loginRequest.getUsername(), response.getMessage(), clientIp);
                return ResponseEntity.status(401).body(response);
            }
            
        } catch (Exception e) {
            // Log d'erreur système
            logger.error("💥 ERREUR SYSTÈME - Utilisateur: '{}' - Erreur: {} - IP: {}", 
                        loginRequest.getUsername(), e.getMessage(), clientIp);
            return ResponseEntity.status(500).body(
                AuthResponse.failure("Erreur interne du serveur")
            );
        }
    }

    /**
     * Déconnexion utilisateur
     * Révoque tous les jetons actifs de l'utilisateur
     * 
     * @param userId ID de l'utilisateur à déconnecter
     * @return Confirmation de déconnexion
     */
    @PostMapping("/logout")
    @Operation(
        summary = "Déconnexion utilisateur",
        description = """
            **Déconnexion sécurisée avec révocation des jetons**
            
            Révoque tous les jetons JWT actifs de l'utilisateur :
            - Access tokens révoqués immédiatement
            - Refresh tokens invalidés
            - Sécurisation contre réutilisation malveillante
            
            **Paramètre :**
            - `userId` : UUID de l'utilisateur (extrait généralement du JWT)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Déconnexion réussie"),
        @ApiResponse(responseCode = "400", description = "ID utilisateur invalide"),
        @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    public ResponseEntity<AuthResponse> logout(
            @RequestParam 
            @Parameter(description = "ID de l'utilisateur", required = true)
            String userId,
            HttpServletRequest request) {
        
        String clientIp = getClientIp(request);
        logger.info("🚪 TENTATIVE DE DÉCONNEXION - UserID: {} - IP: {}", userId, clientIp);
        
        try {
            AuthResponse response = authService.logoutUser(userId);
            
            if (response.isSuccess()) {
                logger.info("✅ DÉCONNEXION RÉUSSIE - UserID: {} - IP: {}", userId, clientIp);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("❌ ÉCHEC DÉCONNEXION - UserID: {} - Raison: {} - IP: {}", 
                           userId, response.getMessage(), clientIp);
                return ResponseEntity.status(404).body(response);
            }
            
        } catch (Exception e) {
            logger.error("💥 ERREUR DÉCONNEXION - UserID: {} - Erreur: {} - IP: {}", 
                        userId, e.getMessage(), clientIp);
            return ResponseEntity.status(500).body(
                AuthResponse.failure("Erreur lors de la déconnexion")
            );
        }
    }

    /**
     * Rafraîchissement du jeton d'accès
     * Génère un nouveau jeton d'accès à partir d'un refresh token valide
     * 
     * @param refreshToken Jeton de rafraîchissement
     * @param request Requête HTTP pour extraction IP/User-Agent
     * @return Nouveau jeton d'accès
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "Rafraîchissement du jeton d'accès",
        description = """
            **Renouvellement sécurisé des jetons JWT**
            
            Permet de renouveler l'accès sans nouvelle authentification :
            - Valide le refresh token (non expiré, non révoqué)
            - Génère un nouveau access token (1h)
            - Maintient le même refresh token
            - Vérifie que l'utilisateur est toujours actif
            
            **Sécurité :**
            - Limite d'utilisation du refresh token
            - Tracking des adresses IP et User-Agent
            - Auto-révocation si comportement suspect
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Jeton renouvelé avec succès"),
        @ApiResponse(responseCode = "401", description = "Refresh token invalide ou expiré"),
        @ApiResponse(responseCode = "403", description = "Compte utilisateur désactivé")
    })
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestParam 
            @Parameter(description = "Jeton de rafraîchissement", required = true)
            String refreshToken,
            HttpServletRequest request) {
        
        String clientIp = getClientIp(request);
        logger.info("🔄 TENTATIVE REFRESH TOKEN - IP: {} - Token: {}...", 
                   clientIp, refreshToken.substring(0, Math.min(10, refreshToken.length())));
        
        try {
            AuthResponse response = authService.refreshAccessToken(refreshToken, request);
            
            if (response.isSuccess()) {
                logger.info("✅ REFRESH TOKEN RÉUSSI - IP: {}", clientIp);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("❌ ÉCHEC REFRESH TOKEN - Raison: {} - IP: {}", 
                           response.getMessage(), clientIp);
                return ResponseEntity.status(401).body(response);
            }
            
        } catch (Exception e) {
            logger.error("💥 ERREUR REFRESH TOKEN - Erreur: {} - IP: {}", 
                        e.getMessage(), clientIp);
            return ResponseEntity.status(500).body(
                AuthResponse.failure("Erreur lors du rafraîchissement")
            );
        }
    }

    /**
     * Vérification de validité d'un jeton
     * Endpoint utilitaire pour valider un jeton JWT côté client
     * 
     * @param token Jeton JWT à valider
     * @return Statut de validité du jeton
     */
    @GetMapping("/validate")
    @Operation(
        summary = "Validation d'un jeton JWT",
        description = """
            **Vérification de validité d'un jeton d'accès**
            
            Endpoint utilitaire pour les applications clientes :
            - Vérifie la signature JWT
            - Contrôle l'expiration
            - Valide le statut en base de données
            - Retourne les informations utilisateur si valide
            
            **Utilisation :**
            - Validation côté frontend avant requêtes sensibles
            - Vérification périodique de la session
            - Déconnexion automatique si jeton invalide
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Jeton valide"),
        @ApiResponse(responseCode = "401", description = "Jeton invalide ou expiré")
    })
    public ResponseEntity<AuthResponse> validateToken(
            @RequestParam 
            @Parameter(description = "Jeton JWT à valider", required = true)
            String token,
            HttpServletRequest request) {
        
        String clientIp = getClientIp(request);
        logger.info("🔍 VALIDATION TOKEN - IP: {} - Token: {}...", 
                   clientIp, token.substring(0, Math.min(10, token.length())));
        
        try {
            AuthResponse response = authService.validateToken(token);
            
            if (response.isSuccess()) {
                logger.info("✅ TOKEN VALIDE - IP: {}", clientIp);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("❌ TOKEN INVALIDE - Raison: {} - IP: {}", 
                           response.getMessage(), clientIp);
                return ResponseEntity.status(401).body(response);
            }
            
        } catch (Exception e) {
            logger.error("💥 ERREUR VALIDATION TOKEN - Erreur: {} - IP: {}", 
                        e.getMessage(), clientIp);
            return ResponseEntity.status(500).body(
                AuthResponse.failure("Erreur lors de la validation")
            );
        }
    }

    /**
     * Extraction de l'adresse IP du client
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
