package fr.vetbrain.vetnutri_mp.View

import fr.vetbrain.vetnutri_mp.Components.AppAlertDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import fr.vetbrain.vetnutri_mp.Data.ConsultationKeyword
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.ConsultationEdit
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

// Éditeur de consultation : sélecteurs (score, coefficients), choix des référentiels et mots-clés, variables

@Composable
internal fun ScoreSelector(
        label: String,
        valeurSelectionnee: Double?,
        onScoreSelected: (Double?) -> Unit,
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
                                        "$valeurSelectionnee/9 - ${descriptions[valeurSelectionnee.toInt()] ?: ConsultationEdit.REF_DESCRIPTION_NONE.translate()}"
                                } else {
                                        ConsultationEdit.BCS_NONE_SELECTED.translate()
                                },
                        onValueChange = {},
                        label = { Text(ConsultationEdit.BCS_FIELD_LABEL.translate()) },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                                Row {
                                        if (valeurSelectionnee != null) {
                                                IconButtonWithTooltip(
                                                        onClick = { onScoreSelected(null) },
                                                        imageVector = AppIcons.Close,
                                                        contentDescription = ConsultationEdit.BCS_CLEAR_TOOLTIP.translate(),
                                                        tooltip = ConsultationEdit.BCS_CLEAR_TOOLTIP.translate(),
                                                        tint = Color.Gray
                                                )
                                        }
                                        IconButtonWithTooltip(
                                                onClick = { showDialog = true },
                                                imageVector = AppIcons.ArrowDropDown,
                                                contentDescription = ConsultationEdit.BCS_SELECT_TOOLTIP.translate(),
                                                tooltip = ConsultationEdit.BCS_SELECT_TOOLTIP.translate()
                                        )
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
        scoreSelectionne: Double?,
        onScoreSelected: (Double) -> Unit,
        onDismiss: () -> Unit
) {
        AppAlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(ConsultationEdit.BCS_DIALOG_TITLE.translate(label)) },
                text = {
                        LazyColumn {
                                items(plageScore.toList()) { score ->
                                        Row(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .padding(vertical = 4.dp)
                                                                .clickable {
                                                                        onScoreSelected(
                                                                                score.toDouble()
                                                                        )
                                                                },
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                RadioButton(
                                                        selected =
                                                                scoreSelectionne ==
                                                                        score.toDouble(),
                                                        onClick = {
                                                                onScoreSelected(score.toDouble())
                                                        }
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
                                                                text = descriptions[score]
                                                                                ?: ConsultationEdit.REF_DESCRIPTION_NONE.translate(),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body2,
                                                                color = Color.Gray
                                                        )
                                                }
                                        }
                                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                                }
                        }
                },
                confirmButton = { TextButton(onClick = onDismiss) { Text(LocalizationKeys.General.CLOSE.translate()) } }
        )
}

@Composable
internal fun CoefficientSelector(
        nom: String,
        valeurSelectionnee: Double?,
        descriptionSelectionnee: String?,
        coefficients: List<fr.vetbrain.vetnutri_mp.Data.CoefP>,
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
                                        "$descriptionSelectionnee (${fr.vetbrain.vetnutri_mp.Utils.TextUtils.formatDecimal(valeurSelectionnee.toDouble(), 2)})"
                                } else {
                                        ConsultationEdit.COEF_SELECTOR_NONE.translate()
                                },
                        onValueChange = {},
                        label = { Text(ConsultationEdit.COEF_SELECTOR_LABEL.translate()) },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                                IconButtonWithTooltip(
                                        onClick = { showDialog = true },
                                        imageVector = AppIcons.ArrowDropDown,
                                        contentDescription = ConsultationEdit.COEF_SELECTOR_NONE.translate(),
                                        tooltip = ConsultationEdit.COEF_SELECTOR_NONE.translate()
                                )
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
        coefficients: List<fr.vetbrain.vetnutri_mp.Data.CoefP>,
        descriptionSelectionnee: String?,
        onCoefficientSelected: (fr.vetbrain.vetnutri_mp.Data.CoefP) -> Unit,
        onDismiss: () -> Unit
) {
        AppAlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(ConsultationEdit.BCS_DIALOG_TITLE.translate(nom)) },
                text = {
                        LazyColumn {
                                items(coefficients) { coef ->
                                        Row(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                RadioButton(
                                                        selected =
                                                                descriptionSelectionnee ==
                                                                        coef.description,
                                                        onClick = { onCoefficientSelected(coef) }
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                        Text(
                                                                text = coef.description
                                                                                ?: ConsultationEdit.REF_COEF_NO_DESC.translate(),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body1
                                                        )
                                                        Text(
                                                                text =
                                                                        ConsultationEdit.COEF_VAL_LABEL.translate(coef.coef?.toString() ?: "0.0"),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body2,
                                                                color = Color.Gray
                                                        )
                                                }
                                        }
                                }
                        }
                },
                confirmButton = { TextButton(onClick = onDismiss) { Text(LocalizationKeys.General.CLOSE.translate()) } }
        )
}

