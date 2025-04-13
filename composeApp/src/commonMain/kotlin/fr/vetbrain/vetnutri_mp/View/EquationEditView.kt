package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.ViewModel.EquationViewModel
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()
    val currentEquation by viewModel.currentEquation.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val operationMessage by viewModel.operationMessage.collectAsState()
    var showErrorAlert by remember { mutableStateOf(false) }
    var showBiblioRefDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val biblioRefs by viewModel.biblioRefs.collectAsState()
    val filteredBiblioRefs =
            remember(biblioRefs, searchQuery) {
                if (searchQuery.isBlank()) {
                    biblioRefs
                } else {
                    biblioRefs.filter { ref ->
                        ref.firstAuthor.contains(searchQuery, ignoreCase = true) ||
                                ref.completeRef.contains(searchQuery, ignoreCase = true) ||
                                ref.year.toString().contains(searchQuery)
                    }
                }
            }
    var expandedKind by remember { mutableStateOf(false) }
    var expandedSpecie by remember { mutableStateOf(false) }

    // Initialisation : charger l'équation existante ou en créer une nouvelle
    LaunchedEffect(equationId) {
        println("DEBUG EquationEditView: Initialisation avec equationId = $equationId")
        // Effacer le message d'opération précédent pour éviter une navigation automatique
        viewModel.clearOperationMessage()

        if (equationId != null) {
            // Chargement d'une équation existante
            viewModel.loadEquationById(equationId)
        } else {
            // Création d'une nouvelle équation
            viewModel.createNewEquation()
        }
    }

    // Gestion des messages
    LaunchedEffect(operationMessage) {
        println("DEBUG EquationEditView: Message d'opération reçu: '$operationMessage'")
        when {
            operationMessage.isEmpty() -> {
                showErrorAlert = false
                // Ne pas naviguer en arrière sur un message vide
            }
            operationMessage.startsWith("Erreur") -> {
                println("DEBUG EquationEditView: Affichage de l'erreur")
                showErrorAlert = true
                // Ne pas naviguer en arrière sur une erreur
            }
            else -> {
                // Message de succès uniquement si l'opération vient d'être effectuée
                // et pas lors du chargement initial
                if (operationMessage.startsWith("Équation sauvegardée")) {
                    println("DEBUG EquationEditView: Sauvegarde réussie, navigation en arrière")
                    // Navigation directe en cas de succès, sans afficher de message
                    onNavigateBack()
                    viewModel.clearOperationMessage()
                } else {
                    println("DEBUG EquationEditView: Message non géré: $operationMessage")
                }
            }
        }
    }

    Scaffold(
            topBar = {
                TopBarSimple(
                        title =
                                if (equationId == null) "Nouvelle équation"
                                else "Modifier l'équation",
                        onNavigateBack = onNavigateBack
                )
            }
    ) { padding: PaddingValues ->
        Box(modifier = modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(16.dp)
                                        .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Nom de l'équation
                    OutlinedTextField(
                            value = currentEquation.name,
                            onValueChange = { newValue -> viewModel.updateName(newValue) },
                            label = { Text("Nom") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )

                    // Description
                    OutlinedTextField(
                            value = currentEquation.description,
                            onValueChange = { newValue -> viewModel.updateDescription(newValue) },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                    )

                    // Type d'équation
                    Box {
                        OutlinedButton(
                                onClick = { expandedKind = true },
                                modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Calculate, contentDescription = null)
                                Text(currentEquation.kind.name)
                            }
                        }
                        DropdownMenu(
                                expanded = expandedKind,
                                onDismissRequest = { expandedKind = false }
                        ) {
                            EquationKind.values().forEach { kind ->
                                DropdownMenuItem(
                                        onClick = {
                                            viewModel.updateKind(kind)
                                            expandedKind = false
                                        }
                                ) { Text(kind.name) }
                            }
                        }
                    }

                    // Espèce
                    Box {
                        OutlinedButton(
                                onClick = { expandedSpecie = true },
                                modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Pets, contentDescription = null)
                                Text(currentEquation.specie?.name ?: "Sélectionner une espèce")
                            }
                        }
                        DropdownMenu(
                                expanded = expandedSpecie,
                                onDismissRequest = { expandedSpecie = false }
                        ) {
                            Espece.values().forEach { specie ->
                                DropdownMenuItem(
                                        onClick = {
                                            viewModel.updateSpecie(specie)
                                            expandedSpecie = false
                                        }
                                ) { Text(specie.name) }
                            }
                        }
                    }

                    // Script de l'équation
                    OutlinedTextField(
                            value = currentEquation.equationScript,
                            onValueChange = { newValue ->
                                viewModel.updateEquationScript(newValue)
                            },
                            label = { Text("Script de l'équation") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Code, contentDescription = null) }
                    )

                    // Note bibliographique
                    OutlinedTextField(
                            value = currentEquation.bib.comments,
                            onValueChange = { newValue -> viewModel.updateBibNote(newValue) },
                            label = { Text("Note bibliographique") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.LibraryBooks, contentDescription = null)
                            }
                    )

                    // Référence bibliographique
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                                value = currentEquation.bib.completeRef,
                                onValueChange = {},
                                label = { Text("Référence bibliographique") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = {
                                    Icon(Icons.Default.LibraryBooks, contentDescription = null)
                                },
                                readOnly = true
                        )
                        Button(
                                onClick = { showBiblioRefDialog = true },
                                modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null)
                                Text("Sélectionner")
                            }
                        }
                    }

                    // Bouton de sauvegarde
                    Button(
                            onClick = {
                                scope.launch {
                                    println(
                                            "DEBUG EquationEditView: Tentative de sauvegarde de l'équation"
                                    )
                                    val result = viewModel.saveCurrentEquation()
                                    println(
                                            "DEBUG EquationEditView: Résultat de la sauvegarde: $result, Message: ${viewModel.operationMessage.value}"
                                    )
                                    // Ne pas naviguer en arrière en cas d'erreur
                                    // L'erreur sera affichée dans l'AlertDialog
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                    ) {
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Text("Sauvegarder")
                        }
                    }
                }
            }
        }
    }

    // Dialog pour sélectionner une référence bibliographique
    if (showBiblioRefDialog) {
        AlertDialog(
                onDismissRequest = { showBiblioRefDialog = false },
                title = { Text("Sélectionner une référence") },
                text = {
                    Column {
                        OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Rechercher") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                }
                        )
                        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                            items(filteredBiblioRefs) { ref ->
                                Row(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .clickable {
                                                            viewModel.selectBiblioRef(ref)
                                                            showBiblioRefDialog = false
                                                        }
                                                        .padding(
                                                                vertical = 8.dp,
                                                                horizontal = 16.dp
                                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                                text = ref.completeRef,
                                                style = MaterialTheme.typography.body1
                                        )
                                        Text(
                                                text = ref.firstAuthor,
                                                style = MaterialTheme.typography.body2,
                                                color =
                                                        MaterialTheme.colors.onSurface.copy(
                                                                alpha = 0.6f
                                                        )
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBiblioRefDialog = false }) { Text("Annuler") }
                }
        )
    }

    // Alertes d'erreur uniquement (suppression de l'alerte de succès)
    if (showErrorAlert) {
        AlertDialog(
                onDismissRequest = {
                    showErrorAlert = false
                    viewModel.clearOperationMessage()
                },
                title = { Text("Erreur") },
                text = { Text(operationMessage) },
                confirmButton = {
                    TextButton(
                            onClick = {
                                showErrorAlert = false
                                viewModel.clearOperationMessage()
                            }
                    ) { Text("OK") }
                }
        )
    }
}
