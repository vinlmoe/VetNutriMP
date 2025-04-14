package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.DismissableBadge
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.EquationType
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.EquationViewModel
import kotlinx.coroutines.delay

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
    // État du chargement
    val isLoading by viewModel.isLoading.collectAsState(initial = false)

    // Équation en cours
    val currentEquation by viewModel.currentEquation.collectAsState()

    // Message d'opération (succès/erreur)
    val operationMessage by viewModel.operationMessage.collectAsState()

    // État de succès de sauvegarde
    val saveSuccessful by viewModel.saveSuccessful.collectAsState()

    // État pour afficher l'alerte de succès
    var showSuccessAlert by remember { mutableStateOf(false) }

    // État pour afficher l'alerte d'erreur
    var showErrorAlert by remember { mutableStateOf(false) }

    // Effet pour charger l'équation à l'initialisation si un ID est fourni
    LaunchedEffect(equationId) {
        println("DEBUG EquationEditView: Initialisation avec equationId=$equationId")
        viewModel.clearOperationMessage()

        if (equationId?.isEmpty() == true || equationId == null) {
            viewModel.createNewEquation()
        } else {
            viewModel.loadEquation(equationId)
        }
    }

    // Effet pour surveiller les messages d'opération
    LaunchedEffect(operationMessage, saveSuccessful) {
        val message = operationMessage
        println(
                "DEBUG EquationEditView: Nouveau message: $message, saveSuccessful: $saveSuccessful"
        )

        if (message != null) {
            if (saveSuccessful) {
                showSuccessAlert = true
                // Attendre que l'alerte soit affichée avant de naviguer
                delay(1500)
                onNavigateBack()
            } else if (message.isNotEmpty()) {
                showErrorAlert = true
            }
        }
    }

    // Titre dynamique basé sur l'opération (création ou édition)
    val title = if (equationId == null) "Nouvelle équation" else "Modifier l'équation"

    Scaffold(topBar = { TopBarSimple(title = title, onNavigateBack = onNavigateBack) }) {
            paddingValues ->
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

                // Facteur de correction (si besoin)
                OutlinedTextField(
                        value = currentEquation.correctionFactor.toString(),
                        onValueChange = {
                            viewModel.updateCorrectionFactor(it.toDoubleOrNull() ?: 1.0)
                        },
                        label = { Text("Facteur de correction") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Script de l'équation
                OutlinedTextField(
                        value = currentEquation.equationScript,
                        onValueChange = { newValue -> viewModel.updateEquationScript(newValue) },
                        label = { Text("Script de l'équation") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Code, contentDescription = null) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sélecteur de variables supplémentaires
                var expandedVariables by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(
                            onClick = { expandedVariables = true },
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null)
                            Text("Insérer une variable")
                        }
                    }
                    DropdownMenu(
                            expanded = expandedVariables,
                            onDismissRequest = { expandedVariables = false }
                    ) {
                        VariableKind.values().forEach { variableKind ->
                            DropdownMenuItem(
                                    onClick = {
                                        // Ajouter à la liste de variables de l'équation
                                        viewModel.addVariable(variableKind)
                                        expandedVariables = false
                                    }
                            ) { Text("${variableKind.variable} - ${variableKind.label}") }
                        }
                    }
                }

                // Affichage des variables sélectionnées
                if (currentEquation.variables.isNotEmpty()) {
                    Card(
                            elevation = 4.dp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                    "Variables disponibles",
                                    style = MaterialTheme.typography.subtitle1,
                                    color = VetNutriColors.Primary
                            )

                            // Utilisation de Row au lieu de LazyRow
                            Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                currentEquation.variables.forEach { variable ->
                                    DismissableBadge(
                                            text = variable.variable,
                                            subText = variable.label,
                                            id = variable.uuid.toString(),
                                            backgroundColor = VetNutriColors.Secondary,
                                            onDismiss = { viewModel.removeVariable(variable) }
                                    )
                                }
                            }

                            Button(
                                    onClick = {
                                        // Insérer toutes les variables sélectionnées dans le script
                                        val currentScript = currentEquation.equationScript
                                        val variablesText =
                                                currentEquation.variables.joinToString(" ") {
                                                    it.variable
                                                }
                                        viewModel.updateEquationScript(
                                                "$currentScript $variablesText"
                                        )
                                        // Effacer la liste après insertion
                                        viewModel.clearSelectedVariables()
                                    },
                                    modifier = Modifier.align(Alignment.End)
                            ) { Text("Insérer dans l'équation") }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Note bibliographique
                OutlinedTextField(
                        value = currentEquation.bib.comments,
                        onValueChange = { viewModel.updateBibNote(it) },
                        label = { Text("Note bibliographique") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Référence bibliographique (sélecteur au lieu de champ libre)
                val biblioRefs by viewModel.biblioRefs.collectAsState()
                var expandedBiblioRefs by remember { mutableStateOf(false) }

                Box {
                    OutlinedTextField(
                            value = currentEquation.bib.completeRef,
                            onValueChange = { /* Lecture seule, modification via le sélecteur uniquement */
                            },
                            label = { Text("Référence bibliographique") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { expandedBiblioRefs = true }) {
                                    Icon(
                                            imageVector = Icons.Default.AddCircle,
                                            contentDescription = "Sélectionner une référence"
                                    )
                                }
                            }
                    )

                    DropdownMenu(
                            expanded = expandedBiblioRefs,
                            onDismissRequest = { expandedBiblioRefs = false }
                    ) {
                        if (biblioRefs.isEmpty()) {
                            DropdownMenuItem(onClick = { expandedBiblioRefs = false }) {
                                Text("Aucune référence disponible")
                            }
                        } else {
                            biblioRefs.forEach { biblioRef ->
                                DropdownMenuItem(
                                        onClick = {
                                            viewModel.selectBiblioRef(biblioRef)
                                            expandedBiblioRefs = false
                                        }
                                ) {
                                    Text(
                                            "${biblioRef.firstAuthor} (${biblioRef.year}) - ${biblioRef.completeRef.take(30)}${if (biblioRef.completeRef.length > 30) "..." else ""}"
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Bouton d'enregistrement
                Button(
                        onClick = {
                            println("DEBUG EquationEditView: Bouton de sauvegarde cliqué")
                            viewModel.saveCurrentEquation()
                            // Ne pas naviguer ici - la navigation se fera via LaunchedEffect si
                            // succès
                        },
                        modifier = Modifier.padding(8.dp)
                ) { Text("Enregistrer") }
            }

            // Indicateur de chargement
            if (isLoading) {
                CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = VetNutriColors.Primary
                )
            }

            // Alerte de succès
            if (showSuccessAlert) {
                AlertDialog(
                        onDismissRequest = {
                            showSuccessAlert = false
                            viewModel.clearOperationMessage()
                        },
                        title = { Text("Succès") },
                        text = {
                            val message = operationMessage
                            Text(message ?: "")
                        },
                        confirmButton = {
                            Button(
                                    onClick = {
                                        showSuccessAlert = false
                                        viewModel.clearOperationMessage()
                                    }
                            ) { Text("OK") }
                        }
                )
            }

            // Alerte d'erreur
            if (showErrorAlert) {
                AlertDialog(
                        onDismissRequest = {
                            showErrorAlert = false
                            viewModel.clearOperationMessage()
                        },
                        title = { Text("Erreur") },
                        text = {
                            val message = operationMessage
                            Text(message ?: "")
                        },
                        confirmButton = {
                            Button(
                                    onClick = {
                                        showErrorAlert = false
                                        viewModel.clearOperationMessage()
                                    }
                            ) { Text("OK") }
                        }
                )
            }

            // Afficher l'erreur s'il y en a une
            val message = operationMessage
            if (message != null && message.isNotEmpty() && message.startsWith("Erreur")) {
                Snackbar(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        action = {
                            TextButton(onClick = { viewModel.clearOperationMessage() }) {
                                Text("Fermer")
                            }
                        }
                ) { Text(message) }
            }
        }
    }
}
