package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        var useDryMatterPer100g by remember {
                mutableStateOf(false)
        } // Toggle pour /1000 kcal vs /100g MS

        Column(
                modifier =
                        modifier.fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(AppSizes.paddingMedium),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
                // En-tête avec sélecteur de type de graphique
                GraphiqueHeader(
                        selectedChart = selectedChart,
                        onChartSelected = { selectedChart = it }
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
                                                if (!useDryMatterPer100g) VetNutriColors.Primary
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
                                                if (useDryMatterPer100g) VetNutriColors.Primary
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
                            onActivateConeAction = { d, w, t -> weightConeState = WeightConeState(d, w, t) },
                            onClearCone = { weightConeState = null }
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
