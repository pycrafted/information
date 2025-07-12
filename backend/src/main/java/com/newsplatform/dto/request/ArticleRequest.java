package com.newsplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * DTO de requête pour la création et modification d'articles.
 * Couche Présentation : Validation et transformation des données d'entrée
 * 
 * Responsabilités :
 * - Validation des données d'entrée
 * - Structure des requêtes API REST
 * - Documentation Swagger automatique
 * - Transformation vers entités métier
 * 
 * @author Équipe Développement
 * @version 2.0 - Validation complète et documentation Swagger
 */
@Schema(
    name = "ArticleRequest",
    description = """
        **Données requises pour créer ou modifier un article**
        
        Structure de données pour les endpoints de création (POST) et 
        modification (PUT) d'articles selon les règles métier DDD.
        
        ### Règles de validation :
        - **Titre** : Obligatoire, 5-200 caractères, unique
        - **Contenu** : Obligatoire, minimum 50 caractères
        - **Catégorie** : ID UUID valide d'une catégorie existante
        - **Résumé** : Optionnel, maximum 500 caractères
        
        ### Génération automatique :
        - **Slug** : Généré automatiquement à partir du titre
        - **Auteur** : Défini automatiquement (utilisateur connecté)
        - **Statut** : DRAFT par défaut à la création
        - **Timestamps** : Gérés automatiquement par le système
        """
)
public class ArticleRequest {

    /**
     * Titre de l'article - obligatoire et unique.
     * Utilisé pour générer automatiquement le slug SEO-friendly.
     */
    @NotBlank(message = "Le titre ne peut pas être vide")
    @Size(
        min = 5, 
        max = 200, 
        message = "Le titre doit contenir entre 5 et 200 caractères"
    )
    @Schema(
        description = """
            **Titre principal de l'article**
            
            - Doit être unique dans la plateforme
            - Utilisé pour générer le slug SEO
            - Affiché dans les listes et détails
            - Indexé pour la recherche
            """,
        example = "Les tendances du développement web en 2024",
        minLength = 5,
        maxLength = 200,
        required = true
    )
    private String title;

    /**
     * Contenu principal de l'article en Markdown.
     * Supporte le formatage riche et les médias intégrés.
     */
    @NotBlank(message = "Le contenu ne peut pas être vide")
    @Size(
        min = 50, 
        message = "Le contenu doit contenir au minimum 50 caractères"
    )
    @Schema(
        description = """
            **Contenu principal de l'article**
            
            - Format : Markdown pour formatage riche
            - Supports : Liens, images, code, tableaux
            - Longueur minimale : 50 caractères
            - Aucune limite maximale (pagination frontend)
            
            ### Exemple de formatage :
            ```markdown
            # Introduction
            
            Cet article traite de **concepts importants** :
            
            - Point 1 avec [lien](https://example.com)
            - Point 2 avec `code inline`
            
            ## Détails techniques
            
            ```java
            public class Example {
                // Code example
            }
            ```
            ```
            """,
        example = """
            # Introduction au développement web moderne
            
            Le développement web évolue constamment avec de **nouvelles technologies** :
            
            - React 18 avec Concurrent Rendering
            - Vue 3 avec Composition API
            - Angular 17 avec Standalone Components
            
            ## Performance et optimisation
            
            ```javascript
            // Exemple d'optimisation
            const lazyComponent = lazy(() => import('./Component'));
            ```
            """,
        minLength = 50,
        required = true
    )
    private String content;

    /**
     * Identifiant de la catégorie parente.
     * Doit correspondre à une catégorie existante et active.
     */
    @NotNull(message = "L'ID de la catégorie est obligatoire")
    @Schema(
        description = """
            **Identifiant UUID de la catégorie**
            
            - Doit correspondre à une catégorie existante
            - Catégorie doit être active (non supprimée)
            - Permet la navigation hiérarchique
            - Utilisé pour le SEO et les URLs
            
            ### Navigation :
            - URLs : `/articles/category/{categorySlug}`
            - Breadcrumb : Accueil > Catégorie > Article
            - Filtrage et recherche par catégorie
            """,
        example = "12345678-1234-1234-1234-123456789012",
        format = "uuid",
        required = true
    )
    private UUID categoryId;

