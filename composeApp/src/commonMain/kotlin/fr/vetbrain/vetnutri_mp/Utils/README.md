# Dossier Utils

Ce dossier contient des classes et fonctions utilitaires qui sont utilisées dans toute l'application VetNutri_MP. Ces utilitaires fournissent des fonctionnalités communes et réutilisables.

## Fichiers principaux

### ImportUtils.kt

Ce fichier contient les utilitaires d'importation de données, notamment pour les animaux et les aliments à partir de fichiers JSON.

**Fonctions principales :**
- `importAnimalsFromJson` : Importe une liste d'animaux à partir d'une chaîne JSON
- `importFoodsFromJson` : Importe une liste d'aliments à partir d'une chaîne JSON
- `preprocessEspecesAndNutrientValues` : Prétraite les valeurs nutritionnelles et les espèces dans un JSON
- `extractFoodsFromAnimalJson` : Extrait les aliments d'un JSON contenant des animaux et leurs rations
- `analyzeAnimalJsonStructure` : Analyse et valide la structure d'un fichier JSON d'animaux
- `analyzeEspecesInFoodJson` : Analyse les espèces des aliments dans un fichier JSON

### DateUtils.kt

Utilitaires pour manipuler les dates et effectuer des conversions entre différents formats.

### StringUtils.kt

Utilitaires pour manipuler les chaînes de caractères, notamment pour le formatage et la normalisation.

### MathUtils.kt

Fonctions mathématiques utilitaires pour les calculs nutritionnels.

### ExportUtils.kt

Utilitaires pour l'exportation de données, notamment au format JSON.

### FileUtils.kt

Utilitaires pour la manipulation de fichiers.

## Caractéristiques principales

- **Traitement de JSON** : Fonctions dédiées à l'importation et l'exportation de données au format JSON
- **Normalisation** : Normalisation des valeurs (comme les noms de nutriments) pour assurer la cohérence
- **Analyse de données** : Outils d'analyse et de validation de données
- **Gestion des erreurs** : Mécanismes robustes pour la gestion des erreurs lors de l'importation
- **Conversions** : Fonctions pour convertir entre différents formats et structures

## Relations avec d'autres modules

- **Data** : Utilise les classes DTO pour la sérialisation/désérialisation
- **Enumer** : Utilise les énumérations pour résoudre et normaliser les valeurs
- **ViewModel** : Les ViewModels utilisent ces utilitaires pour importer et traiter les données
- **Repository** : Les repositories utilisent ces fonctions pour stocker les données importées

## Exemple d'utilisation

```kotlin
// Importer des animaux depuis un fichier JSON
val jsonContent = readFileContent("animaux.json")
val importResult = ImportUtils.importAnimalsFromJson(jsonContent)

// Accéder aux animaux et aliments importés
val animals = importResult.animals
val foods = importResult.foods

// Analyser un fichier JSON d'aliments
val diagnostic = ImportUtils.analyzeEspecesInFoodJson(jsonContent)
println(diagnostic) // Affiche un rapport sur les espèces des aliments
``` 