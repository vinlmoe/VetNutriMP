package fr.vetbrain.vetnutri_mp.Data

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/** Structure JSON pour AlimentEv */
@Serializable
data class AlimentEvJson(
        val UUID: String,
        val nom: String = "",
        val group: String,
        val foodKind: String,
        val ingredients: String = "",
        val prix: Double = 0.0,
        val categoriePrix: String = "i",
        val marque: String = "",
        val indication: List<String> = listOf(),
        val espece: Int,
        val Especes: List<String> = listOf(),
        val gamme: String = "",
        val presentation: String = "",
        val quantInt: Float = 0f,
        val cont: String = "NO",
        val deprecated: Boolean = false,
        val DataB: String = "6",
        val valMap: Map<String, NutrientQuantity> = mapOf()
)

/** Structure JSON pour AnimalEv */
@Serializable
data class AnimalEvJson(
        val UUID: String,
        val version: String = "22.1",
        val nom: String = "",
        val dead: Boolean = false,
        val id: String? = null,
        val sex: Int = 0,
        val espece: String = "1",
        val nomProprio: String = "",
        @Serializable(with = LocalDateSerializer::class)
        val dateNaiss: LocalDate = LocalDate(2023, 1, 1),
        val race: String = "",
        val resume: String = "",
        val listWeight: List<WeightDateJson> = listOf(),
        val list: ListConsultEvJson? = null,
        val consultations: List<ConsultationEvJson>? = null
)

/** Structure JSON pour ConsultationEv */
@Serializable
data class ConsultationEvJson(
        val UUID: String,
        val date: LocalDate,
        val pdate: LocalDate = LocalDate(2023, 1, 1),
        val objet: String? = null,
        val observation: String? = null,
        val CRendu: String = "",
        val Poids: Float = 0f,
        val PoidsIdeal: Float = 0f,
        val PoidsIdealex: Boolean = false,
        val Boisson: Float = 0f,
        val TauxMG: Float = 20f,
        val suivi: Boolean = false,
        val bcs: String = "",
        val MCS: Int = 3,
        val k1value: Float = 1f,
        val k2value: Float = 1f,
        val k3value: Float = 1f,
        val k4value: Float = 1f,
        val k5value: Float = 1f,
        val rationList: Map<String, RationJson> = mapOf(),
        val diseaseRef: List<String> = listOf(),
        val svp: List<SupplementalvariablePJson> = listOf(),
        val RefString: String? = null,
        val k1d: String? = null,
        val k2d: String? = null,
        val k3d: String? = null,
        val k4d: String? = null,
        val k5d: String? = null,
        val k6d: Int = 0,
        val previousBE: Float = 0f,
        val previousRation: RationJson? = null,
        val ky: Float = 0f,
        val newBE: Float = 0f,
        val newRation: List<RationJson> = listOf()
)

/** Structure JSON pour BiblioRef */
@Serializable
data class BiblioRefJson(
        val UUID: String,
        val firstAuthor: String = "",
        val year: Int = 1800,
        val completeRef: String = "",
        val comment: String = "",
        val consistent: Int = 0
)

/** Structure JSON pour AlimSaver */
@Serializable data class AlimSaverJson(val listAl: List<AlimentEvJson>, val db: AlimDBListJson)

/** Structure JSON pour AdjustSaveEv */
@Serializable
data class AdjustSaveEvJson(
        val UUID: String,
        val Name: String = "",
        val description: String = "",
        val esp: String = "CH",
        val list: List<TargetDefinitionEvJson>
)

/** Structure JSON pour AlimentRation */
@Serializable
data class AlimentRationJson(
        val UUID: String,
        val UUIDunif: String,
        val quantite: Float,
        val prop: Float,
        val alime: AlimentEvJson,
        val weight: Float = 1f,
        val categ: Int = 0,
        val density: Double = 0.0
)

// Structures de support

@Serializable data class WeightDateJson(val UUID: String, val date: LocalDate, val value: Float)

@Serializable data class ListConsultEvJson(val consultations: List<ConsultationEvJson>)

@Serializable
data class RationJson(
        val UUID: String,
        val Nom: String = "",
        val alimentList: List<AlimentRationJson> = listOf(),
        val actual: Boolean = false
)

@Serializable data class AlimDBListJson(val dbList: Map<String, AlimDBJson>)

@Serializable data class AlimDBJson(val UUID: String, val sNom: String, val description: String)

@Serializable
data class TargetDefinitionEvJson(
        val target: String,
        val value: Float,
        val unit: String,
        val percentCompletion: Float,
        val pas: Float
)

@Serializable data class SupplementalvariablePJson(val variable: String, val value: Float)

@Serializable data class NutrientQuantity(val value: Float, val nut: String)
