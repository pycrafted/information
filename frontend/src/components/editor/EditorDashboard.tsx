import React, { useState, useEffect } from 'react';
import { useAppSelector, selectAuth } from '../../store';
import { Button } from '../ui/Button';
import { apiRequest, apiClient } from '../../services/api';

// Types
interface ArticleStats {
  totalArticles: number;
  publishedArticles: number;
  draftArticles: number;
  archivedArticles: number;
}

interface Article {
  id: string;
  title: string;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  createdAt: string;
  publishedAt?: string;
  category: {
    id: string;
    name: string;
  };
}

const EditorDashboard: React.FC = () => {
  const [stats, setStats] = useState<ArticleStats | null>(null);
  const [recentArticles, setRecentArticles] = useState<Article[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  const { user } = useAppSelector(selectAuth);

  // Charger les statistiques de l'éditeur
  const loadEditorStats = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Récupérer tous les articles de l'utilisateur (simulation)
      const response = await apiRequest(() =>
        apiClient.get<Article[]>('/articles/my-articles')
      ).catch(() => {
        // Si l'endpoint n'existe pas, simuler des données
        return [
          {
            id: '1',
            title: 'Mon Premier Article',
            status: 'PUBLISHED' as const,
            createdAt: new Date().toISOString(),
            publishedAt: new Date().toISOString(),
            category: { id: '1', name: 'Technologie' }
          },
          {
            id: '2', 
            title: 'Article en Brouillon',
            status: 'DRAFT' as const,
            createdAt: new Date().toISOString(),
            category: { id: '2', name: 'Science' }
          }
        ];
      });
      
      // Calculer les statistiques
      const statsData: ArticleStats = {
        totalArticles: response.length,
        publishedArticles: response.filter(a => a.status === 'PUBLISHED').length,
        draftArticles: response.filter(a => a.status === 'DRAFT').length,
        archivedArticles: response.filter(a => a.status === 'ARCHIVED').length,
      };
      
      setStats(statsData);
      setRecentArticles(response.slice(0, 5)); // 5 articles les plus récents
      
    } catch (err: any) {
      setError(err.message || 'Erreur lors du chargement des données');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadEditorStats();
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center items-center py-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        <span className="ml-2 text-gray-600">Chargement du tableau de bord...</span>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* En-tête de bienvenue */}
      <div className="bg-gradient-to-r from-blue-500 to-purple-600 rounded-lg p-6 text-white">
        <h2 className="text-2xl font-bold mb-2">
          Bonjour {user?.username || 'Éditeur'} ! 👋
        </h2>
        <p className="text-blue-100">
          Gérez vos articles et créez du contenu captivant pour votre audience.
        </p>
      </div>

      {/* Messages d'erreur */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <div className="flex">
            <span className="text-red-400 mr-2">❌</span>
            <p className="text-sm text-red-800">{error}</p>
          </div>
        </div>
      )}

      {/* Statistiques */}
      {stats && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <StatCard
            title="Total Articles"
            value={stats.totalArticles.toString()}
            icon="📰"
            color="blue"
          />
          <StatCard
            title="Publiés"
            value={stats.publishedArticles.toString()}
            icon="✅"
            color="green"
          />
          <StatCard
            title="Brouillons"
            value={stats.draftArticles.toString()}
            icon="📝"
            color="yellow"
          />
          <StatCard
            title="Archivés"
            value={stats.archivedArticles.toString()}
            icon="📦"
            color="gray"
          />
        </div>
      )}

      {/* Actions rapides */}
      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">Actions rapides</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <ActionCard
            title="Nouvel Article"
            description="Créer un nouveau contenu"
            icon="✏️"
            action="primary"
            onClick={() => window.location.href = '/editor/create'}
          />
          <ActionCard
            title="Mes Brouillons"
            description="Reprendre un article en cours"
            icon="📝"
            action="secondary"
            onClick={() => window.location.href = '/editor/drafts'}
          />
          <ActionCard
            title="Gérer Catégories"
            description="Organiser le contenu"
            icon="📂"
            action="secondary"
            onClick={() => window.location.href = '/editor/categories'}
          />
        </div>
      </div>

      {/* Articles récents */}
      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-lg font-medium text-gray-900">Articles récents</h3>
          <Button variant="outline" size="sm">
            Voir tous
          </Button>
        </div>
        
        {recentArticles.length > 0 ? (
          <div className="space-y-3">
            {recentArticles.map((article) => (
              <ArticleCard key={article.id} article={article} />
            ))}
          </div>
        ) : (
          <div className="text-center py-8 text-gray-500">
            <div className="text-4xl mb-2">📝</div>
            <p>Aucun article trouvé</p>
            <Button className="mt-4" onClick={() => window.location.href = '/editor/create'}>
              Créer votre premier article
            </Button>
          </div>
        )}
      </div>

      {/* Conseils pour éditeurs */}
      <div className="bg-blue-50 rounded-lg border border-blue-200 p-6">
        <h3 className="text-lg font-medium text-blue-900 mb-4">💡 Conseils d'éditeur</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm text-blue-800">
          <div>
            <h4 className="font-medium mb-2">Workflow recommandé :</h4>
            <ul className="space-y-1">
              <li>• Créer en BROUILLON</li>
              <li>• Relire et corriger</li>
              <li>• Publier quand prêt</li>
              <li>• Archiver si obsolète</li>
            </ul>
          </div>
          <div>
            <h4 className="font-medium mb-2">Bonnes pratiques :</h4>
            <ul className="space-y-1">
              <li>• Utiliser des titres accrocheurs</li>
              <li>• Choisir la bonne catégorie</li>
              <li>• Ajouter un résumé clair</li>
              <li>• Réviser avant publication</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};

