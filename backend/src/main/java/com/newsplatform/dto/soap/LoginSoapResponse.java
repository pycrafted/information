package com.newsplatform.dto.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.LocalDateTime;

/**
 * DTO de réponse SOAP pour l'authentification login.
 * Couche Présentation : Contrat SOAP pour retourner les jetons JWT
 * Sécurise l'accès aux services selon le cahier des charges.
 */
@XmlRootElement(name = "loginResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class LoginSoapResponse {

    /**
     * Statut de l'authentification
     */
    @XmlElement(required = true)
    private boolean success;

    /**
     * Message de réponse
     */
    @XmlElement
    private String message;

    /**
     * Jeton d'accès JWT
     */
    @XmlElement
    private String accessToken;

    /**
     * Jeton de rafraîchissement
     */
    @XmlElement
    private String refreshToken;

    /**
     * Date d'expiration du jeton d'accès
     */
    @XmlElement
    private LocalDateTime accessTokenExpiresAt;

    /**
     * Date d'expiration du jeton de rafraîchissement
     */
    @XmlElement
    private LocalDateTime refreshTokenExpiresAt;

    /**
     * Informations utilisateur
     */
    @XmlElement
    private UserInfoSoap userInfo;

    // Constructeurs
    public LoginSoapResponse() {}

    public LoginSoapResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public LoginSoapResponse(boolean success, String message, String accessToken, String refreshToken,
                           LocalDateTime accessTokenExpiresAt, LocalDateTime refreshTokenExpiresAt,
                           UserInfoSoap userInfo) {
        this.success = success;
        this.message = message;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
        this.userInfo = userInfo;
    }

    // Méthodes statiques pour créer des réponses standardisées
    public static LoginSoapResponse success(String accessToken, String refreshToken,
                                          LocalDateTime accessTokenExpiresAt, LocalDateTime refreshTokenExpiresAt,
                                          UserInfoSoap userInfo) {
        return new LoginSoapResponse(true, "Authentification réussie", accessToken, refreshToken,
                                   accessTokenExpiresAt, refreshTokenExpiresAt, userInfo);
    }

    public static LoginSoapResponse failure(String message) {
        return new LoginSoapResponse(false, message);
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
    public void setAccessTokenExpiresAt(LocalDateTime accessTokenExpiresAt) { 
        this.accessTokenExpiresAt = accessTokenExpiresAt; 
    }

    public LocalDateTime getRefreshTokenExpiresAt() { return refreshTokenExpiresAt; }
    public void setRefreshTokenExpiresAt(LocalDateTime refreshTokenExpiresAt) { 
        this.refreshTokenExpiresAt = refreshTokenExpiresAt; 
    }

    public UserInfoSoap getUserInfo() { return userInfo; }
    public void setUserInfo(UserInfoSoap userInfo) { this.userInfo = userInfo; }

    @Override
    public String toString() {
        return "LoginSoapResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", accessToken='" + (accessToken != null ? "[PRÉSENT]" : "null") + '\'' +
                ", refreshToken='" + (refreshToken != null ? "[PRÉSENT]" : "null") + '\'' +
                ", userInfo=" + userInfo +
                '}';
    }

    /**
     * Classe interne pour les informations utilisateur dans la réponse SOAP
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class UserInfoSoap {
        
        @XmlElement
        private String username;
        
        @XmlElement
        private String email;
        
        @XmlElement
        private String role;
        
        @XmlElement
        private String roleDescription;

        // Constructeurs
        public UserInfoSoap() {}

        public UserInfoSoap(String username, String email, String role, String roleDescription) {
            this.username = username;
            this.email = email;
            this.role = role;
            this.roleDescription = roleDescription;
        }

        // Getters et Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getRoleDescription() { return roleDescription; }
        public void setRoleDescription(String roleDescription) { this.roleDescription = roleDescription; }

        @Override
        public String toString() {
            return "UserInfoSoap{" +
                    "username='" + username + '\'' +
                    ", email='" + email + '\'' +
                    ", role='" + role + '\'' +
                    ", roleDescription='" + roleDescription + '\'' +
                    '}';
        }
    }
} 