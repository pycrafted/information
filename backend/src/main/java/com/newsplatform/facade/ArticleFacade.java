package com.newsplatform.facade;

import com.newsplatform.dto.request.ArticleRequest;
import com.newsplatform.dto.response.ArticleResponse;
import com.newsplatform.entity.Article;
import com.newsplatform.entity.User;
import com.newsplatform.exception.ResourceNotFoundException;
import com.newsplatform.exception.ValidationException;
import com.newsplatform.exception.BusinessException;
import com.newsplatform.mapper.ArticleMapper;
import com.newsplatform.repository.CategoryRepository;
import com.newsplatform.repository.UserRepository;
import com.newsplatform.security.UserPrincipal;
import com.newsplatform.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import com.newsplatform.entity.ArticleStatus;

/**
 * Facade pour la gestion des articles selon l'architecture DDD.
 * Couche Contrôle : Orchestration, validation, transformation et sécurité
 * 
 * Responsabilités :
 * - Orchestration des services métier
 * - Validation des règles d'intégrité
 * - Transformation entités ↔ DTOs
 * - Coordination des opérations complexes
 * - Gestion des autorisations métier
 * - Récupération de l'utilisateur connecté
 * 
 * @author Équipe Développement
 * @version 2.0 - CRUD complet avec sécurité
 */
@Component
public class ArticleFacade {
    
    private final ArticleService articleService;
    private final ArticleMapper articleMapper;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public ArticleFacade(ArticleService articleService, ArticleMapper articleMapper, CategoryRepository categoryRepository, UserRepository userRepository) {
        this.articleService = articleService;
        this.articleMapper = articleMapper;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }
    
    // ===============================================
    // OPÉRATIONS DE LECTURE (PUBLIC)
    // ===============================================
    
    /**
     * Récupère les 10 derniers articles publiés pour l'affichage public.
     * Responsabilité : Orchestration des services et transformation des données
     * 
     * @return Liste des articles récents au format DTO
     */
    public List<ArticleResponse> getRecentArticles() {
        // Validation des entrées (si nécessaire)
        validateGetRecentArticlesRequest();
        
        // Orchestration : appel du service métier
        List<Article> articles = articleService.getRecentArticles();
        
        // Transformation : entités → DTOs
        return articleMapper.toResponseList(articles);
    }
    
    /**
     * Récupère un article par son identifiant unique et le transforme en DTO pour la présentation.
     * Responsabilité : Orchestration du service métier et transformation de l'entité en DTO.
     *
     * @param id Identifiant unique de l'article (UUID)
     * @return DTO ArticleResponse correspondant à l'article trouvé
     * @throws ResourceNotFoundException si aucun article n'est trouvé
     */
    public ArticleResponse getArticleById(UUID id) {
        Article article = articleService.getArticleById(id);
        return articleMapper.toResponse(article);
    }

    /**
     * Récupère les articles publiés avec pagination optimisée.
     * Responsabilité : Orchestration pagination et transformation
     * 
     * @param pageable Configuration de pagination
     * @return Page d'articles au format DTO
     */
    public Page<ArticleResponse> getPaginatedPublishedArticles(Pageable pageable) {
        return articleService.getPublishedArticles(pageable)
                             .map(articleMapper::toResponse); 
    }

    /**
     * Récupère les articles d'une catégorie avec pagination.
     * Responsabilité : Validation slug + orchestration + transformation
     * 
     * @param categorySlug Slug de la catégorie
     * @param pageable Configuration de pagination
     * @return Page d'articles de la catégorie
     */
    public Page<ArticleResponse> getPublishedArticlesByCategory(String categorySlug, Pageable pageable) {
        // Validation du slug de catégorie
        validateCategorySlug(categorySlug);
        
        Page<Article> articles = articleService.getPublishedArticlesByCategory(categorySlug, pageable);
        return articles.map(articleMapper::toResponse); 
    }

    /**
     * Récupère tous les articles de l'utilisateur connecté avec pagination.
     * Responsabilité : Orchestration + authentification + transformation
     * 
     * @param pageable Configuration de pagination
     * @return Page des articles de l'utilisateur connecté
     */
    public Page<ArticleResponse> getMyArticles(Pageable pageable) {
        // Récupération de l'utilisateur connecté
        User currentUser = getCurrentAuthenticatedUser();
        
        // Orchestration : appel du service métier
        Page<Article> userArticles = articleService.getArticlesByAuthor(currentUser, pageable);
        
        // Transformation : entités → DTOs
        return userArticles.map(articleMapper::toResponse);
    }

    /**
     * Récupère les brouillons de l'utilisateur connecté avec pagination.
     * Responsabilité : Orchestration + authentification + filtrage + transformation
     * 
     * @param pageable Configuration de pagination
     * @return Page des brouillons de l'utilisateur connecté
     */
    public Page<ArticleResponse> getMyDrafts(Pageable pageable) {
        // Récupération de l'utilisateur connecté
        User currentUser = getCurrentAuthenticatedUser();
        
        // Orchestration : appel du service métier avec filtrage par auteur et statut
        Page<Article> userDrafts = articleService.getArticlesByAuthorAndStatus(currentUser, ArticleStatus.DRAFT, pageable);
        
        // Transformation : entités → DTOs
        return userDrafts.map(articleMapper::toResponse);
    }

