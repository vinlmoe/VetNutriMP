package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Canvas
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.CrossConsultationAnalysisViewModel
import kotlin.math.roundToInt
import kotlin.math.floor
import kotlin.math.ceil
import androidx.compose.foundation.layout.heightIn
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

private fun buildMetricOptions(): List<MetricOption> {
    val nutrients =
            buildList {
                NutrientMain.entries.forEach { n ->
                    add(
                            MetricOption(
                                    key = "n:${n.label}",
                                    label = n.nameToString(),
                                    unit = n.unite,
                                    extractor = {
                                        (it.nutrientValues[n.label] ?: 0.0).toFloat().coerceAtLeast(0f)
                                    }
                            )
                    )
                }
                NutrientMacro.entries.forEach { n ->
                    add(
                            MetricOption(
                                    key = "n:${n.label}",
                                    label = n.nameToString(),
                                    unit = n.unite,
                                    extractor = {
                                        (it.nutrientValues[n.label] ?: 0.0).toFloat().coerceAtLeast(0f)
                                    }
                            )
                    )
                }
                NutrientMin.entries.forEach { n ->
                    add(
                            MetricOption(
                                    key = "n:${n.label}",
                                    label = n.nameToString(),
                                    unit = n.unite,
                                    extractor = {
                                        (it.nutrientValues[n.label] ?: 0.0).toFloat().coerceAtLeast(0f)
                                    }
                            )
                    )
                }
                NutrientVitam.entries.forEach { n ->
                    add(
                            MetricOption(
                                    key = "n:${n.label}",
                                    label = n.displayName,
                                    unit = n.unite,
                                    extractor = {
                                        (it.nutrientValues[n.label] ?: 0.0).toFloat().coerceAtLeast(0f)
                                    }
                            )
                    )
                }
                NutrientLipid.entries.forEach { n ->
                    add(
                            MetricOption(
                                    key = "n:${n.label}",
                                    label = n.nameToString(),
                                    unit = n.unite,
                                    extractor = {
                                        (it.nutrientValues[n.label] ?: 0.0).toFloat().coerceAtLeast(0f)
                                    }
                            )
                    )
                }
            }

    val ratios =
            NutrientAnalysis.entries.map { r ->
                MetricOption(
                        key = "r:${r.label}",
                        label = r.displayName,
                        unit = if (r.unite.isBlank()) "ratio" else r.unite,
                        extractor = { (it.ratioValues[r.label] ?: 0.0).toFloat().coerceAtLeast(0f) }
                )
            }

    return nutrients + ratios
}

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
    val metricOptions = remember { buildMetricOptions() }
    var showActual by remember { mutableStateOf(true) }
    var metricMenuExpanded by remember { mutableStateOf(false) }
    var selectedMetricKey by remember { mutableStateOf(metricOptions.first().key) }
    val selectedMetric =
            metricOptions.firstOrNull { it.key == selectedMetricKey } ?: metricOptions.first()
    var showBoxplot by remember { mutableStateOf(false) }
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
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nutriment / rapport :")
                    Box {
                        OutlinedButton(onClick = { metricMenuExpanded = true }) {
                            Text("${selectedMetric.label} (${selectedMetric.unit})")
                        }
                        DropdownMenu(
                                expanded = metricMenuExpanded,
                                onDismissRequest = { metricMenuExpanded = false },
                                modifier = Modifier.heightIn(max = 360.dp)
                        ) {
                            metricOptions.forEach { option ->
                                DropdownMenuItem(
                                        onClick = {
                                            selectedMetricKey = option.key
                                            metricMenuExpanded = false
                                        }
                                ) { Text("${option.label} (${option.unit})") }
                            }
                        }
                    }
                }
                Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                    OutlinedButton(
                            onClick = { showBoxplot = false },
                            enabled = showBoxplot
                    ) { Text("Barres") }
                    OutlinedButton(
                            onClick = { showBoxplot = true },
                            enabled = !showBoxplot
                    ) { Text("Boxplots") }
                }
                if (showBoxplot) {
                    BoxPlotChart(
                            items = displayedRations,
                            metric = selectedMetric,
                            modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                } else {
                    ConsultationBarChart(
                            items = displayedRations,
                            metric = selectedMetric,
                            modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                }
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

private data class BoxPlotStats(
        val min: Float,
        val q1: Float,
        val median: Float,
        val q3: Float,
        val max: Float
)

@Composable
private fun BoxPlotChart(
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

    val values = items.map { metric.extractor(it) }.filter { it.isFinite() }
    if (values.isEmpty()) {
        Text(
                "Aucune donnée à tracer. Sélectionnez des rations.",
                style = androidx.compose.material.MaterialTheme.typography.body2,
                modifier = Modifier.padding(horizontal = AppSizes.paddingMedium)
        )
        return
    }

    val stats = computeBoxPlot(values)
    val allValues = listOf(stats.min, stats.q1, stats.median, stats.q3, stats.max)
    val minValue = allValues.minOrNull() ?: 0f
    val maxValue = allValues.maxOrNull() ?: 1f
    val range = (maxValue - minValue).let { if (it == 0f) 1f else it }

    Row(modifier = modifier.padding(horizontal = AppSizes.paddingMedium)) {
        val paddingTop = 12.dp
        val paddingBottom = 24.dp
        val axisWidth = 52.dp
        val tickCount = 5
        val ticks =
                (0 until tickCount).map { i ->
                    val t = 1f - (i.toFloat() / (tickCount - 1).toFloat())
                    minValue + range * t
                }

        Column(
                modifier = Modifier.width(axisWidth).fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(paddingTop))
            Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceBetween
            ) {
                ticks.forEach { value ->
                    Text(
                            formatAxisValue(value),
                            style = androidx.compose.material.MaterialTheme.typography.caption,
                            color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(paddingBottom))
        }

        Canvas(modifier = Modifier.weight(1f).fillMaxHeight()) {
            val paddingTopPx = paddingTop.toPx()
            val paddingBottomPx = paddingBottom.toPx()
            val paddingSide = 12.dp.toPx()
            val chartHeight = size.height - paddingTopPx - paddingBottomPx
            val chartWidth = size.width - paddingSide * 2
            val slotWidth = chartWidth
            val boxWidth = slotWidth * 0.4f

            fun yFor(value: Float): Float {
                val normalized = (value - minValue) / range
                return paddingTopPx + chartHeight * (1f - normalized)
            }

            val centerX = paddingSide + slotWidth / 2f
            val yMin = yFor(stats.min)
            val yMax = yFor(stats.max)
            val yQ1 = yFor(stats.q1)
            val yQ3 = yFor(stats.q3)
            val yMedian = yFor(stats.median)

            val boxLeft = centerX - boxWidth / 2f
            val boxRight = centerX + boxWidth / 2f

            drawLine(
                    color = VetNutriColors.Primary,
                    start = androidx.compose.ui.geometry.Offset(centerX, yMin),
                    end = androidx.compose.ui.geometry.Offset(centerX, yMax),
                    strokeWidth = 2f
            )
            drawLine(
                    color = VetNutriColors.Primary,
                    start = androidx.compose.ui.geometry.Offset(centerX - boxWidth / 3f, yMin),
                    end = androidx.compose.ui.geometry.Offset(centerX + boxWidth / 3f, yMin),
                    strokeWidth = 2f
            )
            drawLine(
                    color = VetNutriColors.Primary,
                    start = androidx.compose.ui.geometry.Offset(centerX - boxWidth / 3f, yMax),
                    end = androidx.compose.ui.geometry.Offset(centerX + boxWidth / 3f, yMax),
                    strokeWidth = 2f
            )

            drawRect(
                    color = VetNutriColors.Primary.copy(alpha = 0.2f),
                    topLeft = androidx.compose.ui.geometry.Offset(boxLeft, yQ3),
                    size = androidx.compose.ui.geometry.Size(boxRight - boxLeft, yQ1 - yQ3)
            )
            drawRect(
                    color = VetNutriColors.Primary,
                    topLeft = androidx.compose.ui.geometry.Offset(boxLeft, yQ3),
                    size = androidx.compose.ui.geometry.Size(boxRight - boxLeft, yQ1 - yQ3),
                    style = Stroke(width = 2f)
            )
            drawLine(
                    color = VetNutriColors.Primary,
                    start = androidx.compose.ui.geometry.Offset(boxLeft, yMedian),
                    end = androidx.compose.ui.geometry.Offset(boxRight, yMedian),
                    strokeWidth = 2f
            )
        }
    }

    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSizes.paddingMedium),
            horizontalArrangement = Arrangement.Center
    ) {
        Text(
                "Toutes rations",
                style = androidx.compose.material.MaterialTheme.typography.caption
        )
    }
}

private fun computeBoxPlot(values: List<Float>): BoxPlotStats {
    val sorted = values.sorted()
    val min = sorted.first()
    val max = sorted.last()
    val q1 = percentile(sorted, 0.25f)
    val median = percentile(sorted, 0.5f)
    val q3 = percentile(sorted, 0.75f)
    return BoxPlotStats(min = min, q1 = q1, median = median, q3 = q3, max = max)
}

private fun percentile(sorted: List<Float>, p: Float): Float {
    if (sorted.isEmpty()) return 0f
    if (sorted.size == 1) return sorted.first()
    val n = sorted.size
    val index = (n - 1) * p
    val lower = floor(index).toInt()
    val upper = ceil(index).toInt()
    if (lower == upper) return sorted[lower]
    val weight = index - lower
    return sorted[lower] + (sorted[upper] - sorted[lower]) * weight
}

private fun formatAxisValue(value: Float): String {
    val rounded = (value * 10f).roundToInt() / 10f
    return if (rounded == -0f) "0" else rounded.toString()
}
