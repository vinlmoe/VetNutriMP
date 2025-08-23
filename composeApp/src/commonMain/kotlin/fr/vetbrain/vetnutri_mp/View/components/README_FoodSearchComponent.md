# FoodSearchComponent - Composant de recherche d'aliments partagé

## 📋 Description

`FoodSearchComponent` est un composant Compose partagé qui fournit une interface unifiée pour la recherche et le filtrage d'aliments dans l'application VetNutriMP.

## 🎯 Fonctionnalités

### ✅ Filtres disponibles
- **Recherche textuelle** : nom, marque, ingrédients
- **Type d'aliment** : croquettes, pâtée, etc.
- **Groupe d'aliment** : viande, céréales, etc.
- **Espèce ciblée** : chien, chat, etc.
- **Indications** : sensible, senior, etc.

### ✅ Layouts disponibles
- **VERTICAL** : Layout vertical simple (comme FoodListView)
- **HORIZONTAL** : Layout à deux colonnes (comme AddAlimentView)
- **COMPACT** : Layout compact pour les petits espaces

### ✅ Personnalisation
- Affichage/masquage des filtres
- Affichage/masquage de la barre de recherche
- Compteur de résultats
- Actions personnalisables sur les aliments

## 🚀 Utilisation de base

```kotlin
@Composable
fun MyFoodSearchView() {
    var filters by remember { mutableStateOf(FoodSearchFilters()) }
    
    val config = FoodSearchConfig(
        layout = FoodSearchLayout.VERTICAL,
        onFoodSelected = { aliment -> /* Action */ }
    )
    
    FoodSearchComponent(
        foods = myFoodsList,
        filters = filters,
        onFiltersChange = { filters = it },
        config = config
    )
}
```

## ⚙️ Configuration

### FoodSearchConfig

```kotlin
data class FoodSearchConfig(
    val showFilters: Boolean = true,           // Afficher les filtres
    val showSearchBar: Boolean = true,         // Afficher la barre de recherche
    val showResultsCount: Boolean = true,      // Afficher le compteur de résultats
    val layout: FoodSearchLayout = FoodSearchLayout.VERTICAL,  // Type de layout
    val maxHeight: Int? = null,                // Hauteur maximale
    val onFoodSelected: ((AlimentEv) -> Unit)? = null,        // Callback de sélection
    val onFoodAction: ((AlimentEv, String) -> Unit)? = null,  // Callback d'action
    val availableActions: List<String> = emptyList()           // Actions disponibles
)
```

### FoodSearchFilters

```kotlin
data class FoodSearchFilters(
    val searchQuery: String = "",                    // Recherche textuelle
    val selectedFoodType: FoodKind? = null,         // Type d'aliment
    val selectedFoodGroup: GroupAlim? = null,       // Groupe d'aliment
    val selectedEspece: Espece? = null,             // Espèce
    val selectedIndications: Set<AlimIndic> = emptySet()  // Indications
)
```

## 📱 Exemples d'utilisation

### 1. Vue simple (FoodListView)
```kotlin
val config = FoodSearchConfig(
    layout = FoodSearchLayout.VERTICAL,
    showFilters = true,
    availableActions = listOf("Éditer", "Supprimer"),
    onFoodAction = { aliment, action -> 
        when (action) {
            "Éditer" -> editFood(aliment)
            "Supprimer" -> deleteFood(aliment)
        }
    }
)
```

### 2. Vue avec détails (AddAlimentView)
```kotlin
val config = FoodSearchConfig(
    layout = FoodSearchLayout.HORIZONTAL,
    showFilters = true,
    onFoodSelected = { aliment -> selectedFood = aliment }
)
```

### 3. Vue compacte (AnalyseSelectionAlimentsView)
```kotlin
val config = FoodSearchConfig(
    layout = FoodSearchLayout.COMPACT,
    showFilters = true,
    showResultsCount = false
)
```

## 🔧 Migration depuis les vues existantes

### AddAlimentView → FoodSearchComponent
- Remplacer la logique de filtrage par `FoodSearchComponent`
- Utiliser `FoodSearchLayout.HORIZONTAL`
- Conserver la logique de sélection et de quantité

### FoodListView → FoodSearchComponent
- Remplacer les filtres existants par `FoodSearchComponent`
- Utiliser `FoodSearchLayout.VERTICAL`
- Conserver les actions d'édition/suppression

### AnalyseSelectionAlimentsView → FoodSearchComponent
- Ajouter la recherche et le filtrage
- Utiliser `FoodSearchLayout.COMPACT`
- Intégrer avec l'analyse existante

## 🎨 Personnalisation avancée

### Actions personnalisées
```kotlin
val config = FoodSearchConfig(
    availableActions = listOf("Ajouter", "Comparer", "Favoris"),
    onFoodAction = { aliment, action ->
        when (action) {
            "Ajouter" -> addToRation(aliment)
            "Comparer" -> compareWith(aliment)
            "Favoris" -> toggleFavorite(aliment)
        }
    }
)
```

### Filtres personnalisés
```kotlin
// Les filtres sont automatiquement gérés par le composant
// Pas besoin de logique personnalisée
var filters by remember { mutableStateOf(FoodSearchFilters()) }

// Les changements sont automatiquement appliqués
FoodSearchComponent(
    foods = foods,
    filters = filters,
    onFiltersChange = { filters = it },
    config = config
)
```

## 📊 Performance

- **Mémorisation** : Les résultats filtrés sont mémorisés avec `remember`
- **Filtrage efficace** : Logique de filtrage optimisée
- **Re-rendu minimal** : Seuls les changements de filtres déclenchent un re-filtrage

## 🔍 Dépannage

### Problèmes courants

1. **Filtres ne fonctionnent pas**
   - Vérifier que `onFiltersChange` est bien implémenté
   - Vérifier que les `FoodSearchFilters` sont bien mis à jour

2. **Layout incorrect**
   - Vérifier la valeur de `FoodSearchLayout` dans la config
   - Vérifier que le `modifier` est correctement défini

3. **Actions non visibles**
   - Vérifier que `availableActions` n'est pas vide
   - Vérifier que `onFoodAction` est défini

## 🚀 Prochaines étapes

1. **Intégration** dans AddAlimentView
2. **Migration** de FoodListView
3. **Extension** d'AnalyseSelectionAlimentsView
4. **Tests** et validation
5. **Optimisations** de performance
