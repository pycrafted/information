package com.newsplatform.soap;

import com.newsplatform.dto.soap.LoginSoapRequest;
import com.newsplatform.dto.soap.LoginSoapResponse;
import com.newsplatform.dto.soap.LogoutSoapRequest;
import com.newsplatform.dto.soap.LogoutSoapResponse;
import com.newsplatform.entity.User;
import com.newsplatform.repository.UserRepository;
import com.newsplatform.service.AuthSoapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour les services SOAP d'authentification.
 * Couche Intégration : Tests complets du système d'authentification SOAP
 * Valide le fonctionnement selon le cahier des charges.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests d'intégration - Services SOAP d'Authentification")
class AuthEndpointIntegrationTest {

    @Autowired
    private AuthSoapService authSoapService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private final String TEST_PASSWORD = "TestPassword123!";
    private final String TEST_USERNAME = "soapuser";
    private final String TEST_EMAIL = "soapuser@example.com";

    @BeforeEach
    void setUp() {
        // Créer un utilisateur de test pour les services SOAP
        testUser = new User(TEST_USERNAME, TEST_EMAIL, passwordEncoder.encode(TEST_PASSWORD), User.UserRole.EDITEUR);
        testUser.setFirstName("SOAP");
        testUser.setLastName("User");
        testUser.setActive(true);
        testUser = userRepository.save(testUser);
    }

    @Nested
    @DisplayName("Tests d'authentification SOAP (Login)")
    class LoginSoapTests {

        @Test
        @DisplayName("Login SOAP valide doit retourner les jetons JWT")
        void shouldReturnJwtTokensOnValidLogin() {
            // Given
            LoginSoapRequest request = new LoginSoapRequest(
                TEST_USERNAME, 
                TEST_PASSWORD,
                "192.168.1.100",
                "SOAP Client/1.0"
            );

            // When
            LoginSoapResponse response = authSoapService.authenticateUser(request);

            // Then
            assertTrue(response.isSuccess(), "L'authentification doit réussir");
            assertEquals("Authentification réussie", response.getMessage());
            assertNotNull(response.getAccessToken(), "Le jeton d'accès doit être présent");
            assertNotNull(response.getRefreshToken(), "Le jeton de rafraîchissement doit être présent");
            assertNotNull(response.getAccessTokenExpiresAt(), "La date d'expiration du jeton d'accès doit être présente");
            assertNotNull(response.getRefreshTokenExpiresAt(), "La date d'expiration du refresh token doit être présente");
            
            // Vérifier les informations utilisateur
            assertNotNull(response.getUserInfo(), "Les informations utilisateur doivent être présentes");
            assertEquals(TEST_USERNAME, response.getUserInfo().getUsername());
            assertEquals(TEST_EMAIL, response.getUserInfo().getEmail());
            assertEquals("EDITEUR", response.getUserInfo().getRole());
            assertEquals("CRUD Articles + Catégories", response.getUserInfo().getRoleDescription());
        }

        @Test
        @DisplayName("Login SOAP avec email doit fonctionner")
        void shouldLoginWithEmail() {
            // Given
            LoginSoapRequest request = new LoginSoapRequest(
                TEST_EMAIL, // Utiliser l'email au lieu du username
                TEST_PASSWORD,
                "192.168.1.100",
                "SOAP Client/1.0"
            );

            // When
            LoginSoapResponse response = authSoapService.authenticateUser(request);

            // Then
            assertTrue(response.isSuccess(), "L'authentification par email doit réussir");
            assertNotNull(response.getAccessToken());
            assertEquals(TEST_USERNAME, response.getUserInfo().getUsername());
        }

        @Test
        @DisplayName("Login SOAP avec mot de passe incorrect doit échouer")
        void shouldFailOnInvalidPassword() {
            // Given
            LoginSoapRequest request = new LoginSoapRequest(
                TEST_USERNAME,
                "WrongPassword123!",
                "192.168.1.100",
                "SOAP Client/1.0"
            );

            // When
            LoginSoapResponse response = authSoapService.authenticateUser(request);

            // Then
            assertFalse(response.isSuccess(), "L'authentification doit échouer");
            assertEquals("Identifiants invalides", response.getMessage());
            assertNull(response.getAccessToken(), "Aucun jeton ne doit être retourné");
            assertNull(response.getRefreshToken(), "Aucun refresh token ne doit être retourné");
        }

