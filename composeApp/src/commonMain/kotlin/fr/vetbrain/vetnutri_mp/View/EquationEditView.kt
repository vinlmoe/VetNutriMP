package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Enumer.EquationType
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
    // État du chargement
    val isLoading by viewModel.isLoading.collectAsState(initial = false)

    // Équation en cours
    val currentEquation by viewModel.currentEquation.collectAsState(initial = Equation())

    // Message d'opération (succès/erreur)
    val operationMessage by viewModel.operationMessage.collectAsState()

    // État pour afficher l'alerte de succès
    var showSuccessAlert by remember { mutableStateOf(false) }

    // Effet pour charger l'équation à l'initialisation si un ID est fourni
    LaunchedEffect(equationId) {
        if (equationId != null) {
            viewModel.loadEquationById(equationId)
        } else {
            viewModel.clearCurrentEquation()
        }
    }

    // Effet pour afficher l'alerte de succès lorsqu'une opération réussit
    LaunchedEffect(operationMessage) {
        if (operationMessage.isNotEmpty() && !operationMessage.startsWith("Erreur")) {
            showSuccessAlert = true
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
                        value = currentEquation?.name ?: "",
                        onValueChange = { viewModel.updateName(it) },
                        label = { Text("Nom de l'équation") },
                        modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                OutlinedTextField(
                        value = currentEquation?.description ?: "",
                        onValueChange = { viewModel.updateDescription(it) },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Type d'équation (dropdown)
                DropdownField(
                        label = "Type d'équation",
                        selectedValue = currentEquation?.kind?.let { kind ->
                            when (kind) {
                                fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERGYNEED -> EquationType.ENERGYNEED
                                fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERGYDENSITY -> EquationType.ENERGYDENSITY
                                fr.vetbrain.vetnutri_mp.Enumer.EquationKind.MW -> EquationType.MW
                                fr.vetbrain.vetnutri_mp.Enumer.EquationKind.INDICATOR -> EquationType.INDICATOR
                                fr.vetbrain.vetnutri_mp.Enumer.EquationKind.NEED -> EquationType.NEED
                            }
                        },
                        options = EquationType.values().toList(),
                        onValueChange = { viewModel.updateKind(it.toEquationKind()) },
                        valueToString = { it.toString() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Facteur de correction (si besoin)
                OutlinedTextField(
                        value = currentEquation?.correctionFactor?.toString() ?: "1.0",
                        onValueChange = {
                            viewModel.updateCorrectionFactor(it.toDoubleOrNull() ?: 1.0)
                        },
                        label = { Text("Facteur de correction") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Note bibliographique
                OutlinedTextField(
                        value = currentEquation?.bibNote ?: "",
                        onValueChange = { viewModel.updateBibNote(it) },
                        label = { Text("Note bibliographique") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Référence bibliographique
                OutlinedTextField(
                        value = currentEquation?.bibRef ?: "",
                        onValueChange = { viewModel.updateBibRef(it) },
                        label = { Text("Référence bibliographique") },
                        modifier = Modifier.fillMaxWidth()
                )

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

            // Alerte de succès
            if (showSuccessAlert) {
                AlertDialog(
                        onDismissRequest = {
                            showSuccessAlert = false
                            viewModel.clearOperationMessage()
                            onNavigateBack()
                        },
                        title = { Text("Succès") },
                        text = { Text(operationMessage) },
                        confirmButton = {
                            Button(
                                    onClick = {
                                        showSuccessAlert = false
                                        viewModel.clearOperationMessage()
                                        onNavigateBack()
                                    }
                            ) { Text("OK") }
                        }
                )
            }

            // Afficher l'erreur s'il y en a une
            if (operationMessage.isNotEmpty() && operationMessage.startsWith("Erreur")) {
                Snackbar(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        action = {
                            TextButton(onClick = { viewModel.clearOperationMessage() }) {
                                Text("Fermer")
                            }
                        }
                ) { Text(operationMessage) }
            }
        }
    }
}
