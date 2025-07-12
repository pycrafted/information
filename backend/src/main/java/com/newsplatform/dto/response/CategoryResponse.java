package com.newsplatform.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO Response pour l'exposition des catégories via API REST.
 * Couche Présentation : Formatage des données de sortie selon les besoins client
 * Utilisé pour retourner les informations de catégories avec hiérarchie.
 * 
 * @author Équipe Développement
 * @version 1.0
 * @since 2025
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryResponse {

    /**
     * Identifiant unique de la catégorie
     */
    private UUID id;

    /**
     * Nom de la catégorie
     */
    private String name;

    /**
     * Slug généré automatiquement pour SEO
     */
    private String slug;

    /**
     * Description de la catégorie
     */
    private String description;

    /**
     * Catégorie parente (null si catégorie racine)
     */
    private CategoryResponse parent;

    /**
     * Liste des sous-catégories directes
     */
    private List<CategoryResponse> children;

    /**
     * Nombre d'articles dans cette catégorie
     */
    private Long articleCount;

    /**
     * Nombre total d'articles (y compris sous-catégories)
     */
    private Long totalArticleCount;

    /**
     * Profondeur dans la hiérarchie (0 pour racine)
     */
    private Integer depth;

    /**
     * Chemin hiérarchique complet (ex: "Tech > IA > Machine Learning")
     */
    private String fullPath;

    /**
     * Date de création
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Date de dernière modification
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Constructeurs
    public CategoryResponse() {}

    public CategoryResponse(UUID id, String name, String slug, String description) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
    }

    // Getters et Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CategoryResponse getParent() {
        return parent;
    }

    public void setParent(CategoryResponse parent) {
        this.parent = parent;
    }

    public List<CategoryResponse> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryResponse> children) {
        this.children = children;
    }

    public Long getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(Long articleCount) {
        this.articleCount = articleCount;
    }

    public Long getTotalArticleCount() {
        return totalArticleCount;
    }

    public void setTotalArticleCount(Long totalArticleCount) {
        this.totalArticleCount = totalArticleCount;
    }

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "CategoryResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", depth=" + depth +
                ", articleCount=" + articleCount +
                '}';
    }
}
