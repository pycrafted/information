package com.newsplatform.service;

import com.newsplatform.entity.AuthToken;
import com.newsplatform.entity.RefreshToken;
import com.newsplatform.repository.AuthTokenRepository;
import com.newsplatform.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service de nettoyage des tokens en double
 * Résout le problème de "Query did not return a unique result"
 */
@Service
@Transactional
public class TokenCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(TokenCleanupService.class);

    private final AuthTokenRepository authTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public TokenCleanupService(AuthTokenRepository authTokenRepository, 
                              RefreshTokenRepository refreshTokenRepository) {
        this.authTokenRepository = authTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Nettoie les tokens en double (exécution manuelle)
     */
    public void cleanupDuplicateTokens() {
        logger.info("🧹 Début du nettoyage des tokens en double");
        
        int authTokensCleaned = cleanupDuplicateAuthTokens();
        int refreshTokensCleaned = cleanupDuplicateRefreshTokens();
        
        logger.info("✅ Nettoyage terminé - {} auth tokens, {} refresh tokens supprimés", 
                   authTokensCleaned, refreshTokensCleaned);
    }

    /**
     * Nettoie immédiatement les tokens en double pour une valeur spécifique
     * Appelé après chaque refresh pour éviter les erreurs
     */
    public void cleanupDuplicateTokensForValue(String tokenValue) {
        try {
            logger.debug("🔍 Nettoyage immédiat des tokens en double pour la valeur: {}", 
                        tokenValue.substring(0, Math.min(20, tokenValue.length())));
            
            // Nettoyer les auth tokens
            List<AuthToken> authTokens = authTokenRepository.findAllByTokenValueWithUser(tokenValue);
            if (authTokens.size() > 1) {
                logger.warn("🔍 Tokens en double détectés pour la valeur {}: {} tokens", 
                           tokenValue.substring(0, Math.min(20, tokenValue.length())), 
                           authTokens.size());
                
                // Garder le token le plus récent et valide
                AuthToken validToken = authTokens.stream()
                    .filter(AuthToken::isValid)
                    .max((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()))
                    .orElse(null);
                
                if (validToken != null) {
                    // Supprimer les autres tokens
                    List<AuthToken> tokensToDelete = authTokens.stream()
                        .filter(token -> !token.getId().equals(validToken.getId()))
                        .toList();
                    
                    authTokenRepository.deleteAll(tokensToDelete);
                    logger.info("🗑️ Supprimé {} tokens en double pour la valeur {}", 
                               tokensToDelete.size(), 
                               tokenValue.substring(0, Math.min(20, tokenValue.length())));
                } else {
                    // Aucun token valide, supprimer tous
                    authTokenRepository.deleteAll(authTokens);
                    logger.warn("🗑️ Supprimé tous les {} tokens invalides pour la valeur {}", 
                               authTokens.size(), 
                               tokenValue.substring(0, Math.min(20, tokenValue.length())));
                }
            }
            
            // Nettoyer les refresh tokens
            List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByTokenValue(tokenValue);
            if (refreshTokens.size() > 1) {
                logger.warn("🔍 Refresh tokens en double détectés pour la valeur {}: {} tokens", 
                           tokenValue.substring(0, Math.min(20, tokenValue.length())), 
                           refreshTokens.size());
                
                // Garder le token le plus récent et valide
                RefreshToken validToken = refreshTokens.stream()
                    .filter(RefreshToken::isValid)
                    .max((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()))
                    .orElse(null);
                
                if (validToken != null) {
                    // Supprimer les autres tokens
                    List<RefreshToken> tokensToDelete = refreshTokens.stream()
                        .filter(token -> !token.getId().equals(validToken.getId()))
                        .toList();
                    
                    refreshTokenRepository.deleteAll(tokensToDelete);
                    logger.info("🗑️ Supprimé {} refresh tokens en double pour la valeur {}", 
                               tokensToDelete.size(), 
                               tokenValue.substring(0, Math.min(20, tokenValue.length())));
                } else {
                    // Aucun token valide, supprimer tous
                    refreshTokenRepository.deleteAll(refreshTokens);
                    logger.warn("🗑️ Supprimé tous les {} refresh tokens invalides pour la valeur {}", 
                               refreshTokens.size(), 
                               tokenValue.substring(0, Math.min(20, tokenValue.length())));
                }
            }
            
        } catch (Exception e) {
            logger.error("❌ Erreur lors du nettoyage immédiat des tokens: {}", e.getMessage(), e);
        }
    }

    /**
     * Nettoie les tokens d'authentification en double
     */
    private int cleanupDuplicateAuthTokens() {
        try {
            // Récupérer tous les tokens
            List<AuthToken> allTokens = authTokenRepository.findAll();
            
            // Grouper par valeur de token
            Map<String, List<AuthToken>> tokensByValue = allTokens.stream()
                .collect(Collectors.groupingBy(AuthToken::getTokenValue));
            
            int totalDeleted = 0;
            
            // Traiter chaque groupe de tokens avec la même valeur
            for (Map.Entry<String, List<AuthToken>> entry : tokensByValue.entrySet()) {
                List<AuthToken> duplicates = entry.getValue();
                
                if (duplicates.size() > 1) {
                    logger.warn("🔍 Tokens en double détectés pour la valeur {}: {} tokens", 
                               entry.getKey().substring(0, Math.min(20, entry.getKey().length())), 
                               duplicates.size());
                    
                    // Garder le token le plus récent et valide
                    AuthToken validToken = duplicates.stream()
                        .filter(AuthToken::isValid)
                        .max((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()))
                        .orElse(null);
                    
                    if (validToken != null) {
                        // Supprimer les autres tokens
                        List<AuthToken> tokensToDelete = duplicates.stream()
                            .filter(token -> !token.getId().equals(validToken.getId()))
                            .toList();
                        
                        authTokenRepository.deleteAll(tokensToDelete);
                        totalDeleted += tokensToDelete.size();
                        
                        logger.info("🗑️ Supprimé {} tokens en double pour la valeur {}", 
                                   tokensToDelete.size(), 
                                   entry.getKey().substring(0, Math.min(20, entry.getKey().length())));
                    } else {
                        // Aucun token valide, supprimer tous
                        authTokenRepository.deleteAll(duplicates);
                        totalDeleted += duplicates.size();
                        
                        logger.warn("🗑️ Supprimé tous les {} tokens invalides pour la valeur {}", 
                                   duplicates.size(), 
                                   entry.getKey().substring(0, Math.min(20, entry.getKey().length())));
                    }
                }
            }
            
            return totalDeleted;
            
        } catch (Exception e) {
            logger.error("❌ Erreur lors du nettoyage des auth tokens: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Nettoie les refresh tokens en double
     */
    private int cleanupDuplicateRefreshTokens() {
        try {
            // Récupérer tous les refresh tokens
            List<RefreshToken> allTokens = refreshTokenRepository.findAll();
            
            // Grouper par valeur de token
            Map<String, List<RefreshToken>> tokensByValue = allTokens.stream()
                .collect(Collectors.groupingBy(RefreshToken::getTokenValue));
            
            int totalDeleted = 0;
            
            // Traiter chaque groupe de tokens avec la même valeur
            for (Map.Entry<String, List<RefreshToken>> entry : tokensByValue.entrySet()) {
                List<RefreshToken> duplicates = entry.getValue();
                
                if (duplicates.size() > 1) {
                    logger.warn("🔍 Refresh tokens en double détectés pour la valeur {}: {} tokens", 
                               entry.getKey().substring(0, Math.min(20, entry.getKey().length())), 
                               duplicates.size());
                    
                    // Garder le token le plus récent et valide
                    RefreshToken validToken = duplicates.stream()
                        .filter(RefreshToken::isValid)
                        .max((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()))
                        .orElse(null);
                    
                    if (validToken != null) {
                        // Supprimer les autres tokens
                        List<RefreshToken> tokensToDelete = duplicates.stream()
                            .filter(token -> !token.getId().equals(validToken.getId()))
                            .toList();
                        
                        refreshTokenRepository.deleteAll(tokensToDelete);
                        totalDeleted += tokensToDelete.size();
                        
                        logger.info("🗑️ Supprimé {} refresh tokens en double pour la valeur {}", 
                                   tokensToDelete.size(), 
                                   entry.getKey().substring(0, Math.min(20, entry.getKey().length())));
                    } else {
                        // Aucun token valide, supprimer tous
                        refreshTokenRepository.deleteAll(duplicates);
                        totalDeleted += duplicates.size();
                        
                        logger.warn("🗑️ Supprimé tous les {} refresh tokens invalides pour la valeur {}", 
                                   duplicates.size(), 
                                   entry.getKey().substring(0, Math.min(20, entry.getKey().length())));
                    }
                }
            }
            
            return totalDeleted;
            
        } catch (Exception e) {
            logger.error("❌ Erreur lors du nettoyage des refresh tokens: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Nettoyage automatique programmé (toutes les 5 minutes)
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void scheduledCleanup() {
        logger.info("⏰ Nettoyage automatique des tokens en double");
        cleanupDuplicateTokens();
    }

    /**
     * Nettoyage des tokens expirés
     */
    @Scheduled(fixedRate = 86400000) // 24 heures
    public void cleanupExpiredTokens() {
        logger.info("⏰ Nettoyage automatique des tokens expirés");
        
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            
            int deletedAuthTokens = authTokenRepository.deleteOldExpiredTokens(cutoffDate);
            int deletedRefreshTokens = refreshTokenRepository.deleteOldTokens(cutoffDate);
            
            logger.info("✅ Nettoyage des tokens expirés terminé - {} auth tokens, {} refresh tokens supprimés", 
                       deletedAuthTokens, deletedRefreshTokens);
            
        } catch (Exception e) {
            logger.error("❌ Erreur lors du nettoyage des tokens expirés: {}", e.getMessage(), e);
        }
    }
} 