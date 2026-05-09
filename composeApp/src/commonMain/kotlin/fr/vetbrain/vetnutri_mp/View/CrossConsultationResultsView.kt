package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Canvas
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Components.TooltipBubble
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import fr.vetbrain.vetnutri_mp.ExcelPlatform.isCsvFileOperationsSupported
import fr.vetbrain.vetnutri_mp.ExcelPlatform.saveCsvFileForExport
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.NumberUtils
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
        val extractor: (CrossConsultationAnalysisViewModel.RationSummary) -> Float,
        val kind: MetricKind
)

private enum class MetricKind {
        NUTRIENT,
        RATIO
}

private enum class ApportExpression(val label: String) {
        ABSOLU("Absolu"),
        PER_MCAL_RATION("/Mcal de ration"),
        PER_100G_RATION("/100g de ration"),
        PER_MCAL_REF("/Mcal de besoin énergétique de référence"),
        PER_KG_ANIMAL("/kg de poids d'animal"),
        PER_KG_METAB("/kg de poids métabolique d'animal")
}

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
                                    },
                                    kind = MetricKind.NUTRIENT
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
                                    },
                                    kind = MetricKind.NUTRIENT
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
                                    },
                                    kind = MetricKind.NUTRIENT
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
                                    },
                                    kind = MetricKind.NUTRIENT
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
                                    },
                                    kind = MetricKind.NUTRIENT
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
                        extractor = { (it.ratioValues[r.label] ?: 0.0).toFloat().coerceAtLeast(0f) },
                        kind = MetricKind.RATIO
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
    var selectedExpression by remember { mutableStateOf(ApportExpression.ABSOLU) }
    var showBoxplot by remember { mutableStateOf(false) }
    var showPoints by remember { mutableStateOf(true) }
    val displayedRations = viewModel.getSelectedRations(actualOnly = showActual)
    val actualRations = viewModel.getSelectedRations(actualOnly = true)
    val proposedRations = viewModel.getSelectedRations(actualOnly = false)

    Column(modifier = modifier.fillMaxSize()) {
        TopBarSimple(
                title = "Analyse des consultations",
                onNavigateBack = onNavigateBack
        ) {
            OutlinedButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
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
                var rightTabIndex by remember { mutableStateOf(0) }
                TabRow(
                        selectedTabIndex = rightTabIndex,
                        backgroundColor = MaterialTheme.colors.surface,
                        contentColor = VetNutriColors.Primary
                ) {
                    Tab(selected = rightTabIndex == 0, onClick = { rightTabIndex = 0 },
                            text = { Text("Graphique") })
                    Tab(selected = rightTabIndex == 1, onClick = { rightTabIndex = 1 },
                            text = { Text("Statistiques") })
                }

                if (rightTabIndex == 0) {
                    // --- Onglet Graphique ---
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
                    DropdownField(
                            label = "Expression des apports",
                            selectedValue = selectedExpression,
                            options = ApportExpression.entries,
                            onValueChange = { selectedExpression = it },
                            valueToString = { it.label },
                            modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)) {
                        OutlinedButton(onClick = { showBoxplot = false }, enabled = showBoxplot) {
                            Text("Barres")
                        }
                        OutlinedButton(onClick = { showBoxplot = true }, enabled = !showBoxplot) {
                            Text("Boxplots")
                        }
                        if (showBoxplot) {
                            OutlinedButton(onClick = { showPoints = !showPoints }) {
                                Text(if (showPoints) "Points: on" else "Points: off")
                            }
                        }
                    }
                    if (showBoxplot) {
                        BoxPlotChart(
                                actualItems = actualRations,
                                proposedItems = proposedRations,
                                showPoints = showPoints,
                                metric = selectedMetric,
                                expression = selectedExpression,
                                modifier = Modifier.fillMaxWidth().weight(1f)
                        )
                    } else {
                        ConsultationBarChart(
                                items = displayedRations,
                                metric = selectedMetric,
                                expression = selectedExpression,
                                modifier = Modifier.fillMaxWidth().weight(1f)
                        )
                    }
                } else {
                    // --- Onglet Statistiques ---
                    NutrientStatsPanel(
                            viewModel = viewModel,
                            showActual = showActual,
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
        expression: ApportExpression,
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
    val values = items.map { item ->
        val raw = metric.extractor(item)
        applyExpression(item, metric, raw, expression)
    }
    val maxY = (values.maxOrNull() ?: 1f).let { if (it <= 0f) 1f else it * 1.2f }
    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }
    Box(modifier = modifier.padding(horizontal = AppSizes.paddingMedium)) {
        XYGraph(
                xAxisModel = CategoryAxisModel(categories),
                yAxisModel = FloatLinearAxisModel(0f..maxY),
                yAxisTitle = metricTitle(metric, expression),
                modifier = Modifier.fillMaxSize()
        ) {
            VerticalBarPlot(
                    xData = categories,
                    yData = values,
                    bar = { index ->
                        val ration = items[index]
                        val color =
                                if (ration.actual) VetNutriColors.Primary
                                else VetNutriColors.Secondary
                        val tooltip = buildRationTooltip(ration, metric, values[index], expression)
                        Box(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .clickable {
                                                    selectedBarIndex =
                                                            if (selectedBarIndex == index) null else index
                                                }
                        ) {
                            DefaultVerticalBar(
                                    brush = SolidColor(color),
                                    modifier = Modifier.fillMaxSize()
                            )
                            if (selectedBarIndex == index) {
                                TooltipBubble(
                                        text = tooltip,
                                        onDismiss = { selectedBarIndex = null }
                                )
                            }
                        }
                    }
            )
        }
    }
}

private data class BoxPlotStats(
        val min: Float,
        val q1: Float,
        val median: Float,
        val q3: Float,
        val max: Float
)

private data class BoxPlotPoint(
        val item: CrossConsultationAnalysisViewModel.RationSummary,
        val value: Float,
        val color: Color
)

private data class BoxPlotGroup(
        val label: String,
        val color: Color,
        val stats: BoxPlotStats?,
        val points: List<BoxPlotPoint>
)

private fun applyExpression(
        item: CrossConsultationAnalysisViewModel.RationSummary,
        metric: MetricOption,
        rawValue: Float,
        expression: ApportExpression
): Float {
    if (metric.kind == MetricKind.RATIO) return rawValue
    return when (expression) {
        ApportExpression.ABSOLU -> rawValue
        ApportExpression.PER_MCAL_RATION -> {
            val mcal = (item.energyTotalKcal / 1000.0).toFloat()
            if (mcal > 0f) rawValue / mcal else 0f
        }
        ApportExpression.PER_100G_RATION -> {
            val base = (item.quantity / 100.0).toFloat()
            if (base > 0f) rawValue / base else 0f
        }
        ApportExpression.PER_MCAL_REF -> {
            val mcalRef = ((item.beeKcal ?: 0.0) / 1000.0).toFloat()
            if (mcalRef > 0f) rawValue / mcalRef else 0f
        }
        ApportExpression.PER_KG_ANIMAL -> {
            val w = item.animalWeightKg.toFloat()
            if (w > 0f) rawValue / w else 0f
        }
        ApportExpression.PER_KG_METAB -> {
            val w = item.animalMetabolicWeightKg.toFloat()
            if (w > 0f) rawValue / w else 0f
        }
    }
}

private fun metricTitle(metric: MetricOption, expression: ApportExpression): String {
    if (metric.kind == MetricKind.RATIO || expression == ApportExpression.ABSOLU) {
        return "${metric.label} (${metric.unit})"
    }
    return "${metric.label} (${metric.unit}) • ${expression.label}"
}

private fun buildRationTooltip(
        item: CrossConsultationAnalysisViewModel.RationSummary,
        metric: MetricOption,
        value: Float,
        expression: ApportExpression
): String {
    return buildString {
        append(item.name)
        append("\n")
        append(item.animalName)
        append(" • ")
        append(item.consultationDate)
        append("\n")
        append(metric.label)
        append(": ")
        append(formatAxisValue(value))
        if (metric.unit.isNotBlank()) {
            append(" ")
            append(metric.unit)
        }
        if (metric.kind == MetricKind.NUTRIENT && expression != ApportExpression.ABSOLU) {
            append(" • ")
            append(expression.label)
        }
        if (item.ingredients.isNotEmpty()) {
            append("\nIngrédients:")
            item.ingredients.forEach { ingredient ->
                append("\n- ")
                append(ingredient.name)
                append(": ")
                append(formatAxisValue(ingredient.quantity.toFloat()))
                append(" g")
            }
        }
        append("\n")
        append(if (item.actual) "Actuelle" else "Proposée")
    }
}

@Composable
private fun BoxPlotChart(
        actualItems: List<CrossConsultationAnalysisViewModel.RationSummary>,
        proposedItems: List<CrossConsultationAnalysisViewModel.RationSummary>,
        showPoints: Boolean,
        metric: MetricOption,
        expression: ApportExpression,
        modifier: Modifier = Modifier
) {
    if (actualItems.isEmpty() && proposedItems.isEmpty()) {
        Text(
                "Aucune donnée à tracer. Sélectionnez des rations.",
                style = androidx.compose.material.MaterialTheme.typography.body2,
                modifier = Modifier.padding(horizontal = AppSizes.paddingMedium)
        )
        return
    }

    val actualValues =
            actualItems
                    .map { item -> applyExpression(item, metric, metric.extractor(item), expression) }
                    .filter { it.isFinite() }
    val proposedValues =
            proposedItems
                    .map { item -> applyExpression(item, metric, metric.extractor(item), expression) }
                    .filter { it.isFinite() }
    if (actualValues.isEmpty() && proposedValues.isEmpty()) {
        Text(
                "Aucune donnée à tracer. Sélectionnez des rations.",
                style = androidx.compose.material.MaterialTheme.typography.body2,
                modifier = Modifier.padding(horizontal = AppSizes.paddingMedium)
        )
        return
    }

    val groups = listOf(
            BoxPlotGroup(
                    label = "Actuelles",
                    color = VetNutriColors.Primary,
                    stats = actualValues.takeIf { it.isNotEmpty() }?.let { computeBoxPlot(it) },
                    points = actualItems.map { item ->
                        BoxPlotPoint(
                                item = item,
                                value = applyExpression(item, metric, metric.extractor(item), expression),
                                color = VetNutriColors.Primary
                        )
                    }
            ),
            BoxPlotGroup(
                    label = "Proposées",
                    color = VetNutriColors.Secondary,
                    stats = proposedValues.takeIf { it.isNotEmpty() }?.let { computeBoxPlot(it) },
                    points = proposedItems.map { item ->
                        BoxPlotPoint(
                                item = item,
                                value = applyExpression(item, metric, metric.extractor(item), expression),
                                color = VetNutriColors.Secondary
                        )
                    }
            )
    )

    val allValues = groups.flatMap { group ->
        group.stats?.let { listOf(it.min, it.q1, it.median, it.q3, it.max) } ?: emptyList()
    }
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

        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val paddingTopPx = paddingTop.toPx()
            val paddingBottomPx = paddingBottom.toPx()
            val paddingSide = 12.dp.toPx()
            val chartHeight = size.height - paddingTopPx - paddingBottomPx
            val chartWidth = size.width - paddingSide * 2
            val slotWidth = chartWidth / groups.size.toFloat()
            val boxWidth = slotWidth * 0.4f

            fun yFor(value: Float): Float {
                val normalized = (value - minValue) / range
                return paddingTopPx + chartHeight * (1f - normalized)
            }

            ticks.forEach { value ->
                val y = yFor(value)
                drawLine(
                        color = Color.LightGray.copy(alpha = 0.35f),
                        start = androidx.compose.ui.geometry.Offset(paddingSide, y),
                        end = androidx.compose.ui.geometry.Offset(paddingSide + chartWidth, y),
                        strokeWidth = 1f
                )
            }

            groups.forEachIndexed { index, group ->
                val stats = group.stats ?: return@forEachIndexed
                val centerX = paddingSide + slotWidth * (index + 0.5f)
                val yMin = yFor(stats.min)
                    val yMax = yFor(stats.max)
                    val yQ1 = yFor(stats.q1)
                    val yQ3 = yFor(stats.q3)
                    val yMedian = yFor(stats.median)

                    val boxLeft = centerX - boxWidth / 2f
                    val boxRight = centerX + boxWidth / 2f

                    drawLine(
                            color = group.color,
                            start = androidx.compose.ui.geometry.Offset(centerX, yMin),
                            end = androidx.compose.ui.geometry.Offset(centerX, yMax),
                            strokeWidth = 2f
                    )
                    drawLine(
                            color = group.color,
                            start = androidx.compose.ui.geometry.Offset(centerX - boxWidth / 3f, yMin),
                            end = androidx.compose.ui.geometry.Offset(centerX + boxWidth / 3f, yMin),
                            strokeWidth = 2f
                    )
                    drawLine(
                            color = group.color,
                            start = androidx.compose.ui.geometry.Offset(centerX - boxWidth / 3f, yMax),
                            end = androidx.compose.ui.geometry.Offset(centerX + boxWidth / 3f, yMax),
                            strokeWidth = 2f
                    )

                    drawRect(
                            color = group.color.copy(alpha = 0.2f),
                            topLeft = androidx.compose.ui.geometry.Offset(boxLeft, yQ3),
                            size = androidx.compose.ui.geometry.Size(boxRight - boxLeft, yQ1 - yQ3)
                    )
                    drawRect(
                            color = group.color,
                            topLeft = androidx.compose.ui.geometry.Offset(boxLeft, yQ3),
                            size = androidx.compose.ui.geometry.Size(boxRight - boxLeft, yQ1 - yQ3),
                            style = Stroke(width = 2f)
                    )
                    drawLine(
                            color = group.color,
                            start = androidx.compose.ui.geometry.Offset(boxLeft, yMedian),
                            end = androidx.compose.ui.geometry.Offset(boxRight, yMedian),
                            strokeWidth = 2f
                    )
                }
            }

            if (showPoints) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val density = LocalDensity.current
                val widthPx = with(density) { maxWidth.toPx() }
                val heightPx = with(density) { maxHeight.toPx() }
                val paddingTopPx = with(density) { paddingTop.toPx() }
                val paddingBottomPx = with(density) { paddingBottom.toPx() }
                val paddingSidePx = with(density) { 12.dp.toPx() }
                val chartHeight = heightPx - paddingTopPx - paddingBottomPx
                val chartWidth = widthPx - paddingSidePx * 2
                val slotWidth = chartWidth / groups.size.toFloat()
                val boxWidth = slotWidth * 0.4f
                val maxJitterPx = with(density) { 24.dp.toPx() }
                val pointSize =
                        if (groups.sumOf { it.points.size } > 30) 6.dp else 8.dp
                val pointRadiusPx = with(density) { (pointSize / 2f).toPx() }
                val densityFactor =
                        (groups.sumOf { it.points.size }.toFloat() / 20f).coerceAtLeast(1f)
                val jitterMax = (boxWidth * 0.35f / densityFactor).coerceAtMost(maxJitterPx)

                fun yFor(value: Float): Float {
                    val normalized = (value - minValue) / range
                    return paddingTopPx + chartHeight * (1f - normalized)
                }

                groups.forEachIndexed { groupIndex, group ->
                    val points = group.points.filter { it.value.isFinite() }
                    val orderedPoints = points.sortedBy { it.item.rationId }
                    val jitterStep =
                            if (orderedPoints.size <= 1) 0f
                            else (2f * jitterMax) / (orderedPoints.size - 1).toFloat()
                    orderedPoints.forEachIndexed { index, point ->
                        val jitter =
                                if (orderedPoints.size <= 1) 0f else (-jitterMax + jitterStep * index)
                        val centerX = paddingSidePx + slotWidth * (groupIndex + 0.5f) + jitter
                        val centerY = yFor(point.value)
                        val clampedX =
                                centerX.coerceIn(
                                        paddingSidePx + pointRadiusPx,
                                        paddingSidePx + chartWidth - pointRadiusPx
                                )
                        val clampedY =
                                centerY.coerceIn(
                                        paddingTopPx + pointRadiusPx,
                                        paddingTopPx + chartHeight - pointRadiusPx
                                )
                        val tooltip = buildRationTooltip(point.item, metric, point.value, expression)

                        PointWithTooltip(
                                tooltip = tooltip,
                                color = point.color,
                                modifier =
                                        Modifier.offset {
                                            IntOffset(
                                                    (clampedX - pointRadiusPx).roundToInt(),
                                                    (clampedY - pointRadiusPx).roundToInt()
                                            )
                                        }
                                                .zIndex(1f),
                                size = pointSize,
                                alpha =
                                        if (groups.sumOf { it.points.size } > 50) 0.7f else 1f
                        )
                    }
                }
            }
            }
        }
    }

    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSizes.paddingMedium),
            horizontalArrangement = Arrangement.Center
    ) {
        groups.forEachIndexed { index, group ->
            Row(
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(modifier = Modifier.width(10.dp).height(10.dp)) {
                    drawRect(color = group.color)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                        group.label,
                        style = androidx.compose.material.MaterialTheme.typography.caption
                )
            }
            if (index < groups.lastIndex) {
                Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
            }
        }
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

@Composable
private fun PointWithTooltip(
        tooltip: String,
        color: Color,
        modifier: Modifier = Modifier,
        size: androidx.compose.ui.unit.Dp = 8.dp,
        alpha: Float = 1f
) {
    var showTooltip by remember { mutableStateOf(false) }

    val hitSize = if (size < 16.dp) 16.dp else size

    Box(
            modifier =
                    modifier
                            .size(hitSize)
                            .clickable { showTooltip = !showTooltip }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val radius = (size / 2f).toPx()
            drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = radius,
                    center = androidx.compose.ui.geometry.Offset(
                            this.size.width / 2f,
                            this.size.height / 2f
                    )
            )
        }
        if (showTooltip) {
            TooltipBubble(
                    text = tooltip,
                    onDismiss = {
                        showTooltip = false
                    }
            )
        }
    }
}