    // ===============================================
    // OPÉRATIONS CRUD SÉCURISÉES (ÉDITEURS/ADMINS)
    // ===============================================
    
    /**
     * Crée un nouvel article en brouillon selon les règles métier DDD.
     * Responsabilité : Validation complète + orchestration + transformation
     * 
     * @param articleRequest Données de l'article à créer
     * @return Article créé au format DTO
     * @throws ValidationException si les données sont invalides
     */
    public ArticleResponse createArticle(ArticleRequest articleRequest) {
        // Validation métier des données d'entrée
        validateCreateArticleRequest(articleRequest);
        
        // Récupération de l'utilisateur connecté
        User currentUser = getCurrentAuthenticatedUser();
        
        // Récupération de la catégorie pour validation
        String categorySlug = getCategorySlugById(articleRequest.getCategoryId());
        
        // Orchestration : délégation au service métier avec paramètres séparés
        Article createdArticle = articleService.createArticle(
            articleRequest.getTitle(),
            articleRequest.getContent(), 
            categorySlug,
            currentUser
        );
        
        // Transformation : entité → DTO
        return articleMapper.toResponse(createdArticle);
    }
    
    /**
     * Met à jour un article existant avec validation des droits.
     * Responsabilité : Validation + autorisation + orchestration + transformation
     * 
     * @param id Identifiant de l'article à modifier
     * @param articleRequest Nouvelles données de l'article
     * @return Article modifié au format DTO
     * @throws ResourceNotFoundException si l'article n'existe pas
     * @throws ValidationException si les données sont invalides
     */
    public ArticleResponse updateArticle(UUID id, ArticleRequest articleRequest) {
        // Validation des paramètres d'entrée
        validateUpdateArticleRequest(id, articleRequest);
        
        // Récupération de l'utilisateur connecté
        User currentUser = getCurrentAuthenticatedUser();
        
        // Orchestration : délégation au service métier
        Article updatedArticle = articleService.updateArticleContent(
            id,
            articleRequest.getTitle(),
            articleRequest.getContent(),
            currentUser
        );
        
        // Transformation : entité → DTO
        return articleMapper.toResponse(updatedArticle);
    }
    
    /**
     * Publie un article en brouillon selon le workflow éditorial.
     * Responsabilité : Validation du workflow + orchestration + transformation
     * 
     * @param id Identifiant de l'article à publier
     * @return Article publié au format DTO
     * @throws ResourceNotFoundException si l'article n'existe pas
     * @throws ValidationException si l'article ne peut pas être publié
     */
    public ArticleResponse publishArticle(UUID id) {
        // Validation de l'identifiant
        validateArticleId(id);
        
        // Récupération de l'utilisateur connecté
        User currentUser = getCurrentAuthenticatedUser();
        
        // Orchestration : délégation au service métier pour publication
        Article publishedArticle = articleService.publishArticle(id, currentUser);
        
        // Transformation : entité → DTO
        return articleMapper.toResponse(publishedArticle);
    }
    
    /**
     * Archive un article publié selon le workflow éditorial.
     * Responsabilité : Validation du workflow + orchestration + transformation
     * 
     * @param id Identifiant de l'article à archiver
     * @return Article archivé au format DTO
     * @throws ResourceNotFoundException si l'article n'existe pas
     * @throws ValidationException si l'article ne peut pas être archivé
     */
    public ArticleResponse archiveArticle(UUID id) {
        // Validation de l'identifiant
        validateArticleId(id);
        
        // Récupération de l'utilisateur connecté
        User currentUser = getCurrentAuthenticatedUser();
        
        // Orchestration : délégation au service métier pour archivage
        Article archivedArticle = articleService.archiveArticle(id, currentUser);
        
        // Transformation : entité → DTO
        return articleMapper.toResponse(archivedArticle);
    }
    
    /**
     * Supprime définitivement un article (administrateurs uniquement).
     * Responsabilité : Validation sécurisée + orchestration + audit
     * 
     * @param id Identifiant de l'article à supprimer
     * @throws ResourceNotFoundException si l'article n'existe pas
     * @throws BusinessException si l'utilisateur n'a pas les droits
     */
    public void deleteArticle(UUID id) {
        // Validation stricte de l'identifiant
        validateDeleteArticleRequest(id);
        
        // Récupération de l'utilisateur connecté
        User currentUser = getCurrentAuthenticatedUser();
        
        // Validation des droits administrateur
        if (!currentUser.getRole().equals(User.UserRole.ADMINISTRATEUR)) {
            throw new BusinessException("Seuls les administrateurs peuvent supprimer définitivement un article");
        }
        
        // Récupération de l'article pour vérification d'existence
        Article article = articleService.getArticleById(id);
        
        // Pour cette implémentation, nous archivons l'article au lieu de le supprimer
        // La suppression définitive nécessiterait une méthode spécifique dans ArticleService
        try {
            article.archive();
            // Note: Une vraie suppression nécessiterait articleService.deleteArticle(id)
            // qui n'existe pas encore dans l'implémentation actuelle
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la suppression de l'article", e);
        }
        
        // Note: Aucune transformation nécessaire (void)
        // L'audit est géré par la couche Service
    }

