package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import fr.vetbrain.vetnutri_mp.Components.Badge
import fr.vetbrain.vetnutri_mp.Components.ConfirmDialog
import fr.vetbrain.vetnutri_mp.Components.GenericDropdown
import fr.vetbrain.vetnutri_mp.Components.TopBar
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.FoodListViewModel
import fr.vetbrain.vetnutri_mp.View.Components.FoodSearchComponent
import fr.vetbrain.vetnutri_mp.View.Components.FoodSearchConfig
import fr.vetbrain.vetnutri_mp.Data.FoodSearchFilters
import fr.vetbrain.vetnutri_mp.View.Components.FoodSearchLayout
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.launch

/**
 * Liste des aliments.
 * - Utilise `FoodSearchComponent` pour filtrer côté UI à partir de `allFoods`.
 * - Synchronise les filtres saisis avec le `FoodListViewModel` (flow interne).
 */
@OptIn(ExperimentalUuidApi::class, ExperimentalMaterialApi::class)
@Composable
fun FoodListView(
        viewModel: FoodListViewModel,
        onNavigateBack: () -> Unit,
        onOpenSettings: () -> Unit,
        onEditFood: (String) -> Unit = {},
        onCreateFood: () -> Unit = {},
        modifier: Modifier = Modifier
) {
        val foods: List<AlimentEv> = viewModel.foods.collectAsState().value
        val allFoods: List<AlimentEv> = viewModel.allFoods.collectAsState().value
        val searchQuery = viewModel.searchQuery.collectAsState().value
        val selectedFoodType = viewModel.selectedFoodType.collectAsState().value
        val selectedEspece = viewModel.selectedEspece.collectAsState().value
        val selectedIndication = viewModel.selectedIndication.collectAsState().value
        val selectedDataB = viewModel.selectedDataB.collectAsState().value
        val availableFoodTypes = viewModel.availableFoodTypes.collectAsState().value
        val availableIndications = viewModel.availableIndications.collectAsState().value
        val availableEspeces = viewModel.availableEspeces
        val coroutineScope = rememberCoroutineScope()
        var advancedFilters by remember { mutableStateOf(FoodSearchFilters()) }

        // Conversion des filtres du ViewModel vers FoodSearchFilters (en conservant les filtres
        // avancés en local)
        val filters = remember(
                searchQuery,
                selectedFoodType,
                selectedEspece,
                selectedIndication,
                selectedDataB,
                advancedFilters
        ) {
                FoodSearchFilters(
                        searchQuery = searchQuery,
                        selectedFoodType = selectedFoodType,
                        selectedFoodGroup = null, // Pas de filtre par groupe
                        selectedEspece = selectedEspece,
                        selectedIndications = if (selectedIndication != null) setOf(selectedIndication) else emptySet(),
                        dataB = selectedDataB,
                        includeDeprecated = advancedFilters.includeDeprecated,
                        aminoOnly = advancedFilters.aminoOnly,
                        nutrientFilters = advancedFilters.nutrientFilters,
                        sortCriteria = advancedFilters.sortCriteria,
                        sortOrder = advancedFilters.sortOrder
                )
        }

        // Configuration pour FoodSearchComponent
        val searchConfig = remember {
                FoodSearchConfig(
                        layout = FoodSearchLayout.HORIZONTAL,
                        showFilters = true,
                        showSearchBar = true,
                        showResultsCount = true,
                        availableActions = listOf(LocalizationKeys.General.EDIT, LocalizationKeys.General.DELETE),
                        onFoodAction = { aliment, actionKey ->
                                when (actionKey) {
                                        LocalizationKeys.General.EDIT -> onEditFood(aliment.uuid)
                                        LocalizationKeys.General.DELETE -> viewModel.deleteFood(aliment)
                                }
                        },
                        onLoadNutrients = { foodUuids, nutrients ->
                                viewModel.loadNutrientsForFoods(foodUuids, nutrients)
                        }
                )
        }

        LaunchedEffect(Unit) { viewModel.loadFoods() }

        Scaffold(
                modifier = modifier,
                topBar = {
                        TopBar(
                                title = translate(LocalizationKeys.Food.LIST_TITLE),
                                onBackClick = onNavigateBack,
                                onSettingsClick = onOpenSettings
                        )
                },
                floatingActionButton = {
                        FloatingActionButton(
                                onClick = { onCreateFood() },
                                backgroundColor = VetNutriColors.Primary
                        ) {
                                Text(
                                        "+",
                                        style =
                                                MaterialTheme.typography.h5.copy(
                                                        fontSize = AppSizes.fontSizeH5
                                                ),
                                        color = MaterialTheme.colors.onPrimary
                                )
                        }
                }
        ) { paddingValues ->
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(paddingValues)
                                        .padding(AppSizes.paddingMedium),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        // Composant partagé FoodSearchComponent :
                        // - allFoods (liste complète) en entrée
                        // - filtrage géré côté composant, ViewModel conservant les filtres
                        FoodSearchComponent(
                                foods = allFoods,
                                filters = filters,
                                onFiltersChange = { newFilters ->
                                        advancedFilters = newFilters
                                        // Mettre à jour le ViewModel avec les nouveaux filtres
                                        viewModel.setSearchQuery(newFilters.searchQuery)
                                        viewModel.setSelectedFoodType(newFilters.selectedFoodType)
                                        // Pas de setSelectedFoodGroup car on ne filtre plus par groupe
                                        viewModel.setSelectedEspece(newFilters.selectedEspece)
                                        newFilters.selectedIndications.firstOrNull()?.let { indication ->
                                                viewModel.setSelectedIndication(indication)
                                        } ?: viewModel.setSelectedIndication(null)
                                        viewModel.setSelectedDataB(newFilters.dataB)
                                },
                                config = searchConfig,
                                onSearchSubmit = { viewModel.forceRefreshSearch() },
                                modifier = Modifier.fillMaxWidth()
                                )
                }
        }
}
