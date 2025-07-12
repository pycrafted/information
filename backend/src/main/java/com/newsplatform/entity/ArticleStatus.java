package com.newsplatform.entity;

/**
 * Énumération représentant les différents statuts possibles d'un article
 * dans le système de gestion de contenu.
 * 
 * Respecte le principe de responsabilité unique en encapsulant
 * les règles métier liées aux transitions d'état des articles.
 * 
 * @author Équipe Développement
 * @version 1.0
 * @since 2024
 */
public enum ArticleStatus {
    
    /**
     * Article en cours de rédaction, non visible publiquement.
     * Statut initial par défaut pour tous les nouveaux articles.
     */
    DRAFT("Brouillon", "Article en cours de rédaction"),
    
    /**
     * Article publié et visible par les visiteurs.
     * Nécessite validation préalable du contenu.
     */
    PUBLISHED("Publié", "Article visible publiquement"),
    
    /**
     * Article archivé, plus visible publiquement mais conservé.
     * Permet la désactivation temporaire sans suppression.
     */
    ARCHIVED("Archivé", "Article retiré de la publication");
    
    private final String displayName;
    private final String description;
    
    /**
     * Constructeur privé pour l'énumération.
     * 
     * @param displayName nom d'affichage convivial du statut
     * @param description description détaillée du statut
     */
    ArticleStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Retourne le nom d'affichage convivial du statut.
     * 
     * @return nom d'affichage du statut
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Retourne la description détaillée du statut.
     * 
     * @return description du statut
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Vérifie si le statut permet la publication publique.
     * Règle métier : seuls les articles PUBLISHED sont visibles.
     * 
     * @return true si l'article est visible publiquement
     */
    public boolean isPubliclyVisible() {
        return this == PUBLISHED;
    }
    
    /**
     * Vérifie si le statut permet la modification du contenu.
     * Règle métier : seuls les articles DRAFT peuvent être modifiés librement.
     * 
     * @return true si l'article peut être modifié
     */
    public boolean isEditable() {
        return this == DRAFT;
    }
    
    /**
     * Vérifie si une transition vers un nouveau statut est autorisée.
     * Règle métier : définit les transitions valides entre statuts.
     * 
     * @param newStatus nouveau statut cible
     * @return true si la transition est autorisée
     */
    public boolean canTransitionTo(ArticleStatus newStatus) {
        if (newStatus == null || newStatus == this) {
            return false;
        }
        
        return switch (this) {
            case DRAFT -> newStatus == PUBLISHED || newStatus == ARCHIVED;
            case PUBLISHED -> newStatus == ARCHIVED || newStatus == DRAFT;
            case ARCHIVED -> newStatus == DRAFT || newStatus == PUBLISHED;
        };
    }
} 