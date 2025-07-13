import { useState, useEffect } from 'react';
import { type UserFormData, type User, validateUserData } from '../services/userService';

interface UserFormProps {
  user?: User | null; // Pour mode édition
  onSubmit: (data: UserFormData) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
  submitLabel?: string;
}

function UserForm({ 
  user, 
  onSubmit, 
  onCancel, 
  isLoading = false,
  submitLabel = 'Créer l\'utilisateur'
}: UserFormProps) {
  const [formData, setFormData] = useState<UserFormData>({
    username: user?.username || '',
    email: user?.email || '',
    password: '',
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    role: user?.role || 'VISITEUR',
    active: user?.active !== undefined ? user.active : true
  });
  
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [showPassword, setShowPassword] = useState(false);

  const isEditMode = !!user;

  // Initialiser le formulaire si un utilisateur est fourni
  useEffect(() => {
    if (user) {
      setFormData({
        username: user.username,
        email: user.email,
        password: '', // Ne pas pré-remplir le mot de passe en édition
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        role: user.role,
        active: user.active
      });
    }
  }, [user]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;
    const finalValue = type === 'checkbox' ? (e.target as HTMLInputElement).checked : value;
    
    setFormData(prev => ({
      ...prev,
      [name]: finalValue
    }));
    
    // Effacer l'erreur pour ce champ
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const validateForm = (): boolean => {
    const validationErrors = validateUserData(formData, !isEditMode);
    
    if (validationErrors.length > 0) {
      const errorObj: Record<string, string> = {};
      validationErrors.forEach(error => {
        if (error.includes('nom d\'utilisateur')) errorObj.username = error;
        else if (error.includes('email')) errorObj.email = error;
        else if (error.includes('mot de passe')) errorObj.password = error;
        else if (error.includes('rôle')) errorObj.role = error;
        else errorObj.general = error;
      });
      
      setErrors(errorObj);
      return false;
    }
    
    setErrors({});
    return true;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    try {
      await onSubmit(formData);
    } catch (error: any) {
      setErrors({ general: error.message || 'Erreur lors de la soumission du formulaire' });
    }
  };

  return (
    <div style={{
      backgroundColor: '#1a1a1a',
      border: '1px solid #333',
      borderRadius: '8px',
      padding: '24px',
      maxWidth: '600px',
      margin: '0 auto'
    }}>
      <div style={{ marginBottom: '24px' }}>
        <h2 style={{ 
          margin: '0 0 8px 0', 
          color: '#fff',
          fontSize: '1.5em'
        }}>
          {isEditMode ? `✏️ Modifier ${user?.username}` : '➕ Nouvel utilisateur'}
        </h2>
        <p style={{ 
          margin: '0', 
          color: '#999',
          fontSize: '0.9em'
        }}>
          {isEditMode 
            ? 'Modifiez les informations de l\'utilisateur' 
            : 'Créez un nouveau compte utilisateur dans le système'
          }
        </p>
      </div>

      {errors.general && (
        <div style={{
          backgroundColor: '#dc3545',
          color: 'white',
          padding: '12px',
          borderRadius: '4px',
          marginBottom: '20px',
          fontSize: '0.9em'
        }}>
          ⚠️ {errors.general}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div style={{ display: 'grid', gap: '20px' }}>
          {/* Nom d'utilisateur */}
          <div>
            <label style={{
              display: 'block',
              marginBottom: '6px',
              color: '#fff',
              fontSize: '0.9em',
              fontWeight: '500'
            }}>
              Nom d'utilisateur {!isEditMode && <span style={{ color: '#dc3545' }}>*</span>}
            </label>
            <input
              type="text"
              name="username"
              value={formData.username}
              onChange={handleInputChange}
              disabled={isEditMode || isLoading} // Username non modifiable en édition
              style={{
                width: '100%',
                padding: '10px',
                borderRadius: '4px',
                border: `1px solid ${errors.username ? '#dc3545' : '#444'}`,
                backgroundColor: isEditMode ? '#2a2a2a' : '#333',
                color: '#fff',
                fontSize: '0.9em',
                opacity: isEditMode ? 0.7 : 1,
                cursor: isEditMode ? 'not-allowed' : 'text'
              }}
              placeholder="ex: jdupont"
            />
            {errors.username && (
              <div style={{ color: '#dc3545', fontSize: '0.8em', marginTop: '4px' }}>
                {errors.username}
              </div>
            )}
            {isEditMode && (
              <div style={{ color: '#888', fontSize: '0.8em', marginTop: '4px' }}>
                Le nom d'utilisateur ne peut pas être modifié
              </div>
            )}
          </div>

          {/* Email */}
          <div>
            <label style={{
              display: 'block',
              marginBottom: '6px',
              color: '#fff',
              fontSize: '0.9em',
              fontWeight: '500'
            }}>
              Adresse email <span style={{ color: '#dc3545' }}>*</span>
            </label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleInputChange}
              disabled={isLoading}
              style={{
                width: '100%',
                padding: '10px',
                borderRadius: '4px',
                border: `1px solid ${errors.email ? '#dc3545' : '#444'}`,
                backgroundColor: '#333',
                color: '#fff',
                fontSize: '0.9em'
              }}
              placeholder="ex: jean.dupont@example.com"
            />
            {errors.email && (
              <div style={{ color: '#dc3545', fontSize: '0.8em', marginTop: '4px' }}>
                {errors.email}
              </div>
            )}
          </div>

          {/* Prénom et Nom */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div>
              <label style={{
                display: 'block',
                marginBottom: '6px',
                color: '#fff',
                fontSize: '0.9em',
                fontWeight: '500'
              }}>
                Prénom
              </label>
              <input
                type="text"
                name="firstName"
                value={formData.firstName}
                onChange={handleInputChange}
                disabled={isLoading}
                style={{
                  width: '100%',
                  padding: '10px',
                  borderRadius: '4px',
                  border: '1px solid #444',
                  backgroundColor: '#333',
                  color: '#fff',
                  fontSize: '0.9em'
                }}
                placeholder="ex: Jean"
              />
            </div>
            
            <div>
              <label style={{
                display: 'block',
                marginBottom: '6px',
                color: '#fff',
                fontSize: '0.9em',
                fontWeight: '500'
              }}>
                Nom de famille
              </label>
              <input
                type="text"
                name="lastName"
                value={formData.lastName}
                onChange={handleInputChange}
                disabled={isLoading}
                style={{
                  width: '100%',
                  padding: '10px',
                  borderRadius: '4px',
                  border: '1px solid #444',
                  backgroundColor: '#333',
                  color: '#fff',
                  fontSize: '0.9em'
                }}
                placeholder="ex: Dupont"
              />
            </div>
          </div>

          {/* Mot de passe */}
          <div>
            <label style={{
              display: 'block',
              marginBottom: '6px',
              color: '#fff',
              fontSize: '0.9em',
              fontWeight: '500'
            }}>
              Mot de passe {!isEditMode && <span style={{ color: '#dc3545' }}>*</span>}
            </label>
            <div style={{ position: 'relative' }}>
              <input
                type={showPassword ? 'text' : 'password'}
                name="password"
                value={formData.password}
                onChange={handleInputChange}
                disabled={isLoading}
                style={{
                  width: '100%',
                  padding: '10px',
                  paddingRight: '40px',
                  borderRadius: '4px',
                  border: `1px solid ${errors.password ? '#dc3545' : '#444'}`,
                  backgroundColor: '#333',
                  color: '#fff',
                  fontSize: '0.9em'
                }}
                placeholder={isEditMode ? "Laisser vide pour ne pas changer" : "Minimum 8 caractères"}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                style={{
                  position: 'absolute',
                  right: '8px',
                  top: '50%',
                  transform: 'translateY(-50%)',
                  background: 'none',
                  border: 'none',
                  color: '#888',
                  cursor: 'pointer',
                  fontSize: '0.8em'
                }}
              >
                {showPassword ? '🙈' : '👁️'}
              </button>
            </div>
            {errors.password && (
              <div style={{ color: '#dc3545', fontSize: '0.8em', marginTop: '4px' }}>
                {errors.password}
              </div>
            )}
            {isEditMode && (
              <div style={{ color: '#888', fontSize: '0.8em', marginTop: '4px' }}>
                Laissez vide pour conserver le mot de passe actuel
              </div>
            )}
          </div>

          {/* Rôle */}
          <div>
            <label style={{
              display: 'block',
              marginBottom: '6px',
              color: '#fff',
              fontSize: '0.9em',
              fontWeight: '500'
            }}>
              Rôle <span style={{ color: '#dc3545' }}>*</span>
            </label>
            <select
              name="role"
              value={formData.role}
              onChange={handleInputChange}
              disabled={isLoading}
              style={{
                width: '100%',
                padding: '10px',
                borderRadius: '4px',
                border: `1px solid ${errors.role ? '#dc3545' : '#444'}`,
                backgroundColor: '#333',
                color: '#fff',
                fontSize: '0.9em'
              }}
            >
              <option value="VISITEUR">👁️ Visiteur - Lecture seule</option>
              <option value="EDITEUR">✏️ Éditeur - Gestion articles</option>
              <option value="ADMINISTRATEUR">👑 Administrateur - Accès complet</option>
            </select>
            {errors.role && (
              <div style={{ color: '#dc3545', fontSize: '0.8em', marginTop: '4px' }}>
                {errors.role}
              </div>
            )}
          </div>

          {/* Statut actif (seulement en édition) */}
          {isEditMode && (
            <div>
              <label style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                color: '#fff',
                fontSize: '0.9em',
                cursor: 'pointer'
              }}>
                <input
                  type="checkbox"
                  name="active"
                  checked={formData.active}
                  onChange={handleInputChange}
                  disabled={isLoading}
                  style={{
                    width: '16px',
                    height: '16px',
                    cursor: 'pointer'
                  }}
                />
                Compte actif
              </label>
              <div style={{ color: '#888', fontSize: '0.8em', marginTop: '4px' }}>
                Décochez pour désactiver le compte (l'utilisateur ne pourra plus se connecter)
              </div>
            </div>
          )}
        </div>

        {/* Boutons d'action */}
        <div style={{
          display: 'flex',
          gap: '12px',
          marginTop: '24px',
          justifyContent: 'flex-end'
        }}>
          <button
            type="button"
            onClick={onCancel}
            disabled={isLoading}
            style={{
              padding: '10px 20px',
              borderRadius: '4px',
              border: '1px solid #666',
              backgroundColor: 'transparent',
              color: '#fff',
              fontSize: '0.9em',
              cursor: isLoading ? 'not-allowed' : 'pointer',
              opacity: isLoading ? 0.6 : 1,
              transition: 'all 0.2s ease'
            }}
            onMouseEnter={(e) => {
              if (!isLoading) {
                e.currentTarget.style.backgroundColor = '#333';
              }
            }}
            onMouseLeave={(e) => {
              if (!isLoading) {
                e.currentTarget.style.backgroundColor = 'transparent';
              }
            }}
          >
            ❌ Annuler
          </button>
          
          <button
            type="submit"
            disabled={isLoading}
            style={{
              padding: '10px 20px',
              borderRadius: '4px',
              border: 'none',
              backgroundColor: isLoading ? '#666' : '#007bff',
              color: 'white',
              fontSize: '0.9em',
              cursor: isLoading ? 'not-allowed' : 'pointer',
              opacity: isLoading ? 0.6 : 1,
              transition: 'all 0.2s ease'
            }}
            onMouseEnter={(e) => {
              if (!isLoading) {
                e.currentTarget.style.backgroundColor = '#0056b3';
              }
            }}
            onMouseLeave={(e) => {
              if (!isLoading) {
                e.currentTarget.style.backgroundColor = '#007bff';
              }
            }}
          >
            {isLoading ? '⏳ Traitement...' : submitLabel}
          </button>
        </div>
      </form>
    </div>
  );
}

export default UserForm; 