package com.newsplatform.repository;

import com.newsplatform.entity.Article;
import com.newsplatform.entity.ArticleStatus;
import com.newsplatform.entity.Category;
import com.newsplatform.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID; 

/**
 * Repository pour la gestion des articles selon les principes DDD.
 * Couche Persistance : Accès optimisé aux données des articles
 * 
 * Responsabilités :
 * - Requêtes complexes avec jointures optimisées
 * - Gestion de la pagination intelligente
 * - Requêtes de sécurité (articles publiés uniquement)
 * - Support des recherches par critères multiples
 * 
 * @author Équipe Développement
 * @version 2.0 - Refactoring DDD complet
 */
@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {
    
    // ===============================================
    // REQUÊTES DE BASE OPTIMISÉES
    // ===============================================
    
    /**
     * Trouve un article publié par son slug pour affichage public.
     * Sécurité : seuls les articles publiés sont retournés.
     * 
     * @param slug slug unique de l'article
     * @return article publié ou empty si non trouvé/non publié
     */
    @Query("SELECT a FROM Article a " +
           "JOIN FETCH a.category c " +
           "JOIN FETCH a.author u " +
           "WHERE a.slug = :slug AND a.status = :status")
    Optional<Article> findPublishedBySlug(@Param("slug") String slug, 
                                        @Param("status") ArticleStatus status);
    
    /**
     * Trouve un article par son slug (tous statuts) pour administration.
     * Utilisé par les éditeurs et administrateurs uniquement.
     * 
     * @param slug slug unique de l'article
     * @return article trouvé ou empty
     */
    @Query("SELECT a FROM Article a " +
           "JOIN FETCH a.category c " +
           "JOIN FETCH a.author u " +
           "WHERE a.slug = :slug")
    Optional<Article> findBySlugWithDetails(@Param("slug") String slug);

    // ===============================================
    // REQUÊTES PUBLIQUES (ARTICLES PUBLIÉS)
    // ===============================================
    
    /**
     * Récupère les derniers articles publiés avec pagination.
     * Requête optimisée avec JOIN FETCH pour éviter N+1.
     * 
     * @param status statut PUBLISHED
     * @param pageable configuration de pagination
     * @return page d'articles publiés triés par date de publication
     */
    @Query("SELECT a FROM Article a " +
           "JOIN FETCH a.category c " +
           "JOIN FETCH a.author u " +
           "WHERE a.status = :status AND a.publishedAt IS NOT NULL " +
           "ORDER BY a.publishedAt DESC")
    Page<Article> findRecentPublishedArticles(@Param("status") ArticleStatus status, 
                                            Pageable pageable);
    
    /**
     * Récupère les N derniers articles publiés pour page d'accueil.
     * Optimisé pour les performances avec limite fixe.
     * 
     * @param status statut PUBLISHED
     * @param limit nombre maximum d'articles
     * @return liste des derniers articles publiés
     */
    @Query("SELECT a FROM Article a " +
           "JOIN FETCH a.category c " +
           "JOIN FETCH a.author u " +
           "WHERE a.status = :status AND a.publishedAt IS NOT NULL " +
           "ORDER BY a.publishedAt DESC")
    List<Article> findTopRecentPublished(@Param("status") ArticleStatus status, 
                                       Pageable pageable);
    
    /**
     * Récupère les articles publiés d'une catégorie avec pagination.
     * 
     * @param category catégorie de filtrage
     * @param status statut PUBLISHED
     * @param pageable configuration de pagination
     * @return page d'articles de la catégorie
     */
    @Query("SELECT a FROM Article a " +
           "JOIN FETCH a.category c " +
           "JOIN FETCH a.author u " +
           "WHERE a.category = :category AND a.status = :status AND a.publishedAt IS NOT NULL " +
           "ORDER BY a.publishedAt DESC")
    Page<Article> findPublishedByCategory(@Param("category") Category category,
                                        @Param("status") ArticleStatus status,
                                        Pageable pageable);

    // ===============================================
    // REQUÊTES DE RECHERCHE AVANCÉE
    // ===============================================
    
    /**
     * Recherche d'articles publiés par titre ou contenu.
     * Recherche insensible à la casse avec LIKE.
     * 
     * @param searchTerm terme de recherche
     * @param status statut PUBLISHED
     * @param pageable configuration de pagination
     * @return page d'articles correspondants
     */
    @Query("SELECT a FROM Article a " +
           "JOIN FETCH a.category c " +
           "JOIN FETCH a.author u " +
           "WHERE a.status = :status AND a.publishedAt IS NOT NULL " +
           "AND (LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(a.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY a.publishedAt DESC")
    Page<Article> searchPublishedArticles(@Param("searchTerm") String searchTerm,
                                        @Param("status") ArticleStatus status,
                                        Pageable pageable);
    
    /**
     * Recherche d'articles publiés dans une plage de dates.
     * 
     * @param startDate date de début
     * @param endDate date de fin
     * @param status statut PUBLISHED
     * @param pageable configuration de pagination
     * @return page d'articles dans la plage
     */
    @Query("SELECT a FROM Article a " +
           "JOIN FETCH a.category c " +
           "JOIN FETCH a.author u " +
           "WHERE a.status = :status AND a.publishedAt IS NOT NULL " +
           "AND a.publishedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY a.publishedAt DESC")
    Page<Article> findPublishedBetweenDates(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          @Param("status") ArticleStatus status,
                                          Pageable pageable);

    // ===============================================
    // REQUÊTES D'ADMINISTRATION
    // ===============================================
    
    /**
     * Récupère tous les articles (tous statuts) avec pagination pour administration.
     * 
     * @param pageable configuration de pagination
     * @return page de tous les articles
     */
    @Query("SELECT a FROM Article a " +
           "JOIN FETCH a.category c " +
           "JOIN FETCH a.author u " +
           "ORDER BY a.updatedAt DESC")
    Page<Article> findAllWithDetails(Pageable pageable);
    
    /**
     * Récupère les articles d'un auteur spécifique.
     * 
     * @param author auteur des articles
     * @param pageable configuration de pagination
     * @return page des articles de l'auteur
     */
    @Query("SELECT a FROM Article a " +
           "JOIN FETCH a.category c " +
           "WHERE a.author = :author " +
           "ORDER BY a.updatedAt DESC")
    Page<Article> findByAuthor(@Param("author") User author, Pageable pageable);
    
    /**
     * Récupère les articles par statut pour gestion éditoriale.
     * 
     * @param status statut des articles
     * @param pageable configuration de pagination
     * @return page des articles avec le statut donné
     */
    @Query("SELECT a FROM Article a " +
           "JOIN FETCH a.category c " +
           "JOIN FETCH a.author u " +
           "WHERE a.status = :status " +
           "ORDER BY a.updatedAt DESC")
    Page<Article> findByStatus(@Param("status") ArticleStatus status, Pageable pageable);

    /**
     * Récupère les articles d'un auteur avec un statut spécifique.
     * 
     * @param author auteur des articles
     * @param status statut des articles
     * @param pageable configuration de pagination
     * @return page des articles de l'auteur avec le statut donné
     */
    @Query("SELECT a FROM Article a " +
           "JOIN FETCH a.category c " +
           "WHERE a.author = :author AND a.status = :status " +
           "ORDER BY a.updatedAt DESC")
    Page<Article> findByAuthorAndStatus(@Param("author") User author, @Param("status") ArticleStatus status, Pageable pageable);

    // ===============================================
    // REQUÊTES DE STATISTIQUES
    // ===============================================
    
    /**
     * Compte le nombre d'articles publiés d'une catégorie.
     * 
     * @param category catégorie à compter
     * @param status statut PUBLISHED
     * @return nombre d'articles publiés
     */
    @Query("SELECT COUNT(a) FROM Article a " +
           "WHERE a.category = :category AND a.status = :status")
    long countPublishedByCategory(@Param("category") Category category,
                                @Param("status") ArticleStatus status);
    
    /**
     * Compte le nombre total d'articles publiés.
     * 
     * @param status statut PUBLISHED
     * @return nombre total d'articles publiés
     */
    @Query("SELECT COUNT(a) FROM Article a " +
           "WHERE a.status = :status AND a.publishedAt IS NOT NULL")
    long countPublished(@Param("status") ArticleStatus status);
    
    /**
     * Compte les articles par auteur.
     * 
     * @param author auteur des articles
     * @return nombre d'articles de l'auteur
     */
    @Query("SELECT COUNT(a) FROM Article a WHERE a.author = :author")
    long countByAuthor(@Param("author") User author);

    // ===============================================
    // REQUÊTES DE VALIDATION D'UNICITÉ
    // ===============================================
    
    /**
     * Vérifie si un article avec ce slug existe déjà.
     * Utilisé pour validation d'unicité lors de la création.
     * 
     * @param slug slug à vérifier
     * @return true si un article avec ce slug existe
     */
    boolean existsBySlug(String slug);
    
    /**
     * Vérifie si un article avec ce slug existe déjà, excluant l'article avec l'ID donné.
     * Utilisé pour validation d'unicité lors de la mise à jour.
     * 
     * @param slug slug à vérifier
     * @param id ID de l'article à exclure de la vérification
     * @return true si un autre article avec ce slug existe
     */
    boolean existsBySlugAndIdNot(String slug, UUID id);

    // ===============================================
    // REQUÊTES DE SUPPRESSION AVANCÉES
    // ===============================================
    
    /**
     * Supprime les articles en brouillon anciens (nettoyage automatique).
     * 
     * @param status statut DRAFT
     * @param cutoffDate date limite (articles plus anciens)
     * @return nombre d'articles supprimés
     */
    @Query("DELETE FROM Article a " +
           "WHERE a.status = :status AND a.updatedAt < :cutoffDate")
    int deleteOldDrafts(@Param("status") ArticleStatus status,
                       @Param("cutoffDate") LocalDateTime cutoffDate);
}