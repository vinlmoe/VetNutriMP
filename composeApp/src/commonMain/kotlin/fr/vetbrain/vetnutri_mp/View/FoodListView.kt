package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.ConfirmDialog
import fr.vetbrain.vetnutri_mp.Components.TopBar
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
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
        modifier: Modifier = Modifier
) {
    val foods: List<AlimentEv> = viewModel.foods.collectAsState().value
    val searchQuery = viewModel.searchQuery.collectAsState().value
    val selectedFoodType = viewModel.selectedFoodType.collectAsState().value
    val selectedFoodGroup = viewModel.selectedFoodGroup.collectAsState().value
    val selectedEspece = viewModel.selectedEspece.collectAsState().value
    val availableFoodTypes = viewModel.availableFoodTypes.collectAsState().value
    val availableFoodGroups = viewModel.availableFoodGroups.collectAsState().value
    val availableEspeces = viewModel.availableEspeces
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.loadFoods() }

    Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar(
                title = "Liste des aliments",
                onBackClick = onNavigateBack,
                onSettingsClick = onOpenSettings
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filtres de recherche
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Champ de recherche
            OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text("${General.SEARCH.translate()} (nom, marque, ingrédients)")
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
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

        Spacer(modifier = Modifier.height(8.dp))

        // Filtres par type, groupe d'aliment et espèce
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Combobox pour filtrer par type d'aliment
            FoodTypeDropdown(
                    selectedFoodType = selectedFoodType,
                    onFoodTypeSelected = { viewModel.setSelectedFoodType(it) },
                    availableFoodTypes = availableFoodTypes,
                    modifier = Modifier.weight(1f)
            )

            // Combobox pour filtrer par groupe d'aliment
            FoodGroupDropdown(
                    selectedFoodGroup = selectedFoodGroup,
                    onFoodGroupSelected = { viewModel.setSelectedFoodGroup(it) },
                    availableFoodGroups = availableFoodGroups,
                    modifier = Modifier.weight(1f)
            )

            // Combobox pour filtrer par espèce
            EspeceDropdown(
                    selectedEspece = selectedEspece,
                    onEspeceSelected = { viewModel.setSelectedEspece(it) },
                    availableEspeces = availableEspeces,
                    modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (foods.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                        text =
                                if (searchQuery.isEmpty() &&
                                                selectedFoodType == null &&
                                                selectedFoodGroup == null &&
                                                selectedEspece == null
                                )
                                        "Aucun aliment trouvé"
                                else "Aucun résultat pour les filtres sélectionnés",
                        style = MaterialTheme.typography.body1
                )
            }
        } else {
            LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(foods) { food ->
                    FoodCard(food = food, onDelete = { viewModel.deleteFood(food) })
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
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = modifier
    ) {
        OutlinedTextField(
                value = selectedFoodType?.name ?: "Tous types",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp
                            else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                    )
                },
                colors =
                        TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = VetNutriColors.Primary,
                                unfocusedBorderColor = Color.Gray
                        ),
                modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize()
        ) {
            availableFoodTypes.forEach { foodType ->
                DropdownMenuItem(
                        onClick = {
                            onFoodTypeSelected(foodType)
                            expanded = false
                        }
                ) { Text(foodType?.name ?: "Tous types") }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun FoodGroupDropdown(
        selectedFoodGroup: GroupAlim?,
        onFoodGroupSelected: (GroupAlim?) -> Unit,
        availableFoodGroups: List<GroupAlim?>,
        modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = modifier
    ) {
        OutlinedTextField(
                value = selectedFoodGroup?.name ?: "Tous groupes",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp
                            else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                    )
                },
                colors =
                        TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = VetNutriColors.Primary,
                                unfocusedBorderColor = Color.Gray
                        ),
                modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize()
        ) {
            availableFoodGroups.forEach { foodGroup ->
                DropdownMenuItem(
                        onClick = {
                            onFoodGroupSelected(foodGroup)
                            expanded = false
                        }
                ) { Text(foodGroup?.name ?: "Tous groupes") }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun EspeceDropdown(
        selectedEspece: Espece?,
        onEspeceSelected: (Espece?) -> Unit,
        availableEspeces: List<Espece?>,
        modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = modifier
    ) {
        OutlinedTextField(
                value = selectedEspece?.name ?: "Toutes espèces",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp
                            else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                    )
                },
                colors =
                        TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = VetNutriColors.Primary,
                                unfocusedBorderColor = Color.Gray
                        ),
                modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize()
        ) {
            availableEspeces.forEach { espece ->
                DropdownMenuItem(
                        onClick = {
                            onEspeceSelected(espece)
                            expanded = false
                        }
                ) { Text(espece?.name ?: "Toutes espèces") }
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun FoodCard(food: AlimentEv, onDelete: suspend () -> Unit, modifier: Modifier = Modifier) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Card(modifier = modifier.fillMaxWidth(), elevation = 4.dp) {
        Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = food.nom ?: "Sans nom", style = MaterialTheme.typography.h6)
                    if (food.brand?.isNotEmpty() == true) {
                        Text(text = "Marque: ${food.brand}", style = MaterialTheme.typography.body1)
                    }
                    if (food.group != null) {
                        Text(
                                text = "Groupe: ${food.group.name}",
                                style = MaterialTheme.typography.body2
                        )
                    }
                    if (food.typeAliment != null) {
                        Text(
                                text = "Type: ${food.typeAliment.name}",
                                style = MaterialTheme.typography.body2
                        )
                    }
                    if (food.especes.isNotEmpty()) {
                        Text(
                                text = "Espèces: ${food.especes.joinToString(", ")}",
                                style = MaterialTheme.typography.body2
                        )
                    }
                }
                Row {
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        // TODO: Ajouter une icône de suppression
                        Text("🗑")
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
