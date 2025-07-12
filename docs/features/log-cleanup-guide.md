# Guide du Nettoyage Automatique des Logs

## 📋 Vue d'ensemble

Cette fonctionnalité nettoie automatiquement les fichiers de logs au démarrage de l'application pour éviter l'accumulation excessive et maintenir des performances optimales.

## 🚀 Fonctionnalités

### ✨ Nettoyage Automatique
- **Démarrage** : Se lance automatiquement au démarrage de l'application
- **Archivage** : Archive les logs existants avant suppression (configurable)
- **Sélectif** : Ne supprime que les fichiers `.log` et `.log.gz`
- **Sécurisé** : Gère gracieusement les erreurs sans bloquer l'application

### 📊 Types de Logs Nettoyés
- `newsplatform-application.log`
- `newsplatform-errors.log`
- `newsplatform-security.log`
- `newsplatform-performance.log`
- Tous les fichiers `.log.gz` archivés

## ⚙️ Configuration

### Configuration dans `application.yml`

```yaml
# Configuration du nettoyage automatique des logs
app:
  logs:
    cleanup:
      # Active ou désactive le nettoyage automatique des logs au démarrage
      enabled: true
      # Archive les logs existants avant suppression (true) ou les supprime directement (false)
      archive-before-delete: true
      # Délai en secondes avant le nettoyage pour permettre l'initialisation
      delay-seconds: 2

logging:
  file:
    path: ./logs
```

### Paramètres Disponibles

| Paramètre | Type | Défaut | Description |
|-----------|------|--------|-------------|
| `app.logs.cleanup.enabled` | boolean | `true` | Active/désactive le nettoyage |
| `app.logs.cleanup.archive-before-delete` | boolean | `true` | Archive avant suppression |
| `app.logs.cleanup.delay-seconds` | int | `2` | Délai avant nettoyage (secondes) |
| `logging.file.path` | string | `./logs` | Répertoire des logs |

## 🎯 Utilisation

### Démarrage Normal
```bash
# Démarrage avec nettoyage activé (par défaut)
./gradlew bootRun
```

### Désactiver le Nettoyage
```bash
# Via variable d'environnement
APP_LOGS_CLEANUP_ENABLED=false ./gradlew bootRun

# Ou via application.yml
app:
  logs:
    cleanup:
      enabled: false
```

### Désactiver l'Archivage
```bash
# Suppression directe sans archivage
APP_LOGS_CLEANUP_ARCHIVE_BEFORE_DELETE=false ./gradlew bootRun
```

## 📝 Logs de Fonctionnement

### Nettoyage Normal
```
🚀 Démarrage de l'application - Vérification du nettoyage des logs
⏱️  Attente de 2 seconde(s) avant le nettoyage des logs...
🎯 Lancement du nettoyage automatique des logs
🧹 Début du nettoyage des logs au démarrage de l'application
📋 Trouvé 4 fichier(s) de log à traiter
📦 Archivage des logs existants...
✅ Archivage terminé dans : ./logs/archive/2025-07-11_01-12-53
🗑️  Suppression des logs actuels...
✅ Supprimé 4 fichier(s) de log (total : 6.1 MB)
✅ Nettoyage des logs terminé avec succès
🎉 Nettoyage automatique des logs terminé avec succès
```

### Nettoyage Désactivé
```
🚀 Démarrage de l'application - Vérification du nettoyage des logs
⏭️  Nettoyage des logs désactivé via configuration (app.logs.cleanup.enabled=false)
```

### Aucun Log à Nettoyer
```
🚀 Démarrage de l'application - Vérification du nettoyage des logs
🧹 Début du nettoyage des logs au démarrage de l'application
✅ Aucun fichier de log à nettoyer dans : ./logs
```

## 🗂️ Structure des Archives

Quand l'archivage est activé, les logs sont sauvegardés dans :

