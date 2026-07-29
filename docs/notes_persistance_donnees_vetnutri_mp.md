# Note sur la persistance des données dans VetNutri MP

## Principes de base de Room Multiplatform

Selon le guide officiel Android (https://developer.android.com/kotlin/multiplatform/room?hl=fr), Room Multiplatform est une extension de la bibliothèque Room conçue pour fonctionner avec Kotlin Multiplatform. Elle permet de:

- Définir un schéma de base de données unifié pour toutes les plateformes
- Générer du code spécifique à chaque plateforme pour accéder à la base de données
- Utiliser SQLite comme moteur de base de données sous-jacent sur toutes les plateformes
- Partager les requêtes et la logique d'accès aux données entre Android, iOS et Desktop

### Configuration de base

Dans le projet VetNutri MP, Room est configuré dans le fichier `composeApp/build.gradle.kts` avec:

```kotlin
plugins {
    alias(libs.plugins.room)
}

// Configuration Room
room {
    schemaDirectory("$projectDir/schemas")
}

// Processeurs d'annotations Room pour chaque plateforme
dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspDesktop", libs.androidx.room.compiler)
}
```

## Structure de persistance dans VetNutri MP

### AppDatabase

La classe `AppDatabase` (`DataBase/AppDatabase.kt`) est le point d'entrée principal pour accéder à la base de données:

- Définit le schéma de la base de données
- Fournit des accesseurs pour les DAOs (Data Access Objects)
- Gère les migrations de base de données si nécessaire

### Entités

Les entités (`DataBase/Entity.kt`) définissent les tables de la base de données:

- Annotées avec `@Entity`
- Correspondent aux modèles de données métier
- Contiennent les relations entre tables
- Définissent les clés primaires et étrangères

### DAOs (Data Access Objects)

Les DAOs fournissent des méthodes pour accéder aux données:

- Annotés avec `@Dao`
- Définissent des méthodes CRUD (Create, Read, Update, Delete)
- Utilisent des requêtes SQL ou des méthodes générées

### Convertisseurs de type

Les convertisseurs (`DataBase/Converters.kt`) permettent de stocker des types complexes en base:

- Convertissent des types Kotlin en types primitifs pour le stockage
- Gèrent les conversions entre JSON et objets métier
- Supportent les types spécifiques comme les dates, listes, etc.

### Mappers

Les mappers (`DataBase/Mappers.kt`) assurent la conversion entre:

- Entités de base de données et modèles métier
- Modèles métier et JSON pour l'import/export

## Modèles nutritionnels et stockage des références

### Modèles de données nutritionnelles

La persistance des données nutritionnelles repose sur plusieurs classes clés:

1. **BiblioRef et BiblioRefEntity**:
   - Stocke les références bibliographiques utilisées dans les équations et valeurs nutritionnelles
   - Attributs: `uuid`, `firstAuthor`, `year`, `completeRef`, `comments`, `bibtex`, `consistent`
   - Géré par `BiblioRefDao` et `DatabaseBiblioRefRepository`

2. **ReferenceEv**:
   - Agrège plusieurs références nutritionnelles pour différents nutriments
   - Utilise des `Map<Nutrient, Nut4Ref>` organisées par niveau de référence (MIN, MAX, OPTIMIN, OPTIMAX)
   - Inclut des équations pour le calcul des besoins
   - N'est pas directement persisté, mais ses composants le sont

3. **NutrientRefP**:
   - Représente une référence nutritionnelle spécifique
   - Attributs: nutriment (`mne`), valeur (`quantity`), unité (`unit`), référence bibliographique (`biblio`), etc.
   - Utilisé comme élément constitutif dans `ReferenceEv`

4. **UnitP et UnitEnum**:
   - `UnitEnum`: énumération persistante des unités de mesure avec facteurs de conversion
   - `UnitP`: wrapper autour de `UnitEnum` avec logique métier additionnelle
   - Utilisées pour la conversion entre différentes unités nutritionnelles

### Stratégie de persistance des données nutritionnelles

Les données nutritionnelles sont persistées selon deux stratégies:

1. **Persistance en base de données**:
   - `BiblioRefEntity`: stockage direct en base de données via Room
   - `EquationEntity`: stockage direct des équations, liées aux références bibliographiques

2. **Persistance basée sur JSON**:
   - `NutrientRefP`, `ReferenceEv`: sérialisés en JSON pour stockage
   - `ListNutrientRef`: collection persistée en tant que JSON dans la base

3. **Stockage statique de référence**:
   - Certaines énumérations comme `UnitEnum`, `Reflevel`, `Nutrient` sont codées statiquement
   - Les valeurs nutritionnelles de référence peuvent être chargées depuis des fichiers JSON ou ressources

### Gestion des valeurs nutritionnelles

La gestion des valeurs nutritionnelles implique:

1. **Conversion d'unités**:
   - `UnitEnum` définit des facteurs de conversion via la propriété `conv`
   - `UnitP` et `NutrientRefP` utilisent ces facteurs pour convertir entre unités
   - `getConverter()` et `getQuantityConverted()` gèrent les conversions

2. **Organisation par niveaux**:
   - `Reflevel` définit les niveaux MIN, MAX, OPTIMIN, OPTIMAX
   - `ReferenceEv` maintient des Maps séparées pour chaque niveau
   - Les méthodes comme `obtenirNutriment()` et `definirNutriment()` acceptent un `Reflevel`

3. **Hiérarchie des nutriments**:
   - `MainNutrientEnum` définit les catégories principales (MIN, VITAM, MACRO, etc.)
   - `Nutrient` est l'interface de base implémentée par des énumérations spécifiques
   - Ces relations sont utilisées dans `getSousNutrients()` pour organiser les nutriments

## Utilisation dans l'architecture MVVM

### Repositories

Les repositories encapsulent la logique d'accès aux données:

- `DatabaseAnimalRepository`: Gère les opérations CRUD pour les animaux
- `DatabaseFoodRepository`: Gère les opérations CRUD pour les aliments
- `DatabaseBiblioRefRepository`: Gère les références bibliographiques
- `PersistentEquationRepository`: Gère les équations avec stockage JSON
- Utilisent les DAOs pour interagir avec la base de données
- Convertissent les entités en modèles métier via les mappers

### ViewModels

Les ViewModels consomment les repositories:

- Exposent les données aux vues via des états observables (StateFlow)
- Effectuent les opérations métier en s'appuyant sur les repositories
- Gèrent le cycle de vie des données

#### Nouveaux ViewModels nutritionnels

- `BiblioRefViewModel`: Gère les références bibliographiques
- `EquationViewModel`: Gère les équations et leur édition

### Vues

Les vues (écrans Compose) observent les données:

- Observent les StateFlow exposés par les ViewModels
- Affichent les données aux utilisateurs
- Déclenchent les actions utilisateur vers les ViewModels

#### Nouvelles vues nutritionnelles

- `CalculationTabsView`: Interface à onglets pour naviguer entre les différentes données nutritionnelles
- `BiblioRefListView` et `BiblioRefEditView`: Gestion des références bibliographiques
- `EquationListView` et `EquationEditView`: Gestion des équations
- `NutrientRequirementView`: Affichage des besoins nutritionnels de base

## Bonnes pratiques implémentées

1. **Abstraction**: Utilisation d'interfaces pour les repositories
2. **Immutabilité**: Utilisation de data classes et d'objets immuables
3. **Coroutines**: Exécution asynchrone des opérations de base de données
4. **Mappage explicite**: Séparation claire entre entités et modèles métier
5. **Transactions**: Gestion des transactions pour les opérations complexes

## Points d'attention et améliorations possibles

- S'assurer que les migrations de schéma sont correctement gérées
- Mettre en place des tests unitaires pour les DAOs et repositories
- Optimiser les requêtes pour les grandes quantités de données
- Synchroniser les schémas entre les plateformes lors des changements
- Gérer correctement les annotations kotlinx.serialization pour éviter les conflits

## Sécurité et intégrité des données

- Les données sont validées avant insertion dans la base
- Des contraintes d'intégrité référentielle sont définies dans le schéma
- Les transactions garantissent la cohérence des données

## Synchronisation et sauvegarde

- Import/export JSON pour le partage de données
- L'état actuel du stockage est local (pas de synchronisation cloud)
- Possibilité d'ajouter des backups automatiques à l'avenir

## Gestion des flux Kotlin Flow et optimisations

### Problèmes identifiés et solutions

#### Gestion des émissions dans les Flows

**Problème**: Des blocages et des problèmes d'émission ont été constatés dans l'utilisation des `Flow`, notamment lors de la collecte des références bibliographiques et des équations.

**Solution**:
- Utilisation de `firstOrNull()` pour collecter un flow sans rester en attente d'émissions continues
- Structuration claire des flows avec gestion des erreurs via `catch`
- Éviter les émissions multiples et excessives qui peuvent surcharger le collecteur

Exemple de code optimisé:
```kotlin
override fun getAllBiblioRefs(): Flow<List<BiblioRef>> {
    return flow {
        // Émettre d'abord les données en cache
        emit(_biblioRefs.value)
        
        try {
            // Charger les données de la base de données
            val dbRefs = withContext(AppDispatchers.IO) {
                val entities = biblioRefDao.getAllBiblioRefs()
                entities.map { it.toDomain() }
            }
            
            // Mettre à jour le cache et émettre les nouvelles données
            _biblioRefs.value = dbRefs
            emit(dbRefs)
            
        } catch (e: Exception) {
            println("DEBUG: Erreur lors du chargement des références: ${e.message}")
            // En cas d'erreur, on n'émet rien de plus (les données du cache ont déjà été émises)
        }
    }
}
```

#### Optimisation de la collecte dans les ViewModels

**Problème**: Les ViewModels qui tentaient de collecter indéfiniment pouvaient bloquer ou causer des fuites de mémoire.

**Solution**:
- Utilisation de `withTimeoutOrNull` pour limiter le temps d'attente
- Implémentation de `firstOrNull()` pour collecter uniquement la première valeur
- Gestion explicite des erreurs pour éviter les crashs

Exemple dans BiblioRefViewModel:
```kotlin
fun refreshBiblioRefs() {
    viewModelScope.launch {
        try {
            // Utiliser firstOrNull plutôt qu'une collecte directe
            withTimeoutOrNull(2000) {
                val refs = repository.getAllBiblioRefs().firstOrNull() ?: emptyList()
                println("DEBUG: ${refs.size} références récupérées lors du rafraîchissement")
            }
        } catch (e: Exception) {
            println("DEBUG: Erreur lors du rafraîchissement: ${e.message}")
        }
    }
}
```

### Améliorations de l'interface utilisateur

- Ajout d'un FloatingActionButton dans l'écran de liste des équations pour améliorer l'UX
- Utilisation du composant `Scaffold` pour structurer correctement l'interface
- Feedback visuel sur les états de chargement

### Bonnes pratiques pour les flows dans une architecture MVVM

1. **Exposition de StateFlow**: Les ViewModels exposent des `StateFlow` immuables avec `asStateFlow()`
2. **Collecte unique**: Utiliser `first()` ou `firstOrNull()` pour les opérations ponctuelles
3. **Gestion des timeouts**: Implémenter des timeouts pour éviter les blocages indéfinis
4. **Gestion d'erreurs**: Capturer les exceptions avec `.catch { }` avant la collecte
5. **Utilisation de withContext**: Exécuter les opérations de BD sur un dispatcher approprié
6. **Cache en mémoire**: Maintenir un cache en mémoire pour les données fréquemment utilisées

### Points d'attention pour le futur

- Considérer l'utilisation de `stateIn` avec le mode `SharingStarted.WhileSubscribed()` pour les flows partagés
- Implémenter des tests de flux pour vérifier le comportement correct des émissions
- Optimiser la gestion des références bibliographiques avec pagination si le volume augmente
- Analyser les performances des collecteurs de flux avec des outils de profilage 