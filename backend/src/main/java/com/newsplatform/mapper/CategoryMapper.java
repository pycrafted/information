package com.newsplatform.mapper;

import com.newsplatform.dto.request.CategoryRequest;
import com.newsplatform.dto.response.CategoryResponse;
import com.newsplatform.entity.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour la transformation des entités Category en DTOs de réponse.
 * Couche Mapper : Transformation et adaptation des données entre couches.
 * 
 * Respecte les principes DDD en :
 * - Séparant les préoccupations de transformation
 * - Protégeant l'intégrité des entités métier
 * - Exposant uniquement les données nécessaires au frontend
 * 
 * @author Équipe Développement
 * @version 1.0
 * @since 2024
 */
@Component
public class CategoryMapper {
    
    /**
     * Transforme une entité Category en CategoryResponse.
     * Inclut les informations hiérarchiques et statistiques.
     * 
     * @param category entité à transformer
     * @return DTO de réponse ou null si l'entité est null
     */
    public CategoryResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }
        
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setSlug(category.getSlug());
        response.setDescription(category.getDescription());
        response.setDepth(category.getDepth());
        response.setFullPath(category.getFullPath());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        
        // Informations hiérarchiques
        if (category.getParent() != null) {
            response.setParent(toMinimalResponse(category.getParent()));
        }
        
        // Statistiques - Éviter le lazy loading pour la pagination
        try {
            response.setArticleCount((long) category.getArticles().size());
            response.setTotalArticleCount((long) category.getArticles().size());
        } catch (Exception e) {
            // Lazy loading non disponible - utiliser 0 par défaut
            response.setArticleCount(0L);
            response.setTotalArticleCount(0L);
        }
        
        // Sous-catégories - Éviter le lazy loading pour la pagination
        try {
            if (!category.getChildren().isEmpty()) {
                response.setChildren(toSimpleResponseList(category.getChildren().stream().collect(Collectors.toList())));
            }
        } catch (Exception e) {
            // Lazy loading non disponible - laisser children null
        }
        
        return response;
    }
    
    /**
     * Transforme une liste d'entités Category en liste de CategoryResponse.
     * Préserve l'ordre de la liste originale.
     * 
     * @param categories liste des entités à transformer
     * @return liste des DTOs de réponse ou null si la liste est null
     */
    public List<CategoryResponse> toResponseList(List<Category> categories) {
        if (categories == null) {
            return null;
        }
        
        return categories.stream()
            .filter(category -> category != null) // Filtrer les éléments null
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Crée une CategoryResponse simplifiée pour les listes hiérarchiques.
     * Optimisé pour l'affichage en arbre sans charger toutes les relations.
     * 
     * @param category entité à transformer
     * @return DTO simplifié ou null si l'entité est null
     */
    public CategoryResponse toSimpleResponse(Category category) {
        if (category == null) {
            return null;
        }
        
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setSlug(category.getSlug());
        response.setDepth(category.getDepth());
        response.setDescription(category.getDescription());
        
        return response;
    }
    
    /**
     * Transforme une liste de catégories en réponses simplifiées.
     * Utilisé pour les menus et structures hiérarchiques.
     * 
     * @param categories liste des entités à transformer
     * @return liste des DTOs simplifiés
     */
    public List<CategoryResponse> toSimpleResponseList(List<Category> categories) {
        if (categories == null) {
            return null;
        }
        
        return categories.stream()
            .filter(category -> category != null)
            .map(this::toSimpleResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Transforme une catégorie pour l'affichage dans les articles.
     * Version ultra-légère avec uniquement nom et slug.
     * 
     * @param category entité à transformer
     * @return DTO minimal ou null si l'entité est null
     */
    public CategoryResponse toMinimalResponse(Category category) {
        if (category == null) {
            return null;
        }
        
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setSlug(category.getSlug());
        
        return response;
    }

    /**
     * Convertit un CategoryRequest en entité Category pour la création.
     * Ne définit pas l'ID (généré automatiquement) ni les timestamps.
     * 
     * @param request DTO de requête à convertir
     * @return nouvelle entité Category ou null si request est null
     */
    public Category fromRequest(CategoryRequest request) {
        if (request == null) {
            return null;
        }
        
        return new Category(request.getName(), request.getDescription());
    }

    /**
     * Met à jour une entité Category existante avec les données d'un CategoryRequest.
     * Préserve l'ID et les timestamps de création.
     * 
     * @param category entité existante à mettre à jour
     * @param request DTO contenant les nouvelles données
     */
    public void updateFromRequest(Category category, CategoryRequest request) {
        if (category == null || request == null) {
            return;
        }
        
        category.updateInfo(request.getName(), request.getDescription());
    }
}
