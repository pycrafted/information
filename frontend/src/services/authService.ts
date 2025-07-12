import { AxiosResponse } from 'axios'
import { 
  LoginRequest, 
  AuthResponse, 
  User
} from '../types/api'
import { apiClient } from './api'

/**
 * Service d'authentification pour l'API News Platform
 * 
 * Fonctionnalités :
 * - Connexion/déconnexion REST
 * - Refresh des tokens
 * - Récupération du profil utilisateur
 * - Gestion des erreurs
 */

/**
 * Connexion REST - POST /api/auth/login
 */
export const login = async (credentials: LoginRequest): Promise<AxiosResponse<AuthResponse>> => {
  return apiClient.post<AuthResponse>('/auth/login', credentials)
}

/**
 * Déconnexion REST - POST /api/auth/logout
 */
export const logout = async (refreshToken: string): Promise<AxiosResponse<void>> => {
  return apiClient.post<void>('/auth/logout', { refreshToken })
}

/**
 * Refresh du token - POST /api/auth/refresh
 */
export const refreshToken = async (refreshToken: string): Promise<AxiosResponse<AuthResponse>> => {
  return apiClient.post<AuthResponse>('/auth/refresh', { refreshToken })
}

/**
 * Récupérer l'utilisateur actuel - GET /api/auth/me
 */
export const getCurrentUser = async (): Promise<AxiosResponse<User>> => {
  return apiClient.get<User>('/auth/me')
}

/**
 * Vérifier si l'utilisateur est connecté
 */
export const checkAuthStatus = async (): Promise<AxiosResponse<{ authenticated: boolean; user?: User }>> => {
  return apiClient.get('/auth/status')
}

/**
 * Export par défaut pour compatibilité
 */
const authService = {
  login,
  logout,
  refreshToken,
  getCurrentUser,
  checkAuthStatus
}

export default authService 