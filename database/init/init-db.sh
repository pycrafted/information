#!/bin/bash

# Ce script initialise la base de données PostgreSQL en injectant le mot de passe depuis le fichier .env
# Usage : ./init-db.sh

set -e

# Charger les variables du fichier .env
if [ -f ../../.env ]; then
  export $(grep -v '^#' ../../.env | xargs)
else
  echo "Erreur : le fichier .env n'a pas été trouvé à la racine du projet."
  exit 1
fi

if [ -z "$DB_PASSWORD" ] || [ -z "$DB_USERNAME" ] || [ -z "$DB_NAME" ]; then
  echo "Erreur : certaines variables d'environnement (DB_USERNAME, DB_PASSWORD, DB_NAME) ne sont pas définies."
  exit 1
fi

# Création de la base et de l'utilisateur avec le mot de passe injecté
echo "Création de la base de données et de l'utilisateur..."
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USERNAME" -w <<-EOSQL
  CREATE DATABASE $DB_NAME;
  CREATE USER $DB_USERNAME WITH ENCRYPTED PASSWORD '$DB_PASSWORD';
  GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USERNAME;
EOSQL

echo "Ajout des extensions uuid-ossp et pg_trgm..."
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USERNAME" -d "$DB_NAME" -w <<-EOSQL
  CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
  CREATE EXTENSION IF NOT EXISTS "pg_trgm";
EOSQL

echo "Initialisation terminée." 