package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterialApi::class)
@Composable
fun AnimalListView(
        viewModel: AnimalListViewModel,
        onAddAnimal: () -> Unit,
        onSelectAnimal: (AnimalEv) -> Unit,
        modifier: Modifier = Modifier
) {
    val animals: List<AnimalEv> = viewModel.animals.collectAsState().value

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
        ) { Text(General.ADD.translate()) }

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
private fun AnimalCard(
        animal: AnimalEv,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth(), elevation = 4.dp, onClick = onClick) {
        Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = animal.nom, style = MaterialTheme.typography.h6)
            Text(
                    text = "${Animal.SPECIES.translate()}: ${animal.getEspece().label}",
                    style = MaterialTheme.typography.body1
            )
            if (animal.race.isNotEmpty()) {
                Text(
                        text = "${Animal.BREED.translate()}: ${animal.race}",
                        style = MaterialTheme.typography.body2
                )
            }
            if (animal.ownerName.isNotEmpty()) {
                Text(
                        text = "${Animal.OWNER.translate()}: ${animal.ownerName}",
                        style = MaterialTheme.typography.body2
                )
            }
        }
    }
}
