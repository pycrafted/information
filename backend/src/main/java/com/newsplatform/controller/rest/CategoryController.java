package com.newsplatform.controller.rest;

import com.newsplatform.dto.request.CategoryRequest;
import com.newsplatform.dto.response.CategoryResponse;
import com.newsplatform.facade.CategoryFacade;
import com.newsplatform.repository.CategoryRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST pour la gestion des catégories.
 * Couche Présentation : Points d'entrée HTTP pour l'API des catégories.
 * 
 * Responsabilités :
 * - Endpoints CRUD complets pour les catégories
 * - Gestion de la hiérarchie des catégories
 * - Sécurisation par JWT et autorisation par rôles
 * - Validation des données et gestion des erreurs
 * - Documentation API Swagger
 * 
 * Points d'accès :
 * - Endpoints publics : consultation des catégories
 * - Endpoints privés : gestion CRUD (EDITEUR+)
 * - Endpoints administratifs : gestion avancée (ADMIN)
 * 
 * @author Équipe Développement
 * @version 1.0
 * @since 2025
 */
@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Categories", description = "Gestion des catégories d'articles")
public class CategoryController {
    
    private final CategoryFacade categoryFacade;
    private final CategoryRepository categoryRepository; // Pour les tests de diagnostic
    
    @Autowired
    public CategoryController(CategoryFacade categoryFacade, CategoryRepository categoryRepository) {
        this.categoryFacade = categoryFacade;
        this.categoryRepository = categoryRepository;
    }
    
    // ===============================================
    // ENDPOINTS PUBLICS (CONSULTATION)
    // ===============================================
    
