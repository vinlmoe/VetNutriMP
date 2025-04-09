package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.StadePhysio
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.ReferenceEvViewModel
import kotlinx.coroutines.launch

/**
 * Vue d'édition d'une référence évaluée (ReferenceEv). Permet de créer une nouvelle référence ou de
 * modifier une référence existante.
 */
@Composable
fun ReferenceEvEditView(
        viewModel: ReferenceEvViewModel,
        referenceEvId: String?,
        onNavigateBack: () -> Unit,
        onEditNutrients: () -> Unit = {},
        modifier: Modifier = Modifier
) {
    val currentReferenceEv by viewModel.currentReferenceEv.collectAsState(initial = ReferenceEv())
    val nom by viewModel.nom.collectAsState(initial = "")
    val description by viewModel.description.collectAsState(initial = "")
    val nomEnergie by viewModel.nomEnergie.collectAsState(initial = "")
    val espece by viewModel.espece.collectAsState(initial = Espece.CHIEN)
    val stadePhysio by viewModel.stadePhysio.collectAsState(initial = StadePhysio.ADULTE)
    val isMaladie by viewModel.isMaladie.collectAsState(initial = false)
    val nomMaladie by viewModel.nomMaladie.collectAsState(initial = "")
    val isValid by viewModel.isValid.collectAsState(initial = false)
    val operationMessage by viewModel.operationMessage.collectAsState(initial = "")
    val actionInProgress by viewModel.actionInProgress.collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()

    val scrollState = rememberScrollState()

    // Si un ID est fourni, charger la référence
    LaunchedEffect(referenceEvId) {
        viewModel.initForEdit()

        if (referenceEvId != null && referenceEvId.isNotBlank()) {
            viewModel.loadReferenceEvById(referenceEvId)
        }
    }

    // Mode d'édition ou de création
    val isEditMode = remember(currentReferenceEv) { currentReferenceEv.uuid.isNotBlank() }

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
                Card(modifier = Modifier.fillMaxWidth(), elevation = AppSizes.cardElevationNormal) {
                    Column(
                            modifier = Modifier.padding(AppSizes.paddingMedium),
                            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                    ) {
                        Text(text = "Informations générales", style = MaterialTheme.typography.h6)

                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                        // Champ pour le nom
                        OutlinedTextField(
                                value = nom,
                                onValueChange = { viewModel.updateNom(it) },
                                label = { Text("Nom*") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors =
                                        TextFieldDefaults.outlinedTextFieldColors(
                                                focusedBorderColor = VetNutriColors.Primary,
                                                unfocusedBorderColor = Color.Gray
                                        )
                        )

                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                        // Sélection de l'espèce
                        Text("Espèce*", style = MaterialTheme.typography.body1)
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                            Espece.values().forEach { especeOption ->
                                OutlinedButton(
                                        onClick = { viewModel.updateEspece(especeOption) },
                                        modifier = Modifier.weight(1f),
                                        colors =
                                                ButtonDefaults.outlinedButtonColors(
                                                        backgroundColor =
                                                                if (espece == especeOption)
                                                                        VetNutriColors.Primary.copy(
                                                                                alpha = 0.1f
                                                                        )
                                                                else Color.Transparent,
                                                        contentColor =
                                                                if (espece == especeOption)
                                                                        VetNutriColors.Primary
                                                                else Color.Gray
                                                )
                                ) { Text(especeOption.toString()) }
                            }
                        }

                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                        // Sélection du stade physiologique
                        Text("Stade physiologique*", style = MaterialTheme.typography.body1)
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                            StadePhysio.values().forEach { stadeOption ->
                                OutlinedButton(
                                        onClick = { viewModel.updateStadePhysio(stadeOption) },
                                        modifier = Modifier.weight(1f),
                                        colors =
                                                ButtonDefaults.outlinedButtonColors(
                                                        backgroundColor =
                                                                if (stadePhysio == stadeOption)
                                                                        VetNutriColors.Primary.copy(
                                                                                alpha = 0.1f
                                                                        )
                                                                else Color.Transparent,
                                                        contentColor =
                                                                if (stadePhysio == stadeOption)
                                                                        VetNutriColors.Primary
                                                                else Color.Gray
                                                )
                                ) { Text(stadeOption.toString()) }
                            }
                        }

                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                        // Nom de l'énergie
                        OutlinedTextField(
                                value = nomEnergie,
                                onValueChange = { viewModel.updateNomEnergie(it) },
                                label = { Text("Nom de l'énergie") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors =
                                        TextFieldDefaults.outlinedTextFieldColors(
                                                focusedBorderColor = VetNutriColors.Primary,
                                                unfocusedBorderColor = Color.Gray
                                        )
                        )

                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                        // Champ pour la description
                        OutlinedTextField(
                                value = description,
                                onValueChange = { viewModel.updateDescription(it) },
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                maxLines = 5,
                                colors =
                                        TextFieldDefaults.outlinedTextFieldColors(
                                                focusedBorderColor = VetNutriColors.Primary,
                                                unfocusedBorderColor = Color.Gray
                                        )
                        )

                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                        // Option pour indiquer si c'est une maladie
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                    checked = isMaladie,
                                    onCheckedChange = { viewModel.updateIsMaladie(it) },
                                    colors =
                                            CheckboxDefaults.colors(
                                                    checkedColor = VetNutriColors.Primary
                                            )
                            )
                            Text(
                                    "Référence pour une maladie",
                                    style = MaterialTheme.typography.body1
                            )
                        }

                        // Si c'est une maladie, afficher le champ pour le nom de la maladie
                        if (isMaladie) {
                            OutlinedTextField(
                                    value = nomMaladie,
                                    onValueChange = { viewModel.updateNomMaladie(it) },
                                    label = { Text("Nom de la maladie*") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors =
                                            TextFieldDefaults.outlinedTextFieldColors(
                                                    focusedBorderColor = VetNutriColors.Primary,
                                                    unfocusedBorderColor = Color.Gray
                                            )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSizes.paddingLarge))

                // Boutons d'action
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(
                            onClick = { viewModel.initForEdit() },
                            modifier = Modifier.padding(end = AppSizes.paddingMedium)
                    ) { Text("Réinitialiser") }

                    Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.saveReferenceEv()
                                    // Attendre un peu avant de naviguer en arrière
                                    kotlinx.coroutines.delay(500)
                                    onNavigateBack()
                                }
                            },
                            enabled = isValid && !actionInProgress
                    ) { Text(if (isEditMode) "Mettre à jour" else "Ajouter") }

                    // Bouton pour accéder aux besoins nutritionnels (uniquement en mode édition)
                    if (isEditMode) {
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = { onEditNutrients() }) {
                            Text("Éditer les besoins nutritionnels")
                        }
                    }
                }
            }

            // Indicateur de chargement
            if (actionInProgress) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VetNutriColors.Primary)
                }
            }

            // Message d'opération
            if (operationMessage.isNotBlank()) {
                Snackbar(
                        modifier =
                                Modifier.align(Alignment.BottomCenter)
                                        .padding(AppSizes.paddingMedium),
                        action = {
                            TextButton(onClick = { viewModel.clearOperationMessage() }) {
                                Text("OK")
                            }
                        }
                ) { Text(operationMessage) }
            }
        }
    }
}
