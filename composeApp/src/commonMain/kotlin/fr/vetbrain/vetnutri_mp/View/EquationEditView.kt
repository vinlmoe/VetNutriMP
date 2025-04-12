package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Enumer.EquationType
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.EquationViewModel

/**
 * Vue pour éditer une équation
 *
 * @param viewModel ViewModel pour gérer les équations
 * @param equationId Identifiant de l'équation (null pour une nouvelle équation)
 * @param onNavigateBack Callback pour revenir à l'écran précédent
 * @param modifier Modifier à appliquer à la vue
 */
@Composable
fun EquationEditView(
        viewModel: EquationViewModel,
        equationId: String?,
        onNavigateBack: () -> Unit,
        modifier: Modifier = Modifier
) {
    // Charger l'équation si un ID est fourni, sinon créer une nouvelle équation
    LaunchedEffect(equationId) {
        if (equationId != null) {
            viewModel.loadEquation(equationId)
        } else {
            viewModel.clearCurrentEquation()
        }
    }

    val currentEquation by viewModel.currentEquation.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val operationMessage by viewModel.operationMessage.collectAsState()

    // État pour le dialogue de sélection de référence bibliographique
    var showBiblioRefDialog by remember { mutableStateOf(false) }
    var biblioRefSearchQuery by remember { mutableStateOf("") }

    // Charger les références bibliographiques
    val biblioRefRepository = remember { viewModel.getBiblioRefRepository() }
    val allBiblioRefs =
            remember { biblioRefRepository.getAllBiblioRefs() }
                    .collectAsState(initial = emptyList())

    // Filtrer les références selon la recherche
    val filteredBiblioRefs =
            remember(allBiblioRefs.value, biblioRefSearchQuery) {
                if (biblioRefSearchQuery.isBlank()) {
                    allBiblioRefs.value
                } else {
                    val query = biblioRefSearchQuery.lowercase()
                    allBiblioRefs.value.filter { biblioRef ->
                        biblioRef.firstAuthor.lowercase().contains(query) ||
                                biblioRef.year.toString().contains(query) ||
                                biblioRef.completeRef.lowercase().contains(query) ||
                                biblioRef.comments.lowercase().contains(query)
                    }
                }
            }

    // Afficher un message de succès ou d'erreur
    LaunchedEffect(operationMessage) {
        if (operationMessage.isNotEmpty()) {
            // Ici, on pourrait afficher un message toast ou snackbar
            // Pour l'instant, on l'affiche juste dans la console
            println("Message: $operationMessage")
            viewModel.clearOperationMessage()
        }
    }

    // UI principale
    Scaffold(
            topBar = {
                TopBarSimple(
                        title =
                                if (equationId != null) "Modifier l'équation"
                                else "Nouvelle équation",
                        onNavigateBack = onNavigateBack
                )
            }
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            // Formulaire d'édition d'équation
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                // Nom de l'équation
                OutlinedTextField(
                        value = currentEquation.name,
                        onValueChange = { viewModel.updateName(it) },
                        label = { Text("Nom de l'équation") },
                        modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                OutlinedTextField(
                        value = currentEquation.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Type d'équation (dropdown)
                DropdownField(
                        label = "Type d'équation",
                        selectedValue =
                                currentEquation.kind.let { kind ->
                                    when (kind) {
                                        fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERGYNEED ->
                                                EquationType.ENERGYNEED
                                        fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERGYDENSITY ->
                                                EquationType.ENERGYDENSITY
                                        fr.vetbrain.vetnutri_mp.Enumer.EquationKind.MW ->
                                                EquationType.MW
                                        fr.vetbrain.vetnutri_mp.Enumer.EquationKind.INDICATOR ->
                                                EquationType.INDICATOR
                                        fr.vetbrain.vetnutri_mp.Enumer.EquationKind.NEED ->
                                                EquationType.NEED
                                    }
                                },
                        options = EquationType.values().toList(),
                        onValueChange = { viewModel.updateKind(it.toEquationKind()) },
                        valueToString = { it.toString() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Script de l'équation
                OutlinedTextField(
                        value = currentEquation.equationScript,
                        onValueChange = { viewModel.updateScript(it) },
                        label = { Text("Formule") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 5
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Affichage et sélection de la référence bibliographique
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                                text = "Référence bibliographique",
                                style = MaterialTheme.typography.subtitle1
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                                text =
                                        if (currentEquation.bib != null)
                                                "${currentEquation.bib.firstAuthor}, ${currentEquation.bib.year}"
                                        else "Aucune référence sélectionnée",
                                style = MaterialTheme.typography.body1
                        )

                        if (currentEquation.bib != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                    text = currentEquation.bib.completeRef,
                                    style = MaterialTheme.typography.caption
                            )
                        }
                    }

                    Button(onClick = { showBiblioRefDialog = true }, modifier = Modifier) {
                        Text(if (currentEquation.bib != null) "Changer" else "Sélectionner")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Bouton d'enregistrement
                Button(
                        onClick = { viewModel.saveEquation() },
                        modifier = Modifier.align(Alignment.End),
                        enabled = !isLoading
                ) { Text("Enregistrer") }
            }

            // Indicateur de chargement
            if (isLoading) {
                CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = VetNutriColors.Primary
                )
            }
        }
    }

    // Dialogue de sélection de référence bibliographique
    if (showBiblioRefDialog) {
        AlertDialog(
                onDismissRequest = { showBiblioRefDialog = false },
                title = { Text("Sélectionner une référence bibliographique") },
                text = {
                    Column {
                        // Barre de recherche
                        OutlinedTextField(
                                value = biblioRefSearchQuery,
                                onValueChange = { biblioRefSearchQuery = it },
                                label = { Text("Rechercher") },
                                leadingIcon = {
                                    Icon(
                                            imageVector = AppIcons.Search,
                                            contentDescription = "Rechercher"
                                    )
                                },
                                trailingIcon = {
                                    if (biblioRefSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { biblioRefSearchQuery = "" }) {
                                            Icon(
                                                    imageVector = AppIcons.Close,
                                                    contentDescription = "Effacer"
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors =
                                        TextFieldDefaults.outlinedTextFieldColors(
                                                focusedBorderColor = VetNutriColors.Primary,
                                                unfocusedBorderColor = Color.Gray
                                        )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Liste des références filtrées
                        if (filteredBiblioRefs.isEmpty()) {
                            Text(
                                    text = "Aucune référence bibliographique trouvée",
                                    style = MaterialTheme.typography.body1
                            )
                        } else {
                            LazyColumn(
                                    modifier = Modifier.height(300.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredBiblioRefs) { biblioRef ->
                                    Card(
                                            modifier =
                                                    Modifier.fillMaxWidth().clickable {
                                                        viewModel.updateBibRef(biblioRef)
                                                        showBiblioRefDialog = false
                                                        biblioRefSearchQuery = ""
                                                    },
                                            elevation = 2.dp
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                    text =
                                                            "${biblioRef.firstAuthor}, ${biblioRef.year}",
                                                    style = MaterialTheme.typography.subtitle1
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                    text = biblioRef.completeRef,
                                                    style = MaterialTheme.typography.body2
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showBiblioRefDialog = false }) { Text("Fermer") }
                },
                dismissButton = {}
        )
    }
}
