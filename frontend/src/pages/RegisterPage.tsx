import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Button } from '../components/ui/Button'

/**
 * Page d'inscription - Création de nouveaux comptes
 * 
 * Fonctionnalités :
 * - Formulaire d'inscription avec validation
 * - Validation côté client
 * - Design cohérent avec LoginPage
 * - Redirections appropriées
 * 
 * Note : Pour l'instant, cette page affiche un message informatif
 * car l'inscription n'est pas encore implémentée côté backend
 */

export const RegisterPage: React.FC = () => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: ''
  })
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  /**
   * Validation du formulaire
   */
  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {}

    // Validation username
    if (!formData.username.trim()) {
      newErrors.username = 'Le nom d\'utilisateur est requis'
    } else if (formData.username.length < 3) {
      newErrors.username = 'Le nom d\'utilisateur doit contenir au moins 3 caractères'
    }

    // Validation email
    if (!formData.email.trim()) {
      newErrors.email = 'L\'email est requis'
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Format d\'email invalide'
    }

    // Validation password
    if (!formData.password) {
      newErrors.password = 'Le mot de passe est requis'
    } else if (formData.password.length < 6) {
      newErrors.password = 'Le mot de passe doit contenir au moins 6 caractères'
    }

    // Validation confirm password
    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Les mots de passe ne correspondent pas'
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  /**
   * Gestion de la soumission du formulaire
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!validateForm()) {
      return
    }

    setLoading(true)
    
    // TODO: Implémenter l'inscription côté backend
    await new Promise(resolve => setTimeout(resolve, 1000)) // Simulation
    
    setLoading(false)
    
    // Pour l'instant, rediriger vers login avec un message
    navigate('/login', { 
      state: { 
        message: 'Inscription non encore disponible. Utilisez les comptes de test.' 
      } 
    })
  }

  /**
   * Gestion des changements de champs
   */
  const handleInputChange = (field: keyof typeof formData) => (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    setFormData(prev => ({
      ...prev,
      [field]: e.target.value
    }))
    
    // Effacer l'erreur pour ce champ
    if (errors[field]) {
      setErrors(prev => ({
        ...prev,
        [field]: ''
      }))
    }
  }

  /**
   * Affichage des erreurs
   */
  const getFieldError = (field: string) => {
    return errors[field] || ''
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        
        {/* En-tête */}
        <div className="text-center">
          <div className="text-5xl mb-4">📝</div>
          <h1 className="text-3xl font-bold text-gray-900">
            Créer un compte
          </h1>
          <p className="mt-2 text-gray-600">
            Rejoignez la communauté News Platform
          </p>
        </div>

        {/* Message informatif */}
        <div className="bg-blue-50 border border-blue-200 rounded-md p-4">
          <div className="flex">
            <div className="text-blue-400 text-sm">ℹ️</div>
            <div className="ml-2">
              <h3 className="text-sm font-medium text-blue-800">
                Inscription temporairement indisponible
              </h3>
              <p className="mt-1 text-sm text-blue-700">
                Utilisez les comptes de test disponibles sur la page de connexion.
              </p>
            </div>
          </div>
        </div>

        {/* Formulaire */}
        <form className="mt-8 space-y-6" onSubmit={handleSubmit} noValidate>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Prénom */}
            <div>
              <label htmlFor="firstName" className="block text-sm font-medium text-gray-700 mb-1">
                Prénom
              </label>
              <input
                id="firstName"
                name="firstName"
                type="text"
                value={formData.firstName}
                onChange={handleInputChange('firstName')}
                className="relative block w-full px-3 py-2 border border-gray-300 rounded-md placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                placeholder="Votre prénom"
                disabled={loading}
              />
            </div>

            {/* Nom */}
            <div>
              <label htmlFor="lastName" className="block text-sm font-medium text-gray-700 mb-1">
                Nom
              </label>
              <input
                id="lastName"
                name="lastName"
                type="text"
                value={formData.lastName}
                onChange={handleInputChange('lastName')}
                className="relative block w-full px-3 py-2 border border-gray-300 rounded-md placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                placeholder="Votre nom"
                disabled={loading}
              />
            </div>
          </div>

          <div className="space-y-4">
            
            {/* Nom d'utilisateur */}
            <div>
              <label htmlFor="username" className="block text-sm font-medium text-gray-700 mb-1">
                Nom d'utilisateur *
              </label>
              <input
                id="username"
                name="username"
                type="text"
                required
                value={formData.username}
                onChange={handleInputChange('username')}
                className={`
                  relative block w-full px-3 py-2 border rounded-md placeholder-gray-500 text-gray-900 
                  focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors
                  ${getFieldError('username') 
                    ? 'border-red-300 focus:ring-red-500 focus:border-red-500' 
                    : 'border-gray-300'
                  }
                `}
                placeholder="Choisissez un nom d'utilisateur"
                disabled={loading}
              />
              {getFieldError('username') && (
                <p className="mt-1 text-sm text-red-600">{getFieldError('username')}</p>
              )}
            </div>

            {/* Email */}
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
                Adresse email *
              </label>
              <input
                id="email"
                name="email"
                type="email"
                required
                value={formData.email}
                onChange={handleInputChange('email')}
                className={`
                  relative block w-full px-3 py-2 border rounded-md placeholder-gray-500 text-gray-900 
                  focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors
                  ${getFieldError('email') 
                    ? 'border-red-300 focus:ring-red-500 focus:border-red-500' 
                    : 'border-gray-300'
                  }
                `}
                placeholder="votre@email.com"
                disabled={loading}
              />
              {getFieldError('email') && (
                <p className="mt-1 text-sm text-red-600">{getFieldError('email')}</p>
              )}
            </div>

            {/* Mot de passe */}
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
                Mot de passe *
              </label>
              <input
                id="password"
                name="password"
                type="password"
                required
                value={formData.password}
                onChange={handleInputChange('password')}
                className={`
                  relative block w-full px-3 py-2 border rounded-md placeholder-gray-500 text-gray-900 
                  focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors
                  ${getFieldError('password') 
                    ? 'border-red-300 focus:ring-red-500 focus:border-red-500' 
                    : 'border-gray-300'
                  }
                `}
                placeholder="Choisissez un mot de passe"
                disabled={loading}
              />
              {getFieldError('password') && (
                <p className="mt-1 text-sm text-red-600">{getFieldError('password')}</p>
              )}
            </div>

            {/* Confirmation mot de passe */}
            <div>
              <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-1">
                Confirmer le mot de passe *
              </label>
              <input
                id="confirmPassword"
                name="confirmPassword"
                type="password"
                required
                value={formData.confirmPassword}
                onChange={handleInputChange('confirmPassword')}
                className={`
                  relative block w-full px-3 py-2 border rounded-md placeholder-gray-500 text-gray-900 
                  focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors
                  ${getFieldError('confirmPassword') 
                    ? 'border-red-300 focus:ring-red-500 focus:border-red-500' 
                    : 'border-gray-300'
                  }
                `}
                placeholder="Confirmez votre mot de passe"
                disabled={loading}
              />
              {getFieldError('confirmPassword') && (
                <p className="mt-1 text-sm text-red-600">{getFieldError('confirmPassword')}</p>
              )}
            </div>
          </div>

          {/* Bouton de soumission */}
          <div>
            <Button
              type="submit"
              fullWidth
              loading={loading}
              loadingText="Création du compte..."
              disabled={loading}
              size="lg"
              className="font-semibold"
            >
              Créer mon compte
            </Button>
          </div>

          {/* Lien de connexion */}
          <div className="text-center">
            <p className="text-sm text-gray-600">
              Vous avez déjà un compte ?{' '}
              <Link 
                to="/login" 
                className="font-medium text-blue-600 hover:text-blue-500 transition-colors"
              >
                Se connecter
              </Link>
            </p>
          </div>
        </form>
      </div>
    </div>
  )
} 