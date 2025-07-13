-- Migration V1 : Schéma complet avec données réelles minimales
-- Cette migration crée tout le schéma nécessaire et les données de base

-- ===============================================
-- ÉTAPE 1 : EXTENSIONS
-- ===============================================

-- Activer l'extension UUID pour PostgreSQL
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===============================================
-- ÉTAPE 2 : TABLES PRINCIPALES
-- ===============================================

-- Table des utilisateurs
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'VISITEUR',
    active BOOLEAN NOT NULL DEFAULT true,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- Table des catégories
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    parent_id UUID REFERENCES categories(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table des articles
CREATE TABLE articles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    content TEXT NOT NULL,
    summary TEXT,
    author_id UUID NOT NULL REFERENCES users(id),
    category_id UUID NOT NULL REFERENCES categories(id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ===============================================
-- ÉTAPE 3 : TABLES D'AUTHENTIFICATION
-- ===============================================

-- Table des jetons d'authentification
CREATE TABLE auth_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id),
    token_value VARCHAR(1000) NOT NULL,
    token_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    client_ip VARCHAR(45),
    user_agent VARCHAR(500)
);

-- Table des jetons de rafraîchissement
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id),
    token_value VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP,
    revoked BOOLEAN NOT NULL DEFAULT false,
    client_ip VARCHAR(45),
    user_agent VARCHAR(500),
    usage_count INTEGER NOT NULL DEFAULT 0
);

-- Table des logs d'audit
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50),
    resource_id UUID,
    details TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    success BOOLEAN NOT NULL DEFAULT true
);

-- ===============================================
-- ÉTAPE 4 : INDEX POUR PERFORMANCE
-- ===============================================

CREATE INDEX idx_articles_author ON articles(author_id);
CREATE INDEX idx_articles_category ON articles(category_id);
CREATE INDEX idx_articles_status ON articles(status);
CREATE INDEX idx_articles_published ON articles(published_at);
CREATE INDEX idx_categories_parent ON categories(parent_id);
CREATE INDEX idx_auth_tokens_user ON auth_tokens(user_id);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);

-- ===============================================
-- ÉTAPE 5 : DONNÉES RÉELLES MINIMALES
-- ===============================================

-- Utilisateur administrateur réel (pas de test)
-- Mot de passe : 'admin123' (BCrypt hash)
INSERT INTO users (username, email, password, role, active, first_name, last_name) 
VALUES 
  ('admin', 'admin@newsplatform.local', '$2a$12$LQv3c1yqBwEFxDh9895G.eFTg0iMn7rw9r0VrfZU2wxSNZ8hh6xKu', 'ADMINISTRATEUR', true, 'Administrator', 'System');

-- Catégories de base réelles (non test)
INSERT INTO categories (name, slug, description)
VALUES 
  ('Général', 'general', 'Articles généraux'),
  ('Annonces', 'annonces', 'Annonces officielles'),
  ('Actualités', 'actualites', 'Actualités importantes');

-- Pas d'articles fictifs - ils seront créés par les utilisateurs réels 