import axios, { AxiosInstance, AxiosResponse, AxiosError } from 'axios'
import { toast } from 'react-hot-toast'

/**
 * Service API central pour toutes les communications avec le backend Spring Boot
 * 
 * Features :
 * - Configuration Axios avec base URL et timeouts
 * - Intercepteurs pour JWT automatique
 * - Gestion du refresh token automatique
 * - Gestion centralisée des erreurs
 * - Types TypeScript complets
 */

// Configuration de base
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
const REQUEST_TIMEOUT = 30000 // 30 secondes

/**
 * Instance Axios configurée avec les intercepteurs
 */
export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: REQUEST_TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
})

/**
 * Gestion du token dans le localStorage
 */
export const tokenManager = {
  getAccessToken: (): string | null => {
    return localStorage.getItem('accessToken')
  },
  
  setAccessToken: (token: string): void => {
    localStorage.setItem('accessToken', token)
  },
  
  getRefreshToken: (): string | null => {
    return localStorage.getItem('refreshToken')
  },
  
  setRefreshToken: (token: string): void => {
    localStorage.setItem('refreshToken', token)
  },
  
  clearTokens: (): void => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  },
  
  setTokens: (accessToken: string, refreshToken: string): void => {
    tokenManager.setAccessToken(accessToken)
    tokenManager.setRefreshToken(refreshToken)
  }
}

/**
 * Flag pour éviter les boucles infinies lors du refresh
 */
let isRefreshing = false
let failedQueue: Array<{
  resolve: (value?: any) => void
  reject: (error?: any) => void
}> = []

/**
 * Traite la queue des requêtes en attente après refresh
 */
const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error)
    } else {
      resolve(token)
    }
  })
  
  failedQueue = []
}

/**
 * Intercepteur de requête - Ajoute automatiquement le JWT
 */
apiClient.interceptors.request.use(
  (config) => {
    const token = tokenManager.getAccessToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * Intercepteur de réponse - Gère le refresh automatique du token
 */
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    return response
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as any
    
    // Si erreur 401 et pas déjà en train de refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // Si refresh en cours, mettre en queue
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then(token => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          return apiClient(originalRequest)
        }).catch(err => {
          return Promise.reject(err)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      const refreshToken = tokenManager.getRefreshToken()
      
      if (!refreshToken) {
        // Pas de refresh token, rediriger vers login
        tokenManager.clearTokens()
        window.location.href = '/login'
        return Promise.reject(error)
      }

      try {
        // Tentative de refresh du token
        const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
          refreshToken
        })
        
        const { accessToken, refreshToken: newRefreshToken } = response.data
        
        // Mettre à jour les tokens
        tokenManager.setTokens(accessToken, newRefreshToken)
        
        // Traiter la queue
        processQueue(null, accessToken)
        
        // Retry la requête originale
        originalRequest.headers.Authorization = `Bearer ${accessToken}`
        return apiClient(originalRequest)
        
      } catch (refreshError) {
        // Refresh failed, déconnecter
        processQueue(refreshError, null)
        tokenManager.clearTokens()
        window.location.href = '/login'
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }
    
    return Promise.reject(error)
  }
)

/**
 * Gestionnaire d'erreur centralisé
 */
export const handleApiError = (error: any): string => {
  if (error.response) {
    // Erreur du serveur (4xx, 5xx)
    const status = error.response.status
    const message = error.response.data?.message || error.message
    
    switch (status) {
      case 400:
        toast.error('Données invalides')
        return message || 'Données invalides'
      case 401:
        toast.error('Non autorisé')
        return 'Session expirée, veuillez vous reconnecter'
      case 403:
        toast.error('Accès interdit')
        return 'Vous n\'avez pas les permissions nécessaires'
      case 404:
        toast.error('Ressource non trouvée')
        return 'Ressource non trouvée'
      case 409:
        toast.error('Conflit de données')
        return message || 'Conflit de données'
      case 500:
        toast.error('Erreur serveur')
        return 'Erreur interne du serveur'
      default:
        toast.error('Erreur inconnue')
        return message || 'Une erreur est survenue'
    }
  } else if (error.request) {
    // Pas de réponse du serveur
    toast.error('Serveur indisponible')
    return 'Impossible de contacter le serveur'
  } else {
    // Erreur de configuration
    toast.error('Erreur de configuration')
    return error.message || 'Erreur de configuration'
  }
}

/**
 * Helper pour les requêtes avec gestion d'erreur automatique
 */
export const apiRequest = async <T>(
  requestFn: () => Promise<AxiosResponse<T>>
): Promise<T> => {
  try {
    const response = await requestFn()
    return response.data
  } catch (error) {
    const errorMessage = handleApiError(error)
    throw new Error(errorMessage)
  }
}

/**
 * Export de l'instance pour utilisation directe
 */
export default apiClient 