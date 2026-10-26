# VetNutriMP — Guide Architecture

Application Kotlin Compose Multiplatform de nutrition vétérinaire.
Cibles : Android, iOS, Desktop (JVM), Windows.

---

## Stack technique

| Domaine | Technologie | Version |
|---------|-------------|---------|
| UI | Compose Multiplatform | 1.7.0 |
| Langage | Kotlin | 2.2.20 |
| Base de données | Room + BundledSQLiteDriver | 2.7.1 |
| Async | kotlinx.coroutines | 1.10.2 |
| State | StateFlow + ViewModel (lifecycle) | 2.8.4 |
| HTTP | Ktor | 3.0.3 |
| Sérialisation | Kotlin Serialization JSON | 1.8.0 |
| DI | Manuel via `AppContainer` | — |

---

## Architecture — couches

```
View/          ← Composables @Composable (105 fichiers, sous-dossiers Rations/, AnimalDetail/, SettingsSections/)
  ↓
ViewModel/     ← BaseViewModel + StateFlow, logique présentation (30 fichiers)
  ↓
Repository/    ← Interfaces + implémentations Room (19 fichiers)
  ↓
DataBase/      ← DAO Room, Entity, Mappers (9 fichiers)
  ↓
Room/SQLite    ← ~29 tables, 24 migrations, schéma v41
```

Règles à respecter :
- Tout travail bloquant d'un Repository (encodage JSON, fichiers, calculs lourds) se fait dans
  `withContext(AppDispatchers.IO)` ; les DAO Room `suspend` s'exécutent déjà hors du thread principal.
- Les Composables ne touchent jamais les Repository directement : ils reçoivent des ViewModels
  (ou des lambdas fournies par un ViewModel). Seule exception tolérée : `EquationRepository`,
  consommé par la couche `Data` (`AlimentRation.getNutrientWithComplementary`, `getEnergie`).
- La logique métier (calculs, équations, arrondis) appartient à `Data/` ou au ViewModel, jamais au Composable.
- Les analyses de ration et le filtrage du catalogue s'exécutent sur `AppDispatchers.Default`.
- Les écritures en lot (imports, restauration) passent par `TransactionRunner.inTransaction` ;
  chaque écriture notifie `DatabaseChangeNotifier` (la sauvegarde automatique ne tourne que si la base a changé).

---

## Navigation (`Navigation/`)

La navigation est manuelle (sans Voyager/Decompose). Les écrans sont décrits dans `sealed class Screen`.

| Fichier | Rôle |
|---------|------|
| `Screen.kt` | 25 destinations (sealed objects ; l'écran de test `TestYellowBox` a été supprimé) |
| `AppNavController.kt` | État de navigation (`screen`, `selectedXxxId`, etc.) + pile de retour (`navigate`, `back`, `backOr`, `replace`, `navigateAndClear`, `canGoBack`) |
| `PlatformBackHandler.kt` | `expect` Composable : retour système Android (`androidx.activity.compose.BackHandler`) ; no-op Desktop/iOS |
| `AppNavHost.kt` | `when(nav.screen)` — dispatch vers les Composables d'écran |
| `AppNavModels.kt` | `AppNavModels` (ViewModels partagés par les écrans ; plus de repositories transmis aux vues, hors `equationRepository` passé à `AppNavHost` pour les calculs nutritionnels) |
| `AppDialogs.kt` | Dialogues d'overlay transversaux (import résultats, backup) |

`App.kt` est l'orchestrateur : il crée les ViewModels, les repositories, le `AppNavController`, et appelle `AppNavHost`.

### Pile de retour

`AppNavController` garde l'écran courant (`screen`, état Compose en lecture seule depuis l'extérieur) et une
pile des écrans précédents (`ArrayDeque`, 50 entrées max, pas de doublon consécutif) :

