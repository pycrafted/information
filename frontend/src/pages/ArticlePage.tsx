import React, { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ArticleResponse } from '../types/api'
import { apiClient, handleApiError } from '../services/api'
import { LoadingPage } from '../components/common/Loading'
import { Button } from '../components/ui/Button'

/**
 * Page de détail d'un article
 * 
 * Fonctionnalités :
 * - Récupération de l'article par ID depuis /api/articles/{id}
 * - Affichage complet avec métadonnées
 * - Gestion des erreurs (article non trouvé)
 * - Design responsive et lisible
 * 
 * Respect des principes clean code :
 * - Séparation des préoccupations
 * - Gestion d'état claire
 * - UX optimisée
 */

export const ArticlePage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  
  const [article, setArticle] = useState<ArticleResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  /**
   * Récupération de l'article par ID
   */
  const fetchArticle = async (articleId: string) => {
    try {
      setLoading(true)
      const response = await apiClient.get<ArticleResponse>(`/articles/${articleId}`)
      setArticle(response.data)
      setError(null)
    } catch (err: any) {
      const errorMessage = handleApiError(err)
      setError(errorMessage)
      setArticle(null)
    } finally {
      setLoading(false)
    }
  }

  /**
   * Chargement initial
   */
  useEffect(() => {
    if (id) {
      fetchArticle(id)
    } else {
      setError('ID d\'article manquant')
      setLoading(false)
    }
  }, [id])

  /**
   * Formatage de la date
   */
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  /**
   * Formatage du contenu avec paragraphes
   */
  const formatContent = (content: string) => {
    return content.split('\n').map((paragraph, index) => (
      <p key={index} className="mb-4 leading-relaxed">
        {paragraph}
      </p>
    ))
  }

  // État de chargement
  if (loading) {
    return <LoadingPage message="Chargement de l'article..." />
  }

  // État d'erreur
  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center space-y-4 max-w-md mx-auto px-4">
          <div className="text-red-500 text-5xl">📰</div>
          <h1 className="text-2xl font-bold text-gray-900">Article non trouvé</h1>
          <p className="text-gray-600">{error}</p>
          <div className="space-y-2">
            <Button onClick={() => navigate(-1)} variant="primary">
              Retour
            </Button>
            <Button onClick={() => navigate('/')} variant="outline">
              Accueil
            </Button>
          </div>
        </div>
      </div>
    )
  }

  // Affichage de l'article
  if (!article) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center space-y-4">
          <div className="text-gray-400 text-5xl">❓</div>
          <h1 className="text-2xl font-bold text-gray-900">Article introuvable</h1>
          <Button onClick={() => navigate('/')} variant="primary">
            Retour à l'accueil
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      
      {/* En-tête de l'article */}
      <header className="bg-white border-b border-gray-200">
        <div className="container-custom py-8">
          
          {/* Fil d'Ariane */}
          <nav className="mb-6" aria-label="Fil d'Ariane">
            <ol className="flex items-center space-x-2 text-sm text-gray-500">
              <li>
                <button 
                  onClick={() => navigate('/')}
                  className="hover:text-blue-600 transition-colors"
                >
                  Accueil
                </button>
              </li>
              <li className="flex items-center">
                <svg className="w-4 h-4 mx-1" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
                </svg>
                <button 
                  onClick={() => navigate(`/category/${article.category.slug}`)}
                  className="hover:text-blue-600 transition-colors"
                >
                  {article.category.name}
                </button>
              </li>
              <li className="flex items-center">
                <svg className="w-4 h-4 mx-1" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
                </svg>
                <span className="text-gray-900 font-medium">Article</span>
              </li>
            </ol>
          </nav>

          {/* Métadonnées */}
          <div className="mb-6">
            <div className="flex flex-wrap items-center gap-4 text-sm text-gray-600">
              
              {/* Catégorie */}
              <span className="inline-flex items-center px-3 py-1 bg-blue-100 text-blue-800 rounded-full font-medium">
                {article.category.name}
              </span>
              
              {/* Statut */}
              <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                article.status === 'PUBLISHED' 
                  ? 'bg-green-100 text-green-800' 
                  : 'bg-yellow-100 text-yellow-800'
              }`}>
                {article.status === 'PUBLISHED' ? '✓ Publié' : '⏳ Brouillon'}
              </span>
              
              {/* Date */}
              <time className="flex items-center">
                <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                {formatDate(article.publishedAt || article.createdAt)}
              </time>
            </div>
          </div>

          {/* Titre */}
          <h1 className="text-3xl md:text-4xl lg:text-5xl font-bold text-gray-900 leading-tight mb-6">
            {article.title}
          </h1>

          {/* Résumé */}
          {article.summary && (
            <div className="text-xl text-gray-600 leading-relaxed mb-6 italic">
              {article.summary}
            </div>
          )}

          {/* Auteur */}
          <div className="flex items-center space-x-3">
            <div className="h-12 w-12 bg-blue-500 rounded-full flex items-center justify-center text-white font-semibold text-lg">
              {article.author.username.charAt(0).toUpperCase()}
            </div>
            <div>
              <div className="font-semibold text-gray-900">{article.author.username}</div>
              <div className="text-sm text-gray-600">
                {article.author.role} • {article.author.email}
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Contenu principal */}
      <main className="container-custom py-8">
        <div className="max-w-4xl mx-auto">
          
          {/* Image placeholder */}
          <div className="mb-8 h-64 md:h-96 bg-gradient-to-br from-blue-500 to-purple-600 rounded-lg flex items-center justify-center">
            <div className="text-white text-4xl md:text-6xl">📰</div>
          </div>

          {/* Corps de l'article */}
          <article className="prose prose-lg max-w-none">
            <div className="text-gray-900 leading-relaxed text-lg">
              {formatContent(article.content)}
            </div>
          </article>

          {/* Actions */}
          <div className="mt-12 pt-8 border-t border-gray-200">
            <div className="flex flex-col sm:flex-row gap-4 justify-between items-center">
              
              {/* Navigation */}
              <div className="flex gap-3">
                <Button 
                  onClick={() => navigate(-1)}
                  variant="outline"
                  leftIcon={
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                    </svg>
                  }
                >
                  Retour
                </Button>
                
                <Button 
                  onClick={() => navigate('/')}
                  variant="outline"
                >
                  Tous les articles
                </Button>
              </div>

              {/* Partage */}
              <div className="flex items-center space-x-2 text-gray-600">
                <span className="text-sm font-medium">Partager :</span>
                <button 
                  onClick={() => navigator.clipboard.writeText(window.location.href)}
                  className="p-2 hover:bg-gray-100 rounded-full transition-colors"
                  title="Copier le lien"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  )
} 