package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.CrossConsultationAnalysisViewModel
import kotlin.math.roundToInt
import io.github.koalaplot.core.bar.DefaultVerticalBar
import io.github.koalaplot.core.bar.VerticalBarPlot
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.XYGraph
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi

private data class MetricOption(
        val key: String,
        val label: String,
        val unit: String,
        val extractor: (CrossConsultationAnalysisViewModel.RationSummary) -> Float
)

private val METRICS =
        listOf(
                MetricOption(
                        key = "qty_total",
                        label = "Quantité totale",
                        unit = "g",
                        extractor = { it.quantity.toFloat().coerceAtLeast(0f) }
                ),
                MetricOption(
                        key = "energy_total",
                        label = "Énergie totale",
                        unit = "kcal",
                        extractor = {
                            val energy = (it.energyDensity * it.quantity / 100.0).toFloat()
                            energy.coerceAtLeast(0f)
                        }
                ),
                MetricOption(
                        key = "proteins_mcal",
                        label = "Protéines",
                        unit = "g/Mcal BEE",
                        extractor = {
                            val bee = it.beeKcal ?: 0.0
                            if (bee > 0.0) {
                                (it.proteins.toFloat() / (bee / 1000.0).toFloat())
                            } else {
                                val energy = (it.energyDensity * it.quantity / 100.0).toFloat()
                                if (energy <= 0f) 0f else (it.proteins.toFloat() / energy * 1000f)
                            }
                        }
                ),
                MetricOption(
                        key = "lipids_mcal",
                        label = "Lipides",
                        unit = "g/Mcal BEE",
                        extractor = {
                            val bee = it.beeKcal ?: 0.0
                            if (bee > 0.0) {
                                (it.lipids.toFloat() / (bee / 1000.0).toFloat())
                            } else {
                                val energy = (it.energyDensity * it.quantity / 100.0).toFloat()
                                if (energy <= 0f) 0f else (it.lipids.toFloat() / energy * 1000f)
                            }
                        }
                ),
                MetricOption(
                        key = "lipids",
                        label = "Lipides",
                        unit = "g",
                        extractor = { it.lipids.toFloat().coerceAtLeast(0f) }
                ),
                MetricOption(
                        key = "ratio_cap",
                        label = "Ca/P",
                        unit = "ratio",
                        extractor = { it.ratioCaP.toFloat().coerceAtLeast(0f) }
                ),
                MetricOption(
                        key = "ratio_o6o3",
                        label = "O6/O3",
                        unit = "ratio",
                        extractor = { it.ratioOmega6Omega3.toFloat().coerceAtLeast(0f) }
                ),
                MetricOption(
                        key = "ration_count",
                        label = "Nb rations",
                        unit = "rations",
                        extractor = { 1f }
                ),
                MetricOption(
                        key = "qty_moy",
                        label = "Quantité moy./ration",
                        unit = "g/ration",
                        extractor = {
                            val qty = it.quantity.toFloat()
                            qty.coerceAtLeast(0f)
                        }
                )
        )

/**
 * Vue résultats (phase 2 initiale) :
 * - Sépare les consultations actuelles sélectionnées des propositions (placeholder).
 * - Prépare l'espace pour les analyses/graphes futurs.
 */
@Composable
fun CrossConsultationResultsView(
        viewModel: CrossConsultationAnalysisViewModel,
        onNavigateBack: () -> Unit,
        modifier: Modifier = Modifier
) {
    val selected by viewModel.selectedIds.collectAsState()
    var showActual by remember { mutableStateOf(true) }
    var selectedMetric by remember { mutableStateOf(METRICS.first()) }
    val displayedRations = viewModel.getSelectedRations(actualOnly = showActual)

    Column(modifier = modifier.fillMaxSize()) {
        TopBarSimple(
                title = "Analyse des consultations",
                onNavigateBack = onNavigateBack
        ) {
            OutlinedButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Retour sélection")
            }
        }
        Divider()

        if (selected.isEmpty()) {
            Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
            ) {
                Text("Aucune sélection active. Revenez sur l'écran précédent.")
            }
            return
        }

        Row(
                modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
            Column(
                    modifier = Modifier.weight(0.35f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
            ) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                    Text("Rations", style = androidx.compose.material.MaterialTheme.typography.h6)
                    OutlinedButton(onClick = { showActual = true }, enabled = !showActual) {
                        Text("Actuelles")
                    }
                    OutlinedButton(onClick = { showActual = false }, enabled = showActual) {
                        Text("Proposées")
                    }
                }
                Divider()
                if (displayedRations.isEmpty()) {
                    Text(
                            if (showActual) "Aucune ration actuelle sélectionnée."
                            else "Aucune ration proposée sélectionnée."
                    )
                } else {
                    RationList(items = displayedRations)
                }
            }

            Column(
                    modifier = Modifier.weight(0.65f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
            ) {
                Text(
                        "Aperçu graphique (KoalaPlot)",
                        style = androidx.compose.material.MaterialTheme.typography.subtitle1
                )
                Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    METRICS.forEach { option ->
                        OutlinedButton(
                                onClick = { selectedMetric = option },
                                enabled = selectedMetric.key != option.key
                        ) { Text("${option.label} (${option.unit})") }
                    }
                }
                ConsultationBarChart(
                        items = displayedRations,
                        metric = selectedMetric,
                        modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RationList(items: List<CrossConsultationAnalysisViewModel.RationSummary>) {
    LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
    ) {
        items(items) { item ->
            Column(
                    modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingSmall),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(item.name, style = androidx.compose.material.MaterialTheme.typography.subtitle1)
                Text(
                        "${item.animalName} • ${item.consultationDate}",
                        style = androidx.compose.material.MaterialTheme.typography.body2
                )
                Text(
                        "Réf: ${item.referenceLabel ?: "Aucune"} | Espèce: ${item.speciesLabel} | Quantité: ${item.quantity.roundToInt()} g | Statut: ${if (item.actual) "Actuelle" else "Proposée"}",
                        style = androidx.compose.material.MaterialTheme.typography.caption
                )
                Divider()
            }
        }
    }
}

@Composable
@OptIn(ExperimentalKoalaPlotApi::class)
private fun ConsultationBarChart(
        items: List<CrossConsultationAnalysisViewModel.RationSummary>,
        metric: MetricOption,
        modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Text(
                "Aucune donnée à tracer. Sélectionnez des rations.",
                style = androidx.compose.material.MaterialTheme.typography.body2,
                modifier = Modifier.padding(horizontal = AppSizes.paddingMedium)
        )
        return
    }
    val categories = items.mapIndexed { index, item -> "${index + 1}. ${item.name}" }
    val values = items.map { item -> metric.extractor(item) }
    val maxY = (values.maxOrNull() ?: 1f).let { if (it <= 0f) 1f else it * 1.2f }
    XYGraph(
            xAxisModel = CategoryAxisModel(categories),
            yAxisModel = FloatLinearAxisModel(0f..maxY),
            yAxisTitle = "${metric.label} (${metric.unit})",
            modifier = modifier.padding(horizontal = AppSizes.paddingMedium)
    ) {
        VerticalBarPlot(
                xData = categories,
                yData = values,
                bar = { index ->
                    val ration = items[index]
                    val color =
                            if (ration.actual) VetNutriColors.Primary
                            else VetNutriColors.Secondary
                    DefaultVerticalBar(SolidColor(color))
                }
        )
    }
}
