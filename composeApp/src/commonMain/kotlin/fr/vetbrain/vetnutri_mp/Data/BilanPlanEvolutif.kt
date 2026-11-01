package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Utils.EquationEvaluator

/**
 * Bilan d'une étape : poids utilisé, besoin total (K + complémentaire), apport énergétique, et
 * apport en % des bornes basses (MIN/OPTIMIN) et hautes (MAX/OPTIMAX), par label de nutriment.
 */
data class BilanEtape(
        val poids: Double?,
        val besoinTotal: Double?,
        val energieApportee: Double,
        val couverturesMin: Map<String, PositionReference> = emptyMap(),
        val positionsMax: Map<String, PositionReference> = emptyMap()
)

/** Bilans des plans évolutifs d'une consultation (calcul pur, sans état ni base). */
object BilanPlanEvolutif {

    /**
     * Bilan de chaque ration des plans (rations parentes et étapes), indexé par UUID. Chaque
     * étape est calculée avec son poids et ses variables ([VariablesEtape]) ; sans [reference],
     * seul l'apport énergétique est connu.
     */
    suspend fun calculer(
            consultation: ConsultationEv,
            reference: ReferenceEv?,
            referencesMaladies: List<ReferenceEv>,
            equationRepository: EquationSource?
    ): Map<String, BilanEtape> =
            consultation.rations
                    .filter { r -> r.refRationParente != null || PlanEvolutif.aUnPlan(consultation, r) }
                    .associate { etape ->
                        etape.uuid to bilanEtape(consultation, etape, reference, referencesMaladies, equationRepository)
                    }

    private suspend fun bilanEtape(
            consultation: ConsultationEv,
            etape: Ration,
            reference: ReferenceEv?,
            maladies: List<ReferenceEv>,
            equationRepository: EquationSource?
    ): BilanEtape {
        val vue = VariablesEtape.consultationPourEtape(consultation, etape)
        val apport =
                etape.getDensiteEnergetiqueMoyenne(referenceEv = reference, equationRepository = equationRepository) *
                        etape.getQuantiteTotale()
        if (reference == null) return BilanEtape(vue.effectiveWeight, null, apport)

        val poids = vue.effectiveWeight
        val bee = poids?.let { CalculMetabolique.besoinEnergetiqueStandard(it, reference, vue.suppVarp) }
        val mw = poids?.let { CalculMetabolique.poidsMetabolique(it, reference, vue.suppVarp) }
        val besoin =
                bee?.let { b ->
                    val beK = b * RationAnalysisCalculations.calculerCoefficientGlobal(vue)
                    val additionnelle =
                            if (mw != null) {
                                EquationEvaluator.calculerEnergieAdditionnelle(
                                        referencesMaladies = maladies,
                                        poidsCorps = poids,
                                        besoinEnergetiqueApresK = beK,
                                        besoinEnergetiqueStandard = b,
                                        poidsMetabolique = mw,
                                        variablesSupp = vue.suppVarp,
                                        ration = etape,
                                        referenceEv = reference,
                                        equationRepository = equationRepository
                                )
                            } else 0.0
                    beK + additionnelle
                }
        // Nutriments : bornes converties avec le BEE brut (comme l'écran d'analyse et le PDF),
        // nutriments sans donnée ignorés
        val valeurs =
                (equationRepository?.let { analyserValeursNutritionnellesRationAvecEquations(etape, it, reference) }
                                ?: analyserValeursNutritionnellesRation(etape))
                        .values
                        .filter { it.valeur > 0.0 }
        val (couverturesMin, positionsMax) =
                calculerPositionsReferences(
                        valeurs = valeurs,
                        referenceUtilisee = reference,
                        besoinEnergetiqueEntretien = bee,
                        poidsAnimal = poids,
                        poidsMetabolique = mw,
                        referencesMaladies = maladies
                )
        return BilanEtape(poids, besoin, apport, couverturesMin, positionsMax)
    }
}
