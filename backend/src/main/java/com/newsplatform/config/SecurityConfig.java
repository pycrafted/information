package com.newsplatform.config;

import com.newsplatform.security.JwtAuthenticationEntryPoint;
import com.newsplatform.security.JwtAuthenticationFilter;
import com.newsplatform.service.TokenService;
import com.newsplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuration Spring Security pour la plateforme d'actualités.
 * Couche Configuration : Sécurité JWT avec autorisation par rôles
 * Gère les 3 rôles selon le cahier des charges : VISITEUR, EDITEUR, ADMINISTRATEUR.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Autowired
    public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                         TokenService tokenService,
                         UserRepository userRepository) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    /**
     * Configuration principale de la chaîne de filtres de sécurité
     * Sécurité : Définition des autorisations par rôle selon le cahier des charges
     * 
     * @param http Configuration de sécurité HTTP
     * @return Chaîne de filtres configurée
     * @throws Exception si erreur de configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Désactivation CSRF pour API REST stateless
            .csrf(csrf -> csrf.disable())
            
            // Configuration CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Gestion des erreurs d'authentification
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            
            // Sessions stateless (JWT)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configuration des autorisations par endpoint et rôle
            .authorizeHttpRequests(auth -> auth
                // === ENDPOINTS PUBLICS ===
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/articles/recent").permitAll()
                .requestMatchers("/api/articles/published").permitAll()
                .requestMatchers("/api/articles/category/**").permitAll()
                .requestMatchers("/api/categories", "/api/categories/roots").permitAll()
                .requestMatchers("/api/categories/{id}", "/api/categories/slug/**").permitAll()
                
                // === ENDPOINTS DE DÉVELOPPEMENT ===
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/soap/**", "/ws/**").permitAll()
                
                // === ENDPOINTS VISITEUR (lecture uniquement) ===
                // Déjà couverts par les endpoints publics
                
                // === ENDPOINTS EDITEUR (CRUD Articles + Catégories) ===
                .requestMatchers("POST", "/api/articles").hasAnyRole("EDITOR", "ADMIN")
                .requestMatchers("PUT", "/api/articles/**").hasAnyRole("EDITOR", "ADMIN")
                .requestMatchers("POST", "/api/articles/*/publish").hasAnyRole("EDITOR", "ADMIN")
                .requestMatchers("POST", "/api/articles/*/archive").hasAnyRole("EDITOR", "ADMIN")
                .requestMatchers("POST", "/api/categories").hasAnyRole("EDITOR", "ADMIN")
                .requestMatchers("PUT", "/api/categories/**").hasAnyRole("EDITOR", "ADMIN")
                .requestMatchers("PATCH", "/api/categories/*/move").hasAnyRole("EDITOR", "ADMIN")
                
                // === ENDPOINTS ADMINISTRATEUR (CRUD Utilisateurs + Gestion) ===
                .requestMatchers("DELETE", "/api/articles/**").hasRole("ADMIN")
                .requestMatchers("DELETE", "/api/categories/**").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Tous les autres endpoints nécessitent une authentification
                .anyRequest().authenticated()
            );

        // Ajout du filtre JWT avant le filtre d'authentification par défaut
        http.addFilterBefore(new JwtAuthenticationFilter(tokenService, userRepository), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configuration CORS pour l'API REST
     * Sécurité : Autorisation des origines de développement avec credentials
     * 
     * @return Source de configuration CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Autoriser les origines spécifiques du développement
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:5173",
            "http://localhost:3000",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:3000"
        ));
        
        // Autoriser toutes les méthodes HTTP
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Autoriser tous les headers incluant Authorization
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Autoriser les credentials pour JWT
        configuration.setAllowCredentials(true);
        
        // Appliquer cette configuration à tous les endpoints API
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        source.registerCorsConfiguration("/soap/**", configuration);
        source.registerCorsConfiguration("/ws/**", configuration);
        
        return source;
    }

    /**
     * Bean PasswordEncoder pour l'authentification sécurisée
     * Couche Configuration : Encodage BCrypt pour les mots de passe
     * 
     * @return Encodeur de mots de passe BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
