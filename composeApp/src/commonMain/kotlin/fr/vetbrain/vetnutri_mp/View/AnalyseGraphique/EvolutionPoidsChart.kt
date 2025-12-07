package fr.vetbrain.vetnutri_mp.View.AnalyseGraphique

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.rotate
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.GraphFormattingUtils
import fr.vetbrain.vetnutri_mp.Utils.KoalaPlotExtensions
import fr.vetbrain.vetnutri_mp.Utils.isIosPlatform
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.Export.PdfExporter
import fr.vetbrain.vetnutri_mp.Export.DocumentType
import fr.vetbrain.vetnutri_mp.Export.ExportData
import fr.vetbrain.vetnutri_mp.Export.HtmlSection
import fr.vetbrain.vetnutri_mp.Export.RichTextContent
import fr.vetbrain.vetnutri_mp.Export.TextBlock
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import io.github.koalaplot.core.*
import io.github.koalaplot.core.line.AreaBaseline
import io.github.koalaplot.core.line.AreaPlot
import io.github.koalaplot.core.line.LinePlot
import io.github.koalaplot.core.style.AreaStyle
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.*
import io.github.koalaplot.core.xygraph.Point
import io.github.koalaplot.core.xygraph.XYGraph
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlin.math.pow
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.ceil

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun EvolutionPoidsChart(
    viewModel: AnimalDetailViewModel,
    coneState: WeightConeState? = null,
    onActivateConeAction: (LocalDate, Double, Double?) -> Unit = { _, _, _ -> },
    onClearCone: () -> Unit = {}
) {
    var showZoom by remember { mutableStateOf(false) }
    var showGrowthZoom by remember { mutableStateOf(false) }

    if (showZoom && coneState != null) {
        ConeZoomView(viewModel, coneState!!, onClose = { showZoom = false })
        return
    }

    val animal by viewModel.animal.collectAsState()
        val targetWeight = coneState?.targetWeight

        // Utiliser derivedStateOf pour forcer la recomposition quand les données changent
        val consultationsWithAge by
                remember(animal?.consultations?.size, animal?.weightHistory?.size) {
                        derivedStateOf {
                                val consultations =
                                        animal?.consultations?.sortedBy { it.date } ?: emptyList()
                                val weightHistory =
                                        animal?.weightHistory?.sortedBy { it.date } ?: emptyList()

                                // Combiner les poids des consultations et de l'historique
                                val allWeights = mutableListOf<ConsultationAgeData>()

                                // Ajouter les poids des consultations (poids réels uniquement)
                                consultations.forEach { consultation ->
                                        val birthDate = animal?.birthdate
                                        val consultationDate = consultation.date
                                        val weight = consultation.weight

                                        if (birthDate != null &&
                                                        consultationDate != null &&
                                                        weight != null
                                        ) {
                                                val ageInDays =
                                                        birthDate.daysUntil(consultationDate)
                                                val ageInYears = ageInDays / 365.25
                                                val ageInMonths = ageInDays / 30.44
                                                allWeights.add(
                                                        ConsultationAgeData(
                                                                consultationDate,
                                                                ageInDays,
                                                                ageInYears,
                                                                ageInMonths,
                                                                weight,
                                                                isFromConsultation = true
                                                        )
                                                )
                                        }
                                }

                                // Ajouter les poids supplémentaires de l'historique
                                weightHistory.forEach { weightEntry ->
                                        val birthDate = animal?.birthdate
                                        val weightDate = weightEntry.date

                                        if (birthDate != null) {
                                                val ageInDays = birthDate.daysUntil(weightDate)
                                                val ageInYears = ageInDays / 365.25
                                                val ageInMonths = ageInDays / 30.44
                                                allWeights.add(
                                                        ConsultationAgeData(
                                                                weightDate,
                                                                ageInDays,
                                                                ageInYears,
                                                                ageInMonths,
                                                                weightEntry.value,
                                                                isFromConsultation = false,
                                                                weightUuid = weightEntry.uuid
                                                        )
                                                )
                                        }
                                }

                                // Trier par date
                                allWeights.sortedBy { it.date }
                        }
                }

        Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
                // États UI: sélection de la courbe de référence et affichage
                val especeAnimal: Espece =
                        animal?.getEspece() ?: Espece.CHIEN
                val courbesDisponibles: List<CurveP> =
                        when (especeAnimal) {
                                Espece.CHAT -> courbesCroissanceChat
                                else -> courbesCroissanceChien
                        }
                var selectedCurveIndex by remember(especeAnimal, courbesDisponibles.size) {
                        mutableStateOf(0)
                }
                var showReferenceCurves by remember { mutableStateOf(true) }
                val selectedCurve = courbesDisponibles.getOrNull(selectedCurveIndex)

                val useYearsScale: Boolean =
                        consultationsWithAge.maxOfOrNull { it.ageInYears }?.let { it > 1.0 }
                                ?: false

                if (showGrowthZoom && selectedCurve != null) {
                        GrowthZoomView(
                                viewModel = viewModel,
                                selectedCurve = selectedCurve,
                                onClose = { showGrowthZoom = false }
                        )
                        return
                }

                // Contrôles: ComboBox (Dropdown) + Checkbox
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        // Dropdown simple
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                                Button(onClick = { expanded = true }) {
                                        Text(
                                                text = selectedCurve?.description
                                                                ?: "Sélectionner courbe"
                                        )
                                }
                                DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                ) {
                                        courbesDisponibles.forEachIndexed { index, courbe ->
                                                DropdownMenuItem(
                                                        onClick = {
                                                                selectedCurveIndex = index
                                                                expanded = false
                                                        }
                                                ) { Text(text = courbe.description) }
                                        }
                                }
                        }

                        if (selectedCurve != null) {
                            Button(
                                onClick = { showGrowthZoom = true },
                                colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Secondary),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.ZoomIn, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Zoom croissance & PDF")
                            }
                        }

                        if (coneState != null) {
                            Button(
                                onClick = { showZoom = true },
                                colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Secondary),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.ZoomIn, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Zoom Cône & Rapport")
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                        checked = showReferenceCurves,
                                        onCheckedChange = { showReferenceCurves = it }
                                )
                                Text(text = "Afficher courbes de référence")
                        }
                }

                // Préparer données réelles (peut être vide) et fallback courbe 50% 0–12 mois
                // donneesPoids est calculé plus bas, après avoir fusionné les min/max avec le cône
                
                // Calculer les lignes du cône
                val coneLines = remember(coneState, animal?.birthdate) {
                    val birthDate = animal?.birthdate
                    if (coneState != null && birthDate != null) {
                        val startAgeDays = birthDate.daysUntil(coneState.startDate)
                        val startAgeMonths = startAgeDays / 30.44f
                        val startWeight = coneState.startWeight.toFloat()
                        val targetW = coneState.targetWeight?.toFloat()

                        val weeks = 26 // Default projection duration

                        // Function to calculate points until target
                        fun calculatePoints(percentagePerWeek: Float): List<Point<Float, Float>> {
                            val points = mutableListOf<Point<Float, Float>>()
                            points.add(Point(startAgeMonths, startWeight))

                            val effectivePercentage = if (targetW != null && targetW > startWeight && percentagePerWeek < 0) {
                                -percentagePerWeek // Invert for gain
                            } else {
                                percentagePerWeek
                            }

                            val endWeight26Weeks = startWeight * (1f + effectivePercentage * weeks)
                            var endAgeMonthsCalculated = startAgeMonths + (weeks * 7 / 30.44f)
                            var endWeightCalculated = endWeight26Weeks

                            if (targetW != null && effectivePercentage != 0f) {
                                val weeksToTarget = (targetW / startWeight - 1f) / effectivePercentage
                                if (weeksToTarget > 0) {
                                    val daysToTarget = (weeksToTarget * 7).toInt()
                                    val targetDate = coneState.startDate.plus(DatePeriod(days = daysToTarget))
                                    val targetAgeDays = birthDate.daysUntil(targetDate)
                                    endAgeMonthsCalculated = targetAgeDays / 30.44f
                                    endWeightCalculated = targetW
                                }
                            }

                            points.add(Point(endAgeMonthsCalculated, endWeightCalculated))
                            return points
                        }

                        val slowLine = calculatePoints(-0.005f)
                        val fastLine = calculatePoints(-0.02f)

                        Triple(slowLine, fastLine, targetW)
                    } else {
                        null
                    }
                }

                val courbeRef = selectedCurve
                val param50 = courbeRef?.params?.find { it.name == "50%" }
                val pointsRef0_12 =
                        param50?.let { param ->
                                (0..12).map { mois ->
                                        val ageInMonths = mois.toFloat()
                                        val ageAxis =
                                                if (useYearsScale) {
                                                        ageInMonths / 12f
                                                } else {
                                                        ageInMonths
                                                }
                                        val poids =
                                                calculerPoidsCroissance(
                                                        param,
                                                        ageInMonths.toDouble()
                                                )
                                        Point(x = ageAxis, y = poids.toFloat())
                                }
                        } ?: emptyList<Point<Float, Float>>()

                // Axes: si on a des données réelles, utiliser leur plage; sinon, utiliser 0..12
                // mois et y basé sur la courbe. Inclure aussi le cône s'il existe.
                val donneesPoids: List<Point<Float, Float>> =
                        consultationsWithAge.map { d ->
                                val xAxisValue =
                                        if (useYearsScale) {
                                                d.ageInYears.toFloat()
                                        } else {
                                                d.ageInMonths.toFloat()
                                        }
                                Point(x = xAxisValue, y = d.weight.toFloat())
                        }
                val useReal = donneesPoids.isNotEmpty()
                val displayConeLines: Triple<List<Point<Float, Float>>, List<Point<Float, Float>>, Float?>? =
                        if (coneLines != null) {
                                if (useYearsScale) {
                                        val slow =
                                                coneLines.first.map { p ->
                                                        Point(
                                                                x = p.x / 12f,
                                                                y = p.y
                                                        )
                                                }
                                        val fast =
                                                coneLines.second.map { p ->
                                                        Point(
                                                                x = p.x / 12f,
                                                                y = p.y
                                                        )
                                                }
                                        Triple(slow, fast, coneLines.third)
                                } else {
                                        coneLines
                                }
                        } else {
                                null
                        }

                val pointsCone: List<Point<Float, Float>> =
                        if (displayConeLines != null) displayConeLines.first + displayConeLines.second
                        else emptyList()
                val targetY = displayConeLines?.third
                val targetPoints = if (targetY != null) listOf(Point(0f, targetY)) else emptyList() // Dummy point for Y range

                val minXData: Float = if (useReal) donneesPoids.minOf { it.x } else 0.0f
                val minXCone: Float = if (pointsCone.isNotEmpty()) pointsCone.minOf { it.x } else Float.MAX_VALUE
                // On prend le min global, en s'assurant de ne pas casser si un est vide (MAX_VALUE)
                val minXVal = if (minXCone < minXData) minXCone else minXData
                val minX = if (minXVal == Float.MAX_VALUE) 0f else minXVal

                val maxXData: Float = if (useReal) donneesPoids.maxOf { it.x } else 12.0f
                val maxXCone: Float = if (pointsCone.isNotEmpty()) pointsCone.maxOf { it.x } else Float.MIN_VALUE
                val maxXVal = if (maxXCone > maxXData) maxXCone else maxXData
                val maxX = if (maxXVal == Float.MIN_VALUE) 12f else maxXVal

                val xMargin = (maxX - minX).coerceAtLeast(1.0f) * 0.05f
                val xRange = arrondirPlage((minX - xMargin)..(maxX + xMargin))

                val yCandidates = (if (useReal) donneesPoids else pointsRef0_12).map { it.y } + pointsCone.map { it.y } + (if(targetY != null) listOf(targetY) else emptyList())
                val minY = yCandidates.minOrNull() ?: 0.0f
                val maxY = yCandidates.maxOrNull() ?: (minY + 5.0f)
                val yMargin = (maxY - minY).coerceAtLeast(1.0f) * 0.05f
                val yRange = arrondirPlage((minY - yMargin)..(maxY + yMargin))

                // Créer des plages Float pour KoalaPlot (déjà Float)
                val xRangeFloat = xRange
                val yRangeFloat = yRange

                val xRangeWidth = (xRange.endInclusive - xRange.start).coerceAtLeast(0.0f)
                val xTickIncrement = if (xRangeWidth > 10.0f) 3.0f else 1.0f
                val safeTickIncrement =
                        if (xRangeWidth > 0.0f) xTickIncrement.coerceAtMost(xRangeWidth) else 1.0f

                // Graphique
                val sousTitreGraphique =
                        if (useYearsScale) {
                                "Poids en kg selon l'âge (années, avec courbes de référence)"
                        } else {
                                "Poids en kg selon l'âge (mois, avec courbes de référence)"
                        }

                GraphCard(
                        titre = "Évolution du poids corporel",
                        sousTitre = sousTitreGraphique
                ) {
                        Column {
                                XYGraph(
                                        xAxisModel =
                                                KoalaPlotExtensions.createSmartXAxisModel(
                                                        range = xRangeFloat
                                                ),
                                        yAxisModel =
                                                KoalaPlotExtensions.createSmartYAxisModel(
                                                        yRangeFloat
                                                ),
                                        xAxisTitle =
                                                if (useYearsScale) "Âge (années)"
                                                else "Âge (mois)",
                                        yAxisTitle = "Poids (kg)",
                                        modifier = Modifier.height(500.dp)
                                ) {
                                        // Courbes de référence: toutes les percentiles si demandé
                                        if (showReferenceCurves && courbeRef != null) {
                                                courbeRef.params.forEach { param ->
                                                        val pts =
                                                                (0..12).map { mois ->
                                                                        val ageInMonths =
                                                                                mois.toFloat()
                                                                        val xAxisValue =
                                                                                if (useYearsScale) {
                                                                                        ageInMonths /
                                                                                                12f
                                                                                } else {
                                                                                        ageInMonths
                                                                                }
                                                                        val y =
                                                                                calculerPoidsCroissance(
                                                                                        param,
                                                                                        ageInMonths
                                                                                                .toDouble()
                                                                                )
                                                                        Point(
                                                                                x = xAxisValue,
                                                                                y = y.toFloat()
                                                                        )
                                                                }

                                                        if (pts.isNotEmpty()) {
                                                                AreaPlot(
                                                                        data = pts,
                                                                        lineStyle =
                                                                                LineStyle(
                                                                                        brush =
                                                                                                SolidColor(
                                                                                                        Color.Gray
                                                                                                ),
                                                                                        strokeWidth =
                                                                                                0.2.dp
                                                                                ),
                                                                        areaStyle =
                                                                                AreaStyle(
                                                                                        brush =
                                                                                                SolidColor(
                                                                                                        Color.Green
                                                                                                ),
                                                                                        alpha =
                                                                                                0.1f,
                                                                                ),
                                                                        areaBaseline =
                                                                                AreaBaseline
                                                                                        .ConstantLine(
                                                                                                0.0f
                                                                                        )
                                                                )
                                                        }
                                                }
                                        } else if (pointsRef0_12.isNotEmpty()) {
                                                // Sinon, au minimum la 50%
                                                LinePlot(data = pointsRef0_12)
                                        }

                                        // Cône de perte de poids
                                        if (displayConeLines != null) {
                                            val targetW = displayConeLines.third
                                            
                                            // Ligne objectif (Target Weight) - Pointillés noirs
                                            if (targetW != null) {
                                                val lineTarget = listOf(
                                                    Point(xRange.start, targetW),
                                                    Point(xRange.endInclusive, targetW)
                                                )
                                                LinePlot(
                                                    data = lineTarget,
                                                    lineStyle = LineStyle(
                                                        brush = SolidColor(Color.Black),
                                                        strokeWidth = 1.5.dp,
                                                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                                                    )
                                                )
                                            }
                                            
                                            // Ligne lente (-0.5%) - Vert
                                            LinePlot(
                                                data = displayConeLines.first,
                                                lineStyle = LineStyle(
                                                    brush = SolidColor(Color(0xFF4CAF50)), // Green
                                                    strokeWidth = 2.dp,
                                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                                )
                                            )

                                            // Ligne rapide (-2.0%) - Orange
                                            LinePlot(
                                                data = displayConeLines.second,
                                                lineStyle = LineStyle(
                                                    brush = SolidColor(Color(0xFFFF5722)), // Deep Orange
                                                    strokeWidth = 2.dp,
                                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                                )
                                            )
                                        }

                                        // Courbe des données réelles de l'animal
                                        if (donneesPoids.isNotEmpty()) {
                                                LinePlot(
                                                        data = donneesPoids,
                                                        symbol = {
                                                                androidx.compose.foundation.Canvas(
                                                                        modifier =
                                                                                Modifier.size(6.dp)
                                                                ) { drawCircle(color = Color.Blue) }
                                                        }
                                                )
                                        }
                                }

                                // Légende supprimée
                        }
                }
                // Tableau des poids
                PoidsTableau(
                    consultationsWithAge,
                    viewModel,
                    onActivateCone = onActivateConeAction,
                    onClearCone = onClearCone,
                    isConeActive = coneState != null
                )
        }
}

