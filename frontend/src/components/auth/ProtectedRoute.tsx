import React from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAppSelector, selectAuth } from '../../store'
import { UserRole } from '../../types/api'
import { LoadingPage } from '../common/Loading'

/**
 * Composant ProtectedRoute - Protection des routes par rôles
 * 
 * Fonctionnalités :
 * - Vérification de l'authentification
 * - Contrôle d'accès basé sur les rôles (RBAC)
 * - Redirection automatique vers login
 * - Préservation de l'URL de destination
 * 
 * Respect des principes clean code :
 * - Séparation des préoccupations (auth/routing)
 * - API claire avec props typées
 * - Gestion d'état cohérente
 */

// ================================
// TYPES ET INTERFACES
// ================================

interface ProtectedRouteProps {
  children: React.ReactNode
  allowedRoles?: UserRole[]
  requireAuth?: boolean
  fallback?: React.ComponentType
}

// ================================
// COMPOSANT PRINCIPAL
// ================================

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  allowedRoles = [],
  requireAuth = true,
  fallback: FallbackComponent
}) => {
  const { user, isAuthenticated, loading } = useAppSelector(selectAuth)
  const location = useLocation()

  // État de chargement
  if (loading) {
    return <LoadingPage message="Vérification des permissions..." />
  }

  // Vérification de l'authentification
  if (requireAuth && !isAuthenticated) {
    // Rediriger vers login en préservant l'URL de destination
    return (
      <Navigate 
        to="/login" 
        state={{ from: location.pathname }} 
        replace 
      />
    )
  }

  // Vérification des rôles si spécifiés
  if (allowedRoles.length > 0 && user) {
    const hasRequiredRole = allowedRoles.includes(user.role)
    
    if (!hasRequiredRole) {
      // Utilisateur connecté mais sans les permissions requises
      if (FallbackComponent) {
        return <FallbackComponent />
      }
      
      // Page d'accès refusé par défaut
      return <AccessDeniedPage userRole={user.role} requiredRoles={allowedRoles} />
    }
  }

  // Autorisation accordée
  return <>{children}</>
}

// ================================
// COMPOSANT D'ACCÈS REFUSÉ
// ================================

const AccessDeniedPage: React.FC<{
  userRole?: UserRole
  requiredRoles: UserRole[]
}> = ({ userRole, requiredRoles }) => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="text-center space-y-6 max-w-md mx-auto px-4">
        {/* Icône d'erreur */}
        <div className="text-red-500 text-6xl">🚫</div>
        
        {/* Titre */}
        <div className="space-y-2">
          <h1 className="text-3xl font-bold text-gray-900">
            Accès refusé
          </h1>
          <p className="text-lg text-gray-600">
            Vous n'avez pas les permissions nécessaires pour accéder à cette page.
          </p>
        </div>

        {/* Détails des permissions */}
        <div className="bg-gray-100 rounded-lg p-4 space-y-2">
          <div className="text-sm text-gray-700">
            <strong>Votre rôle :</strong> 
            <span className="ml-1 px-2 py-1 bg-blue-100 text-blue-800 rounded text-xs font-medium">
              {userRole}
            </span>
          </div>
          <div className="text-sm text-gray-700">
            <strong>Rôles requis :</strong>
            <div className="mt-1 space-x-1">
              {requiredRoles.map(role => (
                <span 
                  key={role} 
                  className="inline-block px-2 py-1 bg-green-100 text-green-800 rounded text-xs font-medium"
                >
                  {role}
                </span>
              ))}
            </div>
          </div>
        </div>

        {/* Actions */}
        <div className="space-y-3">
          <button
            onClick={() => window.history.back()}
            className="w-full px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors focus-ring"
          >
            Retour à la page précédente
          </button>
          
          <button
            onClick={() => window.location.href = '/'}
            className="w-full px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 transition-colors focus-ring"
          >
            Retour à l'accueil
          </button>
        </div>

        {/* Informations d'aide */}
        <div className="text-xs text-gray-500 space-y-1">
          <p>Si vous pensez qu'il s'agit d'une erreur, contactez l'administrateur.</p>
          <p>
            <a 
              href="mailto:admin@newsplatform.com" 
              className="text-blue-600 hover:text-blue-700 underline"
            >
              admin@newsplatform.com
            </a>
          </p>
        </div>
      </div>
    </div>
  )
}

// ================================
// COMPOSANTS UTILITAIRES
// ================================

/**
 * HOC pour créer des routes protégées spécifiques
 */
export const createProtectedRoute = (allowedRoles: UserRole[]) => {
  return ({ children }: { children: React.ReactNode }) => (
    <ProtectedRoute allowedRoles={allowedRoles}>
      {children}
    </ProtectedRoute>
  )
}

/**
 * Routes pré-configurées pour les rôles courants
 */
export const AdminRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <ProtectedRoute allowedRoles={['ADMINISTRATEUR']}>
    {children}
  </ProtectedRoute>
)

export const EditorRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <ProtectedRoute allowedRoles={['EDITEUR', 'ADMINISTRATEUR']}>
    {children}
  </ProtectedRoute>
)

export const AuthenticatedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <ProtectedRoute requireAuth={true}>
    {children}
  </ProtectedRoute>
)

// ================================
// HOOKS UTILITAIRES
// ================================

/**
 * Hook pour vérifier les permissions dans les composants
 */
export const usePermissions = () => {
  const { user, isAuthenticated } = useAppSelector(selectAuth)

  const hasRole = (role: UserRole): boolean => {
    return isAuthenticated && user?.role === role
  }

  const hasAnyRole = (roles: UserRole[]): boolean => {
    return isAuthenticated && user ? roles.includes(user.role) : false
  }

  const isAdmin = (): boolean => hasRole('ADMINISTRATEUR')
  const isEditor = (): boolean => hasAnyRole(['EDITEUR', 'ADMINISTRATEUR'])
  const isVisitor = (): boolean => hasRole('VISITEUR')

  return {
    user,
    isAuthenticated,
    hasRole,
    hasAnyRole,
    isAdmin,
    isEditor,
    isVisitor
  }
}

// ================================
// EXPORT DEFAULT
// ================================

export default ProtectedRoute 