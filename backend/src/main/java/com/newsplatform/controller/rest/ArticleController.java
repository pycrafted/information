package com.newsplatform.controller.rest;

import com.newsplatform.dto.request.ArticleRequest;
import com.newsplatform.dto.response.ArticleResponse;
import com.newsplatform.facade.ArticleFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST pour la gestion des articles selon architecture DDD.
 * Couche Présentation : Gestion HTTP uniquement avec documentation Swagger complète
 * 
 * Responsabilités :
 * - Exposition des endpoints REST pour articles
 * - Validation des paramètres HTTP
 * - Transformation des réponses HTTP
 * - Documentation API automatique
 * - Gestion des erreurs HTTP
 * 
 * @author Équipe Développement
 * @version 2.0 - Documentation Swagger complète
 */
@RestController
@RequestMapping("/api/articles")
@Tag(
    name = "Articles", 
    description = """
        **Gestion des articles de la plateforme d'actualités**
        
        Endpoints pour la consultation et gestion des articles selon les rôles utilisateur :
        - **Consultation publique** : Articles publiés sans authentification
        - **Gestion éditoriale** : CRUD complet pour éditeurs (authentification requise)
        - **Administration** : Gestion avancée pour administrateurs
        
        **Formats supportés :** JSON (défaut), XML (via header Accept)
        """
)
public class ArticleController {
    
    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);
    
    private final ArticleFacade articleFacade;
    
    @Autowired
    public ArticleController(ArticleFacade articleFacade) {
        this.articleFacade = articleFacade;
    }
    
    // ===============================================
    // ENDPOINTS PUBLICS (SANS AUTHENTIFICATION)
    // ===============================================
    
    /**
     * Récupère les 10 derniers articles publiés pour la page d'accueil.
     * Endpoint public optimisé pour les performances.
     */
    @GetMapping("/recent")
    @Operation(
        summary = "Récupère les derniers articles publiés",
        description = """
            **Endpoint public** - Récupère les 10 derniers articles publiés pour l'affichage de la page d'accueil.
            
            ### Caractéristiques :
            - ✅ **Public** : Aucune authentification requise
            - ⚡ **Optimisé** : Limite fixe de 10 articles pour performances
            - 🔒 **Sécurisé** : Seuls les articles publiés sont retournés
            - 📊 **Trié** : Par date de publication décroissante
            
            ### Cas d'usage :
            - Page d'accueil du site web
            - Widget "Derniers articles"
            - API publique pour applications mobiles
            """,
        tags = {"Articles", "Public"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Liste des derniers articles récupérée avec succès",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ArticleResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erreur serveur interne",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = """
                    {
                        "error": "Internal Server Error",
                        "message": "Une erreur technique s'est produite",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """)
            )
        )
    })
    public ResponseEntity<List<ArticleResponse>> getRecentArticles() {
        List<ArticleResponse> recentArticles = articleFacade.getRecentArticles();
        return ResponseEntity.ok(recentArticles);
    }

    /**
     * Récupère un article spécifique par son identifiant unique.
     * Endpoint public pour consultation détaillée.
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Récupère un article par son ID",
        description = """
            **Endpoint public** - Récupère les détails complets d'un article spécifique.
            
            ### Caractéristiques :
            - ✅ **Public** : Aucune authentification requise
            - 🔍 **Détaillé** : Contenu complet de l'article
            - 🔒 **Sécurisé** : Seuls les articles publiés sont accessibles
            - 📊 **Enrichi** : Inclut catégorie et informations auteur
            
            ### Cas d'usage :
            - Page de détail d'un article
            - Partage d'article via lien direct
            - Intégration dans applications tierces
            """,
        tags = {"Articles", "Public"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Article trouvé et retourné avec succès",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ArticleResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Format d'ID invalide",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = """
                    {
                        "error": "Bad Request",
                        "message": "Format d'UUID invalide : abc123",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Article non trouvé ou non publié",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = """
                    {
                        "error": "Not Found",
                        "message": "Article non trouvé avec l'id : 12345678-1234-1234-1234-123456789012",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """)
            )
        )
    })
    public ResponseEntity<ArticleResponse> getArticleById(
        @Parameter(
            description = "Identifiant unique de l'article (UUID)",
            required = true,
            example = "12345678-1234-1234-1234-123456789012",
            schema = @Schema(type = "string", format = "uuid")
        )
        @PathVariable UUID id
    ) {
        logger.info("🔍 Requête GET article par ID - ID: {}", id);
        
        ArticleResponse article = articleFacade.getArticleById(id);
        
        logger.debug("✅ Article trouvé et retourné - ID: {}, Titre: '{}'", 
                    id, article.getTitle().substring(0, Math.min(article.getTitle().length(), 50)) + "...");
        
        return ResponseEntity.ok(article);
    }

    /**
     * Récupère les articles publiés avec pagination pour navigation.
     * Endpoint public optimisé pour grandes collections.
     */
    @GetMapping("/published")
    @Operation(
        summary = "Récupère les articles publiés avec pagination",
        description = """
            **Endpoint public** - Récupère tous les articles publiés avec support de pagination avancée.
            
            ### Caractéristiques :
            - ✅ **Public** : Aucune authentification requise
            - 📄 **Paginé** : Support pagination complète (page, size, sort)
            - 🔒 **Sécurisé** : Seuls les articles publiés sont retournés
            - ⚡ **Optimisé** : Requêtes avec JOIN FETCH pour performances
            - 📊 **Flexible** : Tri personnalisable
            
            ### Paramètres de pagination :
            - `page` : Numéro de page (0-based, défaut: 0)
            - `size` : Taille de page (défaut: 5, max: 100)
            - `sort` : Critère de tri (défaut: publishedAt,desc)
            
            ### Exemples de tri :
            - `publishedAt,desc` : Plus récents en premier
            - `title,asc` : Alphabétique par titre
            - `updatedAt,desc` : Dernières modifications
            """,
        tags = {"Articles", "Public", "Pagination"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Page d'articles récupérée avec succès",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Paramètres de pagination invalides",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = """
                    {
                        "error": "Bad Request",
                        "message": "La taille de page ne peut pas dépasser 100 éléments",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """)
            )
        )
    })
    public ResponseEntity<Page<ArticleResponse>> getPaginatedPublishedArticles(
        @Parameter(
            description = "Configuration de pagination et tri",
            schema = @Schema(implementation = Pageable.class)
        )
        @PageableDefault(size = 5, sort = "publishedAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        Page<ArticleResponse> paginatedArticles = articleFacade.getPaginatedPublishedArticles(pageable);
        return ResponseEntity.ok(paginatedArticles);
    }

    /**
     * Récupère les articles d'une catégorie spécifique avec pagination.
     * Endpoint public pour navigation par catégorie.
     */
    @GetMapping("/category/{categorySlug}")
    @Operation(
        summary = "Récupère les articles d'une catégorie",
        description = """
            **Endpoint public** - Récupère tous les articles publiés d'une catégorie spécifique.
            
            ### Caractéristiques :
            - ✅ **Public** : Aucune authentification requise
            - 📄 **Paginé** : Support pagination complète
            - 🏷️ **SEO-friendly** : Utilise des slugs au lieu d'IDs
            - 🔒 **Sécurisé** : Seuls les articles publiés sont retournés
            - 📊 **Enrichi** : Inclut informations de la catégorie
            
            ### Navigation :
            - Idéal pour pages de catégorie
            - Support de la hiérarchie de catégories
            - Breadcrumb navigation possible
            
            ### URL Examples :
            - `/api/articles/category/technologie`
            - `/api/articles/category/sport`
            - `/api/articles/category/developpement-web`
            """,
        tags = {"Articles", "Public", "Catégories"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Articles de la catégorie récupérés avec succès",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Catégorie non trouvée",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = """
                    {
                        "error": "Not Found",
                        "message": "Catégorie non trouvée avec le slug : technologie-inexistante",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Slug de catégorie invalide",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = """
                    {
                        "error": "Bad Request",
                        "message": "Le slug de catégorie ne peut pas être vide",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """)
            )
        )
    })
    public ResponseEntity<Page<ArticleResponse>> getArticlesByCategory(
        @Parameter(
            description = "Slug unique de la catégorie (format SEO-friendly)",
            required = true,
            example = "technologie",
            schema = @Schema(
                type = "string", 
                pattern = "^[a-z0-9-]+$",
                minLength = 1,
                maxLength = 100
            )
        )
        @PathVariable String categorySlug,
        
        @Parameter(
            description = "Configuration de pagination et tri pour les articles de la catégorie",
            schema = @Schema(implementation = Pageable.class)
        )
        @PageableDefault(size = 5, sort = "publishedAt", direction = Sort.Direction.DESC) 
        Pageable pageable
    ) {
        Page<ArticleResponse> page = articleFacade.getPublishedArticlesByCategory(categorySlug, pageable);
        return ResponseEntity.ok(page);
    }

    // ===============================================
    // ENDPOINTS UTILISATEUR CONNECTÉ
    // ===============================================
    
    /**
     * Récupère les articles de l'utilisateur connecté avec pagination.
     * Endpoint sécurisé pour les éditeurs pour voir leurs propres articles.
     */
    @GetMapping("/my-articles")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(
        summary = "Récupère mes articles",
        description = """
            **Endpoint sécurisé** - Récupère tous les articles de l'utilisateur connecté.
            
            ### Authentification requise :
            - 🔐 **JWT Bearer Token** obligatoire
            - 👤 **Rôles autorisés** : EDITEUR, ADMINISTRATEUR
            
            ### Caractéristiques :
            - 📄 **Paginé** : Support pagination complète
            - 📊 **Tous statuts** : DRAFT, PUBLISHED, ARCHIVED
            - 👤 **Personnel** : Seuls vos propres articles
            """,
        tags = {"Articles", "Personnel"},
        security = @SecurityRequirement(name = "BearerAuth")
    )
    public ResponseEntity<Page<ArticleResponse>> getMyArticles(
        @Parameter(
            description = "Configuration de pagination et tri",
            schema = @Schema(implementation = Pageable.class)
        )
        @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        Page<ArticleResponse> myArticles = articleFacade.getMyArticles(pageable);
        return ResponseEntity.ok(myArticles);
    }
    
    /**
     * Récupère les brouillons de l'utilisateur connecté avec pagination.
     * Endpoint sécurisé pour les éditeurs pour voir leurs brouillons.
     */
    @GetMapping("/my-drafts")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(
        summary = "Récupère mes brouillons",
        description = """
            **Endpoint sécurisé** - Récupère tous les brouillons de l'utilisateur connecté.
            
            ### Authentification requise :
            - 🔐 **JWT Bearer Token** obligatoire
            - 👤 **Rôles autorisés** : EDITEUR, ADMINISTRATEUR
            
            ### Caractéristiques :
            - 📄 **Paginé** : Support pagination complète
            - 📝 **Brouillons uniquement** : Statut DRAFT
            - 👤 **Personnel** : Seuls vos propres brouillons
            """,
        tags = {"Articles", "Personnel"},
        security = @SecurityRequirement(name = "BearerAuth")
    )
    public ResponseEntity<Page<ArticleResponse>> getMyDrafts(
        @Parameter(
            description = "Configuration de pagination et tri",
            schema = @Schema(implementation = Pageable.class)
        )
        @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        Page<ArticleResponse> myDrafts = articleFacade.getMyDrafts(pageable);
        return ResponseEntity.ok(myDrafts);
    }

    // ===============================================
    // ENDPOINTS SÉCURISÉS (ÉDITEURS + ADMINS)
    // ===============================================
    
    /**
     * Crée un nouvel article en brouillon.
     * Endpoint sécurisé réservé aux éditeurs et administrateurs.
     */
    @PostMapping
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(
        summary = "Crée un nouvel article",
        description = """
            **Endpoint sécurisé** - Crée un nouvel article en brouillon.
            
            ### Authentification requise :
            - 🔐 **JWT Bearer Token** obligatoire
            - 👤 **Rôles autorisés** : EDITEUR, ADMINISTRATEUR
            
            ### Caractéristiques :
            - 📝 **Brouillon par défaut** : Article créé avec statut DRAFT
            - ✅ **Validation complète** : Titre, contenu, catégorie
            - 🏷️ **Slug automatique** : Généré à partir du titre
            - 👤 **Auteur automatique** : Utilisateur connecté
            
            ### Workflow :
            1. Création en brouillon (DRAFT)
            2. Modification possible via PUT
            3. Publication via POST /publish
            4. Archivage via POST /archive
            """,
        tags = {"Articles", "Éditorial", "CRUD"},
        security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Article créé avec succès",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ArticleResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Données de création invalides",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = """
                    {
                        "error": "Bad Request",
                        "message": "Le titre ne peut pas être vide",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentification requise",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = """
                    {
                        "error": "Unauthorized",
                        "message": "Token JWT manquant ou invalide",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Accès interdit - Rôle insuffisant",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = """
                    {
                        "error": "Forbidden",
                        "message": "Rôle EDITEUR ou ADMINISTRATEUR requis",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """)
            )
        )
    })
    public ResponseEntity<ArticleResponse> createArticle(
        @Parameter(
            description = "Données de l'article à créer",
            required = true,
            schema = @Schema(implementation = ArticleRequest.class)
        )
        @Valid @RequestBody ArticleRequest articleRequest
    ) {
        logger.info("📝 Requête POST création article - Titre: '{}'", 
                   articleRequest.getTitle() != null ? 
                   articleRequest.getTitle().substring(0, Math.min(articleRequest.getTitle().length(), 50)) + "..." : "null");
        
        ArticleResponse createdArticle = articleFacade.createArticle(articleRequest);
        
        logger.info("✅ Article créé avec succès - ID: {}, Titre: '{}'", 
                   createdArticle.getId(), 
                   createdArticle.getTitle().substring(0, Math.min(createdArticle.getTitle().length(), 50)) + "...");
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdArticle);
    }
    
    /**
     * Met à jour un article existant.
     * Endpoint sécurisé avec validation des droits d'auteur.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(
        summary = "Met à jour un article existant",
        description = """
            **Endpoint sécurisé** - Met à jour un article existant avec validation des droits.
            
            ### Authentification requise :
            - 🔐 **JWT Bearer Token** obligatoire
            - 👤 **Rôles autorisés** : EDITEUR (propre article), ADMINISTRATEUR (tous)
            
            ### Autorisations :
            - **EDITEUR** : Peut modifier uniquement ses propres articles
            - **ADMINISTRATEUR** : Peut modifier tous les articles
            
            ### Caractéristiques :
            - ✅ **Validation complète** : Nouveau titre, contenu, catégorie
            - 🏷️ **Slug mis à jour** : Regeneré si titre modifié
            - 📊 **Statut préservé** : Le statut de publication est conservé
            - ⏰ **Timestamps** : updatedAt automatiquement mis à jour
            
            ### Restrictions :
            - Articles archivés : modification limitée
            - Validation d'unicité des slugs
            """,
        tags = {"Articles", "Éditorial", "CRUD"},
        security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Article mis à jour avec succès",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ArticleResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Données de mise à jour invalides",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = """
                    {
                        "error": "Bad Request",
                        "message": "Un article avec ce titre (slug) existe déjà",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Droits insuffisants sur cet article",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = """
                    {
                        "error": "Forbidden",
                        "message": "Vous n'êtes pas autorisé à modifier cet article",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Article non trouvé",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = """
                    {
                        "error": "Not Found",
                        "message": "Article non trouvé avec l'ID : 12345678-1234-1234-1234-123456789012",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """)
            )
        )
    })
    public ResponseEntity<ArticleResponse> updateArticle(
        @Parameter(
            description = "Identifiant unique de l'article à modifier",
            required = true,
            example = "12345678-1234-1234-1234-123456789012",
            schema = @Schema(type = "string", format = "uuid")
        )
        @PathVariable UUID id,
        
        @Parameter(
            description = "Nouvelles données de l'article",
            required = true,
            schema = @Schema(implementation = ArticleRequest.class)
        )
        @Valid @RequestBody ArticleRequest articleRequest
    ) {
        ArticleResponse updatedArticle = articleFacade.updateArticle(id, articleRequest);
        return ResponseEntity.ok(updatedArticle);
    }
    
    /**
     * Publie un article en brouillon.
     * Endpoint sécurisé pour la gestion du workflow éditorial.
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(
        summary = "Publie un article en brouillon",
        description = """
            **Endpoint sécurisé** - Change le statut d'un article de DRAFT à PUBLISHED.
            
            ### Authentification requise :
            - 🔐 **JWT Bearer Token** obligatoire
            - 👤 **Rôles autorisés** : EDITEUR (propre article), ADMINISTRATEUR (tous)
            
            ### Workflow de publication :
            1. **Validation** : Article doit être en brouillon (DRAFT)
            2. **Vérification** : Contenu complet et valide
            3. **Publication** : Statut → PUBLISHED + publishedAt → maintenant
            4. **Notification** : Article devient visible publiquement
            
            ### Caractéristiques :
            - ⏰ **Timestamp automatique** : publishedAt défini à l'instant de publication
            - 🌐 **Visibilité publique** : Article accessible via endpoints publics
            - 🔄 **Réversible** : Peut être archivé ou remis en brouillon
            
            ### Prérequis :
            - Article en statut DRAFT uniquement
            - Contenu validé et complet
            """,
        tags = {"Articles", "Éditorial", "Workflow"},
        security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Article publié avec succès",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ArticleResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Article ne peut pas être publié",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = """
                    {
                        "error": "Bad Request",
                        "message": "Seuls les articles en brouillon peuvent être publiés",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Droits insuffisants pour publier",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = """
                    {
                        "error": "Forbidden",
                        "message": "Vous n'êtes pas autorisé à publier cet article",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """)
            )
        )
    })
    public ResponseEntity<ArticleResponse> publishArticle(
        @Parameter(
            description = "Identifiant unique de l'article à publier",
            required = true,
            example = "12345678-1234-1234-1234-123456789012",
            schema = @Schema(type = "string", format = "uuid")
        )
        @PathVariable UUID id
    ) {
        ArticleResponse publishedArticle = articleFacade.publishArticle(id);
        return ResponseEntity.ok(publishedArticle);
    }
    
    /**
     * Archive un article publié.
     * Endpoint sécurisé pour la gestion du cycle de vie des articles.
     */
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(
        summary = "Archive un article publié",
        description = """
            **Endpoint sécurisé** - Change le statut d'un article de PUBLISHED à ARCHIVED.
            
            ### Authentification requise :
            - 🔐 **JWT Bearer Token** obligatoire
            - 👤 **Rôles autorisés** : EDITEUR (propre article), ADMINISTRATEUR (tous)
            
            ### Workflow d'archivage :
            1. **Validation** : Article doit être publié (PUBLISHED)
            2. **Archivage** : Statut → ARCHIVED
            3. **Retrait** : Article retiré des endpoints publics
            4. **Conservation** : Données préservées pour historique
            
            ### Caractéristiques :
            - 🔒 **Invisibilité publique** : Plus accessible via endpoints publics
            - 📊 **Historique préservé** : Données conservées pour administration
            - 🔄 **Réversible** : Peut être republié si nécessaire
            - ⏰ **Timestamp** : updatedAt mis à jour
            
            ### Cas d'usage :
            - Contenu obsolète ou périmé
            - Article temporairement retiré
            - Maintenance éditoriale
            """,
        tags = {"Articles", "Éditorial", "Workflow"},
        security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Article archivé avec succès",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ArticleResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Article ne peut pas être archivé",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = """
                    {
                        "error": "Bad Request",
                        "message": "Seuls les articles publiés peuvent être archivés",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Droits insuffisants pour archiver",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = """
                    {
                        "error": "Forbidden",
                        "message": "Vous n'êtes pas autorisé à archiver cet article",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """)
            )
        )
    })
    public ResponseEntity<ArticleResponse> archiveArticle(
        @Parameter(
            description = "Identifiant unique de l'article à archiver",
            required = true,
            example = "12345678-1234-1234-1234-123456789012",
            schema = @Schema(type = "string", format = "uuid")
        )
        @PathVariable UUID id
    ) {
        ArticleResponse archivedArticle = articleFacade.archiveArticle(id);
        return ResponseEntity.ok(archivedArticle);
    }
    
    /**
     * Supprime définitivement un article.
     * Endpoint sécurisé réservé aux administrateurs uniquement.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Supprime définitivement un article",
        description = """
            **Endpoint ultra-sécurisé** - Suppression définitive d'un article.
            
            ### Authentification requise :
            - 🔐 **JWT Bearer Token** obligatoire
            - 👤 **Rôle requis** : ADMINISTRATEUR uniquement
            
            ### ⚠️ Attention - Action irréversible :
            - 🗑️ **Suppression définitive** : Aucune récupération possible
            - 💾 **Données perdues** : Contenu, métadonnées, historique
            - 🔗 **Liens brisés** : URLs publiques deviennent inaccessibles
            
            ### Recommandations :
            - **Préférer l'archivage** pour la plupart des cas
            - **Utilisez uniquement** pour spam ou contenu illégal
            - **Vérifiez deux fois** avant suppression
            
            ### Auditabilité :
            - Action loggée dans les journaux d'audit
            - Notification administrateur
            """,
        tags = {"Articles", "Administration", "Suppression"},
        security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Article supprimé avec succès",
            content = @Content()
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Accès interdit - Administrateur requis",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = """
                    {
                        "error": "Forbidden",
                        "message": "Rôle ADMINISTRATEUR requis pour supprimer un article",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Article non trouvé",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = """
                    {
                        "error": "Not Found",
                        "message": "Article non trouvé avec l'ID : 12345678-1234-1234-1234-123456789012",
                        "timestamp": "2024-01-15T10:30:00Z"
                    }
                    """)
            )
        )
    })
    public ResponseEntity<Void> deleteArticle(
        @Parameter(
            description = "Identifiant unique de l'article à supprimer définitivement",
            required = true,
            example = "12345678-1234-1234-1234-123456789012",
            schema = @Schema(type = "string", format = "uuid")
        )
        @PathVariable UUID id
    ) {
        articleFacade.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gestion centralisée des exceptions pour les UUID malformés.
     * Assure une réponse cohérente pour les erreurs de format.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @Operation(hidden = true) // Masquer dans la documentation Swagger
    public ResponseEntity<String> handleInvalidUUID(IllegalArgumentException ex) {
        if (ex.getMessage() != null && ex.getMessage().contains("Invalid UUID")) {
            return ResponseEntity.badRequest()
                .body("Format d'UUID invalide : " + ex.getMessage());
        }
        return ResponseEntity.badRequest()
            .body("Paramètre invalide : " + ex.getMessage());
    }
}
