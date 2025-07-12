-- Création de la base de données et de l'utilisateur
CREATE DATABASE newsplatform;
-- Le mot de passe doit être passé via une variable d'environnement
-- Exemple : export DB_PASSWORD=... puis utiliser dans un script d'automatisation
-- CREATE USER admin_9F8a7C WITH ENCRYPTED PASSWORD '<mot_de_passe_depuis_env>';
-- GRANT ALL PRIVILEGES ON DATABASE newsplatform TO admin_9F8a7C;

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";