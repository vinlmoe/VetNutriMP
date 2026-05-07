package fr.vetbrain.vetnutri_mp.View.AnalyseGraphique

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
data class ViewNutrimentOption(
        val key: String,
        val displayName: String,
        val unit: String = "g/1000 kcal"
)

val VIEW_NUTRIMENT_OPTIONS =
        listOf(
                ViewNutrimentOption("", "Aucun"), // Option "aucun" pour l'axe Y
                // Nutriments principaux
                ViewNutrimentOption("proteine", "Protéines", "g"),
                ViewNutrimentOption("lipide", "Lipides", "g"),
                ViewNutrimentOption("energie", "Énergie", "kcal"),
                // Minéraux
                ViewNutrimentOption("calcium", "Calcium", "g"),
                ViewNutrimentOption("phosphore", "Phosphore", "g")
        )

enum class ChartType(val displayName: String) {
        EVOLUTION_POIDS("Évolution du poids"),
        RATIONS_ENERGIE("Rations énergétiques"),
        DENSITE_RATIONS("Densité énergétique des rations"),
        NUTRIMENTS_RATIONS("Nutriments des rations")
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
