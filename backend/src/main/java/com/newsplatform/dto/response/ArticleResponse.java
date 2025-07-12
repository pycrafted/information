package com.newsplatform.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO utilisé pour exposer les données d'un article via l'API REST.
 * Sert de structure de réponse pour la couche présentation.
 * Support JSON (XML sera ajouté après synchronisation Gradle)
 */
public class ArticleResponse {
    private UUID id;
    private String title;
    private String content;
    private LocalDateTime publishedAt;
    private String categoryName;
    
    // Constructeur par défaut
    public ArticleResponse() {}
    
    // Constructeur avec tous les paramètres
    public ArticleResponse(UUID id, String title, String content, LocalDateTime publishedAt, String categoryName) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.publishedAt = publishedAt;
        this.categoryName = categoryName;
    }
    
    // Getters
    public UUID getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getContent() {
        return content;
    }
    
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    // Setters
    public void setId(UUID id) {
        this.id = id;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