@Composable
fun PoidsTableau(
        consultationsWithAge: List<ConsultationAgeData>,
        viewModel: AnimalDetailViewModel,
        onActivateCone: (LocalDate, Double, Double?) -> Unit = { _, _, _ -> },
        onClearCone: () -> Unit = {},
        isConeActive: Boolean = false
) {
        val isAddingWeight = viewModel.isAddingWeight
        var targetWeightInput by remember { mutableStateOf("") }
        
        // Helper to convert input to Double safely
        fun getTargetWeight(): Double? = targetWeightInput.replace(',', '.').toDoubleOrNull()

        Card(modifier = Modifier.fillMaxWidth(), elevation = AppSizes.elevationMedium) {
                Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text = "Historique des poids",
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Primary
                                )

                                // Champ Poids Objectif
                                OutlinedTextField(
                                    value = targetWeightInput,
                                    onValueChange = { targetWeightInput = it },
                                    label = { Text("Objectif (kg)", fontSize = 10.sp) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.width(100.dp).height(55.dp),
                                    textStyle = MaterialTheme.typography.body2
                                )

                                if (isConeActive) {
                                    Button(
                                        onClick = onClearCone,
                                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE57373)),
                                        modifier = Modifier.padding(end = AppSizes.paddingSmall)
                                    ) {
                                        Text("Effacer cône", color = Color.White)
                                    }
                                }

                                Button(
                                        onClick = { viewModel.startAddingWeight() },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Secondary
                                                )
                                ) {
                                        Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Ajouter un poids",
                                                tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                                        Text("Ajouter un poids")
                                }
                        }

                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                        // Interface d'ajout de poids
                        if (isAddingWeight) {
                                AddWeightForm(viewModel)
                                Spacer(modifier = Modifier.height(AppSizes.paddingMedium))
                        }

                        if (consultationsWithAge.isEmpty()) {
                                Text(
                                        text = "Aucun poids enregistré",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                        } else {
                                // En-têtes du tableau
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                        Text(
                                                text = "Date",
                                                modifier = Modifier.weight(1f),
                                                style = MaterialTheme.typography.caption,
                                                fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                                text = "Âge",
                                                modifier = Modifier.weight(1f),
                                                style = MaterialTheme.typography.caption,
                                                fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                                text = "Poids (kg)",
                                                modifier = Modifier.weight(1f),
                                                style = MaterialTheme.typography.caption,
                                                fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                                text = "Source",
                                                modifier = Modifier.weight(1f),
                                                style = MaterialTheme.typography.caption,
                                                fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                                text = "Actions",
                                                modifier = Modifier.weight(0.5f),
                                                style = MaterialTheme.typography.caption,
                                                fontWeight = FontWeight.Bold
                                        )
                                }

                                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                                // Lignes du tableau
                                consultationsWithAge.forEach { consultationData ->
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                Text(
                                                        text = consultationData.date.toString(),
                                                        modifier = Modifier.weight(1f),
                                                        style = MaterialTheme.typography.caption
                                                )
                                                Text(
                                                        text =
                                                                formatAge(
                                                                        consultationData.ageInYears,
                                                                        consultationData.ageInMonths
                                                                ),
                                                        modifier = Modifier.weight(1f),
                                                        style = MaterialTheme.typography.caption
                                                )
                                                Text(
                                                        text =
                                                                GraphFormattingUtils.formatWeight(
                                                                        consultationData.weight
                                                                                .toDouble()
                                                                ),
                                                        modifier = Modifier.weight(1f),
                                                        style = MaterialTheme.typography.caption
                                                )
                                                Text(
                                                        text =
                                                                if (consultationData
                                                                                .isFromConsultation
                                                                )
                                                                        "Consultation"
                                                                else "Hors consultation",
                                                        modifier = Modifier.weight(1f),
                                                        style = MaterialTheme.typography.caption,
                                                        color =
                                                                if (consultationData
                                                                                .isFromConsultation
                                                                )
                                                                        VetNutriColors.Primary
                                                                else VetNutriColors.Secondary
                                                )
                                                // Actions: Cône et Suppression
                                                Row(
                                                    modifier = Modifier.weight(0.5f),
                                                    horizontalArrangement = Arrangement.End,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    IconButtonWithTooltip(
                                                        onClick = { onActivateCone(consultationData.date, consultationData.weight.toDouble(), getTargetWeight()) },
                                                        imageVector = Icons.Default.TrendingDown,
                                                        contentDescription = "Cône de perte",
                                                        tooltip = "Cône de perte",
                                                        tint = VetNutriColors.Secondary,
                                                        iconModifier = Modifier.size(20.dp)
                                                    )

                                                    if (!consultationData.isFromConsultation && consultationData.weightUuid != null) {
                                                        IconButtonWithTooltip(
                                                            onClick = {
                                                                viewModel.deleteWeight(consultationData.weightUuid!!)
                                                            },
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Supprimer le poids",
                                                            tooltip = "Supprimer le poids",
                                                            tint = Color.Red,
                                                            iconModifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                        }

                                        if (consultationsWithAge.last() != consultationData) {
                                                Divider(
                                                        modifier =
                                                                Modifier.padding(
                                                                        vertical =
                                                                                AppSizes.paddingSmall /
                                                                                        2
                                                                ),
                                                        color =
                                                                MaterialTheme.colors.onSurface.copy(
                                                                        alpha = 0.1f
                                                                )
                                                )
                                        }
                                }
                        }
                }
        }
}

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun ConeZoomView(
    viewModel: AnimalDetailViewModel,
    coneState: WeightConeState,
    onClose: () -> Unit
) {
    val animal by viewModel.animal.collectAsState()
    val scope = rememberCoroutineScope()

    // Préparer les données
    val zoomData = remember(animal, coneState) {
        val startDate = coneState.startDate
        val startWeight = coneState.startWeight
        val targetWeight = coneState.targetWeight
        
        // Points réels filtrés et convertis en semaines
        val points = animal?.consultations?.flatMap { c ->
            val cDate = c.date
            val cWeight = c.weight
            if (cDate != null && cDate >= startDate && cWeight != null) {
                listOf(Pair(cDate, cWeight))
            } else emptyList()
        }?.plus(
            animal?.weightHistory?.filter { it.date >= startDate }?.map { Pair(it.date, it.value) } ?: emptyList()
        )?.distinctBy { it.first }?.sortedBy { it.first }?.map { (date, weight) ->
            val weeks = startDate.daysUntil(date) / 7.0f
            Triple(date, weeks, weight)
        } ?: emptyList()

        // Lignes du cône (calculées sur 26 semaines ou jusqu'à l'atteinte de l'objectif)
        val weeksDuration = 26

        fun calculateConeLine(basePercentage: Float): List<Point<Float, Float>> {
            val linePoints = mutableListOf<Point<Float, Float>>()
            linePoints.add(Point(0f, startWeight.toFloat()))

            // Inverser le pourcentage si prise de poids (cible > départ)
            val effectivePercentage = if (targetWeight != null && targetWeight > startWeight && basePercentage < 0) {
                -basePercentage
            } else {
                basePercentage
            }

            var finalWeeks = weeksDuration.toFloat()
            var finalWeight = (startWeight * (1 + effectivePercentage * weeksDuration)).toFloat()

            if (targetWeight != null && effectivePercentage != 0f) {
                val weeksToTarget = ((targetWeight / startWeight - 1.0) / effectivePercentage).toFloat()
                // Si l'objectif est atteint dans le futur, on arrête la ligne à l'objectif
                if (weeksToTarget > 0) {
                    finalWeeks = weeksToTarget
                    finalWeight = targetWeight.toFloat()
                }
            }

            linePoints.add(Point(finalWeeks, finalWeight))
            return linePoints
        }

        val slowLine = calculateConeLine(-0.005f)
        val fastLine = calculateConeLine(-0.02f)
        
        Triple(points, Pair(slowLine, fastLine), targetWeight)
    }

    val (realPoints, coneLines, targetW) = zoomData
    val (slowLine, fastLine) = coneLines

    // Calcul des plages
    val maxWeekData = realPoints.maxOfOrNull { it.second } ?: 0f
    val maxLineWeeks = maxOf(slowLine.last().x, fastLine.last().x)
    val maxX = maxOf(maxOf(26f, maxWeekData), maxLineWeeks)
    val xRange = 0f..maxX
    
    val allY = realPoints.map { it.third.toFloat() } + 
               slowLine.map { it.y } + 
               fastLine.map { it.y } + 
               (if (targetW != null) listOf(targetW.toFloat()) else emptyList())
               
    val minY = allY.minOrNull() ?: 0f
    val maxY = allY.maxOrNull() ?: 10f
    val yRange = calculateAdaptiveRange(allY.map { it }, paddingPercent = 0.05f)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
    ) {
        // Header avec bouton retour et export
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onClose) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.rotate(90f))
                Text("Retour")
            }
            
            Button(
                onClick = {
                    if (isIosPlatform) {
                        // iOS : désactiver l'export graphique pour éviter les problèmes mémoire
                        return@Button
                    }

                    val svgGraph = generateConeGraphSvg(
                        realPoints, slowLine, fastLine, targetW, xRange, yRange
                    )
                    val landscapeGraphHtml =
                        """
                        <div style="width: 100%; height: 17cm;">
                        $svgGraph
                        </div>
                        <div style="page-break-after: always;"></div>
                        """.trimIndent()
                    
                    val exportData = ExportData(
                        animal = null,
                        ration = null,
                        reference = null,
                        title = "Suivi Perte de Poids - ${animal?.nom}",
                        additionalText = "",
                        htmlSections = listOf(
                            HtmlSection(
                                id = genUUID(),
                                title = "Graphique d'évolution",
                                content = RichTextContent(
                                    blocks = listOf(
                                        TextBlock.RawHtml(
                                            id = genUUID(),
                                            html = landscapeGraphHtml
                                        )
                                    )
                                )
                            ),
                            HtmlSection(
                                id = genUUID(),
                                title = "Données détaillées",
                                content = RichTextContent(
                                    blocks = listOf(
                                        TextBlock.Paragraph(
                                            id = genUUID(), 
                                            text = "Début du régime: ${coneState.startDate} | Poids initial: ${coneState.startWeight}kg${if(coneState.targetWeight != null) " | Objectif: ${coneState.targetWeight}kg" else ""}"
                                        ),
                                        TextBlock.TableBlock(
                                            id = genUUID(),
                                            headers = listOf("Date", "Semaine", "Poids (kg)"),
                                            rows = realPoints.map { (date, week, weight) ->
                                                listOf(
                                                    date.toString(),
                                                    GraphFormattingUtils.formatDecimal(week.toDouble(), 1),
                                                    GraphFormattingUtils.formatDecimal(weight, 2)
                                                )
                                            }
                                        )
                                    )
                                )
                            )
                        ),
                        isLandscape = true
                    )
                    PdfExporter.exportDocument(
                        DocumentType.RATION_ANALYSIS,
                        exportData,
                        "suivi_poids_${animal?.nom ?: "animal"}.pdf"
                    )
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Secondary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export PDF (Données + Graph)")
            }
        }

        // Graphique Zoomé
        GraphCard(
            titre = "Zoom sur le cône de perte de poids",
            sousTitre = "Évolution en semaines depuis le début du régime (${coneState.startDate})"
        ) {
            XYGraph(
                xAxisModel = FloatLinearAxisModel(xRange, minimumMajorTickIncrement = 2f),
                yAxisModel = KoalaPlotExtensions.createSmartYAxisModel(yRange),
                xAxisTitle = "Semaines",
                yAxisTitle = "Poids (kg)",
                modifier = Modifier.height(400.dp)
            ) {
                // Cône
                LinePlot(
                    data = slowLine,
                    lineStyle = LineStyle(brush = SolidColor(Color(0xFF4CAF50)), strokeWidth = 2.dp, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                )
                LinePlot(
                    data = fastLine,
                    lineStyle = LineStyle(brush = SolidColor(Color(0xFFFF5722)), strokeWidth = 2.dp, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                )
                
                if (targetW != null) {
                    val lineTarget = listOf(Point(xRange.start, targetW.toFloat()), Point(xRange.endInclusive, targetW.toFloat()))
                    LinePlot(
                        data = lineTarget,
                        lineStyle = LineStyle(brush = SolidColor(Color.Black), strokeWidth = 1.5.dp, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f))
                    )
                }

                // Points réels
                if (realPoints.isNotEmpty()) {
                    LinePlot(
                        data = realPoints.map { Point(it.second, it.third.toFloat()) },
                        symbol = {
                            androidx.compose.foundation.Canvas(modifier = Modifier.size(8.dp)) {
                                drawCircle(color = Color.Blue)
                            }
                        }
                    )
                }
            }
        }
        
        // Tableau des données
        Card(modifier = Modifier.fillMaxWidth(), elevation = AppSizes.elevationSmall) {
            Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                Text("Données de la période", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                realPoints.forEach { (date, week, weight) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(date.toString(), style = MaterialTheme.typography.body2)
                        Text("Semaine ${GraphFormattingUtils.formatDecimal(week.toDouble(), 1)}", style = MaterialTheme.typography.body2)
                        Text("${GraphFormattingUtils.formatDecimal(weight, 2)} kg", style = MaterialTheme.typography.body2, fontWeight = FontWeight.Bold)
                    }
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}

// Fonction pour générer un SVG du graphique de cône
fun generateConeGraphSvg(
    realPoints: List<Triple<LocalDate, Float, Double>>,
    slowLine: List<Point<Float, Float>>,
    fastLine: List<Point<Float, Float>>,
    targetWeight: Double?,
    xRange: ClosedFloatingPointRange<Float>,
    yRange: ClosedFloatingPointRange<Float>,
    width: Int = 600,
    height: Int = 400
): String {
    val padding = 40.0
    val graphWidth = width - 2 * padding
    val graphHeight = height - 2 * padding
    
    val xMin = xRange.start
    val xMax = xRange.endInclusive
    val yMin = yRange.start
    val yMax = yRange.endInclusive
    
    fun scaleX(x: Float): Double = padding + (x - xMin) / (xMax - xMin) * graphWidth
    fun scaleY(y: Float): Double = height - padding - (y - yMin) / (yMax - yMin) * graphHeight
    
    val sb = StringBuilder()
    sb.append("<svg width='100%' height='100%' viewBox='0 0 $width $height' xmlns='http://www.w3.org/2000/svg' version='1.1'>")
    
    // Fond blanc
    sb.append("<rect width='$width' height='$height' fill='white' />")
    
    // Axes
    sb.append("<line x1='$padding' y1='${height - padding}' x2='${width - padding}' y2='${height - padding}' stroke='black' stroke-width='1' />") // X
    sb.append("<line x1='$padding' y1='$padding' x2='$padding' y2='${height - padding}' stroke='black' stroke-width='1' />") // Y
    
    // Grille et labels Y (Calcul de pas "intelligent")
    val yRangeSpan = yMax - yMin
    val targetYSteps = 5.0
    val rawYStep = yRangeSpan / targetYSteps
    val magY = 10.0.pow(floor(log10(rawYStep.toDouble())))
    val normY = rawYStep / magY
    val yStep = (when {
        normY < 1.5 -> 1.0
        normY < 3.5 -> 2.0
        normY < 7.5 -> 5.0
        else -> 10.0
    } * magY).toFloat()

    val startY = (ceil(yMin / yStep) * yStep).toFloat()
    var currentY = startY
    
    while (currentY <= yMax + (yStep * 0.01f)) {
        val yPos = scaleY(currentY)
        // Ne dessiner que si c'est dans la zone visible
        if (yPos >= padding - 1 && yPos <= height - padding + 1) {
            sb.append("<line x1='$padding' y1='$yPos' x2='${width - padding}' y2='$yPos' stroke='lightgray' stroke-width='0.5' />")
            sb.append("<text x='${padding - 5}' y='$yPos' font-family='Arial' font-size='10' text-anchor='end' dominant-baseline='middle'>${GraphFormattingUtils.formatDecimal(currentY.toDouble(), 1)}</text>")
        }
        currentY += yStep
    }
    
    // Labels X (Semaines - Pas entier)
    val xStep = 2f // Grille toutes les 2 semaines
    
    var currentX = (ceil(xMin / xStep) * xStep).toFloat()
    
    while (currentX <= xMax + (xStep * 0.01f)) {
        val xPos = scaleX(currentX)
        if (xPos >= padding - 1 && xPos <= width - padding + 1) {
            // Ligne de grille verticale
            sb.append("<line x1='$xPos' y1='$padding' x2='$xPos' y2='${height - padding}' stroke='lightgray' stroke-width='0.5' />")
            // Graduation X
            sb.append("<line x1='$xPos' y1='${height - padding}' x2='$xPos' y2='${height - padding + 5}' stroke='black' stroke-width='1' />")
            sb.append("<text x='$xPos' y='${height - padding + 15}' font-family='Arial' font-size='10' text-anchor='middle'>${currentX.toInt()}</text>")
        }
        currentX += xStep
    }
    
    // Titres axes
    sb.append("<text x='${width / 2}' y='${height - 5}' font-family='Arial' font-size='12' text-anchor='middle'>Semaines</text>")
    sb.append("<text x='10' y='${height / 2}' font-family='Arial' font-size='12' text-anchor='middle' transform='rotate(-90 10 ${height / 2})'>Poids (kg)</text>")

    // Ligne Objectif
    if (targetWeight != null) {
        val yTarget = scaleY(targetWeight.toFloat())
        sb.append("<line x1='${scaleX(xMin)}' y1='$yTarget' x2='${scaleX(xMax)}' y2='$yTarget' stroke='black' stroke-width='1.5' stroke-dasharray='5,5' />")
    }
    
    // Ligne Lente (Verte)
    if (slowLine.size >= 2) {
        val x1 = scaleX(slowLine[0].x)
        val y1 = scaleY(slowLine[0].y)
        val x2 = scaleX(slowLine[1].x)
        val y2 = scaleY(slowLine[1].y)
        sb.append("<line x1='$x1' y1='$y1' x2='$x2' y2='$y2' stroke='#4CAF50' stroke-width='2' stroke-dasharray='10,5' />")
    }
    
    // Ligne Rapide (Orange)
    if (fastLine.size >= 2) {
        val x1 = scaleX(fastLine[0].x)
        val y1 = scaleY(fastLine[0].y)
        val x2 = scaleX(fastLine[1].x)
        val y2 = scaleY(fastLine[1].y)
        sb.append("<line x1='$x1' y1='$y1' x2='$x2' y2='$y2' stroke='#FF5722' stroke-width='2' stroke-dasharray='10,5' />")
    }
    
    // Points réels (Bleu)
    realPoints.forEach { (_, week, weight) ->
        val cx = scaleX(week)
        val cy = scaleY(weight.toFloat())
        sb.append("<circle cx='$cx' cy='$cy' r='4' fill='blue' />")
    }
    
    sb.append("</svg>")
    return sb.toString()
}

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun GrowthZoomView(
    viewModel: AnimalDetailViewModel,
    selectedCurve: CurveP,
    onClose: () -> Unit
) {
    val animal by viewModel.animal.collectAsState()

    val birthDate = animal?.birthdate
    if (birthDate == null) {
        onClose()
        return
    }

    val zoomData =
            remember(animal, selectedCurve) {
                val realPoints =
                        buildList {
                            val consultations = animal?.consultations ?: emptyList()
                            consultations.forEach { consultation ->
                                val date = consultation.date
                                val weight = consultation.weight
                                if (date != null && weight != null) {
                                    val ageDays = birthDate.daysUntil(date)
                                    val weeks = ageDays / 7.0f
                                    add(Triple(date, weeks, weight))
                                }
                            }

                            val history = animal?.weightHistory ?: emptyList()
                            history.forEach { weightEntry ->
                                val date = weightEntry.date
                                val ageDays = birthDate.daysUntil(date)
                                val weeks = ageDays / 7.0f
                                add(Triple(date, weeks, weightEntry.value))
                            }
                        }
                                .sortedBy { it.second }

                val referenceCurves =
                        selectedCurve.params.map { param ->
                            val points =
                                    (12..70).map { week ->
                                        val ageInWeeks = week.toFloat()
                                        val ageInMonths = ageInWeeks / 4.345f
                                        val poids =
                                                calculerPoidsCroissance(
                                                        param,
                                                        ageInMonths.toDouble()
                                                )
                                        Point(x = ageInWeeks, y = poids.toFloat())
                                    }
                            param.name to points
                        }

                Triple(realPoints, referenceCurves, 70f)
            }

    val (realPoints, referenceCurves, maxWeeks) = zoomData

    val allYValues =
            buildList {
                addAll(realPoints.map { it.third.toFloat() })
                referenceCurves.forEach { (_, pts) -> addAll(pts.map { it.y }) }
            }

    val yRange =
            if (allYValues.isNotEmpty()) {
                calculateAdaptiveRange(allYValues, paddingPercent = 0.05f)
            } else {
                0f..10f
            }

    val xRange = 0f..70f

    Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
                Button(onClick = onClose) {
                        Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.rotate(90f)
                        )
                        Text("Retour")
                }

                Button(
                        onClick = {
                                if (isIosPlatform) {
                                        // iOS : désactiver l'export graphique pour éviter les problèmes mémoire
                                        return@Button
                                }

                                val svgGraph =
                                        generateGrowthGraphSvg(
                                                realPoints = realPoints,
                                                referenceCurves = referenceCurves,
                                                xRange = xRange,
                                                yRange = yRange
                                        )
                                val landscapeGraphHtml =
                                        """
                                        <div style="width: 100%; height: 17cm;">
                                        $svgGraph
                                        </div>
                                        <div style="page-break-after: always;"></div>
                                        """.trimIndent()

                                val exportData =
                                        ExportData(
                                                animal = null,
                                                ration = null,
                                                reference = null,
                                                title =
                                                        "Courbe de croissance - ${animal?.nom ?: ""}",
                                                additionalText = "",
                                                htmlSections =
                                                        listOf(
                                                                HtmlSection(
                                                                        id = genUUID(),
                                                                        title =
                                                                                "Graphique de croissance avec courbes de référence",
                                                                        content =
                                                                                RichTextContent(
                                                                                        blocks =
                                                                                                listOf(
                                                                                                        TextBlock
                                                                                                                .RawHtml(
                                                                                                                        id =
                                                                                                                                genUUID(),
                                                                                                                        html =
                                                                                                                                landscapeGraphHtml
                                                                                                                )
                                                                                                )
                                                                                )
                                                                ),
                                                                HtmlSection(
                                                                        id = genUUID(),
                                                                        title =
                                                                                "Données de poids",
                                                                        content =
                                                                                RichTextContent(
                                                                                        blocks =
                                                                                                listOf(
                                                                                                        TextBlock
                                                                                                                .TableBlock(
                                                                                                                        id =
                                                                                                                                genUUID(),
                                                                                                                        headers =
                                                                                                                                listOf(
                                                                                                                                        "Date",
                                                                                                                                        "Semaine depuis naissance",
                                                                                                                                        "Poids (kg)"
                                                                                                                                ),
                                                                                                                        rows =
                                                                                                                                realPoints
                                                                                                                                        .map {
                                                                                                                                                listOf(
                                                                                                                                                        it
                                                                                                                                                                .first
                                                                                                                                                                .toString(),
                                                                                                                                                        GraphFormattingUtils
                                                                                                                                                                .formatDecimal(
                                                                                                                                                                        it
                                                                                                                                                                                .second
                                                                                                                                                                                .toDouble(),
                                                                                                                                                                        1
                                                                                                                                                                ),
                                                                                                                                                        GraphFormattingUtils
                                                                                                                                                                .formatDecimal(
                                                                                                                                                                        it
                                                                                                                                                                                .third,
                                                                                                                                                                        2
                                                                                                                                                                )
                                                                                                                                                )
                                                                                                                                        }
                                                                                                                )
                                                                                                )
                                                                                )
                                                                )
                                                        ),
                                                isLandscape = true
                                        )

                                PdfExporter.exportDocument(
                                        DocumentType.RATION_ANALYSIS,
                                        exportData,
                                        "croissance_${animal?.nom ?: "animal"}.pdf"
                                )
                        },
                        colors =
                                ButtonDefaults.buttonColors(
                                        backgroundColor = VetNutriColors.Secondary
                                )
                ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export PDF (croissance)")
                }
        }

        GraphCard(
                titre = "Zoom sur la courbe de croissance",
                sousTitre = "Poids en fonction du temps (semaines depuis la naissance)"
        ) {
                XYGraph(
                        xAxisModel = FloatLinearAxisModel(
                                range = xRange,
                                minimumMajorTickIncrement = 2f
                        ),
                        yAxisModel = KoalaPlotExtensions.createSmartYAxisModel(
                                range = yRange
                        ),
                        xAxisTitle = "Semaines depuis la naissance",
                        yAxisTitle = "Poids (kg)",
                        modifier = Modifier.height(400.dp)
                ) {
                        referenceCurves.forEach { (name, points) ->
                                if (points.isNotEmpty()) {
                                        val isMedian = name == "50%"
                                        LinePlot(
                                                data = points,
                                                lineStyle =
                                                        LineStyle(
                                                                brush =
                                                                        SolidColor(
                                                                                if (isMedian)
                                                                                        Color
                                                                                                .DarkGray
                                                                                else Color.Gray
                                                                        ),
                                                                strokeWidth =
                                                                        if (isMedian) 2.dp
                                                                        else 1.dp
                                                        )
                                        )
                                }
                        }

                        if (realPoints.isNotEmpty()) {
                                LinePlot(
                                        data =
                                                realPoints.map {
                                                    Point(
                                                            x = it.second,
                                                            y = it.third.toFloat()
                                                    )
                                                },
                                        symbol = {
                                                androidx.compose.foundation.Canvas(
                                                        modifier = Modifier.size(6.dp)
                                                ) { drawCircle(color = Color.Blue) }
                                        }
                                )
                        }
                }
        }
    }
}