    /**
     * Résumé court de l'article pour les aperçus.
     * Optionnel - généré automatiquement si non fourni.
     */
    @Size(
        max = 500, 
        message = "Le résumé ne peut pas dépasser 500 caractères"
    )
    @Schema(
        description = """
            **Résumé court de l'article (optionnel)**
            
            - Affiché dans les listes d'articles
            - Utilisé pour les meta descriptions SEO
            - Limite : 500 caractères recommandés
            - Si non fourni : généré automatiquement (150 premiers mots)
            
            ### Usage :
            - Cards d'articles sur page d'accueil
            - Résultats de recherche
            - Partage sur réseaux sociaux
            - Meta description pour SEO
            """,
        example = "Découvrez les principales tendances qui façonnent le développement web moderne, des frameworks JavaScript aux techniques d'optimisation des performances.",
        maxLength = 500,
        required = false
    )
    private String summary;

    /**
     * Constructeur par défaut requis pour la désérialisation JSON.
     */
    public ArticleRequest() {
        // Constructeur par défaut pour Jackson
    }

    /**
     * Constructeur complet pour les tests et l'initialisation.
     *
     * @param title Titre de l'article
     * @param content Contenu principal
     * @param categoryId Identifiant de la catégorie
     * @param summary Résumé optionnel
     */
    public ArticleRequest(String title, String content, UUID categoryId, String summary) {
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
        this.summary = summary;
    }

    // ===============================================
    // GETTERS ET SETTERS
    // ===============================================

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    // ===============================================
    // MÉTHODES UTILITAIRES
    // ===============================================

    /**
     * Validation de l'intégrité des données métier.
     * Vérifie les règles métier beyond validation Jakarta.
     *
     * @return true si les données sont cohérentes
     */
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() &&
               content != null && !content.trim().isEmpty() &&
               categoryId != null;
    }

    /**
     * Génère un résumé automatique si non fourni.
     * Utilise les 150 premiers mots du contenu.
     *
     * @return résumé généré ou existant
     */
    public String getOrGenerateSummary() {
        if (summary != null && !summary.trim().isEmpty()) {
            return summary;
        }
        
        if (content == null || content.trim().isEmpty()) {
            return "";
        }
        
        // Suppression du Markdown et extraction des premiers mots
        String plainText = content
            .replaceAll("#+ ", "")  // Headers Markdown
            .replaceAll("\\*\\*(.*?)\\*\\*", "$1")  // Bold
            .replaceAll("\\*(.*?)\\*", "$1")  // Italic
            .replaceAll("\\[([^\\]]+)\\]\\([^\\)]+\\)", "$1")  // Links
            .replaceAll("```[\\s\\S]*?```", "")  // Code blocks
            .replaceAll("`([^`]+)`", "$1")  // Inline code
            .replaceAll("\\n+", " ")  // Multiple newlines
            .trim();
        
        String[] words = plainText.split("\\s+");
        if (words.length <= 25) {
            return plainText;
        }
        
        StringBuilder autoSummary = new StringBuilder();
        for (int i = 0; i < Math.min(25, words.length); i++) {
            if (i > 0) autoSummary.append(" ");
            autoSummary.append(words[i]);
        }
        autoSummary.append("...");
        
        return autoSummary.toString();
    }

    @Override
    public String toString() {
        return String.format(
            "ArticleRequest{title='%s', categoryId=%s, contentLength=%d, hasSummary=%b}",
            title,
            categoryId,
            content != null ? content.length() : 0,
            summary != null && !summary.trim().isEmpty()
        );
    }
}
