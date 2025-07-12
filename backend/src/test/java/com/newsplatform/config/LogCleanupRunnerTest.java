package com.newsplatform.config;

import com.newsplatform.service.LogCleanupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.ApplicationArguments;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaires pour le composant de démarrage du nettoyage des logs.
 * 
 * Cette classe teste :
 * - L'exécution normale du nettoyage au démarrage
 * - La désactivation via configuration
 * - La gestion des erreurs du service
 * - Les délais configurables
 * 
 * @author News Platform Team
 * @version 1.0
 * @since 2024
 */
class LogCleanupRunnerTest {

    @Mock
    private LogCleanupService logCleanupService;

    @Mock
    private ApplicationArguments applicationArguments;

    private LogCleanupRunner logCleanupRunner;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        logCleanupRunner = new LogCleanupRunner(logCleanupService);
    }

    /**
     * Test de l'exécution normale avec nettoyage activé.
     */
    @Test
    void testRun_WithCleanupEnabled() {
        // Configuration
        ReflectionTestUtils.setField(logCleanupRunner, "cleanupEnabled", true);
        ReflectionTestUtils.setField(logCleanupRunner, "delaySeconds", 0);

        // Exécution
        assertDoesNotThrow(() -> logCleanupRunner.run(applicationArguments));

        // Vérification
        verify(logCleanupService, times(1)).cleanupLogs();
    }

    /**
     * Test avec nettoyage désactivé.
     */
    @Test
    void testRun_WithCleanupDisabled() {
        // Configuration
        ReflectionTestUtils.setField(logCleanupRunner, "cleanupEnabled", false);

        // Exécution
        assertDoesNotThrow(() -> logCleanupRunner.run(applicationArguments));

        // Vérification - Le service ne doit pas être appelé
        verify(logCleanupService, never()).cleanupLogs();
    }

    /**
     * Test avec délai avant nettoyage.
     */
    @Test
    void testRun_WithDelay() {
        // Configuration
        ReflectionTestUtils.setField(logCleanupRunner, "cleanupEnabled", true);
        ReflectionTestUtils.setField(logCleanupRunner, "delaySeconds", 1);

        // Mesure du temps d'exécution
        long startTime = System.currentTimeMillis();
        
        // Exécution
        assertDoesNotThrow(() -> logCleanupRunner.run(applicationArguments));
        
        long executionTime = System.currentTimeMillis() - startTime;

        // Vérification - L'exécution doit prendre au moins le délai configuré
        assert(executionTime >= 1000); // Au moins 1 seconde
        
        // Vérification - Le service doit être appelé
        verify(logCleanupService, times(1)).cleanupLogs();
    }

    /**
     * Test avec gestion d'erreur du service.
     */
    @Test
    void testRun_WithServiceException() {
        // Configuration
        ReflectionTestUtils.setField(logCleanupRunner, "cleanupEnabled", true);
        ReflectionTestUtils.setField(logCleanupRunner, "delaySeconds", 0);

        // Configuration du service pour lever une exception
        doThrow(new RuntimeException("Erreur de test")).when(logCleanupService).cleanupLogs();

        // Exécution - Ne doit pas lever d'exception (gestion gracieuse)
        assertDoesNotThrow(() -> logCleanupRunner.run(applicationArguments));

        // Vérification - Le service a bien été appelé malgré l'erreur
        verify(logCleanupService, times(1)).cleanupLogs();
    }

    /**
     * Test avec délai zéro (pas d'attente).
     */
    @Test
    void testRun_WithZeroDelay() {
        // Configuration
        ReflectionTestUtils.setField(logCleanupRunner, "cleanupEnabled", true);
        ReflectionTestUtils.setField(logCleanupRunner, "delaySeconds", 0);

        // Exécution
        assertDoesNotThrow(() -> logCleanupRunner.run(applicationArguments));

        // Vérification
        verify(logCleanupService, times(1)).cleanupLogs();
    }

    /**
     * Test avec délai négatif (traité comme zéro).
     */
    @Test
    void testRun_WithNegativeDelay() {
        // Configuration
        ReflectionTestUtils.setField(logCleanupRunner, "cleanupEnabled", true);
        ReflectionTestUtils.setField(logCleanupRunner, "delaySeconds", -1);

        // Exécution
        assertDoesNotThrow(() -> logCleanupRunner.run(applicationArguments));

        // Vérification
        verify(logCleanupService, times(1)).cleanupLogs();
    }

    /**
     * Test de gestion de l'interruption pendant le délai.
     */
    @Test
    void testRun_WithInterruptedException() {
        // Configuration
        ReflectionTestUtils.setField(logCleanupRunner, "cleanupEnabled", true);
        ReflectionTestUtils.setField(logCleanupRunner, "delaySeconds", 5);

        // Création d'un runner qui sera interrompu
        LogCleanupRunner interruptibleRunner = new LogCleanupRunner(logCleanupService) {
            @Override
            public void run(ApplicationArguments args) {
                // Simulation d'interruption
                Thread.currentThread().interrupt();
                super.run(args);
            }
        };
        
        ReflectionTestUtils.setField(interruptibleRunner, "cleanupEnabled", true);
        ReflectionTestUtils.setField(interruptibleRunner, "delaySeconds", 1);

        // Exécution - Doit gérer gracieusement l'interruption
        assertDoesNotThrow(() -> interruptibleRunner.run(applicationArguments));

        // Vérification du flag d'interruption
        assertTrue(Thread.currentThread().isInterrupted());
        
        // Restauration du flag pour les autres tests
        Thread.interrupted();
    }

    /**
     * Test de la construction avec injection de dépendance.
     */
    @Test
    void testConstructor() {
        // Création d'une nouvelle instance
        LogCleanupRunner runner = new LogCleanupRunner(logCleanupService);
        
        // Vérification - L'instance est créée sans erreur
        assertDoesNotThrow(() -> runner.toString());
    }
} 