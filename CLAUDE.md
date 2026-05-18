# VetNutriMP — Guide Architecture

Application Kotlin Compose Multiplatform de nutrition vétérinaire.
Cibles : Android, iOS, Desktop (JVM), Windows.

---

## Stack technique

| Domaine | Technologie | Version |
|---------|-------------|---------|
| UI | Compose Multiplatform | 1.7.0 |
| Langage | Kotlin | 2.2.20 |
| Base de données | Room + BundledSQLiteDriver | 2.7.0 |
| Async | kotlinx.coroutines | 1.7.3 |
| State | StateFlow + ViewModel (lifecycle) | 2.8.4 |
| HTTP | Ktor | 3.0.3 |
| Sérialisation | Kotlin Serialization JSON | 1.8.0 |
| DI | Manuel via `AppContainer` | — |

---

## Architecture — couches

```
View/          ← Composables @Composable (51 fichiers)
  ↓
ViewModel/     ← StateFlow, coroutineScope, logique présentation (23 fichiers)
  ↓
Repository/    ← Interfaces + implémentations Room (19 fichiers)
  ↓
DataBase/      ← DAO Room, Entity, Mappers (8 fichiers)
  ↓
Room/SQLite    ← ~24 tables, 18 migrations, schéma v35
```

Règles à respecter :
- Toute opération DB se fait dans `withContext(AppDispatchers.IO)`.
- Les Composables ne touchent jamais les Repository directement.
- La logique métier (calculs, équations) appartient au ViewModel, jamais au Composable.

---

## Navigation (`Navigation/`)

La navigation est manuelle (sans Voyager/Decompose). Les écrans sont décrits dans `sealed class Screen`.

| Fichier | Rôle |
|---------|------|
| `Screen.kt` | 23 destinations (sealed objects) |
| `AppNavController.kt` | État mutable de navigation (`screen`, `selectedXxxId`, etc.) |
| `AppNavHost.kt` | `when(nav.screen)` — dispatch vers les Composables d'écran |
| `AppNavModels.kt` | `AppNavModels` (ViewModels) + `AppNavRepositories` (repos directs) |
| `AppDialogs.kt` | Dialogues d'overlay transversaux (import résultats, backup) |

`App.kt` est l'orchestrateur : il crée les ViewModels, les repositories, le `AppNavController`, et appelle `AppNavHost`.

### Ajouter un écran

1. Ajouter un `object MonEcran : Screen()` dans `Screen.kt`.
2. Ajouter un `Screen.MonEcran -> { MonEcranView(...) }` dans `AppNavHost.kt`.
3. Si l'écran a besoin d'un état sélectionné (ex. un ID), l'ajouter dans `AppNavController`.
4. Si l'écran a besoin d'un ViewModel, l'ajouter dans `AppNavModels` et l'instancier dans `App.kt`.

---

## Suffixe "Ev" — convention

Les classes suffixées `Ev` (`AnimalEv`, `AlimentEv`, `ReferenceEv`, `ConsultationEv`) sont des **objets domaine agrégés** : ils combinent les données de plusieurs tables Room en un seul objet riche utilisable par les ViewModels et les Views.

Distinction avec les entités Room (`AnimalEntity`, `FoodEntity`, etc.) qui sont des représentations plate-à-plat de la BD.

Les classes sans suffixe (`Ration`, `Equation`, `BiblioRef`) sont des objets domaine simples qui correspondent à une seule table.

---

## Convention de nommage FR/EN

Le code mélange français et anglais pour des raisons historiques. La règle à suivre est :
- **Méthodes publiques de ViewModel/Repository** : anglais (`getAllFoods()`, `saveAnimal()`)
- **Méthodes privées d'import/parsing** : français (`creerReferenceDepuisJson()`, `traiterEquations()`)
- **Labels et textes UI** : français (toujours)

Ne pas tenter d'uniformiser rétrospectivement — risque de cassure élevé, valeur faible.

---

## Répertoires clés

```
composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/
├── App.kt                    # Orchestrateur principal (ViewModels + AppContainer)
├── Navigation/               # Routing manuel (Screen, NavController, NavHost)
├── View/                     # Écrans Composable
├── ViewModel/                # Logique de présentation (StateFlow)
├── Repository/               # Accès données (interfaces + implémentations DB)
├── DataBase/                 # DAO Room, Entity, Mappers.kt
├── Data/                     # Modèles domaine (Ev, JSON, API)
├── Enumer/                   # Enums (nutriments, espèces, unités...)
├── Service/                  # Services applicatifs (backup, import Excel, startup)
├── Components/               # Composants UI réutilisables (TopBar, ConfirmDialog...)
├── Utils/                    # Utilitaires (ImportUtils, EquationEvaluator, AppDispatchers)
├── Theme/                    # Couleurs, typographie, tailles
└── Export/                   # Export PDF/JSON
```

---

## Points d'attention

### Caches mémoire
Trois caches utilisent le pattern `LinkedHashMap` LRU (eviction automatique de l'entrée la plus ancienne) :
- `DatabaseFoodRepository.searchCache` — 50 entrées max
- `EquationEvaluator.expressionCache` — 1000 entrées max
- `AnimalDetailViewModel.rationAnalysisCache` — 50 entrées + TTL 2 minutes

### AppContainer (DI manuel)
`AppContainer` est un data class avec 13 dépendances construit dans `rememberAppContainer(appDatabase)`. Il n'est pas lazy. Pour ajouter une dépendance, l'ajouter dans `AppContainer.kt` **et** dans `rememberAppContainer`.

### Résolveur de nutriments
`NutrientResolver.AllNutrientResolver(label)` est le point d'entrée unique pour résoudre un label textuel vers un `Nutrient`. Ne pas appeler `getByLabel()` directement sur les enums individuels depuis les couches Repository/Service — passer par `NutrientResolver`.

### Threads
- `AppDispatchers.IO` → opérations DB et réseau
- `AppDispatchers.Main` → mise à jour UI (StateFlow)
- `withContext(AppDispatchers.IO)` **obligatoire** dans toutes les `suspend fun` de Repository

---

## Ajouter un type de nutriment

1. Ajouter l'entrée dans l'enum approprié (`NutrientMain`, `NutrientMin`, `NutrientVitam`, etc.).
2. Mettre à jour `NutrientResolver.kt` si le résolveur ne couvre pas automatiquement la nouvelle entrée.
3. Si c'est un nouveau type d'enum, l'ajouter dans `FoodEditViewModel.loadNutrients()` et dans `preloadCustomNutrientsFromRepository()`.

---

## Commandes de build

```bash
# Desktop (JVM) — le plus rapide
./gradlew :composeApp:run

# Android
./gradlew :composeApp:assembleDebug

# Tests unitaires
./gradlew :composeApp:testDebugUnitTest

# Nettoyage
./gradlew clean
```

> Le build nécessite le plugin Android Gradle (AGP 8.7.3) et une installation SDK Android complète.
> Les builds iOS/Desktop n'ont pas cette contrainte.

---

## Fichiers JSON de données initiales

| Fichier | Taille | Usage |
|---------|--------|-------|
| `food.json` | ~15 MB | Base aliments initiale (racine projet) |
| `commonMain/resources/data/vetnutri_export_init.json` | ~11 MB | Export init complet (refs + aliments) |
| `androidMain/assets/data/vetfood.json` | ~1.7 MB | Aliments Android spécifiques |

Ces fichiers sont chargés via `StartupService` au premier lancement, pas à chaque démarrage.
