# Dossier View

Ce dossier contient les écrans principaux de l'application VetNutri_MP. Il s'agit des vues complètes qui représentent des pages entières de l'application, utilisant Compose Multiplatform pour créer une interface utilisateur cohérente sur toutes les plateformes.

## Architecture des vues

Les vues suivent le pattern MVVM (Model-View-ViewModel) :
- Elles observent les données exposées par les ViewModels
- Elles délèguent les actions utilisateur aux ViewModels
- Elles ne contiennent pas de logique métier, seulement de la logique de présentation

## Vues principales

### AnimalView

Écran principal de gestion des animaux, permettant de :
- Visualiser la liste des animaux
- Ajouter un nouvel animal
- Sélectionner un animal pour voir ses détails
- Rechercher un animal

### AnimalDetailView

Écran de détail d'un animal, affichant :
- Informations générales de l'animal (nom, espèce, âge, etc.)
- Historique des consultations
- Courbe de poids
- Pathologies et traitements en cours

### ConsultationView

Écran de gestion des consultations, permettant de :
- Créer une nouvelle consultation
- Modifier une consultation existante
- Visualiser l'historique des consultations

### RationsView

Écran de gestion des rations alimentaires, permettant de :
- Créer une nouvelle ration
- Modifier une ration existante
- Ajouter des aliments à une ration
- Visualiser l'analyse nutritionnelle de la ration

### FoodListView

Écran de gestion de la base de données alimentaire, permettant de :
- Visualiser la liste des aliments disponibles
- Rechercher des aliments par nom, marque ou type
- Ajouter de nouveaux aliments
- Modifier des aliments existants
- Visualiser les détails nutritionnels d'un aliment

### ImportExportView

Écran permettant d'importer et d'exporter des données (animaux, aliments, consultations).

## États UI communs

Les vues utilisent des modèles d'état communs pour gérer les différents états de l'interface utilisateur :

```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

## Composabilité et réutilisation

Les vues sont composées de composants réutilisables du dossier `Components` :
- Cards pour afficher des informations
- Formulaires pour la saisie de données
- Listes pour afficher des collections d'éléments
- Boutons et autres éléments d'interaction

## Navigation

La navigation entre les vues est gérée par un système de routage qui permet :
- Une navigation par pile (push/pop)
- Une navigation par onglets
- Le passage de paramètres entre les vues

## Exemple de vue

```kotlin
@Composable
fun RationsView(
    viewModel: RationViewModel,
    onNavigateToFoodList: () -> Unit
) {
    val rationState by viewModel.selectedRation.collectAsState()
    val analyseNutritionnelle by viewModel.analyseNutritionnelle.collectAsState()
    val aliments by viewModel.aliments.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // En-tête avec titre et boutons d'action
        RationHeader(
            rationState?.titre ?: "Nouvelle ration",
            onAddAlimentClick = { showAddDialog = true }
        )
        
        // Liste des aliments dans la ration
        AlimentList(
            aliments = rationState?.aliments ?: emptyList(),
            onDeleteAliment = { viewModel.deleteAliment(it) },
            onUpdateQuantity = { aliment, quantity -> 
                viewModel.updateAlimentQuantity(aliment, quantity) 
            }
        )
        
        // Analyse nutritionnelle
        AnalyseNutritionnelle(analyseNutritionnelle)
    }
    
    // Dialogue d'ajout d'aliment
    if (showAddDialog) {
        AddAlimentDialog(
            searchText = searchText,
            onSearchTextChange = { searchText = it },
            onDismiss = { showAddDialog = false },
            onAlimentSelected = { aliment, quantity ->
                viewModel.addAliment(aliment, quantity)
                showAddDialog = false
            },
            onNavigateToFoodList = onNavigateToFoodList,
            filteredAliments = viewModel.getFilteredAliments(searchText)
        )
    }
}
```

## Relations avec d'autres modules

- **ViewModel** : Les vues observent les données exposées par les ViewModels et leur délèguent les actions
- **Components** : Les vues utilisent des composants réutilisables pour construire leur interface
- **Theme** : Les vues utilisent les thèmes définis pour garantir une cohérence visuelle
- **Localization** : Les vues utilisent les ressources localisées pour l'internationalisation
- **Data** : Les vues affichent les données des modèles exposés par les ViewModels 