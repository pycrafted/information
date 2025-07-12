import { get, post, put, del } from './api';
import type { 
  ArticleResponse, 
  ArticleRequest, 
  Page, 
  SearchParams,
  ArticleStatus 
} from '../types/api';

// =============================================================================
// SERVICE DES ARTICLES - ENDPOINTS REST
// =============================================================================

/**
 * Service des articles pour tous les endpoints REST
 * Correspond exactement au contrôleur ArticleController du backend
 */
export const articleService = {
  
  // ---------------------------------------------------------------------------
  // ENDPOINTS PUBLICS (SANS AUTHENTIFICATION)
  // ---------------------------------------------------------------------------
  
  /**
   * Récupère les 10 derniers articles publiés pour la page d'accueil
   * GET /api/articles/recent
   */
  async getRecentArticles(): Promise<ArticleResponse[]> {
    try {
      const articles = await get<ArticleResponse[]>('/api/articles/recent');
      console.log('✅ Recent articles retrieved:', articles.length);
      return articles;
      
    } catch (error) {
      console.error('❌ Failed to get recent articles:', error);
      throw error;
    }
  },

  /**
   * Récupère un article spécifique par son ID (UUID)
   * GET /api/articles/{id}
   */
  async getArticleById(id: string): Promise<ArticleResponse> {
    try {
      const article = await get<ArticleResponse>(`/api/articles/${id}`);
      console.log('✅ Article retrieved:', article.title);
      return article;
      
    } catch (error) {
      console.error(`❌ Failed to get article ${id}:`, error);
      throw error;
    }
  },

  /**
   * Récupère les articles publiés avec pagination
   * GET /api/articles/published?page=0&size=5&sort=publishedAt,desc
   */
  async getPublishedArticles(
    page: number = 0, 
    size: number = 5, 
    sort: string = 'publishedAt,desc'
  ): Promise<Page<ArticleResponse>> {
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
        sort,
      });

      const articlesPage = await get<Page<ArticleResponse>>(
        `/api/articles/published?${params}`
      );
      
      console.log('✅ Published articles retrieved:', {
        totalElements: articlesPage.totalElements,
        currentPage: articlesPage.number,
        totalPages: articlesPage.totalPages,
      });
      
      return articlesPage;
      
    } catch (error) {
      console.error('❌ Failed to get published articles:', error);
      throw error;
    }
  },

  /**
   * Récupère les articles d'une catégorie spécifique
   * GET /api/articles/category/{categorySlug}?page=0&size=5&sort=publishedAt,desc
   */
  async getArticlesByCategory(
    categorySlug: string,
    page: number = 0,
    size: number = 5,
    sort: string = 'publishedAt,desc'
  ): Promise<Page<ArticleResponse>> {
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
        sort,
      });

      const articlesPage = await get<Page<ArticleResponse>>(
        `/api/articles/category/${categorySlug}?${params}`
      );
      
      console.log('✅ Articles by category retrieved:', {
        category: categorySlug,
        totalElements: articlesPage.totalElements,
        currentPage: articlesPage.number,
      });
      
      return articlesPage;
      
    } catch (error) {
      console.error(`❌ Failed to get articles for category ${categorySlug}:`, error);
      throw error;
    }
  },

  // ---------------------------------------------------------------------------
  // ENDPOINTS SÉCURISÉS (ÉDITEURS + ADMINS)
  // ---------------------------------------------------------------------------

  /**
   * Crée un nouvel article en brouillon
   * POST /api/articles
   * Requiert : EDITEUR ou ADMINISTRATEUR
   */
  async createArticle(articleData: ArticleRequest): Promise<ArticleResponse> {
    try {
      const article = await post<ArticleResponse>('/api/articles', articleData);
      console.log('✅ Article created:', article.title, '(Status: DRAFT)');
      return article;
      
    } catch (error) {
      console.error('❌ Failed to create article:', error);
      throw error;
    }
  },

  /**
   * Met à jour un article existant
   * PUT /api/articles/{id}
   * Requiert : EDITEUR (propre article) ou ADMINISTRATEUR (tous)
   */
  async updateArticle(id: string, articleData: ArticleRequest): Promise<ArticleResponse> {
    try {
      const article = await put<ArticleResponse>(`/api/articles/${id}`, articleData);
      console.log('✅ Article updated:', article.title);
      return article;
      
    } catch (error) {
      console.error(`❌ Failed to update article ${id}:`, error);
      throw error;
    }
  },

  /**
   * Publie un article (change le statut de DRAFT à PUBLISHED)
   * POST /api/articles/{id}/publish
   * Requiert : EDITEUR ou ADMINISTRATEUR
   */
  async publishArticle(id: string): Promise<ArticleResponse> {
    try {
      const article = await post<ArticleResponse>(`/api/articles/${id}/publish`);
      console.log('✅ Article published:', article.title);
      return article;
      
    } catch (error) {
      console.error(`❌ Failed to publish article ${id}:`, error);
      throw error;
    }
  },

  /**
   * Archive un article (change le statut à ARCHIVED)
   * POST /api/articles/{id}/archive
   * Requiert : EDITEUR ou ADMINISTRATEUR
   */
  async archiveArticle(id: string): Promise<ArticleResponse> {
    try {
      const article = await post<ArticleResponse>(`/api/articles/${id}/archive`);
      console.log('✅ Article archived:', article.title);
      return article;
      
    } catch (error) {
      console.error(`❌ Failed to archive article ${id}:`, error);
      throw error;
    }
  },

  /**
   * Supprime définitivement un article
   * DELETE /api/articles/{id}
   * Requiert : ADMINISTRATEUR uniquement
   */
  async deleteArticle(id: string): Promise<void> {
    try {
      await del(`/api/articles/${id}`);
      console.log('✅ Article deleted permanently:', id);
      
    } catch (error) {
      console.error(`❌ Failed to delete article ${id}:`, error);
      throw error;
    }
  },

  // ---------------------------------------------------------------------------
  // MÉTHODES UTILITAIRES ET RECHERCHE
  // ---------------------------------------------------------------------------

  /**
   * Recherche d'articles avec filtres multiples
   * Utilise l'endpoint de recherche du backend si disponible
   */
  async searchArticles(searchParams: SearchParams): Promise<Page<ArticleResponse>> {
    try {
      const params = new URLSearchParams();
      
      if (searchParams.query) params.append('q', searchParams.query);
      if (searchParams.categoryId) params.append('categoryId', searchParams.categoryId);
      if (searchParams.status) params.append('status', searchParams.status);
      if (searchParams.page !== undefined) params.append('page', searchParams.page.toString());
      if (searchParams.size !== undefined) params.append('size', searchParams.size.toString());
      if (searchParams.sort) params.append('sort', searchParams.sort);

      const articlesPage = await get<Page<ArticleResponse>>(
        `/api/articles/search?${params}`
      );
      
      console.log('✅ Articles search completed:', {
        query: searchParams.query,
        totalResults: articlesPage.totalElements,
      });
      
      return articlesPage;
      
    } catch (error) {
      console.error('❌ Failed to search articles:', error);
      throw error;
    }
  },

  /**
   * Récupère les articles d'un auteur spécifique (pour éditeurs)
   * Utilise les endpoints existants avec filtres
   */
  async getArticlesByAuthor(
    authorId: string,
    page: number = 0,
    size: number = 10
  ): Promise<Page<ArticleResponse>> {
    try {
      const params = new URLSearchParams({
        authorId,
        page: page.toString(),
        size: size.toString(),
        sort: 'updatedAt,desc',
      });

      const articlesPage = await get<Page<ArticleResponse>>(
        `/api/articles?${params}`
      );
      
      console.log('✅ Articles by author retrieved:', {
        authorId,
        totalElements: articlesPage.totalElements,
      });
      
      return articlesPage;
      
    } catch (error) {
      console.error(`❌ Failed to get articles by author ${authorId}:`, error);
      throw error;
    }
  },

  /**
   * Récupère les articles par statut (pour éditeurs/admins)
   */
  async getArticlesByStatus(
    status: ArticleStatus,
    page: number = 0,
    size: number = 10
  ): Promise<Page<ArticleResponse>> {
    try {
      const params = new URLSearchParams({
        status,
        page: page.toString(),
        size: size.toString(),
        sort: 'updatedAt,desc',
      });

      const articlesPage = await get<Page<ArticleResponse>>(
        `/api/articles?${params}`
      );
      
      console.log('✅ Articles by status retrieved:', {
        status,
        totalElements: articlesPage.totalElements,
      });
      
      return articlesPage;
      
    } catch (error) {
      console.error(`❌ Failed to get articles by status ${status}:`, error);
      throw error;
    }
  },

  /**
   * Récupère les brouillons de l'utilisateur connecté
   */
  async getMyDrafts(page: number = 0, size: number = 10): Promise<Page<ArticleResponse>> {
    return this.getArticlesByStatus('DRAFT', page, size);
  },

  /**
   * Récupère tous les articles publiés de l'utilisateur connecté
   */
  async getMyPublishedArticles(page: number = 0, size: number = 10): Promise<Page<ArticleResponse>> {
    return this.getArticlesByStatus('PUBLISHED', page, size);
  },

  /**
   * Récupère les statistiques des articles (pour dashboard)
   */
  async getArticleStats(): Promise<{
    totalArticles: number;
    publishedArticles: number;
    draftArticles: number;
    archivedArticles: number;
  }> {
    try {
      const stats = await get<{
        totalArticles: number;
        publishedArticles: number;
        draftArticles: number;
        archivedArticles: number;
      }>('/api/articles/stats');
      
      console.log('✅ Article stats retrieved:', stats);
      return stats;
      
    } catch (error) {
      console.error('❌ Failed to get article stats:', error);
      throw error;
    }
  },
}; 