import api from './api';

// Types pour les articles
export interface Article {
  id: string;
  title: string;
  content: string;
  summary?: string;
  publishedAt: string;
  categoryName: string;
  categoryId?: string; // ID de la catégorie pour l'édition
  slug?: string;
  author?: {
    username: string;
    firstName?: string;
    lastName?: string;
  };
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  createdAt?: string;
  updatedAt?: string;
}

export interface ArticleListResponse {
  content: Article[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

/**
 * Récupère les articles récents (10 derniers publiés)
 * Endpoint public : GET /api/articles/recent
 */
export const getRecentArticles = async (): Promise<Article[]> => {
  try {
    console.log('📰 Récupération des articles récents...');
    const response = await api.get<Article[]>('/api/articles/recent');
    
    console.log(`✅ ${response.data.length} articles récents récupérés`);
    return response.data;
  } catch (error: any) {
    console.error('❌ Erreur lors de la récupération des articles récents:', error);
    throw new Error(
      error.response?.data?.message || 
      'Impossible de récupérer les articles récents'
    );
  }
};

/**
 * Récupère les articles publiés avec pagination
 * Endpoint public : GET /api/articles/published
 */
export const getPublishedArticles = async (
  page: number = 0, 
  size: number = 5
): Promise<ArticleListResponse> => {
  try {
    console.log(`📰 Récupération des articles publiés (page ${page}, taille ${size})...`);
    
    const response = await api.get<ArticleListResponse>('/api/articles/published', {
      params: { page, size }
    });
    
    console.log(`✅ Page ${page} récupérée : ${response.data.content.length} articles`);
    return response.data;
  } catch (error: any) {
    console.error('❌ Erreur lors de la récupération des articles publiés:', error);
    throw new Error(
      error.response?.data?.message || 
      'Impossible de récupérer les articles publiés'
    );
  }
};

/**
 * Récupère un article par son ID
 * Endpoint public : GET /api/articles/{id}
 */
export const getArticleById = async (id: string): Promise<Article> => {
  try {
    console.log(`📰 Récupération de l'article ${id}...`);
    const response = await api.get<Article>(`/api/articles/${id}`);
    
    console.log(`✅ Article "${response.data.title}" récupéré`);
    return response.data;
  } catch (error: any) {
    console.error(`❌ Erreur lors de la récupération de l'article ${id}:`, error);
    throw new Error(
      error.response?.data?.message || 
      'Article non trouvé'
    );
  }
};

/**
 * Utilitaires pour formater les articles
 */
export const formatDate = (dateString: string): string => {
  try {
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
      day: 'numeric',
      month: 'long', 
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (error) {
    return 'Date inconnue';
  }
};

export const truncateContent = (content: string, maxLength: number = 150): string => {
  if (content.length <= maxLength) return content;
  return content.substring(0, maxLength).trim() + '...';
};

// ============================================
// CRUD ARTICLES (ÉDITEURS/ADMINS)
// ============================================

/**
 * Interface pour créer/modifier un article
 */
export interface ArticleFormData {
  title: string;
  content: string;
  summary?: string;
  categoryId: string;
}

/**
 * Créer un nouveau article (brouillon)
 * Endpoint protégé : POST /api/articles
 * Rôles autorisés : EDITEUR, ADMINISTRATEUR
 */
export const createArticle = async (articleData: ArticleFormData): Promise<Article> => {
  try {
    console.log('✏️ Création d\'un nouvel article...');
    
    const response = await api.post<Article>('/api/articles', articleData);
    
    console.log(`✅ Article "${response.data.title}" créé avec succès (ID: ${response.data.id})`);
    return response.data;
  } catch (error: any) {
    console.error('❌ Erreur lors de la création de l\'article:', error);
    throw new Error(
      error.response?.data?.message || 
      'Impossible de créer l\'article'
    );
  }
};

/**
 * Helper pour retrouver l'ID d'une catégorie depuis son nom
 * Nécessaire car le backend exige categoryId pour validation même si non modifiable
 */
const findCategoryIdByName = async (categoryName: string): Promise<string> => {
  try {
    const { getRootCategories, flattenCategories } = await import('./categoryService');
    const rootCategories = await getRootCategories();
    const allCategories = flattenCategories(rootCategories);
    
    const category = allCategories.find(cat => cat.name === categoryName);
    if (!category) {
      throw new Error(`Catégorie non trouvée : ${categoryName}`);
    }
    
    return category.id;
  } catch (error) {
    console.error('❌ Erreur lors de la recherche de catégorie:', error);
    throw error;
  }
};

/**
 * Modifier un article existant
 * Endpoint protégé : PUT /api/articles/{id}
 * Rôles autorisés : EDITEUR (ses articles), ADMINISTRATEUR (tous)
 */
export const updateArticle = async (id: string, articleData: ArticleFormData, currentArticle?: Article): Promise<Article> => {
  try {
    console.log(`✏️ Modification de l'article ${id}...`);
    
    // PROBLÈME BACKEND: Validation exige categoryId mais service ne l'utilise pas
    // Solution: Envoyer le categoryId actuel pour satisfaire la validation
    let categoryId = articleData.categoryId;
    
    if (!categoryId && currentArticle?.categoryName) {
      console.log(`🔍 Récupération de l'ID pour la catégorie "${currentArticle.categoryName}" (validation backend)...`);
      categoryId = await findCategoryIdByName(currentArticle.categoryName);
      console.log(`✅ ID de catégorie trouvé: ${categoryId}`);
    }
    
    if (!categoryId) {
      throw new Error('Impossible de déterminer l\'ID de la catégorie pour validation');
    }
    
    // Payload complet pour satisfaire la validation backend
    // Note: categoryId sera ignoré par le service mais requis pour validation
    const updatePayload = {
      title: articleData.title,
      content: articleData.content,
      summary: articleData.summary || null,
      categoryId: categoryId // Requis pour validation (mais non modifiable)
    };
    
    console.log('📦 Payload envoyé:', updatePayload);
    console.log('🔍 Payload JSON détaillé:', JSON.stringify(updatePayload, null, 2));
    console.log('🎯 Types des champs:', {
      title: typeof updatePayload.title,
      content: typeof updatePayload.content,
      summary: typeof updatePayload.summary,
      categoryId: typeof updatePayload.categoryId,
      titleLength: updatePayload.title?.length,
      contentLength: updatePayload.content?.length
    });
    
    const response = await api.put<Article>(`/api/articles/${id}`, updatePayload);
    
    console.log(`✅ Article "${response.data.title}" modifié avec succès`);
    return response.data;
  } catch (error: any) {
    console.error('❌ Erreur lors de la modification de l\'article:', error);
    console.error('📋 Détails de l\'erreur:', error.response?.data);
    console.error('🚨 Status code:', error.response?.status);
    console.error('🚨 Status text:', error.response?.statusText);
    console.error('🚨 Headers de réponse:', error.response?.headers);
    
    // Log du détail complet de l'erreur backend
    if (error.response?.data) {
      console.error('🔥 Erreur backend détaillée:', JSON.stringify(error.response.data, null, 2));
    }
    
    throw new Error(
      error.response?.data?.message || 
      error.response?.data?.error ||
      `Erreur ${error.response?.status}: ${error.response?.statusText}` ||
      'Impossible de modifier l\'article'
    );
  }
};

/**
 * Publier un article (passer de DRAFT à PUBLISHED)
 * Endpoint protégé : POST /api/articles/{id}/publish
 * Rôles autorisés : EDITEUR (ses articles), ADMINISTRATEUR (tous)
 */
export const publishArticle = async (id: string): Promise<Article> => {
  try {
    console.log(`📢 Publication de l'article ${id}...`);
    
    const response = await api.post<Article>(`/api/articles/${id}/publish`);
    
    console.log(`✅ Article "${response.data.title}" publié avec succès`);
    return response.data;
  } catch (error: any) {
    console.error('❌ Erreur lors de la publication de l\'article:', error);
    throw new Error(
      error.response?.data?.message || 
      'Impossible de publier l\'article'
    );
  }
};

/**
 * Archiver un article (passer de PUBLISHED à ARCHIVED)
 * Endpoint protégé : POST /api/articles/{id}/archive
 * Rôles autorisés : EDITEUR (ses articles), ADMINISTRATEUR (tous)
 */
export const archiveArticle = async (id: string): Promise<Article> => {
  try {
    console.log(`📦 Archivage de l'article ${id}...`);
    
    const response = await api.post<Article>(`/api/articles/${id}/archive`);
    
    console.log(`✅ Article "${response.data.title}" archivé avec succès`);
    return response.data;
  } catch (error: any) {
    console.error('❌ Erreur lors de l\'archivage de l\'article:', error);
    throw new Error(
      error.response?.data?.message || 
      'Impossible d\'archiver l\'article'
    );
  }
};

/**
 * Supprimer un article définitivement
 * Endpoint protégé : DELETE /api/articles/{id}
 * Rôles autorisés : ADMINISTRATEUR uniquement
 */
export const deleteArticle = async (id: string): Promise<void> => {
  try {
    console.log(`🗑️ Suppression de l'article ${id}...`);
    
    await api.delete(`/api/articles/${id}`);
    
    console.log(`✅ Article supprimé définitivement`);
  } catch (error: any) {
    console.error('❌ Erreur lors de la suppression de l\'article:', error);
    throw new Error(
      error.response?.data?.message || 
      'Impossible de supprimer l\'article'
    );
  }
};

/**
 * Récupérer les articles de l'utilisateur connecté avec filtres
 * Endpoint protégé : GET /api/articles/my-articles
 * Inclut les brouillons et articles privés
 */
export const getMyArticles = async (
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED',
  page: number = 0,
  size: number = 10
): Promise<ArticleListResponse> => {
  try {
    console.log(`📝 Récupération de mes articles (statut: ${status || 'tous'})...`);
    
    const params: any = { page, size };
    if (status) params.status = status;
    
    const response = await api.get<ArticleListResponse>('/api/articles/my-articles', { params });
    
    console.log(`✅ ${response.data.content.length} de mes articles récupérés`);
    return response.data;
  } catch (error: any) {
    console.error('❌ Erreur lors de la récupération de mes articles:', error);
    throw new Error(
      error.response?.data?.message || 
      'Impossible de récupérer vos articles'
    );
  }
};

/**
 * Utilitaires pour la gestion des articles
 */
export const getStatusBadgeStyle = (status: string) => {
  const styles = {
    DRAFT: { backgroundColor: '#ffc107', color: '#000' },
    PUBLISHED: { backgroundColor: '#28a745', color: '#fff' },
    ARCHIVED: { backgroundColor: '#6c757d', color: '#fff' }
  };
  return styles[status as keyof typeof styles] || { backgroundColor: '#6c757d', color: '#fff' };
};

export const getStatusLabel = (status: string) => {
  const labels = {
    DRAFT: '📝 Brouillon',
    PUBLISHED: '✅ Publié',
    ARCHIVED: '📦 Archivé'
  };
  return labels[status as keyof typeof labels] || status;
}; 