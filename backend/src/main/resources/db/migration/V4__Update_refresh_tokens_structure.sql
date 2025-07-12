-- Migration pour mettre à jour la structure de la table refresh_tokens
-- afin qu'elle corresponde à l'entité RefreshToken Java

-- Renommer la colonne token_hash en token_value et ajuster sa taille
ALTER TABLE refresh_tokens 
RENAME COLUMN token_hash TO token_value;

ALTER TABLE refresh_tokens 
ALTER COLUMN token_value TYPE VARCHAR(500);

-- Ajouter la contrainte d'unicité sur token_value
ALTER TABLE refresh_tokens 
ADD CONSTRAINT uk_refresh_tokens_token_value UNIQUE (token_value);

-- Ajouter les nouvelles colonnes manquantes
ALTER TABLE refresh_tokens 
ADD COLUMN last_used_at TIMESTAMP;

ALTER TABLE refresh_tokens 
ADD COLUMN revoked BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE refresh_tokens 
ADD COLUMN client_ip VARCHAR(45);

ALTER TABLE refresh_tokens 
ADD COLUMN user_agent VARCHAR(500);

ALTER TABLE refresh_tokens 
ADD COLUMN usage_count INTEGER NOT NULL DEFAULT 0;

-- Créer des index pour les performances
CREATE INDEX idx_refresh_tokens_revoked ON refresh_tokens(revoked);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_token_value ON refresh_tokens(token_value); 