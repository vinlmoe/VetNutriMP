package fr.vetbrain.vetnutri_mp.View.AnalyseGraphique

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Components.AppDatePicker
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Utils.isIosPlatform

@Composable
fun GraphCard(titre: String, sousTitre: String? = null, content: @Composable () -> Unit) {
        Card(modifier = Modifier.fillMaxWidth(), elevation = AppSizes.elevationMedium) {
                Column(
                        modifier = Modifier.padding(AppSizes.paddingMedium),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        Text(
                                text = titre,
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold,
                                color = VetNutriColors.Primary
                        )

                        if (sousTitre != null) {
                                Text(
                                        text = sousTitre,
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                        }

                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

                        content()
                }
        }
}

@Composable
fun GraphiqueHeader(
        selectedChart: ChartType,
        onChartSelected: (ChartType) -> Unit,
        isCompact: Boolean = false
) {
        Card(modifier = Modifier.fillMaxWidth(), elevation = AppSizes.elevationSmall) {
                Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = VetNutriColors.Primary
                                )
                                Text(
                                        text = translate(LocalizationKeys.Graph.GRAPHIC_ANALYSIS_TITLE),
                                        style = MaterialTheme.typography.h6,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Primary
                                )
                        }

                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

                        // Sélecteur de type de graphique avec layout adapté aux écrans étroits
                        if (isCompact) {
                                Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingSmall)
                                ) {
                                        ChartType.values().forEach { chartType ->
                                                Button(
                                                        onClick = { onChartSelected(chartType) },
                                                        colors =
                                                                ButtonDefaults.buttonColors(
                                                                        backgroundColor =
                                                                                if (selectedChart ==
                                                                                                chartType
                                                                                )
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                                else
                                                                                        MaterialTheme
                                                                                                .colors
                                                                                                .surface
                                                                ),
                                                        modifier = Modifier.fillMaxWidth()
                                                ) {
                                                        Text(
                                                                text = chartType.displayName,
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption,
                                                                color =
                                                                        if (selectedChart ==
                                                                                        chartType
                                                                        )
                                                                                Color.White
                                                                        else
                                                                                MaterialTheme.colors
                                                                                        .onSurface
                                                        )
                                                }
                                        }
                                }
                        } else {
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingSmall)
                                ) {
                                        ChartType.values().forEach { chartType ->
                                                Button(
                                                        onClick = { onChartSelected(chartType) },
                                                        colors =
                                                                ButtonDefaults.buttonColors(
                                                                        backgroundColor =
                                                                                if (selectedChart ==
                                                                                                chartType
                                                                                )
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                                else
                                                                                        MaterialTheme
                                                                                                .colors
                                                                                                .surface
                                                                ),
                                                        modifier = Modifier.weight(1f)
                                                ) {
                                                        Text(
                                                                text = chartType.displayName,
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption,
                                                                color =
                                                                        if (selectedChart ==
                                                                                        chartType
                                                                        )
                                                                                Color.White
                                                                        else
                                                                                MaterialTheme.colors
                                                                                        .onSurface
                                                        )
                                                }
                                        }
                                }
                        }
                }
        }
}

@Composable
fun GraphiqueLegend(selectedChart: ChartType) {
        Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = AppSizes.elevationSmall,
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.8f)
        ) {
                Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                        Text(
                                text = translate(LocalizationKeys.Graph.LEGEND_INFO_TITLE),
                                style = MaterialTheme.typography.subtitle2,
                                fontWeight = FontWeight.Bold,
                                color = VetNutriColors.Primary
                        )

                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                        val infoText =
                                when (selectedChart) {
                                        ChartType.EVOLUTION_POIDS ->
                                                translate(LocalizationKeys.Graph.LEGEND_INFO_EVOLUTION)
                                        ChartType.RATIONS_ENERGIE ->
                                                translate(LocalizationKeys.Graph.LEGEND_INFO_RATIONS)
                                        ChartType.DENSITE_RATIONS ->
                                                translate(LocalizationKeys.Graph.LEGEND_INFO_DENSITY)
                                        ChartType.NUTRIMENTS_RATIONS ->
                                                translate(LocalizationKeys.Graph.LEGEND_INFO_NUTRIMENTS)
                                }

                        Text(
                                text = infoText,
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                }
        }
}

