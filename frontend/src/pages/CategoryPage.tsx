import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { type Category, getCategoryBySlug } from '../services/categoryService';
import { type Article, getPublishedArticles } from '../services/articleService';
import ArticleCard from '../components/ArticleCard';

function CategoryPage() {
  const { slug } = useParams<{ slug: string }>();
  
  const [category, setCategory] = useState<Category | null>(null);
  const [articles, setArticles] = useState<Article[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 5,
    totalElements: 0,
    totalPages: 0
  });

  useEffect(() => {
    if (!slug) return;

    const fetchCategoryData = async () => {
      try {
        setLoading(true);
        setError(null);
        
        console.log(`📁 Chargement de la catégorie "${slug}"...`);
        
        // Récupérer les informations de la catégorie
        const categoryData = await getCategoryBySlug(slug);
        setCategory(categoryData);
        
        // Récupérer les articles publiés (pour l'instant tous, on filtrera plus tard)
        const articlesData = await getPublishedArticles(0, 10);
        
        // Filtrer les articles de cette catégorie côté client
        // (En attendant un endpoint backend dédié)
        const filteredArticles = articlesData.content.filter(
          article => article.categoryName === categoryData.name
        );
        
        setArticles(filteredArticles);
        setPagination({
          page: 0,
          size: 10,
          totalElements: filteredArticles.length,
          totalPages: Math.ceil(filteredArticles.length / 10)
        });
        
        console.log(`✅ Catégorie "${categoryData.name}" chargée avec ${filteredArticles.length} articles`);
      } catch (err: any) {
        console.error('❌ Erreur lors du chargement de la catégorie:', err);
        setError(err.message || 'Erreur lors du chargement de la catégorie');
      } finally {
        setLoading(false);
      }
    };

    fetchCategoryData();
  }, [slug]);

  const handleArticleClick = (article: Article) => {
    console.log('🔗 Clic sur article:', article.title);
    // TODO: Navigation vers page article détaillée
    alert(`Article sélectionné: "${article.title}"\n\n(Navigation vers détail à implémenter)`);
  };

  if (loading) {
    return (
      <div style={{ 
        width: '100%', 
        padding: '20px',
        textAlign: 'center',
        minHeight: '50vh',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center'
      }}>
        <div style={{ fontSize: '3em', marginBottom: '20px' }}>🔄</div>
        <h2 style={{ color: '#666' }}>Chargement de la catégorie...</h2>
        <p style={{ color: '#999' }}>Récupération des articles en cours</p>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ 
        width: '100%', 
        padding: '20px' 
      }}>
        <div style={{
          backgroundColor: '#f8d7da',
          border: '1px solid #f5c6cb',
          color: '#721c24',
          padding: '20px',
          borderRadius: '8px',
          textAlign: 'center'
        }}>
          <div style={{ fontSize: '3em', marginBottom: '10px' }}>❌</div>
          <h2 style={{ margin: '0 0 10px 0' }}>Erreur</h2>
          <p style={{ margin: '0 0 15px 0' }}>{error}</p>
          <div style={{ display: 'flex', gap: '10px', justifyContent: 'center' }}>
            <Link 
              to="/"
              style={{
                padding: '10px 20px',
                backgroundColor: '#007bff',
                color: 'white',
                textDecoration: 'none',
                borderRadius: '4px'
              }}
            >
              🏠 Retour à l'accueil
            </Link>
            <button
              onClick={() => window.location.reload()}
              style={{
                padding: '10px 20px',
                backgroundColor: '#721c24',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              🔄 Réessayer
            </button>
          </div>
        </div>
      </div>
    );
  }

  if (!category) {
    return (
      <div style={{ 
        width: '100%', 
        padding: '20px',
        textAlign: 'center'
      }}>
        <div style={{ fontSize: '3em', marginBottom: '20px' }}>❓</div>
        <h2 style={{ color: '#666' }}>Catégorie non trouvée</h2>
        <p style={{ color: '#999' }}>La catégorie "{slug}" n'existe pas.</p>
        <Link 
          to="/"
          style={{
            display: 'inline-block',
            marginTop: '20px',
            padding: '10px 20px',
            backgroundColor: '#007bff',
            color: 'white',
            textDecoration: 'none',
            borderRadius: '4px'
          }}
        >
          🏠 Retour à l'accueil
        </Link>
      </div>
    );
  }

  return (
    <div style={{ 
      width: '100%', 
      padding: '20px',
      backgroundColor: '#f8f9fa',
      minHeight: '100vh'
    }}>
      {/* Breadcrumb */}
      <nav style={{ 
        marginBottom: '30px',
        padding: '10px 0',
        borderBottom: '1px solid #e0e0e0'
      }}>
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          fontSize: '0.9em',
          color: '#666'
        }}>
          <Link 
            to="/" 
            style={{ 
              color: '#007bff', 
              textDecoration: 'none' 
            }}
          >
            🏠 Accueil
          </Link>
          <span>→</span>
          <span style={{ color: '#333', fontWeight: 'bold' }}>
            📁 {category.name}
          </span>
        </div>
      </nav>

      {/* En-tête de la catégorie */}
      <header style={{ marginBottom: '30px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '15px', marginBottom: '15px' }}>
          <div style={{ fontSize: '3em' }}>📂</div>
          <div>
            <h1 style={{ 
              margin: '0',
              fontSize: '2.5em', 
              color: '#333',
              fontWeight: 'bold'
            }}>
              {category.name}
            </h1>
            <div style={{ 
              fontSize: '1em', 
              color: '#666',
              marginTop: '5px'
            }}>
              {articles.length} article{articles.length !== 1 ? 's' : ''} dans cette catégorie
            </div>
          </div>
        </div>
        
        {category.description && (
          <p style={{ 
            fontSize: '1.1em', 
            color: '#555',
            lineHeight: '1.6',
            margin: '0',
            padding: '15px',
            backgroundColor: 'white',
            borderRadius: '8px',
            border: '1px solid #e0e0e0'
          }}>
            {category.description}
          </p>
        )}
      </header>

      {/* Liste des articles */}
      <main>
        {articles.length > 0 ? (
          <>
            <div style={{ marginBottom: '20px' }}>
              <h2 style={{ 
                fontSize: '1.8em', 
                color: '#333',
                margin: '0'
              }}>
                📝 Articles récents
              </h2>
            </div>
            
            <div>
              {articles.map((article) => (
                <ArticleCard
                  key={article.id}
                  article={article}
                  onClick={() => handleArticleClick(article)}
                />
              ))}
            </div>
            
            {/* Info pagination (pour plus tard) */}
            {pagination.totalElements > pagination.size && (
              <div style={{
                textAlign: 'center',
                padding: '20px',
                color: '#666',
                backgroundColor: 'white',
                borderRadius: '8px',
                border: '1px solid #e0e0e0',
                marginTop: '20px'
              }}>
                <p style={{ margin: '0' }}>
                  Affichage de {Math.min(pagination.size, articles.length)} articles sur {pagination.totalElements}
                </p>
                <p style={{ margin: '5px 0 0 0', fontSize: '0.9em' }}>
                  (Pagination à implémenter dans une version future)
                </p>
              </div>
            )}
          </>
        ) : (
          <div style={{
            textAlign: 'center',
            padding: '60px 20px',
            backgroundColor: 'white',
            borderRadius: '8px',
            border: '1px solid #e0e0e0'
          }}>
            <div style={{ fontSize: '4em', marginBottom: '20px' }}>📭</div>
            <h3 style={{ color: '#666', marginBottom: '10px' }}>
              Aucun article dans cette catégorie
            </h3>
            <p style={{ color: '#999', margin: '0 0 20px 0' }}>
              Il n'y a pas encore d'articles publiés dans la catégorie "{category.name}".
            </p>
            <Link 
              to="/"
              style={{
                display: 'inline-block',
                padding: '12px 24px',
                backgroundColor: '#007bff',
                color: 'white',
                textDecoration: 'none',
                borderRadius: '4px'
              }}
            >
              🏠 Retour à l'accueil
            </Link>
          </div>
        )}
      </main>

      {/* Footer */}
      <footer style={{ 
        marginTop: '40px', 
        textAlign: 'center',
        padding: '20px',
        borderTop: '1px solid #e0e0e0',
        color: '#666',
        fontSize: '0.9em'
      }}>
        <p>📁 Catégorie : {category.name} • Slug : {category.slug}</p>
        <p>Étape 4 : Navigation et catégories</p>
      </footer>
    </div>
  );
}

export default CategoryPage; 