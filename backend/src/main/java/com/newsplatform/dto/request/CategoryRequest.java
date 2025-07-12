package com.newsplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

/**
 * DTO Request pour les opérations CRUD sur les catégories.
 * Couche Présentation : Validation des données d'entrée selon les règles métier
 * Utilisé pour la création et modification de catégories via API REST.
 * 
 * @author Équipe Développement
 * @version 1.0
 * @since 2025
 */
public class CategoryRequest {

    /**
     * Nom de la catégorie (obligatoire, 2-100 caractères)
     */
    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    @Pattern(regexp = "^[a-zA-Z0-9\\sÀ-ÿ\\-_]+$", message = "Le nom ne peut contenir que des lettres, chiffres, espaces et traits d'union")
    private String name;

    /**
     * Description de la catégorie (optionnelle, max 500 caractères)
     */
    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;

    /**
     * ID de la catégorie parente (optionnel, null pour catégorie racine)
     */
    private UUID parentId;

    // Constructeurs
    public CategoryRequest() {}

    public CategoryRequest(String name, String description, UUID parentId) {
        this.name = name;
        this.description = description;
        this.parentId = parentId;
    }

    // Getters et Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    @Override
    public String toString() {
        return "CategoryRequest{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", parentId=" + parentId +
                '}';
    }
}
