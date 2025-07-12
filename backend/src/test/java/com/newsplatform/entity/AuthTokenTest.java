package com.newsplatform.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour l'entité AuthToken.
 * Couche Domaine : Validation des règles métier d'authentification JWT
 * 
 * @author News Platform Team
 * @version 1.0
 */
@DisplayName("Tests unitaires - Entité AuthToken")
class AuthTokenTest {

    private User testUser;
    private AuthToken validAuthToken;
    private final String TEST_TOKEN_VALUE = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.VwHHVQCBzJ88-uFzsjhznKBhkX8lY_xp3-wEz7bLrfyqB8JLs9Y-Oe-yVJ7sQP4r0zzktS6TjCCl7Gj6Q3J4w";

    @BeforeEach
    void setUp() {
        // Création d'un utilisateur de test
        testUser = new User("testuser", "test@example.com", "Test123!", User.UserRole.EDITEUR);
        testUser.setActive(true);

        // Création d'un AuthToken valide pour les tests
        validAuthToken = new AuthToken(
            TEST_TOKEN_VALUE,
            AuthToken.TokenType.ACCESS,
            testUser,
            LocalDateTime.now().plusHours(1)
        );
    }

    @Nested
    @DisplayName("Tests de construction et validation")
    class ConstructionTests {

        @Test
        @DisplayName("Création d'un AuthToken valide avec tous les paramètres")
        void shouldCreateValidAuthToken() {
            // Given
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
            
            // When
            AuthToken token = new AuthToken(TEST_TOKEN_VALUE, AuthToken.TokenType.ACCESS, testUser, expiresAt);
            
            // Then
            assertNotNull(token);
            assertEquals(TEST_TOKEN_VALUE, token.getTokenValue());
            assertEquals(AuthToken.TokenType.ACCESS, token.getTokenType());
            assertEquals(testUser, token.getUser());
            assertEquals(expiresAt, token.getExpiresAt());
            assertEquals(AuthToken.TokenStatus.ACTIVE, token.getStatus());
            assertNotNull(token.getCreatedAt());
            assertNull(token.getClientIp());
            assertNull(token.getUserAgent());
        }

        @Test
        @DisplayName("Création d'un AuthToken avec constructeur par défaut")
        void shouldCreateAuthTokenWithDefaultConstructor() {
            // When
            AuthToken token = new AuthToken();
            
            // Then
            assertNotNull(token);
            assertEquals(AuthToken.TokenStatus.ACTIVE, token.getStatus());
        }

        @ParameterizedTest
        @EnumSource(AuthToken.TokenType.class)
        @DisplayName("Création d'un AuthToken avec tous les types de jeton")
        void shouldCreateAuthTokenWithAllTokenTypes(AuthToken.TokenType tokenType) {
            // Given
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
            
            // When
            AuthToken token = new AuthToken(TEST_TOKEN_VALUE, tokenType, testUser, expiresAt);
            
            // Then
            assertEquals(tokenType, token.getTokenType());
        }
    }

    @Nested
    @DisplayName("Tests de validation de jeton")
    class ValidationTests {

        @Test
        @DisplayName("Un jeton actif et non expiré doit être valide")
        void shouldBeValidWhenActiveAndNotExpired() {
            // Given - token créé dans setUp() expire dans 1 heure
            
            // When
            boolean isValid = validAuthToken.isValid();
            
            // Then
            assertTrue(isValid);
        }

        @Test
        @DisplayName("Un jeton révoqué doit être invalide")
        void shouldBeInvalidWhenRevoked() {
            // Given
            validAuthToken.revoke();
            
            // When
            boolean isValid = validAuthToken.isValid();
            
            // Then
            assertFalse(isValid);
            assertEquals(AuthToken.TokenStatus.REVOKED, validAuthToken.getStatus());
        }

        @Test
        @DisplayName("Un jeton expiré doit être invalide")
        void shouldBeInvalidWhenExpired() {
            // Given
            AuthToken expiredToken = new AuthToken(
                TEST_TOKEN_VALUE,
                AuthToken.TokenType.ACCESS,
                testUser,
                LocalDateTime.now().minusMinutes(1) // Déjà expiré
            );
            
            // When
            boolean isValid = expiredToken.isValid();
            
            // Then
            assertFalse(isValid);
            assertTrue(expiredToken.isExpired());
        }

