import { type ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { type User } from '../services/authService';

interface ProtectedRouteProps {
  children: ReactNode;
  requiredRole?: User['role'];
  fallbackPath?: string;
}

/**
 * Composant pour protéger les routes selon l'authentification et les rôles
 */
function ProtectedRoute({ 
  children, 
  requiredRole, 
  fallbackPath = '/login' 
}: ProtectedRouteProps) {
  const { isAuthenticated, user, hasRole, isLoading } = useAuth();
  const location = useLocation();

  // Afficher un loading pendant la vérification d'auth
  if (isLoading) {
    return (
      <div style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '60vh',
        backgroundColor: '#f8f9fa'
      }}>
        <div style={{ fontSize: '3em', marginBottom: '20px' }}>🔄</div>
        <h2 style={{ color: '#666', margin: '0' }}>Vérification des permissions...</h2>
      </div>
    );
  }

  // Vérifier si l'utilisateur est connecté
  if (!isAuthenticated || !user) {
    console.log('🚫 Accès refusé : utilisateur non connecté');
    
    return (
      <Navigate 
        to={fallbackPath} 
        state={{ from: location }} 
        replace 
      />
    );
  }

  // Vérifier le rôle si spécifié
  if (requiredRole && !hasRole(requiredRole)) {
    console.log(`🚫 Accès refusé : rôle insuffisant. Requis: ${requiredRole}, Actuel: ${user.role}`);
    
    return (
      <div style={{
        maxWidth: '600px',
        margin: '0 auto',
        padding: '40px 20px',
        textAlign: 'center',
        backgroundColor: '#f8f9fa',
        minHeight: '60vh',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center'
      }}>
        <div style={{ fontSize: '4em', marginBottom: '20px' }}>⛔</div>
        <h1 style={{ color: '#dc3545', marginBottom: '15px' }}>
          Accès interdit
        </h1>
        <p style={{ color: '#666', marginBottom: '10px', fontSize: '1.1em' }}>
          Vous n'avez pas les permissions nécessaires pour accéder à cette page.
        </p>
        <div style={{
          backgroundColor: 'white',
          padding: '20px',
          borderRadius: '8px',
          border: '1px solid #e0e0e0',
          marginBottom: '25px'
        }}>
          <p style={{ margin: '0 0 10px 0', fontSize: '0.9em' }}>
            <strong>Rôle requis :</strong> {requiredRole}
          </p>
          <p style={{ margin: '0', fontSize: '0.9em' }}>
            <strong>Votre rôle :</strong> {user.role}
          </p>
        </div>
        <div style={{ display: 'flex', gap: '15px', justifyContent: 'center' }}>
          <button
            onClick={() => window.history.back()}
            style={{
              padding: '10px 20px',
              backgroundColor: '#6c757d',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            ← Retour
          </button>
          <button
            onClick={() => window.location.href = '/'}
            style={{
              padding: '10px 20px',
              backgroundColor: '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            🏠 Accueil
          </button>
        </div>
      </div>
    );
  }

  // Utilisateur authentifié avec les bons rôles
  console.log(`✅ Accès autorisé pour ${user.username} (${user.role})`);
  return <>{children}</>;
}

export default ProtectedRoute; 