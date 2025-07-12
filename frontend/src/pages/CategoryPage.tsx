import React, { useEffect, useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { CategoryResponse, ArticleResponse, PaginatedResponse } from '../types/api'
import { apiClient, handleApiError } from '../services/api'
import { LoadingPage, LoadingArticleCard } from '../components/common/Loading'
import { Button } from '../components/ui/Button'

/**
 * Page d'une catégorie spécifique avec ses articles
 * 
 * Fonctionnalités :
 * - Récupération de la catégorie par slug
 * - Affichage des articles de cette catégorie
 * - Navigation hierarchique (parent/enfants)
 * - Pagination des articles
 * 
 * Respect des principes clean code :
 * - Gestion d'état claire
 * - UX cohérente avec le reste de l'app
 * - Performance optimisée
 */

export const CategoryPage: React.FC = () => {
  const { slug } = useParams<{ slug: string }>()
  const navigate = useNavigate()
  
  const [category, setCategory] = useState<CategoryResponse | null>(null)
  const [articles, setArticles] = useState<ArticleResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [loadingArticles, setLoadingArticles] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  /**
   * Récupération de la catégorie par slug
   */
  const fetchCategory = async (categorySlug: string) => {
    try {
      const response = await apiClient.get<CategoryResponse>(`/categories/slug/${categorySlug}`)
      setCategory(response.data)
      setError(null)
    } catch (err: any) {
      const errorMessage = handleApiError(err)
      setError(errorMessage)
      setCategory(null)
    }
  }

  /**
   * Récupération des articles de la catégorie
   */
  const fetchArticles = async (categoryId: number, pageNumber = 0, append = false) => {
    try {
      if (!append) setLoadingArticles(true)
      
      const response = await apiClient.get<PaginatedResponse<ArticleResponse>>(
        `/articles/category/${categoryId}?page=${pageNumber}&size=12`
      )

      const { content, totalPages: total } = response.data

      if (append) {
        setArticles(prev => [...prev, ...content])
      } else {
        setArticles(content)
      }
      
      setTotalPages(total)
      setPage(pageNumber)
    } catch (err: any) {
      const errorMessage = handleApiError(err)
      setError(errorMessage)
    } finally {
      setLoadingArticles(false)
    }
  }

  /**
   * Chargement initial
   */
  useEffect(() => {
    if (slug) {
      setLoading(true)
      fetchCategory(slug)
    } else {
      setError('Slug de catégorie manquant')
      setLoading(false)
    }
  }, [slug])

  /**
   * Chargement des articles quand la catégorie est récupérée
   */
  useEffect(() => {
    if (category) {
      fetchArticles(category.id)
      setLoading(false)
    }
  }, [category])

  /**
   * Charger plus d'articles
   */
  const loadMoreArticles = () => {
    if (category && page < totalPages - 1) {
      fetchArticles(category.id, page + 1, true)
    }
  }

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
   * Extraire un extrait du contenu
   */
  const getExcerpt = (content: string, maxLength = 150) => {
    if (content.length <= maxLength) return content
    return content.substring(0, maxLength).trim() + '...'
  }

  // État de chargement initial
  if (loading) {
    return <LoadingPage message="Chargement de la catégorie..." />
  }

  // État d'erreur
  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center space-y-4 max-w-md mx-auto px-4">
          <div className="text-red-500 text-5xl">📁</div>
          <h1 className="text-2xl font-bold text-gray-900">Catégorie non trouvée</h1>
          <p className="text-gray-600">{error}</p>
          <div className="space-y-2">
            <Button onClick={() => navigate('/categories')} variant="primary">
              Voir toutes les catégories
            </Button>
            <Button onClick={() => navigate('/')} variant="outline">
              Accueil
            </Button>
          </div>
        </div>
      </div>
    )
  }

  if (!category) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center space-y-4">
          <div className="text-gray-400 text-5xl">❓</div>
          <h1 className="text-2xl font-bold text-gray-900">Catégorie introuvable</h1>
          <Button onClick={() => navigate('/categories')} variant="primary">
            Voir toutes les catégories
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      
      {/* En-tête de la catégorie */}
      <header className="bg-white border-b border-gray-200">
        <div className="container-custom py-12">
          
          {/* Fil d'Ariane */}
          <nav className="mb-6" aria-label="Fil d'Ariane">
            <ol className="flex items-center space-x-2 text-sm text-gray-500">
              <li>
                <Link to="/" className="hover:text-blue-600 transition-colors">
                  Accueil
                </Link>
              </li>
              <li className="flex items-center">
                <svg className="w-4 h-4 mx-1" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
                </svg>
                <Link to="/categories" className="hover:text-blue-600 transition-colors">
                  Catégories
                </Link>
              </li>
              <li className="flex items-center">
                <svg className="w-4 h-4 mx-1" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
                </svg>
                <span className="text-gray-900 font-medium">{category.name}</span>
              </li>
            </ol>
          </nav>

          {/* Header avec icône coloré */}
          <div className="flex items-start space-x-6">
            <div className="flex-shrink-0 h-20 w-20 bg-gradient-to-br from-blue-500 to-purple-600 rounded-xl flex items-center justify-center">
              <span className="text-white text-2xl">📁</span>
            </div>
            
            <div className="flex-1 min-w-0">
              <h1 className="text-3xl md:text-4xl font-bold text-gray-900 mb-2">
                {category.name}
              </h1>
              
              {category.description && (
                <p className="text-xl text-gray-600 leading-relaxed mb-4">
                  {category.description}
                </p>
              )}
              
              <div className="flex flex-wrap items-center gap-4 text-sm text-gray-500">
                <span className="flex items-center">
                  <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                  {articles.length} articles
                </span>
                
                <span className="flex items-center">
                  <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                  </svg>
                  Niveau {category.level}
                </span>
                
                <time className="flex items-center">
                  <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  Créée le {formatDate(category.createdAt)}
                </time>
              </div>
            </div>
          </div>

          {/* Sous-catégories si présentes */}
          {category.children && category.children.length > 0 && (
            <div className="mt-8">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">Sous-catégories</h2>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                {category.children.map(child => (
                  <Link
                    key={child.id}
                    to={`/category/${child.slug}`}
                    className="p-3 bg-gray-50 border border-gray-200 rounded-lg hover:border-blue-300 hover:shadow-md transition-all text-center group"
                  >
                    <div className="text-lg mb-1">📂</div>
                    <div className="text-sm font-medium text-gray-900 group-hover:text-blue-600 transition-colors">
                      {child.name}
                    </div>
                  </Link>
                ))}
              </div>
            </div>
          )}
        </div>
      </header>

      {/* Articles de la catégorie */}
      <main className="container-custom py-8">
        
        {loadingArticles && articles.length === 0 ? (
          // Chargement des articles
          <div className="grid gap-6 md:gap-8 grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
            {Array.from({ length: 6 }).map((_, index) => (
              <LoadingArticleCard key={index} />
            ))}
          </div>
        ) : articles.length === 0 ? (
          // Aucun article
          <div className="text-center py-12">
            <div className="text-gray-400 text-6xl mb-4">📄</div>
            <h2 className="text-2xl font-semibold text-gray-900 mb-2">
              Aucun article dans cette catégorie
            </h2>
            <p className="text-gray-600 mb-6">
              Il n'y a pas encore d'articles publiés dans "{category.name}".
            </p>
            <div className="space-x-3">
              <Button onClick={() => navigate('/categories')} variant="primary">
                Voir d'autres catégories
              </Button>
              <Button onClick={() => navigate('/')} variant="outline">
                Derniers articles
              </Button>
            </div>
          </div>
        ) : (
          <>
            {/* En-tête des articles */}
            <div className="mb-8">
              <h2 className="text-2xl font-bold text-gray-900 mb-2">
                Articles de la catégorie "{category.name}"
              </h2>
              <p className="text-gray-600">
                {articles.length} article{articles.length > 1 ? 's' : ''} trouvé{articles.length > 1 ? 's' : ''}
              </p>
            </div>

            {/* Grille d'articles */}
            <div className="grid gap-6 md:gap-8 grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
              {articles.map((article) => (
                <article 
                  key={article.id} 
                  className="bg-white rounded-lg shadow-md overflow-hidden hover-lift transition-transform"
                >
                  {/* Image placeholder */}
                  <div className="h-48 bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center">
                    <span className="text-white text-2xl">📰</span>
                  </div>
                  
                  {/* Contenu */}
                  <div className="p-6 space-y-4">
                    {/* Statut */}
                    <div className="flex items-center space-x-2">
                      <span className={`inline-block px-2 py-1 text-xs font-medium rounded-full ${
                        article.status === 'PUBLISHED' 
                          ? 'bg-green-100 text-green-800' 
                          : 'bg-yellow-100 text-yellow-800'
                      }`}>
                        {article.status === 'PUBLISHED' ? '✓ Publié' : '⏳ Brouillon'}
                      </span>
                    </div>
                    
                    {/* Titre */}
                    <h3 className="text-xl font-bold text-gray-900 line-clamp-2 hover:text-blue-600 transition-colors">
                      <Link to={`/article/${article.id}`}>
                        {article.title}
                      </Link>
                    </h3>
                    
                    {/* Résumé */}
                    <p className="text-gray-600 text-sm leading-relaxed line-clamp-3">
                      {article.summary || getExcerpt(article.content)}
                    </p>
                    
                    {/* Métadonnées */}
                    <div className="flex items-center justify-between pt-4 border-t border-gray-100">
                      <div className="flex items-center space-x-2">
                        <div className="h-8 w-8 bg-gray-300 rounded-full flex items-center justify-center text-xs font-medium">
                          {article.author.username.charAt(0).toUpperCase()}
                        </div>
                        <div className="text-sm">
                          <div className="font-medium text-gray-900">{article.author.username}</div>
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
                        Lire l'article
                        <svg className="ml-1 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                        </svg>
                      </Link>
                    </div>
                  </div>
                </article>
              ))}
            </div>

            {/* Bouton "Charger plus" */}
            {page < totalPages - 1 && (
              <div className="text-center mt-12">
                <Button 
                  onClick={loadMoreArticles}
                  loading={loadingArticles}
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