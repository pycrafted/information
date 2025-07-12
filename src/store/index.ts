import { configureStore } from '@reduxjs/toolkit';
import authReducer from './authSlice';
import articleReducer from './articleSlice';
import categoryReducer from './categorySlice';
import userReducer from './userSlice';

// =============================================================================
// CONFIGURATION REDUX STORE
// =============================================================================

/**
 * Store Redux configuré avec tous les slices
 * Inclut la persistance locale et les middlewares de développement
 */
export const store = configureStore({
  reducer: {
    auth: authReducer,
    articles: articleReducer,
    categories: categoryReducer,
    users: userReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        // Ignorer les actions non sérialisables pour les dates
        ignoredActions: ['persist/PERSIST', 'persist/REHYDRATE'],
        ignoredActionsPaths: ['meta.arg', 'payload.timestamp'],
        ignoredPaths: ['items.dates'],
      },
    }),
  devTools: true, // Activer Redux DevTools en développement
});

// =============================================================================
// TYPES TYPESCRIPT
// =============================================================================

/**
 * Type pour l'état global du store
 */
export type RootState = ReturnType<typeof store.getState>;

/**
 * Type pour le dispatch du store
 */
export type AppDispatch = typeof store.dispatch;

// =============================================================================
// HOOKS TYPÉS POUR REACT-REDUX
// =============================================================================

import { TypedUseSelectorHook, useDispatch, useSelector } from 'react-redux';

/**
 * Hook useDispatch typé
 */
export const useAppDispatch = () => useDispatch<AppDispatch>();

/**
 * Hook useSelector typé
 */
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;

// =============================================================================
// ACTIONS ET SÉLECTEURS GLOBAUX
// =============================================================================

/**
 * Sélecteur pour vérifier si l'application est en cours de chargement
 */
export const selectIsGlobalLoading = (state: RootState): boolean => {
  return state.auth.loading || 
         state.articles.loading || 
         state.categories.loading || 
         state.users.loading;
};

/**
 * Sélecteur pour récupérer toutes les erreurs actives
 */
export const selectGlobalErrors = (state: RootState): string[] => {
  const errors: string[] = [];
  
  if (state.auth.error) errors.push(state.auth.error);
  if (state.articles.error) errors.push(state.articles.error);
  if (state.categories.error) errors.push(state.categories.error);
  if (state.users.error) errors.push(state.users.error);
  
  return errors;
};

/**
 * Action pour nettoyer toutes les erreurs
 */
export const clearAllErrors = () => (dispatch: AppDispatch) => {
  // Import dynamique pour éviter les dépendances circulaires
  import('./authSlice').then(({ clearError: clearAuthError }) => {
    dispatch(clearAuthError());
  });
  import('./articleSlice').then(({ clearError: clearArticleError }) => {
    dispatch(clearArticleError());
  });
  import('./categorySlice').then(({ clearError: clearCategoryError }) => {
    dispatch(clearCategoryError());
  });
  import('./userSlice').then(({ clearError: clearUserError }) => {
    dispatch(clearUserError());
  });
};

// =============================================================================
// EXPORT DEFAULT
// =============================================================================

export default store; 