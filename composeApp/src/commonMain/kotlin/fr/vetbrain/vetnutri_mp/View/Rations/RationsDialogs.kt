package fr.vetbrain.vetnutri_mp.View

// Dialogues de l'écran Rations (extraits de RationsView.kt) : édition de ration,
// valeurs métaboliques, coefficients, et lignes d'information associées.

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import fr.vetbrain.vetnutri_mp.Components.IconWithTooltip
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.CenteredMessage
import fr.vetbrain.vetnutri_mp.Components.RationGroupItem
import fr.vetbrain.vetnutri_mp.Components.RationItem
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Data.ValeurNutritionnelle
import fr.vetbrain.vetnutri_mp.Data.convertirPreferencesVersLabelsNutriments
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.AnalNut
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.AnimalDetail
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Consultation
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.ConsultationEdit
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Evolutive
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Ration as RationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.PreferencesStorage
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import fr.vetbrain.vetnutri_mp.Utils.createPreferencesStorage
import fr.vetbrain.vetnutri_mp.Utils.normalizeDecimalInput
import fr.vetbrain.vetnutri_mp.Utils.parsePositiveDecimal
import fr.vetbrain.vetnutri_mp.View.AnalNut.AnalyseNutritionnelleCard
import fr.vetbrain.vetnutri_mp.View.AnalNut.MultiNutrientAdjustmentView
import fr.vetbrain.vetnutri_mp.View.AnalNut.NutrientDetailDialog
import fr.vetbrain.vetnutri_mp.View.AnalNut.SectionAlimentsRation
import fr.vetbrain.vetnutri_mp.View.AnalNut.SectionBilanEnergetique
import fr.vetbrain.vetnutri_mp.View.AnalNut.SectionCoefficients
import fr.vetbrain.vetnutri_mp.View.AnalNut.SectionValeursMetaboliques
import fr.vetbrain.vetnutri_mp.View.AnalNut.AnalyseQuantitativeRationSection
import fr.vetbrain.vetnutri_mp.View.Components.RecipeDialog
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.RecipeDialogViewModel
import kotlinx.coroutines.launch
import fr.vetbrain.vetnutri_mp.Utils.isIosPlatform

// Fonction locale InfoRow pour éviter les problèmes d'import
@Composable
private fun LocalInfoRow(label: String, value: String) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = AppSizes.paddingXSmall),
                horizontalArrangement = Arrangement.Start
        ) {
                Text(
                        text = "$label :",
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.width(120.dp)
                )
                Text(text = value, style = MaterialTheme.typography.body1)
        }
}


/**
 * Dialogue pour créer ou éditer une ration
 *
 * @param ration Ration à éditer, null pour une nouvelle ration
 * @param onDismiss Action à exécuter pour fermer le dialogue
 * @param onSave Action à exécuter pour sauvegarder la ration
 */
