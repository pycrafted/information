package com.newsplatform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration Swagger/OpenAPI pour la documentation des services REST.
 * Couche Configuration : Documentation automatique de l'API
 * 
 * Responsabilités :
 * - Documentation complète des endpoints REST
 * - Schémas de sécurité JWT
 * - Modèles de données et exemples
 * - Environnements multiples (dev, staging, prod)
 * 
 * @author Équipe Développement
 * @version 2.0 - Configuration DDD complète
 */
@Configuration
public class SwaggerConfig {

    @Value("${app.version:2.0.0}")
    private String appVersion;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${app.profile:dev}")
    private String activeProfile;

    /**
     * Configuration OpenAPI 3.0 complète pour l'API News Platform.
     * 
     * @return configuration OpenAPI avec sécurité JWT et documentation
     */
    @Bean
    public OpenAPI newsPlatformOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(createServerList())
                .addSecurityItem(createSecurityRequirement())
                .components(createComponents());
    }

    /**
     * Informations générales de l'API selon les exigences du cahier des charges.
     * 
     * @return informations détaillées de l'API
     */
    private Info createApiInfo() {
        return new Info()
                .title("News Platform API")
                .description("""
                    **API REST pour la plateforme d'actualités selon architecture 5 couches**
                    
                    ## Architecture
                    
                    Cette API respecte strictement l'architecture en 5 couches :
                    - **Présentation** : Endpoints REST (ce document)
                    - **Contrôle** : Orchestration et validation
                    - **Service** : Logique métier et autorisation
                    - **Domaine** : Entités métier avec règles DDD
                    - **Persistance** : Accès optimisé aux données
                    
                    ## Authentification
                    
                    L'API utilise l'authentification JWT Bearer Token.
                    Obtenez votre token via `/api/auth/login`.
                    
                    ## Rôles Utilisateur
                    
                    - **VISITEUR** : Lecture des articles publiés uniquement
                    - **EDITEUR** : CRUD articles et catégories
                    - **ADMINISTRATEUR** : Gestion complète utilisateurs + articles
                    
                    ## Formats Supportés
                    
                    - **JSON** (par défaut)
                    - **XML** (négociation de contenu)
                    
                    ## Pagination
                    
                    Tous les endpoints paginés utilisent les paramètres standards :
                    - `page` : numéro de page (0-based)
                    - `size` : taille de page (max 100)
                    - `sort` : tri (ex: `publishedAt,desc`)
                    """)
                .version(appVersion)
                .contact(createContactInfo())
                .license(createLicenseInfo());
    }

    /**
     * Informations de contact pour l'équipe de développement.
     * 
     * @return contact de l'équipe
     */
    private Contact createContactInfo() {
        return new Contact()
                .name("Équipe Développement News Platform")
                .email("dev@newsplatform.com")
                .url("https://github.com/newsplatform/backend");
    }

    /**
     * Informations de licence du projet.
     * 
     * @return licence du projet
     */
    private License createLicenseInfo() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    /**
     * Liste des serveurs selon l'environnement actif.
     * 
     * @return serveurs configurés
     */
    private List<Server> createServerList() {
        Server localServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Serveur de développement local");

        Server stagingServer = new Server()
                .url("https://staging-api.newsplatform.com")
                .description("Serveur de staging (pré-production)");

        Server productionServer = new Server()
                .url("https://api.newsplatform.com")
                .description("Serveur de production");

        // Retourner la liste selon l'environnement
        return switch (activeProfile.toLowerCase()) {
            case "prod" -> List.of(productionServer, stagingServer, localServer);
            case "staging" -> List.of(stagingServer, localServer, productionServer);
            default -> List.of(localServer, stagingServer, productionServer);
        };
    }

    /**
     * Configuration de sécurité JWT pour tous les endpoints protégés.
     * 
     * @return exigence de sécurité JWT
     */
    private SecurityRequirement createSecurityRequirement() {
        return new SecurityRequirement()
                .addList("BearerAuth");
    }

    /**
     * Composants réutilisables incluant les schémas de sécurité.
     * 
     * @return composants OpenAPI
     */
    private Components createComponents() {
        return new Components()
                .addSecuritySchemes("BearerAuth", createJwtSecurityScheme());
    }

    /**
     * Schéma de sécurité JWT Bearer Token.
     * 
     * @return schéma de sécurité JWT
     */
    private SecurityScheme createJwtSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("""
                    **Authentification JWT Bearer Token**
                    
                    Pour accéder aux endpoints protégés :
                    
                    1. Obtenez un token via `POST /api/auth/login`
                    2. Incluez le token dans l'en-tête : `Authorization: Bearer <votre-token>`
                    
                    **Format du token :**
                    ```
                    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
                    ```
                    
                    **Durée de validité :** 24 heures (configurable)
                    
                    **Renouvellement :** Utilisez le refresh token via `/api/auth/refresh`
                    """);
    }
}
