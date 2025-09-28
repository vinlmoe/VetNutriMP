# Améliorations Import/Export Excel - VetNutriMP

## Vue d'ensemble

L'implémentation de l'import/export Excel a été considérablement améliorée pour offrir une intégration complète avec les repositories existants et une gestion des fichiers multiplateforme.

## Nouvelles fonctionnalités

### 1. Service Excel intégré (`ExcelFoodService`)

**Fichier**: `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/Services/ExcelFoodService.kt`

- **Intégration complète** avec `FoodRepository`
- **Export réel** de tous les aliments ou d'une sélection
- **Import avec persistance** en base de données
- **Prévisualisation** avant import final
- **Gestion d'erreurs** détaillée

#### Méthodes principales :

```kotlin
// Export
suspend fun exportAllFoodsToCsv(): String
suspend fun exportSelectedFoodsToCsv(foodIds: Set<String>): String

// Import
suspend fun importFoodsFromCsv(csvContent: String): ExcelImportResult
suspend fun previewCsvImport(csvContent: String): ExcelImportResult

// Utilitaires
fun generateExampleCsv(): String
```

### 2. Gestion des fichiers multiplateforme

**Fichiers**:
- `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/Platform/ExcelFileOperations.kt` (interface)
- `composeApp/src/desktopMain/kotlin/fr/vetbrain/vetnutri_mp/Platform/ExcelFileOperations.desktop.kt` (Desktop)
- `composeApp/src/androidMain/kotlin/fr/vetbrain/vetnutri_mp/Platform/ExcelFileOperations.android.kt` (Android)
- `composeApp/src/iosMain/kotlin/fr/vetbrain/vetnutri_mp/Platform/ExcelFileOperations.ios.kt` (iOS)

#### Fonctions disponibles :

```kotlin
// Sélection de fichier pour import
expect fun openCsvFileForImport(): String?

// Sauvegarde de fichier pour export
expect fun saveCsvFileForExport(csvContent: String, defaultFileName: String): Boolean

// Vérification du support
expect fun isCsvFileOperationsSupported(): Boolean
```

### 3. Interface utilisateur améliorée

**Fichier**: `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/View/ExcelImportExportComponents.kt`

#### Améliorations :

- **Intégration avec repositories** : Utilise le `FoodRepository` réel
- **Gestion des fichiers** : Sélection et sauvegarde de fichiers réels
- **Prévisualisation** : Dialogue d'import avec détails des erreurs
- **Indicateurs de progression** : Feedback visuel pendant les opérations
- **Gestion d'erreurs** : Affichage détaillé des erreurs et statistiques
- **Support multiplateforme** : Détection automatique du support CSV

#### Nouveau dialogue d'import :

```kotlin
ExcelImportDialog(
    result: ExcelFoodService.ExcelImportResult,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
)
```

### 4. Intégration dans SettingsView

**Fichier**: `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/View/SettingsView.kt`

L'onglet "Excel Import/Export" utilise maintenant :

```kotlin
val excelFoodService = remember {
    viewModel.foodRepository?.let { foodRepo ->
        ExcelFoodService(foodRepo)
    }
}
ExcelImportExportSection(
    modifier = Modifier.fillMaxWidth(),
    excelFoodService = excelFoodService
)
```

## Structure des données

### ExcelImportResult

```kotlin
data class ExcelImportResult(
    val success: Boolean,
    val message: String,
    val importedCount: Int = 0,
    val updatedCount: Int = 0,
    val errorCount: Int = 0,
    val errors: List<String> = emptyList(),
    val previewMode: Boolean = false
)
```

## Support des plateformes

| Plateforme | Support CSV | File Picker | Sauvegarde |
|------------|-------------|-------------|------------|
| **Desktop** | ✅ | ✅ (JFileChooser) | ✅ |
| **Android** | ✅ | ✅ (ActivityResultContracts) | ✅ |
| **iOS** | ⚠️ | ❌ (TODO) | ❌ (TODO) |

## Utilisation

### 1. Export d'aliments

```kotlin
val excelService = ExcelFoodService(foodRepository)

// Export de tous les aliments
val csvContent = excelService.exportAllFoodsToCsv()
val success = saveCsvFileForExport(csvContent, "aliments_export.csv")

// Export d'une sélection
val selectedIds = setOf("uuid1", "uuid2")
val selectedCsv = excelService.exportSelectedFoodsToCsv(selectedIds)
```

### 2. Import d'aliments

```kotlin
val excelService = ExcelFoodService(foodRepository)

// Sélection et prévisualisation
val csvContent = openCsvFileForImport()
if (csvContent != null) {
    val preview = excelService.previewCsvImport(csvContent)
    // Afficher la prévisualisation...
    
    // Import final
    val result = excelService.importFoodsFromCsv(csvContent)
}
```

### 3. Génération d'exemple

```kotlin
val exampleCsv = excelService.generateExampleCsv()
// Utiliser pour créer un modèle de fichier CSV
```

## Format CSV

Le format CSV utilise la structure suivante :

- **Séparateur** : Point-virgule (`;`)
- **Encodage** : UTF-8
- **Colonnes principales** : 17 colonnes (UUID, nom, marque, etc.)
- **Colonnes de nutriments** : 76 colonnes (1 par nutriment)
  - **Format** : `NOM_NUTRIMENT (unité)` dans l'en-tête
  - **Valeur** : Valeur numérique du nutriment
- **Total** : 93 colonnes par aliment

### Exemple de colonnes de nutriments

```
HUMIDITE (g);PROTEINE (g);LIPIDE (g);ENERGIE (kcal);...
85.5;32.0;12.0;350.0;...
```

### Unités utilisées

Les unités sont basées sur l'enum `UnitEnum` défini dans le système :

| Label | Unité | Description |
|-------|-------|-------------|
| `BUg` | g | Gramme |
| `BUmg` | mg | Milligramme |
| `BUmu` | µg | Microgramme |
| `KCAL` | kcal | Kilocalorie |
| `AUui` | UI | Unité Internationale (Vitamine A) |
| etc. | | |

**Important** : L'unité est maintenant intégrée directement dans l'en-tête de la colonne (ex: "PROTEINE (g)"). Plus besoin de colonnes séparées pour les unités, ce qui simplifie le format CSV.

## Avantages des améliorations

1. **Intégration réelle** : Plus de simulation, utilisation des repositories
2. **Persistance** : Les imports sont sauvegardés en base de données
3. **Multiplateforme** : Support Desktop et Android (iOS en cours)
4. **Gestion d'erreurs** : Feedback détaillé pour l'utilisateur
5. **Prévisualisation** : Vérification avant import final
6. **Performance** : Utilisation des dispatchers appropriés
7. **Maintenabilité** : Code modulaire et testable

## Prochaines étapes

1. **iOS** : Implémenter UIDocumentPickerViewController
2. **Tests** : Ajouter des tests unitaires pour le service
3. **Validation** : Améliorer la validation des données CSV
4. **Performance** : Optimiser pour de gros volumes de données
5. **UI/UX** : Améliorer l'interface de prévisualisation
