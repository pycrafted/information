package com.newsplatform.repository;

import com.newsplatform.entity.RefreshToken;
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
 * Repository pour la gestion des jetons de rafraîchissement.
 * Couche Persistance : Accès aux données des refresh tokens
 * Permet le renouvellement sécurisé des jetons d'accès.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Trouve un jeton de rafraîchissement par sa valeur
     * @param tokenValue Valeur unique du refresh token
     * @return Optional contenant le jeton si trouvé
     */
    Optional<RefreshToken> findByTokenValue(String tokenValue);

    /**
     * Trouve tous les jetons de rafraîchissement valides d'un utilisateur
     * @param user Utilisateur propriétaire des jetons
     * @param revoked Statut de révocation (false pour les valides)
     * @return Liste des jetons non révoqués de l'utilisateur
     */
    List<RefreshToken> findByUserAndRevoked(User user, Boolean revoked);

    /**
     * Trouve le dernier jeton de rafraîchissement valide d'un utilisateur
     * @param user Utilisateur
     * @param revoked Statut de révocation (false pour les valides)
     * @return Optional contenant le dernier jeton valide
     */
    @Query("SELECT r FROM RefreshToken r WHERE r.user = :user AND r.revoked = :revoked ORDER BY r.createdAt DESC")
    Optional<RefreshToken> findLatestValidTokenByUser(@Param("user") User user, @Param("revoked") Boolean revoked);

    /**
     * Trouve tous les jetons expirés
     * @param now Date/heure actuelle
     * @return Liste des jetons expirés
     */
    @Query("SELECT r FROM RefreshToken r WHERE r.expiresAt < :now")
    List<RefreshToken> findExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Révoque tous les jetons valides d'un utilisateur
     * @param user Utilisateur dont les jetons doivent être révoqués
     * @return Nombre de jetons révoqués
     */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user = :user AND r.revoked = false")
    int revokeAllValidTokensForUser(@Param("user") User user);

    /**
     * Supprime tous les jetons expirés ou révoqués depuis plus de 30 jours
     * @param cutoffDate Date limite (maintenant - 30 jours)
     * @return Nombre de jetons supprimés
     */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :cutoffDate OR (r.revoked = true AND r.createdAt < :cutoffDate)")
    int deleteOldTokens(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Compte le nombre de jetons valides pour un utilisateur
     * @param user Utilisateur
     * @param revoked Statut de révocation (false pour les valides)
     * @return Nombre de jetons valides
     */
    long countByUserAndRevoked(User user, Boolean revoked);

    /**
     * Vérifie si un jeton existe et est valide
     * @param tokenValue Valeur du jeton
     * @param revoked Statut de révocation (false pour valide)
     * @param now Date/heure actuelle pour vérifier l'expiration
     * @return true si le jeton existe et est valide
     */
    @Query("SELECT COUNT(r) > 0 FROM RefreshToken r WHERE r.tokenValue = :tokenValue AND r.revoked = :revoked AND r.expiresAt > :now")
    boolean existsValidToken(@Param("tokenValue") String tokenValue, @Param("revoked") Boolean revoked, @Param("now") LocalDateTime now);

    /**
     * Trouve les jetons créés depuis une adresse IP spécifique
     * @param clientIp Adresse IP du client
     * @param since Date de début de recherche
     * @return Liste des jetons créés depuis cette IP
     */
    List<RefreshToken> findByClientIpAndCreatedAtAfter(String clientIp, LocalDateTime since);

    /**
     * Trouve les jetons utilisés récemment (dans les dernières 24h)
     * @param since Date limite (maintenant - 24h)
     * @return Liste des jetons utilisés récemment
     */
    @Query("SELECT r FROM RefreshToken r WHERE r.lastUsedAt > :since AND r.revoked = false")
    List<RefreshToken> findRecentlyUsedTokens(@Param("since") LocalDateTime since);

    /**
     * Trouve les jetons surutilisés (plus de 100 utilisations)
     * @param usageThreshold Seuil d'utilisation (100)
     * @return Liste des jetons surutilisés
     */
    @Query("SELECT r FROM RefreshToken r WHERE r.usageCount > :usageThreshold AND r.revoked = false")
    List<RefreshToken> findOverusedTokens(@Param("usageThreshold") Integer usageThreshold);

    /**
     * Met à jour les statistiques d'utilisation d'un jeton
     * @param tokenValue Valeur du jeton
     * @param now Date/heure de dernière utilisation
     * @return Nombre de jetons mis à jour
     */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.lastUsedAt = :now, r.usageCount = r.usageCount + 1 WHERE r.tokenValue = :tokenValue")
    int updateUsageStats(@Param("tokenValue") String tokenValue, @Param("now") LocalDateTime now);

    /**
     * Trouve tous les jetons d'un utilisateur avec statistiques d'utilisation
     * @param user Utilisateur
     * @return Liste des jetons avec leurs statistiques
     */
    @Query("SELECT r FROM RefreshToken r WHERE r.user = :user ORDER BY r.lastUsedAt DESC NULLS LAST")
    List<RefreshToken> findAllByUserOrderByLastUsed(@Param("user") User user);
}
