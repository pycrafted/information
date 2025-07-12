package com.newsplatform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité JPA représentant un jeton de rafraîchissement.
 * Couche Domaine : Gestion du renouvellement des jetons d'accès
 * Utilisé pour maintenir la session utilisateur de manière sécurisée.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * Valeur unique du jeton de rafraîchissement
     */
    @Column(name = "token_value", nullable = false, unique = true, length = 500)
    private String tokenValue;

    /**
     * Utilisateur propriétaire du jeton
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Date d'expiration du jeton de rafraîchissement
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Date de création du jeton
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Date de dernière utilisation
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    /**
     * Indique si le jeton a été révoqué
     */
    @Column(nullable = false)
    private Boolean revoked = false;

    /**
     * Adresse IP de création (sécurité)
     */
    @Column(name = "client_ip", length = 45)
    private String clientIp;

    /**
     * User Agent du client (sécurité)
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Nombre d'utilisations du jeton
     */
    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    // Constructeurs
    public RefreshToken() {}

    public RefreshToken(String tokenValue, User user, LocalDateTime expiresAt) {
        this.tokenValue = tokenValue;
        this.user = user;
        this.expiresAt = expiresAt;
    }

    // Méthodes métier (Couche Domaine)

    /**
     * Vérifie si le jeton de rafraîchissement est expiré
     * @return true si le jeton est expiré
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * Vérifie si le jeton est valide (non révoqué et non expiré)
     * @return true si le jeton est valide
     */
    public boolean isValid() {
        return !this.revoked && !isExpired();
    }

    /**
     * Révoque le jeton de rafraîchissement
     */
    public void revoke() {
        this.revoked = true;
    }

    /**
     * Marque le jeton comme utilisé
     */
    public void markAsUsed() {
        this.lastUsedAt = LocalDateTime.now();
        this.usageCount++;
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

    /**
     * Vérifie si le jeton a été utilisé récemment (moins de 24h)
     * @return true si utilisé dans les dernières 24h
     */
    public boolean isRecentlyUsed() {
        if (lastUsedAt == null) return false;
        return lastUsedAt.isAfter(LocalDateTime.now().minusHours(24));
    }

    /**
     * Vérifie si le jeton est surutilisé (plus de 100 utilisations)
     * @return true si surutilisé
     */
    public boolean isOverused() {
        return usageCount > 100;
    }

    // Getters et Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTokenValue() { return tokenValue; }
    public void setTokenValue(String tokenValue) { this.tokenValue = tokenValue; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }

    public Boolean getRevoked() { return revoked; }
    public void setRevoked(Boolean revoked) { this.revoked = revoked; }

    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public Integer getUsageCount() { return usageCount; }
    public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }

    @Override
    public String toString() {
        return "RefreshToken{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", revoked=" + revoked +
                ", expiresAt=" + expiresAt +
                ", usageCount=" + usageCount +
                '}';
    }
}
