import React, { useState } from 'react'
import { useAppSelector, selectAuth } from '../store'
import { Navigate } from 'react-router-dom'
import EditorDashboard from '../components/editor/EditorDashboard'
import { Button } from '../components/ui/Button'

/**
 * Page d'édition complète pour les éditeurs
 * 
 * Fonctionnalités :
 * - Dashboard avec statistiques
 * - Gestion des articles (CRUD complet)
 * - Workflow éditorial (DRAFT → PUBLISHED → ARCHIVED)
 * - Gestion des catégories
 * 
 * Accessible aux EDITEUR et ADMINISTRATEUR
 */

type TabType = 'dashboard' | 'articles' | 'drafts' | 'create' | 'categories';

export const EditorPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<TabType>('dashboard')
  const { user, isAuthenticated } = useAppSelector(selectAuth)

  // Redirection si pas éditeur ou admin
  if (!isAuthenticated || !['EDITEUR', 'ADMINISTRATEUR'].includes(user?.role || '')) {
    return <Navigate to="/login" replace />
  }

  const tabs = [
    { id: 'dashboard' as TabType, name: 'Tableau de bord', icon: '📊' },
    { id: 'articles' as TabType, name: 'Mes Articles', icon: '📰' },
    { id: 'drafts' as TabType, name: 'Brouillons', icon: '📝' },
    { id: 'create' as TabType, name: 'Créer', icon: '✏️' },
    { id: 'categories' as TabType, name: 'Catégories', icon: '📂' },
  ]

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        
        {/* En-tête */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Espace Éditeur</h1>
          <p className="text-gray-600 mt-2">
            Créez et gérez vos contenus éditoriaux
          </p>
        </div>

        {/* Navigation par onglets */}
        <div className="mb-8">
          <div className="border-b border-gray-200">
            <nav className="-mb-px flex space-x-8 overflow-x-auto">
              {tabs.map((tab) => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`py-2 px-1 border-b-2 font-medium text-sm whitespace-nowrap ${
                    activeTab === tab.id
                      ? 'border-blue-500 text-blue-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  }`}
                >
                  <span className="mr-2">{tab.icon}</span>
                  {tab.name}
                </button>
              ))}
            </nav>
          </div>
        </div>

        {/* Contenu des onglets */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200">
          {activeTab === 'dashboard' && <EditorDashboard />}
          {activeTab === 'articles' && <ArticlesContent />}
          {activeTab === 'drafts' && <DraftsContent />}
          {activeTab === 'create' && <CreateArticleContent />}
          {activeTab === 'categories' && <CategoriesContent />}
        </div>
      </div>
    </div>
  )
}

// Composant Articles
const ArticlesContent: React.FC = () => (
  <div className="p-6">
    <div className="flex justify-between items-center mb-6">
      <h2 className="text-2xl font-bold text-gray-900">Mes Articles</h2>
      <Button onClick={() => console.log('Nouvel article')}>
        + Nouvel Article
      </Button>
    </div>
    
    {/* Filtres */}
    <div className="flex space-x-4 mb-6">
      <select className="border border-gray-300 rounded-md px-3 py-2">
        <option>Tous les statuts</option>
        <option>Brouillon</option>
        <option>Publié</option>
        <option>Archivé</option>
      </select>
      <select className="border border-gray-300 rounded-md px-3 py-2">
        <option>Toutes les catégories</option>
        <option>Technologie</option>
        <option>Science</option>
        <option>Culture</option>
      </select>
      <input
        type="text"
        placeholder="Rechercher un article..."
        className="border border-gray-300 rounded-md px-3 py-2 flex-1"
      />
    </div>

    {/* Liste des articles */}
    <div className="space-y-4">
      {[
        {
          id: '1',
          title: 'Guide complet de TypeScript en 2024',
          status: 'PUBLISHED',
          category: 'Technologie',
          views: 1247,
          publishedAt: '2024-01-15',
        },
        {
          id: '2',
          title: 'Les nouveautés de React 19',
          status: 'DRAFT',
          category: 'Développement',
          views: 0,
          createdAt: '2024-01-20',
        },
        {
          id: '3',
          title: 'Intelligence Artificielle et éthique',
          status: 'PUBLISHED',
          category: 'Science',
          views: 892,
          publishedAt: '2024-01-10',
        },
      ].map((article) => (
        <ArticleRow key={article.id} article={article} />
      ))}
    </div>

    {/* Pagination */}
    <div className="flex justify-center mt-8">
      <div className="flex space-x-2">
        <Button variant="outline" size="sm">Précédent</Button>
        <Button variant="outline" size="sm">1</Button>
        <Button size="sm">2</Button>
        <Button variant="outline" size="sm">3</Button>
        <Button variant="outline" size="sm">Suivant</Button>
      </div>
    </div>
  </div>
)

