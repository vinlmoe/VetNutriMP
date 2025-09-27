package fr.vetbrain.vetnutri_mp.Utils

import io.github.koalaplot.core.xygraph.FloatLinearAxisModel

/**
 * Extensions pour KoalaPlot avec formatage intelligent des décimales
 * 
 * Note: KoalaPlot utilise son propre formatage interne pour les étiquettes d'axes des graphiques XY.
 * Le formatage intelligent est appliqué dans les légendes, titres et informations contextuelles
 * où l'utilisateur peut vraiment voir la différence.
 */
object KoalaPlotExtensions {

    /**
     * Crée un modèle d'axe X avec des paramètres optimisés pour contrôler les ticks
     */
    fun createSmartXAxisModel(range: ClosedFloatingPointRange<Float>): FloatLinearAxisModel {
        return FloatLinearAxisModel(
            range = range,
            minimumMajorTickIncrement = calculateOptimalTickIncrement(range)
        )
    }

    /**
     * Crée un modèle d'axe Y avec des paramètres optimisés pour contrôler les ticks
     */
    fun createSmartYAxisModel(range: ClosedFloatingPointRange<Float>): FloatLinearAxisModel {
        return FloatLinearAxisModel(
            range = range,
            minimumMajorTickIncrement = calculateOptimalTickIncrement(range)
        )
    }

    /**
     * Crée un modèle d'axe pour les pourcentages (0-100%)
     * Optimisé pour afficher des valeurs de 0 à 100 avec des ticks appropriés
     */
    fun createSmartPercentageAxisModel(range: ClosedFloatingPointRange<Float>): FloatLinearAxisModel {
        val tickIncrement = when {
            range.endInclusive - range.start <= 10f -> 1f  // 1% par tick pour petites plages
            range.endInclusive - range.start <= 50f -> 5f  // 5% par tick pour moyennes plages
            else -> 10f  // 10% par tick pour grandes plages
        }
        
        return FloatLinearAxisModel(
            range = range,
            minimumMajorTickIncrement = tickIncrement
        )
    }

    /**
     * Crée un modèle d'axe pour les énergies
     */
    fun createSmartEnergyAxisModel(range: ClosedFloatingPointRange<Float>): FloatLinearAxisModel {
        return FloatLinearAxisModel(
            range = range,
            minimumMajorTickIncrement = calculateOptimalTickIncrement(range)
        )
    }

    /**
     * Crée un modèle d'axe pour les densités énergétiques (kcal/100g)
     * Optimisé pour les valeurs de densité
     */
    fun createSmartDensityAxisModel(range: ClosedFloatingPointRange<Float>): FloatLinearAxisModel {
        val tickIncrement = when {
            range.endInclusive - range.start <= 50f -> 10f   // 10 kcal/100g par tick
            range.endInclusive - range.start <= 200f -> 25f // 25 kcal/100g par tick
            else -> 50f  // 50 kcal/100g par tick
        }
        
        return FloatLinearAxisModel(
            range = range,
            minimumMajorTickIncrement = tickIncrement
        )
    }

    /**
     * Calcule un incrément de tick optimal basé sur la plage de valeurs
     * Cela aide à contrôler le nombre de décimales affichées sur les axes
     */
    private fun calculateOptimalTickIncrement(range: ClosedFloatingPointRange<Float>): Float {
        val rangeSize = range.endInclusive - range.start
        
        return when {
            rangeSize <= 1f -> 0.1f      // 1 décimale pour petites plages
            rangeSize <= 5f -> 0.5f      // 1 décimale pour moyennes plages
            rangeSize <= 20f -> 2f       // Entiers pour plages normales
            rangeSize <= 100f -> 10f     // Entiers pour grandes plages
            rangeSize <= 500f -> 50f     // Entiers pour très grandes plages
            else -> 100f                 // Entiers pour plages énormes
        }
    }
}
