package fr.vetbrain.vetnutri_mp.View

import fr.vetbrain.vetnutri_mp.Data.ConsultationEv

internal object RationAnalysisCalculations {
    fun calculerCoefficientGlobal(consultation: ConsultationEv?): Double {
        if (consultation == null) return 1.0

        return listOf(
            consultation.k1Value,
            consultation.k2Value,
            consultation.k3Value,
            consultation.k4Value,
            consultation.k5Value
        ).fold(consultation.coefficientAjustement) { produit, coefficient ->
            produit * (coefficient ?: 1.0)
        }
    }

    fun calculerBesoinApresK(
        besoinEnergetiqueStandard: Double?,
        coefficientGlobal: Double
    ): Double? = besoinEnergetiqueStandard?.times(coefficientGlobal)

    fun calculerBesoinTotal(
        besoinApresK: Double?,
        energieAdditionnelle: Double
    ): Double? = besoinApresK?.plus(energieAdditionnelle)

    fun calculerPourcentageCouverture(
        energieApportee: Double,
        besoinEnergetiqueTotal: Double?
    ): Double {
        return if (besoinEnergetiqueTotal != null && besoinEnergetiqueTotal > 0.0) {
            (energieApportee / besoinEnergetiqueTotal) * 100.0
        } else {
            0.0
        }
    }

    fun calculerCoefficientObserve(
        energieApportee: Double,
        besoinEnergetiqueStandard: Double?
    ): Double {
        return if (besoinEnergetiqueStandard != null && besoinEnergetiqueStandard > 0.0) {
            energieApportee / besoinEnergetiqueStandard
        } else {
            0.0
        }
    }
}
