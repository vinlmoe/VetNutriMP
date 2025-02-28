package fr.vetbrain.vetnutri_mp.Data

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
@OptIn(ExperimentalUuidApi::class)
data class ConsultationEv(
        var uuid: String = Uuid.random().toString(),
        var idAnim: String = "",
        var date: LocalDate? = null,
        var objectConsult: String = "",
        var observation: String = "",
        var cRendu: String = "",
        var weight: Float? = null,
        var idealWeight: Float? = null,
        var water: Float? = null,
        var bodyFat: Float? = null,
        var methodAnalysis: String = "",
        var BCS: Int? = null,
        var k1Id: String? = null,
        var k1Value: Float? = null,
        var k2Id: String? = null,
        var k2Value: Float? = null,
        var k3Id: String? = null,
        var k3Value: Float? = null,
        var k4Id: String? = null,
        var k4Value: Float? = null,
        var k5Id: String? = null,
        var k5Value: Float? = null,
        var nLittle: Int? = null,
        var pAdult: Float? = null,
        var coefGes: Int? = null,
        var coefLact: Int? = null,
        var MCS: Int? = null,
        var suppVarp: MutableList<SupplementalvariableP> = mutableListOf(),
        var rations: MutableList<Ration> = mutableListOf()
) {
        constructor() :
                this(
                        uuid = Uuid.random().toString(),
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
}
