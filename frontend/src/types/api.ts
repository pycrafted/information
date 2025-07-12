/**
 * Types et interfaces correspondant exactement aux DTOs du backend Spring Boot
 * 
 * Respecte la structure :
 * - User avec les rôles VISITEUR, EDITEUR, ADMINISTRATEUR
 * - Article avec statut DRAFT, PUBLISHED, ARCHIVED
 * - Category avec structure hiérarchique
 * - Tokens JWT et Refresh
 */

// ================================
// ENUMS
// ================================

export type UserRole = 'VISITEUR' | 'EDITEUR' | 'ADMINISTRATEUR'

export type ArticleStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'

// ================================
// ENTITIES
// ================================

export interface User {
  id: number
  username: string
  email: string
  role: UserRole
  createdAt: string
  updatedAt: string
}

export interface Category {
  id: number
  name: string
  slug: string
  description?: string
  parentId?: number
  level: number
  createdAt: string
  updatedAt: string
  children?: Category[]
}

export interface Article {
  id: number
  title: string
  content: string
  summary?: string
  slug: string
  status: ArticleStatus
  categoryId: number
  authorId: number
  publishedAt?: string
  createdAt: string
  updatedAt: string
  category?: Category
  author?: User
}

// ================================
// REQUEST DTOs
// ================================

export interface LoginRequest {
  username: string
  password: string
}

export interface UserRequest {
  username: string
  email: string
  password: string
  role?: UserRole
}

export interface ArticleRequest {
  title: string
  content: string
  summary?: string
  slug: string
  status: ArticleStatus
  categoryId: number
}

export interface CategoryRequest {
  name: string
  slug: string
  description?: string
  parentId?: number
}

// ================================
// RESPONSE DTOs
// ================================

export interface AuthResponse {
  user: User
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
}

export interface UserResponse {
  id: number
  username: string
  email: string
  role: UserRole
  createdAt: string
  updatedAt: string
}

export interface ArticleResponse {
  id: number
  title: string
  content: string
  summary?: string
  slug: string
  status: ArticleStatus
  categoryId: number
  authorId: number
  publishedAt?: string
  createdAt: string
  updatedAt: string
  category: CategoryResponse
  author: UserResponse
}

export interface CategoryResponse {
  id: number
  name: string
  slug: string
  description?: string
  parentId?: number
  level: number
  createdAt: string
  updatedAt: string
  children?: CategoryResponse[]
}

// ================================
// API RESPONSE WRAPPERS
// ================================

export interface ApiResponse<T> {
  data: T
  message?: string
  status: number
}

export interface PaginatedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export interface ApiError {
  message: string
  code?: string
  details?: Record<string, any>
  timestamp: string
}

// ================================
// SOAP DTOs
// ================================

export interface LoginSoapRequest {
  username: string
  password: string
}

export interface LoginSoapResponse {
  success: boolean
  message: string
  token?: string
  user?: UserResponse
}

export interface LogoutSoapRequest {
  token: string
}

export interface LogoutSoapResponse {
  success: boolean
  message: string
}

// ================================
// UTILITY TYPES
// ================================

export type LoadingState = 'idle' | 'loading' | 'succeeded' | 'failed'

export interface AsyncState<T> {
  data: T | null
  loading: boolean
  error: string | null
}

export interface AuthState {
  user: User | null
  accessToken: string | null
  refreshToken: string | null
  isAuthenticated: boolean
  loading: boolean
  error: string | null
} 