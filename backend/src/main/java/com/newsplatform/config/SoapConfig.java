package com.newsplatform.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.server.endpoint.interceptor.PayloadLoggingInterceptor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.core.io.ClassPathResource;

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
public class SoapConfig {

    /**
     * Enregistre la servlet MessageDispatcher pour traiter les requêtes SOAP.
     * Configure l'URL mapping et les paramètres de transformation.
     * 
     * @param applicationContext le contexte Spring pour l'injection
     * @return bean de configuration de la servlet SOAP
     */
    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        
        ServletRegistrationBean<MessageDispatcherServlet> registrationBean = 
            new ServletRegistrationBean<>(servlet, "/soap/*", "/ws/*");
        registrationBean.setName("messageDispatcherServlet");
        
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
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("AuthServicePort");
        wsdl11Definition.setLocationUri("/soap");
        wsdl11Definition.setTargetNamespace("http://newsplatform.com/soap/auth");
        wsdl11Definition.setSchema(authSchema);
        
        return wsdl11Definition;
    }

    /**
     * Charge le schéma XSD pour les services d'authentification.
     * 
     * @return schéma XSD pour la validation des messages SOAP
     */
    @Bean
    public XsdSchema authSchema() {
        return new SimpleXsdSchema(new ClassPathResource("wsdl/auth.xsd"));
    }

    /**
     * Intercepteur de logging pour les requêtes et réponses SOAP.
     * Utilisé pour le debug et l'audit des services SOAP.
     * 
     * @return intercepteur configuré pour le logging
     */
    @Bean
    public PayloadLoggingInterceptor payloadLoggingInterceptor() {
        PayloadLoggingInterceptor loggingInterceptor = new PayloadLoggingInterceptor();
        loggingInterceptor.setLogRequest(true);
        loggingInterceptor.setLogResponse(true);
        return loggingInterceptor;
    }
}
