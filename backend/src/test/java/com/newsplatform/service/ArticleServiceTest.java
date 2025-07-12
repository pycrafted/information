package com.newsplatform.service;

import com.newsplatform.entity.Article;
import com.newsplatform.entity.ArticleStatus;
import com.newsplatform.entity.Category;
import com.newsplatform.entity.User;
import com.newsplatform.repository.ArticleRepository;
import com.newsplatform.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ArticleService - Version DDD rapide
 */
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;
    
    @Mock
    private CategoryRepository categoryRepository;

    private ArticleService articleService;
    private Category category;
    private User author;
    private Article article1;
    private Article article2;

    @BeforeEach
    void setUp() {
        articleService = new ArticleService(articleRepository, categoryRepository);
        
        // Setup avec constructeurs DDD
        category = new Category("Technologie", "Articles sur la technologie");
        author = new User("editeur@test.com", "password123", "Éditeur Test", User.UserRole.EDITEUR);
        article1 = new Article("Guide Spring Boot", "Contenu du guide Spring Boot", category, author);
        article2 = new Article("Guide React", "Contenu du guide React", category, author);
        
        // Publier les articles pour les tests
        article1.publish();
        article2.publish();
    }

    @Test
    void testGetRecentArticles() {
        // Arrange
        List<Article> articles = Arrays.asList(article2, article1);
        when(articleRepository.findTopRecentPublished(ArticleStatus.PUBLISHED, PageRequest.of(0, 10)))
            .thenReturn(articles);

        // Act
        List<Article> result = articleService.getRecentArticles();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Guide React");
        assertThat(result.get(1).getTitle()).isEqualTo("Guide Spring Boot");
        
        verify(articleRepository, times(1))
            .findTopRecentPublished(ArticleStatus.PUBLISHED, PageRequest.of(0, 10));
    }
} 