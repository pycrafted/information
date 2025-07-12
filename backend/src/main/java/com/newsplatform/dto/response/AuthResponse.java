package com.newsplatform.dto.response;

import com.newsplatform.entity.User;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse d'authentification REST.
 * Couche Présentation : Contrat REST pour retourner les jetons JWT et infos utilisateur
 * Respecte le cahier des charges avec les 3 rôles utilisateur.
 */
public class AuthResponse {

    /**
     * Statut de l'authentification
     */
    private boolean success;

    /**
     * Message de réponse
     */
    private String message;

    /**
     * Jeton d'accès JWT
     */
    private String accessToken;

    /**
     * Jeton de rafraîchissement
     */
    private String refreshToken;

    /**
     * Date d'expiration du jeton d'accès
     */
    private LocalDateTime accessTokenExpiresAt;

    /**
     * Date d'expiration du jeton de rafraîchissement
     */
    private LocalDateTime refreshTokenExpiresAt;

    /**
     * Informations utilisateur connecté
     */
    private UserInfo user;

    /**
     * Classe interne pour les informations utilisateur
     */
    public static class UserInfo {
        private UUID id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private User.UserRole role;
        private String roleDescription;

        // Constructeurs
        public UserInfo() {}

        public UserInfo(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.role = user.getRole();
            this.roleDescription = user.getRole().getDescription();
        }

        // Getters et Setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public User.UserRole getRole() { return role; }
        public void setRole(User.UserRole role) { this.role = role; }

        public String getRoleDescription() { return roleDescription; }
        public void setRoleDescription(String roleDescription) { this.roleDescription = roleDescription; }
    }

    // Constructeurs
    public AuthResponse() {}

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Constructeur pour succès d'authentification
     */
    public AuthResponse(String accessToken, String refreshToken, 
                       LocalDateTime accessTokenExpiresAt, LocalDateTime refreshTokenExpiresAt,
                       User user) {
        this.success = true;
        this.message = "Authentification réussie";
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
        this.user = new UserInfo(user);
    }

    /**
     * Méthode statique pour créer une réponse d'échec
     */
    public static AuthResponse failure(String message) {
        return new AuthResponse(false, message);
    }

    /**
     * Méthode statique pour créer une réponse de succès
     */
    public static AuthResponse success(String accessToken, String refreshToken,
                                     LocalDateTime accessTokenExpiresAt, LocalDateTime refreshTokenExpiresAt,
                                     User user) {
        return new AuthResponse(accessToken, refreshToken, accessTokenExpiresAt, refreshTokenExpiresAt, user);
    }

    // Getters et Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public LocalDateTime getAccessTokenExpiresAt() { return accessTokenExpiresAt; }
    public void setAccessTokenExpiresAt(LocalDateTime accessTokenExpiresAt) { this.accessTokenExpiresAt = accessTokenExpiresAt; }

    public LocalDateTime getRefreshTokenExpiresAt() { return refreshTokenExpiresAt; }
    public void setRefreshTokenExpiresAt(LocalDateTime refreshTokenExpiresAt) { this.refreshTokenExpiresAt = refreshTokenExpiresAt; }

    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }
}
