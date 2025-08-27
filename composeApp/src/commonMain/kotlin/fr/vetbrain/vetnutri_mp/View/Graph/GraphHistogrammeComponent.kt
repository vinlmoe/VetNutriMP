package fr.vetbrain.vetnutri_mp.View.Graph

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import io.github.koalaplot.core.xygraph.*
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.bar.DefaultVerticalBar
import io.github.koalaplot.core.bar.VerticalBarPlot

// Import des modèles de données depuis le fichier parent
import fr.vetbrain.vetnutri_mp.View.AlimentAnalyseData

/**
 * Composant graphique pour afficher un histogramme de densité énergétique
 * Basé sur les exemples KoalaPlot : https://koalaplot.github.io/docs/xygraphs/bar_plots/
 */
@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun HistogrammeEnergieAliments(
    alimentsAnalyses: List<AlimentAnalyseData>,
    alimentSelectionne: String? = null,
    modifier: Modifier = Modifier
) {
    if (alimentsAnalyses.isEmpty()) {
        Text(
            text = "Aucune donnée disponible pour l'histogramme",
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        return
    }

    // Préparer les données pour l'histogramme
    // Créer les catégories avec les noms des aliments (tronqués) et leurs numéros
    val categories = alimentsAnalyses.map { data ->
        val nomTronque = if (data.aliment.nom?.length ?: 0 > 10) {
            "${data.aliment.nom?.take(10)}..."
        } else {
            data.aliment.nom ?: "N/A"
        }
        "$nomTronque\n(${data.numero})"
    }
    
    // Données de densité énergétique
    val densiteEnergetique = alimentsAnalyses.map { it.densiteEnergetique.toFloat() }
    
    // Plage pour l'axe Y basée sur les données
    val minDensite = densiteEnergetique.minOfOrNull { it } ?: 0f
    val maxDensite = densiteEnergetique.maxOfOrNull { it } ?: 100f
    val yRange = (minDensite - minDensite * 0.05f).coerceAtLeast(0f)..(maxDensite + maxDensite * 0.1f)

    // Créer le graphique
    XYGraph(
        xAxisModel = remember { CategoryAxisModel(categories) },
        yAxisModel = remember { FloatLinearAxisModel(yRange) },
        yAxisTitle = "Densité énergétique (kcal/100g)",
        modifier = modifier
    ) {
        VerticalBarPlot(
            xData = categories,
            yData = densiteEnergetique,
            bar = { index ->
                // Couleur de la barre selon si l'aliment est sélectionné
                val aliment = alimentsAnalyses[index]
                val couleur = if (aliment.aliment.uuid == alimentSelectionne) {
                    Color(0xFF9C27B0) // Violet pour l'aliment sélectionné
                } else {
                    VetNutriColors.Primary // Couleur par défaut
                }
                DefaultVerticalBar(SolidColor(couleur))
            }
        )
    }
}

/**
 * Composant générique pour histogrammes personnalisés
 */
@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun HistogrammeGenerique(
    categories: List<String>,
    valeurs: List<Float>,
    titre: String,
    uniteY: String,
    elementSelectionne: Int? = null,
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty() || valeurs.isEmpty()) {
        Text(
            text = "Aucune donnée disponible",
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        return
    }

    // Plage pour l'axe Y
    val minVal = valeurs.minOfOrNull { it } ?: 0f
    val maxVal = valeurs.maxOfOrNull { it } ?: 100f
    val yRange = (minVal - minVal * 0.05f).coerceAtLeast(0f)..(maxVal + maxVal * 0.1f)

    XYGraph(
        xAxisModel = remember { CategoryAxisModel(categories) },
        yAxisModel = remember { FloatLinearAxisModel(yRange) },
        yAxisTitle = uniteY,
        modifier = modifier
    ) {
        VerticalBarPlot(
            xData = categories,
            yData = valeurs,
            bar = { index ->
                val couleur = if (elementSelectionne == index) {
                    Color(0xFF9C27B0) // Violet pour l'élément sélectionné
                } else {
                    VetNutriColors.Primary // Couleur par défaut
                }
                DefaultVerticalBar(SolidColor(couleur))
            }
        )
    }
}