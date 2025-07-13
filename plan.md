# Plan de Développement Frontend - News Platform

**Objectif :** Créer un frontend React simple et fonctionnel qui interagit avec le backend Spring Boot, en testant chaque étape manuellement.

---

## 📋 **Analyse Backend Complète**

### 🔌 **Endpoints Disponibles**

#### **🌐 Endpoints Publics (Sans authentification)**
- `GET /api/articles/recent` - 10 derniers articles publiés
- `GET /api/articles/published` - Articles publiés avec pagination
- `GET /api/articles/{id}` - Article par ID
- `GET /api/categories/roots` - Catégories racines avec hiérarchie
- `GET /api/categories` - Toutes catégories avec pagination
- `GET /api/categories/{id}` - Catégorie par ID
- `GET /api/categories/slug/{slug}` - Catégorie par slug

#### **🔐 Endpoints Authentification**
- `POST /api/auth/login` - Connexion utilisateur
- `POST /api/auth/logout` - Déconnexion
- `POST /api/auth/refresh` - Rafraîchir token

#### **✏️ Endpoints Protégés (EDITEUR + ADMIN)**
- `POST /api/articles` - Créer article (brouillon)
- `PUT /api/articles/{id}` - Modifier article
- `POST /api/articles/{id}/publish` - Publier article
- `POST /api/articles/{id}/archive` - Archiver article
- `POST /api/categories` - Créer catégorie
- `PUT /api/categories/{id}` - Modifier catégorie

#### **👑 Endpoints Admin (ADMIN uniquement)**
- `DELETE /api/articles/{id}` - Supprimer article
- `DELETE /api/categories/{id}` - Supprimer catégorie
- `GET /api/users` - Lister utilisateurs
- `POST /api/users` - Créer utilisateur
- `PUT /api/users/{id}` - Modifier utilisateur
- `DELETE /api/users/{id}` - Supprimer utilisateur
- `POST /api/admin/cleanup-tokens` - Nettoyer tokens

### 👥 **Utilisateurs de Test Disponibles**
```javascript
// Tous avec mot de passe : 'OusmaneSonko@2029'
const testUsers = {
  admin: { username: 'admin', password: 'OusmaneSonko@2029', role: 'ADMINISTRATEUR' },
  editeur: { username: 'editeur', password: 'OusmaneSonko@2029', role: 'EDITEUR' },
  visiteur: { username: 'visiteur', password: 'OusmaneSonko@2029', role: 'VISITEUR' }
}
```

### 📊 **Données de Test Disponibles**
- **5 catégories** : Actualités, Sport, Technologie, Football (sous Sport), Développement Web (sous Technologie)
- **5+ articles** : Variés avec statuts PUBLISHED/DRAFT
- **Base H2** en mémoire avec données rechargées à chaque redémarrage

---

## 🚀 **Plan de Développement Étape par Étape**

### **🔧 Étape 1 : Configuration de Base**
**Objectif :** Mettre en place un projet React minimal fonctionnel

#### **1.1 Initialisation Projet**
- [x] Créer dossier `frontend/` 
- [x] Initialiser projet Vite + React
- [x] Installer dépendances de base : `axios`, `react-router-dom`
- [x] Configuration proxy Vite vers backend (port 8080)

#### **1.2 Structure de Base**
```
frontend/
├── package.json
├── vite.config.js
├── index.html
└── src/
    ├── main.jsx
    ├── App.jsx
    ├── index.css
    ├── pages/
    ├── components/
    ├── services/
    └── utils/
```

#### **🧪 Test Étape 1**
1. Démarrer backend : `./gradlew bootRun` 
2. Démarrer frontend : `npm run dev`
3. Vérifier : Page blanche sans erreurs console
4. Vérifier : http://localhost:5173 accessible

---

### **📡 Étape 2 : Test Connexion Backend**
**Objectif :** Vérifier que frontend peut communiquer avec backend

#### **2.1 Service API de Base**
- [x] Créer `src/services/api.js` avec axios configuré
- [x] Ajouter fonction `testConnection()` qui appelle `/api/articles/recent`

#### **2.2 Page de Test Simple**
- [x] Créer `src/pages/TestPage.jsx`
- [x] Bouton "Tester Backend" qui appelle API
- [x] Afficher résultat : succès/erreur + données reçues

#### **🧪 Test Étape 2**
1. Cliquer sur "Tester Backend"
2. Vérifier : Statut 200 dans console réseau
3. Vérifier : Articles de test s'affichent (ou array vide)
4. Vérifier : Pas d'erreurs CORS

