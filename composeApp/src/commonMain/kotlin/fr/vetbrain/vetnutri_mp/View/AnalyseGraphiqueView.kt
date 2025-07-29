package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import io.github.koalaplot.core.line.LinePlot
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.*
import kotlin.random.Random

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun AnalyseGraphiqueView(modifier: Modifier = Modifier) {
        var selectedChart by remember { mutableStateOf(ChartType.EVOLUTION_POIDS) }

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

                // Affichage du graphique sélectionné
                when (selectedChart) {
                        ChartType.EVOLUTION_POIDS -> EvolutionPoidsChart()
                        ChartType.COMPOSITION_NUTRITIONNELLE -> CompositionNutritionnelleChart()
                        ChartType.COMPARAISON_BESOINS -> ComparaisonBesoinsChart()
                        ChartType.REPARTITION_ENERGIE -> RepartitionEnergieChart()
                }

                // Légende et informations
                GraphiqueLegend(selectedChart)
        }
}

enum class ChartType(val displayName: String) {
        EVOLUTION_POIDS("Évolution du poids"),
        COMPOSITION_NUTRITIONNELLE("Composition nutritionnelle"),
        COMPARAISON_BESOINS("Apports vs Besoins"),
        REPARTITION_ENERGIE("Répartition énergétique")
}

@Composable
private fun GraphiqueHeader(selectedChart: ChartType, onChartSelected: (ChartType) -> Unit) {
        Card(modifier = Modifier.fillMaxWidth(), elevation = AppSizes.elevationSmall) {
                Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                Icon(
                                        imageVector = Icons.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = VetNutriColors.Primary
                                )
                                Text(
                                        text = "Analyse Graphique",
                                        style = MaterialTheme.typography.h6,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Primary
                                )
                        }

                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

                        // Sélecteur de type de graphique avec Row normale
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                ChartType.values().forEach { chartType ->
                                        Button(
                                                onClick = { onChartSelected(chartType) },
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                backgroundColor =
                                                                        if (selectedChart ==
                                                                                        chartType
                                                                        )
                                                                                VetNutriColors
                                                                                        .Primary
                                                                        else
                                                                                MaterialTheme.colors
                                                                                        .surface
                                                        ),
                                                modifier = Modifier.weight(1f)
                                        ) {
                                                Text(
                                                        text = chartType.displayName,
                                                        style = MaterialTheme.typography.caption,
                                                        color =
                                                                if (selectedChart == chartType)
                                                                        Color.White
                                                                else MaterialTheme.colors.onSurface
                                                )
                                        }
                                }
                        }
                }
        }
}

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun EvolutionPoidsChart() {
        // Données d'exemple pour l'évolution du poids
        val donneesPoids = remember {
                generateSequence(1) { it + 1 }
                        .take(12)
                        .map { mois ->
                                Point(
                                        x = mois.toFloat(),
                                        y = (15f + Random.nextFloat() * 5f) // Entre 15 et 20 kg
                                )
                        }
                        .toList()
        }

        GraphCard(titre = "Évolution du poids corporel", sousTitre = "Poids en kg sur 12 mois") {
                XYGraph(
                        xAxisModel =
                                FloatLinearAxisModel(
                                        range = 1f..12f,
                                        minimumMajorTickIncrement = 1f
                                ),
                        yAxisModel =
                                FloatLinearAxisModel(
                                        range = 10f..25f,
                                        minimumMajorTickIncrement = 2.5f
                                ),
                        modifier = Modifier.height(250.dp)
                ) {
                        LinePlot(
                                data = donneesPoids,
                                lineStyle =
                                        LineStyle(
                                                brush =
                                                        androidx.compose.ui.graphics.SolidColor(
                                                                VetNutriColors.Primary
                                                        ),
                                                strokeWidth = 2.dp
                                        )
                        )
                }
        }
}

@Composable
private fun CompositionNutritionnelleChart() {
        // Données d'exemple pour la composition sous forme de barres
        val donneesComposition = remember {
                mapOf(
                        "Protéines" to 25f,
                        "Lipides" to 15f,
                        "Glucides" to 45f,
                        "Fibres" to 10f,
                        "Cendres" to 5f
                )
        }

        val couleurs =
                listOf(
                        Color(0xFF2196F3),
                        Color(0xFF4CAF50),
                        Color(0xFFFF9800),
                        Color(0xFF9C27B0),
                        Color(0xFF607D8B)
                )

        GraphCard(
                titre = "Composition nutritionnelle",
                sousTitre = "Répartition en % de matière sèche"
        ) {
                // Implémentation avec des barres de progression
                Column(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceEvenly
                ) {
                        donneesComposition.entries.forEachIndexed { index, (nutriment, valeur) ->
                                val couleur = couleurs[index % couleurs.size]

                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                text = nutriment,
                                                modifier = Modifier.width(100.dp),
                                                style = MaterialTheme.typography.caption
                                        )

                                        LinearProgressIndicator(
                                                progress = valeur / 100f,
                                                modifier = Modifier.weight(1f).height(20.dp),
                                                color = couleur,
                                                backgroundColor = Color.LightGray.copy(alpha = 0.3f)
                                        )

                                        Text(
                                                text = "${valeur.toInt()}%",
                                                modifier = Modifier.width(50.dp),
                                                style = MaterialTheme.typography.caption,
                                                color = couleur
                                        )
                                }
                        }
                }
        }
}

