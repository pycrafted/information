import React from 'react';
import { Link } from 'react-router-dom';

/**
 * Footer global de l'application avec liens et informations
 */
const Footer: React.FC = () => {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="bg-gray-50 border-t border-gray-200 mt-auto">
      <div className="container mx-auto px-4 py-12">
        
        {/* Contenu principal du footer */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          
          {/* =================================== */}
          {/* SECTION MARQUE */}
          {/* =================================== */}
          <div className="md:col-span-1">
            <div className="flex items-center space-x-3 mb-4">
              <div className="w-8 h-8 bg-gradient-to-br from-blue-500 to-blue-600 rounded-lg flex items-center justify-center">
                <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z" />
                </svg>
              </div>
              <div>
                <h3 className="text-lg font-bold text-gray-900">News Platform</h3>
                <p className="text-sm text-gray-500">Actualités modernes</p>
              </div>
            </div>
            <p className="text-sm text-gray-600 leading-relaxed">
              Votre source d'information moderne et fiable. Découvrez les dernières actualités 
              avec une interface intuitive et professionnelle.
            </p>
          </div>

          {/* =================================== */}
          {/* NAVIGATION */}
          {/* =================================== */}
          <div>
            <h4 className="text-sm font-semibold text-gray-900 uppercase tracking-wider mb-4">
              Navigation
            </h4>
            <ul className="space-y-3">
              <li>
                <Link to="/" className="text-sm text-gray-600 hover:text-blue-600 transition-colors">
                  Accueil
                </Link>
              </li>
              <li>
                <Link to="/categories" className="text-sm text-gray-600 hover:text-blue-600 transition-colors">
                  Catégories
                </Link>
              </li>
              <li>
                <Link to="/about" className="text-sm text-gray-600 hover:text-blue-600 transition-colors">
                  À propos
                </Link>
              </li>
              <li>
                <Link to="/contact" className="text-sm text-gray-600 hover:text-blue-600 transition-colors">
                  Contact
                </Link>
              </li>
            </ul>
          </div>

          {/* =================================== */}
          {/* RESSOURCES */}
          {/* =================================== */}
          <div>
            <h4 className="text-sm font-semibold text-gray-900 uppercase tracking-wider mb-4">
              Ressources
            </h4>
            <ul className="space-y-3">
              <li>
                <Link to="/help" className="text-sm text-gray-600 hover:text-blue-600 transition-colors">
                  Centre d'aide
                </Link>
              </li>
              <li>
                <Link to="/api-docs" className="text-sm text-gray-600 hover:text-blue-600 transition-colors">
                  Documentation API
                </Link>
              </li>
              <li>
                <Link to="/privacy" className="text-sm text-gray-600 hover:text-blue-600 transition-colors">
                  Politique de confidentialité
                </Link>
              </li>
              <li>
                <Link to="/terms" className="text-sm text-gray-600 hover:text-blue-600 transition-colors">
                  Conditions d'utilisation
                </Link>
              </li>
            </ul>
          </div>

          {/* =================================== */}
          {/* CONTACT & RÉSEAUX */}
          {/* =================================== */}
          <div>
            <h4 className="text-sm font-semibold text-gray-900 uppercase tracking-wider mb-4">
              Suivez-nous
            </h4>
            
            {/* Réseaux sociaux */}
            <div className="flex space-x-4 mb-6">
              <a 
                href="#" 
                className="p-2 bg-gray-200 hover:bg-blue-500 text-gray-600 hover:text-white rounded-lg transition-colors"
                aria-label="Twitter"
              >
                <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M8.29 20.251c7.547 0 11.675-6.253 11.675-11.675 0-.178 0-.355-.012-.53A8.348 8.348 0 0022 5.92a8.19 8.19 0 01-2.357.646 4.118 4.118 0 001.804-2.27 8.224 8.224 0 01-2.605.996 4.107 4.107 0 00-6.993 3.743 11.65 11.65 0 01-8.457-4.287 4.106 4.106 0 001.27 5.477A4.072 4.072 0 012.8 9.713v.052a4.105 4.105 0 003.292 4.022 4.095 4.095 0 01-1.853.07 4.108 4.108 0 003.834 2.85A8.233 8.233 0 012 18.407a11.616 11.616 0 006.29 1.84" />
                </svg>
              </a>
              
              <a 
                href="#" 
                className="p-2 bg-gray-200 hover:bg-blue-600 text-gray-600 hover:text-white rounded-lg transition-colors"
                aria-label="LinkedIn"
              >
                <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433c-1.144 0-2.063-.926-2.063-2.065 0-1.138.92-2.063 2.063-2.063 1.14 0 2.064.925 2.064 2.063 0 1.139-.925 2.065-2.064 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z" />
                </svg>
              </a>
              
              <a 
                href="#" 
                className="p-2 bg-gray-200 hover:bg-gray-800 text-gray-600 hover:text-white rounded-lg transition-colors"
                aria-label="GitHub"
              >
                <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z" />
                </svg>
              </a>
            </div>

            {/* Contact info */}
            <div className="space-y-2">
              <p className="text-sm text-gray-600">
                <span className="font-medium">Email:</span>{' '}
                <a href="mailto:contact@newsplatform.com" className="hover:text-blue-600 transition-colors">
                  contact@newsplatform.com
                </a>
              </p>
              <p className="text-sm text-gray-600">
                <span className="font-medium">Téléphone:</span>{' '}
                <a href="tel:+33123456789" className="hover:text-blue-600 transition-colors">
                  +33 1 23 45 67 89
                </a>
              </p>
            </div>
          </div>
        </div>

        {/* =================================== */}
        {/* LIGNE DE SÉPARATION */}
        {/* =================================== */}
        <div className="border-t border-gray-200 mt-8 pt-8">
          <div className="flex flex-col md:flex-row justify-between items-center space-y-4 md:space-y-0">
            
            {/* Copyright */}
            <div className="text-sm text-gray-500">
              © {currentYear} News Platform. Tous droits réservés.
            </div>

            {/* Liens légaux */}
            <div className="flex space-x-6">
              <Link to="/privacy" className="text-sm text-gray-500 hover:text-blue-600 transition-colors">
                Confidentialité
              </Link>
              <Link to="/terms" className="text-sm text-gray-500 hover:text-blue-600 transition-colors">
                Conditions
              </Link>
              <Link to="/cookies" className="text-sm text-gray-500 hover:text-blue-600 transition-colors">
                Cookies
              </Link>
            </div>

            {/* Indicateur de statut */}
            <div className="flex items-center space-x-2">
              <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse" />
              <span className="text-sm text-gray-500">Tous les systèmes opérationnels</span>
            </div>
          </div>
        </div>

        {/* =================================== */}
        {/* INFORMATIONS TECHNIQUES */}
        {/* =================================== */}
        <div className="mt-4 pt-4 border-t border-gray-100">
          <div className="text-center">
            <p className="text-xs text-gray-400">
              Construit avec React, TypeScript et Tailwind CSS • 
              Backend Spring Boot • 
              Base de données PostgreSQL
            </p>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer; 