package com.newsplatform.dto.response;

import com.newsplatform.entity.User;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour la gestion des utilisateurs via API REST.
 * Couche Présentation : Contrat REST pour retourner les informations utilisateur
 * Exclut les données sensibles comme les mots de passe.
 */
public class UserResponse {

    /**
     * Identifiant unique de l'utilisateur
     */
    private UUID id;

    /**
     * Nom d'utilisateur
     */
    private String username;

    /**
     * Adresse email
     */
    private String email;

    /**
     * Prénom de l'utilisateur
     */
    private String firstName;

    /**
     * Nom de famille de l'utilisateur
     */
    private String lastName;

    /**
     * Rôle de l'utilisateur
     */
    private String role;

    /**
     * Description du rôle
     */
    private String roleDescription;

    /**
     * Statut actif/inactif
     */
    private Boolean active;

    /**
     * Date de création du compte
     */
    private LocalDateTime createdAt;

    /**
     * Date de dernière modification
     */
    private LocalDateTime updatedAt;

    /**
     * Date de dernière connexion
     */
    private LocalDateTime lastLogin;

    /**
     * Statistiques utilisateur (optionnel)
     */
    private UserStats stats;

    /**
     * Classe interne pour les statistiques utilisateur
     */
    public static class UserStats {
        private Long totalArticles;
        private Long publishedArticles;
        private Long totalConnections;
        private LocalDateTime lastActivity;

        // Constructeurs
        public UserStats() {}

        public UserStats(Long totalArticles, Long publishedArticles, 
                        Long totalConnections, LocalDateTime lastActivity) {
            this.totalArticles = totalArticles;
            this.publishedArticles = publishedArticles;
            this.totalConnections = totalConnections;
            this.lastActivity = lastActivity;
        }

        // Getters et Setters
        public Long getTotalArticles() { return totalArticles; }
        public void setTotalArticles(Long totalArticles) { this.totalArticles = totalArticles; }

        public Long getPublishedArticles() { return publishedArticles; }
        public void setPublishedArticles(Long publishedArticles) { this.publishedArticles = publishedArticles; }

        public Long getTotalConnections() { return totalConnections; }
        public void setTotalConnections(Long totalConnections) { this.totalConnections = totalConnections; }

        public LocalDateTime getLastActivity() { return lastActivity; }
        public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
    }

    // Constructeurs
    public UserResponse() {}

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.role = user.getRole().name();
        this.roleDescription = user.getRole().getDescription();
        this.active = user.getActive();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.lastLogin = user.getLastLogin();
    }

    /**
     * Constructeur complet avec statistiques
     */
    public UserResponse(User user, UserStats stats) {
        this(user);
        this.stats = stats;
    }

    /**
     * Méthode factory pour créer une UserResponse depuis une entité User
     * 
     * @param user Entité utilisateur
     * @return UserResponse configurée
     */
    public static UserResponse from(User user) {
        return new UserResponse(user);
    }

    /**
     * Méthode factory avec statistiques
     * 
     * @param user Entité utilisateur
     * @param stats Statistiques utilisateur
     * @return UserResponse configurée avec statistiques
     */
    public static UserResponse fromWithStats(User user, UserStats stats) {
        return new UserResponse(user, stats);
    }

    /**
     * Retourne le nom complet de l'utilisateur
     * 
     * @return Prénom + Nom ou nom d'utilisateur si pas de nom complet
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return username;
        }
    }

    /**
     * Indique si l'utilisateur est un administrateur
     * 
     * @return true si administrateur
     */
    public boolean isAdmin() {
        return "ADMINISTRATEUR".equals(role);
    }

    /**
     * Indique si l'utilisateur est un éditeur ou plus
     * 
     * @return true si éditeur ou administrateur
     */
    public boolean isEditor() {
        return "EDITEUR".equals(role) || "ADMINISTRATEUR".equals(role);
    }

    /**
     * Indique si l'utilisateur s'est connecté récemment (dans les 30 derniers jours)
     * 
     * @return true si connexion récente
     */
    public boolean hasRecentActivity() {
        return lastLogin != null && 
               lastLogin.isAfter(LocalDateTime.now().minusDays(30));
    }

    // Getters et Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    public void setRoleDescription(String roleDescription) {
        this.roleDescription = roleDescription;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public UserStats getStats() {
        return stats;
    }

    public void setStats(UserStats stats) {
        this.stats = stats;
    }

    @Override
    public String toString() {
        return "UserResponse{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                ", hasRecentActivity=" + hasRecentActivity() +
                '}';
    }
}