@Composable
fun RationEditDialog(ration: Ration?, onDismiss: () -> Unit, onSave: (Ration) -> Unit) {
        val isNewRation = ration == null
        val title =
                if (isNewRation) {
                        translate(RationKeys.CREATE_TITLE)
                } else {
                        translate(RationKeys.EDIT_TITLE)
                }

        // État éditable de la ration
        var editedRation by remember {
                mutableStateOf(
                        ration?.copy()
                                ?: Ration(
                                        name = translate(RationKeys.NEW_NAME),
                                        actual = false,
                                        alimentMutableList = mutableListOf()
                                )
                )
        }

        // Conserver le texte brut pendant la saisie, sans réinsérer les décimales effacées.
        var coefficientText by remember(ration?.uuid) {
                mutableStateOf(editedRation.coef.toString().replace('.', ','))
        }
        val coefficient = coefficientText.replace(',', '.').toDoubleOrNull()
        val coefficientValid = coefficient != null && coefficient.isFinite() && coefficient > 0

        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(title, style = MaterialTheme.typography.h6) },
                text = {
                        Column(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(vertical = AppSizes.paddingSmall),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                // Nom de la ration
                                OutlinedTextField(
                                        value = editedRation.name,
                                        onValueChange = {
                                                editedRation = editedRation.copy(name = it)
                                        },
                                        label = { Text(translate(RationKeys.NAME_LABEL)) },
                                        modifier = Modifier.fillMaxWidth()
                                )

                                // Type de ration (actuelle ou proposée)
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                translate(RationKeys.TYPE_LABEL),
                                                modifier = Modifier.weight(1f)
                                        )

                                        Switch(
                                                checked = editedRation.actual,
                                                onCheckedChange = { checked ->
                                                        editedRation =
                                                                editedRation.copy(actual = checked)
                                                },
                                                colors =
                                                        SwitchDefaults.colors(
                                                                checkedThumbColor =
                                                                        VetNutriColors.Primary,
                                                                checkedTrackColor =
                                                                        VetNutriColors.Primary.copy(
                                                                                alpha = 0.5f
                                                                        )
                                                        )
                                        )

                                        Text(
                                                text =
                                                        if (editedRation.actual) {
                                                                translate(RationKeys.ACTUAL)
                                                        } else {
                                                                translate(RationKeys.PROPOSED)
                                                        },
                                                style = MaterialTheme.typography.body2,
                                                modifier =
                                                        Modifier.padding(
                                                                start = AppSizes.paddingSmall
                                                        )
                                        )
                                }

                                // Description de la ration
                                OutlinedTextField(
                                        value = editedRation.description,
                                        onValueChange = {
                                                editedRation = editedRation.copy(description = it)
                                        },
                                        label = { Text(translate(RationKeys.DESCRIPTION)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 3,
                                        singleLine = false
                                )

                                // Coefficient de la ration
                                OutlinedTextField(
                                        value = coefficientText,
                                        onValueChange = { coefficientText = it },
                                        isError = !coefficientValid,
                                        label = { Text(translate(RationKeys.COEFFICIENT_LABEL)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions =
                                                KeyboardOptions(
                                                        keyboardType = KeyboardType.Decimal
                                                ),
                                        placeholder = {
                                                Text(translate(RationKeys.COEFFICIENT_PLACEHOLDER))
                                        }
                                )
                        }
                },
                confirmButton = {
                        Button(
                                onClick = { coefficient?.let { onSave(editedRation.copy(coef = it)) } },
                                enabled = coefficientValid,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Primary,
                                                contentColor = VetNutriColors.OnPrimary
                                        )
                        ) { Text(translate(General.SAVE)) }
                },
                dismissButton = { TextButton(onClick = onDismiss) { Text(translate(General.CANCEL)) } }
        )
}

/**
 * Composant compact pour afficher une ligne d'information avec label et valeur Format en colonne
 * pour économiser l'espace
 */
