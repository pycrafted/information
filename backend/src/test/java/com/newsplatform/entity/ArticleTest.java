package com.newsplatform.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires simplifiés pour l'entité Article
 * Couche Domaine : Tests de base en attendant le refactoring complet
 * 
 * @author Équipe Développement 
 * @version 2.0 - Version simplifiée temporaire
 */
@DisplayName("Tests simplifiés de l'entité Article")
class ArticleTest {

    @Nested
    @DisplayName("Tests de création de base")
    class BasicCreationTests {

        @Test
        @DisplayName("✅ Doit créer un article valide avec constructeur métier")
        void shouldCreateValidArticleWithBusinessConstructor() {
            // Arrange
            User author = new User("editeur", "editeur@test.com", "password123", User.UserRole.EDITEUR);
            Category category = new Category("Technologie", "Articles tech");

            // Act
            Article article = new Article(
                "Guide Spring Boot Complet", 
                "Voici un guide détaillé pour maîtriser Spring Boot et développer des applications robustes qui respecte la limite minimale de caractères.",
                category,
                author
            );

            // Assert
            assertNotNull(article.getId(), "L'ID doit être généré automatiquement");
            assertEquals("Guide Spring Boot Complet", article.getTitle(), "Le titre doit être correct");
            assertEquals("guide-spring-boot-complet", article.getSlug(), "Le slug doit être généré automatiquement");
            assertTrue(article.getContent().contains("Spring Boot"), "Le contenu doit être correct");
            assertEquals(author, article.getAuthor(), "L'auteur doit être défini");
            assertEquals(category, article.getCategory(), "La catégorie doit être définie");
            assertEquals(ArticleStatus.DRAFT, article.getStatus(), "Le statut initial doit être DRAFT");
            assertNotNull(article.getCreatedAt(), "La date de création doit être initialisée");
            assertNotNull(article.getUpdatedAt(), "La date de mise à jour doit être initialisée");
            assertNull(article.getPublishedAt(), "Un brouillon n'a pas de date de publication");
        }

        @Test
        @DisplayName("❌ Doit rejeter un titre invalide")
        void shouldRejectInvalidTitle() {
            // Arrange
            User author = new User("editeur", "editeur@test.com", "password123", User.UserRole.EDITEUR);
            Category category = new Category("Tech", "Description");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Article("", "Contenu très long pour l'article de test qui respecte la limite minimale", category, author),
                "Un titre invalide doit lever une exception"
            );
            
            assertTrue(exception.getMessage().contains("titre"), 
                      "Le message d'erreur doit mentionner le titre");
        }

        @Test
        @DisplayName("❌ Doit rejeter un contenu trop court")
        void shouldRejectTooShortContent() {
            // Arrange
            User author = new User("editeur", "editeur@test.com", "password123", User.UserRole.EDITEUR);
            Category category = new Category("Tech", "Description");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Article("Titre Valide", "Court", category, author),
                "Un contenu trop court doit lever une exception"
            );
            