@Composable
fun AddWeightForm(viewModel: AnimalDetailViewModel) {
        var selectedDate by remember {
                mutableStateOf(
                        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                )
        }
        var weightText by remember { mutableStateOf("") }
        var showDatePicker by remember { mutableStateOf(false) }

        Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = AppSizes.elevationSmall,
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.8f)
        ) {
                Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                        Text(
                                text = translate(LocalizationKeys.Graph.ADD_WEIGHT_TITLE),
                                style = MaterialTheme.typography.subtitle2,
                                fontWeight = FontWeight.Bold,
                                color = VetNutriColors.Primary
                        )

                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                        // Sélecteur de date
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text = "${translate(LocalizationKeys.Graph.DATE_PREFIX)}${selectedDate}",
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.body2
                                )

                                Button(
                                        onClick = { showDatePicker = true },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Primary
                                                )
                                ) { Text(translate(LocalizationKeys.Graph.PICK_DATE_BUTTON)) }
                        }

                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                        // Champ de poids
                        OutlinedTextField(
                                value = weightText,
                                onValueChange = { nouveauTexte ->
                                        // Filtrer pour n'accepter que les chiffres, point et virgule
                                        val texteFiltre =
                                                nouveauTexte.filter { char ->
                                                        char.isDigit() || char == '.' || char == ','
                                                }
                                        // S'assurer qu'il n'y a qu'un seul séparateur décimal
                                        val pointCount = texteFiltre.count { it == '.' }
                                        val virguleCount = texteFiltre.count { it == ',' }
                                        if (pointCount <= 1 && virguleCount <= 1 && pointCount + virguleCount <= 1) {
                                                weightText = texteFiltre
                                        }
                                },
                                label = { Text(translate(LocalizationKeys.Graph.WEIGHT_KG_LABEL)) },
                                keyboardOptions =
                                        KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                        )

                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

                        // Boutons d'action
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                        ) {
                                TextButton(onClick = { viewModel.stopAddingWeight() }) {
                                        Text(translate(LocalizationKeys.General.CANCEL))
                                }

                                Spacer(modifier = Modifier.width(AppSizes.paddingSmall))

                                val poidsValide = convertirTexteEnPoids(weightText)
                                val isPoidsValide = poidsValide != null && poidsValide > 0

                                Button(
                                        onClick = {
                                                val weight = convertirTexteEnPoids(weightText)
                                                if (weight != null && weight > 0) {
                                                        viewModel.addWeight(selectedDate, weight)
                                                        weightText = ""
                                                }
                                        },
                                        enabled = isPoidsValide,
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Primary
                                                )
                                ) { Text(translate(LocalizationKeys.General.ADD)) }
                        }
                }
        }

        // Date picker
        if (showDatePicker) {
                AppDatePicker(
                        selectedDate = selectedDate,
                        onDateSelected = {
                                selectedDate = it
                                showDatePicker = false
                        },
                        label = translate(LocalizationKeys.General.MEASURE_DATE)
                )
        }
}

/** Sélecteur de nutriment avec dropdown */
@Composable
fun NutrimentSelector(
        label: String,
        selectedNutriment: String?,
        onNutrimentSelected: (String) -> Unit,
        modifier: Modifier = Modifier
) {
        var expanded by remember { mutableStateOf(false) }
        Column(modifier = modifier) {
                Text(
                        text = label,
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold,
                        color = VetNutriColors.Primary
                )
                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        val selectedOption =
                                VIEW_NUTRIMENT_OPTIONS.find { it.key == selectedNutriment }
                        Text(
                                text = selectedOption?.displayName ?: translate(LocalizationKeys.General.SELECT_PLACEHOLDER),
                                style = MaterialTheme.typography.body2
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = translate(LocalizationKeys.General.EXPAND)
                        )
                }
                DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            if (!isIosPlatform) {
                                expanded = false
                            }
                        }
                ) {
                        VIEW_NUTRIMENT_OPTIONS.forEach { option ->
                                DropdownMenuItem(
                                        onClick = {
                                                onNutrimentSelected(option.key)
                                                expanded = false
                                        }
                                ) {
                                        Text(
                                                text = "${option.displayName} (${option.unit})",
                                                style = MaterialTheme.typography.body2
                                        )
                                }
                        }
                }
        }
}
