-- Migration pour recréer complètement la table refresh_tokens
-- avec la structure correcte correspondant à l'entité Java

-- Sauvegarder les données existantes
CREATE TEMP TABLE temp_refresh_tokens AS 
SELECT id, user_id, token_hash AS token_value, expires_at, created_at 
FROM refresh_tokens;

-- Supprimer la table existante
DROP TABLE IF EXISTS refresh_tokens CASCADE;

-- Recréer la table avec la structure correcte
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

-- Restaurer les données existantes avec les valeurs par défaut
INSERT INTO refresh_tokens (id, user_id, token_value, expires_at, created_at, revoked, usage_count)
SELECT id, user_id, token_value, expires_at, created_at, false, 0
FROM temp_refresh_tokens;

-- Créer les index pour les performances
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_revoked ON refresh_tokens(revoked);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_token_value ON refresh_tokens(token_value); 