package fr.vetbrain.vetnutri_mp.View

import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.AppDatePicker
import fr.vetbrain.vetnutri_mp.Components.AppTextField
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationKeyword
import fr.vetbrain.vetnutri_mp.Data.avecParametresType
import fr.vetbrain.vetnutri_mp.Data.SupplementalvariableP
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.ConsultationEdit
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Ration
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.datetime.LocalDate
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import androidx.compose.runtime.MutableState

/** Éditeur de consultation : coefficients d'ajustement énergétique K1 à K5 */
@Composable
internal fun ColumnScope.CarteCoefficientsConsultation(
        availableReferences: List<ReferenceEv>,
        editedConsultationState: MutableState<ConsultationEv>,
        referenceGeneraleSelectionnee: ReferenceEv?
) {
        var editedConsultation by editedConsultationState
        if (availableReferences.isNotEmpty()) {
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = AppSizes.elevationSmall,
                        backgroundColor = VetNutriColors.Surface
                ) {
                        Column(
                                modifier = Modifier.padding(AppSizes.paddingLarge),
                                verticalArrangement =
                                        Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                Text(
                                        text =
                                                ConsultationEdit.ADJUST_COEF_TITLE.translate(),
                                        style = MaterialTheme.typography.h6,
                                        color = VetNutriColors.Primary
                                )

                                Divider(
                                        color =
                                                VetNutriColors.Primary.copy(
                                                        alpha = 0.3f
                                                )
                                )

                                // Grille des coefficients K1-K5
                                Column(
                                        verticalArrangement =
                                                Arrangement.spacedBy(
                                                        AppSizes.paddingSmall
                                                )
                                ) {
                                        // Coefficient K1
                                        CoefficientSelector(
                                                nom =
                                                        referenceGeneraleSelectionnee
                                                                ?.nomk1?.takeIf {
                                                                it.isNotBlank()
                                                        }
                                                                ?: ConsultationEdit.COEF_K1_FALLBACK.translate(),
                                                valeurSelectionnee =
                                                        editedConsultation.k1Value,
                                                descriptionSelectionnee =
                                                        editedConsultation.k1Id,
                                                coefficients =
                                                        referenceGeneraleSelectionnee
                                                                ?.modk1
                                                                ?: arrayListOf(),
                                                onCoefficientSelected = { coef ->
                                                        editedConsultation =
                                                                editedConsultation
                                                                        .copy(
                                                                                k1Id =
                                                                                        coef.description,
                                                                                k1Value =
                                                                                        coef.coef
                                                                        )
                                                }
                                        )

                                        // Coefficient K2
                                        CoefficientSelector(
                                                nom =
                                                        referenceGeneraleSelectionnee
                                                                ?.nomk2?.takeIf {
                                                                it.isNotBlank()
                                                        }
                                                                ?: ConsultationEdit.COEF_K2_FALLBACK.translate(),
                                                valeurSelectionnee =
                                                        editedConsultation.k2Value,
                                                descriptionSelectionnee =
                                                        editedConsultation.k2Id,
                                                coefficients =
                                                        referenceGeneraleSelectionnee
                                                                ?.modk2
                                                                ?: arrayListOf(),
                                                onCoefficientSelected = { coef ->
                                                        editedConsultation =
                                                                editedConsultation
                                                                        .copy(
                                                                                k2Id =
                                                                                        coef.description,
                                                                                k2Value =
                                                                                        coef.coef
                                                                        )
                                                }
                                        )

                                        // Coefficient K3
                                        CoefficientSelector(
                                                nom =
                                                        referenceGeneraleSelectionnee
                                                                ?.nomk3?.takeIf {
                                                                it.isNotBlank()
                                                        }
                                                                ?: ConsultationEdit.COEF_K3_FALLBACK.translate(),
                                                valeurSelectionnee =
                                                        editedConsultation.k3Value,
                                                descriptionSelectionnee =
                                                        editedConsultation.k3Id,
                                                coefficients =
                                                        referenceGeneraleSelectionnee
                                                                ?.modk3
                                                                ?: arrayListOf(),
                                                onCoefficientSelected = { coef ->
                                                        editedConsultation =
                                                                editedConsultation
                                                                        .copy(
                                                                                k3Id =
                                                                                        coef.description,
                                                                                k3Value =
                                                                                        coef.coef
                                                                        )
                                                }
                                        )

                                        // Coefficient K4
                                        CoefficientSelector(
                                                nom =
                                                        referenceGeneraleSelectionnee
                                                                ?.nomk4?.takeIf {
                                                                it.isNotBlank()
                                                        }
                                                                ?: ConsultationEdit.COEF_K4_FALLBACK.translate(),
                                                valeurSelectionnee =
                                                        editedConsultation.k4Value,
                                                descriptionSelectionnee =
                                                        editedConsultation.k4Id,
                                                coefficients =
                                                        referenceGeneraleSelectionnee
                                                                ?.modk4
                                                                ?: arrayListOf(),
                                                onCoefficientSelected = { coef ->
                                                        editedConsultation =
                                                                editedConsultation
                                                                        .copy(
                                                                                k4Id =
                                                                                        coef.description,
                                                                                k4Value =
                                                                                        coef.coef
                                                                        )
                                                }
                                        )

                                        // Coefficient K5
                                        CoefficientSelector(
                                                nom =
                                                        referenceGeneraleSelectionnee
                                                                ?.nomk5?.takeIf {
                                                                it.isNotBlank()
                                                        }
                                                                ?: ConsultationEdit.COEF_K5_FALLBACK.translate(),
                                                valeurSelectionnee =
                                                        editedConsultation.k5Value,
                                                descriptionSelectionnee =
                                                        editedConsultation.k5Id,
                                                coefficients =
                                                        referenceGeneraleSelectionnee
                                                                ?.modk5
                                                                ?: arrayListOf(),
                                                onCoefficientSelected = { coef ->
                                                        editedConsultation =
                                                                editedConsultation
                                                                        .copy(
                                                                                k5Id =
                                                                                        coef.description,
                                                                                k5Value =
                                                                                        coef.coef
                                                                        )
                                                }
                                        )
                                }
                        }
                }
        }
}

