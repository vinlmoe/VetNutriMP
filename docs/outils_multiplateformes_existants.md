# Outils Multiplateformes Existants dans VetNutriMP

## 1. Gestion des Dispatchers

### AppDispatchers (Utils/Dispatchers.kt)
- **IO** : Pour les opérations de base de données et I/O
- **Default** : Pour les opérations CPU-intensives  
- **Main** : Pour les opérations UI
- Utilise `PlatformDispatcher` pour l'injection de dépendances

### PlatformDispatcher (Utils/MainDispatcher.kt)
- Interface expect/actual pour fournir les dispatchers spécifiques à chaque plateforme
- `provideMainDispatcher()` : Dispatcher principal pour UI
- `provideIODispatcher()` : Dispatcher pour I/O

## 2. Gestion des Fichiers

### FileService (Service/FileService.kt)
- Interface expect/actual déjà implémentée
- Implémentations existantes :
  - **Android** : `FileService.android.kt` (utilise Context)
  - **Desktop** : `FileService.desktop.kt` (utilise System.getProperty)
  - **iOS** : `FileService.ios.kt` (à corriger)

### Méthodes disponibles :
- `getBackupDirectory()` : Répertoire de sauvegarde
- `getDataDirectory()` : Répertoire de données
- `createDirectoryIfNotExists()` : Création de répertoires
- `fileExists()` : Vérification d'existence
- `getFileSize()` : Taille de fichier
- `deleteFile()` : Suppression de fichier
- `listFiles()` : Liste des fichiers
- `copyFile()` : Copie de fichier
- `moveFile()` : Déplacement de fichier

## 3. Problèmes Identifiés

### Dans commonMain :
1. **Imports Java** : `java.io.File`, `java.lang.System` utilisés dans `BackupService.kt`
2. **Dispatchers.IO** : Utilisé directement au lieu d'`AppDispatchers.IO`
3. **Fonction format** : `Double.format()` non définie pour iOS
4. **BaseFileService** : Utilise `java.io.File` au lieu de l'interface expect

### Solutions Recommandées :
1. Utiliser `AppDispatchers.IO` au lieu de `Dispatchers.IO`
2. Remplacer `java.io.File` par l'interface `FileService` existante
3. Créer une fonction `format` multiplateforme
4. Utiliser `PlatformSystem.currentTimeMillis()` au lieu de `System.currentTimeMillis()`

## 4. Architecture Recommandée

```
commonMain/
├── Service/
│   ├── FileService.kt (expect interface)
│   └── BackupService.kt (utilise FileService)
├── Utils/
│   ├── Dispatchers.kt (AppDispatchers)
│   └── MainDispatcher.kt (PlatformDispatcher)
└── Platform/
    └── PlatformFile.kt (si nécessaire)

androidMain/desktopMain/iosMain/
├── Service/
│   └── FileService.{platform}.kt (actual implementation)
└── Utils/
    └── MainDispatcher.{platform}.kt (actual implementation)
```

## 5. Actions à Effectuer

1. ✅ Supprimer `PlatformFile.kt` (redondant avec FileService)
2. ✅ Corriger `BackupService.kt` pour utiliser `FileService` et `AppDispatchers`
3. ✅ Corriger `AlimentItem.kt` pour la fonction `format`
4. ✅ Corriger `FileService.ios.kt` pour utiliser les bonnes APIs iOS
5. ✅ Remplacer tous les `Dispatchers.IO` par `AppDispatchers.IO`
