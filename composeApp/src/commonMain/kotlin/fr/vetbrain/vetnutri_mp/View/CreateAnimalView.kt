package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Enumerise.Espece
import fr.vetbrain.vetnutri_mp.Enumerise.Sex
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
                label = { Text("Nom de l'animal") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        // Espèce
        ComboBox(
                items = Espece.valuesExcept(),
                init =null,
                
                onItemSelected = { selectedLabel ->
                    viewModel.selectedEspece = Espece.values().find { it.label == selectedLabel }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        // Sexe
        ComboBox(
                items = Sex.entries,
                init =null,
               
                label = "Sexe",
                onItemSelected = { selectedLabel ->
                    viewModel.selectedSex = Sex.values().find { it.label== selectedLabel }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        // Identifiant
        OutlinedTextField(
                value = viewModel.id ?: "",
                onValueChange = { viewModel.id = it },
                label = { Text("Identifiant") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        // Propriétaire
        OutlinedTextField(
                value = viewModel.ownerName ?: "",
                onValueChange = { viewModel.ownerName = it },
                label = { Text("Nom du propriétaire") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        // Race
        OutlinedTextField(
                value = viewModel.race ?: "",
                onValueChange = { viewModel.race = it },
                label = { Text("Race") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        // Résumé
        OutlinedTextField(
                value = viewModel.summary ?: "",
                onValueChange = { viewModel.summary = it },
                label = { Text("Résumé") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                maxLines = 3
        )

        // État (vivant/mort)
        Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Start
        ) {
            Checkbox(checked = viewModel.dead, onCheckedChange = { viewModel.dead = it })
            Text(text = "Décédé", modifier = Modifier.padding(start = 8.dp))
        }

        // Bouton de sauvegarde
        Button(
                onClick = onSave,
                enabled = viewModel.isValid(),
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) { Text("Créer l'animal") }
    }
}
