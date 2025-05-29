package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun AppConsultationDetailView(
        consultation: ConsultationEv?,
        onDismiss: () -> Unit,
        onSave: (ConsultationEv) -> Unit
) {
        var editedConsultation by remember { mutableStateOf(consultation ?: ConsultationEv()) }
        var weightText by remember { mutableStateOf(editedConsultation.weight?.toString() ?: "") }
        var showDateError by remember { mutableStateOf(false) }
        var showWeightError by remember { mutableStateOf(false) }
        var dateErrorMessage by remember { mutableStateOf<String?>(null) }
        var weightErrorMessage by remember { mutableStateOf<String?>(null) }

        Column(
                modifier = Modifier.padding(AppSizes.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
                // Titre
                Text(
                        text =
                                if (consultation == null || consultation.uuid.isEmpty())
                                        General.ADD.translate()
                                else General.EDIT.translate(),
                        style = MaterialTheme.typography.h6
                )

                Divider(
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                        thickness = AppSizes.dividerHeight
                )

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
                                                editedConsultation =
                                                        editedConsultation.copy(weight = weight)
                                                showWeightError = false
                                                weightErrorMessage = null
                                        } else {
                                                editedConsultation =
                                                        editedConsultation.copy(weight = null)
                                                showWeightError = false
                                                weightErrorMessage = null
                                        }
                                } catch (e: Exception) {
                                        showWeightError = true
                                        weightErrorMessage =
                                                "Format de poids invalide (nombre décimal)"
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
                                editedConsultation =
                                        editedConsultation.copy(objectConsult = newValue)
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
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        maxLines = 5,
                        singleLine = false
                )

                // Liste des rations
                Text(text = "Rations", style = MaterialTheme.typography.subtitle1)

                LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                        items(editedConsultation.rations) { ration ->
                                RationCard(
                                        ration = ration,
                                        onEdit = { /* TODO: Implémenter l'édition de ration */},
                                        onDelete = { editedConsultation.rations.remove(ration) }
                                )
                        }
                }

                Divider(
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                        thickness = AppSizes.dividerHeight
                )

                Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

                // Section Références Nutritionnelles
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = AppSizes.elevationSmall,
                        backgroundColor = VetNutriColors.Surface
                ) {
                        Column(
                                modifier = Modifier.padding(AppSizes.paddingMedium),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
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
                                        style = MaterialTheme.typography.subtitle2,
                                        fontWeight = FontWeight.Bold
                                )

                                // TODO: Ajouter un dropdown pour sélectionner la référence générale
                                OutlinedTextField(
                                        value = editedConsultation.referenceGeneraleId
                                                        ?: "Aucune référence sélectionnée",
                                        onValueChange = {},
                                        label = { Text("Référence générale") },
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                                IconButton(
                                                        onClick = {
                                                                // TODO: Ouvrir le sélecteur de
                                                                // références
                                                        }
                                                ) {
                                                        Icon(
                                                                AppIcons.ArrowDropDown,
                                                                contentDescription =
                                                                        "Sélectionner une référence"
                                                        )
                                                }
                                        }
                                )

                                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                                // Références de maladies
                                Text(
                                        text = "Références de maladies",
                                        style = MaterialTheme.typography.subtitle2,
                                        fontWeight = FontWeight.Bold
                                )

                                if (editedConsultation.referencesMaladies.isEmpty()) {
                                        Text(
                                                text = "Aucune référence de maladie",
                                                style = MaterialTheme.typography.body2,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(AppSizes.paddingSmall)
                                        )
                                } else {
                                        editedConsultation.referencesMaladies.forEach { referenceId
                                                ->
                                                Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        elevation = 2.dp,
                                                        backgroundColor =
                                                                VetNutriColors.Secondary.copy(
                                                                        alpha = 0.1f
                                                                )
                                                ) {
                                                        Row(
                                                                modifier =
                                                                        Modifier.padding(
                                                                                AppSizes.paddingSmall
                                                                        ),
                                                                horizontalArrangement =
                                                                        Arrangement.SpaceBetween,
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                Text(
                                                                        text =
                                                                                "Référence: $referenceId",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body2,
                                                                        modifier =
                                                                                Modifier.weight(1f)
                                                                )
                                                                IconButton(
                                                                        onClick = {
                                                                                editedConsultation
                                                                                        .supprimerReferenceMaladie(
                                                                                                referenceId
                                                                                        )
                                                                        }
                                                                ) {
                                                                        Icon(
                                                                                AppIcons.Delete,
                                                                                contentDescription =
                                                                                        "Supprimer la référence",
                                                                                tint = Color.Red
                                                                        )
                                                                }
                                                        }
                                                }
                                        }
                                }

                                // Bouton pour ajouter une référence de maladie
                                OutlinedButton(
                                        onClick = {
                                                // TODO: Ouvrir le sélecteur de références de
                                                // maladies
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors =
                                                ButtonDefaults.outlinedButtonColors(
                                                        contentColor = VetNutriColors.Primary
                                                )
                                ) {
                                        Icon(
                                                AppIcons.Add,
                                                contentDescription =
                                                        "Ajouter une référence de maladie",
                                                modifier = Modifier.size(AppSizes.iconSizeSmall)
                                        )
                                        Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                                        Text("Ajouter une référence de maladie")
                                }
                        }
                }

                Spacer(modifier = Modifier.height(AppSizes.paddingLarge))

                // Boutons d'action
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                        Button(
                                onClick = onDismiss,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Secondary,
                                                contentColor = VetNutriColors.OnSecondary
                                        ),
                                modifier = Modifier.weight(1f)
                        ) {
                                Row(
                                        horizontalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingSmall),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Icon(
                                                AppIcons.Cancel,
                                                contentDescription = null,
                                                modifier = Modifier.size(AppSizes.iconSizeSmall)
                                        )
                                        Text(General.CANCEL.translate())
                                }
                        }

                        Button(
                                onClick = {
                                        if (!showDateError &&
                                                        !showWeightError &&
                                                        editedConsultation.date != null
                                        ) {
                                                // S'assurer que l'UUID est généré si c'est une
                                                // nouvelle consultation
                                                if (editedConsultation.uuid.isEmpty()) {
                                                        editedConsultation =
                                                                editedConsultation.copy(
                                                                        uuid =
                                                                                kotlin.uuid.Uuid
                                                                                        .random()
                                                                                        .toString()
                                                                )
                                                }
                                                onSave(editedConsultation)
                                        } else if (editedConsultation.date == null) {
                                                showDateError = true
                                                dateErrorMessage = "La date est obligatoire"
                                        }
                                },
                                enabled = !showDateError && !showWeightError,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Primary,
                                                contentColor = VetNutriColors.OnPrimary
                                        ),
                                modifier = Modifier.weight(1f)
                        ) {
                                Row(
                                        horizontalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingSmall),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Icon(
                                                AppIcons.Save,
                                                contentDescription = null,
                                                modifier = Modifier.size(AppSizes.iconSizeSmall)
                                        )
                                        Text(General.SAVE.translate())
                                }
                        }
                }
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
                                        Text(
                                                text = ration.name,
                                                style = MaterialTheme.typography.subtitle1
                                        )
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