@Composable
private fun CompactLocalInfoRow(label: String, value: String) {
        Column {
                Text(
                        text = label,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
                Text(
                        text = value,
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Medium
                )
        }
}

/** Dialogue agrandi pour les valeurs métaboliques */
@Composable
internal fun MetabolicValuesDialog(
        selectedConsultation: ConsultationEv?,
        poidsCalcul: Double?,
        poidsMetabolique: Double?,
        besoinEnergetiqueStandard: Double?,
        besoinEnergetiqueTotal: Double?,
        kObserve: Double,
        kCalcule: Double,
        referenceUtilisee: ReferenceEv?,
        avertissements: List<String> = emptyList(),
        onDismiss: () -> Unit
) {
        AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                        Text(
                                translate(AnalNut.METABOLIC_VALUES_TITLE),
                                style = MaterialTheme.typography.h5,
                                color = VetNutriColors.Primary
                        )
                },
                text = {
                        Column(verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)) {
                                fr.vetbrain.vetnutri_mp.View.AnalNut.AvertissementsMetaboliques(avertissements)
                                LocalInfoRow(
                                        label = translate(AnalNut.WEIGHT_CURRENT),
                                        value =
                                                selectedConsultation?.weight?.let {
                                                        "${fr.vetbrain.vetnutri_mp.Utils.TextUtils.formatDecimal(it.toDouble(), 1)} kg"
                                                }
                                                        ?: translate(General.NOT_SPECIFIED)
                                )

                                LocalInfoRow(
                                        label = translate(AnalNut.WEIGHT_IDEAL),
                                        value =
                                                poidsCalcul?.let {
                                                        "${fr.vetbrain.vetnutri_mp.Utils.TextUtils.formatDecimal(it.toDouble(), 1)} kg"
                                                }
                                                        ?: translate(General.NOT_CALCULATED)
                                )

                                LocalInfoRow(
                                        label = translate(AnalNut.WEIGHT_METABOLIC),
                                        value =
                                                poidsMetabolique?.let {
                                                        TextUtils.formatKgAvecPuissanceDynamique(
                                                                it,
                                                                referenceUtilisee?.equationBW?.equationScript
                                                        )
                                                }
                                                        ?: translate(General.NOT_CALCULATED)
                                )

                                LocalInfoRow(
                                        label = translate(AnalNut.ENERGY_STANDARD),
                                        value =
                                                besoinEnergetiqueStandard?.let {
                                                        translate(
                                                                "ration.energyPerDayFormat",
                                                                fr.vetbrain.vetnutri_mp.Utils.TextUtils.formatDecimal(it.toDouble(), 1)
                                                        )
                                                }
                                                        ?: translate(General.NOT_CALCULATED)
                                )

                                LocalInfoRow(
                                        label = translate(AnalNut.ENERGY_TOTAL),
                                        value =
                                                besoinEnergetiqueTotal?.let {
                                                        translate(
                                                                "ration.energyPerDayFormat",
                                                                fr.vetbrain.vetnutri_mp.Utils.TextUtils.formatDecimal(it.toDouble(), 1)
                                                        )
                                                }
                                                        ?: translate(General.NOT_CALCULATED)
                                )

                                Divider()

                                LocalInfoRow(
                                        label = translate(AnalNut.K_OBSERVED),
                                        value =
                                                fr.vetbrain.vetnutri_mp.Utils.TextUtils
                                                        .formatDecimal(kObserve, 2)
                                )

                                LocalInfoRow(
                                        label = translate(AnalNut.K_CALCULATED),
                                        value =
                                                fr.vetbrain.vetnutri_mp.Utils.TextUtils
                                                        .formatDecimal(kCalcule, 2)
                                )

                                referenceUtilisee?.let { reference ->
                                        Divider()
                                        Text(
                                                translate(
                                                        AnalNut.REFERENCE_USED,
                                                        reference.nom
                                                ),
                                                style = MaterialTheme.typography.caption,
                                                color = VetNutriColors.Primary
                                        )
                                }
                        }
                },
                confirmButton = {
                        TextButton(onClick = onDismiss) { Text(translate(General.CLOSE)) }
                }
        )
}

/**
 * Composable pour éditer un coefficient avec une combobox de valeurs prédéfinies et édition directe
 * pour valeurs personnalisées
 */
