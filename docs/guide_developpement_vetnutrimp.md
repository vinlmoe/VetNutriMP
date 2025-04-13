# Guide de développement pour VetNutriMP

Ce guide recense les bonnes pratiques à suivre lors du développement de VetNutriMP, notamment pour éviter les erreurs courantes.

## ⚠️ IMPORTANT : Problèmes connus nécessitant refactorisation

Le projet contient actuellement plusieurs problèmes architecturaux qui causent des erreurs de compilation difficiles à résoudre :

1. **Incohérence entre `AlimentRepository` et `FoodRepository`** : Ces deux repositories ont des fonctionnalités qui se chevauchent. `AlimentRepository` était prévu pour être déprécié mais certaines parties du code l'utilisent encore directement.

2. **Problème spécifique dans `App.kt`** : Une erreur persistante apparaît à la ligne 92 où une incompatibilité de type est signalée entre `DatabaseFoodRepository` et `AlimentRepository`.

3. **Architecture inconsistante des repositories** : Le pattern Singleton utilisé avec `AlimentRepository.initializeDatabaseFoodRepository()` crée des dépendances difficiles à gérer.

**Solution à court terme** : Évitez de modifier la structure des repositories et concentrez-vous sur les nouvelles fonctionnalités. Une refactorisation complète est prévue.

**Solution à long terme** : Refactoriser entièrement le système de repositories en :
- Remplaçant complètement `AlimentRepository` par `FoodRepository`
- Supprimant l'accès statique aux repositories
- Utilisant l'injection de dépendances

## Structure des modèles de données

Le package `fr.vetbrain.vetnutri_mp.Data` contient les classes de données qui représentent les entités métier manipulées par l'application. Ces classes sont généralement implémentées sous forme de data classes Kotlin.

### Caractéristiques communes des classes de données

La plupart des classes de données partagent les caractéristiques suivantes :

1. **Génération d'UUID** : Utilisation de `Uuid.random().toString()` pour générer des identifiants uniques
2. **Valeurs par défaut** : Paramètres avec valeurs par défaut pour faciliter l'instanciation
3. **Types nullables** : Utilisation de types nullables (`String?`, `Float?`, etc.) pour les propriétés optionnelles
4. **Collections mutables** : Utilisation de collections mutables (`MutableList`, `MutableMap`) pour les propriétés qui peuvent être modifiées
5. **Méthodes d'accès** : Des fonctions d'aide pour accéder à des propriétés dérivées ou pour manipuler les collections internes

### Principales classes de données et leurs relations

```
AnimalEv
    |
    ├── weightHistory: List<WeightDate>
    └── consultations: List<ConsultationEv>
                           |
                           └── rations: List<Ration>
                                           |
                                           └── alimentMutableList: List<AlimentRation>
                                                                      |
                                                                      └── aliment: AlimentEv
```

#### AnimalEv

Représente un animal suivi dans l'application.
- Propriétés principales: `uuid`, `nom`, `specieId`, `sexId`, `birthdate`, etc.
- Relations:
  * `weightHistory`: Historique des poids de l'animal
  * `consultations`: Liste des consultations vétérinaires

#### ConsultationEv

