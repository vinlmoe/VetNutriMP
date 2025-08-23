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
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.FoodListViewModel
import fr.vetbrain.vetnutri_mp.View.components.FoodSearchComponent
import fr.vetbrain.vetnutri_mp.View.components.FoodSearchConfig
import fr.vetbrain.vetnutri_mp.View.components.FoodSearchFilters
import fr.vetbrain.vetnutri_mp.View.components.FoodSearchLayout
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.launch

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
        val searchQuery = viewModel.searchQuery.collectAsState().value
        val selectedFoodType = viewModel.selectedFoodType.collectAsState().value
        val selectedFoodGroup = viewModel.selectedFoodGroup.collectAsState().value
        val selectedEspece = viewModel.selectedEspece.collectAsState().value
        val selectedIndication = viewModel.selectedIndication.collectAsState().value
        val availableFoodTypes = viewModel.availableFoodTypes.collectAsState().value
        val availableFoodGroups = viewModel.availableFoodGroups.collectAsState().value
        val availableIndications = viewModel.availableIndications.collectAsState().value
        val availableEspeces = viewModel.availableEspeces
        val coroutineScope = rememberCoroutineScope()

        // Conversion des filtres du ViewModel vers FoodSearchFilters
        val filters = remember(
                searchQuery,
                selectedFoodType,
                selectedFoodGroup,
                selectedEspece,
                selectedIndication
        ) {
                FoodSearchFilters(
                        searchQuery = searchQuery,
                        selectedFoodType = selectedFoodType,
                        selectedFoodGroup = selectedFoodGroup,
                        selectedEspece = selectedEspece,
                        selectedIndications = if (selectedIndication != null) setOf(selectedIndication) else emptySet()
                )
        }

        // Configuration pour FoodSearchComponent
        val searchConfig = remember {
                FoodSearchConfig(
                        layout = FoodSearchLayout.VERTICAL,
                        showFilters = true,
                        showSearchBar = true,
                        showResultsCount = true,
                        availableActions = listOf("Éditer", "Supprimer"),
                        onFoodAction = { aliment, action ->
                                when (action) {
                                        "Éditer" -> onEditFood(aliment.uuid)
                                        "Supprimer" -> viewModel.deleteFood(aliment)
                                }
                        }
                )
        }

        LaunchedEffect(Unit) { viewModel.loadFoods() }

        Scaffold(
                modifier = modifier,
                topBar = {
                        TopBar(
                                title = "Liste des aliments",
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
                        // Utilisation du composant partagé FoodSearchComponent
                        FoodSearchComponent(
                                foods = foods,
                                filters = filters,
                                onFiltersChange = { newFilters ->
                                        // Mettre à jour le ViewModel avec les nouveaux filtres
                                        viewModel.setSearchQuery(newFilters.searchQuery)
                                        viewModel.setSelectedFoodType(newFilters.selectedFoodType)
                                        viewModel.setSelectedFoodGroup(newFilters.selectedFoodGroup)
                                        viewModel.setSelectedEspece(newFilters.selectedEspece)
                                        newFilters.selectedIndications.firstOrNull()?.let { indication ->
                                                viewModel.setSelectedIndication(indication)
                                        } ?: viewModel.setSelectedIndication(null)
                                },
                                config = searchConfig,
                                        modifier = Modifier.fillMaxWidth()
                                )
                }
        }
}
