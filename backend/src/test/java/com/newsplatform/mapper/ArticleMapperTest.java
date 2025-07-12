package com.newsplatform.mapper;

import com.newsplatform.dto.response.ArticleResponse;
import com.newsplatform.entity.Article;
import com.newsplatform.entity.Category;
import com.newsplatform.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests exhaustifs pour ArticleMapper selon les principes DDD.
 * Tests de transformation entités ↔ DTOs avec validation complète.
 * 
 * @author Équipe Développement
 * @version 2.0 - Tests DDD avec constructeurs
 */
@SpringBootTest
@DisplayName("ArticleMapper - Tests de transformation DDD")
class ArticleMapperTest {

    @Autowired
    private ArticleMapper articleMapper;

    private Category category;
    private User author;
    private Article article;

    @BeforeEach
    void setUp() {
        // Création des données de test avec constructeurs DDD
        category = new Category("Technologie", "Articles sur la technologie");
        author = new User("editeur@test.com", "password123", "Éditeur Test", User.UserRole.EDITEUR);
        article = new Article("Guide Spring Boot", "Voici un guide complet sur Spring Boot...", category, author);
    }

    @Nested
    @DisplayName("Transformation Article vers ArticleResponse")
    class ArticleToResponse {

        @Test
        @DisplayName("Doit transformer un article complet avec succès")
        void shouldTransformCompleteArticleSuccessfully() {
            // When
            ArticleResponse response = articleMapper.toResponse(article);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(article.getId());
            assertThat(response.getTitle()).isEqualTo("Guide Spring Boot");
            assertThat(response.getContent()).isEqualTo("Voici un guide complet sur Spring Boot...");
            assertThat(response.getCategoryName()).isEqualTo("Technologie");
            // publishedAt est null pour un article DRAFT
            assertThat(response.getPublishedAt()).isNull();
        }

        @Test
        @DisplayName("Doit gérer un article avec catégorie null")
        void shouldHandleArticleWithNullCategory() {
            // Arrange
            Article articleSansCategorie = new Article("Article Sans Catégorie", "Contenu sans catégorie", null, author);

            // When
            ArticleResponse response = articleMapper.toResponse(articleSansCategorie);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getCategoryName()).isNull();
            assertThat(response.getTitle()).isEqualTo("Article Sans Catégorie");
        }

        @Test
        @DisplayName("Doit retourner null pour un article null")
        void shouldReturnNullForNullArticle() {
            // When
            ArticleResponse response = articleMapper.toResponse(null);

            // Then
            assertThat(response).isNull();
        }

        @Test
        @DisplayName("Doit transformer un article publié correctement")
        void shouldTransformPublishedArticleCorrectly() {
            // Arrange - Publier l'article
            article.publish();

            // When
            ArticleResponse response = articleMapper.toResponse(article);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("Guide Spring Boot");
            assertThat(response.getPublishedAt()).isNotNull();
            assertThat(response.getCategoryName()).isEqualTo("Technologie");
        }

        @Test
        @DisplayName("Doit transformer un article archivé correctement")
        void shouldTransformArchivedArticleCorrectly() {
            // Arrange - Publier puis archiver l'article
            article.publish();
            article.archive();

            // When
            ArticleResponse response = articleMapper.toResponse(article);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("Guide Spring Boot");
            assertThat(response.getPublishedAt()).isNotNull(); // Conserve la date de publication
            assertThat(response.getCategoryName()).isEqualTo("Technologie");
        }
    }

    @Nested
    @DisplayName("Transformation de listes d'articles")
    class ArticleListTransformation {

        @Test
        @DisplayName("Doit transformer une liste d'articles avec succès")
        void shouldTransformArticleListSuccessfully() {
            // Arrange
            Category category2 = new Category("Science", "Articles scientifiques");
            Article article2 = new Article("Découvertes Scientifiques", "Les dernières découvertes...", category2, author);
            Article article3 = new Article("Article sans catégorie", "Contenu sans catégorie", null, author);
            
            List<Article> articles = Arrays.asList(article, article2, article3);

            // When
            List<ArticleResponse> responses = articleMapper.toResponseList(articles);

            // Then
            assertThat(responses).hasSize(3);
            
            // Vérification premier article
            assertThat(responses.get(0).getTitle()).isEqualTo("Guide Spring Boot");
            assertThat(responses.get(0).getCategoryName()).isEqualTo("Technologie");
            
            // Vérification deuxième article
            assertThat(responses.get(1).getTitle()).isEqualTo("Découvertes Scientifiques");
            assertThat(responses.get(1).getCategoryName()).isEqualTo("Science");
            
            // Vérification troisième article (sans catégorie)
            assertThat(responses.get(2).getTitle()).isEqualTo("Article sans catégorie");
            assertThat(responses.get(2).getCategoryName()).isNull();
        }

        @Test
        @DisplayName("Doit gérer une liste vide")
        void shouldHandleEmptyList() {
            // When
            List<ArticleResponse> responses = articleMapper.toResponseList(Arrays.asList());

            // Then
            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("Doit retourner null pour une liste null")
        void shouldReturnNullForNullList() {
            // When
            List<ArticleResponse> responses = articleMapper.toResponseList(null);

            // Then
            assertThat(responses).isNull();
        }

        @Test
        @DisplayName("Doit filtrer les articles null dans une liste")
        void shouldFilterNullArticlesInList() {
            // Arrange
            List<Article> articlesWithNull = Arrays.asList(article, null);

            // When
            List<ArticleResponse> responses = articleMapper.toResponseList(articlesWithNull);

            // Then
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getTitle()).isEqualTo("Guide Spring Boot");
        }
    }

    @Nested
    @DisplayName("Tests de performance et robustesse")
    class PerformanceAndRobustness {

        @Test
        @DisplayName("Doit gérer une grande liste d'articles efficacement")
        void shouldHandleLargeListEfficiently() {
            // Arrange
            List<Article> largeList = createLargeArticleList(100); // Réduit pour les tests

            // When
            long startTime = System.currentTimeMillis();
            List<ArticleResponse> responses = articleMapper.toResponseList(largeList);
            long endTime = System.currentTimeMillis();

            // Then
            assertThat(responses).hasSize(100);
            assertThat(endTime - startTime).isLessThan(1000); // Moins d'1 seconde
        }

        @Test
        @DisplayName("Doit conserver la consistance des données lors de multiples transformations")
        void shouldMaintainDataConsistencyOnMultipleTransformations() {
            // When - Transformation multiple
            ArticleResponse response1 = articleMapper.toResponse(article);
            ArticleResponse response2 = articleMapper.toResponse(article);

            // Then
            assertThat(response1.getId()).isEqualTo(response2.getId());
            assertThat(response1.getTitle()).isEqualTo(response2.getTitle());
            assertThat(response1.getContent()).isEqualTo(response2.getContent());
            assertThat(response1.getCategoryName()).isEqualTo(response2.getCategoryName());
        }

        /**
         * Crée une grande liste d'articles pour les tests de performance.
         */
        private List<Article> createLargeArticleList(int size) {
            return java.util.stream.IntStream.range(0, size)
                .mapToObj(i -> {
                    String title = "Article Test " + i;
                    return new Article(title, "Contenu de " + title, category, author);
                })
                .toList();
        }
    }
} 