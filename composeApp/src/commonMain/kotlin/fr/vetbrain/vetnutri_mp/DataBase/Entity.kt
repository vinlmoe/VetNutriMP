package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.*
import kotlinx.serialization.Serializable

// Entités pour la base de données des aliments
@Serializable
@Entity(
        tableName = "FOOD",
        foreignKeys =
                [
                        ForeignKey(
                                entity = RationEntity::class,
                                parentColumns = ["uuid"],
                                childColumns = ["RefRation"],
                                onDelete = ForeignKey.SET_NULL
                        )],
        indices =
                [
                        Index("RefRation"),
                        Index(value = ["uuid"], unique = true),
                        Index("groupAlim"), // Pour le filtrage par groupe
                        Index("typeAlim"), // Pour le filtrage par type
                        Index("brand"), // Pour la recherche par marque
                        Index("name"), // Pour la recherche par nom
                        Index("especesJson"), // Pour le filtrage par espèce
                        Index("deprecated") // Pour filtrer les aliments obsolètes
                ]
)
data class FoodEntity(
        @PrimaryKey val uuid: String,
        val groupAlim: Int,
        val typeAlim: Int,
        val ingredients: String,
        val price: Double,
        val categPrice: String,
        val brand: String,
        val gamme: String,
        val cont: String,
        val unitPres: Int,
        val quantityPres: Double,
        val version: Int,
        val date: String,
        val nameDef: String,
        val consistent: Int,
        val deprecated: Int,
        val DataB: String,
        val RefRation: String? = null,
        val RefAlimUnif: String? = null,
        val especesJson: String? = null,
        val indicationsJson: String? = null,
        val name: String? = null,
        val quantite: Double = 0.0
)

@Serializable
@Entity(tableName = "NAMEFOOD")
data class NameFoodEntity(@PrimaryKey val refFood: String, val lang: String, val value: String)

@Serializable
@Entity(tableName = "ESPECE")
data class EspeceEntity(@PrimaryKey val refFood: String, val value: String)

@Serializable
@Entity(tableName = "INDICATION", primaryKeys = ["refFood", "value"])
data class IndicationEntity(val refFood: String, val value: Int = 0)

@Serializable
@Entity(tableName = "dataDef")
data class DataDefEntity(@PrimaryKey val uuid: String, val sNAME: String, val compNAME: String)

// Entités pour la base de données des animaux
@Serializable
@Entity(tableName = "ANIMALS")
data class AnimalEntity(
        @PrimaryKey val uuid: String,
        val nom: String?,
        val dead: Boolean = false,
        val id: String?,
        val sexId: Int = 0,
        val specieId: String?,
        val ownerName: String?,
        val birthdate: String?,
        val race: String?,
        val summary: String?
)

@Serializable
@Entity(
        tableName = "CONSULTATIONS",
        foreignKeys =
                [
                        ForeignKey(
                                entity = AnimalEntity::class,
                                parentColumns = ["uuid"],
                                childColumns = ["idAnim"],
                                onDelete = ForeignKey.CASCADE
                        )],
        indices = [Index("idAnim")]
)
data class ConsultationEntity(
        @PrimaryKey val uuid: String,
        val idAnim: String,
        val date: String?,
        val objectConsult: String?,
        val observation: String?,
        val cRendu: String?,
        val weight: Double = 0.0,
        val idealWeight: Double = 0.0,
        val water: Double = 0.0,
        val bodyFat: Double = 0.0,
        val methodAnalysis: String?,
        val BCS: Int = 0,
        val k1Id: String?,
        val k1Value: Double = 0.0,
        val k2Id: String?,
        val k2Value: Double = 0.0,
        val k3Id: String?,
        val k3Value: Double = 0.0,
        val k4Id: String?,
        val k4Value: Double = 0.0,
        val k5Id: String?,
        val k5Value: Double = 0.0,
        val nLittle: Int = 0,
        val pAdult: Double = 0.0,
        val coefGes: Int = 0,
        val coefLact: Int = 0,
        val MCS: Int = 0,
        val referenceGeneraleId: String? = null,
        val referencesMaladiesJson: String? = null,
        val coefficientAjustement: Double = 1.0
)

