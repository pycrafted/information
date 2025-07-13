import { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { type Category, getRootCategories, flattenCategories } from '../services/categoryService';
import { useAuth } from '../contexts/AuthContext';

function Header() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [menuOpen, setMenuOpen] = useState(false);
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  
  const location = useLocation();
  const { user, isAuthenticated, logout } = useAuth();

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        setLoading(true);
        setError(null);
        
        console.log('🧭 Chargement du menu de navigation...');
        const rootCategories = await getRootCategories();
        
        // Aplatir la hiérarchie pour le menu
        const flatCategories = flattenCategories(rootCategories);
        setCategories(flatCategories);
        
        console.log(`✅ Menu chargé avec ${flatCategories.length} catégories`);
      } catch (err: any) {
        console.error('❌ Erreur lors du chargement du menu:', err);
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchCategories();
  }, []);

  const isActiveRoute = (path: string) => {
    return location.pathname === path;
  };

  return (
    <header style={{
      backgroundColor: '#fff',
      borderBottom: '2px solid #e0e0e0',
      boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
      position: 'sticky',
      top: 0,
      zIndex: 1000
    }}>
      <div style={{
        width: '100%',
        padding: '0 20px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        height: '70px'
      }}>
        {/* Logo et titre */}
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <Link 
            to="/" 
            style={{ 
              textDecoration: 'none',
              display: 'flex',
              alignItems: 'center',
              gap: '10px'
            }}
          >
            <div style={{ fontSize: '2em' }}>📰</div>
            <h1 style={{
              margin: 0,
              fontSize: '1.8em',
              color: '#007bff',
              fontWeight: 'bold'
            }}>
              News Platform
            </h1>
          </Link>
        </div>

        {/* Navigation desktop */}
        <nav style={{ 
          display: 'flex',
          alignItems: 'center',
          gap: '0'
        }}>
          {/* Lien Accueil */}
          <Link
            to="/"
            style={{
              padding: '12px 20px',
              textDecoration: 'none',
              color: isActiveRoute('/') ? '#007bff' : '#333',
              fontWeight: isActiveRoute('/') ? 'bold' : 'normal',
              backgroundColor: isActiveRoute('/') ? '#f8f9fa' : 'transparent',
              borderRadius: '4px',
              transition: 'all 0.2s ease'
            }}
            onMouseEnter={(e) => {
              if (!isActiveRoute('/')) {
                e.currentTarget.style.backgroundColor = '#f8f9fa';
                e.currentTarget.style.color = '#007bff';
              }
            }}
            onMouseLeave={(e) => {
              if (!isActiveRoute('/')) {
                e.currentTarget.style.backgroundColor = 'transparent';
                e.currentTarget.style.color = '#333';
              }
            }}
          >
            🏠 Accueil
          </Link>

          {/* Menu déroulant catégories */}
          <div 
            style={{ position: 'relative' }}
            onMouseEnter={() => setMenuOpen(true)}
            onMouseLeave={() => setMenuOpen(false)}
          >
            <button
              style={{
                padding: '12px 20px',
                backgroundColor: 'transparent',
                border: 'none',
                color: '#333',
                fontSize: '1em',
                cursor: 'pointer',
                borderRadius: '4px',
                transition: 'all 0.2s ease',
                display: 'flex',
                alignItems: 'center',
                gap: '5px'
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.backgroundColor = '#f8f9fa';
                e.currentTarget.style.color = '#007bff';
              }}
              onMouseLeave={(e) => {
                if (!menuOpen) {
                  e.currentTarget.style.backgroundColor = 'transparent';
                  e.currentTarget.style.color = '#333';
                }
              }}
            >
              📁 Catégories
              <span style={{ 
                marginLeft: '5px',
                transform: menuOpen ? 'rotate(180deg)' : 'rotate(0deg)',
                transition: 'transform 0.2s ease'
              }}>
                ▼
              </span>
            </button>

            {/* Menu déroulant */}
            {menuOpen && (
              <div style={{
                position: 'absolute',
                top: '100%',
                left: 0,
                backgroundColor: 'white',
                border: '1px solid #e0e0e0',
                borderRadius: '4px',
                boxShadow: '0 4px 8px rgba(0,0,0,0.15)',
                minWidth: '250px',
                zIndex: 1001,
                padding: '8px 0'
              }}>
                {loading && (
                  <div style={{ padding: '12px 20px', color: '#666' }}>
                    🔄 Chargement...
                  </div>
                )}
                
                {error && (
                  <div style={{ padding: '12px 20px', color: '#dc3545' }}>
                    ❌ Erreur: {error}
                  </div>
                )}
                
                {!loading && !error && categories.length > 0 && (
                  <>
                    {categories.map((category) => (
                      <Link
                        key={category.id}
                        to={`/category/${category.slug}`}
                        style={{
                          display: 'block',
                          padding: '10px 20px',
                          paddingLeft: `${20 + (category.depth || 0) * 20}px`,
                          textDecoration: 'none',
                          color: isActiveRoute(`/category/${category.slug}`) ? '#007bff' : '#333',
                          backgroundColor: isActiveRoute(`/category/${category.slug}`) ? '#f8f9fa' : 'transparent',
                          fontSize: category.depth === 0 ? '1em' : '0.9em',
                          fontWeight: category.depth === 0 ? 'bold' : 'normal',
                          borderBottom: category.depth === 0 ? '1px solid #f0f0f0' : 'none'
                        }}
                        onMouseEnter={(e) => {
                          if (!isActiveRoute(`/category/${category.slug}`)) {
                            e.currentTarget.style.backgroundColor = '#f8f9fa';
                            e.currentTarget.style.color = '#007bff';
                          }
                        }}
                        onMouseLeave={(e) => {
                          if (!isActiveRoute(`/category/${category.slug}`)) {
                            e.currentTarget.style.backgroundColor = 'transparent';
                            e.currentTarget.style.color = '#333';
                          }
                        }}
                      >
                        {category.depth === 0 ? '📂' : '📄'} {category.name}
                        {category.articleCount !== undefined && (
                          <span style={{ 
                            fontSize: '0.8em', 
                            color: '#666', 
                            marginLeft: '8px' 
                          }}>
                            ({category.articleCount})
                          </span>
                        )}
                      </Link>
                    ))}
                  </>
                )}
                
                {!loading && !error && categories.length === 0 && (
                  <div style={{ padding: '12px 20px', color: '#666' }}>
                    Aucune catégorie disponible
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Section authentification */}
          <div style={{ 
            display: 'flex', 
            alignItems: 'center',
            marginLeft: '20px'
          }}>
            {isAuthenticated && user ? (
              // Utilisateur connecté
              <div 
                style={{ position: 'relative' }}
                onMouseEnter={() => setUserMenuOpen(true)}
                onMouseLeave={() => setUserMenuOpen(false)}
              >
                <button
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    padding: '8px 12px',
                    backgroundColor: 'transparent',
                    border: '1px solid #007bff',
                    borderRadius: '4px',
                    color: '#007bff',
                    cursor: 'pointer',
                    fontSize: '0.9em',
                    transition: 'all 0.2s ease'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = '#007bff';
                    e.currentTarget.style.color = 'white';
                  }}
                  onMouseLeave={(e) => {
                    if (!userMenuOpen) {
                      e.currentTarget.style.backgroundColor = 'transparent';
                      e.currentTarget.style.color = '#007bff';
                    }
                  }}
                >
                  <span>
                    {user.role === 'ADMINISTRATEUR' ? '👑' : 
                     user.role === 'EDITEUR' ? '✏️' : '👤'}
                  </span>
                  <span>{user.username}</span>
                  <span style={{ 
                    transform: userMenuOpen ? 'rotate(180deg)' : 'rotate(0deg)',
                    transition: 'transform 0.2s ease'
                  }}>
                    ▼
                  </span>
                </button>

                {/* Menu utilisateur */}
                {userMenuOpen && (
                  <div style={{
                    position: 'absolute',
                    top: '100%',
                    right: 0,
                    backgroundColor: 'white',
                    border: '1px solid #e0e0e0',
                    borderRadius: '4px',
                    boxShadow: '0 4px 8px rgba(0,0,0,0.15)',
                    minWidth: '220px',
                    zIndex: 1001,
                    padding: '8px 0'
                  }}>
                    {/* Info utilisateur */}
                    <div style={{
                      padding: '12px 16px',
                      borderBottom: '1px solid #f0f0f0',
                      fontSize: '0.9em'
                    }}>
                      <div style={{ fontWeight: 'bold', marginBottom: '4px' }}>
                        {user.firstName && user.lastName 
                          ? `${user.firstName} ${user.lastName}`
                          : user.username}
                      </div>
                      <div style={{ color: '#666', fontSize: '0.8em' }}>
                        {user.email}
                      </div>
                      <div style={{ 
                        color: '#007bff', 
                        fontSize: '0.8em',
                        fontWeight: 'bold',
                        marginTop: '4px'
                      }}>
                        {user.role}
                      </div>
                    </div>

                    {/* Actions futures (placeholder) */}
                    <div style={{ padding: '8px 0' }}>
                      <div style={{
                        padding: '8px 16px',
                        color: '#999',
                        fontSize: '0.8em',
                        fontStyle: 'italic'
                      }}>
                        🚧 Profil (Étape 6)
                      </div>
                      
                      {user.role !== 'VISITEUR' && (
                        <a
                          href="/editor"
                          style={{
                            display: 'block',
                            padding: '8px 16px',
                            textDecoration: 'none',
                            color: '#333',
                            fontSize: '0.9em',
                            transition: 'background-color 0.2s ease'
                          }}
                          onMouseEnter={(e) => {
                            e.currentTarget.style.backgroundColor = '#f8f9fa';
                          }}
                          onMouseLeave={(e) => {
                            e.currentTarget.style.backgroundColor = 'transparent';
                          }}
                        >
                          ✏️ Mes articles
                        </a>
                      )}
                      
                      {user.role === 'ADMINISTRATEUR' && (
                        <a
                          href="/admin"
                          style={{
                            display: 'block',
                            padding: '8px 16px',
                            textDecoration: 'none',
                            color: '#333',
                            fontSize: '0.9em',
                            transition: 'background-color 0.2s ease'
                          }}
                          onMouseEnter={(e) => {
                            e.currentTarget.style.backgroundColor = '#f8f9fa';
                          }}
                          onMouseLeave={(e) => {
                            e.currentTarget.style.backgroundColor = 'transparent';
                          }}
                        >
                          👑 Administration
                        </a>
                      )}
                    </div>

                    {/* Déconnexion */}
                    <div style={{ borderTop: '1px solid #f0f0f0', padding: '8px 0' }}>
                      <button
                        onClick={async () => {
                          try {
                            await logout();
                            console.log('✅ Déconnexion réussie depuis le header');
                            window.location.href = '/'; // Redirection forcée
                          } catch (error) {
                            console.error('❌ Erreur déconnexion:', error);
                          }
                        }}
                        style={{
                          width: '100%',
                          padding: '8px 16px',
                          backgroundColor: 'transparent',
                          border: 'none',
                          textAlign: 'left',
                          cursor: 'pointer',
                          color: '#dc3545',
                          fontSize: '0.9em',
                          transition: 'background-color 0.2s ease'
                        }}
                        onMouseEnter={(e) => {
                          e.currentTarget.style.backgroundColor = '#f8f9fa';
                        }}
                        onMouseLeave={(e) => {
                          e.currentTarget.style.backgroundColor = 'transparent';
                        }}
                      >
                        🚪 Se déconnecter
                      </button>
                    </div>
                  </div>
                )}
              </div>
            ) : (
              // Utilisateur non connecté
              <Link
                to="/login"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px',
                  padding: '8px 16px',
                  backgroundColor: '#007bff',
                  color: 'white',
                  textDecoration: 'none',
                  borderRadius: '4px',
                  fontSize: '0.9em',
                  transition: 'background-color 0.2s ease'
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.backgroundColor = '#0056b3';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.backgroundColor = '#007bff';
                }}
              >
                🔐 Connexion
              </Link>
            )}
          </div>
        </nav>
      </div>
    </header>
  );
}

export default Header; 