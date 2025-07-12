import React from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Button } from '../components/ui/Button'

/**
 * Page 404 - Page non trouvée
 * 
 * Fonctionnalités :
 * - Design moderne et engageant
 * - Navigation utile vers les pages principales
 * - Bouton retour intelligent
 * - Suggestions de contenu
 * 
 * Respect des principes clean code :
 * - Composant pur sans state complexe
 * - UX claire et helpful
 * - Accessibilité optimisée
 */

export const NotFoundPage: React.FC = () => {
  const navigate = useNavigate()

  const handleGoBack = () => {
    // Si possible, retour à la page précédente, sinon accueil
    if (window.history.length > 1) {
      navigate(-1)
    } else {
      navigate('/')
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4 sm:px-6 lg:px-8">
      <div className="max-w-lg w-full text-center space-y-8">
        
        {/* Illustration et code d'erreur */}
        <div className="space-y-4">
          <div className="text-6xl md:text-8xl font-bold text-gray-200">404</div>
          <div className="text-4xl md:text-5xl">🔍</div>
        </div>

        {/* Titre et description */}
        <div className="space-y-4">
          <h1 className="text-3xl md:text-4xl font-bold text-gray-900">
            Page non trouvée
          </h1>
          <p className="text-lg text-gray-600 leading-relaxed">
            Oups ! La page que vous recherchez semble avoir disparu. 
            Elle a peut-être été déplacée, supprimée ou l'URL est incorrecte.
          </p>
        </div>

        {/* Actions principales */}
        <div className="space-y-4">
          <div className="flex flex-col sm:flex-row gap-3 justify-center">
            <Button
              onClick={handleGoBack}
              variant="primary"
              size="lg"
              className="font-medium"
            >
              ← Retour
            </Button>
            
            <Button
              onClick={() => navigate('/')}
              variant="outline"
              size="lg"
              className="font-medium"
            >
              🏠 Accueil
            </Button>
          </div>
        </div>

        {/* Suggestions de navigation */}
        <div className="space-y-4">
          <h2 className="text-lg font-semibold text-gray-900">
            Où souhaitez-vous aller ?
          </h2>
          
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <Link
              to="/"
              className="p-4 bg-white border border-gray-200 rounded-lg hover:border-blue-300 hover:shadow-md transition-all text-left group"
            >
              <div className="flex items-center space-x-3">
                <div className="text-2xl">📰</div>
                <div>
                  <div className="font-medium text-gray-900 group-hover:text-blue-600 transition-colors">
                    Derniers articles
                  </div>
                  <div className="text-sm text-gray-500">
                    Découvrez l'actualité
                  </div>
                </div>
              </div>
            </Link>

            <Link
              to="/categories"
              className="p-4 bg-white border border-gray-200 rounded-lg hover:border-blue-300 hover:shadow-md transition-all text-left group"
            >
              <div className="flex items-center space-x-3">
                <div className="text-2xl">📁</div>
                <div>
                  <div className="font-medium text-gray-900 group-hover:text-blue-600 transition-colors">
                    Catégories
                  </div>
                  <div className="text-sm text-gray-500">
                    Parcourir par thème
                  </div>
                </div>
              </div>
            </Link>
          </div>
        </div>

        {/* Informations d'aide */}
        <div className="bg-gray-100 rounded-lg p-6 space-y-3">
          <h3 className="font-medium text-gray-900">Besoin d'aide ?</h3>
          <div className="text-sm text-gray-600 space-y-2">
            <p>
              Si vous pensez qu'il s'agit d'une erreur, vous pouvez :
            </p>
            <ul className="list-disc list-inside space-y-1 text-left">
              <li>Vérifier l'URL dans la barre d'adresse</li>
              <li>Utiliser le menu de navigation</li>
              <li>Contacter notre équipe de support</li>
            </ul>
          </div>
          
          <div className="pt-2">
            <a 
              href="mailto:support@newsplatform.com"
              className="inline-flex items-center text-blue-600 hover:text-blue-700 text-sm font-medium transition-colors"
            >
              📧 Contacter le support
              <svg className="ml-1 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
              </svg>
            </a>
          </div>
        </div>

        {/* Informations techniques */}
        <div className="text-xs text-gray-400 space-y-1">
          <p>Code d'erreur : HTTP 404</p>
          <p>Si le problème persiste, merci de le signaler à notre équipe technique.</p>
        </div>
      </div>
    </div>
  )
} 