import React from 'react'

/**
 * Composants de chargement pour l'application News Platform
 * 
 * Variantes disponibles :
 * - Spinner simple
 * - Skeleton pour les articles
 * - Loading page complète
 * - Loading inline
 * 
 * Respect des principes clean code :
 * - Composants réutilisables
 * - Props typées avec variants
 * - Accessibilité (aria-label)
 * - Animations CSS optimisées
 */

// ================================
// TYPES
// ================================

export interface LoadingSpinnerProps {
  size?: 'sm' | 'md' | 'lg' | 'xl'
  color?: 'primary' | 'secondary' | 'white'
  className?: string
}

export interface LoadingSkeletonProps {
  lines?: number
  className?: string
  avatar?: boolean
  title?: boolean
}

export interface LoadingPageProps {
  message?: string
}

// ================================
// SPINNER DE CHARGEMENT
// ================================

export const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  size = 'md',
  color = 'primary',
  className = ''
}) => {
  const sizeClasses = {
    sm: 'h-4 w-4',
    md: 'h-6 w-6',
    lg: 'h-8 w-8',
    xl: 'h-12 w-12'
  }

  const colorClasses = {
    primary: 'text-blue-600',
    secondary: 'text-gray-600',
    white: 'text-white'
  }

  return (
    <div
      className={`animate-spin ${sizeClasses[size]} ${colorClasses[color]} ${className}`}
      role="status"
      aria-label="Chargement en cours"
    >
      <svg 
        fill="none" 
        viewBox="0 0 24 24" 
        className="w-full h-full"
        aria-hidden="true"
      >
        <circle
          className="opacity-25"
          cx="12"
          cy="12"
          r="10"
          stroke="currentColor"
          strokeWidth="4"
        />
        <path
          className="opacity-75"
          fill="currentColor"
          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
        />
      </svg>
    </div>
  )
}

// ================================
// SKELETON LOADER
// ================================

export const LoadingSkeleton: React.FC<LoadingSkeletonProps> = ({
  lines = 3,
  className = '',
  avatar = false,
  title = false
}) => {
  return (
    <div className={`animate-pulse ${className}`} role="status" aria-label="Chargement du contenu">
      <div className="space-y-3">
        {/* Avatar */}
        {avatar && (
          <div className="flex items-center space-x-3">
            <div className="h-10 w-10 bg-gray-300 rounded-full" />
            <div className="space-y-2">
              <div className="h-4 bg-gray-300 rounded w-20" />
              <div className="h-3 bg-gray-200 rounded w-16" />
            </div>
          </div>
        )}

        {/* Titre */}
        {title && (
          <div className="space-y-2">
            <div className="h-6 bg-gray-300 rounded w-3/4" />
            <div className="h-4 bg-gray-200 rounded w-1/2" />
          </div>
        )}

        {/* Lignes de contenu */}
        <div className="space-y-2">
          {Array.from({ length: lines }).map((_, index) => (
            <div
              key={index}
              className={`h-4 bg-gray-200 rounded ${
                index === lines - 1 ? 'w-2/3' : 'w-full'
              }`}
            />
          ))}
        </div>
      </div>
    </div>
  )
}

// ================================
// LOADING ARTICLE CARD
// ================================

export const LoadingArticleCard: React.FC = () => {
  return (
    <div className="bg-white rounded-lg shadow-md overflow-hidden animate-pulse">
      {/* Image placeholder */}
      <div className="h-48 bg-gray-300" />
      
      {/* Contenu */}
      <div className="p-6 space-y-4">
        {/* Catégorie */}
        <div className="h-4 bg-gray-200 rounded w-24" />
        
        {/* Titre */}
        <div className="space-y-2">
          <div className="h-6 bg-gray-300 rounded w-full" />
          <div className="h-6 bg-gray-300 rounded w-3/4" />
        </div>
        
        {/* Description */}
        <div className="space-y-2">
          <div className="h-4 bg-gray-200 rounded w-full" />
          <div className="h-4 bg-gray-200 rounded w-full" />
          <div className="h-4 bg-gray-200 rounded w-2/3" />
        </div>
        
        {/* Métadonnées */}
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <div className="h-8 w-8 bg-gray-300 rounded-full" />
            <div className="h-4 bg-gray-200 rounded w-20" />
          </div>
          <div className="h-4 bg-gray-200 rounded w-16" />
        </div>
      </div>
    </div>
  )
}

// ================================
// LOADING PAGE COMPLÈTE
// ================================

export const LoadingPage: React.FC<LoadingPageProps> = ({
  message = 'Chargement en cours...'
}) => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="text-center space-y-4">
        <LoadingSpinner size="xl" />
        <div className="space-y-2">
          <h2 className="text-xl font-semibold text-gray-900">
            {message}
          </h2>
          <p className="text-gray-600">
            Veuillez patienter pendant que nous chargeons le contenu.
          </p>
        </div>
      </div>
    </div>
  )
}

// ================================
// LOADING INLINE
// ================================

export const LoadingInline: React.FC<{ message?: string; className?: string }> = ({
  message = 'Chargement...',
  className = ''
}) => {
  return (
    <div className={`flex items-center space-x-2 ${className}`}>
      <LoadingSpinner size="sm" />
      <span className="text-sm text-gray-600">{message}</span>
    </div>
  )
}

// ================================
// LOADING BOUTON
// ================================

export const LoadingButton: React.FC<{ 
  message?: string
  size?: 'sm' | 'md' | 'lg'
  className?: string 
}> = ({
  message = 'Chargement...',
  size = 'md',
  className = ''
}) => {
  const sizeClasses = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-base',
    lg: 'px-6 py-3 text-lg'
  }

  return (
    <button
      disabled
      className={`
        inline-flex items-center space-x-2 bg-blue-600 text-white rounded-md font-medium
        cursor-not-allowed opacity-75 transition-opacity
        ${sizeClasses[size]} ${className}
      `}
    >
      <LoadingSpinner size="sm" color="white" />
      <span>{message}</span>
    </button>
  )
}

// ================================
// EXPORTS
// ================================

export default {
  Spinner: LoadingSpinner,
  Skeleton: LoadingSkeleton,
  ArticleCard: LoadingArticleCard,
  Page: LoadingPage,
  Inline: LoadingInline,
  Button: LoadingButton
} 