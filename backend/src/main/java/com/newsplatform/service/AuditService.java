package com.newsplatform.service;

import com.newsplatform.entity.AuditLog;
import com.newsplatform.entity.User;
import com.newsplatform.exception.BusinessException;
import com.newsplatform.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service pour la gestion des logs d'audit selon les principes DDD.
 * Couche Service : Orchestration de la logique métier d'audit
 * 
 * Responsabilités :
 * - Enregistrement des actions critiques
 * - Gestion de la traçabilité utilisateur
 * - Détection d'activités suspectes
 * - Nettoyage automatique des logs anciens
 * 
 * @author Équipe Développement
 * @version 1.0 - Implémentation complète
 */
@Service
@Transactional
public class AuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);
    
    private final AuditLogRepository auditLogRepository;
    
    @Autowired
    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // ===============================================
    // OPÉRATIONS DE CRÉATION
    // ===============================================
    
    /**
     * Enregistre une action d'audit réussie.
     * 
     * @param user utilisateur qui effectue l'action
     * @param action action effectuée
     * @param details détails de l'action
     * @param resourceType type de ressource concernée
     * @param resourceId ID de la ressource concernée
     * @return log d'audit créé
     */
    @Transactional
    public AuditLog logSuccessfulAction(User user, String action, String details, 
                                      String resourceType, UUID resourceId) {
        try {
            AuditLog auditLog = new AuditLog(user, action, details, resourceType, resourceId);
            AuditLog savedLog = auditLogRepository.save(auditLog);
            logger.debug("Action d'audit enregistrée avec succès - User: {}, Action: {}", 
                        user.getEmail(), action);
            return savedLog;
        } catch (Exception e) {
            logger.error("Erreur lors de l'enregistrement de l'action d'audit", e);
            throw new BusinessException("Impossible d'enregistrer l'action d'audit", e);
        }
    }
    
    /**
     * Enregistre une action d'audit échouée.
     * 
     * @param user utilisateur qui effectue l'action
     * @param action action effectuée
     * @param details détails de l'action
     * @param resourceType type de ressource concernée
     * @param resourceId ID de la ressource concernée
     * @return log d'audit créé
     */
    @Transactional
    public AuditLog logFailedAction(User user, String action, String details, 
                                  String resourceType, UUID resourceId) {
        try {
            AuditLog auditLog = new AuditLog(user, action, details, resourceType, resourceId);
            auditLog.markAsFailed();
            AuditLog savedLog = auditLogRepository.save(auditLog);
            logger.warn("Action d'audit échouée enregistrée - User: {}, Action: {}", 
                       user.getEmail(), action);
            return savedLog;
        } catch (Exception e) {
            logger.error("Erreur lors de l'enregistrement de l'action d'audit échouée", e);
            throw new BusinessException("Impossible d'enregistrer l'action d'audit échouée", e);
        }
    }
    
    /**
     * Enregistre une action d'audit avec informations de session.
     * 
     * @param user utilisateur qui effectue l'action
     * @param action action effectuée
     * @param details détails de l'action
     * @param resourceType type de ressource concernée
     * @param resourceId ID de la ressource concernée
     * @param ipAddress adresse IP
     * @param userAgent user agent
     * @return log d'audit créé
     */
    @Transactional
    public AuditLog logActionWithSession(User user, String action, String details, 
                                       String resourceType, UUID resourceId,
                                       String ipAddress, String userAgent) {
        try {
            AuditLog auditLog = new AuditLog(user, action, details, resourceType, 
                                           resourceId, ipAddress, userAgent);
            AuditLog savedLog = auditLogRepository.save(auditLog);
            logger.debug("Action d'audit avec session enregistrée - User: {}, Action: {}, IP: {}", 
                        user.getEmail(), action, ipAddress);
            return savedLog;
        } catch (Exception e) {
            logger.error("Erreur lors de l'enregistrement de l'action d'audit avec session", e);
            throw new BusinessException("Impossible d'enregistrer l'action d'audit avec session", e);
        }
    }

    // ===============================================
    // OPÉRATIONS DE LECTURE
    // ===============================================
    
    /**
     * Récupère les logs d'audit d'un utilisateur avec pagination.
     * 
     * @param user utilisateur
     * @param pageable configuration de pagination
     * @return page de logs d'audit
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getUserAuditLogs(User user, Pageable pageable) {
        try {
            return auditLogRepository.findByUser(user, pageable);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des logs d'audit utilisateur", e);
            throw new BusinessException("Impossible de récupérer les logs d'audit utilisateur", e);
        }
    }
    
    /**
     * Récupère les logs d'audit par action avec pagination.
     * 
     * @param action action recherchée
     * @param pageable configuration de pagination
     * @return page de logs d'audit
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable) {
        try {
            return auditLogRepository.findByAction(action, pageable);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des logs d'audit par action", e);
            throw new BusinessException("Impossible de récupérer les logs d'audit par action", e);
        }
    }
    
    /**
     * Récupère les logs d'audit dans une plage de dates.
     * 
     * @param startDate date de début
     * @param endDate date de fin
     * @param pageable configuration de pagination
     * @return page de logs d'audit
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, 
                                                 Pageable pageable) {
        try {
            return auditLogRepository.findByTimestampBetween(startDate, endDate, pageable);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des logs d'audit par plage de dates", e);
            throw new BusinessException("Impossible de récupérer les logs d'audit par plage de dates", e);
        }
    }

    // ===============================================
    // OPÉRATIONS DE SÉCURITÉ
    // ===============================================
    
    /**
     * Vérifie s'il y a des tentatives d'authentification échouées récentes.
     * 
     * @param user utilisateur
     * @param since depuis quand
     * @return liste des tentatives échouées
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getRecentFailedLogins(User user, LocalDateTime since) {
        try {
            return auditLogRepository.findRecentFailedLogins(user, since);
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification des tentatives d'authentification échouées", e);
            throw new BusinessException("Impossible de vérifier les tentatives d'authentification échouées", e);
        }
    }
    
    /**
     * Vérifie s'il y a des actions suspectes.
     * 
     * @param user utilisateur
     * @param action action
     * @param since depuis quand
     * @return liste des actions suspectes
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getSuspiciousActions(User user, String action, LocalDateTime since) {
        try {
            return auditLogRepository.findSuspiciousActions(user, action, since);
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification des actions suspectes", e);
            throw new BusinessException("Impossible de vérifier les actions suspectes", e);
        }
    }

    // ===============================================
    // OPÉRATIONS DE STATISTIQUES
    // ===============================================
    
    /**
     * Compte les logs d'audit d'un utilisateur.
     * 
     * @param user utilisateur
     * @return nombre de logs
     */
    @Transactional(readOnly = true)
    public long countUserAuditLogs(User user) {
        try {
            return auditLogRepository.countByUser(user);
        } catch (Exception e) {
            logger.error("Erreur lors du comptage des logs d'audit utilisateur", e);
            throw new BusinessException("Impossible de compter les logs d'audit utilisateur", e);
        }
    }
    
    /**
     * Compte les actions échouées.
     * 
     * @return nombre d'actions échouées
     */
    @Transactional(readOnly = true)
    public long countFailedActions() {
        try {
            return auditLogRepository.countFailedActions();
        } catch (Exception e) {
            logger.error("Erreur lors du comptage des actions échouées", e);
            throw new BusinessException("Impossible de compter les actions échouées", e);
        }
    }

    // ===============================================
    // OPÉRATIONS DE NETTOYAGE
    // ===============================================
    
    /**
     * Nettoie les logs d'audit anciens.
     * 
     * @param cutoffDate date limite
     * @return nombre de logs supprimés
     */
    @Transactional
    public int cleanupOldLogs(LocalDateTime cutoffDate) {
        try {
            int deletedCount = auditLogRepository.deleteOldLogs(cutoffDate);
            logger.info("Nettoyage des logs d'audit - {} logs supprimés avant {}", 
                       deletedCount, cutoffDate);
            return deletedCount;
        } catch (Exception e) {
            logger.error("Erreur lors du nettoyage des logs d'audit anciens", e);
            throw new BusinessException("Impossible de nettoyer les logs d'audit anciens", e);
        }
    }
    
    /**
     * Supprime tous les logs d'audit d'un utilisateur.
     * 
     * @param user utilisateur
     * @return nombre de logs supprimés
     */
    @Transactional
    public int deleteUserAuditLogs(User user) {
        try {
            int deletedCount = auditLogRepository.deleteByUser(user);
            logger.info("Suppression des logs d'audit utilisateur - {} logs supprimés pour {}", 
                       deletedCount, user.getEmail());
            return deletedCount;
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression des logs d'audit utilisateur", e);
            throw new BusinessException("Impossible de supprimer les logs d'audit utilisateur", e);
        }
    }
}