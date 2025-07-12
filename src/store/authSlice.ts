import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { authService } from '../services/authService';
import type { User, LoginRequest, AuthResponse } from '../types/api';

// =============================================================================
// TYPES DU STATE
// =============================================================================

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  accessToken: string | null;
  refreshToken: string | null;
  loading: boolean;
  error: string | null;
  lastLoginAt: string | null;
}

// =============================================================================
// STATE INITIAL
// =============================================================================

const initialState: AuthState = {
  user: null,
  isAuthenticated: false,
  accessToken: localStorage.getItem('accessToken'),
  refreshToken: localStorage.getItem('refreshToken'),
  loading: false,
  error: null,
  lastLoginAt: null,
};

// =============================================================================
// ASYNC THUNKS - ACTIONS ASYNCHRONES
// =============================================================================

/**
 * Action pour connecter un utilisateur
 */
export const loginUser = createAsyncThunk<
  AuthResponse,
  LoginRequest,
  { rejectValue: string }
>(
  'auth/login',
  async (credentials, { rejectWithValue }) => {
    try {
      const response = await authService.login(credentials);
      return response;
    } catch (error: any) {
      const message = error.response?.data?.message || error.message || 'Erreur de connexion';
      return rejectWithValue(message);
    }
  }
);

/**
 * Action pour déconnecter un utilisateur
 */
export const logoutUser = createAsyncThunk<
  void,
  void,
  { rejectValue: string }
>(
  'auth/logout',
  async (_, { rejectWithValue }) => {
    try {
      await authService.logout();
    } catch (error: any) {
      // Même en cas d'erreur, on déconnecte localement
      console.warn('Logout error (will proceed anyway):', error);
    }
  }
);

/**
 * Action pour refresh le token
 */
export const refreshUserToken = createAsyncThunk<
  AuthResponse,
  void,
  { rejectValue: string }
>(
  'auth/refresh',
  async (_, { rejectWithValue }) => {
    try {
      const response = await authService.refreshToken();
      return response;
    } catch (error: any) {
      const message = error.response?.data?.message || 'Échec du refresh token';
      return rejectWithValue(message);
    }
  }
);

/**
 * Action pour récupérer l'utilisateur connecté
 */
export const getCurrentUser = createAsyncThunk<
  User,
  void,
  { rejectValue: string }
>(
  'auth/getCurrentUser',
  async (_, { rejectWithValue }) => {
    try {
      const user = await authService.getCurrentUser();
      return user;
    } catch (error: any) {
      const message = error.response?.data?.message || 'Impossible de récupérer les informations utilisateur';
      return rejectWithValue(message);
    }
  }
);

/**
 * Action pour valider le token actuel
 */
export const validateToken = createAsyncThunk<
  boolean,
  void,
  { rejectValue: string }
>(
  'auth/validateToken',
  async (_, { rejectWithValue }) => {
    try {
      const isValid = await authService.validateToken();
      return isValid;
    } catch (error: any) {
      return rejectWithValue('Token invalide');
    }
  }
);

