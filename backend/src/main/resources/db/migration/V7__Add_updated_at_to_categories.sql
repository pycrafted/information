-- Migration pour ajouter la colonne updated_at à la table categories

-- Ajouter la colonne updated_at avec une valeur par défaut
ALTER TABLE categories 
ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Mettre à jour les enregistrements existants avec la valeur de created_at
UPDATE categories 
SET updated_at = created_at 
WHERE updated_at IS NULL; 