- `navigate(cible)` empile l'écran courant puis affiche la cible (sans effet si la cible est déjà affichée) ;
- `back(): Boolean` revient à l'écran précédent, `false` si la pile est vide ;
- `backOr(repli)` = `back()` sinon `navigate(repli)` : c'est la forme à utiliser pour tous les
  `onNavigateBack`/`onBack` dans `AppNavHost` (le repli reproduit l'ancienne destination codée en dur) ;
- `replace(cible)` affiche la cible sans empiler (redirections, formulaire remplacé par la fiche créée) ;
- `navigateAndClear(cible)` vide la pile et affiche une racine ;
- `clearFoodSelection()` / `clearSelection()` remettent les identifiants sélectionnés à zéro ; les lambdas de
  retour qui réinitialisent une sélection (`selectedXxxId = null`, onglet) gardent cet effet avant `backOr`.

`AppNavHost` appelle `PlatformBackHandler(enabled = nav.canGoBack || nav.screen != Screen.List) { nav.backOr(Screen.List) }`
pour brancher le bouton/geste retour Android sur la pile ; sur la liste, pile vide, le retour système
quitte l'application comme avant.

### Ajouter un écran

1. Ajouter un `object MonEcran : Screen()` dans `Screen.kt`.
2. Ajouter un `Screen.MonEcran -> { MonEcranView(...) }` dans `AppNavHost.kt`, avec `onNavigateBack = { nav.backOr(Screen.Parent) }`.
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
├── Utils/                    # Utilitaires (EquationEvaluator, AppDispatchers, SynchronizedLock)
│   └── Import/               # Importeurs JSON (animaux, aliments, besoins) ; ImportUtils = façade
├── Theme/                    # Couleurs, typographie, tailles
└── Export/                   # Export PDF/JSON
```

---

## Points d'attention

### Caches mémoire
Trois caches utilisent `LruMap` (eviction de l'entrée la plus ancienne) :
- `DatabaseFoodRepository.searchCache` — 50 entrées max, protégé par `Mutex`, vidé à chaque écriture
- `EquationEvaluator.expressionCache` — 1000 entrées max, clé déterministe (expression + variables triées),
  protégé par `SynchronizedLock` (verrou non suspendant `expect/actual`, utilisable depuis Main)
- `AnimalDetailViewModel.rationAnalysisCache` — 50 entrées + TTL 2 minutes

`NutrientResolver` et `CustomNutrientRegistry` sont également protégés par `SynchronizedLock` ; le
résolveur se resynchronise quand `CustomNutrientRegistry.version` change.

### AppContainer (DI manuel)
`AppContainer` est un data class construit dans `rememberAppContainer(appDatabase)` ; il fournit aussi
`RoomTransactionRunner(appDatabase)` aux repositories qui importent en lot. Il n'est pas lazy. Pour ajouter une dépendance, l'ajouter dans `AppContainer.kt` **et** dans `rememberAppContainer`.

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

# Tests unitaires (commonTest + desktopTest, vrai SQLite)
./gradlew :composeApp:desktopTest

# Nettoyage
./gradlew clean
```

> Le build nécessite le plugin Android Gradle (AGP 8.7.3) et une installation SDK Android complète.
> Les builds iOS/Desktop n'ont pas cette contrainte.

---

## Fichiers JSON de données initiales

| Fichier | Taille | Usage |
|---------|--------|-------|
| `commonMain/resources/data/vetnutri_export_init.json` | ~20 MB | Export init complet (refs + aliments) — source unique ; Desktop le lit via le classpath, Android via les assets générés par la tâche Gradle `copyCommonDataAssets` (plus de copie dans `androidMain/assets`), iOS via la copie unique `iosApp/iosApp/vetnutri_export_init.json` référencée par Xcode |
| `commonMain/resources/strings_*.json` | ~50–140 KB | Traductions UI (même mécanisme : source unique, copiées dans les assets Android par `copyCommonDataAssets`) |

Ces fichiers sont importés à la demande par `SettingsViewModel` (seule la version du JSON est lue au démarrage ;
l'import complet n'a lieu que si elle est plus récente ou si le catalogue est vide). `StartupService` ne fait
que lancer la sauvegarde automatique.

---

## Sécurité et données

- Partage en ligne (`JsonShareService`) : chiffrement AES-CBC + HMAC-SHA256 (format v2, `CryptoUtils`) ;
  les clés jsonbin restent compilées dans les binaires, voir `docs/qualite/decision-cles-jsonbin.md`.
- Android : base et sauvegardes exclues de la sauvegarde cloud (`res/xml/backup_rules.xml`).
- Aucun jeu de données contenant des noms de propriétaires ne doit être commité (`AnonymizationUtils`).
- Analyse qualité et plan d'action : `docs/qualite/`.
