package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.ComboBox
import fr.vetbrain.vetnutri_mp.Components.DatePicker
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal as AnimalKeys
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.CreateAnimalViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterialApi::class)
@Composable
fun CreateAnimalView(
        viewModel: CreateAnimalViewModel,
        onNavigateBack: () -> Unit,
        isEditing: Boolean = false,
        modifier: Modifier = Modifier
) {
        val animal = viewModel.animal.collectAsState().value
        val isSaving = viewModel.isSaving.collectAsState().value
        val saveSuccess = viewModel.saveSuccess.collectAsState().value
        var dateText by remember { mutableStateOf(animal.birthdate?.toString() ?: "") }
        var showDateError by remember { mutableStateOf(false) }

        LaunchedEffect(saveSuccess) {
                if (saveSuccess) {
                        viewModel.resetSaveStatus()
                        if (!isEditing) {
                                viewModel.resetAnimal()
                        }
                        onNavigateBack()
                }
        }

        Column(
                modifier =
                        modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                Text(
                        text =
                                if (isEditing) AnimalKeys.EDIT_ANIMAL.translate()
                                else AnimalKeys.NEW_ANIMAL.translate(),
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = 8.dp)
                )

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

                DatePicker(
                        selectedDate = animal.birthdate,
                        onDateSelected = { date ->
                                viewModel.updateAnimal(animal.copy(birthdate = date))
                        },
                        label = AnimalKeys.BIRTH_DATE.translate(),
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
                                viewModel.updateAnimal(animal.copy(birthdate = today))
                                showDateError = false
                        }
                ) { Text("general.today".translate()) }

                ComboBox(
                        items = Espece.values().toList(),
                        init = animal.getEspece(),
                        label = AnimalKeys.SPECIES.translate(),
                        onItemSelected = { selectedLabel ->
                                val selectedEspece =
                                        Espece.values().find { it.label == selectedLabel }
                                selectedEspece?.let {
                                        viewModel.updateAnimal(
                                                animal.copy().apply { specieId = it.name }
                                        )
                                }
                        },
                        modifier = Modifier.fillMaxWidth()
                )

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
                                        text = sexe.label.translate(),
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

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        Button(
                                onClick = {
                                        if (!isEditing) {
                                                viewModel.resetAnimal()
                                        }
                                        onNavigateBack()
                                },
                                modifier = Modifier.weight(1f),
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Secondary,
                                                contentColor = VetNutriColors.OnSecondary
                                        )
                        ) { Text(General.CANCEL.translate()) }

                        Button(
                                onClick = { viewModel.saveAnimal() },
                                modifier = Modifier.weight(1f),
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
                                        Text(
                                                if (isEditing) General.UPDATE.translate()
                                                else General.SAVE.translate()
                                        )
                                }
                        }
                }
        }
}