@Composable
internal fun ReferenceGeneraleDialog(
        value: String,
        onValueChange: (String) -> Unit,
        availableReferences: List<fr.vetbrain.vetnutri_mp.Data.ReferenceEv>
) {
        val referencesGenerales = availableReferences.filter { !it.maladie }
        var searchText by remember { mutableStateOf("") }

        // Filtrer les références selon le texte de recherche
        val filteredReferences =
                remember(referencesGenerales, searchText) {
                        if (searchText.isBlank()) {
                                referencesGenerales
                        } else {
                                referencesGenerales.filter { reference ->
                                        reference.nom.contains(searchText, ignoreCase = true) ||
                                                reference.description?.contains(
                                                        searchText,
                                                        ignoreCase = true
                                                ) == true
                                }
                        }
                }

        AppAlertDialog(
                onDismissRequest = { onValueChange(value) },
                title = { Text(ConsultationEdit.REF_GENERAL_DIALOG_TITLE.translate()) },
                text = {
                        Column(modifier = Modifier.width(500.dp).height(400.dp)) {
                                // Barre de recherche
                                OutlinedTextField(
                                        value = searchText,
                                        onValueChange = { searchText = it },
                                        label = { Text(ConsultationEdit.REF_SEARCH_LABEL.translate()) },
                                        leadingIcon = {
                                                Icon(
                                                        AppIcons.Search,
                                                        contentDescription = ConsultationEdit.REF_SEARCH_TOOLTIP.translate()
                                                )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                        text = ConsultationEdit.REF_SEARCH_RESULTS.translate(filteredReferences.size.toString()),
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Liste scrollable des références
                                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                        // Option pour aucune référence
                                        item {
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .clickable { onValueChange("") }
                                                                        .padding(4.dp),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        RadioButton(
                                                                selected = value.isEmpty(),
                                                                onClick = { onValueChange("") }
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                                text = ConsultationEdit.REF_NONE.translate(),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body1,
                                                                fontWeight = FontWeight.Bold
                                                        )
                                                }
                                        }

                                        // Toutes les références filtrées
                                        items(filteredReferences) { reference ->
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .clickable { onValueChange(reference.uuid) }
                                                                        .padding(4.dp),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        RadioButton(
                                                                selected = value == reference.uuid,
                                                                onClick = {
                                                                        onValueChange(
                                                                                reference.uuid
                                                                        )
                                                                }
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                        text =
                                                                                reference.nom
                                                                                        .ifBlank {
                                                                                                ConsultationEdit.REF_NO_NAME.translate()
                                                                                        },
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body1,
                                                                        fontWeight =
                                                                                FontWeight.Medium
                                                                )
                                                                if (!reference.description
                                                                                .isNullOrBlank()
                                                                ) {
                                                                        Text(
                                                                                text =
                                                                                        reference
                                                                                                .description,
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .body2,
                                                                                color = Color.Gray,
                                                                                maxLines = 2
                                                                        )
                                                                }
                                                                Text(
                                                                        text =
                                                                                ConsultationEdit.REF_SPECIES_LABEL.translate(reference.espece?.name ?: ConsultationEdit.REF_SPECIES_UNSPECIFIED.translate()),
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .caption,
                                                                        color =
                                                                                VetNutriColors
                                                                                        .Primary
                                                                )
                                                        }
                                                }
                                                Divider(color = Color.LightGray.copy(alpha = 0.3f))
                                        }
                                }
                        }
                },
                confirmButton = {
                        TextButton(onClick = { onValueChange(value) }) { Text(LocalizationKeys.General.CLOSE.translate()) }
                }
        )
}

