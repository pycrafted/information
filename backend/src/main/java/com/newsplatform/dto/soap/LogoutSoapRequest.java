package com.newsplatform.dto.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * DTO de requête SOAP pour la déconnexion (logout).
 * Couche Présentation : Contrat SOAP pour la révocation des jetons
 * Sécurise la déconnexion selon le cahier des charges.
 */
@XmlRootElement(name = "logoutRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class LogoutSoapRequest {

    /**
     * Jeton d'accès à révoquer
     */
    @XmlElement(required = true)
    private String accessToken;

    /**
     * Jeton de rafraîchissement à révoquer (optionnel)
     */
    @XmlElement
    private String refreshToken;

    /**
     * Déconnexion globale (révoquer tous les jetons de l'utilisateur)
     */
    @XmlElement
    private boolean globalLogout = false;

    // Constructeurs
    public LogoutSoapRequest() {}

    public LogoutSoapRequest(String accessToken) {
        this.accessToken = accessToken;
    }

    public LogoutSoapRequest(String accessToken, String refreshToken, boolean globalLogout) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.globalLogout = globalLogout;
    }

    // Getters et Setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public boolean isGlobalLogout() { return globalLogout; }
    public void setGlobalLogout(boolean globalLogout) { this.globalLogout = globalLogout; }

    @Override
    public String toString() {
        return "LogoutSoapRequest{" +
                "accessToken='" + (accessToken != null ? "[PRÉSENT]" : "null") + '\'' +
                ", refreshToken='" + (refreshToken != null ? "[PRÉSENT]" : "null") + '\'' +
                ", globalLogout=" + globalLogout +
                '}';
    }
} 