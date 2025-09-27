package fr.vetbrain.vetnutri_mp.View.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import kotlinx.coroutines.runBlocking
import io.github.koalaplot.core.*
import io.github.koalaplot.core.pie.*
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi

/**
 * Données d'un nutriment pour le graphique en secteurs
 */
data class NutrientData(
    val name: String,
    val value: Double,
    val color: Color,
    val percentage: Double
)

/**
 * Composant graphique en secteurs pour afficher la répartition des nutriments
 * 
 * @param aliment Aliment dont on veut afficher les nutriments
 * @param referenceEv Référence pour calculer les nutriments complémentaires via équations
 * @param modifier Modificateur optionnel
 */
@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun NutrientPieChart(
    aliment: AlimentEv,
    referenceEv: ReferenceEv? = null,
    equationRepository: EquationRepository? = null,
    modifier: Modifier = Modifier
) {
    val nutrientData = remember(aliment, referenceEv, equationRepository) {
        extractNutrientDataWithComplementary(aliment, referenceEv, equationRepository)
    }

    if (nutrientData.isEmpty()) {
        // Afficher un message si aucune donnée nutritionnelle n'est disponible
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = AppSizes.elevationSmall,
            backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.5f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSizes.paddingMedium),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucune donnée nutritionnelle disponible",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = AppSizes.elevationSmall
    ) {
        Column(
            modifier = Modifier.padding(AppSizes.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
        ) {
            // Titre du graphique
            Text(
                text = "Répartition des nutriments",
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                color = VetNutriColors.Primary
            )

            // Graphique en secteurs avec KoalaPlot
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                 PieChart(
                     values = nutrientData.map { it.value.toFloat() },
                     slice = { index ->
                         val data = nutrientData[index]
                         DefaultSlice(data.color)
                     },
                     modifier = Modifier.size(200.dp)
                 )
            }

            // Légende des couleurs
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                nutrientData.forEach { data ->
                    NutrientLegendItem(
                        name = data.name,
                        value = data.value,
                        color = data.color,
                        percentage = data.percentage
                    )
                }
            }
        }
    }
}

/**
 * Élément de légende pour un nutriment
 */
@Composable
private fun NutrientLegendItem(
    name: String,
    value: Double,
    color: Color,
    percentage: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Indicateur de couleur
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, MaterialTheme.shapes.small)
            )
            
            // Nom du nutriment
            Text(
                text = name,
                style = MaterialTheme.typography.body2,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Valeur et pourcentage
        Text(
            text = "${TextUtils.formatDecimal(value, 1)}g (${TextUtils.formatDecimal(percentage, 1)}%)",
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * Extrait les données des nutriments principaux depuis un aliment, en utilisant les équations complémentaires si nécessaire
 * 
 * @param aliment Aliment dont extraire les données
 * @param referenceEv Référence pour calculer les nutriments complémentaires
 * @return Liste des données de nutriments
 */
private fun extractNutrientDataWithComplementary(aliment: AlimentEv, referenceEv: ReferenceEv?, equationRepository: EquationRepository?): List<NutrientData> {
    val targetNutrients = listOf(
        NutrientMain.HUMIDITE,
        NutrientMain.PROTEINE,
        NutrientMain.LIPIDE,
        NutrientMain.ENA,
        NutrientMain.CENDRE,
        NutrientMain.CELLULOSE
    )

    val nutrientData = mutableListOf<NutrientData>()
    var totalValue = 0.0

    // D'abord, calculer toutes les valeurs des nutriments (directes + équations)
    val nutrientValues = mutableMapOf<NutrientMain, Double>()
    
    targetNutrients.forEach { nutrient ->
        // Utiliser getNutrientWithComplementary pour obtenir la valeur (directe + calculée via équations)
        val nutrientValue = runBlocking {
            // Créer un AlimentRation temporaire pour utiliser getNutrientWithComplementary
            val alimentRation = AlimentRation(
                aliment = aliment,
                quantite = 100.0, // 100g pour les calculs
                proportion = 100.0,
                weight = 100.0,
                densiteEnergetique = 0.0
            )
            
            // Utiliser getNutrientWithComplementary qui gère automatiquement les équations complémentaires
            alimentRation.getNutrientWithComplementary(
                nutrient = nutrient,
                preferences = null, // Pas de préférences spécifiques pour l'instant
                equationRepository = equationRepository, // Utiliser le repository d'équations
                referenceEv = referenceEv
            )
        }
        
        if (nutrientValue != null && nutrientValue > 0) {
            nutrientValues[nutrient] = nutrientValue
            totalValue += nutrientValue
        }
    }

    // Si aucune donnée nutritionnelle n'est disponible, retourner une liste vide
    if (totalValue == 0.0) {
        return emptyList()
    }

    // Créer les données pour chaque nutriment avec les valeurs calculées
    nutrientValues.forEach { (nutrient, nutrientValue) ->
        val percentage = (nutrientValue / totalValue) * 100.0
        val color = parseColor(nutrient.color)
        
        nutrientData.add(
            NutrientData(
                name = getNutrientDisplayName(nutrient),
                value = nutrientValue,
                color = color,
                percentage = percentage
            )
        )
    }

    return nutrientData
}


/**
 * Parse une couleur hexadécimale en Color Compose
 */
private fun parseColor(hexColor: String): Color {
    val cleanHex = hexColor.removePrefix("#")
    val colorValue = cleanHex.toLong(16)
    return Color(colorValue or 0xFF000000L)
}

/**
 * Obtient le nom d'affichage d'un nutriment
 */
private fun getNutrientDisplayName(nutrient: NutrientMain): String {
    return when (nutrient) {
        NutrientMain.HUMIDITE -> "Humidité"
        NutrientMain.PROTEINE -> "Protéines"
        NutrientMain.LIPIDE -> "Lipides"
        NutrientMain.ENA -> "ENA"
        NutrientMain.CENDRE -> "Cendres"
        NutrientMain.CELLULOSE -> "Cellulose"
        else -> nutrient.name
    }
}
