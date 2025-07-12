import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';

// =============================================================================
// CONFIGURATION API CLIENT
// =============================================================================

/**
 * Instance Axios configurée pour le backend News Platform
 * Base URL: http://localhost:8080 (backend Spring Boot)
 */
const api: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000, // 10 secondes
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
});

// =============================================================================
// INTERCEPTEURS DE REQUÊTE ET RÉPONSE
// =============================================================================

/**
 * Intercepteur de requête - Ajoute automatiquement le token JWT
 */
api.interceptors.request.use(
  (config) => {
    // Récupérer le token JWT du localStorage
    const token = localStorage.getItem('accessToken');
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // Log de la requête pour debug en développement
    console.log(`🔹 API Request: ${config.method?.toUpperCase()} ${config.url}`);
    
    return config;
  },
  (error) => {
    console.error('❌ Request Error:', error);
    return Promise.reject(error);
  }
);

/**
 * Intercepteur de réponse - Gestion globale des erreurs et refresh token
 */
api.interceptors.response.use(
  (response: AxiosResponse) => {
    // Log de la réponse pour debug en développement
    console.log(`✅ API Response: ${response.status} ${response.config.url}`);
    
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    
    // Si erreur 401 (Unauthorized) et ce n'est pas déjà une tentative de refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      const refreshToken = localStorage.getItem('refreshToken');
      
      if (refreshToken) {
        try {
          // Tentative de refresh du token
          const response = await axios.post('http://localhost:8080/auth/refresh', {
            refreshToken,
          });
          
          const { accessToken } = response.data;
          
          // Sauvegarder le nouveau token
          localStorage.setItem('accessToken', accessToken);
          
          // Retenter la requête originale avec le nouveau token
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          return api(originalRequest);
          
        } catch (refreshError) {
          // Si le refresh échoue, déconnecter l'utilisateur
          console.error('❌ Token refresh failed:', refreshError);
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          
          // Rediriger vers la page de connexion
          window.location.href = '/login';
          
          return Promise.reject(refreshError);
        }
      } else {
        // Pas de refresh token, rediriger vers login
        window.location.href = '/login';
      }
    }
    
    // Log des erreurs pour debug
    console.error(`❌ API Error: ${error.response?.status} ${error.config?.url}`, {
      response: error.response?.data,
    });
    
    return Promise.reject(error);
  }
);

// =============================================================================
// MÉTHODES HELPER POUR DIFFÉRENTS TYPES DE REQUÊTES
// =============================================================================

/**
 * GET request helper
 */
export const get = async <T = any>(url: string, config?: AxiosRequestConfig): Promise<T> => {
  const response = await api.get<T>(url, config);
  return response.data;
};

/**
 * POST request helper
 */
export const post = async <T = any>(
  url: string, 
  data?: any, 
  config?: AxiosRequestConfig
): Promise<T> => {
  const response = await api.post<T>(url, data, config);
  return response.data;
};

/**
 * PUT request helper
 */
export const put = async <T = any>(
  url: string, 
  data?: any, 
  config?: AxiosRequestConfig
): Promise<T> => {
  const response = await api.put<T>(url, data, config);
  return response.data;
};

/**
 * PATCH request helper
 */
export const patch = async <T = any>(
  url: string, 
  data?: any, 
  config?: AxiosRequestConfig
): Promise<T> => {
  const response = await api.patch<T>(url, data, config);
  return response.data;
};

/**
 * DELETE request helper
 */
export const del = async <T = any>(url: string, config?: AxiosRequestConfig): Promise<T> => {
  const response = await api.delete<T>(url, config);
  return response.data;
};

// =============================================================================
// MÉTHODES UTILITAIRES
// =============================================================================

/**
 * Vérifie si l'utilisateur est connecté (a un token valide)
 */
export const isAuthenticated = (): boolean => {
  const token = localStorage.getItem('accessToken');
  return !!token;
};

/**
 * Nettoie les tokens de l'utilisateur (déconnexion)
 */
export const clearTokens = (): void => {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
};

/**
 * Récupère le token d'accès actuel
 */
export const getAccessToken = (): string | null => {
  return localStorage.getItem('accessToken');
};

/**
 * Sauvegarde les tokens d'authentification
 */
export const setTokens = (accessToken: string, refreshToken: string): void => {
  localStorage.setItem('accessToken', accessToken);
  localStorage.setItem('refreshToken', refreshToken);
};

export default api; 