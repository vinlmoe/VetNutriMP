package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Export.HtmlSection
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
        val dateMaj: String = "",
        val imageRef: String = "",
        val presentation: String = "",
        val quantInt: Double = 0.0,
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
        val jsonbinId: String? = null, // ID du bin jsonbin.io pour le partage en ligne
        val exam: Boolean = false, // Indique si l'animal a été créé en mode examen
        val examStudentId: String? = null, // Identifiant de l'étudiant
        val examStudentNumber: String? = null, // Numéro de l'étudiant
        val examExerciseId: String? = null, // ID de l'exercice
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
        val Poids: Double = 0.0,
        val PoidsIdeal: Double = 0.0,
        val PoidsIdealex: Boolean = false,
        val Boisson: Double = 0.0,
        val TauxMG: Double = 20.0,
        val suivi: Boolean = false,
        val bcs: String = "",
        val MCS: Int = 3,
        val k1value: Double = 1.0,
        val k2value: Double = 1.0,
        val k3value: Double = 1.0,
        val k4value: Double = 1.0,
        val k5value: Double = 1.0,
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
        val previousBE: Double = 0.0,
        val previousRation: RationJson? = null,
        val ky: Double = 0.0,
        val newBE: Double = 0.0,
        val newRation: List<RationJson> = listOf(),
        val keywords: List<String> = listOf(),
        // Ordonnance: état sauvegardé par consultation
        val prescriptionAdditionalText: String = "",
        val prescriptionSelectedConseilIds: List<String> = listOf(),
        val prescriptionLocalHtmlSections: List<HtmlSection> = listOf(),
        val prescriptionSelectedRationIds: List<String> = listOf()
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
        val quantite: Double,
        val prop: Double,
        val alime: AlimentEvJson,
        val weight: Double = 1.0,
        val categ: Int = 0,
        val density: Double = 0.0
)

// Structures de support

@Serializable data class WeightDateJson(val UUID: String, val date: LocalDate, val value: Double)

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
        val value: Double,
        val unit: String,
        val percentCompletion: Double,
        val pas: Double
)

@Serializable data class SupplementalvariablePJson(val variable: String, val value: Double)

@Serializable
data class NutrientQuantity(val value: Double, val nut: String) {
        /**
         * Obtient l'unité du nutriment
         *
         * @return L'unité sous forme de String
         */
        val unit: String
                get() = nut

        /** Propriété pour rendre compatible avec les références à quantity */
        val quantity: Double
                get() = value

        /**
         * Multiplie cette quantité par un facteur
         *
         * @param factor Le facteur de multiplication
         * @return La nouvelle quantité résultante
         */
        fun times(factor: Double): NutrientQuantity {
                return NutrientQuantity(value * factor, nut)
        }

        /**
         * Additionne cette quantité avec une autre
         *
         * @param other L'autre quantité à additionner
         * @return La somme des deux quantités
         */
        fun plus(other: NutrientQuantity): NutrientQuantity? {
                if (this.nut != other.nut) {
                        return null
                }
                return NutrientQuantity(value + other.value, nut)
        }
}
