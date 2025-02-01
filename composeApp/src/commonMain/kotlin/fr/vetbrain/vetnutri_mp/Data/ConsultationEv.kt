package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumerise.TextConstant
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

import kotlinx.serialization.Serializable

@ExperimentalUuidApi
@Serializable
data class ConsultationEv(
    var UUID: String = Uuid.random().toString(),
    var date: LocalDate=LocalDate(1990,7,9),
    var pdate: LocalDate = LocalDate(1990,7,9),
    var objet: String? = null,
    var observation: String? = null,
    var cRendu: String = "",
    var poids: Float = 0f,
    var poidsIdeal: Float = 0f,
    var poidsIdealex: Boolean = false,
    var boisson: Float = 0f,
    var tauxMG: Float = 20f,
    var suivi: Boolean = false,
    var bcs: String? = null,
    var activite: String? = null,
    var mcs: Int = 3,
    var k2value: Float = 1f,
    var k1value: Float = 1f,
    var physio: String? = null,
    var refString: String? = null,
    var k3value: Float = 1f,
    var pathologie: String? = null,
    var version: String = TextConstant.VERSION.nameToString(),
    var k4value: Float = 1f,
    var autreobserv: String? = null,
    var k1d: String? = null,
    var k2d: String? = null,
    var k3d: String? = null,
    var k4d: String? = null,
    var k5d: String? = null,
    var k6d: Int = 0,
    var k5value: Float = 1f,
    var previousBE: Float = 0f,
    var previousRation: Ration = Ration(),
    var ky: Float = 1f,
    var newBE: Float = 0f,
    var newRation: MutableList<Ration> = mutableListOf(),
    var rationList: MutableMap<String, Ration> = mutableMapOf(),
    var newBCS: Int = 4,
    var objectif: Float = 0f,
    var coefIntG: Int = 0,
    var coefIntL: Int = 0,
    var nbPetit: Int = 0,
    var pMere: Float = 0f,
    var diseaseRef: MutableList<String> = mutableListOf(),
    var svp: MutableList<SupplementalvariableP> = mutableListOf()
)  {

    companion object {
        private const val serialVersionUID = 101L
    }

    init {
        for (s in VariableKind.values()) {
            svp.add(SupplementalvariableP(s))
        }
        if (rationList.isEmpty()) {
            val r = Ration()
            rationList[r.UUID] = r
        }
    }

    constructor(acons: ConsultationEv) : this() {
        if (acons.poidsIdealex) {
            this.poidsIdeal = acons.poidsIdeal
            poidsIdealex = true
        }
        k1d = acons.k1d
        k2d = acons.k2d
        k3d = acons.k3d
        k4d = acons.k4d
        k5d = acons.k5d
        pMere = acons.pMere
        k2value = acons.k2value
        k3value = acons.k3value
        k4value = acons.k4value
        k5value = acons.k5value
        coefIntG = acons.coefIntG
        coefIntL = acons.coefIntL
        nbPetit = acons.nbPetit
        svp = acons.svp
        val r = Ration()
        rationList[r.UUID] = r
        version = TextConstant.VERSION.nameToString()
    }


}