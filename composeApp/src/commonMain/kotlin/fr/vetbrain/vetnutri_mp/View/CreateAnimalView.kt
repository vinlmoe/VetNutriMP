package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.TextButton as M3TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.Components.AutocompleteTextField
import fr.vetbrain.vetnutri_mp.Components.ComboBox
import kotlinx.coroutines.launch
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal as AnimalKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.CreateAnimalViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
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
        var isDatePickerVisible by remember { mutableStateOf(false) }
        var availableRaces by remember { mutableStateOf<List<String>>(emptyList()) }
        val coroutineScope = rememberCoroutineScope()

        // Charger les races disponibles pour l'espèce actuelle
        LaunchedEffect(animal.specieId) {
                coroutineScope.launch {
                        try {
                                availableRaces = viewModel.getRacesBySpecies(animal.specieId)
                        } catch (e: Exception) {
                                availableRaces = emptyList()
                        }
                }
        }

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

                        ComboBox(
                                items = Espece.valuesExcept(Espece.CH).toList(),
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

                        AutocompleteTextField(
                                value = animal.race,
                                onValueChange = { newBreed: String ->
                                        viewModel.updateAnimal(animal.copy(race = newBreed))
                                },
                                suggestions = availableRaces,
                                label = AnimalKeys.BREED.translate(),
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

                        TextButton(onClick = { isDatePickerVisible = true }) {
                                Text("general.selectDate".translate())
                        }

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

                        if (isDatePickerVisible) {
                                val pickerState = rememberDatePickerState()
                                val vetNutriColorScheme = lightColorScheme(
                                        primary = VetNutriColors.Primary,
                                        onPrimary = VetNutriColors.OnPrimary,
                                        secondary = VetNutriColors.Secondary,
                                        onSecondary = VetNutriColors.OnSecondary,
                                        error = VetNutriColors.Error,
                                        onError = VetNutriColors.OnError,
                                        background = VetNutriColors.Background,
                                        onBackground = VetNutriColors.OnBackground,
                                        surface = VetNutriColors.Background,
                                        onSurface = VetNutriColors.OnBackground,
                                        surfaceVariant = VetNutriColors.Background
                                )
                                M3MaterialTheme(colorScheme = vetNutriColorScheme) {
                                        DatePickerDialog(
                                                onDismissRequest = { isDatePickerVisible = false },
                                                confirmButton = {
                                                        M3TextButton(
                                                                onClick = {
                                                                        val selected: Long? = pickerState.selectedDateMillis
                                                                        if (selected != null) {
                                                                                val date = Instant.fromEpochMilliseconds(selected)
                                                                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                                                                        .date
                                                                                dateText = date.toString()
                                                                                viewModel.updateAnimal(animal.copy(birthdate = date))
                                                                                showDateError = false
                                                                        }
                                                                        isDatePickerVisible = false
                                                                }
                                                        ) { M3Text("general.confirm".translate()) }
                                                },
                                                dismissButton = {
                                                        M3TextButton(onClick = { isDatePickerVisible = false }) {
                                                                M3Text("general.cancel".translate())
                                                        }
                                                }
                                        ) {
                                                DatePicker(state = pickerState)
                                        }
                                }
                        }

                        ComboBox(
                                items = Sex.values().toList(),
                                init = animal.getSex(),
                                label = AnimalKeys.SEX.translate(),
                                onItemSelected = { selectedLabel ->
                                        val selectedSex =
                                                Sex.values().find { it.label == selectedLabel }
                                        selectedSex?.let {
                                                val newAnimal = animal.copy()
                                                newAnimal.sexId = it.id
                                                viewModel.updateAnimal(newAnimal)
                                        }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                itemLabelProvider = { it.translateEnum() }
                        )

                        OutlinedTextField(
                                value = animal.summary,
                                onValueChange = { newSummary: String ->
                                        viewModel.updateAnimal(animal.copy(summary = newSummary))
                                },
                                label = { Text(AnimalKeys.SUMMARY.translate()) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                        )
                }
        }
}
