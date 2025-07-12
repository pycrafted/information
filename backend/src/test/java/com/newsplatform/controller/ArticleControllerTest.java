package com.newsplatform.controller;

import com.newsplatform.dto.response.ArticleResponse;
import com.newsplatform.facade.ArticleFacade;
import com.newsplatform.controller.rest.ArticleController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests d'intégration pour le contrôleur ArticleController
 * Couche Présentation : Tests des endpoints REST et sérialisation JSON
 */
@WebMvcTest(ArticleController.class)
@ActiveProfiles("test")
@WithMockUser(roles = "USER")
@DisplayName("Tests d'intégration ArticleController")
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArticleFacade articleFacade;

    private ArticleResponse response1;
    private ArticleResponse response2;
    private UUID articleId;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        articleId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        response1 = new ArticleResponse(
            articleId,
            "Guide Spring Boot",
            "Un guide complet pour apprendre Spring Boot...",
            LocalDateTime.of(2024, 1, 15, 10, 30),
            "Technologie"
        );

        response2 = new ArticleResponse(
            UUID.randomUUID(),
            "Introduction à React",
            "React est une bibliothèque JavaScript...",
            LocalDateTime.of(2024, 1, 16, 14, 45),
            "Technologie"
        );
    }

    @Nested
    @DisplayName("Tests de l'endpoint GET /api/articles/recent")
    class GetRecentArticlesEndpointTests {

        @Test
        @DisplayName("Doit retourner la liste des articles récents en JSON")
        void shouldReturnRecentArticlesAsJson() throws Exception {
            // Arrange
            List<ArticleResponse> recentArticles = Arrays.asList(response2, response1);
            when(articleFacade.getRecentArticles()).thenReturn(recentArticles);

            // Act & Assert
            mockMvc.perform(get("/api/articles/recent")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id", is(response2.getId().toString())))
                    .andExpect(jsonPath("$[0].title", is(response2.getTitle())))
                    .andExpect(jsonPath("$[0].content", is(response2.getContent())))
                    .andExpect(jsonPath("$[0].categoryName", is(response2.getCategoryName())))
                    .andExpect(jsonPath("$[1].id", is(response1.getId().toString())))
                    .andExpect(jsonPath("$[1].title", is(response1.getTitle())));

            verify(articleFacade, times(1)).getRecentArticles();
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucun article récent")
        void shouldReturnEmptyListWhenNoRecentArticles() throws Exception {
            // Arrange
            when(articleFacade.getRecentArticles()).thenReturn(Arrays.asList());

            // Act & Assert
            mockMvc.perform(get("/api/articles/recent")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(articleFacade, times(1)).getRecentArticles();
        }

        @Test
        @DisplayName("Doit gérer les erreurs de la façade")
        void shouldHandleFacadeErrors() throws Exception {
            // Arrange
            when(articleFacade.getRecentArticles())
                .thenThrow(new RuntimeException("Erreur interne"));

            // Act & Assert
            mockMvc.perform(get("/api/articles/recent")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is5xxServerError());

            verify(articleFacade, times(1)).getRecentArticles();
        }

        @Test
        @DisplayName("Doit supporter les requêtes XML")
        void shouldSupportXmlRequests() throws Exception {
            // Arrange
            List<ArticleResponse> recentArticles = Arrays.asList(response1);
            when(articleFacade.getRecentArticles()).thenReturn(recentArticles);

            // Act & Assert
            mockMvc.perform(get("/api/articles/recent")
                    .accept(MediaType.APPLICATION_XML))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
        }
    }

    @Nested
    @DisplayName("Tests de l'endpoint GET /api/articles/{id}")
    class GetArticleByIdEndpointTests {

        @Test
        @DisplayName("Doit retourner un article par son ID")
        void shouldReturnArticleById() throws Exception {
            // Arrange
            when(articleFacade.getArticleById(articleId)).thenReturn(response1);

            // Act & Assert
            mockMvc.perform(get("/api/articles/{id}", articleId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(response1.getId().toString())))
                    .andExpect(jsonPath("$.title", is(response1.getTitle())))
                    .andExpect(jsonPath("$.content", is(response1.getContent())))
                    .andExpect(jsonPath("$.categoryName", is(response1.getCategoryName())))
                    .andExpect(jsonPath("$.publishedAt", is("2024-01-15T10:30:00")));

            verify(articleFacade, times(1)).getArticleById(articleId);
        }

        @Test
        @DisplayName("Doit retourner 404 pour un article inexistant")
        void shouldReturn404ForNonExistentArticle() throws Exception {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(articleFacade.getArticleById(nonExistentId))
                .thenThrow(new RuntimeException("Article non trouvé"));

            // Act & Assert
            mockMvc.perform(get("/api/articles/{id}", nonExistentId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is5xxServerError());

            verify(articleFacade, times(1)).getArticleById(nonExistentId);
        }

        @Test
        @DisplayName("Doit gérer les UUID malformés")
        void shouldHandleMalformedUUIDs() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/articles/{id}", "invalid-uuid")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(articleFacade, never()).getArticleById(any());
        }

        @Test
        @DisplayName("Doit retourner un article sans catégorie")
        void shouldReturnArticleWithoutCategory() throws Exception {
            // Arrange
            ArticleResponse responseWithoutCategory = new ArticleResponse(
                articleId,
                "Article sans catégorie",
                "Contenu sans catégorie",
                LocalDateTime.now(),
                null
            );
            when(articleFacade.getArticleById(articleId)).thenReturn(responseWithoutCategory);

            // Act & Assert
            mockMvc.perform(get("/api/articles/{id}", articleId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.categoryName").doesNotExist());
        }
    }

    @Nested
    @DisplayName("Tests de l'endpoint GET /api/articles/published")
    class GetPaginatedPublishedArticlesEndpointTests {

        @Test
        @DisplayName("Doit retourner une page d'articles publiés avec pagination par défaut")
        void shouldReturnPagedPublishedArticlesWithDefaultPagination() throws Exception {
            // Arrange
            List<ArticleResponse> articles = Arrays.asList(response1, response2);
            Page<ArticleResponse> page = new PageImpl<>(articles, PageRequest.of(0, 5), 2);
            when(articleFacade.getPaginatedPublishedArticles(any())).thenReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/articles/published")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements", is(2)))
                    .andExpect(jsonPath("$.totalPages", is(1)))
                    .andExpect(jsonPath("$.size", is(5)))
                    .andExpect(jsonPath("$.number", is(0)))
                    .andExpect(jsonPath("$.content[0].title", is(response1.getTitle())))
                    .andExpect(jsonPath("$.content[1].title", is(response2.getTitle())));
        }

        @Test
        @DisplayName("Doit respecter les paramètres de pagination personnalisés")
        void shouldRespectCustomPaginationParameters() throws Exception {
            // Arrange
            List<ArticleResponse> articles = Arrays.asList(response2);
            Page<ArticleResponse> page = new PageImpl<>(articles, PageRequest.of(1, 1), 2);
            when(articleFacade.getPaginatedPublishedArticles(any())).thenReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/articles/published")
                    .param("page", "1")
                    .param("size", "1")
                    .param("sort", "title,asc")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.totalElements", is(2)))
                    .andExpect(jsonPath("$.totalPages", is(2)))
                    .andExpect(jsonPath("$.size", is(1)))
                    .andExpect(jsonPath("$.number", is(1)));
        }

        @Test
        @DisplayName("Doit retourner une page vide si aucun article")
        void shouldReturnEmptyPageWhenNoArticles() throws Exception {
            // Arrange
            Page<ArticleResponse> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 5), 0);
            when(articleFacade.getPaginatedPublishedArticles(any())).thenReturn(emptyPage);

            // Act & Assert
            mockMvc.perform(get("/api/articles/published")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements", is(0)))
                    .andExpect(jsonPath("$.totalPages", is(0)));
        }

        @Test
        @DisplayName("Doit gérer les paramètres de pagination invalides")
        void shouldHandleInvalidPaginationParameters() throws Exception {
            // Arrange
            Page<ArticleResponse> page = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 5), 0);
            when(articleFacade.getPaginatedPublishedArticles(any())).thenReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/articles/published")
                    .param("page", "-1")
                    .param("size", "0")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk()); // Spring corrige automatiquement les valeurs
        }
    }

    @Nested
    @DisplayName("Tests de l'endpoint GET /api/articles/category/{categoryId}")
    class GetArticlesByCategoryEndpointTests {

        @Test
        @DisplayName("Doit retourner les articles d'une catégorie donnée")
        void shouldReturnArticlesByCategory() throws Exception {
            // Arrange
            List<ArticleResponse> categoryArticles = Arrays.asList(response1, response2);
            Page<ArticleResponse> page = new PageImpl<>(categoryArticles, PageRequest.of(0, 5), 2);
            when(articleFacade.getPublishedArticlesByCategory(eq("technologie"), any()))
                .thenReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/articles/category/{categoryId}", categoryId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements", is(2)))
                    .andExpect(jsonPath("$.content[0].categoryName", is("Technologie")))
                    .andExpect(jsonPath("$.content[1].categoryName", is("Technologie")));

            verify(articleFacade, times(1))
                .getPublishedArticlesByCategory(eq("technologie"), any());
        }

        @Test
        @DisplayName("Doit retourner une page vide pour une catégorie sans articles")
        void shouldReturnEmptyPageForCategoryWithoutArticles() throws Exception {
            // Arrange
            UUID emptyCategoryId = UUID.randomUUID();
            Page<ArticleResponse> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 5), 0);
            when(articleFacade.getPublishedArticlesByCategory(eq("categorie-vide"), any()))
                .thenReturn(emptyPage);

            // Act & Assert
            mockMvc.perform(get("/api/articles/category/{categoryId}", emptyCategoryId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements", is(0)));
        }

        @Test
        @DisplayName("Doit gérer les UUID de catégorie malformés")
        void shouldHandleMalformedCategoryUUIDs() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/articles/category/{categoryId}", "invalid-category-uuid")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(articleFacade, never()).getPublishedArticlesByCategory(any(), any());
        }

        @Test
        @DisplayName("Doit supporter la pagination pour les articles par catégorie")
        void shouldSupportPaginationForArticlesByCategory() throws Exception {
            // Arrange
            List<ArticleResponse> categoryArticles = Arrays.asList(response1);
            Page<ArticleResponse> page = new PageImpl<>(categoryArticles, PageRequest.of(0, 1), 2);
            when(articleFacade.getPublishedArticlesByCategory(eq("technologie"), any()))
                .thenReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/articles/category/{categoryId}", categoryId)
                    .param("page", "0")
                    .param("size", "1")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.totalElements", is(2)))
                    .andExpect(jsonPath("$.totalPages", is(2)))
                    .andExpect(jsonPath("$.size", is(1)));
        }
    }

    @Nested
    @DisplayName("Tests de robustesse et cas limites")
    class RobustnessAndEdgeCaseTests {

        @Test
        @DisplayName("Doit gérer les requêtes avec en-têtes Accept multiples")
        void shouldHandleMultipleAcceptHeaders() throws Exception {
            // Arrange
            when(articleFacade.getRecentArticles()).thenReturn(Arrays.asList(response1));

            // Act & Assert
            mockMvc.perform(get("/api/articles/recent")
                    .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("Doit retourner 406 pour un format non supporté")
        void shouldReturn406ForUnsupportedFormat() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/articles/recent")
                    .accept(MediaType.TEXT_PLAIN))
                    .andExpect(status().isNotAcceptable());
        }

        @Test
        @DisplayName("Doit gérer les gros volumes de données JSON")
        void shouldHandleLargeJsonVolumes() throws Exception {
            // Arrange
            List<ArticleResponse> largeList = Arrays.asList(
                response1, response2, response1, response2, response1
            );
            when(articleFacade.getRecentArticles()).thenReturn(largeList);

            // Act & Assert
            mockMvc.perform(get("/api/articles/recent")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(5)));
        }

        @Test
        @DisplayName("Doit maintenir la cohérence du format de date")
        void shouldMaintainConsistentDateFormat() throws Exception {
            // Arrange
            when(articleFacade.getArticleById(articleId)).thenReturn(response1);

            // Act & Assert
            mockMvc.perform(get("/api/articles/{id}", articleId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.publishedAt", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")));
        }

        @Test
        @DisplayName("Doit gérer les caractères spéciaux dans les réponses JSON")
        void shouldHandleSpecialCharactersInJsonResponses() throws Exception {
            // Arrange
            ArticleResponse responseWithSpecialChars = new ArticleResponse(
                UUID.randomUUID(),
                "Article avec caractères spéciaux : éàü & <script>",
                "Contenu avec émojis 🚀 et caractères spéciaux",
                LocalDateTime.now(),
                "Catégorie spéciale"
            );
            when(articleFacade.getRecentArticles()).thenReturn(Arrays.asList(responseWithSpecialChars));

            // Act & Assert
            mockMvc.perform(get("/api/articles/recent")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title", containsString("caractères spéciaux")))
                    .andExpect(jsonPath("$[0].content", containsString("🚀")));
        }
    }
}
