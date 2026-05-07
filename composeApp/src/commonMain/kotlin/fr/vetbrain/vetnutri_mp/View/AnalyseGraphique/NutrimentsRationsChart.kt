package fr.vetbrain.vetnutri_mp.View.AnalyseGraphique

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Utils.GraphFormattingUtils
import fr.vetbrain.vetnutri_mp.Utils.KoalaPlotExtensions
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import io.github.koalaplot.core.*
import io.github.koalaplot.core.bar.DefaultVerticalBar
import io.github.koalaplot.core.bar.VerticalBarPlot
import io.github.koalaplot.core.line.LinePlot
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.*

/** Graphique personnalisé pour l'analyse des nutriments des rations */
@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun NutrimentsRationsChart(
        viewModel: AnimalDetailViewModel,
        equationRepository: EquationRepository? = null
) {
        val animal by viewModel.animal.collectAsState()
        val referenceUtilisee by viewModel.referenceUtilisee.collectAsState()
        val speciesPreferences by viewModel.speciesPreferences.collectAsState()

        // États pour les données des rations
        var rationsNutrimentData by remember {
                mutableStateOf<List<RationNutrimentData>>(emptyList())
        }
        var isLoading by remember { mutableStateOf(true) }
        var rationSelectionnee by remember { mutableStateOf<String?>(null) }
        var nutrimentX by remember { mutableStateOf<String?>("proteine") }
        var nutrimentY by remember {
                mutableStateOf<String?>("")
        } // "" = histogramme, autre = scatter plot

        // Calculer les données des rations de manière asynchrone
        LaunchedEffect(animal?.consultations?.size, referenceUtilisee, speciesPreferences) {
                isLoading = true
                val resultat = mutableListOf<RationNutrimentData>()

                // Identifier les rations actuelles
                val rationsActuellesIds =
                        animal?.consultations
                                ?.flatMap { it.rations }
                                ?.filter { it.actual }
                                ?.map { it.uuid }
                                ?.toSet()
                                ?: emptySet()

                animal?.consultations?.forEachIndexed { consultationIndex, consultation ->
                        consultation.rations.forEachIndexed { rationIndex, ration ->
                                try {
                                        val rationData =
                                                calculerNutrimentsRation(
                                                        ration = ration,
                                                        referenceEv = referenceUtilisee,
                                                        preferencesEspece = speciesPreferences,
                                                        equationRepository = equationRepository,
                                                        isRationActuelle =
                                                                ration.uuid in rationsActuellesIds
                                                )

                                        rationData?.let { data ->
                                                val dataWithDate =
                                                        data.copy(
                                                                consultationDate =
                                                                        consultation.date,
                                                                numero =
                                                                        consultationIndex * 100 +
                                                                                rationIndex +
                                                                                1
                                                        )
                                                resultat.add(dataWithDate)
                                        }
                                } catch (e: Exception) {
                                        e.printStackTrace()
                                }
                        }
                }

                rationsNutrimentData = resultat
                isLoading = false
        }

        // Vérifier si une consultation et une référence sont disponibles
        val hasConsultations = animal?.consultations?.isNotEmpty() == true
        val hasReference = referenceUtilisee != null
        val hasPreferences = speciesPreferences != null

        if (!hasConsultations) {
                // Aucune consultation disponible
                Box(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                ) {
                        Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                Text(
                                        text = translate(LocalizationKeys.Graph.NO_CONSULTATION),
                                        style = MaterialTheme.typography.body1,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Error
                                )
                                Text(
                                        text = translate(LocalizationKeys.Graph.CREATE_CONSULTATION_HINT),
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                        }
                }
        } else if (!hasReference) {
                // Aucune référence disponible
                Box(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                ) {
                        Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                Text(
                                        text = translate(LocalizationKeys.Graph.NO_REFERENCE),
                                        style = MaterialTheme.typography.body1,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Error
                                )
                                Text(
                                        text = translate(LocalizationKeys.Graph.SELECT_REFERENCE_HINT),
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                        }
                }
        } else if (!hasPreferences) {
                // Aucune préférence disponible
                Box(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                ) {
                        Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                Text(
                                        text = translate(LocalizationKeys.Graph.NO_PREFERENCE),
                                        style = MaterialTheme.typography.body1,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Error
                                )
                                Text(
                                        text = translate(LocalizationKeys.Graph.CONFIG_PREFERENCE_HINT),
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                        }
                }
        } else if (isLoading) {
                Box(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                ) {
                        Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                CircularProgressIndicator(color = VetNutriColors.Primary)
                                Text(
                                        text = translate(LocalizationKeys.Graph.CALCULATING),
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                        }
                }
        } else if (rationsNutrimentData.isEmpty()) {
                Box(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                ) {
                        Text(
                                text = translate(LocalizationKeys.Graph.NO_RATION),
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                }
        } else {
                // Graphique personnalisé des nutriments des rations
                GraphCard(
                        titre = translate(LocalizationKeys.Graph.NUTRIMENTS_ANALYSIS_TITLE),
                        sousTitre =
                                translate(LocalizationKeys.Graph.NUTRIMENTS_ANALYSIS_SUBTITLE)
                ) {
                        // Récupérer les informations des nutriments sélectionnés
                        val xOption = VIEW_NUTRIMENT_OPTIONS.find { it.key == nutrimentX }
                        val yOption = VIEW_NUTRIMENT_OPTIONS.find { it.key == nutrimentY }

                        // Titre dynamique selon le type de graphique
                        val titre =
                                if (nutrimentY.isNullOrEmpty()) {
                                        "${translate(LocalizationKeys.Graph.DISTRIBUTION_OF)} ${xOption?.displayName ?: "Nutriment"}"
                                } else {
                                        "${xOption?.displayName ?: "X"} vs ${yOption?.displayName ?: "Y"}"
                                }

                        Text(
                                text = titre,
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold,
                                color = VetNutriColors.Primary
                        )

                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

                        if (nutrimentX.isNullOrEmpty()) {
                                Text(
                                        text =
                                                translate(LocalizationKeys.Graph.SELECT_NUTRIMENTS_X_HINT),
                                        style = MaterialTheme.typography.body1,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                        } else if (nutrimentY.isNullOrEmpty()) {
                                // 📊 HISTOGRAMME : Distribution du nutriment X
                                // Copier dans une variable locale pour permettre le smart cast
                                val nutrimentXLocal: String = nutrimentX!!
                                val valeurs =
                                        rationsNutrimentData.map {
                                                it.getNutrimentValue(nutrimentXLocal).toFloat()
                                        }
                                val categories = rationsNutrimentData.map { "${it.numero}" }

                                // Vérifier que nous avons des données valides
                                if (valeurs.isEmpty() || valeurs.all { it == 0f }) {
                                        Text(
                                                text = translate(LocalizationKeys.Graph.INSUFFICIENT_DATA_HISTOGRAM),
                                                style = MaterialTheme.typography.body1,
                                                color =
                                                        MaterialTheme.colors.onSurface.copy(
                                                                alpha = 0.7f
                                                        )
                                        )
                                } else {
                                        // Calculer la plage adaptative
                                        val valeursValides =
                                                valeurs.filter { it.isFinite() && !it.isNaN() }
                                        val yRange =
                                                if (valeursValides.isNotEmpty()) {
                                                        val range = calculateAdaptiveRange(
                                                                valeursValides,
                                                                paddingPercent = 0.06f
                                                        )
                                                        arrondirPlage(range)
                                                } else {
                                                        0f..1f
                                                }

                                        XYGraph(
                                                xAxisModel =
                                                        remember(categories) {
                                                                CategoryAxisModel(categories)
                                                        },
                                                yAxisModel =
                                                        remember(yRange) {
                                                                KoalaPlotExtensions
                                                                        .createSmartYAxisModel(
                                                                                yRange
                                                                        )
                                                        },
                                                yAxisTitle =
                                                        "${xOption?.displayName} (${xOption?.unit})",
                                                modifier = Modifier.height(400.dp)
                                        ) {
                                                VerticalBarPlot(
                                                        xData = categories,
                                                        yData = valeurs,
                                                        bar = { index ->
                                                                val ration =
                                                                        rationsNutrimentData[index]
                                                                val couleur =
                                                                        if (ration.rationId ==
                                                                                        rationSelectionnee
                                                                        ) {
                                                                                Color(
                                                                                        0xFF9C27B0
                                                                                ) // Violet pour
                                                                                // sélectionné
                                                                        } else if (ration.isRationActuelle
                                                                        ) {
                                                                                Color(
                                                                                        0xFFFF9800
                                                                                ) // Orange pour
                                                                                // rations
                                                                                // actuelles
                                                                        } else {
                                                                                VetNutriColors
                                                                                        .Primary // Couleur normale
                                                                        }
                                                                DefaultVerticalBar(
                                                                        SolidColor(couleur)
                                                                )
                                                        }
                                                )
                                        }

                                        // Numéros superposés sur les barres
                                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                                rationsNutrimentData.forEachIndexed { index, ration
                                                        ->
                                                        val barWidth =
                                                                maxWidth / rationsNutrimentData.size
                                                        val xPosition =
                                                                (index.toFloat() + 0.5f) *
                                                                        (maxWidth.value /
                                                                                rationsNutrimentData
                                                                                        .size)

                                                        Box(
                                                                modifier =
                                                                        Modifier.fillMaxSize()
                                                                                .wrapContentSize(
                                                                                        Alignment
                                                                                                .TopStart
                                                                                )
                                                                                .offset(
                                                                                        x =
                                                                                                (xPosition -
                                                                                                                10)
                                                                                                        .dp,
                                                                                        y = 20.dp
                                                                                ),
                                                                contentAlignment = Alignment.Center
                                                        ) {
                                                                // Fond du numéro
                                                                androidx.compose.foundation.Canvas(
                                                                        modifier =
                                                                                Modifier.size(20.dp)
                                                                ) {
                                                                        drawCircle(
                                                                                color = Color.White,
                                                                                radius =
                                                                                        10.dp.toPx()
                                                                        )
                                                                        drawCircle(
                                                                                color =
                                                                                        if (ration.rationId ==
                                                                                                        rationSelectionnee
                                                                                        ) {
                                                                                                Color(
                                                                                                        0xFF9C27B0
                                                                                                )
                                                                                        } else if (ration.isRationActuelle
                                                                                        ) {
                                                                                                Color(
                                                                                                        0xFFFF9800
                                                                                                )
                                                                                        } else {
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                        },
                                                                                radius =
                                                                                        10.dp.toPx(),
                                                                                style =
                                                                                        Stroke(
                                                                                                width =
                                                                                                        2.dp.toPx()
                                                                                        )
                                                                        )
                                                                }

                                                                // Numéro
                                                                Text(
                                                                        text = "${ration.numero}",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .caption
                                                                                        .copy(
                                                                                                fontWeight =
                                                                                                        FontWeight
                                                                                                                .Bold,
                                                                                                fontSize =
                                                                                                        12.sp
                                                                                        ),
                                                                        color =
                                                                                if (ration.rationId ==
                                                                                                rationSelectionnee
                                                                                ) {
                                                                                        Color(
                                                                                                0xFF9C27B0
                                                                                        )
                                                                                } else if (ration.isRationActuelle
                                                                                ) {
                                                                                        Color(
                                                                                                0xFFFF9800
                                                                                        )
                                                                                } else {
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                                }
                                                                )
                                                        }
                                                }
                                        }
                                }
                        } else {
                                // 📈 SCATTER PLOT : X vs Y
                                // Copier dans des variables locales pour permettre le smart cast
                                val nutrimentXLocal: String = nutrimentX!!
                                val nutrimentYLocal: String = nutrimentY!!
                                val points =
                                        rationsNutrimentData.map { data ->
                                                Point(
                                                        x =
                                                                data.getNutrimentValue(
                                                                                nutrimentXLocal
                                                                        )
                                                                        .toFloat(),
                                                        y =
                                                                data.getNutrimentValue(
                                                                                nutrimentYLocal
                                                                        )
                                                                        .toFloat()
                                                )
                                        }

                                // Vérifier que nous avons des données valides
                                if (points.isEmpty() || points.all { it.x == 0f && it.y == 0f }) {
                                        Text(
                                                text =
                                                        translate(LocalizationKeys.Graph.INSUFFICIENT_DATA_SCATTER),
                                                style = MaterialTheme.typography.body1,
                                                color =
                                                        MaterialTheme.colors.onSurface.copy(
                                                                alpha = 0.7f
                                                        )
                                        )
                                } else {
                                        // Calculer les plages
                                        val minX = points.minOf { it.x }.coerceAtLeast(0f)
                                        val maxX = points.maxOf { it.x }
                                        val minY = points.minOf { it.y }.coerceAtLeast(0f)
                                        val maxY = points.maxOf { it.y }

                                        val baseXRange = arrondirPlage((minX - minX * 0.05f)..(maxX + maxX * 0.05f))
                                        val baseYRange = arrondirPlage((minY - minY * 0.05f)..(maxY + maxY * 0.05f))
                                        
                                        // État du zoom/pan
                                        var zoomPanState by remember { mutableStateOf(ZoomPanStateView()) }
                                        val originalRanges = remember(baseXRange, baseYRange) {
                                                Pair(baseXRange, baseYRange)
                                        }
                                        
                                        // Réinitialiser le zoom quand les nutriments changent
                                        LaunchedEffect(nutrimentX, nutrimentY) {
                                                zoomPanState = ZoomPanStateView()
                                        }
                                        
                                        // Calculer les plages zoomées
                                        val xRange = calculateZoomedRangeView(originalRanges.first, zoomPanState, isXAxis = true)
                                        val yRange = calculateZoomedRangeView(originalRanges.second, zoomPanState, isXAxis = false)
                                        
                                        // Boutons de zoom (pour desktop où le pinch ne fonctionne pas)
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                IconButtonWithTooltip(
                                                        onClick = {
                                                                // Zoom out
                                                                val newScaleX = (zoomPanState.scaleX * 0.9f).coerceIn(0.5f, 5f)
                                                                val newScaleY = (zoomPanState.scaleY * 0.9f).coerceIn(0.5f, 5f)
                                                                zoomPanState = ZoomPanStateView(
                                                                        scaleX = newScaleX,
                                                                        scaleY = newScaleY,
                                                                        panX = zoomPanState.panX,
                                                                        panY = zoomPanState.panY
                                                                )
                                                        },
                                                        imageVector = Icons.Default.ZoomOut,
                                                        contentDescription = translate(LocalizationKeys.Graph.ZOOM_OUT_TOOLTIP),
                                                        tooltip = translate(LocalizationKeys.Graph.ZOOM_OUT_TOOLTIP)
                                                )
                                                IconButtonWithTooltip(
                                                        onClick = {
                                                                // Zoom in
                                                                val newScaleX = (zoomPanState.scaleX * 1.1f).coerceIn(0.5f, 5f)
                                                                val newScaleY = (zoomPanState.scaleY * 1.1f).coerceIn(0.5f, 5f)
                                                                zoomPanState = ZoomPanStateView(
                                                                        scaleX = newScaleX,
                                                                        scaleY = newScaleY,
                                                                        panX = zoomPanState.panX,
                                                                        panY = zoomPanState.panY
                                                                )
                                                        },
                                                        imageVector = Icons.Default.ZoomIn,
                                                        contentDescription = translate(LocalizationKeys.Graph.ZOOM_IN_TOOLTIP),
                                                        tooltip = translate(LocalizationKeys.Graph.ZOOM_IN_TOOLTIP)
                                                )
                                                if (zoomPanState.scaleX != 1f || zoomPanState.scaleY != 1f || 
                                                    zoomPanState.panX != 0f || zoomPanState.panY != 0f) {
                                                        TextButton(
                                                                onClick = { zoomPanState = ZoomPanStateView() }
                                                        ) {
                                                                Text(translate(LocalizationKeys.Graph.RESET_ZOOM), fontSize = 12.sp)
                                                        }
                                                }
                                        }

                                        XYGraph(
                                                xAxisModel =
                                                        KoalaPlotExtensions.createSmartXAxisModel(
                                                                xRange
                                                        ),
                                                yAxisModel =
                                                        KoalaPlotExtensions.createSmartYAxisModel(
                                                                yRange
                                                        ),
                                                xAxisTitle =
                                                        "${xOption?.displayName} (${xOption?.unit})",
                                                yAxisTitle =
                                                        "${yOption?.displayName} (${yOption?.unit})",
                                                modifier = Modifier
                                                        .height(400.dp)
                                                        .clipToBounds()
                                                        .pointerInput(Unit) {
                                                                detectTransformGestures { _, pan, zoom, _ ->
                                                                        // Limiter le zoom entre 0.5x et 5x
                                                                        val newScaleX = (zoomPanState.scaleX * zoom).coerceIn(0.5f, 5f)
                                                                        val newScaleY = (zoomPanState.scaleY * zoom).coerceIn(0.5f, 5f)
                                                                        
                                                                        // Calculer les plages actuelles (zoomées) pour le pan
                                                                        val currentXRange = calculateZoomedRangeView(originalRanges.first, zoomPanState, isXAxis = true)
                                                                        val currentYRange = calculateZoomedRangeView(originalRanges.second, zoomPanState, isXAxis = false)
                                                                        
                                                                        // Convertir le pan en coordonnées de données (basé sur la plage actuelle)
                                                                        val panXDelta = pan.x / size.width * (currentXRange.endInclusive - currentXRange.start)
                                                                        val panYDelta = -pan.y / size.height * (currentYRange.endInclusive - currentYRange.start)
                                                                        
                                                                        zoomPanState = ZoomPanStateView(
                                                                                scaleX = newScaleX,
                                                                                scaleY = newScaleY,
                                                                                panX = zoomPanState.panX + panXDelta,
                                                                                panY = zoomPanState.panY + panYDelta
                                                                        )
                                                                }
                                                        }
                                        ) {
                                                rationsNutrimentData.forEachIndexed { index, data ->
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
                                                                                if (data.rationId ==
                                                                                                rationSelectionnee
                                                                                ) {
                                                                                        Color(
                                                                                                0xFF9C27B0
                                                                                        ) // Violet
                                                                                        // pour
                                                                                        // sélectionné
                                                                                } else if (data.isRationActuelle
                                                                                ) {
                                                                                        Color(
                                                                                                0xFFFF9800
                                                                                        ) // Orange
                                                                                        // pour
                                                                                        // rations
                                                                                        // actuelles
                                                                                } else {
                                                                                        VetNutriColors
                                                                                                .Primary // Couleur normale
                                                                                }

                                                                        androidx.compose.foundation
                                                                                .Canvas(
                                                                                        modifier =
                                                                                                Modifier.size(
                                                                                                        12.dp
                                                                                                )
                                                                                ) {
                                                                                        drawCircle(
                                                                                                color =
                                                                                                        couleurPoint,
                                                                                                radius =
                                                                                                        6f,
                                                                                                center =
                                                                                                        center
                                                                                        )
                                                                                }
                                                                }
                                                        )
                                                }
                                        }

                                        // Numéros superposés
                                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                                rationsNutrimentData.forEachIndexed { index, data ->
                                                        val point = points[index]
                                                        val xPosition =
                                                                ((point.x - xRange.start) /
                                                                        (xRange.endInclusive -
                                                                                xRange.start))
                                                        val yPosition =
                                                                1f -
                                                                        ((point.y - yRange.start) /
                                                                                (yRange.endInclusive -
                                                                                        yRange.start))

                                                        // Marges typiques des axes KoalaPlot
                                                        val leftAxisMargin = 10.dp
                                                        val bottomAxisMargin = 15.dp
                                                        val topMargin = 10.dp
                                                        val rightMargin = 20.dp

                                                        // Zone de graphique effective
                                                        val effectiveGraphWidth =
                                                                maxWidth -
                                                                        leftAxisMargin -
                                                                        rightMargin
                                                        val effectiveGraphHeight =
                                                                maxHeight -
                                                                        bottomAxisMargin -
                                                                        topMargin

                                                        val numeroColor =
                                                                if (data.rationId ==
                                                                                rationSelectionnee
                                                                ) {
                                                                        Color(
                                                                                0xFF9C27B0
                                                                        ) // Violet pour sélectionné
                                                                } else if (data.isRationActuelle) {
                                                                        Color(
                                                                                0xFFFF9800
                                                                        ) // Orange pour rations
                                                                        // actuelles
                                                                } else {
                                                                        VetNutriColors
                                                                                .Primary // Couleur
                                                                        // par
                                                                        // défaut
                                                                }

                                                        // Vérifier si le label est visible
                                                        val labelX = leftAxisMargin + (xPosition * effectiveGraphWidth.value).dp - 10.dp
                                                        val labelY = topMargin + (yPosition * effectiveGraphHeight.value).dp - 30.dp
                                                        
                                                        val isLabelVisible = labelX >= (-20).dp && 
                                                                        labelX <= maxWidth + 20.dp &&
                                                                        labelY >= (-20).dp && 
                                                                        labelY <= maxHeight + 20.dp
                                                        
                                                        if (!isLabelVisible) return@forEachIndexed

                                                        Box(
                                                                modifier =
                                                                        Modifier.fillMaxSize()
                                                                                .wrapContentSize(
                                                                                        Alignment
                                                                                                .TopStart
                                                                                )
                                                                                .offset(
                                                                                        x = labelX,
                                                                                        y = labelY
                                                                                ),
                                                                contentAlignment = Alignment.Center
                                                        ) {
                                                                androidx.compose.foundation.Canvas(
                                                                        modifier =
                                                                                Modifier.size(20.dp)
                                                                ) {
                                                                        drawCircle(
                                                                                color = Color.White,
                                                                                radius =
                                                                                        10.dp.toPx()
                                                                        )
                                                                        drawCircle(
                                                                                color = numeroColor,
                                                                                radius =
                                                                                        10.dp.toPx(),
                                                                                style =
                                                                                        Stroke(
                                                                                                width =
                                                                                                        2.dp.toPx()
                                                                                        )
                                                                        )
                                                                }

                                                                Text(
                                                                        text = "${data.numero}",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .caption
                                                                                        .copy(
                                                                                                fontWeight =
                                                                                                        FontWeight
                                                                                                                .Bold,
                                                                                                fontSize =
                                                                                                        12.sp
                                                                                        ),
                                                                        color = numeroColor
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }
                }

                // Sélecteurs de nutriments
                Spacer(modifier = Modifier.height(AppSizes.paddingMedium))
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                        NutrimentSelector(
                                label = translate(LocalizationKeys.Graph.AXIS_X_LABEL),
                                selectedNutriment = nutrimentX,
                                onNutrimentSelected = { nutrimentX = it },
                                modifier = Modifier.weight(1f)
                        )
                        NutrimentSelector(
                                label = translate(LocalizationKeys.Graph.AXIS_Y_LABEL),
                                selectedNutriment = nutrimentY,
                                onNutrimentSelected = { nutrimentY = it },
                                modifier = Modifier.weight(1f)
                        )
                }

                // Légende des rations
                Spacer(modifier = Modifier.height(AppSizes.paddingMedium))
                Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                        Text(
                                text = translate(LocalizationKeys.Graph.LEGEND_RATIONS),
                                style = MaterialTheme.typography.caption,
                                fontWeight = FontWeight.Bold
                        )
                        rationsNutrimentData.forEach { data ->
                                Row(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .clickable {
                                                                rationSelectionnee =
                                                                        if (rationSelectionnee ==
                                                                                        data.rationId
                                                                        )
                                                                                null
                                                                        else data.rationId
                                                        }
                                                        .background(
                                                                when {
                                                                        rationSelectionnee ==
                                                                                data.rationId ->
                                                                                Color(0xFF9C27B0)
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.1f
                                                                                        )
                                                                        data.isRationActuelle ->
                                                                                Color(0xFFFF9800)
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.1f
                                                                                        )
                                                                        else -> Color.Transparent
                                                                }
                                                        )
                                                        .padding(AppSizes.paddingSmall),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall)
                                        ) {
                                                androidx.compose.foundation.Canvas(
                                                        modifier = Modifier.size(8.dp)
                                                ) {
                                                        drawCircle(
                                                                color =
                                                                        when {
                                                                                data.rationId ==
                                                                                        rationSelectionnee ->
                                                                                        Color(
                                                                                                0xFF9C27B0
                                                                                        )
                                                                                data.isRationActuelle ->
                                                                                        Color(
                                                                                                0xFFFF9800
                                                                                        )
                                                                                else ->
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                        },
                                                                radius = 4f,
                                                                center = center
                                                        )
                                                }
                                                Column {
                                                        Text(
                                                                text =
                                                                        "${data.numero}. ${data.rationName}",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption,
                                                                fontWeight = FontWeight.Medium
                                                        )
                                                        Text(
                                                                text =
                                                                        "Protéines: ${GraphFormattingUtils.formatDecimal(data.proteines, 1)}g | Lipides: ${GraphFormattingUtils.formatDecimal(data.lipides, 1)}g",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption,
                                                                color =
                                                                        MaterialTheme.colors
                                                                                .onSurface.copy(
                                                                                alpha = 0.6f
                                                                        ),
                                                                fontSize = 10.sp
                                                        )
                                                }
                                        }
                                        Text(
                                                text =
                                                        "${data.consultationDate?.toString() ?: "Date inconnue"}",
                                                style = MaterialTheme.typography.caption,
                                                color =
                                                        MaterialTheme.colors.onSurface.copy(
                                                                alpha = 0.7f
                                                        )
                                        )
                                }
                        }
                }
        }
}

