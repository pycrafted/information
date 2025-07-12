-- Migration pour synchroniser la table users avec l'entité Java

-- Renommer is_active en active
ALTER TABLE users 
RENAME COLUMN is_active TO active;

-- Renommer password_hash en password  
ALTER TABLE users 
RENAME COLUMN password_hash TO password;

-- Ajouter les colonnes manquantes
ALTER TABLE users 
ADD COLUMN first_name VARCHAR(100);

ALTER TABLE users 
ADD COLUMN last_name VARCHAR(100);

ALTER TABLE users 
ADD COLUMN last_login TIMESTAMP;

-- Ajouter updated_at si elle n'existe pas déjà
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'updated_at') THEN
        ALTER TABLE users ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
        UPDATE users SET updated_at = created_at WHERE updated_at IS NULL;
    END IF;
END $$; 