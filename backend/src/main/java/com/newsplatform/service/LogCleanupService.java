package com.newsplatform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Service responsable du nettoyage et de la gestion des fichiers de logs.
 * 
 * Ce service fait partie de la couche Service et gère :
 * - Le nettoyage automatique des logs au démarrage
 * - L'archivage des logs existants si nécessaire
 * - La rotation et maintenance des fichiers de logs
 * 
 * @author News Platform Team
 * @version 1.0
 * @since 2024
 */
@Service
public class LogCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(LogCleanupService.class);
    
    // Répertoire des logs configuré via application.yml
    @Value("${logging.file.path:./logs}")
    private String logPath;
    
    // Configuration pour l'archivage (optionnel)
    @Value("${app.logs.cleanup.archive-before-delete:true}")
    private boolean archiveBeforeDelete;
    
    // Extensions des fichiers de logs à nettoyer
    private static final List<String> LOG_EXTENSIONS = Arrays.asList(".log", ".log.gz");
    
    // Pattern pour les fichiers d'archive
    private static final DateTimeFormatter ARCHIVE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * Nettoie tous les fichiers de logs du répertoire configuré.
     * 
     * Cette méthode :
     * 1. Vérifie l'existence du répertoire de logs
     * 2. Archive les logs existants si configuré
     * 3. Supprime les fichiers de logs actuels
     * 4. Log les opérations effectuées
     * 
     * @throws IOException en cas d'erreur lors des opérations de fichiers
     */
    public void cleanupLogs() {
        logger.info("🧹 Début du nettoyage des logs au démarrage de l'application");
        
        try {
            Path logsDirectory = Paths.get(logPath);
            
            // Vérification de l'existence du répertoire
            if (!Files.exists(logsDirectory)) {
                logger.info("📁 Le répertoire de logs n'existe pas encore : {}", logPath);
                return;
            }
            
            // Collecte des fichiers de logs à traiter
            List<File> logFiles = collectLogFiles(logsDirectory);
            
            if (logFiles.isEmpty()) {
                logger.info("✅ Aucun fichier de log à nettoyer dans : {}", logPath);
                return;
            }
            
            logger.info("📋 Trouvé {} fichier(s) de log à traiter", logFiles.size());
            
            // Archive des logs si configuré
            if (archiveBeforeDelete) {
                archiveExistingLogs(logFiles);
            }
            
            // Suppression des logs actuels
            deleteCurrentLogs(logFiles);
            
            logger.info("✅ Nettoyage des logs terminé avec succès");
            
        } catch (Exception e) {
            logger.error("❌ Erreur lors du nettoyage des logs : {}", e.getMessage(), e);
            throw new RuntimeException("Échec du nettoyage des logs", e);
        }
    }
    
    /**
     * Collecte tous les fichiers de logs dans le répertoire spécifié.
     * 
     * @param logsDirectory le répertoire à analyser
     * @return la liste des fichiers de logs trouvés
     * @throws IOException en cas d'erreur de lecture du répertoire
     */
    private List<File> collectLogFiles(Path logsDirectory) throws IOException {
        try (Stream<Path> files = Files.walk(logsDirectory, 1)) {
            return files
                .filter(Files::isRegularFile)
                .filter(this::isLogFile)
                .map(Path::toFile)
                .toList();
        }
    }
    
    /**
     * Vérifie si un fichier est un fichier de log basé sur son extension.
     * 
     * @param path le chemin du fichier à vérifier
     * @return true si c'est un fichier de log, false sinon
     */
    private boolean isLogFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return LOG_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }
    
    /**
     * Archive les fichiers de logs existants avant suppression.
     * 
     * @param logFiles la liste des fichiers à archiver
     */
    private void archiveExistingLogs(List<File> logFiles) {
        logger.info("📦 Archivage des logs existants...");
        
        String timestamp = LocalDateTime.now().format(ARCHIVE_FORMATTER);
        Path archiveDirectory = Paths.get(logPath, "archive", timestamp);
        
        try {
            // Création du répertoire d'archive
            Files.createDirectories(archiveDirectory);
            
            // Copie des fichiers vers l'archive
            for (File logFile : logFiles) {
                Path sourcePath = logFile.toPath();
                Path targetPath = archiveDirectory.resolve(logFile.getName());
                
                Files.copy(sourcePath, targetPath);
                logger.debug("📄 Archivé : {} -> {}", sourcePath.getFileName(), targetPath);
            }
            
            logger.info("✅ Archivage terminé dans : {}", archiveDirectory);
            
        } catch (IOException e) {
            logger.warn("⚠️  Erreur lors de l'archivage (logs seront supprimés sans archivage) : {}", e.getMessage());
        }
    }
    
    /**
     * Supprime les fichiers de logs actuels.
     * 
     * @param logFiles la liste des fichiers à supprimer
     */
    private void deleteCurrentLogs(List<File> logFiles) {
        logger.info("🗑️  Suppression des logs actuels...");
        
        int deletedCount = 0;
        long totalSize = 0;
        
        for (File logFile : logFiles) {
            try {
                long fileSize = logFile.length();
                
                if (logFile.delete()) {
                    totalSize += fileSize;
                    deletedCount++;
                    logger.debug("🗑️  Supprimé : {} ({})", logFile.getName(), formatFileSize(fileSize));
                } else {
                    logger.warn("⚠️  Impossible de supprimer : {}", logFile.getName());
                }
                
            } catch (Exception e) {
                logger.warn("⚠️  Erreur lors de la suppression de {} : {}", logFile.getName(), e.getMessage());
            }
        }
        
        logger.info("✅ Supprimé {} fichier(s) de log (total : {})", deletedCount, formatFileSize(totalSize));
    }
    
    /**
     * Formate la taille d'un fichier pour l'affichage.
     * 
     * @param bytes la taille en bytes
     * @return la taille formatée (ex: "1.2 MB")
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
} 