    /**
     * Récupère toutes les catégories racines pour navigation.
     * Endpoint public accessible à tous les visiteurs.
     * 
     * @return liste des catégories racines avec leurs enfants
     */
    @Operation(summary = "Récupérer les catégories racines", 
               description = "Récupère toutes les catégories racines avec leur hiérarchie pour la navigation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des catégories racines récupérée avec succès",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class)))
    })
    @GetMapping("/roots")
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        try {
            List<CategoryResponse> categories = categoryFacade.getRootCategoriesWithChildren();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            // Log de l'erreur (optionnel : utiliser un logger)
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
    
    /**
     * Récupère toutes les catégories avec pagination.
     * Endpoint public pour listing complet.
     * 
     * @param pageable configuration de pagination (par défaut : 20 par page, tri par nom)
     * @return page de catégories
     */
    @Operation(summary = "Lister toutes les catégories", 
               description = "Récupère toutes les catégories avec pagination et tri")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Page de catégories récupérée avec succès",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            @Parameter(description = "Configuration de pagination et tri") @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) 
            Pageable pageable) {
        Page<CategoryResponse> categories = categoryFacade.getAllCategories(pageable);
        return ResponseEntity.ok(categories);
    }
    
    /**
     * Récupère une catégorie par son slug.
     * Endpoint public pour affichage détaillé.
     * 
     * @param slug slug unique de la catégorie
     * @return détails de la catégorie
     */
    @Operation(summary = "Récupérer une catégorie par slug", 
               description = "Récupère une catégorie par son slug SEO-friendly")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Catégorie trouvée",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class))),
        @ApiResponse(responseCode = "404", description = "Catégorie non trouvée")
    })
    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(
            @Parameter(description = "Slug unique de la catégorie") @PathVariable String slug) {
        CategoryResponse category = categoryFacade.getCategoryBySlug(slug);
        return ResponseEntity.ok(category);
    }
    
    /**
     * Récupère une catégorie par son ID.
     * Endpoint public pour accès direct.
     * 
     * @param id identifiant unique de la catégorie
     * @return détails de la catégorie
     */
    @Operation(summary = "Récupérer une catégorie par ID", 
               description = "Récupère les détails complets d'une catégorie par son identifiant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Catégorie trouvée",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class))),
        @ApiResponse(responseCode = "404", description = "Catégorie non trouvée")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "Identifiant unique de la catégorie") @PathVariable UUID id) {
        CategoryResponse category = categoryFacade.getCategoryById(id);
        return ResponseEntity.ok(category);
    }
    
    /**
     * Récupère les sous-catégories d'une catégorie.
     * Endpoint public pour navigation hiérarchique.
     * 
     * @param id ID de la catégorie parente
     * @return liste des sous-catégories
     */
    @Operation(summary = "Récupérer les sous-catégories", 
               description = "Récupère toutes les sous-catégories directes d'une catégorie parente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des sous-catégories récupérée",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class))),
        @ApiResponse(responseCode = "404", description = "Catégorie parente non trouvée")
    })
    @GetMapping("/{id}/children")
    public ResponseEntity<List<CategoryResponse>> getSubCategories(
            @Parameter(description = "ID de la catégorie parente") @PathVariable UUID id) {
        List<CategoryResponse> subCategories = categoryFacade.getSubCategories(id);
        return ResponseEntity.ok(subCategories);
    }
    
    // ===============================================
    // ENDPOINT DE TEST POUR DIAGNOSTIQUER
    // ===============================================
    
    /**
     * Endpoint de test simple pour diagnostiquer les problèmes.
     */
    @GetMapping("/test")
    public ResponseEntity<String> testCategories() {
        try {
            // Test direct du repository sans service ni facade
            long count = categoryRepository.count();
            return ResponseEntity.ok("✅ Test repository réussi - " + count + " catégories trouvées");
        } catch (Exception e) {
            return ResponseEntity.ok("❌ Erreur repository: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
    
    /**
     * Test encore plus simple - juste vérifier que le repository est injecté
     */
    @GetMapping("/test2")
    public ResponseEntity<String> testBasic() {
        try {
            if (categoryRepository == null) {
                return ResponseEntity.ok("❌ Repository null");
            }
            return ResponseEntity.ok("✅ Repository injecté correctement");
        } catch (Exception e) {
            return ResponseEntity.ok("❌ Erreur: " + e.getMessage());
        }
    }
    
    // ===============================================
    // ENDPOINTS PRIVÉS (GESTION ÉDITEUR)
    // ===============================================
    
    /**
     * Crée une nouvelle catégorie racine.
     * Accès : EDITEUR et ADMIN uniquement.
     * 
     * @param request données de la nouvelle catégorie
     * @return catégorie créée avec statut 201
     */
    @Operation(summary = "Créer une catégorie racine", 
               description = "Crée une nouvelle catégorie racine. Accès réservé aux éditeurs et administrateurs.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Catégorie créée avec succès",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class))),
        @ApiResponse(responseCode = "400", description = "Données de requête invalides"),
        @ApiResponse(responseCode = "401", description = "Authentification requise"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - rôle insuffisant")
    })
    @PostMapping
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse category = categoryFacade.createRootCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }
    
    /**
     * Crée une nouvelle sous-catégorie.
     * Accès : EDITEUR et ADMIN uniquement.
     * 
     * @param parentId ID de la catégorie parente
     * @param request données de la nouvelle sous-catégorie
     * @return sous-catégorie créée avec statut 201
     */
    @Operation(summary = "Créer une sous-catégorie", 
               description = "Crée une nouvelle sous-catégorie sous une catégorie parente. Accès réservé aux éditeurs et administrateurs.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Sous-catégorie créée avec succès",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class))),
        @ApiResponse(responseCode = "400", description = "Données de requête invalides"),
        @ApiResponse(responseCode = "401", description = "Authentification requise"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - rôle insuffisant"),
        @ApiResponse(responseCode = "404", description = "Catégorie parente non trouvée")
    })
    @PostMapping("/{parentId}/subcategories")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createSubCategory(
            @Parameter(description = "ID de la catégorie parente") @PathVariable UUID parentId, 
            @Valid @RequestBody CategoryRequest request) {
        // Définir le parentId dans la requête
        request.setParentId(parentId);
        CategoryResponse category = categoryFacade.createSubCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }
    
    /**
     * Met à jour une catégorie existante.
     * Accès : EDITEUR et ADMIN uniquement.
     * 
     * @param id ID de la catégorie à modifier
     * @param request nouvelles données de la catégorie
     * @return catégorie mise à jour
     */
    @Operation(summary = "Mettre à jour une catégorie", 
               description = "Met à jour les informations d'une catégorie existante. Accès réservé aux éditeurs et administrateurs.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Catégorie mise à jour avec succès",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class))),
        @ApiResponse(responseCode = "400", description = "Données de requête invalides"),
        @ApiResponse(responseCode = "401", description = "Authentification requise"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - rôle insuffisant"),
        @ApiResponse(responseCode = "404", description = "Catégorie non trouvée")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "ID de la catégorie à modifier") @PathVariable UUID id, 
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse category = categoryFacade.updateCategory(id, request);
        return ResponseEntity.ok(category);
    }
    
    /**
     * Déplace une catégorie vers un nouveau parent.
     * Accès : EDITEUR et ADMIN uniquement.
     * 
     * @param id ID de la catégorie à déplacer
     * @param newParentId ID du nouveau parent (peut être null pour racine)
     * @return catégorie déplacée
     */
    @Operation(summary = "Déplacer une catégorie", 
               description = "Déplace une catégorie vers un nouveau parent ou vers la racine. Accès réservé aux éditeurs et administrateurs.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Catégorie déplacée avec succès",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class))),
        @ApiResponse(responseCode = "400", description = "Déplacement invalide (cycle détecté)"),
        @ApiResponse(responseCode = "401", description = "Authentification requise"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - rôle insuffisant"),
        @ApiResponse(responseCode = "404", description = "Catégorie non trouvée")
    })
    @PatchMapping("/{id}/move")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> moveCategory(
            @Parameter(description = "ID de la catégorie à déplacer") @PathVariable UUID id, 
            @Parameter(description = "ID du nouveau parent (null pour racine)") @RequestParam(required = false) UUID newParentId) {
        CategoryResponse category = categoryFacade.moveCategory(id, newParentId);
        return ResponseEntity.ok(category);
    }
    
    // ===============================================
    // ENDPOINTS ADMINISTRATIFS (ADMINISTRATEUR UNIQUEMENT)
    // ===============================================
    
    /**
     * Supprime une catégorie.
     * Accès : ADMINISTRATEUR uniquement.
     * Règle métier : la catégorie ne doit contenir ni articles ni sous-catégories.
     * 
     * @param id ID de la catégorie à supprimer
     * @return confirmation de suppression
     */
    @Operation(summary = "Supprimer une catégorie", 
               description = "Supprime une catégorie vide (sans articles ni sous-catégories). Accès réservé aux administrateurs.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Catégorie supprimée avec succès"),
        @ApiResponse(responseCode = "400", description = "Suppression impossible - catégorie non vide"),
        @ApiResponse(responseCode = "401", description = "Authentification requise"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - rôle administrateur requis"),
        @ApiResponse(responseCode = "404", description = "Catégorie non trouvée")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@Parameter(description = "ID de la catégorie à supprimer") @PathVariable UUID id) {
        categoryFacade.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
    

}
