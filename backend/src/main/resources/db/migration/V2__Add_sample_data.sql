-- Migration V2 : Ajout de données d'exemple pour le développement

-- Utilisateurs avec tous les rôles
INSERT INTO users (id, username, email, password_hash, role, is_active, first_name, last_name, created_at, updated_at) VALUES
  ('00000000-0000-0000-0000-000000000001', 'admin', 'admin@newsplatform.com', '$2a$10$examplehash1', 'ADMINISTRATEUR', true, 'Alice', 'Admin', NOW(), NOW()),
  ('00000000-0000-0000-0000-000000000002', 'editeur', 'editeur@newsplatform.com', '$2a$10$examplehash2', 'EDITEUR', true, 'Bob', 'Editeur', NOW(), NOW()),
  ('00000000-0000-0000-0000-000000000003', 'visiteur', 'visiteur@newsplatform.com', '$2a$10$examplehash3', 'VISITEUR', true, 'Charlie', 'Visiteur', NOW(), NOW());

-- Catégories avec hiérarchie
INSERT INTO categories (id, name, slug, description, parent_id, created_at, updated_at) VALUES
  ('10000000-0000-0000-0000-000000000001', 'Actualités', 'actualites', 'Toutes les actualités générales', NULL, NOW(), NOW()),
  ('10000000-0000-0000-0000-000000000002', 'Sport', 'sport', 'Toutes les actualités sportives', NULL, NOW(), NOW()),
  ('10000000-0000-0000-0000-000000000003', 'Technologie', 'technologie', 'Actualités technologiques et informatiques', NULL, NOW(), NOW()),
  ('10000000-0000-0000-0000-000000000004', 'Football', 'football', 'Actualités football', '10000000-0000-0000-0000-000000000002', NOW(), NOW()),
  ('10000000-0000-0000-0000-000000000005', 'Développement Web', 'developpement-web', 'Articles sur le développement web', '10000000-0000-0000-0000-000000000003', NOW(), NOW());

-- Articles variés avec différents statuts
INSERT INTO articles (id, title, slug, content, summary, author_id, category_id, status, published_at, created_at, updated_at) VALUES
  ('20000000-0000-0000-0000-000000000001', 'Première actualité du jour', 'premiere-actualite-jour', 'Contenu détaillé de la première actualité avec beaucoup d''informations importantes pour nos lecteurs.', 'Résumé de la première actualité', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'PUBLISHED', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '3 hours', NOW()),
  ('20000000-0000-0000-0000-000000000002', 'Match de football exceptionnel', 'match-football-exceptionnel', 'Retour sur un match de football absolument passionnant qui s''est déroulé hier soir dans des conditions exceptionnelles.', 'Un match inoubliable', '00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000004', 'PUBLISHED', NOW() - INTERVAL '1 hour', NOW() - INTERVAL '2 hours', NOW()),
  ('20000000-0000-0000-0000-000000000003', 'Article en brouillon', 'article-brouillon', 'Cet article est encore en cours de rédaction et n''est pas encore publié.', 'Article en cours', '00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000003', 'DRAFT', NULL, NOW() - INTERVAL '1 hour', NOW()),
  ('20000000-0000-0000-0000-000000000004', 'Nouvelles tendances en développement web', 'nouvelles-tendances-dev-web', 'Exploration des dernières tendances en développement web, frameworks et outils modernes.', 'Tendances web modernes', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000005', 'PUBLISHED', NOW() - INTERVAL '30 minutes', NOW() - INTERVAL '1 hour', NOW()),
  ('20000000-0000-0000-0000-000000000005', 'Innovation technologique de 2025', 'innovation-tech-2025', 'Les innovations technologiques qui marquent cette année et qui transforment notre quotidien.', 'Tech 2025', '00000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003', 'PUBLISHED', NOW() - INTERVAL '15 minutes', NOW() - INTERVAL '45 minutes', NOW()); 