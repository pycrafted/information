# Cahier des Charges – Projet d’Architecture Logicielle

## 1. Contexte et Objectif

Ce projet vise à mettre en œuvre une architecture logicielle multicouche respectant les bonnes pratiques de développement logiciel enseignées dans le cours. Il s'agit de concevoir et développer un système complet d'actualités avec des services web et une application cliente. Le projet doit être conçu de manière modulaire, testable, maintenable et évolutive.

## 2. Contraintes Générales

- **Séparation stricte en 5 couches logiques** : 
  - Présentation
  - Contrôle
  - Service
  - Domaine
  - Persistance

- **Respect rigoureux des responsabilités de chaque couche.**
- **Utilisation des Design Patterns recommandés** : MVC, DAO, etc.
- **Respect total des principes SOLID et Clean Code.**
- **Couplage faible entre les couches, haute cohésion.**
- **Mise en place de tests unitaires et tests d’intégration.**
- **Livraison du code sur un dépôt GitHub public.**

---

## 3. Fonctionnalités attendues

### 3.1 Site Web (Côté utilisateur)

#### 🧾 Accueil
- Affichage des derniers articles avec description sommaire
- Pagination avec boutons "suivant" / "précédent"

#### 📄 Consultation d’un article
- Affichage détaillé au clic sur le titre

#### 🗂 Affichage par catégories
- Liste des articles filtrés par catégorie

### 3.2 Gestion des utilisateurs selon leur rôle

| Rôle | Accès |
|------|-------|
| **Visiteur** | Lecture uniquement |
| **Éditeur** | CRUD Articles + Catégories |
| **Administrateur** | CRUD Utilisateurs + gestion des jetons d’authentification |

---

## 4. Services Web

### 4.1 Services SOAP (Sécurisés par jeton)

- Authentification d’un utilisateur (login + mot de passe)
- Gestion des utilisateurs : lister, ajouter, modifier, supprimer

### 4.2 Services REST

- Obtenir tous les articles (format JSON ou XML)
- Obtenir les articles groupés par catégorie
- Obtenir les articles d'une catégorie donnée

---

## 5. Application Client (Java ou Python)

- Authentification via Web Service SOAP
- Interface CLI ou GUI permettant :
  - L’ajout, la modification, la suppression et la liste des utilisateurs
- Appels sécurisés aux services web via jetons d’authentification

---

## 6. Architecture en 5 couches

### 6.1 Couche Présentation (Frontend ou IHM)
- HTML/CSS/JS ou framework web
- Interface CLI ou GUI pour l’application cliente
- Ne contient aucune logique métier
- Respecte le principe MVC

### 6.2 Couche Contrôle (Contrôleurs)
- Reçoit les requêtes utilisateur (HTTP ou GUI)
- Valide les entrées
- Appelle les services métiers appropriés
- Gère les exceptions, sessions et droits d’accès

### 6.3 Couche Service
- Implémente la logique métier
- Expose les cas d’utilisation
- Gère la sécurité applicative
- Orchestration des appels aux objets du domaine
- Traite les conversions DTO ↔ Objet métier

### 6.4 Couche Domaine
- Contient les entités métier (Article, Catégorie, Utilisateur)
- Contient les règles métier pures (ex : validation métier)
- Entièrement indépendante des frameworks et technologies

### 6.5 Couche Persistance
- Gère l’accès aux données (JPA, JDBC, etc.)
- Implémentation des DAO
- Mapping Objet/Relationnel
- Stockage en base ou fichier
- Ne contient aucune logique métier

---

## 7. Bonnes Pratiques à Respecter

- Respect **strict du modèle en 5 couches**
- **Pas de fuite de responsabilité entre les couches**
- **Tests unitaires** : Couche Domaine et Service
- **Tests d’intégration** : Services Web SOAP/REST
- Respect des principes :
  - **SOLID**
  - **KISS** (Keep It Simple, Stupid)
  - **DRY** (Don't Repeat Yourself)
  - **YAGNI** (You Aren't Gonna Need It)

- Documentation claire (Javadoc ou équivalent)
- Convention de nommage uniforme
- Gestion des erreurs centralisée
- Utilisation de DTOs pour exposer les données

---

## 8. Livraison attendue

- Projet sur **GitHub public**
- Structure claire avec séparation stricte des 5 couches
- Dossier `tests` pour les tests unitaires et d’intégration
- README.md documentant :
  - L'architecture du projet
  - Le mode de déploiement
  - La documentation API (Swagger ou WSDL si SOAP)
- Respect des deadlines et du format d’envoi par email

---

## 9. Technologies recommandées

- Backend : Spring Boot (Java) ou Flask/FastAPI (Python)
- Frontend : React, Angular ou simple HTML/CSS/JS
- BDD : PostgreSQL ou MySQL
- SOAP : JAX-WS ou Zeep
- REST : Spring REST ou Flask/DRF
- Tests : JUnit / Pytest / Postman / SoapUI

---

## 10. Rappel : Critères de Notation

- Respect des **fonctionnalités demandées**
- **Qualité de l’architecture** (5 couches)
- **Clarté du code** et respect du clean code
- **Complétude des tests**
- **Documentation**
- **Travail en équipe (max 3)**
- **Livraison sur Git à temps**

---
