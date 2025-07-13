package com.newsplatform.security;

import com.newsplatform.entity.AuthToken;
import com.newsplatform.entity.User;
import com.newsplatform.service.TokenService;
import com.newsplatform.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Filtre JWT pour l'authentification des requêtes.
 * Couche Sécurité : Validation des jetons JWT et extraction des informations utilisateur
 * Supporte les 3 rôles selon le cahier des charges : VISITEUR, EDITEUR, ADMINISTRATEUR.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Autowired
    public JwtAuthenticationFilter(TokenService tokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    /**
     * Filtre principal pour la validation JWT
     * Sécurité : Extraction et validation du jeton Bearer Token
     * 
     * @param request Requête HTTP
     * @param response Réponse HTTP  
     * @param filterChain Chaîne de filtres
     * @throws ServletException si erreur de traitement
     * @throws IOException si erreur I/O
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws IOException, ServletException {
        
        // Vérification si ce filtre doit s'appliquer à la requête
        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extraction du jeton JWT de l'en-tête Authorization
            String jwtToken = extractJwtFromRequest(request);
            
            if (jwtToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Validation du jeton via le service TokenService
                Optional<AuthToken> authTokenOpt = tokenService.validateAccessToken(jwtToken);
                
                if (authTokenOpt.isPresent()) {
                    AuthToken authToken = authTokenOpt.get();
                    User user = authToken.getUser();
                    
                    logger.debug("🔍 Token validé pour l'utilisateur: {} (actif: {})", 
                               user != null ? user.getUsername() : "null", 
                               user != null ? user.getActive() : "null");
                    
                    // Vérification supplémentaire que l'utilisateur est actif
                    if (user != null && user.getActive()) {
                        
                        // Création des autorités selon le rôle utilisateur
                        List<SimpleGrantedAuthority> authorities = createAuthoritiesFromUserRole(user.getRole());
                        
                        // Création du contexte d'authentification Spring Security
                        UserPrincipal userPrincipal = UserPrincipal.create(user, authorities);
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                userPrincipal, 
                                null, 
                                authorities
                            );
                        
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // Définition du contexte de sécurité
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.debug("✅ Authentification définie pour l'utilisateur: {}", user.getUsername());
                    }
                }
            }
            
        } catch (Exception e) {
            // En cas d'erreur, on laisse la chaîne continuer sans authentification
            if (logger.isDebugEnabled()) {
                logger.debug("Impossible de définir l'authentification utilisateur : " + e.getMessage());
            }
        }

        // Continuation de la chaîne de filtres
        filterChain.doFilter(request, response);
    }

    /**
     * Extraction du jeton JWT de l'en-tête Authorization
     * Sécurité : Validation du format Bearer Token
     * 
     * @param request Requête HTTP
     * @return Jeton JWT extrait ou null
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Suppression du préfixe "Bearer "
        }
        
        return null;
    }

    /**
     * Création des autorités Spring Security selon le rôle utilisateur
     * Sécurité : Mapping des rôles métier vers les autorités Spring
     * 
     * @param userRole Rôle utilisateur du domaine
     * @return Liste des autorités Spring Security
     */
    private List<SimpleGrantedAuthority> createAuthoritiesFromUserRole(User.UserRole userRole) {
        return switch (userRole) {
            case ADMINISTRATEUR -> List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_EDITOR"),
                new SimpleGrantedAuthority("ROLE_USER")
            );
            case EDITEUR -> List.of(
                new SimpleGrantedAuthority("ROLE_EDITOR"),
                new SimpleGrantedAuthority("ROLE_USER")
            );
            case VISITEUR -> List.of(
                new SimpleGrantedAuthority("ROLE_USER")
            );
        };
    }

    /**
     * Détermine si ce filtre doit s'appliquer à la requête
     * Sécurité : Exclusion des endpoints publics pour performance
     * 
     * @param request Requête HTTP
     * @return true si le filtre ne doit PAS s'appliquer
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Exclusion des endpoints publics d'authentification
        return path.startsWith("/api/auth/login") || 
               path.startsWith("/api/auth/register") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/h2-console") ||
               path.startsWith("/soap") ||
               path.startsWith("/ws");
    }
}
