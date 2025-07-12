package com.newsplatform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.*;
import java.util.UUID;

/**
 * Entité Category représentant une catégorie d'articles dans le domaine métier.
 * 
 * Respecte les principes du Domain-Driven Design (DDD) en encapsulant
 * les règles métier et les invariants liés aux catégories hiérarchiques.
 * 
 * Règles métier principales :
 * - Une catégorie doit avoir un nom unique
 * - Le slug est généré automatiquement et doit être unique
 * - Une catégorie peut avoir une catégorie parente (hiérarchie)
 * - Une catégorie ne peut pas être sa propre parente (cycle)
 * - La profondeur de hiérarchie est limitée pour éviter les performances dégradées
 * - Une catégorie avec des sous-catégories ne peut pas être supprimée directement
 * 
 * @author Équipe Développement
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_category_slug", columnList = "slug"),
    @Index(name = "idx_category_parent", columnList = "parent_id"),
    @Index(name = "idx_category_name", columnList = "name")
})
public class Category {
    
    /**
     * Profondeur maximale autorisée pour la hiérarchie des catégories.
     * Règle métier : éviter une hiérarchie trop profonde pour les performances.
     */
    private static final int MAX_HIERARCHY_DEPTH = 5;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Le nom de la catégorie ne peut pas être vide")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Le slug ne peut pas être vide")
    @Size(max = 100, message = "Le slug ne peut pas dépasser 100 caractères")
    @Column(unique = true, nullable = false, length = 100)
    private String slug;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Category> children = new HashSet<>();

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private Set<Article> articles = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructeur par défaut pour JPA.
     * Initialise les timestamps lors de la création.
     */
    public Category() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructeur pour créer une nouvelle catégorie racine.
     * 
     * @param name nom de la catégorie (obligatoire)
     * @param description description de la catégorie (optionnel)
     * @throws IllegalArgumentException si le nom est invalide
     */
    public Category(String name, String description) {
        this();
        validateName(name);
        this.name = name.trim();
        this.description = description != null ? description.trim() : null;
        this.slug = generateSlug(name);
    }

    /**
     * Constructeur pour créer une sous-catégorie.
     * 
     * @param name nom de la catégorie (obligatoire)
     * @param description description de la catégorie (optionnel)
     * @param parent catégorie parente (obligatoire)
     * @throws IllegalArgumentException si les paramètres sont invalides
     */
    public Category(String name, String description, Category parent) {
        this(name, description);
        validateParent(parent);
        validateHierarchyDepth(parent);
        this.parent = parent;
        parent.addChild(this);
    }

    // === MÉTHODES MÉTIER (DOMAIN-DRIVEN DESIGN) ===

    /**
     * Ajoute une sous-catégorie à cette catégorie.
     * Règle métier : vérifie la profondeur de hiérarchie et les cycles.
     * 
     * @param child catégorie enfant à ajouter
     * @throws IllegalArgumentException si l'ajout viole les règles métier
     */
    public void addSubCategory(String childName, String childDescription) {
        validateHierarchyDepth(this);
        Category child = new Category(childName, childDescription, this);
        // La relation est automatiquement gérée par le constructeur
    }

    /**
     * Retire une sous-catégorie de cette catégorie.
     * Règle métier : la sous-catégorie ne doit pas avoir d'articles ou de sous-catégories.
     * 
     * @param child catégorie enfant à retirer
     * @throws IllegalStateException si la sous-catégorie contient des données
     */
    public void removeSubCategory(Category child) {
        if (child == null || !children.contains(child)) {
            throw new IllegalArgumentException("La sous-catégorie spécifiée n'existe pas");
        }
        
        if (!child.canBeDeleted()) {
            throw new IllegalStateException("Impossible de supprimer une catégorie contenant des articles ou des sous-catégories");
        }
        
        children.remove(child);
        child.parent = null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Met à jour les informations de la catégorie.
     * 
     * @param newName nouveau nom
     * @param newDescription nouvelle description
     * @throws IllegalArgumentException si le nom est invalide
     */
    public void updateInfo(String newName, String newDescription) {
        validateName(newName);
        this.name = newName.trim();
        this.description = newDescription != null ? newDescription.trim() : null;
        this.slug = generateSlug(newName);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Déplace cette catégorie vers une nouvelle catégorie parente.
     * Règle métier : vérifie les cycles et la profondeur de hiérarchie.
     * 
     * @param newParent nouvelle catégorie parente (null pour racine)
     * @throws IllegalArgumentException si le déplacement crée un cycle
     */
    public void moveToParent(Category newParent) {
        if (newParent != null) {
            validateParent(newParent);
            validateHierarchyDepth(newParent);
            validateNoCycle(newParent);
        }
        
        // Retirer de l'ancien parent
        if (this.parent != null) {
            this.parent.children.remove(this);
        }
        
        // Ajouter au nouveau parent
        this.parent = newParent;
        if (newParent != null) {
            newParent.addChild(this);
        }
        
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Vérifie si cette catégorie peut être supprimée.
     * Règle métier : pas d'articles ni de sous-catégories.
     * 
     * @return true si la catégorie peut être supprimée
     */
    public boolean canBeDeleted() {
        return !hasArticles() && !hasSubCategories();
    }

    /**
     * Vérifie si la catégorie contient des articles.
     * 
     * @return true si la catégorie a des articles
     */
    public boolean hasArticles() {
        return articles != null && !articles.isEmpty();
    }

    /**
     * Vérifie si la catégorie a des sous-catégories.
     * 
     * @return true si la catégorie a des sous-catégories
     */
    public boolean hasSubCategories() {
        return children != null && !children.isEmpty();
    }

    /**
     * Calcule la profondeur de cette catégorie dans la hiérarchie.
     * 
     * @return profondeur (0 pour une catégorie racine)
     */
    public int getDepth() {
        int depth = 0;
        Category current = this.parent;
        while (current != null && depth < MAX_HIERARCHY_DEPTH) {
            depth++;
            current = current.parent;
        }
        return depth;
    }

    /**
     * Retourne le chemin complet de la catégorie.
     * Exemple : "Technologie > Informatique > IA"
     * 
     * @return chemin hiérarchique de la catégorie
     */
    public String getFullPath() {
        List<String> path = new ArrayList<>();
        Category current = this;
        
        while (current != null) {
            path.add(0, current.name);
            current = current.parent;
        }
        
        return String.join(" > ", path);
    }

    // === MÉTHODES DE VALIDATION PRIVÉES ===

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la catégorie ne peut pas être vide");
        }
        if (name.trim().length() < 2 || name.trim().length() > 100) {
            throw new IllegalArgumentException("Le nom doit contenir entre 2 et 100 caractères");
        }
    }

    private void validateParent(Category parent) {
        if (parent == this) {
            throw new IllegalArgumentException("Une catégorie ne peut pas être sa propre parente");
        }
    }

    private void validateHierarchyDepth(Category parent) {
        if (parent != null && parent.getDepth() >= MAX_HIERARCHY_DEPTH - 1) {
            throw new IllegalArgumentException(
                String.format("La profondeur maximale de hiérarchie (%d) serait dépassée", MAX_HIERARCHY_DEPTH)
            );
        }
    }

    private void validateNoCycle(Category newParent) {
        Category current = newParent;
        while (current != null) {
            if (current == this) {
                throw new IllegalArgumentException("Le déplacement créerait un cycle dans la hiérarchie");
            }
            current = current.parent;
        }
    }

    private void addChild(Category child) {
        if (child != null) {
            this.children.add(child);
            this.updatedAt = LocalDateTime.now();
        }
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    // === ACCESSEURS (LECTURE SEULE SELON DDD) ===

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public Category getParent() {
        return parent;
    }

    public Set<Category> getChildren() {
        return Collections.unmodifiableSet(children);
    }

    public Set<Article> getArticles() {
        return Collections.unmodifiableSet(articles);
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
        Category category = (Category) o;
        return Objects.equals(id, category.id) && 
               Objects.equals(slug, category.slug);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, slug);
    }

    @Override
    public String toString() {
        return String.format("Category{id=%s, name='%s', slug='%s', depth=%d}", 
                           id, name, slug, getDepth());
    }
}