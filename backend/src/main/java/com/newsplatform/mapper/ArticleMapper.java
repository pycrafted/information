package com.newsplatform.mapper;

import com.newsplatform.dto.response.ArticleResponse;
import com.newsplatform.entity.Article;
import com.newsplatform.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour la transformation entre entités Article et DTOs ArticleResponse
 * Responsabilité : Transformation des données entre couches
 */
@Component
public class ArticleMapper {
    
    /**
     * Transforme une entité Article en DTO ArticleResponse
     * 
     * @param article L'entité Article à transformer
     * @return Le DTO ArticleResponse correspondant
     */
    public ArticleResponse toResponse(Article article) {
        if (article == null) {
            throw new ValidationException("L'article ne peut pas être null");
        }
        
        // Validation des champs obligatoires
        if (article.getId() == null) {
            throw new ValidationException("L'ID de l'article ne peut pas être null");
        }
        
        if (article.getTitle() == null || article.getTitle().trim().isEmpty()) {
            throw new ValidationException("Le titre de l'article ne peut pas être vide");
        }
        
        return new ArticleResponse(
            article.getId(),
            article.getTitle(),
            article.getContent(),
            article.getPublishedAt(),
            article.getCategory() != null ? article.getCategory().getName() : null
        );
    }
    
    /**
     * Transforme une liste d'entités Article en liste de DTOs ArticleResponse
     * 
     * @param articles La liste d'entités Article à transformer
     * @return La liste de DTOs ArticleResponse correspondante
     */
    public List<ArticleResponse> toResponseList(List<Article> articles) {
        if (articles == null) {
            throw new ValidationException("La liste des articles ne peut pas être null");
        }
        
        try {
            return articles.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ValidationException("Erreur lors de la transformation des articles", e);
        }
    }
}