@Composable
internal fun ReferenceMaladieDialog(
        references: List<String>,
        availableReferences: List<fr.vetbrain.vetnutri_mp.Data.ReferenceEv>,
        onReferenceSelected: (List<String>) -> Unit,
        onReferenceRemoved: (List<String>) -> Unit,
        onDismiss: () -> Unit
) {
        // Les références arrivent déjà filtrées par espèce et type complémentaire
        var searchText by remember { mutableStateOf("") }

        // Filtrer les références selon le texte de recherche
        val filteredReferences =
                remember(availableReferences, searchText) {
                        if (searchText.isBlank()) {
                                availableReferences
                        } else {
                                availableReferences.filter { reference ->
                                        reference.nom.contains(searchText, ignoreCase = true) ||
                                                reference.description?.contains(
                                                        searchText,
                                                        ignoreCase = true
                                                ) == true
                                }
                        }
                }

        AppAlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(ConsultationEdit.REF_DISEASE_DIALOG_TITLE.translate()) },
                text = {
                        Column(modifier = Modifier.width(500.dp).height(400.dp)) {
                                // Barre de recherche
                                OutlinedTextField(
                                        value = searchText,
                                        onValueChange = { searchText = it },
                                        label = { Text(ConsultationEdit.REF_DISEASE_SEARCH_LABEL.translate()) },
                                        leadingIcon = {
                                                Icon(
                                                        AppIcons.Search,
                                                        contentDescription = ConsultationEdit.REF_SEARCH_TOOLTIP.translate()
                                                )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                        text =
                                                ConsultationEdit.REF_DISEASE_SEARCH_RESULTS.translate(filteredReferences.size.toString()),
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Liste scrollable des références
                                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                        items(filteredReferences) { reference ->
                                                val isSelected = references.contains(reference.uuid)
                                                val onCheckedChange = { checked: Boolean ->
                                                        val updatedReferences =
                                                                if (checked) {
                                                                        references + reference.uuid
                                                                } else {
                                                                        references - reference.uuid
                                                                }
                                                        onReferenceSelected(updatedReferences)
                                                }

                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .clickable { onCheckedChange(!isSelected) }
                                                                        .padding(4.dp),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Checkbox(
                                                                checked = isSelected,
                                                                onCheckedChange = onCheckedChange
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                        text =
                                                                                reference.nom
                                                                                        .ifBlank {
                                                                                                ConsultationEdit.REF_NO_NAME.translate()
                                                                                        },
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body1,
                                                                        fontWeight =
                                                                                FontWeight.Medium
                                                                )
                                                                if (!reference.description
                                                                                .isNullOrBlank()
                                                                ) {
                                                                        Text(
                                                                                text =
                                                                                        reference
                                                                                                .description,
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .body2,
                                                                                color = Color.Gray,
                                                                                maxLines = 2
                                                                        )
                                                                }
                                                                Text(
                                                                        text =
                                                                                ConsultationEdit.REF_SPECIES_LABEL.translate(reference.espece?.name ?: ConsultationEdit.REF_SPECIES_UNSPECIFIED.translate()),
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .caption,
                                                                        color =
                                                                                VetNutriColors
                                                                                        .Secondary
                                                                )
                                                        }
                                                }
                                                Divider(color = Color.LightGray.copy(alpha = 0.3f))
                                        }
                                }
                        }
                },
                confirmButton = {
                        TextButton(onClick = onDismiss) {
                                Text(LocalizationKeys.General.CLOSE.translate())
                        }
                }
        )
}

