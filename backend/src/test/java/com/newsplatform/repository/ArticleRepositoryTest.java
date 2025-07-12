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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests complets pour ArticleRepository selon les principes DDD.
 * Tests d'intégration avec base H2 en mémoire.
 * 
 * @author Équipe Développement
 * @version 2.0 - Tests DDD complets
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests d'intégration - ArticleRepository")
class ArticleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ArticleRepository articleRepository;

    // Données de test
    private User author;
    private Category technologyCategory;
    private Category scienceCategory;
    private Article publishedArticle1;
    private Article publishedArticle2;
    private Article draftArticle;

    @BeforeEach
    void setUp() {
        // Arrange - Préparation des données de test avec constructeurs DDD
        author = new User("editeur", "editeur@test.com", "password123", User.UserRole.EDITEUR);
        entityManager.persistAndFlush(author);

        technologyCategory = new Category("Technologie", "Articles sur la technologie");
        scienceCategory = new Category("Science", "Articles scientifiques");
        entityManager.persistAndFlush(technologyCategory);
        entityManager.persistAndFlush(scienceCategory);

        // Articles publiés
        publishedArticle1 = new Article(
            "Guide Spring Boot",
            "Un guide complet sur Spring Boot avec des exemples pratiques et des bonnes pratiques pour développer des applications robustes.",
            technologyCategory,
            author
        );
        publishedArticle1.publish();
        
        publishedArticle2 = new Article(
            "Introduction à React",
            "React est une bibliothèque JavaScript populaire pour créer des interfaces utilisateur interactives et modernes.",
            technologyCategory,
            author
        );
        publishedArticle2.publish();

        // Article en brouillon
        draftArticle = new Article(
            "Article en brouillon",
            "Contenu en cours de rédaction qui n'est pas encore prêt pour publication.",
            scienceCategory,
            author
        );

        entityManager.persistAndFlush(publishedArticle1);
        entityManager.persistAndFlush(publishedArticle2);
        entityManager.persistAndFlush(draftArticle);
        entityManager.clear();
    }

    @Nested
    @DisplayName("Tests des requêtes de base optimisées")
    class BaseQueriesTests {

        @Test
        @DisplayName("✅ Doit trouver un article publié par slug avec détails")
        void shouldFindPublishedBySlugWithDetails() {
            // Act
            Optional<Article> result = articleRepository.findPublishedBySlug(
                publishedArticle1.getSlug(), 
                ArticleStatus.PUBLISHED
            );

            // Assert
            assertThat(result).isPresent();
            Article article = result.get();
            assertThat(article.getTitle()).isEqualTo("Guide Spring Boot");
            assertThat(article.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
            assertThat(article.getCategory().getName()).isEqualTo("Technologie");
            assertThat(article.getAuthor().getUsername()).isEqualTo("editeur");
        }

        @Test
        @DisplayName("❌ Ne doit pas trouver un brouillon par slug dans la recherche publique")
        void shouldNotFindDraftBySlugInPublicSearch() {
            // Act
            Optional<Article> result = articleRepository.findPublishedBySlug(
                draftArticle.getSlug(), 
                ArticleStatus.PUBLISHED
            );

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("✅ Doit trouver un article par slug (tous statuts) pour administration")
        void shouldFindBySlugWithDetailsForAdmin() {
            // Act
            Optional<Article> result = articleRepository.findBySlugWithDetails(draftArticle.getSlug());

            // Assert
            assertThat(result).isPresent();
            Article article = result.get();
            assertThat(article.getTitle()).isEqualTo("Article en brouillon");
            assertThat(article.getStatus()).isEqualTo(ArticleStatus.DRAFT);
        }
    }

    @Nested
    @DisplayName("Tests des requêtes publiques (articles publiés)")
    class PublicQueriesTests {

        @Test
        @DisplayName("✅ Doit récupérer les articles publiés récents avec pagination")
        void shouldFindRecentPublishedArticlesWithPagination() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<Article> result = articleRepository.findRecentPublishedArticles(
                ArticleStatus.PUBLISHED, 
                pageable
            );

            // Assert
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            
            // Vérifier le tri par date de publication (plus récent en premier)
            List<Article> articles = result.getContent();
            assertThat(articles.get(0).getPublishedAt())
                .isAfterOrEqualTo(articles.get(1).getPublishedAt());
            
            // Vérifier que seuls les articles publiés sont retournés
            articles.forEach(article -> 
                assertThat(article.getStatus()).isEqualTo(ArticleStatus.PUBLISHED)
            );
        }

        @Test
        @DisplayName("✅ Doit récupérer le top N des articles publiés")
        void shouldFindTopRecentPublished() {
            // Arrange
            Pageable limit = PageRequest.of(0, 1);

            // Act
            List<Article> result = articleRepository.findTopRecentPublished(
                ArticleStatus.PUBLISHED, 
                limit
            );

            // Assert
            assertThat(result).hasSize(1);
            Article mostRecent = result.get(0);
            assertThat(mostRecent.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
            assertThat(mostRecent.getPublishedAt()).isNotNull();
        }

        @Test
        @DisplayName("✅ Doit récupérer les articles publiés par catégorie")
        void shouldFindPublishedByCategory() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<Article> result = articleRepository.findPublishedByCategory(
                technologyCategory, 
                ArticleStatus.PUBLISHED, 
                pageable
            );

            // Assert
            assertThat(result.getContent()).hasSize(2);
            result.getContent().forEach(article -> {
                assertThat(article.getCategory()).isEqualTo(technologyCategory);
                assertThat(article.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
            });
        }

        @Test
        @DisplayName("✅ Doit rechercher des articles publiés par terme")
        void shouldSearchPublishedArticles() {
            // Arrange
            String searchTerm = "Spring";
            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<Article> result = articleRepository.searchPublishedArticles(
                searchTerm, 
                ArticleStatus.PUBLISHED, 
                pageable
            );

            // Assert
            assertThat(result.getContent()).hasSize(1);
            Article foundArticle = result.getContent().get(0);
            assertThat(foundArticle.getTitle()).contains("Spring");
            assertThat(foundArticle.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
        }

        @Test
        @DisplayName("✅ Doit rechercher des articles dans une plage de dates")
        void shouldFindPublishedBetweenDates() {
            // Arrange
            LocalDateTime startDate = LocalDateTime.now().minusDays(1);
            LocalDateTime endDate = LocalDateTime.now().plusDays(1);
            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<Article> result = articleRepository.findPublishedBetweenDates(
                startDate, 
                endDate, 
                ArticleStatus.PUBLISHED, 
                pageable
            );

            // Assert
            assertThat(result.getContent()).hasSize(2);
            result.getContent().forEach(article -> {
                assertThat(article.getPublishedAt()).isBetween(startDate, endDate);
                assertThat(article.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
            });
        }
    }

    @Nested
    @DisplayName("Tests des requêtes d'administration")
    class AdminQueriesTests {

        @Test
        @DisplayName("✅ Doit récupérer tous les articles avec détails pour administration")
        void shouldFindAllWithDetailsForAdmin() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<Article> result = articleRepository.findAllWithDetails(pageable);

            // Assert
            assertThat(result.getContent()).hasSize(3); // 2 publiés + 1 brouillon
            
            // Vérifier que les détails sont chargés
            result.getContent().forEach(article -> {
                assertThat(article.getCategory()).isNotNull();
                assertThat(article.getAuthor()).isNotNull();
            });
        }

        @Test
        @DisplayName("✅ Doit récupérer les articles par auteur")
        void shouldFindByAuthor() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<Article> result = articleRepository.findByAuthor(author, pageable);

            // Assert
            assertThat(result.getContent()).hasSize(3);
            result.getContent().forEach(article -> 
                assertThat(article.getAuthor()).isEqualTo(author)
            );
        }

        @Test
        @DisplayName("✅ Doit récupérer les articles par statut")
        void shouldFindByStatus() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<Article> drafts = articleRepository.findByStatus(ArticleStatus.DRAFT, pageable);
            Page<Article> published = articleRepository.findByStatus(ArticleStatus.PUBLISHED, pageable);

            // Assert
            assertThat(drafts.getContent()).hasSize(1);
            assertThat(published.getContent()).hasSize(2);
            
            drafts.getContent().forEach(article -> 
                assertThat(article.getStatus()).isEqualTo(ArticleStatus.DRAFT)
            );
            published.getContent().forEach(article -> 
                assertThat(article.getStatus()).isEqualTo(ArticleStatus.PUBLISHED)
            );
        }
    }

    @Nested
    @DisplayName("Tests des requêtes de statistiques")
    class StatisticsQueriesTests {

        @Test
        @DisplayName("✅ Doit compter les articles publiés par catégorie")
        void shouldCountPublishedByCategory() {
            // Act
            long technologyCount = articleRepository.countPublishedByCategory(
                technologyCategory, 
                ArticleStatus.PUBLISHED
            );
            long scienceCount = articleRepository.countPublishedByCategory(
                scienceCategory, 
                ArticleStatus.PUBLISHED
            );

            // Assert
            assertThat(technologyCount).isEqualTo(2);
            assertThat(scienceCount).isEqualTo(0); // Le brouillon n'est pas compté
        }

        @Test
        @DisplayName("✅ Doit compter le total des articles publiés")
        void shouldCountPublished() {
            // Act
            long count = articleRepository.countPublished(ArticleStatus.PUBLISHED);

            // Assert
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("✅ Doit compter les articles par auteur")
        void shouldCountByAuthor() {
            // Act
            long count = articleRepository.countByAuthor(author);

            // Assert
            assertThat(count).isEqualTo(3); // Tous statuts confondus
        }
    }

    @Nested
    @DisplayName("Tests des requêtes de validation métier")
    class ValidationQueriesTests {

        @Test
        @DisplayName("✅ Doit vérifier l'existence d'un slug")
        void shouldCheckSlugExistence() {
            // Act
            boolean exists = articleRepository.existsBySlug(publishedArticle1.getSlug());
            boolean notExists = articleRepository.existsBySlug("slug-inexistant");

            // Assert
            assertThat(exists).isTrue();
            assertThat(notExists).isFalse();
        }

        @Test
        @DisplayName("✅ Doit vérifier l'unicité d'un slug pour modification")
        void shouldCheckSlugUniquenessForUpdate() {
            // Act
            boolean existsForOther = articleRepository.existsBySlugAndIdNot(
                publishedArticle1.getSlug(), 
                publishedArticle2.getId()
            );
            boolean uniqueForSame = articleRepository.existsBySlugAndIdNot(
                publishedArticle1.getSlug(), 
                publishedArticle1.getId()
            );

            // Assert
            assertThat(existsForOther).isTrue(); // Le slug existe pour un autre article
            assertThat(uniqueForSame).isFalse(); // Le slug n'existe pas pour un autre que lui-même
        }
    }

    @Nested
    @DisplayName("Tests de performance et optimisation")
    class PerformanceTests {

        @Test
        @DisplayName("✅ Doit créer des articles supplémentaires pour tests de performance")
        void shouldCreateAdditionalArticlesForPerformanceTest() {
            // Arrange - Créer 15 articles supplémentaires pour tester la pagination
            for (int i = 1; i <= 15; i++) {
                Article extraArticle = new Article(
                    "Article Extra " + i,
                    "Contenu extra " + i + " avec suffisamment de caractères pour respecter les règles métier DDD.",
                    technologyCategory,
                    author
                );
                extraArticle.publish();
                entityManager.persistAndFlush(extraArticle);
            }
            entityManager.clear();

            // Act - Test de pagination avec un grand nombre d'articles
            Pageable firstPage = PageRequest.of(0, 5);
            Pageable secondPage = PageRequest.of(1, 5);
            
            Page<Article> page1 = articleRepository.findRecentPublishedArticles(
                ArticleStatus.PUBLISHED, 
                firstPage
            );
            Page<Article> page2 = articleRepository.findRecentPublishedArticles(
                ArticleStatus.PUBLISHED, 
                secondPage
            );

            // Assert
            assertThat(page1.getContent()).hasSize(5);
            assertThat(page2.getContent()).hasSize(5);
            assertThat(page1.getTotalElements()).isEqualTo(17); // 2 + 15 nouveaux
            assertThat(page1.getTotalPages()).isEqualTo(4); // (17 / 5) arrondi vers le haut
            
            // Vérifier qu'il n'y a pas de doublons entre les pages
            List<String> page1Titles = page1.getContent().stream()
                .map(Article::getTitle)
                .toList();
            List<String> page2Titles = page2.getContent().stream()
                .map(Article::getTitle)
                .toList();
            
            assertThat(page1Titles).doesNotContainAnyElementsOf(page2Titles);
        }

        @Test
        @DisplayName("✅ Doit optimiser les requêtes avec JOIN FETCH")
        void shouldOptimizeQueriesWithJoinFetch() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            // Act - Cette requête doit charger category et author en une seule requête
            Page<Article> articles = articleRepository.findRecentPublishedArticles(
                ArticleStatus.PUBLISHED, 
                pageable
            );

            // Assert - Vérifier que les relations sont déjà chargées (pas de lazy loading)
            assertThat(articles.getContent()).isNotEmpty();
            articles.getContent().forEach(article -> {
                // Ces accès ne doivent pas déclencher de requêtes supplémentaires
                assertThat(article.getCategory().getName()).isNotNull();
                assertThat(article.getAuthor().getUsername()).isNotNull();
            });
        }
    }

    @Nested
    @DisplayName("Tests de cas limites")
    class EdgeCasesTests {

        @Test
        @DisplayName("✅ Doit gérer les recherches avec termes vides")
        void shouldHandleEmptySearchTerms() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<Article> result = articleRepository.searchPublishedArticles(
                "", 
                ArticleStatus.PUBLISHED, 
                pageable
            );

            // Assert - Recherche vide doit retourner tous les articles publiés
            assertThat(result.getContent()).hasSize(2);
        }

        @Test
        @DisplayName("✅ Doit gérer les requêtes avec catégorie sans articles")
        void shouldHandleEmptyCategories() {
            // Arrange
            Category emptyCategory = new Category("Vide", "Catégorie sans articles");
            entityManager.persistAndFlush(emptyCategory);
            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<Article> result = articleRepository.findPublishedByCategory(
                emptyCategory, 
                ArticleStatus.PUBLISHED, 
                pageable
            );

            // Assert
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("✅ Doit gérer les plages de dates sans articles")
        void shouldHandleEmptyDateRanges() {
            // Arrange - Plage de dates dans le futur
            LocalDateTime futureStart = LocalDateTime.now().plusDays(10);
            LocalDateTime futureEnd = LocalDateTime.now().plusDays(20);
            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<Article> result = articleRepository.findPublishedBetweenDates(
                futureStart, 
                futureEnd, 
                ArticleStatus.PUBLISHED, 
                pageable
            );

            // Assert
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }
} 