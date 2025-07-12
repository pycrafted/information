import React, { useState } from 'react'
import { useAppSelector } from '../store'
import { selectAuthUser, selectIsAuthenticated, selectUserRole } from '../store/authSlice'
import { Navigate } from 'react-router-dom'
import UserManagement from '../components/admin/UserManagement'
import { Button } from '../components/ui/Button'

/**
 * Page d'administration complète
 * 
 * Fonctionnalités :
 * - Gestion des utilisateurs (CRUD complet)
 * - Dashboard avec statistiques
 * - Audit logs
 * - Configuration système
 * 
 * Accessible uniquement aux ADMINISTRATEURS
 */

type TabType = 'users' | 'dashboard' | 'audit' | 'settings';

export const AdminPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<TabType>('dashboard')
  const user = useAppSelector(selectAuthUser)
  const isAuthenticated = useAppSelector(selectIsAuthenticated)
  const userRole = useAppSelector(selectUserRole)

  // Redirection si pas admin
  if (!isAuthenticated || userRole !== 'ADMINISTRATEUR') {
    return <Navigate to="/login" replace />
  }

  const tabs = [
    { id: 'dashboard' as TabType, name: 'Tableau de bord', icon: '📊' },
    { id: 'users' as TabType, name: 'Utilisateurs', icon: '👥' },
    { id: 'audit' as TabType, name: 'Audit', icon: '📝' },
    { id: 'settings' as TabType, name: 'Paramètres', icon: '⚙️' },
  ]

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        
        {/* En-tête */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Administration</h1>
          <p className="text-gray-600 mt-2">
            Panneau de contrôle pour la gestion de la plateforme
          </p>
        </div>

        {/* Navigation par onglets */}
        <div className="mb-8">
          <div className="border-b border-gray-200">
            <nav className="-mb-px flex space-x-8">
              {tabs.map((tab) => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`py-2 px-1 border-b-2 font-medium text-sm ${
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
          {activeTab === 'dashboard' && <DashboardContent />}
          {activeTab === 'users' && <UserManagement />}
          {activeTab === 'audit' && <AuditContent />}
          {activeTab === 'settings' && <SettingsContent />}
        </div>
      </div>
    </div>
  )
}

// Composant Dashboard
const DashboardContent: React.FC = () => (
  <div className="p-6">
    <h2 className="text-2xl font-bold text-gray-900 mb-6">Tableau de bord</h2>
    
    {/* Statistiques */}
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <StatCard
        title="Total Utilisateurs"
        value="156"
        icon="👥"
        trend="+12%"
        trendPositive={true}
      />
      <StatCard
        title="Articles Publiés"
        value="1,248"
        icon="📰"
        trend="+8%"
        trendPositive={true}
      />
      <StatCard
        title="Catégories"
        value="24"
        icon="📂"
        trend="+2"
        trendPositive={true}
      />
      <StatCard
        title="Connexions Aujourd'hui"
        value="89"
        icon="🔐"
        trend="-3%"
        trendPositive={false}
      />
    </div>

    {/* Actions rapides */}
    <div className="mb-8">
      <h3 className="text-lg font-medium text-gray-900 mb-4">Actions rapides</h3>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <QuickActionCard
          title="Nouvel Utilisateur"
          description="Créer un nouveau compte utilisateur"
          icon="➕"
          onClick={() => console.log('Créer utilisateur')}
        />
        <QuickActionCard
          title="Voir les Logs"
          description="Consulter les journaux d'audit"
          icon="📋"
          onClick={() => console.log('Voir logs')}
        />
        <QuickActionCard
          title="Paramètres"
          description="Configurer la plateforme"
          icon="⚙️"
          onClick={() => console.log('Paramètres')}
        />
      </div>
    </div>

    {/* Graphique de tendance (placeholder) */}
    <div className="mb-6">
      <h3 className="text-lg font-medium text-gray-900 mb-4">Activité récente</h3>
      <div className="bg-gray-100 rounded-lg h-64 flex items-center justify-center">
        <div className="text-center text-gray-500">
          <div className="text-4xl mb-2">📈</div>
          <p>Graphique d'activité</p>
          <p className="text-sm">(À implémenter avec Chart.js)</p>
        </div>
      </div>
    </div>
  </div>
)

// Composant de statistique
const StatCard: React.FC<{
  title: string
  value: string
  icon: string
  trend: string
  trendPositive: boolean
}> = ({ title, value, icon, trend, trendPositive }) => (
  <div className="bg-white p-6 rounded-lg border border-gray-200 hover:shadow-md transition-shadow">
    <div className="flex items-center justify-between">
      <div>
        <p className="text-sm font-medium text-gray-600">{title}</p>
        <p className="text-3xl font-bold text-gray-900">{value}</p>
      </div>
      <div className="text-3xl">{icon}</div>
    </div>
    <div className="mt-4">
      <span className={`text-sm ${trendPositive ? 'text-green-600' : 'text-red-600'}`}>
        {trend}
      </span>
      <span className="text-sm text-gray-500 ml-1">vs mois dernier</span>
    </div>
  </div>
)

// Composant d'action rapide
const QuickActionCard: React.FC<{
  title: string
  description: string
  icon: string
  onClick: () => void
}> = ({ title, description, icon, onClick }) => (
  <div 
    className="bg-white p-6 rounded-lg border border-gray-200 hover:shadow-md transition-all cursor-pointer hover:border-blue-300"
    onClick={onClick}
  >
    <div className="text-3xl mb-4">{icon}</div>
    <h4 className="text-lg font-medium text-gray-900 mb-2">{title}</h4>
    <p className="text-gray-600 text-sm">{description}</p>
  </div>
)

// Composant Audit
const AuditContent: React.FC = () => (
  <div className="p-6">
    <h2 className="text-2xl font-bold text-gray-900 mb-6">Journaux d'audit</h2>
    
    <div className="bg-gray-100 rounded-lg p-8 text-center">
      <div className="text-4xl mb-4">📝</div>
      <h3 className="text-lg font-medium text-gray-900 mb-2">Logs d'audit</h3>
      <p className="text-gray-600 mb-4">
        Fonctionnalité à implémenter : affichage des logs d'activité
      </p>
      <Button variant="outline">
        Implémenter les logs
      </Button>
    </div>
  </div>
)

// Composant Paramètres
const SettingsContent: React.FC = () => (
  <div className="p-6">
    <h2 className="text-2xl font-bold text-gray-900 mb-6">Paramètres</h2>
    
    <div className="space-y-6">
      {/* Paramètres généraux */}
      <div className="bg-gray-50 rounded-lg p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">Configuration générale</h3>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Nom de la plateforme
            </label>
            <input
              type="text"
              defaultValue="News Platform"
              className="w-full border border-gray-300 rounded-md px-3 py-2"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Email administrateur
            </label>
            <input
              type="email"
              defaultValue="admin@newsplatform.com"
              className="w-full border border-gray-300 rounded-md px-3 py-2"
            />
          </div>
        </div>
        <Button className="mt-4">
          Sauvegarder
        </Button>
      </div>

      {/* Paramètres de sécurité */}
      <div className="bg-gray-50 rounded-lg p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">Sécurité</h3>
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="font-medium">Authentification à deux facteurs</p>
              <p className="text-sm text-gray-600">Activer la 2FA pour tous les admins</p>
            </div>
            <input type="checkbox" className="h-4 w-4" />
          </div>
          <div className="flex items-center justify-between">
            <div>
              <p className="font-medium">Audit des connexions</p>
              <p className="text-sm text-gray-600">Enregistrer toutes les tentatives de connexion</p>
            </div>
            <input type="checkbox" defaultChecked className="h-4 w-4" />
          </div>
        </div>
      </div>
    </div>
  </div>
)

export default AdminPage 