@Composable
internal fun ConsultationKeywordDialog(
        selectedKeywordIds: List<String>,
        availableKeywords: List<ConsultationKeyword>,
        onKeywordsSelected: (List<String>) -> Unit,
        onKeywordCreated: (ConsultationKeyword) -> Unit,
        onDismiss: () -> Unit
) {
        var searchText by remember { mutableStateOf("") }
        val trimmedSearch = searchText.trim()

        val filteredKeywords =
                remember(availableKeywords, searchText) {
                        if (searchText.isBlank()) {
                                availableKeywords
                        } else {
                                availableKeywords.filter { keyword ->
                                        keyword.label.contains(searchText, ignoreCase = true)
                                }
                        }
                }

        val existingKeyword =
                remember(availableKeywords, trimmedSearch) {
                        if (trimmedSearch.isBlank()) {
                                null
                        } else {
                                availableKeywords.firstOrNull {
                                        it.label.equals(trimmedSearch, ignoreCase = true)
                                }
                        }
                }

        AppAlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(ConsultationEdit.KEYWORDS_DIALOG_TITLE.translate()) },
                text = {
                        Column(modifier = Modifier.width(500.dp).height(420.dp)) {
                                OutlinedTextField(
                                        value = searchText,
                                        onValueChange = { searchText = it },
                                        label = { Text(ConsultationEdit.KEYWORDS_SEARCH_LABEL.translate()) },
                                        leadingIcon = {
                                                Icon(
                                                        AppIcons.Search,
                                                        contentDescription = ConsultationEdit.KEYWORDS_SEARCH_TOOLTIP.translate()
                                                )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                )

                                if (trimmedSearch.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        if (existingKeyword == null) {
                                                OutlinedButton(
                                                        onClick = {
                                                                val newKeyword =
                                                                        ConsultationKeyword(
                                                                                label = trimmedSearch
                                                                        )
                                                                onKeywordCreated(newKeyword)
                                                        },
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
                                                                        ConsultationEdit.KEYWORDS_ADD_NEW.translate(
                                                                                trimmedSearch
                                                                        ),
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
                                                        Text(
                                                                ConsultationEdit.KEYWORDS_ADD_NEW.translate(
                                                                        trimmedSearch
                                                                )
                                                        )
                                                }
                                        } else {
                                                OutlinedButton(
                                                        onClick = {
                                                                onKeywordsSelected(
                                                                        (selectedKeywordIds +
                                                                                        existingKeyword.uuid)
                                                                                .distinct()
                                                                )
                                                        },
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
                                                                        ConsultationEdit.KEYWORDS_ADD_EXISTING.translate(
                                                                                existingKeyword.label
                                                                        ),
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
                                                        Text(
                                                                ConsultationEdit.KEYWORDS_ADD_EXISTING.translate(
                                                                        existingKeyword.label
                                                                )
                                                        )
                                                }
                                        }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                        text =
                                                ConsultationEdit.KEYWORDS_SEARCH_RESULTS.translate(
                                                        filteredKeywords.size.toString()
                                                ),
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                        items(filteredKeywords) { keyword ->
                                                val isSelected =
                                                        selectedKeywordIds.contains(keyword.uuid)

                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(4.dp),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Checkbox(
                                                                checked = isSelected,
                                                                onCheckedChange = { checked ->
                                                                        val updatedKeywords =
                                                                                if (checked) {
                                                                                        selectedKeywordIds +
                                                                                                keyword
                                                                                                        .uuid
                                                                                } else {
                                                                                        selectedKeywordIds -
                                                                                                keyword
                                                                                                        .uuid
                                                                                }
                                                                        onKeywordsSelected(
                                                                                updatedKeywords
                                                                        )
                                                                }
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                                text = keyword.label,
                                                                style = MaterialTheme.typography.body1,
                                                                fontWeight = FontWeight.Medium
                                                        )
                                                }
                                                Divider(color = Color.LightGray.copy(alpha = 0.3f))
                                        }
                                }
                        }
                },
                confirmButton = {
                        TextButton(onClick = onDismiss) {
                                Text(LocalizationKeys.General.CLOSE.translate())
                        }
                }
        )
}

