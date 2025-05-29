package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.AppDatePicker
import fr.vetbrain.vetnutri_mp.Components.AppTextField
import fr.vetbrain.vetnutri_mp.Components.NumberTextField
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Consultation
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalUuidApi::class)
@Composable
fun ConsultationFullScreenEditView(
        consultation: ConsultationEv?,
        animalName: String = "",
        availableReferences: List<fr.vetbrain.vetnutri_mp.Data.ReferenceEv> = emptyList(),
        onBackPressed: (ConsultationEv) -> Unit,
        onLoadReferences: () -> Unit = {}
) {
    var editedConsultation by
            remember(consultation) { mutableStateOf(consultation ?: ConsultationEv()) }
    var weightText by
            remember(consultation) { mutableStateOf(consultation?.weight?.toString() ?: "") }
    var showDateError by remember(consultation) { mutableStateOf(false) }
    var showWeightError by remember(consultation) { mutableStateOf(false) }
    var dateErrorMessage by remember(consultation) { mutableStateOf<String?>(null) }
    var weightErrorMessage by remember(consultation) { mutableStateOf<String?>(null) }

    // États pour les dialogues de sélection de références
    var showReferenceGeneraleDialog by remember(consultation) { mutableStateOf(false) }
    var showReferenceMaladieDialog by remember(consultation) { mutableStateOf(false) }

    // Charger les références au démarrage
    LaunchedEffect(Unit) { onLoadReferences() }

    // Fonction pour sauvegarder et retourner
    val saveAndGoBack = {
        if (!showDateError && !showWeightError && editedConsultation.date != null) {
            // S'assurer que l'UUID est généré si c'est une nouvelle consultation
            if (editedConsultation.uuid.isEmpty()) {
                editedConsultation =
                        editedConsultation.copy(uuid = kotlin.uuid.Uuid.random().toString())
            }
            onBackPressed(editedConsultation)
        } else if (editedConsultation.date == null) {
            showDateError = true
            dateErrorMessage = "La date est obligatoire"
        }
    }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = {
                            Column {
                                Text(
                                        text =
                                                if (consultation == null ||
                                                                consultation.uuid.isEmpty()
                                                )
                                                        "Nouvelle consultation"
                                                else "Modifier consultation",
                                        style = MaterialTheme.typography.h6,
                                        color = VetNutriColors.OnPrimary
                                )
                                if (animalName.isNotEmpty()) {
                                    Text(
                                            text = animalName,
                                            style = MaterialTheme.typography.subtitle2,
                                            color = VetNutriColors.OnPrimary.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { saveAndGoBack() }) {
                                Icon(
                                        AppIcons.ArrowBack,
                                        contentDescription = "Retour et sauvegarde",
                                        tint = VetNutriColors.OnPrimary
                                )
                            }
                        },
                        actions = {
                            // Bouton de sauvegarde explicite
                            TextButton(
                                    onClick = { saveAndGoBack() },
                                    enabled = !showDateError && !showWeightError
                            ) {
                                Text(
                                        text = General.SAVE.translate(),
                                        color = VetNutriColors.OnPrimary
                                )
                            }
                        },
                        backgroundColor = VetNutriColors.Primary,
                        contentColor = VetNutriColors.OnPrimary
                )
            }
    ) { paddingValues ->
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(paddingValues)
                                .verticalScroll(rememberScrollState())
                                .padding(AppSizes.paddingLarge),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
            // Date
            AppDatePicker(
                    selectedDate = editedConsultation.date,
                    onDateSelected = { date: LocalDate ->
                        editedConsultation = editedConsultation.copy(date = date)
                        showDateError = false
                        dateErrorMessage = null
                    },
                    label = Consultation.DATE.translate(),
                    isError = showDateError,
                    errorMessage = dateErrorMessage,
                    modifier = Modifier.fillMaxWidth()
            )

            // Poids
            NumberTextField(
                    value = weightText,
                    onValueChange = { newValue: String ->
                        weightText = newValue
                        try {
                            if (newValue.isNotEmpty()) {
                                val weight = newValue.toFloat()
                                editedConsultation = editedConsultation.copy(weight = weight)
                                showWeightError = false
                                weightErrorMessage = null
                            } else {
                                editedConsultation = editedConsultation.copy(weight = null)
                                showWeightError = false
                                weightErrorMessage = null
                            }
                        } catch (e: Exception) {
                            showWeightError = true
                            weightErrorMessage = "Format de poids invalide (nombre décimal)"
                        }
                    },
                    label = Animal.WEIGHT.translate(),
                    leadingIcon = AppIcons.Weight,
                    isError = showWeightError,
                    errorMessage = weightErrorMessage,
                    modifier = Modifier.fillMaxWidth()
            )

            // Objectif
            AppTextField(
                    value = editedConsultation.objectConsult,
                    onValueChange = { newValue: String ->
                        editedConsultation = editedConsultation.copy(objectConsult = newValue)
                    },
                    label = Consultation.OBJECTIVE.translate(),
                    leadingIcon = AppIcons.Info,
                    modifier = Modifier.fillMaxWidth()
            )

            // Observations
            AppTextField(
                    value = editedConsultation.observation,
                    onValueChange = { newValue: String ->
                        editedConsultation = editedConsultation.copy(observation = newValue)
                    },
                    label = Consultation.OBSERVATION.translate(),
                    leadingIcon = AppIcons.Info,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    maxLines = 6,
                    singleLine = false
            )

            // Section Références Nutritionnelles
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = AppSizes.elevationSmall,
                    backgroundColor = VetNutriColors.Surface
            ) {
                Column(
                        modifier = Modifier.padding(AppSizes.paddingLarge),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                    Text(
                            text = "Références nutritionnelles",
                            style = MaterialTheme.typography.h6,
                            color = VetNutriColors.Primary
                    )

                    Divider(color = VetNutriColors.Primary.copy(alpha = 0.3f))

                    // Référence générale
                    Text(
                            text = "Référence générale",
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                            value = editedConsultation.referenceGeneraleId
                                            ?: "Aucune référence sélectionnée",
                            onValueChange = {},
                            label = { Text("Référence générale") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { showReferenceGeneraleDialog = true }) {
                                    Icon(
                                            AppIcons.ArrowDropDown,
                                            contentDescription = "Sélectionner une référence"
                                    )
                                }
                            }
                    )

                    Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

                    // Références liées aux maladies
                    Text(
                            text = "Références liées aux maladies",
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold
                    )

                    // Liste des références de maladies
                    if (editedConsultation.referencesMaladies.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)) {
                            editedConsultation.referencesMaladies.forEach { referenceId ->
                                Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        backgroundColor = VetNutriColors.Surface.copy(alpha = 0.7f),
                                        elevation = 2.dp
                                ) {
                                    Row(
                                            modifier = Modifier.padding(AppSizes.paddingMedium),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                                text = "Référence: $referenceId",
                                                style = MaterialTheme.typography.body1,
                                                modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                                onClick = {
                                                    editedConsultation.supprimerReferenceMaladie(
                                                            referenceId
                                                    )
                                                }
                                        ) {
                                            Icon(
                                                    AppIcons.Delete,
                                                    contentDescription = "Supprimer la référence",
                                                    tint = Color.Red
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bouton pour ajouter une référence de maladie
                    OutlinedButton(
                            onClick = { showReferenceMaladieDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                    ButtonDefaults.outlinedButtonColors(
                                            contentColor = VetNutriColors.Primary
                                    )
                    ) {
                        Icon(
                                AppIcons.Add,
                                contentDescription = "Ajouter une référence de maladie",
                                modifier = Modifier.size(AppSizes.iconSizeSmall)
                        )
                        Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                        Text("Ajouter une référence de maladie")
                    }
                }
            }

            // Section Rations
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = AppSizes.elevationSmall,
                    backgroundColor = VetNutriColors.Surface
            ) {
                Column(
                        modifier = Modifier.padding(AppSizes.paddingLarge),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                    Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                                text = "Rations",
                                style = MaterialTheme.typography.h6,
                                color = VetNutriColors.Primary
                        )

                        OutlinedButton(
                                onClick = {
                                    // TODO: Ajouter une nouvelle ration
                                },
                                colors =
                                        ButtonDefaults.outlinedButtonColors(
                                                contentColor = VetNutriColors.Primary
                                        )
                        ) {
                            Icon(
                                    AppIcons.Add,
                                    contentDescription = "Ajouter une ration",
                                    modifier = Modifier.size(AppSizes.iconSizeSmall)
                            )
                            Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                            Text("Ajouter ration")
                        }
                    }

                    Divider(color = VetNutriColors.Primary.copy(alpha = 0.3f))

                    if (editedConsultation.rations.isEmpty()) {
                        Text(
                                text = "Aucune ration ajoutée",
                                style = MaterialTheme.typography.body1,
                                color = Color.Gray,
                                modifier = Modifier.padding(AppSizes.paddingMedium)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)) {
                            editedConsultation.rations.forEach { ration ->
                                RationCard(
                                        ration = ration,
                                        onEdit = { /* TODO: Implémenter l'édition de ration */},
                                        onDelete = { editedConsultation.rations.remove(ration) }
                                )
                            }
                        }
                    }
                }
            }

            // Espacement final
            Spacer(modifier = Modifier.height(AppSizes.paddingLarge))
        }
    }

    if (showReferenceGeneraleDialog) {
        ReferenceGeneraleDialog(
                value = editedConsultation.referenceGeneraleId ?: "",
                onValueChange = { newValue: String ->
                    editedConsultation = editedConsultation.copy(referenceGeneraleId = newValue)
                    showReferenceGeneraleDialog = false
                },
                availableReferences = availableReferences
        )
    }

    if (showReferenceMaladieDialog) {
        ReferenceMaladieDialog(
                references = editedConsultation.referencesMaladies,
                onReferenceSelected = { referenceId ->
                    editedConsultation =
                            editedConsultation.copy(
                                    referencesMaladies = referenceId.toMutableList()
                            )
                    showReferenceMaladieDialog = false
                },
                onReferenceRemoved = { referenceId ->
                    editedConsultation =
                            editedConsultation.copy(
                                    referencesMaladies = referenceId.toMutableList()
                            )
                    showReferenceMaladieDialog = false
                }
        )
    }
}