@Serializable
@Entity(
        tableName = "WEIGHT",
        foreignKeys =
                [
                        ForeignKey(
                                entity = AnimalEntity::class,
                                parentColumns = ["uuid"],
                                childColumns = ["refAnimal"],
                                onDelete = ForeignKey.CASCADE
                        )],
        indices = [Index("refAnimal")]
)
data class WeightEntity(
        @PrimaryKey val uuid: String,
        val refAnimal: String,
        val date: String,
        val value: Double
)

@Serializable
@Entity(
        tableName = "RATIONS",
        foreignKeys =
                [
                        ForeignKey(
                                entity = ConsultationEntity::class,
                                parentColumns = ["uuid"],
                                childColumns = ["idConsult"],
                                onDelete = ForeignKey.CASCADE
                        )],
        indices = [Index("idConsult")]
)
data class RationEntity(
        @PrimaryKey val uuid: String,
        var idConsult: String,
        val name: String?,
        val coef: Double = 0.0,
        val actual: Boolean = false,
        val number: Int = 0,
        val espece: String?,
        val recette: Boolean = false,
        val description: String?
)

@Serializable
@Entity(
        tableName = "ALIMENTS",
        foreignKeys =
                [
                        ForeignKey(
                                entity = RationEntity::class,
                                parentColumns = ["uuid"],
                                childColumns = ["refRation"],
                                onDelete = ForeignKey.CASCADE
                        ),
                        ForeignKey(
                                entity = FoodEntity::class,
                                parentColumns = ["uuid"],
                                childColumns = ["refAlimUnif"],
                                onDelete = ForeignKey.SET_NULL
                        )],
        indices =
                [
                        Index("refRation"),
                        Index("refAlimUnif"),
                        Index(value = ["refAlimUnif", "refRation"], unique = true)]
)
data class AlimentRationEntity(
        @PrimaryKey val uuid: String,
        val refAlimUnif: String = "",
        val refRation: String,
        val quantity: Double = 0.0,
        val refTarget: Int = 0
)

@Serializable
@Entity(tableName = "RECETTES")
data class RecetteEntity(
        @PrimaryKey val uuid: String,
        val name: String?,
        val number: Int = 0,
        val espece: String?,
        val description: String?
)

@Serializable
@Entity(
        tableName = "ALIMENTS_RECETTES",
        foreignKeys =
                [
                        ForeignKey(
                                entity = RecetteEntity::class,
                                parentColumns = ["uuid"],
                                childColumns = ["refRecipe"],
                                onDelete = ForeignKey.CASCADE
                        ),
                        ForeignKey(
                                entity = FoodEntity::class,
                                parentColumns = ["uuid"],
                                childColumns = ["refAlimUnif"],
                                onDelete = ForeignKey.SET_NULL
                        )],
        indices =
                [
                        Index("refRecipe"),
                        Index("refAlimUnif"),
                        Index(value = ["refAlimUnif", "refRecipe"], unique = true)]
)
data class AlimentRecetteEntity(
        @PrimaryKey val uuid: String,
        val refAlimUnif: String = "",
        val refRecipe: String,
        val quantity: Double = 0.0,
        val refTarget: Int = 0
)

@Serializable
@Entity(
        tableName = "ESPECES_ALIMENTS",
        foreignKeys =
                [
                        ForeignKey(
                                entity = FoodEntity::class,
                                parentColumns = ["uuid"],
                                childColumns = ["refAliment"],
                                onDelete = ForeignKey.CASCADE
                        )],
        indices = [Index("refAliment")]
)
data class EspeceAlimentEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val refAliment: String,
        val espece: String
)

@Serializable
@Entity(
        tableName = "INDICATIONS_ALIMENTS",
        foreignKeys =
                [
                        ForeignKey(
                                entity = FoodEntity::class,
                                parentColumns = ["uuid"],
                                childColumns = ["refAliment"],
                                onDelete = ForeignKey.CASCADE
                        )],
        indices = [Index("refAliment")],
        primaryKeys = ["refAliment", "indication"]
)
data class IndicationAlimentEntity(val refAliment: String, val indication: Int)

@Serializable
@Entity(
        tableName = "SUPPLEMENTAL_VARIABLES",
        foreignKeys =
                [
                        ForeignKey(
                                entity = ConsultationEntity::class,
                                parentColumns = ["uuid"],
                                childColumns = ["idConsult"],
                                onDelete = ForeignKey.CASCADE
                        )],
        indices = [Index("idConsult")],
        primaryKeys = ["idConsult", "variableKind"]
)
data class SupplementalVariableEntity(
        val idConsult: String,
        val variableKind: Int,
        val value: Double = 0.0
)

