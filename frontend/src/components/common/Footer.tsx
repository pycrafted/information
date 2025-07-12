import React from 'react'
import { Link } from 'react-router-dom'

/**
 * Composant Footer - Pied de page de l'application
 * 
 * Fonctionnalités :
 * - Informations de l'entreprise
 * - Liens utiles organisés par catégorie
 * - Design responsive
 * - Accessibilité optimisée
 * 
 * Respect des principes clean code :
 * - Composant pur sans state
 * - Structure sémantique HTML5
 * - Liens organisés logiquement
 */

export const Footer: React.FC = () => {
  const currentYear = new Date().getFullYear()

  return (
    <footer className="bg-gray-900 text-white" role="contentinfo">
      <div className="container-custom py-12">
        
        {/* Contenu principal du footer */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          
          {/* Informations entreprise */}
          <div className="space-y-4">
            <h3 className="text-lg font-semibold text-white">
              📰 News Platform
            </h3>
            <p className="text-gray-300 text-sm leading-relaxed">
              Plateforme d'actualités moderne offrant une expérience de lecture 
              optimale et des outils de gestion éditoriale complets.
            </p>
            <div className="flex space-x-4">
              <a 
                href="mailto:contact@newsplatform.com" 
                className="text-gray-400 hover:text-white transition-colors focus-ring p-1 rounded"
                aria-label="Nous contacter par email"
              >
                <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 20 20" aria-hidden="true">
                  <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z" />
                  <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z" />
                </svg>
              </a>
              <a 
                href="https://github.com/newsplatform" 
                target="_blank" 
                rel="noopener noreferrer"
                className="text-gray-400 hover:text-white transition-colors focus-ring p-1 rounded"
                aria-label="Voir notre code source sur GitHub"
              >
                <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 20 20" aria-hidden="true">
                  <path fillRule="evenodd" d="M10 0C4.477 0 0 4.484 0 10.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0110 4.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.203 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.942.359.31.678.921.678 1.856 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0020 10.017C20 4.484 15.522 0 10 0z" clipRule="evenodd" />
                </svg>
              </a>
            </div>
          </div>

          {/* Navigation */}
          <div className="space-y-4">
            <h3 className="text-lg font-semibold text-white">Navigation</h3>
            <ul className="space-y-2">
              <li>
                <Link 
                  to="/" 
                  className="text-gray-300 hover:text-white transition-colors text-sm focus-ring p-1 rounded"
                >
                  Accueil
                </Link>
              </li>
              <li>
                <Link 
                  to="/categories" 
                  className="text-gray-300 hover:text-white transition-colors text-sm focus-ring p-1 rounded"
                >
                  Catégories
                </Link>
              </li>
              <li>
                <a 
                  href="/api/docs" 
                  target="_blank" 
                  rel="noopener noreferrer"
                  className="text-gray-300 hover:text-white transition-colors text-sm focus-ring p-1 rounded"
                >
                  API Documentation
                </a>
              </li>
            </ul>
          </div>

          {/* Ressources */}
          <div className="space-y-4">
            <h3 className="text-lg font-semibold text-white">Ressources</h3>
            <ul className="space-y-2">
              <li>
                <a 
                  href="/docs/user-guide" 
                  className="text-gray-300 hover:text-white transition-colors text-sm focus-ring p-1 rounded"
                >
                  Guide utilisateur
                </a>
              </li>
              <li>
                <a 
                  href="/docs/api" 
                  className="text-gray-300 hover:text-white transition-colors text-sm focus-ring p-1 rounded"
                >
                  Documentation API
                </a>
              </li>
              <li>
                <a 
                  href="/support" 
                  className="text-gray-300 hover:text-white transition-colors text-sm focus-ring p-1 rounded"
                >
                  Support technique
                </a>
              </li>
              <li>
                <a 
                  href="/status" 
                  className="text-gray-300 hover:text-white transition-colors text-sm focus-ring p-1 rounded"
                >
                  Statut des services
                </a>
              </li>
            </ul>
          </div>

          {/* Légal */}
          <div className="space-y-4">
            <h3 className="text-lg font-semibold text-white">Légal</h3>
            <ul className="space-y-2">
              <li>
                <a 
                  href="/privacy" 
                  className="text-gray-300 hover:text-white transition-colors text-sm focus-ring p-1 rounded"
                >
                  Politique de confidentialité
                </a>
              </li>
              <li>
                <a 
                  href="/terms" 
                  className="text-gray-300 hover:text-white transition-colors text-sm focus-ring p-1 rounded"
                >
                  Conditions d'utilisation
                </a>
              </li>
              <li>
                <a 
                  href="/cookies" 
                  className="text-gray-300 hover:text-white transition-colors text-sm focus-ring p-1 rounded"
                >
                  Politique des cookies
                </a>
              </li>
              <li>
                <a 
                  href="/accessibility" 
                  className="text-gray-300 hover:text-white transition-colors text-sm focus-ring p-1 rounded"
                >
                  Accessibilité
                </a>
              </li>
            </ul>
          </div>
        </div>

        {/* Séparateur */}
        <div className="border-t border-gray-800 mt-8 pt-8">
          <div className="flex flex-col md:flex-row justify-between items-center space-y-4 md:space-y-0">
            
            {/* Copyright */}
            <p className="text-gray-400 text-sm">
              © {currentYear} News Platform. Tous droits réservés.
            </p>

            {/* Informations techniques */}
            <div className="flex items-center space-x-4 text-xs text-gray-500">
              <span>Version 1.0.0</span>
              <span>•</span>
              <span>Propulsé par React & Spring Boot</span>
              <span>•</span>
              <a 
                href="https://github.com/newsplatform/issues" 
                target="_blank" 
                rel="noopener noreferrer"
                className="hover:text-gray-300 transition-colors focus-ring p-1 rounded"
              >
                Signaler un problème
              </a>
            </div>
          </div>
        </div>
      </div>
    </footer>
  )
} 