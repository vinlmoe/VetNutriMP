package fr.vetbrain.vetnutri_mp.Data

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate

/**
 * Modèles de données des graphiques d'analyse (rations, courbes de croissance).
 * Déplacés depuis View/AnalyseGraphique pour que les calculs (Data/AnalyseGraphiqueCalculs.kt)
 * ne dépendent pas de la couche View.
 */

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
        val poidsTotal: Double = 0.0,
        val isRationActuelle: Boolean = false
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
