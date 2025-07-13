import { useState } from 'react';
import { testConnection } from '../services/api';

interface TestResult {
  success: boolean;
  status: string | number;
  data: any;
  message: string;
}

function TestPage() {
  const [testing, setTesting] = useState(false);
  const [result, setResult] = useState<TestResult | null>(null);

  const handleTest = async () => {
    setTesting(true);
    setResult(null);
    
    try {
      const testResult = await testConnection();
      setResult(testResult);
    } catch (error) {
      setResult({
        success: false,
        status: 'Error',
        data: null,
        message: 'Erreur inattendue lors du test'
      });
    } finally {
      setTesting(false);
    }
  };

  return (
    <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
      <h1>🧪 Test de Connexion Backend</h1>
      
      <div style={{ marginBottom: '20px' }}>
        <p>Cette page teste la communication entre le frontend React et le backend Spring Boot.</p>
        <p><strong>Endpoint testé :</strong> <code>GET /api/articles/recent</code></p>
      </div>

      <button 
        onClick={handleTest}
        disabled={testing}
        style={{
          padding: '12px 24px',
          fontSize: '16px',
          backgroundColor: testing ? '#ccc' : '#007bff',
          color: 'white',
          border: 'none',
          borderRadius: '4px',
          cursor: testing ? 'not-allowed' : 'pointer'
        }}
      >
        {testing ? '🔄 Test en cours...' : '🚀 Tester Backend'}
      </button>

      {result && (
        <div 
          style={{
            marginTop: '20px',
            padding: '15px',
            borderRadius: '4px',
            backgroundColor: result.success ? '#d4edda' : '#f8d7da',
            border: result.success ? '1px solid #c3e6cb' : '1px solid #f5c6cb',
            color: result.success ? '#155724' : '#721c24'
          }}
        >
          <h3>{result.success ? '✅ Succès' : '❌ Échec'}</h3>
          
          <p><strong>Statut :</strong> {result.status}</p>
          <p><strong>Message :</strong> {result.message}</p>
          
          {result.data && (
            <details style={{ marginTop: '10px' }}>
              <summary style={{ cursor: 'pointer' }}>
                📊 Données reçues ({Array.isArray(result.data) ? result.data.length : 'object'})
              </summary>
              <pre 
                style={{
                  backgroundColor: '#f8f9fa',
                  padding: '10px',
                  borderRadius: '4px',
                  overflow: 'auto',
                  fontSize: '12px',
                  marginTop: '5px'
                }}
              >
                {JSON.stringify(result.data, null, 2)}
              </pre>
            </details>
          )}
        </div>
      )}

      <div style={{ marginTop: '30px', fontSize: '14px', color: '#666' }}>
        <h4>🔧 Configuration :</h4>
        <ul>
          <li><strong>Frontend :</strong> http://localhost:5173</li>
          <li><strong>Backend :</strong> http://localhost:8080 (proxy)</li>
          <li><strong>Endpoint :</strong> /api/articles/recent</li>
        </ul>
        
        <h4>📋 À vérifier :</h4>
        <ul>
          <li>Backend Spring Boot démarré sur port 8080</li>
          <li>Pas d'erreurs CORS</li>
          <li>Données de test présentes (articles)</li>
        </ul>
      </div>
    </div>
  );
}

export default TestPage; 