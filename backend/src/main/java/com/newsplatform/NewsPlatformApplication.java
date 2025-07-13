package com.newsplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application principale News Platform
 * 
 * Architecture Spring Boot avec :
 * - Authentification JWT
 * - API REST + SOAP
 * - Base de données PostgreSQL
 * - Swagger/OpenAPI
 * - Logging structuré
 */
@SpringBootApplication
// TEMPORAIREMENT DÉSACTIVÉ POUR DÉBUGGER LE PROBLÈME DE DÉMARRAGE
// @EnableScheduling
public class NewsPlatformApplication {

    private static final Logger logger = LoggerFactory.getLogger(NewsPlatformApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(NewsPlatformApplication.class, args);
    }

    /**
     * Affichage des informations de démarrage
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        String port = env.getProperty("server.port", "8080");
        String profile = env.getActiveProfiles().length > 0 ? env.getActiveProfiles()[0] : "default";
        
        logger.info("🚀 =========================================");
        logger.info("🚀 NEWS PLATFORM - DÉMARRAGE TERMINÉ");
        logger.info("🚀 =========================================");
        logger.info("🌐 URL API: http://localhost:{}/api", port);
        logger.info("📚 Documentation Swagger: http://localhost:{}/swagger-ui.html", port);
        logger.info("🗄️  Base de données PostgreSQL connectée");
        logger.info("⚙️  Profil actif: {}", profile);
        logger.info("🔐 Endpoints d'authentification:");
        logger.info("   - POST /api/auth/login");
        logger.info("   - POST /api/auth/logout");
        logger.info("   - POST /api/auth/refresh");
        logger.info("   - GET  /api/auth/validate");
        logger.info("👥 Compte administrateur par défaut:");
        logger.info("   - admin / admin123 (ADMINISTRATEUR)");
        logger.info("🚀 =========================================");
        logger.info("🔍 Application démarrée avec succès");
        logger.info("🚀 =========================================");
    }
}
