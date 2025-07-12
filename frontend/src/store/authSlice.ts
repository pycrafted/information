import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit'
import type { AuthState, User, LoginRequest, AuthResponse } from '../types/api'
import { tokenManager, apiRequest, apiClient } from '../services/api'

/**
 * Slice Redux pour la gestion de l'authentification
 * 
 * Fonctionnalités :
 * - Login/logout avec JWT
 * - Gestion des tokens (access + refresh)
 * - Persistence du state dans localStorage
 * - Vérification automatique de la session
 * - Types TypeScript complets
 */

// État initial
const initialState: AuthState = {
  user: null,
  accessToken: tokenManager.getAccessToken(),
  refreshToken: tokenManager.getRefreshToken(),
  isAuthenticated: !!tokenManager.getAccessToken(),
  loading: false,
  error: null,
}

/**
 * Thunk asynchrone pour la connexion
 */
export const loginAsync = createAsyncThunk(
  'auth/login',
  async (credentials: LoginRequest, { rejectWithValue }) => {
    try {
      const response = await apiRequest<AuthResponse>(() =>
        apiClient.post<AuthResponse>('/auth/login', credentials)
      )
      
      // Sauvegarder les tokens
      tokenManager.setTokens(response.accessToken, response.refreshToken)
      
      return response
    } catch (error: any) {
      return rejectWithValue(error.message || 'Erreur de connexion')
    }
  }
)

/**
 * Thunk asynchrone pour la déconnexion
 */
export const logoutAsync = createAsyncThunk(
  'auth/logout',
  async (_, { rejectWithValue }) => {
    try {
      const refreshToken = tokenManager.getRefreshToken()
      
      if (refreshToken) {
        // Appeler l'API de logout si token disponible
        await apiRequest(() =>
          apiClient.post<void>('/auth/logout', { refreshToken })
        )
      }
      
      // Nettoyer les tokens localement
      tokenManager.clearTokens()
      
      return true
    } catch (error: any) {
      // Même en cas d'erreur, on nettoie localement
      tokenManager.clearTokens()
      return rejectWithValue(error.message || 'Erreur de déconnexion')
    }
  }
)

/**
 * Thunk pour vérifier la validité du token au démarrage
 */
export const verifyTokenAsync = createAsyncThunk(
  'auth/verifyToken',
  async (_, { rejectWithValue }) => {
    try {
      const token = tokenManager.getAccessToken()
      
      if (!token) {
        throw new Error('Aucun token disponible')
      }
      
      // Décoder le token pour vérifier l'expiration
      const { jwtDecode } = await import('jwt-decode')
      const decoded: any = jwtDecode(token)
      
      // Vérifier si le token est expiré
      const currentTime = Date.now() / 1000
      if (decoded.exp < currentTime) {
        throw new Error('Token expiré')
      }
      
      // Récupérer les infos utilisateur si token valide
      const user = await apiRequest<User>(() =>
        apiClient.get<User>('/auth/me')
      )
      
      return user
    } catch (error: any) {
      // Token invalide, nettoyer
      tokenManager.clearTokens()
      return rejectWithValue(error.message || 'Token invalide')
    }
  }
)

/**
 * Slice d'authentification
 */
const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    // Action pour nettoyer les erreurs
    clearError: (state) => {
      state.error = null
    },
    
    // Action pour mise à jour du token (utilisée par l'intercepteur)
    updateTokens: (state, action: PayloadAction<{ accessToken: string; refreshToken: string }>) => {
      state.accessToken = action.payload.accessToken
      state.refreshToken = action.payload.refreshToken
      state.isAuthenticated = true
    },
    
    // Action pour mise à jour des infos utilisateur
    updateUser: (state, action: PayloadAction<User>) => {
      state.user = action.payload
    },
  },
  extraReducers: (builder) => {
    // Login
    builder
      .addCase(loginAsync.pending, (state) => {
        state.loading = true
        state.error = null
      })
      .addCase(loginAsync.fulfilled, (state, action) => {
        state.loading = false
        state.user = action.payload.user
        state.accessToken = action.payload.accessToken
        state.refreshToken = action.payload.refreshToken
        state.isAuthenticated = true
        state.error = null
      })
      .addCase(loginAsync.rejected, (state, action) => {
        state.loading = false
        state.user = null
        state.accessToken = null
        state.refreshToken = null
        state.isAuthenticated = false
        state.error = action.payload as string
      })
    
    // Logout
    builder
      .addCase(logoutAsync.pending, (state) => {
        state.loading = true
      })
      .addCase(logoutAsync.fulfilled, (state) => {
        state.loading = false
        state.user = null
        state.accessToken = null
        state.refreshToken = null
        state.isAuthenticated = false
        state.error = null
      })
      .addCase(logoutAsync.rejected, (state, action) => {
        state.loading = false
        // Même en cas d'erreur, on déconnecte localement
        state.user = null
        state.accessToken = null
        state.refreshToken = null
        state.isAuthenticated = false
        state.error = action.payload as string
      })
    
    // Verify token
    builder
      .addCase(verifyTokenAsync.pending, (state) => {
        state.loading = true
      })
      .addCase(verifyTokenAsync.fulfilled, (state, action) => {
        state.loading = false
        state.user = action.payload
        state.isAuthenticated = true
        state.error = null
      })
      .addCase(verifyTokenAsync.rejected, (state, action) => {
        state.loading = false
        state.user = null
        state.accessToken = null
        state.refreshToken = null
        state.isAuthenticated = false
        state.error = action.payload as string
      })
  },
})

// Export des actions
export const { clearError, updateTokens, updateUser } = authSlice.actions

// Export du reducer
export default authSlice.reducer

// Sélecteurs spécifiques
export const selectAuthUser = (state: { auth: AuthState }) => state.auth.user
export const selectAuthLoading = (state: { auth: AuthState }) => state.auth.loading
export const selectAuthError = (state: { auth: AuthState }) => state.auth.error
export const selectIsAuthenticated = (state: { auth: AuthState }) => state.auth.isAuthenticated
export const selectUserRole = (state: { auth: AuthState }) => state.auth.user?.role 