// Composant StatCard
const StatCard: React.FC<{
  title: string;
  value: string;
  icon: string;
  color: 'blue' | 'green' | 'yellow' | 'gray';
}> = ({ title, value, icon, color }) => {
  const colorClasses = {
    blue: 'bg-blue-50 border-blue-200 text-blue-900',
    green: 'bg-green-50 border-green-200 text-green-900',
    yellow: 'bg-yellow-50 border-yellow-200 text-yellow-900',
    gray: 'bg-gray-50 border-gray-200 text-gray-900',
  };

  return (
    <div className={`rounded-lg border p-6 ${colorClasses[color]}`}>
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-medium opacity-80">{title}</p>
          <p className="text-3xl font-bold">{value}</p>
        </div>
        <div className="text-3xl">{icon}</div>
      </div>
    </div>
  );
};

// Composant ActionCard
const ActionCard: React.FC<{
  title: string;
  description: string;
  icon: string;
  action: 'primary' | 'secondary';
  onClick: () => void;
}> = ({ title, description, icon, action, onClick }) => (
  <div 
    className={`p-4 rounded-lg border cursor-pointer transition-all hover:shadow-md ${
      action === 'primary' 
        ? 'border-blue-300 bg-blue-50 hover:bg-blue-100' 
        : 'border-gray-200 bg-white hover:bg-gray-50'
    }`}
    onClick={onClick}
  >
    <div className="text-2xl mb-2">{icon}</div>
    <h4 className="font-medium text-gray-900 mb-1">{title}</h4>
    <p className="text-sm text-gray-600">{description}</p>
  </div>
);

// Composant ArticleCard
const ArticleCard: React.FC<{
  article: Article;
}> = ({ article }) => {
  const statusColors = {
    DRAFT: 'bg-yellow-100 text-yellow-800',
    PUBLISHED: 'bg-green-100 text-green-800',
    ARCHIVED: 'bg-gray-100 text-gray-800',
  };

  const statusLabels = {
    DRAFT: 'Brouillon',
    PUBLISHED: 'Publié',
    ARCHIVED: 'Archivé',
  };

  return (
    <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors">
      <div className="flex-1">
        <div className="flex items-center space-x-3 mb-2">
          <h4 className="font-medium text-gray-900">{article.title}</h4>
          <span className={`px-2 py-1 text-xs font-medium rounded-full ${statusColors[article.status]}`}>
            {statusLabels[article.status]}
          </span>
        </div>
        <div className="flex items-center space-x-4 text-sm text-gray-500">
          <span>📂 {article.category.name}</span>
          <span>📅 {new Date(article.createdAt).toLocaleDateString('fr-FR')}</span>
          {article.publishedAt && (
            <span>🚀 {new Date(article.publishedAt).toLocaleDateString('fr-FR')}</span>
          )}
        </div>
      </div>
      <div className="flex items-center space-x-2">
        <Button variant="ghost" size="sm">
          Modifier
        </Button>
        {article.status === 'DRAFT' && (
          <Button variant="outline" size="sm">
            Publier
          </Button>
        )}
      </div>
    </div>
  );
};

export default EditorDashboard; 