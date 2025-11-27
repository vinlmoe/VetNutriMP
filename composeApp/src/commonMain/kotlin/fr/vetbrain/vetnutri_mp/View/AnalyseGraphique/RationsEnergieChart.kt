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
import fr.vetbrain.vetnutri_mp.Utils.GraphFormattingUtils
import fr.vetbrain.vetnutri_mp.Utils.KoalaPlotExtensions
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import io.github.koalaplot.core.*
import io.github.koalaplot.core.line.LinePlot
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.*

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun RationsEnergieChart(
        viewModel: AnimalDetailViewModel,
        equationRepository: EquationRepository? = null
) {
        val animal by viewModel.animal.collectAsState()
        val referenceUtilisee by viewModel.referenceUtilisee.collectAsState()
        val speciesPreferences by viewModel.speciesPreferences.collectAsState()
        val scope = rememberCoroutineScope()

        // États pour les données des rations
        var rationsEnergieData by remember { mutableStateOf<List<RationEnergyData>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var rationSelectionnee by remember { mutableStateOf<String?>(null) }

        // Calculer les données des rations de manière asynchrone
        LaunchedEffect(animal?.consultations?.size, referenceUtilisee, speciesPreferences) {
                isLoading = true
                val resultat = mutableListOf<RationEnergyData>()

                animal?.consultations?.forEachIndexed { consultationIndex, consultation ->
                        consultation.rations.forEachIndexed { rationIndex, ration ->
                                try {
                                        // 🔍 LOG DIAGNOSTIC : Vérifier les données de la ration

                                        val rationData =
                                                calculerPourcentagesEnergieRation(
                                                        ration = ration,
                                                        referenceEv = referenceUtilisee,
                                                        preferencesEspece = speciesPreferences,
                                                        equationRepository = equationRepository
                                                )

                                        rationData?.let { data ->
                                                // Ajouter la date de consultation et un numéro
                                                // unique
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
                                                ?: run {}
                                } catch (e: Exception) {

                                        e.printStackTrace()
                                }
                        }
                }

                rationsEnergieData = resultat
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
                                        text = "Aucune consultation disponible",
                                        style = MaterialTheme.typography.body1,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Error
                                )
                                Text(
                                        text = "Veuillez créer une consultation pour analyser les rations",
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
                                        text = "Aucune référence sélectionnée",
                                        style = MaterialTheme.typography.body1,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Error
                                )
                                Text(
                                        text = "Veuillez sélectionner une référence dans une consultation pour calculer l'énergie avec les équations du référentiel",
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
                                        text = "Aucune préférence disponible",
                                        style = MaterialTheme.typography.body1,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Error
                                )
                                Text(
                                        text = "Veuillez configurer les préférences pour l'espèce dans les paramètres",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                        }
                }
        } else if (isLoading) {
                // Indicateur de chargement
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
                                        text = "Calcul des données énergétiques des rations...",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                        }
                }
        } else if (rationsEnergieData.isEmpty()) {
                // Aucune donnée disponible
                Box(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                ) {
                        Text(
                                text = "Aucune ration disponible pour l'analyse",
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                }
        } else {
                // Identifier les rations actuelles basées sur la propriété 'actual' des rations
                val rationsActuellesIds =
                        rationsEnergieData
                                .filter { data ->
                                        // Trouver la ration originale pour vérifier sa propriété
                                        // 'actual'
                                        animal?.consultations
                                                ?.flatMap { it.rations }
                                                ?.find { it.uuid == data.rationId }
                                                ?.actual == true
                                }
                                .map { it.rationId }
                                .toSet()

                // Graphique des rations
                GraphCard(
                        titre = "Répartition énergétique des rations",
                        sousTitre = "Pourcentages d'énergie des protéines vs lipides par ration"
                ) {
                        // Préparer les données pour le graphique
                        val points =
                                rationsEnergieData.map { data ->
                                        Point(
                                                x = data.proteineEnergyPercentage.toFloat(),
                                                y = data.lipideEnergyPercentage.toFloat()
                                        )
                                }

                        // Calculer les plages des axes
                        val minX = points.minOf { it.x }.coerceAtLeast(0f)
                        val maxX = points.maxOf { it.x }.coerceAtMost(100f)
                        val minY = points.minOf { it.y }.coerceAtLeast(0f)
                        val maxY = points.maxOf { it.y }.coerceAtMost(100f)

                        val baseXRange = arrondirPlage((minX - minX * 0.05f)..(maxX + maxX * 0.05f))
                        val baseYRange = arrondirPlage((minY - minY * 0.05f)..(maxY + maxY * 0.05f))
                        
                        // État du zoom/pan
                        var zoomPanState by remember { mutableStateOf(ZoomPanStateView()) }
                        val originalRanges = remember(baseXRange, baseYRange) {
                                Pair(baseXRange, baseYRange)
                        }
                        
                        // Réinitialiser le zoom quand les données changent
                        LaunchedEffect(rationsEnergieData.size) {
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
                                        contentDescription = "Zoom arrière",
                                        tooltip = "Zoom arrière"
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
                                        contentDescription = "Zoom avant",
                                        tooltip = "Zoom avant"
                                )
                                if (zoomPanState.scaleX != 1f || zoomPanState.scaleY != 1f || 
                                    zoomPanState.panX != 0f || zoomPanState.panY != 0f) {
                                        TextButton(
                                                onClick = { zoomPanState = ZoomPanStateView() }
                                        ) {
                                                Text("Réinitialiser", fontSize = 12.sp)
                                        }
                                }
                        }

                        // Graphique avec numéros superposés
                        BoxWithConstraints(modifier = Modifier.height(400.dp).clipToBounds()) {
                                // Graphique principal
                                XYGraph(
                                        xAxisModel =
                                                KoalaPlotExtensions.createSmartPercentageAxisModel(
                                                        xRange
                                                ),
                                        yAxisModel =
                                                KoalaPlotExtensions.createSmartPercentageAxisModel(
                                                        yRange
                                                ),
                                        xAxisTitle = "Énergie des protéines (%)",
                                        yAxisTitle = "Énergie des lipides (%)",
                                        modifier = Modifier
                                                .fillMaxSize()
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
                                        // 🔸 LIGNES DE RÉFÉRENCE pour la répartition énergétique
                                        // Ligne 80-x : Protéines + Lipides = 80% (ENA = 20%)
                                        val ligne80MinusX =
                                                listOf(
                                                        Point(
                                                                x = xRange.start,
                                                                y = 80f - xRange.start
                                                        ),
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

                                        // Ligne 60-x : Protéines + Lipides = 60% (ENA = 40%)
                                        val ligne60MinusX =
                                                listOf(
                                                        Point(
                                                                x = xRange.start,
                                                                y = 60f - xRange.start
                                                        ),
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

                                        // Ligne 40-x : Protéines + Lipides = 40% (ENA = 60%)
                                        val ligne40MinusX =
                                                listOf(
                                                        Point(
                                                                x = xRange.start,
                                                                y = 40f - xRange.start
                                                        ),
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

                                        // Afficher chaque point individuellement (uniquement ceux dans la plage visible)
                                        rationsEnergieData.forEachIndexed { index, data ->
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
                                                                // Point principal avec couleur
                                                                // selon sélection
                                                                val couleurPoint =
                                                                        if (data.rationId ==
                                                                                        rationSelectionnee
                                                                        ) {
                                                                                Color(
                                                                                        0xFF9C27B0
                                                                                ) // Violet
                                                                        } else {
                                                                                VetNutriColors
                                                                                        .Primary
                                                                        }

                                                                androidx.compose.foundation.Canvas(
                                                                        modifier =
                                                                                Modifier.size(12.dp)
                                                                ) {
                                                                        drawCircle(
                                                                                color =
                                                                                        couleurPoint,
                                                                                radius = 6f,
                                                                                center = center
                                                                        )
                                                                }
                                                        }
                                                )
                                        }
                                }

                                // Numéros superposés (uniquement ceux dans la plage visible)
                                val leftAxisMargin = 10.dp
                                val bottomAxisMargin = 15.dp
                                val topMargin = 10.dp
                                val rightMargin = 20.dp
                                
                                // Zone de graphique effective
                                val effectiveGraphWidth = maxWidth - leftAxisMargin - rightMargin
                                val effectiveGraphHeight = maxHeight - bottomAxisMargin - topMargin
                                
                                rationsEnergieData.forEachIndexed { index, data ->
                                        val point = points[index]
                                        
                                        // Vérifier si le point est dans la plage visible
                                        val isPointVisible = point.x >= xRange.start && 
                                                                point.x <= xRange.endInclusive &&
                                                                point.y >= yRange.start && 
                                                                point.y <= yRange.endInclusive
                                        
                                        if (!isPointVisible) return@forEachIndexed
                                        
                                        // Calculer la position du numéro
                                        val xPosition =
                                                ((point.x - xRange.start) /
                                                        (xRange.endInclusive - xRange.start))
                                        val yPosition =
                                                1f -
                                                        ((point.y - yRange.start) /
                                                                (yRange.endInclusive -
                                                                        yRange.start))

                                        // Couleur selon la sélection
                                        val numeroColor =
                                                if (data.rationId == rationSelectionnee) {
                                                        Color(0xFF9C27B0) // Violet pour sélectionné
                                                } else {
                                                        VetNutriColors.Primary // Couleur par défaut
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
                                                                .wrapContentSize(Alignment.TopStart)
                                                                .offset(
                                                                        x = labelX,
                                                                        y = labelY
                                                                ),
                                                contentAlignment = Alignment.Center
                                        ) {
                                                // Fond du numéro
                                                androidx.compose.foundation.Canvas(
                                                        modifier = Modifier.size(20.dp)
                                                ) {
                                                        drawCircle(
                                                                color = Color.White,
                                                                radius = 10.dp.toPx()
                                                        )
                                                        drawCircle(
                                                                color = numeroColor,
                                                                radius = 10.dp.toPx(),
                                                                style = Stroke(width = 2.dp.toPx())
                                                        )
                                                }

                                                // Numéro
                                                Text(
                                                        text = "${data.numero}",
                                                        style =
                                                                MaterialTheme.typography.caption
                                                                        .copy(
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Bold,
                                                                                fontSize = 12.sp
                                                                        ),
                                                        color = numeroColor
                                                )
                                        }
                                }
                        }

                        // Légende des lignes de référence
                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                // Ligne magenta 80-x
                                androidx.compose.foundation.Canvas(
                                        modifier = Modifier.size(20.dp, 2.dp)
                                ) {
                                        drawLine(
                                                color = Color.Magenta.copy(alpha = 0.7f),
                                                start =
                                                        androidx.compose.ui.geometry.Offset(
                                                                0f,
                                                                size.height / 2
                                                        ),
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
                                        text = "80-x: 20% ENA",
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Magenta.copy(alpha = 0.7f)
                                )

                                Spacer(modifier = Modifier.width(AppSizes.paddingMedium))

                                // Ligne cyan 60-x
                                androidx.compose.foundation.Canvas(
                                        modifier = Modifier.size(20.dp, 2.dp)
                                ) {
                                        drawLine(
                                                color = Color.Cyan.copy(alpha = 0.7f),
                                                start =
                                                        androidx.compose.ui.geometry.Offset(
                                                                0f,
                                                                size.height / 2
                                                        ),
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
                                        text = "60-x: 40% ENA",
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Cyan.copy(alpha = 0.7f)
                                )

                                Spacer(modifier = Modifier.width(AppSizes.paddingMedium))

                                // Ligne jaune 40-x
                                androidx.compose.foundation.Canvas(
                                        modifier = Modifier.size(20.dp, 2.dp)
                                ) {
                                        drawLine(
                                                color = Color.Yellow.copy(alpha = 0.7f),
                                                start =
                                                        androidx.compose.ui.geometry.Offset(
                                                                0f,
                                                                size.height / 2
                                                        ),
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
                                        text = "40-x: 60% ENA",
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Yellow.copy(alpha = 0.7f)
                                )
                        }

                        // Légende des rations
                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))
                        Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                Text(
                                        text = "Légende des rations :",
                                        style = MaterialTheme.typography.caption,
                                        fontWeight = FontWeight.Bold
                                )
                                rationsEnergieData.forEach { data ->
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
                                                                        if (rationSelectionnee ==
                                                                                        data.rationId
                                                                        )
                                                                                Color(0xFF9C27B0)
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.1f
                                                                                        )
                                                                        else Color.Transparent
                                                                )
                                                                .padding(AppSizes.paddingSmall),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                Column(modifier = Modifier.weight(1f)) {
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
                                                                        "Protéines: ${GraphFormattingUtils.formatPercentage(data.proteineEnergyPercentage)} | Lipides: ${GraphFormattingUtils.formatPercentage(data.lipideEnergyPercentage)}",
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
}

