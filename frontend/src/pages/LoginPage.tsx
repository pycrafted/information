import React, { useState, useEffect } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAppDispatch, useAppSelector, selectAuth } from '../store'
import { loginAsync, clearError } from '../store/authSlice'
import { Button } from '../components/ui/Button'
import { LoadingInline } from '../components/common/Loading'

/**
 * Page de connexion avec authentification JWT
 * 
 * Fonctionnalités :
 * - Formulaire de connexion avec validation
 * - Intégration Redux pour l'authentification
 * - Redirection après connexion réussie
 * - Gestion d'erreurs en temps réel
 * - Design responsive et accessible
 * 
 * Respect des principes clean code :
 * - Validation côté client
 * - Gestion d'état cohérente
 * - UX optimisée (loading states, messages d'erreur)
 */

interface LocationState {
  from?: string
}

export const LoginPage: React.FC = () => {
  const [credentials, setCredentials] = useState({
    username: '',
    password: ''
  })
  const [showPassword, setShowPassword] = useState(false)
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({})

  const dispatch = useAppDispatch()
  const navigate = useNavigate()
  const location = useLocation()
  
  const { loading, error, isAuthenticated } = useAppSelector(selectAuth)
  
  // URL de redirection après connexion
  const from = (location.state as LocationState)?.from || '/'

  /**
   * Redirection si déjà connecté
   */
  useEffect(() => {
    if (isAuthenticated) {
      navigate(from, { replace: true })
    }
  }, [isAuthenticated, navigate, from])

  /**
   * Nettoyer les erreurs lors du changement de champs
   */
  useEffect(() => {
    if (error) {
      dispatch(clearError())
    }
  }, [credentials, dispatch])

  /**
   * Validation du formulaire
   */
  const validateForm = (): boolean => {
    const errors: Record<string, string> = {}

    // Validation username
    if (!credentials.username.trim()) {
      errors.username = 'Le nom d\'utilisateur est requis'
    } else if (credentials.username.length < 3) {
      errors.username = 'Le nom d\'utilisateur doit contenir au moins 3 caractères'
    }

    // Validation password
    if (!credentials.password) {
      errors.password = 'Le mot de passe est requis'
    } else if (credentials.password.length < 4) {
      errors.password = 'Le mot de passe doit contenir au moins 4 caractères'
    }

    setValidationErrors(errors)
    return Object.keys(errors).length === 0
  }

  /**
   * Gestion de la soumission du formulaire
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!validateForm()) {
      return
    }

    try {
      await dispatch(loginAsync(credentials)).unwrap()
      // La redirection sera gérée par l'useEffect
    } catch (error) {
      // L'erreur est déjà gérée par Redux
      console.error('Erreur de connexion:', error)
    }
  }

  /**
   * Gestion des changements de champs
   */
  const handleInputChange = (field: keyof typeof credentials) => (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    setCredentials(prev => ({
      ...prev,
      [field]: e.target.value
    }))
    
    // Effacer l'erreur de validation pour ce champ
    if (validationErrors[field]) {
      setValidationErrors(prev => ({
        ...prev,
        [field]: ''
      }))
    }
  }

  /**
   * Affichage des erreurs
   */
  const getFieldError = (field: string) => {
    return validationErrors[field] || ''
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        
        {/* En-tête */}
        <div className="text-center">
          <div className="text-5xl mb-4">📰</div>
          <h1 className="text-3xl font-bold text-gray-900">
            Connexion
          </h1>
          <p className="mt-2 text-gray-600">
            Connectez-vous à votre compte News Platform
          </p>
        </div>

        {/* Formulaire */}
        <form className="mt-8 space-y-6" onSubmit={handleSubmit} noValidate>
          
          {/* Message d'erreur global */}
          {error && (
            <div className="bg-red-50 border border-red-200 rounded-md p-4 animate-fade-in">
              <div className="flex">
                <div className="text-red-400 text-sm">⚠️</div>
                <div className="ml-2">
                  <h3 className="text-sm font-medium text-red-800">
                    Erreur de connexion
                  </h3>
                  <p className="mt-1 text-sm text-red-700">{error}</p>
                </div>
              </div>
            </div>
          )}

          <div className="space-y-4">
            
            {/* Champ Username */}
            <div>
              <label htmlFor="username" className="block text-sm font-medium text-gray-700 mb-1">
                Nom d'utilisateur
              </label>
              <input
                id="username"
                name="username"
                type="text"
                autoComplete="username"
                required
                value={credentials.username}
                onChange={handleInputChange('username')}
                className={`
                  relative block w-full px-3 py-2 border rounded-md placeholder-gray-500 text-gray-900 
                  focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors
                  ${getFieldError('username') 
                    ? 'border-red-300 focus:ring-red-500 focus:border-red-500' 
                    : 'border-gray-300'
                  }
                `}
                placeholder="Votre nom d'utilisateur"
                disabled={loading}
              />
              {getFieldError('username') && (
                <p className="mt-1 text-sm text-red-600 animate-fade-in">
                  {getFieldError('username')}
                </p>
              )}
            </div>

            {/* Champ Password */}
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
                Mot de passe
              </label>
              <div className="relative">
                <input
                  id="password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="current-password"
                  required
                  value={credentials.password}
                  onChange={handleInputChange('password')}
                  className={`
                    relative block w-full px-3 py-2 pr-10 border rounded-md placeholder-gray-500 text-gray-900 
                    focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors
                    ${getFieldError('password') 
                      ? 'border-red-300 focus:ring-red-500 focus:border-red-500' 
                      : 'border-gray-300'
                    }
                  `}
                  placeholder="Votre mot de passe"
                  disabled={loading}
                />
                
                {/* Bouton toggle password */}
                <button
                  type="button"
                  className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600"
                  onClick={() => setShowPassword(!showPassword)}
                  disabled={loading}
                >
                  <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    {showPassword ? (
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21" />
                    ) : (
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    )}
                  </svg>
                </button>
              </div>
              
              {getFieldError('password') && (
                <p className="mt-1 text-sm text-red-600 animate-fade-in">
                  {getFieldError('password')}
                </p>
              )}
            </div>
          </div>

          {/* Options supplémentaires */}
          <div className="flex items-center justify-between">
            <div className="flex items-center">
              <input
                id="remember-me"
                name="remember-me"
                type="checkbox"
                className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <label htmlFor="remember-me" className="ml-2 block text-sm text-gray-700">
                Se souvenir de moi
              </label>
            </div>

            <div className="text-sm">
              <a href="/forgot-password" className="font-medium text-blue-600 hover:text-blue-500 transition-colors">
                Mot de passe oublié ?
              </a>
            </div>
          </div>

          {/* Bouton de soumission */}
          <div>
            <Button
              type="submit"
              fullWidth
              loading={loading}
              loadingText="Connexion en cours..."
              disabled={loading || !credentials.username || !credentials.password}
              size="lg"
              className="font-semibold"
            >
              Se connecter
            </Button>
          </div>

          {/* Lien d'inscription */}
          <div className="text-center">
            <p className="text-sm text-gray-600">
              Pas encore de compte ?{' '}
              <Link 
                to="/register" 
                className="font-medium text-blue-600 hover:text-blue-500 transition-colors"
              >
                Créer un compte
              </Link>
            </p>
          </div>

          {/* Informations de test */}
          <div className="mt-6 p-4 bg-gray-50 border border-gray-200 rounded-md">
            <h3 className="text-sm font-medium text-gray-700 mb-2">
              💡 Comptes de test disponibles :
            </h3>
            <div className="text-xs text-gray-600 space-y-1">
              <div><strong>Admin:</strong> admin / password</div>
              <div><strong>Éditeur:</strong> editeur / password</div>
              <div><strong>Visiteur:</strong> visiteur / password</div>
            </div>
            <div className="text-xs text-blue-600 mt-2">
              ℹ️ Le mot de passe est "password" pour tous les comptes de test
            </div>
          </div>
        </form>
      </div>
    </div>
  )
} 