        @Test
        @DisplayName("Un jeton avec statut EXPIRED doit être invalide")
        void shouldBeInvalidWhenStatusIsExpired() {
            // Given
            validAuthToken.setStatus(AuthToken.TokenStatus.EXPIRED);
            
            // When
            boolean isValid = validAuthToken.isValid();
            
            // Then
            assertFalse(isValid);
        }
    }

    @Nested
    @DisplayName("Tests des méthodes métier")
    class BusinessMethodsTests {

        @Test
        @DisplayName("La révocation doit changer le statut à REVOKED")
        void shouldChangeStatusToRevokedWhenRevoked() {
            // Given
            assertEquals(AuthToken.TokenStatus.ACTIVE, validAuthToken.getStatus());
            
            // When
            validAuthToken.revoke();
            
            // Then
            assertEquals(AuthToken.TokenStatus.REVOKED, validAuthToken.getStatus());
        }

        @Test
        @DisplayName("La révocation d'un jeton déjà révoqué ne doit pas poser de problème")
        void shouldAllowRevokingAlreadyRevokedToken() {
            // Given
            validAuthToken.revoke();
            assertEquals(AuthToken.TokenStatus.REVOKED, validAuthToken.getStatus());
            
            // When & Then - ne doit pas lever d'exception
            assertDoesNotThrow(() -> validAuthToken.revoke());
            assertEquals(AuthToken.TokenStatus.REVOKED, validAuthToken.getStatus());
        }

        @Test
        @DisplayName("markAsExpired doit changer le statut à EXPIRED")
        void shouldChangeStatusToExpiredWhenMarkedAsExpired() {
            // Given
            assertEquals(AuthToken.TokenStatus.ACTIVE, validAuthToken.getStatus());
            
            // When
            validAuthToken.markAsExpired();
            
            // Then
            assertEquals(AuthToken.TokenStatus.EXPIRED, validAuthToken.getStatus());
        }

        @Test
        @DisplayName("isExpired doit retourner true pour un jeton expiré")
        void shouldReturnTrueWhenTokenIsExpired() {
            // Given
            AuthToken expiredToken = new AuthToken(
                TEST_TOKEN_VALUE,
                AuthToken.TokenType.ACCESS,
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
            // Given - validAuthToken expire dans 1 heure
            
            // When
            boolean isExpired = validAuthToken.isExpired();
            
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
            validAuthToken.setClientIp(clientIp);
            
            // Then
            assertEquals(clientIp, validAuthToken.getClientIp());
        }

        @Test
        @DisplayName("Configuration du User Agent doit être persistée")
        void shouldPersistUserAgent() {
            // Given
            String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
            
            // When
            validAuthToken.setUserAgent(userAgent);
            
            // Then
            assertEquals(userAgent, validAuthToken.getUserAgent());
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
            boolean isAdminToken = validAuthToken.isAdminToken();
            
            // Then
            assertFalse(isAdminToken);
        }

        @Test
        @DisplayName("isAdminToken doit retourner true pour un administrateur")
        void shouldReturnTrueForAdminTokenWhenAdmin() {
            // Given
            testUser.setRole(User.UserRole.ADMINISTRATEUR);
            
            // When
            boolean isAdminToken = validAuthToken.isAdminToken();
            
            // Then
            assertTrue(isAdminToken);
        }

        @Test
        @DisplayName("isEditorToken doit retourner true pour un éditeur")
        void shouldReturnTrueForEditorTokenWhenEditor() {
            // Given - testUser est EDITEUR
            
            // When
            boolean isEditorToken = validAuthToken.isEditorToken();
            
            // Then
            assertTrue(isEditorToken);
        }

        @Test
        @DisplayName("isEditorToken doit retourner true pour un administrateur")
        void shouldReturnTrueForEditorTokenWhenAdmin() {
            // Given
            testUser.setRole(User.UserRole.ADMINISTRATEUR);
            
            // When
            boolean isEditorToken = validAuthToken.isEditorToken();
            
            // Then
            assertTrue(isEditorToken);
        }

        @Test
        @DisplayName("isEditorToken doit retourner false pour un visiteur")
        void shouldReturnFalseForEditorTokenWhenVisitor() {
            // Given
            testUser.setRole(User.UserRole.VISITEUR);
            
            // When
            boolean isEditorToken = validAuthToken.isEditorToken();
            
            // Then
            assertFalse(isEditorToken);
        }
    }

