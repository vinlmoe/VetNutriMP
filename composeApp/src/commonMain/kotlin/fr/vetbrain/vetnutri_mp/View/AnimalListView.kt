package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.ConfirmDialog
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterialApi::class)
@Composable
fun AnimalListView(
        viewModel: AnimalListViewModel,
        onAddAnimal: () -> Unit,
        onSelectAnimal: (AnimalEv) -> Unit,
        onEditAnimal: (AnimalEv) -> Unit,
        onShowFoodList: () -> Unit,
        onShowCalculationTabs: () -> Unit,
        modifier: Modifier = Modifier
) {
        val animals: List<AnimalEv> = viewModel.animals.collectAsState().value
        val searchQuery = viewModel.searchQuery.collectAsState().value
        val selectedEspece = viewModel.selectedEspece.collectAsState().value
        val coroutineScope = rememberCoroutineScope()

        // États pour l'import rapide
        var showImportDialog by remember { mutableStateOf(false) }
        var importCode by remember { mutableStateOf("") }
        
        // États pour le suivi de l'import API
        val apiImportResult = viewModel.importResult.collectAsState().value
        val apiImporting = viewModel.isApiImporting.collectAsState().value
        val apiProgress = viewModel.apiImportProgress.collectAsState().value
        val apiLogs = viewModel.apiImportLogs.collectAsState().value
        var showApiResultDialog by remember { mutableStateOf(false) }

        LaunchedEffect(apiImportResult) {
            if (apiImportResult != null && apiImportResult is AnimalListViewModel.ImportResult.Success) {
                showApiResultDialog = true
                showImportDialog = false
            } else if (apiImportResult != null && apiImportResult is AnimalListViewModel.ImportResult.Error) {
                showApiResultDialog = true
            }
        }

        LaunchedEffect(Unit) { viewModel.loadAnimals() }

        Scaffold(
                floatingActionButton = {
                        FloatingActionButton(
                                onClick = onAddAnimal,
                                backgroundColor = VetNutriColors.Primary
                        ) {
                                Icon(
                                        imageVector = AppIcons.Add,
                                        contentDescription = "Ajouter un animal",
                                        tint = VetNutriColors.OnPrimary
                                )
                        }
                }
        ) { paddingValues ->
                Column(
                        modifier = modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                                // Boutons d'import supprimés
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

                                // Bouton pour accéder aux données de calcul
                                Button(
                                        onClick = onShowCalculationTabs,
                                        modifier = Modifier.weight(1f),
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Primary,
                                                        contentColor = VetNutriColors.OnPrimary
                                                )
                                ) { Text("Données de calcul") }

                                // Bouton Import Rapide
                                Button(
                                        onClick = { showImportDialog = true },
                                        modifier = Modifier.weight(1f),
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Secondary,
                                                        contentColor = Color.White
                                                )
                                ) { Text("Import Rapide") }
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
                                                Icon(
                                                        Icons.Default.Search,
                                                        contentDescription = null
                                                )
                                        },
                                        trailingIcon = {
                                                if (searchQuery.isNotEmpty()) {
                                                        IconButtonWithTooltip(
                                                                onClick = {
                                                                        viewModel.setSearchQuery("")
                                                                },
                                                                imageVector = Icons.Default.Clear,
                                                                contentDescription = "Effacer",
                                                                tooltip = "Effacer"
                                                        )
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
                                                        if (searchQuery.isEmpty() &&
                                                                        selectedEspece == null
                                                        )
                                                                "Aucun animal trouvé"
                                                        else
                                                                "Aucun résultat pour les filtres sélectionnés",
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
                                                        onDelete = {
                                                                viewModel.deleteAnimal(animal)
                                                        }
                                                )
                                        }
                                }
                        }
                }
        }

        if (showImportDialog) {
            var showScannerNotImpl by remember { mutableStateOf(false) }

            if (showScannerNotImpl) {
                AlertDialog(
                    onDismissRequest = { showScannerNotImpl = false },
                    title = { Text("Scanner QR Code") },
                    text = { Text("La fonctionnalité de scan par caméra sera disponible prochainement. Veuillez entrer le code manuellement.") },
                    confirmButton = {
                        Button(onClick = { showScannerNotImpl = false }) { Text("OK") }
                    }
                )
            }

            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                title = { Text("Import Rapide via Internet") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Entrez le code de partage ou l'URL jsonbin.io :")
                        OutlinedTextField(
                            value = importCode,
                            onValueChange = { importCode = it },
                            label = { Text("Code ou URL") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Ou scanner
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("- OU -", color = Color.Gray)
                        }

                        Button(
                            onClick = { showScannerNotImpl = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Scanner un QR Code")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (importCode.isNotBlank()) {
                                coroutineScope.launch {
                                    viewModel.importFromJsonBin(importCode)
                                }
                                // On ne ferme pas le dialog tout de suite, le progress va s'afficher
                            }
                        },
                        enabled = importCode.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Primary, contentColor = Color.White)
                    ) { Text("Importer") }
                },
                dismissButton = {
                    Button(onClick = { showImportDialog = false }) { Text("Annuler") }
                }
            )
        }

        if (apiImporting) {
             AlertDialog(
                onDismissRequest = {},
                title = { Text("Importation en cours...") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LinearProgressIndicator(progress = apiProgress.toFloat(), modifier = Modifier.fillMaxWidth())
                        Text("Progression: ${(apiProgress * 100).toInt()}%")

                        Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(Color.LightGray.copy(alpha = 0.3f)).padding(4.dp)) {
                            LazyColumn {
                                items(apiLogs.takeLast(5)) { log ->
                                    Text(log, style = MaterialTheme.typography.caption)
                                }
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }

        if (showApiResultDialog) {
            AlertDialog(
                onDismissRequest = {
                    showApiResultDialog = false
                    viewModel.resetImportResult() // Reset result
                },
                title = {
                    Text(if (apiImportResult is AnimalListViewModel.ImportResult.Success) "Succès" else "Erreur")
                },
                text = {
                    when (apiImportResult) {
                        is AnimalListViewModel.ImportResult.Success -> {
                            Column {
                                Text("Import terminé avec succès!")
                                Text("Total éléments: ${apiImportResult.count}")
                            }
                        }
                        is AnimalListViewModel.ImportResult.Error -> {
                            Text(apiImportResult.message)
                        }
                        null -> {}
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showApiResultDialog = false
                        viewModel.resetImportResult()
                    }) { Text("OK") }
                }
            )
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
                                                        "${Animal.SPECIES.translate()}: ${animal.getEspece().translateEnum()}",
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
                                        IconButtonWithTooltip(
                                                onClick = onClick,
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Détails",
                                                tooltip = "Voir les détails"
                                        )
                                        IconButtonWithTooltip(
                                                onClick = { showDeleteConfirmation = true },
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Supprimer",
                                                tooltip = "Supprimer",
                                                tint = Color.Red
                                        )
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
