package com.newsplatform.facade;

import com.newsplatform.dto.request.CategoryRequest;
import com.newsplatform.dto.response.CategoryResponse;
import com.newsplatform.entity.Category;
import com.newsplatform.mapper.CategoryMapper;
import com.newsplatform.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Façade pour l'orchestration des opérations de gestion des catégories.
 * Couche Contrôle : Orchestration et coordination entre services et mappers.
 * 
 * Responsabilités selon l'architecture 5 couches :
 * - Orchestration des appels aux services métier
 * - Transformation des DTOs via les mappers
 * - Validation des données d'entrée
 * - Gestion centralisée des exceptions
 * - Coordination des opérations complexes
 * 
 * @author Équipe Développement
 * @version 1.0
 * @since 2025
 */
@Component
public class CategoryFacade {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @Autowired
    public CategoryFacade(CategoryService categoryService, CategoryMapper categoryMapper) {
        this.categoryService = categoryService;
        this.categoryMapper = categoryMapper;
    }

    // ===============================================
    // OPÉRATIONS DE CRÉATION
    // ===============================================

    /**
     * Crée une nouvelle catégorie racine.
     * Orchestration : validation → service → mapping.
     * 
     * @param request données de la catégorie à créer
     * @return DTO de réponse avec la catégorie créée
     */
    public CategoryResponse createRootCategory(CategoryRequest request) {
        validateCategoryRequest(request);
        
        Category created = categoryService.createRootCategory(
            request.getName(), 
            request.getDescription()
        );
        
        return categoryMapper.toResponse(created);
    }

    /**
     * Crée une nouvelle sous-catégorie.
     * Orchestration : validation → service → mapping.
     * 
     * @param request données de la catégorie à créer
     * @return DTO de réponse avec la sous-catégorie créée
     */
    public CategoryResponse createSubCategory(CategoryRequest request) {
        validateCategoryRequest(request);
        validateParentId(request.getParentId());
        
        Category created = categoryService.createSubCategory(
            request.getName(), 
            request.getDescription(),
            request.getParentId()
        );
        
        return categoryMapper.toResponse(created);
    }

    // ===============================================
    // OPÉRATIONS DE LECTURE
    // ===============================================

    /**
     * Récupère toutes les catégories avec pagination.
     * 
     * @param pageable configuration de pagination
     * @return page de catégories en DTO
     */
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        Page<Category> categories = categoryService.getAllCategories(pageable);
        return categories.map(categoryMapper::toResponse);
    }

    /**
     * Récupère toutes les catégories racines pour l'affichage hiérarchique.
     * 
     * @return liste des catégories racines avec leurs enfants
     */
    public List<CategoryResponse> getRootCategoriesWithChildren() {
        List<Category> rootCategories = categoryService.getRootCategories();
        return categoryMapper.toResponseList(rootCategories);
    }

    /**
     * Récupère une catégorie par son ID avec toutes ses informations.
     * 
     * @param id identifiant de la catégorie
     * @return DTO de réponse complet
     */
    public CategoryResponse getCategoryById(UUID id) {
        validateCategoryId(id);
        
        Category category = categoryService.getCategoryById(id);
        return categoryMapper.toResponse(category);
    }

    /**
     * Récupère une catégorie par son slug pour l'affichage public.
     * 
     * @param slug slug de la catégorie
     * @return DTO de réponse complet
     */
    public CategoryResponse getCategoryBySlug(String slug) {
        validateSlug(slug);
        
        Category category = categoryService.getCategoryBySlug(slug);
        return categoryMapper.toResponse(category);
    }

    /**
     * Récupère les sous-catégories d'une catégorie parente.
     * 
     * @param parentId ID de la catégorie parente
     * @return liste des sous-catégories
     */
    public List<CategoryResponse> getSubCategories(UUID parentId) {
        validateCategoryId(parentId);
        
        List<Category> subCategories = categoryService.getSubCategories(parentId);
        return categoryMapper.toSimpleResponseList(subCategories);
    }

    // ===============================================
    // OPÉRATIONS DE MODIFICATION
    // ===============================================

    /**
     * Met à jour une catégorie existante.
     * Orchestration : validation → service → mapping.
     * 
     * @param id ID de la catégorie à modifier
     * @param request nouvelles données
     * @return DTO de réponse avec la catégorie mise à jour
     */
    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        validateCategoryId(id);
        validateCategoryRequest(request);
        
        Category updated = categoryService.updateCategory(
            id, 
            request.getName(), 
            request.getDescription()
        );
        
        return categoryMapper.toResponse(updated);
    }

    /**
     * Déplace une catégorie vers un nouveau parent.
     * 
     * @param categoryId ID de la catégorie à déplacer
     * @param newParentId ID du nouveau parent (null pour racine)
     * @return DTO de réponse avec la catégorie déplacée
     */
    public CategoryResponse moveCategory(UUID categoryId, UUID newParentId) {
        validateCategoryId(categoryId);
        // newParentId peut être null pour déplacer vers racine
        
        Category moved = categoryService.moveCategory(categoryId, newParentId);
        return categoryMapper.toResponse(moved);
    }

    // ===============================================
    // OPÉRATIONS DE SUPPRESSION
    // ===============================================

    /**
     * Supprime une catégorie si elle respecte les règles métier.
     * 
     * @param id ID de la catégorie à supprimer
     */
    public void deleteCategory(UUID id) {
        validateCategoryId(id);
        categoryService.deleteCategory(id);
    }

    // ===============================================
    // MÉTHODES DE VALIDATION PRIVÉES
    // ===============================================

    private void validateCategoryRequest(CategoryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La requête de catégorie ne peut pas être null");
        }
    }

    private void validateCategoryId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID de la catégorie ne peut pas être null");
        }
    }

    private void validateParentId(UUID parentId) {
        if (parentId == null) {
            throw new IllegalArgumentException("L'ID du parent est requis pour créer une sous-catégorie");
        }
    }

    private void validateSlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            throw new IllegalArgumentException("Le slug ne peut pas être vide");
        }
    }
    
    // ===============================================
    // MÉTHODE DE TEST POUR DIAGNOSTIQUER
    // ===============================================
    
    /**
     * Compte simple des catégories pour diagnostic.
     */
    public long getCategoryCount() {
        return categoryService.getCategoryCount();
    }
} 