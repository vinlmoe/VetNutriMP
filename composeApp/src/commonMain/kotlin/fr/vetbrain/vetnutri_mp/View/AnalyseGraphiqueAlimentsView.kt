package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import io.github.koalaplot.core.*
import io.github.koalaplot.core.line.LinePlot
import io.github.koalaplot.core.xygraph.*
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.Point
import io.github.koalaplot.core.xygraph.XYGraph
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi

/**
 * Données calculées pour un aliment avec sa densité énergétique et pourcentages
 */
data class AlimentAnalyseData(
    val aliment: AlimentEv,
    val numero: Int,
    val densiteEnergetique: Double,
    val pourcentageProteines: Double,
    val pourcentageLipides: Double
)

/**
 * Vue d'analyse graphique des aliments sélectionnés
 */
@Composable
fun AnalyseGraphiqueAlimentsView(
    aliments: List<AlimentEv>,
    referenceEv: ReferenceEv?,
    equationRepository: EquationRepository?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Calculer les données d'analyse pour chaque aliment
    val alimentsAnalyses = remember(aliments, referenceEv, equationRepository) {
        aliments.mapIndexedNotNull { index, aliment ->
            try {
                val densiteEnergetique = calculerDensiteEnergetique(aliment, referenceEv, equationRepository)
                val pourcentageProteines = calculerPourcentageEnergieProteines(aliment, densiteEnergetique)
                val pourcentageLipides = calculerPourcentageEnergieLipides(aliment, densiteEnergetique)
                
                AlimentAnalyseData(
                    aliment = aliment,
                    numero = index + 1,
                    densiteEnergetique = densiteEnergetique,
                    pourcentageProteines = pourcentageProteines,
                    pourcentageLipides = pourcentageLipides
                )
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.densiteEnergetique }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(AppSizes.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
    ) {
        // En-tête avec titre et bouton de fermeture
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Retour",
                        tint = VetNutriColors.Primary
                    )
                }
                Column {
                    Text(
                        text = "Analyse graphique de ${aliments.size} aliment(s)",
                        style = MaterialTheme.typography.h5,
                        fontWeight = FontWeight.Bold,
                        color = VetNutriColors.Primary
                    )
                    Text(
                        text = "Visualisation des caractéristiques nutritionnelles",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Contenu principal - responsive selon la largeur
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val isCompact = maxWidth < 800.dp
            
            if (isCompact) {
                // Vue compacte : graphiques puis liste
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                    // Graphique principal
                    GraphiqueNuagePoints(alimentsAnalyses, modifier = Modifier.fillMaxWidth())
                    
                    // Liste des aliments
                    ListeAlimentsAnalyse(alimentsAnalyses, modifier = Modifier.fillMaxWidth())
                }
            } else {
                // Vue large : côte à côte
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                    // Colonne gauche : liste des aliments (1/4 de la largeur)
                    Column(
                        modifier = Modifier.weight(0.25f),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                    ) {
                        ListeAlimentsAnalyse(alimentsAnalyses, modifier = Modifier.fillMaxWidth())
                    }
                    
                    // Colonne droite : graphiques (3/4 de la largeur)
                    Column(
                        modifier = Modifier.weight(0.75f),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                    ) {
                        GraphiqueNuagePoints(alimentsAnalyses, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

/**
 * Graphique en nuage de points : % énergie protéines vs % énergie lipides
 */
@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun GraphiqueNuagePoints(
    alimentsAnalyses: List<AlimentAnalyseData>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = AppSizes.elevationMedium
    ) {
        Column(
            modifier = Modifier.padding(AppSizes.paddingMedium)
        ) {
            Text(
                text = "Répartition énergétique : Protéines vs Lipides",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                color = VetNutriColors.Primary
            )
            
            Text(
                text = "Chaque point représente un aliment (numéroté de 1 à ${alimentsAnalyses.size})",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(AppSizes.paddingMedium))
            
            if (alimentsAnalyses.isNotEmpty()) {
                // Préparer les données pour le graphique
                val points = alimentsAnalyses.map { data ->
                    Point(
                        x = data.pourcentageProteines.toFloat(),
                        y = data.pourcentageLipides.toFloat()
                    )
                }
                
                // Calculer les plages des axes
                val minX = points.minOf { it.x }.coerceAtLeast(0f)
                val maxX = points.maxOf { it.x }.coerceAtMost(100f)
                val minY = points.minOf { it.y }.coerceAtLeast(0f)
                val maxY = points.maxOf { it.y }.coerceAtMost(100f)
                
                val xRange = (minX - 5f)..(maxX + 5f)
                val yRange = (minY - 5f)..(maxY + 5f)
                
                XYGraph(
                    xAxisModel = FloatLinearAxisModel(range = xRange),
                    yAxisModel = FloatLinearAxisModel(range = yRange),
                    modifier = Modifier.height(400.dp)
                ) {
                    // Créer tous les points d'un coup pour LinePlot
                    val allPoints = alimentsAnalyses.map { data ->
                        Point(
                            x = data.pourcentageProteines.toFloat(),
                            y = data.pourcentageLipides.toFloat()
                        )
                    }
                    
                    // Afficher tous les points en une seule fois
                    if (allPoints.isNotEmpty()) {
                        LinePlot(data = allPoints)
                    }
                }
                
                // Légende
                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                Text(
                    text = "Axes : X = % énergie protéines, Y = % énergie lipides",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            } else {
                Text(
                    text = "Aucune donnée disponible pour l'analyse",
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Liste des aliments avec leurs données d'analyse
 */
@Composable
private fun ListeAlimentsAnalyse(
    alimentsAnalyses: List<AlimentAnalyseData>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = AppSizes.elevationMedium
    ) {
        Column(
            modifier = Modifier.padding(AppSizes.paddingMedium)
        ) {
            Text(
                text = "Liste des aliments (triés par densité énergétique décroissante)",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                color = VetNutriColors.Primary
            )
            
            Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
            
            if (alimentsAnalyses.isNotEmpty()) {
                // En-têtes du tableau
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "N°",
                        modifier = Modifier.weight(0.1f),
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Nom",
                        modifier = Modifier.weight(0.4f),
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Gamme",
                        modifier = Modifier.weight(0.25f),
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Marque",
                        modifier = Modifier.weight(0.25f),
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = AppSizes.paddingSmall))
                
                // Lignes du tableau
                alimentsAnalyses.forEach { data ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${data.numero}",
                            modifier = Modifier.weight(0.1f),
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Bold,
                            color = VetNutriColors.Primary
                        )
                        Text(
                            text = data.aliment.nom ?: "Sans nom",
                            modifier = Modifier.weight(0.4f),
                            style = MaterialTheme.typography.caption
                        )
                        Text(
                            text = data.aliment.gamme ?: "-",
                            modifier = Modifier.weight(0.25f),
                            style = MaterialTheme.typography.caption
                        )
                        Text(
                            text = data.aliment.brand ?: "-",
                            modifier = Modifier.weight(0.25f),
                            style = MaterialTheme.typography.caption
                        )
                    }
                    
                    if (alimentsAnalyses.last() != data) {
                        Divider(
                            modifier = Modifier.padding(vertical = AppSizes.paddingSmall / 2),
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f)
                        )
                    }
                }
            } else {
                Text(
                    text = "Aucun aliment à analyser",
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Calcule la densité énergétique d'un aliment
 */
private fun calculerDensiteEnergetique(
    aliment: AlimentEv,
    referenceEv: ReferenceEv?,
    equationRepository: EquationRepository?
): Double {
    // Essayer d'abord de calculer via les équations si disponible
    if (referenceEv != null && equationRepository != null) {
        try {
            val energie = aliment.getNutrient(NutrientMain.ENERGIE, referenceEv)
            if (energie != null && energie > 0) {
                return energie
            }
        } catch (e: Exception) {
            // Fallback sur la méthode simple
        }
    }
    
    // Méthode simple : calcul basé sur les macronutriments
    val proteines = aliment.getNutrient(NutrientMain.PROTEINE) ?: 0.0
    val lipides = aliment.getNutrient(NutrientMain.LIPIDE) ?: 0.0
    val glucides = aliment.getNutrient(NutrientMain.GLUCIDE) ?: 0.0
    
    // Coefficients énergétiques (kcal/g)
    val kcalProteines = proteines * 3.5
    val kcalLipides = lipides * 8.5
    val kcalGlucides = glucides * 3.5
    
    return kcalProteines + kcalLipides + kcalGlucides
}

/**
 * Calcule le pourcentage d'énergie apporté par les protéines
 */
private fun calculerPourcentageEnergieProteines(
    aliment: AlimentEv,
    densiteEnergetique: Double
): Double {
    if (densiteEnergetique <= 0) return 0.0
    
    val proteines = aliment.getNutrient(NutrientMain.PROTEINE) ?: 0.0
    val energieProteines = proteines * 3.5
    
    return (energieProteines / densiteEnergetique) * 100.0
}

/**
 * Calcule le pourcentage d'énergie apporté par les lipides
 */
private fun calculerPourcentageEnergieLipides(
    aliment: AlimentEv,
    densiteEnergetique: Double
): Double {
    if (densiteEnergetique <= 0) return 0.0
    
    val lipides = aliment.getNutrient(NutrientMain.LIPIDE) ?: 0.0
    val energieLipides = lipides * 8.5
    
    return (energieLipides / densiteEnergetique) * 100.0
}
