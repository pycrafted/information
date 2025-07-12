import React from 'react';

// =============================================================================
// TYPES ET INTERFACES
// =============================================================================

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'default' | 'destructive' | 'outline' | 'secondary' | 'ghost' | 'link';
  size?: 'default' | 'sm' | 'lg' | 'icon';
  isLoading?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
  asChild?: boolean;
}

// =============================================================================
// STYLES ET VARIANTS
// =============================================================================

const getButtonClasses = (variant: string, size: string, isLoading: boolean, disabled: boolean) => {
  const baseClasses = 'inline-flex items-center justify-center rounded-lg font-medium transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50';
  
  // Variants
  const variantClasses = {
    default: 'bg-primary text-primary-foreground hover:bg-primary/90 shadow-sm hover:shadow-md',
    destructive: 'bg-red-500 text-white hover:bg-red-600 shadow-sm hover:shadow-md',
    outline: 'border border-input bg-background hover:bg-accent hover:text-accent-foreground shadow-sm',
    secondary: 'bg-secondary text-secondary-foreground hover:bg-secondary/80 shadow-sm',
    ghost: 'hover:bg-accent hover:text-accent-foreground',
    link: 'text-primary underline-offset-4 hover:underline p-0',
  };
  
  // Sizes
  const sizeClasses = {
    default: 'h-10 px-4 py-2 text-sm',
    sm: 'h-9 rounded-md px-3 text-sm',
    lg: 'h-11 rounded-md px-8 text-base',
    icon: 'h-10 w-10',
  };
  
  // Loading state
  const loadingClasses = isLoading ? 'cursor-not-allowed opacity-70' : '';
  
  return `${baseClasses} ${variantClasses[variant as keyof typeof variantClasses]} ${sizeClasses[size as keyof typeof sizeClasses]} ${loadingClasses}`.trim();
};

// =============================================================================
// COMPOSANT LOADING SPINNER
// =============================================================================

const LoadingSpinner: React.FC<{ size?: 'sm' | 'md' }> = ({ size = 'sm' }) => {
  const sizeClass = size === 'sm' ? 'h-4 w-4' : 'h-5 w-5';
  
  return (
    <svg 
      className={`animate-spin ${sizeClass}`} 
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

// =============================================================================
// COMPOSANT BUTTON PRINCIPAL
// =============================================================================

/**
 * Composant Button professionnel avec variants, états et animations
 * Suit le design system de l'application avec Tailwind CSS
 */
const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ 
    className = '', 
    variant = 'default', 
    size = 'default', 
    isLoading = false,
    leftIcon,
    rightIcon,
    children, 
    disabled,
    ...props 
  }, ref) => {
    
    const buttonClasses = getButtonClasses(variant, size, isLoading, disabled || isLoading);
    const finalClasses = `${buttonClasses} ${className}`.trim();
    
    return (
      <button
        className={finalClasses}
        ref={ref}
        disabled={disabled || isLoading}
        {...props}
      >
        {/* Loading Spinner */}
        {isLoading && (
          <LoadingSpinner size={size === 'lg' ? 'md' : 'sm'} />
        )}
        
        {/* Left Icon */}
        {!isLoading && leftIcon && (
          <span className="mr-2 flex-shrink-0">
            {leftIcon}
          </span>
        )}
        
        {/* Button Content */}
        {!isLoading && children && (
          <span className={leftIcon || rightIcon ? 'flex-1' : ''}>
            {children}
          </span>
        )}
        
        {/* Loading Text */}
        {isLoading && children && (
          <span className="ml-2">
            {typeof children === 'string' ? 'Chargement...' : children}
          </span>
        )}
        
        {/* Right Icon */}
        {!isLoading && rightIcon && (
          <span className="ml-2 flex-shrink-0">
            {rightIcon}
          </span>
        )}
      </button>
    );
  }
);

Button.displayName = 'Button';

// =============================================================================
// EXPORTS
// =============================================================================

export default Button; 