// Composant Brouillons
const DraftsContent: React.FC = () => (
  <div className="p-6">
    <h2 className="text-2xl font-bold text-gray-900 mb-6">Brouillons</h2>
    
    <div className="space-y-4">
      {[
        {
          id: '2',
          title: 'Les nouveautés de React 19',
          category: 'Développement',
          lastModified: '2024-01-20',
          progress: 75,
        },
        {
          id: '4',
          title: 'Début d\'article sur l\'IA',
          category: 'Science',
          lastModified: '2024-01-18',
          progress: 25,
        },
      ].map((draft) => (
        <DraftCard key={draft.id} draft={draft} />
      ))}
    </div>

    {/* Empty state si pas de brouillons */}
    <div className="text-center py-12 text-gray-500">
      <div className="text-4xl mb-4">📝</div>
      <h3 className="text-lg font-medium text-gray-900 mb-2">Aucun brouillon</h3>
      <p className="mb-4">Commencez à rédiger votre prochain article</p>
      <Button>Créer un article</Button>
    </div>
  </div>
)

// Composant Création d'article
const CreateArticleContent: React.FC = () => (
  <div className="p-6">
    <h2 className="text-2xl font-bold text-gray-900 mb-6">Créer un Article</h2>
    
    <form className="space-y-6 max-w-4xl">
      {/* Titre */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Titre de l'article *
        </label>
        <input
          type="text"
          placeholder="Un titre accrocheur pour votre article..."
          className="w-full border border-gray-300 rounded-md px-4 py-3 text-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      {/* Slug (URL) */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          URL de l'article (slug)
        </label>
        <div className="flex">
          <span className="inline-flex items-center px-3 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500">
            /article/
          </span>
          <input
            type="text"
            placeholder="guide-typescript-2024"
            className="flex-1 border border-gray-300 rounded-r-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </div>

      {/* Catégorie et statut */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Catégorie *
          </label>
          <select className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500">
            <option>Sélectionner une catégorie</option>
            <option>Technologie</option>
            <option>Science</option>
            <option>Culture</option>
            <option>Sport</option>
          </select>
        </div>
        
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Statut
          </label>
          <select className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500">
            <option value="DRAFT">Brouillon</option>
            <option value="PUBLISHED">Publié</option>
          </select>
        </div>
      </div>

      {/* Résumé */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Résumé de l'article
        </label>
        <textarea
          rows={3}
          placeholder="Un court résumé qui donnera envie de lire votre article..."
          className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      {/* Contenu */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Contenu de l'article *
        </label>
        <div className="border border-gray-300 rounded-md">
          <div className="bg-gray-50 px-4 py-2 border-b border-gray-300">
            <div className="flex space-x-2 text-sm">
              <button type="button" className="p-1 hover:bg-gray-200 rounded">📝</button>
              <button type="button" className="p-1 hover:bg-gray-200 rounded">🔗</button>
              <button type="button" className="p-1 hover:bg-gray-200 rounded">📷</button>
              <button type="button" className="p-1 hover:bg-gray-200 rounded">📋</button>
            </div>
          </div>
          <textarea
            rows={15}
            placeholder="Rédigez votre article ici... Vous pouvez utiliser Markdown pour la mise en forme."
            className="w-full px-4 py-3 border-0 focus:outline-none focus:ring-0 resize-none"
          />
        </div>
        <p className="text-sm text-gray-500 mt-2">
          Support Markdown : **gras**, *italique*, [lien](url), etc.
        </p>
      </div>

      {/* Actions */}
      <div className="flex justify-end space-x-4">
        <Button variant="outline" type="button">
          Prévisualiser
        </Button>
        <Button variant="outline" type="button">
          Sauvegarder en brouillon
        </Button>
        <Button type="submit">
          Publier l'article
        </Button>
      </div>
    </form>
  </div>
)

// Composant Catégories
const CategoriesContent: React.FC = () => (
  <div className="p-6">
    <div className="flex justify-between items-center mb-6">
      <h2 className="text-2xl font-bold text-gray-900">Gestion des Catégories</h2>
      <Button>+ Nouvelle Catégorie</Button>
    </div>
    
    <div className="bg-gray-100 rounded-lg p-8 text-center">
      <div className="text-4xl mb-4">📂</div>
      <h3 className="text-lg font-medium text-gray-900 mb-2">Gestion hiérarchique</h3>
      <p className="text-gray-600 mb-4">
        Interface pour gérer l'arborescence des catégories
      </p>
      <Button variant="outline">
        Implémenter l'arbre des catégories
      </Button>
    </div>
  </div>
)

// Composant ligne d'article
const ArticleRow: React.FC<{
  article: {
    id: string;
    title: string;
    status: string;
    category: string;
    views: number;
    publishedAt?: string;
    createdAt?: string;
  };
}> = ({ article }) => {
  const statusColors = {
    PUBLISHED: 'bg-green-100 text-green-800',
    DRAFT: 'bg-yellow-100 text-yellow-800',
    ARCHIVED: 'bg-gray-100 text-gray-800',
  };

  return (
    <div className="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50">
      <div className="flex-1">
        <div className="flex items-center space-x-3 mb-2">
          <h3 className="font-medium text-gray-900">{article.title}</h3>
          <span className={`px-2 py-1 text-xs font-medium rounded-full ${statusColors[article.status as keyof typeof statusColors]}`}>
            {article.status}
          </span>
        </div>
        <div className="flex items-center space-x-4 text-sm text-gray-500">
          <span>📂 {article.category}</span>
          <span>👁️ {article.views} vues</span>
          <span>📅 {article.publishedAt || article.createdAt}</span>
        </div>
      </div>
      <div className="flex items-center space-x-2">
        <Button variant="ghost" size="sm">Modifier</Button>
        <Button variant="ghost" size="sm">Dupliquer</Button>
        <Button variant="ghost" size="sm">⋮</Button>
      </div>
    </div>
  );
};

// Composant carte de brouillon
const DraftCard: React.FC<{
  draft: {
    id: string;
    title: string;
    category: string;
    lastModified: string;
    progress: number;
  };
}> = ({ draft }) => (
  <div className="border border-gray-200 rounded-lg p-4 hover:bg-gray-50">
    <div className="flex items-center justify-between mb-3">
      <h3 className="font-medium text-gray-900">{draft.title}</h3>
      <span className="text-sm text-gray-500">{draft.lastModified}</span>
    </div>
    
    {/* Barre de progression */}
    <div className="mb-3">
      <div className="flex justify-between text-sm text-gray-600 mb-1">
        <span>Progression</span>
        <span>{draft.progress}%</span>
      </div>
      <div className="w-full bg-gray-200 rounded-full h-2">
        <div 
          className="bg-blue-600 h-2 rounded-full"
          style={{ width: `${draft.progress}%` }}
        />
      </div>
    </div>
    
    <div className="flex items-center justify-between">
      <span className="text-sm text-gray-500">📂 {draft.category}</span>
      <div className="flex space-x-2">
        <Button variant="ghost" size="sm">Continuer</Button>
        <Button variant="outline" size="sm">Publier</Button>
      </div>
    </div>
  </div>
)

export default EditorPage 