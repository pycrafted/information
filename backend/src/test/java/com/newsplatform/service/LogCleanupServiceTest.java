package com.newsplatform.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour le service de nettoyage des logs.
 * 
 * Cette classe teste :
 * - Le nettoyage basique des fichiers de logs
 * - L'archivage des logs existants
 * - La gestion des répertoires inexistants
 * - La gestion des erreurs
 * - Le formatage des tailles de fichiers
 * 
 * @author News Platform Team
 * @version 1.0
 * @since 2024
 */
class LogCleanupServiceTest {

    private LogCleanupService logCleanupService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        logCleanupService = new LogCleanupService();
        
        // Configuration du service avec le répertoire temporaire
        ReflectionTestUtils.setField(logCleanupService, "logPath", tempDir.toString());
        ReflectionTestUtils.setField(logCleanupService, "archiveBeforeDelete", true);
    }

    /**
     * Test du nettoyage avec un répertoire vide.
     */
    @Test
    void testCleanupLogs_WithEmptyDirectory() {
        // Exécution
        assertDoesNotThrow(() -> logCleanupService.cleanupLogs());
    }

    /**
     * Test du nettoyage avec des fichiers de logs existants.
     */
    @Test
    void testCleanupLogs_WithExistingLogFiles() throws IOException {
        // Préparation - Création de fichiers de logs
        createTestLogFile("newsplatform-application.log", "Test log content");
        createTestLogFile("newsplatform-errors.log", "Error log content");
        createTestLogFile("newsplatform-security.log", "Security log content");
        createTestLogFile("old-archive.log.gz", "Archived content");

        // Vérification de l'existence avant nettoyage
        assertEquals(4, countLogFiles());

        // Exécution
        assertDoesNotThrow(() -> logCleanupService.cleanupLogs());

        // Vérification - Les logs doivent être supprimés
        assertEquals(0, countLogFiles());
        
        // Vérification - Le répertoire d'archive doit exister
        Path archiveDir = tempDir.resolve("archive");
        assertTrue(Files.exists(archiveDir));
    }

    /**
     * Test du nettoyage sans archivage.
     */
    @Test
    void testCleanupLogs_WithoutArchiving() throws IOException {
        // Configuration - Désactiver l'archivage
        ReflectionTestUtils.setField(logCleanupService, "archiveBeforeDelete", false);

        // Préparation
        createTestLogFile("test.log", "Test content");

        // Exécution
        assertDoesNotThrow(() -> logCleanupService.cleanupLogs());

        // Vérification - Pas de répertoire d'archive créé
        Path archiveDir = tempDir.resolve("archive");
        assertFalse(Files.exists(archiveDir));
        
        // Vérification - Fichier supprimé
        assertEquals(0, countLogFiles());
    }

    /**
     * Test avec des fichiers non-logs (qui ne doivent pas être touchés).
     */
    @Test
    void testCleanupLogs_WithNonLogFiles() throws IOException {
        // Préparation - Création de fichiers logs et non-logs
        createTestLogFile("application.log", "Log content");
        createTestFile("config.txt", "Config content");
        createTestFile("data.json", "JSON content");

        // Exécution
        assertDoesNotThrow(() -> logCleanupService.cleanupLogs());

        // Vérification - Seuls les fichiers logs sont supprimés
        assertFalse(Files.exists(tempDir.resolve("application.log")));
        assertTrue(Files.exists(tempDir.resolve("config.txt")));
        assertTrue(Files.exists(tempDir.resolve("data.json")));
    }

    /**
     * Test avec un répertoire de logs inexistant.
     */
    @Test
    void testCleanupLogs_WithNonExistentDirectory() {
        // Configuration avec un répertoire inexistant
        Path nonExistentDir = tempDir.resolve("non-existent");
        ReflectionTestUtils.setField(logCleanupService, "logPath", nonExistentDir.toString());

        // Exécution - Ne doit pas lever d'exception
        assertDoesNotThrow(() -> logCleanupService.cleanupLogs());
    }

    /**
     * Test de gestion d'erreur lors de l'accès aux fichiers.
     */
    @Test
    void testCleanupLogs_WithReadOnlyFile() throws IOException {
        // Cette fonctionnalité peut être limitée sur certains systèmes
        // Le test vérifie que l'application gère gracieusement les erreurs
        
        // Préparation
        Path logFile = createTestLogFile("readonly.log", "Content");
        
        // Tentative de rendre le fichier en lecture seule (peut ne pas fonctionner sur tous les OS)
        try {
            logFile.toFile().setReadOnly();
        } catch (Exception ignored) {
            // Ignore si le système ne supporte pas cette opération
        }

        // Exécution - Ne doit pas lever d'exception même en cas d'erreur
        assertDoesNotThrow(() -> logCleanupService.cleanupLogs());
    }

    /**
     * Test de l'archivage avec création du répertoire.
     */
    @Test
    void testCleanupLogs_ArchiveDirectoryCreation() throws IOException {
        // Préparation
        createTestLogFile("test.log", "Test content for archiving");

        // Exécution
        assertDoesNotThrow(() -> logCleanupService.cleanupLogs());

        // Vérification - Le répertoire d'archive existe
        Path archiveDir = tempDir.resolve("archive");
        assertTrue(Files.exists(archiveDir));
        assertTrue(Files.isDirectory(archiveDir));

        // Vérification - Il y a un sous-répertoire avec timestamp
        try (var stream = Files.list(archiveDir)) {
            long subdirCount = stream.filter(Files::isDirectory).count();
            assertEquals(1, subdirCount);
        }
    }

    /**
     * Test avec des fichiers de différentes tailles.
     */
    @Test
    void testCleanupLogs_WithDifferentFileSizes() throws IOException {
        // Préparation - Création de fichiers de différentes tailles
        createTestLogFile("small.log", "Small");
        createTestLogFile("medium.log", "Medium size content for testing");
        createTestLogFile("large.log", "Large content ".repeat(1000));

        // Exécution
        assertDoesNotThrow(() -> logCleanupService.cleanupLogs());

        // Vérification - Tous les fichiers sont supprimés
        assertEquals(0, countLogFiles());
    }

    /**
     * Test de formatage des tailles de fichiers (méthode privée testée indirectement).
     */
    @Test
    void testFormatFileSizeIndirectly() throws IOException {
        // Préparation - Création d'un fichier avec du contenu
        String content = "Test content for size formatting".repeat(100);
        createTestLogFile("size-test.log", content);

        // Exécution - Le formatage est testé indirectement via les logs
        assertDoesNotThrow(() -> logCleanupService.cleanupLogs());
        
        // Le test vérifie principalement que l'exécution se déroule sans erreur
        // Le formatage des tailles est visible dans les logs de l'application
    }

    // ===== MÉTHODES UTILITAIRES POUR LES TESTS =====

    /**
     * Crée un fichier de log de test avec le contenu spécifié.
     */
    private Path createTestLogFile(String fileName, String content) throws IOException {
        return createTestFile(fileName, content);
    }

    /**
     * Crée un fichier de test avec le contenu spécifié.
     */
    private Path createTestFile(String fileName, String content) throws IOException {
        Path filePath = tempDir.resolve(fileName);
        Files.writeString(filePath, content);
        return filePath;
    }

    /**
     * Compte le nombre de fichiers de logs dans le répertoire de test.
     */
    private int countLogFiles() {
        try {
            return (int) Files.list(tempDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String fileName = path.getFileName().toString().toLowerCase();
                        return fileName.endsWith(".log") || fileName.endsWith(".log.gz");
                    })
                    .count();
        } catch (IOException e) {
            return 0;
        }
    }
} 