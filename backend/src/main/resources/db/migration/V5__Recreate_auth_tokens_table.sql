-- Migration pour recréer complètement la table auth_tokens
-- avec la structure correcte correspondant à l'entité Java

-- Sauvegarder les données existantes
CREATE TEMP TABLE temp_auth_tokens AS 
SELECT id, user_id, token_hash AS token_value, expires_at, created_at 
FROM auth_tokens;

-- Supprimer la table existante
DROP TABLE IF EXISTS auth_tokens CASCADE;

-- Recréer la table avec la structure correcte
CREATE TABLE auth_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id),
    token_value VARCHAR(1000) NOT NULL,
    token_type VARCHAR(20) NOT NULL DEFAULT 'ACCESS',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    client_ip VARCHAR(45),
    user_agent VARCHAR(500)
);

-- Restaurer les données existantes avec les valeurs par défaut
INSERT INTO auth_tokens (id, user_id, token_value, expires_at, created_at, token_type, status)
SELECT id, user_id, token_value, expires_at, created_at, 'ACCESS', 'ACTIVE'
FROM temp_auth_tokens;

-- Créer les index pour les performances
CREATE INDEX idx_auth_tokens_user ON auth_tokens(user_id);
CREATE INDEX idx_auth_tokens_status ON auth_tokens(status);
CREATE INDEX idx_auth_tokens_type ON auth_tokens(token_type);
CREATE INDEX idx_auth_tokens_expires ON auth_tokens(expires_at); 