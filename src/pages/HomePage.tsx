import React, { useEffect, useState } from 'react';
import { articleService } from '../services/articleService';
import type { ArticleResponse } from '../types/api';

/**
 * Page d'accueil - Affiche les derniers articles publiés
 * Utilise l'endpoint GET /api/articles/recent du backend
 */
const HomePage: React.FC = () => {
  const [articles, setArticles] = useState<ArticleResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Charger les articles au montage du composant
  useEffect(() => {
    const loadRecentArticles = async () => {
      try {
        setLoading(true);
        setError(null);
        
        const recentArticles = await articleService.getRecentArticles();
        setArticles(recentArticles);
        
      } catch (err: any) {
        console.error('Failed to load recent articles:', err);
        setError(err.response?.data?.message || 'Erreur lors du chargement des articles');
      } finally {
        setLoading(false);
      }
    };

    loadRecentArticles();
  }, []);

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto"></div>
          <p className="mt-4 text-muted-foreground">Chargement des articles...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center">
          <div className="bg-destructive/10 border border-destructive/20 rounded-lg p-6">
            <h2 className="text-lg font-semibold text-destructive mb-2">
              Erreur de chargement
            </h2>
            <p className="text-sm text-muted-foreground">{error}</p>
            <button 
              onClick={() => window.location.reload()} 
              className="mt-4 px-4 py-2 bg-primary text-primary-foreground rounded-md hover:bg-primary/90"
            >
              Réessayer
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Hero Section */}
      <section className="text-center mb-12">
        <h1 className="text-4xl font-bold text-foreground mb-4">
          Bienvenue sur News Platform
        </h1>
        <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
          Découvrez les dernières actualités, articles et analyses de qualité. 
          Notre plateforme vous offre un accès à l'information de façon moderne et intuitive.
        </p>
      </section>

      {/* Section des derniers articles */}
      <section>
        <div className="flex items-center justify-between mb-8">
          <h2 className="text-2xl font-semibold text-foreground">
            Derniers Articles
          </h2>
          <span className="text-sm text-muted-foreground">
            {articles.length} article{articles.length > 1 ? 's' : ''} récent{articles.length > 1 ? 's' : ''}
          </span>
        </div>

        {articles.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-6xl mb-4">📰</div>
            <h3 className="text-lg font-medium text-foreground mb-2">
              Aucun article disponible
            </h3>
            <p className="text-muted-foreground">
              Il n'y a pas encore d'articles publiés sur la plateforme.
            </p>
          </div>
        ) : (
          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {articles.map((article) => (
              <article 
                key={article.id} 
                className="bg-card border border-border rounded-lg p-6 hover:shadow-md transition-shadow"
              >
                {/* Badge de catégorie */}
                <div className="mb-3">
                  <span className="inline-block px-2 py-1 text-xs font-medium bg-primary/10 text-primary rounded-full">
                    {article.categoryName}
                  </span>
                </div>

                {/* Titre */}
                <h3 className="text-lg font-semibold text-foreground mb-2 line-clamp-2">
                  <a 
                    href={`/article/${article.id}`}
                    className="hover:text-primary transition-colors"
                  >
                    {article.title}
                  </a>
                </h3>

                {/* Résumé */}
                <p className="text-sm text-muted-foreground mb-4 line-clamp-3">
                  {article.summary}
                </p>

                {/* Métadonnées */}
                <div className="flex items-center justify-between text-xs text-muted-foreground">
                  <span>Par {article.authorUsername}</span>
                  <span>
                    {new Date(article.publishedAt || article.createdAt).toLocaleDateString('fr-FR')}
                  </span>
                </div>

                {/* Lien de lecture */}
                <div className="mt-4">
                  <a 
                    href={`/article/${article.id}`}
                    className="inline-flex items-center text-sm font-medium text-primary hover:text-primary/80"
                  >
                    Lire l'article
                    <svg className="ml-1 w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5l7 7-7 7" />
                    </svg>
                  </a>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>

      {/* Call to Action */}
      {articles.length > 0 && (
        <section className="text-center mt-12">
          <a 
            href="/categories"
            className="inline-flex items-center px-6 py-3 bg-primary text-primary-foreground font-medium rounded-lg hover:bg-primary/90 transition-colors"
          >
            Voir toutes les catégories
            <svg className="ml-2 w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5l7 7-7 7" />
            </svg>
          </a>
        </section>
      )}
    </div>
  );
};

export default HomePage; 