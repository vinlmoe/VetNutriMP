package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal as AnimalKeys
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.CreateAnimalViewModel
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterialApi::class)
@Composable
fun CreateAnimalView(
        viewModel: CreateAnimalViewModel,
        onNavigateBack: () -> Unit,
        modifier: Modifier = Modifier
) {
        val animal = viewModel.animal.collectAsState().value
        val isSaving = viewModel.isSaving.collectAsState().value
        val saveSuccess = viewModel.saveSuccess.collectAsState().value

        LaunchedEffect(saveSuccess) {
                if (saveSuccess) {
                        viewModel.resetSaveStatus()
                        onNavigateBack()
                }
        }

        Column(
                modifier =
                        modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                OutlinedTextField(
                        value = animal.id ?: "",
                        onValueChange = { newId: String ->
                                viewModel.updateAnimal(animal.copy(id = newId))
                        },
                        label = { Text(AnimalKeys.ID.translate()) },
                        modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                        value = animal.nom,
                        onValueChange = { newName: String ->
                                viewModel.updateAnimal(animal.copy(nom = newName))
                        },
                        label = { Text(AnimalKeys.NAME.translate()) },
                        modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                        value = animal.ownerName,
                        onValueChange = { newOwner: String ->
                                viewModel.updateAnimal(animal.copy(ownerName = newOwner))
                        },
                        label = { Text(AnimalKeys.OWNER.translate()) },
                        modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                        value = animal.race,
                        onValueChange = { newBreed: String ->
                                viewModel.updateAnimal(animal.copy(race = newBreed))
                        },
                        label = { Text(AnimalKeys.BREED.translate()) },
                        modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                        value = animal.birthdate?.toString() ?: "",
                        onValueChange = { newDate: String ->
                                // TODO: Implement proper date parsing
                                viewModel.updateAnimal(animal.copy(birthdate = null))
                        },
                        label = { Text(AnimalKeys.BIRTH_DATE.translate()) },
                        modifier = Modifier.fillMaxWidth()
                )

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        Espece.values().forEach { espece ->
                                RadioButton(
                                        selected = animal.getEspece() == espece,
                                        onClick = {
                                                val newAnimal = animal.copy()
                                                newAnimal.specieId = espece.name
                                                viewModel.updateAnimal(newAnimal)
                                        }
                                )
                                Text(
                                        text = espece.label,
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                )
                        }
                }

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        Sex.values().forEach { sexe ->
                                RadioButton(
                                        selected = animal.getSex() == sexe,
                                        onClick = {
                                                val newAnimal = animal.copy()
                                                newAnimal.sexId = sexe.id
                                                viewModel.updateAnimal(newAnimal)
                                        }
                                )
                                Text(
                                        text = sexe.label,
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                )
                        }
                }

                OutlinedTextField(
                        value = animal.summary,
                        onValueChange = { newSummary: String ->
                                viewModel.updateAnimal(animal.copy(summary = newSummary))
                        },
                        label = { Text(AnimalKeys.SUMMARY.translate()) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                )

                Button(
                        onClick = { viewModel.saveAnimal() },
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                                ButtonDefaults.buttonColors(
                                        backgroundColor = VetNutriColors.Primary,
                                        contentColor = VetNutriColors.OnPrimary
                                )
                ) {
                        if (isSaving) {
                                CircularProgressIndicator(
                                        color = VetNutriColors.OnPrimary,
                                        modifier = Modifier.size(24.dp)
                                )
                        } else {
                                Text(General.SAVE.translate())
                        }
                }
        }
}
