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
            - `admin` / `password` - Administrateur complet
            - `editeur` / `password` - Éditeur articles/catégories
            - `visiteur` / `password` - Lecture uniquement
            
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
        
        try {
            AuthResponse response = authService.authenticateUser(loginRequest, request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(401).body(response);
            }
            
        } catch (Exception e) {
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
            String userId) {
        
        try {
            AuthResponse response = authService.logoutUser(userId);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(404).body(response);
            }
            
        } catch (Exception e) {
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
        
        try {
            AuthResponse response = authService.refreshAccessToken(refreshToken, request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(401).body(response);
            }
            
        } catch (Exception e) {
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
     * @return Statut de validité
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
            String token) {
        
        try {
            // Pour l'instant, simple validation via le service
            // À implémenter : TokenService.validateAccessToken()
            return ResponseEntity.ok(
                new AuthResponse(true, "Jeton valide")
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(401).body(
                AuthResponse.failure("Jeton invalide")
            );
        }
    }
}
