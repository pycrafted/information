package com.newsplatform.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de test pour désactiver la sécurité et corriger les problèmes Spring
 * Couche Configuration : Configuration spécifique aux tests d'intégration
 */
@TestConfiguration
@EnableWebSecurity
@Profile("test")
public class TestConfig {

    /**
     * Configuration de sécurité désactivée pour les tests
     */
    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())); // Configuration moderne pour H2 Console
        
        return http.build();
    }
} 