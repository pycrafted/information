package com.newsplatform.service;

import com.newsplatform.entity.Article;
import com.newsplatform.entity.ArticleStatus;
import com.newsplatform.entity.Category;
import com.newsplatform.entity.User;
import com.newsplatform.exception.BusinessException;
import com.newsplatform.exception.ResourceNotFoundException;
import com.newsplatform.exception.ValidationException;
import com.newsplatform.repository.ArticleRepository;
import com.newsplatform.repository.CategoryRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service pour la gestion des articles selon les principes DDD.
 * Couche Service : Orchestration de la logique métier et validation
 * 
 * Responsabilités :
 * - Orchestration des opérations CRUD avec validation métier
 * - Gestion des autorisations selon les rôles utilisateur
 * - Coordination entre les repositories
 * - Application des règles métier complexes
 * - Transformation et validation des données
 * 
 * @author Équipe Développement
 * @version 2.0 - Refactoring DDD complet
 */
@Service
@Transactional
public class ArticleService {
    
    private static final Logger logger = LoggerFactory.getLogger(ArticleService.class);
    
    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    
    @Autowired
    public ArticleService(ArticleRepository articleRepository, CategoryRepository categoryRepository) {
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
    }

    // ===============================================
    // REQUÊTES PUBLIQUES (ARTICLES PUBLIÉS)
    // ===============================================
    
    /**
     * Récupère les derniers articles publiés pour la page d'accueil.
     * Optimisé avec limite fixe pour les performances.
     * 
     * @param limit nombre maximum d'articles (par défaut 10)
     * @return liste des derniers articles publiés
     * @throws BusinessException en cas d'erreur technique
     */
    @Transactional(readOnly = true)
    public List<Article> getRecentPublishedArticles(int limit) {
        // Validation des paramètres
        if (limit <= 0 || limit > 50) {
            throw new ValidationException("La limite doit être entre 1 et 50 articles");
        }
        
        try {
            Pageable pageable = PageRequest.of(0, limit);
            return articleRepository.findTopRecentPublished(ArticleStatus.PUBLISHED, pageable);
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la récupération des articles récents", e);
        }
    }

    /**
     * Récupère les derniers articles publiés avec la limite par défaut.
     * Méthode de convenance pour compatibilité.
     * 
     * @return liste des 10 derniers articles publiés
     */
    @Transactional(readOnly = true)
    public List<Article> getRecentArticles() {
        return getRecentPublishedArticles(10);
    }
    
