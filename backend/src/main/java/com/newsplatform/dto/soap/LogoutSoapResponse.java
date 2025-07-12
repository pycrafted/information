package com.newsplatform.dto.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.LocalDateTime;

/**
 * DTO de réponse SOAP pour la déconnexion (logout).
 * Couche Présentation : Contrat SOAP pour confirmer la révocation des jetons
 * Sécurise la déconnexion selon le cahier des charges.
 */
@XmlRootElement(name = "logoutResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class LogoutSoapResponse {

    /**
     * Statut de la déconnexion
     */
    @XmlElement(required = true)
    private boolean success;

    /**
     * Message de réponse
     */
    @XmlElement
    private String message;

    /**
     * Nombre de jetons révoqués
     */
    @XmlElement
    private int tokensRevoked;

    /**
     * Date et heure de la déconnexion
     */
    @XmlElement
    private LocalDateTime logoutTimestamp;

    // Constructeurs
    public LogoutSoapResponse() {}

    public LogoutSoapResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.logoutTimestamp = LocalDateTime.now();
    }

    public LogoutSoapResponse(boolean success, String message, int tokensRevoked) {
        this.success = success;
        this.message = message;
        this.tokensRevoked = tokensRevoked;
        this.logoutTimestamp = LocalDateTime.now();
    }

    // Méthodes statiques pour créer des réponses standardisées
    public static LogoutSoapResponse success(int tokensRevoked) {
        return new LogoutSoapResponse(true, "Déconnexion réussie", tokensRevoked);
    }

    public static LogoutSoapResponse success(String message, int tokensRevoked) {
        return new LogoutSoapResponse(true, message, tokensRevoked);
    }

    public static LogoutSoapResponse failure(String message) {
        return new LogoutSoapResponse(false, message);
    }

    // Getters et Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getTokensRevoked() { return tokensRevoked; }
    public void setTokensRevoked(int tokensRevoked) { this.tokensRevoked = tokensRevoked; }

    public LocalDateTime getLogoutTimestamp() { return logoutTimestamp; }
    public void setLogoutTimestamp(LocalDateTime logoutTimestamp) { this.logoutTimestamp = logoutTimestamp; }

    @Override
    public String toString() {
        return "LogoutSoapResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", tokensRevoked=" + tokensRevoked +
                ", logoutTimestamp=" + logoutTimestamp +
                '}';
    }
} 