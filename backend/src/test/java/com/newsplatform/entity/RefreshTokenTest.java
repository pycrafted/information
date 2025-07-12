package com.newsplatform.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour l'entité RefreshToken.
 * Couche Domaine : Validation des règles métier de rafraîchissement de jetons
 * 
 * @author News Platform Team
 * @version 1.0
 */
@DisplayName("Tests unitaires - Entité RefreshToken")
class RefreshTokenTest {

    private User testUser;
    private RefreshToken validRefreshToken;
    private final String TEST_TOKEN_VALUE = "refresh-token-12345-abcdef-67890";

    @BeforeEach
    void setUp() {
        // Création d'un utilisateur de test
        testUser = new User("testuser", "test@example.com", "Test123!", User.UserRole.EDITEUR);
        testUser.setActive(true);

        // Création d'un RefreshToken valide pour les tests
        validRefreshToken = new RefreshToken(
            TEST_TOKEN_VALUE,
            testUser,
            LocalDateTime.now().plusDays(7) // Expire dans 7 jours
        );
    }

    @Nested
    @DisplayName("Tests de construction et validation")
    class ConstructionTests {

        @Test
        @DisplayName("Création d'un RefreshToken valide avec tous les paramètres")
        void shouldCreateValidRefreshToken() {
            // Given
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);
            
            // When
            RefreshToken token = new RefreshToken(TEST_TOKEN_VALUE, testUser, expiresAt);
            
            // Then
            assertNotNull(token);
            assertEquals(TEST_TOKEN_VALUE, token.getTokenValue());
            assertEquals(testUser, token.getUser());
            assertEquals(expiresAt, token.getExpiresAt());
            assertFalse(token.getRevoked());
            assertEquals(0, token.getUsageCount());
            assertNotNull(token.getCreatedAt());
            assertNull(token.getLastUsedAt());
            assertNull(token.getClientIp());
            assertNull(token.getUserAgent());
        }

