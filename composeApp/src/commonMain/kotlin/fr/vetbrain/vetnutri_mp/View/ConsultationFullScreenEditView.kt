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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.AppDatePicker
import fr.vetbrain.vetnutri_mp.Components.AppTextField
import fr.vetbrain.vetnutri_mp.Components.NumberTextField
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
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

    // États pour les dialogues de coefficients
    var showK1Dialog by remember { mutableStateOf(false) }
    var showK2Dialog by remember { mutableStateOf(false) }
    var showK3Dialog by remember { mutableStateOf(false) }
    var showK4Dialog by remember { mutableStateOf(false) }
    var showK5Dialog by remember { mutableStateOf(false) }

    // Trouver la référence générale sélectionnée
    val referenceGeneraleSelectionnee =
            remember(editedConsultation.referenceGeneraleId, availableReferences) {
                availableReferences.find { it.uuid == editedConsultation.referenceGeneraleId }
            }

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

            // Section Note d'État Corporel
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
                            text = "Note d'État Corporel (BCS)",
                            style = MaterialTheme.typography.h6,
                            color = VetNutriColors.Primary
                    )

                    Divider(color = VetNutriColors.Primary.copy(alpha = 0.3f))

                    // Sélecteur de BCS
                    ScoreSelector(
                            label = "Note BCS (1-9)",
                            valeurSelectionnee = editedConsultation.BCS,
                            onScoreSelected = { score ->
                                editedConsultation = editedConsultation.copy(BCS = score)
                            },
                            plageScore = 1..9,
                            descriptions =
                                    mapOf(
                                            1 to
                                                    "Très maigre - Côtes, vertèbres et os du bassin très saillants",
                                            2 to
                                                    "Maigre - Côtes facilement palpables, faible couverture graisseuse",
                                            3 to
                                                    "Mince - Côtes palpables avec légère couverture graisseuse",
                                            4 to
                                                    "Idéal inférieur - Côtes facilement palpables, taille visible",
                                            5 to
                                                    "Idéal - Côtes palpables sans excès de graisse, taille bien définie",
                                            6 to
                                                    "Idéal supérieur - Côtes palpables avec légère couverture graisseuse",
                                            7 to
                                                    "Surpoids - Côtes difficiles à palper, dépôts graisseux visibles",
                                            8 to
                                                    "Obèse - Côtes très difficiles à palper, importante couverture graisseuse",
                                            9 to
                                                    "Très obèse - Côtes non palpables, dépôts graisseux massifs"
                                    )
                    )
                }
            }

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

            // Section Coefficients
            if (referenceGeneraleSelectionnee != null) {
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
                                text = "Coefficients d'ajustement énergétique",
                                style = MaterialTheme.typography.h6,
                                color = VetNutriColors.Primary
                        )

                        Divider(color = VetNutriColors.Primary.copy(alpha = 0.3f))

                        // Grille des coefficients K1-K5
                        Column(verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)) {
                            if (referenceGeneraleSelectionnee.nomk1.isNotBlank()) {
                                CoefficientSelector(
                                        nom = referenceGeneraleSelectionnee.nomk1,
                                        valeurSelectionnee = editedConsultation.k1Value,
                                        descriptionSelectionnee = editedConsultation.k1Id,
                                        coefficients = referenceGeneraleSelectionnee.getModk1(),
                                        onCoefficientSelected = { coef ->
                                            editedConsultation =
                                                    editedConsultation.copy(
                                                            k1Id = coef.description,
                                                            k1Value = coef.coef
                                                    )
                                        }
                                )
                            }

                            if (referenceGeneraleSelectionnee.nomk2.isNotBlank()) {
                                CoefficientSelector(
                                        nom = referenceGeneraleSelectionnee.nomk2,
                                        valeurSelectionnee = editedConsultation.k2Value,
                                        descriptionSelectionnee = editedConsultation.k2Id,
                                        coefficients = referenceGeneraleSelectionnee.getModk2(),
                                        onCoefficientSelected = { coef ->
                                            editedConsultation =
                                                    editedConsultation.copy(
                                                            k2Id = coef.description,
                                                            k2Value = coef.coef
                                                    )
                                        }
                                )
                            }

                            if (referenceGeneraleSelectionnee.nomk3.isNotBlank()) {
                                CoefficientSelector(
                                        nom = referenceGeneraleSelectionnee.nomk3,
                                        valeurSelectionnee = editedConsultation.k3Value,
                                        descriptionSelectionnee = editedConsultation.k3Id,
                                        coefficients = referenceGeneraleSelectionnee.getModk3(),
                                        onCoefficientSelected = { coef ->
                                            editedConsultation =
                                                    editedConsultation.copy(
                                                            k3Id = coef.description,
                                                            k3Value = coef.coef
                                                    )
                                        }
                                )
                            }

                            if (referenceGeneraleSelectionnee.nomk4.isNotBlank()) {
                                CoefficientSelector(
                                        nom = referenceGeneraleSelectionnee.nomk4,
                                        valeurSelectionnee = editedConsultation.k4Value,
                                        descriptionSelectionnee = editedConsultation.k4Id,
                                        coefficients = referenceGeneraleSelectionnee.getModk4(),
                                        onCoefficientSelected = { coef ->
                                            editedConsultation =
                                                    editedConsultation.copy(
                                                            k4Id = coef.description,
                                                            k4Value = coef.coef
                                                    )
                                        }
                                )
                            }

                            if (referenceGeneraleSelectionnee.nomk5.isNotBlank()) {
                                CoefficientSelector(
                                        nom = referenceGeneraleSelectionnee.nomk5,
                                        valeurSelectionnee = editedConsultation.k5Value,
                                        descriptionSelectionnee = editedConsultation.k5Id,
                                        coefficients = referenceGeneraleSelectionnee.getModk5(),
                                        onCoefficientSelected = { coef ->
                                            editedConsultation =
                                                    editedConsultation.copy(
                                                            k5Id = coef.description,
                                                            k5Value = coef.coef
                                                    )
                                        }
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
private fun ScoreSelector(
        label: String,
        valeurSelectionnee: Int?,
        onScoreSelected: (Int?) -> Unit,
        plageScore: IntRange,
        descriptions: Map<Int, String>,
        modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Label du score
        Text(
                text = label,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                color = VetNutriColors.Primary
        )

        // Champ de sélection
        OutlinedTextField(
                value =
                        if (valeurSelectionnee != null) {
                            "$valeurSelectionnee/9 - ${descriptions[valeurSelectionnee] ?: "Description non disponible"}"
                        } else {
                            "Aucune note sélectionnée"
                        },
                onValueChange = {},
                label = { Text("Note d'état corporel") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Row {
                        if (valeurSelectionnee != null) {
                            IconButton(onClick = { onScoreSelected(null) }) {
                                Icon(
                                        AppIcons.Close,
                                        contentDescription = "Effacer la note",
                                        tint = Color.Gray
                                )
                            }
                        }
                        IconButton(onClick = { showDialog = true }) {
                            Icon(
                                    AppIcons.ArrowDropDown,
                                    contentDescription = "Sélectionner une note"
                            )
                        }
                    }
                }
        )
    }

    // Dialog de sélection
    if (showDialog) {
        ScoreSelectionDialog(
                label = label,
                plageScore = plageScore,
                descriptions = descriptions,
                scoreSelectionne = valeurSelectionnee,
                onScoreSelected = { score ->
                    onScoreSelected(score)
                    showDialog = false
                },
                onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun ScoreSelectionDialog(
        label: String,
        plageScore: IntRange,
        descriptions: Map<Int, String>,
        scoreSelectionne: Int?,
        onScoreSelected: (Int) -> Unit,
        onDismiss: () -> Unit
) {
    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Sélectionner $label") },
            text = {
                LazyColumn {
                    items(plageScore.toList()) { score ->
                        Row(
                                modifier =
                                        Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                                            onScoreSelected(score)
                                        },
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                    selected = scoreSelectionne == score,
                                    onClick = { onScoreSelected(score) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                        text = "$score/9",
                                        style = MaterialTheme.typography.h6,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Primary
                                )
                                Text(
                                        text = descriptions[score] ?: "Description non disponible",
                                        style = MaterialTheme.typography.body2,
                                        color = Color.Gray
                                )
                            }
                        }
                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                    }
                }
            },
            confirmButton = { TextButton(onClick = onDismiss) { Text("Fermer") } }
    )
}

@Composable
private fun CoefficientSelector(
        nom: String,
        valeurSelectionnee: Float?,
        descriptionSelectionnee: String?,
        coefficients: ArrayList<fr.vetbrain.vetnutri_mp.Data.CoefP>,
        onCoefficientSelected: (fr.vetbrain.vetnutri_mp.Data.CoefP) -> Unit,
        modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Label du coefficient
        Text(
                text = nom,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                color = VetNutriColors.Primary
        )

        // Champ de sélection
        OutlinedTextField(
                value =
                        if (valeurSelectionnee != null && descriptionSelectionnee != null) {
                            "$descriptionSelectionnee (${String.format("%.2f", valeurSelectionnee)})"
                        } else {
                            "Sélectionner un coefficient"
                        },
                onValueChange = {},
                label = { Text("Coefficient") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                                AppIcons.ArrowDropDown,
                                contentDescription = "Sélectionner un coefficient"
                        )
                    }
                }
        )
    }

    // Dialog de sélection
    if (showDialog) {
        CoefficientSelectionDialog(
                nom = nom,
                coefficients = coefficients,
                descriptionSelectionnee = descriptionSelectionnee,
                onCoefficientSelected = { coef ->
                    onCoefficientSelected(coef)
                    showDialog = false
                },
                onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun CoefficientSelectionDialog(
        nom: String,
        coefficients: ArrayList<fr.vetbrain.vetnutri_mp.Data.CoefP>,
        descriptionSelectionnee: String?,
        onCoefficientSelected: (fr.vetbrain.vetnutri_mp.Data.CoefP) -> Unit,
        onDismiss: () -> Unit
) {
    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Sélectionner $nom") },
            text = {
                LazyColumn {
                    items(coefficients) { coef ->
                        Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                    selected = descriptionSelectionnee == coef.description,
                                    onClick = { onCoefficientSelected(coef) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                        text = coef.description ?: "Sans description",
                                        style = MaterialTheme.typography.body1
                                )
                                Text(
                                        text =
                                                "Coefficient: ${String.format("%.2f", coef.coef ?: 1.0f)}",
                                        style = MaterialTheme.typography.body2,
                                        color = Color.Gray
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = onDismiss) { Text("Fermer") } }
    )
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