// =============================================================================
// SLICE REDUX
// =============================================================================

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    /**
     * Nettoie les erreurs
     */
    clearError: (state) => {
      state.error = null;
    },

    /**
     * Initialise l'auth depuis le localStorage
     */
    initializeAuth: (state) => {
      const token = localStorage.getItem('accessToken');
      const refreshToken = localStorage.getItem('refreshToken');
      
      if (token && refreshToken) {
        state.accessToken = token;
        state.refreshToken = refreshToken;
        state.isAuthenticated = true;
      }
    },

    /**
     * Déconnexion forcée (nettoie tout)
     */
    forceLogout: (state) => {
      state.user = null;
      state.isAuthenticated = false;
      state.accessToken = null;
      state.refreshToken = null;
      state.error = null;
      state.lastLoginAt = null;
      
      // Nettoyer le localStorage
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
    },

    /**
     * Met à jour les informations utilisateur
     */
    updateUser: (state, action: PayloadAction<Partial<User>>) => {
      if (state.user) {
        state.user = { ...state.user, ...action.payload };
      }
    },

    /**
     * Marque comme connecté (pour l'initialisation)
     */
    setAuthenticated: (state, action: PayloadAction<boolean>) => {
      state.isAuthenticated = action.payload;
    },
  },

  extraReducers: (builder) => {
    builder
      // =======================================================
      // LOGIN USER
      // =======================================================
      .addCase(loginUser.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        state.loading = false;
        state.user = action.payload.user;
        state.accessToken = action.payload.accessToken;
        state.refreshToken = action.payload.refreshToken;
        state.isAuthenticated = true;
        state.lastLoginAt = new Date().toISOString();
        state.error = null;
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || 'Erreur de connexion';
        state.isAuthenticated = false;
        state.user = null;
        state.accessToken = null;
        state.refreshToken = null;
      })

      // =======================================================
      // LOGOUT USER
      // =======================================================
      .addCase(logoutUser.pending, (state) => {
        state.loading = true;
      })
      .addCase(logoutUser.fulfilled, (state) => {
        state.loading = false;
        state.user = null;
        state.accessToken = null;
        state.refreshToken = null;
        state.isAuthenticated = false;
        state.error = null;
        state.lastLoginAt = null;
      })
      .addCase(logoutUser.rejected, (state) => {
        // Même en cas d'erreur, déconnecter localement
        state.loading = false;
        state.user = null;
        state.accessToken = null;
        state.refreshToken = null;
        state.isAuthenticated = false;
        state.lastLoginAt = null;
      })

      // =======================================================
      // REFRESH TOKEN
      // =======================================================
      .addCase(refreshUserToken.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(refreshUserToken.fulfilled, (state, action) => {
        state.loading = false;
        state.accessToken = action.payload.accessToken;
        state.refreshToken = action.payload.refreshToken;
        state.user = action.payload.user;
        state.isAuthenticated = true;
        state.error = null;
      })
      .addCase(refreshUserToken.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || 'Échec du refresh token';
        // Déconnecter en cas d'échec du refresh
        state.user = null;
        state.accessToken = null;
        state.refreshToken = null;
        state.isAuthenticated = false;
      })

      // =======================================================
      // GET CURRENT USER
      // =======================================================
      .addCase(getCurrentUser.pending, (state) => {
        state.loading = true;
      })
      .addCase(getCurrentUser.fulfilled, (state, action) => {
        state.loading = false;
        state.user = action.payload;
        state.isAuthenticated = true;
        state.error = null;
      })
      .addCase(getCurrentUser.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || 'Impossible de récupérer les informations utilisateur';
      })

      // =======================================================
      // VALIDATE TOKEN
      // =======================================================
      .addCase(validateToken.fulfilled, (state, action) => {
        if (!action.payload) {
          // Token invalide, déconnecter
          state.user = null;
          state.accessToken = null;
          state.refreshToken = null;
          state.isAuthenticated = false;
        }
      })
      .addCase(validateToken.rejected, (state) => {
        // Token invalide, déconnecter
        state.user = null;
        state.accessToken = null;
        state.refreshToken = null;
        state.isAuthenticated = false;
      });
  },
});

// =============================================================================
// SÉLECTEURS
// =============================================================================

/**
 * Sélecteur pour l'utilisateur connecté
 */
export const selectCurrentUser = (state: { auth: AuthState }) => state.auth.user;

/**
 * Sélecteur pour le statut d'authentification
 */
export const selectIsAuthenticated = (state: { auth: AuthState }) => state.auth.isAuthenticated;

/**
 * Sélecteur pour le rôle de l'utilisateur
 */
export const selectUserRole = (state: { auth: AuthState }) => state.auth.user?.role;

/**
 * Sélecteur pour vérifier si l'utilisateur a un rôle spécifique
 */
export const selectHasRole = (requiredRole: string) => (state: { auth: AuthState }) => {
  const userRole = state.auth.user?.role;
  if (!userRole) return false;

  const roleHierarchy: Record<string, number> = {
    'VISITEUR': 1,
    'EDITEUR': 2,
    'ADMINISTRATEUR': 3,
  };

  const userLevel = roleHierarchy[userRole] || 0;
  const requiredLevel = roleHierarchy[requiredRole] || 0;

  return userLevel >= requiredLevel;
};

/**
 * Sélecteur pour les erreurs d'authentification
 */
export const selectAuthError = (state: { auth: AuthState }) => state.auth.error;

/**
 * Sélecteur pour le loading d'authentification
 */
export const selectAuthLoading = (state: { auth: AuthState }) => state.auth.loading;

/**
 * Sélecteur pour les tokens
 */
export const selectTokens = (state: { auth: AuthState }) => ({
  accessToken: state.auth.accessToken,
  refreshToken: state.auth.refreshToken,
});

// =============================================================================
// EXPORTS
// =============================================================================

export const {
  clearError,
  initializeAuth,
  forceLogout,
  updateUser,
  setAuthenticated,
} = authSlice.actions;

export default authSlice.reducer; 