---

### **🏠 Étape 3 : Page d'Accueil Publique**
**Objectif :** Afficher les articles récents sans authentification

#### **3.1 Service Articles**
- [x] Créer `src/services/articleService.js`
- [x] Fonction `getRecentArticles()` 
- [x] Fonction `getPublishedArticles(page, size)`

#### **3.2 Page d'Accueil**
- [x] Créer `src/pages/HomePage.jsx`
- [x] Hook pour charger articles récents au mount
- [x] Affichage liste d'articles avec : titre, résumé, date, catégorie
- [x] Gestion état loading/error

#### **3.3 Composant Article Card**
- [x] Créer `src/components/ArticleCard.jsx`
- [x] Props : `article` object
- [x] Affichage : titre, contenu tronqué, métadonnées

#### **🧪 Test Étape 3**
1. Aller sur page d'accueil
2. Vérifier : Articles de test s'affichent correctement
3. Vérifier : Dates formatées lisiblement
4. Vérifier : Pas d'erreurs avec articles manquants

---

### **🧭 Étape 4 : Navigation et Catégories**
**Objectif :** Navigation publique par catégories

#### **4.1 Service Catégories**
- [x] Créer `src/services/categoryService.js`
- [x] Fonction `getRootCategories()`
- [x] Fonction `getCategoryById(id)`

#### **4.2 Navigation Header**
- [x] Créer `src/components/Header.jsx`
- [x] Logo + menu navigation
- [x] Liste catégories récupérée dynamiquement
- [x] Links vers `/category/{slug}`

#### **4.3 Page Catégorie**
- [x] Créer `src/pages/CategoryPage.jsx`
- [x] Route : `/category/:slug`
- [x] Afficher articles de la catégorie
- [x] Breadcrumb : Accueil > Catégorie

#### **🧪 Test Étape 4**
1. Vérifier : Menu catégories se charge
2. Cliquer sur une catégorie
3. Vérifier : Articles filtrés s'affichent
4. Vérifier : Breadcrumb correct

---

### **🔐 Étape 5 : Authentification**
**Objectif :** Système de login/logout fonctionnel

#### **5.1 Service Auth**
- [x] Créer `src/services/authService.js`
- [x] Fonction `login(username, password)`
- [x] Fonction `logout()`
- [x] Gestion token localStorage
- [x] Intercepteur axios pour token automatique

#### **5.2 Contexte Authentification**
- [x] Créer `src/contexts/AuthContext.jsx`
- [x] État utilisateur global
- [x] Fonctions login/logout partagées
- [x] Vérification token au chargement

#### **5.3 Page Login**
- [x] Créer `src/pages/LoginPage.jsx`
- [x] Formulaire : username/password
- [x] Comptes de test pré-remplis (boutons)
- [x] Redirection après connexion

#### **5.4 Protection Routes**
- [x] Créer `src/components/ProtectedRoute.jsx`
- [x] Redirection vers login si non connecté

#### **🧪 Test Étape 5**
1. Tester connexion avec `admin` / `OusmaneSonko@2029`
2. Vérifier : Token stocké dans localStorage  
3. Vérifier : Header affiche "Bonjour admin"
4. Vérifier : Déconnexion fonctionne
5. Tester les 3 comptes (admin, editeur, visiteur)

---

### **✏️ Étape 6 : Gestion Articles (Éditeurs)**
**Objectif :** CRUD articles pour éditeurs/admins

#### **6.1 Extension Service Articles**
- [x] Fonction `createArticle(data)`
- [x] Fonction `updateArticle(id, data)`
- [x] Fonction `publishArticle(id)`
- [x] Fonction `deleteArticle(id)` (admin seulement)

#### **6.2 Page Éditeur**
- [x] Créer `src/pages/EditorPage.jsx`
- [x] Route protégée : `/editor`
- [x] Liste des articles de l'utilisateur connecté
- [x] Boutons : Nouveau, Modifier, Publier, Supprimer

#### **6.3 Formulaire Article**
- [x] Créer `src/components/ArticleForm.jsx`
- [x] Champs : titre, contenu, catégorie
- [x] Validation côté client
- [x] Mode création/édition

#### **6.4 Gestion Permissions**
- [x] Vérifier rôle utilisateur avant affichage
- [x] Masquer fonctionnalités selon rôle
- [x] Visiteur → lecture seule
- [x] Éditeur → CRUD articles
- [x] Admin → + suppression

