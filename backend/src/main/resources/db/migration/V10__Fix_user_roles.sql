-- Migration V10 : Correction des rôles utilisateur incorrects

-- Corriger tous les rôles utilisateur incorrects
UPDATE users SET role = 'VISITEUR' WHERE role = 'USER' OR role NOT IN ('VISITEUR', 'EDITEUR', 'ADMINISTRATEUR');

-- Vérifier que tous les utilisateurs ont des rôles valides
UPDATE users SET role = 'VISITEUR' WHERE role IS NULL;

-- Afficher un log de confirmation (PostgreSQL)
DO $$
BEGIN
    RAISE NOTICE 'Migration V10 terminée - Tous les rôles utilisateur ont été corrigés';
END $$; 