package fr.vetbrain.vetnutri_mp.View.AnalyseGraphique

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.GraphFormattingUtils
import fr.vetbrain.vetnutri_mp.Utils.KoalaPlotExtensions
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import io.github.koalaplot.core.*
import io.github.koalaplot.core.bar.DefaultVerticalBar
import io.github.koalaplot.core.bar.VerticalBarPlot
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.*

/** Graphique d'histogramme de la densité énergétique des rations */
@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun DensiteRationsChart(
        viewModel: AnimalDetailViewModel,
        equationRepository: EquationRepository? = null,
        useDryMatterPer100g: Boolean = false
) {
        val animal by viewModel.animal.collectAsState()
        val referenceUtilisee by viewModel.referenceUtilisee.collectAsState()
        val speciesPreferences by viewModel.speciesPreferences.collectAsState()

        // États pour les données des rations
        var rationsEnergieData by remember { mutableStateOf<List<RationEnergyData>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var rationSelectionnee by remember { mutableStateOf<String?>(null) }
        var nutrimentX by remember { mutableStateOf<String?>("energie") }

        // Calculer les données des rations de manière asynchrone
        LaunchedEffect(animal?.consultations?.size, referenceUtilisee, speciesPreferences) {
                isLoading = true
                val resultat = mutableListOf<RationEnergyData>()

                animal?.consultations?.forEachIndexed { consultationIndex, consultation ->
                        consultation.rations.forEachIndexed { rationIndex, ration ->
                                try {
                                        val rationData =
                                                calculerPourcentagesEnergieRation(
                                                        ration = ration,
                                                        referenceEv = referenceUtilisee,
                                                        preferencesEspece = speciesPreferences,
                                                        equationRepository = equationRepository
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
                Box(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                ) {
                        Text(
                                text = "Aucune ration disponible pour l'analyse de densité",
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

                // Préparer les données pour l'histogramme
                val categories = rationsEnergieData.map { "${it.numero}" }
                val densiteEnergetique =
                        rationsEnergieData.map {
                                if (useDryMatterPer100g) {
                                        // /100g MS : utiliser la matière sèche
                                        val poidsBase = it.matiereSeche
                                        if (poidsBase > 0) {
                                                (it.energieTotale / poidsBase * 100.0).toFloat()
                                        } else {
                                                0f
                                        }
                                } else {
                                        // /1000 kcal : calculer la densité énergétique pour 1000
                                        // kcal
                                        if (it.energieTotale > 0) {
                                                (it.poidsTotal / it.energieTotale * 1000.0)
                                                        .toFloat()
                                        } else {
                                                0f
                                        }
                                }
                        }

                // Calculer la plage adaptative
                val valeursValides = densiteEnergetique.filter { it.isFinite() && !it.isNaN() }
                val yRange =
                        if (valeursValides.isNotEmpty()) {
                                // Calcul adaptatif simple
                                val min = valeursValides.minOf { it }
                                val max = valeursValides.maxOf { it }
                                val range = max - min
                                val padding = maxOf(range * 0.1f, 10f)
                                arrondirPlage((maxOf(0f, min - padding))..(max + padding))
                        } else {
                                0f..1f
                        }

                // Graphique d'histogramme
                GraphCard(
                        titre = "Densité énergétique des rations",
                        sousTitre =
                                if (useDryMatterPer100g)
                                        "Énergie pour 100g de matière sèche (kcal/100g MS)"
                                else "Poids pour 1000 kcal (g/1000 kcal)"
                ) {
                        XYGraph(
                                xAxisModel = remember(categories) { CategoryAxisModel(categories) },
                                yAxisModel = remember(yRange) { KoalaPlotExtensions.createSmartDensityAxisModel(yRange) },
                                yAxisTitle =
                                        if (useDryMatterPer100g)
                                                "Densité énergétique (kcal/100g MS)"
                                        else "Densité énergétique (g/1000 kcal)",
                                modifier = Modifier.height(400.dp)
                        ) {
                                VerticalBarPlot(
                                        xData = categories,
                                        yData = densiteEnergetique,
                                        bar = { index ->
                                                val ration = rationsEnergieData[index]
                                                val couleur =
                                                        if (ration.rationId == rationSelectionnee) {
                                                                Color(
                                                                        0xFF9C27B0
                                                                ) // Violet pour sélectionnée
                                                        } else if (ration.rationId in
                                                                        rationsActuellesIds
                                                        ) {
                                                                Color(
                                                                        0xFFFF9800
                                                                ) // Orange pour rations actuelles
                                                        } else {
                                                                VetNutriColors
                                                                        .Primary // Couleur normale
                                                        }
                                                DefaultVerticalBar(brush = SolidColor(couleur))
                                        }
                                )
                        }

                        // Légende
                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))
                        Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                text = "Légende des rations :",
                                                style = MaterialTheme.typography.caption,
                                                fontWeight = FontWeight.Bold
                                        )

                                        Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall)
                                        ) {
                                                androidx.compose.foundation.Canvas(
                                                        modifier = Modifier.size(12.dp)
                                                ) {
                                                        drawCircle(
                                                                color = Color(0xFFFF9800),
                                                                radius = 6f,
                                                                center = center
                                                        )
                                                }
                                                Text(
                                                        text = "Rations actuelles",
                                                        style = MaterialTheme.typography.caption,
                                                        color = Color(0xFFFF9800)
                                                )
                                        }
                                }

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
                                                                        when {
                                                                                rationSelectionnee ==
                                                                                        data.rationId ->
                                                                                        Color(
                                                                                                        0xFF9C27B0
                                                                                                )
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.1f
                                                                                                )
                                                                                data.rationId in
                                                                                        rationsActuellesIds ->
                                                                                        Color(
                                                                                                        0xFFFF9800
                                                                                                )
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.1f
                                                                                                )
                                                                                else ->
                                                                                        Color.Transparent
                                                                        }
                                                                )
                                                                .padding(AppSizes.paddingSmall),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                Row(
                                                        modifier = Modifier.weight(1f),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically,
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(
                                                                        AppSizes.paddingSmall
                                                                )
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
                                                                                        data.rationId in
                                                                                                rationsActuellesIds ->
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
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .caption,
                                                                        fontWeight =
                                                                                FontWeight.Medium
                                                                )
                                                                Text(
                                                                        text =
                                                                                if (useDryMatterPer100g
                                                                                )
                                                                                        "Densité: ${GraphFormattingUtils.formatEnergyDensity(data.energieTotale / data.matiereSeche * 100.0)}"
                                                                                else
                                                                                        "Densité: ${GraphFormattingUtils.formatEnergyDensity(data.poidsTotal / data.energieTotale * 1000.0)}",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .caption,
                                                                        color =
                                                                                MaterialTheme.colors
                                                                                        .onSurface
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.6f
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

                // Sélecteurs de nutriments pour l'histogramme
               
        }
}

