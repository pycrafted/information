package com.newsplatform.facade;

import com.newsplatform.dto.response.ArticleResponse;
import com.newsplatform.entity.Article;
import com.newsplatform.entity.Category;
import com.newsplatform.entity.User;
import com.newsplatform.exception.ValidationException;
import com.newsplatform.mapper.ArticleMapper;
import com.newsplatform.repository.CategoryRepository;
import com.newsplatform.service.ArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires corrigés pour ArticleFacade selon DDD
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de ArticleFacade - DDD")
class ArticleFacadeTest {

    @Mock
    private ArticleService articleService;
    
    @Mock
    private ArticleMapper articleMapper;

    @Mock 
    private CategoryRepository categoryRepository;

    private ArticleFacade articleFacade;
    private Category category;
    private User author;
    private Article article1;
    private Article article2;

    @BeforeEach
    void setUp() {
        // Initialisation des objets avec constructeurs DDD
        category = new Category("Technologie", "Articles technologiques");
        author = new User("test@example.com", "password", "Test Author", User.UserRole.EDITEUR);
        article1 = new Article("Guide Spring Boot", "Contenu du guide Spring Boot", category, author);
        article2 = new Article("Guide React", "Contenu du guide React", category, author);

        // Création de la façade avec le nouveau constructeur (3 paramètres)
        articleFacade = new ArticleFacade(articleService, articleMapper, categoryRepository);
    }

    @Nested
    @DisplayName("Tests de getRecentArticles")
    class GetRecentArticlesTests {

        @Test
        @DisplayName("Doit retourner les articles récents avec succès")
        void shouldReturnRecentArticlesSuccessfully() {
            // Arrange
            List<Article> articles = Arrays.asList(article1, article2);
            List<ArticleResponse> expectedResponses = Arrays.asList(
                new ArticleResponse(article1.getId(), "Guide Spring Boot", "Contenu...", null, "Technologie"),
                new ArticleResponse(article2.getId(), "Guide React", "Contenu...", null, "Technologie")
            );

            when(articleService.getRecentArticles()).thenReturn(articles);
            when(articleMapper.toResponseList(articles)).thenReturn(expectedResponses);

            // Act
            List<ArticleResponse> result = articleFacade.getRecentArticles();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(articleService, times(1)).getRecentArticles();
            verify(articleMapper, times(1)).toResponseList(articles);
        }
    }

    @Nested
    @DisplayName("Tests de getPaginatedPublishedArticles")
    class GetPaginatedPublishedArticlesTests {

        @Test
        @DisplayName("Doit retourner une page d'articles avec succès")
        void shouldReturnPaginatedArticlesSuccessfully() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 5);
            Page<Article> articlePage = new PageImpl<>(Arrays.asList(article1, article2));
            
            ArticleResponse response1 = new ArticleResponse(article1.getId(), "Guide Spring Boot", "Contenu...", null, "Technologie");
            ArticleResponse response2 = new ArticleResponse(article2.getId(), "Guide React", "Contenu...", null, "Technologie");
            
            when(articleService.getPublishedArticles(pageable)).thenReturn(articlePage);
            when(articleMapper.toResponse(article1)).thenReturn(response1);
            when(articleMapper.toResponse(article2)).thenReturn(response2);

            // Act
            Page<ArticleResponse> result = articleFacade.getPaginatedPublishedArticles(pageable);

            // Assert
            assertNotNull(result);
            verify(articleService, times(1)).getPublishedArticles(pageable);
        }
    }

    @Nested
    @DisplayName("Tests de getPublishedArticlesByCategory")
    class GetPublishedArticlesByCategoryTests {

        @Test
        @DisplayName("Doit retourner les articles d'une catégorie avec succès")
        void shouldReturnArticlesByCategorySuccessfully() {
            // Arrange
            String categorySlug = "technologie";
            Pageable pageable = PageRequest.of(0, 5);
            Page<Article> articlePage = new PageImpl<>(Arrays.asList(article1, article2));

            when(articleService.getPublishedArticlesByCategory(categorySlug, pageable)).thenReturn(articlePage);
            when(articleMapper.toResponse(any(Article.class))).thenReturn(
                new ArticleResponse(UUID.randomUUID(), "Test", "Content", null, "Technologie")
            );

            // Act
            Page<ArticleResponse> result = articleFacade.getPublishedArticlesByCategory(categorySlug, pageable);

            // Assert
            assertNotNull(result);
            verify(articleService, times(1)).getPublishedArticlesByCategory(categorySlug, pageable);
        }
    }

    @Nested
    @DisplayName("Tests de validation")
    class ValidationTests {

        @Test
        @DisplayName("Doit lever une exception si ArticleService est null")
        void shouldThrowExceptionWhenArticleServiceIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, 
                () -> new ArticleFacade(null, articleMapper, categoryRepository));
        }

        @Test
        @DisplayName("Doit lever une exception si ArticleMapper est null")
        void shouldThrowExceptionWhenArticleMapperIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, 
                () -> new ArticleFacade(articleService, null, categoryRepository));
        }
    }
} 