@Composable
private fun CoefficientEditableRow(
        label: String,
        currentValue: Double?,
        currentDescription: String?,
        availableCoefficients: List<fr.vetbrain.vetnutri_mp.Data.CoefP>,
        onCoefficientSelected: (fr.vetbrain.vetnutri_mp.Data.CoefP) -> Unit
) {
        var showDropdown by remember { mutableStateOf(false) }
        val customValueLabel = translate(RationKeys.CUSTOM_VALUE)

        // Vérifier si la valeur actuelle est personnalisée (pas dans la liste prédéfinie)
        val isCustomValue =
                remember(currentValue, currentDescription, availableCoefficients) {
                        currentDescription == customValueLabel ||
                                availableCoefficients.none {
                                        it.coef == currentValue &&
                                                it.description == currentDescription
                                }
                }

        // État pour l'édition directe des valeurs personnalisées
        var editableValue by
                remember {
                        mutableStateOf(
                                fr.vetbrain.vetnutri_mp.Utils.TextUtils.formatDecimal(
                                                (currentValue ?: 1.0).toDouble(),
                                                2
                                        )
                                        .replace('.', ',')
                        )
                }
        var isEditing by remember { mutableStateOf(false) }

        // Synchroniser editableValue avec currentValue uniquement hors mode édition
        LaunchedEffect(currentValue) {
                if (!isEditing) {
                        editableValue =
                                fr.vetbrain.vetnutri_mp.Utils.TextUtils.formatDecimal(
                                                (currentValue ?: 1.0).toDouble(),
                                                2
                                        )
                                        .replace('.', ',')
                }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
                // Label du coefficient
                Text(
                        text = label,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold,
                        color = VetNutriColors.Primary
                )

                // Champ de sélection avec dropdown ou édition directe
                if (isEditing) {
                        // Mode édition : afficher le TextField
                        OutlinedTextField(
                                value = editableValue,
                                onValueChange = { newValue ->
                                        editableValue = newValue
                                },
                                label = { Text(translate(ConsultationEdit.COEF_SELECTOR_LABEL)) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions =
                                        KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                placeholder = {
                                        Text(translate(RationKeys.COEFFICIENT_PLACEHOLDER))
                                },
                                trailingIcon = {
                                        Row {
                                                // Bouton de validation (uniquement en mode édition)
                                                if (isEditing) {
                                                        IconButtonWithTooltip(
                                                                onClick = {
                                                                        // Valider et sortir du mode
                                                                        // édition
                                                                        val normalizedText =
                                                                                editableValue
                                                                                        .replace(
                                                                                                ',',
                                                                                                '.'
                                                                                        )
                                                                        val value =
                                                                                normalizedText
                                                                                        .toDoubleOrNull()
                                                                        if (value != null &&
                                                                                        value > 0
                                                                        ) {
                                                                                val customCoef =
                                                                                        fr.vetbrain
                                                                                                .vetnutri_mp
                                                                                                .Data
                                                                                                .CoefP(
                                                                                                        description =
                                                                                                                customValueLabel,
                                                                                                        coef =
                                                                                                                value,
                                                                                                        groupUUID =
                                                                                                                null
                                                                                                )
                                                                                onCoefficientSelected(
                                                                                        customCoef
                                                                                )
                                                                        }
                                                                        isEditing = false
                                                                },
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription =
                                                                        translate(General.VALIDATE),
                                                                tooltip = translate(General.VALIDATE),
                                                                tint = VetNutriColors.Primary
                                                        )
                                                }

                                                // Bouton dropdown (toujours présent)
                                                IconButtonWithTooltip(
                                                        onClick = {
                                                                showDropdown = true
                                                                isEditing = false // Sortir du mode
                                                                // édition si
                                                                // on ouvre le dropdown
                                                        },
                                                        imageVector = if (showDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                        contentDescription =
                                                                translate(ConsultationEdit.COEF_SELECTOR_NONE),
                                                        tooltip =
                                                                translate(ConsultationEdit.COEF_SELECTOR_NONE)
                                                )
                                        }
                                }
                        )
                } else {
                        // Mode lecture : afficher le texte cliquable dans un Box
                        Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                        value =
                                                buildString {
                                                        if (currentDescription != null &&
                                                                        currentValue != null &&
                                                                        !isCustomValue
                                                        ) {
                                                                append(
                                                                        "$currentDescription (${fr.vetbrain.vetnutri_mp.Utils.TextUtils.formatDecimal((currentValue ?: 0.0).toDouble(), 2)})"
                                                                )
                                                        } else {
                                                                append(
                                                                        (fr.vetbrain.vetnutri_mp
                                                                                        .Utils
                                                                                        .TextUtils
                                                                                        .formatDecimal(
                                                                                                (currentValue
                                                                                                                ?: 1.0)
                                                                                                        .toDouble(),
                                                                                                2
                                                                                        ))
                                                                                .replace('.', ',')
                                                                )
                                                        }
                                                },
                                        onValueChange = { /* Pas d'édition en mode lecture */},
                                        label = {
                                                Text(
                                                        translate(
                                                                ConsultationEdit.COEF_SELECTOR_LABEL
                                                        )
                                                )
                                        },
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                                // Bouton dropdown (toujours présent)
                                                IconButtonWithTooltip(
                                                        onClick = { showDropdown = true },
                                                        imageVector = if (showDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                        contentDescription =
                                                                translate(ConsultationEdit.COEF_SELECTOR_NONE),
                                                        tooltip =
                                                                translate(ConsultationEdit.COEF_SELECTOR_NONE)
                                                )
                                        }
                                )

                                // Zone cliquable invisible qui couvre le contenu du TextField
                                Box(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .height(
                                                                56.dp
                                                        ) // Hauteur standard d'un OutlinedTextField
                                                        .clickable {
                                                                // Cliquer sur le texte lance
                                                                // directement l'édition
                                                                isEditing = true
                                                                // Si c'était un coefficient de
                                                                // référence, on initialise avec la
                                                                // valeur seule
                                                                if (!isCustomValue &&
                                                                                currentValue != null
                                                                ) {
                                                                        editableValue =
                                                                                fr.vetbrain
                                                                                        .vetnutri_mp
                                                                                        .Utils
                                                                                        .TextUtils
                                                                                        .formatDecimal(
                                                                                                (currentValue
                                                                                                                ?: 0.0)
                                                                                                        .toDouble(),
                                                                                                2
                                                                                        )
                                                                                        .replace(
                                                                                                '.',
                                                                                                ','
                                                                                        )
                                                                }
                                                        }
                                                        .padding(
                                                                end = 64.dp
                                                        ) // Eviter de chevaucher avec le bouton
                                        // dropdown (plus de marge)
                                        )
                        }
                }

                // DropdownMenu avec les coefficients disponibles
                DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = {
                            if (!isIosPlatform) {
                                showDropdown = false 
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                ) {
                        availableCoefficients.forEach { coef ->
                                DropdownMenuItem(
                                        onClick = {
                                                onCoefficientSelected(coef)
                                                showDropdown = false
                                        }
                                ) {
                                        Column {
                                                Text(
                                                        text = coef.description
                                                                        ?: translate(
                                                                                ConsultationEdit
                                                                                        .REF_COEF_NO_DESC
                                                                        ),
                                                        style = MaterialTheme.typography.body1
                                                )
                                                Text(
                                                        text =
                                                                translate(
                                                                        ConsultationEdit.COEF_VAL_LABEL,
                                                                        fr.vetbrain
                                                                                .vetnutri_mp
                                                                                .Utils
                                                                                .TextUtils
                                                                                .formatDecimal(
                                                                                        (coef.coef
                                                                                                ?: 1.0)
                                                                                                .toDouble(),
                                                                                        2
                                                                                )
                                                                ),
                                                        style = MaterialTheme.typography.body2,
                                                        color = Color.Gray
                                                )
                                        }
                                }
                        }

                        // Option pour saisie manuelle
                        Divider()
                        DropdownMenuItem(
                                onClick = {
                                        // Lancer l'édition directe
                                        isEditing = true
                                        if (!isCustomValue && currentValue != null) {
                                                editableValue =
                                                        fr.vetbrain.vetnutri_mp.Utils.TextUtils
                                                                .formatDecimal(
                                                                        (currentValue ?: 0.0)
                                                                                .toDouble(),
                                                                        2
                                                                )
                                                                .replace('.', ',')
                                        }
                                        showDropdown = false
                                }
                        ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                                text = translate(RationKeys.DIRECT_EDIT),
                                                style = MaterialTheme.typography.body2,
                                                fontStyle = FontStyle.Italic
                                        )
                                }
                        }
                }
        }
}

