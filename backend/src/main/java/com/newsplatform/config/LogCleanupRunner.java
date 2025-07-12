package com.newsplatform.config;

import com.newsplatform.service.LogCleanupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Composant de configuration responsable du nettoyage automatique des logs au démarrage.
 * 
 * Ce composant fait partie de la couche Configuration et :
 * - S'exécute automatiquement au démarrage de l'application
 * - Nettoie les logs existants pour éviter l'accumulation
 * - Peut être activé/désactivé via configuration
 * - S'exécute avec une priorité élevée (Order = 1)
 * 
 * @author News Platform Team
 * @version 1.0
 * @since 2024
 */
@Component
@Order(1) // Priorité élevée pour s'exécuter tôt dans le processus de démarrage
public class LogCleanupRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(LogCleanupRunner.class);

    private final LogCleanupService logCleanupService;

    // Configuration pour activer/désactiver le nettoyage automatique
    @Value("${app.logs.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    // Configuration pour le délai avant nettoyage (en secondes)
    @Value("${app.logs.cleanup.delay-seconds:1}")
    private int delaySeconds;

    /**
     * Constructeur avec injection de dépendance.
     * 
     * @param logCleanupService le service de nettoyage des logs
     */
    @Autowired
    public LogCleanupRunner(LogCleanupService logCleanupService) {
        this.logCleanupService = logCleanupService;
    }

    /**
     * Méthode appelée automatiquement au démarrage de l'application.
     * 
     * Cette méthode :
     * 1. Vérifie si le nettoyage est activé
     * 2. Applique un délai configurable avant le nettoyage
     * 3. Lance le processus de nettoyage des logs
     * 4. Gère les erreurs de manière gracieuse
     * 
     * @param args les arguments de l'application
     */
    @Override
    public void run(ApplicationArguments args) {
        logger.info("🚀 Démarrage de l'application - Vérification du nettoyage des logs");

        // Vérification si le nettoyage est activé
        if (!cleanupEnabled) {
            logger.info("⏭️  Nettoyage des logs désactivé via configuration (app.logs.cleanup.enabled=false)");
            return;
        }

        try {
            // Délai avant nettoyage pour permettre l'initialisation complète
            if (delaySeconds > 0) {
                logger.info("⏱️  Attente de {} seconde(s) avant le nettoyage des logs...", delaySeconds);
                Thread.sleep(delaySeconds * 1000L);
            }

            // Lancement du nettoyage
            logger.info("🎯 Lancement du nettoyage automatique des logs");
            logCleanupService.cleanupLogs();
            
            logger.info("🎉 Nettoyage automatique des logs terminé avec succès");

        } catch (InterruptedException e) {
            logger.warn("⚠️  Interruption lors de l'attente avant nettoyage : {}", e.getMessage());
            Thread.currentThread().interrupt(); // Restauration du flag d'interruption
            
        } catch (Exception e) {
            logger.error("❌ Erreur lors du nettoyage automatique des logs : {}", e.getMessage(), e);
            // Note: On ne relance pas l'exception pour ne pas empêcher le démarrage de l'application
            logger.info("ℹ️  L'application continuera de démarrer malgré l'erreur de nettoyage des logs");
        }
    }
} 