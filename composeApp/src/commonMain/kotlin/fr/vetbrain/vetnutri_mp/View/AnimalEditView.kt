package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import fr.vetbrain.vetnutri_mp.Components.ComboBox
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalEditView(
        animal: AnimalEv,
        onSave: (AnimalEv) -> Unit,
        onCancel: () -> Unit,
        modifier: Modifier = Modifier
) {
        var nom by remember { mutableStateOf(animal.nom) }
        var race by remember { mutableStateOf(animal.race) }
        var ownerName by remember { mutableStateOf(animal.ownerName) }
        var summary by remember { mutableStateOf(animal.summary) }
        var selectedSex by remember { mutableStateOf(animal.getSex()) }
        var selectedEspece by remember { mutableStateOf(animal.getEspece()) }
        var birthDateText by remember { mutableStateOf(animal.birthdate?.toString() ?: "") }
        var isDateValid by remember { mutableStateOf(true) }
        var isDead by remember { mutableStateOf(animal.dead) }
        var isDatePickerVisible by remember { mutableStateOf(false) }

        val scrollState = rememberScrollState()

        Card(
                modifier = modifier.fillMaxSize(),
                elevation = AppSizes.elevationMedium,
                backgroundColor = MaterialTheme.colors.surface
        ) {
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(AppSizes.paddingLarge)
                                        .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                        // Titre avec style amélioré
                        Text(
                                text = Animal.EDIT_ANIMAL.translate(),
                                style = MaterialTheme.typography.h5,
                                color = VetNutriColors.Primary
                        )

                        Divider(color = Color.LightGray, thickness = AppSizes.dividerHeight)

                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                        // Nom de l'animal
                        OutlinedTextField(
                                value = nom,
                                onValueChange = { nom = it },
                                label = { Text(Animal.NAME.translate()) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = {
                                        Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = "Nom",
                                                tint = VetNutriColors.Primary
                                        )
                                }
                        )

                        // Race
                        OutlinedTextField(
                                value = race,
                                onValueChange = { race = it },
                                label = { Text(Animal.BREED.translate()) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                        )

                        // Propriétaire
                        OutlinedTextField(
                                value = ownerName,
                                onValueChange = { ownerName = it },
                                label = { Text(Animal.OWNER.translate()) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = {
                                        Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Propriétaire",
                                                tint = VetNutriColors.Primary
                                        )
                                }
                        )

                        // Date de naissance
                        OutlinedTextField(
                                value = birthDateText,
                                onValueChange = {
                                        birthDateText = it
                                        isDateValid =
                                                try {
                                                        if (it.isNotEmpty()) LocalDate.parse(it)
                                                        true
                                                } catch (e: Exception) {
                                                        false
                                                }
                                },
                                label = { Text(Animal.BIRTH_DATE.translate()) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = !isDateValid,
                                leadingIcon = {
                                        Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = "Date de naissance",
                                                tint = VetNutriColors.Primary
                                        )
                                },
                                trailingIcon = {
                                        IconButton(onClick = { isDatePickerVisible = true }) {
                                                Icon(
                                                        imageVector = Icons.Default.DateRange,
                                                        contentDescription = General.DATE_PICKER.translate(),
                                                        tint = VetNutriColors.Primary
                                                )
                                        }
                                }
                        )

                        if (!isDateValid) {
                                Text(
                                        text = "Format de date invalide (YYYY-MM-DD)",
                                        color = MaterialTheme.colors.error,
                                        style = MaterialTheme.typography.caption
                                )
                        }

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
                                                                                birthDateText = date.toString()
                                                                                isDateValid = true
                                                                        }
                                                                        isDatePickerVisible = false
                                                                }
                                                        ) { M3Text(General.VALIDATE.translate()) }
                                                },
                                                dismissButton = {
                                                        M3TextButton(onClick = { isDatePickerVisible = false }) {
                                                                M3Text(General.CANCEL.translate())
                                                        }
                                                }
                                        ) {
                                                DatePicker(state = pickerState)
                                        }
                                }
                        }

                        // Sexe avec ComboBox
                        ComboBox(
                                items = Sex.values().toList(),
                                init = selectedSex,
                                label = Animal.SEX.translate(),
                                onItemSelected = { selectedLabel ->
                                        val newSelectedSex =
                                                Sex.values().find { it.label == selectedLabel }
                                        newSelectedSex?.let { selectedSex = it }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                itemLabelProvider = { it.translateEnum() }
                        )

                        // Espèce avec ComboBox
                        ComboBox(
                                items = Espece.valuesExcept(Espece.CH).toList(),
                                init = selectedEspece,
                                label = Animal.SPECIES.translate(),
                                onItemSelected = { selectedLabel ->
                                        val newSelectedEspece =
                                                Espece.values().find { it.label == selectedLabel }
                                        newSelectedEspece?.let { selectedEspece = it }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                itemLabelProvider = { it.translateEnum() }
                        )

                        // Animal décédé
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier =
                                        Modifier.padding(vertical = AppSizes.paddingSmall)
                                                .fillMaxWidth()
                                                .padding(horizontal = AppSizes.paddingSmall)
                        ) {
                                Checkbox(
                                        checked = isDead,
                                        onCheckedChange = { isDead = it },
                                        colors =
                                                CheckboxDefaults.colors(
                                                        checkedColor = VetNutriColors.Primary
                                                )
                                )
                                Text(
                                        text = Animal.DEAD.translate(),
                                        modifier = Modifier.padding(start = AppSizes.paddingSmall),
                                        style = MaterialTheme.typography.body1
                                )
                        }

                        // Résumé
                        OutlinedTextField(
                                value = summary,
                                onValueChange = { summary = it },
                                label = { Text(Animal.SUMMARY.translate()) },
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .height(AppSizes.inputMultilineHeight),
                                maxLines = 5
                        )

                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

                        Divider(color = Color.LightGray, thickness = AppSizes.dividerHeight)

                        // Boutons d'action
                        Row(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(top = AppSizes.paddingMedium),
                                horizontalArrangement =
                                        Arrangement.spacedBy(AppSizes.paddingMedium, Alignment.End),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                OutlinedButton(
                                        onClick = onCancel,
                                        colors =
                                                ButtonDefaults.outlinedButtonColors(
                                                        contentColor = VetNutriColors.Secondary
                                                ),
                                        border =
                                                ButtonDefaults.outlinedBorder.copy(
                                                        brush = SolidColor(VetNutriColors.Secondary)
                                                )
                                ) {
                                        Text(
                                                General.CANCEL.translate(),
                                                style = MaterialTheme.typography.button
                                        )
                                }

                                Button(
                                        onClick = {
                                                val birthdate =
                                                        try {
                                                                if (birthDateText.isNotEmpty())
                                                                        LocalDate.parse(
                                                                                birthDateText
                                                                        )
                                                                else null
                                                        } catch (e: Exception) {
                                                                null
                                                        }

                                                val updatedAnimal =
                                                        animal.copy(
                                                                nom = nom,
                                                                race = race,
                                                                ownerName = ownerName,
                                                                summary = summary,
                                                                sexId = selectedSex.id,
                                                                birthdate = birthdate,
                                                                dead = isDead,
                                                                specieId = selectedEspece.label
                                                        )

                                                // Log de débogage pour vérifier les informations de
                                                // l'animal

                                                onSave(updatedAnimal)
                                        },
                                        enabled = nom.isNotEmpty() && isDateValid,
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Primary,
                                                        contentColor = VetNutriColors.OnPrimary,
                                                        disabledBackgroundColor =
                                                                VetNutriColors.Primary.copy(
                                                                        alpha = 0.3f
                                                                )
                                                )
                                ) {
                                        Text(
                                                General.SAVE.translate(),
                                                style = MaterialTheme.typography.button
                                        )
                                }
                        }
                }
        }
}
