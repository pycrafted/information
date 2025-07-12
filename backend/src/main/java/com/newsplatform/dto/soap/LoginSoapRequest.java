package com.newsplatform.dto.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * DTO de requête SOAP pour l'authentification login.
 * Couche Présentation : Contrat SOAP pour l'authentification
 * Sécurise l'accès aux services selon le cahier des charges.
 */
@XmlRootElement(name = "loginRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class LoginSoapRequest {

    /**
     * Nom d'utilisateur ou email pour l'authentification
     */
    @XmlElement(required = true)
    private String username;

    /**
     * Mot de passe de l'utilisateur
     */
    @XmlElement(required = true)
    private String password;

    /**
     * Adresse IP du client (pour la sécurité)
     */
    @XmlElement
    private String clientIp;

    /**
     * User Agent du client (pour la sécurité)
     */
    @XmlElement
    private String userAgent;

    // Constructeurs
    public LoginSoapRequest() {}

    public LoginSoapRequest(String username, String password, String clientIp, String userAgent) {
        this.username = username;
        this.password = password;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
    }

    // Getters et Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    @Override
    public String toString() {
        return "LoginSoapRequest{" +
                "username='" + username + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", userAgent='" + userAgent + '\'' +
                '}';
    }
} 