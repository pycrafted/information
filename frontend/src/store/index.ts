import { configureStore } from '@reduxjs/toolkit'
import type { TypedUseSelectorHook } from 'react-redux'
import { useDispatch, useSelector } from 'react-redux'

import authReducer from './authSlice'

/**
 * Store Redux principal pour l'application News Platform
 * 
 * Configuration :
 * - Redux Toolkit pour la configuration simplifiée
 * - DevTools activé en développement
 * - Middleware par défaut (thunk, serializability check, etc.)
 * - Types TypeScript pour les hooks personnalisés
 */
export const store = configureStore({
  reducer: {
    auth: authReducer,
    // Autres slices seront ajoutés ici :
    // articles: articlesReducer,
    // categories: categoriesReducer,
    // users: usersReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        // Ignorer ces actions pour les checks de sérialisation
        ignoredActions: ['persist/PERSIST', 'persist/REHYDRATE'],
      },
    }),
  devTools: import.meta.env.DEV, // DevTools uniquement en développement
})

// Types pour TypeScript
export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch

/**
 * Hooks TypeScript personnalisés pour Redux
 * Utilisez ces hooks au lieu de useDispatch et useSelector standard
 */
export const useAppDispatch: () => AppDispatch = useDispatch
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector

/**
 * Sélecteurs globaux utiles
 */
export const selectAuth = (state: RootState) => state.auth
export const selectCurrentUser = (state: RootState) => state.auth.user
export const selectIsAuthenticated = (state: RootState) => state.auth.isAuthenticated 