fun generateGrowthGraphSvg(
    realPoints: List<Triple<LocalDate, Float, Double>>,
    referenceCurves: List<Pair<String, List<Point<Float, Float>>>>,
    xRange: ClosedFloatingPointRange<Float>,
    yRange: ClosedFloatingPointRange<Float>,
    width: Int = 600,
    height: Int = 400
): String {
    val padding = 40.0
    val graphWidth = width - 2 * padding
    val graphHeight = height - 2 * padding

    val xMin = xRange.start
    val xMax = xRange.endInclusive
    val yMin = yRange.start
    val yMax = yRange.endInclusive

    fun scaleX(x: Float): Double = padding + (x - xMin) / (xMax - xMin) * graphWidth
    fun scaleY(y: Float): Double =
            height - padding - (y - yMin) / (yMax - yMin) * graphHeight

    val sb = StringBuilder()
    sb.append(
            "<svg width='100%' height='100%' viewBox='0 0 $width $height' xmlns='http://www.w3.org/2000/svg' version='1.1'>"
    )

    sb.append("<rect width='$width' height='$height' fill='white' />")

    sb.append(
            "<line x1='$padding' y1='${height - padding}' x2='${width - padding}' y2='${height - padding}' stroke='black' stroke-width='1' />"
    )
    sb.append(
            "<line x1='$padding' y1='$padding' x2='$padding' y2='${height - padding}' stroke='black' stroke-width='1' />"
    )

    val yRangeSpan = yMax - yMin
    val targetYSteps = 5.0
    val rawYStep = yRangeSpan / targetYSteps
    val magY = 10.0.pow(floor(log10(rawYStep.toDouble())))
    val normY = rawYStep / magY
    val yStep =
            (when {
                        normY < 1.5 -> 1.0
                        normY < 3.5 -> 2.0
                        normY < 7.5 -> 5.0
                        else -> 10.0
                } * magY)
                    .toFloat()

    val startY = (ceil(yMin / yStep) * yStep).toFloat()
    var currentY = startY

    while (currentY <= yMax + (yStep * 0.01f)) {
        val yPos = scaleY(currentY)
        if (yPos >= padding - 1 && yPos <= height - padding + 1) {
            sb.append(
                    "<line x1='$padding' y1='$yPos' x2='${width - padding}' y2='$yPos' stroke='lightgray' stroke-width='0.5' />"
            )
            sb.append(
                    "<text x='${padding - 5}' y='$yPos' font-family='Arial' font-size='10' text-anchor='end' dominant-baseline='middle'>${GraphFormattingUtils.formatDecimal(currentY.toDouble(), 1)}</text>"
            )
        }
        currentY += yStep
    }

    val xStep = 2f // Grille toutes les 2 semaines

    var currentX = (ceil(xMin / xStep) * xStep).toFloat()

    while (currentX <= xMax + (xStep * 0.01f)) {
        val xPos = scaleX(currentX)
        if (xPos >= padding - 1 && xPos <= width - padding + 1) {
            // Ligne de grille verticale
            sb.append(
                    "<line x1='$xPos' y1='$padding' x2='$xPos' y2='${height - padding}' stroke='lightgray' stroke-width='0.5' />"
            )
            // Graduation X
            sb.append(
                    "<line x1='$xPos' y1='${height - padding}' x2='$xPos' y2='${height - padding + 5}' stroke='black' stroke-width='1' />"
            )
            sb.append(
                    "<text x='$xPos' y='${height - padding + 15}' font-family='Arial' font-size='10' text-anchor='middle'>${currentX.toInt()}</text>"
            )
        }
        currentX += xStep
    }

    sb.append(
            "<text x='${width / 2}' y='${height - 5}' font-family='Arial' font-size='12' text-anchor='middle'>Semaines depuis la naissance</text>"
    )
    sb.append(
            "<text x='10' y='${height / 2}' font-family='Arial' font-size='12' text-anchor='middle' transform='rotate(-90 10 ${height / 2})'>Poids (kg)</text>"
    )

    referenceCurves.forEach { (name, points) ->
        if (points.size >= 2) {
            val isMedian = name == "50%"
            val strokeColor = if (isMedian) "#444444" else "#AAAAAA"
            val strokeWidth = if (isMedian) 2.0 else 1.0

            val pathData =
                    points.joinToString(" ") { p ->
                        val x = scaleX(p.x)
                        val y = scaleY(p.y)
                        "L$x,$y"
                    }
            val first = points.first()
            val startX = scaleX(first.x)
            val startY = scaleY(first.y)
            sb.append(
                    "<path d='M$startX,$startY $pathData' fill='none' stroke='$strokeColor' stroke-width='$strokeWidth' />"
            )
        }
    }

    realPoints.forEach { (_, week, weight) ->
        val cx = scaleX(week)
        val cy = scaleY(weight.toFloat())
        sb.append("<circle cx='$cx' cy='$cy' r='4' fill='blue' />")
    }

    sb.append("</svg>")
    return sb.toString()
}

