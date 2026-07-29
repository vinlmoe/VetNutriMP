# Dossier ViewModel

Ce dossier contient les ViewModels de l'application VetNutri_MP. Les ViewModels font partie du pattern MVVM (Model-View-ViewModel) et servent de couche intermédiaire entre les vues (UI) et les modèles de données.

## Rôle des ViewModels

Les ViewModels dans cette application ont plusieurs responsabilités :

1. **Exposition des données** : Transformer les données du modèle en un format adapté à l'affichage dans l'UI
2. **Gestion de l'état** : Maintenir et mettre à jour l'état de l'UI
3. **Traitement des événements** : Réagir aux actions de l'utilisateur et effectuer les opérations correspondantes
4. **Communication avec les repositories** : Interagir avec les repositories pour accéder aux données
5. **Logique métier** : Implémenter la logique métier liée à la vue

## ViewModels principaux

### AnimalListViewModel

Gère la liste des animaux et les opérations associées.

**Responsabilités :**
- Charger la liste des animaux
- Filtrer et trier les animaux
- Gérer l'ajout, la modification et la suppression d'animaux
- Gérer l'importation d'animaux depuis un fichier JSON

### AnimalDetailViewModel

Gère les détails d'un animal spécifique et ses consultations.

**Responsabilités :**
- Charger les détails d'un animal
- Gérer les consultations de l'animal
- Gérer l'historique du poids
- Gérer les rations associées aux consultations

### FoodListViewModel

Gère la liste des aliments et les opérations associées.

**Responsabilités :**
- Charger la liste des aliments
- Filtrer et trier les aliments
- Gérer l'ajout, la modification et la suppression d'aliments
- Gérer l'importation d'aliments depuis un fichier JSON

### ImportViewModel

Gère les opérations d'importation de données.

**Responsabilités :**
- Coordonner l'importation d'animaux et d'aliments
- Gérer les états d'importation (en cours, succès, erreur)
- Fournir des diagnostics sur les données importées

### NutritionalAnalysisViewModel

Gère l'analyse nutritionnelle des rations.

**Responsabilités :**
- Calculer les valeurs nutritionnelles des rations
- Comparer les valeurs aux recommandations
- Générer des rapports d'analyse

## Structure des ViewModels

Chaque ViewModel suit généralement cette structure:
- Des flux observables (`StateFlow`/`Flow`) pour les données exposées à l'UI
- Des méthodes pour récupérer et manipuler les données
- Des états internes (chargement, erreur, etc.)
- Des références aux repositories nécessaires

## Relations avec d'autres modules

- **View** : Les vues observent les ViewModels et réagissent aux changements
- **Repository** : Les ViewModels utilisent les repositories pour accéder aux données
- **Data** : Les ViewModels manipulent les modèles de données
- **Utils** : Les ViewModels utilisent les utilitaires pour des opérations spécifiques

## Exemple d'utilisation

```kotlin
// Dans une vue (écran ou composant)
val viewModel = AnimalListViewModel(animalRepository)
val animals by viewModel.animals.collectAsState()

// Afficher la liste des animaux
LazyColumn {
    items(animals) { animal ->
        AnimalItem(animal = animal, onClick = { viewModel.selectAnimal(animal) })
    }
}

// Ajouter un nouvel animal
Button(onClick = { viewModel.addNewAnimal(newAnimal) }) {
    Text("Ajouter")
}
```

## Gestion des états

Les ViewModels utilisent Kotlin Flows pour exposer les états observables aux vues :

```kotlin
// Dans le ViewModel
private val _state = MutableStateFlow<UiState<T>>(UiState.Loading)
val state: StateFlow<UiState<T>> = _state.asStateFlow()

// Dans la Vue
val state by viewModel.state.collectAsState()
```

## Gestion des erreurs

Les ViewModels capturent et gèrent les erreurs pour les présenter à l'utilisateur :

```kotlin
fun loadData() {
    viewModelScope.launch {
        try {
            _state.value = UiState.Loading
            val data = repository.getData()
            _state.value = UiState.Success(data)
        } catch (e: Exception) {
            _state.value = UiState.Error(e.message ?: "Une erreur est survenue")
        }
    }
}
```

## Injection de dépendances

Les ViewModels sont créés avec leurs dépendances injectées, ce qui facilite les tests :

```kotlin
// Création d'un ViewModel avec ses dépendances
val animalViewModel = AnimalListViewModel(animalRepository)
```

## Tests

Les ViewModels sont conçus pour être facilement testables :

```kotlin
class AnimalListViewModelTest {
    @Test
    fun `loadAnimals should update state with animals from repository`() {
        // Given
        val mockRepository = mock<AnimalRepository>()
        whenever(mockRepository.getAnimals()).thenReturn(flowOf(listOf(testAnimal)))
        val viewModel = AnimalListViewModel(mockRepository)
        
        // When
        viewModel.loadAnimals()
        
        // Then
        assertEquals(listOf(testAnimal), viewModel.animals.value)
    }
}
``` 