        @Test
        @DisplayName("Création d'un RefreshToken avec constructeur par défaut")
        void shouldCreateRefreshTokenWithDefaultConstructor() {
            // When
            RefreshToken token = new RefreshToken();
            
            // Then
            assertNotNull(token);
            assertFalse(token.getRevoked());
            assertEquals(0, token.getUsageCount());
        }
    }

    @Nested
    @DisplayName("Tests de validation de jeton")
    class ValidationTests {

        @Test
        @DisplayName("Un jeton non révoqué et non expiré doit être valide")
        void shouldBeValidWhenNotRevokedAndNotExpired() {
            // Given - token créé dans setUp() expire dans 7 jours
            
            // When
            boolean isValid = validRefreshToken.isValid();
            
            // Then
            assertTrue(isValid);
        }

        @Test
        @DisplayName("Un jeton révoqué doit être invalide")
        void shouldBeInvalidWhenRevoked() {
            // Given
            validRefreshToken.revoke();
            
            // When
            boolean isValid = validRefreshToken.isValid();
            
            // Then
            assertFalse(isValid);
            assertTrue(validRefreshToken.getRevoked());
        }

        @Test
        @DisplayName("Un jeton expiré doit être invalide")
        void shouldBeInvalidWhenExpired() {
            // Given
            RefreshToken expiredToken = new RefreshToken(
                TEST_TOKEN_VALUE,
                testUser,
                LocalDateTime.now().minusMinutes(1) // Déjà expiré
            );
            
            // When
            boolean isValid = expiredToken.isValid();
            
            // Then
            assertFalse(isValid);
            assertTrue(expiredToken.isExpired());
        }
    }

    @Nested
    @DisplayName("Tests des méthodes métier")
    class BusinessMethodsTests {

        @Test
        @DisplayName("La révocation doit marquer le jeton comme révoqué")
        void shouldMarkAsRevokedWhenRevoked() {
            // Given
            assertFalse(validRefreshToken.getRevoked());
            
            // When
            validRefreshToken.revoke();
            
            // Then
            assertTrue(validRefreshToken.getRevoked());
        }

        @Test
        @DisplayName("La révocation d'un jeton déjà révoqué ne doit pas poser de problème")
        void shouldAllowRevokingAlreadyRevokedToken() {
            // Given
            validRefreshToken.revoke();
            assertTrue(validRefreshToken.getRevoked());
            
            // When & Then - ne doit pas lever d'exception
            assertDoesNotThrow(() -> validRefreshToken.revoke());
            assertTrue(validRefreshToken.getRevoked());
        }

        @Test
        @DisplayName("markAsUsed doit incrémenter le compteur d'utilisation")
        void shouldIncrementUsageCountWhenMarkedAsUsed() {
            // Given
            assertEquals(0, validRefreshToken.getUsageCount());
            assertNull(validRefreshToken.getLastUsedAt());
            
            // When
            validRefreshToken.markAsUsed();
            
            // Then
            assertEquals(1, validRefreshToken.getUsageCount());
            assertNotNull(validRefreshToken.getLastUsedAt());
        }

        @Test
        @DisplayName("markAsUsed multiple fois doit incrémenter à chaque fois")
        void shouldIncrementUsageCountMultipleTimes() {
            // Given
            assertEquals(0, validRefreshToken.getUsageCount());
            
            // When
            validRefreshToken.markAsUsed();
            validRefreshToken.markAsUsed();
            validRefreshToken.markAsUsed();
            
            // Then
            assertEquals(3, validRefreshToken.getUsageCount());
        }

        @Test
        @DisplayName("markAsUsed doit mettre à jour lastUsedAt à chaque utilisation")
        void shouldUpdateLastUsedAtOnEachUse() {
            // Given
            LocalDateTime beforeFirstUse = LocalDateTime.now().minusSeconds(1);
            
            // When
            validRefreshToken.markAsUsed();
            LocalDateTime firstUse = validRefreshToken.getLastUsedAt();
            
            // Attendre un peu puis utiliser à nouveau
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            validRefreshToken.markAsUsed();
            LocalDateTime secondUse = validRefreshToken.getLastUsedAt();
            
            // Then
            assertTrue(firstUse.isAfter(beforeFirstUse));
            assertTrue(secondUse.isAfter(firstUse));
        }

        @Test
        @DisplayName("isExpired doit retourner true pour un jeton expiré")
        void shouldReturnTrueWhenTokenIsExpired() {
            // Given
            RefreshToken expiredToken = new RefreshToken(
                TEST_TOKEN_VALUE,
                testUser,
                LocalDateTime.now().minusMinutes(1) // Déjà expiré
            );
            
            // When
            boolean isExpired = expiredToken.isExpired();
            
            // Then
            assertTrue(isExpired);
        }

        @Test
        @DisplayName("isExpired doit retourner false pour un jeton non expiré")
        void shouldReturnFalseWhenTokenIsNotExpired() {
            // Given - validRefreshToken expire dans 7 jours
            
            // When
            boolean isExpired = validRefreshToken.isExpired();
            
            // Then
            assertFalse(isExpired);
        }
    }

    @Nested
    @DisplayName("Tests des propriétés de sécurité")
    class SecurityPropertiesTests {

        @Test
        @DisplayName("Configuration de l'IP client doit être persistée")
        void shouldPersistClientIp() {
            // Given
            String clientIp = "192.168.1.100";
            
            // When
            validRefreshToken.setClientIp(clientIp);
            
            // Then
            assertEquals(clientIp, validRefreshToken.getClientIp());
        }

        @Test
        @DisplayName("Configuration du User Agent doit être persistée")
        void shouldPersistUserAgent() {
            // Given
            String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
            
            // When
            validRefreshToken.setUserAgent(userAgent);
            
            // Then
            assertEquals(userAgent, validRefreshToken.getUserAgent());
        }
    }

    @Nested
    @DisplayName("Tests des méthodes de rôles et permissions")
    class RolePermissionTests {

        @Test
        @DisplayName("isAdminToken doit retourner false pour un éditeur")
        void shouldReturnFalseForAdminTokenWhenEditor() {
            // Given - testUser est EDITEUR
            
            // When
            boolean isAdminToken = validRefreshToken.isAdminToken();
            
            // Then
            assertFalse(isAdminToken);
        }

        @Test
        @DisplayName("isAdminToken doit retourner true pour un administrateur")
        void shouldReturnTrueForAdminTokenWhenAdmin() {
            // Given
            testUser.setRole(User.UserRole.ADMINISTRATEUR);
            
            // When
            boolean isAdminToken = validRefreshToken.isAdminToken();
            
            // Then
            assertTrue(isAdminToken);
        }

        @Test
        @DisplayName("isEditorToken doit retourner true pour un éditeur")
        void shouldReturnTrueForEditorTokenWhenEditor() {
            // Given - testUser est EDITEUR
            
            // When
            boolean isEditorToken = validRefreshToken.isEditorToken();
            
            // Then
            assertTrue(isEditorToken);
        }

        @Test
        @DisplayName("isEditorToken doit retourner true pour un administrateur")
        void shouldReturnTrueForEditorTokenWhenAdmin() {
            // Given
            testUser.setRole(User.UserRole.ADMINISTRATEUR);
            
            // When
            boolean isEditorToken = validRefreshToken.isEditorToken();
            
            // Then
            assertTrue(isEditorToken);
        }

        @Test
        @DisplayName("isEditorToken doit retourner false pour un visiteur")
        void shouldReturnFalseForEditorTokenWhenVisitor() {
            // Given
            testUser.setRole(User.UserRole.VISITEUR);
            
            // When
            boolean isEditorToken = validRefreshToken.isEditorToken();
            
            // Then
            assertFalse(isEditorToken);
        }
    }

    @Nested
    @DisplayName("Tests des statistiques d'utilisation")
    class UsageStatisticsTests {

        @Test
        @DisplayName("Un nouveau jeton doit avoir des statistiques à zéro")
        void shouldHaveZeroStatisticsWhenNew() {
            // Given - nouveau token
            
            // When & Then
            assertEquals(0, validRefreshToken.getUsageCount());
            assertNull(validRefreshToken.getLastUsedAt());
        }

        @Test
        @DisplayName("isRecentlyUsed doit retourner false si jamais utilisé")
        void shouldReturnFalseForRecentlyUsedWhenNeverUsed() {
            // Given - token jamais utilisé
            
            // When
            boolean isRecentlyUsed = validRefreshToken.isRecentlyUsed();
            
            // Then
            assertFalse(isRecentlyUsed);
        }

        @Test
        @DisplayName("isRecentlyUsed doit retourner true après utilisation")
        void shouldReturnTrueForRecentlyUsedAfterUse() {
            // Given
            validRefreshToken.markAsUsed();
            
            // When
            boolean isRecentlyUsed = validRefreshToken.isRecentlyUsed();
            
            // Then
            assertTrue(isRecentlyUsed);
        }

        @Test
        @DisplayName("isOverused doit retourner false pour usage normal")
        void shouldReturnFalseForOverusedWhenNormalUsage() {
            // Given
            for (int i = 0; i < 50; i++) { // 50 utilisations (seuil : 100)
                validRefreshToken.markAsUsed();
            }
            
            // When
            boolean isOverused = validRefreshToken.isOverused();
            
            // Then
            assertFalse(isOverused);
        }

        @Test
        @DisplayName("isOverused doit retourner true pour usage excessif")
        void shouldReturnTrueForOverusedWhenExcessiveUsage() {
            // Given
            validRefreshToken.setUsageCount(150); // Plus de 100
            
            // When
            boolean isOverused = validRefreshToken.isOverused();
            
            // Then
            assertTrue(isOverused);
        }
    }

    @Nested
    @DisplayName("Tests d'égalité et toString")
    class EqualityAndToStringTests {

        @Test
        @DisplayName("toString doit contenir les informations essentielles")
        void shouldContainEssentialInfoInToString() {
            // Given
            validRefreshToken.setId(UUID.randomUUID());
            validRefreshToken.markAsUsed();
            
            // When
            String tokenString = validRefreshToken.toString();
            
            // Then
            assertTrue(tokenString.contains(validRefreshToken.getId().toString()));
            assertTrue(tokenString.contains(testUser.getUsername()));
            assertTrue(tokenString.contains("false")); // revoked
            assertTrue(tokenString.contains("1")); // usageCount
        }
    }

    @Nested
    @DisplayName("Tests de scénarios métier complets")
    class BusinessScenariosTests {

        @Test
        @DisplayName("Scénario : Cycle de vie complet d'un refresh token")
        void shouldHandleCompleteRefreshTokenLifecycle() {
            // Création
            RefreshToken token = new RefreshToken(
                "lifecycle-token-123",
                testUser,
                LocalDateTime.now().plusDays(7)
            );
            token.setClientIp("192.168.1.100");
            token.setUserAgent("TestAgent/1.0");
            
            // Vérifications initiales
            assertTrue(token.isValid());
            assertEquals(0, token.getUsageCount());
            assertNull(token.getLastUsedAt());
            assertFalse(token.isRecentlyUsed());
            
            // Première utilisation
            token.markAsUsed();
            assertTrue(token.isValid());
            assertEquals(1, token.getUsageCount());
            assertNotNull(token.getLastUsedAt());
            assertTrue(token.isRecentlyUsed());
            
            // Utilisations multiples
            token.markAsUsed();
            token.markAsUsed();
            assertEquals(3, token.getUsageCount());
            
            // Révocation
            token.revoke();
            assertFalse(token.isValid());
            assertTrue(token.getRevoked());
            assertEquals(3, token.getUsageCount()); // Le compteur reste
        }

        @Test
        @DisplayName("Scénario : Token fortement utilisé puis révoqué")
        void shouldHandleHighlyUsedTokenRevocation() {
            // Given
            validRefreshToken.setUsageCount(150); // Usage excessif
            assertTrue(validRefreshToken.isOverused());
            assertTrue(validRefreshToken.isValid());
            
            // When
            validRefreshToken.revoke();
            
            // Then
            assertFalse(validRefreshToken.isValid());
            assertTrue(validRefreshToken.getRevoked());
            assertTrue(validRefreshToken.isOverused()); // Stats restent
            assertEquals(150, validRefreshToken.getUsageCount());
        }
    }
} 