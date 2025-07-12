package com.newsplatform.entity;

import com.newsplatform.entity.User.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour l'entité User
 * Couche Domaine : Tests des règles métier et validations des rôles
 */
@DisplayName("Tests de l'entité User")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Nested
    @DisplayName("Tests de création et initialisation")
    class CreationAndInitializationTests {

        @Test
        @DisplayName("Doit créer un utilisateur avec les valeurs par défaut")
        void shouldCreateUserWithDefaultValues() {
            User newUser = new User();

            assertNotNull(newUser.getCreatedAt(), "La date de création doit être initialisée");
            assertNotNull(newUser.getUpdatedAt(), "La date de mise à jour doit être initialisée");
            assertEquals(UserRole.VISITEUR, newUser.getRole(), "Le rôle par défaut doit être VISITEUR");
            assertTrue(newUser.getActive(), "L'utilisateur doit être actif par défaut");
            assertNull(newUser.getLastLogin(), "La dernière connexion doit être null par défaut");
        }

        @Test
        @DisplayName("Doit créer un utilisateur avec constructeur paramétré")
        void shouldCreateUserWithParameterizedConstructor() {
            String username = "john_doe";
            String email = "john@example.com";
            String password = "hashedPassword123";
            UserRole role = UserRole.EDITEUR;

            User newUser = new User(username, email, password, role);

            assertEquals(username, newUser.getUsername());
            assertEquals(email, newUser.getEmail());
            assertEquals(password, newUser.getPassword());
            assertEquals(role, newUser.getRole());
            assertNotNull(newUser.getCreatedAt());
            assertNotNull(newUser.getUpdatedAt());
        }

        @Test
        @DisplayName("Doit initialiser les dates automatiquement")
        void shouldInitializeDatesAutomatically() {
            User newUser = new User();
            LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);
            LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

            assertTrue(newUser.getCreatedAt().isAfter(beforeCreation));
            assertTrue(newUser.getCreatedAt().isBefore(afterCreation));
            assertTrue(newUser.getUpdatedAt().isAfter(beforeCreation));
            assertTrue(newUser.getUpdatedAt().isBefore(afterCreation));
        }
    }

    @Nested
    @DisplayName("Tests des rôles utilisateur")
    class UserRoleTests {

        @Test
        @DisplayName("VISITEUR : doit avoir accès lecture uniquement")
        void shouldHaveReadOnlyAccessForVisiteur() {
            user.setRole(UserRole.VISITEUR);

            assertFalse(user.isAdministrator(), "VISITEUR ne doit pas être administrateur");
            assertFalse(user.isEditor(), "VISITEUR ne doit pas être éditeur");
            assertFalse(user.canCreateArticles(), "VISITEUR ne peut pas créer d'articles");
            assertFalse(user.canManageUsers(), "VISITEUR ne peut pas gérer les utilisateurs");
            assertFalse(user.canManageTokens(), "VISITEUR ne peut pas gérer les jetons");
        }

        @Test
        @DisplayName("EDITEUR : doit pouvoir gérer articles et catégories")
        void shouldManageArticlesAndCategoriesForEditor() {
            user.setRole(UserRole.EDITEUR);

            assertFalse(user.isAdministrator(), "EDITEUR ne doit pas être administrateur");
            assertTrue(user.isEditor(), "EDITEUR doit être reconnu comme éditeur");
            assertTrue(user.canCreateArticles(), "EDITEUR peut créer des articles");
            assertFalse(user.canManageUsers(), "EDITEUR ne peut pas gérer les utilisateurs");
            assertFalse(user.canManageTokens(), "EDITEUR ne peut pas gérer les jetons");
        }

        @Test
        @DisplayName("ADMINISTRATEUR : doit avoir tous les droits")
        void shouldHaveFullAccessForAdministrator() {
            user.setRole(UserRole.ADMINISTRATEUR);

            assertTrue(user.isAdministrator(), "ADMINISTRATEUR doit être administrateur");
            assertTrue(user.isEditor(), "ADMINISTRATEUR doit aussi être considéré comme éditeur");
            assertTrue(user.canCreateArticles(), "ADMINISTRATEUR peut créer des articles");
            assertTrue(user.canManageUsers(), "ADMINISTRATEUR peut gérer les utilisateurs");
            assertTrue(user.canManageTokens(), "ADMINISTRATEUR peut gérer les jetons");
        }

        @Test
        @DisplayName("Doit vérifier les descriptions des rôles")
        void shouldHaveCorrectRoleDescriptions() {
            assertEquals("Lecture uniquement", UserRole.VISITEUR.getDescription());
            assertEquals("CRUD Articles + Catégories", UserRole.EDITEUR.getDescription());
            assertEquals("CRUD Utilisateurs + gestion jetons", UserRole.ADMINISTRATEUR.getDescription());
        }
    }

    @Nested
    @DisplayName("Tests des méthodes métier")
    class BusinessMethodTests {

        @Test
        @DisplayName("updateLastLogin : doit mettre à jour la dernière connexion")
        void shouldUpdateLastLogin() {
            LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
            
            user.updateLastLogin();
            
            LocalDateTime afterUpdate = LocalDateTime.now().plusSeconds(1);
            
            assertNotNull(user.getLastLogin(), "La dernière connexion doit être définie");
            assertTrue(user.getLastLogin().isAfter(beforeUpdate));
            assertTrue(user.getLastLogin().isBefore(afterUpdate));
            assertTrue(user.getUpdatedAt().isAfter(beforeUpdate));
            assertTrue(user.getUpdatedAt().isBefore(afterUpdate));
        }

        @Test
        @DisplayName("deactivate : doit désactiver l'utilisateur")
        void shouldDeactivateUser() {
            user.setActive(true);
            LocalDateTime beforeDeactivation = LocalDateTime.now().minusSeconds(1);

            user.deactivate();

            assertFalse(user.getActive(), "L'utilisateur doit être désactivé");
            assertTrue(user.getUpdatedAt().isAfter(beforeDeactivation), 
                      "La date de mise à jour doit être mise à jour");
        }

        @Test
        @DisplayName("activate : doit activer l'utilisateur")
        void shouldActivateUser() {
            user.setActive(false);
            LocalDateTime beforeActivation = LocalDateTime.now().minusSeconds(1);

            user.activate();

            assertTrue(user.getActive(), "L'utilisateur doit être activé");
            assertTrue(user.getUpdatedAt().isAfter(beforeActivation), 
                      "La date de mise à jour doit être mise à jour");
        }
    }

    @Nested
    @DisplayName("Tests des getters et setters")
    class GettersAndSettersTests {

        @Test
        @DisplayName("Doit permettre de définir et récupérer l'ID")
        void shouldSetAndGetId() {
            UUID id = UUID.randomUUID();
            user.setId(id);

            assertEquals(id, user.getId());
        }

        @Test
        @DisplayName("Doit mettre à jour updatedAt lors de la modification du username")
        void shouldUpdateTimestampWhenSettingUsername() {
            LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
            
            user.setUsername("newUsername");
            
            assertEquals("newUsername", user.getUsername());
            assertTrue(user.getUpdatedAt().isAfter(beforeUpdate));
        }

        @Test
        @DisplayName("Doit mettre à jour updatedAt lors de la modification de l'email")
        void shouldUpdateTimestampWhenSettingEmail() {
            LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
            
            user.setEmail("new@example.com");
            
            assertEquals("new@example.com", user.getEmail());
            assertTrue(user.getUpdatedAt().isAfter(beforeUpdate));
        }

        @Test
        @DisplayName("Doit mettre à jour updatedAt lors de la modification du mot de passe")
        void shouldUpdateTimestampWhenSettingPassword() {
            LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
            
            user.setPassword("newHashedPassword");
            
            assertEquals("newHashedPassword", user.getPassword());
            assertTrue(user.getUpdatedAt().isAfter(beforeUpdate));
        }

        @Test
        @DisplayName("Doit permettre de définir les noms")
        void shouldSetAndGetNames() {
            user.setFirstName("John");
            user.setLastName("Doe");

            assertEquals("John", user.getFirstName());
            assertEquals("Doe", user.getLastName());
        }

        @Test
        @DisplayName("Doit mettre à jour updatedAt lors de la modification du rôle")
        void shouldUpdateTimestampWhenSettingRole() {
            LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
            
            user.setRole(UserRole.ADMINISTRATEUR);
            
            assertEquals(UserRole.ADMINISTRATEUR, user.getRole());
            assertTrue(user.getUpdatedAt().isAfter(beforeUpdate));
        }
    }

    @Nested
    @DisplayName("Tests de validation et contraintes")
    class ValidationTests {

        @Test
        @DisplayName("Doit accepter un username valide")
        void shouldAcceptValidUsername() {
            String validUsername = "valid_user123";
            
            assertDoesNotThrow(() -> user.setUsername(validUsername));
            assertEquals(validUsername, user.getUsername());
        }

        @Test
        @DisplayName("Doit accepter un email valide")
        void shouldAcceptValidEmail() {
            String validEmail = "user@example.com";
            
            assertDoesNotThrow(() -> user.setEmail(validEmail));
            assertEquals(validEmail, user.getEmail());
        }

        @Test
        @DisplayName("Doit accepter tous les rôles valides")
        void shouldAcceptAllValidRoles() {
            assertDoesNotThrow(() -> user.setRole(UserRole.VISITEUR));
            assertDoesNotThrow(() -> user.setRole(UserRole.EDITEUR));
            assertDoesNotThrow(() -> user.setRole(UserRole.ADMINISTRATEUR));
        }
    }

    @Nested
    @DisplayName("Tests de scénarios métier complets")
    class BusinessScenariosTests {

        @Test
        @DisplayName("Scénario : Création d'un visiteur complet")
        void shouldCreateCompleteVisitor() {
            User visitor = new User("visitor123", "visitor@example.com", "hashedPwd", UserRole.VISITEUR);
            visitor.setFirstName("Jane");
            visitor.setLastName("Smith");

            assertEquals("visitor123", visitor.getUsername());
            assertEquals("visitor@example.com", visitor.getEmail());
            assertEquals(UserRole.VISITEUR, visitor.getRole());
            assertEquals("Jane", visitor.getFirstName());
            assertEquals("Smith", visitor.getLastName());
            assertTrue(visitor.getActive());
            assertFalse(visitor.canCreateArticles());
        }

        @Test
        @DisplayName("Scénario : Promotion d'un utilisateur")
        void shouldPromoteUser() {
            user.setRole(UserRole.VISITEUR);
            assertFalse(user.canCreateArticles());

            user.setRole(UserRole.EDITEUR);
            assertTrue(user.canCreateArticles());
            assertFalse(user.canManageUsers());

            user.setRole(UserRole.ADMINISTRATEUR);
            assertTrue(user.canCreateArticles());
            assertTrue(user.canManageUsers());
            assertTrue(user.canManageTokens());
        }

        @Test
        @DisplayName("Scénario : Cycle de vie d'un utilisateur")
        void shouldHandleUserLifecycle() {
            // Création
            user.setUsername("testuser");
            user.setEmail("test@example.com");
            user.setRole(UserRole.EDITEUR);
            assertTrue(user.getActive());

            // Première connexion
            user.updateLastLogin();
            assertNotNull(user.getLastLogin());

            // Désactivation
            user.deactivate();
            assertFalse(user.getActive());

            // Réactivation
            user.activate();
            assertTrue(user.getActive());
        }

        @Test
        @DisplayName("Scénario : toString doit contenir les informations essentielles")
        void shouldContainEssentialInfoInToString() {
            user.setId(UUID.randomUUID());
            user.setUsername("testuser");
            user.setEmail("test@example.com");
            user.setRole(UserRole.EDITEUR);
            user.setActive(true);

            String userString = user.toString();

            assertTrue(userString.contains(user.getId().toString()));
            assertTrue(userString.contains("testuser"));
            assertTrue(userString.contains("test@example.com"));
            assertTrue(userString.contains("EDITEUR"));
            assertTrue(userString.contains("true"));
        }
    }
} 