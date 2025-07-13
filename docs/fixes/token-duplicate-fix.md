# Correction du problème de tokens en double

## Problème identifié

Le système rencontrait des erreurs `NonUniqueResultException` lors de la validation des tokens d'authentification, causant des déconnexions inattendues et empêchant la création d'articles.

### Symptômes
- Erreurs dans les logs : `Query did not return a unique result: 2 results were returned`
- Déconnexions automatiques lors de la création d'articles
- Échec de l'authentification après refresh de tokens

### Cause racine
Les tokens d'authentification et de refresh étaient créés en double dans la base de données, probablement à cause de :
1. Requêtes simultanées de refresh
2. Gestion incorrecte des transactions
3. Absence de nettoyage automatique des doublons

## Solutions implémentées

### 1. Service de nettoyage automatique (`TokenCleanupService`)

#### Fonctionnalités ajoutées :
- **Nettoyage immédiat** : `cleanupDuplicateTokensForValue(String tokenValue)`
  - Appelé après chaque génération de token
  - Nettoie les doublons pour une valeur spécifique
  - Garde le token le plus récent et valide

- **Nettoyage programmé** : `scheduledCleanup()`
  - Exécution toutes les 5 minutes (au lieu d'1 heure)
  - Nettoyage complet de tous les tokens en double

- **Nettoyage des tokens expirés** : `cleanupExpiredTokens()`
  - Exécution quotidienne
  - Suppression des tokens expirés depuis plus de 30 jours

#### Logique de nettoyage :
```java
// Pour chaque groupe de tokens avec la même valeur
if (duplicates.size() > 1) {
    // Garder le token le plus récent et valide
    AuthToken validToken = duplicates.stream()
        .filter(AuthToken::isValid)
        .max((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()))
        .orElse(null);
    
    // Supprimer les autres tokens
    if (validToken != null) {
        List<AuthToken> tokensToDelete = duplicates.stream()
            .filter(token -> !token.getId().equals(validToken.getId()))
            .toList();
        authTokenRepository.deleteAll(tokensToDelete);
    }
}
```

### 2. Intégration dans le `TokenService`

#### Modifications apportées :
- **Injection du `TokenCleanupService`** dans le constructeur
- **Nettoyage après génération** d'access token
- **Nettoyage après génération** de refresh token
- **Nettoyage lors du refresh** d'access token

#### Code ajouté :
```java
// Après génération d'access token
AuthToken savedToken = authTokenRepository.save(authToken);
tokenCleanupService.cleanupDuplicateTokensForValue(savedToken.getTokenValue());
return savedToken;

// Après génération de refresh token
RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
tokenCleanupService.cleanupDuplicateTokensForValue(savedToken.getTokenValue());
return savedToken;

// Lors du refresh d'access token
tokenCleanupService.cleanupDuplicateTokensForValue(refreshTokenValue);
```

### 3. Amélioration du repository

#### Méthodes ajoutées :
- `AuthTokenRepository.findAllByTokenValue(String tokenValue)`
- `RefreshTokenRepository.findAllByTokenValue(String tokenValue)`

#### Gestion des erreurs :
- Capture des `NonUniqueResultException`
- Appel automatique du nettoyage en cas de doublons
- Récupération du token valide après nettoyage

### 4. Endpoint d'administration

#### Endpoint existant amélioré :
- `POST /api/admin/cleanup-tokens`
- Déclenchement manuel du nettoyage
- Accessible uniquement aux administrateurs
- Retourne le statut de l'opération

## Tests et validation

### Script de test créé
- `scripts/test-token-cleanup.sh`
- Teste la connexion, refresh, nettoyage et création d'articles
- Valide que le problème est résolu

### Scénarios testés :
1. **Connexion normale** : Vérification de l'authentification
2. **Refresh multiple** : Création intentionnelle de doublons
3. **Nettoyage manuel** : Test de l'endpoint admin
4. **Authentification post-nettoyage** : Vérification de la continuité
5. **Création d'article** : Test du cas problématique

## Configuration

### Scheduling activé
```java
@EnableScheduling
public class NewsPlatformApplication {
    // ...
}
```

### Fréquences de nettoyage :
- **Nettoyage des doublons** : Toutes les 5 minutes
- **Nettoyage des expirés** : Toutes les 24 heures
- **Nettoyage immédiat** : Après chaque génération de token

## Monitoring et logs

### Logs ajoutés :
- `🔍 Nettoyage immédiat des tokens en double`
- `🗑️ Supprimé X tokens en double`
- `⏰ Nettoyage automatique des tokens en double`
- `✅ Nettoyage terminé - X auth tokens, Y refresh tokens supprimés`

### Métriques disponibles :
- Nombre de tokens supprimés par opération
- Fréquence des nettoyages
- Erreurs de nettoyage

## Impact et bénéfices

### Résolution des problèmes :
- ✅ Plus d'erreurs `NonUniqueResultException`
- ✅ Création d'articles fonctionnelle
- ✅ Authentification stable
- ✅ Pas de déconnexions inattendues

### Améliorations de performance :
- Réduction de la taille de la base de données
- Optimisation des requêtes d'authentification
- Prévention de l'accumulation de tokens inutiles

### Sécurité renforcée :
- Suppression automatique des tokens expirés
- Nettoyage des tokens invalides
- Prévention des attaques par accumulation de tokens

## Instructions d'utilisation

### Pour les développeurs :
1. Le nettoyage est automatique, aucune action requise
2. En cas de problème, utiliser l'endpoint `/api/admin/cleanup-tokens`
3. Surveiller les logs pour détecter les anomalies

### Pour les administrateurs :
1. Accéder à l'endpoint de nettoyage via l'interface admin
2. Surveiller les métriques de nettoyage
3. Ajuster les fréquences si nécessaire

## Maintenance

### Surveillance recommandée :
- Logs de nettoyage automatique
- Taille de la table `auth_tokens`
- Taille de la table `refresh_tokens`
- Fréquence des erreurs d'authentification

### Optimisations futures possibles :
- Index sur `token_value` pour améliorer les performances
- Partitionnement des tables de tokens
- Archivage des tokens anciens 