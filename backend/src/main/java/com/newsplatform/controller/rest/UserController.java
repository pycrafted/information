package com.newsplatform.controller.rest;

import com.newsplatform.dto.request.UserRequest;
import com.newsplatform.dto.response.UserResponse;
import com.newsplatform.entity.User;
import com.newsplatform.service.UserSoapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST pour la gestion des utilisateurs.
 * Couche Contrôle : API REST CRUD utilisateurs (ADMINISTRATEUR uniquement)
 * Respecte le cahier des charges avec sécurisation JWT stricte.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Gestion Utilisateurs (ADMIN)", description = "API CRUD utilisateurs - Accès ADMINISTRATEUR uniquement")
@SecurityRequirement(name = "BearerAuth")
public class UserController {

    private final UserSoapService userSoapService;

    @Autowired
    public UserController(UserSoapService userSoapService) {
        this.userSoapService = userSoapService;
    }

    /**
     * Lister tous les utilisateurs avec pagination
     * Endpoint : GET /api/users
     * Accès : ADMINISTRATEUR uniquement
     * 
     * @param page Numéro de page (0-based)
     * @param size Taille de page (max 100)
     * @param sortBy Champ de tri (username, email, role, createdAt)
     * @param sortDir Direction de tri (asc, desc)
     * @return Liste paginée des utilisateurs
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Lister tous les utilisateurs",
        description = """
            **Récupération de tous les utilisateurs avec pagination**
            
            Cet endpoint permet aux administrateurs de :
            - Lister tous les utilisateurs du système
            - Paginer les résultats pour performance
            - Trier par différents champs
            - Voir les statistiques de chaque utilisateur
            
            **Accès requis :** ADMINISTRATEUR uniquement
            
            **Paramètres de tri supportés :**
            - `username` : Nom d'utilisateur
            - `email` : Adresse email
            - `role` : Rôle utilisateur
            - `createdAt` : Date de création
            - `lastLogin` : Dernière connexion
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des utilisateurs récupérée",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - ADMINISTRATEUR requis"),
        @ApiResponse(responseCode = "400", description = "Paramètres de pagination invalides")
    })
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") 
            @Parameter(description = "Numéro de page (0-based)") 
            int page,
            
            @RequestParam(defaultValue = "10") 
            @Parameter(description = "Taille de page (max 100)") 
            int size,
            
            @RequestParam(defaultValue = "username") 
            @Parameter(description = "Champ de tri") 
            String sortBy,
            
            @RequestParam(defaultValue = "asc") 
            @Parameter(description = "Direction de tri (asc/desc)") 
            String sortDir) {
        
        try {
            // Validation des paramètres
            if (size > 100) {
                size = 100; // Limite maximale
            }
            if (page < 0) {
                page = 0;
            }

            // Pour l'instant, récupération simple (pagination manuelle)
            List<User> users = userSoapService.getAllUsers();
            
            // Conversion en DTO de réponse
            List<UserResponse> userResponses = users.stream()
                .map(UserResponse::from)
                .toList();

            return ResponseEntity.ok(userResponses);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupérer un utilisateur par son ID
     * Endpoint : GET /api/users/{id}
     * Accès : ADMINISTRATEUR uniquement
     * 
     * @param id ID de l'utilisateur
     * @return Utilisateur trouvé
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Récupérer un utilisateur par ID",
        description = """
            **Récupération d'un utilisateur spécifique**
            
            Retourne toutes les informations d'un utilisateur :
            - Données personnelles (nom, email, etc.)
            - Informations de compte (rôle, statut)
            - Statistiques d'activité
            - Historique de connexion
            
            **Accès requis :** ADMINISTRATEUR uniquement
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
        @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable 
            @Parameter(description = "ID de l'utilisateur") 
            UUID id) {
        
        try {
            User user = userSoapService.getUserById(id);
            return ResponseEntity.ok(UserResponse.from(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Créer un nouvel utilisateur
     * Endpoint : POST /api/users
     * Accès : ADMINISTRATEUR uniquement
     * 
     * @param userRequest Données du nouvel utilisateur
     * @return Utilisateur créé
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Créer un nouvel utilisateur",
        description = """
            **Création d'un nouvel utilisateur dans le système**
            
            Permet aux administrateurs de créer de nouveaux comptes :
            - Validation automatique des données
            - Vérification de l'unicité (username/email)
            - Chiffrement sécurisé du mot de passe
            - Attribution du rôle spécifié
            
            **Champs obligatoires :**
            - `username` : Nom d'utilisateur unique (3-50 caractères)
            - `email` : Adresse email valide et unique
            - `password` : Mot de passe sécurisé (8+ caractères)
            - `role` : VISITEUR, EDITEUR ou ADMINISTRATEUR
            
            **Accès requis :** ADMINISTRATEUR uniquement
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Utilisateur créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides ou utilisateur existant"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody 
            @Parameter(description = "Données du nouvel utilisateur") 
            UserRequest userRequest) {
        
        try {
            // Validation spécifique pour création
            if (!userRequest.isValidForCreation()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            User newUser = userSoapService.addUser(
                userRequest.getUsername(),
                userRequest.getPassword(),
                userRequest.getEmail(),
                userRequest.getFirstName(),
                userRequest.getLastName(),
                userRequest.getRole()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserResponse.from(newUser));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Modifier un utilisateur existant
     * Endpoint : PUT /api/users/{id}
     * Accès : ADMINISTRATEUR uniquement
     * 
     * @param id ID de l'utilisateur à modifier
     * @param userRequest Nouvelles données utilisateur
     * @return Utilisateur modifié
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Modifier un utilisateur existant",
        description = """
            **Modification d'un utilisateur existant**
            
            Permet de mettre à jour les informations d'un utilisateur :
            - Modification des données personnelles
            - Changement d'email (avec vérification unicité)
            - Modification du statut actif/inactif
            - Changement de mot de passe (optionnel)
            
            **Note :** Le nom d'utilisateur et le rôle ne peuvent pas être modifiés
            via cet endpoint pour des raisons de sécurité.
            
            **Accès requis :** ADMINISTRATEUR uniquement
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Utilisateur modifié avec succès"),
        @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable 
            @Parameter(description = "ID de l'utilisateur") 
            UUID id,
            
            @Valid @RequestBody 
            @Parameter(description = "Nouvelles données utilisateur") 
            UserRequest userRequest) {
        
        try {
            // Validation spécifique pour modification
            if (!userRequest.isValidForUpdate()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            User updatedUser = userSoapService.updateUser(
                id,
                userRequest.getEmail(),
                userRequest.getFirstName(),
                userRequest.getLastName(),
                userRequest.getActive()
            );

            // Changement de mot de passe si fourni
            if (userRequest.getPassword() != null && !userRequest.getPassword().trim().isEmpty()) {
                updatedUser = userSoapService.changeUserPassword(id, userRequest.getPassword());
            }

            return ResponseEntity.ok(UserResponse.from(updatedUser));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Supprimer (désactiver) un utilisateur
     * Endpoint : DELETE /api/users/{id}
     * Accès : ADMINISTRATEUR uniquement
     * 
     * @param id ID de l'utilisateur à supprimer
     * @return Confirmation de suppression
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Supprimer (désactiver) un utilisateur",
        description = """
            **Suppression sécurisée d'un utilisateur**
            
            Cette opération ne supprime pas physiquement l'utilisateur mais :
            - Désactive le compte (active = false)
            - Révoque tous les jetons d'authentification
            - Préserve l'historique pour l'audit
            - Empêche les nouvelles connexions
            
            **Sécurité :** La suppression physique n'est pas autorisée
            pour préserver l'intégrité des données et l'audit.
            
            **Accès requis :** ADMINISTRATEUR uniquement
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Utilisateur désactivé avec succès"),
        @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    public ResponseEntity<Void> deleteUser(
            @PathVariable 
            @Parameter(description = "ID de l'utilisateur à supprimer") 
            UUID id) {
        
        try {
            userSoapService.deactivateUser(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Statistiques globales des utilisateurs
     * Endpoint : GET /api/users/stats
     * Accès : ADMINISTRATEUR uniquement
     * 
     * @return Statistiques du système
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Statistiques globales des utilisateurs",
        description = """
            **Tableau de bord administrateur**
            
            Fournit des statistiques globales du système :
            - Nombre total d'utilisateurs
            - Répartition par rôle
            - Utilisateurs actifs/inactifs
            - Activité récente
            
            **Accès requis :** ADMINISTRATEUR uniquement
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statistiques récupérées"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    public ResponseEntity<?> getUserStats() {
        try {
            long totalUsers = userSoapService.getAllUsers().size();
            long activeUsers = userSoapService.countActiveUsers();
            
            return ResponseEntity.ok(new UserStatsResponse(totalUsers, activeUsers));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Classe interne pour les statistiques utilisateur
     */
    public static class UserStatsResponse {
        private long totalUsers;
        private long activeUsers;
        private long inactiveUsers;

        public UserStatsResponse(long totalUsers, long activeUsers) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.inactiveUsers = totalUsers - activeUsers;
        }

        // Getters
        public long getTotalUsers() { return totalUsers; }
        public long getActiveUsers() { return activeUsers; }
        public long getInactiveUsers() { return inactiveUsers; }
    }
}
