package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.Animal
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
        val animal by viewModel.animal.collectAsState()
        val isSaving by viewModel.isSaving.collectAsState()
        val saveSuccess by viewModel.saveSuccess.collectAsState()

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
                        value = animal.id.toString(),
                        onValueChange = { newId ->
                                viewModel.updateAnimal(animal.copy(id = newId.toLongOrNull() ?: 0))
                        },
                        label = { Text(AnimalKeys.ID.translate()) },
                        modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                        value = animal.nom,
                        onValueChange = { newName ->
                                viewModel.updateAnimal(animal.copy(nom = newName))
                        },
                        label = { Text(AnimalKeys.NAME.translate()) },
                        modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                        value = animal.nomProprio,
                        onValueChange = { newOwner ->
                                viewModel.updateAnimal(animal.copy(nomProprio = newOwner))
                        },
                        label = { Text(AnimalKeys.OWNER.translate()) },
                        modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                        value = animal.race,
                        onValueChange = { newBreed ->
                                viewModel.updateAnimal(animal.copy(race = newBreed))
                        },
                        label = { Text(AnimalKeys.BREED.translate()) },
                        modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                        value = animal.dateNaissance?.toString() ?: "",
                        onValueChange = { newDate ->
                                // TODO: Implement proper date parsing
                                viewModel.updateAnimal(animal.copy(dateNaissance = null))
                        },
                        label = { Text(AnimalKeys.BIRTH_DATE.translate()) },
                        modifier = Modifier.fillMaxWidth()
                )

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        Animal.Espece.values().forEach { espece ->
                                RadioButton(
                                        selected = animal.espece == espece,
                                        onClick = {
                                                viewModel.updateAnimal(animal.copy(espece = espece))
                                        }
                                )
                                Text(
                                        text = espece.nameToString(),
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                )
                        }
                }

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        Animal.Sexe.values().forEach { sexe ->
                                RadioButton(
                                        selected = animal.sexe == sexe,
                                        onClick = {
                                                viewModel.updateAnimal(animal.copy(sexe = sexe))
                                        }
                                )
                                Text(
                                        text = sexe.nameToString(),
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                )
                        }
                }

                OutlinedTextField(
                        value = animal.resume,
                        onValueChange = { newSummary ->
                                viewModel.updateAnimal(animal.copy(resume = newSummary))
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
