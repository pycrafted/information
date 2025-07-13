import { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { TEST_ACCOUNTS } from '../services/authService';

function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, isLoading } = useAuth();

  const [formData, setFormData] = useState({
    username: '',
    password: '',
    rememberMe: false
  });
  const [error, setError] = useState('');

  // Récupérer l'URL de redirection depuis state ou par défaut vers '/'
  const redirectTo = (location.state as any)?.from?.pathname || '/';

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!formData.username.trim() || !formData.password.trim()) {
      setError('Veuillez remplir tous les champs');
      return;
    }

    try {
      await login(formData);
      
      // Redirection après connexion réussie
      console.log(`🔄 Redirection vers: ${redirectTo}`);
      navigate(redirectTo, { replace: true });
    } catch (err: any) {
      setError(err.message || 'Erreur lors de la connexion');
    }
  };

  const handleTestLogin = async (accountType: keyof typeof TEST_ACCOUNTS) => {
    const account = TEST_ACCOUNTS[accountType];
    setError('');

    try {
      await login({
        username: account.username,
        password: account.password
      });
      
      console.log(`🧪 Connexion test réussie avec ${account.username}`);
      navigate(redirectTo, { replace: true });
    } catch (err: any) {
      setError(`Erreur test ${accountType}: ${err.message}`);
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      backgroundColor: '#f8f9fa',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '20px'
    }}>
      <div style={{
        backgroundColor: 'white',
        borderRadius: '8px',
        boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
        padding: '40px',
        width: '100%',
        maxWidth: '500px'
      }}>
        {/* En-tête */}
        <div style={{ textAlign: 'center', marginBottom: '30px' }}>
          <div style={{ fontSize: '3em', marginBottom: '10px' }}>🔐</div>
          <h1 style={{ 
            fontSize: '2em', 
            color: '#333', 
            margin: '0 0 10px 0',
            fontWeight: 'bold' 
          }}>
            Connexion
          </h1>
          <p style={{ color: '#666', margin: '0' }}>
            Accédez à votre espace personnel
          </p>
        </div>

        {/* Message d'erreur */}
        {error && (
          <div style={{
            backgroundColor: '#f8d7da',
            border: '1px solid #f5c6cb',
            color: '#721c24',
            padding: '12px',
            borderRadius: '4px',
            marginBottom: '20px',
            fontSize: '0.9em'
          }}>
            <strong>❌ Erreur :</strong> {error}
          </div>
        )}

        {/* Formulaire */}
        <form onSubmit={handleSubmit} style={{ marginBottom: '30px' }}>
          <div style={{ marginBottom: '20px' }}>
            <label style={{
              display: 'block',
              marginBottom: '8px',
              fontWeight: 'bold',
              color: '#333'
            }}>
              Nom d'utilisateur
            </label>
            <input
              type="text"
              name="username"
              value={formData.username}
              onChange={handleInputChange}
              placeholder="Entrez votre nom d'utilisateur"
              disabled={isLoading}
              style={{
                width: '100%',
                padding: '12px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '1em',
                backgroundColor: isLoading ? '#f8f9fa' : 'white'
              }}
            />
          </div>

          <div style={{ marginBottom: '20px' }}>
            <label style={{
              display: 'block',
              marginBottom: '8px',
              fontWeight: 'bold',
              color: '#333'
            }}>
              Mot de passe
            </label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleInputChange}
              placeholder="Entrez votre mot de passe"
              disabled={isLoading}
              style={{
                width: '100%',
                padding: '12px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '1em',
                backgroundColor: isLoading ? '#f8f9fa' : 'white'
              }}
            />
          </div>

          <div style={{ marginBottom: '25px' }}>
            <label style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              cursor: 'pointer',
              color: '#666'
            }}>
              <input
                type="checkbox"
                name="rememberMe"
                checked={formData.rememberMe}
                onChange={handleInputChange}
                disabled={isLoading}
              />
              Se souvenir de moi
            </label>
          </div>

          <button
            type="submit"
            disabled={isLoading}
            style={{
              width: '100%',
              padding: '12px',
              backgroundColor: isLoading ? '#6c757d' : '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              fontSize: '1.1em',
              fontWeight: 'bold',
              cursor: isLoading ? 'not-allowed' : 'pointer',
              transition: 'background-color 0.2s ease'
            }}
          >
            {isLoading ? '🔄 Connexion en cours...' : '🚀 Se connecter'}
          </button>
        </form>

        {/* Comptes de test */}
        <div style={{
          borderTop: '1px solid #e0e0e0',
          paddingTop: '25px'
        }}>
          <h3 style={{
            fontSize: '1.1em',
            color: '#333',
            margin: '0 0 15px 0',
            textAlign: 'center'
          }}>
            🧪 Comptes de test
          </h3>
          
          <div style={{
            display: 'grid',
            gap: '10px'
          }}>
            {Object.entries(TEST_ACCOUNTS).map(([key, account]) => (
              <button
                key={key}
                onClick={() => handleTestLogin(key as keyof typeof TEST_ACCOUNTS)}
                disabled={isLoading}
                style={{
                  padding: '10px 15px',
                  backgroundColor: 'transparent',
                  border: '1px solid #007bff',
                  borderRadius: '4px',
                  color: '#007bff',
                  cursor: isLoading ? 'not-allowed' : 'pointer',
                  fontSize: '0.9em',
                  transition: 'all 0.2s ease',
                  textAlign: 'left'
                }}
                onMouseEnter={(e) => {
                  if (!isLoading) {
                    e.currentTarget.style.backgroundColor = '#007bff';
                    e.currentTarget.style.color = 'white';
                  }
                }}
                onMouseLeave={(e) => {
                  if (!isLoading) {
                    e.currentTarget.style.backgroundColor = 'transparent';
                    e.currentTarget.style.color = '#007bff';
                  }
                }}
              >
                <div style={{ fontWeight: 'bold' }}>
                  {account.role === 'ADMINISTRATEUR' ? '👑' : 
                   account.role === 'EDITEUR' ? '✏️' : '👤'} {account.username}
                </div>
                <div style={{ fontSize: '0.8em', opacity: 0.8 }}>
                  {account.description}
                </div>
              </button>
            ))}
          </div>
        </div>

        {/* Liens */}
        <div style={{
          textAlign: 'center',
          marginTop: '25px',
          paddingTop: '20px',
          borderTop: '1px solid #e0e0e0'
        }}>
          <Link
            to="/"
            style={{
              color: '#007bff',
              textDecoration: 'none',
              fontSize: '0.9em'
            }}
          >
            🏠 Retour à l'accueil
          </Link>
        </div>

        {/* Info développement */}
        <div style={{
          marginTop: '20px',
          padding: '15px',
          backgroundColor: '#f8f9fa',
          borderRadius: '4px',
          fontSize: '0.8em',
          color: '#666',
          textAlign: 'center'
        }}>
          <strong>💡 Mode développement</strong><br />
          Utilisez les boutons de test ci-dessus pour tester les différents rôles utilisateur.
        </div>
      </div>
    </div>
  );
}

export default LoginPage; 