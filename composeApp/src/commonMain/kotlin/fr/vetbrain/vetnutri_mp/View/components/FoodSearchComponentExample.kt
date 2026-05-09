package fr.vetbrain.vetnutri_mp.View.Components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.FoodSearchFilters

/**
 * Exemple d'utilisation du composant FoodSearchComponent
 * Ce fichier sert uniquement à tester la compilation du composant
 */
@Composable
fun FoodSearchComponentExample(
    foods: List<AlimentEv>,
    modifier: Modifier = Modifier
) {
    var filters by remember { mutableStateOf(FoodSearchFilters()) }
    
    val config = FoodSearchConfig(
        showFilters = true,
        showSearchBar = true,
        showResultsCount = true,
        layout = FoodSearchLayout.VERTICAL,
        onFoodSelected = { /* Action lors de la sélection */ },
        availableActions = listOf("Éditer", "Supprimer"),
        onFoodAction = { aliment, action -> /* Action sur l'aliment */ }
    )
    
    FoodSearchComponent(
        foods = foods,
        filters = filters,
        onFiltersChange = { filters = it },
        config = config,
        modifier = modifier
    )
}
