import { useState, useEffect } from 'react';
import { useAuth, usePermissions } from '../contexts/AuthContext';
import { 
  type User, 
  type UserFormData,
  type UserStats,
  getUsers,
  createUser,
  updateUser,
  deleteUser,
  getUserStats,
  formatRole,
  formatDate
} from '../services/userService';
import UserForm from '../components/UserForm';

type ViewMode = 'dashboard' | 'users' | 'create' | 'edit';

function AdminPage() {
  const { user } = useAuth();
  const { isAdmin } = usePermissions();
  
  const [viewMode, setViewMode] = useState<ViewMode>('dashboard');
  const [users, setUsers] = useState<User[]>([]);
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [stats, setStats] = useState<UserStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');

  // Redirection si non admin
  useEffect(() => {
    if (!isAdmin) {
      console.log('🚫 Accès admin refusé - redirection nécessaire');
    }
  }, [isAdmin]);

  // Charger les données
  useEffect(() => {
    if (isAdmin) {
      fetchData();
    }
  }, [isAdmin]);

  const fetchData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const [usersData, statsData] = await Promise.all([
        getUsers(),
        getUserStats()
      ]);
      
      setUsers(usersData);
      setStats(statsData);
      console.log(`✅ ${usersData.length} utilisateurs et statistiques chargés`);
    } catch (err: any) {
      console.error('❌ Erreur lors du chargement des données admin:', err);
      setError(err.message || 'Erreur lors du chargement des données');
    } finally {
      setLoading(false);
    }
  };

  // Créer un utilisateur
  const handleCreateUser = async (formData: UserFormData) => {
    try {
      setActionLoading('create');
      setError(null);
      
      const newUser = await createUser(formData);
      
      setSuccess(`Utilisateur "${newUser.username}" créé avec succès !`);
      setViewMode('users');
      await fetchData();
    } catch (err: any) {
      setError(err.message || 'Erreur lors de la création de l\'utilisateur');
    } finally {
      setActionLoading(null);
    }
  };

  // Modifier un utilisateur
  const handleUpdateUser = async (formData: UserFormData) => {
    if (!currentUser) return;
    
    try {
      setActionLoading('update');
      setError(null);
      
      const updatedUser = await updateUser(currentUser.id, formData);
      
      setSuccess(`Utilisateur "${updatedUser.username}" modifié avec succès !`);
      setViewMode('users');
      setCurrentUser(null);
      await fetchData();
    } catch (err: any) {
      setError(err.message || 'Erreur lors de la modification de l\'utilisateur');
    } finally {
      setActionLoading(null);
    }
  };

  // Supprimer un utilisateur
  const handleDeleteUser = async (userToDelete: User) => {
    if (!confirm(`Êtes-vous sûr de vouloir supprimer l'utilisateur "${userToDelete.username}" ?\n\nCette action désactivera le compte de façon permanente.`)) {
      return;
    }
    
    try {
      setActionLoading(`delete-${userToDelete.id}`);
      setError(null);
      
      await deleteUser(userToDelete.id);
      
      setSuccess(`Utilisateur "${userToDelete.username}" supprimé avec succès !`);
      await fetchData();
    } catch (err: any) {
      setError(err.message || 'Erreur lors de la suppression de l\'utilisateur');
    } finally {
      setActionLoading(null);
    }
  };

  const clearMessages = () => {
    setError(null);
    setSuccess(null);
  };

  const filteredUsers = users.filter(u =>
    u.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
    u.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
    u.firstName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    u.lastName?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (!isAdmin) {
    return (
      <div style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '60vh',
        backgroundColor: '#f8f9fa',
        textAlign: 'center'
      }}>
        <div style={{ fontSize: '5em', marginBottom: '20px' }}>🚫</div>
        <h1 style={{ color: '#666', margin: '0 0 10px 0' }}>Accès refusé</h1>
        <p style={{ color: '#999', marginBottom: '30px' }}>
          Vous devez être administrateur pour accéder à cette page.
        </p>
        <a 
          href="/"
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
        </a>
      </div>
    );
  }

  if (loading) {
    return (
      <div style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '60vh'
      }}>
        <div style={{ fontSize: '3em', marginBottom: '20px' }}>⏳</div>
        <h2 style={{ color: '#666', margin: '0' }}>Chargement de l'administration...</h2>
      </div>
    );
  }

  return (
    <div style={{ 
      padding: '20px',
      width: '100%',
      margin: '0',
      backgroundColor: '#1a1a1a',
      minHeight: '100vh'
    }}>
      {/* Header */}
      <div style={{ marginBottom: '30px' }}>
        <h1 style={{ 
          margin: '0 0 8px 0', 
          color: '#fff',
          fontSize: '2em'
        }}>
          👑 Administration
        </h1>
        <p style={{ 
          margin: '0', 
          color: '#999',
          fontSize: '1em'
        }}>
          Bienvenue {user?.username} - Gestion complète du système
        </p>
      </div>

      {/* Messages */}
      {(error || success) && (
        <div style={{
          padding: '12px 16px',
          borderRadius: '4px',
          marginBottom: '20px',
          backgroundColor: error ? '#dc3545' : '#28a745',
          color: 'white',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          <span>{error || success}</span>
          <button
            onClick={clearMessages}
            style={{
              background: 'none',
              border: 'none',
              color: 'white',
              cursor: 'pointer',
              fontSize: '1.2em'
            }}
          >
            ×
          </button>
        </div>
      )}

      {/* Navigation */}
      <div style={{
        display: 'flex',
        gap: '12px',
        marginBottom: '30px',
        flexWrap: 'wrap'
      }}>
        {[
          { key: 'dashboard', label: '📊 Tableau de bord', icon: '📊' },
          { key: 'users', label: '👥 Utilisateurs', icon: '👥' },
          { key: 'create', label: '➕ Nouvel utilisateur', icon: '➕' }
        ].map(tab => (
          <button
            key={tab.key}
            onClick={() => {
              setViewMode(tab.key as ViewMode);
              setCurrentUser(null);
              clearMessages();
            }}
            style={{
              padding: '10px 16px',
              borderRadius: '4px',
              border: 'none',
              backgroundColor: viewMode === tab.key ? '#007bff' : '#333',
              color: 'white',
              cursor: 'pointer',
              fontSize: '0.9em',
              transition: 'all 0.2s ease'
            }}
            onMouseEnter={(e) => {
              if (viewMode !== tab.key) {
                e.currentTarget.style.backgroundColor = '#555';
              }
            }}
            onMouseLeave={(e) => {
              if (viewMode !== tab.key) {
                e.currentTarget.style.backgroundColor = '#333';
              }
            }}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Contenu principal */}
      {viewMode === 'dashboard' && (
        <div>
          <h2 style={{ color: '#fff', marginBottom: '20px' }}>📊 Tableau de bord</h2>
          
          {/* Statistiques */}
          {stats && (
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
              gap: '20px',
              marginBottom: '30px'
            }}>
              <div style={{
                backgroundColor: '#333',
                padding: '20px',
                borderRadius: '8px',
                border: '1px solid #444'
              }}>
                <div style={{ fontSize: '2em', marginBottom: '8px' }}>👥</div>
                <div style={{ color: '#fff', fontSize: '1.5em', fontWeight: 'bold' }}>
                  {stats.totalUsers}
                </div>
                <div style={{ color: '#999', fontSize: '0.9em' }}>
                  Utilisateurs total
                </div>
              </div>

              <div style={{
                backgroundColor: '#333',
                padding: '20px',
                borderRadius: '8px',
                border: '1px solid #444'
              }}>
                <div style={{ fontSize: '2em', marginBottom: '8px' }}>✅</div>
                <div style={{ color: '#28a745', fontSize: '1.5em', fontWeight: 'bold' }}>
                  {stats.activeUsers}
                </div>
                <div style={{ color: '#999', fontSize: '0.9em' }}>
                  Comptes actifs
                </div>
              </div>

              <div style={{
                backgroundColor: '#333',
                padding: '20px',
                borderRadius: '8px',
                border: '1px solid #444'
              }}>
                <div style={{ fontSize: '2em', marginBottom: '8px' }}>❌</div>
                <div style={{ color: '#dc3545', fontSize: '1.5em', fontWeight: 'bold' }}>
                  {stats.inactiveUsers}
                </div>
                <div style={{ color: '#999', fontSize: '0.9em' }}>
                  Comptes inactifs
                </div>
              </div>
            </div>
          )}

          {/* Actions rapides */}
          <div style={{
            backgroundColor: '#333',
            padding: '24px',
            borderRadius: '8px',
            border: '1px solid #444'
          }}>
            <h3 style={{ color: '#fff', marginBottom: '16px' }}>Actions rapides</h3>
            <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
              <button
                onClick={() => setViewMode('users')}
                style={{
                  padding: '10px 16px',
                  backgroundColor: '#007bff',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontSize: '0.9em'
                }}
              >
                👥 Gérer les utilisateurs
              </button>
              <button
                onClick={() => setViewMode('create')}
                style={{
                  padding: '10px 16px',
                  backgroundColor: '#28a745',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontSize: '0.9em'
                }}
              >
                ➕ Créer un utilisateur
              </button>
            </div>
          </div>
        </div>
      )}

      {viewMode === 'users' && (
        <div>
          <div style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: '20px',
            flexWrap: 'wrap',
            gap: '12px'
          }}>
            <h2 style={{ color: '#fff', margin: '0' }}>👥 Gestion des utilisateurs</h2>
            
            {/* Recherche */}
            <input
              type="text"
              placeholder="🔍 Rechercher un utilisateur..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={{
                padding: '8px 12px',
                borderRadius: '4px',
                border: '1px solid #444',
                backgroundColor: '#333',
                color: '#fff',
                fontSize: '0.9em',
                minWidth: '250px'
              }}
            />
          </div>

          {/* Tableau des utilisateurs */}
          <div style={{
            backgroundColor: '#333',
            borderRadius: '8px',
            border: '1px solid #444',
            overflow: 'hidden'
          }}>
            {filteredUsers.length === 0 ? (
              <div style={{
                padding: '40px',
                textAlign: 'center',
                color: '#999'
              }}>
                {searchTerm ? 
                  `Aucun utilisateur trouvé pour "${searchTerm}"` : 
                  'Aucun utilisateur trouvé'
                }
              </div>
            ) : (
              <div style={{ overflowX: 'auto' }}>
                <table style={{
                  width: '100%',
                  borderCollapse: 'collapse'
                }}>
                  <thead>
                    <tr style={{ backgroundColor: '#444' }}>
                      <th style={{ padding: '12px', textAlign: 'left', color: '#fff', fontSize: '0.9em' }}>
                        Utilisateur
                      </th>
                      <th style={{ padding: '12px', textAlign: 'left', color: '#fff', fontSize: '0.9em' }}>
                        Email
                      </th>
                      <th style={{ padding: '12px', textAlign: 'left', color: '#fff', fontSize: '0.9em' }}>
                        Rôle
                      </th>
                      <th style={{ padding: '12px', textAlign: 'left', color: '#fff', fontSize: '0.9em' }}>
                        Statut
                      </th>
                      <th style={{ padding: '12px', textAlign: 'left', color: '#fff', fontSize: '0.9em' }}>
                        Dernière connexion
                      </th>
                      <th style={{ padding: '12px', textAlign: 'center', color: '#fff', fontSize: '0.9em' }}>
                        Actions
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredUsers.map((u) => (
                      <tr
                        key={u.id}
                        style={{
                          borderBottom: '1px solid #444',
                          backgroundColor: u.active ? 'transparent' : '#2a1f1f'
                        }}
                      >
                        <td style={{ padding: '12px' }}>
                          <div>
                            <div style={{ color: '#fff', fontWeight: '500' }}>
                              {u.username}
                            </div>
                            {(u.firstName || u.lastName) && (
                              <div style={{ color: '#999', fontSize: '0.8em' }}>
                                {u.firstName} {u.lastName}
                              </div>
                            )}
                          </div>
                        </td>
                        <td style={{ padding: '12px', color: '#ccc', fontSize: '0.9em' }}>
                          {u.email}
                        </td>
                        <td style={{ padding: '12px', fontSize: '0.9em' }}>
                          <span style={{
                            padding: '4px 8px',
                            borderRadius: '4px',
                            backgroundColor: u.role === 'ADMINISTRATEUR' ? '#dc3545' : 
                                            u.role === 'EDITEUR' ? '#fd7e14' : '#6c757d',
                            color: 'white',
                            fontSize: '0.8em'
                          }}>
                            {formatRole(u.role)}
                          </span>
                        </td>
                        <td style={{ padding: '12px', fontSize: '0.9em' }}>
                          <span style={{
                            padding: '4px 8px',
                            borderRadius: '4px',
                            backgroundColor: u.active ? '#28a745' : '#dc3545',
                            color: 'white',
                            fontSize: '0.8em'
                          }}>
                            {u.active ? '✅ Actif' : '❌ Inactif'}
                          </span>
                        </td>
                        <td style={{ padding: '12px', color: '#ccc', fontSize: '0.9em' }}>
                          {formatDate(u.lastLogin)}
                        </td>
                        <td style={{ padding: '12px', textAlign: 'center' }}>
                          <div style={{ display: 'flex', gap: '8px', justifyContent: 'center' }}>
                            <button
                              onClick={() => {
                                setCurrentUser(u);
                                setViewMode('edit');
                              }}
                              style={{
                                padding: '6px 12px',
                                backgroundColor: '#007bff',
                                color: 'white',
                                border: 'none',
                                borderRadius: '4px',
                                cursor: 'pointer',
                                fontSize: '0.8em'
                              }}
                            >
                              ✏️ Modifier
                            </button>
                            {u.id !== user?.id && (
                              <button
                                onClick={() => handleDeleteUser(u)}
                                disabled={actionLoading === `delete-${u.id}`}
                                style={{
                                  padding: '6px 12px',
                                  backgroundColor: actionLoading === `delete-${u.id}` ? '#666' : '#dc3545',
                                  color: 'white',
                                  border: 'none',
                                  borderRadius: '4px',
                                  cursor: actionLoading === `delete-${u.id}` ? 'not-allowed' : 'pointer',
                                  fontSize: '0.8em'
                                }}
                              >
                                {actionLoading === `delete-${u.id}` ? '⏳' : '🗑️'} Supprimer
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      {viewMode === 'create' && (
        <div>
          <h2 style={{ color: '#fff', marginBottom: '20px' }}>➕ Créer un nouvel utilisateur</h2>
          <UserForm
            onSubmit={handleCreateUser}
            onCancel={() => setViewMode('users')}
            isLoading={actionLoading === 'create'}
            submitLabel="Créer l'utilisateur"
          />
        </div>
      )}

      {viewMode === 'edit' && currentUser && (
        <div>
          <h2 style={{ color: '#fff', marginBottom: '20px' }}>
            ✏️ Modifier {currentUser.username}
          </h2>
          <UserForm
            user={currentUser}
            onSubmit={handleUpdateUser}
            onCancel={() => {
              setViewMode('users');
              setCurrentUser(null);
            }}
            isLoading={actionLoading === 'update'}
            submitLabel="Sauvegarder les modifications"
          />
        </div>
      )}
    </div>
  );
}

export default AdminPage; 