        @Test
        @DisplayName("Login SOAP avec utilisateur inexistant doit échouer")
        void shouldFailOnNonExistentUser() {
            // Given
            LoginSoapRequest request = new LoginSoapRequest(
                "inexistentuser",
                TEST_PASSWORD,
                "192.168.1.100",
                "SOAP Client/1.0"
            );

            // When
            LoginSoapResponse response = authSoapService.authenticateUser(request);

            // Then
            assertFalse(response.isSuccess(), "L'authentification doit échouer");
            assertEquals("Identifiants invalides", response.getMessage());
        }

        @Test
        @DisplayName("Login SOAP avec utilisateur désactivé doit échouer")
        void shouldFailOnDeactivatedUser() {
            // Given
            testUser.setActive(false);
            userRepository.save(testUser);

            LoginSoapRequest request = new LoginSoapRequest(
                TEST_USERNAME,
                TEST_PASSWORD,
                "192.168.1.100",
                "SOAP Client/1.0"
            );

            // When
            LoginSoapResponse response = authSoapService.authenticateUser(request);

            // Then
            assertFalse(response.isSuccess(), "L'authentification doit échouer");
            assertEquals("Compte utilisateur désactivé", response.getMessage());
        }

        @Test
        @DisplayName("Login SOAP avec paramètres nulls doit échouer")
        void shouldFailOnNullParameters() {
            // Given
            LoginSoapRequest request = new LoginSoapRequest(null, null, null, null);

            // When
            LoginSoapResponse response = authSoapService.authenticateUser(request);

            // Then
            assertFalse(response.isSuccess(), "L'authentification doit échouer");
            assertEquals("Nom d'utilisateur et mot de passe requis", response.getMessage());
        }
    }

    @Nested
    @DisplayName("Tests de déconnexion SOAP (Logout)")
    class LogoutSoapTests {

        private String validAccessToken;
        private String validRefreshToken;

        @BeforeEach
        void setupTokens() {
            // Obtenir des jetons valides via login
            LoginSoapRequest loginRequest = new LoginSoapRequest(
                TEST_USERNAME,
                TEST_PASSWORD,
                "192.168.1.100",
                "SOAP Client/1.0"
            );
            LoginSoapResponse loginResponse = authSoapService.authenticateUser(loginRequest);
            validAccessToken = loginResponse.getAccessToken();
            validRefreshToken = loginResponse.getRefreshToken();
        }

        @Test
        @DisplayName("Logout SOAP simple doit révoquer le jeton d'accès")
        void shouldRevokeAccessTokenOnSimpleLogout() {
            // Given
            LogoutSoapRequest request = new LogoutSoapRequest(validAccessToken);

            // When
            LogoutSoapResponse response = authSoapService.logoutUser(request);

            // Then
            assertTrue(response.isSuccess(), "La déconnexion doit réussir");
            assertEquals("Déconnexion réussie", response.getMessage());
            assertEquals(1, response.getTokensRevoked(), "Un jeton doit être révoqué");
            assertNotNull(response.getLogoutTimestamp(), "L'horodatage de déconnexion doit être présent");
        }

        @Test
        @DisplayName("Logout SOAP avec refresh token doit révoquer les deux jetons")
        void shouldRevokeBothTokensWhenRefreshTokenProvided() {
            // Given
            LogoutSoapRequest request = new LogoutSoapRequest(validAccessToken, validRefreshToken, false);

            // When
            LogoutSoapResponse response = authSoapService.logoutUser(request);

            // Then
            assertTrue(response.isSuccess(), "La déconnexion doit réussir");
            assertEquals(2, response.getTokensRevoked(), "Deux jetons doivent être révoqués");
        }

        @Test
        @DisplayName("Logout SOAP global doit révoquer tous les jetons utilisateur")
        void shouldRevokeAllTokensOnGlobalLogout() {
            // Given
            LogoutSoapRequest request = new LogoutSoapRequest(validAccessToken, validRefreshToken, true);

            // When
            LogoutSoapResponse response = authSoapService.logoutUser(request);

            // Then
            assertTrue(response.isSuccess(), "La déconnexion globale doit réussir");
            assertEquals("Déconnexion globale réussie", response.getMessage());
            assertTrue(response.getTokensRevoked() >= 2, "Au moins deux jetons doivent être révoqués");
        }

        @Test
        @DisplayName("Logout SOAP avec jeton invalide doit échouer")
        void shouldFailOnInvalidToken() {
            // Given
            LogoutSoapRequest request = new LogoutSoapRequest("invalid-token");

            // When
            LogoutSoapResponse response = authSoapService.logoutUser(request);

            // Then
            assertFalse(response.isSuccess(), "La déconnexion doit échouer");
            assertEquals("Jeton d'accès invalide ou expiré", response.getMessage());
        }

