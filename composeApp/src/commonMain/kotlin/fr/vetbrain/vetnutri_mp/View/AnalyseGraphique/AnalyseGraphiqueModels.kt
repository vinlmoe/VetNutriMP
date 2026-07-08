package fr.vetbrain.vetnutri_mp.View.AnalyseGraphique

import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate

// Data class pour gérer l'état du cône de perte de poids
data class WeightConeState(
    val startDate: LocalDate,
    val startWeight: Double,
    val targetWeight: Double? = null
)

// Data class pour stocker les informations d'âge d'une consultation
data class ConsultationAgeData(
        val date: LocalDate,
        val ageInDays: Int,
        val ageInYears: Double,
        val ageInMonths: Double,
        val weight: Double,
        val isFromConsultation: Boolean = false,
        val weightUuid: String? = null
)

// Data class pour stocker les données énergétiques des rations
data class RationEnergyData(
        val consultationDate: LocalDate?,
        val consultationId: String,
        val rationName: String,
        val rationId: String,
        val numero: Int,
        val proteineEnergyPercentage: Double,
        val lipideEnergyPercentage: Double,
        val energieTotale: Double,
        val matiereSeche: Double = 0.0,
        val poidsTotal: Double = 0.0
)

fun generateUuidString(): String {
        return Clock.System.now().toEpochMilliseconds().toString() +
                "-" +
                kotlin.random.Random.nextInt()
}

// Classes pour les courbes de croissance
data class CurveParamP(
        val name: String,
        val max: Double,
        val half: Double,
        val slope: Double,
        val UUID: String = generateUuidString()
)

data class CurveP(
        val description: String,
        val params: List<CurveParamP>,
        val biblioRef: String,
        val espece: String,
        val code: String,
        val ageMax: Int
)

// Données étendues des nutriments des rations
data class RationNutrimentData(
        val consultationDate: LocalDate?,
        val consultationId: String,
        val rationName: String,
        val rationId: String,
        val numero: Int,
        val proteines: Double = 0.0,
        val lipides: Double = 0.0,
        val glucides: Double = 0.0,
        val energie: Double = 0.0,
        val calcium: Double = 0.0,
        val phosphore: Double = 0.0,
        val magnesium: Double = 0.0,
        val sodium: Double = 0.0,
        val potassium: Double = 0.0,
        val matiereSeche: Double = 0.0,
        val poidsTotal: Double = 0.0,
        val isRationActuelle: Boolean = false
)

// Liste des nutriments disponibles pour les graphiques personnalisés (spécifique à cette vue)
// displayName contient une clé LocalizationKeys : utiliser translate(option.displayName) à l'affichage.
data class ViewNutrimentOption(
        val key: String,
        val displayName: String,
        val unit: String = "g/1000 kcal"
)

val VIEW_NUTRIMENT_OPTIONS =
        listOf(
                ViewNutrimentOption("", "graph.nutrimentNone"), // Option "aucun" pour l'axe Y
                // Nutriments principaux
                ViewNutrimentOption("proteine", LocalizationKeys.Chart.PROTEIN, "g"),
                ViewNutrimentOption("lipide", LocalizationKeys.Chart.FAT, "g"),
                ViewNutrimentOption("energie", LocalizationKeys.NutrientCategory.ENERGIE_NAME, "kcal"),
                // Minéraux
                ViewNutrimentOption("calcium", LocalizationKeys.Minerals.CALCIUM, "g"),
                ViewNutrimentOption("phosphore", LocalizationKeys.Minerals.PHOSPHORUS, "g")
        )

// Marges calibrées pour le layout interne de KoalaPlot (en dp)
// Titre Y + labels ticks Y ≈ 58dp | Titre X + labels ticks X ≈ 38dp
internal const val KOALAPLOT_LEFT_DP = 58f
internal const val KOALAPLOT_BOTTOM_DP = 38f
internal const val KOALAPLOT_TOP_DP = 6f
internal const val KOALAPLOT_RIGHT_DP = 14f

// displayName contient une clé LocalizationKeys : utiliser translate(chartType.displayName) à l'affichage.
enum class ChartType(val displayName: String) {
        EVOLUTION_POIDS("graph.chartTypeEvolutionPoids"),
        RATIONS_ENERGIE("graph.chartTypeRationsEnergie"),
        DENSITE_RATIONS(LocalizationKeys.Graph.DENSITY_TITLE),
        NUTRIMENTS_RATIONS("graph.chartTypeNutrimentsRations")
}

// Data class pour gérer l'état du zoom et du pan
data class ZoomPanStateView(
        val scaleX: Float = 1f,
        val scaleY: Float = 1f,
        val panX: Float = 0f,
        val panY: Float = 0f
) {
        fun reset(): ZoomPanStateView = ZoomPanStateView()
}
