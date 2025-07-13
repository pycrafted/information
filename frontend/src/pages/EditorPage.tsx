import { useState, useEffect } from 'react';
import { useAuth, usePermissions } from '../contexts/AuthContext';
import { 
  type Article, 
  type ArticleFormData,
  getMyArticles,
  createArticle,
  updateArticle,
  publishArticle,
  archiveArticle,
  deleteArticle,
  getStatusBadgeStyle,
  getStatusLabel,
  formatDate
} from '../services/articleService';
import ArticleForm from '../components/ArticleForm';

type ViewMode = 'list' | 'create' | 'edit';

function EditorPage() {
  const { user } = useAuth();
  const { isEditor, isAdmin, canDeleteArticles } = usePermissions();
  
  const [viewMode, setViewMode] = useState<ViewMode>('list');
  const [articles, setArticles] = useState<Article[]>([]);
  const [currentArticle, setCurrentArticle] = useState<Article | null>(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [filter, setFilter] = useState<'ALL' | 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'>('ALL');

  // Charger les articles
  useEffect(() => {
    fetchMyArticles();
  }, [filter]);

  const fetchMyArticles = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Note: Pour l'instant, on récupère tous les articles publiés et on filtre côté client
      // Il faudra adapter quand l'endpoint /my-articles sera disponible
      const response = await getMyArticles(
        filter === 'ALL' ? undefined : filter,
        0,
        50
      );
      
      setArticles(response.content);
      console.log(`✅ ${response.content.length} articles chargés`);
    } catch (err: any) {
      console.error('❌ Erreur lors du chargement des articles:', err);
      setError(err.message || 'Erreur lors du chargement des articles');
    } finally {
      setLoading(false);
    }
  };

  // Créer un article
  const handleCreateArticle = async (formData: ArticleFormData) => {
    try {
      setActionLoading('create');
      setError(null);
      
      const newArticle = await createArticle(formData);
      
      setSuccess(`Article "${newArticle.title}" créé avec succès !`);
      setViewMode('list');
      await fetchMyArticles();
    } catch (err: any) {
      setError(err.message || 'Erreur lors de la création de l\'article');
    } finally {
      setActionLoading(null);
    }
  };

  // Modifier un article
  const handleUpdateArticle = async (formData: ArticleFormData) => {
    if (!currentArticle) return;

    try {
      setActionLoading('update');
      setError(null);
      
      const updatedArticle = await updateArticle(currentArticle.id, formData, currentArticle);
      
      setSuccess(`Article "${updatedArticle.title}" modifié avec succès !`);
      setViewMode('list');
      setCurrentArticle(null);
      await fetchMyArticles();
    } catch (err: any) {
      setError(err.message || 'Erreur lors de la modification de l\'article');
    } finally {
      setActionLoading(null);
    }
  };

  // Publier un article
  const handlePublishArticle = async (article: Article) => {
    if (!window.confirm(`Êtes-vous sûr de vouloir publier l'article "${article.title}" ?`)) {
      return;
    }

    try {
      setActionLoading(`publish-${article.id}`);
      setError(null);
      
      await publishArticle(article.id);
      
      setSuccess(`Article "${article.title}" publié avec succès !`);
      await fetchMyArticles();
    } catch (err: any) {
      setError(err.message || 'Erreur lors de la publication');
    } finally {
      setActionLoading(null);
    }
  };

  // Archiver un article
  const handleArchiveArticle = async (article: Article) => {
    if (!window.confirm(`Êtes-vous sûr de vouloir archiver l'article "${article.title}" ?`)) {
      return;
    }

    try {
      setActionLoading(`archive-${article.id}`);
      setError(null);
      
      await archiveArticle(article.id);
      
      setSuccess(`Article "${article.title}" archivé avec succès !`);
      await fetchMyArticles();
    } catch (err: any) {
      setError(err.message || 'Erreur lors de l\'archivage');
    } finally {
      setActionLoading(null);
    }
  };

  // Supprimer un article (admin seulement)
  const handleDeleteArticle = async (article: Article) => {
    if (!window.confirm(`⚠️ ATTENTION ⚠️\n\nÊtes-vous sûr de vouloir supprimer DÉFINITIVEMENT l'article "${article.title}" ?\n\nCette action est irréversible !`)) {
      return;
    }

    try {
      setActionLoading(`delete-${article.id}`);
      setError(null);
      
      await deleteArticle(article.id);
      
      setSuccess(`Article "${article.title}" supprimé définitivement !`);
      await fetchMyArticles();
    } catch (err: any) {
      setError(err.message || 'Erreur lors de la suppression');
    } finally {
      setActionLoading(null);
    }
  };

  // Effacer les messages
  const clearMessages = () => {
    setError(null);
    setSuccess(null);
  };

  // Interface selon le mode
  if (viewMode === 'create') {
    return (
      <div style={{ width: '100%', padding: '20px' }}>
        <ArticleForm
          onSubmit={handleCreateArticle}
          onCancel={() => {
            setViewMode('list');
            clearMessages();
          }}
          isLoading={actionLoading === 'create'}
          submitLabel="📝 Créer l'article"
        />
      </div>
    );
  }

  if (viewMode === 'edit' && currentArticle) {
    return (
      <div style={{ width: '100%', padding: '20px' }}>
        <ArticleForm
          article={currentArticle}
          onSubmit={handleUpdateArticle}
          onCancel={() => {
            setViewMode('list');
            setCurrentArticle(null);
            clearMessages();
          }}
          isLoading={actionLoading === 'update'}
          submitLabel="✅ Sauvegarder les modifications"
        />
      </div>
    );
  }

  // Vue liste (par défaut)
  return (
    <div style={{ width: '100%', padding: '20px' }}>
      {/* En-tête */}
      <div style={{ marginBottom: '30px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '15px' }}>
          <div>
            <h1 style={{ fontSize: '2.2em', color: '#333', margin: '0 0 5px 0' }}>
              ✏️ Mes Articles
            </h1>
            <p style={{ color: '#666', margin: '0' }}>
              Gérez vos articles • Connecté en tant que <strong>{user?.username}</strong> ({user?.role})
            </p>
          </div>
          
          <button
            onClick={() => {
              setViewMode('create');
              clearMessages();
            }}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '12px 20px',
              backgroundColor: '#28a745',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              fontSize: '1em',
              cursor: 'pointer',
              transition: 'background-color 0.2s ease'
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.backgroundColor = '#218838';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.backgroundColor = '#28a745';
            }}
          >
            📝 Nouvel Article
          </button>
        </div>

        {/* Filtres */}
        <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
          <span style={{ color: '#666', fontSize: '0.9em' }}>Filtrer :</span>
          {(['ALL', 'DRAFT', 'PUBLISHED', 'ARCHIVED'] as const).map((filterOption) => (
            <button
              key={filterOption}
              onClick={() => setFilter(filterOption)}
              style={{
                padding: '6px 12px',
                backgroundColor: filter === filterOption ? '#007bff' : 'transparent',
                color: filter === filterOption ? 'white' : '#007bff',
                border: '1px solid #007bff',
                borderRadius: '4px',
                fontSize: '0.8em',
                cursor: 'pointer',
                transition: 'all 0.2s ease'
              }}
            >
              {filterOption === 'ALL' ? '📄 Tous' : getStatusLabel(filterOption)}
            </button>
          ))}
        </div>
      </div>

      {/* Messages */}
      {error && (
        <div style={{
          backgroundColor: '#f8d7da',
          border: '1px solid #f5c6cb',
          color: '#721c24',
          padding: '12px',
          borderRadius: '4px',
          marginBottom: '20px'
        }}>
          <strong>❌ Erreur :</strong> {error}
          <button
            onClick={clearMessages}
            style={{
              float: 'right',
              background: 'none',
              border: 'none',
              color: '#721c24',
              cursor: 'pointer'
            }}
          >
            ✕
          </button>
        </div>
      )}

      {success && (
        <div style={{
          backgroundColor: '#d4edda',
          border: '1px solid #c3e6cb',
          color: '#155724',
          padding: '12px',
          borderRadius: '4px',
          marginBottom: '20px'
        }}>
          <strong>✅ Succès :</strong> {success}
          <button
            onClick={clearMessages}
            style={{
              float: 'right',
              background: 'none',
              border: 'none',
              color: '#155724',
              cursor: 'pointer'
            }}
          >
            ✕
          </button>
        </div>
      )}

      {/* Liste des articles */}
      {loading ? (
        <div style={{ textAlign: 'center', padding: '40px' }}>
          <div style={{ fontSize: '3em', marginBottom: '15px' }}>🔄</div>
          <h3 style={{ color: '#666' }}>Chargement de vos articles...</h3>
        </div>
      ) : articles.length === 0 ? (
        <div style={{
          textAlign: 'center',
          padding: '60px',
          backgroundColor: 'white',
          borderRadius: '8px',
          border: '1px solid #e0e0e0'
        }}>
          <div style={{ fontSize: '4em', marginBottom: '20px' }}>📝</div>
          <h3 style={{ color: '#666', marginBottom: '15px' }}>
            {filter === 'ALL' ? 'Aucun article trouvé' : `Aucun article ${getStatusLabel(filter).toLowerCase()}`}
          </h3>
          <p style={{ color: '#999', marginBottom: '25px' }}>
            Commencez par créer votre premier article !
          </p>
          <button
            onClick={() => setViewMode('create')}
            style={{
              padding: '12px 24px',
              backgroundColor: '#28a745',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              fontSize: '1em',
              cursor: 'pointer'
            }}
          >
            📝 Créer mon premier article
          </button>
        </div>
      ) : (
        <div style={{ display: 'grid', gap: '15px' }}>
          {articles.map((article) => (
            <div
              key={article.id}
              style={{
                backgroundColor: 'white',
                border: '1px solid #e0e0e0',
                borderRadius: '8px',
                padding: '20px',
                boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '15px' }}>
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
                    <h3 style={{ margin: '0', fontSize: '1.3em', color: '#333' }}>
                      {article.title}
                    </h3>
                    <span
                      style={{
                        padding: '4px 8px',
                        borderRadius: '4px',
                        fontSize: '0.7em',
                        fontWeight: 'bold',
                        ...getStatusBadgeStyle(article.publishedAt ? 'PUBLISHED' : 'DRAFT')
                      }}
                    >
                      {getStatusLabel(article.publishedAt ? 'PUBLISHED' : 'DRAFT')}
                    </span>
                  </div>
                  
                  <p style={{ 
                    color: '#666', 
                    margin: '0 0 10px 0', 
                    lineHeight: '1.5',
                    maxHeight: '3em',
                    overflow: 'hidden'
                  }}>
                    {article.summary || article.content.substring(0, 150) + '...'}
                  </p>
                  
                  <div style={{ fontSize: '0.8em', color: '#999' }}>
                    📁 {article.categoryName} • 📅 {formatDate(article.publishedAt || article.createdAt || '')} • 🆔 {article.id.substring(0, 8)}
                  </div>
                </div>

                {/* Actions */}
                <div style={{ display: 'flex', gap: '8px', flexShrink: 0, marginLeft: '20px' }}>
                  {/* Modifier - seulement pour les brouillons */}
                  {!article.publishedAt ? (
                    <button
                      onClick={() => {
                        setCurrentArticle(article);
                        setViewMode('edit');
                        clearMessages();
                      }}
                      disabled={!!actionLoading}
                      style={{
                        padding: '6px 12px',
                        backgroundColor: '#007bff',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        fontSize: '0.8em',
                        cursor: actionLoading ? 'not-allowed' : 'pointer'
                      }}
                    >
                      ✏️ Modifier
                    </button>
                  ) : (
                    <div
                      style={{
                        padding: '6px 12px',
                        backgroundColor: '#6c757d',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        fontSize: '0.8em',
                        cursor: 'not-allowed',
                        opacity: 0.6
                      }}
                      title="Les articles publiés ne peuvent pas être modifiés"
                    >
                      🔒 Non modifiable
                    </div>
                  )}

                  {/* Publier */}
                  {/* Si publishedAt est null/undefined, c'est un brouillon */}
                  {!article.publishedAt && (
                    <button
                      onClick={() => handlePublishArticle(article)}
                      disabled={actionLoading === `publish-${article.id}` || !!actionLoading}
                      style={{
                        padding: '6px 12px',
                        backgroundColor: '#28a745',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        fontSize: '0.8em',
                        cursor: actionLoading ? 'not-allowed' : 'pointer'
                      }}
                    >
                      {actionLoading === `publish-${article.id}` ? '🔄' : '📢'} Publier
                    </button>
                  )}

                  {/* Archiver */}
                  {/* Si publishedAt existe, c'est un article publié */}
                  {article.publishedAt && (
                    <button
                      onClick={() => handleArchiveArticle(article)}
                      disabled={actionLoading === `archive-${article.id}` || !!actionLoading}
                      style={{
                        padding: '6px 12px',
                        backgroundColor: '#6c757d',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        fontSize: '0.8em',
                        cursor: actionLoading ? 'not-allowed' : 'pointer'
                      }}
                    >
                      {actionLoading === `archive-${article.id}` ? '🔄' : '📦'} Archiver
                    </button>
                  )}

                  {/* Supprimer (admin seulement) */}
                  {canDeleteArticles && (
                    <button
                      onClick={() => handleDeleteArticle(article)}
                      disabled={actionLoading === `delete-${article.id}` || !!actionLoading}
                      style={{
                        padding: '6px 12px',
                        backgroundColor: '#dc3545',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        fontSize: '0.8em',
                        cursor: actionLoading ? 'not-allowed' : 'pointer'
                      }}
                    >
                      {actionLoading === `delete-${article.id}` ? '🔄' : '🗑️'} Supprimer
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Info et règles métier */}
      <div style={{
        marginTop: '40px',
        textAlign: 'center',
        padding: '20px',
        backgroundColor: '#f8f9fa',
        borderRadius: '8px',
        fontSize: '0.9em',
        color: '#666'
      }}>
        <p style={{ margin: '0 0 10px 0' }}>
          🚀 Étape 6 : Gestion des articles • 
          {isEditor && ' ✏️ Éditeur : Créer, modifier, publier articles •'}
          {isAdmin && ' 👑 Admin : + Supprimer articles •'}
          📊 {articles.length} article{articles.length !== 1 ? 's' : ''} affiché{articles.length !== 1 ? 's' : ''}
        </p>
        <div style={{ 
          fontSize: '0.8em', 
          color: '#888',
          fontStyle: 'italic',
          borderTop: '1px solid #e0e0e0',
          paddingTop: '10px',
          marginTop: '10px'
        }}>
          📝 <strong>Règles métier :</strong> Les brouillons peuvent être modifiés et publiés. 
          Les articles publiés peuvent être archivés mais ne sont plus modifiables.
        </div>
      </div>
    </div>
  );
}

export default EditorPage; 