Représente une consultation vétérinaire pour un animal.
- Propriétés principales: `uuid`, `idAnim` (référence à l'animal), `date`, `weight`, etc.
- Relations:
  * `suppVarp`: Variables supplémentaires (médicaments, suppléments, etc.)
  * `rations`: Rations alimentaires prescrites lors de la consultation

#### Ration

Représente une ration alimentaire prescrite lors d'une consultation.
- Propriétés principales: `uuid`, `idConsult` (référence à la consultation), `name`, etc.
- Relations:
  * `alimentMutableList`: Liste des aliments qui composent la ration

#### AlimentRation

Représente un aliment spécifique dans une ration avec sa quantité.
- Propriétés principales: `uuid`, `quantite`, `prop`, etc.
- Relations:
  * `aliment`: Référence à l'aliment (AlimentEv)
  
#### AlimentEv

Représente un aliment dans la base de données.
- Propriétés principales: `uuid`, `nom`, `brand`, `ingredients`, etc.
- Relations:
  * `valMap`: Carte des valeurs nutritionnelles (Nutrient -> NutrientQuantity)
  * `especes`: Liste des espèces pour lesquelles l'aliment est adapté
  * `indicat`: Liste des indications médicales

### Autres classes importantes

- **Equation**: Modèle de calcul des besoins énergétiques
- **BiblioRef**: Référence bibliographique pour les équations et valeurs nutritionnelles
- **NutrientRef**: Valeur de référence pour un nutriment selon les standards nutritionnels
- **CoefP**: Coefficient pour le calcul des besoins selon l'état physiologique

### Sérialisation et persistance

Toutes ces classes sont:
1. Sérialisables avec Kotlinx Serialization pour la communication entre composants
2. Mappées vers/depuis des entités Room pour la persistance en base de données
3. Complétées par des classes de mappage et de conversion dans les packages `DataBase.Mappers` et `Data.JsonMappers`

### Bonnes pratiques pour travailler avec les classes de données

1. **Respect de l'immutabilité** : Privilégiez les opérations qui créent de nouvelles instances plutôt que de modifier des instances existantes.
   ```kotlin
   // Bon - Création d'une nouvelle instance
   val nouvelAnimal = animal.copy(nom = "Nouveau nom")
   
   // À éviter - Modification directe (sauf si la propriété est explicitement var)
   animal.nom = "Nouveau nom"
   ```

2. **Gestion des nullables** : Vérifiez toujours les valeurs nullables avant de les utiliser.
   ```kotlin
   // Utilisation sécurisée avec l'opérateur elvis
   val nomAliment = aliment.nom ?: "Sans nom"
   
   // Ou avec let pour les opérations plus complexes
   aliment.nom?.let { nom ->
       // Faire quelque chose avec le nom
   }
   ```

3. **Utilisation des mappers** : Les conversions entre objets de données et entités de base de données doivent toujours passer par les mappers.
   ```kotlin
   // Pour la persistance
   val animalEntity = animal.toEntity()
   animalDao.insert(animalEntity)
   
   // Pour récupérer depuis la base
   val animalEntity = animalDao.getById(id)
   val animal = animalEntity.toData()
   ```

4. **UUIDs et références** : Assurez-vous que les références entre objets sont cohérentes.
   ```kotlin
   // S'assurer que l'ID de consultation pointe vers le bon animal
   consultation.idAnim = animal.uuid
   
   // De même pour les rations
   ration.idConsult = consultation.uuid
   ```

### Système de mappers

Le projet utilise deux types de mappers pour la conversion des données :

#### 1. Mappers de base de données (`DataBase.Mappers`)

Ces mappers convertissent les objets métier en entités de base de données et vice-versa.

```kotlin
// Objet métier -> Entité
fun AnimalEv.toEntity(): AnimalEntity

// Entité -> Objet métier
fun AnimalEntity.toData(): AnimalEv
```

**Attention** : Certaines méthodes de mapping sont marquées comme "zones protégées" et ne doivent pas être modifiées sans autorisation. Ces méthodes sont critiques pour le fonctionnement de l'application.

#### 2. Mappers JSON (`Data.JsonMappers`)

Ces mappers convertissent les objets métier en structures JSON pour la sérialisation et vice-versa.

```kotlin
// Objet métier -> Structure JSON
fun AnimalEv.toJson(): AnimalEvJson

// Structure JSON -> Objet métier
fun AnimalEvJson.toData(): AnimalEv
```

### Considérations pour la migration UUID

Le projet est en cours de migration de `java.util.UUID` vers `kotlin.uuid.Uuid`. Pour assurer la compatibilité :

1. Utilisez `toString()` lors de la sérialisation pour stocker les UUIDs sous forme de chaînes
2. Vérifiez quelle implémentation est utilisée dans le fichier actuel avant de faire des modifications
3. Suivez les directives du guide concernant la gestion des UUIDs

## Gestion des modèles nutritionnels

Le système de gestion des données nutritionnelles utilise plusieurs modèles et énumérations imbriqués pour représenter de manière complète les besoins, les références et les conversions d'unités.

### Hiérarchie des modèles nutritionnels

```
Nutrient (interface)
   |
   ├── NutrientEnergy
   ├── NutrientMin
   ├── NutrientVitam
   ├── NutrientMacro
   ├── NutrientLipid
   ├── AAEnum (acides aminés)
   └── ...

MainNutrientEnum
   |
   └── getSousNutrients() → List<Nutrient>

UnitEnum
   |
   ├── conv: Float (facteur de conversion)
   └── ...

UnitP
   |
   └── unitEnum: UnitEnum

Reflevel
   |
   ├── MIN
   ├── MAX
   ├── OPTIMIN
   └── OPTIMAX

NutrientRefP
   |
   ├── mne: MainNutrientEnum
   ├── unit: UnitP
   ├── unitReq: UnitReqEnum
   ├── quantity: String
   ├── biblio: BiblioRef
   └── ...

ReferenceEv
   |
   ├── refMapMin: Map<Nutrient, Nut4Ref>
   ├── refMapMax: Map<Nutrient, Nut4Ref>
   ├── refMapOMin: Map<Nutrient, Nut4Ref>
   ├── refMapOMax: Map<Nutrient, Nut4Ref>
   └── ...
```

### Classes principales et leurs responsabilités

#### Nutrient et énumérations dérivées

L'interface `Nutrient` est la base du système nutritionnel et définit les propriétés communes à tous les nutriments :

```kotlin
interface Nutrient : Labelable {
    val ue: UnitEnum  // Unité par défaut du nutriment
    val coef: Int     // Code numérique du nutriment
    val unite: String // Chaîne représentant l'unité

    fun getMNE(): MainNutrientEnum // Catégorie principale du nutriment
}
```

Cette interface est implémentée par plusieurs énumérations spécifiques, chacune représentant une catégorie de nutriments :

```kotlin
enum class NutrientMin(
    private val displayName: String,
    override val coef: Int,
    override val unite: String,
    override val ue: UnitEnum,
    override val label: String,
    val abr: String
) : Nutrient {
    FE("Fer", 0, "mg", UnitEnum.BUmg, "FE", "Fe"),
    CU("Cuivre", 1, "mg", UnitEnum.BUmg, "CU", "Cu"),
    // ...
}
```

#### MainNutrientEnum

`MainNutrientEnum` organise les nutriments en catégories principales :

```kotlin
enum class MainNutrientEnum(override val label: String, val coef: Int) : Labelable {
    MIN("Mineraux", 0),
    ANA("Analysis", 1),
    MACRO("Macro", 2),
    VITAM("Vit", 3),
    // ...

    fun getSousNutrients(): List<Nutrient> {
        return when (this) {
            MIN -> NutrientMin.entries.toList()
            VITAM -> NutrientVitam.entries.toList()
            // ...
        }
    }
}
```

#### Unités et conversions

Les unités sont gérées par deux classes complémentaires :

1. `UnitEnum` - Énumération des unités de base avec facteurs de conversion :

```kotlin
enum class UnitEnum(
    private val unitName: String,
    private val id: Int,
    private val idFamily: Int,
    private val refId: Int,
    val conv: Float,  // Facteur de conversion
    override val label: String
) : Labelable {
    BUg("g", 1, 1, 1, 1f, "BUg"),
    BUmg("mg", 2, 1, 1, 0.001f, "BUmg"),
    BUmu("µg", 3, 1, 1, 0.000001f, "BUmu"),
    // ...
}
```

2. `UnitP` - Classe wrapper qui encapsule `UnitEnum` avec des fonctionnalités supplémentaires :

```kotlin
class UnitP(private val unitEnum: UnitEnum) {
    private val nom: String = unitEnum.displayName
    
    fun getUnit(): UnitEnum = unitEnum
    fun getNom(): String = nom
    fun getNomS(): String = nom
}
```

#### NutrientRefP

`NutrientRefP` représente une référence nutritionnelle spécifique avec sa valeur, son unité et sa source bibliographique :

```kotlin
class NutrientRefP(
    val mne: MainNutrientEnum,
    var nom: String,
    var kind: Int,
    var relativekind: Int,
    var quantity: String,
    var present: Boolean,
    var unit: UnitP,
    var unitReq: UnitReqEnum,
    var biblio: BiblioRef
) {
    private val unitMain: UnitP = unit
    
    // Calcul de la conversion entre l'unité courante et l'unité principale
    fun getConverter(): Float {
        return unit.getUnit().conv / unitMain.getUnit().conv
    }
    
    // Obtient la valeur convertie
    fun getQuantityConverted(): Float {
        if (quantity.isBlank()) return 0f
        val value = quantity.replace(",", ".").toFloat()
        return value * getConverter()
    }
}
```

#### ReferenceEv

`ReferenceEv` est un conteneur qui agrège plusieurs références nutritionnelles organisées par niveau (MIN, MAX, OPTIMIN, OPTIMAX) :

```kotlin
class ReferenceEv(uuid: String? = null) {
    val uuid: String = uuid ?: generateRandomUUID()
    var nom: String = ""
    var description: String = ""
    var espece: Espece = Espece.CHIEN
    var stadePhysio: StadePhysio = StadePhysio.ADULTE

    private val refMapMin: MutableMap<Nutrient, Nut4Ref> = HashMap()
    private val refMapMax: MutableMap<Nutrient, Nut4Ref> = HashMap()
    private val refMapOMin: MutableMap<Nutrient, Nut4Ref> = HashMap()
    private val refMapOMax: MutableMap<Nutrient, Nut4Ref> = HashMap()
    
    // Définit une valeur pour un nutriment à un niveau de référence spécifique
    fun definirNutriment(
        valeur: Float,
        nutrient: Nutrient,
        niveauRef: Reflevel,
        uniteReq: UnitReqEnum,
        biblio: BiblioRef
    ) {
        obtenirMap(niveauRef)[nutrient] = Nut4Ref(
            nutrient = nutrient,
            niveauRelatif = niveauRef,
            quantite = valeur,
            unite = nutrient.ue,
            uniteReq = uniteReq,
            biblio = biblio
        )
    }
    
    // Obtient la Map correspondant au niveau de référence
    private fun obtenirMap(niveauRef: Reflevel): MutableMap<Nutrient, Nut4Ref> {
        return when (niveauRef) {
            Reflevel.OPTIMIN -> refMapOMin
            Reflevel.OPTIMAX -> refMapOMax
            Reflevel.MIN -> refMapMin
            Reflevel.MAX -> refMapMax
        }
    }
    
    // Classe interne pour stocker les références avec leurs détails
    inner class Nut4Ref(
        val nutrient: Nutrient,
        val niveauRelatif: Reflevel,
        val quantite: Float,
        val unite: UnitEnum,
        val uniteReq: UnitReqEnum,
        val biblio: BiblioRef
    )
}
```

### Bonnes pratiques pour travailler avec les modèles nutritionnels

1. **Utilisation des énumérations** : Utilisez toujours les énumérations existantes comme `UnitEnum`, `Reflevel` et `MainNutrientEnum` plutôt que de recréer des constantes.

   ```kotlin
   // Bien
   val niveau = Reflevel.MIN
   
   // À éviter
   val niveau = "minimum"
   ```

2. **Conversion d'unités** : Laissez les méthodes `getConverter()` et `getQuantityConverted()` gérer les conversions plutôt que de les recalculer.

   ```kotlin
   // Bien
   val valeurConvertie = nutrientRef.getQuantityConverted()
   
   // À éviter
   val valeurConvertie = nutrientRef.quantity.toFloat() * (nutrientRef.unit.getUnit().conv / unitBase.conv)
   ```

3. **Organisation des références** : Utilisez les méthodes de `ReferenceEv` pour manipuler les références plutôt que d'accéder directement aux maps.

   ```kotlin
   // Bien
   referenceEv.definirNutriment(valeur, nutrient, Reflevel.MIN, UnitReqEnum.KGBW, biblio)
   
   // À éviter
   referenceEv.refMapMin[nutrient] = Nut4Ref(...)
   ```

4. **Cohérence des unités** : Assurez-vous que les unités utilisées sont appropriées pour le type de nutriment.

   ```kotlin
   // Bien - Utiliser l'unité par défaut du nutriment
   val unite = nutrient.ue
   
   // Ou spécifier une unité compatible
   val unite = UnitEnum.BUmg // Pour les minéraux
   ```

## Interface utilisateur pour les données nutritionnelles

L'interface utilisateur pour la gestion des données nutritionnelles est centrée autour du composant `CalculationTabsView`, qui organise les différentes fonctionnalités sous forme d'onglets.

### Structure de la CalculationTabsView

```
CalculationTabsView
   |
   ├── TabRow
   |    ├── Tab "Équations"
   |    ├── Tab "Références"
   |    └── Tab "Besoins"
   |
   └── Content area
        ├── EquationListView (Tab 0)
        ├── BiblioRefListView (Tab 1)
        └── NutrientRequirementView (Tab 2)
```

### Implémentation de CalculationTabsView

Le composant `CalculationTabsView` est implémenté comme suit :

```kotlin
@Composable
fun CalculationTabsView(
    equationViewModel: EquationViewModel,
    biblioRefViewModel: BiblioRefViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedEquationId by remember { mutableStateOf<String?>(null) }
    var selectedBiblioRefId by remember { mutableStateOf<String?>(null) }
    
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Données de calcul") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = AppIcons.ArrowBack, contentDescription = "Retour")
                }
            }
        )

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Équations") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Références") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Besoins") }
            )
        }

        when (selectedTab) {
            0 -> EquationListView(/* ... */)
            1 -> BiblioRefListView(/* ... */)
            2 -> NutrientRequirementView(/* ... */)
        }
    }
}
```

### NutrientRequirementView

`NutrientRequirementView` affiche les besoins nutritionnels de base pour différentes espèces :

```kotlin
@Composable
fun NutrientRequirementView(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSpecies by remember { mutableStateOf("Chien") }
    
    val nutritionRequirements = remember {
        mapOf(
            "Chien" to listOf(
                NutrientRequirement("Énergie", "Besoins énergétiques", "95-130 kcal/kg^0.75"),
                NutrientRequirement("Protéines", "Matière azotée totale", "18-25% MS"),
                // ...
            ),
            "Chat" to listOf(
                NutrientRequirement("Énergie", "Besoins énergétiques", "100-140 kcal/kg^0.67"),
                NutrientRequirement("Protéines", "Matière azotée totale", "25-35% MS"),
                // ...
            )
        )
    }
    
    Scaffold(
        topBar = { /* ... */ }
    ) { paddingValues ->
        Column(modifier = modifier.padding(paddingValues)) {
            // Sélection de l'espèce
            Card {
                Row {
                    Button(
                        onClick = { selectedSpecies = "Chien" },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (selectedSpecies == "Chien") 
                                VetNutriColors.Primary else VetNutriColors.Surface
                        )
                    ) { Text("Chien") }
                    
                    Button(
                        onClick = { selectedSpecies = "Chat" },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (selectedSpecies == "Chat") 
                                VetNutriColors.Primary else VetNutriColors.Surface
                        )
                    ) { Text("Chat") }
                }
            }
            
            // Liste des besoins nutritionnels
            LazyColumn {
                items(nutritionRequirements[selectedSpecies] ?: emptyList()) { requirement ->
                    NutrientRequirementCard(requirement = requirement)
                }
            }
        }
    }
}
```

### Intégration avec le reste de l'application

Les vues nutritionnelles sont intégrées dans le flux de navigation principal via une entrée dans l'enum `Screen` :

```kotlin
private sealed class Screen {
    object List : Screen()
    object Create : Screen()
    object Detail : Screen()
    // ...
    object CalculationTabs : Screen() // Nouvel écran pour les données nutritionnelles
}
```

Et dans le `when` principal de la fonction `App` :

```kotlin
when (currentScreen) {
    // ...
    Screen.CalculationTabs -> {
        CalculationTabsView(
            equationViewModel = equationViewModel,
            biblioRefViewModel = biblioRefViewModel,
            onNavigateBack = { currentScreen = Screen.List },
            modifier = Modifier.fillMaxSize()
        )
    }
}
```

### Bonnes pratiques pour l'UI des données nutritionnelles

1. **Séparation des onglets** : Maintenir une séparation claire entre les différentes catégories de données (équations, références, besoins).

2. **État local vs. ViewModel** : Utiliser l'état local pour les interactions UI (onglet sélectionné) et les ViewModels pour les données métier.

3. **Gestion des navigations imbriquées** : 
   ```kotlin
   // Gestion correcte de la navigation imbriquée
   if (selectedEquationId != null) {
       EquationEditView(
           viewModel = equationViewModel,
           equationId = selectedEquationId,
           onNavigateBack = { selectedEquationId = null }
       )
       return
   }
   ```

4. **Réutilisation des composants** : Les vues de liste et d'édition sont réutilisées entre les différents contextes :
   ```kotlin
   // Dans CalculationTabsView
   EquationListView(/* ... */)
   
   // Utilisé aussi indépendamment dans l'application
   currentScreen = Screen.EquationList
   ```

## Gestion des Kotlin Flow et optimisation des performances

La gestion des flux de données asynchrones est une partie critique de l'application. Certains problèmes ont été identifiés et résolus concernant l'utilisation des Kotlin Flow.

### Problèmes potentiels avec Kotlin Flow

1. **Blocages dans la collecte** : La collecte sans limite de temps d'un Flow peut bloquer indéfiniment le thread appelant.
2. **Erreurs non gérées** : Les exceptions non capturées dans les Flow peuvent causer des crashs de l'application.
3. **Émissions multiples inutiles** : Des émissions excessives peuvent surcharger les collecteurs et dégrader les performances.
4. **Fuites mémoire** : Des collectes continues sans annulation appropriée peuvent causer des fuites mémoire.

### Bonnes pratiques pour l'utilisation des Flow

#### Dans les repositories

```kotlin
// Bien - Structure recommandée pour les méthodes retournant un Flow
override fun getAllItems(): Flow<List<Item>> {
    return flow {
        // Émettre d'abord les données en cache 
        emit(_items.value)
        
        try {
            // Charger les données de la base de données sur le thread IO
            val dbItems = withContext(AppDispatchers.IO) {
                val entities = itemDao.getAllItems()
                entities.map { it.toDomain() }
            }
            
            // Mettre à jour le cache et émettre les nouvelles données
            _items.value = dbItems
            emit(dbItems)
        } catch (e: Exception) {
            // Logger l'erreur mais ne pas bloquer
            println("ERROR: ${e.message}")
        }
    }
}
```

#### Dans les ViewModels

```kotlin
// ❌ À ÉVITER - Collecte sans limite qui peut bloquer
fun refreshItems() {
    viewModelScope.launch {
        repository.getAllItems().collect { items ->
            // Traitement des items
        }
    }
}

// ✅ RECOMMANDÉ - Collecte avec timeout ou utilisation de firstOrNull()
fun refreshItems() {
    viewModelScope.launch {
        try {
            withTimeoutOrNull(2000) {
                val items = repository.getAllItems().firstOrNull() ?: emptyList()
                // Traiter les items
            }
        } catch (e: Exception) {
            // Gérer l'erreur
        }
    }
}
```

### Pattern recommandé pour l'exposition des données

Dans les ViewModels, suivez ce pattern pour exposer les données :

```kotlin
// État interne mutable
private val _items = MutableStateFlow<List<Item>>(emptyList())

// API publique immuable
val items: StateFlow<List<Item>> = _items.asStateFlow()

// Pour les données qui doivent être partagées entre plusieurs collecteurs
val sharedItems: StateFlow<List<Item>> = repository
    .observeAllItems()
    .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000), // Timeout de 5 secondes
        emptyList()
    )
```

### Optimisations spécifiques implémentées

1. **BiblioRefRepository.getAllBiblioRefs()** : Corrigé pour émettre correctement les données en cache puis les données fraîches
2. **BiblioRefViewModel.refreshBiblioRefs()** : Optimisé pour utiliser `firstOrNull()` avec timeout
3. **EquationViewModel.loadBiblioRefs()** : Amélioré pour gérer les erreurs avec `.catch { }`
4. **EquationViewModel.loadEquations()** : Migré vers `observeAllEquations()` avec gestion d'erreurs adéquate

### Points d'attention pour le développement futur

1. Pour toute méthode retournant un `Flow`, assurez-vous de :
   - Gérer les erreurs avec `catch`
   - Effectuer les opérations lourdes sur un dispatcher dédié (IO)
   - Fournir une valeur initiale pour éviter les états vides

2. Dans les ViewModels, préférez :
   - `.firstOrNull()` pour les opérations ponctuelles
   - `.stateIn()` pour les données observées en continu
   - `withTimeoutOrNull()` pour limiter le temps d'attente

3. Pour le debugging, utilisez :
   - `.onStart { }`, `.onEach { }` et `.onCompletion { }` pour tracer le flux
   - Enregistrez les erreurs avant de les propager

4. Dans l'UI, utilisez :
   - `collectAsStateWithLifecycle()` pour observer les StateFlow dans Compose
   - Gérez toujours l'état de chargement et les erreurs

## Naviguer dans le code existant

Pour travailler efficacement avec la base de code actuelle :

1. **Comprendre la coexistence des repositories** :
   - `DatabaseFoodRepository` implémente l'interface `FoodRepository`
   - `AlimentRepository` est une ancienne couche d'abstraction qui délègue à `FoodRepository`
   - `AlimentRepository.initializeDatabaseFoodRepository()` initialise un singleton accessible via les méthodes statiques

2. **Ordre d'utilisation recommandé** :
   - Préférez utiliser directement une instance de `DatabaseFoodRepository` dans les nouveaux ViewModels
   - Si vous devez utiliser `AlimentRepository` pour la compatibilité, passez toujours une instance de `FoodRepository` au constructeur

3. **Modification de la vue principale (`App.kt`)** :
   - Évitez de modifier la ligne `AlimentRepository.initializeDatabaseFoodRepository(foodRepository)` qui initialise le singleton
   - Si vous devez créer une instance d'`AlimentRepository`, utilisez : `val alimentRepository = AlimentRepository(foodRepository)`

4. **Dans AnimalListView** :
   - Assurez-vous que le paramètre `onEditAnimal` est présent dans la signature de la fonction
   - Vérifiez que ce paramètre est correctement passé lors de l'appel dans `App.kt`

5. **Points d'attention pour les futures corrections** :
   - Utiliser des branches de développement séparées pour les modifications importantes
   - Documenter toutes les contournements d'architecture dans le code

## Bonnes pratiques pour la gestion des dépendances

### Gestion des UUID

**ÉTAT ACTUEL DU PROJET**: Actuellement, le projet utilise `java.util.UUID` car la dépendance pour `kotlin.uuid.Uuid` n'est pas correctement configurée. La migration vers `kotlin.uuid.Uuid` est prévue mais pas encore implémentée.

```kotlin
// Utilisation actuelle (en production)
import java.util.UUID

class MaClasse {
    val id: String = UUID.randomUUID().toString()
}

// Utilisation future (après configuration des dépendances)
import kotlin.uuid.Uuid
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class MaClasse {
    val id: String = Uuid.random().toString()
}
```

Pour les classes sérialisables qui utilisent des UUID, assurez-vous d'ajouter l'annotation `@Serializable` et de stocker l'UUID sous forme de chaîne de caractères :

```kotlin
import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class MonEntité(
    val uuid: String = UUID.randomUUID().toString(),
    val nom: String = "",
    // autres propriétés...
)
```

### Sérialisation
- Le projet utilise `kotlinx.serialization` pour la sérialisation des données
- **TOUJOURS** s'assurer que le plugin est correctement configuré dans `build.gradle.kts`:
  ```kotlin
  plugins {
      alias(libs.plugins.kotlinxSerialization)
      // NE PAS ajouter: kotlin("plugin.serialization") - déjà inclus
  }
  ```
- **VÉRIFIER** que la dépendance est bien ajoutée:
  ```kotlin
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
  ```

### Utilisation des icônes

Pour assurer une cohérence visuelle dans toute l'application et faciliter les mises à jour futures, il est **impératif** d'utiliser la classe `AppIcons` plutôt que des références directes à `Icons.Default.*` :

```kotlin
// À faire (recommandé)
import fr.vetbrain.vetnutri_mp.Theme.AppIcons

// Dans un composant
Icon(
    imageVector = AppIcons.Add,
    contentDescription = "Ajouter",
    tint = VetNutriColors.Primary
)

// À éviter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add

// Dans un composant
Icon(
    imageVector = Icons.Default.Add,
    contentDescription = "Ajouter",
    tint = VetNutriColors.Primary
)
```

Avantages de l'utilisation d'`AppIcons` :
- Centralisation des icônes dans un seul fichier
- Facilité de remplacement ou mise à jour des icônes
- Garantie de cohérence visuelle
- Possibilité d'ajouter des icônes personnalisées
- Simplification des imports dans les composants

Si une icône n'est pas disponible dans `AppIcons`, ajoutez-la au fichier `AppIcons.kt` plutôt que d'utiliser directement `Icons.Default.*`.

### Compose Multiplatform
- **TOUJOURS** vérifier que les imports sont correctement configurés:
  ```kotlin
  // Ne pas utiliser les imports spécifiques à Android:
  // import androidx.compose.*
  
  // Utiliser les imports Compose Multiplatform:
  import androidx.compose.runtime.*
  import androidx.compose.foundation.*
  import androidx.compose.material.*
  import androidx.compose.ui.*
  ```

## Structure du projet et conventions

### Modèles de données
- Les classes de données doivent être placées dans le package `fr.vetbrain.vetnutri_mp.Data`
- Utiliser les annotations de sérialisation pour toutes les classes qui seront persistées
- Préférer l'utilisation d'objets immuables (data class avec propriétés val)

### Vues
- Les vues doivent être placées dans le package `fr.vetbrain.vetnutri_mp.View`
- Séparer les vues en composants réutilisables
- Utiliser les composants définis dans le thème du projet (VetNutriColors, AppSizes)

### ViewModels
- Les ViewModels doivent être placés dans le package `fr.vetbrain.vetnutri_mp.ViewModel`
- Exposer les données via StateFlow pour assurer la cohérence et la réactivité

## Résolution des problèmes fréquents

### Imports manquants
- **SYMPTÔME**: Erreur "Unresolved reference" pour des imports
- **SOLUTION**: 
  1. Vérifier que la dépendance est bien déclarée dans le fichier build.gradle.kts
  2. Exécuter `./gradlew build --refresh-dependencies` pour actualiser les dépendances
  3. Redémarrer l'IDE si nécessaire

### Erreurs de compilation avec UUID
- **SYMPTÔME**: Erreur avec kotlin.uuid
- **SOLUTION**: Utiliser l'annotation `@OptIn(ExperimentalUuidApi::class)` avec l'import de `kotlin.uuid.ExperimentalUuidApi`

### Problèmes de sérialisation
- **SYMPTÔME**: Erreur "Unresolved reference: kotlinx"
- **SOLUTION**: 
  1. S'assurer que le plugin de sérialisation est correctement configuré
  2. Vérifier que les imports sont corrects: `import kotlinx.serialization.Serializable`

### Erreurs de navigation
- **SYMPTÔME**: "Unresolved reference" ou erreurs lors de la navigation entre écrans
- **SOLUTION**: 
  1. Vérifier que les nouveaux écrans sont correctement définis dans la classe Screen
  2. S'assurer que les paramètres de navigation correspondent entre la définition et l'appel

## Intégration avec l'architecture existante

### Persistance des données
- Se référer au document `notes_persistance_donnees_vetnutri_mp.md` pour comprendre la structure de persistance
- Respecter les conventions existantes pour la création de nouvelles entités et DAOs

### Navigation
- Suivre le modèle de navigation existant lors de l'ajout de nouveaux écrans
- S'assurer que les transitions entre écrans sont cohérentes

### Responsive Design
- Utiliser BoxWithConstraints pour adapter les layouts selon la taille de l'écran
- Concevoir les interfaces pour qu'elles fonctionnent sur différentes plateformes (Android, iOS, Desktop)

## Conseils pour le développement

- **AVANT** toute modification majeure, créer une branche dédiée
- **TOUJOURS** tester les modifications sur toutes les plateformes ciblées
- **DOCUMENTER** les nouvelles fonctionnalités et les changements apportés
- **PRÉFÉRER** les modifications incrémentales aux refactorisations massives 

## Résolution des erreurs de compilation courantes

Plusieurs types d'erreurs apparaissent régulièrement lors de la compilation du projet :

### 1. Problèmes avec les types Screen et la navigation

```
Assignment type mismatch: actual type is 'fr.vetbrain.vetnutri_mp.Screen.Detail', but 'fr.vetbrain.vetnutri_mp.Screen.List' was expected.
```

**Cause**: Le type de `currentScreen` est probablement déclaré comme `var currentScreen by remember { mutableStateOf(Screen.List) }` mais le compilateur s'attend à ce que cette variable ne contienne que des valeurs de type `Screen.List`.

**Solution**: Corriger la déclaration pour accepter n'importe quel type de Screen :
```kotlin
var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }
```

### 2. Paramètres de composants manquants

```
No parameter with name 'onEditAnimal' found.
```

**Cause**: Les paramètres attendus par un composant ne correspondent pas à ceux fournis lors de son appel.

**Solution**: Vérifier la signature du composant et ajuster les paramètres fournis lors de l'appel.

### 3. Incompatibilité de types avec les repositories

```
Argument type mismatch: actual type is 'fr.vetbrain.vetnutri_mp.Repository.DatabaseFoodRepository', but 'fr.vetbrain.vetnutri_mp.Repository.AlimentRepository' was expected.
```

**Cause**: Le type de paramètre attendu par une méthode ne correspond pas au type fourni.

**Solution**: 
1. Vérifier que le paramètre est du bon type dans la définition de la méthode `initializeDatabaseFoodRepository`.
2. Dans `AlimentRepository.kt`, cette méthode attend un paramètre de type `FoodRepository`:
   ```kotlin
   fun initializeDatabaseFoodRepository(databaseFoodRepository: FoodRepository)
   ```
3. Comme `DatabaseFoodRepository` implémente `FoodRepository`, le code suivant est correct:
   ```kotlin
   val foodRepository = remember {
       DatabaseFoodRepository(appDatabase.foodDao(), appDatabase.nutrientValueDao())
   }
   AlimentRepository.initializeDatabaseFoodRepository(foodRepository)
   ```
4. S'il y a une erreur liée à l'initialisation d'une instance d'`AlimentRepository`, s'assurer d'utiliser le constructeur correctement:
   ```kotlin
   // Si nécessaire, créer une instance d'AlimentRepository:
   val alimentRepository = AlimentRepository(foodRepository) // foodRepository doit être de type FoodRepository
   ```

### 4. Problèmes de sérialisation avec Kotlin Serialization

```
Serializer has not been found for type 'fr.vetbrain.vetnutri_mp.Data.Equation?'. To use context serializer as fallback, explicitly annotate type or property with @Contextual
```

**Causes possibles**:
- La classe `Equation` n'a pas d'annotation `@Serializable`
- Des propriétés non sérialisables sont utilisées dans cette classe

**Solutions**:
1. Ajouter l'annotation `@Serializable` à la classe
2. Ajouter `@Contextual` au champ utilisant ce type
3. Fournir un sérialiseur explicite pour cette classe

### 5. Arguments optionnels versus obligatoires

```
Argument type mismatch: actual type is 'kotlin.String?', but 'kotlin.String' was expected.
```

**Cause**: Une méthode attend une `String` non-nullable mais reçoit une `String?` (nullable).

**Solution**: Utiliser l'opérateur de non-nullité `!!` ou une vérification préalable avec une valeur par défaut :
```kotlin
val valueToPass = nullableValue ?: ""
// ou
if (nullableValue != null) {
    functionExpectingNonNullable(nullableValue)
}
```

## Bonnes pratiques pour éviter ces erreurs

1. **Type checking**: Toujours vérifier la compatibilité des types, particulièrement avec les génériques
2. **Gestion de nullabilité**: Être explicite sur les types nullable vs non-nullable
3. **Documentation des composants**: Documenter clairement les paramètres attendus par les composants
4. **Tests unitaires**: Écrire des tests pour vérifier le comportement attendu
5. **Refactoring progressif**: Effectuer des changements incrémentaux et vérifier la compilation à chaque étape

Référez-vous toujours aux fichiers existants et fonctionnels comme modèles lorsque vous développez de nouvelles fonctionnalités.

## Gestion des coroutines et des dispatchers

### Utilisation des dispatchers

Les ViewModels utilisent `AppDispatchers` pour gérer les opérations asynchrones. Les dispatchers disponibles sont :

- `AppDispatchers.Main` : Pour les opérations UI et les mises à jour d'état
- `AppDispatchers.IO` : Pour les opérations de base de données et les E/S
- `AppDispatchers.Default` : Pour les calculs intensifs

Exemple d'utilisation dans un ViewModel :
```kotlin
class MonViewModel(private val monDao: MonDao) {
    private val coroutineScope = CoroutineScope(AppDispatchers.Main)

    fun chargerDonnees() {
        coroutineScope.launch {
            // Utiliser IO pour les opérations de base de données
            withContext(AppDispatchers.IO) {
                val donnees = monDao.getDonnees()
                // Retour sur Main pour mettre à jour l'UI
                _state.value = donnees
            }
        }
    }
}
```

### Bonnes pratiques pour les coroutines

1. **Scope des coroutines** :
   - Utiliser `AppDispatchers.Main` pour le scope principal des ViewModels
   - Utiliser `viewModelScope` si disponible dans le contexte Android
   - Annuler les coroutines dans `onCleared()` pour éviter les fuites de mémoire

2. **Gestion des états** :
   - Utiliser `StateFlow` pour les états observables
   - Préférer `MutableStateFlow` à `MutableState` pour la compatibilité multiplateforme
   - Exposer les états en tant que `StateFlow` immutable

3. **Opérations de base de données** :
   - Toujours utiliser `AppDispatchers.IO` pour les opérations de base de données
   - Gérer les exceptions avec try/catch
   - Utiliser `withContext` pour changer de contexte

## Manipulation des collections dans les StateFlow

### Problème avec les collections immuables dans MutableStateFlow

Lorsque vous utilisez `MutableStateFlow` pour stocker une collection (comme une liste), il est important de savoir que les modifications sur la collection ne déclenchent pas automatiquement une émission si la référence à la collection ne change pas.

```kotlin
// ❌ Problème : Cela ne déclenche pas de mise à jour des collecteurs
val items = _stateFlow.value
items.add(newItem) // Modifie la collection mais pas la référence

// ❌ Problème potentiel : update peut ne pas fonctionner correctement dans certains cas
_stateFlow.update { currentList ->
    // Si cette opération ne crée pas une nouvelle liste, les collecteurs ne seront pas notifiés
    currentList.filter { /* condition */ }
}
```

### Solution recommandée

Toujours créer une nouvelle référence de collection et l'assigner directement à la propriété `value` du `MutableStateFlow` :

```kotlin
// ✅ Bonne pratique : créer une nouvelle liste et l'assigner
val newList = _stateFlow.value.toMutableList()
newList.add(newItem)
_stateFlow.value = newList

// ✅ Alternative avec update (garantit une nouvelle référence)
_stateFlow.update { currentList -> 
    // Création explicite d'une nouvelle liste
    currentList + newItem 
}
```

### Exemple avec un repository de données

Voici un exemple d'implémentation correcte pour un repository qui gère une liste d'objets :

```kotlin
class InMemoryRepository<T> {
    private val _items = MutableStateFlow<List<T>>(emptyList())
    val items: StateFlow<List<T>> = _items.asStateFlow()

    fun addItem(item: T) {
        // Création explicite d'une nouvelle liste
        val newList = _items.value.toMutableList()
        newList.add(item)
        _items.value = newList
    }

    fun updateItem(predicate: (T) -> Boolean, update: (T) -> T) {
        val newList = _items.value.map { item ->
            if (predicate(item)) update(item) else item
        }
        _items.value = newList
    }

    fun removeItem(predicate: (T) -> Boolean) {
        _items.value = _items.value.filter { !predicate(it) }
    }
}
```

Cette approche garantit que les changements dans la collection sont correctement propagés à tous les collecteurs du flux.

## Éviter les redéclarations de classes

Une erreur courante dans le projet est la redéclaration de classes, particulièrement avec les implémentations de repositories. Pour éviter cette erreur:

1. **Vérifiez si une classe existe déjà** avant de créer un nouveau fichier. Par exemple, vérifiez si une implémentation comme `InMemoryBiblioRefRepository` existe déjà dans un autre fichier, comme `BiblioRefRepository.kt`.

2. **Préférez définir l'interface et l'implémentation dans le même fichier** pour les repositories lorsque l'implémentation est simple. Par exemple:

```kotlin
// Dans BiblioRefRepository.kt
interface BiblioRefRepository {
    // Méthodes
}

// Implémentation dans le même fichier
class InMemoryBiblioRefRepository : BiblioRefRepository {
    // Implémentation
}
```

3. **Utilisez des noms clairs et distincts** pour éviter les confusions.

4. Si vous rencontrez des erreurs de compilation du type `Redeclaration: class InMemoryBiblioRefRepository`, recherchez dans le projet où cette classe est déjà définie et supprimez la redéclaration. 

## Gestion des erreurs avec les coroutines

### Structure recommandée pour les opérations asynchrones

```kotlin
fun maFonction() {
    coroutineScope.launch {
        _isLoading.value = true
        try {
            // Opération asynchrone
            withContext(AppDispatchers.IO) {
                // Appel à la base de données ou au réseau
            }
            _operationMessage.value = "Opération réussie"
        } catch (e: Exception) {
            _operationMessage.value = "Erreur : ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
}
```

### États d'erreur

Les ViewModels doivent gérer trois types d'états :
1. État de chargement (`isLoading: StateFlow<Boolean>`)
2. Messages d'erreur (`errorMessage: StateFlow<String>`)
3. État de succès (`successMessage: StateFlow<String>`)

### Bonnes pratiques pour la gestion des erreurs

1. **Isolation des erreurs** :
   - Capturer les exceptions au niveau approprié
   - Ne pas laisser les exceptions se propager hors des coroutines
   - Utiliser des types d'erreur spécifiques plutôt que `Exception`

2. **Messages d'erreur** :
   - Fournir des messages d'erreur explicites et compréhensibles
   - Traduire les messages techniques en messages utilisateur
   - Logger les erreurs techniques pour le débogage

3. **Gestion des annulations** :
   - Gérer correctement `CancellationException`
   - Nettoyer les ressources dans le bloc `finally`
   - Annuler les coroutines enfants si nécessaire

4. **Tests** :
   - Tester les cas d'erreur avec des tests unitaires
   - Simuler différents types d'erreurs
   - Vérifier que les états sont correctement mis à jour