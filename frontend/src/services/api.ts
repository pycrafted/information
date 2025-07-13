import axios from 'axios';

// Configuration de base pour axios
const api = axios.create({
  baseURL: '', // Vide car le proxy Vite gère la redirection
  timeout: 10000, // 10 secondes de timeout
  headers: {
    'Content-Type': 'application/json',
  },
});

// Intercepteur pour logger les requêtes (utile pour debug)
api.interceptors.request.use(
  (config) => {
    console.log(`🚀 API Request: ${config.method?.toUpperCase()} ${config.url}`);
    return config;
  },
  (error) => {
    console.error('❌ API Request Error:', error);
    return Promise.reject(error);
  }
);

// Intercepteur pour logger les réponses
api.interceptors.response.use(
  (response) => {
    console.log(`✅ API Response: ${response.status} ${response.config.url}`);
    return response;
  },
  (error) => {
    console.error('❌ API Response Error:', error.response?.status, error.response?.data);
    return Promise.reject(error);
  }
);

// Fonction de test de connexion backend
export const testConnection = async () => {
  try {
    console.log('🔄 Test de connexion backend...');
    const response = await api.get('/api/articles/recent');
    
    return {
      success: true,
      status: response.status,
      data: response.data,
      message: `Connexion réussie ! ${response.data.length} articles récupérés.`
    };
  } catch (error: any) {
    const errorDetails = {
      success: false,
      status: error.response?.status || 'Network Error',
      data: error.response?.data || null,
      message: error.response?.data?.message || error.message || 'Erreur de connexion au backend'
    };
    
    console.error('❌ Test connexion backend échoué:', errorDetails);
    return errorDetails;
  }
};

export default api; 