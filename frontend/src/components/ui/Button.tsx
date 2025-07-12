import React from 'react';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  children: React.ReactNode;
}

export const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'md',
  className = '',
  children,
  ...props
}) => {
  const baseClasses = 'inline-flex items-center justify-center font-medium rounded-md transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:pointer-events-none';
  
  const variantClasses = {
    primary: 'bg-blue-600 text-white hover:bg-blue-700 focus:ring-blue-500',
    secondary: 'bg-gray-200 text-gray-900 hover:bg-gray-300 focus:ring-gray-500',
    ghost: 'bg-transparent text-gray-700 hover:bg-gray-100 focus:ring-gray-500',
    danger: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500'
  };
  
  const sizeClasses = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-sm',
    lg: 'px-6 py-3 text-base'
  };
  
  const classes = `${baseClasses} ${variantClasses[variant]} ${sizeClasses[size]} ${className}`;
  
  return (
    <button className={classes} {...props}>
      {children}
    </button>
  );
};

Button.displayName = 'Button'

// ================================
// COMPOSANTS SPÉCIALISÉS
// ================================

/**
 * Bouton avec icône seulement (carré)
 */
export const IconButton = React.forwardRef<HTMLButtonElement, 
  Omit<ButtonProps, 'leftIcon' | 'rightIcon' | 'children'> & { 
    icon: React.ReactNode
    'aria-label': string
  }
>(({ icon, size = 'md', className, ...props }, ref) => {
  const iconSizeClasses = {
    xs: 'h-6 w-6 p-1',
    sm: 'h-7 w-7 p-1.5',
    md: 'h-8 w-8 p-2',
    lg: 'h-9 w-9 p-2',
    xl: 'h-10 w-10 p-2.5'
  }

  const currentSize = size || 'md'

  return (
    <Button
      ref={ref}
      size={size}
      className={`${iconSizeClasses[currentSize]} ${className || ''}`}
      {...props}
    >
      {icon}
    </Button>
  )
})

IconButton.displayName = 'IconButton'

/**
 * Groupe de boutons
 */
export const ButtonGroup: React.FC<{
  children: React.ReactNode
  className?: string
  orientation?: 'horizontal' | 'vertical'
}> = ({ 
  children, 
  className = '', 
  orientation = 'horizontal' 
}) => {
  const orientationClasses = {
    horizontal: 'flex flex-row',
    vertical: 'flex flex-col'
  }

  return (
    <div 
      className={`${orientationClasses[orientation]} ${className}`}
      role="group"
    >
      {children}
    </div>
  )
}

// ================================
// BOUTONS PRÉDÉFINIS
// ================================

export const PrimaryButton: React.FC<Omit<ButtonProps, 'variant'>> = (props) => (
  <Button variant="primary" {...props} />
)

export const SecondaryButton: React.FC<Omit<ButtonProps, 'variant'>> = (props) => (
  <Button variant="secondary" {...props} />
)

export const OutlineButton: React.FC<Omit<ButtonProps, 'variant'>> = (props) => (
  <Button variant="outline" {...props} />
)

export const GhostButton: React.FC<Omit<ButtonProps, 'variant'>> = (props) => (
  <Button variant="ghost" {...props} />
)

export const DestructiveButton: React.FC<Omit<ButtonProps, 'variant'>> = (props) => (
  <Button variant="destructive" {...props} />
)

export const SuccessButton: React.FC<Omit<ButtonProps, 'variant'>> = (props) => (
  <Button variant="success" {...props} />
)

export const WarningButton: React.FC<Omit<ButtonProps, 'variant'>> = (props) => (
  <Button variant="warning" {...props} />
)

export const LinkButton: React.FC<Omit<ButtonProps, 'variant'>> = (props) => (
  <Button variant="link" {...props} />
)

// ================================
// EXPORT DEFAULT
// ================================

export default Button 