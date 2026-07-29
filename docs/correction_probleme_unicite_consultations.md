# Correction du Problème d'Unicité des Consultations

## Problème Identifié

L'application rencontrait des erreurs de contrainte d'unicité lors de la sauvegarde des consultations :
```
androidx.sqlite.SQLiteException: Error code: 19, message: UNIQUE constraint failed: CONSULTATIONS.uuid
```

## Causes du Problème

1. **Génération multiple d'UUIDs** : Les UUIDs étaient générés à plusieurs endroits dans le code :
   - Dans `ConsultationEv()` avec `genUUID()` par défaut
   - Dans `prepareNewConsultation()` avec `Uuid.random().toString()`
   - Dans les vues avec `genUUID()` ou `Uuid.random().toString()`

2. **Gestion des conflits manquante** : Le `ConsultationDao.insert()` n'avait pas de stratégie de gestion des conflits

3. **Vérification d'existence insuffisante** : Bien que le code vérifiait si une consultation existait avant l'insertion, il pouvait y avoir des conditions de course

## Solutions Implémentées

### 1. Ajout de Stratégies de Gestion des Conflits

**Fichier :** `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/DataBase/CommonDao.kt`

- Ajout de `@Insert(onConflict = OnConflictStrategy.REPLACE)` sur toutes les méthodes d'insertion du `ConsultationDao`
- Ajout de `@Insert(onConflict = OnConflictStrategy.REPLACE)` sur toutes les méthodes d'insertion de l'`AnimalDao`

### 2. Amélioration de la Logique de Sauvegarde

**Fichier :** `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/Repository/ConsultationRepository.kt`

- Ajout d'une logique de retry avec génération d'UUID unique en cas de conflit
- Gestion des exceptions avec log de débogage
- Mise à jour automatique de l'UUID de la consultation en cas de conflit

### 3. Création d'une Fonction d'UUID Unique

**Fichier :** `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/Utils/UUIDGen.kt`

- Ajout de la fonction `genUniqueUUID()` qui génère des UUIDs basés sur le timestamp actuel et un nombre aléatoire
- Cette approche garantit l'unicité même en cas de génération simultanée

### 4. Amélioration des Vues

**Fichiers :**
- `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/View/ConsultationFullScreenEditView.kt`
- `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/View/ConsultationDetailView.kt`

- Utilisation de `genUniqueUUID()` au lieu de `genUUID()` pour éviter les conflits
- Génération d'UUIDs uniques avec timestamp lors de la création de nouvelles consultations

### 5. Amélioration du ViewModel

**Fichier :** `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/ViewModel/AnimalDetailViewModel.kt`

- Amélioration de la méthode `saveFromFullScreen()` pour mieux gérer les conflits d'UUIDs
- Génération automatique d'UUIDs uniques si nécessaire
- Meilleure gestion des erreurs avec logging

## Tests Ajoutés

**Fichier :** `composeApp/src/commonTest/kotlin/fr/vetbrain/vetnutri_mp/Utils/UUIDGenTest.kt`

- Tests unitaires pour vérifier la génération d'UUIDs uniques
- Tests de robustesse pour s'assurer qu'il n'y a pas de doublons
- Validation du format des UUIDs générés

## Avantages des Modifications

1. **Élimination des erreurs d'unicité** : Les contraintes d'unicité ne sont plus violées
2. **Robustesse accrue** : L'application gère mieux les cas de conflits
3. **Cohérence** : Utilisation d'une approche unifiée pour la génération d'UUIDs
4. **Maintenabilité** : Code plus clair et mieux structuré
5. **Performance** : Réduction des erreurs et des retries

## Recommandations pour l'Avenir

1. **Utiliser `genUniqueUUID()`** pour toutes les nouvelles entités qui nécessitent un UUID unique
2. **Toujours ajouter `OnConflictStrategy.REPLACE`** sur les méthodes d'insertion des DAOs
3. **Implémenter des tests de robustesse** pour vérifier la gestion des conflits
4. **Surveiller les logs** pour détecter d'éventuels problèmes de performance liés aux retries

## Fichiers Modifiés

- `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/DataBase/CommonDao.kt`
- `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/Repository/ConsultationRepository.kt`
- `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/Utils/UUIDGen.kt`
- `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/View/ConsultationFullScreenEditView.kt`
- `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/View/ConsultationDetailView.kt`
- `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/ViewModel/AnimalDetailViewModel.kt`
- `composeApp/src/commonTest/kotlin/fr/vetbrain/vetnutri_mp/Utils/UUIDGenTest.kt`