```
logs/
├── archive/
│   ├── 2025-07-11_01-12-53/
│   │   ├── newsplatform-application.log
│   │   ├── newsplatform-errors.log
│   │   ├── newsplatform-security.log
│   │   └── newsplatform-performance.log
│   └── 2025-07-11_02-15-30/
│       └── ...
└── (nouveaux logs après nettoyage)
```

## 🛠️ Test et Démonstration

### 1. Créer des Logs de Test
```bash
# Créer le répertoire de logs
mkdir -p backend/logs

# Créer des fichiers de logs factices
echo "Test application logs" > backend/logs/newsplatform-application.log
echo "Test error logs" > backend/logs/newsplatform-errors.log
echo "Test security logs" > backend/logs/newsplatform-security.log

# Vérifier la taille
ls -lh backend/logs/
```

### 2. Démarrer l'Application
```bash
cd backend
./gradlew bootRun
```

### 3. Observer les Logs
Vous verrez dans la console les messages de nettoyage automatique.

### 4. Vérifier le Résultat
```bash
# Vérifier que les logs ont été nettoyés
ls -la backend/logs/

# Vérifier l'archive (si activée)
ls -la backend/logs/archive/
```

## ⚡ Avantages

### 🚀 Performance
- **Réduction de l'espace disque** : Évite l'accumulation de logs volumineux
- **Démarrage plus rapide** : Supprime les anciens logs qui ralentissent l'accès
- **Maintenance automatique** : Pas d'intervention manuelle nécessaire

### 🔒 Sécurité
- **Archivage optionnel** : Conservation des logs importants si nécessaire
- **Gestion d'erreurs** : N'empêche pas le démarrage de l'application
- **Logs préservés** : Ne touche que les fichiers de logs identifiés

### 📈 Monitoring
- **Logs détaillés** : Chaque opération est journalisée
- **Métriques** : Affichage des tailles et nombres de fichiers traités
- **Configuration flexible** : Adaptation à tous les environnements

## 🔧 Dépannage

### Problème : Logs Non Supprimés
**Cause** : Permissions insuffisantes ou fichiers en cours d'utilisation
**Solution** :
```bash
# Vérifier les permissions
ls -la backend/logs/

# Arrêter complètement l'application avant redémarrage
./gradlew bootStop
```

### Problème : Archivage Échoué
**Cause** : Espace disque insuffisant
**Solution** :
```yaml
# Désactiver l'archivage temporairement
app:
  logs:
    cleanup:
      archive-before-delete: false
```

### Problème : Délai Trop Court
**Cause** : Nettoyage avant initialisation complète
**Solution** :
```yaml
# Augmenter le délai
app:
  logs:
    cleanup:
      delay-seconds: 5
```

## 📚 Architecture Technique

### Composants
- **LogCleanupService** : Service de nettoyage (couche Service)
- **LogCleanupRunner** : Composant de démarrage (couche Configuration)
- **Configuration YAML** : Paramètres d'application

### Principes Respectés
- ✅ **Clean Code** : Code lisible et maintenable
- ✅ **Séparation des couches** : Architecture en 5 couches respectée
- ✅ **Tests unitaires** : Couverture complète des fonctionnalités
- ✅ **Gestion d'erreurs** : Robustesse et résilience
- ✅ **Configuration** : Paramétrage flexible

## 🎯 Cas d'Usage

### Développement Local
```yaml
app:
  logs:
    cleanup:
      enabled: true
      archive-before-delete: false  # Suppression directe
      delay-seconds: 1
```

### Environnement de Production
```yaml
app:
  logs:
    cleanup:
      enabled: true
      archive-before-delete: true   # Conservation par sécurité
      delay-seconds: 5              # Délai plus long
```

### Environnement de Test
```yaml
app:
  logs:
    cleanup:
      enabled: false  # Préservation pour débogage
```

---

*Cette fonctionnalité respecte l'architecture en 5 couches du projet et suit les principes de clean code pour une maintenance optimale.* 