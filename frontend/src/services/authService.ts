import api from './api';

// Types pour l'authentification
export interface LoginCredentials {
  username: string;
  password: string;
  rememberMe?: boolean;
}

export interface User {
  id: string;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  role: 'VISITEUR' | 'EDITEUR' | 'ADMINISTRATEUR';
  roleDescription: string;
}

export interface AuthResponse {
  success: boolean;
  message: string;
  accessToken: string;
  refreshToken: string;
  accessTokenExpiresAt: string;
  refreshTokenExpiresAt: string;
  user: User;
}

// Clés pour le stockage local
const ACCESS_TOKEN_KEY = 'news_platform_access_token';
const REFRESH_TOKEN_KEY = 'news_platform_refresh_token';
const USER_KEY = 'news_platform_user';

/**
 * Connexion utilisateur
 * Endpoint : POST /api/auth/login
 */
export const login = async (credentials: LoginCredentials): Promise<AuthResponse> => {
  try {
    console.log('🔐 Tentative de connexion pour:', credentials.username);
    
    const response = await api.post<AuthResponse>('/api/auth/login', credentials);
    
    if (response.data.success) {
      // Stocker les tokens et les informations utilisateur
      localStorage.setItem(ACCESS_TOKEN_KEY, response.data.accessToken);
      localStorage.setItem(REFRESH_TOKEN_KEY, response.data.refreshToken);
      localStorage.setItem(USER_KEY, JSON.stringify(response.data.user));
      
      console.log(`✅ Connexion réussie pour ${response.data.user.username} (${response.data.user.role})`);
      
      // Configurer le token par défaut pour les prochaines requêtes
      setAuthToken(response.data.accessToken);
      
      return response.data;
    } else {
      throw new Error(response.data.message || 'Échec de la connexion');
    }
  } catch (error: any) {
    console.error('❌ Erreur lors de la connexion:', error);
    
    // Nettoyer en cas d'erreur
    clearAuthData();
    
    throw new Error(
      error.response?.data?.message || 
      error.message || 
      'Erreur lors de la connexion'
    );
  }
};

/**
 * Déconnexion utilisateur
 * Endpoint : POST /api/auth/logout
 */
export const logout = async (): Promise<void> => {
  try {
    const accessToken = getAccessToken();
    
    if (accessToken) {
      console.log('🔓 Déconnexion en cours...');
      
      // Appeler l'endpoint de déconnexion
      await api.post('/api/auth/logout');
      
      console.log('✅ Déconnexion réussie côté serveur');
    }
  } catch (error: any) {
    console.warn('⚠️ Erreur lors de la déconnexion serveur (on continue quand même):', error.message);
  } finally {
    // Toujours nettoyer côté client
    clearAuthData();
    console.log('✅ Données d\'authentification nettoyées côté client');
  }
};

/**
 * Rafraîchir le token d'accès
 * Endpoint : POST /api/auth/refresh
 */
export const refreshToken = async (): Promise<string | null> => {
  try {
    const refreshTokenValue = getRefreshToken();
    
    if (!refreshTokenValue) {
      throw new Error('Pas de refresh token disponible');
    }
    
    console.log('🔄 Rafraîchissement du token d\'accès...');
    
    const response = await api.post<AuthResponse>('/api/auth/refresh', {
      refreshToken: refreshTokenValue
    });
    
    if (response.data.success) {
      // Mettre à jour les tokens
      localStorage.setItem(ACCESS_TOKEN_KEY, response.data.accessToken);
      localStorage.setItem(REFRESH_TOKEN_KEY, response.data.refreshToken);
      
      setAuthToken(response.data.accessToken);
      
      console.log('✅ Token d\'accès rafraîchi avec succès');
      return response.data.accessToken;
    } else {
      throw new Error('Échec du rafraîchissement');
    }
  } catch (error: any) {
    console.error('❌ Erreur lors du rafraîchissement du token:', error);
    
    // En cas d'échec, déconnecter complètement
    clearAuthData();
    return null;
  }
};

/**
 * Vérifier si l'utilisateur est connecté
 */
export const isAuthenticated = (): boolean => {
  const token = getAccessToken();
  const user = getCurrentUser();
  return !!(token && user);
};

/**
 * Obtenir l'utilisateur actuel depuis le localStorage
 */
export const getCurrentUser = (): User | null => {
  try {
    const userStr = localStorage.getItem(USER_KEY);
    return userStr ? JSON.parse(userStr) : null;
  } catch (error) {
    console.error('❌ Erreur lors de la récupération de l\'utilisateur:', error);
    return null;
  }
};

/**
 * Obtenir le token d'accès
 */
export const getAccessToken = (): string | null => {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
};

/**
 * Obtenir le refresh token
 */
export const getRefreshToken = (): string | null => {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
};

/**
 * Configurer le token d'autorisation pour axios
 */
export const setAuthToken = (token: string | null): void => {
  if (token) {
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  } else {
    delete api.defaults.headers.common['Authorization'];
  }
};

/**
 * Nettoyer toutes les données d'authentification
 */
export const clearAuthData = (): void => {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  setAuthToken(null);
};

/**
 * Initialiser l'authentification au démarrage de l'app
 */
export const initializeAuth = (): User | null => {
  const token = getAccessToken();
  const user = getCurrentUser();
  
  if (token && user) {
    console.log(`🔐 Utilisateur connecté restauré: ${user.username} (${user.role})`);
    setAuthToken(token);
    return user;
  } else {
    console.log('👤 Aucun utilisateur connecté');
    clearAuthData();
    return null;
  }
};

/**
 * Vérifier les permissions par rôle
 */
export const hasRole = (user: User | null, requiredRole: User['role']): boolean => {
  if (!user) return false;
  
  // Hiérarchie des rôles : ADMINISTRATEUR > EDITEUR > VISITEUR
  const roleHierarchy = {
    'VISITEUR': 1,
    'EDITEUR': 2,
    'ADMINISTRATEUR': 3
  };
  
  const userLevel = roleHierarchy[user.role] || 0;
  const requiredLevel = roleHierarchy[requiredRole] || 0;
  
  return userLevel >= requiredLevel;
};

/**
 * Comptes de test pour faciliter les tests
 */
export const TEST_ACCOUNTS = {
  admin: {
    username: 'admin',
    password: 'OusmaneSonko@2029',
    role: 'ADMINISTRATEUR' as const,
    description: 'Accès complet : gestion utilisateurs, articles, catégories'
  },
  editeur: {
    username: 'editeur', 
    password: 'OusmaneSonko@2029',
    role: 'EDITEUR' as const,
    description: 'Gestion articles et catégories'
  },
  visiteur: {
    username: 'visiteur',
    password: 'OusmaneSonko@2029', 
    role: 'VISITEUR' as const,
    description: 'Lecture seule des articles publics'
  }
}; 