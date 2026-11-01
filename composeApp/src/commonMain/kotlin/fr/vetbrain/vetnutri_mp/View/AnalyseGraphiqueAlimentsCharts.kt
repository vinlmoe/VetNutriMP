package fr.vetbrain.vetnutri_mp.View

import fr.vetbrain.vetnutri_mp.Localization.translate
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.GraphFormattingUtils
import fr.vetbrain.vetnutri_mp.Utils.KoalaPlotExtensions
import io.github.koalaplot.core.*
import io.github.koalaplot.core.bar.DefaultVerticalBar
import io.github.koalaplot.core.bar.VerticalBarPlot
import io.github.koalaplot.core.line.LinePlot
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.*
import io.github.koalaplot.core.xygraph.Point
import io.github.koalaplot.core.xygraph.XYGraph

// Graphiques de l'analyse graphique des aliments : nuage de points, nutriments personnalisés, histogramme d'énergie

/** Graphique en nuage de points : % énergie protéines vs % énergie lipides */
@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
internal fun GraphiqueNuagePoints(
        alimentsAnalyses: List<AlimentAnalyseData>,
        ongletActif: String,
        alimentSelectionne: String? = null,
        nutrimentX: String? = null,
        nutrimentY: String? = null,
        useDryMatterPer100g: Boolean = false,
        referenceEv: ReferenceEv?,
        equationRepository: EquationRepository?,
        modifier: Modifier = Modifier
) {
    // États pour le zoom et le pan (uniquement pour les scatter plots)
    val zoomPanState = remember { mutableStateOf(ZoomPanState()) }
    val originalRanges = remember(alimentsAnalyses, ongletActif) {
        if (alimentsAnalyses.isEmpty() || ongletActif == "densite_energetique" || ongletActif == "nutriments_perso") {
            null
        } else {
            // Calculer les plages originales
            val points = when (ongletActif) {
                "protein_lipid" -> {
                    alimentsAnalyses.map { data ->
                        Point(
                                x = data.pourcentageProteines.toFloat(),
                                y = data.pourcentageLipides.toFloat()
                        )
                    }
                }
                "phosphore_protein" -> {
                    alimentsAnalyses.map { data ->
                        Point(
                                x = data.phosphorePer1000Kcal.toFloat(),
                                y = data.proteinePer1000Kcal.toFloat()
                        )
                    }
                }
                "calcium_phosphore" -> {
                    alimentsAnalyses.map { data ->
                        Point(
                                x = data.phosphorePer1000Kcal.toFloat(),
                                y = data.calciumPer1000Kcal.toFloat()
                        )
                    }
                }
                else -> emptyList()
            }
            
            if (points.isNotEmpty()) {
                val minX = points.minOf { it.x }.coerceAtLeast(0f)
                val maxX = points.maxOf { it.x }
                val minY = points.minOf { it.y }.coerceAtLeast(0f)
                val maxY = points.maxOf { it.y }
                
                val xRange = arrondirPlage(when (ongletActif) {
                    "phosphore_protein" -> (minX - minX * 0.05f)..(maxX + maxX * 0.05f)
                    else -> (minX - minX * 0.05f)..(maxX.coerceAtMost(100f) + maxX * 0.05f)
                })
                
                val yRange = arrondirPlage(when (ongletActif) {
                    "phosphore_protein" -> (minY - minY * 0.05f)..(maxY + maxY * 0.05f)
                    else -> (minY - minY * 0.05f)..(maxY.coerceAtMost(100f) + maxY * 0.05f)
                })
                
                Pair(xRange, yRange)
            } else {
                null
            }
        }
    }
    
    // Réinitialiser le zoom/pan quand l'onglet change
    LaunchedEffect(ongletActif) {
        zoomPanState.value = ZoomPanState()
    }
    
    Card(modifier = modifier, elevation = AppSizes.elevationMedium) {
        Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
            // Titre dynamique selon l'onglet
            val titre =
                    when (ongletActif) {
                        "protein_lipid" -> "Répartition énergétique : Protéines vs Lipides"
                        "phosphore_protein" -> if (useDryMatterPer100g) "Phosphore vs Protéines (/100g MS)" else "Phosphore vs Protéines (/1000 kcal)"
                        "calcium_phosphore" -> if (useDryMatterPer100g) "Calcium vs Phosphore (/100g MS)" else "Calcium vs Phosphore (/1000 kcal)"
                        "nutriments_perso" -> {
                            val xOption = NUTRIMENT_OPTIONS.find { it.key == nutrimentX }
                            val yOption = NUTRIMENT_OPTIONS.find { it.key == nutrimentY }
                            val unit = if (useDryMatterPer100g) "/100g MS" else "/1000 kcal"
                            if (nutrimentY != null && nutrimentY.isNotEmpty()) {
                                "${xOption?.displayName ?: "X"} vs ${yOption?.displayName ?: "Y"} ($unit)"
                            } else {
                                translate("graphAliments.distribution", xOption?.displayName ?: "", unit)
                            }
                        }
                        else -> "Analyse nutritionnelle"
                    }

            Text(
                    text = titre,
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    color = VetNutriColors.Primary
            )

            Text(
                    text =
                            "Chaque point représente un aliment (numéroté de 1 à ${alimentsAnalyses.size})",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
            
            // Informations supplémentaires selon le type de graphique
            when (ongletActif) {
                "densite_energetique" -> {
                    val avgDensity = alimentsAnalyses.map { it.densiteEnergetique }.average()
                    Text(
                            text = translate("graphAliments.densiteMoyenne", GraphFormattingUtils.formatEnergyDensity(avgDensity)),
                            style = MaterialTheme.typography.caption,
                            color = VetNutriColors.Primary.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                    )
                }
                "protein_lipid" -> {
                    val avgProtein = alimentsAnalyses.map { it.pourcentageProteines }.average()
                    val avgLipid = alimentsAnalyses.map { it.pourcentageLipides }.average()
                    Text(
                            text = translate("graphAliments.moyennesProtLip", GraphFormattingUtils.formatPercentage(avgProtein), GraphFormattingUtils.formatPercentage(avgLipid)),
                            style = MaterialTheme.typography.caption,
                            color = VetNutriColors.Primary.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                    )
                }
                "phosphore_protein" -> {
                    val avgPhosphore = alimentsAnalyses.map { it.phosphorePer1000Kcal }.average()
                    val avgProtein = alimentsAnalyses.map { it.proteinePer1000Kcal }.average()
                    Text(
                            text = translate("graphAliments.moyennesPhosProt", GraphFormattingUtils.formatNutrientPer1000Kcal(avgPhosphore), GraphFormattingUtils.formatNutrientPer1000Kcal(avgProtein)),
                            style = MaterialTheme.typography.caption,
                            color = VetNutriColors.Primary.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                    )
                }
                "calcium_phosphore" -> {
                    val avgCalcium = alimentsAnalyses.map { it.calciumPer1000Kcal }.average()
                    val avgPhosphore = alimentsAnalyses.map { it.phosphorePer1000Kcal }.average()
                    Text(
                            text = translate("graphAliments.moyennesCaPhos", GraphFormattingUtils.formatNutrientPer1000Kcal(avgCalcium), GraphFormattingUtils.formatNutrientPer1000Kcal(avgPhosphore)),
                            style = MaterialTheme.typography.caption,
                            color = VetNutriColors.Primary.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                    )
                }
            }

            // Légende des lignes de référence pour le graphique Calcium/Phosphore
            if (ongletActif == "calcium_phosphore") {
                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ligne rouge 1:1
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp, 2.dp)) {
                        drawLine(
                                color = Color.Red.copy(alpha = 0.7f),
                                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                                end =
                                        androidx.compose.ui.geometry.Offset(
                                                size.width,
                                                size.height / 2
                                        ),
                                strokeWidth = 2.dp.toPx()
                        )
                    }
                    Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                    Text(
                            text = translate("auto.view.analysegraphiquealimentsview.ratio_1_1"),
                            style = MaterialTheme.typography.caption,
                            color = Color.Red.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.width(AppSizes.paddingMedium))

                    // Ligne bleue 2:1
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp, 2.dp)) {
                        drawLine(
                                color = Color.Blue.copy(alpha = 0.7f),
                                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                                end =
                                        androidx.compose.ui.geometry.Offset(
                                                size.width,
                                                size.height / 2
                                        ),
                                strokeWidth = 2.dp.toPx()
                        )
                    }
                    Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                    Text(
                            text = translate("auto.view.analysegraphiquealimentsview.ratio_2_1"),
                            style = MaterialTheme.typography.caption,
                            color = Color.Blue.copy(alpha = 0.7f)
                    )
                }
            }

            // Légende des lignes de référence pour le graphique Protéines/Phosphore
            if (ongletActif == "phosphore_protein") {
                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ligne verte 35:1
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp, 2.dp)) {
                        drawLine(
                                color = Color.Green.copy(alpha = 0.7f),
                                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                                end =
                                        androidx.compose.ui.geometry.Offset(
                                                size.width,
                                                size.height / 2
                                        ),
                                strokeWidth = 2.dp.toPx()
                        )
                    }
                    Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                    Text(
                            text = translate("auto.view.analysegraphiquealimentsview.ratio_35_1"),
                            style = MaterialTheme.typography.caption,
                            color = Color.Green.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.width(AppSizes.paddingMedium))

                    // Ligne orange 25:1
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp, 2.dp)) {
                        drawLine(
                                color = Color(0xFFFF9800).copy(alpha = 0.7f),
                                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                                end =
                                        androidx.compose.ui.geometry.Offset(
                                                size.width,
                                                size.height / 2
                                        ),
                                strokeWidth = 2.dp.toPx()
                        )
                    }
                    Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                    Text(
                            text = translate("auto.view.analysegraphiquealimentsview.ratio_25_1"),
                            style = MaterialTheme.typography.caption,
                            color = Color(0xFFFF9800).copy(alpha = 0.7f)
                    )
                }
            }

            // Légende des lignes de référence pour le graphique Répartition énergétique
            if (ongletActif == "protein_lipid") {
                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ligne magenta 80-x
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp, 2.dp)) {
                        drawLine(
                                color = Color.Magenta.copy(alpha = 0.7f),
                                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                                end =
                                        androidx.compose.ui.geometry.Offset(
                                                size.width,
                                                size.height / 2
                                        ),
                                strokeWidth = 2.dp.toPx()
                        )
                    }
                    Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                    Text(
                            text = translate("auto.view.analysegraphiquealimentsview.20_ena"),
                            style = MaterialTheme.typography.caption,
                            color = Color.Magenta.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.width(AppSizes.paddingMedium))

                    // Ligne cyan 60-x
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp, 2.dp)) {
                        drawLine(
                                color = Color.Cyan.copy(alpha = 0.7f),
                                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                                end =
                                        androidx.compose.ui.geometry.Offset(
                                                size.width,
                                                size.height / 2
                                        ),
                                strokeWidth = 2.dp.toPx()
                        )
                    }
                    Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                    Text(
                            text = translate("auto.view.analysegraphiquealimentsview.60_ena"),
                            style = MaterialTheme.typography.caption,
                            color = Color.Cyan.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.width(AppSizes.paddingMedium))

                    // Ligne jaune 40-x
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp, 2.dp)) {
                        drawLine(
                                color = Color.Yellow.copy(alpha = 0.7f),
                                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                                end =
                                        androidx.compose.ui.geometry.Offset(
                                                size.width,
                                                size.height / 2
                                        ),
                                strokeWidth = 2.dp.toPx()
                        )
                    }
                    Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                    Text(
                            text = translate("auto.view.analysegraphiquealimentsview.40_ena"),
                            style = MaterialTheme.typography.caption,
                            color = Color.Yellow.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

            if (alimentsAnalyses.isNotEmpty()) {
                // Affichage selon le type de graphique
                if (ongletActif == "densite_energetique") {
                    // 📊 HISTOGRAMME DENSITÉ ÉNERGÉTIQUE
                    HistogrammeEnergieAliments(
                            alimentsAnalyses = alimentsAnalyses,
                            alimentSelectionne = alimentSelectionne,
                            useDryMatterPer100g = useDryMatterPer100g,
                            modifier = Modifier.fillMaxWidth().height(400.dp)
                    )
                } else if (ongletActif == "nutriments_perso") {
                    // ✨ GRAPHIQUE PERSONNALISÉ
                    GraphiqueNutrimentsPersonnalise(
                            alimentsAnalyses = alimentsAnalyses,
                            nutrimentX = nutrimentX ?: "proteine",
                            nutrimentY = nutrimentY,
                            alimentSelectionne = alimentSelectionne,
                            useDryMatterPer100g = useDryMatterPer100g,
                            referenceEv = referenceEv,
                            equationRepository = equationRepository,
                            modifier = Modifier.fillMaxWidth().height(400.dp)
                    )
                } else {
                    // 📈 SCATTER PLOTS (PROTÉINES/LIPIDES et PHOSPHORE/PROTÉINES)
                    // Préparer les données pour le graphique selon l'onglet actif
                    val points =
                            when (ongletActif) {
                                "protein_lipid" -> {
                                    alimentsAnalyses.map { data ->
                                        Point(
                                                x = data.pourcentageProteines.toFloat(),
                                                y = data.pourcentageLipides.toFloat()
                                        )
                                    }
                                }
                                "phosphore_protein" -> {
                                    alimentsAnalyses.map { data ->
                                        Point(
                                                x = data.phosphorePer1000Kcal.toFloat(),
                                                y = data.proteinePer1000Kcal.toFloat()
                                        )
                                    }
                                }
                                "calcium_phosphore" -> {
                                    alimentsAnalyses.map { data ->
                                        Point(
                                                x = data.phosphorePer1000Kcal.toFloat(),
                                                y = data.calciumPer1000Kcal.toFloat()
                                        )
                                    }
                                }
                                else -> {
                                    alimentsAnalyses.map { data ->
                                        Point(
                                                x = data.pourcentageProteines.toFloat(),
                                                y = data.pourcentageLipides.toFloat()
                                        )
                                    }
                                }
                            }

                    // Utiliser les plages originales ou calculer si nécessaire
                    val (baseXRange, baseYRange) = originalRanges ?: run {
                        val minX = points.minOf { it.x }.coerceAtLeast(0f)
                        val maxX = points.maxOf { it.x }
                        val minY = points.minOf { it.y }.coerceAtLeast(0f)
                        val maxY = points.maxOf { it.y }
                        
                        val xRange = arrondirPlage(when (ongletActif) {
                            "phosphore_protein" -> (minX - minX * 0.05f)..(maxX + maxX * 0.05f)
                            else -> (minX - minX * 0.05f)..(maxX.coerceAtMost(100f) + maxX * 0.05f)
                        })
                        
                        val yRange = arrondirPlage(when (ongletActif) {
                            "phosphore_protein" -> (minY - minY * 0.05f)..(maxY + maxY * 0.05f)
                            else -> (minY - minY * 0.05f)..(maxY.coerceAtMost(100f) + maxY * 0.05f)
                        })
                        
                        Pair(xRange, yRange)
                    }
                    
                    // Calculer les plages zoomées
                    val xRange = calculateZoomedRange(baseXRange, zoomPanState.value, isXAxis = true)
                    val yRange = calculateZoomedRange(baseYRange, zoomPanState.value, isXAxis = false)
                    
                    // Boutons de zoom (pour desktop où le pinch ne fonctionne pas)
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButtonWithTooltip(
                                onClick = {
                                    // Zoom out
                                    val newScaleX = (zoomPanState.value.scaleX * 0.9f).coerceIn(0.5f, 5f)
                                    val newScaleY = (zoomPanState.value.scaleY * 0.9f).coerceIn(0.5f, 5f)
                                    zoomPanState.value = ZoomPanState(
                                            scaleX = newScaleX,
                                            scaleY = newScaleY,
                                            panX = zoomPanState.value.panX,
                                            panY = zoomPanState.value.panY
                                    )
                                },
                                imageVector = Icons.Default.ZoomOut,
                                contentDescription = translate("graph.zoomOutTooltip"),
                                tooltip = translate("graph.zoomOutTooltip")
                        )
                        IconButtonWithTooltip(
                                onClick = {
                                    // Zoom in
                                    val newScaleX = (zoomPanState.value.scaleX * 1.1f).coerceIn(0.5f, 5f)
                                    val newScaleY = (zoomPanState.value.scaleY * 1.1f).coerceIn(0.5f, 5f)
                                    zoomPanState.value = ZoomPanState(
                                            scaleX = newScaleX,
                                            scaleY = newScaleY,
                                            panX = zoomPanState.value.panX,
                                            panY = zoomPanState.value.panY
                                    )
                                },
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = translate("graph.zoomInTooltip"),
                                tooltip = translate("graph.zoomInTooltip")
                        )
                        if (zoomPanState.value.scaleX != 1f || zoomPanState.value.scaleY != 1f || 
                            zoomPanState.value.panX != 0f || zoomPanState.value.panY != 0f) {
                            TextButton(
                                    onClick = { zoomPanState.value = ZoomPanState() }
                            ) {
                                Text(translate("crossConsultation.reset"), fontSize = 12.sp)
                            }
                        }
                    }

                    // 🎯 Graphique avec numéros superposés et zoom/pan
                    BoxWithConstraints(modifier = Modifier.height(400.dp).clipToBounds()) {
                        // Graphique principal avec gestes de zoom/pan
                        XYGraph(
                                xAxisModel = KoalaPlotExtensions.createSmartXAxisModel(range = xRange),
                                xAxisLabels = { GraphFormattingUtils.formatAxisTick(it, xRange) },
                                yAxisModel = KoalaPlotExtensions.createSmartYAxisModel(range = yRange),
                                yAxisLabels = { GraphFormattingUtils.formatAxisTick(it, yRange) },
                                xAxisTitle = when (ongletActif) {
                                    "protein_lipid" -> "Protéines (% énergie)"
                                    "phosphore_protein" -> if (useDryMatterPer100g) "Phosphore (g/100g MS)" else "Phosphore (g/1000 kcal)"
                                    "calcium_phosphore" -> if (useDryMatterPer100g) "Phosphore (g/100g MS)" else "Phosphore (g/1000 kcal)"
                                    else -> ""
                                },
                                yAxisTitle = when (ongletActif) {
                                    "protein_lipid" -> "Lipides (% énergie)"
                                    "phosphore_protein" -> if (useDryMatterPer100g) "Protéines (g/100g MS)" else "Protéines (g/1000 kcal)"
                                    "calcium_phosphore" -> if (useDryMatterPer100g) "Calcium (g/100g MS)" else "Calcium (g/1000 kcal)"
                                    else -> ""
                                },
                                modifier = Modifier
                                        .fillMaxSize()
                                        .clipToBounds()
                                        .pointerInput(Unit) {
                                            detectTransformGestures { _, pan, zoom, _ ->
                                                // Limiter le zoom entre 0.5x et 5x
                                                val newScaleX = (zoomPanState.value.scaleX * zoom).coerceIn(0.5f, 5f)
                                                val newScaleY = (zoomPanState.value.scaleY * zoom).coerceIn(0.5f, 5f)
                                                
                                                // Calculer les plages actuelles (zoomées) pour le pan
                                                val currentXRange = calculateZoomedRange(baseXRange, zoomPanState.value, isXAxis = true)
                                                val currentYRange = calculateZoomedRange(baseYRange, zoomPanState.value, isXAxis = false)
                                                
                                                // Convertir le pan en coordonnées de données (basé sur la plage actuelle)
                                                val panXDelta = pan.x / size.width * (currentXRange.endInclusive - currentXRange.start)
                                                val panYDelta = -pan.y / size.height * (currentYRange.endInclusive - currentYRange.start)
                                                
                                                zoomPanState.value = ZoomPanState(
                                                        scaleX = newScaleX,
                                                        scaleY = newScaleY,
                                                        panX = zoomPanState.value.panX + panXDelta,
                                                        panY = zoomPanState.value.panY + panYDelta
                                                )
                                            }
                                        }
                        ) {
                            FoodGraphRationAreas(alimentsAnalyses, points)
                            // Afficher chaque point individuellement avec LinePlot et symbol
                            // Filtrer les points qui sont dans la plage visible
                            alimentsAnalyses.forEachIndexed { index, data ->
                                val point =
                                        points[index] // Utiliser le point calculé selon l'onglet
                                // actif
                                
                                // Vérifier si le point est dans la plage visible
                                val isPointVisible = point.x >= xRange.start && 
                                                    point.x <= xRange.endInclusive &&
                                                    point.y >= yRange.start && 
                                                    point.y <= yRange.endInclusive
                                
                                if (isPointVisible) {
                                    LinePlot(
                                            data = listOf(point),
                                            symbol = {
                                            // Point principal avec couleur selon sélection
                                            val couleurPoint =
                                                    if (data.rationActual != null) { data.rationColor!! } else if (data.aliment.uuid == alimentSelectionne) {
                                                        Color(0xFF9C27B0) // Violet pour sélectionné
                                                    } else {
                                                        // Vérifier l'humidité pour les aliments non
                                                        // sélectionnés
                                                        val humidite =
                                                                data.aliment.getNutrient(
                                                                        NutrientMain.HUMIDITE
                                                                )
                                                        if (humidite == null || humidite < 20.0) {
                                                            VetNutriColors.FoodAnalysisDry
                                                        } else {
                                                            VetNutriColors.FoodAnalysisWet
                                                        }
                                                    }

                                            androidx.compose.foundation.Canvas(
                                                    modifier = Modifier.size(12.dp)
                                            ) {
                                                drawCircle(
                                                        color = couleurPoint,
                                                        radius = 6f,
                                                        center = center
                                                )
                                            }
                                        }
                                    )
                                }
                            }

                            // 🔸 LIGNES DE RÉFÉRENCE pour le graphique Calcium/Phosphore
                            if (ongletActif == "calcium_phosphore") {
                                // Ligne 1:1 (Calcium = Phosphore)
                                val ligne1to1 =
                                        listOf(
                                                Point(x = xRange.start, y = xRange.start),
                                                Point(
                                                        x = xRange.endInclusive,
                                                        y = xRange.endInclusive
                                                )
                                        )
                                LinePlot(
                                        data = ligne1to1,
                                        lineStyle =
                                                LineStyle(
                                                        brush =
                                                                SolidColor(
                                                                        Color.Red.copy(alpha = 0.7f)
                                                                ),
                                                        strokeWidth = 2.dp
                                                )
                                )

                                // Ligne 2:1 (Calcium = 2 × Phosphore)
                                val ligne2to1 =
                                        listOf(
                                                Point(x = xRange.start, y = xRange.start * 2),
                                                Point(
                                                        x = xRange.endInclusive,
                                                        y = xRange.endInclusive * 2
                                                )
                                        )
                                LinePlot(
                                        data = ligne2to1,
                                        lineStyle =
                                                LineStyle(
                                                        brush =
                                                                SolidColor(
                                                                        Color.Blue.copy(
                                                                                alpha = 0.7f
                                                                        )
                                                                ),
                                                        strokeWidth = 2.dp
                                                )
                                )
                            }

                            // 🔸 LIGNES DE RÉFÉRENCE pour le graphique Protéines/Phosphore
                            if (ongletActif == "phosphore_protein") {
                                // Ligne 35:1 (Protéines = 35 × Phosphore)
                                val ligne35to1 =
                                        listOf(
                                                Point(x = xRange.start, y = xRange.start * 35),
                                                Point(
                                                        x = xRange.endInclusive,
                                                        y = xRange.endInclusive * 35
                                                )
                                        )
                                LinePlot(
                                        data = ligne35to1,
                                        lineStyle =
                                                LineStyle(
                                                        brush =
                                                                SolidColor(
                                                                        Color.Green.copy(
                                                                                alpha = 0.7f
                                                                        )
                                                                ),
                                                        strokeWidth = 2.dp
                                                )
                                )

                                // Ligne 25:1 (Protéines = 25 × Phosphore)
                                val ligne25to1 =
                                        listOf(
                                                Point(x = xRange.start, y = xRange.start * 25),
                                                Point(
                                                        x = xRange.endInclusive,
                                                        y = xRange.endInclusive * 25
                                                )
                                        )
                                LinePlot(
                                        data = ligne25to1,
                                        lineStyle =
                                                LineStyle(
                                                        brush =
                                                                SolidColor(
                                                                        Color(0xFFFF9800)
                                                                                .copy(alpha = 0.7f)
                                                                ),
                                                        strokeWidth = 2.dp
                                                )
                                )
                            }

                            // 🔸 LIGNES DE RÉFÉRENCE pour le graphique Répartition énergétique
                            // (Protéines/Lipides)
                            if (ongletActif == "protein_lipid") {
                                // Ligne 80-x : Protéines + Lipides = 80% (reste = glucides)
                                val ligne80MinusX =
                                        listOf(
                                                Point(x = xRange.start, y = 80f - xRange.start),
                                                Point(
                                                        x = xRange.endInclusive,
                                                        y = 80f - xRange.endInclusive
                                                )
                                        )
                                LinePlot(
                                        data = ligne80MinusX,
                                        lineStyle =
                                                LineStyle(
                                                        brush =
                                                                SolidColor(
                                                                        Color.Magenta.copy(
                                                                                alpha = 0.7f
                                                                        )
                                                                ),
                                                        strokeWidth = 2.dp
                                                )
                                )

                                // Ligne 60-x : Protéines + Lipides = 60% (reste = glucides)
                                val ligne60MinusX =
                                        listOf(
                                                Point(x = xRange.start, y = 60f - xRange.start),
                                                Point(
                                                        x = xRange.endInclusive,
                                                        y = 60f - xRange.endInclusive
                                                )
                                        )
                                LinePlot(
                                        data = ligne60MinusX,
                                        lineStyle =
                                                LineStyle(
                                                        brush =
                                                                SolidColor(
                                                                        Color.Cyan.copy(
                                                                                alpha = 0.7f
                                                                        )
                                                                ),
                                                        strokeWidth = 2.dp
                                                )
                                )

                                // Ligne 40-x : Protéines + Lipides = 40% (reste = glucides)
                                val ligne40MinusX =
                                        listOf(
                                                Point(x = xRange.start, y = 40f - xRange.start),
                                                Point(
                                                        x = xRange.endInclusive,
                                                        y = 40f - xRange.endInclusive
                                                )
                                        )
                                LinePlot(
                                        data = ligne40MinusX,
                                        lineStyle =
                                                LineStyle(
                                                        brush =
                                                                SolidColor(
                                                                        Color.Yellow.copy(
                                                                                alpha = 0.7f
                                                                        )
                                                                ),
                                                        strokeWidth = 2.dp
                                                )
                                )
                            }
                        }

                        // 🎯 Numéros superposés directement avec correction des marges d'axes
                        // Marges typiques des axes KoalaPlot (estimation)
                        // 🔧 Marges AJUSTÉES basées sur l'observation des logs (décalage
                        // empirique)
                        val leftAxisMargin =
                                10.dp // Marge pour les labels de l'axe Y (augmentée)
                        val bottomAxisMargin =
                                15.dp // Marge pour les labels de l'axe X (augmentée)
                        val topMargin = 10.dp // Marge supérieure
                        val rightMargin = 20.dp // Marge droite

                        // Zone de graphique effective
                        val effectiveGraphWidth = maxWidth - leftAxisMargin - rightMargin
                        val effectiveGraphHeight = maxHeight - bottomAxisMargin - topMargin
                        
                        alimentsAnalyses.forEachIndexed { index, data ->
                            val point =
                                    points[index] // Utiliser le point calculé selon l'onglet actif
                            
                            // Vérifier si le point est dans la plage visible
                            val isPointVisible = point.x >= xRange.start && 
                                                point.x <= xRange.endInclusive &&
                                                point.y >= yRange.start && 
                                                point.y <= yRange.endInclusive
                            
                            if (!isPointVisible) return@forEachIndexed
                            
                            // Calculer la position du numéro avec les vraies dimensions
                            val xPosition =
                                    ((point.x - xRange.start) /
                                            (xRange.endInclusive - xRange.start))
                            val yPosition =
                                    1f -
                                            ((point.y - yRange.start) /
                                                    (yRange.endInclusive - yRange.start))
                            
                            // Vérifier si le label est dans la zone visible (avec une petite marge)
                            val labelX = leftAxisMargin + (xPosition * effectiveGraphWidth.value).dp - 10.dp
                            val labelY = topMargin + (yPosition * effectiveGraphHeight.value).dp - 30.dp
                            val isLabelVisible = labelX >= (-20).dp && 
                                                labelX <= maxWidth + 20.dp &&
                                                labelY >= (-20).dp && 
                                                labelY <= maxHeight + 20.dp
                            
                            if (!isLabelVisible) return@forEachIndexed

                            // Couleur selon la sélection et l'humidité
                            val numeroColor =
                                    if (data.rationActual != null) { data.rationColor!! } else if (data.aliment.uuid == alimentSelectionne) {
                                        Color(0xFF9C27B0) // Violet pour sélectionné
                                    } else {
                                        // Vérifier l'humidité pour les aliments non sélectionnés
                                        val humidite =
                                                data.aliment.getNutrient(NutrientMain.HUMIDITE)
                                        if (humidite == null || humidite < 20.0) {
                                            VetNutriColors.FoodAnalysisDry
                                        } else {
                                            VetNutriColors.FoodAnalysisWet
                                        }
                                    }

                            Box(
                                    modifier =
                                            Modifier.fillMaxSize()
                                                    .wrapContentSize(Alignment.TopStart)
                                                    .offset(
                                                            x =
                                                                    leftAxisMargin +
                                                                            (xPosition *
                                                                                            effectiveGraphWidth
                                                                                                    .value)
                                                                                    .dp -
                                                                            10.dp, // Position dans
                                                            // la zone
                                                            // effective +
                                                            // centrage
                                                            y =
                                                                    topMargin +
                                                                            (yPosition *
                                                                                            effectiveGraphHeight
                                                                                                    .value)
                                                                                    .dp -
                                                                            30.dp // Position dans
                                                            // la zone
                                                            // effective +
                                                            // au-dessus du
                                                            // point
                                                            ),
                                    contentAlignment = Alignment.Center
                            ) {
                                // Fond du numéro
                                androidx.compose.foundation.Canvas(
                                        modifier = Modifier.size(20.dp)
                                ) {
                                    drawCircle(color = Color.White, radius = 10.dp.toPx())
                                    drawCircle(
                                            color = numeroColor,
                                            radius = 10.dp.toPx(),
                                            style = Stroke(width = 2.dp.toPx())
                                    )
                                }

                                // Numéro
                                Text(
                                        text = data.graphLabel,
                                        style =
                                                MaterialTheme.typography.caption.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                ),
                                        color = numeroColor
                                )
                            }
                        }
                    }
                } // Fin du bloc else (scatter plots)
            } else {
                Text(
                        text = translate("auto.view.analysegraphiquealimentsview.aucune_donnee_disponible_pour_l_analyse"),
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/** Graphique personnalisé pour les nutriments sélectionnés */
@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun GraphiqueNutrimentsPersonnalise(
        alimentsAnalyses: List<AlimentAnalyseData>,
        nutrimentX: String?,
        nutrimentY: String?,
        alimentSelectionne: String? = null,
        useDryMatterPer100g: Boolean = false,
        referenceEv: ReferenceEv?,
        equationRepository: EquationRepository?,
        modifier: Modifier = Modifier
) {
    // États pour le zoom et le pan
    val zoomPanState = remember { mutableStateOf(ZoomPanState()) }
    
    // Réinitialiser le zoom/pan quand les nutriments changent
    LaunchedEffect(nutrimentX, nutrimentY) {
        zoomPanState.value = ZoomPanState()
    }
    
    if (alimentsAnalyses.isEmpty()) {
        Text(
                text = translate("auto.view.analysegraphiquealimentsview.aucune_donnee_disponible_pour_le_graphique_perso"),
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        return
    }

    // Récupérer les informations des nutriments sélectionnés
    val xOption = NUTRIMENT_OPTIONS.find { it.key == nutrimentX }
    val yOption = NUTRIMENT_OPTIONS.find { it.key == nutrimentY }

    // Si aucun nutriment X n'est sélectionné
    if (nutrimentX == null) {
        Text(
                text = translate("graph.selectNutrimentsXHint"),
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        return
    }

    var scatterPoints by remember(
        nutrimentX,
        nutrimentY,
        alimentsAnalyses,
        referenceEv,
        equationRepository,
        useDryMatterPer100g
    ) {
        mutableStateOf<List<Point<Float, Float>>>(emptyList())
    }
    var histogramValues by remember(
        nutrimentX,
        alimentsAnalyses,
        referenceEv,
        equationRepository,
        useDryMatterPer100g
    ) {
        mutableStateOf<List<Float>>(emptyList())
    }
    var isDataLoading by remember(
        nutrimentX,
        nutrimentY,
        alimentsAnalyses,
        referenceEv,
        equationRepository,
        useDryMatterPer100g
    ) {
        mutableStateOf(true)
    }

    LaunchedEffect(
        nutrimentX,
        nutrimentY,
        alimentsAnalyses,
        referenceEv,
        equationRepository,
        useDryMatterPer100g
    ) {
        isDataLoading = true
        if (nutrimentY != null && nutrimentY.isNotEmpty()) {
            scatterPoints =
                alimentsAnalyses.map { data ->
                    Point(
                        x = data.getNutrimentValue(
                                nutrimentX,
                                referenceEv,
                                equationRepository,
                                useDryMatterPer100g
                        ).toFloat(),
                        y = data.getNutrimentValue(
                                nutrimentY,
                                referenceEv,
                                equationRepository,
                                useDryMatterPer100g
                        ).toFloat()
                    )
                }
        } else {
            histogramValues =
                alimentsAnalyses.map { data ->
                    data.getNutrimentValue(
                            nutrimentX,
                            referenceEv,
                            equationRepository,
                            useDryMatterPer100g
                    ).toFloat()
                }
        }
        isDataLoading = false
    }

    if (isDataLoading) {
        Text(
                text = translate("auto.view.analysegraphiquealimentsview.chargement_des_donnees"),
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        return
    }

    // Si Y est sélectionné (et pas "aucun") : scatter plot
    if (nutrimentY != null && nutrimentY.isNotEmpty()) {
        // 📈 SCATTER PLOT : X vs Y
        val points = scatterPoints

        // Vérifier que nous avons des données valides
        if (points.isEmpty() || points.all { it.x == 0f && it.y == 0f }) {
            Text(
                    text = translate("graph.insufficientDataScatter"),
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
            return
        }

        // Calculer les plages de base avec validation
        val minX = points.minOf { it.x }.coerceAtLeast(0f)
        val maxX = points.maxOf { it.x }

        // S'assurer que minX != maxX pour éviter la division par zéro
        val safeMinX = if (minX == maxX) minX * 0.9f else minX
        val safeMaxX = if (minX == maxX) maxX * 1.1f else maxX

        val minY = points.minOf { it.y }.coerceAtLeast(0f)
        val maxY = points.maxOf { it.y }

        // S'assurer que minY != maxY pour éviter la division par zéro
        val safeMinY = if (minY == maxY) minY * 0.9f else minY
        val safeMaxY = if (minY == maxY) maxY * 1.1f else maxY

        val baseXRange = arrondirPlage(
                (safeMinX - safeMinX * 0.05f).coerceAtLeast(0f)..(safeMaxX + safeMaxX * 0.05f)
                                .coerceAtLeast(safeMinX + 0.1f)
        )
        val baseYRange = arrondirPlage(
                (safeMinY - safeMinY * 0.05f).coerceAtLeast(0f)..(safeMaxY + safeMaxY * 0.05f)
                                .coerceAtLeast(safeMinY + 0.1f)
        )
        
        // Calculer les plages zoomées
        val xRange = calculateZoomedRange(baseXRange, zoomPanState.value, isXAxis = true)
        val yRange = calculateZoomedRange(baseYRange, zoomPanState.value, isXAxis = false)
        
        // Boutons de zoom (pour desktop où le pinch ne fonctionne pas)
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
        ) {
            IconButtonWithTooltip(
                    onClick = {
                        // Zoom out
                        val newScaleX = (zoomPanState.value.scaleX * 0.9f).coerceIn(0.5f, 5f)
                        val newScaleY = (zoomPanState.value.scaleY * 0.9f).coerceIn(0.5f, 5f)
                        zoomPanState.value = ZoomPanState(
                                scaleX = newScaleX,
                                scaleY = newScaleY,
                                panX = zoomPanState.value.panX,
                                panY = zoomPanState.value.panY
                        )
                    },
                    imageVector = Icons.Default.ZoomOut,
                    contentDescription = translate("graph.zoomOutTooltip"),
                    tooltip = translate("graph.zoomOutTooltip")
            )
            IconButtonWithTooltip(
                    onClick = {
                        // Zoom in
                        val newScaleX = (zoomPanState.value.scaleX * 1.1f).coerceIn(0.5f, 5f)
                        val newScaleY = (zoomPanState.value.scaleY * 1.1f).coerceIn(0.5f, 5f)
                        zoomPanState.value = ZoomPanState(
                                scaleX = newScaleX,
                                scaleY = newScaleY,
                                panX = zoomPanState.value.panX,
                                panY = zoomPanState.value.panY
                        )
                    },
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = translate("graph.zoomInTooltip"),
                    tooltip = translate("graph.zoomInTooltip")
            )
            if (zoomPanState.value.scaleX != 1f || zoomPanState.value.scaleY != 1f || 
                zoomPanState.value.panX != 0f || zoomPanState.value.panY != 0f) {
                TextButton(
                        onClick = { zoomPanState.value = ZoomPanState() }
                ) {
                    Text(translate("crossConsultation.reset"), fontSize = 12.sp)
                }
            }
        }

        BoxWithConstraints(modifier = modifier.clipToBounds()) {
            XYGraph(
                    xAxisModel = KoalaPlotExtensions.createSmartXAxisModel(range = xRange),
                    xAxisLabels = { GraphFormattingUtils.formatAxisTick(it, xRange) },
                    yAxisModel = KoalaPlotExtensions.createSmartYAxisModel(range = yRange),
                    yAxisLabels = { GraphFormattingUtils.formatAxisTick(it, yRange) },
                    xAxisTitle = "${xOption?.displayName} (${if (useDryMatterPer100g) "/100g MS" else "/1000 kcal"})",
                    yAxisTitle = "${yOption?.displayName} (${if (useDryMatterPer100g) "/100g MS" else "/1000 kcal"})",
                    modifier = Modifier
                            .fillMaxSize()
                            .clipToBounds()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    // Limiter le zoom entre 0.5x et 5x
                                    val newScaleX = (zoomPanState.value.scaleX * zoom).coerceIn(0.5f, 5f)
                                    val newScaleY = (zoomPanState.value.scaleY * zoom).coerceIn(0.5f, 5f)
                                    
                                    // Calculer les plages actuelles (zoomées) pour le pan
                                    val currentXRange = calculateZoomedRange(baseXRange, zoomPanState.value, isXAxis = true)
                                    val currentYRange = calculateZoomedRange(baseYRange, zoomPanState.value, isXAxis = false)
                                    
                                    // Convertir le pan en coordonnées de données (basé sur la plage actuelle)
                                    val panXDelta = pan.x / size.width * (currentXRange.endInclusive - currentXRange.start)
                                    val panYDelta = -pan.y / size.height * (currentYRange.endInclusive - currentYRange.start)
                                    
                                    zoomPanState.value = ZoomPanState(
                                            scaleX = newScaleX,
                                            scaleY = newScaleY,
                                            panX = zoomPanState.value.panX + panXDelta,
                                            panY = zoomPanState.value.panY + panYDelta
                                    )
                                }
                            }
            ) {
                FoodGraphRationAreas(alimentsAnalyses, points)
                // Afficher chaque point (uniquement ceux dans la plage visible)
                alimentsAnalyses.forEachIndexed { index, data ->
                    val point = points[index]
                    
                    // Vérifier si le point est dans la plage visible
                    val isPointVisible = point.x >= xRange.start && 
                                        point.x <= xRange.endInclusive &&
                                        point.y >= yRange.start && 
                                        point.y <= yRange.endInclusive
                    
                    if (!isPointVisible) return@forEachIndexed
                    
                    LinePlot(
                            data = listOf(point),
                            symbol = {
                                val couleurPoint =
                                        if (data.rationActual != null) { data.rationColor!! } else if (data.aliment.uuid == alimentSelectionne) {
                                            Color(0xFF9C27B0) // Violet pour sélectionné
                                        } else {
                                            // Vérifier l'humidité pour les aliments non sélectionnés
                                            val humidite =
                                                    data.aliment.getNutrient(NutrientMain.HUMIDITE)
                                            if (humidite == null || humidite < 20.0) {
                                                VetNutriColors.FoodAnalysisDry
                                            } else {
                                                VetNutriColors.FoodAnalysisWet
                                            }
                                        }

                                androidx.compose.foundation.Canvas(modifier = Modifier.size(10.dp)) {
                                    drawCircle(color = couleurPoint, radius = 5f, center = center)
                                }
                            }
                    )
                }
            }
            
            // Numéros superposés (en dehors de XYGraph pour pouvoir utiliser maxWidth/maxHeight)
            BoxWithConstraints(modifier = Modifier.fillMaxSize().clipToBounds()) {
                // Marges typiques des axes KoalaPlot (estimation)
                // 🔧 Marges AJUSTÉES basées sur l'observation des logs (décalage empirique)
                val leftAxisMargin = 10.dp // Marge pour les labels de l'axe Y (augmentée)
                val bottomAxisMargin = 15.dp // Marge pour les labels de l'axe X (augmentée)
                val topMargin = 10.dp // Marge supérieure
                val rightMargin = 20.dp // Marge droite

                // Zone de graphique effective
                val effectiveGraphWidth = maxWidth - leftAxisMargin - rightMargin
                val effectiveGraphHeight = maxHeight - bottomAxisMargin - topMargin
                
                alimentsAnalyses.forEachIndexed { index, data ->
                    val point = points[index]
                    
                    // Vérifier si le point est dans la plage visible
                    val isPointVisible = point.x >= xRange.start && 
                                        point.x <= xRange.endInclusive &&
                                        point.y >= yRange.start && 
                                        point.y <= yRange.endInclusive
                    
                    if (!isPointVisible) return@forEachIndexed
                    
                    val xPosition =
                            ((point.x - xRange.start) / (xRange.endInclusive - xRange.start))
                    val yPosition =
                            1f - ((point.y - yRange.start) / (yRange.endInclusive - yRange.start))
                    
                    // Vérifier si le label est dans la zone visible (avec une petite marge)
                    val labelX = leftAxisMargin + (xPosition * effectiveGraphWidth.value).dp - 10.dp
                    val labelY = topMargin + (yPosition * effectiveGraphHeight.value).dp - 30.dp
                    val isLabelVisible = labelX >= (-20).dp && 
                                        labelX <= maxWidth + 20.dp &&
                                        labelY >= (-20).dp && 
                                        labelY <= maxHeight + 20.dp
                    
                    if (!isLabelVisible) return@forEachIndexed

                    val numeroColor =
                            if (data.rationActual != null) { data.rationColor!! } else if (data.aliment.uuid == alimentSelectionne) {
                                Color(0xFF9C27B0) // Violet pour sélectionné
                            } else {
                                // Vérifier l'humidité pour les aliments non sélectionnés
                                val humidite = data.aliment.getNutrient(NutrientMain.HUMIDITE)
                                if (humidite == null || humidite < 20.0) {
                                    VetNutriColors.FoodAnalysisDry
                                } else {
                                    VetNutriColors.FoodAnalysisWet
                                }
                            }

                    Box(
                            modifier =
                                    Modifier.fillMaxSize()
                                            .wrapContentSize(Alignment.TopStart)
                                            .offset(
                                                    x =
                                                            leftAxisMargin +
                                                                    (xPosition *
                                                                                    effectiveGraphWidth
                                                                                            .value)
                                                                            .dp -
                                                                    10.dp, // Position dans la zone
                                                    // effective + centrage
                                                    y =
                                                            topMargin +
                                                                    (yPosition *
                                                                                    effectiveGraphHeight
                                                                                            .value)
                                                                            .dp -
                                                                    30.dp // Position dans la zone
                                                    // effective + au-dessus
                                                    // du point
                                                    ),
                            contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp)) {
                            drawCircle(color = Color.White, radius = 10.dp.toPx())
                            drawCircle(
                                    color = numeroColor,
                                    radius = 10.dp.toPx(),
                                    style = Stroke(width = 2.dp.toPx())
                            )
                        }

                        Text(
                                text = data.graphLabel,
                                style =
                                        MaterialTheme.typography.caption.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                        ),
                                color = numeroColor
                        )
                    }
                }
            }
        }
    } else {
        // 📊 HISTOGRAMME : Distribution du nutriment X
        val valeurs = histogramValues
        val categories = alimentsAnalyses.map { it.aliment.uuid }

        // Vérifier que nous avons des données valides
        if (valeurs.isEmpty() || valeurs.all { it == 0f }) {
            Text(
                    text = translate("graph.insufficientDataHistogram"),
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
            return
        }

        // Calculer la plage adaptative basée sur la distribution des données
        // Filtrer les valeurs invalides (NaN, Infinite) avant le calcul
        val valeursValides = valeurs.filter { it.isFinite() && !it.isNaN() }
        val yRange =
                if (valeursValides.isNotEmpty()) {
                    calculateAdaptiveRange(valeursValides, paddingPercent = 0.06f)
                } else {
                    // Valeurs par défaut si aucune valeur valide
                    0f..1f
                }

        XYGraph(
                xAxisModel = remember(categories) { CategoryAxisModel(categories) },
                // A previous axis tick may survive briefly while the filtered data is recomposed.
                xAxisLabels = { id -> alimentsAnalyses.firstOrNull { it.aliment.uuid == id }?.graphLabel.orEmpty() },
                yAxisModel = remember(yRange) { KoalaPlotExtensions.createSmartYAxisModel(range = yRange) },
                yAxisLabels = { GraphFormattingUtils.formatAxisTick(it, yRange) },
                yAxisTitle = "${xOption?.displayName} (${if (useDryMatterPer100g) "/100g MS" else "/1000 kcal"})",
                modifier = modifier
        ) {
            VerticalBarPlot(
                    xData = categories,
                    yData = valeurs,
                    bar = { index ->
                        val aliment = alimentsAnalyses[index]
                        val couleur =
                                if (aliment.rationActual != null) { aliment.rationColor!! } else if (aliment.aliment.uuid == alimentSelectionne) {
                                    Color(0xFF9C27B0) // Violet pour sélectionné
                                } else {
                                    // Vérifier l'humidité pour les aliments non sélectionnés
                                    val humidite =
                                            aliment.aliment.getNutrient(NutrientMain.HUMIDITE)
                                    if (humidite == null || humidite < 20.0) {
                                        VetNutriColors.FoodAnalysisDry
                                    } else {
                                        VetNutriColors.FoodAnalysisWet
                                    }
                                }
                        DefaultVerticalBar(SolidColor(couleur))
                    }
            )
        }
    }
}

/** Composant graphique pour afficher un histogramme de densité énergétique */
@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun HistogrammeEnergieAliments(
        alimentsAnalyses: List<AlimentAnalyseData>,
        alimentSelectionne: String? = null,
        useDryMatterPer100g: Boolean = false,
        modifier: Modifier = Modifier
) {
    if (alimentsAnalyses.isEmpty()) {
        Text(
                text = translate("graph.noDataHistogram"),
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        return
    }

    // Préparer les données pour l'histogramme
    // Créer les catégories avec seulement les numéros des aliments
    val categories = alimentsAnalyses.map { it.aliment.uuid }

    // Données de densité énergétique
    val densiteEnergetique = alimentsAnalyses.map { it.densiteEnergetique.toFloat() }

    // Vérifier que nous avons des données valides
    if (densiteEnergetique.isEmpty() || densiteEnergetique.all { it == 0f }) {
        Text(
                text = translate("auto.view.analysegraphiquealimentsview.donnees_insuffisantes_pour_l_histogramme_de_dens"),
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        return
    }

    // Plage pour l'axe Y basée sur les données
    // Calculer la plage adaptative basée sur la distribution des données
    // Filtrer les valeurs invalides (NaN, Infinite) avant le calcul
    val valeursValides = densiteEnergetique.filter { it.isFinite() && !it.isNaN() }
    val yRange =
            if (valeursValides.isNotEmpty()) {
                calculateAdaptiveRange(valeursValides, paddingPercent = 0.06f)
            } else {
                // Valeurs par défaut si aucune valeur valide
                0f..1f
            }

    // Créer le graphique
    XYGraph(
            xAxisModel = remember(categories) { CategoryAxisModel(categories) },
                // A previous axis tick may survive briefly while the filtered data is recomposed.
                xAxisLabels = { id -> alimentsAnalyses.firstOrNull { it.aliment.uuid == id }?.graphLabel.orEmpty() },
            yAxisModel = remember(yRange) { KoalaPlotExtensions.createSmartDensityAxisModel(range = yRange) },
            yAxisLabels = { GraphFormattingUtils.formatAxisTick(it, yRange) },
            yAxisTitle = if (useDryMatterPer100g) 
                    "Densité énergétique (kcal/100g MS)"
                else  
                    "Densité énergétique (kcal/100g)",
            modifier = modifier
    ) {
        VerticalBarPlot(
                xData = categories,
                yData = densiteEnergetique,
                bar = { index ->
                    // Couleur de la barre selon si l'aliment est sélectionné
                    val aliment = alimentsAnalyses[index]
                    val couleur =
                            if (aliment.rationActual != null) { aliment.rationColor!! } else if (aliment.aliment.uuid == alimentSelectionne) {
                                Color(0xFF9C27B0) // Violet pour l'aliment sélectionné
                            } else {
                                // Vérifier l'humidité pour les aliments non sélectionnés
                                val humidite = aliment.aliment.getNutrient(NutrientMain.HUMIDITE)
                                if (humidite == null || humidite < 20.0) {
                                    VetNutriColors.FoodAnalysisDry
                                } else {
                                    VetNutriColors.FoodAnalysisWet
                                }
                            }
                    DefaultVerticalBar(SolidColor(couleur))
                }
        )
    }
}
