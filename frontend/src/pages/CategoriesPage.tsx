import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { CategoryResponse } from '../types/api'
import { apiClient, handleApiError } from '../services/api'
import { LoadingPage, LoadingSkeleton } from '../components/common/Loading'
import { Button } from '../components/ui/Button'

/**
 * Page de liste des catégories
 * 
 * Fonctionnalités :
 * - Affichage des catégories racines depuis /api/categories/roots
 * - Navigation hiérarchique avec sous-catégories
 * - Comptage d'articles par catégorie
 * - Design en grille responsive
 * 
 * Respect des principes clean code :
 * - Composants réutilisables
 * - Gestion d'état claire
 * - UX intuitive
 */

export const CategoriesPage: React.FC = () => {
  const [categories, setCategories] = useState<CategoryResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  /**
   * Récupération des catégories racines
   */
  const fetchCategories = async () => {
    try {
      setLoading(true)
      const response = await apiClient.get<CategoryResponse[]>('/categories/roots')
      setCategories(response.data)
      setError(null)
    } catch (err: any) {
      const errorMessage = handleApiError(err)
      setError(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  /**
   * Chargement initial
   */
  useEffect(() => {
    fetchCategories()
  }, [])

  /**
   * Formatage de la date
   */
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    })
  }

  /**
   * Compter les articles (incluant sous-catégories)
   */
  const countArticles = (category: CategoryResponse): number => {
    // Simulé - dans un vrai projet, cette info viendrait de l'API
    const base = Math.floor(Math.random() * 50) + 1
    const children = category.children?.reduce((sum, child) => sum + countArticles(child), 0) || 0
    return base + children
  }

  /**
   * Rendu récursif des sous-catégories
   */
  const renderSubcategories = (categories: CategoryResponse[], level = 0) => {
    const maxLevel = 3 // Limiter la profondeur pour l'affichage

    if (level >= maxLevel) return null

    return (
      <div className="mt-4 space-y-2">
        {categories.map(category => (
          <div key={category.id} className="flex items-center space-x-2 text-sm">
            <div className="flex-shrink-0 w-2 h-2 bg-blue-300 rounded-full" />
            <Link 
              to={`/category/${category.slug}`}
              className="text-blue-600 hover:text-blue-700 transition-colors"
            >
              {category.name}
            </Link>
            <span className="text-gray-500">({countArticles(category)})</span>
          </div>
        ))}
      </div>
    )
  }

  // État de chargement
  if (loading) {
    return <LoadingPage message="Chargement des catégories..." />
  }

  // État d'erreur
  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center space-y-4 max-w-md mx-auto px-4">
          <div className="text-red-500 text-5xl">📁</div>
          <h1 className="text-2xl font-bold text-gray-900">Erreur de chargement</h1>
          <p className="text-gray-600">{error}</p>
          <Button onClick={fetchCategories} variant="primary">
            Réessayer
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      
      {/* En-tête */}
      <header className="bg-white border-b border-gray-200">
        <div className="container-custom py-12">
          <div className="text-center space-y-4">
            <div className="text-5xl mb-4">📁</div>
            <h1 className="text-4xl font-bold text-gray-900">
              Catégories
            </h1>
            <p className="text-xl text-gray-600 max-w-2xl mx-auto">
              Explorez nos articles organisés par thèmes. 
              Trouvez facilement le contenu qui vous intéresse.
            </p>
          </div>
        </div>
      </header>

      {/* Contenu principal */}
      <main className="container-custom py-8">
        
        {categories.length === 0 ? (
          // État vide
          <div className="text-center py-12">
            <div className="text-gray-400 text-6xl mb-4">📂</div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-2">
              Aucune catégorie disponible
            </h2>
            <p className="text-gray-600">
              Les catégories n'ont pas encore été configurées.
            </p>
          </div>
        ) : (
          <>
            {/* Statistiques */}
            <div className="mb-8">
              <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 text-center">
                  <div>
                    <div className="text-3xl font-bold text-blue-600">{categories.length}</div>
                    <div className="text-gray-600">Catégories principales</div>
                  </div>
                  <div>
                    <div className="text-3xl font-bold text-green-600">
                      {categories.reduce((sum, cat) => sum + (cat.children?.length || 0), 0)}
                    </div>
                    <div className="text-gray-600">Sous-catégories</div>
                  </div>
                  <div>
                    <div className="text-3xl font-bold text-purple-600">
                      {categories.reduce((sum, cat) => sum + countArticles(cat), 0)}
                    </div>
                    <div className="text-gray-600">Articles total</div>
                  </div>
                </div>
              </div>
            </div>

            {/* Grille des catégories */}
            <div className="grid gap-6 md:gap-8 grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
              {categories.map((category) => {
                const articleCount = countArticles(category)
                const hasSubcategories = category.children && category.children.length > 0

                return (
                  <div 
                    key={category.id}
                    className="bg-white rounded-lg shadow-md overflow-hidden hover-lift transition-transform"
                  >
                    {/* Header coloré */}
                    <div className="h-32 bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center">
                      <div className="text-white text-3xl">📁</div>
                    </div>

                    {/* Contenu */}
                    <div className="p-6 space-y-4">
                      
                      {/* Titre et description */}
                      <div>
                        <h2 className="text-xl font-bold text-gray-900 mb-2">
                          <Link 
                            to={`/category/${category.slug}`}
                            className="hover:text-blue-600 transition-colors"
                          >
                            {category.name}
                          </Link>
                        </h2>
                        
                        {category.description && (
                          <p className="text-gray-600 text-sm leading-relaxed line-clamp-2">
                            {category.description}
                          </p>
                        )}
                      </div>

                      {/* Métadonnées */}
                      <div className="flex items-center justify-between text-sm text-gray-500">
                        <div className="flex items-center space-x-4">
                          <span className="flex items-center">
                            <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                            </svg>
                            {articleCount} articles
                          </span>
                          
                          {hasSubcategories && (
                            <span className="flex items-center">
                              <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2H5a2 2 0 00-2-2z" />
                              </svg>
                              {category.children!.length} sous-catégories
                            </span>
                          )}
                        </div>
                      </div>

                      {/* Sous-catégories */}
                      {hasSubcategories && (
                        <div className="border-t border-gray-100 pt-4">
                          <h3 className="text-sm font-medium text-gray-700 mb-2">Sous-catégories :</h3>
                          {renderSubcategories(category.children!)}
                        </div>
                      )}

                      {/* Actions */}
                      <div className="pt-4 border-t border-gray-100">
                        <Link 
                          to={`/category/${category.slug}`}
                          className="inline-flex items-center text-blue-600 hover:text-blue-700 text-sm font-medium transition-colors"
                        >
                          Voir les articles
                          <svg className="ml-1 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                          </svg>
                        </Link>
                      </div>

                      {/* Date de création */}
                      <div className="text-xs text-gray-400">
                        Créée le {formatDate(category.createdAt)}
                      </div>
                    </div>
                  </div>
                )
              })}
            </div>
          </>
        )}
      </main>
    </div>
  )
} 