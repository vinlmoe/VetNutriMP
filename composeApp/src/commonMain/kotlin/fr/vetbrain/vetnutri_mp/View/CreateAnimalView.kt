package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.Components.ComboBox
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal as AnimalKeys
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.CreateAnimalViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.datetime.LocalDate
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

        Scaffold(
                floatingActionButton = {
                        FloatingActionButton(
                                onClick = {
                                        viewModel.saveAnimal()
                                        onNavigateBack()
                                },
                                backgroundColor = VetNutriColors.Primary
                        ) {
                                Icon(
                                        imageVector = AppIcons.Check,
                                        contentDescription = "Enregistrer l'animal",
                                        tint = VetNutriColors.OnPrimary
                                )
                        }
                }
        ) { paddingValues ->
                Column(
                        modifier =
                                modifier.fillMaxSize()
                                        .padding(paddingValues)
                                        .padding(AppSizes.paddingMedium)
                                        .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                        Text(
                                text =
                                        if (isEditing) AnimalKeys.EDIT_ANIMAL.translate()
                                        else AnimalKeys.NEW_ANIMAL.translate(),
                                style = MaterialTheme.typography.h6,
                                modifier = Modifier.padding(bottom = AppSizes.paddingSmall)
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

                        OutlinedTextField(
                                value = dateText,
                                onValueChange = { newDate: String ->
                                        dateText = newDate
                                        try {
                                                val date = LocalDate.parse(newDate)
                                                viewModel.updateAnimal(
                                                        animal.copy(birthdate = date)
                                                )
                                                showDateError = false
                                        } catch (e: Exception) {
                                                showDateError = true
                                        }
                                },
                                label = { Text(AnimalKeys.BIRTH_DATE.translate()) },
                                modifier = Modifier.fillMaxWidth(),
                                isError = showDateError
                        )

                        if (showDateError) {
                                Text(
                                        text = "error.invalidValue".translate(),
                                        color = MaterialTheme.colors.error,
                                        style = MaterialTheme.typography.caption
                                )
                        }

                        // Bouton pour définir la date à aujourd'hui
                        TextButton(
                                onClick = {
                                        val today =
                                                kotlinx.datetime.Clock.System.now()
                                                        .toLocalDateTime(
                                                                kotlinx.datetime.TimeZone
                                                                        .currentSystemDefault()
                                                        )
                                                        .date
                                        dateText = today.toString()
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
                                                        animal.copy().apply { specieId = it.label }
                                                )
                                        }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                itemLabelProvider = { it.translateEnum() }
                        )

                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
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
                                                text = sexe.translateEnum(),
                                                modifier =
                                                        Modifier.align(Alignment.CenterVertically)
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

                        // Le FAB gère l'enregistrement; conserver un bouton d'annulation si besoin
                        Button(
                                onClick = {
                                        if (!isEditing) {
                                                viewModel.resetAnimal()
                                        }
                                        onNavigateBack()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Secondary,
                                                contentColor = VetNutriColors.OnSecondary
                                        )
                        ) { Text(General.CANCEL.translate()) }
                }
        }
}
