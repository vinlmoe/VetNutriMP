package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.Animal
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterialApi::class)
@Composable
fun AnimalListView(
        viewModel: AnimalListViewModel,
        onAddAnimal: () -> Unit,
        onSelectAnimal: (Animal) -> Unit,
        modifier: Modifier = Modifier
) {
    val animals by viewModel.animals.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadAnimals() }

    Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
                onClick = onAddAnimal,
                colors =
                        ButtonDefaults.buttonColors(
                                backgroundColor = VetNutriColors.Primary,
                                contentColor = VetNutriColors.OnPrimary
                        )
        ) { Text("Ajouter un animal") }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(animals) { animal ->
                AnimalCard(animal = animal, onClick = { onSelectAnimal(animal) })
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterialApi::class)
@Composable
private fun AnimalCard(animal: Animal, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), elevation = 4.dp, onClick = onClick) {
        Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = animal.nom, style = MaterialTheme.typography.h6)
            Text(
                    text = "Espèce: ${animal.espece.nameToString()}",
                    style = MaterialTheme.typography.body1
            )
            if (animal.race.isNotEmpty()) {
                Text(text = "Race: ${animal.race}", style = MaterialTheme.typography.body2)
            }
            if (animal.nomProprio.isNotEmpty()) {
                Text(
                        text = "Propriétaire: ${animal.nomProprio}",
                        style = MaterialTheme.typography.body2
                )
            }
        }
    }
}
