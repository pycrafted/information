import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import Header from './components/Header';
import HomePage from './pages/HomePage';
import CategoryPage from './pages/CategoryPage';
import LoginPage from './pages/LoginPage';
import EditorPage from './pages/EditorPage';
import AdminPage from './pages/AdminPage';
import ProtectedRoute from './components/ProtectedRoute';
import './App.css';

function App() {
  return (
    <Router>
      <AuthProvider>
        <div className="App">
          {/* Header avec navigation */}
          <Header />
          
          {/* Contenu principal avec routes */}
          <main>
            <Routes>
              {/* Page d'accueil */}
              <Route path="/" element={<HomePage />} />
              
              {/* Pages de catégories */}
              <Route path="/category/:slug" element={<CategoryPage />} />
              
              {/* Page de connexion */}
              <Route path="/login" element={<LoginPage />} />
              
              {/* Routes protégées (exemples pour plus tard) */}
              <Route path="/profile" element={
                <ProtectedRoute>
                  <div style={{ padding: '40px', textAlign: 'center' }}>
                    <h1>👤 Profil Utilisateur</h1>
                    <p>Page à implémenter dans l'étape 6</p>
                  </div>
                </ProtectedRoute>
              } />
              
              <Route path="/editor" element={
                <ProtectedRoute requiredRole="EDITEUR">
                  <EditorPage />
                </ProtectedRoute>
              } />
              
              <Route path="/admin" element={
                <ProtectedRoute requiredRole="ADMINISTRATEUR">
                  <AdminPage />
                </ProtectedRoute>
              } />
              
              {/* Route 404 */}
              <Route path="*" element={
                <div style={{ 
                  textAlign: 'center', 
                  padding: '50px',
                  maxWidth: '600px',
                  margin: '0 auto'
                }}>
                  <div style={{ fontSize: '5em', marginBottom: '20px' }}>🤔</div>
                  <h1 style={{ color: '#666' }}>Page non trouvée</h1>
                  <p style={{ color: '#999', marginBottom: '30px' }}>
                    La page que vous cherchez n'existe pas.
                  </p>
                  <a 
                    href="/"
                    style={{
                      display: 'inline-block',
                      padding: '12px 24px',
                      backgroundColor: '#007bff',
                      color: 'white',
                      textDecoration: 'none',
                      borderRadius: '4px'
                    }}
                  >
                    🏠 Retour à l'accueil
                  </a>
                </div>
              } />
            </Routes>
          </main>
        </div>
      </AuthProvider>
    </Router>
  );
}

export default App;
