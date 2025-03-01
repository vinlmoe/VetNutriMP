package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import kotlinx.datetime.*

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
                AppDatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        onDateSelected = { date ->
                                dateText = date.toString()
                                onDateSelected(date)
                                showDatePicker = false
                        },
                        selectedDate = selectedDate
                                        ?: Clock.System.now()
                                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                                .date
                )
        }
}

/**
 * Dialogue de sélection de date avec des contrôles pour l'année, le mois et le jour
 *
 * @param onDismissRequest Callback appelé lorsque le dialogue est fermé
 * @param onDateSelected Callback appelé lorsqu'une date est sélectionnée
 * @param selectedDate Date actuellement sélectionnée
 */
@Composable
private fun AppDatePickerDialog(
        onDismissRequest: () -> Unit,
        onDateSelected: (LocalDate) -> Unit,
        selectedDate: LocalDate
) {
        var year by remember { mutableStateOf(selectedDate.year) }
        var month by remember { mutableStateOf(selectedDate.monthNumber) }
        var day by remember { mutableStateOf(selectedDate.dayOfMonth) }

        AlertDialog(
                onDismissRequest = onDismissRequest,
                title = { Text(General.DATE_PICKER.translate()) },
                text = {
                        Column(
                                modifier = Modifier.padding(AppSizes.paddingMedium),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                // Année
                                Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                        Text(
                                                General.YEAR.translate(),
                                                style = MaterialTheme.typography.subtitle1
                                        )
                                        Row(
                                                horizontalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall),
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                IconButton(onClick = { year-- }) {
                                                        Icon(
                                                                AppIcons.ArrowDropDown,
                                                                contentDescription =
                                                                        General.PREVIOUS_YEAR
                                                                                .translate(),
                                                                modifier = Modifier.rotate(90f)
                                                        )
                                                }
                                                Text(
                                                        year.toString(),
                                                        style = MaterialTheme.typography.h6
                                                )
                                                IconButton(onClick = { year++ }) {
                                                        Icon(
                                                                AppIcons.ArrowDropDown,
                                                                contentDescription =
                                                                        General.NEXT_YEAR
                                                                                .translate(),
                                                                modifier = Modifier.rotate(270f)
                                                        )
                                                }
                                        }
                                }

                                // Mois
                                Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                        Text(
                                                General.MONTH.translate(),
                                                style = MaterialTheme.typography.subtitle1
                                        )
                                        Row(
                                                horizontalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall),
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                IconButton(onClick = { if (month > 1) month-- }) {
                                                        Icon(
                                                                AppIcons.ArrowDropDown,
                                                                contentDescription =
                                                                        General.PREVIOUS_MONTH
                                                                                .translate(),
                                                                modifier = Modifier.rotate(90f)
                                                        )
                                                }
                                                Text(
                                                        month.toString().padStart(2, '0'),
                                                        style = MaterialTheme.typography.h6
                                                )
                                                IconButton(onClick = { if (month < 12) month++ }) {
                                                        Icon(
                                                                AppIcons.ArrowDropDown,
                                                                contentDescription =
                                                                        General.NEXT_MONTH
                                                                                .translate(),
                                                                modifier = Modifier.rotate(270f)
                                                        )
                                                }
                                        }
                                }

                                // Jour
                                Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                        Text(
                                                General.DAY.translate(),
                                                style = MaterialTheme.typography.subtitle1
                                        )
                                        Row(
                                                horizontalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall),
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                IconButton(onClick = { if (day > 1) day-- }) {
                                                        Icon(
                                                                AppIcons.ArrowDropDown,
                                                                contentDescription =
                                                                        General.PREVIOUS_DAY
                                                                                .translate(),
                                                                modifier = Modifier.rotate(90f)
                                                        )
                                                }
                                                Text(
                                                        day.toString().padStart(2, '0'),
                                                        style = MaterialTheme.typography.h6
                                                )
                                                IconButton(
                                                        onClick = {
                                                                val lastDayOfMonth =
                                                                        LocalDate(year, month, 1)
                                                                                .daysUntil(
                                                                                        LocalDate(
                                                                                                year,
                                                                                                if (month <
                                                                                                                12
                                                                                                )
                                                                                                        month +
                                                                                                                1
                                                                                                else
                                                                                                        1,
                                                                                                1
                                                                                        )
                                                                                )
                                                                if (day < lastDayOfMonth) day++
                                                        }
                                                ) {
                                                        Icon(
                                                                AppIcons.ArrowDropDown,
                                                                contentDescription =
                                                                        General.NEXT_DAY
                                                                                .translate(),
                                                                modifier = Modifier.rotate(270f)
                                                        )
                                                }
                                        }
                                }
                        }
                },
                confirmButton = {
                        Button(
                                onClick = {
                                        try {
                                                val date = LocalDate(year, month, day)
                                                onDateSelected(date)
                                        } catch (e: Exception) {
                                                // Date invalide, ne rien faire
                                        }
                                },
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Primary,
                                                contentColor = VetNutriColors.OnPrimary
                                        )
                        ) { Text(General.VALIDATE.translate()) }
                },
                dismissButton = {
                        Button(
                                onClick = onDismissRequest,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Secondary,
                                                contentColor = VetNutriColors.OnSecondary
                                        )
                        ) { Text(General.CANCEL.translate()) }
                }
        )
}
