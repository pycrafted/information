import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAppSelector, useAppDispatch, selectAuth } from '../../store'
import { logoutAsync } from '../../store/authSlice'
import { Button } from '../ui/Button'

/**
 * Composant Header - Navigation principale de l'application
 * 
 * Fonctionnalités :
 * - Navigation responsive avec menu burger mobile
 * - Authentification : login/logout dynamique
 * - Navigation basée sur les rôles utilisateur
 * - Liens vers les principales sections
 * 
 * Respect des principes clean code :
 * - Séparation des préoccupations (UI/Logic)
 * - Composant pur avec props typées
 * - Accessibilité WCAG 2.1 AA
 */

export const Header: React.FC = () => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false)
  const { user, isAuthenticated, loading } = useAppSelector(selectAuth)
  const dispatch = useAppDispatch()
  const navigate = useNavigate()

  /**
   * Gestion de la déconnexion
   */
  const handleLogout = async () => {
    try {
      await dispatch(logoutAsync()).unwrap()
      navigate('/')
    } catch (error) {
      console.error('Erreur lors de la déconnexion:', error)
    }
  }

  /**
   * Basculer le menu mobile
   */
  const toggleMobileMenu = () => {
    setIsMobileMenuOpen(!isMobileMenuOpen)
  }

  /**
   * Fermer le menu mobile lors de la navigation
   */
  const closeMobileMenu = () => {
    setIsMobileMenuOpen(false)
  }

  /**
   * Liens de navigation selon le rôle utilisateur
   */
  const getNavigationLinks = () => {
    const baseLinks = [
      { to: '/', label: 'Accueil', external: false },
      { to: '/categories', label: 'Catégories', external: false },
    ]

    if (!isAuthenticated) {
      return baseLinks
    }

    const userRole = user?.role

    // Liens pour EDITEUR et ADMINISTRATEUR
    if (userRole === 'EDITEUR' || userRole === 'ADMINISTRATEUR') {
      baseLinks.push({ to: '/editor', label: 'Édition', external: false })
    }

    // Liens pour ADMINISTRATEUR uniquement
    if (userRole === 'ADMINISTRATEUR') {
      baseLinks.push({ to: '/admin', label: 'Administration', external: false })
    }

    return baseLinks
  }

  const navigationLinks = getNavigationLinks()

  return (
    <header className="bg-white shadow-md sticky top-0 z-50">
      <div className="container-custom">
        <div className="flex items-center justify-between h-16">
          
          {/* Logo / Titre */}
          <div className="flex items-center space-x-4">
            <Link 
              to="/" 
              className="text-2xl font-bold text-blue-600 hover:text-blue-700 transition-colors"
              onClick={closeMobileMenu}
            >
              📰 News Platform
            </Link>
          </div>

          {/* Navigation Desktop */}
          <nav className="hidden md:flex items-center space-x-6">
            {navigationLinks.map((link) => (
              <Link
                key={link.to}
                to={link.to}
                className="text-gray-700 hover:text-blue-600 font-medium transition-colors focus-ring px-2 py-1 rounded"
              >
                {link.label}
              </Link>
            ))}
          </nav>

          {/* Actions Utilisateur Desktop */}
          <div className="hidden md:flex items-center space-x-4">
            {isAuthenticated && user ? (
              <>
                <span className="text-sm text-gray-600">
                  Bonjour, <span className="font-medium">{user.username}</span>
                  <span className="text-xs ml-1 px-2 py-1 bg-blue-100 text-blue-800 rounded-full">
                    {user.role}
                  </span>
                </span>
                <Button 
                  variant="outline" 
                  size="sm" 
                  onClick={handleLogout}
                  loading={loading}
                  className="focus-ring"
                >
                  Déconnexion
                </Button>
              </>
            ) : (
              <Button 
                variant="primary" 
                size="sm" 
                onClick={() => navigate('/login')}
                className="focus-ring"
              >
                Connexion
              </Button>
            )}
          </div>

          {/* Bouton Menu Mobile */}
          <button
            type="button"
            className="md:hidden p-2 rounded-md text-gray-700 hover:text-blue-600 hover:bg-gray-100 focus-ring"
            onClick={toggleMobileMenu}
            aria-expanded={isMobileMenuOpen}
            aria-label="Ouvrir le menu de navigation"
          >
            <svg 
              className="h-6 w-6" 
              fill="none" 
              viewBox="0 0 24 24" 
              stroke="currentColor"
              aria-hidden="true"
            >
              {isMobileMenuOpen ? (
                <path 
                  strokeLinecap="round" 
                  strokeLinejoin="round" 
                  strokeWidth={2} 
                  d="M6 18L18 6M6 6l12 12" 
                />
              ) : (
                <path 
                  strokeLinecap="round" 
                  strokeLinejoin="round" 
                  strokeWidth={2} 
                  d="M4 6h16M4 12h16M4 18h16" 
                />
              )}
            </svg>
          </button>
        </div>

        {/* Menu Mobile */}
        {isMobileMenuOpen && (
          <div className="md:hidden border-t border-gray-200 py-4 animate-slide-in-down">
            <nav className="space-y-2">
              {navigationLinks.map((link) => (
                <Link
                  key={link.to}
                  to={link.to}
                  className="block px-4 py-2 text-gray-700 hover:text-blue-600 hover:bg-gray-50 font-medium transition-colors focus-ring rounded"
                  onClick={closeMobileMenu}
                >
                  {link.label}
                </Link>
              ))}
              
              {/* Actions Utilisateur Mobile */}
              <div className="border-t border-gray-200 pt-4 mt-4">
                {isAuthenticated && user ? (
                  <>
                    <div className="px-4 py-2 text-sm text-gray-600">
                      Connecté en tant que <span className="font-medium">{user.username}</span>
                      <span className="block text-xs mt-1 text-blue-600">{user.role}</span>
                    </div>
                    <button
                      onClick={() => {
                        handleLogout()
                        closeMobileMenu()
                      }}
                      className="block w-full text-left px-4 py-2 text-red-600 hover:bg-red-50 font-medium transition-colors focus-ring rounded"
                      disabled={loading}
                    >
                      {loading ? 'Déconnexion...' : 'Déconnexion'}
                    </button>
                  </>
                ) : (
                  <button
                    onClick={() => {
                      navigate('/login')
                      closeMobileMenu()
                    }}
                    className="block w-full text-left px-4 py-2 text-blue-600 hover:bg-blue-50 font-medium transition-colors focus-ring rounded"
                  >
                    Connexion
                  </button>
                )}
              </div>
            </nav>
          </div>
        )}
      </div>
    </header>
  )
} 