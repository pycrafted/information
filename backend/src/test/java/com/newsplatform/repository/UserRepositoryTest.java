package com.newsplatform.repository;

import com.newsplatform.entity.User;
import com.newsplatform.entity.User.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour le repository UserRepository
 * Couche Persistance : Tests avec base de données H2 en mémoire
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests d'intégration UserRepository")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User adminUser;
    private User editorUser;
    private User visitorUser;
    private User inactiveUser;

    @BeforeEach
    void setUp() {
        // Utilisateur administrateur actif
        adminUser = new User("admin", "admin@example.com", "hashedPassword", UserRole.ADMINISTRATEUR);
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setActive(true);
        adminUser.updateLastLogin();
        adminUser = entityManager.persistAndFlush(adminUser);

        // Utilisateur éditeur actif
        editorUser = new User("editor", "editor@example.com", "hashedPassword", UserRole.EDITEUR);
        editorUser.setFirstName("Editor");
        editorUser.setLastName("User");
        editorUser.setActive(true);
        editorUser.setLastLogin(LocalDateTime.now().minusDays(2));
        editorUser = entityManager.persistAndFlush(editorUser);

        // Utilisateur visiteur actif
        visitorUser = new User("visitor", "visitor@example.com", "hashedPassword", UserRole.VISITEUR);
        visitorUser.setFirstName("Visitor");
        visitorUser.setLastName("User");
        visitorUser.setActive(true);
        visitorUser = entityManager.persistAndFlush(visitorUser);

        // Utilisateur inactif
        inactiveUser = new User("inactive", "inactive@example.com", "hashedPassword", UserRole.VISITEUR);
        inactiveUser.setFirstName("Inactive");
        inactiveUser.setLastName("User");
        inactiveUser.setActive(false);
        inactiveUser = entityManager.persistAndFlush(inactiveUser);

        entityManager.clear();
    }

    @Nested
    @DisplayName("Tests de recherche par attributs uniques")
    class UniqueAttributeSearchTests {

        @Test
        @DisplayName("findByUsername : doit trouver un utilisateur par nom d'utilisateur")
        void shouldFindUserByUsername() {
            Optional<User> result = userRepository.findByUsername("admin");

            assertTrue(result.isPresent());
            assertEquals(adminUser.getId(), result.get().getId());
            assertEquals("admin", result.get().getUsername());
        }

        @Test
        @DisplayName("findByUsername : doit retourner vide si utilisateur inexistant")
        void shouldReturnEmptyWhenUsernameNotFound() {
            Optional<User> result = userRepository.findByUsername("nonexistent");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("findByEmail : doit trouver un utilisateur par email")
        void shouldFindUserByEmail() {
            Optional<User> result = userRepository.findByEmail("editor@example.com");

            assertTrue(result.isPresent());
            assertEquals(editorUser.getId(), result.get().getId());
            assertEquals("editor@example.com", result.get().getEmail());
        }

        @Test
        @DisplayName("findByUsernameOrEmail : doit trouver par username")
        void shouldFindByUsernameOrEmail_WithUsername() {
            Optional<User> result = userRepository.findByUsernameOrEmail("visitor", "unknown@example.com");

            assertTrue(result.isPresent());
            assertEquals(visitorUser.getId(), result.get().getId());
        }

        @Test
        @DisplayName("findByUsernameOrEmail : doit trouver par email")
        void shouldFindByUsernameOrEmail_WithEmail() {
            Optional<User> result = userRepository.findByUsernameOrEmail("unknown", "admin@example.com");

            assertTrue(result.isPresent());
            assertEquals(adminUser.getId(), result.get().getId());
        }
    }

    @Nested
    @DisplayName("Tests de vérification d'existence")
    class ExistenceCheckTests {

        @Test
        @DisplayName("existsByUsername : doit retourner true si username existe")
        void shouldReturnTrueWhenUsernameExists() {
            assertTrue(userRepository.existsByUsername("admin"));
            assertTrue(userRepository.existsByUsername("editor"));
            assertFalse(userRepository.existsByUsername("nonexistent"));
        }

        @Test
        @DisplayName("existsByEmail : doit retourner true si email existe")
        void shouldReturnTrueWhenEmailExists() {
            assertTrue(userRepository.existsByEmail("visitor@example.com"));
            assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
        }

        @Test
        @DisplayName("existsByUsernameOrEmail : doit vérifier username ou email")
        void shouldCheckExistenceByUsernameOrEmail() {
            assertTrue(userRepository.existsByUsernameOrEmail("admin", "unknown@example.com"));
            assertTrue(userRepository.existsByUsernameOrEmail("unknown", "editor@example.com"));
            assertFalse(userRepository.existsByUsernameOrEmail("unknown", "unknown@example.com"));
        }
    }

    @Nested
    @DisplayName("Tests de recherche par rôle")
    class RoleSearchTests {

        @Test
        @DisplayName("findByRole : doit retourner les utilisateurs par rôle")
        void shouldFindUsersByRole() {
            List<User> administrators = userRepository.findByRole(UserRole.ADMINISTRATEUR);
            List<User> editors = userRepository.findByRole(UserRole.EDITEUR);
            List<User> visitors = userRepository.findByRole(UserRole.VISITEUR);

            assertEquals(1, administrators.size());
            assertEquals(adminUser.getId(), administrators.get(0).getId());

            assertEquals(1, editors.size());
            assertEquals(editorUser.getId(), editors.get(0).getId());

            assertEquals(2, visitors.size()); // visitorUser + inactiveUser
        }

        @Test
        @DisplayName("findByRoleAndActiveTrue : doit retourner seulement les utilisateurs actifs")
        void shouldFindActiveUsersByRole() {
            List<User> activeVisitors = userRepository.findByRoleAndActiveTrue(UserRole.VISITEUR);

            assertEquals(1, activeVisitors.size());
            assertEquals(visitorUser.getId(), activeVisitors.get(0).getId());
            assertTrue(activeVisitors.get(0).getActive());
        }

        @Test
        @DisplayName("countByRole : doit compter les utilisateurs par rôle")
        void shouldCountUsersByRole() {
            assertEquals(1, userRepository.countByRole(UserRole.ADMINISTRATEUR));
            assertEquals(1, userRepository.countByRole(UserRole.EDITEUR));
            assertEquals(2, userRepository.countByRole(UserRole.VISITEUR));
        }
    }

    @Nested
    @DisplayName("Tests de recherche par statut d'activation")
    class ActiveStatusSearchTests {

        @Test
        @DisplayName("findByActiveTrue : doit retourner les utilisateurs actifs")
        void shouldFindActiveUsers() {
            List<User> activeUsers = userRepository.findByActiveTrue();

            assertEquals(3, activeUsers.size()); // admin, editor, visitor
            activeUsers.forEach(user -> assertTrue(user.getActive()));
        }

        @Test
        @DisplayName("findByActiveFalse : doit retourner les utilisateurs inactifs")
        void shouldFindInactiveUsers() {
            List<User> inactiveUsers = userRepository.findByActiveFalse();

            assertEquals(1, inactiveUsers.size());
            assertEquals(inactiveUser.getId(), inactiveUsers.get(0).getId());
            assertFalse(inactiveUsers.get(0).getActive());
        }

        @Test
        @DisplayName("countByActiveTrue/False : doit compter par statut")
        void shouldCountByActiveStatus() {
            assertEquals(3, userRepository.countByActiveTrue());
            assertEquals(1, userRepository.countByActiveFalse());
        }
    }

    @Nested
    @DisplayName("Tests de recherche par date de connexion")
    class LastLoginSearchTests {

        @Test
        @DisplayName("findByLastLoginAfter : doit trouver les connexions récentes")
        void shouldFindRecentLogins() {
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            List<User> recentUsers = userRepository.findByLastLoginAfter(yesterday);

            assertEquals(1, recentUsers.size());
            assertEquals(adminUser.getId(), recentUsers.get(0).getId());
        }

        @Test
        @DisplayName("findByLastLoginBefore : doit trouver les anciennes connexions")
        void shouldFindOldLogins() {
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            List<User> oldUsers = userRepository.findByLastLoginBefore(yesterday);

            assertEquals(1, oldUsers.size());
            assertEquals(editorUser.getId(), oldUsers.get(0).getId());
        }

        @Test
        @DisplayName("findByLastLoginIsNull : doit trouver les utilisateurs jamais connectés")
        void shouldFindUsersNeverLoggedIn() {
            List<User> neverLoggedIn = userRepository.findByLastLoginIsNull();

            assertEquals(2, neverLoggedIn.size()); // visitor et inactive
        }

        @Test
        @DisplayName("countUsersConnectedSince : doit compter les connexions depuis une date")
        void shouldCountUsersConnectedSince() {
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            long count = userRepository.countUsersConnectedSince(yesterday);

            assertEquals(1, count); // adminUser uniquement
        }
    }

    @Nested
    @DisplayName("Tests de recherche textuelle")
    class TextSearchTests {

        @Test
        @DisplayName("searchUsers : doit rechercher dans username, email, firstName, lastName")
        void shouldSearchInMultipleFields() {
            List<User> results = userRepository.searchUsers("Admin");

            assertEquals(1, results.size());
            assertEquals(adminUser.getId(), results.get(0).getId());
        }

        @Test
        @DisplayName("searchUsers : doit être insensible à la casse")
        void shouldBeCaseInsensitive() {
            List<User> results = userRepository.searchUsers("EDITOR");

            assertEquals(1, results.size());
            assertEquals(editorUser.getId(), results.get(0).getId());
        }

        @Test
        @DisplayName("searchActiveUsers : doit rechercher seulement parmi les utilisateurs actifs")
        void shouldSearchOnlyActiveUsers() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> results = userRepository.searchActiveUsers("User", pageable);

            assertEquals(3, results.getTotalElements()); // admin, editor, visitor (actifs)
            results.getContent().forEach(user -> assertTrue(user.getActive()));
        }
    }

    @Nested
    @DisplayName("Tests de pagination")
    class PaginationTests {

        @Test
        @DisplayName("findByRole avec pagination")
        void shouldPaginateUsersByRole() {
            // Ajouter plus d'utilisateurs visiteurs pour tester la pagination
            for (int i = 0; i < 3; i++) {
                User extraVisitor = new User("visitor" + i, "visitor" + i + "@example.com", "pwd", UserRole.VISITEUR);
                entityManager.persistAndFlush(extraVisitor);
            }
            entityManager.clear();

            Pageable pageable = PageRequest.of(0, 2);
            Page<User> page = userRepository.findByRole(UserRole.VISITEUR, pageable);

            assertEquals(5, page.getTotalElements()); // 2 originaux + 3 ajoutés
            assertEquals(3, page.getTotalPages());
            assertEquals(2, page.getContent().size());
        }

        @Test
        @DisplayName("findUsersWithFilters : doit filtrer par rôle et statut")
        void shouldFilterByRoleAndStatus() {
            Pageable pageable = PageRequest.of(0, 10);
            
            // Filtrer par rôle uniquement
            Page<User> visitorPage = userRepository.findUsersWithFilters(UserRole.VISITEUR, null, pageable);
            assertEquals(2, visitorPage.getTotalElements());

            // Filtrer par statut uniquement
            Page<User> activePage = userRepository.findUsersWithFilters(null, true, pageable);
            assertEquals(3, activePage.getTotalElements());

            // Filtrer par rôle ET statut
            Page<User> activeVisitorPage = userRepository.findUsersWithFilters(UserRole.VISITEUR, true, pageable);
            assertEquals(1, activeVisitorPage.getTotalElements());
        }
    }

    @Nested
    @DisplayName("Tests de requêtes d'administration")
    class AdminQueriesTests {

        @Test
        @DisplayName("findActiveAdministrators : doit retourner les administrateurs actifs")
        void shouldFindActiveAdministrators() {
            List<User> activeAdmins = userRepository.findActiveAdministrators();

            assertEquals(1, activeAdmins.size());
            assertEquals(adminUser.getId(), activeAdmins.get(0).getId());
            assertEquals(UserRole.ADMINISTRATEUR, activeAdmins.get(0).getRole());
            assertTrue(activeAdmins.get(0).getActive());
        }

        @Test
        @DisplayName("countActiveAdministrators : doit compter les administrateurs actifs")
        void shouldCountActiveAdministrators() {
            long count = userRepository.countActiveAdministrators();

            assertEquals(1, count);
        }

        @Test
        @DisplayName("findUsersNeverLoggedInCreatedBefore : doit trouver les utilisateurs jamais connectés")
        void shouldFindUsersNeverLoggedInCreatedBefore() {
            LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
            List<User> users = userRepository.findUsersNeverLoggedInCreatedBefore(tomorrow);

            assertEquals(2, users.size()); // visitor et inactive
            users.forEach(user -> assertNull(user.getLastLogin()));
        }

        @Test
        @DisplayName("findRecentlyUpdatedUsers : doit trouver les utilisateurs récemment modifiés")
        void shouldFindRecentlyUpdatedUsers() {
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            List<User> recentlyUpdated = userRepository.findRecentlyUpdatedUsers(oneHourAgo);

            assertEquals(4, recentlyUpdated.size()); // tous créés récemment
        }
    }

    @Nested
    @DisplayName("Tests de robustesse")
    class RobustnessTests {

        @Test
        @DisplayName("Doit gérer les recherches avec chaînes vides")
        void shouldHandleEmptyStringSearches() {
            List<User> results = userRepository.searchUsers("");

            assertEquals(4, results.size()); // tous les utilisateurs
        }

        @Test
        @DisplayName("Doit gérer les recherches avec null")
        void shouldHandleNullSearches() {
            assertDoesNotThrow(() -> {
                List<User> results = userRepository.searchUsers(null);
                assertTrue(results.isEmpty());
            });
        }

        @Test
        @DisplayName("Doit maintenir la cohérence des données")
        void shouldMaintainDataConsistency() {
            // Vérifier que tous les utilisateurs créés sont cohérents
            List<User> allUsers = userRepository.findAll();
            assertEquals(4, allUsers.size());

            // Vérifier les contraintes d'unicité
            List<String> usernames = allUsers.stream().map(User::getUsername).toList();
            List<String> emails = allUsers.stream().map(User::getEmail).toList();
            
            assertEquals(4, usernames.stream().distinct().count());
            assertEquals(4, emails.stream().distinct().count());
        }
    }
}
