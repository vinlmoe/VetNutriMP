package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.NewReferenceEvViewModel

// Éditeur de référentiel : groupes de coefficients d'ajustement et leurs dialogues

/**
 * Vue pour afficher et gérer un groupe de coefficients spécifique
 *
 * @param viewModel ViewModel pour les opérations sur les références
 * @param groupIndex Index du groupe de coefficients (0-4)
 * @param onAddCoefficient Callback pour ajouter un coefficient
 * @param onEditCoefficient Callback pour éditer un coefficient
 */
@Composable
fun CoefficientGroupView(
        viewModel: NewReferenceEvViewModel,
        groupIndex: Int,
        onAddCoefficient: () -> Unit,
        onEditCoefficient: (Int) -> Unit
) {
        // Observer directement les StateFlow des coefficients pour une réactivité immédiate
        val coefficients by viewModel.getCoefficientGroupStateFlow(groupIndex).collectAsState()
        val groupNames by viewModel.groupNames.collectAsState()

        // État local pour l'édition du nom du groupe
        var isEditingGroupName by remember { mutableStateOf(false) }
        var editingGroupName by remember { mutableStateOf("") }

        Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
                // En-tête avec nom du groupe éditable
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        if (isEditingGroupName) {
                                OutlinedTextField(
                                        value = editingGroupName,
                                        onValueChange = { editingGroupName = it },
                                        label = { Text(translate(LocalizationKeys.NewReference.GROUP_NAME_LABEL)) },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions =
                                                KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions =
                                                KeyboardActions(
                                                        onDone = {
                                                                viewModel
                                                                        .updateCoefficientGroupName(
                                                                                groupIndex,
                                                                                editingGroupName
                                                                        )
                                                                isEditingGroupName = false
                                                        }
                                                )
                                )
                                TextButton(
                                        onClick = {
                                                viewModel.updateCoefficientGroupName(
                                                        groupIndex,
                                                        editingGroupName
                                                )
                                                isEditingGroupName = false
                                        }
                                ) { Text(translate(LocalizationKeys.General.OK)) }
                        } else {
                                val groupName =
                                        groupNames.getOrElse(groupIndex) {
                                                "Groupe ${groupIndex + 1}"
                                        }
                                Text(
                                        text = groupName.ifEmpty { "Groupe ${groupIndex + 1}" },
                                        style = MaterialTheme.typography.h6,
                                        modifier =
                                                Modifier.weight(1f).clickable {
                                                        editingGroupName = groupName
                                                        isEditingGroupName = true
                                                }
                                )
                                IconButton(
                                        onClick = {
                                                editingGroupName = groupName
                                                isEditingGroupName = true
                                        }
                                ) { Icon(Icons.Default.Edit, contentDescription = translate(LocalizationKeys.NewReference.EDIT_GROUP_NAME)) }
                        }
                }

                // Bouton ajouter coefficient
                OutlinedButton(onClick = onAddCoefficient, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(translate(LocalizationKeys.NewReference.ADD_COEF))
                }

                // Liste des coefficients
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        itemsIndexed(coefficients) { index, coefficient ->
                                CoefficientCard(
                                        coefficient = coefficient,
                                        onEdit = { onEditCoefficient(index) },
                                        onDelete = {
                                                viewModel.removeCoefficient(groupIndex, index)
                                        }
                                )
                        }
                }
        }
}

/**
 * Carte affichant un coefficient individuel
 *
 * @param coefficient Le coefficient à afficher
 * @param onEdit Callback pour éditer le coefficient
 */
@Composable
fun CoefficientCard(
        coefficient: fr.vetbrain.vetnutri_mp.Data.CoefP,
        onEdit: () -> Unit,
        onDelete: () -> Unit
) {
        Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp,
                backgroundColor = MaterialTheme.colors.surface
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        // Informations du coefficient
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        text = coefficient.description ?: translate(LocalizationKeys.NewReference.COEF_UNNAMED),
                                        style = MaterialTheme.typography.subtitle2,
                                        fontWeight = FontWeight.Bold
                                )
                                Text(
                                        text = translate(LocalizationKeys.NewReference.COEF_VALUE_LABEL, (coefficient.coef ?: 1.0).toString()),
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                        }

                        // Bouton d'édition
                        IconButton(onClick = onEdit) {
                                Icon(
                                        Icons.Default.Edit,
                                        contentDescription = translate("general.edit"),
                                        tint = VetNutriColors.Primary
                                )
                        }

                        // Bouton de suppression
                        IconButton(onClick = onDelete) {
                                Icon(
                                        Icons.Default.Delete,
                                        contentDescription = translate("general.delete"),
                                        tint = MaterialTheme.colors.error
                                )
                        }
                }
        }
}

/**
 * Boîte de dialogue pour ajouter un nouveau coefficient
 *
 * @param onDismiss Callback pour fermer la boîte de dialogue
 * @param onConfirm Callback pour confirmer l'ajout avec description et coefficient
 */
