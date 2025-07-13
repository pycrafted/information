package com.newsplatform.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.server.endpoint.interceptor.PayloadLoggingInterceptor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.core.io.ClassPathResource;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Configuration SOAP pour les services web de la plateforme d'actualités.
 * Respecte l'architecture 5 couches en configurant la couche Présentation
 * pour les services SOAP avec validation et gestion d'erreurs centralisée.
 * 
 * @author Équipe Développement
 * @version 1.0
 * @since 2024
 */
@EnableWs
@Configuration
@ComponentScan(basePackages = "com.newsplatform.soap")
public class SoapConfig {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SoapConfig.class);
    
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Enregistre la servlet MessageDispatcher pour traiter les requêtes SOAP.
     * Configure l'URL mapping et les paramètres de transformation.
     * 
     * @param applicationContext le contexte Spring pour l'injection
     * @return bean de configuration de la servlet SOAP
     */
    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
        logger.info("🚀 SOAP - Configuration MessageDispatcherServlet...");
        
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        
        ServletRegistrationBean<MessageDispatcherServlet> registrationBean = 
            new ServletRegistrationBean<>(servlet, "/soap/*", "/ws/*");
        registrationBean.setName("messageDispatcherServlet");
        
        logger.info("✅ SOAP - MessageDispatcherServlet configuré avec URL patterns: /soap/*, /ws/*");
        return registrationBean;
    }

    /**
     * Définit le WSDL pour les services d'authentification.
     * Génère automatiquement la documentation WSDL selon les bonnes pratiques.
     * Utilise le schéma XSD auth.xsd pour définir la structure des messages.
     * 
     * @return définition WSDL complète pour les services d'authentification
     */
    @Bean(name = "auth")
    public DefaultWsdl11Definition authWsdlDefinition(XsdSchema authSchema) {
        logger.info("🚀 SOAP - Configuration WSDL d'authentification...");
        
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("AuthServicePort");
        wsdl11Definition.setLocationUri("/soap");
        wsdl11Definition.setTargetNamespace("http://newsplatform.com/soap/auth");
        wsdl11Definition.setSchema(authSchema);
        
        logger.info("✅ SOAP - WSDL Auth configuré: LocationUri=/soap, Namespace=http://newsplatform.com/soap/auth");
        return wsdl11Definition;
    }

    /**
     * Définit le WSDL pour les services de gestion des utilisateurs.
     * Génère automatiquement la documentation WSDL selon les bonnes pratiques.
     * Utilise le schéma XSD users.xsd pour définir la structure des messages.
     * 
     * @return définition WSDL complète pour les services utilisateurs
     */
    @Bean(name = "users")
    public DefaultWsdl11Definition usersWsdlDefinition(XsdSchema usersSchema) {
        logger.info("🚀 SOAP - Configuration WSDL de gestion des utilisateurs...");
        
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("UsersServicePort");
        wsdl11Definition.setLocationUri("/soap");
        wsdl11Definition.setTargetNamespace("http://newsplatform.com/soap/users");
        wsdl11Definition.setSchema(usersSchema);
        
        logger.info("✅ SOAP - WSDL Users configuré: LocationUri=/soap, Namespace=http://newsplatform.com/soap/users");
        return wsdl11Definition;
    }

    /**
     * Charge le schéma XSD pour les services d'authentification.
     * 
     * @return schéma XSD pour la validation des messages SOAP
     */
    @Bean
    public XsdSchema authSchema() {
        logger.info("🚀 SOAP - Chargement du schéma XSD auth.xsd...");
        
        ClassPathResource resource = new ClassPathResource("wsdl/auth.xsd");
        if (resource.exists()) {
            logger.info("✅ SOAP - Schéma XSD auth.xsd trouvé et chargé");
        } else {
            logger.error("❌ SOAP - Schéma XSD auth.xsd NON TROUVÉ dans le classpath !");
        }
        
        return new SimpleXsdSchema(resource);
    }

    /**
     * Charge le schéma XSD pour les services de gestion des utilisateurs.
     * 
     * @return schéma XSD pour la validation des messages SOAP
     */
    @Bean
    public XsdSchema usersSchema() {
        logger.info("🚀 SOAP - Chargement du schéma XSD users.xsd...");
        
        ClassPathResource resource = new ClassPathResource("wsdl/users.xsd");
        if (resource.exists()) {
            logger.info("✅ SOAP - Schéma XSD users.xsd trouvé et chargé");
        } else {
            logger.error("❌ SOAP - Schéma XSD users.xsd NON TROUVÉ dans le classpath !");
        }
        
        return new SimpleXsdSchema(resource);
    }

    /**
     * Intercepteur de logging pour les requêtes et réponses SOAP.
     * Utilisé pour le debug et l'audit des services SOAP.
     * 
     * @return intercepteur configuré pour le logging
     */
    @Bean
    public PayloadLoggingInterceptor payloadLoggingInterceptor() {
        logger.info("🚀 SOAP - Configuration PayloadLoggingInterceptor...");
        
        PayloadLoggingInterceptor loggingInterceptor = new PayloadLoggingInterceptor();
        loggingInterceptor.setLogRequest(true);
        loggingInterceptor.setLogResponse(true);
        
        logger.info("✅ SOAP - PayloadLoggingInterceptor configuré (requêtes et réponses loggées)");
        return loggingInterceptor;
    }
    
    /**
     * Méthode exécutée après initialisation pour vérifier les endpoints enregistrés
     */
    @PostConstruct
    public void logEndpoints() {
        logger.info("🔍 SOAP - Vérification des endpoints enregistrés...");
        
        try {
            // Rechercher tous les beans annotés avec @Endpoint
            String[] endpointBeans = applicationContext.getBeanNamesForAnnotation(org.springframework.ws.server.endpoint.annotation.Endpoint.class);
            
            if (endpointBeans.length > 0) {
                logger.info("✅ SOAP - {} endpoint(s) trouvé(s):", endpointBeans.length);
                for (String beanName : endpointBeans) {
                    Object bean = applicationContext.getBean(beanName);
                    logger.info("   📍 {} - {}", beanName, bean.getClass().getSimpleName());
                }
            } else {
                logger.error("❌ SOAP - AUCUN endpoint @Endpoint trouvé ! Problème de scanning des composants.");
            }
            
        } catch (Exception e) {
            logger.error("❌ SOAP - Erreur lors de la vérification des endpoints: {}", e.getMessage(), e);
        }
    }
}
