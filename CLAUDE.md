# VetNutriMP — Guide Architecture

Application Kotlin Compose Multiplatform de nutrition vétérinaire.
Cibles : Android, iOS, Desktop (JVM ; Windows et macOS sont des paquets de la cible JVM).

---

## Stack technique

| Domaine | Technologie | Version |
|---------|-------------|---------|
| UI | Compose Multiplatform | 1.7.0 |
| Langage | Kotlin | 2.2.20 |
| Base de données | Room + BundledSQLiteDriver | 2.7.0 |
| Async | kotlinx.coroutines | 1.10.2 |
| State | StateFlow + ViewModel (lifecycle) | 2.8.4 |
| HTTP | Ktor | 3.0.3 |
| Sérialisation | Kotlin Serialization JSON | 1.8.0 |
| DI | Manuel via `AppContainer` | — |

---

## Architecture — couches

```
View/          ← Composables @Composable (99 fichiers)
  ↓
ViewModel/     ← StateFlow, coroutineScope, logique présentation (26 fichiers)
  ↓
Repository/    ← Interfaces + implémentations Room (18 fichiers)
  ↓
DataBase/      ← DAO Room, Entity, Mappers (9 fichiers)
  ↓
Room/SQLite    ← ~29 tables, 25 migrations, schéma v42
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
| `Screen.kt` | 26 destinations (sealed objects) |
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
Deux caches utilisent le pattern `LinkedHashMap` LRU (eviction automatique de l'entrée la plus ancienne) :
- `DatabaseFoodRepository.searchCache` — 50 entrées max
- `EquationEvaluator.expressionCache` — 1000 entrées max

### AppContainer (DI manuel)
`AppContainer` est un data class avec 15 dépendances construit dans `rememberAppContainer(appDatabase)`. Il n'est pas lazy. Pour ajouter une dépendance, l'ajouter dans `AppContainer.kt` **et** dans `rememberAppContainer`.

### Résolveur de nutriments
`NutrientResolver.AllNutrientResolver(label)` est le point d'entrée unique pour résoudre un label textuel vers un `Nutrient`. Ne pas appeler `getByLabel()` directement sur les enums individuels depuis les couches Repository/Service — passer par `NutrientResolver`.

### Exploration multiration
Écran `Screen.MultiRation` (bouton « Exploration multiration » de la liste des animaux) : moteur pur dans
`Data/ExplorationMultiration.kt` (ajustement successif protéines → fibres → calcium → oméga-6 → sodium →
énergie, arrondis `arrondirQuantiteSelonRegles`, conformité `calculerConformite` comme l'analyse de ration),
BEE et poids métabolique via `Data/CalculMetabolique.kt` (partagé avec `AnimalDetailViewModel`). Une ration
explorée est ouverte dans une consultation de l'animal de travail « Exploration multiration » (poids,
référentiel, K = `coefficientAjustement`) pour réutiliser analyse, édition et export existants. Toutes
espèces : l'espèce choisie filtre les référentiels généraux et les aliments proposés. Les configurations
nommées sont enregistrées dans `MULTI_RATION_EXPLORATIONS` (JSON d'identifiants, `Data/ExplorationEnregistree.kt`,
`MultiRationExplorationRepository`) ; les résultats ne sont pas stockés, ils se recalculent.

### Mode troupeau
Un animal peut être un **troupeau** : à la création (ou modification), choix « Individu / Troupeau » ;
un troupeau n'a ni espèce, ni race, ni sexe propres : il a des groupes d'individus
(`AnimalEv.typesTroupeau` : nom, effectif, poids moyen, espèce, race, sexe ; colonne
`ANIMALS.herdTypesJson`, null = individu), éventuellement d'espèces différentes. La fiche garde l'espèce
du premier groupe (`avecEspeceDuTroupeau`) pour les écrans communs ; les référentiels proposés à chaque
groupe suivent son espèce (`especeDuGroupe`). Il réutilise toute la fiche animal (consultations, rations,
analyse, graphiques, export). Consultation d'un troupeau : « État du troupeau » par groupe (effectif,
poids moyen, NEC, référentiel de l'espèce du groupe, K, variables ; `ConsultationEv.parametresTroupeau`,
colonne `CONSULTATIONS.herdParamsJson`) à la place du poids, de la NEC, du référentiel, des variables et
des K de l'individu (`EditeurEtatTroupeau`, `DetailConsultationTroupeau`) ; ses rations sont en quantités
pour tout le troupeau. Onglet Rations : `PanneauTroupeauConsultation` (`View/TroupeauComponents.kt`) choisit
ce qui est analysé, sans saisie de référentiel ni de variable. « Troupeau entier » : sections de
l'individu masquées, `AnalyseApportsTroupeau` compare l'apport total aux besoins sommés
(`apportsTroupeauEntier`, exposé par le ViewModel : Σ nᵢ × seuil, groupes hors normes ; les référentiels
maladies de la consultation s'appliquent à chaque groupe, comme dans l'analyse d'un groupe). Un groupe (`selectTypeTroupeau`) : la
ration affichée devient celle d'un individu (quantités × Bᵢ / Σ nⱼBⱼ, `rationParAnimalTroupeau`,
virtuelle et en lecture seule) et la consultation d'analyse `consultationAnalysee` (`pourTypeTroupeau` :
poids, référentiel, K du groupe), pour réutiliser l'analyse de ration existante. Moteur pur dans
`Data/Troupeau.kt` (`facteursRepartitionTroupeau`, `AnalyseurTroupeau`).

### Erreurs et journalisation
- Jamais de `println` ni de `printStackTrace` : passer par `Utils/AppLog` (`d` = debug uniquement, `w`, `e`).
- Tout `catch (e: Exception)` / `catch (e: Throwable)` commence par `e.rethrowIfCancellation()` :
  sinon l'annulation d'une coroutine est avalée et la tâche continue sur un écran fermé.
- Pas de `catch` vide : journaliser au minimum avec `AppLog.d`.
- Pas de `!!` sur une donnée lue **après** un clic ou une attente asynchrone (sélection, état
  d'écran) : elle a pu changer. Utiliser `valeur.orUserError("aucune ration n'est sélectionnée") ?: return@launch`
  (`Utils/UserErrors`) : l'utilisateur voit « Action impossible : … » dans `UserErrorDialog`
  (monté dans `App.kt`) au lieu d'un plantage.

### Dépendances
Toutes les versions sont dans `gradle/libs.versions.toml` ; aucune version codée en dur dans
`build.gradle.kts`, aucune entrée de catalogue inutilisée.

### Données initiales et traductions
`commonMain/resources/data/vetnutri_export_init.json` et `commonMain/resources/strings_*.json` sont la
**source unique**. Les assets Android sont générés au build (`syncInitialDataAndroidAssets`) ; les copies
du bundle iOS (`iosApp/iosApp/` : données et `strings_fr/en/zh.json`) sont rafraîchies par
`syncInitialDataIos` (appelée avant `embedAndSignAppleFrameworkForXcode`). `InitialDataSyncTest` échoue
si elles divergent : après modification d'une source, lancer `./gradlew :composeApp:syncInitialDataIos`
et commiter les copies.

### ViewModels
Tous héritent de `androidx.lifecycle.ViewModel` et sont créés par `rememberViewModel(clés) { … }`
(`ViewModel/RememberViewModel.kt`) : un `ViewModelStore` par clé, vidé quand la clé change ou que
l'appelant quitte la composition, ce qui appelle `onCleared()`. Pas de `clear()` public à appeler
à la main ; un scope propre (dispatcher injecté pour les tests) s'annule dans `onCleared()`.
Exceptions : `ConseilsViewModel` et `LegacyMigrationViewModel` reçoivent un scope de composition.

### Préférences
Une seule instance de `PreferencesRepository` (celle d'`AppContainer`). Les vues lisent
`viewModel.preferences` (`StateFlow`, null avant la première lecture) ou appellent
`viewModel.getLoadedPreferences()` ; ne jamais construire un `PreferencesRepository` dans une vue,
sinon une modification faite dans Paramètres n'y est pas vue.

### Petits écrans
`AppSizes.breakpointCompact` (600 dp) : en dessous, une seule colonne (liste puis détail).
`AppSizes.breakpointWideScreen` (900 dp) : barre latérale permanente de la fiche animal au-dessus, tiroir
en dessous. Barres d'onglets : `AdaptiveTabRow` (défilante quand les onglets ne tiennent pas).
Vérifier une mise en page : `GuideScreenshotDriver` avec `VETNUTRI_GUIDE_SIZE=400x800`
(voir `docs/guide_utilisateur/README.md`).

### Threads
- Pas de `runBlocking` hors des tests. `getRoomDatabase` est `suspend` (sauvegarde `.bak` et
  migrations sur IO) ; Android et iOS l'ouvrent via `AppWithDatabase`, une fois par processus.
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
| `commonMain/resources/data/vetnutri_export_init.json` | ~20 MB | Export init complet (refs + aliments) — source unique |
| `androidMain/assets/data/vetfood.json` | ~1.7 MB | Aliments Android spécifiques |
| `iosApp/iosApp/vetnutri_export_init.json` | ~20 MB | Copie générée de l'export init (bundle iOS) |

Ces fichiers sont chargés via `StartupService` au premier lancement, pas à chaque démarrage.
