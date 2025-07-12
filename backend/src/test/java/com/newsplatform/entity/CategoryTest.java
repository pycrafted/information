package com.newsplatform.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour l'entité Category selon les principes DDD
 * Couche Domaine : Tests des règles métier et validations strictes
 * 
 * @author Équipe Développement 
 * @version 2.0 - Refactoring DDD complet
 */
@DisplayName("Tests DDD de l'entité Category")
class CategoryTest {

    @Nested
    @DisplayName("Tests de création avec constructeurs métier")
    class ConstructorTests {

        @Test
        @DisplayName("✅ Doit créer une catégorie racine valide")
        void shouldCreateValidRootCategory() {
            // Arrange & Act
            Category category = new Category("Technologie", "Articles sur la technologie");

            // Assert
            assertNotNull(category.getId(), "L'ID doit être généré automatiquement");
            assertEquals("Technologie", category.getName(), "Le nom doit être correct");
            assertEquals("technologie", category.getSlug(), "Le slug doit être généré automatiquement");
            assertEquals("Articles sur la technologie", category.getDescription(), "La description doit être correcte");
            assertNull(category.getParent(), "Une catégorie racine n'a pas de parent");
            assertNotNull(category.getCreatedAt(), "La date de création doit être initialisée");
            assertNotNull(category.getUpdatedAt(), "La date de mise à jour doit être initialisée");
        }

        @Test
        @DisplayName("✅ Doit créer une sous-catégorie valide")
        void shouldCreateValidSubCategory() {
            // Arrange
            Category parent = new Category("Technologie", "Parent category");

            // Act
            Category child = new Category("Frontend", "Développement côté client", parent);

            // Assert
            assertEquals("Frontend", child.getName(), "Le nom doit être correct");
            assertEquals("frontend", child.getSlug(), "Le slug doit être généré");
            assertEquals("Développement côté client", child.getDescription(), "La description doit être correcte");
            assertEquals(parent, child.getParent(), "Le parent doit être défini");
            assertTrue(parent.getChildren().contains(child), "L'enfant doit être ajouté au parent");
            assertEquals(1, child.getDepth(), "La profondeur doit être 1");
        }

        @Test
        @DisplayName("✅ Doit créer une catégorie avec description nulle")
        void shouldCreateCategoryWithNullDescription() {
            // Arrange & Act
            Category category = new Category("Science", null);

            // Assert
            assertEquals("Science", category.getName(), "Le nom doit être correct");
            assertEquals("science", category.getSlug(), "Le slug doit être généré");
            assertNull(category.getDescription(), "La description peut être nulle");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\n", "\t"})
        @DisplayName("❌ Doit rejeter les noms vides ou blancs")
        void shouldRejectBlankNames(String invalidName) {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Category(invalidName, "Description"),
                "Un nom vide doit lever une exception"
            );
            
            assertTrue(exception.getMessage().contains("nom"), 
                      "Le message d'erreur doit mentionner le nom");
        }

        @Test
        @DisplayName("❌ Doit rejeter un nom null")
        void shouldRejectNullName() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Category(null, "Description"),
                "Un nom null doit lever une exception"
            );
            
