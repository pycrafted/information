package com.newsplatform.repository;

import com.newsplatform.entity.Article;
import com.newsplatform.entity.ArticleStatus;
import com.newsplatform.entity.Category;
import com.newsplatform.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests complets pour CategoryRepository selon les principes DDD.
 * Tests d'intégration avec base H2 en mémoire pour la hiérarchie des catégories.
 * 
 * @author Équipe Développement
 * @version 2.0 - Tests DDD complets hiérarchiques
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests d'intégration - CategoryRepository")
class CategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    // Données de test hiérarchiques
    private User author;
    private Category rootTechnology;
    private Category rootScience;
    private Category webDevelopment; // Enfant de technology
    private Category mobileDevelopment; // Enfant de technology
    private Category frontend; // Enfant de webDevelopment
    private Category backend; // Enfant de webDevelopment

    @BeforeEach
    void setUp() {
        // Arrange - Création de la hiérarchie de test
        author = new User("editeur", "editeur@test.com", "password123", User.UserRole.EDITEUR);
        entityManager.persistAndFlush(author);

        // Catégories racines
        rootTechnology = new Category("Technologie", "Technologies et développement");
        rootScience = new Category("Science", "Sciences et recherche");
        
        entityManager.persistAndFlush(rootTechnology);
        entityManager.persistAndFlush(rootScience);

        // Niveau 2 : Enfants de Technology
        webDevelopment = new Category("Développement Web", "Technologies web", rootTechnology);
        mobileDevelopment = new Category("Développement Mobile", "Applications mobiles", rootTechnology);
        
        entityManager.persistAndFlush(webDevelopment);
        entityManager.persistAndFlush(mobileDevelopment);

        // Niveau 3 : Petits-enfants de Technology
        frontend = new Category("Frontend", "Technologies côté client", webDevelopment);
        backend = new Category("Backend", "Technologies côté serveur", webDevelopment);
        
        entityManager.persistAndFlush(frontend);
        entityManager.persistAndFlush(backend);

        // Ajouter quelques articles pour les tests de comptage
        Article article1 = new Article(
            "Guide React",
            "Guide complet pour apprendre React et développer des interfaces modernes et interactives.",
            frontend,
            author
        );
        article1.publish();

        Article article2 = new Article(
            "API REST Spring Boot",
            "Créer des API REST robustes avec Spring Boot et les meilleures pratiques de sécurité.",
            backend,
            author
        );
        article2.publish();

        Article draftArticle = new Article(
            "Article en brouillon",
            "Contenu en cours de rédaction qui n'est pas encore prêt pour la publication.",
            rootScience,
            author
        );

        entityManager.persistAndFlush(article1);
        entityManager.persistAndFlush(article2);
        entityManager.persistAndFlush(draftArticle);
        entityManager.clear();
    }

    @Nested
    @DisplayName("Tests des requêtes de base optimisées")
    class BaseQueriesTests {

        @Test
        @DisplayName("✅ Doit trouver une catégorie par slug avec hiérarchie")
        void shouldFindBySlugWithHierarchy() {
            // Act
            Optional<Category> result = categoryRepository.findBySlugWithHierarchy("frontend");

            // Assert
            assertThat(result).isPresent();
            Category category = result.get();
            assertThat(category.getName()).isEqualTo("Frontend");
            assertThat(category.getParent()).isNotNull();
            assertThat(category.getParent().getName()).isEqualTo("Développement Web");
        }

        @Test
        @DisplayName("✅ Doit trouver une catégorie par slug simple")
        void shouldFindBySlug() {
            // Act
            Optional<Category> result = categoryRepository.findBySlug("technologie");

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Technologie");
        }

        @Test
        @DisplayName("✅ Doit trouver une catégorie par nom")
        void shouldFindByName() {
            // Act
            Optional<Category> result = categoryRepository.findByName("Science");

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getSlug()).isEqualTo("science");
        }

        @Test
        @DisplayName("❌ Ne doit pas trouver de catégorie inexistante")
        void shouldNotFindNonExistentCategory() {
            // Act
            Optional<Category> result = categoryRepository.findBySlug("inexistant");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tests des requêtes hiérarchiques")
    class HierarchyQueriesTests {

        @Test
        @DisplayName("✅ Doit récupérer toutes les catégories racines")
        void shouldFindRootCategories() {
            // Act
            List<Category> roots = categoryRepository.findRootCategories();

            // Assert
            assertThat(roots).hasSize(2);
            assertThat(roots).extracting(Category::getName)
                .containsExactly("Science", "Technologie"); // Ordre alphabétique
            
            roots.forEach(category -> 
                assertThat(category.getParent()).isNull()
            );
        }

        @Test
        @DisplayName("✅ Doit récupérer les catégories racines avec leurs enfants")
        void shouldFindRootCategoriesWithChildren() {
            // Act
            List<Category> rootsWithChildren = categoryRepository.findRootCategoriesWithChildren();

            // Assert
            assertThat(rootsWithChildren).hasSize(2);
            
            Category tech = rootsWithChildren.stream()
                .filter(c -> c.getName().equals("Technologie"))
                .findFirst()
                .orElseThrow();
            
            assertThat(tech.getChildren()).hasSize(2);
            assertThat(tech.getChildren()).extracting(Category::getName)
                .containsExactlyInAnyOrder("Développement Web", "Développement Mobile");
        }

        @Test
        @DisplayName("✅ Doit récupérer les enfants directs d'une catégorie")
        void shouldFindDirectChildren() {
            // Act
            List<Category> children = categoryRepository.findDirectChildren(webDevelopment);

            // Assert
            assertThat(children).hasSize(2);
            assertThat(children).extracting(Category::getName)
                .containsExactly("Backend", "Frontend"); // Ordre alphabétique
            
            children.forEach(child -> 
                assertThat(child.getParent()).isEqualTo(webDevelopment)
            );
        }

        @Test
        @DisplayName("✅ Doit récupérer une catégorie avec tous ses descendants")
        void shouldFindWithDescendants() {
            // Act
            Optional<Category> result = categoryRepository.findWithDescendants(rootTechnology);

            // Assert
            assertThat(result).isPresent();
            Category tech = result.get();
            assertThat(tech.getChildren()).isNotEmpty();
            
            // Vérifier que les petits-enfants sont aussi chargés
            Category webDev = tech.getChildren().stream()
                .filter(c -> c.getName().equals("Développement Web"))
                .findFirst()
                .orElseThrow();
            
            assertThat(webDev.getChildren()).hasSize(2);
        }

        @Test
        @DisplayName("✅ Doit gérer les catégories sans enfants")
        void shouldHandleCategoriesWithoutChildren() {
            // Act
            List<Category> children = categoryRepository.findDirectChildren(frontend);

            // Assert
            assertThat(children).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tests des requêtes avec comptage d'articles")
    class ArticleCountQueriesTests {

        @Test
        @DisplayName("✅ Doit récupérer les catégories racines avec comptage d'articles")
        void shouldFindRootCategoriesWithArticleCount() {
            // Act
            List<Object[]> results = categoryRepository.findRootCategoriesWithArticleCount();

            // Assert
            assertThat(results).hasSize(2);
            
            // Vérifier le comptage pour chaque catégorie racine
            for (Object[] result : results) {
                Category category = (Category) result[0];
                Long count = (Long) result[1];
                
                if (category.getName().equals("Technologie")) {
                    assertThat(count).isEqualTo(0L); // Pas d'articles directs sur la racine
                } else if (category.getName().equals("Science")) {
                    assertThat(count).isEqualTo(0L); // Le brouillon n'est pas compté
                }
            }
        }

        @Test
        @DisplayName("✅ Doit compter les articles avec descendants")
        void shouldFindCategoryWithTotalArticleCount() {
            // Act
            Optional<Object[]> result = categoryRepository.findCategoryWithTotalArticleCount(rootTechnology.getId());

            // Assert
            assertThat(result).isPresent();
            Object[] data = result.get();
            Category category = (Category) data[0];
            Long totalCount = (Long) data[1];
            
            assertThat(category.getName()).isEqualTo("Technologie");
            assertThat(totalCount).isEqualTo(2L); // Les 2 articles publiés dans les sous-catégories
        }
    }

    @Nested
    @DisplayName("Tests des requêtes de recherche")
    class SearchQueriesTests {

        @Test
        @DisplayName("✅ Doit rechercher des catégories par nom")
        void shouldSearchByName() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<Category> result = categoryRepository.searchByName("dev", pageable);

            // Assert
            assertThat(result.getContent()).hasSize(3); // "Développement Web", "Développement Mobile"
            result.getContent().forEach(category -> 
                assertThat(category.getName().toLowerCase()).contains("dev")
            );
        }

        @Test
        @DisplayName("✅ Doit rechercher des catégories par nom ou description")
        void shouldSearchByNameOrDescription() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<Category> result = categoryRepository.searchByNameOrDescription("technolog", pageable);

            // Assert
            assertThat(result.getContent()).hasSize(3); // Technology + ses enfants qui contiennent "technolog"
        }

        @Test
        @DisplayName("✅ Doit gérer les recherches sans résultats")
        void shouldHandleSearchWithNoResults() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<Category> result = categoryRepository.searchByName("inexistant", pageable);

            // Assert
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Tests des requêtes d'administration")
    class AdminQueriesTests {

        @Test
        @DisplayName("✅ Doit récupérer toutes les catégories avec parent pour administration")
        void shouldFindAllWithParentForAdmin() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<Category> result = categoryRepository.findAllWithParent(pageable);

            // Assert
            assertThat(result.getContent()).hasSize(6); // Toutes les catégories créées
            
            // Vérifier que les relations parent sont chargées
            result.getContent().forEach(category -> {
                if (category.getParent() != null) {
                    assertThat(category.getParent().getName()).isNotNull();
                }
            });
        }

        @Test
        @DisplayName("✅ Doit récupérer les catégories par profondeur maximale")
        void shouldFindByMaxDepth() {
            // Act
            List<Category> depth1 = categoryRepository.findByMaxDepth(1);
            List<Category> depth2 = categoryRepository.findByMaxDepth(2);
            List<Category> depth3 = categoryRepository.findByMaxDepth(3);

            // Assert
            assertThat(depth1).hasSize(2); // Seulement les racines
            assertThat(depth2).hasSize(4); // Racines + niveau 1
            assertThat(depth3).hasSize(6); // Racines + niveau 1 + niveau 2
        }
    }

    @Nested
    @DisplayName("Tests des requêtes de validation métier")
    class ValidationQueriesTests {

        @Test
        @DisplayName("✅ Doit vérifier l'existence d'un slug")
        void shouldCheckSlugExistence() {
            // Act
            boolean exists = categoryRepository.existsBySlug("technologie");
            boolean notExists = categoryRepository.existsBySlug("inexistant");

            // Assert
            assertThat(exists).isTrue();
            assertThat(notExists).isFalse();
        }

        @Test
        @DisplayName("✅ Doit vérifier l'unicité d'un slug pour modification")
        void shouldCheckSlugUniquenessForUpdate() {
            // Act
            boolean existsForOther = categoryRepository.existsBySlugAndIdNot(
                "technologie", 
                rootScience.getId()
            );
            boolean uniqueForSame = categoryRepository.existsBySlugAndIdNot(
                "technologie", 
                rootTechnology.getId()
            );

            // Assert
            assertThat(existsForOther).isTrue(); // Le slug existe pour une autre catégorie
            assertThat(uniqueForSame).isFalse(); // Le slug n'existe pas pour une autre qu'elle-même
        }

        @Test
        @DisplayName("✅ Doit vérifier l'existence d'un nom")
        void shouldCheckNameExistence() {
            // Act
            boolean exists = categoryRepository.existsByName("Technologie");
            boolean notExists = categoryRepository.existsByName("Inexistant");

            // Assert
            assertThat(exists).isTrue();
            assertThat(notExists).isFalse();
        }

        @Test
        @DisplayName("✅ Doit vérifier l'unicité d'un nom pour modification")
        void shouldCheckNameUniquenessForUpdate() {
            // Act
            boolean existsForOther = categoryRepository.existsByNameAndIdNot(
                "Technologie", 
                rootScience.getId()
            );
            boolean uniqueForSame = categoryRepository.existsByNameAndIdNot(
                "Technologie", 
                rootTechnology.getId()
            );

            // Assert
            assertThat(existsForOther).isTrue();
            assertThat(uniqueForSame).isFalse();
        }

        @Test
        @DisplayName("✅ Doit vérifier si une catégorie a des enfants")
        void shouldCheckIfCategoryHasChildren() {
            // Act
            boolean rootHasChildren = categoryRepository.hasChildren(rootTechnology);
            boolean leafHasChildren = categoryRepository.hasChildren(frontend);

            // Assert
            assertThat(rootHasChildren).isTrue();
            assertThat(leafHasChildren).isFalse();
        }

        @Test
        @DisplayName("✅ Doit vérifier si une catégorie a des articles")
        void shouldCheckIfCategoryHasArticles() {
            // Act
            boolean frontendHasArticles = categoryRepository.hasArticles(frontend);
            boolean emptyHasArticles = categoryRepository.hasArticles(mobileDevelopment);

            // Assert
            assertThat(frontendHasArticles).isTrue();
            assertThat(emptyHasArticles).isFalse();
        }
    }

    @Nested
    @DisplayName("Tests des requêtes de statistiques")
    class StatisticsQueriesTests {

        @Test
        @DisplayName("✅ Doit compter toutes les catégories")
        void shouldCountAllCategories() {
            // Act
            long count = categoryRepository.countAll();

            // Assert
            assertThat(count).isEqualTo(6); // Toutes les catégories créées
        }

        @Test
        @DisplayName("✅ Doit compter les catégories racines")
        void shouldCountRootCategories() {
            // Act
            long count = categoryRepository.countRootCategories();

            // Assert
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("✅ Doit compter les enfants d'une catégorie")
        void shouldCountChildren() {
            // Act
            long techChildren = categoryRepository.countChildren(rootTechnology);
            long webDevChildren = categoryRepository.countChildren(webDevelopment);
            long frontendChildren = categoryRepository.countChildren(frontend);

            // Assert
            assertThat(techChildren).isEqualTo(2);
            assertThat(webDevChildren).isEqualTo(2);
            assertThat(frontendChildren).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Tests des requêtes de suppression sécurisées")
    class SafeDeletionQueriesTests {

        @Test
        @DisplayName("✅ Doit trouver les catégories supprimables")
        void shouldFindDeletableCategories() {
            // Act
            List<Category> deletable = categoryRepository.findDeletableCategories();

            // Assert
            // Seule mobileDevelopment peut être supprimée (pas d'enfants ni d'articles)
            assertThat(deletable).hasSize(1);
            assertThat(deletable.get(0).getName()).isEqualTo("Développement Mobile");
        }

        @Test
        @DisplayName("✅ Doit exclure les catégories avec enfants ou articles")
        void shouldExcludeCategoriesWithChildrenOrArticles() {
            // Act
            List<Category> deletable = categoryRepository.findDeletableCategories();

            // Assert
            List<String> deletableNames = deletable.stream()
                .map(Category::getName)
                .toList();
            
            // Ces catégories ne doivent PAS être supprimables
            assertThat(deletableNames).doesNotContain(
                "Technologie",      // A des enfants
                "Développement Web", // A des enfants
                "Frontend",         // A des articles
                "Backend",          // A des articles
                "Science"           // A des articles (même en brouillon)
            );
        }
    }

    @Nested
    @DisplayName("Tests de cas limites et performance")
    class EdgeCasesAndPerformanceTests {

        @Test
        @DisplayName("✅ Doit gérer les requêtes sur des hiérarchies profondes")
        void shouldHandleDeepHierarchies() {
            // Arrange - Créer une hiérarchie plus profonde
            Category level4 = new Category("JavaScript", "Langage JavaScript", frontend);
            Category level5 = new Category("React", "Bibliothèque React", level4);
            entityManager.persistAndFlush(level4);
            entityManager.persistAndFlush(level5);
            entityManager.clear();

            // Act
            List<Category> allCategories = categoryRepository.findAll();

            // Assert
            assertThat(allCategories).hasSize(8); // 6 + 2 nouvelles
            
            // Vérifier que la profondeur est correctement calculée
            Category react = categoryRepository.findByName("React").orElseThrow();
            assertThat(react.getDepth()).isEqualTo(4);
        }

        @Test
        @DisplayName("✅ Doit optimiser les requêtes avec JOIN FETCH")
        void shouldOptimizeQueriesWithJoinFetch() {
            // Act - Cette requête doit charger les relations en une fois
            List<Category> rootsWithChildren = categoryRepository.findRootCategoriesWithChildren();

            // Assert - Vérifier que les relations sont déjà chargées
            assertThat(rootsWithChildren).isNotEmpty();
            rootsWithChildren.forEach(category -> {
                if (!category.getChildren().isEmpty()) {
                    // Ces accès ne doivent pas déclencher de requêtes supplémentaires
                    category.getChildren().forEach(child -> 
                        assertThat(child.getName()).isNotNull()
                    );
                }
            });
        }

        @Test
        @DisplayName("✅ Doit gérer les recherches insensibles à la casse")
        void shouldHandleCaseInsensitiveSearch() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<Category> upperCase = categoryRepository.searchByName("TECH", pageable);
            Page<Category> lowerCase = categoryRepository.searchByName("tech", pageable);
            Page<Category> mixedCase = categoryRepository.searchByName("TeCh", pageable);

            // Assert
            assertThat(upperCase.getContent()).hasSize(1);
            assertThat(lowerCase.getContent()).hasSize(1);
            assertThat(mixedCase.getContent()).hasSize(1);
            
            // Tous doivent retourner la même catégorie
            assertThat(upperCase.getContent().get(0).getName())
                .isEqualTo(lowerCase.getContent().get(0).getName())
                .isEqualTo(mixedCase.getContent().get(0).getName());
        }

        @Test
        @DisplayName("✅ Doit gérer la pagination avec de nombreuses catégories")
        void shouldHandlePaginationWithManyCategories() {
            // Arrange - Créer 20 catégories supplémentaires
            for (int i = 1; i <= 20; i++) {
                Category extraCategory = new Category("Extra " + i, "Description " + i);
                entityManager.persistAndFlush(extraCategory);
            }
            entityManager.clear();

            // Act
            Pageable firstPage = PageRequest.of(0, 5);
            Pageable secondPage = PageRequest.of(1, 5);
            
            Page<Category> page1 = categoryRepository.findAllWithParent(firstPage);
            Page<Category> page2 = categoryRepository.findAllWithParent(secondPage);

            // Assert
            assertThat(page1.getContent()).hasSize(5);
            assertThat(page2.getContent()).hasSize(5);
            assertThat(page1.getTotalElements()).isEqualTo(26); // 6 + 20 nouvelles
            assertThat(page1.getTotalPages()).isEqualTo(6); // (26 / 5) arrondi vers le haut
            
            // Vérifier qu'il n'y a pas de doublons entre les pages
            List<String> page1Names = page1.getContent().stream()
                .map(Category::getName)
                .toList();
            List<String> page2Names = page2.getContent().stream()
                .map(Category::getName)
                .toList();
            
            assertThat(page1Names).doesNotContainAnyElementsOf(page2Names);
        }
    }
} 