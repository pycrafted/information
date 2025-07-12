package com.newsplatform.repository;

import com.newsplatform.entity.AuthToken;
import com.newsplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour la gestion des jetons d'authentification.
 * Couche Persistance : Accès aux données des jetons JWT
 * Utilisé pour sécuriser les services SOAP et REST selon le cahier des charges.
 */
@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {

    /**
     * Trouve un jeton par sa valeur
     * @param tokenValue Valeur du jeton JWT
     * @return Optional contenant le jeton si trouvé
     */
    Optional<AuthToken> findByTokenValue(String tokenValue);

    /**
     * Trouve tous les jetons actifs d'un utilisateur
     * @param user Utilisateur propriétaire des jetons
     * @param status Statut des jetons (ACTIVE)
     * @return Liste des jetons actifs de l'utilisateur
     */
    List<AuthToken> findByUserAndStatus(User user, AuthToken.TokenStatus status);

    /**
     * Trouve tous les jetons d'un type spécifique pour un utilisateur
     * @param user Utilisateur propriétaire des jetons
     * @param tokenType Type de jeton (ACCESS, REFRESH)
     * @return Liste des jetons du type spécifié
     */
    List<AuthToken> findByUserAndTokenType(User user, AuthToken.TokenType tokenType);

    /**
     * Trouve tous les jetons actifs d'un type spécifique pour un utilisateur
     * @param user Utilisateur propriétaire des jetons
     * @param tokenType Type de jeton (ACCESS, REFRESH)
     * @param status Statut des jetons (ACTIVE)
     * @return Liste des jetons actifs du type spécifié
     */
    List<AuthToken> findByUserAndTokenTypeAndStatus(User user, AuthToken.TokenType tokenType, AuthToken.TokenStatus status);

    /**
     * Trouve tous les jetons expirés
     * @param now Date/heure actuelle
     * @return Liste des jetons expirés
     */
    @Query("SELECT t FROM AuthToken t WHERE t.expiresAt < :now")
    List<AuthToken> findExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Révoque tous les jetons actifs d'un utilisateur
     * @param user Utilisateur dont les jetons doivent être révoqués
     * @param status Nouveau statut (REVOKED)
     * @return Nombre de jetons mis à jour
     */
    @Modifying
    @Query("UPDATE AuthToken t SET t.status = :status WHERE t.user = :user AND t.status = 'ACTIVE'")
    int revokeAllActiveTokensForUser(@Param("user") User user, @Param("status") AuthToken.TokenStatus status);

    /**
     * Supprime tous les jetons expirés depuis plus de 30 jours
     * @param cutoffDate Date limite (maintenant - 30 jours)
     * @return Nombre de jetons supprimés
     */
    @Modifying
    @Query("DELETE FROM AuthToken t WHERE t.expiresAt < :cutoffDate AND t.status != 'ACTIVE'")
    int deleteOldExpiredTokens(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Compte le nombre de jetons actifs pour un utilisateur
     * @param user Utilisateur
     * @param status Statut (ACTIVE)
     * @return Nombre de jetons actifs
     */
    long countByUserAndStatus(User user, AuthToken.TokenStatus status);

    /**
     * Vérifie si un jeton existe et est valide
     * @param tokenValue Valeur du jeton
     * @param status Statut requis (ACTIVE)
     * @param now Date/heure actuelle pour vérifier l'expiration
     * @return true si le jeton existe et est valide
     */
    @Query("SELECT COUNT(t) > 0 FROM AuthToken t WHERE t.tokenValue = :tokenValue AND t.status = :status AND t.expiresAt > :now")
    boolean existsValidToken(@Param("tokenValue") String tokenValue, @Param("status") AuthToken.TokenStatus status, @Param("now") LocalDateTime now);

    /**
     * Trouve les jetons créés depuis une adresse IP spécifique
     * @param clientIp Adresse IP du client
     * @param since Date de début de recherche
     * @return Liste des jetons créés depuis cette IP
     */
    List<AuthToken> findByClientIpAndCreatedAtAfter(String clientIp, LocalDateTime since);

    /**
     * Trouve le dernier jeton d'accès actif d'un utilisateur
     * @param user Utilisateur
     * @param tokenType Type de jeton (ACCESS)
     * @param status Statut (ACTIVE)
     * @return Optional contenant le dernier jeton d'accès
     */
    @Query("SELECT t FROM AuthToken t WHERE t.user = :user AND t.tokenType = :tokenType AND t.status = :status ORDER BY t.createdAt DESC")
    Optional<AuthToken> findLatestActiveTokenByUserAndType(@Param("user") User user, 
                                                           @Param("tokenType") AuthToken.TokenType tokenType, 
                                                           @Param("status") AuthToken.TokenStatus status);
}
