package fr.vetbrain.vetnutri_mp.ViewModel

// Calculs métaboliques de la fiche animal (extensions extraites de AnimalDetailViewModel).

import fr.vetbrain.vetnutri_mp.Data.CalculMetabolique
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv

/** Calcule le poids métabolique en utilisant l'équation BW de la référence */
internal fun AnimalDetailViewModel.calculerPoidsMetabolique(
        consultation: ConsultationEv,
        reference: ReferenceEv
): Double? {
    val poids = consultation.effectiveWeight ?: return null
    return CalculMetabolique.poidsMetabolique(poids, reference, consultation.suppVarp)
}

/** Calcule le besoin énergétique standard en utilisant l'équation BEE de la référence */
internal fun AnimalDetailViewModel.calculerBesoinEnergetiqueStandard(
        consultation: ConsultationEv,
        reference: ReferenceEv
): Double? {
    val poids = consultation.effectiveWeight ?: return null
    return CalculMetabolique.besoinEnergetiqueStandard(poids, reference, consultation.suppVarp)
}

/**
 * Calcule le besoin énergétique total en multipliant le BEE par tous les coefficients K et le
 * coefficient d'ajustement
 */
internal fun AnimalDetailViewModel.calculerBesoinEnergetiqueTotal(consultation: ConsultationEv, bee: Double): Double {
    // Récupération des coefficients K de la consultation
    val k1 = consultation.k1Value?.toDouble() ?: 1.0
    val k2 = consultation.k2Value?.toDouble() ?: 1.0
    val k3 = consultation.k3Value?.toDouble() ?: 1.0
    val k4 = consultation.k4Value?.toDouble() ?: 1.0
    val k5 = consultation.k5Value?.toDouble() ?: 1.0
    val coefficientAjustement = consultation.coefficientAjustement?.toDouble() ?: 1.0

    // Calcul du besoin total
    val besoinTotal = bee * k1 * k2 * k3 * k4 * k5 * coefficientAjustement

    return besoinTotal
}

/**
 * Obtient la référence active pour une consultation (générale ou première référence de maladie)
 */
internal suspend fun AnimalDetailViewModel.obtenirReferenceActiveConsultation(
        consultation: ConsultationEv
): ReferenceEv? {
    return try {
        // Priorité 1: Référence générale
        consultation.referenceGeneraleId?.let { referenceId ->
            val reference = databaseReferenceEvRepository.getReferenceEvById(referenceId)
            if (reference != null) {
                return reference
            }
        }

        // Priorité 2: Première référence de maladie
        consultation.referencesMaladies.firstOrNull()?.let { referenceId ->
            val reference = databaseReferenceEvRepository.getReferenceEvById(referenceId)
            if (reference != null) {
                return reference
            }
        }

        null
    } catch (e: Exception) {
        null
    }
}