/** Éditeur de consultation : variables supplémentaires requises par les référentiels */
@Composable
internal fun ColumnScope.VariablesSupplementairesConsultation(
        editedConsultationState: MutableState<ConsultationEv>,
        variablesRequises: List<VariableKind>
) {
        var editedConsultation by editedConsultationState
        if (variablesRequises.isNotEmpty()) {
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor =
                                VetNutriColors.Primary.copy(alpha = 0.05f),
                        elevation = 2.dp
                ) {
                        Column(
                                modifier = Modifier.padding(AppSizes.paddingMedium)
                        ) {
                                Text(
                                        text = ConsultationEdit.SUPP_VAR_TITLE.translate(),
                                        style = MaterialTheme.typography.h6,
                                        color = VetNutriColors.Primary,
                                        fontWeight = FontWeight.Bold
                                )

                                Text(
                                        text =
                                                ConsultationEdit.SUPP_VAR_DESC.translate(),
                                        style = MaterialTheme.typography.body2,
                                        color = Color.Gray,
                                        modifier =
                                                Modifier.padding(
                                                        vertical =
                                                                AppSizes.paddingSmall
                                                )
                                )

                                variablesRequises.forEach { variableKind ->
                                        VariableSupplementaireField(
                                                variable = variableKind,
                                                valeurActuelle =
                                                        editedConsultation.suppVarp
                                                                .find {
                                                                        it.variable ==
                                                                                variableKind
                                                                }
                                                                ?.varue,
                                                onValeurChange = { nouvelleValeur ->
                                                        val variablesModifiees =
                                                                editedConsultation
                                                                        .suppVarp
                                                                        .toMutableList()

                                                        // Supprimer
                                                        // l'ancienne valeur
                                                        variablesModifiees
                                                                .removeAll {
                                                                        it.variable ==
                                                                                variableKind
                                                                }

                                                        // Ajouter
                                                        // la
                                                        // nouvelle
                                                        // valeur si
                                                        // elle
                                                        // n'est pas
                                                        // nulle
                                                        if (nouvelleValeur != null
                                                        ) {
                                                                variablesModifiees
                                                                        .add(
                                                                                SupplementalvariableP(
                                                                                        variableKind,
                                                                                        nouvelleValeur
                                                                                )
                                                                        )
                                                        }

                                editedConsultation =
                                        editedConsultation
                                                .copy(
                                                        suppVarp =
                                                                variablesModifiees
                                                )
                        },
                                                modifier =
                                                        Modifier.padding(
                                                                vertical =
                                                                        AppSizes.paddingXSmall
                                                        )
                                        )
                                }
                        }
                }
        }
}

