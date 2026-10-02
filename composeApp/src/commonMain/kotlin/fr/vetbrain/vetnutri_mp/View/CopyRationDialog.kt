package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.sortedForDisplay

@Composable
internal fun CopyRationDialog(
        consultations: List<ConsultationEv>,
        onDismiss: () -> Unit,
        onCopy: (ConsultationEv, Ration) -> Unit
) {
        var selected by remember { mutableStateOf<Pair<String, String>?>(null) }
        val sources = consultations.filter { it.rations.isNotEmpty() }.sortedByDescending { it.date }
        val consultation = sources.firstOrNull { it.uuid == selected?.first }
        val ration = consultation?.rations?.firstOrNull { it.uuid == selected?.second }
        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Reprendre une ration d’une autre consultation") },
                text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Les aliments et leurs quantités seront ajoutés dans une nouvelle ration proposée. Vous pourrez ensuite les modifier.")
                                if (sources.isEmpty()) {
                                        Text("Aucune ration disponible dans les autres consultations de cet animal.")
                                } else {
                                        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                                                sources.forEach { source ->
                                                        item(key = source.uuid) {
                                                                Text(
                                                                        listOfNotNull(source.date?.let { "${it.dayOfMonth.toString().padStart(2, '0')}/${it.monthNumber.toString().padStart(2, '0')}/${it.year}" } ?: "Date non renseignée", source.objectConsult.takeIf { it.isNotBlank() }).joinToString(" — "),
                                                                        style = MaterialTheme.typography.subtitle2,
                                                                        modifier = Modifier.padding(vertical = 8.dp)
                                                                )
                                                        }
                                                        items(source.rations.sortedForDisplay(), key = { "${source.uuid}/${it.uuid}" }) { candidate ->
                                                                val label = candidate.name.ifBlank { "Ration ${candidate.number}" }
                                                                OutlinedButton(
                                                                        onClick = { selected = source.uuid to candidate.uuid },
                                                                        modifier = Modifier.fillMaxWidth()
                                                                ) {
                                                                        RadioButton(selected = selected == (source.uuid to candidate.uuid), onClick = null)
                                                                        Spacer(Modifier.width(8.dp))
                                                                        Text("$label · ${if (candidate.actual) "Actuelle" else "Proposée"} · ${candidate.alimentMutableList.size} aliment(s)", modifier = Modifier.weight(1f))
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                },
                confirmButton = {
                        Button(enabled = ration != null, onClick = {
                                if (consultation != null && ration != null) onCopy(consultation, ration)
                        }) { Text("Copier la ration") }
                },
                dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
        )
}