#### **🧪 Test Étape 6**
1. Connecté comme `editeur`
2. Créer nouvel article
3. Vérifier : Article créé en DRAFT
4. Publier l'article
5. Vérifier : Article apparaît dans liste publique
6. Tester avec `visiteur` : pas d'accès éditeur

---

### **👑 Étape 7 : Administration (Admins)**
**Objectif :** Interface admin pour gestion utilisateurs

#### **7.1 Service Utilisateurs**
- [ ] Créer `src/services/userService.js`
- [ ] Fonction `getUsers()`
- [ ] Fonction `createUser(data)`
- [ ] Fonction `updateUser(id, data)`
- [ ] Fonction `deleteUser(id)`

#### **7.2 Page Administration**
- [ ] Créer `src/pages/AdminPage.jsx`
- [ ] Route protégée admin : `/admin`
- [ ] Onglets : Utilisateurs, Articles, Statistiques
- [ ] Tableau utilisateurs avec actions

#### **7.3 Formulaire Utilisateur**
- [ ] Créer `src/components/UserForm.jsx`
- [ ] Champs : username, email, password, rôle
- [ ] Validation et sélection rôle

#### **🧪 Test Étape 7**
1. Connecté comme `admin`
2. Accéder à `/admin`
3. Voir liste des utilisateurs
4. Créer nouvel utilisateur
5. Tester : nouveaux utilisateurs peuvent se connecter
6. Vérifier : éditeur/visiteur n'ont pas accès admin

---

### **🎨 Étape 8 : Interface Utilisateur**
**Objectif :** Améliorer UX et design

#### **8.1 Styles CSS**
- [ ] CSS modules ou styled-components
- [ ] Design responsive mobile-first
- [ ] Palette couleurs cohérente

#### **8.2 Composants UI**
- [ ] Boutons standardisés
- [ ] Modales pour confirmations
- [ ] Notifications toast
- [ ] Loading spinners

#### **8.3 Gestion Erreurs**
- [ ] Page 404
- [ ] Messages d'erreur utilisateur
- [ ] Fallback pour erreurs réseau

#### **🧪 Test Étape 8**
1. Tester sur mobile/tablette
2. Vérifier : Design cohérent
3. Tester : Gestion erreurs réseau
4. Vérifier : Accessibilité de base

---

## 🎯 **Critères de Validation par Étape**

### **✅ Critères de Succès Généraux**
- [ ] Aucune erreur console
- [ ] Communication backend fonctionnelle
- [ ] Authentification JWT opérationnelle
- [ ] Permissions par rôle respectées
- [ ] Interface responsive
- [ ] Gestion erreurs appropriée

### **📝 Tests Manuels à Effectuer**

#### **Test Complet Workflow :**
1. **Visiteur** : Navigation publique, lecture articles
2. **Éditeur** : Connexion, création article, publication
3. **Admin** : Gestion utilisateurs, supervision complète

#### **Test Sécurité :**
1. Vérifier : Routes protégées redirigent vers login
2. Vérifier : JWT expiré gère correctement
3. Vérifier : Permissions rôles strictement appliquées

---

## 🚀 **Commandes de Développement**

```bash
# Backend (terminal 1)
cd backend && ./gradlew bootRun

# Frontend (terminal 2) 
cd frontend && npm run dev

# URLs de test
# Frontend: http://localhost:5173
# Backend: http://localhost:8080
# H2 Console: http://localhost:8080/h2-console
# Swagger: http://localhost:8080/swagger-ui.html
```

---

## 📋 **Checklist Validation Finale**

### **🔐 Authentification**
- [ ] Login avec 3 types utilisateurs
- [ ] Logout fonctionnel  
- [ ] Token persistence
- [ ] Redirection appropriée

### **📰 Articles**
- [ ] Liste publique articles
- [ ] Détail article
- [ ] Création (éditeur+)
- [ ] Publication (éditeur+)
- [ ] Suppression (admin)

### **📁 Catégories**
- [ ] Navigation par catégorie
- [ ] Hiérarchie affichée
- [ ] Filtrage articles

### **👥 Utilisateurs**
- [ ] Liste utilisateurs (admin)
- [ ] Création utilisateur (admin)
- [ ] Gestion permissions

### **🎨 UX/UI**
- [ ] Design cohérent
- [ ] Responsive design
- [ ] Gestion erreurs
- [ ] Performance acceptable

---

**🎯 Résultat attendu :** Frontend React fonctionnel permettant de tester toutes les fonctionnalités du backend avec une interface utilisateur simple mais complète. 