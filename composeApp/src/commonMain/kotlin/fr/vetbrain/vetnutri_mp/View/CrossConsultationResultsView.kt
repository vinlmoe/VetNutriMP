package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import fr.vetbrain.vetnutri_mp.Components.AdaptiveTabRow
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Components.FlowRow
import fr.vetbrain.vetnutri_mp.Components.PuceFiltre
import fr.vetbrain.vetnutri_mp.Components.TooltipBubble
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.nomTraduitNutriment
import fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import fr.vetbrain.vetnutri_mp.ExcelPlatform.isCsvFileOperationsSupported
import fr.vetbrain.vetnutri_mp.ExcelPlatform.saveCsvFileForExport
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.GraphFormattingUtils
import fr.vetbrain.vetnutri_mp.Utils.NumberUtils
import fr.vetbrain.vetnutri_mp.Utils.Recherche
import fr.vetbrain.vetnutri_mp.ViewModel.CrossConsultationAnalysisViewModel
import io.github.koalaplot.core.bar.DefaultVerticalBar
import io.github.koalaplot.core.bar.VerticalBarPlot
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.XYGraph
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private data class MetricOption(
        val key: String,
        val label: String,
        val unit: String,
        val extractor: (CrossConsultationAnalysisViewModel.RationSummary) -> Float,
        val kind: MetricKind,
        /** Groupe affiché dans le sélecteur (macronutriments, minéraux…). */
        val groupe: String
)

private enum class MetricKind {
        NUTRIENT,
        RATIO
}

private enum class ApportExpression {
        ABSOLU,
        PER_MCAL_RATION,
        PER_100G_RATION,
        PER_MCAL_REF,
        PER_KG_ANIMAL,
        PER_KG_METAB
}

private fun ApportExpression.libelle(): String = translate("crossConsultation.expression.$name")

/** Rations analysées : actuelles, proposées ou toutes. */
private enum class FiltreRations(val actualOnly: Boolean?) {
        ACTUELLES(true),
        PROPOSEES(false),
        TOUTES(null)
}

private val bordureResultats = Color(0x1F000000)
private val couleurActuelle = VetNutriColors.Primary
private val couleurProposee = Color(0xFFE07B39)

@Composable
private fun discretResultats(): Color = MaterialTheme.colors.onSurface.copy(alpha = 0.62f)

private fun buildMetricOptions(): List<MetricOption> {
    fun nutriment(n: fr.vetbrain.vetnutri_mp.Enumer.Nutrient, groupe: String) =
            MetricOption(
                    key = "n:${n.label}",
                    label = nomTraduitNutriment(n),
                    unit = n.unite,
                    extractor = { (it.nutrientValues[n.label] ?: 0.0).toFloat().coerceAtLeast(0f) },
                    kind = MetricKind.NUTRIENT,
                    groupe = groupe
            )
    val nutrients =
            NutrientMain.entries.map { nutriment(it, translate("crossConsultation.groupe.principaux")) } +
                    NutrientMacro.entries.map { nutriment(it, translate("crossConsultation.groupe.macro")) } +
                    NutrientMin.entries.map { nutriment(it, translate("crossConsultation.groupe.oligo")) } +
                    NutrientVitam.entries.map { nutriment(it, translate("crossConsultation.groupe.vitamines")) } +
                    NutrientLipid.entries.map { nutriment(it, translate("crossConsultation.groupe.lipides")) }
    val ratios =
            NutrientAnalysis.entries.map { r ->
                MetricOption(
                        key = "r:${r.label}",
                        label = r.displayName,
                        unit = if (r.unite.isBlank()) "ratio" else r.unite,
                        extractor = { (it.ratioValues[r.label] ?: 0.0).toFloat().coerceAtLeast(0f) },
                        kind = MetricKind.RATIO,
                        groupe = translate("crossConsultation.groupe.rapports")
                )
            }
    return nutrients + ratios
}

