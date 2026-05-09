package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Consultation
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import kotlinx.datetime.LocalDate

/**
 * Vue pour afficher la liste des consultations d'un animal
 *
 * @param viewModel ViewModel contenant les données de l'animal
 * @param showConsultationDetail Indique si le détail d'une consultation est affiché
 * @param onShowConsultationDetail Action à exécuter pour afficher/masquer le détail d'une
 * consultation
 * @param modifier Modificateur optionnel pour personnaliser l'apparence
 */
@Composable
fun ConsultationsView(
        viewModel: AnimalDetailViewModel,
        showConsultationDetail: Boolean,
        onShowConsultationDetail: (Boolean) -> Unit,
        modifier: Modifier = Modifier
) {
    val animal by viewModel.animal.collectAsState()
    val selectedConsultation by viewModel.selectedConsultation.collectAsState()
    val isEditingConsultation by remember { derivedStateOf { viewModel.isEditingConsultation } }
    val showFullScreenEdit by viewModel.showFullScreenEdit.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var consultationToDelete by remember { mutableStateOf<ConsultationEv?>(null) }

    // Trier les consultations de la plus récente à la plus ancienne
    val sortedConsultations =
            remember(animal) {
                val defaultDate =
                        LocalDate(2000, 1, 1) // Date par défaut pour les consultations sans date
                animal?.consultations?.sortedByDescending { it.date ?: defaultDate } ?: emptyList()
            }

    // Affichage conditionnel : vue plein écran ou vue normale
    if (showFullScreenEdit) {
        val availableReferences by viewModel.availableReferences.collectAsState()
        val availableKeywords by viewModel.availableKeywords.collectAsState()
        var showNoReferenceDialog by remember { mutableStateOf(false) }

        ConsultationFullScreenEditView(
                consultation = selectedConsultation,
                animalName = animal?.nom ?: "",
                animalEspece = animal?.getEspece(),
                availableReferences = availableReferences,
                availableKeywords = availableKeywords,
                onBackPressed = { consultation -> viewModel.saveFromFullScreen(consultation) },
                onCancel = {
                    // Annuler la création si la consultation venait d'être créée (uuid vide)
                    if (selectedConsultation?.uuid?.isEmpty() == true) {
                        viewModel.stopEditingConsultation()
                    }
                    viewModel.closeFullScreenEdit()
                },
                onLoadReferences = { viewModel.chargerReferencesDisponibles() },
                onLoadKeywords = { viewModel.chargerMotsClesConsultation() },
                onCreateKeyword = { keyword -> viewModel.ajouterMotCleConsultation(keyword) }
        )

        // Dialog uniquement après clic sur Valider: on le pilote ici via la sélection
        if (showNoReferenceDialog) {
            AlertDialog(
                    onDismissRequest = { showNoReferenceDialog = false },
                    title = { Text(translate(Consultation.MISSING_REF_TITLE)) },
                    text = {
                        Text(
                                translate(Consultation.MISSING_REF_MESSAGE)
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showNoReferenceDialog = false }) { Text("OK") }
                    }
            )
        }
    } else {
        // Vue normale avec layout en colonnes
        ConsultationsMainView(
                viewModel = viewModel,
                animal = animal,
                sortedConsultations = sortedConsultations,
                selectedConsultation = selectedConsultation,
                isEditingConsultation = isEditingConsultation,
                showConsultationDetail = showConsultationDetail,
                onShowConsultationDetail = onShowConsultationDetail,
                showDeleteConfirmation = showDeleteConfirmation,
                onShowDeleteConfirmation = { showDeleteConfirmation = it },
                consultationToDelete = consultationToDelete,
                onConsultationToDelete = { consultationToDelete = it },
                modifier = modifier
        )
    }
}

