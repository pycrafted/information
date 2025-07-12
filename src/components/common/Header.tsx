import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAppSelector, useAppDispatch } from '../../store/index';
import { selectIsAuthenticated, selectCurrentUser, logoutUser } from '../../store/authSlice';
import Button from '../ui/Button';

// =============================================================================
// COMPOSANT HEADER PRINCIPAL
// =============================================================================

/**
 * Header global de l'application avec navigation responsive
 * Gère l'authentification et les menus par rôle
 */
const Header: React.FC = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  
  const isAuthenticated = useAppSelector(selectIsAuthenticated);
  const currentUser = useAppSelector(selectCurrentUser);
  
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);

  // Navigation principale
  const mainNavigation = [
    { name: 'Accueil', href: '/', current: location.pathname === '/' },
    { name: 'Catégories', href: '/categories', current: location.pathname === '/categories' },
  ];

  // Navigation pour utilisateurs connectés
  const userNavigation = currentUser ? [
    ...(currentUser.role === 'EDITEUR' || currentUser.role === 'ADMINISTRATEUR' 
      ? [{ name: 'Espace Éditeur', href: '/editor' }] 
      : []
    ),
    ...(currentUser.role === 'ADMINISTRATEUR' 
      ? [{ name: 'Administration', href: '/admin' }] 
      : []
    ),
  ] : [];

  // Gestion de la déconnexion
  const handleLogout = async () => {
    try {
      await dispatch(logoutUser()).unwrap();
      setIsUserMenuOpen(false);
      navigate('/');
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  return (
    <header className="bg-white/95 backdrop-blur-sm border-b border-gray-200 sticky top-0 z-50">
      <div className="container mx-auto px-4">
        <div className="flex justify-between items-center h-16">
          
          {/* =================================== */}
          {/* LOGO ET MARQUE */}
          {/* =================================== */}
          <div className="flex items-center">
            <Link to="/" className="flex items-center space-x-3 group">
              {/* Logo Icon */}
              <div className="flex-shrink-0">
                <div className="w-8 h-8 bg-gradient-to-br from-blue-500 to-blue-600 rounded-lg flex items-center justify-center transform group-hover:scale-110 transition-transform duration-200">
                  <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z" />
                  </svg>
                </div>
              </div>
              
              {/* Brand Name */}
              <div className="hidden sm:block">
                <h1 className="text-xl font-bold bg-gradient-to-r from-blue-600 to-blue-800 bg-clip-text text-transparent">
                  News Platform
                </h1>
                <p className="text-xs text-gray-500 -mt-1">Actualités modernes</p>
              </div>
            </Link>
          </div>

          {/* =================================== */}
          {/* NAVIGATION DESKTOP */}
          {/* =================================== */}
          <nav className="hidden md:flex items-center space-x-1">
            {mainNavigation.map((item) => (
              <Link
                key={item.name}
                to={item.href}
                className={`px-4 py-2 rounded-lg text-sm font-medium transition-all relative ${
                  item.current
                    ? 'text-blue-600 bg-blue-50'
                    : 'text-gray-600 hover:text-blue-600 hover:bg-gray-50'
                }`}
              >
                {item.name}
                {item.current && (
                  <div className="absolute inset-0 bg-blue-50 rounded-lg -z-10" />
                )}
              </Link>
            ))}
          </nav>

          {/* =================================== */}
          {/* ACTIONS UTILISATEUR */}
          {/* =================================== */}
          <div className="flex items-center space-x-4">
            
            {/* Navigation utilisateur connecté (Desktop) */}
            {isAuthenticated && userNavigation.length > 0 && (
              <nav className="hidden md:flex items-center space-x-2">
                {userNavigation.map((item) => (
                  <Link
                    key={item.name}
                    to={item.href}
                    className="px-3 py-1 text-sm text-gray-600 hover:text-blue-600 hover:bg-gray-50 rounded-md transition-colors"
                  >
                    {item.name}
                  </Link>
                ))}
              </nav>
            )}

            {/* Menu utilisateur ou bouton de connexion */}
            {isAuthenticated && currentUser ? (
              <div className="relative">
                <button
                  onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
                  className="flex items-center space-x-2 p-2 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  {/* Avatar */}
                  <div className="w-8 h-8 bg-gradient-to-br from-blue-500 to-blue-600 rounded-full flex items-center justify-center">
                    <span className="text-sm font-medium text-white">
                      {currentUser.username.charAt(0).toUpperCase()}
                    </span>
                  </div>
                  
                  {/* Nom utilisateur (Desktop) */}
                  <div className="hidden md:block text-left">
                    <p className="text-sm font-medium text-gray-900">{currentUser.username}</p>
                    <p className="text-xs text-gray-500">{currentUser.role}</p>
                  </div>
                  
                  {/* Chevron */}
                  <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7" />
                  </svg>
                </button>

                {/* Menu déroulant utilisateur */}
                {isUserMenuOpen && (
                  <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-1 z-50">
                    <div className="px-4 py-2 border-b border-gray-100">
                      <p className="text-sm font-medium text-gray-900">{currentUser.username}</p>
                      <p className="text-xs text-gray-500">{currentUser.email}</p>
                    </div>
                    
                    {/* Menu items */}
                    <Link
                      to="/profile"
                      className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
                      onClick={() => setIsUserMenuOpen(false)}
                    >
                      Mon profil
                    </Link>
                    
                    <button
                      onClick={handleLogout}
                      className="block w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50"
                    >
                      Se déconnecter
                    </button>
                  </div>
                )}
              </div>
            ) : (
              /* Bouton de connexion */
              <div className="flex items-center space-x-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => navigate('/login')}
                >
                  Connexion
                </Button>
              </div>
            )}

            {/* =================================== */}
            {/* MENU MOBILE TOGGLE */}
            {/* =================================== */}
            <button
              className="md:hidden p-2 rounded-md text-gray-600 hover:text-gray-900 hover:bg-gray-50"
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            >
              <span className="sr-only">Ouvrir le menu principal</span>
              {isMobileMenuOpen ? (
                <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              ) : (
                <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 6h16M4 12h16M4 18h16" />
                </svg>
              )}
            </button>
          </div>
        </div>

        {/* =================================== */}
        {/* MENU MOBILE */}
        {/* =================================== */}
        {isMobileMenuOpen && (
          <div className="md:hidden border-t border-gray-200 py-4">
            <div className="space-y-2">
              {/* Navigation principale mobile */}
              {mainNavigation.map((item) => (
                <Link
                  key={item.name}
                  to={item.href}
                  className={`block px-4 py-2 text-base font-medium rounded-lg ${
                    item.current
                      ? 'text-blue-600 bg-blue-50'
                      : 'text-gray-600 hover:text-blue-600 hover:bg-gray-50'
                  }`}
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  {item.name}
                </Link>
              ))}
              
              {/* Navigation utilisateur mobile */}
              {isAuthenticated && userNavigation.map((item) => (
                <Link
                  key={item.name}
                  to={item.href}
                  className="block px-4 py-2 text-base font-medium text-gray-600 hover:text-blue-600 hover:bg-gray-50 rounded-lg"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  {item.name}
                </Link>
              ))}
              
              {/* Actions mobile */}
              {!isAuthenticated && (
                <div className="px-4 pt-4">
                  <Button
                    variant="default"
                    size="sm"
                    className="w-full"
                    onClick={() => {
                      navigate('/login');
                      setIsMobileMenuOpen(false);
                    }}
                  >
                    Connexion
                  </Button>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
      
      {/* Overlay pour fermer les menus */}
      {(isMobileMenuOpen || isUserMenuOpen) && (
        <div 
          className="fixed inset-0 z-40" 
          onClick={() => {
            setIsMobileMenuOpen(false);
            setIsUserMenuOpen(false);
          }}
        />
      )}
    </header>
  );
};

export default Header; 