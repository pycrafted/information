package com.newsplatform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entité Article représentant un article de presse dans le domaine métier.
 * 
 * Respecte les principes du Domain-Driven Design (DDD) en encapsulant
 * les règles métier et les invariants liés aux articles.
 * 
 * Règles métier principales :
 * - Un article doit avoir un titre et un contenu non vides
 * - Un article appartient obligatoirement à une catégorie
 * - Un article a un auteur (éditeur ou administrateur)
 * - Les transitions de statut sont contrôlées
 * - Un slug doit être unique dans le système
 * 
 * @author Équipe Développement
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "articles", indexes = {
    @Index(name = "idx_article_slug", columnList = "slug"),
    @Index(name = "idx_article_status", columnList = "status"),
    @Index(name = "idx_article_published_at", columnList = "published_at")
})
public class Article {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Le titre ne peut pas être vide")
    @Size(min = 5, max = 255, message = "Le titre doit contenir entre 5 et 255 caractères")
    @Column(nullable = false, length = 255)
    private String title;

    @NotBlank(message = "Le slug ne peut pas être vide")
    @Size(max = 255, message = "Le slug ne peut pas dépasser 255 caractères")
    @Column(unique = true, nullable = false, length = 255)
    private String slug;

    @NotBlank(message = "Le contenu ne peut pas être vide")
    @Size(min = 50, message = "Le contenu doit contenir au moins 50 caractères")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Size(max = 500, message = "Le résumé ne peut pas dépasser 500 caractères")
    @Column(columnDefinition = "TEXT")
    private String summary;

    @NotNull(message = "Un article doit appartenir à une catégorie")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotNull(message = "Un article doit avoir un auteur")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArticleStatus status = ArticleStatus.DRAFT;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructeur par défaut pour JPA.
     * Initialise les timestamps lors de la création.
     */
    public Article() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructeur pour créer un nouvel article avec les données essentielles.
     * Applique les règles métier dès la création.
     * 
     * @param title titre de l'article (obligatoire)
     * @param content contenu de l'article (obligatoire)
     * @param category catégorie de l'article (obligatoire)
     * @param author auteur de l'article (obligatoire)
     * @throws IllegalArgumentException si les paramètres obligatoires sont invalides
     */
    public Article(String title, String content, Category category, User author) {
        this();
        validateTitle(title);
        validateContent(content);
        validateCategory(category);
        validateAuthor(author);
        
        this.title = title.trim();
        this.content = content.trim();
        this.category = category;
        this.author = author;
        this.slug = generateSlug(title);
        this.status = ArticleStatus.DRAFT;
    }
    
    // === MÉTHODES MÉTIER (DOMAIN-DRIVEN DESIGN) ===
    
