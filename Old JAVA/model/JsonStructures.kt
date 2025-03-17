package model

import java.time.LocalDate

/**
 * Structure JSON pour AlimentEv
 */
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

/**
 * Structure JSON pour AnimalEv
 */
data class AnimalEvJson(
    val UUID: String,
    val version: String = "22.1",
    val nom: String = "",
    val dead: Boolean = false,
    val id: String?,
    val sex: Int = 0,
    val espece: String = "1",
    val nomProprio: String = "",
    val dateNaiss: LocalDate = LocalDate.now(),
    val race: String = "",
    val resume: String = "",
    val listWeight: List<WeightDateJson> = listOf(),
    val list: ListConsultEvJson
)

/**
 * Structure JSON pour ConsultationEv
 */
data class ConsultationEvJson(
    val UUID: String,
    val date: LocalDate,
    val pdate: LocalDate,
    val objet: String?,
    val observation: String?,
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
    val rationList: Map<String, RationJson>,
    val diseaseRef: List<String> = listOf(),
    val svp: List<SupplementalvariablePJson>
)

/**
 * Structure JSON pour BiblioRef
 */
data class BiblioRefJson(
    val UUID: String,
    val firstAuthor: String = "",
    val year: Int = 1800,
    val completeRef: String = "",
    val comment: String = "",
    val consistent: Int = 0
)

/**
 * Structure JSON pour AlimSaver
 */
data class AlimSaverJson(
    val listAl: List<AlimentEvJson>,
    val db: AlimDBListJson
)

/**
 * Structure JSON pour AdjustSaveEv
 */
data class AdjustSaveEvJson(
    val UUID: String,
    val Name: String = "",
    val description: String = "",
    val esp: String = "CH",
    val list: List<TargetDefinitionEvJson>
)

/**
 * Structure JSON pour AlimentRation
 */
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

data class WeightDateJson(
    val UUID: String,
    val date: LocalDate,
    val value: Float
)

data class ListConsultEvJson(
    val consultations: List<ConsultationEvJson>
)

data class RationJson(
    val UUID: String,
    val nom: String,
    val aliments: List<AlimentRationJson>,
    val actual: Boolean = false
)

data class AlimDBListJson(
    val dbList: Map<String, AlimDBJson>
)

data class AlimDBJson(
    val UUID: String,
    val sNom: String,
    val description: String
)

data class TargetDefinitionEvJson(
    val target: String,
    val value: Float,
    val unit: String,
    val percentCompletion: Float,
    val pas: Float
)

data class SupplementalvariablePJson(
    val variable: String,
    val value: Float
)

data class NutrientQuantity(
    val value: Float,
    val present: Boolean
) 