package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalViewModel

@Composable
fun CreateAnimalView(
        viewModel: AnimalViewModel,
        onSave: () -> Unit,
        modifier: Modifier = Modifier
) {
        Column(modifier = modifier.padding(16.dp).fillMaxWidth()) {
                // Nom de l'animal
                OutlinedTextField(
                        value = viewModel.name ?: "",
                        onValueChange = { viewModel.name = it },
                        label = { Text("name".translate()) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )

                // Espèce
                ComboBox(
                        items = Espece.valuesExcept(),
                        init = null,
                        label = "species".translate(),
                        onItemSelected = { selectedLabel ->
                                viewModel.selectedEspece =
                                        Espece.values().find { it.label == selectedLabel }
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )

                // Sexe
                ComboBox(
                        items = Sex.entries,
                        init = null,
                        label = "sex".translate(),
                        onItemSelected = { selectedLabel ->
                                viewModel.selectedSex =
                                        Sex.values().find { it.label == selectedLabel }
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )

                // Identifiant
                OutlinedTextField(
                        value = viewModel.id ?: "",
                        onValueChange = { viewModel.id = it },
                        label = { Text("id".translate()) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )

                // Propriétaire
                OutlinedTextField(
                        value = viewModel.ownerName ?: "",
                        onValueChange = { viewModel.ownerName = it },
                        label = { Text("owner".translate()) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )

                // Race
                OutlinedTextField(
                        value = viewModel.race ?: "",
                        onValueChange = { viewModel.race = it },
                        label = { Text("breed".translate()) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )

                // Résumé
                OutlinedTextField(
                        value = viewModel.summary ?: "",
                        onValueChange = { viewModel.summary = it },
                        label = { Text("description".translate()) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        maxLines = 3
                )

                // État (vivant/mort)
                Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Start
                ) {
                        Checkbox(
                                checked = viewModel.dead,
                                onCheckedChange = { viewModel.dead = it }
                        )
                        Text(text = "dead".translate(), modifier = Modifier.padding(start = 8.dp))
                }

                // Bouton de sauvegarde
                Button(
                        onClick = onSave,
                        enabled = viewModel.isValid(),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) { Text("save".translate()) }
        }
}