            assertTrue(exception.getMessage().contains("50 caractères"), 
                      "Le message doit mentionner la limite de 50 caractères");
        }

        @Test
        @DisplayName("❌ Doit rejeter un auteur null")
        void shouldRejectNullAuthor() {
            // Arrange
            Category category = new Category("Tech", "Description");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Article("Titre", "Contenu très long pour valider la règle métier selon nos exigences", category, null),
                "Un auteur null doit lever une exception"
            );
            
            assertTrue(exception.getMessage().contains("auteur"), 
                      "Le message doit mentionner l'auteur");
        }

        @Test
        @DisplayName("❌ Doit rejeter une catégorie null")
        void shouldRejectNullCategory() {
            // Arrange
            User author = new User("editeur", "editeur@test.com", "password123", User.UserRole.EDITEUR);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Article("Titre", "Contenu très long pour valider la règle métier selon nos exigences", null, author),
                "Une catégorie null doit lever une exception"
            );
            
            assertTrue(exception.getMessage().contains("catégorie"), 
                      "Le message doit mentionner la catégorie");
        }
    }

    @Nested
    @DisplayName("Tests des méthodes métier")
    class BusinessMethodsTests {

        @Test
        @DisplayName("✅ Doit publier un article en brouillon")
        void shouldPublishDraftArticle() {
            // Arrange
            User author = new User("editeur", "editeur@test.com", "password123", User.UserRole.EDITEUR);
            Category category = new Category("Tech", "Description");
            Article article = new Article("Titre", "Contenu très long pour l'article de test qui respecte nos règles métier", category, author);

            // Act
            article.publish();

            // Assert
            assertEquals(ArticleStatus.PUBLISHED, article.getStatus(), "Le statut doit être PUBLISHED");
            assertNotNull(article.getPublishedAt(), "La date de publication doit être définie");
            assertTrue(article.getPublishedAt().isBefore(LocalDateTime.now().plusSeconds(1)), 
                      "La date de publication doit être récente");
        }

        @Test
        @DisplayName("✅ Doit archiver un article publié")
        void shouldArchivePublishedArticle() {
            // Arrange
            User author = new User("editeur", "editeur@test.com", "password123", User.UserRole.EDITEUR);
            Category category = new Category("Tech", "Description");
            Article article = new Article("Titre", "Contenu très long pour l'article de test qui respecte nos règles métier", category, author);
            article.publish();

            // Act
            article.archive();

            // Assert
            assertEquals(ArticleStatus.ARCHIVED, article.getStatus(), "Le statut doit être ARCHIVED");
        }

        @Test
        @DisplayName("✅ Doit remettre un article publié en brouillon")
        void shouldMovePublishedArticleBackToDraft() {
            // Arrange
            User author = new User("editeur", "editeur@test.com", "password123", User.UserRole.EDITEUR);
            Category category = new Category("Tech", "Description");
            Article article = new Article("Titre", "Contenu très long pour l'article de test qui respecte nos règles métier", category, author);
            article.publish();

            // Act
            article.moveBackToDraft();

            // Assert
            assertEquals(ArticleStatus.DRAFT, article.getStatus(), "Le statut doit être DRAFT");
            assertNull(article.getPublishedAt(), "La date de publication doit être effacée");
        }

        @Test
        @DisplayName("✅ Doit changer de catégorie")
        void shouldChangeCategory() {
            // Arrange
            User author = new User("editeur", "editeur@test.com", "password123", User.UserRole.EDITEUR);
            Category originalCategory = new Category("Tech", "Description");
            Category newCategory = new Category("Science", "Articles scientifiques");
            Article article = new Article("Titre", "Contenu très long pour l'article de test qui respecte nos règles métier", originalCategory, author);

            // Act
            article.changeCategory(newCategory);

            // Assert
            assertEquals(newCategory, article.getCategory(), "La catégorie doit être mise à jour");
        }
    }

    @Nested
    @DisplayName("Tests de visibilité publique")
    class PublicVisibilityTests {

        @Test
        @DisplayName("✅ Un article publié doit être visible publiquement")
        void shouldShowPublishedArticleAsPubliclyVisible() {
            // Arrange
            User author = new User("editeur", "editeur@test.com", "password123", User.UserRole.EDITEUR);
            Category category = new Category("Tech", "Description");
            Article article = new Article("Titre", "Contenu très long pour l'article de test qui respecte nos règles métier", category, author);
            article.publish();

            // Act & Assert
            assertTrue(article.getStatus().isPubliclyVisible(), "Un article publié doit être visible");
        }

        @Test
        @DisplayName("❌ Un brouillon ne doit pas être visible publiquement")
        void shouldShowDraftArticleAsNotPubliclyVisible() {
            // Arrange
            User author = new User("editeur", "editeur@test.com", "password123", User.UserRole.EDITEUR);
            Category category = new Category("Tech", "Description");
            Article article = new Article("Titre", "Contenu très long pour l'article de test qui respecte nos règles métier", category, author);

            // Act & Assert
            assertFalse(article.getStatus().isPubliclyVisible(), "Un brouillon ne doit pas être visible");
        }

        @Test
        @DisplayName("❌ Un article archivé ne doit pas être visible publiquement")
        void shouldShowArchivedArticleAsNotPubliclyVisible() {
            // Arrange
            User author = new User("editeur", "editeur@test.com", "password123", User.UserRole.EDITEUR);
            Category category = new Category("Tech", "Description");
            Article article = new Article("Titre", "Contenu très long pour l'article de test qui respecte nos règles métier", category, author);
            article.publish();
            article.archive();

            // Act & Assert
            assertFalse(article.getStatus().isPubliclyVisible(), "Un article archivé ne doit pas être visible");
        }
    }

    @Nested
    @DisplayName("Tests des méthodes utilitaires")
    class UtilityMethodsTests {

        @Test
        @DisplayName("✅ Doit implémenter equals et hashCode correctement")
        void shouldImplementEqualsAndHashCodeCorrectly() {
            // Arrange
            User author = new User("editeur", "editeur@test.com", "password123", User.UserRole.EDITEUR);
            Category category = new Category("Tech", "Description");
            Article article1 = new Article("Titre", "Contenu très long pour l'article de test qui respecte nos règles métier", category, author);
            Article article2 = new Article("Titre", "Contenu très long pour l'article de test qui respecte nos règles métier", category, author);

            // Assert
            assertNotEquals(article1, article2, "Deux instances distinctes ne doivent pas être égales");
            assertEquals(article1, article1, "Une instance doit être égale à elle-même");
            assertNotEquals(article1.hashCode(), article2.hashCode(), "Les hashCodes doivent être différents");
        }

        @Test
        @DisplayName("✅ Doit avoir une représentation toString informative")
        void shouldHaveInformativeToString() {
            // Arrange
            User author = new User("editeur", "editeur@test.com", "password123", User.UserRole.EDITEUR);
            Category category = new Category("Technologie", "Description");
            Article article = new Article("Guide Spring Boot", "Contenu très long pour l'article de test qui respecte nos règles métier", category, author);

            // Act
            String toString = article.toString();

            // Assert
            assertTrue(toString.contains("Article"), "ToString doit contenir le nom de la classe");
            assertTrue(toString.contains("Guide Spring Boot"), "ToString doit contenir le titre");
        }
    }
} 