package com.newsplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration Web pour la négociation de contenu et CORS
 * Couche Configuration : Configuration des aspects techniques web
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configuration de la négociation de contenu
     * Support JSON (par défaut) et XML pour les réponses
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .favorParameter(false)  // Pas de paramètre format dans l'URL
            .ignoreAcceptHeader(false)  // Utiliser l'en-tête Accept
            .defaultContentType(MediaType.APPLICATION_JSON)  // JSON par défaut
            .mediaType("json", MediaType.APPLICATION_JSON)
            .mediaType("xml", MediaType.APPLICATION_XML);
    }

    /**
     * Configuration CORS pour les tests et le développement
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
