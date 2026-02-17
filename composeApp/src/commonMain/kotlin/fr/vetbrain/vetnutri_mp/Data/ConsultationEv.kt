package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Export.HtmlSection
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import kotlinx.datetime.LocalDate

/**
 * Consultation d'un animal.
 * - Données cliniques (poids, BCS, coefficients K, observations).
 * - Rations associées et références nutritionnelles (générale + maladies).
 * - Fournit `effectiveWeight` (poids idéal prioritaire) avec cache simple.
 */
data class ConsultationEv(
        var uuid: String = genUUID(),
        var idAnim: String = "",
        var date: LocalDate? = null,
        var objectConsult: String = "",
        var observation: String = "",
        var cRendu: String = "",
        var weight: Double? = null,
        var idealWeight: Double? = null,
        var water: Double? = null,
        var bodyFat: Double? = null,
        var methodAnalysis: String = "",
        var BCS: Int? = null,
        var k1Id: String? = null,
        var k1Value: Double? = null,
        var k2Id: String? = null,
        var k2Value: Double? = null,
        var k3Id: String? = null,
        var k3Value: Double? = null,
        var k4Id: String? = null,
        var k4Value: Double? = null,
        var k5Id: String? = null,
        var k5Value: Double? = null,
        var nLittle: Int? = null,
        var pAdult: Double? = null,
        var coefGes: Int? = null,
        var coefLact: Int? = null,
        var MCS: Int? = null,
        var suppVarp: MutableList<SupplementalvariableP> = mutableListOf(),
        var rations: MutableList<Ration> = mutableListOf(),
        var referenceGeneraleId: String? = null,
        var referencesMaladies: MutableList<String> = mutableListOf(),
        var keywordIds: MutableList<String> = mutableListOf(),
        var coefficientAjustement: Double = 1.0,
        // Ordonnance: état sauvegardé par consultation
        var prescriptionAdditionalText: String = "",
        var prescriptionSelectedConseilIds: MutableList<String> = mutableListOf(),
        var prescriptionLocalHtmlSections: MutableList<HtmlSection> = mutableListOf(),
        var prescriptionSelectedRationIds: MutableList<String> = mutableListOf()
) {

        // Cache pour la propriété calculée
        private var cachedEffectiveWeight: Double? = null
        private var lastWeight: Double? = null
        private var lastIdealWeight: Double? = null

        // Propriété calculée qui retourne le poids idéal si défini, sinon le poids actuel
        val effectiveWeight: Double?
                get() {
                    // Utiliser le cache si les valeurs n'ont pas changé
                    if (cachedEffectiveWeight != null &&
                        lastWeight == weight &&
                        lastIdealWeight == idealWeight) {
                        return cachedEffectiveWeight
                    }

                    val result = idealWeight ?: weight

                    // Mettre en cache
                    cachedEffectiveWeight = result
                    lastWeight = weight
                    lastIdealWeight = idealWeight

                    return result
                }

        fun getRationByID(uuid: String): Ration {
                return rations.last { ration: Ration -> ration.uuid == uuid }
        }

        fun ajouterReferenceMaladie(referenceId: String) {
                if (!referencesMaladies.contains(referenceId)) {
                        referencesMaladies.add(referenceId)
                }
        }

        fun supprimerReferenceMaladie(referenceId: String) {
                referencesMaladies.remove(referenceId)
        }

        fun contientReferenceMaladie(referenceId: String): Boolean {
                return referencesMaladies.contains(referenceId)
        }

        fun obtenirToutesReferences(): List<String> {
                val toutesReferences = mutableListOf<String>()
                referenceGeneraleId?.let { toutesReferences.add(it) }
                toutesReferences.addAll(referencesMaladies)
                return toutesReferences
        }

        fun ajouterMotCle(keywordId: String) {
                if (!keywordIds.contains(keywordId)) {
                        keywordIds.add(keywordId)
                }
        }

        fun supprimerMotCle(keywordId: String) {
                keywordIds.remove(keywordId)
        }

        fun contientMotCle(keywordId: String): Boolean {
                return keywordIds.contains(keywordId)
        }
}
