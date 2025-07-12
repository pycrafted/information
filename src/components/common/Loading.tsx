import React from 'react';

// =============================================================================
// TYPES ET INTERFACES
// =============================================================================

interface LoadingProps {
  variant?: 'spinner' | 'dots' | 'pulse' | 'skeleton';
  size?: 'sm' | 'md' | 'lg';
  text?: string;
  className?: string;
}

// =============================================================================
// COMPOSANTS DE LOADING
// =============================================================================

/**
 * Spinner classique animé
 */
const Spinner: React.FC<{ size: string; className?: string }> = ({ size, className = '' }) => {
  const sizeClasses = {
    sm: 'h-4 w-4',
    md: 'h-8 w-8',
    lg: 'h-12 w-12',
  };

  return (
    <svg 
      className={`animate-spin ${sizeClasses[size as keyof typeof sizeClasses]} ${className}`} 
      fill="none" 
      viewBox="0 0 24 24"
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
  );
};

/**
 * Animation de points qui rebondissent
 */
const Dots: React.FC<{ size: string }> = ({ size }) => {
  const dotClasses = {
    sm: 'h-2 w-2',
    md: 'h-3 w-3',
    lg: 'h-4 w-4',
  };

  const dotSize = dotClasses[size as keyof typeof dotClasses];

  return (
    <div className="flex space-x-1">
      <div className={`${dotSize} bg-blue-500 rounded-full animate-bounce`} style={{ animationDelay: '0ms' }} />
      <div className={`${dotSize} bg-blue-500 rounded-full animate-bounce`} style={{ animationDelay: '150ms' }} />
      <div className={`${dotSize} bg-blue-500 rounded-full animate-bounce`} style={{ animationDelay: '300ms' }} />
    </div>
  );
};

/**
 * Animation de pulsation
 */
const Pulse: React.FC<{ size: string }> = ({ size }) => {
  const sizeClasses = {
    sm: 'h-4 w-4',
    md: 'h-8 w-8',
    lg: 'h-12 w-12',
  };

  return (
    <div className={`${sizeClasses[size as keyof typeof sizeClasses]} bg-blue-500 rounded-full animate-pulse`} />
  );
};

/**
 * Skeleton loader pour du contenu
 */
const Skeleton: React.FC<{ size: string }> = ({ size }) => {
  const heightClasses = {
    sm: 'h-16',
    md: 'h-24',
    lg: 'h-32',
  };

  return (
    <div className="space-y-3 animate-pulse">
      <div className="flex space-x-3">
        <div className="rounded-full bg-gray-200 h-10 w-10" />
        <div className="flex-1 space-y-2">
          <div className="h-4 bg-gray-200 rounded w-3/4" />
          <div className="h-3 bg-gray-200 rounded w-1/2" />
        </div>
      </div>
      <div className={`${heightClasses[size as keyof typeof heightClasses]} bg-gray-200 rounded`} />
      <div className="space-y-2">
        <div className="h-3 bg-gray-200 rounded" />
        <div className="h-3 bg-gray-200 rounded w-5/6" />
        <div className="h-3 bg-gray-200 rounded w-4/6" />
      </div>
    </div>
  );
};

// =============================================================================
// COMPOSANT LOADING PRINCIPAL
// =============================================================================

/**
 * Composant Loading polyvalent avec différents variants et animations
 */
const Loading: React.FC<LoadingProps> = ({ 
  variant = 'spinner', 
  size = 'md', 
  text, 
  className = '' 
}) => {
  const renderLoadingComponent = () => {
    switch (variant) {
      case 'dots':
        return <Dots size={size} />;
      case 'pulse':
        return <Pulse size={size} />;
      case 'skeleton':
        return <Skeleton size={size} />;
      case 'spinner':
      default:
        return <Spinner size={size} className="text-blue-500" />;
    }
  };

  return (
    <div className={`flex flex-col items-center justify-center space-y-3 ${className}`}>
      {/* Composant de loading */}
      <div className="flex items-center justify-center">
        {renderLoadingComponent()}
      </div>
      
      {/* Texte optionnel */}
      {text && (
        <p className="text-sm text-gray-600 text-center animate-pulse">
          {text}
        </p>
      )}
    </div>
  );
};

// =============================================================================
// COMPOSANTS SPÉCIALISÉS
// =============================================================================

/**
 * Loading pour une page complète
 */
export const PageLoading: React.FC<{ text?: string }> = ({ text = 'Chargement...' }) => (
  <div className="min-h-screen flex items-center justify-center bg-background">
    <Loading variant="spinner" size="lg" text={text} />
  </div>
);

/**
 * Loading pour un bouton
 */
export const ButtonLoading: React.FC = () => (
  <Loading variant="spinner" size="sm" className="inline-flex" />
);

/**
 * Loading pour une card/section
 */
export const CardLoading: React.FC = () => (
  <div className="p-6">
    <Loading variant="skeleton" size="md" />
  </div>
);

/**
 * Loading inline pour du texte
 */
export const InlineLoading: React.FC<{ text?: string }> = ({ text = 'Chargement...' }) => (
  <div className="inline-flex items-center space-x-2">
    <Loading variant="dots" size="sm" />
    {text && <span className="text-sm text-gray-600">{text}</span>}
  </div>
);

// =============================================================================
// EXPORTS
// =============================================================================

export default Loading; 