@Composable
private fun ConsultationsMainView(
        viewModel: AnimalDetailViewModel,
        animal: fr.vetbrain.vetnutri_mp.Data.AnimalEv?,
        sortedConsultations: List<ConsultationEv>,
        selectedConsultation: ConsultationEv?,
        isEditingConsultation: Boolean,
        showConsultationDetail: Boolean,
        onShowConsultationDetail: (Boolean) -> Unit,
        showDeleteConfirmation: Boolean,
        onShowDeleteConfirmation: (Boolean) -> Unit,
        consultationToDelete: ConsultationEv?,
        onConsultationToDelete: (ConsultationEv?) -> Unit,
        modifier: Modifier = Modifier
) {
    // Dialogue de confirmation de suppression
    if (showDeleteConfirmation) {
        AlertDialog(
                onDismissRequest = { onShowDeleteConfirmation(false) },
                title = { Text(Consultation.DELETE_CONSULTATION.translate()) },
                text = { Text(Consultation.DELETE_CONSULTATION_CONFIRM.translate()) },
                confirmButton = {
                    Button(
                            onClick = {
                                consultationToDelete?.let { consultation ->
                                    viewModel.deleteConsultation(consultation)
                                }
                                onShowDeleteConfirmation(false)
                            },
                            colors =
                                    ButtonDefaults.buttonColors(
                                            backgroundColor = Color.Red,
                                            contentColor = Color.White
                                    )
                    ) { Text(General.CONFIRM.translate()) }
                },
                dismissButton = {
                    Button(
                            onClick = { onShowDeleteConfirmation(false) },
                            colors =
                                    ButtonDefaults.buttonColors(
                                            backgroundColor = VetNutriColors.Secondary,
                                            contentColor = VetNutriColors.OnSecondary
                                    )
                    ) { Text(General.CANCEL.translate()) }
                }
        )
    }

    Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                        onClick = { viewModel.createNewConsultationFullScreen() },
                        backgroundColor = VetNutriColors.Primary
                ) {
                    Icon(
                            imageVector = AppIcons.Add,
                            contentDescription = translate(Consultation.ADD),
                            tint = VetNutriColors.OnPrimary
                    )
                }
            }
    ) { paddingValues ->
        Row(modifier = modifier.fillMaxSize().padding(paddingValues)) {
            // Liste des consultations
            Column(
                    modifier =
                            Modifier.weight(0.4f).fillMaxHeight().padding(AppSizes.paddingMedium),
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
            ) {
                // FAB remplace le bouton d'ajout; laisser l'espace entête

                Divider(
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                        thickness = AppSizes.dividerHeight
                )

                Text(
                        text = translate(Consultation.TITLE),
                        style = MaterialTheme.typography.h6,
                        color = VetNutriColors.Primary
                )

                animal?.let { animalDetails ->
                    if (animalDetails.consultations.isEmpty()) {
                        Box(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                contentAlignment = Alignment.Center
                        ) {
                            Text(
                                    text = translate(Consultation.NONE),
                                    style = MaterialTheme.typography.body1,
                                    color = Color.Gray
                            )
                        }
                    } else {
                        // Déterminer si la suppression est autorisée (plus d'une consultation)
                        val canDeleteConsultation = animalDetails.consultations.size > 1

                        LazyColumn(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.cardSpacing)
                        ) {
                            items(sortedConsultations) { consultation ->
                                ConsultationCard(
                                        consultation = consultation,
                                        isSelected =
                                                selectedConsultation?.uuid == consultation.uuid,
                                        onEdit = {
                                            viewModel.editConsultationFullScreen(consultation)
                                        },
                                        onDelete = {
                                            onConsultationToDelete(consultation)
                                            onShowDeleteConfirmation(true)
                                        },
                                        onDuplicate = {
                                            viewModel.duplicateConsultation(consultation)
                                        },
                                        isDeleteEnabled = canDeleteConsultation,
                                        onClick = {
                                            viewModel.selectConsultation(consultation)
                                            onShowConsultationDetail(true)
                                        }
                                )
                            }
                        }
                    }
                }
            }

            // Séparateur vertical
            Divider(
                    modifier = Modifier.fillMaxHeight().width(AppSizes.dividerWidth),
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
            )

            // Détail de la consultation
            if (showConsultationDetail) {
                Box(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
                    selectedConsultation?.let { consultation ->
                        val availableReferences by viewModel.availableReferences.collectAsState()

                        // Charger les références au démarrage
                        LaunchedEffect(Unit) { viewModel.chargerReferencesDisponibles() }

                        AppConsultationDetailView(
                                consultation = consultation,
                                availableReferences = availableReferences,
                                onDismiss = {
                                    if (isEditingConsultation && consultation.uuid.isEmpty()) {
                                        // Si on annule l'ajout d'une nouvelle consultation
                                        viewModel.stopEditingConsultation()
                                    }
                                    onShowConsultationDetail(false)
                                },
                                onSave = { updatedConsultation ->
                                    if (isEditingConsultation && consultation.uuid.isEmpty()) {
                                        // Nouvelle consultation
                                        viewModel.addConsultation(updatedConsultation)
                                    } else {
                                        // Mise à jour d'une consultation existante
                                        viewModel.updateConsultation(updatedConsultation)
                                    }
                                    viewModel.stopEditingConsultation()
                                    onShowConsultationDetail(false)
                                },
                                viewModel = viewModel
                        )
                    }
                }
            } else {
                // Message indiquant de sélectionner une consultation
                CenteredMessage(
                        message = translate(Consultation.SELECT_DETAIL_HINT),
                        modifier = Modifier.weight(0.6f).fillMaxHeight()
                )
            }
        }
    }
}