/** Dialogue agrandi pour les coefficients */
@Composable
internal fun CoefficientsDialog(
        selectedConsultation: ConsultationEv?,
        viewModel: AnimalDetailViewModel,
        onDismiss: () -> Unit
) {
        // Observer la référence utilisée pour récupérer les coefficients disponibles
        val referenceUtilisee by viewModel.referenceUtilisee.collectAsState()
        val focusManager = LocalFocusManager.current

        // État pour l'édition du coefficient d'ajustement
        var isEditingCoefficient by remember { mutableStateOf(false) }
        var coefficientText by
                remember(selectedConsultation) {
                        mutableStateOf(
                                normalizeDecimalInput(
                                        selectedConsultation?.coefficientAjustement?.toString() ?: "1.0"
                                )
                        )
                }
        val coefficientValue = parsePositiveDecimal(coefficientText)
        val isCoefficientValid = coefficientValue != null
        val showCoefficientError = coefficientText.isNotBlank() && !isCoefficientValid

        AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                        Text(
                                translate(RationKeys.COEFFICIENTS_DETAIL_TITLE),
                                style = MaterialTheme.typography.h5,
                                color = VetNutriColors.Primary
                        )
                },
                text = {
                        LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium),
                                modifier = Modifier.height(400.dp)
                        ) {
                                item {
                                        Text(
                                                translate(RationKeys.COEFFICIENTS_K_TITLE),
                                                style = MaterialTheme.typography.h6,
                                                color = VetNutriColors.Primary,
                                                fontWeight = FontWeight.Bold
                                        )
                                }

                                // K1 - Stade physiologique
                                item {
                                        CoefficientEditableRow(
                                                label = "K1",
                                                currentValue = selectedConsultation?.k1Value,
                                                currentDescription = selectedConsultation?.k1Id,
                                                availableCoefficients =
                                                        referenceUtilisee?.modk1
                                                                ?: emptyList(),
                                                onCoefficientSelected = { coef ->
                                                        selectedConsultation?.let { consultation ->
                                                                viewModel.updateCoefficient(
                                                                        consultation.uuid,
                                                                        "k1",
                                                                        coef.coef ?: 1.0,
                                                                        coef.description
                                                                )
                                                        }
                                                }
                                        )
                                }

                                // K2 - Activité
                                item {
                                        CoefficientEditableRow(
                                                label = "K2",
                                                currentValue = selectedConsultation?.k2Value,
                                                currentDescription = selectedConsultation?.k2Id,
                                                availableCoefficients =
                                                        referenceUtilisee?.modk2
                                                                ?: emptyList(),
                                                onCoefficientSelected = { coef ->
                                                        selectedConsultation?.let { consultation ->
                                                                viewModel.updateCoefficient(
                                                                        consultation.uuid,
                                                                        "k2",
                                                                        coef.coef ?: 1.0,
                                                                        coef.description
                                                                )
                                                        }
                                                }
                                        )
                                }

                                // K3 - Environnement
                                item {
                                        CoefficientEditableRow(
                                                label = "K3",
                                                currentValue = selectedConsultation?.k3Value,
                                                currentDescription = selectedConsultation?.k3Id,
                                                availableCoefficients =
                                                        referenceUtilisee?.modk3
                                                                ?: emptyList(),
                                                onCoefficientSelected = { coef ->
                                                        selectedConsultation?.let { consultation ->
                                                                viewModel.updateCoefficient(
                                                                        consultation.uuid,
                                                                        "k3",
                                                                        coef.coef ?: 1.0,
                                                                        coef.description
                                                                )
                                                        }
                                                }
                                        )
                                }

                                // K4 - État corporel
                                item {
                                        CoefficientEditableRow(
                                                label = "K4",
                                                currentValue = selectedConsultation?.k4Value,
                                                currentDescription = selectedConsultation?.k4Id,
                                                availableCoefficients =
                                                        referenceUtilisee?.modk4
                                                                ?: emptyList(),
                                                onCoefficientSelected = { coef ->
                                                        selectedConsultation?.let { consultation ->
                                                                viewModel.updateCoefficient(
                                                                        consultation.uuid,
                                                                        "k4",
                                                                        coef.coef ?: 1.0,
                                                                        coef.description
                                                                )
                                                        }
                                                }
                                        )
                                }

                                // K5 - Pathologie
                                item {
                                        CoefficientEditableRow(
                                                label = "K5",
                                                currentValue = selectedConsultation?.k5Value,
                                                currentDescription = selectedConsultation?.k5Id,
                                                availableCoefficients =
                                                        referenceUtilisee?.modk5
                                                                ?: emptyList(),
                                                onCoefficientSelected = { coef ->
                                                        selectedConsultation?.let { consultation ->
                                                                viewModel.updateCoefficient(
                                                                        consultation.uuid,
                                                                        "k5",
                                                                        coef.coef ?: 1.0,
                                                                        coef.description
                                                                )
                                                        }
                                                }
                                        )
                                }

                                item { Divider(color = VetNutriColors.Primary.copy(alpha = 0.3f)) }

                                // Coefficient d'ajustement éditable
                                item {
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                Text(
                                                        translate(RationKeys.COEFFICIENT_ADJUST_LABEL),
                                                        style = MaterialTheme.typography.subtitle1
                                                )

                                                if (isEditingCoefficient) {
                                                        Row(
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                OutlinedTextField(
                                                                        value = coefficientText,
                                                                        onValueChange = {
                                                                                coefficientText = normalizeDecimalInput(it)
                                                                        },
                                                                        modifier =
                                                                                Modifier.width(
                                                                                        100.dp
                                                                                ),
                                                                        singleLine = true,
                                                                        isError =
                                                                                showCoefficientError,
                                                                        keyboardOptions =
                                                                                KeyboardOptions(
                                                                                        keyboardType =
                                                                                                KeyboardType
                                                                                                        .Decimal,
                                                                                        imeAction =
                                                                                                ImeAction
                                                                                                        .Done
                                                                                ),
                                                                        keyboardActions =
                                                                                KeyboardActions(
                                                                                        onDone = {
                                                                                                coefficientValue
                                                                                                        ?.let {
                                                                                                                newValue
                                                                                                                ->
                                                                                                                selectedConsultation
                                                                                                                        ?.let {
                                                                                                                                consultation
                                                                                                                                ->
                                                                                                                                viewModel
                                                                                                                                        .updateCoefficientAjustement(
                                                                                                                                                consultation
                                                                                                                                                        .uuid,
                                                                                                                                                newValue
                                                                                                                                        )
                                                                                                                        }
                                                                                                isEditingCoefficient =
                                                                                                        false
                                                                                                focusManager
                                                                                                        .clearFocus()
                                                                                                        }
                                                                                        }
                                                                                )
                                                                )
                                                                IconButtonWithTooltip(
                                                                        onClick = {
                                                                                coefficientValue
                                                                                        ?.let { newValue ->
                                                                                                selectedConsultation?.let {
                                                                                                        consultation
                                                                                                        ->
                                                                                                        viewModel
                                                                                                                .updateCoefficientAjustement(
                                                                                                                        consultation
                                                                                                                                .uuid,
                                                                                                                        newValue
                                                                                                                )
                                                                                                }
                                                                                        }
                                                                                isEditingCoefficient =
                                                                                        false
                                                                                focusManager
                                                                                        .clearFocus()
                                                                        },
                                                                        imageVector = Icons.Filled.Check,
                                                                        contentDescription =
                                                                                translate(General.VALIDATE),
                                                                        tooltip =
                                                                                translate(General.VALIDATE),
                                                                        enabled = isCoefficientValid,
                                                                        tint = Color.Green
                                                                )
                                                                IconButtonWithTooltip(
                                                                        onClick = {
                                                                                coefficientText =
                                                                                        selectedConsultation
                                                                                                ?.coefficientAjustement
                                                                                                ?.toString()
                                                                                                ?: "1.0"
                                                                                isEditingCoefficient =
                                                                                        false
                                                                                focusManager
                                                                                        .clearFocus()
                                                                        },
                                                                        imageVector = Icons.Filled.Close,
                                                                        contentDescription =
                                                                                translate(General.CANCEL),
                                                                        tooltip =
                                                                                translate(General.CANCEL),
                                                                        tint = Color.Red
                                                                )
                                                        }
                                                } else {
                                                        Row(
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                Text(
                                                                        selectedConsultation
                                                                                ?.coefficientAjustement
                                                                                ?.let {
                                                                                        fr.vetbrain
                                                                                                .vetnutri_mp
                                                                                                .Utils
                                                                                                .TextUtils
                                                                                                .formatDecimal(
                                                                                                        it,
                                                                                                        2
                                                                                                )
                                                                                }
                                                                                ?: "1.00",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body1,
                                                                        fontWeight =
                                                                                FontWeight.Medium
                                                                )
                                                                IconButtonWithTooltip(
                                                                        onClick = {
                                                                                coefficientText =
                                                                                        normalizeDecimalInput(
                                                                                                selectedConsultation
                                                                                                        ?.coefficientAjustement
                                                                                                        ?.toString()
                                                                                                        ?: "1.0"
                                                                                        )
                                                                                isEditingCoefficient =
                                                                                        true
                                                                        },
                                                                        imageVector = Icons.Filled.Edit,
                                                                        contentDescription =
                                                                                translate(General.EDIT),
                                                                        tooltip =
                                                                                translate(General.EDIT)
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }
                },
                confirmButton = { TextButton(onClick = onDismiss) { Text(translate(General.CLOSE)) } }
        )
}

/** Composable pour afficher une ligne d'information détaillée avec description */
@Composable
private fun DetailedLocalInfoRow(label: String, value: String, description: String? = null) {
        Column {
                Text(
                        text = label,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold,
                        color = VetNutriColors.Primary
                )
                Text(
                        text = value,
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Medium
                )
                description?.let {
                        Text(
                                text = it,
                                style = MaterialTheme.typography.caption,
                                color = VetNutriColors.Primary.copy(alpha = 0.7f)
                        )
                }
        }
}