/** Éditeur de consultation : référentiel général et référentiels maladies */
@Composable
internal fun ColumnScope.CarteReferencesConsultation(
        availableReferences: List<ReferenceEv>,
        editedConsultationState: MutableState<ConsultationEv>,
        referenceGeneraleSelectionnee: ReferenceEv?,
        referencesMaladies: List<ReferenceEv>,
        showReferenceGeneraleDialogState: MutableState<Boolean>,
        showReferenceMaladieDialogState: MutableState<Boolean>
) {
        var editedConsultation by editedConsultationState
        var showReferenceGeneraleDialog by showReferenceGeneraleDialogState
        var showReferenceMaladieDialog by showReferenceMaladieDialogState
        Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = AppSizes.elevationSmall,
                backgroundColor = VetNutriColors.Surface
        ) {
                Column(
                        modifier = Modifier.padding(AppSizes.paddingLarge),
                        verticalArrangement =
                                Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                        Text(
                                text = ConsultationEdit.REF_SECTION_TITLE.translate(),
                                style = MaterialTheme.typography.h6,
                                color = VetNutriColors.Primary
                        )

                        Divider(color = VetNutriColors.Primary.copy(alpha = 0.3f))

                        // Référence générale
                        Text(
                                text = ConsultationEdit.REF_GENERAL_SUBTITLE.translate(),
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                                value = referenceGeneraleSelectionnee?.nom
                                                ?: ConsultationEdit.REF_GENERAL_NONE.translate(),
                                onValueChange = {},
                                label = { Text(ConsultationEdit.REF_GENERAL_SUBTITLE.translate()) },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                        IconButtonWithTooltip(
                                                onClick = {
                                                        showReferenceGeneraleDialog =
                                                                true
                                                },
                                                imageVector = AppIcons.ArrowDropDown,
                                                contentDescription = ConsultationEdit.REF_SELECT_TOOLTIP.translate(),
                                                tooltip = ConsultationEdit.REF_SELECT_TOOLTIP.translate()
                                        )
                                }
                        )

                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

                        // Références liées aux maladies
                        Text(
                                text = ConsultationEdit.REF_DISEASE_SUBTITLE.translate(),
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold
                        )

                        // Liste des références de maladies
                        if (editedConsultation.referencesMaladies.isNotEmpty()) {
                                Column(
                                        verticalArrangement =
                                                Arrangement.spacedBy(
                                                        AppSizes.paddingSmall
                                                )
                                ) {
                                        editedConsultation.referencesMaladies
                                                .forEach { referenceId ->
                                                        val referenceMaladie =
                                                                availableReferences
                                                                        .find {
                                                                                it.uuid ==
                                                                                        referenceId
                                                                        }
                                                        Card(
                                                                modifier =
                                                                        Modifier.fillMaxWidth(),
                                                                backgroundColor =
                                                                        VetNutriColors
                                                                                .Surface
                                                                                .copy(
                                                                                        alpha =
                                                                                                0.7f
                                                                                ),
                                                                elevation = 2.dp
                                                        ) {
                                                                Row(
                                                                        modifier =
                                                                                Modifier.padding(
                                                                                        AppSizes.paddingMedium
                                                                                ),
                                                                        horizontalArrangement =
                                                                                Arrangement
                                                                                        .SpaceBetween,
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically
                                                                ) {
                                                                        Column(
                                                                                modifier =
                                                                                        Modifier.weight(
                                                                                                1f
                                                                                        )
                                                                        ) {
                                                                                Text(
                                                                                        text =
                                                                                                referenceMaladie
                                                                                                        ?.nom
                                                                                                        ?: ConsultationEdit.REF_UNKNOWN.translate(),
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .body1,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .Bold
                                                                                )
                                                                                if (referenceMaladie
                                                                                                ?.description
                                                                                                ?.isNotBlank() ==
                                                                                                true
                                                                                ) {
                                                                                        Text(
                                                                                                text =
                                                                                                        referenceMaladie
                                                                                                                .description,
                                                                                                style =
                                                                                                        MaterialTheme
                                                                                                                .typography
                                                                                                                .body2,
                                                                                                color =
                                                                                                        Color.Gray
                                                                                        )
                                                                                }
                                                                        }
                                                                        IconButtonWithTooltip(
                                                                                onClick = {
                                                                                        val nouvellesReferences =
                                                                                                editedConsultation
                                                                                                        .referencesMaladies
                                                                                                        .toMutableList()
                                                                                        nouvellesReferences
                                                                                                .remove(
                                                                                                        referenceId
                                                                                                )
                                                                                        editedConsultation =
                                                                                                editedConsultation
                                                                                                        .copy(
                                                                                                                referencesMaladies =
                                                                                                                        nouvellesReferences
                                                                                                        )
                                                                                },
                                                                                imageVector = AppIcons.Delete,
                                                                                contentDescription = ConsultationEdit.REF_DELETE_TOOLTIP.translate(),
                                                                                tooltip = ConsultationEdit.REF_DELETE_TOOLTIP.translate(),
                                                                                tint = Color.Red
                                                                        )
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
                                                contentColor =
                                                        VetNutriColors.Primary
                                        )
                        ) {
                                Icon(
                                        AppIcons.Add,
                                        contentDescription =
                                                ConsultationEdit.REF_ADD_DISEASE.translate(),
                                        modifier =
                                                Modifier.size(
                                                        AppSizes.iconSizeSmall
                                                )
                                )
                                Spacer(
                                        modifier =
                                                Modifier.width(
                                                        AppSizes.paddingSmall
                                                )
                                )
                                Text(ConsultationEdit.REF_ADD_DISEASE.translate())
                        }
                }
        }
}

/** Éditeur de consultation : mots-clés de la consultation */
@Composable
internal fun ColumnScope.CarteMotsClesConsultation(
        editedConsultationState: MutableState<ConsultationEv>,
        keywordsById: Map<String, ConsultationKeyword>,
        showKeywordDialogState: MutableState<Boolean>
) {
        var editedConsultation by editedConsultationState
        var showKeywordDialog by showKeywordDialogState
        Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = AppSizes.elevationSmall,
                backgroundColor = VetNutriColors.Surface
        ) {
                Column(
                        modifier = Modifier.padding(AppSizes.paddingLarge),
                        verticalArrangement =
                                Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                        Text(
                                text = ConsultationEdit.KEYWORDS_SECTION_TITLE.translate(),
                                style = MaterialTheme.typography.h6,
                                color = VetNutriColors.Primary
                        )

                        Divider(color = VetNutriColors.Primary.copy(alpha = 0.3f))

                        if (editedConsultation.keywordIds.isNotEmpty()) {
                                Column(
                                        verticalArrangement =
                                                Arrangement.spacedBy(
                                                        AppSizes.paddingSmall
                                                )
                                ) {
                                        editedConsultation.keywordIds.forEach {
                                                keywordId ->
                                                val keywordLabel =
                                                        keywordsById[keywordId]
                                                                ?.label
                                                                ?: ConsultationEdit.KEYWORDS_UNKNOWN.translate()

                                                Card(
                                                        modifier =
                                                                Modifier.fillMaxWidth(),
                                                        backgroundColor =
                                                                VetNutriColors
                                                                        .Surface
                                                                        .copy(
                                                                                alpha =
                                                                                        0.7f
                                                                        ),
                                                        elevation = 2.dp
                                                ) {
                                                        Row(
                                                                modifier =
                                                                        Modifier.padding(
                                                                                AppSizes.paddingMedium
                                                                        ),
                                                                horizontalArrangement =
                                                                        Arrangement
                                                                                .SpaceBetween,
                                                                verticalAlignment =
                                                                        Alignment
                                                                                .CenterVertically
                                                        ) {
                                                                Text(
                                                                        text = keywordLabel,
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body1,
                                                                        fontWeight =
                                                                                FontWeight
                                                                                        .Bold,
                                                                        modifier =
                                                                                Modifier.weight(
                                                                                        1f
                                                                                )
                                                                )
                                                                IconButtonWithTooltip(
                                                                        onClick = {
                                                                                val updatedKeywords =
                                                                                        editedConsultation
                                                                                                .keywordIds
                                                                                                .toMutableList()
                                                                                updatedKeywords.remove(
                                                                                        keywordId
                                                                                )
                                                                                editedConsultation =
                                                                                        editedConsultation
                                                                                                .copy(
                                                                                                        keywordIds =
                                                                                                                updatedKeywords
                                                                                                )
                                                                        },
                                                                        imageVector = AppIcons.Delete,
                                                                        contentDescription = ConsultationEdit.KEYWORDS_DELETE_TOOLTIP.translate(),
                                                                        tooltip = ConsultationEdit.KEYWORDS_DELETE_TOOLTIP.translate(),
                                                                        tint = Color.Red
                                                                )
                                                        }
                                                }
                                        }
                                }
                        } else {
                                Text(
                                        text = ConsultationEdit.KEYWORDS_NONE.translate(),
                                        style = MaterialTheme.typography.body2,
                                        color = Color.Gray
                                )
                        }

                        OutlinedButton(
                                onClick = { showKeywordDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors =
                                        ButtonDefaults.outlinedButtonColors(
                                                contentColor =
                                                        VetNutriColors.Primary
                                        )
                        ) {
                                Icon(
                                        AppIcons.Add,
                                        contentDescription =
                                                ConsultationEdit.KEYWORDS_ADD.translate(),
                                        modifier =
                                                Modifier.size(
                                                        AppSizes.iconSizeSmall
                                                )
                                )
                                Spacer(
                                        modifier =
                                                Modifier.width(
                                                        AppSizes.paddingSmall
                                                )
                                )
                                Text(ConsultationEdit.KEYWORDS_ADD.translate())
                        }
                }
        }
}

/** Éditeur de consultation : poids idéal saisi et estimation d'après la note d'état corporel */
@Composable
internal fun ColumnScope.LignePoidsIdealConsultation(
        editedConsultationState: MutableState<ConsultationEv>,
        estimatedIdealWeight: Double?,
        idealWeightErrorMessageState: MutableState<String?>,
        idealWeightTextState: MutableState<String>,
        showIdealWeightErrorState: MutableState<Boolean>
) {
        var editedConsultation by editedConsultationState
        var idealWeightErrorMessage by idealWeightErrorMessageState
        var idealWeightText by idealWeightTextState
        var showIdealWeightError by showIdealWeightErrorState
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
                // Champ de saisie du poids idéal (grand clavier numérique)
                Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                                value = idealWeightText,
                                onValueChange = { newValue: String ->
                                        val texteFiltre =
                                                newValue.filter { char ->
                                                        char.isDigit() || char == '.' || char == ','
                                                }
                                        val pointCount = texteFiltre.count { it == '.' }
                                        val virguleCount = texteFiltre.count { it == ',' }
                                        if (pointCount <= 1 &&
                                                        virguleCount <= 1 &&
                                                        pointCount + virguleCount <= 1
                                        ) {
                                                idealWeightText = texteFiltre
                                                val texteNormalise = texteFiltre.replace(',', '.')
                                                try {
                                                        if (texteNormalise.isNotEmpty()) {
                                                                val idealWeight =
                                                                        texteNormalise.toDouble()
                                                                editedConsultation =
                                                                        editedConsultation.copy(
                                                                                idealWeight =
                                                                                        idealWeight
                                                                        )
                                                                showIdealWeightError = false
                                                                idealWeightErrorMessage = null
                                                        } else {
                                                                editedConsultation =
                                                                        editedConsultation.copy(
                                                                                idealWeight = null
                                                                        )
                                                                showIdealWeightError = false
                                                                idealWeightErrorMessage = null
                                                        }
                                                } catch (e: Exception) {
                                                        e.rethrowIfCancellation()
                                                        showIdealWeightError = true
                                                        idealWeightErrorMessage =
                                                                ConsultationEdit.FORMAT_IDEAL_WEIGHT_ERROR.translate()
                                                }
                                        }
                                },
                                label = { Text(ConsultationEdit.IDEAL_WEIGHT_LABEL.translate()) },
                                leadingIcon = {
                                        Icon(
                                                imageVector = AppIcons.Weight,
                                                contentDescription = null
                                        )
                                },
                                isError = showIdealWeightError,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions =
                                        KeyboardOptions(
                                                keyboardType = KeyboardType.Text,
                                                imeAction = ImeAction.Done
                                        ),
                                singleLine = true
                        )
                        val currentIdealWeightErrorMessage = idealWeightErrorMessage
                        if (showIdealWeightError && currentIdealWeightErrorMessage != null) {
                                Text(
                                        text = currentIdealWeightErrorMessage,
                                        color = MaterialTheme.colors.error,
                                        style = MaterialTheme.typography.caption,
                                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                )
                        }
                }

                // Affichage de l'estimation
                if (estimatedIdealWeight != null) {
                        Card(
                                modifier = Modifier.weight(1f).padding(top = 8.dp),
                                backgroundColor =
                                        VetNutriColors.Surface.copy(alpha = 0.7f),
                                elevation = 1.dp
                        ) {
                                Column(
                                        modifier =
                                                Modifier.padding(
                                                        AppSizes.paddingMedium
                                                ),
                                        horizontalAlignment =
                                                Alignment.CenterHorizontally
                                ) {
                                        Text(
                                                text = ConsultationEdit.ESTIMATION.translate(),
                                                style =
                                                        MaterialTheme.typography
                                                                .caption,
                                                color = Color.Gray
                                        )
                                        Text(
                                                text =
                                                        "${fr.vetbrain.vetnutri_mp.Utils.TextUtils.formatDecimal(estimatedIdealWeight, 2)} kg",
                                                style =
                                                        MaterialTheme.typography
                                                                .body1,
                                                fontWeight = FontWeight.Bold,
                                                color = VetNutriColors.Primary
                                        )
                                }
                        }
                } else {
                        Card(
                                modifier = Modifier.weight(1f).padding(top = 8.dp),
                                backgroundColor =
                                        VetNutriColors.Surface.copy(alpha = 0.3f),
                                elevation = 1.dp
                        ) {
                                Column(
                                        modifier =
                                                Modifier.padding(
                                                        AppSizes.paddingMedium
                                                ),
                                        horizontalAlignment =
                                                Alignment.CenterHorizontally
                                ) {
                                        Text(
                                                text = ConsultationEdit.ESTIMATION.translate(),
                                                style =
                                                        MaterialTheme.typography
                                                                .caption,
                                                color = Color.Gray
                                        )
                                        Text(
                                                text = ConsultationEdit.ESTIMATION_REQUIRES.translate(),
                                                style =
                                                        MaterialTheme.typography
                                                                .caption,
                                                color = Color.Gray
                                        )
                                }
                        }
                }
        }
}
