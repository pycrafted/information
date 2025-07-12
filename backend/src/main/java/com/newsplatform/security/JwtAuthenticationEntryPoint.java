package com.newsplatform.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Point d'entrée pour les erreurs d'authentification JWT.
 * Couche Sécurité : Gestion des erreurs d'authentification pour l'API REST
 * Retourne des réponses JSON standardisées pour les erreurs 401 Unauthorized.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Gestion des erreurs d'authentification
     * Sécurité : Retourne une réponse JSON standardisée pour les erreurs 401
     * 
     * @param request Requête HTTP qui a causé l'erreur
     * @param response Réponse HTTP à personnaliser
     * @param authException Exception d'authentification
     * @throws IOException si erreur d'écriture de la réponse
     * @throws ServletException si erreur de servlet
     */
    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        // Configuration de la réponse HTTP pour erreur 401
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        // Corps de réponse JSON standardisé
        String jsonResponse = """
            {
                "success": false,
                "message": "Accès non autorisé - Authentification requise",
                "error": "UNAUTHORIZED",
                "status": 401,
                "path": "%s",
                "timestamp": "%s"
            }
            """.formatted(
                request.getRequestURI(),
                java.time.LocalDateTime.now().toString()
            );
        
        // Écriture de la réponse
        response.getWriter().write(jsonResponse);
    }
}
