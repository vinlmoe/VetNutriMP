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
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterialApi::class)
@Composable
fun AnimalListView(
        viewModel: AnimalListViewModel,
        onAddAnimal: () -> Unit,
        onSelectAnimal: (AnimalEv) -> Unit,
        onEditAnimal: (AnimalEv) -> Unit,
        onImportAnimals: () -> Unit,
        onImportFoods: () -> Unit,
        onShowFoodList: () -> Unit,
        onShowCalculationTabs: () -> Unit,
        modifier: Modifier = Modifier
) {
        val animals: List<AnimalEv> = viewModel.animals.collectAsState().value
        val searchQuery = viewModel.searchQuery.collectAsState().value
        val selectedEspece = viewModel.selectedEspece.collectAsState().value

        LaunchedEffect(Unit) { viewModel.loadAnimals() }

        Column(
                modifier = modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                        Button(
                                onClick = onAddAnimal,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Primary,
                                                contentColor = VetNutriColors.OnPrimary
                                        )
                        ) { Text(General.ADD.translate()) }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                        onClick = onImportAnimals,
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Secondary,
                                                        contentColor = VetNutriColors.OnSecondary
                                                )
                                ) { Text(General.IMPORT.translate() + " " + "Animaux") }

                                Button(
                                        onClick = onImportFoods,
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Secondary,
                                                        contentColor = VetNutriColors.OnSecondary
                                                )
                                ) { Text(General.IMPORT.translate() + " " + "Aliments") }
                        }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Rangée de boutons pour les listes spéciales
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        // Bouton pour accéder à la liste des aliments
                        Button(
                                onClick = onShowFoodList,
                                modifier = Modifier.weight(1f),
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Primary,
                                                contentColor = VetNutriColors.OnPrimary
                                        )
                        ) { Text("Liste des aliments") }

                        // Bouton pour accéder aux données de calcul (remplace les deux boutons
                        // précédents)
                        Button(
                                onClick = onShowCalculationTabs,
                                modifier = Modifier.weight(1f),
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Primary,
                                                contentColor = VetNutriColors.OnPrimary
                                        )
                        ) { Text("Données de calcul") }
                }

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
                                modifier = Modifier.weight(2f),
                                placeholder = {
                                        Text(
                                                "${General.SEARCH.translate()} (nom, propriétaire, race)"
                                        )
                                },
                                leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = null)
                                },
                                trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                                IconButton(
                                                        onClick = { viewModel.setSearchQuery("") }
                                                ) {
                                                        Icon(
                                                                Icons.Default.Clear,
                                                                contentDescription = null
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

                        // Combobox pour filtrer par espèce
                        EspeceDropdown(
                                selectedEspece = selectedEspece,
                                onEspeceSelected = { viewModel.setSelectedEspece(it) },
                                availableEspeces = viewModel.availableEspeces,
                                modifier = Modifier.weight(1f)
                        )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (animals.isEmpty()) {
                        Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                        ) {
                                Text(
                                        text =
                                                if (searchQuery.isEmpty() && selectedEspece == null)
                                                        "Aucun animal trouvé"
                                                else "Aucun résultat pour les filtres sélectionnés",
                                        style = MaterialTheme.typography.body1
                                )
                        }
                } else {
                        LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                items(animals) { animal ->
                                        AnimalCard(
                                                animal = animal,
                                                onClick = { onSelectAnimal(animal) },
                                                onDelete = { viewModel.deleteAnimal(animal) }
                                        )
                                }
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
                        value = selectedEspece?.label ?: "Toutes espèces",
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
                                ) { Text(espece?.label ?: "Toutes espèces") }
                        }
                }
        }
}

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterialApi::class)
@Composable
private fun AnimalCard(
        animal: AnimalEv,
        onClick: () -> Unit,
        onDelete: () -> Unit,
        modifier: Modifier = Modifier
) {
        var showDeleteConfirmation by remember { mutableStateOf(false) }

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
                                        Text(text = animal.nom, style = MaterialTheme.typography.h6)
                                        Text(
                                                text =
                                                        "${Animal.SPECIES.translate()}: ${animal.getEspece().label}",
                                                style = MaterialTheme.typography.body1
                                        )
                                        if (animal.race.isNotEmpty()) {
                                                Text(
                                                        text =
                                                                "${Animal.BREED.translate()}: ${animal.race}",
                                                        style = MaterialTheme.typography.body2
                                                )
                                        }
                                        if (animal.ownerName.isNotEmpty()) {
                                                Text(
                                                        text =
                                                                "${Animal.OWNER.translate()}: ${animal.ownerName}",
                                                        style = MaterialTheme.typography.body2
                                                )
                                        }
                                }
                                Row {
                                        IconButton(onClick = onClick) {
                                                // TODO: Ajouter une icône de détails
                                                Text("→")
                                        }
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
                        title = Animal.DELETE_ANIMAL.translate(),
                        message = Animal.DELETE_ANIMAL_CONFIRM.translate(),
                        onConfirm = {
                                onDelete()
                                showDeleteConfirmation = false
                        },
                        onDismiss = { showDeleteConfirmation = false }
                )
        }
}
