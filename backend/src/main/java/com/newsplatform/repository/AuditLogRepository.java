package com.newsplatform.repository;

import com.newsplatform.entity.AuditLog;
import com.newsplatform.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository pour la gestion des logs d'audit selon les principes DDD.
 * Couche Persistance : Accès optimisé aux données d'audit
 * 
 * Responsabilités :
 * - Requêtes de logs d'audit avec filtres avancés
 * - Gestion de la pagination pour les gros volumes
 * - Requêtes de sécurité et conformité
 * - Support des recherches temporelles
 * 
 * @author Équipe Développement
 * @version 1.0 - Implémentation complète
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    
    // ===============================================
    // REQUÊTES DE BASE OPTIMISÉES
    // ===============================================
    
    /**
     * Trouve les logs d'audit d'un utilisateur avec pagination.
     * 
     * @param user utilisateur dont on veut les logs
     * @param pageable configuration de pagination
     * @return page de logs d'audit
     */
    @Query("SELECT a FROM AuditLog a " +
           "WHERE a.user = :user " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findByUser(@Param("user") User user, Pageable pageable);
    
    /**
     * Trouve les logs d'audit par action avec pagination.
     * 
     * @param action action recherchée
     * @param pageable configuration de pagination
     * @return page de logs d'audit
     */
    @Query("SELECT a FROM AuditLog a " +
           "WHERE a.action = :action " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findByAction(@Param("action") String action, Pageable pageable);

    // ===============================================
    // REQUÊTES DE RECHERCHE AVANCÉE
    // ===============================================
    
    /**
     * Recherche de logs d'audit par plage de dates.
     * 
     * @param startDate date de début
     * @param endDate date de fin
     * @param pageable configuration de pagination
     * @return page de logs dans la plage
     */
    @Query("SELECT a FROM AuditLog a " +
           "WHERE a.timestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findByTimestampBetween(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate,
                                        Pageable pageable);
    
    /**
     * Recherche de logs d'audit par ressource.
     * 
     * @param resourceType type de ressource
     * @param resourceId ID de la ressource
     * @param pageable configuration de pagination
     * @return page de logs pour la ressource
     */
    @Query("SELECT a FROM AuditLog a " +
           "WHERE a.resourceType = :resourceType AND a.resourceId = :resourceId " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findByResource(@Param("resourceType") String resourceType,
                                @Param("resourceId") UUID resourceId,
                                Pageable pageable);
    
    /**
     * Recherche de logs d'audit par statut de succès.
     * 
     * @param success statut de succès
     * @param pageable configuration de pagination
     * @return page de logs avec le statut donné
     */
    @Query("SELECT a FROM AuditLog a " +
           "WHERE a.success = :success " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findBySuccess(@Param("success") boolean success, Pageable pageable);

    // ===============================================
    // REQUÊTES DE STATISTIQUES
    // ===============================================
    
    /**
     * Compte les logs d'audit d'un utilisateur.
     * 
     * @param user utilisateur
     * @return nombre de logs
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.user = :user")
    long countByUser(@Param("user") User user);
    
    /**
     * Compte les logs d'audit par action.
     * 
     * @param action action
     * @return nombre de logs
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action = :action")
    long countByAction(@Param("action") String action);
    
    /**
     * Compte les logs d'audit échoués.
     * 
     * @return nombre de logs échoués
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.success = false")
    long countFailedActions();
    
    /**
     * Compte les logs d'audit dans une plage de dates.
     * 
     * @param startDate date de début
     * @param endDate date de fin
     * @return nombre de logs dans la plage
     */
    @Query("SELECT COUNT(a) FROM AuditLog a " +
           "WHERE a.timestamp BETWEEN :startDate AND :endDate")
    long countByTimestampBetween(@Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate);

    // ===============================================
    // REQUÊTES DE NETTOYAGE
    // ===============================================
    
    /**
     * Supprime les logs d'audit anciens (nettoyage automatique).
     * 
     * @param cutoffDate date limite (logs plus anciens)
     * @return nombre de logs supprimés
     */
    @Query("DELETE FROM AuditLog a WHERE a.timestamp < :cutoffDate")
    int deleteOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Supprime les logs d'audit d'un utilisateur spécifique.
     * 
     * @param user utilisateur
     * @return nombre de logs supprimés
     */
    @Query("DELETE FROM AuditLog a WHERE a.user = :user")
    int deleteByUser(@Param("user") User user);

    // ===============================================
    // REQUÊTES DE SÉCURITÉ
    // ===============================================
    
    /**
     * Trouve les tentatives d'authentification échouées récentes.
     * 
     * @param user utilisateur
     * @param since depuis quand
     * @return liste des tentatives échouées
     */
    @Query("SELECT a FROM AuditLog a " +
           "WHERE a.user = :user AND a.action = 'LOGIN' AND a.success = false " +
           "AND a.timestamp >= :since " +
           "ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentFailedLogins(@Param("user") User user,
                                        @Param("since") LocalDateTime since);
    
    /**
     * Trouve les actions suspectes (échecs répétés).
     * 
     * @param user utilisateur
     * @param action action
     * @param since depuis quand
     * @param maxFailures nombre maximum d'échecs
     * @return liste des actions suspectes
     */
    @Query("SELECT a FROM AuditLog a " +
           "WHERE a.user = :user AND a.action = :action AND a.success = false " +
           "AND a.timestamp >= :since " +
           "ORDER BY a.timestamp DESC")
    List<AuditLog> findSuspiciousActions(@Param("user") User user,
                                       @Param("action") String action,
                                       @Param("since") LocalDateTime since);
}