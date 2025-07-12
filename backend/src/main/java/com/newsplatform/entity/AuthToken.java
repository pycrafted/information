package com.newsplatform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité JPA représentant un jeton d'authentification JWT.
 * Couche Domaine : Gestion des jetons pour sécuriser les services SOAP/REST
 * Utilisé pour l'authentification des utilisateurs selon leurs rôles.
 */
@Entity
@Table(name = "auth_tokens")
public class AuthToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * Jeton JWT encodé
     */
    @Column(name = "token_value", nullable = false, length = 1000)
    private String tokenValue;

    /**
     * Type de jeton (ACCESS, REFRESH)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TokenType tokenType;

    /**
     * Utilisateur propriétaire du jeton
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Date d'expiration du jeton
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Date de création du jeton
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Statut du jeton (actif, révoqué, expiré)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TokenStatus status = TokenStatus.ACTIVE;

    /**
     * Adresse IP de création du jeton (sécurité)
     */
    @Column(name = "client_ip", length = 45)
    private String clientIp;

    /**
     * User Agent du client (sécurité)
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Énumération des types de jetons
     */
    public enum TokenType {
        ACCESS("Jeton d'accès aux services"),
        REFRESH("Jeton de renouvellement");

        private final String description;

        TokenType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Énumération des statuts de jeton
     */
    public enum TokenStatus {
        ACTIVE("Jeton actif"),
        REVOKED("Jeton révoqué"), 
        EXPIRED("Jeton expiré");

        private final String description;

        TokenStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Constructeurs
    public AuthToken() {}

    public AuthToken(String tokenValue, TokenType tokenType, User user, LocalDateTime expiresAt) {
        this.tokenValue = tokenValue;
        this.tokenType = tokenType;
        this.user = user;
        this.expiresAt = expiresAt;
    }

    // Méthodes métier (Couche Domaine)

    /**
     * Vérifie si le jeton est expiré
     * @return true si le jeton est expiré
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * Vérifie si le jeton est valide (actif et non expiré)
     * @return true si le jeton est valide
     */
    public boolean isValid() {
        return TokenStatus.ACTIVE.equals(this.status) && !isExpired();
    }

    /**
     * Révoque le jeton
     */
    public void revoke() {
        this.status = TokenStatus.REVOKED;
    }

    /**
     * Marque le jeton comme expiré
     */
    public void markAsExpired() {
        this.status = TokenStatus.EXPIRED;
    }

    /**
     * Vérifie si le jeton appartient à un administrateur
     * @return true si le propriétaire est administrateur
     */
    public boolean isAdminToken() {
        return user != null && user.isAdministrator();
    }

    /**
     * Vérifie si le jeton appartient à un éditeur ou plus
     * @return true si le propriétaire peut éditer
     */
    public boolean isEditorToken() {
        return user != null && user.isEditor();
    }

    // Getters et Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTokenValue() { return tokenValue; }
    public void setTokenValue(String tokenValue) { this.tokenValue = tokenValue; }

    public TokenType getTokenType() { return tokenType; }
    public void setTokenType(TokenType tokenType) { this.tokenType = tokenType; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public TokenStatus getStatus() { return status; }
    public void setStatus(TokenStatus status) { this.status = status; }

    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    @Override
    public String toString() {
        return "AuthToken{" +
                "id=" + id +
                ", tokenType=" + tokenType +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", status=" + status +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
