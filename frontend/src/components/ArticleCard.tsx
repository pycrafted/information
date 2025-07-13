import { type Article, formatDate, truncateContent } from '../services/articleService';

interface ArticleCardProps {
  article: Article;
  showFullContent?: boolean;
  onClick?: () => void;
}

function ArticleCard({ article, showFullContent = false, onClick }: ArticleCardProps) {
  const displayContent = showFullContent 
    ? article.content 
    : article.summary || truncateContent(article.content);

  return (
    <div 
      className="article-card"
      onClick={onClick}
      style={{
        border: '1px solid #e0e0e0',
        borderRadius: '8px',
        padding: '20px',
        marginBottom: '16px',
        backgroundColor: 'white',
        boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
        cursor: onClick ? 'pointer' : 'default',
        transition: 'transform 0.2s ease, box-shadow 0.2s ease'
      }}
      onMouseEnter={(e) => {
        if (onClick) {
          e.currentTarget.style.transform = 'translateY(-2px)';
          e.currentTarget.style.boxShadow = '0 4px 8px rgba(0,0,0,0.15)';
        }
      }}
      onMouseLeave={(e) => {
        if (onClick) {
          e.currentTarget.style.transform = 'translateY(0)';
          e.currentTarget.style.boxShadow = '0 2px 4px rgba(0,0,0,0.1)';
        }
      }}
    >
      {/* En-tête de l'article */}
      <div style={{ marginBottom: '12px' }}>
        <h2 style={{ 
          margin: '0 0 8px 0', 
          fontSize: '1.5em', 
          color: '#333',
          lineHeight: '1.3'
        }}>
          {article.title}
        </h2>
        
        <div style={{ 
          display: 'flex', 
          gap: '16px', 
          fontSize: '0.9em', 
          color: '#666'
        }}>
          <span>
            📅 {formatDate(article.publishedAt)}
          </span>
          
          <span>
            🏷️ {article.categoryName}
          </span>
          
          {article.author && (
            <span>
              ✍️ {article.author.firstName && article.author.lastName 
                    ? `${article.author.firstName} ${article.author.lastName}`
                    : article.author.username}
            </span>
          )}
        </div>
      </div>

      {/* Contenu de l'article */}
      <div style={{ 
        fontSize: '1em', 
        lineHeight: '1.6', 
        color: '#555',
        marginBottom: '12px'
      }}>
        {displayContent}
      </div>

      {/* Footer avec actions */}
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        marginTop: '16px',
        paddingTop: '12px',
        borderTop: '1px solid #f0f0f0'
      }}>
        <div style={{ fontSize: '0.85em', color: '#888' }}>
          ID: {article.id.substring(0, 8)}...
        </div>
        
        {onClick && (
          <button
            style={{
              padding: '6px 12px',
              backgroundColor: '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              fontSize: '0.9em',
              cursor: 'pointer'
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.backgroundColor = '#0056b3';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.backgroundColor = '#007bff';
            }}
          >
            Lire plus →
          </button>
        )}
      </div>
    </div>
  );
}

export default ArticleCard; 