@Composable
private fun RationCard(
        ration: Ration,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
        modifier: Modifier = Modifier
) {
    Card(
            modifier = modifier.fillMaxWidth(),
            elevation = AppSizes.elevationSmall,
            backgroundColor = MaterialTheme.colors.surface,
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(AppSizes.borderWidth, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(
                modifier = Modifier.padding(AppSizes.paddingMedium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                        AppIcons.Ration,
                        contentDescription = null,
                        tint = VetNutriColors.Primary,
                        modifier = Modifier.size(AppSizes.iconSizeMedium)
                )
                Column {
                    Text(text = ration.name, style = MaterialTheme.typography.subtitle1)
                    Text(
                            text = "Coef: ${ration.coef}",
                            style = MaterialTheme.typography.body2,
                            color = Color.Gray
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                            imageVector = AppIcons.Edit,
                            contentDescription = "Modifier la ration",
                            tint = VetNutriColors.Primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                            imageVector = AppIcons.Delete,
                            contentDescription = "Supprimer la ration",
                            tint = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
private fun ReferenceGeneraleDialog(
        value: String,
        onValueChange: (String) -> Unit,
        availableReferences: List<fr.vetbrain.vetnutri_mp.Data.ReferenceEv>
) {
    val referencesGenerales = availableReferences.filter { !it.maladie }

    AlertDialog(
            onDismissRequest = { onValueChange(value) },
            title = { Text("Sélectionner une référence générale") },
            text = {
                Column {
                    Text("${referencesGenerales.size} références disponibles")

                    // Option pour aucune référence
                    Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = value.isEmpty(), onClick = { onValueChange("") })
                        Text("Aucune référence")
                    }

                    // Afficher quelques références (limité pour éviter les problèmes de taille)
                    referencesGenerales.take(5).forEach { reference ->
                        Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                    selected = value == reference.uuid,
                                    onClick = { onValueChange(reference.uuid) }
                            )
                            Text(reference.nom.ifBlank { "Référence sans nom" })
                        }
                    }

                    if (referencesGenerales.size > 5) {
                        Text("... et ${referencesGenerales.size - 5} autres références")
                    }
                }
            },
            confirmButton = { TextButton(onClick = { onValueChange(value) }) { Text("Fermer") } }
    )
}

@Composable
private fun ReferenceMaladieDialog(
        references: List<String>,
        onReferenceSelected: (List<String>) -> Unit,
        onReferenceRemoved: (List<String>) -> Unit
) {
    AlertDialog(
            onDismissRequest = {},
            title = { Text("Gérer les références de maladies") },
            text = {
                Column {
                    Text("Cette fonctionnalité sera implémentée prochainement")
                    // TODO: Implémenter la sélection multiple de références de maladies
                }
            },
            confirmButton = {
                TextButton(onClick = { onReferenceSelected(references) }) { Text("Fermer") }
            }
    )
}