@Serializable
@Entity(tableName = "ReferenceDisease", primaryKeys = ["idCons", "refRef"])
data class ReferenceDiseaseEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val idCons: String,
        val refRef: String
)
/*
@Serializable
@Entity(tableName = "Breed")
data class BreedEntity(@PrimaryKey val id: String, val refSpecie: Int)

@Serializable
@Entity(tableName = "breedName")
data class BreedNameEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val refBreed: String,
        val lang: String,
        val value: String
)

@Serializable
@Entity(tableName = "Especes")
data class EspecesEntity(@PrimaryKey val id: String, val category: String, val value: String)

@Serializable
@Entity(tableName = "EspeceName")
data class EspeceNameEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        @ColumnInfo(name = "ref_espece") val refEspece: String,
        @ColumnInfo(name = "lang") val lang: String,
        @ColumnInfo(name = "value") val value: String
)

// Entités pour la base de données des références
@Serializable
@Entity(tableName = "equation")
data class EquationEntity(
        @PrimaryKey val uuid: String,
        val script: String,
        val refBiblio: String,
        val name: String,
        val description: String,
        val speciesRef: String,
        val kind: Int,
        val consistent: Int,
        val nutrient: Int
)

@Serializable
@Entity(tableName = "SupplementVariable")
data class SupplementVariableEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val refEquation: String,
        val VariableKind: Int
)

@Serializable
@Entity(tableName = "Biblio")
data class BiblioEntity(
        @PrimaryKey val uuid: String,
        val fAuthor: String,
        val year: String,
        val fullRef: String,
        val comments: String,
        val consistent: Int
)

@Serializable
@Entity(tableName = "method")
data class MethodEntity(
        @PrimaryKey val uuid: String,
        val name: String,
        val species: String,
        val description: String
)

@Serializable
@Entity(tableName = "targetMethod")
data class TargetMethodEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val refMethod: String,
        val ord: Int,
        val kind: Int,
        val value: Double,
        val unit: Int,
        val percent: Double,
        val measure: Double
)

@Serializable
@Entity(tableName = "dataRef")
data class DataRefEntity(
        @PrimaryKey val uuid: String,
        val name: String,
        val description: String,
        val disease: Int,
        val BWeqRef: String?,
        val SERName: String?,
        val SERRef: String?,
        val DEcomRef: String?,
        val DErawRef: String?,
        val k1Name: String?,
        val k1Ref: String?,
        val k2Name: String?,
        val k2Ref: String?,
        val k3Name: String?,
        val k3Ref: String?,
        val k4Name: String?,
        val k4Ref: String?,
        val k5Name: String?,
        val k5Ref: String?,
        val specie: String?,
        val consistent: Int
)

@Serializable
@Entity(
        tableName = "coef",
        foreignKeys =
                [
                        ForeignKey(
                                entity = BiblioRefEntity::class,
                                parentColumns = ["uuid"],
                                childColumns = ["refRef"],
                                onDelete = ForeignKey.CASCADE
                        )],
        indices = [Index("refRef")]
)
data class CoefEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val coefName: String,
        val value: Double,
        val groupUUID: Int,
        val refRef: String
)

@Serializable
@Entity(tableName = "speReqEq", primaryKeys = ["refEq", "refRef"])
data class SpeReqEqEntity(val refEq: String, val refRef: String)
*/
@Serializable
@Entity(
        tableName = "ALIMENT_REFERENCES",
        foreignKeys =
                [
                        ForeignKey(
                                entity = FoodEntity::class,
                                parentColumns = ["uuid"],
                                childColumns = ["foodId"],
                                onDelete = ForeignKey.CASCADE
                        )],
        indices = [Index("foodId")]
)
data class AlimentReferenceEntity(
        @PrimaryKey val uuid: String,
        val foodId: String,
        val referenceType: String,
        val referenceValue: String,
        val version: Int,
        val date: String
)

@Serializable
@Entity(
        tableName = "NUTRIENT_VALUES",
        foreignKeys =
                [
                        ForeignKey(
                                entity = FoodEntity::class,
                                parentColumns = ["uuid"],
                                childColumns = ["refAliment"]
                        )],
        indices = [Index("refAliment")],
        primaryKeys = ["refAliment", "nutrientLabel"]
)
data class NutrientValueEntity(
        val refAliment: String,
        val nutrientLabel: String,
        val value: Double
)

