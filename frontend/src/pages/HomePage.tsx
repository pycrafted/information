import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ArticleResponse, PaginatedResponse } from '../types/api'
import { apiClient, handleApiError } from '../services/api'
import { LoadingArticleCard, LoadingPage } from '../components/common/Loading'
import { Button } from '../components/ui/Button'

/**
 * Page d'accueil - Affichage des derniers articles
 * 
 * Fonctionnalités :
 * - Affichage des articles récents depuis /api/articles/recent
 * - Design responsive avec grid
 * - Gestion des états de chargement et d'erreur
 * - Pagination simple
 * 
 * Respect des principes clean code :
 * - Séparation logique/présentation
 * - Gestion d'erreur centralisée
 * - Composants réutilisables
 * - Vérifications de sécurité pour éviter les erreurs undefined
 */

export const HomePage: React.FC = () => {
  const [articles, setArticles] = useState<ArticleResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loadingMore, setLoadingMore] = useState(false)

  /**
   * Charger les articles récents depuis l'API
   */
  const fetchRecentArticles = async (pageNumber = 0, append = false) => {
    try {
      if (!append) setLoading(true)
      else setLoadingMore(true)

      const response = await apiClient.get<PaginatedResponse<ArticleResponse>>(
        `/articles/recent?page=${pageNumber}&size=12`
      )

      const { content, totalPages: total } = response.data

      // Vérification de sécurité pour éviter les erreurs undefined
      const safeContent = Array.isArray(content) ? content : []

      if (append) {
        setArticles(prev => {
          const prevSafe = Array.isArray(prev) ? prev : []
          return [...prevSafe, ...safeContent]
        })
      } else {
        setArticles(safeContent)
      }
      
      setTotalPages(total || 0)
      setPage(pageNumber)
      setError(null)
    } catch (err: any) {
      const errorMessage = handleApiError(err)
      setError(errorMessage)
      // En cas d'erreur, s'assurer que articles reste un tableau
      if (!Array.isArray(articles)) {
        setArticles([])
      }
    } finally {
      setLoading(false)
      setLoadingMore(false)
    }
  }

  /**
   * Charger plus d'articles (pagination)
   */
  const loadMore = () => {
    if (page < totalPages - 1) {
      fetchRecentArticles(page + 1, true)
    }
  }

  /**
   * Chargement initial
   */
  useEffect(() => {
    fetchRecentArticles()
  }, [])

  /**
   * Formatage de la date
   */
  const formatDate = (dateString: string) => {
    try {
      return new Date(dateString).toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    } catch {
      return 'Date inconnue'
    }
  }

  /**
   * Extraire un extrait du contenu
   */
  const getExcerpt = (content: string, maxLength = 150) => {
    if (!content || typeof content !== 'string') return ''
    if (content.length <= maxLength) return content
    return content.substring(0, maxLength).trim() + '...'
  }

  // Vérification de sécurité supplémentaire
  const safeArticles = Array.isArray(articles) ? articles : []

  // Page de chargement initial
  if (loading && safeArticles.length === 0) {
    return <LoadingPage message="Chargement des derniers articles..." />
  }

  // Page d'erreur
  if (error && safeArticles.length === 0) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center space-y-4 max-w-md mx-auto px-4">
          <div className="text-red-500 text-5xl">⚠️</div>
          <h1 className="text-2xl font-bold text-gray-900">Erreur de chargement</h1>
          <p className="text-gray-600">{error}</p>
          <Button onClick={() => fetchRecentArticles()} className="mt-4">
            Réessayer
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* En-tête de la page */}
      <section className="bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <div className="text-center space-y-4">
            <h1 className="text-4xl font-bold text-gray-900">
              Dernières Actualités
            </h1>
            <p className="text-xl text-gray-600 max-w-2xl mx-auto">
              Découvrez les derniers articles de notre plateforme d'actualités. 
              Restez informé des événements les plus récents.
            </p>
          </div>
        </div>
      </section>

      {/* Contenu principal */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {safeArticles.length === 0 ? (
          // État vide
          <div className="text-center py-12">
            <div className="text-gray-400 text-6xl mb-4">📰</div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-2">
              Aucun article disponible
            </h2>
            <p className="text-gray-600">
              Il n'y a pas encore d'articles publiés sur la plateforme.
            </p>
          </div>
        ) : (
          <>
            {/* Grille d'articles */}
            <div className="grid gap-6 md:gap-8 grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
              {safeArticles.map((article) => (
                <article 
                  key={article.id} 
                  className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-200"
                >
                  {/* Image placeholder */}
                  <div className="h-48 bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center">
                    <span className="text-white text-2xl">📰</span>
                  </div>
                  
                  {/* Contenu */}
                  <div className="p-6 space-y-4">
                    {/* Catégorie */}
                    <div className="flex items-center space-x-2">
                      <span className="inline-block px-2 py-1 text-xs font-medium bg-blue-100 text-blue-800 rounded-full">
                        {article.category?.name || 'Sans catégorie'}
                      </span>
                      <span className="text-gray-400 text-xs">
                        {article.status === 'PUBLISHED' ? '✓ Publié' : '⏳ Brouillon'}
                      </span>
                    </div>
                    
                    {/* Titre */}
                    <h2 className="text-xl font-bold text-gray-900 line-clamp-2 hover:text-blue-600 transition-colors">
                      <Link to={`/article/${article.id}`}>
                        {article.title || 'Titre non disponible'}
                      </Link>
                    </h2>
                    
                    {/* Résumé ou extrait */}
                    <p className="text-gray-600 text-sm leading-relaxed line-clamp-3">
                      {article.summary || getExcerpt(article.content || '')}
                    </p>
                    
                    {/* Métadonnées */}
                    <div className="flex items-center justify-between pt-4 border-t border-gray-100">
                      <div className="flex items-center space-x-2">
                        <div className="h-8 w-8 bg-gray-300 rounded-full flex items-center justify-center text-xs font-medium">
                          {article.author?.username?.charAt(0)?.toUpperCase() || '?'}
                        </div>
                        <div className="text-sm">
                          <div className="font-medium text-gray-900">{article.author?.username || 'Auteur inconnu'}</div>
                          <div className="text-gray-500">{article.author?.role || 'VISITEUR'}</div>
                        </div>
                      </div>
                      <time className="text-sm text-gray-500">
                        {formatDate(article.publishedAt || article.createdAt)}
                      </time>
                    </div>
                    
                    {/* Lien vers l'article */}
                    <div className="pt-2">
                      <Link 
                        to={`/article/${article.id}`}
                        className="inline-flex items-center text-blue-600 hover:text-blue-700 text-sm font-medium transition-colors"
                      >
                        Lire la suite
                        <svg className="ml-1 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                        </svg>
                      </Link>
                    </div>
                  </div>
                </article>
              ))}
              
              {/* Cartes de chargement lors du "Load More" */}
              {loadingMore && Array.from({ length: 3 }, (_, index) => (
                <LoadingArticleCard key={`loading-${index}`} />
              ))}
            </div>

            {/* Bouton "Charger plus" */}
            {page < totalPages - 1 && (
              <div className="text-center mt-12">
                <Button 
                  onClick={loadMore}
                  loading={loadingMore}
                  loadingText="Chargement..."
                  size="lg"
                  variant="outline"
                >
                  Charger plus d'articles
                </Button>
              </div>
            )}
          </>
        )}
      </main>
    </div>
  )
} 