import { useState, useEffect } from 'react';
import { type Article, getRecentArticles } from '../services/articleService';
import ArticleCard from '../components/ArticleCard';

function HomePage() {
  const [articles, setArticles] = useState<Article[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchArticles = async () => {
      try {
        setLoading(true);
        setError(null);
        
        console.log('🏠 Chargement de la page d\'accueil...');
        const recentArticles = await getRecentArticles();
        
        setArticles(recentArticles);
        console.log(`✅ Page d'accueil chargée avec ${recentArticles.length} articles`);
      } catch (err: any) {
        console.error('❌ Erreur lors du chargement de la page d\'accueil:', err);
        setError(err.message || 'Erreur lors du chargement des articles');
      } finally {
        setLoading(false);
      }
    };

    fetchArticles();
  }, []);

  const handleArticleClick = (article: Article) => {
    console.log('🔗 Clic sur article:', article.title);
    // TODO: Navigation vers page article détaillée (Étape 4)
    alert(`Article sélectionné: "${article.title}"\n\n(Navigation vers détail à implémenter dans l'étape suivante)`);
  };

  return (
    <div style={{ 
      width: '100%', 
      padding: '20px',
      backgroundColor: '#f8f9fa',
      minHeight: '100vh'
    }}>
      {/* En-tête de bienvenue */}
      <header style={{ marginBottom: '30px', textAlign: 'center' }}>
        <h1 style={{ 
          fontSize: '2.2em', 
          color: '#333', 
          marginBottom: '10px',
          fontWeight: 'bold'
        }}>
          Bienvenue sur la plateforme d'actualités
        </h1>
        <p style={{ 
          fontSize: '1.1em', 
          color: '#666',
          margin: '0'
        }}>
          Découvrez les dernières actualités
        </p>
      </header>

      {/* État de chargement */}
      {loading && (
        <div style={{ 
          textAlign: 'center', 
          padding: '40px',
          fontSize: '1.1em',
          color: '#666'
        }}>
          <div style={{ marginBottom: '10px' }}>🔄</div>
          Chargement des articles...
        </div>
      )}

      {/* État d'erreur */}
      {error && (
        <div style={{
          backgroundColor: '#f8d7da',
          border: '1px solid #f5c6cb',
          color: '#721c24',
          padding: '15px',
          borderRadius: '4px',
          marginBottom: '20px'
        }}>
          <h3 style={{ margin: '0 0 10px 0' }}>❌ Erreur</h3>
          <p style={{ margin: '0' }}>{error}</p>
          <button
            onClick={() => window.location.reload()}
            style={{
              marginTop: '10px',
              padding: '8px 16px',
              backgroundColor: '#721c24',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            🔄 Recharger
          </button>
        </div>
      )}

      {/* Liste des articles */}
      {!loading && !error && (
        <main>
          {articles.length > 0 ? (
            <>
              <h2 style={{ 
                fontSize: '1.8em', 
                marginBottom: '20px',
                color: '#333'
              }}>
                📝 Articles récents ({articles.length})
              </h2>
              
              <div>
                {articles.map((article) => (
                  <ArticleCard
                    key={article.id}
                    article={article}
                    onClick={() => handleArticleClick(article)}
                  />
                ))}
              </div>
            </>
          ) : (
            <div style={{
              textAlign: 'center',
              padding: '40px',
              backgroundColor: 'white',
              borderRadius: '8px',
              border: '1px solid #e0e0e0'
            }}>
              <div style={{ fontSize: '3em', marginBottom: '10px' }}>📭</div>
              <h3 style={{ color: '#666', marginBottom: '10px' }}>
                Aucun article disponible
              </h3>
              <p style={{ color: '#999', margin: '0' }}>
                Il semblerait qu'aucun article ne soit publié pour le moment.
              </p>
            </div>
          )}
        </main>
      )}

      {/* Footer */}
      <footer style={{ 
        marginTop: '40px', 
        textAlign: 'center',
        padding: '20px',
        borderTop: '1px solid #e0e0e0',
        color: '#666',
        fontSize: '0.9em'
      }}>
        <p>🚀 Frontend React + ⚙️ Backend Spring Boot</p>
        <p>Étape 3 : Page d'accueil publique avec articles récents</p>
      </footer>
    </div>
  );
}

export default HomePage; 