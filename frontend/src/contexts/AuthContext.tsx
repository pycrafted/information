import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { 
  type User, 
  type LoginCredentials,
  login as authLogin,
  logout as authLogout,
  initializeAuth,
  isAuthenticated as checkAuth
} from '../services/authService';

// Types pour le contexte
interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginCredentials) => Promise<void>;
  logout: () => Promise<void>;
  hasRole: (requiredRole: User['role']) => boolean;
}

// Création du contexte
const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Props du provider
interface AuthProviderProps {
  children: ReactNode;
}

/**
 * Provider d'authentification
 * Gère l'état global de l'utilisateur connecté
 */
export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Initialiser l'authentification au montage
  useEffect(() => {
    const initAuth = async () => {
      try {
        console.log('🔄 Initialisation de l\'authentification...');
        
        // Restaurer l'utilisateur depuis localStorage
        const currentUser = initializeAuth();
        setUser(currentUser);
        
        if (currentUser) {
          console.log(`✅ Session restaurée pour ${currentUser.username}`);
        } else {
          console.log('👤 Aucune session existante');
        }
      } catch (error) {
        console.error('❌ Erreur lors de l\'initialisation auth:', error);
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    };

    initAuth();
  }, []);

  /**
   * Connexion utilisateur
   */
  const login = async (credentials: LoginCredentials): Promise<void> => {
    try {
      setIsLoading(true);
      
      const authResponse = await authLogin(credentials);
      setUser(authResponse.user);
      
      console.log(`✅ Connexion réussie dans le contexte pour ${authResponse.user.username}`);
    } catch (error) {
      console.error('❌ Erreur de connexion dans le contexte:', error);
      setUser(null);
      throw error; // Re-lancer pour que le composant puisse gérer l'erreur
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Déconnexion utilisateur
   */
  const logout = async (): Promise<void> => {
    try {
      setIsLoading(true);
      
      await authLogout();
      setUser(null);
      
      console.log('✅ Déconnexion réussie dans le contexte');
    } catch (error) {
      console.error('❌ Erreur de déconnexion dans le contexte:', error);
      // Forcer la déconnexion côté client même en cas d'erreur serveur
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Vérifier si l'utilisateur a un rôle spécifique
   */
  const hasRole = (requiredRole: User['role']): boolean => {
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

  // Valeur du contexte
  const contextValue: AuthContextType = {
    user,
    isAuthenticated: checkAuth(),
    isLoading,
    login,
    logout,
    hasRole
  };

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
}

/**
 * Hook personnalisé pour utiliser le contexte d'authentification
 */
export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  
  return context;
}

/**
 * Hook pour vérifier les permissions
 */
export function usePermissions() {
  const { user, hasRole } = useAuth();
  
  return {
    isVisitor: user?.role === 'VISITEUR',
    isEditor: hasRole('EDITEUR'),
    isAdmin: hasRole('ADMINISTRATEUR'),
    canCreateArticles: hasRole('EDITEUR'),
    canManageUsers: hasRole('ADMINISTRATEUR'),
    canDeleteArticles: hasRole('ADMINISTRATEUR')
  };
} 