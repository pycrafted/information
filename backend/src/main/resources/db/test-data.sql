-- Données de test pour H2 en développement
-- Utilisateurs avec mots de passe simples (hashés avec BCrypt pour 'password')

INSERT INTO users (id, username, email, password, role, active, first_name, last_name, created_at, updated_at) VALUES
  ('00000000-0000-0000-0000-000000000001', 'admin', 'admin@newsplatform.com', '$2a$10$N.EMYB7u2SFTgLOlp5QeduGHxjJ3T6Pk4TCdz.ZOyQmSvY9M9V.SW', 'ADMINISTRATEUR', true, 'Alice', 'Admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('00000000-0000-0000-0000-000000000002', 'editeur', 'editeur@newsplatform.com', '$2a$10$N.EMYB7u2SFTgLOlp5QeduGHxjJ3T6Pk4TCdz.ZOyQmSvY9M9V.SW', 'EDITEUR', true, 'Bob', 'Editeur', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('00000000-0000-0000-0000-000000000003', 'visiteur', 'visiteur@newsplatform.com', '$2a$10$N.EMYB7u2SFTgLOlp5QeduGHxjJ3T6Pk4TCdz.ZOyQmSvY9M9V.SW', 'VISITEUR', true, 'Charlie', 'Visiteur', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Catégories avec hiérarchie
INSERT INTO categories (id, name, slug, description, parent_id, created_at, updated_at) VALUES
  ('10000000-0000-0000-0000-000000000001', 'Actualités', 'actualites', 'Toutes les actualités générales', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('10000000-0000-0000-0000-000000000002', 'Sport', 'sport', 'Toutes les actualités sportives', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('10000000-0000-0000-0000-000000000003', 'Technologie', 'technologie', 'Actualités technologiques et informatiques', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('10000000-0000-0000-0000-000000000004', 'Football', 'football', 'Actualités football', '10000000-0000-0000-0000-000000000002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('10000000-0000-0000-0000-000000000005', 'Développement Web', 'developpement-web', 'Articles sur le développement web', '10000000-0000-0000-0000-000000000003', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Articles avec différents statuts
INSERT INTO articles (id, title, slug, content, summary, author_id, category_id, status, published_at, created_at, updated_at) VALUES
  ('20000000-0000-0000-0000-000000000001', 'Première actualité du jour', 'premiere-actualite-jour', 'Contenu détaillé de la première actualité avec beaucoup d''informations importantes pour nos lecteurs. Cette actualité couvre tous les événements marquants de la journée.', 'Résumé de la première actualité', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '2' HOUR, CURRENT_TIMESTAMP - INTERVAL '3' HOUR, CURRENT_TIMESTAMP),
  ('20000000-0000-0000-0000-000000000002', 'Match de football exceptionnel', 'match-football-exceptionnel', 'Retour sur un match de football absolument passionnant qui s''est déroulé hier soir dans des conditions exceptionnelles. Les joueurs ont montré un niveau de jeu remarquable.', 'Un match inoubliable', '00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000004', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '1' HOUR, CURRENT_TIMESTAMP - INTERVAL '2' HOUR, CURRENT_TIMESTAMP),
  ('20000000-0000-0000-0000-000000000003', 'Article en brouillon', 'article-brouillon', 'Cet article est encore en cours de rédaction et n''est pas encore publié. Il sera disponible prochainement.', 'Article en cours', '00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000003', 'DRAFT', NULL, CURRENT_TIMESTAMP - INTERVAL '1' HOUR, CURRENT_TIMESTAMP),
  ('20000000-0000-0000-0000-000000000004', 'Nouvelles tendances en développement web', 'nouvelles-tendances-dev-web', 'Exploration des dernières tendances en développement web, frameworks et outils modernes qui transforment l''industrie du développement.', 'Tendances web modernes', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000005', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '30' MINUTE, CURRENT_TIMESTAMP - INTERVAL '1' HOUR, CURRENT_TIMESTAMP),
  ('20000000-0000-0000-0000-000000000005', 'Intelligence artificielle en 2025', 'ia-2025', 'L''intelligence artificielle continue d''évoluer rapidement avec de nouvelles avancées chaque mois. Découvrez les tendances qui marqueront cette année.', 'IA et futur proche', '00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000003', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '4' HOUR, CURRENT_TIMESTAMP - INTERVAL '5' HOUR, CURRENT_TIMESTAMP); 