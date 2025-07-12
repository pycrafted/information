// =============================================================================
// TYPES API - CORRESPONDANCE EXACTE AVEC LE BACKEND
// =============================================================================

// -----------------------------------------------------------------------------
// AUTHENTIFICATION (AuthResponse, User, Tokens)
// -----------------------------------------------------------------------------

export interface User {
  id: string;
  username: string;
  email: string;
  role: UserRole;
  createdAt: string;
  updatedAt: string;
  isActive: boolean;
}

export type UserRole = 'VISITEUR' | 'EDITEUR' | 'ADMINISTRATEUR';

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface UserRequest {
  username: string;
  email: string;
  password: string;
  role: UserRole;
}

// -----------------------------------------------------------------------------
// ARTICLES (ArticleResponse, ArticleRequest, Status)
// -----------------------------------------------------------------------------

export interface ArticleResponse {
  id: string;
  title: string;
  content: string;
  summary: string;
  slug: string;
  status: ArticleStatus;
  authorId: string;
  authorUsername: string;
  categoryId: string;
  categoryName: string;
  createdAt: string;
  updatedAt: string;
  publishedAt?: string;
}

export interface ArticleRequest {
  title: string;
  content: string;
  summary: string;
  categoryId: string;
}

export type ArticleStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

// -----------------------------------------------------------------------------
// CATÉGORIES (CategoryResponse, CategoryRequest)
// -----------------------------------------------------------------------------

export interface CategoryResponse {
  id: string;
  name: string;
  slug: string;
  description?: string;
  parentId?: string;
  parentName?: string;
  level: number;
  children?: CategoryResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface CategoryRequest {
  name: string;
  description?: string;
  parentId?: string;
}

// -----------------------------------------------------------------------------
// PAGINATION (Page)
// -----------------------------------------------------------------------------

export interface Page<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    offset: number;
    sort: {
      sorted: boolean;
      empty: boolean;
      unsorted: boolean;
    };
    paged: boolean;
    unpaged: boolean;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  size: number;
  number: number;
  numberOfElements: number;
  empty: boolean;
  sort: {
    sorted: boolean;
    empty: boolean;
    unsorted: boolean;
  };
}

// -----------------------------------------------------------------------------
// REQUÊTES DE RECHERCHE
// -----------------------------------------------------------------------------

export interface SearchParams {
  query?: string;
  categoryId?: string;
  status?: ArticleStatus;
  page?: number;
  size?: number;
  sort?: string;
}

// -----------------------------------------------------------------------------
// GESTION DES ERREURS API
// -----------------------------------------------------------------------------

export interface ApiError {
  error: string;
  message: string;
  timestamp: string;
  status?: number;
}

// -----------------------------------------------------------------------------
// AUDIT LOGS (pour l'administration)
// -----------------------------------------------------------------------------

export interface AuditLog {
  id: string;
  action: string;
  entityType: string;
  entityId: string;
  userId: string;
  username: string;
  timestamp: string;
  details?: string;
}

// -----------------------------------------------------------------------------
// TOKENS (pour la gestion administrative)
// -----------------------------------------------------------------------------

export interface AuthToken {
  id: string;
  token: string;
  userId: string;
  username: string;
  expiresAt: string;
  isActive: boolean;
  createdAt: string;
}

export interface RefreshToken {
  id: string;
  token: string;
  userId: string;
  username: string;
  expiresAt: string;
  isActive: boolean;
  createdAt: string;
}

// -----------------------------------------------------------------------------
// SERVICES SOAP (types pour les endpoints SOAP)
// -----------------------------------------------------------------------------

export interface LoginSoapRequest {
  email: string;
  password: string;
}

export interface LoginSoapResponse {
  success: boolean;
  token?: string;
  message?: string;
}

export interface LogoutSoapRequest {
  token: string;
}

export interface LogoutSoapResponse {
  success: boolean;
  message?: string;
} 