            assertTrue(exception.getMessage().contains("nom"), 
                      "Le message d'erreur doit mentionner le nom");
        }

        @Test
        @DisplayName("❌ Doit rejeter un nom trop court")
        void shouldRejectTooShortName() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Category("A", "Description"),
                "Un nom trop court doit lever une exception"
            );
            
            assertTrue(exception.getMessage().contains("2 et 100"), 
                      "Le message doit mentionner la limite de caractères");
        }

        @Test
        @DisplayName("❌ Doit rejeter un nom trop long")
        void shouldRejectTooLongName() {
            // Arrange
            String tooLongName = "A".repeat(101);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Category(tooLongName, "Description"),
                "Un nom trop long doit lever une exception"
            );
            
            assertTrue(exception.getMessage().contains("2 et 100"), 
                      "Le message doit mentionner la limite de caractères");
        }
    }

    @Nested
    @DisplayName("Tests des règles métier de hiérarchie")
    class HierarchyBusinessRulesTests {

        @Test
        @DisplayName("✅ Doit calculer correctement la profondeur")
        void shouldCalculateCorrectDepth() {
            // Arrange
            Category level1 = new Category("Informatique", "Niveau 1");
            Category level2 = new Category("Développement", "Niveau 2", level1);
            Category level3 = new Category("Frontend", "Niveau 3", level2);

            // Act & Assert
            assertEquals(0, level1.getDepth(), "Niveau 1 : profondeur 0");
            assertEquals(1, level2.getDepth(), "Niveau 2 : profondeur 1");
            assertEquals(2, level3.getDepth(), "Niveau 3 : profondeur 2");
        }

        @Test
        @DisplayName("✅ Doit générer le chemin complet correct")
        void shouldGenerateCorrectFullPath() {
            // Arrange
            Category tech = new Category("Technologie", "Tech category");
            Category dev = new Category("Développement", "Dev category", tech);
            Category js = new Category("JavaScript", "JS category", dev);

            // Act & Assert
            assertEquals("Technologie", tech.getFullPath(), "Chemin racine correct");
            assertEquals("Technologie > Développement", dev.getFullPath(), "Chemin niveau 2 correct");
            assertEquals("Technologie > Développement > JavaScript", js.getFullPath(), "Chemin niveau 3 correct");
        }

        @Test
        @DisplayName("❌ Doit rejeter une hiérarchie trop profonde")
        void shouldRejectTooDeepHierarchy() {
            // Arrange - Créer une hiérarchie de 5 niveaux (MAX_DEPTH = 5)
            Category level1 = new Category("Niveau1", "Description");
            Category level2 = new Category("Niveau2", "Description", level1);
            Category level3 = new Category("Niveau3", "Description", level2);
            Category level4 = new Category("Niveau4", "Description", level3);
            Category level5 = new Category("Niveau5", "Description", level4);

            // Act & Assert - Tenter d'ajouter un 6ème niveau
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Category("Niveau6", "Description", level5),
                "Une hiérarchie trop profonde doit être rejetée"
            );
            
            assertTrue(exception.getMessage().contains("profondeur maximale"), 
                      "Le message doit mentionner la profondeur maximale");
        }

        @Test
        @DisplayName("❌ Doit rejeter une catégorie comme son propre parent")
        void shouldRejectSelfAsParent() {
            // Arrange
            Category category = new Category("Test", "Description");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> category.moveToParent(category),
                "Une catégorie ne peut pas être son propre parent"
            );
            
            assertTrue(exception.getMessage().contains("propre parente"), 
                      "Le message doit mentionner l'auto-parenté");
        }
    }

    @Nested
    @DisplayName("Tests des méthodes métier")
    class BusinessMethodsTests {

        @Test
        @DisplayName("✅ Doit ajouter une sous-catégorie")
        void shouldAddSubCategory() {
            // Arrange
            Category parent = new Category("Technologie", "Parent");

            // Act
            parent.addSubCategory("Frontend", "Développement frontend");

            // Assert
            assertEquals(1, parent.getChildren().size(), "Une sous-catégorie doit être ajoutée");
            Category child = parent.getChildren().iterator().next();
            assertEquals("Frontend", child.getName(), "Le nom de l'enfant doit être correct");
            assertEquals("frontend", child.getSlug(), "Le slug de l'enfant doit être correct");
            assertEquals(parent, child.getParent(), "Le parent de l'enfant doit être correct");
        }

        @Test
        @DisplayName("✅ Doit mettre à jour les informations")
        void shouldUpdateInfo() {
            // Arrange
            Category category = new Category("OldName", "Old description");
            LocalDateTime originalUpdatedAt = category.getUpdatedAt();

            // Act - Pause pour s'assurer que la date change
            try { Thread.sleep(10); } catch (InterruptedException e) {}
            category.updateInfo("NewName", "New description");

            // Assert
            assertEquals("NewName", category.getName(), "Le nom doit être mis à jour");
            assertEquals("newname", category.getSlug(), "Le slug doit être régénéré");
            assertEquals("New description", category.getDescription(), "La description doit être mise à jour");
            assertTrue(category.getUpdatedAt().isAfter(originalUpdatedAt), 
                      "La date de mise à jour doit être actualisée");
        }

        @Test
        @DisplayName("✅ Doit déplacer vers un nouveau parent")
        void shouldMoveToNewParent() {
            // Arrange
            Category oldParent = new Category("OldParent", "Description");
            Category newParent = new Category("NewParent", "Description");
            Category child = new Category("Child", "Description", oldParent);

            // Act
            child.moveToParent(newParent);

            // Assert
            assertEquals(newParent, child.getParent(), "Le nouveau parent doit être défini");
            assertFalse(oldParent.getChildren().contains(child), "L'ancien parent ne doit plus contenir l'enfant");
            assertTrue(newParent.getChildren().contains(child), "Le nouveau parent doit contenir l'enfant");
        }

        @Test
        @DisplayName("✅ Doit permettre de devenir une catégorie racine")
        void shouldAllowBecomingRootCategory() {
            // Arrange
            Category parent = new Category("Parent", "Description");
            Category child = new Category("Child", "Description", parent);

            // Act
            child.moveToParent(null);

            // Assert
            assertNull(child.getParent(), "Le parent doit être null");
            assertFalse(parent.getChildren().contains(child), "L'ancien parent ne doit plus contenir l'enfant");
            assertEquals(0, child.getDepth(), "La profondeur doit être 0");
        }

        @Test
        @DisplayName("❌ Doit empêcher la création de cycles")
        void shouldPreventCycles() {
            // Arrange
            Category grandParent = new Category("GrandParent", "Description");
            Category parent = new Category("Parent", "Description", grandParent);
            Category child = new Category("Child", "Description", parent);

            // Act & Assert - Tenter de faire du grand-parent un enfant de l'enfant
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> grandParent.moveToParent(child),
                "Un cycle doit être détecté et rejeté"
            );
            
            assertTrue(exception.getMessage().contains("cycle"), 
                      "Le message doit mentionner le cycle");
        }
    }

    @Nested
    @DisplayName("Tests des règles de suppression")
    class DeletionRulesTests {

        @Test
        @DisplayName("✅ Une catégorie vide peut être supprimée")
        void shouldAllowDeletingEmptyCategory() {
            // Arrange
            Category category = new Category("Empty", "Description");

            // Act & Assert
            assertTrue(category.canBeDeleted(), "Une catégorie vide peut être supprimée");
            assertFalse(category.hasArticles(), "Ne doit pas avoir d'articles");
            assertFalse(category.hasSubCategories(), "Ne doit pas avoir de sous-catégories");
        }

        @Test
        @DisplayName("✅ Doit retirer une sous-catégorie vide")
        void shouldRemoveEmptySubCategory() {
            // Arrange
            Category parent = new Category("Parent", "Description");
            parent.addSubCategory("Child", "Description");
            Category child = parent.getChildren().iterator().next();

            // Act
            parent.removeSubCategory(child);

            // Assert
            assertFalse(parent.getChildren().contains(child), "L'enfant doit être retiré");
            assertNull(child.getParent(), "Le parent de l'enfant doit être null");
        }

        @Test
        @DisplayName("❌ Doit rejeter la suppression d'une sous-catégorie inexistante")
        void shouldRejectRemovingNonExistentSubCategory() {
            // Arrange
            Category parent = new Category("Parent", "Description");
            Category unrelatedChild = new Category("Unrelated", "Description");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parent.removeSubCategory(unrelatedChild),
                "La suppression d'un enfant inexistant doit être rejetée"
            );
            
            assertTrue(exception.getMessage().contains("n'existe pas"), 
                      "Le message doit mentionner que la sous-catégorie n'existe pas");
        }
    }

    @Nested
    @DisplayName("Tests de génération automatique")
    class AutoGenerationTests {

        @Test
        @DisplayName("✅ Doit générer un slug correct")
        void shouldGenerateCorrectSlug() {
            // Arrange & Act
            Category category1 = new Category("Intelligence Artificielle", "Description");
            Category category2 = new Category("Machine Learning & IA", "Description");
            Category category3 = new Category("  Frontend   Development  ", "Description");

            // Assert
            assertEquals("intelligence-artificielle", category1.getSlug(), "Slug avec espaces");
            assertEquals("machine-learning-ia", category2.getSlug(), "Slug avec caractères spéciaux");
            assertEquals("frontend-development", category3.getSlug(), "Slug avec espaces en trop");
        }

        @Test
        @DisplayName("✅ Doit trimmer automatiquement les champs texte")
        void shouldTrimTextFieldsAutomatically() {
            // Arrange & Act
            Category category = new Category("  Technologie  ", "  Description avec espaces  ");

            // Assert
            assertEquals("Technologie", category.getName(), "Le nom doit être trimé");
            assertEquals("Description avec espaces", category.getDescription(), "La description doit être trimée");
        }
    }

    @Nested
    @DisplayName("Tests des méthodes utilitaires")
    class UtilityMethodsTests {

        @Test
        @DisplayName("✅ Doit implémenter equals et hashCode correctement")
        void shouldImplementEqualsAndHashCodeCorrectly() {
            // Arrange
            Category category1 = new Category("Test", "Description");
            Category category2 = new Category("Test", "Description");

            // Assert
            assertNotEquals(category1, category2, "Deux instances distinctes ne doivent pas être égales");
            assertEquals(category1, category1, "Une instance doit être égale à elle-même");
            assertNotEquals(category1.hashCode(), category2.hashCode(), "Les hashCodes doivent être différents");
        }

        @Test
        @DisplayName("✅ Doit avoir une représentation toString informative")
        void shouldHaveInformativeToString() {
            // Arrange
            Category category = new Category("Technologie", "Description");

            // Act
            String toString = category.toString();

            // Assert
            assertTrue(toString.contains("Category"), "ToString doit contenir le nom de la classe");
            assertTrue(toString.contains("Technologie"), "ToString doit contenir le nom");
            assertTrue(toString.contains("technologie"), "ToString doit contenir le slug");
            assertTrue(toString.contains("depth=0"), "ToString doit contenir la profondeur");
        }
    }
} 