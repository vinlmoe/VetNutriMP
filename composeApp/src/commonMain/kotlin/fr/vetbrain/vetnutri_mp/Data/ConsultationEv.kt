package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Utils.genUUID
import kotlinx.datetime.LocalDate

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
        var coefficientAjustement: Double = 1.0
) {

        // Propriété calculée qui retourne le poids idéal si défini, sinon le poids actuel
        val effectiveWeight: Double?
                get() = idealWeight ?: weight
        constructor() :
                this(
                        uuid = genUUID(),
                        date = null,
                        objectConsult = "",
                        observation = "",
                        cRendu = "",
                        weight = null,
                        idealWeight = null,
                        water = null,
                        bodyFat = null,
                        methodAnalysis = "",
                        BCS = null,
                        k1Id = null,
                        k1Value = null,
                        k2Id = null,
                        k2Value = null,
                        k3Id = null,
                        k3Value = null,
                        k4Id = null,
                        k4Value = null,
                        k5Id = null,
                        k5Value = null,
                        nLittle = null,
                        pAdult = null,
                        coefGes = null,
                        coefLact = null,
                        idAnim = "",
                        MCS = null
                )

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
}
