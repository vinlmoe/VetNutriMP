package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import kotlinx.datetime.*

@Composable
fun DatePicker(
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
                        Icon(AppIcons.DateRange, "general.date_picker".translate())
                    }
                },
                isError = isError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
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
        DatePickerDialog(
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

@Composable
private fun DatePickerDialog(
        onDismissRequest: () -> Unit,
        onDateSelected: (LocalDate) -> Unit,
        selectedDate: LocalDate
) {
    var year by remember { mutableStateOf(selectedDate.year) }
    var month by remember { mutableStateOf(selectedDate.monthNumber) }
    var day by remember { mutableStateOf(selectedDate.dayOfMonth) }

    AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text("general.date_picker".translate()) },
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
                        IconButton(onClick = { year-- }) {
                            Icon(AppIcons.ArrowDropDown, null, modifier = Modifier.rotate(90f))
                        }
                        Text(year.toString(), style = MaterialTheme.typography.h6)
                        IconButton(onClick = { year++ }) {
                            Icon(AppIcons.ArrowDropDown, null, modifier = Modifier.rotate(270f))
                        }
                    }

                    // Mois
                    Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { if (month > 1) month-- }) {
                            Icon(AppIcons.ArrowDropDown, null, modifier = Modifier.rotate(90f))
                        }
                        Text(month.toString().padStart(2, '0'), style = MaterialTheme.typography.h6)
                        IconButton(onClick = { if (month < 12) month++ }) {
                            Icon(AppIcons.ArrowDropDown, null, modifier = Modifier.rotate(270f))
                        }
                    }

                    // Jour
                    Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { if (day > 1) day-- }) {
                            Icon(AppIcons.ArrowDropDown, null, modifier = Modifier.rotate(90f))
                        }
                        Text(day.toString().padStart(2, '0'), style = MaterialTheme.typography.h6)
                        IconButton(
                                onClick = {
                                    val lastDayOfMonth =
                                            LocalDate(year, month, 1)
                                                    .daysUntil(
                                                            LocalDate(
                                                                    year,
                                                                    if (month < 12) month + 1
                                                                    else 1,
                                                                    1
                                                            )
                                                    )
                                    if (day < lastDayOfMonth) day++
                                }
                        ) { Icon(AppIcons.ArrowDropDown, null, modifier = Modifier.rotate(270f)) }
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
                        }
                ) { Text("general.validate".translate()) }
            },
            dismissButton = {
                Button(onClick = onDismissRequest) { Text("general.cancel".translate()) }
            }
    )
}