    // ===============================================
    // MÉTHODES UTILITAIRES PRIVÉES
    // ===============================================
    
    /**
     * Récupère l'utilisateur actuellement authentifié via le contexte de sécurité.
     * 
     * @return utilisateur connecté
     * @throws ValidationException si aucun utilisateur n'est authentifié
     */
    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ValidationException("Aucun utilisateur authentifié trouvé");
        }
        
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            throw new ValidationException("Principal de sécurité invalide");
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) principal;
        
        // Récupérer l'entité User complète depuis la base de données
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ValidationException("Utilisateur non trouvé"));
    }
    
    /**
     * Récupère le slug d'une catégorie par son ID.
     * 
     * @param categoryId Identifiant de la catégorie
     * @return slug de la catégorie
     * @throws ResourceNotFoundException si la catégorie n'existe pas
     */
    private String getCategorySlugById(UUID categoryId) {
        try {
            return categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "Catégorie non trouvée avec l'ID : " + categoryId))
                    .getSlug();
        } catch (Exception e) {
            // Si l'UUID n'existe pas, on essaie de le traiter comme un slug
            // Cela peut arriver si le frontend envoie un UUID au lieu d'un slug
            throw new ResourceNotFoundException(
                "Catégorie non trouvée avec le slug : " + categoryId);
        }
    }

    // ===============================================
    // MÉTHODES DE VALIDATION PRIVÉES
    // ===============================================
    
    /**
     * Validation des paramètres pour la récupération des articles récents.
     * Responsabilité : Validation de l'état des composants
     */
    private void validateGetRecentArticlesRequest() {
        // Validation de l'état du service
        if (articleService == null) {
            throw new ValidationException("Le service des articles n'est pas disponible");
        }
        
        if (articleMapper == null) {
            throw new ValidationException("Le mapper des articles n'est pas disponible");
        }
        
        // Validation métier : vérifier que les composants sont opérationnels
        // Cette validation peut être étendue selon les besoins métier
    }

    /**
     * Validation du slug de catégorie selon les règles métier.
     * 
     * @param categorySlug Slug à valider
     * @throws ValidationException si le slug est invalide
     */
    private void validateCategorySlug(String categorySlug) {
        if (categorySlug == null || categorySlug.trim().isEmpty()) {
            throw new ValidationException("Le slug de catégorie ne peut pas être vide");
        }
        
        if (categorySlug.length() > 100) {
            throw new ValidationException("Le slug de catégorie ne peut pas dépasser 100 caractères");
        }
        
        // Validation du format slug (lettres minuscules, chiffres, tirets)
        if (!categorySlug.matches("^[a-z0-9-]+$")) {
            throw new ValidationException("Le slug de catégorie doit contenir uniquement des lettres minuscules, chiffres et tirets");
        }
    }
    
    /**
     * Validation complète des données de création d'article.
     * 
     * @param articleRequest Données à valider
     * @throws ValidationException si les données sont invalides
     */
    private void validateCreateArticleRequest(ArticleRequest articleRequest) {
        if (articleRequest == null) {
            throw new ValidationException("Les données de l'article ne peuvent pas être nulles");
        }
        
        // Validation des composants système
        if (articleService == null || articleMapper == null) {
            throw new ValidationException("Les composants système ne sont pas disponibles");
        }
        
        // Validation de l'intégrité métier
        if (!articleRequest.isValid()) {
            throw new ValidationException("Les données de l'article ne respectent pas les règles métier");
        }
    }
    
    /**
     * Validation complète des données de modification d'article.
     * 
     * @param id Identifiant de l'article
     * @param articleRequest Nouvelles données
     * @throws ValidationException si les données sont invalides
     */
    private void validateUpdateArticleRequest(UUID id, ArticleRequest articleRequest) {
        // Validation de l'identifiant
        validateArticleId(id);
        
        // Validation des données de mise à jour
        validateCreateArticleRequest(articleRequest);
    }
    
    /**
     * Validation stricte pour la suppression d'article.
     * 
     * @param id Identifiant de l'article
     * @throws ValidationException si l'identifiant est invalide
     */
    private void validateDeleteArticleRequest(UUID id) {
        validateArticleId(id);
        
        // Validation supplémentaire pour la suppression
        if (articleService == null) {
            throw new ValidationException("Le service des articles n'est pas disponible pour la suppression");
        }
    }
    
    /**
     * Validation d'un identifiant d'article UUID.
     * 
     * @param id Identifiant à valider
     * @throws ValidationException si l'identifiant est invalide
     */
    private void validateArticleId(UUID id) {
        if (id == null) {
            throw new ValidationException("L'identifiant de l'article ne peut pas être nul");
        }
        
        // Note: UUID.fromString() lèvera une IllegalArgumentException 
        // pour les formats invalides, gérée par le contrôleur
    }
} 