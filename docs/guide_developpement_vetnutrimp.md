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

## Gestion des Coroutines et Dispatchers

### Problèmes connus avec Dispatchers.Main

Dans un environnement multiplateforme, l'utilisation directe de `Dispatchers.Main` peut provoquer des erreurs lors de l'exécution sur certaines plateformes, notamment Desktop. L'erreur typique est :

```
java.lang.IllegalStateException: Dispatchers.Main was accessed when the platform dispatcher was absent and the test dispatcher was unset.
```

Ou parfois :

```
java.lang.NoClassDefFoundError: android/os/Looper
```

### Recommandations

1. **Toujours utiliser PlatformDispatcher** : Utilisez la classe `PlatformDispatcher` fournie dans le projet pour obtenir un dispatcher approprié pour chaque plateforme.

   ```kotlin
   // À faire
   private val dispatcher = PlatformDispatcher().provideMainDispatcher()
   private val viewModelScope = CoroutineScope(dispatcher)
   
   // À éviter
   private val viewModelScope = CoroutineScope(Dispatchers.Main)
   ```

2. **Injecter les dispatchers** : Pour les tests unitaires, pensez à injecter les dispatchers dans les constructeurs des classes.

   ```kotlin
   class MonViewModel(
       private val repository: MonRepository,
       private val dispatcher: CoroutineDispatcher = PlatformDispatcher().provideMainDispatcher()
   ) {
       private val viewModelScope = CoroutineScope(dispatcher)
       // ...
   }
   ```

3. **Structure du PlatformDispatcher** : Assurez-vous que l'implémentation de `PlatformDispatcher` est correcte pour chaque plateforme.

   - Pour Desktop : Utilisez `Dispatchers.Default` ou `Dispatchers.IO`
   - Pour Android : Utilisez `Dispatchers.Main.immediate` lorsque disponible

### Implémentation recommandée

```kotlin
// common/Utils/MainDispatcher.kt
expect class PlatformDispatcher() {
    fun provideMainDispatcher(): CoroutineDispatcher
}

// androidMain/Utils/MainDispatcher.kt
actual class PlatformDispatcher {
    actual fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}

// desktopMain/Utils/MainDispatcher.kt
actual class PlatformDispatcher {
    actual fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
```

Cette approche assure que vos ViewModels et autres composants utilisant des coroutines fonctionneront correctement sur toutes les plateformes supportées.

**Important** : Ne jamais utiliser directement `Dispatchers.Main` dans le code commun sans passer par un wrapper multiplateforme. 