package com.newsplatform.repository;

import com.newsplatform.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour la gestion des catégories hiérarchiques selon les principes DDD.
 * Couche Persistance : Accès optimisé aux données des catégories avec support de l'arborescence
 * 
 * Responsabilités :
 * - Gestion de la hiérarchie parent/enfant
 * - Requêtes optimisées pour navigation arborescente
 * - Validation de l'intégrité des liens hiérarchiques
 * - Comptage d'articles par catégorie
 * - Support de la recherche dans l'arborescence
 * 
 * @author Équipe Développement
 * @version 2.0 - Refactoring DDD complet
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    // ===============================================
    // REQUÊTES DE BASE OPTIMISÉES
    // ===============================================
    
    /**
     * Trouve une catégorie par son slug unique.
     * Optimisé pour l'affichage public avec chargement des relations.
     * 
     * @param slug slug unique de la catégorie
     * @return catégorie trouvée ou empty
     */
    @Query("SELECT c FROM Category c " +
           "LEFT JOIN FETCH c.parent p " +
           "LEFT JOIN FETCH c.children ch " +
           "WHERE c.slug = :slug")
    Optional<Category> findBySlugWithHierarchy(@Param("slug") String slug);
    
    /**
     * Trouve une catégorie par son slug (requête simple).
     * Utilisé pour validation d'unicité et recherches rapides.
     * 
     * @param slug slug unique de la catégorie
     * @return catégorie trouvée ou empty
     */
    Optional<Category> findBySlug(String slug);
    
    /**
     * Trouve une catégorie par son nom.
     * Utilisé pour validation d'unicité des noms.
     * 
     * @param name nom de la catégorie
     * @return catégorie trouvée ou empty
     */
    Optional<Category> findByName(String name);

    // ===============================================
    // REQUÊTES HIÉRARCHIQUES - NAVIGATION
    // ===============================================
    
    /**
     * Récupère toutes les catégories racines (sans parent).
     * Utilisé pour l'affichage de la navigation principale.
     * 
     * @return liste des catégories racines triées par nom
     */
    @Query("SELECT c FROM Category c " +
           "WHERE c.parent IS NULL " +
           "ORDER BY c.name ASC")
    List<Category> findRootCategories();
    
    /**
     * Récupère les catégories racines avec leurs enfants directs.
     * Optimisé pour l'affichage du menu de navigation.
     * 
     * @return liste des catégories racines avec enfants
     */
    @Query("SELECT DISTINCT c FROM Category c " +
           "LEFT JOIN FETCH c.children ch " +
           "WHERE c.parent IS NULL " +
           "ORDER BY c.name ASC")
    List<Category> findRootCategoriesWithChildren();
    
    /**
     * Récupère les sous-catégories directes d'une catégorie parent.
     * 
     * @param parent catégorie parent
     * @return liste des enfants directs triés par nom
     */
    @Query("SELECT c FROM Category c " +
           "WHERE c.parent = :parent " +
           "ORDER BY c.name ASC")
    List<Category> findDirectChildren(@Param("parent") Category parent);
    
    /**
     * Récupère une catégorie avec tous ses descendants (arbre complet).
     * Attention : peut être coûteux pour des hiérarchies profondes.
     * 
     * @param root catégorie racine
     * @return catégorie avec tout son sous-arbre
     */
    @Query("SELECT c FROM Category c " +
           "LEFT JOIN FETCH c.children ch1 " +
           "LEFT JOIN FETCH ch1.children ch2 " +
           "LEFT JOIN FETCH ch2.children ch3 " +
           "WHERE c = :root")
    Optional<Category> findWithDescendants(@Param("root") Category root);

    // ===============================================
    // REQUÊTES AVEC COMPTAGE D'ARTICLES
    // ===============================================
    
    /**
     * Récupère les catégories racines avec le nombre d'articles publiés.
     * Optimisé pour l'affichage de la navigation avec compteurs.
     * 
     * @return liste de catégories avec comptage
     */
    @Query("SELECT c, " +
           "(SELECT COUNT(a) FROM Article a WHERE a.category = c AND a.status = 'PUBLISHED') AS articleCount " +
           "FROM Category c " +
           "WHERE c.parent IS NULL " +
           "ORDER BY c.name ASC")
    List<Object[]> findRootCategoriesWithArticleCount();
    
    /**
     * Récupère une catégorie avec le nombre total d'articles publiés.
     * Inclut les articles des sous-catégories (comptage récursif).
     * 
     * @param categoryId ID de la catégorie
     * @return catégorie avec comptage total
     */
    @Query("SELECT c, " +
           "(SELECT COUNT(a) FROM Article a " +
           " JOIN a.category cat " +
           " WHERE cat = c OR cat.parent = c OR cat.parent.parent = c " +
           " AND a.status = 'PUBLISHED') AS totalArticleCount " +
           "FROM Category c " +
           "WHERE c.id = :categoryId")
    Optional<Object[]> findCategoryWithTotalArticleCount(@Param("categoryId") UUID categoryId);

    // ===============================================
    // REQUÊTES DE RECHERCHE
    // ===============================================
    
    /**
     * Recherche de catégories par nom (insensible à la casse).
     * 
     * @param searchTerm terme de recherche
     * @param pageable configuration de pagination
     * @return page de catégories correspondantes
     */
    @Query("SELECT c FROM Category c " +
           "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY c.name ASC")
    Page<Category> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Recherche de catégories par nom ou description.
     * 
     * @param searchTerm terme de recherche
     * @param pageable configuration de pagination
     * @return page de catégories correspondantes
     */
    @Query("SELECT c FROM Category c " +
           "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY c.name ASC")
    Page<Category> searchByNameOrDescription(@Param("searchTerm") String searchTerm, 
                                           Pageable pageable);

    // ===============================================
    // REQUÊTES D'ADMINISTRATION
    // ===============================================
    
    /**
     * Récupère toutes les catégories avec pagination pour administration.
     * 
     * @param pageable configuration de pagination
     * @return page de toutes les catégories
     */
    @Query("SELECT c FROM Category c " +
           "LEFT JOIN FETCH c.parent p " +
           "ORDER BY c.name ASC")
    Page<Category> findAllWithParent(Pageable pageable);
    
    /**
     * Récupère les catégories par niveau de profondeur.
     * Utile pour l'affichage hiérarchique en administration.
     * 
     * @param maxDepth profondeur maximale
     * @return liste des catégories jusqu'à la profondeur donnée
     */
    @Query("SELECT c FROM Category c " +
           "WHERE c.parent IS NULL " +
           "OR c.parent.parent IS NULL " +
           "OR (c.parent.parent.parent IS NULL AND :maxDepth >= 3) " +
           "OR (c.parent.parent.parent.parent IS NULL AND :maxDepth >= 4) " +
           "ORDER BY c.name ASC")
    List<Category> findByMaxDepth(@Param("maxDepth") int maxDepth);

    // ===============================================
    // REQUÊTES DE VALIDATION MÉTIER
    // ===============================================
    
    /**
     * Vérifie si un slug existe déjà.
     * Utilisé pour validation d'unicité lors de la création/modification.
     * 
     * @param slug slug à vérifier
     * @return true si le slug existe
     */
    boolean existsBySlug(String slug);
    
    /**
     * Vérifie si un slug existe pour une autre catégorie (modification).
     * 
     * @param slug slug à vérifier
     * @param categoryId ID de la catégorie en cours de modification
     * @return true si le slug existe pour une autre catégorie
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c " +
           "WHERE c.slug = :slug AND c.id != :categoryId")
    boolean existsBySlugAndIdNot(@Param("slug") String slug, 
                                @Param("categoryId") UUID categoryId);
    
    /**
     * Vérifie si un nom existe déjà.
     * Utilisé pour validation d'unicité des noms.
     * 
     * @param name nom à vérifier
     * @return true si le nom existe
     */
    boolean existsByName(String name);
    
    /**
     * Vérifie si un nom existe pour une autre catégorie (modification).
     * 
     * @param name nom à vérifier
     * @param categoryId ID de la catégorie en cours de modification
     * @return true si le nom existe pour une autre catégorie
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c " +
           "WHERE c.name = :name AND c.id != :categoryId")
    boolean existsByNameAndIdNot(@Param("name") String name, 
                                @Param("categoryId") UUID categoryId);
    
    /**
     * Vérifie si une catégorie a des sous-catégories.
     * Utilisé pour validation avant suppression.
     * 
     * @param parent catégorie parent à vérifier
     * @return true si elle a des enfants
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.parent = :parent")
    boolean hasChildren(@Param("parent") Category parent);
    
    /**
     * Vérifie si une catégorie contient des articles.
     * Utilisé pour validation avant suppression.
     * 
     * @param category catégorie à vérifier
     * @return true si elle contient des articles
     */
    @Query("SELECT COUNT(a) > 0 FROM Article a WHERE a.category = :category")
    boolean hasArticles(@Param("category") Category category);

    // ===============================================
    // REQUÊTES DE STATISTIQUES
    // ===============================================
    
    /**
     * Compte le nombre total de catégories.
     * 
     * @return nombre total de catégories
     */
    @Query("SELECT COUNT(c) FROM Category c")
    long countAll();
    
    /**
     * Compte le nombre de catégories racines.
     * 
     * @return nombre de catégories racines
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.parent IS NULL")
    long countRootCategories();
    
    /**
     * Compte le nombre de sous-catégories d'une catégorie.
     * 
     * @param parent catégorie parent
     * @return nombre d'enfants directs
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.parent = :parent")
    long countChildren(@Param("parent") Category parent);

    // ===============================================
    // REQUÊTES DE SUPPRESSION SÉCURISÉES
    // ===============================================
    
    /**
     * Trouve les catégories qui peuvent être supprimées en toute sécurité.
     * (sans enfants ni articles)
     * 
     * @return liste des catégories supprimables
     */
    @Query("SELECT c FROM Category c " +
           "WHERE c.id NOT IN (" +
           "   SELECT DISTINCT p.id FROM Category p " +
           "   JOIN p.children ch" +
           ") " +
           "AND c.id NOT IN (" +
           "   SELECT DISTINCT cat.id FROM Article a " +
           "   JOIN a.category cat" +
           ")")
    List<Category> findDeletableCategories();
}
