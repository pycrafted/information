import React from 'react'
import { Routes, Route } from 'react-router-dom'

import { Header } from './components/common/Header'
import { Footer } from './components/common/Footer'
import { HomePage } from './pages/HomePage'
import { ArticlePage } from './pages/ArticlePage'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { AdminPage } from './pages/AdminPage'
import { EditorPage } from './pages/EditorPage'
import { CategoriesPage } from './pages/CategoriesPage'
import { CategoryPage } from './pages/CategoryPage'
import { NotFoundPage } from './pages/NotFoundPage'
import { ProtectedRoute } from './components/auth/ProtectedRoute'

/**
 * Composant principal de l'application News Platform
 * 
 * Architecture :
 * - Header : navigation commune à toutes les pages
 * - Main : contenu principal avec routage React Router
 * - Footer : informations communes
 * 
 * Routing :
 * - Routes publiques : accueil, articles, connexion, inscription
 * - Routes protégées : administration, édition
 * - Routes avec paramètres : articles/{id}, catégories/{slug}
 */
const App: React.FC = () => {
  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      <Header />
      
      <main className="flex-1">
        <Routes>
          {/* Routes publiques */}
          <Route path="/" element={<HomePage />} />
          <Route path="/article/:id" element={<ArticlePage />} />
          <Route path="/categories" element={<CategoriesPage />} />
          <Route path="/category/:slug" element={<CategoryPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          
          {/* Routes protégées - Éditeur */}
          <Route 
            path="/editor/*" 
            element={
              <ProtectedRoute allowedRoles={['EDITEUR', 'ADMINISTRATEUR']}>
                <EditorPage />
              </ProtectedRoute>
            } 
          />
          
          {/* Routes protégées - Administrateur */}
          <Route 
            path="/admin/*" 
            element={
              <ProtectedRoute allowedRoles={['ADMINISTRATEUR']}>
                <AdminPage />
              </ProtectedRoute>
            } 
          />
          
          {/* Page 404 */}
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
      
      <Footer />
    </div>
  )
}

export default App 