/** Entité pour la table des références bibliographiques */
@Serializable
@Entity(tableName = "BIBLIO_REFS")
data class BiblioRefEntity(
        @PrimaryKey val uuid: String,
        val firstAuthor: String,
        val year: Int,
        val completeRef: String,
        val comments: String,
        val bibtex: String,
        val consistent: Int
)

@Serializable
@Entity(
        tableName = "EQUATIONS",
        foreignKeys =
                [
                        ForeignKey(
                                entity = BiblioRefEntity::class,
                                parentColumns = ["uuid"],
                                childColumns = ["bibRef"],
                                onDelete = ForeignKey.SET_NULL
                        )],
        indices = [Index("bibRef"), Index(value = ["uuid"], unique = true)]
)
data class EquationEntity(
        @PrimaryKey val uuid: String,
        val name: String,
        val description: String,
        val equationScript: String,
        val specie: String?,
        val kind: String,
        val consistent: Boolean,
        val bibRef: String?,
        val variables: String, // Stocké en JSON
        val nutrient: String?, // Label du nutriment associé
        val ratio: Boolean
)

@Serializable
@Entity(tableName = "REFERENCE_EV", indices = [Index(value = ["uuid"], unique = true)])
data class ReferenceEvEntity(
        @PrimaryKey val uuid: String,
        val nom: String,
        val description: String,
        val maladie: Boolean,
        val nomMaladie: String,
        val nomEnergie: String,
        val consistent: Int,
        val espece: String,
        val stadePhysio: String,
        val nomk1: String,
        val nomk2: String,
        val nomk3: String,
        val nomk4: String,
        val nomk5: String
)

/** Entité pour les relations entre ReferenceEv et Equations */
@Serializable
@Entity(
        tableName = "REFERENCE_EV_EQUATIONS",
        foreignKeys =
                [
                        ForeignKey(
                                entity = ReferenceEvEntity::class,
                                parentColumns = ["uuid"],
                                childColumns = ["referenceEvId"],
                                onDelete = ForeignKey.CASCADE
                        ),
                        ForeignKey(
                                entity = EquationEntity::class,
                                parentColumns = ["uuid"],
                                childColumns = ["equationId"],
                                onDelete = ForeignKey.CASCADE
                        )],
        indices = [Index("referenceEvId"), Index("equationId")],
        primaryKeys = ["referenceEvId", "equationType"]
)
data class ReferenceEvEquationEntity(
        val referenceEvId: String,
        val equationId: String,
        val equationType: String // "BW", "BEE", "DEcom", "DEraw", "ME"
)

/** Entité pour les coefficients de ReferenceEv */
@Serializable
@Entity(
        tableName = "REFERENCE_EV_COEFFICIENTS",
        foreignKeys =
                [
                        ForeignKey(
                                entity = ReferenceEvEntity::class,
                                parentColumns = ["uuid"],
                                childColumns = ["referenceEvId"],
                                onDelete = ForeignKey.CASCADE
                        )],
        indices = [Index("referenceEvId")]
)
data class ReferenceEvCoefficientEntity(
        @PrimaryKey val uuid: String,
        val referenceEvId: String,
        val groupType: String, // "k1", "k2", "k3", "k4", "k5"
        val description: String,
        val coef: Double,
        val groupUUID: Int
)

/** Entité pour les nutriments de ReferenceEv avec références bibliographiques */
@Serializable
@Entity(
        tableName = "REFERENCE_EV_NUTRIENTS",
        foreignKeys =
                [
                        ForeignKey(
                                entity = ReferenceEvEntity::class,
                                parentColumns = ["uuid"],
                                childColumns = ["referenceEvId"],
                                onDelete = ForeignKey.CASCADE
                        ),
                        ForeignKey(
                                entity = BiblioRefEntity::class,
                                parentColumns = ["uuid"],
                                childColumns = ["biblioRefId"],
                                onDelete = ForeignKey.SET_NULL
                        )],
        indices = [Index("referenceEvId"), Index("biblioRefId")]
)
data class ReferenceEvNutrientEntity(
        @PrimaryKey val uuid: String,
        val referenceEvId: String,
        val nutrientCode: String,
        val reflevel: String, // "MIN", "MAX", "OPTIMIN", "OPTIMAX"
        val quantite: Double,
        val uniteId: Int,
        val uniteReqId: Int,
        val biblioRefId: String?
)
