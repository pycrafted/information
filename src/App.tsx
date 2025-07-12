import React, { useEffect } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from './store/index';
import { initializeAuth, getCurrentUser, selectIsAuthenticated } from './store/authSlice';

// Composants de layout
import Header from './components/common/Header';
import Footer from './components/common/Footer';
import Loading from './components/common/Loading';

// Pages publiques
import HomePage from './pages/HomePage';
import ArticlePage from './pages/ArticlePage';
import CategoryPage from './pages/CategoryPage';
import CategoriesPage from './pages/CategoriesPage';
import LoginPage from './pages/LoginPage';
import NotFoundPage from './pages/NotFoundPage';

// Pages privées
import AdminPage from './pages/AdminPage';
import EditorPage from './pages/EditorPage';

// Composant de protection des routes
import ProtectedRoute from './components/auth/ProtectedRoute';

/**
 * Composant principal de l'application News Platform
 * Gère le routing, l'authentification et la structure globale
 */
function App() {
  const dispatch = useAppDispatch();
  const isAuthenticated = useAppSelector(selectIsAuthenticated);
  const [isInitialized, setIsInitialized] = React.useState(false);

  // Initialisation de l'application
  useEffect(() => {
    const initializeApp = async () => {
      try {
        // Initialiser l'authentification depuis le localStorage
        dispatch(initializeAuth());
        
        // Si on a un token, récupérer les infos utilisateur
        const token = localStorage.getItem('accessToken');
        if (token) {
          await dispatch(getCurrentUser()).unwrap();
        }
      } catch (error) {
        console.warn('Failed to initialize user:', error);
        // L'erreur sera gérée par le store
      } finally {
        setIsInitialized(true);
      }
    };

    initializeApp();
  }, [dispatch]);

  // Afficher un loader pendant l'initialisation
  if (!isInitialized) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <Loading />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background text-foreground">
      {/* Header global */}
      <Header />
      
      {/* Contenu principal */}
      <main id="main-content" className="flex-1">
        <Routes>
          {/* =================================== */}
          {/* ROUTES PUBLIQUES */}
          {/* =================================== */}
          
          {/* Page d'accueil */}
          <Route path="/" element={<HomePage />} />
          
          {/* Page de détail d'un article */}
          <Route path="/article/:id" element={<ArticlePage />} />
          
          {/* Pages des catégories */}
          <Route path="/categories" element={<CategoriesPage />} />
          <Route path="/category/:slug" element={<CategoryPage />} />
          
          {/* Page de connexion */}
          <Route 
            path="/login" 
            element={
              isAuthenticated ? (
                <Navigate to="/" replace />
              ) : (
                <LoginPage />
              )
            } 
          />
          
          {/* =================================== */}
          {/* ROUTES PROTÉGÉES - ÉDITEURS */}
          {/* =================================== */}
          
          {/* Espace éditeur */}
          <Route 
            path="/editor/*" 
            element={
              <ProtectedRoute requiredRole="EDITEUR">
                <EditorPage />
              </ProtectedRoute>
            } 
          />
          
          {/* =================================== */}
          {/* ROUTES PROTÉGÉES - ADMINISTRATEURS */}
          {/* =================================== */}
          
          {/* Panel d'administration */}
          <Route 
            path="/admin/*" 
            element={
              <ProtectedRoute requiredRole="ADMINISTRATEUR">
                <AdminPage />
              </ProtectedRoute>
            } 
          />
          
          {/* =================================== */}
          {/* REDIRECTIONS ET 404 */}
          {/* =================================== */}
          
          {/* Redirection pour les utilisateurs connectés */}
          <Route 
            path="/dashboard" 
            element={
              isAuthenticated ? (
                <Navigate to="/editor" replace />
              ) : (
                <Navigate to="/login" replace />
              )
            } 
          />
          
          {/* Page 404 */}
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
      
      {/* Footer global */}
      <Footer />
    </div>
  );
}

export default App; 