    /**
     * Publie l'article s'il respecte les règles métier.
     * Règle métier : seuls les articles DRAFT peuvent être publiés.
     * 
     * @throws IllegalStateException si l'article ne peut pas être publié
     */
    public void publish() {
        if (!status.canTransitionTo(ArticleStatus.PUBLISHED)) {
            throw new IllegalStateException(
                String.format("Impossible de publier un article avec le statut %s", status.getDisplayName())
            );
        }
        
        validateForPublication();
        this.status = ArticleStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Archive l'article.
     * Règle métier : un article publié ou en brouillon peut être archivé.
     * 
     * @throws IllegalStateException si l'article ne peut pas être archivé
     */
    public void archive() {
        if (!status.canTransitionTo(ArticleStatus.ARCHIVED)) {
            throw new IllegalStateException(
                String.format("Impossible d'archiver un article avec le statut %s", status.getDisplayName())
            );
        }
        
        this.status = ArticleStatus.ARCHIVED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Remet l'article en brouillon pour modification.
     * Règle métier : un article publié ou archivé peut être remis en brouillon.
     * 
     * @throws IllegalStateException si l'article ne peut pas être modifié
     */
    public void moveBackToDraft() {
        if (!status.canTransitionTo(ArticleStatus.DRAFT)) {
            throw new IllegalStateException(
                String.format("Impossible de remettre en brouillon un article avec le statut %s", status.getDisplayName())
            );
        }
        
        this.status = ArticleStatus.DRAFT;
        this.publishedAt = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Met à jour le contenu de l'article si autorisé.
     * Règle métier : seuls les articles DRAFT peuvent être modifiés librement.
     * 
     * @param newTitle nouveau titre
     * @param newContent nouveau contenu
     * @param newSummary nouveau résumé (optionnel)
     * @throws IllegalStateException si l'article n'est pas modifiable
     */
    public void updateContent(String newTitle, String newContent, String newSummary) {
        if (!status.isEditable()) {
            throw new IllegalStateException(
                String.format("Impossible de modifier un article avec le statut %s", status.getDisplayName())
            );
        }
        
        validateTitle(newTitle);
        validateContent(newContent);
        
        this.title = newTitle.trim();
        this.content = newContent.trim();
        this.summary = newSummary != null ? newSummary.trim() : null;
        this.slug = generateSlug(newTitle);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Change la catégorie de l'article.
     * 
     * @param newCategory nouvelle catégorie
     * @throws IllegalArgumentException si la catégorie est nulle
     */
    public void changeCategory(Category newCategory) {
        validateCategory(newCategory);
        this.category = newCategory;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Vérifie si l'article est visible publiquement.
     * 
     * @return true si l'article est publié
     */
    public boolean isPubliclyVisible() {
        return status.isPubliclyVisible();
    }
    
    /**
     * Vérifie si l'article peut être modifié.
     * 
     * @return true si l'article est en brouillon
     */
    public boolean isEditable() {
        return status.isEditable();
    }
    
    // === MÉTHODES DE VALIDATION PRIVÉES ===
    
    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre ne peut pas être vide");
        }
        if (title.trim().length() < 5 || title.trim().length() > 255) {
            throw new IllegalArgumentException("Le titre doit contenir entre 5 et 255 caractères");
        }
    }
    
    private void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Le contenu ne peut pas être vide");
        }
        if (content.trim().length() < 50) {
            throw new IllegalArgumentException("Le contenu doit contenir au moins 50 caractères");
        }
    }
    
    private void validateCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Un article doit appartenir à une catégorie");
        }
    }
    
    private void validateAuthor(User author) {
        if (author == null) {
            throw new IllegalArgumentException("Un article doit avoir un auteur");
        }
    }
    
    private void validateForPublication() {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalStateException("Impossible de publier un article sans titre");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalStateException("Impossible de publier un article sans contenu");
        }
        if (category == null) {
            throw new IllegalStateException("Impossible de publier un article sans catégorie");
        }
    }
    
    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
    
    // === ACCESSEURS (LECTURE SEULE SELON DDD) ===
    
    public UUID getId() { 
        return id; 
    }
    
    public String getTitle() { 
        return title; 
    }
    
    public String getSlug() { 
        return slug; 
    }
    
    public String getContent() { 
        return content; 
    }
    
    public String getSummary() { 
        return summary; 
    }
    
    public Category getCategory() { 
        return category; 
    }
    
    public User getAuthor() { 
        return author; 
    }
    
    public ArticleStatus getStatus() { 
        return status; 
    }
    
    public LocalDateTime getPublishedAt() { 
        return publishedAt; 
    }
    
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    
    public LocalDateTime getUpdatedAt() { 
        return updatedAt; 
    }
    
    // === MÉTHODES JPA (CALLBACKS) ===
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // === MÉTHODES EQUALS ET HASHCODE ===
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Article article = (Article) o;
        return Objects.equals(id, article.id) && 
               Objects.equals(slug, article.slug);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, slug);
    }
    
    @Override
    public String toString() {
        return String.format("Article{id=%s, title='%s', status=%s, author=%s}", 
                           id, title, status, author != null ? author.getUsername() : "null");
    }
}