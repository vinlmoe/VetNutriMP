package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.vetbrain.vetnutri_mp.Components.DatePicker
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Consultation
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import kotlinx.datetime.*

@Composable
fun ConsultationEditDialog(
        consultation: ConsultationEv?,
        onDismiss: () -> Unit,
        onSave: (ConsultationEv) -> Unit
) {
    var editedConsultation by remember { mutableStateOf(consultation ?: ConsultationEv()) }
    var dateText by remember { mutableStateOf(editedConsultation.date?.toString() ?: "") }
    var showDateError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colors.surface
        ) {
            Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                        text =
                                if (consultation == null) General.ADD.translate()
                                else General.EDIT.translate(),
                        style = MaterialTheme.typography.h6
                )

                DatePicker(
                        selectedDate = editedConsultation.date,
                        onDateSelected = { date ->
                            editedConsultation = editedConsultation.copy(date = date)
                            showDateError = false
                        },
                        label = Consultation.DATE.translate(),
                        isError = showDateError,
                        errorMessage =
                                if (showDateError) "general.date_format".translate() else null,
                        modifier = Modifier.fillMaxWidth()
                )

                // Bouton pour définir la date à aujourd'hui
                TextButton(
                        onClick = {
                            val today =
                                    Clock.System.now()
                                            .toLocalDateTime(TimeZone.currentSystemDefault())
                                            .date
                            editedConsultation = editedConsultation.copy(date = today)
                            showDateError = false
                        }
                ) { Text("general.today".translate()) }

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
                Text(
                        text = Consultation.RATIONS.translate(),
                        style = MaterialTheme.typography.subtitle1
                )

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
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
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
                                if (!showDateError && editedConsultation.date != null) {
                                    onSave(editedConsultation)
                                }
                            },
                            enabled = !showDateError && editedConsultation.date != null,
                            colors =
                                    ButtonDefaults.buttonColors(
                                            backgroundColor = VetNutriColors.Primary,
                                            contentColor = VetNutriColors.OnPrimary
                                    )
                    ) { Text(General.SAVE.translate()) }
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
    Card(modifier = modifier.fillMaxWidth(), elevation = 2.dp) {
        Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = ration.name, style = MaterialTheme.typography.subtitle1)
                Text(
                        text = Consultation.RATION_COEF.translate() + ": " + ration.coef.toString(),
                        style = MaterialTheme.typography.body2
                )
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
