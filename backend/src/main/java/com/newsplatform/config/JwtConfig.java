package com.newsplatform.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Configuration pour la gestion des JWT (JSON Web Tokens).
 * Fournit les beans nécessaires pour la génération et validation des tokens.
 * 
 * @author Équipe Développement
 * @version 1.0
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.secret:defaultSecretKeyForDevelopmentOnly}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 heures par défaut
    private long jwtExpiration;

    @Value("${jwt.refresh.expiration:604800000}") // 7 jours par défaut
    private long refreshExpiration;

    /**
     * Clé secrète pour signer les JWT.
     * 
     * @return clé secrète
     */
    @Bean
    public SecretKey jwtSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }



    /**
     * Génère un token JWT pour un utilisateur.
     * 
     * @param username nom d'utilisateur
     * @param role rôle de l'utilisateur
     * @return token JWT
     */
    public String generateToken(String username, String role) {
        return generateToken(username, role, new HashMap<>());
    }

    /**
     * Génère un token JWT avec des claims personnalisés.
     * 
     * @param username nom d'utilisateur
     * @param role rôle de l'utilisateur
     * @param extraClaims claims supplémentaires
     * @return token JWT
     */
    public String generateToken(String username, String role, Map<String, Object> extraClaims) {
        return buildToken(extraClaims, username, role, jwtExpiration);
    }

    /**
     * Génère un refresh token.
     * 
     * @param username nom d'utilisateur
     * @return refresh token
     */
    public String generateRefreshToken(String username) {
        return buildToken(new HashMap<>(), username, null, refreshExpiration);
    }

    /**
     * Construit un token JWT.
     * 
     * @param extraClaims claims supplémentaires
     * @param username nom d'utilisateur
     * @param role rôle de l'utilisateur
     * @param expiration durée d'expiration
     * @return token JWT
     */
    private String buildToken(Map<String, Object> extraClaims, String username, String role, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(jwtSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Extrait le nom d'utilisateur d'un token.
     * 
     * @param token token JWT
     * @return nom d'utilisateur
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrait le rôle d'un token.
     * 
     * @param token token JWT
     * @return rôle
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extrait la date d'expiration d'un token.
     * 
     * @param token token JWT
     * @return date d'expiration
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrait un claim spécifique d'un token.
     * 
     * @param token token JWT
     * @param claimsResolver fonction de résolution du claim
     * @param <T> type du claim
     * @return valeur du claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrait tous les claims d'un token.
     * 
     * @param token token JWT
     * @return tous les claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Vérifie si un token est expiré.
     * 
     * @param token token JWT
     * @return true si le token est expiré
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Valide un token pour un utilisateur.
     * 
     * @param token token JWT
     * @param username nom d'utilisateur
     * @return true si le token est valide
     */
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (username.equals(extractedUsername) && !isTokenExpired(token));
    }

    /**
     * Obtient la durée d'expiration du token JWT.
     * 
     * @return durée d'expiration en millisecondes
     */
    public long getJwtExpiration() {
        return jwtExpiration;
    }

    /**
     * Obtient la durée d'expiration du refresh token.
     * 
     * @return durée d'expiration en millisecondes
     */
    public long getRefreshExpiration() {
        return refreshExpiration;
    }
}