@Composable
fun AddCoefficientDialog(onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
        var description by remember { mutableStateOf("") }
        var coefText by remember { mutableStateOf("1.0") }
        var showError by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }

        // Fonction de validation
        fun validateAndConfirm() {
                if (description.isBlank()) {
                        showError = true
                        errorMessage = "La description ne peut pas être vide"
                        return
                }

                val coef = coefText.toDoubleOrNull()
                if (coef == null) {
                        showError = true
                        errorMessage = "Le coefficient doit être un nombre valide"
                        return
                }

                showError = false
                onConfirm(description.trim(), coef)
        }

        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(text = translate(LocalizationKeys.NewReference.ADD_COEF_TITLE)) },
                text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                        value = description,
                                        onValueChange = {
                                                description = it
                                                showError = false
                                        },
                                        label = { Text(translate(LocalizationKeys.NewReference.FIELD_DESCRIPTION)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = showError && description.isBlank()
                                )

                                OutlinedTextField(
                                        value = coefText,
                                        onValueChange = {
                                                coefText = it
                                                showError = false
                                        },
                                        label = { Text(translate("consultation.edit.coef.selector_label")) },
                                        keyboardOptions =
                                                KeyboardOptions(
                                                        keyboardType = KeyboardType.Decimal
                                                ),
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = showError && coefText.toDoubleOrNull() == null
                                )

                                if (showError) {
                                        Text(
                                                text = errorMessage,
                                                color = MaterialTheme.colors.error,
                                                style = MaterialTheme.typography.caption
                                        )
                                }
                        }
                },
                confirmButton = { Button(onClick = { validateAndConfirm() }) { Text(translate("general.add")) } },
                dismissButton = {
                        Button(
                                onClick = onDismiss,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = MaterialTheme.colors.surface
                                        )
                        ) { Text(translate("general.cancel")) }
                }
        )
}

/**
 * Boîte de dialogue pour éditer ou supprimer un coefficient existant
 *
 * @param initialDescription Description initiale du coefficient
 * @param initialCoef Valeur initiale du coefficient
 * @param onDismiss Callback pour fermer la boîte de dialogue
 * @param onConfirm Callback pour confirmer les modifications
 * @param onDelete Callback pour supprimer le coefficient
 */
@Composable
fun EditCoefficientDialog(
        initialDescription: String,
        initialCoef: Double,
        onDismiss: () -> Unit,
        onConfirm: (String, Double) -> Unit,
        onDelete: () -> Unit
) {
        var description by remember { mutableStateOf(initialDescription) }
        var coefText by remember { mutableStateOf(initialCoef.toString()) }
        var showError by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }
        var showDeleteConfirmation by remember { mutableStateOf(false) }

        // Fonction de validation
        fun validateAndConfirm() {
                if (description.isBlank()) {
                        showError = true
                        errorMessage = "La description ne peut pas être vide"
                        return
                }

                val coef = coefText.toDoubleOrNull()
                if (coef == null) {
                        showError = true
                        errorMessage = "Le coefficient doit être un nombre valide"
                        return
                }

                showError = false
                onConfirm(description.trim(), coef)
        }

        if (showDeleteConfirmation) {
                AlertDialog(
                        onDismissRequest = { showDeleteConfirmation = false },
                        title = { Text(translate("new_reference.confirm_delete_title")) },
                        text = { Text(translate("new_reference.confirm_delete_text")) },
                        confirmButton = {
                                Button(
                                        onClick = {
                                                showDeleteConfirmation = false
                                                onDelete()
                                        },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = MaterialTheme.colors.error
                                                )
                                ) { Text(translate("new_reference.button_delete_coef"), color = Color.White) }
                        },
                        dismissButton = {
                                Button(onClick = { showDeleteConfirmation = false }) {
                                        Text(translate("general.cancel"))
                                }
                        }
                )
        } else {
                AlertDialog(
                        onDismissRequest = onDismiss,
                        title = { Text(text = translate("new_reference.edit_coef_title")) },
                        text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                                value = description,
                                                onValueChange = {
                                                        description = it
                                                        showError = false
                                                },
                                                label = { Text(translate(LocalizationKeys.NewReference.FIELD_DESCRIPTION)) },
                                                modifier = Modifier.fillMaxWidth(),
                                                isError = showError && description.isBlank()
                                        )

                                        OutlinedTextField(
                                                value = coefText,
                                                onValueChange = {
                                                        coefText = it
                                                        showError = false
                                                },
                                                label = { Text(translate("consultation.edit.coef.selector_label")) },
                                                keyboardOptions =
                                                        KeyboardOptions(
                                                                keyboardType = KeyboardType.Decimal
                                                        ),
                                                modifier = Modifier.fillMaxWidth(),
                                                isError =
                                                        showError &&
                                                                coefText.toDoubleOrNull() == null
                                        )

                                        if (showError) {
                                                Text(
                                                        text = errorMessage,
                                                        color = MaterialTheme.colors.error,
                                                        style = MaterialTheme.typography.caption
                                                )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Bouton de suppression
                                        Button(
                                                onClick = { showDeleteConfirmation = true },
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                backgroundColor =
                                                                        MaterialTheme.colors.error
                                                        ),
                                                modifier = Modifier.fillMaxWidth()
                                        ) { Text(translate("new_reference.button_delete_coef"), color = Color.White) }
                                }
                        },
                        confirmButton = {
                                Button(onClick = { validateAndConfirm() }) { Text(translate("auto.view.settingsview.enregistrer")) }
                        },
                        dismissButton = {
                                Button(
                                        onClick = onDismiss,
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor =
                                                                MaterialTheme.colors.surface
                                                )
                                ) { Text(translate("general.cancel")) }
                        }
                )
        }
}
