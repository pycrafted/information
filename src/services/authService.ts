import { get, post, clearTokens, setTokens } from './api';
import type { 
  AuthResponse, 
  LoginRequest, 
  User,
  LoginSoapRequest,
  LoginSoapResponse,
  LogoutSoapRequest,
  LogoutSoapResponse 
} from '../types/api';

// =============================================================================
// SERVICE D'AUTHENTIFICATION - ENDPOINTS REST
// =============================================================================

/**
 * Service d'authentification pour tous les endpoints REST et SOAP
 * Correspond exactement aux contrôleurs AuthController et AuthEndpoint du backend
 */
export const authService = {
  
  // ---------------------------------------------------------------------------
  // ENDPOINTS REST (/api/auth/*)
  // ---------------------------------------------------------------------------
  
  /**
   * Connexion utilisateur via REST API
   * POST /api/auth/login
   */
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    try {
      const response = await post<AuthResponse>('/api/auth/login', credentials);
      
      // Sauvegarder les tokens automatiquement
      setTokens(response.accessToken, response.refreshToken);
      
      console.log('✅ Login successful:', response.user.username);
      return response;
      
    } catch (error) {
      console.error('❌ Login failed:', error);
      throw error;
    }
  },

  /**
   * Déconnexion utilisateur via REST API
   * POST /api/auth/logout
   */
  async logout(): Promise<void> {
    try {
      await post('/api/auth/logout');
      
      // Nettoyer les tokens locaux
      clearTokens();
      
      console.log('✅ Logout successful');
      
    } catch (error) {
      console.error('❌ Logout failed:', error);
      // Nettoyer les tokens même en cas d'erreur
      clearTokens();
      throw error;
    }
  },

  /**
   * Refresh du token d'accès
   * POST /api/auth/refresh
   */
  async refreshToken(): Promise<AuthResponse> {
    try {
      const refreshToken = localStorage.getItem('refreshToken');
      
      if (!refreshToken) {
        throw new Error('No refresh token available');
      }
      
      const response = await post<AuthResponse>('/api/auth/refresh', {
        refreshToken,
      });
      
      // Sauvegarder le nouveau token
      setTokens(response.accessToken, response.refreshToken);
      
      console.log('✅ Token refreshed successfully');
      return response;
      
    } catch (error) {
      console.error('❌ Token refresh failed:', error);
      clearTokens();
      throw error;
    }
  },

  /**
   * Récupérer les informations de l'utilisateur connecté
   * GET /api/auth/me
   */
  async getCurrentUser(): Promise<User> {
    try {
      const user = await get<User>('/api/auth/me');
      console.log('✅ Current user retrieved:', user.username);
      return user;
      
    } catch (error) {
      console.error('❌ Failed to get current user:', error);
      throw error;
    }
  },

  /**
   * Vérifier la validité du token actuel
   * GET /api/auth/validate
   */
  async validateToken(): Promise<boolean> {
    try {
      await get('/api/auth/validate');
      console.log('✅ Token is valid');
      return true;
      
    } catch (error) {
      console.error('❌ Token validation failed:', error);
      return false;
    }
  },

  // ---------------------------------------------------------------------------
  // ENDPOINTS SOAP (/soap/auth)
  // ---------------------------------------------------------------------------

  /**
   * Connexion utilisateur via SOAP
   * POST /soap/auth (LoginSoapRequest)
   */
  async loginSoap(credentials: LoginSoapRequest): Promise<LoginSoapResponse> {
    try {
      const soapBody = `
        <?xml version="1.0" encoding="UTF-8"?>
        <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                       xmlns:auth="http://newsplatform.com/auth">
          <soap:Body>
            <auth:LoginRequest>
              <auth:email>${credentials.email}</auth:email>
              <auth:password>${credentials.password}</auth:password>
            </auth:LoginRequest>
          </soap:Body>
        </soap:Envelope>
      `;

      const response = await post<LoginSoapResponse>('/soap/auth', soapBody, {
        headers: {
          'Content-Type': 'text/xml; charset=utf-8',
          'SOAPAction': 'login',
        },
      });

      console.log('✅ SOAP Login successful');
      return response;

    } catch (error) {
      console.error('❌ SOAP Login failed:', error);
      throw error;
    }
  },

  /**
   * Déconnexion utilisateur via SOAP
   * POST /soap/auth (LogoutSoapRequest)
   */
  async logoutSoap(request: LogoutSoapRequest): Promise<LogoutSoapResponse> {
    try {
      const soapBody = `
        <?xml version="1.0" encoding="UTF-8"?>
        <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                       xmlns:auth="http://newsplatform.com/auth">
          <soap:Body>
            <auth:LogoutRequest>
              <auth:token>${request.token}</auth:token>
            </auth:LogoutRequest>
          </soap:Body>
        </soap:Envelope>
      `;

      const response = await post<LogoutSoapResponse>('/soap/auth', soapBody, {
        headers: {
          'Content-Type': 'text/xml; charset=utf-8',
          'SOAPAction': 'logout',
        },
      });

      // Nettoyer les tokens locaux
      clearTokens();

      console.log('✅ SOAP Logout successful');
      return response;

    } catch (error) {
      console.error('❌ SOAP Logout failed:', error);
      // Nettoyer les tokens même en cas d'erreur
      clearTokens();
      throw error;
    }
  },

  // ---------------------------------------------------------------------------
  // MÉTHODES UTILITAIRES
  // ---------------------------------------------------------------------------

  /**
   * Vérifie si l'utilisateur est connecté
   */
  isAuthenticated(): boolean {
    const token = localStorage.getItem('accessToken');
    return !!token;
  },

  /**
   * Récupère le rôle de l'utilisateur connecté
   */
  async getUserRole(): Promise<string | null> {
    try {
      if (!this.isAuthenticated()) {
        return null;
      }

      const user = await this.getCurrentUser();
      return user.role;

    } catch (error) {
      console.error('❌ Failed to get user role:', error);
      return null;
    }
  },

  /**
   * Vérifie si l'utilisateur a un rôle spécifique ou supérieur
   */
  async hasRole(requiredRole: string): Promise<boolean> {
    try {
      const userRole = await this.getUserRole();
      
      if (!userRole) {
        return false;
      }

      const roleHierarchy: Record<string, number> = {
        'VISITEUR': 1,
        'EDITEUR': 2,
        'ADMINISTRATEUR': 3,
      };

      const userLevel = roleHierarchy[userRole] || 0;
      const requiredLevel = roleHierarchy[requiredRole] || 0;

      return userLevel >= requiredLevel;

    } catch (error) {
      console.error('❌ Failed to check user role:', error);
      return false;
    }
  },

  /**
   * Déconnexion forcée (nettoie tout)
   */
  forceLogout(): void {
    clearTokens();
    console.log('🔒 Force logout completed');
  },
}; 