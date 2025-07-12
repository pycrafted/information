-- Migration pour mettre à jour la structure de la table auth_tokens
-- afin qu'elle corresponde à l'entité AuthToken Java

-- Renommer la colonne token_hash en token_value et augmenter sa taille
ALTER TABLE auth_tokens 
RENAME COLUMN token_hash TO token_value;

ALTER TABLE auth_tokens 
ALTER COLUMN token_value TYPE VARCHAR(1000);

-- Ajouter les nouvelles colonnes manquantes
ALTER TABLE auth_tokens 
ADD COLUMN token_type VARCHAR(20) NOT NULL DEFAULT 'ACCESS';

ALTER TABLE auth_tokens 
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE auth_tokens 
ADD COLUMN client_ip VARCHAR(45);

ALTER TABLE auth_tokens 
ADD COLUMN user_agent VARCHAR(500);

-- Créer des index pour les performances
CREATE INDEX idx_auth_tokens_status ON auth_tokens(status);
CREATE INDEX idx_auth_tokens_type ON auth_tokens(token_type);
CREATE INDEX idx_auth_tokens_expires ON auth_tokens(expires_at); 