/**
 * Résultats de l'analyse croisée : rations des consultations sélectionnées, graphique d'un
 * nutriment ou d'un rapport (barres par ration ou boîtes à moustaches actuelles / proposées) et
 * statistiques comparées des rations actuelles et proposées.
 *
 * Grand écran : liste des rations à gauche, graphique et statistiques à droite. Sous
 * [AppSizes.breakpointCompact] : trois onglets.
 */
@Composable
fun CrossConsultationResultsView(
        viewModel: CrossConsultationAnalysisViewModel,
        onNavigateBack: () -> Unit,
        modifier: Modifier = Modifier
) {
    val selected by viewModel.selectedIds.collectAsState()
    val metricOptions = remember { buildMetricOptions() }
    var selectedMetricKey by remember { mutableStateOf("n:${NutrientMain.PROTEINE.label}") }
    val selectedMetric = metricOptions.firstOrNull { it.key == selectedMetricKey } ?: metricOptions.first()
    var selectedExpression by remember { mutableStateOf(ApportExpression.ABSOLU) }
    var filtre by remember { mutableStateOf(FiltreRations.ACTUELLES) }
    var showBoxplot by remember { mutableStateOf(false) }
    var showPoints by remember { mutableStateOf(true) }
    var trierParValeur by remember { mutableStateOf(false) }
    var onglet by remember { mutableStateOf(0) }

    val displayedRations = remember(selected, filtre) { viewModel.getSelectedRations(actualOnly = filtre.actualOnly) }
    val actualRations = remember(selected) { viewModel.getSelectedRations(actualOnly = true) }
    val proposedRations = remember(selected) { viewModel.getSelectedRations(actualOnly = false) }
    val valeur: (CrossConsultationAnalysisViewModel.RationSummary) -> Float = { applyExpression(it, selectedMetric, selectedMetric.extractor(it), selectedExpression) }

    Column(modifier = modifier.fillMaxSize()) {
        TopBarSimple(title = translate("auto.view.crossconsultationresultsview.analyse_des_consultations"), onNavigateBack = onNavigateBack)
        Divider()

        if (selected.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(translate("auto.view.crossconsultationresultsview.aucune_selection_active_revenez_sur_l_ecran_prec"), color = discretResultats())
            }
            return
        }

        BoxWithConstraints(Modifier.fillMaxSize()) {
            val largeur = maxWidth
            val compact = largeur < AppSizes.breakpointCompact

            @Composable
            fun Controles() {
                FlowRow(mainAxisSpacing = 6.dp, crossAxisSpacing = 6.dp, modifier = Modifier.padding(bottom = 6.dp)) {
                    FiltreRations.entries.forEach { f ->
                        val n = when (f) {
                            FiltreRations.ACTUELLES -> actualRations.size
                            FiltreRations.PROPOSEES -> proposedRations.size
                            FiltreRations.TOUTES -> actualRations.size + proposedRations.size
                        }
                        PuceFiltre(translate("crossConsultation.filtre.${f.name}", n.toString()), selectionnee = filtre == f, onClick = { filtre = f })
                    }
                }
            }

            @Composable
            fun Graphique(modifier: Modifier) {
                OngletGraphique(
                        viewModel = viewModel,
                        metricOptions = metricOptions,
                        metric = selectedMetric,
                        onMetric = { selectedMetricKey = it.key },
                        expression = selectedExpression,
                        onExpression = { selectedExpression = it },
                        showBoxplot = showBoxplot,
                        onBoxplot = { showBoxplot = it },
                        showPoints = showPoints,
                        onPoints = { showPoints = it },
                        trier = trierParValeur,
                        onTrier = { trierParValeur = it },
                        displayedRations = displayedRations,
                        actualRations = actualRations,
                        proposedRations = proposedRations,
                        valeur = valeur,
                        modifier = modifier
                )
            }

            if (compact) {
                Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Controles()
                    TabRow(selectedTabIndex = onglet, backgroundColor = MaterialTheme.colors.surface, contentColor = VetNutriColors.Primary) {
                        listOf("rations", "graphique", "statistiques").forEachIndexed { i, cle ->
                            Tab(selected = onglet == i, onClick = { onglet = i }, text = { Text(translate("crossConsultation.onglet.$cle"), fontSize = 12.sp, maxLines = 1) })
                        }
                    }
                    Box(Modifier.weight(1f).fillMaxWidth().padding(top = 8.dp)) {
                        when (onglet) {
                            0 -> RationList(displayedRations, selectedMetric, valeur, Modifier.fillMaxSize())
                            1 -> Graphique(Modifier.fillMaxSize())
                            else -> NutrientStatsPanel(viewModel, compact = true, modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            } else {
                Row(
                        modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                    Column(Modifier.width(if (largeur < 1100.dp) 280.dp else 340.dp).fillMaxHeight()) {
                        Text(translate("crossConsultation.rationsLabel"), style = MaterialTheme.typography.h6)
                        Spacer(Modifier.height(6.dp))
                        Controles()
                        RationList(displayedRations, selectedMetric, valeur, Modifier.weight(1f))
                    }
                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        AdaptiveTabRow(tabCount = 2, selectedTabIndex = if (onglet == 2) 1 else 0, backgroundColor = MaterialTheme.colors.surface) {
                            Tab(selected = onglet != 2, onClick = { onglet = 1 }, selectedContentColor = VetNutriColors.Primary,
                                    unselectedContentColor = discretResultats(),
                                    text = { Text(translate("crossConsultation.onglet.graphique")) })
                            Tab(selected = onglet == 2, onClick = { onglet = 2 }, selectedContentColor = VetNutriColors.Primary,
                                    unselectedContentColor = discretResultats(),
                                    text = { Text(translate("crossConsultation.onglet.statistiques")) })
                        }
                        Spacer(Modifier.height(8.dp))
                        if (onglet == 2) NutrientStatsPanel(viewModel, compact = false, modifier = Modifier.fillMaxWidth().weight(1f))
                        else Graphique(Modifier.fillMaxWidth().weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun OngletGraphique(
        viewModel: CrossConsultationAnalysisViewModel,
        metricOptions: List<MetricOption>,
        metric: MetricOption,
        onMetric: (MetricOption) -> Unit,
        expression: ApportExpression,
        onExpression: (ApportExpression) -> Unit,
        showBoxplot: Boolean,
        onBoxplot: (Boolean) -> Unit,
        showPoints: Boolean,
        onPoints: (Boolean) -> Unit,
        trier: Boolean,
        onTrier: (Boolean) -> Unit,
        displayedRations: List<CrossConsultationAnalysisViewModel.RationSummary>,
        actualRations: List<CrossConsultationAnalysisViewModel.RationSummary>,
        proposedRations: List<CrossConsultationAnalysisViewModel.RationSummary>,
        valeur: (CrossConsultationAnalysisViewModel.RationSummary) -> Float,
        modifier: Modifier
) {
    var choixMetrique by remember { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
            // Sélecteur de nutriment : bouton qui ouvre une liste avec recherche
            Column(Modifier.weight(1f)) {
                Text(translate("auto.view.crossconsultationresultsview.nutriment_rapport"), fontSize = 10.sp, color = discretResultats())
                OutlinedButton(onClick = { choixMetrique = true }, modifier = Modifier.fillMaxWidth().height(40.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)) {
                    Text("${metric.label} (${metric.unit})", fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colors.onSurface, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
            DropdownField(
                    label = translate("auto.view.crossconsultationresultsview.expression_des_apports"),
                    selectedValue = expression,
                    options = ApportExpression.entries,
                    onValueChange = onExpression,
                    valueToString = { it.libelle() },
                    enabled = metric.kind == MetricKind.NUTRIENT,
                    modifier = Modifier.weight(1f)
            )
        }
        FlowRow(mainAxisSpacing = 6.dp, crossAxisSpacing = 6.dp) {
            PuceFiltre(translate("auto.view.crossconsultationresultsview.barres"), selectionnee = !showBoxplot, onClick = { onBoxplot(false) })
            PuceFiltre(translate("crossConsultation.boites"), selectionnee = showBoxplot, onClick = { onBoxplot(true) })
            if (showBoxplot) {
                PuceFiltre(translate("crossConsultation.points"), selectionnee = showPoints, onClick = { onPoints(!showPoints) })
            } else {
                PuceFiltre(translate("crossConsultation.trierValeur"), selectionnee = trier, onClick = { onTrier(!trier) })
            }
        }

        // Résumé chiffré de la série tracée
        val series = if (showBoxplot) listOf(
                translate("auto.view.crossconsultationresultsview.actuelles") to actualRations,
                translate("auto.view.crossconsultationresultsview.proposees") to proposedRations
        ) else listOf(translate("crossConsultation.rationsAffichees") to displayedRations)
        Column(
                Modifier.fillMaxWidth().background(VetNutriColors.Primary.copy(alpha = 0.05f), RoundedCornerShape(6.dp)).padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            series.forEach { (nom, rations) ->
                val s = viewModel.describe(rations.map { valeur(it).toDouble() })
                Text(
                        if (s == null) translate("crossConsultation.resumeSerieVide", nom)
                        else translate("crossConsultation.resumeSerie", nom, s.count.toString(), formatStatValue(s.mean), formatStatValue(s.stdDev),
                                formatStatValue(s.median), formatStatValue(s.min), formatStatValue(s.max)),
                        fontSize = 12.sp
                )
            }
        }

        if (showBoxplot) {
            BoxPlotChart(
                    actualItems = actualRations,
                    proposedItems = proposedRations,
                    showPoints = showPoints,
                    metric = metric,
                    expression = expression,
                    modifier = Modifier.fillMaxWidth().weight(1f).heightIn(min = 220.dp)
            )
        } else {
            ConsultationBarChart(
                    items = if (trier) displayedRations.sortedByDescending { valeur(it) } else displayedRations,
                    metric = metric,
                    expression = expression,
                    modifier = Modifier.fillMaxWidth().weight(1f).heightIn(min = 220.dp)
            )
        }
    }
    if (choixMetrique) {
        SelecteurMetrique(metricOptions, metric, onChoix = { onMetric(it); choixMetrique = false }, onDismiss = { choixMetrique = false })
    }
}

/** Liste des nutriments et rapports, groupée, avec recherche. */
@Composable
private fun SelecteurMetrique(options: List<MetricOption>, actuelle: MetricOption, onChoix: (MetricOption) -> Unit, onDismiss: () -> Unit) {
    var requete by remember { mutableStateOf("") }
    val mots = Recherche.mots(requete)
    val visibles = options.filter { mots.isEmpty() || Recherche.correspond(mots, Recherche.indexer(it.label, it.groupe)) }
    Dialog(onDismissRequest = onDismiss) {
        Card(Modifier.fillMaxWidth().heightIn(max = 560.dp), elevation = 8.dp) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(translate("crossConsultation.choisirMetrique"), style = MaterialTheme.typography.h6, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = translate("crossConsultation.fermer")) }
                }
                OutlinedTextField(
                        value = requete,
                        onValueChange = { requete = it },
                        placeholder = { Text(translate("crossConsultation.rechercherNutriment")) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))
                LazyColumn(Modifier.fillMaxWidth().weight(1f, fill = false)) {
                    visibles.groupBy { it.groupe }.forEach { (groupe, liste) ->
                        item(key = "g:$groupe") {
                            Text(groupe, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VetNutriColors.PrimaryVariant,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp, start = 4.dp))
                        }
                        items(liste, key = { it.key }) { option ->
                            val choisie = option.key == actuelle.key
                            Row(
                                    Modifier.fillMaxWidth()
                                            .background(if (choisie) VetNutriColors.Primary.copy(alpha = 0.12f) else Color.Transparent, RoundedCornerShape(4.dp))
                                            .clickable { onChoix(option) }
                                            .padding(horizontal = 8.dp, vertical = 7.dp)
                            ) {
                                Text(option.label, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text(option.unit, fontSize = 12.sp, color = discretResultats())
                            }
                        }
                    }
                    if (visibles.isEmpty()) item { Text(translate("crossConsultation.aucunNutriment"), color = discretResultats(), modifier = Modifier.padding(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun RationList(
        items: List<CrossConsultationAnalysisViewModel.RationSummary>,
        metric: MetricOption,
        valeur: (CrossConsultationAnalysisViewModel.RationSummary) -> Float,
        modifier: Modifier
) {
    if (items.isEmpty()) {
        Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(translate("crossConsultation.aucuneRation"), color = discretResultats(), textAlign = TextAlign.Center)
        }
        return
    }
    LazyColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(items, key = { it.rationId }) { item ->
            Card(elevation = 0.dp, border = BorderStroke(1.dp, bordureResultats), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                    Box(Modifier.padding(top = 4.dp).size(width = 4.dp, height = 34.dp)
                            .background(if (item.actual) couleurActuelle else couleurProposee, RoundedCornerShape(2.dp)))
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${item.animalName} • ${item.consultationDate}", fontSize = 12.sp, color = discretResultats(),
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                                listOfNotNull(
                                        item.referenceLabel,
                                        item.speciesLabel,
                                        "${item.quantity.roundToInt()} g",
                                        if (item.actual) translate("crossConsultation.actuelle") else translate("crossConsultation.proposee")
                                ).joinToString(" · "),
                                fontSize = 11.sp, color = discretResultats(), maxLines = 2, overflow = TextOverflow.Ellipsis
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(formatAxisValue(valeur(item)), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VetNutriColors.PrimaryVariant)
                        Text(metric.unit, fontSize = 10.sp, color = discretResultats())
                    }
                }
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
                translate("auto.view.crossconsultationresultsview.aucune_donnee_a_tracer_selectionnez_des_rations"),
                style = androidx.compose.material.MaterialTheme.typography.body2,
                modifier = Modifier.padding(horizontal = AppSizes.paddingMedium)
        )
        return
    }
    // Le nom de l'animal distingue les rations (souvent toutes nommées « Ration » ou « Croquettes »)
    val categories = items.mapIndexed { index, item -> "${index + 1}. ${item.animalName}" }
    val values = items.map { item ->
        val raw = metric.extractor(item)
        applyExpression(item, metric, raw, expression)
    }
    val maxY = (values.maxOrNull() ?: 1f).let { if (it <= 0f) 1f else it * 1.2f }
    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }
    Box(modifier = modifier.padding(horizontal = AppSizes.paddingMedium)) {
        XYGraph(
                yAxisLabels = { GraphFormattingUtils.formatAxisTick(it, 0f..maxY) },
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
                                if (ration.actual) couleurActuelle
                                else couleurProposee
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
    return "${metric.label} (${metric.unit}) • ${expression.libelle()}"
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
            append(expression.libelle())
        }
        if (item.ingredients.isNotEmpty()) {
            append("\n" + translate("crossConsultation.ingredients"))
            item.ingredients.forEach { ingredient ->
                append("\n- ")
                append(ingredient.name)
                append(": ")
                append(formatAxisValue(ingredient.quantity.toFloat()))
                append(" g")
            }
        }
        append("\n")
        append(if (item.actual) translate("crossConsultation.actuelle") else translate("crossConsultation.proposee"))
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
                translate("auto.view.crossconsultationresultsview.aucune_donnee_a_tracer_selectionnez_des_rations"),
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
                translate("auto.view.crossconsultationresultsview.aucune_donnee_a_tracer_selectionnez_des_rations"),
                style = androidx.compose.material.MaterialTheme.typography.body2,
                modifier = Modifier.padding(horizontal = AppSizes.paddingMedium)
        )
        return
    }

    val groups = listOf(
            BoxPlotGroup(
                    label = translate("auto.view.crossconsultationresultsview.actuelles"),
                    color = couleurActuelle,
                    stats = actualValues.takeIf { it.isNotEmpty() }?.let { computeBoxPlot(it) },
                    points = actualItems.map { item ->
                        BoxPlotPoint(
                                item = item,
                                value = applyExpression(item, metric, metric.extractor(item), expression),
                                color = couleurActuelle
                        )
                    }
            ),
            BoxPlotGroup(
                    label = translate("auto.view.crossconsultationresultsview.proposees"),
                    color = couleurProposee,
                    stats = proposedValues.takeIf { it.isNotEmpty() }?.let { computeBoxPlot(it) },
                    points = proposedItems.map { item ->
                        BoxPlotPoint(
                                item = item,
                                value = applyExpression(item, metric, metric.extractor(item), expression),
                                color = couleurProposee
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


/** Nom traduit et unité d'un nutriment désigné par son label interne. */
private fun nomEtUnite(label: String): Pair<String, String> {
    val n = NutrientResolver.AllNutrientResolver(label)
    return (n?.let { nomTraduitNutriment(it) } ?: label) to (n?.unite ?: "")
}

/**
 * Statistiques des nutriments : rations actuelles et proposées côte à côte, écart relatif des
 * moyennes, recherche, export CSV.
 */
@Composable
private fun NutrientStatsPanel(
        viewModel: CrossConsultationAnalysisViewModel,
        compact: Boolean,
        modifier: Modifier = Modifier
) {
    val selection by viewModel.selectedIds.collectAsState()
    val comparaisons = remember(selection) { viewModel.compareNutrientStats() }
    var requete by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val lignes = remember(comparaisons) {
        comparaisons.map { c -> nomEtUnite(c.label).let { (nom, unite) -> Triple(c, nom, unite) } }.sortedBy { it.second.lowercase() }
    }
    val mots = Recherche.mots(requete)
    val visibles = lignes.filter { mots.isEmpty() || Recherche.correspond(mots, Recherche.indexer(it.second, it.first.label)) }

    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
            OutlinedTextField(
                    value = requete,
                    onValueChange = { requete = it },
                    placeholder = { Text(translate("crossConsultation.rechercherNutriment"), fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                    modifier = Modifier.weight(1f)
            )
            if (isCsvFileOperationsSupported()) {
                Spacer(Modifier.width(6.dp))
                TextButton(
                        onClick = { coroutineScope.launch { saveCsvFileForExport(buildStatsCsv(lignes), "statistiques_nutriments.csv") } },
                        enabled = comparaisons.isNotEmpty()
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(translate("auto.view.crossconsultationresultsview.exporter_statistiques_csv"), fontSize = 12.sp)
                }
            }
        }
        Text(translate("crossConsultation.statsAide"), fontSize = 11.sp, color = discretResultats(), modifier = Modifier.padding(bottom = 6.dp))

        if (comparaisons.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(translate("auto.view.crossconsultationresultsview.aucune_donnee_disponible_selectionnez_des_ration"),
                        color = discretResultats(), style = MaterialTheme.typography.body2)
            }
            return@Column
        }

        if (!compact) {
            Row(
                    modifier = Modifier.fillMaxWidth()
                            .background(VetNutriColors.Primary.copy(alpha = 0.08f), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .padding(vertical = 6.dp, horizontal = 8.dp)
            ) {
                EnteteColonne(translate("nutrientComponents.columnNutrient"), 2.4f)
                EnteteColonne(translate("crossConsultation.colActuelles"), 2.4f, couleurActuelle)
                EnteteColonne(translate("crossConsultation.colProposees"), 2.4f, couleurProposee)
                EnteteColonne(translate("crossConsultation.colEcart"), 1f, align = TextAlign.End)
            }
        }
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(visibles, key = { it.first.label }) { (c, nom, unite) ->
                if (compact) LigneStatCompacte(c, nom, unite) else LigneStat(c, nom, unite)
                Divider(color = bordureResultats, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun RowScope.EnteteColonne(texte: String, poids: Float, couleur: Color = Color.Unspecified, align: TextAlign = TextAlign.Start) {
    Text(texte, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = couleur, textAlign = align, modifier = Modifier.weight(poids))
}

/** « moyenne ± ET (n) » puis « médiane · min–max » sur une seconde ligne. */
@Composable
private fun CelluleStat(s: CrossConsultationAnalysisViewModel.NutrientStat?, modifier: Modifier) {
    Column(modifier) {
        if (s == null) {
            Text("—", fontSize = 12.sp, color = discretResultats())
        } else {
            Text("${formatStatValue(s.mean)} ± ${formatStatValue(s.stdDev)}  (n=${s.count})", fontSize = 12.sp)
            Text(
                    translate("crossConsultation.medianeMinMax", formatStatValue(s.median), formatStatValue(s.min), formatStatValue(s.max)),
                    fontSize = 10.sp,
                    color = discretResultats()
            )
        }
    }
}

@Composable
private fun TexteEcart(ecart: Double?, modifier: Modifier = Modifier, align: TextAlign = TextAlign.End) {
    // Neutre : une baisse peut être l'objectif (phosphore d'un insuffisant rénal…). Seuls les écarts
    // de plus de 10 % sont mis en avant.
    val couleur = if (ecart != null && kotlin.math.abs(ecart) > 10.0) VetNutriColors.PrimaryVariant else discretResultats()
    Text(
            ecart?.let { "${if (it > 0) "+" else ""}${NumberUtils.format(it, 0)} %" } ?: "—",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = couleur,
            textAlign = align,
            modifier = modifier
    )
}

@Composable
private fun LigneStat(c: CrossConsultationAnalysisViewModel.NutrientComparison, nom: String, unite: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(2.4f)) {
            Text(nom, fontSize = 12.sp, maxLines = 2)
            if (unite.isNotBlank()) Text(unite, fontSize = 10.sp, color = discretResultats())
        }
        CelluleStat(c.actual, Modifier.weight(2.4f))
        CelluleStat(c.proposed, Modifier.weight(2.4f))
        TexteEcart(c.ecartPourcent, Modifier.weight(1f))
    }
}

@Composable
private fun LigneStatCompacte(c: CrossConsultationAnalysisViewModel.NutrientComparison, nom: String, unite: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (unite.isBlank()) nom else "$nom ($unite)", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TexteEcart(c.ecartPourcent)
        }
        Row {
            Box(Modifier.padding(top = 4.dp).size(8.dp).background(couleurActuelle, RoundedCornerShape(2.dp)))
            Spacer(Modifier.width(6.dp))
            CelluleStat(c.actual, Modifier.weight(1f))
        }
        Row {
            Box(Modifier.padding(top = 4.dp).size(8.dp).background(couleurProposee, RoundedCornerShape(2.dp)))
            Spacer(Modifier.width(6.dp))
            CelluleStat(c.proposed, Modifier.weight(1f))
        }
    }
}

private fun formatStatValue(v: Double): String =
        if (v < 10.0) NumberUtils.format(v, 2) else NumberUtils.format(v, 1)

private fun buildStatsCsv(lignes: List<Triple<CrossConsultationAnalysisViewModel.NutrientComparison, String, String>>): String {
    fun champ(v: String) = if (v.any { it == ';' || it == '"' || it == '\n' }) "\"" + v.replace("\"", "\"\"") + "\"" else v
    fun stats(s: CrossConsultationAnalysisViewModel.NutrientStat?) =
            if (s == null) List(6) { "" } else listOf(s.count.toString(), s.mean.toString(), s.stdDev.toString(), s.median.toString(), s.min.toString(), s.max.toString())
    val entete = listOf("nutriment", "unite") +
            listOf("n", "moyenne", "ecart_type", "mediane", "min", "max").map { "actuelles_$it" } +
            listOf("n", "moyenne", "ecart_type", "mediane", "min", "max").map { "proposees_$it" } +
            listOf("ecart_moyennes_pourcent")
    val sb = StringBuilder()
    sb.appendLine(entete.joinToString(";"))
    lignes.forEach { (c, nom, unite) ->
        sb.appendLine((listOf(nom, unite) + stats(c.actual) + stats(c.proposed) + listOf(c.ecartPourcent?.toString() ?: ""))
                .joinToString(";") { champ(it) })
    }
    return sb.toString()
}
