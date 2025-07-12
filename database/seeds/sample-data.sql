-- Seeds pour la table users
INSERT INTO users (id, username, email, password_hash, role, is_active) VALUES
  ('00000000-0000-0000-0000-000000000001', 'alice', 'alice@example.com', '$2a$10$hash1', 'ADMINISTRATEUR', true),
  ('00000000-0000-0000-0000-000000000002', 'bob', 'bob@example.com', '$2a$10$hash2', 'EDITEUR', true);

-- Seeds pour la table categories
INSERT INTO categories (id, name, slug, description) VALUES
  ('10000000-0000-0000-0000-000000000001', 'Actualités', 'actualites', 'Catégorie principale pour les actualités'),
  ('10000000-0000-0000-0000-000000000002', 'Sport', 'sport', 'Catégorie pour les articles sportifs');

-- Seeds pour la table articles
INSERT INTO articles (id, title, slug, content, author_id, category_id, status) VALUES
  ('20000000-0000-0000-0000-000000000001', 'Premier article', 'premier-article', 'Contenu de l\'article 1', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'PUBLISHED'),
  ('20000000-0000-0000-0000-000000000002', 'Deuxième article', 'deuxieme-article', 'Contenu de l\'article 2', '00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', 'DRAFT');
