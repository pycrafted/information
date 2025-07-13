import api from './api';

// Types pour les catégories
export interface Category {
  id: string;
  name: string;
  slug: string;
  description?: string;
  parent?: Category;
  children?: Category[];
  articleCount?: number;
  totalArticleCount?: number;
  depth?: number;
  fullPath?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Récupère les catégories racines avec leur hiérarchie
 * Endpoint public : GET /api/categories/roots
 */
export const getRootCategories = async (): Promise<Category[]> => {
  try {
    console.log('📁 Récupération des catégories racines...');
    const response = await api.get<Category[]>('/api/categories/roots');
    
    console.log(`✅ ${response.data.length} catégories racines récupérées`);
    return response.data;
  } catch (error: any) {
    console.error('❌ Erreur lors de la récupération des catégories racines:', error);
    throw new Error(
      error.response?.data?.message || 
      'Impossible de récupérer les catégories'
    );
  }
};

/**
 * Récupère toutes les catégories avec pagination
 * Endpoint public : GET /api/categories
 */
export const getAllCategories = async (
  page: number = 0, 
  size: number = 20
): Promise<{content: Category[], totalElements: number}> => {
  try {
    console.log(`📁 Récupération de toutes les catégories (page ${page})...`);
    
    const response = await api.get('/api/categories', {
      params: { page, size }
    });
    
    console.log(`✅ Page ${page} des catégories récupérée`);
    return response.data;
  } catch (error: any) {
    console.error('❌ Erreur lors de la récupération des catégories:', error);
    throw new Error(
      error.response?.data?.message || 
      'Impossible de récupérer les catégories'
    );
  }
};

/**
 * Récupère une catégorie par son ID
 * Endpoint public : GET /api/categories/{id}
 */
export const getCategoryById = async (id: string): Promise<Category> => {
  try {
    console.log(`📁 Récupération de la catégorie ${id}...`);
    const response = await api.get<Category>(`/api/categories/${id}`);
    
    console.log(`✅ Catégorie "${response.data.name}" récupérée`);
    return response.data;
  } catch (error: any) {
    console.error(`❌ Erreur lors de la récupération de la catégorie ${id}:`, error);
    throw new Error(
      error.response?.data?.message || 
      'Catégorie non trouvée'
    );
  }
};

/**
 * Récupère une catégorie par son slug
 * Endpoint public : GET /api/categories/slug/{slug}
 */
export const getCategoryBySlug = async (slug: string): Promise<Category> => {
  try {
    console.log(`📁 Récupération de la catégorie par slug "${slug}"...`);
    const response = await api.get<Category>(`/api/categories/slug/${slug}`);
    
    console.log(`✅ Catégorie "${response.data.name}" récupérée par slug`);
    return response.data;
  } catch (error: any) {
    console.error(`❌ Erreur lors de la récupération de la catégorie "${slug}":`, error);
    throw new Error(
      error.response?.data?.message || 
      'Catégorie non trouvée'
    );
  }
};

/**
 * Récupère les sous-catégories d'une catégorie
 * Endpoint public : GET /api/categories/{id}/children
 */
export const getSubCategories = async (id: string): Promise<Category[]> => {
  try {
    console.log(`📁 Récupération des sous-catégories de ${id}...`);
    const response = await api.get<Category[]>(`/api/categories/${id}/children`);
    
    console.log(`✅ ${response.data.length} sous-catégories récupérées`);
    return response.data;
  } catch (error: any) {
    console.error(`❌ Erreur lors de la récupération des sous-catégories:`, error);
    throw new Error(
      error.response?.data?.message || 
      'Impossible de récupérer les sous-catégories'
    );
  }
};

/**
 * Crée une structure plate de toutes les catégories pour la navigation
 */
export const flattenCategories = (categories: Category[]): Category[] => {
  const result: Category[] = [];
  
  const flatten = (cats: Category[], depth: number = 0) => {
    for (const cat of cats) {
      result.push({ ...cat, depth });
      if (cat.children && cat.children.length > 0) {
        flatten(cat.children, depth + 1);
      }
    }
  };
  
  flatten(categories);
  return result;
}; 