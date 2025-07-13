import { useState, useEffect } from 'react';
import { type ArticleFormData, type Article } from '../services/articleService';
import { type Category, getRootCategories, flattenCategories } from '../services/categoryService';

interface ArticleFormProps {
  article?: Article | null; // Pour mode édition
  onSubmit: (data: ArticleFormData) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
  submitLabel?: string;
}

function ArticleForm({ 
  article, 
  onSubmit, 
  onCancel, 
  isLoading = false,
  submitLabel = 'Créer l\'article'
}: ArticleFormProps) {
  const [formData, setFormData] = useState<ArticleFormData>({
    title: article?.title || '',
    content: article?.content || '',
    summary: article?.summary || '',
    categoryId: article?.categoryId || ''
  });
  
  const [categories, setCategories] = useState<Category[]>([]);
  const [categoriesLoading, setCategoriesLoading] = useState(true);
  const [errors, setErrors] = useState<Record<string, string>>({});

  // Charger les catégories
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        setCategoriesLoading(true);
        const rootCategories = await getRootCategories();
        const flatCategories = flattenCategories(rootCategories);
        setCategories(flatCategories);
      } catch (error) {
        console.error('❌ Erreur lors du chargement des catégories:', error);
      } finally {
        setCategoriesLoading(false);
      }
    };

    fetchCategories();
  }, []);

  // Initialiser le formulaire si un article est fourni
  useEffect(() => {
    if (article) {
      setFormData({
        title: article.title,
        content: article.content,
        summary: article.summary || '',
        categoryId: article.categoryId || ''
      });
    }
  }, [article]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
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
    const newErrors: Record<string, string> = {};

    if (!formData.title.trim()) {
      newErrors.title = 'Le titre est obligatoire';
    } else if (formData.title.length < 5) {
      newErrors.title = 'Le titre doit contenir au moins 5 caractères';
    } else if (formData.title.length > 200) {
      newErrors.title = 'Le titre ne peut pas dépasser 200 caractères';
    }

    if (!formData.content.trim()) {
      newErrors.content = 'Le contenu est obligatoire';
    } else if (formData.content.length < 50) {
      newErrors.content = 'Le contenu doit contenir au moins 50 caractères';
    }

    // Validation catégorie : uniquement en mode création
    if (!article && !formData.categoryId) {
      newErrors.categoryId = 'Veuillez sélectionner une catégorie';
    }

    if (formData.summary && formData.summary.length > 500) {
      newErrors.summary = 'Le résumé ne peut pas dépasser 500 caractères';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    try {
      await onSubmit(formData);
    } catch (error) {
      console.error('❌ Erreur lors de la soumission:', error);
    }
  };

  return (
    <div style={{
      backgroundColor: 'white',
      borderRadius: '8px',
      boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
      padding: '30px',
      margin: '20px 0'
    }}>
      {/* En-tête */}
      <div style={{ marginBottom: '30px' }}>
        <h2 style={{
          fontSize: '1.8em',
          color: '#333',
          margin: '0 0 10px 0',
          display: 'flex',
          alignItems: 'center',
          gap: '10px'
        }}>
          {article ? '✏️ Modifier l\'article' : '📝 Créer un nouvel article'}
        </h2>
        <p style={{ color: '#666', margin: '0' }}>
          {article 
            ? 'Modifiez les informations de votre article'
            : 'Créez un nouvel article qui sera sauvegardé en brouillon'
          }
        </p>
      </div>

      <form onSubmit={handleSubmit}>
        {/* Titre */}
        <div style={{ marginBottom: '25px' }}>
          <label style={{
            display: 'block',
            marginBottom: '8px',
            fontWeight: 'bold',
            color: '#333'
          }}>
            Titre de l'article *
          </label>
          <input
            type="text"
            name="title"
            value={formData.title}
            onChange={handleInputChange}
            placeholder="Entrez le titre de votre article"
            disabled={isLoading}
            style={{
              width: '100%',
              padding: '12px',
              border: errors.title ? '2px solid #dc3545' : '1px solid #ddd',
              borderRadius: '4px',
              fontSize: '1em',
              backgroundColor: isLoading ? '#f8f9fa' : 'white'
            }}
          />
          {errors.title && (
            <div style={{ color: '#dc3545', fontSize: '0.8em', marginTop: '5px' }}>
              {errors.title}
            </div>
          )}
          <div style={{ 
            fontSize: '0.8em', 
            color: '#666', 
            marginTop: '5px' 
          }}>
            {formData.title.length}/200 caractères
          </div>
        </div>

        {/* Catégorie */}
        <div style={{ marginBottom: '25px' }}>
          <label style={{
            display: 'block',
            marginBottom: '8px',
            fontWeight: 'bold',
            color: '#333'
          }}>
            Catégorie *
          </label>
          
          {article ? (
            // Mode édition : catégorie non modifiable pour l'instant
            <div style={{
              width: '100%',
              padding: '12px',
              border: '1px solid #e9ecef',
              borderRadius: '4px',
              backgroundColor: '#f8f9fa',
              color: '#6c757d',
              fontSize: '1em'
            }}>
              📁 {article.categoryName} (non modifiable pour l'instant)
            </div>
          ) : (
            // Mode création : sélection normale
            <select
              name="categoryId"
              value={formData.categoryId}
              onChange={handleInputChange}
              disabled={isLoading || categoriesLoading}
              style={{
                width: '100%',
                padding: '12px',
                border: errors.categoryId ? '2px solid #dc3545' : '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '1em',
                backgroundColor: isLoading || categoriesLoading ? '#f8f9fa' : 'white'
              }}
            >
              <option value="">
                {categoriesLoading ? 'Chargement des catégories...' : 'Sélectionnez une catégorie'}
              </option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {'  '.repeat(category.depth || 0)} {category.name}
                </option>
              ))}
            </select>
          )}
          
          {errors.categoryId && (
            <div style={{ color: '#dc3545', fontSize: '0.8em', marginTop: '5px' }}>
              {errors.categoryId}
            </div>
          )}
          
          {article && (
            <div style={{ 
              fontSize: '0.8em', 
              color: '#6c757d', 
              marginTop: '5px',
              fontStyle: 'italic'
            }}>
              ℹ️ La catégorie ne peut pas être modifiée (limitation backend). L'ID actuel sera conservé.
            </div>
          )}
        </div>

        {/* Résumé */}
        <div style={{ marginBottom: '25px' }}>
          <label style={{
            display: 'block',
            marginBottom: '8px',
            fontWeight: 'bold',
            color: '#333'
          }}>
            Résumé (optionnel)
          </label>
          <textarea
            name="summary"
            value={formData.summary}
            onChange={handleInputChange}
            placeholder="Entrez un résumé de votre article (optionnel)"
            disabled={isLoading}
            rows={3}
            style={{
              width: '100%',
              padding: '12px',
              border: errors.summary ? '2px solid #dc3545' : '1px solid #ddd',
              borderRadius: '4px',
              fontSize: '1em',
              backgroundColor: isLoading ? '#f8f9fa' : 'white',
              resize: 'vertical'
            }}
          />
          {errors.summary && (
            <div style={{ color: '#dc3545', fontSize: '0.8em', marginTop: '5px' }}>
              {errors.summary}
            </div>
          )}
          <div style={{ 
            fontSize: '0.8em', 
            color: '#666', 
            marginTop: '5px' 
          }}>
            {(formData.summary || '').length}/500 caractères
          </div>
        </div>

        {/* Contenu */}
        <div style={{ marginBottom: '30px' }}>
          <label style={{
            display: 'block',
            marginBottom: '8px',
            fontWeight: 'bold',
            color: '#333'
          }}>
            Contenu de l'article *
          </label>
          <textarea
            name="content"
            value={formData.content}
            onChange={handleInputChange}
            placeholder="Rédigez le contenu de votre article ici..."
            disabled={isLoading}
            rows={15}
            style={{
              width: '100%',
              padding: '12px',
              border: errors.content ? '2px solid #dc3545' : '1px solid #ddd',
              borderRadius: '4px',
              fontSize: '1em',
              backgroundColor: isLoading ? '#f8f9fa' : 'white',
              resize: 'vertical',
              lineHeight: '1.6'
            }}
          />
          {errors.content && (
            <div style={{ color: '#dc3545', fontSize: '0.8em', marginTop: '5px' }}>
              {errors.content}
            </div>
          )}
          <div style={{ 
            fontSize: '0.8em', 
            color: '#666', 
            marginTop: '5px' 
          }}>
            {formData.content.length} caractères (minimum 50)
          </div>
        </div>

        {/* Boutons d'action */}
        <div style={{
          display: 'flex',
          gap: '15px',
          justifyContent: 'flex-end',
          paddingTop: '20px',
          borderTop: '1px solid #e0e0e0'
        }}>
          <button
            type="button"
            onClick={onCancel}
            disabled={isLoading}
            style={{
              padding: '12px 24px',
              backgroundColor: 'transparent',
              border: '1px solid #6c757d',
              borderRadius: '4px',
              color: '#6c757d',
              fontSize: '1em',
              cursor: isLoading ? 'not-allowed' : 'pointer',
              transition: 'all 0.2s ease'
            }}
            onMouseEnter={(e) => {
              if (!isLoading) {
                e.currentTarget.style.backgroundColor = '#6c757d';
                e.currentTarget.style.color = 'white';
              }
            }}
            onMouseLeave={(e) => {
              if (!isLoading) {
                e.currentTarget.style.backgroundColor = 'transparent';
                e.currentTarget.style.color = '#6c757d';
              }
            }}
          >
            ❌ Annuler
          </button>
          
          <button
            type="submit"
            disabled={isLoading}
            style={{
              padding: '12px 24px',
              backgroundColor: isLoading ? '#6c757d' : '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              fontSize: '1em',
              fontWeight: 'bold',
              cursor: isLoading ? 'not-allowed' : 'pointer',
              transition: 'background-color 0.2s ease'
            }}
          >
            {isLoading ? '🔄 Sauvegarde...' : submitLabel}
          </button>
        </div>
      </form>
    </div>
  );
}

export default ArticleForm; 