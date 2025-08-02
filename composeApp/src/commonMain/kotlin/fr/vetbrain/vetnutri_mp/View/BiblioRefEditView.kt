package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.BiblioRefViewModel
import kotlinx.coroutines.launch

/**
 * Vue d'édition d'une référence bibliographique. Permet de créer une nouvelle référence ou de
 * modifier une référence existante.
 */
@Composable
fun BiblioRefEditView(
        viewModel: BiblioRefViewModel,
        biblioRefId: String?,
        onNavigateBack: () -> Unit,
        modifier: Modifier = Modifier
) {
        val currentBiblioRef by viewModel.currentBiblioRef.collectAsState()
        val firstAuthor by viewModel.firstAuthor
        val year by viewModel.year
        val completeRef by viewModel.completeRef
        val comments by viewModel.comments
        val isValid by viewModel.isValid
        val operationMessage by viewModel.operationMessage.collectAsState()
        val actionInProgress by viewModel.actionInProgress.collectAsState()
        val coroutineScope = rememberCoroutineScope()

        val scrollState = rememberScrollState()

        // Si un ID est fourni, charger la référence
        LaunchedEffect(biblioRefId) {
                viewModel.initForEdit()

                if (biblioRefId != null && biblioRefId.isNotBlank()) {
                        viewModel.loadBiblioRefById(biblioRefId)
                } else {
                }
        }

        // Mode d'édition ou de création
        val isEditMode =
                remember(currentBiblioRef) {
                        currentBiblioRef.uuid.isNotBlank() && currentBiblioRef != BiblioRef.EMPTY
                }

        Scaffold(
                topBar = {
                        TopBarSimple(
                                title =
                                        if (isEditMode) "Modifier une référence"
                                        else "Ajouter une référence",
                                onNavigateBack = onNavigateBack
                        )
                }
        ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                        Column(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .padding(AppSizes.paddingMedium)
                                                .verticalScroll(scrollState)
                        ) {
                                Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        elevation = AppSizes.cardElevationNormal
                                ) {
                                        Column(
                                                modifier = Modifier.padding(AppSizes.paddingMedium),
                                                verticalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall)
                                        ) {
                                                Text(
                                                        text = "Informations de la référence",
                                                        style = MaterialTheme.typography.h6
                                                )

                                                Spacer(
                                                        modifier =
                                                                Modifier.height(
                                                                        AppSizes.paddingSmall
                                                                )
                                                )

                                                // Champ pour le premier auteur
                                                OutlinedTextField(
                                                        value = firstAuthor,
                                                        onValueChange = {
                                                                viewModel.updateFirstAuthor(it)
                                                        },
                                                        label = { Text("Premier auteur*") },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        singleLine = true,
                                                        colors =
                                                                TextFieldDefaults
                                                                        .outlinedTextFieldColors(
                                                                                focusedBorderColor =
                                                                                        VetNutriColors
                                                                                                .Primary,
                                                                                unfocusedBorderColor =
                                                                                        Color.Gray
                                                                        )
                                                )

                                                Spacer(
                                                        modifier =
                                                                Modifier.height(
                                                                        AppSizes.paddingSmall
                                                                )
                                                )

                                                // Champ pour l'année
                                                OutlinedTextField(
                                                        value = year,
                                                        onValueChange = {
                                                                viewModel.updateYear(it)
                                                        },
                                                        label = { Text("Année*") },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        keyboardOptions =
                                                                KeyboardOptions(
                                                                        keyboardType =
                                                                                KeyboardType.Number
                                                                ),
                                                        singleLine = true,
                                                        colors =
                                                                TextFieldDefaults
                                                                        .outlinedTextFieldColors(
                                                                                focusedBorderColor =
                                                                                        VetNutriColors
                                                                                                .Primary,
                                                                                unfocusedBorderColor =
                                                                                        Color.Gray
                                                                        )
                                                )

                                                Spacer(
                                                        modifier =
                                                                Modifier.height(
                                                                        AppSizes.paddingSmall
                                                                )
                                                )

                                                // Champ pour la référence complète
                                                OutlinedTextField(
                                                        value = completeRef,
                                                        onValueChange = {
                                                                viewModel.updateCompleteRef(it)
                                                        },
                                                        label = { Text("Référence complète*") },
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .height(120.dp),
                                                        maxLines = 5,
                                                        colors =
                                                                TextFieldDefaults
                                                                        .outlinedTextFieldColors(
                                                                                focusedBorderColor =
                                                                                        VetNutriColors
                                                                                                .Primary,
                                                                                unfocusedBorderColor =
                                                                                        Color.Gray
                                                                        )
                                                )

                                                Spacer(
                                                        modifier =
                                                                Modifier.height(
                                                                        AppSizes.paddingSmall
                                                                )
                                                )

                                                // Champ pour les commentaires
                                                OutlinedTextField(
                                                        value = comments,
                                                        onValueChange = {
                                                                viewModel.updateComments(it)
                                                        },
                                                        label = { Text("Commentaires") },
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .height(120.dp),
                                                        maxLines = 5,
                                                        colors =
                                                                TextFieldDefaults
                                                                        .outlinedTextFieldColors(
                                                                                focusedBorderColor =
                                                                                        VetNutriColors
                                                                                                .Primary,
                                                                                unfocusedBorderColor =
                                                                                        Color.Gray
                                                                        )
                                                )
                                        }
                                }

                                Spacer(modifier = Modifier.height(AppSizes.paddingLarge))

                                // Boutons d'action
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                ) {
                                        OutlinedButton(
                                                onClick = { viewModel.initForEdit() },
                                                modifier =
                                                        Modifier.padding(
                                                                end = AppSizes.paddingMedium
                                                        )
                                        ) { Text("Réinitialiser") }

                                        Button(
                                                onClick = {
                                                        coroutineScope.launch {
                                                                viewModel.saveBiblioRef()
                                                                // Attendre un peu avant de naviguer
                                                                // en arrière
                                                                kotlinx.coroutines.delay(500)
                                                                onNavigateBack()
                                                        }
                                                },
                                                enabled = isValid && !actionInProgress
                                        ) { Text(if (isEditMode) "Mettre à jour" else "Ajouter") }
                                }
                        }

                        // Indicateur de chargement
                        if (actionInProgress) {
                                CircularProgressIndicator(
                                        modifier = Modifier.size(50.dp).align(Alignment.Center),
                                        color = VetNutriColors.Primary
                                )
                        }

                        // Message d'opération
                        operationMessage?.let { message ->
                                AlertDialog(
                                        onDismissRequest = { viewModel.clearOperationMessage() },
                                        title = { Text("Information") },
                                        text = { Text(message) },
                                        confirmButton = {
                                                Button(
                                                        onClick = {
                                                                viewModel.clearOperationMessage()
                                                        }
                                                ) { Text("OK") }
                                        }
                                )
                        }
                }
        }
}