@Composable
internal fun VariableSupplementaireField(
        variable: fr.vetbrain.vetnutri_mp.Enumer.VariableKind,
        valeurActuelle: Double?,
        onValeurChange: (Double?) -> Unit,
        modifier: Modifier = Modifier
) {
        var textValue by
                remember(valeurActuelle) { mutableStateOf(valeurActuelle?.toString() ?: "") }
        var isError by remember { mutableStateOf(false) }

        Card(
                modifier = modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 1.dp
        ) {
                Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                        // En-tête avec le nom de la variable
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text = variable.label,
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight = FontWeight.Medium,
                                        color = VetNutriColors.Primary
                                )
                                Text(
                                        text = "(${variable.variable})",
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Gray
                                )
                        }

                        // Description de la variable
                        getVariableDescription(variable)?.let { description ->
                                Text(
                                        text = description,
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                )
                        }

                        // Champ de saisie (grand clavier numérique)
                        OutlinedTextField(
                                value = textValue,
                                onValueChange = { newValue ->
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
                                                textValue = texteFiltre
                                                try {
                                                        val texteNormalise = texteFiltre.replace(',', '.')
                                                        when {
                                                                texteNormalise.isEmpty() -> {
                                                                        onValeurChange(null)
                                                                        isError = false
                                                                }
                                                                texteNormalise.toDoubleOrNull() != null -> {
                                                                        val doubleValue =
                                                                                texteNormalise.toDouble()
                                                                        if (doubleValue >= 0) {
                                                                                onValeurChange(doubleValue)
                                                                                isError = false
                                                                        } else {
                                                                                isError = true
                                                                        }
                                                                }
                                                                else -> {
                                                                        isError = true
                                                                }
                                                        }
                                                } catch (e: NumberFormatException) {
                                                        isError = true
                                                }
                                        }
                                },
                                label = { Text(ConsultationEdit.VAR_VALUE_LABEL.translate()) },
                                placeholder = { Text(ConsultationEdit.VAR_VALUE_PLACEHOLDER.translate()) },
                                leadingIcon = {
                                        Icon(
                                                imageVector = AppIcons.Analysis,
                                                contentDescription = null,
                                                tint =
                                                        if (isError) MaterialTheme.colors.error
                                                        else VetNutriColors.Primary
                                        )
                                },
                                isError = isError,
                                modifier = Modifier.fillMaxWidth(),
                                colors =
                                        TextFieldDefaults.outlinedTextFieldColors(
                                                focusedBorderColor = VetNutriColors.Primary,
                                                focusedLabelColor = VetNutriColors.Primary
                                        ),
                                keyboardOptions =
                                        KeyboardOptions(
                                                keyboardType = KeyboardType.Text,
                                                imeAction = ImeAction.Done
                                        ),
                                singleLine = true
                        )
                }

                // (supprimé) pas de dialog ici; géré au niveau supérieur de l'écran
        }
}

/** Fonction pour obtenir une description de la variable */
private fun getVariableDescription(variable: fr.vetbrain.vetnutri_mp.Enumer.VariableKind): String? {
        return when (variable) {
                fr.vetbrain.vetnutri_mp.Enumer.VariableKind.AdultWeight ->
                        fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Variables.DESC_ADULT_WEIGHT.translate()
                fr.vetbrain.vetnutri_mp.Enumer.VariableKind.LitterSize ->
                        fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Variables.DESC_LITTER_SIZE.translate()
                fr.vetbrain.vetnutri_mp.Enumer.VariableKind.WeekGestation ->
                        fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Variables.DESC_WEEK_GESTATION.translate()
                fr.vetbrain.vetnutri_mp.Enumer.VariableKind.WeekLactation ->
                        fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Variables.DESC_WEEK_LACTATION.translate()
                fr.vetbrain.vetnutri_mp.Enumer.VariableKind.BEE ->
                        fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Variables.DESC_BEE.translate()
                fr.vetbrain.vetnutri_mp.Enumer.VariableKind.BE -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Variables.DESC_BE.translate()
                fr.vetbrain.vetnutri_mp.Enumer.VariableKind.iBW -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Variables.DESC_IBW.translate()
                fr.vetbrain.vetnutri_mp.Enumer.VariableKind.MW -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Variables.DESC_MW.translate()
                else -> null
        }
}
