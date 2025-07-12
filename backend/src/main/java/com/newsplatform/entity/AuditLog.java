package com.newsplatform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entité AuditLog pour tracer les actions utilisateur selon les principes DDD.
 * 
 * Responsabilités :
 * - Enregistrement des actions critiques (CRUD, authentification)
 * - Traçabilité pour la sécurité et la conformité
 * - Historique des modifications importantes
 * 
 * @author Équipe Développement
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp")
})
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "L'utilisateur est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "L'action ne peut pas être vide")
    @Column(nullable = false, length = 100)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "resource_type", length = 50)
    private String resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(name = "success", nullable = false)
    private boolean success = true;

    /**
     * Constructeur par défaut pour JPA.
     */
    public AuditLog() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructeur pour créer un log d'audit.
     * 
     * @param user utilisateur qui effectue l'action
     * @param action action effectuée
     * @param details détails de l'action
     * @param resourceType type de ressource concernée
     * @param resourceId ID de la ressource concernée
     */
    public AuditLog(User user, String action, String details, String resourceType, UUID resourceId) {
        this();
        this.user = user;
        this.action = action;
        this.details = details;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    /**
     * Constructeur complet avec informations de session.
     */
    public AuditLog(User user, String action, String details, String resourceType, 
                   UUID resourceId, String ipAddress, String userAgent) {
        this(user, action, details, resourceType, resourceId);
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    // === MÉTHODES MÉTIER ===

    /**
     * Marque l'action comme échouée.
     */
    public void markAsFailed() {
        this.success = false;
    }

    /**
     * Vérifie si l'action a réussi.
     */
    public boolean isSuccessful() {
        return success;
    }

    /**
     * Vérifie si l'action concerne une ressource spécifique.
     */
    public boolean hasResource() {
        return resourceType != null && resourceId != null;
    }

    // === GETTERS ET SETTERS ===

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public void setResourceId(UUID resourceId) {
        this.resourceId = resourceId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    // === MÉTHODES UTILITAIRES ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLog auditLog = (AuditLog) o;
        return Objects.equals(id, auditLog.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", action='" + action + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", resourceId=" + resourceId +
                ", timestamp=" + timestamp +
                ", success=" + success +
                '}';
    }
}