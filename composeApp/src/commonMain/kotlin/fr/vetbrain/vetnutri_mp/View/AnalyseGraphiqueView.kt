package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.View.AnalyseGraphique.*
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun AnalyseGraphiqueView(
        viewModel: AnimalDetailViewModel,
        equationRepository: EquationRepository? = null,
        modifier: Modifier = Modifier
) {
        var selectedChart by remember { mutableStateOf(ChartType.EVOLUTION_POIDS) }
        var weightConeState by remember { mutableStateOf<WeightConeState?>(null) }
        var minVariationPercent by remember { mutableStateOf<Double>(DEFAULT_MIN_VARIATION_PERCENT) }
        var maxVariationPercent by remember { mutableStateOf<Double>(DEFAULT_MAX_VARIATION_PERCENT) }
        var useDryMatterPer100g by remember {
                mutableStateOf(false)
        } // Toggle pour /1000 kcal vs /100g MS

        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
                val isCompact = maxWidth < 600.dp
                val contentPadding =
                        if (isCompact) AppSizes.paddingSmall else AppSizes.paddingMedium
                val verticalSpacing =
                        if (isCompact) AppSizes.paddingSmall else AppSizes.paddingMedium

                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(contentPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
                ) {
                        // En-tête avec sélecteur de type de graphique
                        GraphiqueHeader(
                                selectedChart = selectedChart,
                                onChartSelected = { selectedChart = it },
                                isCompact = isCompact
                        )

                        // Toggle pour /1000 kcal vs /100g MS (seulement pour les graphiques de densité)
                        if (selectedChart == ChartType.DENSITE_RATIONS) {
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                text = "/1000 kcal",
                                                style = MaterialTheme.typography.caption,
                                                color =
                                                        if (!useDryMatterPer100g)
                                                                VetNutriColors.Primary
                                                        else
                                                                MaterialTheme.colors.onSurface.copy(
                                                                        alpha = 0.7f
                                                                )
                                        )
                                        Switch(
                                                checked = useDryMatterPer100g,
                                                onCheckedChange = { useDryMatterPer100g = it }
                                        )
                                        Text(
                                                text = "/100g MS",
                                                style = MaterialTheme.typography.caption,
                                                color =
                                                        if (useDryMatterPer100g)
                                                                VetNutriColors.Primary
                                                        else
                                                                MaterialTheme.colors.onSurface.copy(
                                                                        alpha = 0.7f
                                                                )
                                        )
                                }
                        }

                        // Affichage du graphique sélectionné
                        when (selectedChart) {
                                ChartType.EVOLUTION_POIDS -> EvolutionPoidsChart(
                                        viewModel,
                                        weightConeState,
                                        onActivateConeAction = { d, w, t ->
                                                weightConeState = WeightConeState(d, w, t)
                                        },
                                        onClearCone = { weightConeState = null },
                                        minVariationPercent = minVariationPercent,
                                        maxVariationPercent = maxVariationPercent,
                                        onUpdateMinVariation = { minVariationPercent = it },
                                        onUpdateMaxVariation = { maxVariationPercent = it }
                                )
                                ChartType.RATIONS_ENERGIE ->
                                        RationsEnergieChart(viewModel, equationRepository)
                                ChartType.DENSITE_RATIONS ->
                                        DensiteRationsChart(
                                                viewModel,
                                                equationRepository,
                                                useDryMatterPer100g
                                        )
                                ChartType.NUTRIMENTS_RATIONS ->
                                        NutrimentsRationsChart(viewModel, equationRepository)
                        }

                        // Légende et informations
                        GraphiqueLegend(selectedChart)
                }
        }
}