    /**
     * Récupère un article publié par son slug pour affichage public.
     * Sécurité : seuls les articles publiés sont accessibles.
     * 
     * @param slug slug unique de l'article
     * @return article publié
     * @throws ResourceNotFoundException si l'article n'existe pas ou n'est pas publié
     */
    @Transactional(readOnly = true)
    public Article getPublishedArticleBySlug(String slug) {
        // Validation des paramètres
        if (slug == null || slug.trim().isEmpty()) {
            throw new ValidationException("Le slug ne peut pas être vide");
        }
        
        return articleRepository.findPublishedBySlug(slug.trim(), ArticleStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Article publié non trouvé avec le slug : " + slug));
    }
    
    /**
     * Récupère les articles publiés avec pagination.
     * Optimisé avec JOIN FETCH pour éviter N+1.
     * 
     * @param pageable configuration de pagination
     * @return page d'articles publiés
     * @throws ValidationException si pageable est invalide
     */
    @Transactional(readOnly = true)
    public Page<Article> getPublishedArticles(Pageable pageable) {
        // Validation des paramètres
        validatePageable(pageable);
        
        try {
            return articleRepository.findRecentPublishedArticles(ArticleStatus.PUBLISHED, pageable);
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la récupération des articles paginés", e);
        }
    }
    
    /**
     * Récupère les articles publiés d'une catégorie avec pagination.
     * 
     * @param categorySlug slug de la catégorie
     * @param pageable configuration de pagination
     * @return page d'articles de la catégorie
     * @throws ResourceNotFoundException si la catégorie n'existe pas
     */
    @Transactional(readOnly = true)
    public Page<Article> getPublishedArticlesByCategory(String categorySlug, Pageable pageable) {
        // Validation des paramètres
        if (categorySlug == null || categorySlug.trim().isEmpty()) {
            throw new ValidationException("Le slug de catégorie ne peut pas être vide");
        }
        validatePageable(pageable);
        
        // Récupération de la catégorie
        Category category = categoryRepository.findBySlug(categorySlug.trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Catégorie non trouvée avec le slug : " + categorySlug));
        
        try {
            return articleRepository.findPublishedByCategory(category, ArticleStatus.PUBLISHED, pageable);
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la récupération des articles par catégorie", e);
        }
    }
    
    /**
     * Recherche d'articles publiés par terme de recherche.
     * 
     * @param searchTerm terme de recherche
     * @param pageable configuration de pagination
     * @return page d'articles correspondants
     */
    @Transactional(readOnly = true)
    public Page<Article> searchPublishedArticles(String searchTerm, Pageable pageable) {
        // Validation des paramètres
        if (searchTerm == null) {
            searchTerm = "";
        }
        validatePageable(pageable);
        
        try {
            return articleRepository.searchPublishedArticles(
                searchTerm.trim(), 
                ArticleStatus.PUBLISHED, 
                pageable
            );
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la recherche d'articles", e);
        }
    }

    // ===============================================
    // REQUÊTES D'ADMINISTRATION
    // ===============================================
    
    /**
     * Récupère un article par son ID (tous statuts) pour administration.
     * Utilisé par les éditeurs et administrateurs.
     * 
     * @param id identifiant unique de l'article
     * @return article trouvé
     * @throws ResourceNotFoundException si l'article n'existe pas
     */
    @Transactional(readOnly = true)
    public Article getArticleById(UUID id) {
        // Validation des paramètres
        if (id == null) {
            throw new ValidationException("L'ID de l'article ne peut pas être null");
        }
        
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Article non trouvé avec l'ID : " + id));
    }
    
    /**
     * Récupère un article par son slug (tous statuts) pour administration.
     * 
     * @param slug slug unique de l'article
     * @return article trouvé
     * @throws ResourceNotFoundException si l'article n'existe pas
     */
    @Transactional(readOnly = true)
    public Article getArticleBySlugForAdmin(String slug) {
        // Validation des paramètres
        if (slug == null || slug.trim().isEmpty()) {
            throw new ValidationException("Le slug ne peut pas être vide");
        }
        
        return articleRepository.findBySlugWithDetails(slug.trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Article non trouvé avec le slug : " + slug));
    }
    
    /**
     * Récupère tous les articles avec pagination pour administration.
     * 
     * @param pageable configuration de pagination
     * @return page de tous les articles
     */
    @Transactional(readOnly = true)
    public Page<Article> getAllArticles(Pageable pageable) {
        validatePageable(pageable);
        
        try {
            return articleRepository.findAllWithDetails(pageable);
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la récupération de tous les articles", e);
        }
    }
    
    /**
     * Récupère les articles d'un auteur avec pagination.
     * 
     * @param author auteur des articles
     * @param pageable configuration de pagination
     * @return page des articles de l'auteur
     */
    @Transactional(readOnly = true)
    public Page<Article> getArticlesByAuthor(User author, Pageable pageable) {
        // Validation des paramètres
        if (author == null) {
            throw new ValidationException("L'auteur ne peut pas être null");
        }
        validatePageable(pageable);
        
        try {
            return articleRepository.findByAuthor(author, pageable);
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la récupération des articles par auteur", e);
        }
    }
    
    /**
     * Récupère les articles par statut avec pagination.
     * 
     * @param status statut des articles
     * @param pageable configuration de pagination
     * @return page des articles avec le statut donné
     */
    @Transactional(readOnly = true)
    public Page<Article> getArticlesByStatus(ArticleStatus status, Pageable pageable) {
        // Validation des paramètres
        if (status == null) {
            throw new ValidationException("Le statut ne peut pas être null");
        }
        validatePageable(pageable);
        
        try {
            return articleRepository.findByStatus(status, pageable);
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la récupération des articles par statut", e);
        }
    }

    /**
     * Récupère les articles d'un auteur avec un statut spécifique et pagination.
     * 
     * @param author auteur des articles
     * @param status statut des articles
     * @param pageable configuration de pagination
     * @return page des articles de l'auteur avec le statut donné
     */
    @Transactional(readOnly = true)
    public Page<Article> getArticlesByAuthorAndStatus(User author, ArticleStatus status, Pageable pageable) {
        // Validation des paramètres
        if (author == null) {
            throw new ValidationException("L'auteur ne peut pas être null");
        }
        if (status == null) {
            throw new ValidationException("Le statut ne peut pas être null");
        }
        validatePageable(pageable);
        
        try {
            return articleRepository.findByAuthorAndStatus(author, status, pageable);
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la récupération des articles par auteur et statut", e);
        }
    }

    // ===============================================
    // OPÉRATIONS CRUD ET GESTION DES STATUTS
    // ===============================================
    
    /**
     * Crée un nouvel article en brouillon.
     * Validation complète selon les règles métier DDD.
     * 
     * @param title titre de l'article
     * @param content contenu de l'article
     * @param categorySlug slug de la catégorie
     * @param author auteur de l'article
     * @return article créé
     * @throws ValidationException si les données sont invalides
     * @throws ResourceNotFoundException si la catégorie n'existe pas
     */
    @Transactional
    public Article createArticle(String title, String content, String categorySlug, User author) {
        logger.info("📝 Création d'un nouvel article - Titre: '{}', Catégorie: '{}', Auteur: '{}'", 
                   title != null ? title.substring(0, Math.min(title.length(), 50)) + "..." : "null",
                   categorySlug, 
                   author != null ? author.getUsername() : "null");
        
        // Validation des paramètres
        validateArticleCreationParams(title, content, categorySlug, author);
        
        // Récupération et validation de la catégorie
        Category category = categoryRepository.findBySlug(categorySlug.trim())
                .orElseThrow(() -> {
                    logger.warn("⚠️ Tentative de création d'article avec catégorie inexistante: '{}'", categorySlug);
                    return new ResourceNotFoundException("Catégorie non trouvée avec le slug : " + categorySlug);
                });
        
        try {
            // Création avec constructeur DDD (automatiquement en DRAFT)
            Article article = new Article(title.trim(), content.trim(), category, author);
            
            // Validation d'unicité du slug
            if (articleRepository.existsBySlug(article.getSlug())) {
                logger.warn("⚠️ Tentative de création d'article avec slug déjà existant: '{}'", article.getSlug());
                throw new ValidationException("Un article avec ce slug existe déjà : " + article.getSlug());
            }
            
            Article savedArticle = articleRepository.save(article);
            logger.info("✅ Article créé avec succès - ID: {}, Slug: '{}'", savedArticle.getId(), savedArticle.getSlug());
            
            return savedArticle;
        } catch (Exception e) {
            if (e instanceof ValidationException || e instanceof ResourceNotFoundException) {
                throw e;
            }
            logger.error("❌ Erreur lors de la création de l'article - Titre: '{}', Auteur: '{}'", 
                        title, author != null ? author.getUsername() : "null", e);
            throw new BusinessException("Erreur lors de la création de l'article", e);
        }
    }
    
    /**
     * Met à jour le contenu d'un article existant.
     * Utilise la méthode métier DDD updateContent().
     * 
     * @param articleId ID de l'article à modifier
     * @param newTitle nouveau titre
     * @param newContent nouveau contenu
     * @param currentUser utilisateur effectuant la modification
     * @return article mis à jour
     * @throws ResourceNotFoundException si l'article n'existe pas
     * @throws ValidationException si les données sont invalides
     * @throws BusinessException si l'utilisateur n'a pas les droits
     */
    @Transactional
    public Article updateArticleContent(UUID articleId, String newTitle, String newContent, User currentUser) {
        // Validation des paramètres
        if (articleId == null) {
            throw new ValidationException("L'ID de l'article ne peut pas être null");
        }
        validateArticleContentParams(newTitle, newContent);
        if (currentUser == null) {
            throw new ValidationException("L'utilisateur actuel ne peut pas être null");
        }
        
        // Récupération de l'article
        Article article = getArticleById(articleId);
        
        // Vérification des droits (seul l'auteur ou un admin peut modifier)
        if (!article.getAuthor().equals(currentUser) && !currentUser.getRole().equals(User.UserRole.ADMINISTRATEUR)) {
            throw new BusinessException("Vous n'êtes pas autorisé à modifier cet article");
        }
        
        try {
            // Validation d'unicité du slug si le titre change
            String potentialSlug = generateSlugFromTitle(newTitle.trim());
            if (!potentialSlug.equals(article.getSlug()) && 
                articleRepository.existsBySlugAndIdNot(potentialSlug, articleId)) {
                throw new ValidationException("Un article avec ce titre (slug) existe déjà");
            }
            
            // Utilisation de la méthode métier DDD
            article.updateContent(newTitle.trim(), newContent.trim(), null);
            
            return articleRepository.save(article);
        } catch (Exception e) {
            if (e instanceof ValidationException || e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException("Erreur lors de la mise à jour de l'article", e);
        }
    }
    
    /**
     * Publie un article en brouillon.
     * Utilise la méthode métier DDD publish().
     * 
     * @param articleId ID de l'article à publier
     * @param currentUser utilisateur effectuant l'action
     * @return article publié
     * @throws ResourceNotFoundException si l'article n'existe pas
     * @throws BusinessException si l'article ne peut pas être publié
     */
    @Transactional
    public Article publishArticle(UUID articleId, User currentUser) {
        logger.info("📢 Tentative de publication d'article - ID: {}, Utilisateur: '{}'", 
                   articleId, currentUser != null ? currentUser.getUsername() : "null");
        
        // Validation des paramètres
        if (articleId == null) {
            throw new ValidationException("L'ID de l'article ne peut pas être null");
        }
        if (currentUser == null) {
            throw new ValidationException("L'utilisateur actuel ne peut pas être null");
        }
        
        // Récupération de l'article
        Article article = getArticleById(articleId);
        
        // Vérification des droits
        if (!article.getAuthor().equals(currentUser) && !currentUser.getRole().equals(User.UserRole.ADMINISTRATEUR)) {
            logger.warn("🚫 Tentative de publication non autorisée - Article: {}, Auteur: '{}', Utilisateur tentant: '{}'", 
                       articleId, article.getAuthor().getUsername(), currentUser.getUsername());
            throw new BusinessException("Vous n'êtes pas autorisé à publier cet article");
        }
        
        try {
            logger.debug("🔄 Publication en cours - Article: '{}' (Status actuel: {})", 
                        article.getTitle(), article.getStatus());
            
            // Utilisation de la méthode métier DDD
            article.publish();
            
            Article publishedArticle = articleRepository.save(article);
            logger.info("✅ Article publié avec succès - ID: {}, Titre: '{}', Publié par: '{}'", 
                       publishedArticle.getId(), 
                       publishedArticle.getTitle().substring(0, Math.min(publishedArticle.getTitle().length(), 50)) + "...", 
                       currentUser.getUsername());
            
            return publishedArticle;
        } catch (Exception e) {
            if (e instanceof ValidationException || e instanceof BusinessException) {
                throw e;
            }
            logger.error("❌ Erreur lors de la publication de l'article - ID: {}, Utilisateur: '{}'", 
                        articleId, currentUser.getUsername(), e);
            throw new BusinessException("Erreur lors de la publication de l'article", e);
        }
    }
    
    /**
     * Archive un article publié.
     * Utilise la méthode métier DDD archive().
     * 
     * @param articleId ID de l'article à archiver
     * @param currentUser utilisateur effectuant l'action
     * @return article archivé
     */
    @Transactional
    public Article archiveArticle(UUID articleId, User currentUser) {
        // Validation des paramètres
        if (articleId == null) {
            throw new ValidationException("L'ID de l'article ne peut pas être null");
        }
        if (currentUser == null) {
            throw new ValidationException("L'utilisateur actuel ne peut pas être null");
        }
        
        // Récupération de l'article
        Article article = getArticleById(articleId);
        
        // Vérification des droits
        if (!article.getAuthor().equals(currentUser) && !currentUser.getRole().equals(User.UserRole.ADMINISTRATEUR)) {
            throw new BusinessException("Vous n'êtes pas autorisé à archiver cet article");
        }
        
        try {
            // Utilisation de la méthode métier DDD
            article.archive();
            
            return articleRepository.save(article);
        } catch (Exception e) {
            if (e instanceof ValidationException || e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException("Erreur lors de l'archivage de l'article", e);
        }
    }

    // ===============================================
    // STATISTIQUES ET MÉTRIQUES
    // ===============================================
    
    /**
     * Compte le nombre d'articles publiés d'une catégorie.
     * 
     * @param categorySlug slug de la catégorie
     * @return nombre d'articles publiés
     */
    @Transactional(readOnly = true)
    public long countPublishedArticlesByCategory(String categorySlug) {
        if (categorySlug == null || categorySlug.trim().isEmpty()) {
            throw new ValidationException("Le slug de catégorie ne peut pas être vide");
        }
        
        Category category = categoryRepository.findBySlug(categorySlug.trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Catégorie non trouvée avec le slug : " + categorySlug));
        
        return articleRepository.countPublishedByCategory(category, ArticleStatus.PUBLISHED);
    }
    
    /**
     * Compte le nombre total d'articles publiés.
     * 
     * @return nombre total d'articles publiés
     */
    @Transactional(readOnly = true)
    public long countTotalPublishedArticles() {
        return articleRepository.countPublished(ArticleStatus.PUBLISHED);
    }
    
    /**
     * Compte les articles d'un auteur.
     * 
     * @param author auteur des articles
     * @return nombre d'articles de l'auteur
     */
    @Transactional(readOnly = true)
    public long countArticlesByAuthor(User author) {
        if (author == null) {
            throw new ValidationException("L'auteur ne peut pas être null");
        }
        
        return articleRepository.countByAuthor(author);
    }

    // ===============================================
    // MÉTHODES UTILITAIRES PRIVÉES
    // ===============================================
    
    /**
     * Valide les paramètres de pagination.
     * 
     * @param pageable objet de pagination à valider
     * @throws ValidationException si invalide
     */
    private void validatePageable(Pageable pageable) {
        if (pageable == null) {
            throw new ValidationException("Les paramètres de pagination ne peuvent pas être null");
        }
        if (pageable.getPageSize() > 100) {
            throw new ValidationException("La taille de page ne peut pas dépasser 100 éléments");
        }
    }
    
    /**
     * Valide les paramètres de création d'article.
     */
    private void validateArticleCreationParams(String title, String content, String categorySlug, User author) {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Le titre ne peut pas être vide");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new ValidationException("Le contenu ne peut pas être vide");
        }
        if (categorySlug == null || categorySlug.trim().isEmpty()) {
            throw new ValidationException("Le slug de catégorie ne peut pas être vide");
        }
        if (author == null) {
            throw new ValidationException("L'auteur ne peut pas être null");
        }
    }
    
    /**
     * Valide les paramètres de contenu d'article.
     */
    private void validateArticleContentParams(String title, String content) {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Le titre ne peut pas être vide");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new ValidationException("Le contenu ne peut pas être vide");
        }
    }
    
    /**
     * Génère un slug à partir d'un titre.
     * Méthode utilitaire pour validation d'unicité.
     */
    private String generateSlugFromTitle(String title) {
        return title.toLowerCase()
                   .replaceAll("[^a-z0-9\\s-]", "")
                   .replaceAll("\\s+", "-")
                   .replaceAll("-+", "-")
                   .replaceAll("^-|-$", "");
    }
}