    @Nested
    @DisplayName("Tests des énumérations")
    class EnumerationTests {

        @Test
        @DisplayName("TokenType doit avoir ACCESS et REFRESH")
        void shouldHaveCorrectTokenTypes() {
            // When & Then
            assertEquals(2, AuthToken.TokenType.values().length);
            assertNotNull(AuthToken.TokenType.ACCESS);
            assertNotNull(AuthToken.TokenType.REFRESH);
        }

        @Test
        @DisplayName("TokenStatus doit avoir ACTIVE, REVOKED et EXPIRED")
        void shouldHaveCorrectTokenStatuses() {
            // When & Then
            assertEquals(3, AuthToken.TokenStatus.values().length);
            assertNotNull(AuthToken.TokenStatus.ACTIVE);
            assertNotNull(AuthToken.TokenStatus.REVOKED);
            assertNotNull(AuthToken.TokenStatus.EXPIRED);
        }

        @Test
        @DisplayName("TokenType doit avoir les bonnes descriptions")
        void shouldHaveCorrectTokenTypeDescriptions() {
            // When & Then
            assertEquals("Jeton d'accès aux services", AuthToken.TokenType.ACCESS.getDescription());
            assertEquals("Jeton de renouvellement", AuthToken.TokenType.REFRESH.getDescription());
        }

        @Test
        @DisplayName("TokenStatus doit avoir les bonnes descriptions")
        void shouldHaveCorrectTokenStatusDescriptions() {
            // When & Then
            assertEquals("Jeton actif", AuthToken.TokenStatus.ACTIVE.getDescription());
            assertEquals("Jeton révoqué", AuthToken.TokenStatus.REVOKED.getDescription());
            assertEquals("Jeton expiré", AuthToken.TokenStatus.EXPIRED.getDescription());
        }
    }

    @Nested
    @DisplayName("Tests de toString")
    class ToStringTests {

        @Test
        @DisplayName("toString doit contenir les informations essentielles")
        void shouldContainEssentialInfoInToString() {
            // Given
            validAuthToken.setId(UUID.randomUUID());
            
            // When
            String tokenString = validAuthToken.toString();
            
            // Then
            assertTrue(tokenString.contains(validAuthToken.getId().toString()));
            assertTrue(tokenString.contains("ACCESS"));
            assertTrue(tokenString.contains(testUser.getUsername()));
            assertTrue(tokenString.contains("ACTIVE"));
        }
    }

    @Nested
    @DisplayName("Tests de scénarios métier complets")
    class BusinessScenariosTests {

        @Test
        @DisplayName("Scénario : Cycle de vie complet d'un auth token")
        void shouldHandleCompleteAuthTokenLifecycle() {
            // Création
            AuthToken token = new AuthToken(
                "lifecycle-token-123",
                AuthToken.TokenType.ACCESS,
                testUser,
                LocalDateTime.now().plusHours(1)
            );
            token.setClientIp("192.168.1.100");
            token.setUserAgent("TestAgent/1.0");
            
            // Vérifications initiales
            assertTrue(token.isValid());
            assertEquals(AuthToken.TokenStatus.ACTIVE, token.getStatus());
            assertFalse(token.isExpired());
            assertTrue(token.isEditorToken());
            assertFalse(token.isAdminToken());
            
            // Révocation
            token.revoke();
            assertFalse(token.isValid());
            assertEquals(AuthToken.TokenStatus.REVOKED, token.getStatus());
            
            // Marquer comme expiré (après révocation)
            token.markAsExpired();
            assertEquals(AuthToken.TokenStatus.EXPIRED, token.getStatus());
            assertFalse(token.isValid());
        }

        @Test
        @DisplayName("Scénario : Token administrateur avec permissions complètes")
        void shouldHandleAdminTokenWithFullPermissions() {
            // Given
            testUser.setRole(User.UserRole.ADMINISTRATEUR);
            AuthToken adminToken = new AuthToken(
                "admin-token-456",
                AuthToken.TokenType.ACCESS,
                testUser,
                LocalDateTime.now().plusHours(2)
            );
            
            // When & Then
            assertTrue(adminToken.isValid());
            assertTrue(adminToken.isAdminToken());
            assertTrue(adminToken.isEditorToken()); // Admin est aussi éditeur
            assertEquals(AuthToken.TokenType.ACCESS, adminToken.getTokenType());
        }
    }
} 