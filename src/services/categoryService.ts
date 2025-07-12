import { get, post, put, patch, del } from './api';
import type { 
  CategoryResponse, 
  CategoryRequest, 
  Page 
} from '../types/api';

// =============================================================================
// SERVICE DES CATÉGORIES - ENDPOINTS REST
// =============================================================================

/**
 * Service des catégories pour tous les endpoints REST
 * Correspond exactement au contrôleur CategoryController du backend
 */
export const categoryService = {
  
  // ---------------------------------------------------------------------------
  // ENDPOINTS PUBLICS (CONSULTATION)
  // ---------------------------------------------------------------------------
  
  /**
   * Récupère toutes les catégories racines avec leur hiérarchie
   * GET /api/categories/roots
   */
  async getRootCategories(): Promise<CategoryResponse[]> {
    try {
      const categories = await get<CategoryResponse[]>('/api/categories/roots');
      console.log('✅ Root categories retrieved:', categories.length);
      return categories;
      
    } catch (error) {
      console.error('❌ Failed to get root categories:', error);
      throw error;
    }
  },

  /**
   * Récupère toutes les catégories avec pagination
   * GET /api/categories?page=0&size=20&sort=name,asc
   */
  async getAllCategories(
    page: number = 0,
    size: number = 20,
    sort: string = 'name,asc'
  ): Promise<Page<CategoryResponse>> {
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
        sort,
      });

      const categoriesPage = await get<Page<CategoryResponse>>(
        `/api/categories?${params}`
      );
      
      console.log('✅ All categories retrieved:', {
        totalElements: categoriesPage.totalElements,
        currentPage: categoriesPage.number,
        totalPages: categoriesPage.totalPages,
      });
      
      return categoriesPage;
      
    } catch (error) {
      console.error('❌ Failed to get all categories:', error);
      throw error;
    }
  },

  /**
   * Récupère une catégorie par son slug
   * GET /api/categories/slug/{slug}
   */
  async getCategoryBySlug(slug: string): Promise<CategoryResponse> {
    try {
      const category = await get<CategoryResponse>(`/api/categories/slug/${slug}`);
      console.log('✅ Category retrieved by slug:', category.name);
      return category;
      
    } catch (error) {
      console.error(`❌ Failed to get category by slug ${slug}:`, error);
      throw error;
    }
  },

  /**
   * Récupère une catégorie par son ID
   * GET /api/categories/{id}
   */
  async getCategoryById(id: string): Promise<CategoryResponse> {
    try {
      const category = await get<CategoryResponse>(`/api/categories/${id}`);
      console.log('✅ Category retrieved by ID:', category.name);
      return category;
      
    } catch (error) {
      console.error(`❌ Failed to get category by ID ${id}:`, error);
      throw error;
    }
  },

  /**
   * Récupère les sous-catégories d'une catégorie
   * GET /api/categories/{id}/children
   */
  async getSubCategories(parentId: string): Promise<CategoryResponse[]> {
    try {
      const subCategories = await get<CategoryResponse[]>(`/api/categories/${parentId}/children`);
      console.log('✅ Sub-categories retrieved:', subCategories.length);
      return subCategories;
      
    } catch (error) {
      console.error(`❌ Failed to get sub-categories for ${parentId}:`, error);
      throw error;
    }
  },

  // ---------------------------------------------------------------------------
  // ENDPOINTS DE TEST/DIAGNOSTIC
  // ---------------------------------------------------------------------------

  /**
   * Test simple pour vérifier la connectivité
   * GET /api/categories/test
   */
  async testConnection(): Promise<string> {
    try {
      const result = await get<string>('/api/categories/test');
      console.log('✅ Categories test successful:', result);
      return result;
      
    } catch (error) {
      console.error('❌ Categories test failed:', error);
      throw error;
    }
  },

  /**
   * Test basique du repository
   * GET /api/categories/test2
   */
  async testBasic(): Promise<string> {
    try {
      const result = await get<string>('/api/categories/test2');
      console.log('✅ Categories basic test successful:', result);
      return result;
      
    } catch (error) {
      console.error('❌ Categories basic test failed:', error);
      throw error;
    }
  },

  // ---------------------------------------------------------------------------
  // ENDPOINTS PRIVÉS (GESTION ÉDITEUR)
  // ---------------------------------------------------------------------------

  /**
   * Crée une nouvelle catégorie racine
   * POST /api/categories
   * Requiert : EDITEUR ou ADMINISTRATEUR
   */
  async createRootCategory(categoryData: Omit<CategoryRequest, 'parentId'>): Promise<CategoryResponse> {
    try {
      const category = await post<CategoryResponse>('/api/categories', categoryData);
      console.log('✅ Root category created:', category.name);
      return category;
      
    } catch (error) {
      console.error('❌ Failed to create root category:', error);
      throw error;
    }
  },

  /**
   * Crée une nouvelle sous-catégorie
   * POST /api/categories/{parentId}/subcategories
   * Requiert : EDITEUR ou ADMINISTRATEUR
   */
  async createSubCategory(
    parentId: string, 
    categoryData: Omit<CategoryRequest, 'parentId'>
  ): Promise<CategoryResponse> {
    try {
      const category = await post<CategoryResponse>(
        `/api/categories/${parentId}/subcategories`, 
        categoryData
      );
      console.log('✅ Sub-category created:', category.name, 'under parent:', parentId);
      return category;
      
    } catch (error) {
      console.error(`❌ Failed to create sub-category under ${parentId}:`, error);
      throw error;
    }
  },

  /**
   * Met à jour une catégorie existante
   * PUT /api/categories/{id}
   * Requiert : EDITEUR ou ADMINISTRATEUR
   */
  async updateCategory(id: string, categoryData: CategoryRequest): Promise<CategoryResponse> {
    try {
      const category = await put<CategoryResponse>(`/api/categories/${id}`, categoryData);
      console.log('✅ Category updated:', category.name);
      return category;
      
    } catch (error) {
      console.error(`❌ Failed to update category ${id}:`, error);
      throw error;
    }
  },

  /**
   * Déplace une catégorie dans la hiérarchie
   * PATCH /api/categories/{id}/move
   * Requiert : EDITEUR ou ADMINISTRATEUR
   */
  async moveCategory(id: string, newParentId?: string): Promise<CategoryResponse> {
    try {
      const moveData = newParentId ? { parentId: newParentId } : { parentId: null };
      
      const category = await patch<CategoryResponse>(`/api/categories/${id}/move`, moveData);
      console.log('✅ Category moved:', category.name, 'to new parent:', newParentId || 'root');
      return category;
      
    } catch (error) {
      console.error(`❌ Failed to move category ${id}:`, error);
      throw error;
    }
  },

  /**
   * Supprime une catégorie vide
   * DELETE /api/categories/{id}
   * Requiert : ADMINISTRATEUR uniquement
   */
  async deleteCategory(id: string): Promise<void> {
    try {
      await del(`/api/categories/${id}`);
      console.log('✅ Category deleted:', id);
      
    } catch (error) {
      console.error(`❌ Failed to delete category ${id}:`, error);
      throw error;
    }
  },

  // ---------------------------------------------------------------------------
  // MÉTHODES UTILITAIRES
  // ---------------------------------------------------------------------------

  /**
   * Récupère la hiérarchie complète d'une catégorie (parents et enfants)
   */
  async getCategoryHierarchy(categoryId: string): Promise<{
    category: CategoryResponse;
    parents: CategoryResponse[];
    children: CategoryResponse[];
  }> {
    try {
      const [category, children] = await Promise.all([
        this.getCategoryById(categoryId),
        this.getSubCategories(categoryId),
      ]);

      // Construire la chaîne des parents
      const parents: CategoryResponse[] = [];
      let currentCategory = category;
      
      while (currentCategory.parentId) {
        const parent = await this.getCategoryById(currentCategory.parentId);
        parents.unshift(parent);
        currentCategory = parent;
      }

      console.log('✅ Category hierarchy retrieved:', {
        category: category.name,
        parentsCount: parents.length,
        childrenCount: children.length,
      });

      return {
        category,
        parents,
        children,
      };
      
    } catch (error) {
      console.error(`❌ Failed to get category hierarchy for ${categoryId}:`, error);
      throw error;
    }
  },

  /**
   * Recherche de catégories par nom (filtrage local)
   */
  async searchCategories(query: string): Promise<CategoryResponse[]> {
    try {
      // Récupérer toutes les catégories et filtrer localement
      const allCategories = await this.getAllCategories(0, 1000);
      
      const filteredCategories = allCategories.content.filter(category =>
        category.name.toLowerCase().includes(query.toLowerCase()) ||
        category.slug.toLowerCase().includes(query.toLowerCase())
      );

      console.log('✅ Categories search completed:', {
        query,
        totalResults: filteredCategories.length,
      });
      
      return filteredCategories;
      
    } catch (error) {
      console.error('❌ Failed to search categories:', error);
      throw error;
    }
  },

  /**
   * Récupère les statistiques des catégories
   */
  async getCategoryStats(): Promise<{
    totalCategories: number;
    rootCategories: number;
    maxDepth: number;
  }> {
    try {
      const [allCategories, rootCategories] = await Promise.all([
        this.getAllCategories(0, 1000),
        this.getRootCategories(),
      ]);

      const maxDepth = Math.max(...allCategories.content.map(cat => cat.level));

      const stats = {
        totalCategories: allCategories.totalElements,
        rootCategories: rootCategories.length,
        maxDepth,
      };

      console.log('✅ Category stats retrieved:', stats);
      return stats;
      
    } catch (error) {
      console.error('❌ Failed to get category stats:', error);
      throw error;
    }
  },

  /**
   * Valide si une catégorie peut être supprimée (pas d'articles, pas de sous-catégories)
   */
  async canDeleteCategory(categoryId: string): Promise<{
    canDelete: boolean;
    reason?: string;
    subCategoriesCount: number;
  }> {
    try {
      const subCategories = await this.getSubCategories(categoryId);
      
      if (subCategories.length > 0) {
        return {
          canDelete: false,
          reason: `La catégorie contient ${subCategories.length} sous-catégorie(s)`,
          subCategoriesCount: subCategories.length,
        };
      }

      // Note: ici on pourrait aussi vérifier s'il y a des articles dans cette catégorie
      // en utilisant articleService.getArticlesByCategory()

      return {
        canDelete: true,
        subCategoriesCount: 0,
      };
      
    } catch (error) {
      console.error(`❌ Failed to check if category ${categoryId} can be deleted:`, error);
      throw error;
    }
  },
}; 