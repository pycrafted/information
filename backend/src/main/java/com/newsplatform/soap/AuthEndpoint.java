package com.newsplatform.soap;

import com.newsplatform.dto.soap.LoginSoapRequest;
import com.newsplatform.dto.soap.LoginSoapResponse;
import com.newsplatform.dto.soap.LogoutSoapRequest;
import com.newsplatform.dto.soap.LogoutSoapResponse;
import com.newsplatform.service.AuthSoapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

/**
 * Endpoint SOAP pour l'authentification.
 * Couche Présentation : Interface SOAP pour les services d'authentification
 * Expose les opérations login/logout sécurisées selon le cahier des charges.
 */
@Endpoint
public class AuthEndpoint {

    private static final String NAMESPACE_URI = "http://newsplatform.com/soap/auth";
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthEndpoint.class);

    private final AuthSoapService authSoapService;

    @Autowired
    public AuthEndpoint(AuthSoapService authSoapService) {
        this.authSoapService = authSoapService;
        logger.info("🚀 SOAP - AuthEndpoint instancié avec namespace: {}", NAMESPACE_URI);
        logger.info("✅ SOAP - AuthEndpoint prêt à traiter les requêtes loginRequest et logoutRequest");
    }

    /**
     * Service SOAP de connexion (login)
     * Interface SOAP : Authentification et génération de jetons JWT
     * 
     * @param request Requête de connexion SOAP
     * @return Réponse d'authentification avec jetons JWT
     */
    @PayloadRoot(namespace = "", localPart = "loginRequest")
    @ResponsePayload
    public LoginSoapResponse login(@RequestPayload LoginSoapRequest request) {
        logger.info("🔐 SOAP - Méthode login() appelée !");
        logger.info("📋 SOAP - Requête login reçue pour utilisateur: {}", request != null ? request.getUsername() : "null");
        
        try {
            LoginSoapResponse response = authSoapService.authenticateUser(request);
            logger.info("✅ SOAP - Authentification traitée, succès: {}", response != null ? response.isSuccess() : "null");
            return response;
        } catch (Exception e) {
            logger.error("❌ SOAP - Erreur lors de l'authentification: {}", e.getMessage(), e);
            return LoginSoapResponse.failure("Erreur du service SOAP : " + e.getMessage());
        }
    }

    /**
     * Service SOAP de déconnexion (logout)
     * Interface SOAP : Révocation des jetons JWT
     * 
     * @param request Requête de déconnexion SOAP
     * @return Réponse de déconnexion avec statut
     */
    @PayloadRoot(namespace = "", localPart = "logoutRequest")
    @ResponsePayload
    public LogoutSoapResponse logout(@RequestPayload LogoutSoapRequest request) {
        try {
            return authSoapService.logoutUser(request);
        } catch (Exception e) {
            return LogoutSoapResponse.failure("Erreur du service SOAP : " + e.getMessage());
        }
    }
}