@Composable
private fun NutrientStatsPanel(
        viewModel: CrossConsultationAnalysisViewModel,
        showActual: Boolean,
        modifier: Modifier = Modifier
) {
    val stats = remember(viewModel.selectedIds.collectAsState().value, showActual) {
        viewModel.computeNutrientStats(actualOnly = showActual)
    }

    Column(modifier = modifier) {
        if (isCsvFileOperationsSupported()) {
            Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                        onClick = {
                            val csv = buildStatsCsv(stats)
                            saveCsvFileForExport(csv, "statistiques_nutriments.csv")
                        },
                        enabled = stats.isNotEmpty()
                ) {
                    Text("Exporter statistiques CSV", fontSize = 12.sp)
                }
            }
        }

        if (stats.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                        "Aucune donnée disponible. Sélectionnez des rations.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.body2
                )
            }
            return@Column
        }

        // En-tête
        Row(
                modifier = Modifier.fillMaxWidth()
                        .background(MaterialTheme.colors.surface)
                        .padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
            Text("Nutriment", fontWeight = FontWeight.Bold, fontSize = 12.sp,
                    modifier = Modifier.weight(2.5f))
            Text("Moy ± ET", fontWeight = FontWeight.Bold, fontSize = 12.sp,
                    modifier = Modifier.weight(2f))
            Text("Médiane", fontWeight = FontWeight.Bold, fontSize = 12.sp,
                    modifier = Modifier.weight(1.5f))
            Text("Min", fontWeight = FontWeight.Bold, fontSize = 12.sp,
                    modifier = Modifier.weight(1f))
            Text("Max", fontWeight = FontWeight.Bold, fontSize = 12.sp,
                    modifier = Modifier.weight(1f))
        }
        Divider()

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(stats) { stat ->
                Row(
                        modifier = Modifier.fillMaxWidth()
                                .padding(vertical = 3.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stat.name, fontSize = 11.sp, modifier = Modifier.weight(2.5f),
                            maxLines = 2)
                    Text(
                            "${formatStatValue(stat.mean)} ± ${formatStatValue(stat.stdDev)}",
                            fontSize = 11.sp, modifier = Modifier.weight(2f)
                    )
                    Text(formatStatValue(stat.median), fontSize = 11.sp,
                            modifier = Modifier.weight(1.5f))
                    Text(formatStatValue(stat.min), fontSize = 11.sp,
                            modifier = Modifier.weight(1f))
                    Text(formatStatValue(stat.max), fontSize = 11.sp,
                            modifier = Modifier.weight(1f))
                }
                Divider(color = Color.LightGray, thickness = 0.5.dp)
            }
        }
    }
}

private fun formatStatValue(v: Double): String =
        if (v < 10.0) NumberUtils.format(v, 2) else NumberUtils.format(v, 1)

private fun buildStatsCsv(stats: List<CrossConsultationAnalysisViewModel.NutrientStat>): String {
    val sb = StringBuilder()
    sb.appendLine("Nutriment;Moyenne;Écart-type;Médiane;Min;Max")
    stats.forEach { s ->
        sb.appendLine("${s.name};${s.mean};${s.stdDev};${s.median};${s.min};${s.max}")
    }
    return sb.toString()
}
