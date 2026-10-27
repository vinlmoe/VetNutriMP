---
name: ajouter-ecran
description: Ajouter un écran (destination de navigation) à VetNutriMP — Screen, AppNavHost, AppNavController, ViewModel instancié dans App.kt, point d'entrée depuis un écran existant, éventuellement derrière une option d'administration. À utiliser quand on crée une nouvelle vue plein écran, une nouvelle fonctionnalité accessible par un bouton de navigation, ou qu'on touche à Navigation/.
---

# Ajouter un écran (VetNutriMP)

La navigation est manuelle : pas de bibliothèque, pas de pile de retour. Un écran = un `object` dans
`Screen`, une branche du `when (nav.screen)` de `AppNavHost`, et un `onNavigateBack` explicite qui dit
où revenir. Modèle le plus récent et le plus propre à copier : l'exploration multiration
(`Screen.MultiRation`, `MultiRationViewModel`, `View/MultiRationExplorerView.kt`).

Préfixe : `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/`.

## Étapes

### 1. Destination — `Navigation/Screen.kt`

```kotlin
object MonEcran : Screen()
```
Nom en anglais ou français court, cohérent avec la fonctionnalité (`MultiRation`, `CrossAnalysis`).

### 2. État de sélection — `Navigation/AppNavController.kt` (seulement si nécessaire)

Si l'écran affiche un élément précis, ajouter `var selectedXxxId by mutableStateOf<String?>(null)`.
Stocker un **identifiant**, pas l'objet (exception historique : `selectedAnimal`). Si l'écran peut être
ouvert depuis plusieurs endroits, ajouter un `xxxReturnScreen` comme `detailReturnScreen` plutôt que
de coder le retour en dur.

### 3. ViewModel — `ViewModel/MonEcranViewModel.kt`

Convention actuelle (à suivre pour tout nouveau ViewModel) :

```kotlin
/** <Rôle de l'écran en une ou deux phrases, en français.> */
class MonEcranViewModel(
        private val monRepository: MonRepository
) : ViewModel() {

    private val _elements = MutableStateFlow<List<Element>>(emptyList())
    val elements: StateFlow<List<Element>> = _elements.asStateFlow()

    fun charger() {
        viewModelScope.launch { _elements.value = monRepository.getAll() }
    }
}
```
- `androidx.lifecycle.ViewModel` + `viewModelScope` (les anciens ViewModels à `CoroutineScope` manuel
  ne sont pas un modèle).
- Méthodes publiques en anglais **ou** en français selon le voisinage ; les récents écrans métier
  (multiration, troupeau) sont en français (`chargerReferences`, `selectTypeTroupeau`).
- Les calculs vont dans le ViewModel ou, mieux, dans un moteur pur sous `Data/` testable en
  `commonTest` (`Data/ExplorationMultiration.kt`, `Data/Troupeau.kt`). Jamais dans le Composable.
- Le ViewModel ne reçoit que des Repository (ou d'autres ViewModels), jamais un DAO.
  Nouveau repository : voir le skill `ajouter-repository`.

### 4. Câblage — `App.kt` et `Navigation/AppNavModels.kt`

- `App.kt` : instancier dans `remember { }` à côté des autres, avec les repositories tirés de
  `appContainer` :
  ```kotlin
  val monEcranViewModel = remember { MonEcranViewModel(monRepository = appContainer.monRepository) }
  ```
- `AppNavModels` : ajouter `val monEcranViewModel: MonEcranViewModel` (dernier paramètre),
  puis le passer dans la construction `AppNavModels(...)` de `App.kt`.
- Un écran qui n'a besoin que d'un repository (pas de ViewModel) le reçoit via `AppNavRepositories`.
- Rechargement à l'arrivée sur l'écran : soit `LaunchedEffect(Unit) { viewModel.charger() }` dans la
  vue (cas MultiRation), soit une branche dans `LaunchedEffect(nav.screen)` de `App.kt`.

### 5. Branche de navigation — `Navigation/AppNavHost.kt`

```kotlin
Screen.MonEcran -> {
    MonEcranView(
        viewModel = models.monEcranViewModel,
        onNavigateBack = { nav.navigate(Screen.List) },
        modifier = Modifier.fillMaxSize()
    )
}
```
Placer la branche près des écrans de la même famille. Le `when` doit rester exhaustif : le compilateur
signale l'oubli.

### 6. Vue — `View/MonEcranView.kt`

- Signature : `viewModel`, callbacks de navigation (`onNavigateBack`, `onOpenXxx`), `modifier` en dernier.
  La vue ne connaît ni `nav` ni `Screen`.
- Barre de titre : `TopBarSimple(title, onNavigateBack)` ; `TopBar(title, onBackClick, onSettingsClick)`
  si l'écran doit donner accès aux réglages. Composants réutilisables dans `Components/`
  (`ConfirmDialog`, `DropdownField`, `Section`, `AppTextField`…) avant d'en écrire un nouveau.
- États : `val x by viewModel.x.collectAsState()`.
- Textes : en français ; si le fichier voisin passe par `translate(...)`, suivre le skill
  `traduire-interface`.
- Mise en page large (desktop) ET étroite (Android/iOS) : éviter les largeurs fixes sans repli.

### 7. Point d'entrée

Ajouter un callback `onShowMonEcran: (() -> Unit)? = null` à l'écran parent (ex. `AnimalListView`)
et le brancher dans `AppNavHost` : `onShowMonEcran = { nav.navigate(Screen.MonEcran) }`.

**Fonctionnalité optionnelle / expérimentale** : la cacher derrière une préférence, comme
l'exploration multiration :
- clé dans `Utils/FeaturePreferences.kt` (`const val MON_ECRAN_ENABLED = "feature.mon_ecran.enabled"`) ;
- interrupteur dans `View/SettingsSections/AdministrationSettings.kt` ;
- lecture dans l'écran parent via `createPreferencesStorage().getString(KEY, "false").toBoolean()`
  dans un `LaunchedEffect(Unit)` ;
- masquer le bouton en session d'examen si l'écran n'y a pas sa place (`examSession == null`).

### 8. Vérifier et documenter

```bash
./gradlew :composeApp:compileKotlinDesktop      # compilation rapide
./gradlew :composeApp:desktopTest               # tests (inclut commonTest)
./scripts/run_fresh_desktop.sh                  # lancer sur une base neuve, sans toucher la vraie
```
- Tester le moteur pur dans `composeApp/src/commonTest/...` (même paquet que la classe testée).
- `CLAUDE.md` : mettre à jour le nombre de destinations de `Screen.kt` et, pour une fonctionnalité
  notable, ajouter un paragraphe dans « Points d'attention » (modèle : « Exploration multiration »).

## Liste de contrôle

- [ ] `object` dans `Screen`, branche dans `AppNavHost`, retour explicite
- [ ] ViewModel `: ViewModel()` avec `StateFlow`, instancié dans `App.kt`, ajouté à `AppNavModels`
- [ ] Aucun accès Repository ni calcul métier dans la vue
- [ ] Point d'entrée ajouté (et option d'administration si fonctionnalité optionnelle)
- [ ] Compilation desktop et `desktopTest` verts
- [ ] `CLAUDE.md` à jour
