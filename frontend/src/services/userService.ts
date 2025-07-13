import api from './api';

// Types pour la gestion des utilisateurs
export interface UserFormData {
  username: string;
  email: string;
  password?: string;
  firstName?: string;
  lastName?: string;
  role: 'VISITEUR' | 'EDITEUR' | 'ADMINISTRATEUR';
  active?: boolean;
}

export interface User {
  id: string;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  role: 'VISITEUR' | 'EDITEUR' | 'ADMINISTRATEUR';
  roleDescription: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  lastLogin?: string;
}

export interface UserStats {
  totalUsers: number;
  activeUsers: number;
  inactiveUsers: number;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

/**
 * Récupérer tous les utilisateurs avec pagination
 * Endpoint : GET /api/users
 */
export const getUsers = async (page = 0, size = 10, sortBy = 'username', sortDir = 'asc'): Promise<User[]> => {
  try {
    console.log(`👥 Récupération des utilisateurs (page ${page}, taille ${size})`);
    
    const response = await api.get('/api/users', {
      params: { page, size, sortBy, sortDir }
    });
    
    // Le backend retourne directement un array pour l'instant
    const users = Array.isArray(response.data) ? response.data : response.data.content || [];
    
    console.log(`✅ ${users.length} utilisateurs récupérés`);
    return users;
  } catch (error: any) {
    console.error('❌ Erreur lors de la récupération des utilisateurs:', error);
    throw new Error(error.response?.data?.message || 'Erreur lors de la récupération des utilisateurs');
  }
};

/**
 * Récupérer un utilisateur par son ID
 * Endpoint : GET /api/users/{id}
 */
export const getUserById = async (id: string): Promise<User> => {
  try {
    console.log(`👤 Récupération de l'utilisateur ${id}`);
    
    const response = await api.get(`/api/users/${id}`);
    
    console.log(`✅ Utilisateur ${response.data.username} récupéré`);
    return response.data;
  } catch (error: any) {
    console.error(`❌ Erreur lors de la récupération de l'utilisateur ${id}:`, error);
    throw new Error(error.response?.data?.message || 'Utilisateur non trouvé');
  }
};

/**
 * Créer un nouvel utilisateur
 * Endpoint : POST /api/users
 */
export const createUser = async (userData: UserFormData): Promise<User> => {
  try {
    console.log(`➕ Création d'un nouvel utilisateur: ${userData.username}`);
    
    const response = await api.post('/api/users', userData);
    
    console.log(`✅ Utilisateur ${response.data.username} créé avec succès`);
    return response.data;
  } catch (error: any) {
    console.error('❌ Erreur lors de la création de l\'utilisateur:', error);
    
    if (error.response?.status === 400) {
      throw new Error(error.response.data?.message || 'Données utilisateur invalides');
    }
    
    throw new Error(error.response?.data?.message || 'Erreur lors de la création de l\'utilisateur');
  }
};

/**
 * Modifier un utilisateur existant
 * Endpoint : PUT /api/users/{id}
 */
export const updateUser = async (id: string, userData: Partial<UserFormData>): Promise<User> => {
  try {
    console.log(`📝 Modification de l'utilisateur ${id}`);
    
    const response = await api.put(`/api/users/${id}`, userData);
    
    console.log(`✅ Utilisateur ${response.data.username} modifié avec succès`);
    return response.data;
  } catch (error: any) {
    console.error(`❌ Erreur lors de la modification de l'utilisateur ${id}:`, error);
    
    if (error.response?.status === 404) {
      throw new Error('Utilisateur non trouvé');
    }
    if (error.response?.status === 400) {
      throw new Error(error.response.data?.message || 'Données utilisateur invalides');
    }
    
    throw new Error(error.response?.data?.message || 'Erreur lors de la modification de l\'utilisateur');
  }
};

/**
 * Supprimer (désactiver) un utilisateur
 * Endpoint : DELETE /api/users/{id}
 */
export const deleteUser = async (id: string): Promise<void> => {
  try {
    console.log(`🗑️ Suppression de l'utilisateur ${id}`);
    
    await api.delete(`/api/users/${id}`);
    
    console.log(`✅ Utilisateur ${id} supprimé avec succès`);
  } catch (error: any) {
    console.error(`❌ Erreur lors de la suppression de l'utilisateur ${id}:`, error);
    
    if (error.response?.status === 404) {
      throw new Error('Utilisateur non trouvé');
    }
    
    throw new Error(error.response?.data?.message || 'Erreur lors de la suppression de l\'utilisateur');
  }
};

/**
 * Récupérer les statistiques des utilisateurs
 * Endpoint : GET /api/users/stats
 */
export const getUserStats = async (): Promise<UserStats> => {
  try {
    console.log('📊 Récupération des statistiques utilisateurs');
    
    const response = await api.get('/api/users/stats');
    
    console.log(`✅ Statistiques récupérées: ${response.data.totalUsers} utilisateurs`);
    return response.data;
  } catch (error: any) {
    console.error('❌ Erreur lors de la récupération des statistiques:', error);
    throw new Error(error.response?.data?.message || 'Erreur lors de la récupération des statistiques');
  }
};

/**
 * Valider les données d'un utilisateur
 */
export const validateUserData = (userData: Partial<UserFormData>, isCreation = false): string[] => {
  const errors: string[] = [];
  
  if (isCreation && !userData.username?.trim()) {
    errors.push('Le nom d\'utilisateur est obligatoire');
  }
  
  if (userData.username && userData.username.length < 3) {
    errors.push('Le nom d\'utilisateur doit contenir au moins 3 caractères');
  }
  
  if (!userData.email?.trim()) {
    errors.push('L\'adresse email est obligatoire');
  }
  
  if (userData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(userData.email)) {
    errors.push('L\'adresse email n\'est pas valide');
  }
  
  if (isCreation && !userData.password?.trim()) {
    errors.push('Le mot de passe est obligatoire pour la création');
  }
  
  if (userData.password && userData.password.length < 8) {
    errors.push('Le mot de passe doit contenir au moins 8 caractères');
  }
  
  if (!userData.role) {
    errors.push('Le rôle est obligatoire');
  }
  
  if (userData.role && !['VISITEUR', 'EDITEUR', 'ADMINISTRATEUR'].includes(userData.role)) {
    errors.push('Le rôle sélectionné n\'est pas valide');
  }
  
  return errors;
};

/**
 * Formater le rôle pour l'affichage
 */
export const formatRole = (role: string): string => {
  const roleLabels = {
    'VISITEUR': '👁️ Visiteur',
    'EDITEUR': '✏️ Éditeur', 
    'ADMINISTRATEUR': '👑 Administrateur'
  };
  
  return roleLabels[role as keyof typeof roleLabels] || role;
};

/**
 * Formater la date pour l'affichage
 */
export const formatDate = (dateString?: string): string => {
  if (!dateString) return 'Jamais';
  
  try {
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch {
    return dateString;
  }
}; 