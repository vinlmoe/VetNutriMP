package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Consultation
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalUuidApi::class)
@Composable
fun ConsultationDetailView(
        consultation: ConsultationEv?,
        onDismiss: () -> Unit,
        onSave: (ConsultationEv) -> Unit
) {
    var editedConsultation by remember { mutableStateOf(consultation ?: ConsultationEv()) }
    var dateText by remember { mutableStateOf(editedConsultation.date?.toString() ?: "") }
    var weightText by remember { mutableStateOf(editedConsultation.weight?.toString() ?: "") }
    var showDateError by remember { mutableStateOf(false) }
    var showWeightError by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
                text =
                        if (consultation == null || consultation.uuid.isEmpty())
                                General.ADD.translate()
                        else General.EDIT.translate(),
                style = MaterialTheme.typography.h6
        )

        // Date
        OutlinedTextField(
                value = dateText,
                onValueChange = { newValue: String ->
                    dateText = newValue
                    try {
                        val date = LocalDate.parse(newValue)
                        editedConsultation = editedConsultation.copy(date = date)
                        showDateError = false
                    } catch (e: Exception) {
                        showDateError = true
                    }
                },
                label = { Text(Consultation.DATE.translate()) },
                modifier = Modifier.fillMaxWidth(),
                isError = showDateError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true
        )

        if (showDateError) {
            Text(
                    text = "Format de date invalide (YYYY-MM-DD)",
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption
            )
        }

        // Poids
        OutlinedTextField(
                value = weightText,
                onValueChange = { newValue: String ->
                    weightText = newValue
                    try {
                        if (newValue.isNotEmpty()) {
                            val weight = newValue.toFloat()
                            editedConsultation = editedConsultation.copy(weight = weight)
                            showWeightError = false
                        } else {
                            editedConsultation = editedConsultation.copy(weight = null)
                            showWeightError = false
                        }
                    } catch (e: Exception) {
                        showWeightError = true
                    }
                },
                label = { Text(Animal.WEIGHT.translate()) },
                modifier = Modifier.fillMaxWidth(),
                isError = showWeightError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
        )

        if (showWeightError) {
            Text(
                    text = "Format de poids invalide (nombre décimal)",
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption
            )
        }

        // Objectif
        OutlinedTextField(
                value = editedConsultation.objectConsult,
                onValueChange = { newValue: String ->
                    editedConsultation = editedConsultation.copy(objectConsult = newValue)
                },
                label = { Text(Consultation.OBJECTIVE.translate()) },
                modifier = Modifier.fillMaxWidth()
        )

        // Observations
        OutlinedTextField(
                value = editedConsultation.observation,
                onValueChange = { newValue: String ->
                    editedConsultation = editedConsultation.copy(observation = newValue)
                },
                label = { Text(Consultation.OBSERVATION.translate()) },
                modifier = Modifier.fillMaxWidth().weight(1f)
        )

        // Liste des rations
        Text(text = "Rations", style = MaterialTheme.typography.subtitle1)

        LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(editedConsultation.rations) { ration ->
                RationCard(
                        ration = ration,
                        onEdit = { /* TODO: Implémenter l'édition de ration */},
                        onDelete = { editedConsultation.rations.remove(ration) }
                )
            }
        }

        // Boutons d'action
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                    onClick = onDismiss,
                    colors =
                            ButtonDefaults.buttonColors(
                                    backgroundColor = VetNutriColors.Secondary,
                                    contentColor = VetNutriColors.OnSecondary
                            )
            ) { Text(General.CANCEL.translate()) }

            Button(
                    onClick = {
                        if (!showDateError && !showWeightError && editedConsultation.date != null) {
                            // S'assurer que l'UUID est généré si c'est une nouvelle consultation
                            if (editedConsultation.uuid.isEmpty()) {
                                editedConsultation =
                                        editedConsultation.copy(
                                                uuid = kotlin.uuid.Uuid.random().toString()
                                        )
                            }
                            onSave(editedConsultation)
                        }
                    },
                    enabled = !showDateError && !showWeightError && editedConsultation.date != null,
                    colors =
                            ButtonDefaults.buttonColors(
                                    backgroundColor = VetNutriColors.Primary,
                                    contentColor = VetNutriColors.OnPrimary
                            )
            ) { Text(General.SAVE.translate()) }
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
    Card(modifier = modifier.fillMaxWidth(), elevation = 2.dp) {
        Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = ration.name, style = MaterialTheme.typography.subtitle1)
                Text(text = "Coef: ${ration.coef}", style = MaterialTheme.typography.body2)
            }

            Row {
                IconButton(onClick = onEdit) {
                    // TODO: Ajouter une icône d'édition
                }
                IconButton(onClick = onDelete) {
                    // TODO: Ajouter une icône de suppression
                }
            }
        }
    }
}
