package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.*
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
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Composant de sélection de date avec un champ de texte et un dialogue de sélection
 *
 * @param selectedDate Date actuellement sélectionnée
 * @param onDateSelected Callback appelé lorsqu'une date est sélectionnée
 * @param label Libellé du champ de texte
 * @param modifier Modificateur optionnel
 * @param isError Indique si le champ contient une erreur
 * @param errorMessage Message d'erreur à afficher
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePicker(
        selectedDate: LocalDate?,
        onDateSelected: (LocalDate) -> Unit,
        label: String,
        modifier: Modifier = Modifier,
        isError: Boolean = false,
        errorMessage: String? = null
) {
        var showDatePicker by remember { mutableStateOf(false) }
        var dateText by remember(selectedDate) { mutableStateOf(selectedDate?.toString() ?: "") }

        Column(modifier = modifier) {
                OutlinedTextField(
                        value = dateText,
                        onValueChange = { newValue ->
                                dateText = newValue
                                try {
                                        val date = LocalDate.parse(newValue)
                                        onDateSelected(date)
                                } catch (e: Exception) {
                                        // L'erreur sera gérée par le composant parent
                                }
                        },
                        label = { Text(label) },
                        trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                        Icon(
                                                AppIcons.DateRange,
                                                contentDescription = General.DATE_PICKER.translate()
                                        )
                                }
                        },
                        isError = isError,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                                TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = VetNutriColors.Primary,
                                        unfocusedBorderColor =
                                                MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                                        errorBorderColor = MaterialTheme.colors.error
                                )
                )

                if (isError && errorMessage != null) {
                        Text(
                                text = errorMessage,
                                color = MaterialTheme.colors.error,
                                style = MaterialTheme.typography.caption
                        )
                }
        }

        if (showDatePicker) {
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
                                onDismissRequest = { showDatePicker = false },
                                confirmButton = {
                                        M3TextButton(
                                                onClick = {
                                                        val selected: Long? = pickerState.selectedDateMillis
                                                        if (selected != null) {
                                                                val date = Instant.fromEpochMilliseconds(selected)
                                                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                                                        .date
                                                                dateText = date.toString()
                                                                onDateSelected(date)
                                                        }
                                                        showDatePicker = false
                                                }
                                        ) { M3Text(General.VALIDATE.translate()) }
                                },
                                dismissButton = {
                                        M3TextButton(onClick = { showDatePicker = false }) {
                                                M3Text(General.CANCEL.translate())
                                        }
                                }
                        ) {
                                DatePicker(state = pickerState)
                        }
                }
        }
}