@Composable
private fun ComparaisonBesoinsChart() {
        // Données d'exemple pour comparaison apports vs besoins
        val nutriments = listOf("Protéines", "Calcium", "Phosphore", "Vitamine A", "Fer")
        val apports = listOf(120f, 95f, 110f, 85f, 105f) // en % des besoins

        GraphCard(
                titre = "Apports vs Besoins nutritionnels",
                sousTitre = "En % des recommandations (100% = ligne de référence)"
        ) {
                // Implémentation simplifiée d'un graphique en barres
                Column(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceEvenly
                ) {
                        nutriments.forEachIndexed { index, nutriment ->
                                val apport = apports[index]
                                val couleur =
                                        when {
                                                apport < 80f -> Color.Red
                                                apport < 100f -> Color(0xFFFFA500) // Orange
                                                apport > 120f -> Color.Blue
                                                else -> Color.Green
                                        }

                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                text = nutriment,
                                                modifier = Modifier.width(100.dp),
                                                style = MaterialTheme.typography.caption
                                        )

                                        LinearProgressIndicator(
                                                progress = (apport / 150f).coerceIn(0f, 1f),
                                                modifier = Modifier.weight(1f).height(20.dp),
                                                color = couleur,
                                                backgroundColor = Color.LightGray.copy(alpha = 0.3f)
                                        )

                                        Text(
                                                text = "${apport.toInt()}%",
                                                modifier = Modifier.width(50.dp),
                                                style = MaterialTheme.typography.caption,
                                                color = couleur
                                        )
                                }
                        }
                }
        }
}

@Composable
private fun RepartitionEnergieChart() {
        // Données d'exemple pour la répartition énergétique
        val donneesEnergie = remember {
                mapOf(
                        "Protéines\n(4 kcal/g)" to 30f,
                        "Lipides\n(9 kcal/g)" to 35f,
                        "Glucides\n(4 kcal/g)" to 35f
                )
        }

        val couleurs = listOf(Color(0xFF3F51B5), Color(0xFFE91E63), Color(0xFFFF5722))

        GraphCard(
                titre = "Répartition énergétique",
                sousTitre = "Distribution calorique par macronutriment"
        ) {
                Column(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceEvenly
                ) {
                        donneesEnergie.entries.forEachIndexed { index, (nutriment, valeur) ->
                                val couleur = couleurs[index % couleurs.size]

                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                text = nutriment,
                                                modifier = Modifier.width(120.dp),
                                                style = MaterialTheme.typography.caption
                                        )

                                        LinearProgressIndicator(
                                                progress = valeur / 100f,
                                                modifier = Modifier.weight(1f).height(25.dp),
                                                color = couleur,
                                                backgroundColor = Color.LightGray.copy(alpha = 0.3f)
                                        )

                                        Text(
                                                text = "${valeur.toInt()}%",
                                                modifier = Modifier.width(50.dp),
                                                style = MaterialTheme.typography.caption,
                                                color = couleur
                                        )
                                }
                        }
                }
        }
}

@Composable
private fun GraphCard(titre: String, sousTitre: String, content: @Composable () -> Unit) {
        Card(modifier = Modifier.fillMaxWidth(), elevation = AppSizes.elevationMedium) {
                Column(
                        modifier = Modifier.padding(AppSizes.paddingMedium),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        Text(
                                text = titre,
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold,
                                color = VetNutriColors.Primary
                        )

                        Text(
                                text = sousTitre,
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

                        content()
                }
        }
}

@Composable
private fun GraphiqueLegend(selectedChart: ChartType) {
        Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = AppSizes.elevationSmall,
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.8f)
        ) {
                Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                        Text(
                                text = "Informations",
                                style = MaterialTheme.typography.subtitle2,
                                fontWeight = FontWeight.Bold,
                                color = VetNutriColors.Primary
                        )

                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                        val infoText =
                                when (selectedChart) {
                                        ChartType.EVOLUTION_POIDS ->
                                                "Suivez l'évolution du poids de l'animal sur plusieurs mois pour détecter les tendances."
                                        ChartType.COMPOSITION_NUTRITIONNELLE ->
                                                "Visualisez la répartition des macronutriments dans la ration actuelle."
                                        ChartType.COMPARAISON_BESOINS ->
                                                "Comparez les apports nutritionnels aux besoins recommandés. Vert = optimal, Orange = insuffisant, Rouge = carencé, Bleu = excès."
                                        ChartType.REPARTITION_ENERGIE ->
                                                "Analysez la distribution calorique entre protéines, lipides et glucides."
                                }

                        Text(
                                text = infoText,
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                }
        }
}