        @Test
        @DisplayName("Logout SOAP sans jeton doit échouer")
        void shouldFailOnMissingToken() {
            // Given
            LogoutSoapRequest request = new LogoutSoapRequest(null);

            // When
            LogoutSoapResponse response = authSoapService.logoutUser(request);

            // Then
            assertFalse(response.isSuccess(), "La déconnexion doit échouer");
            assertEquals("Jeton d'accès requis pour la déconnexion", response.getMessage());
        }
    }

    @Nested
    @DisplayName("Tests de validation d'accès SOAP")
    class AccessValidationTests {

        private String validAccessToken;

        @BeforeEach
        void setupToken() {
            LoginSoapRequest loginRequest = new LoginSoapRequest(
                TEST_USERNAME,
                TEST_PASSWORD,
                "192.168.1.100",
                "SOAP Client/1.0"
            );
            LoginSoapResponse loginResponse = authSoapService.authenticateUser(loginRequest);
            validAccessToken = loginResponse.getAccessToken();
        }

        @Test
        @DisplayName("Validation d'accès SOAP avec jeton valide doit réussir")
        void shouldValidateValidTokenForSoapAccess() {
            // When & Then
            assertDoesNotThrow(() -> {
                User user = authSoapService.validateTokenForSoapAccess(validAccessToken);
                assertEquals(TEST_USERNAME, user.getUsername());
                assertEquals(User.UserRole.EDITEUR, user.getRole());
            });
        }

        @Test
        @DisplayName("Validation d'accès éditeur doit réussir pour un éditeur")
        void shouldValidateEditorAccessForEditor() {
            // When & Then
            assertDoesNotThrow(() -> {
                User user = authSoapService.validateEditorAccess(validAccessToken);
                assertTrue(user.isEditor());
            });
        }

        @Test
        @DisplayName("Validation d'accès admin doit échouer pour un éditeur")
        void shouldFailAdminAccessForEditor() {
            // When & Then
            assertThrows(Exception.class, () -> {
                authSoapService.validateAdminAccess(validAccessToken);
            }, "L'accès admin doit être refusé pour un éditeur");
        }

        @Test
        @DisplayName("Validation d'accès admin doit réussir pour un administrateur")
        void shouldValidateAdminAccessForAdmin() {
            // Given
            testUser.setRole(User.UserRole.ADMINISTRATEUR);
            userRepository.save(testUser);

            // Obtenir un nouveau jeton avec le rôle admin
            LoginSoapRequest loginRequest = new LoginSoapRequest(
                TEST_USERNAME,
                TEST_PASSWORD,
                "192.168.1.100",
                "SOAP Client/1.0"
            );
            LoginSoapResponse loginResponse = authSoapService.authenticateUser(loginRequest);
            String adminToken = loginResponse.getAccessToken();

            // When & Then
            assertDoesNotThrow(() -> {
                User user = authSoapService.validateAdminAccess(adminToken);
                assertTrue(user.isAdministrator());
            });
        }
    }

    @Nested
    @DisplayName("Tests de scénarios complets SOAP")
    class CompleteScenarioTests {

        @Test
        @DisplayName("Scénario complet : Login puis Logout SOAP")
        void shouldHandleCompleteLoginLogoutScenario() {
            // Étape 1: Login
            LoginSoapRequest loginRequest = new LoginSoapRequest(
                TEST_USERNAME,
                TEST_PASSWORD,
                "192.168.1.100",
                "SOAP Client/1.0"
            );
            LoginSoapResponse loginResponse = authSoapService.authenticateUser(loginRequest);

            assertTrue(loginResponse.isSuccess(), "Le login doit réussir");
            assertNotNull(loginResponse.getAccessToken(), "Un jeton d'accès doit être retourné");

            // Étape 2: Validation d'accès
            String accessToken = loginResponse.getAccessToken();
            assertDoesNotThrow(() -> {
                User user = authSoapService.validateTokenForSoapAccess(accessToken);
                assertEquals(TEST_USERNAME, user.getUsername());
            }, "La validation du jeton doit réussir");

            // Étape 3: Logout
            LogoutSoapRequest logoutRequest = new LogoutSoapRequest(
                accessToken,
                loginResponse.getRefreshToken(),
                false
            );
            LogoutSoapResponse logoutResponse = authSoapService.logoutUser(logoutRequest);

            assertTrue(logoutResponse.isSuccess(), "Le logout doit réussir");
            assertEquals(2, logoutResponse.getTokensRevoked(), "Deux jetons doivent être révoqués");

            // Étape 4: Vérification que l'accès est maintenant refusé
            assertThrows(Exception.class, () -> {
                authSoapService.validateTokenForSoapAccess(accessToken);
            }, "L'accès doit être refusé après logout");
        }
    }
} 