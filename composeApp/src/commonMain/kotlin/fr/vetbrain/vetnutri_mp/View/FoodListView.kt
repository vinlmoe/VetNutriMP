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
                        // Filtres de recherche
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                // Champ de recherche
                                OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { viewModel.setSearchQuery(it) },
                                        modifier = Modifier.weight(1f),
                                        placeholder = {
                                                Text(
                                                        "${General.SEARCH.translate()} (nom, marque, ingrédients)",
                                                        style =
                                                                MaterialTheme.typography.body1.copy(
                                                                        fontSize =
                                                                                AppSizes.fontSizeBody1
                                                                )
                                                )
                                        },
                                        leadingIcon = {
                                                Icon(
                                                        Icons.Default.Search,
                                                        contentDescription = null,
                                                        modifier =
                                                                Modifier.size(
                                                                        AppSizes.iconSizeMedium
                                                                )
                                                )
                                        },
                                        trailingIcon = {
                                                if (searchQuery.isNotEmpty()) {
                                                        IconButton(
                                                                onClick = {
                                                                        viewModel.setSearchQuery("")
                                                                }
                                                        ) {
                                                                Icon(
                                                                        Icons.Default.Clear,
                                                                        contentDescription = null,
                                                                        modifier =
                                                                                Modifier.size(
                                                                                        AppSizes.iconSizeMedium
                                                                                )
                                                                )
                                                        }
                                                }
                                        },
                                        singleLine = true,
                                        colors =
                                                TextFieldDefaults.outlinedTextFieldColors(
                                                        focusedBorderColor = VetNutriColors.Primary,
                                                        unfocusedBorderColor = Color.Gray
                                                )
                                )
                        }

                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                        // Filtres par type, groupe d'aliment et espèce
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                FoodTypeDropdown(
                                        selectedFoodType = selectedFoodType,
                                        onFoodTypeSelected = { viewModel.setSelectedFoodType(it) },
                                        availableFoodTypes = availableFoodTypes,
                                        modifier = Modifier.weight(1f)
                                )

                                FoodGroupDropdown(
                                        selectedFoodGroup = selectedFoodGroup,
                                        onFoodGroupSelected = {
                                                viewModel.setSelectedFoodGroup(it)
                                        },
                                        availableFoodGroups = availableFoodGroups,
                                        modifier = Modifier.weight(1f)
                                )

                                EspeceDropdown(
                                        selectedEspece = selectedEspece,
                                        onEspeceSelected = { viewModel.setSelectedEspece(it) },
                                        availableEspeces = availableEspeces,
                                        modifier = Modifier.weight(1f)
                                )
                        }

                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                        // Filtre par indication
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                IndicationDropdown(
                                        selectedIndication = selectedIndication,
                                        onIndicationSelected = {
                                                viewModel.setSelectedIndication(it)
                                        },
                                        availableIndications = availableIndications,
                                        modifier = Modifier.fillMaxWidth()
                                )
                        }

                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

                        if (foods.isEmpty()) {
                                Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                ) {
                                        Text(
                                                text =
                                                        if (searchQuery.isEmpty() &&
                                                                        selectedFoodType == null &&
                                                                        selectedFoodGroup == null &&
                                                                        selectedEspece == null &&
                                                                        selectedIndication == null
                                                        )
                                                                "Aucun aliment trouvé"
                                                        else
                                                                "Aucun résultat pour les filtres sélectionnés",
                                                style =
                                                        MaterialTheme.typography.body1.copy(
                                                                fontSize = AppSizes.fontSizeBody1
                                                        )
                                        )
                                }
                        } else {
                                LazyColumn(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement =
                                                Arrangement.spacedBy(AppSizes.cardSpacing)
                                ) {
                                        items(foods) { food ->
                                                FoodCard(
                                                        food = food,
                                                        onDelete = { viewModel.deleteFood(food) },
                                                        onEdit = { onEditFood(food.uuid) }
                                                )
                                        }
                                }
                        }
                }
        }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun FoodTypeDropdown(
        selectedFoodType: FoodKind?,
        onFoodTypeSelected: (FoodKind?) -> Unit,
        availableFoodTypes: List<FoodKind?>,
        modifier: Modifier = Modifier
) {
        GenericDropdown(
                selectedItem = selectedFoodType,
                onItemSelected = onFoodTypeSelected,
                items = availableFoodTypes,
                getDisplayText = { it?.name ?: "Tous types" },
                placeholder = "Tous types",
                modifier = modifier
        )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun FoodGroupDropdown(
        selectedFoodGroup: GroupAlim?,
        onFoodGroupSelected: (GroupAlim?) -> Unit,
        availableFoodGroups: List<GroupAlim?>,
        modifier: Modifier = Modifier
) {
        GenericDropdown(
                selectedItem = selectedFoodGroup,
                onItemSelected = onFoodGroupSelected,
                items = availableFoodGroups,
                getDisplayText = { it?.name ?: "Tous groupes" },
                placeholder = "Tous groupes",
                modifier = modifier
        )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun EspeceDropdown(
        selectedEspece: Espece?,
        onEspeceSelected: (Espece?) -> Unit,
        availableEspeces: List<Espece?>,
        modifier: Modifier = Modifier
) {
        GenericDropdown(
                selectedItem = selectedEspece,
                onItemSelected = onEspeceSelected,
                items = availableEspeces,
                getDisplayText = { it?.name ?: "Toutes espèces" },
                placeholder = "Toutes espèces",
                modifier = modifier
        )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun IndicationDropdown(
        selectedIndication: AlimIndic?,
        onIndicationSelected: (AlimIndic?) -> Unit,
        availableIndications: List<AlimIndic?>,
        modifier: Modifier = Modifier
) {
        GenericDropdown(
                selectedItem = selectedIndication,
                onItemSelected = onIndicationSelected,
                items = availableIndications,
                getDisplayText = { it?.label ?: "Toutes indications" },
                placeholder = "Toutes indications",
                modifier = modifier
        )
}

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun FoodCard(
        food: AlimentEv,
        onDelete: suspend () -> Unit,
        onEdit: () -> Unit = {},
        modifier: Modifier = Modifier
) {
        var showDeleteConfirmation by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        // Fonction utilitaire pour obtenir l'espèce à partir d'une chaîne
        fun getEspeceFromString(especeLabel: String): Espece? {
                return Espece.getFromString(especeLabel)
        }

        Card(
                modifier = modifier.fillMaxWidth().clickable { onEdit() },
                elevation = AppSizes.cardElevationNormal
        ) {
                Column(
                        modifier = Modifier.padding(AppSizes.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                text = food.nom ?: "Sans nom",
                                                style =
                                                        MaterialTheme.typography.h6.copy(
                                                                fontSize = AppSizes.fontSizeH6
                                                        )
                                        )
                                        if (food.brand?.isNotEmpty() == true) {
                                                Text(
                                                        text = "Marque: ${food.brand}",
                                                        style =
                                                                MaterialTheme.typography.body1.copy(
                                                                        fontSize =
                                                                                AppSizes.fontSizeBody1
                                                                )
                                                )
                                        }
                                        if (food.group != null) {
                                                Text(
                                                        text = "Groupe: ${food.group.name}",
                                                        style =
                                                                MaterialTheme.typography.body2.copy(
                                                                        fontSize =
                                                                                AppSizes.fontSizeBody2
                                                                )
                                                )
                                        }
                                        if (food.typeAliment != null) {
                                                Text(
                                                        text = "Type: ${food.typeAliment.name}",
                                                        style =
                                                                MaterialTheme.typography.body2.copy(
                                                                        fontSize =
                                                                                AppSizes.fontSizeBody2
                                                                )
                                                )
                                        }
                                }
                                Row {
                                        IconButton(
                                                onClick = { showDeleteConfirmation = true },
                                                modifier = Modifier.size(AppSizes.iconSizeMedium)
                                        ) { Text("🗑") }
                                }
                        }

                        // Section dédiée pour les espèces et indications
                        Divider(
                                modifier = Modifier.padding(vertical = AppSizes.paddingXSmall),
                                thickness = AppSizes.dividerHeight
                        )
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                                Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                text = "Espèces",
                                                style =
                                                        MaterialTheme.typography.subtitle1.copy(
                                                                fontSize =
                                                                        AppSizes.fontSizeSubtitle1
                                                        ),
                                                color = VetNutriColors.Primary
                                        )
                                        if (food.especes.isNotEmpty()) {
                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(
                                                                        AppSizes.paddingXSmall
                                                                ),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically,
                                                        content = {
                                                                food.especes.forEach { especeLabel
                                                                        ->
                                                                        val espece =
                                                                                getEspeceFromString(
                                                                                        especeLabel
                                                                                )
                                                                        if (espece != null) {
                                                                                Badge(
                                                                                        text =
                                                                                                espece.label,
                                                                                        subText =
                                                                                                espece.name,
                                                                                        id =
                                                                                                if (espece.id !=
                                                                                                                especeLabel
                                                                                                )
                                                                                                        espece.id
                                                                                                else
                                                                                                        null,
                                                                                        backgroundColor =
                                                                                                VetNutriColors
                                                                                                        .Secondary
                                                                                )
                                                                        } else {
                                                                                Badge(
                                                                                        text =
                                                                                                especeLabel,
                                                                                        backgroundColor =
                                                                                                Color.Gray
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                )
                                        } else {
                                                Text(
                                                        text = "Aucune espèce spécifiée",
                                                        style =
                                                                MaterialTheme.typography.body2.copy(
                                                                        fontSize =
                                                                                AppSizes.fontSizeBody2
                                                                ),
                                                        color = Color.Gray,
                                                        modifier =
                                                                Modifier.padding(
                                                                        vertical =
                                                                                AppSizes.paddingXXSmall
                                                                )
                                                )
                                        }
                                }

                                Spacer(modifier = Modifier.width(AppSizes.paddingMedium))

                                Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                text = "Indications",
                                                style =
                                                        MaterialTheme.typography.subtitle1.copy(
                                                                fontSize =
                                                                        AppSizes.fontSizeSubtitle1
                                                        ),
                                                color = VetNutriColors.Primary
                                        )
                                        if (food.indicat.isNotEmpty()) {
                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(
                                                                        AppSizes.paddingXSmall
                                                                ),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically,
                                                        content = {
                                                                food.indicat.forEach { indication ->
                                                                        Badge(
                                                                                text =
                                                                                        indication
                                                                                                .label,
                                                                                subText =
                                                                                        indication
                                                                                                .name,
                                                                                id =
                                                                                        indication
                                                                                                .coef,
                                                                                backgroundColor =
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                        )
                                                                }
                                                        }
                                                )
                                        } else {
                                                Text(
                                                        text = "Aucune indication spécifiée",
                                                        style =
                                                                MaterialTheme.typography.body2.copy(
                                                                        fontSize =
                                                                                AppSizes.fontSizeBody2
                                                                ),
                                                        color = Color.Gray,
                                                        modifier =
                                                                Modifier.padding(
                                                                        vertical =
                                                                                AppSizes.paddingXXSmall
                                                                )
                                                )
                                        }
                                }
                        }
                }
        }

        if (showDeleteConfirmation) {
                ConfirmDialog(
                        title = "Supprimer l'aliment",
                        message = "Êtes-vous sûr de vouloir supprimer cet aliment ?",
                        onConfirm = {
                                coroutineScope.launch { onDelete() }
                                showDeleteConfirmation = false
                        },
                        onDismiss = { showDeleteConfirmation = false }
                )
        }
}
