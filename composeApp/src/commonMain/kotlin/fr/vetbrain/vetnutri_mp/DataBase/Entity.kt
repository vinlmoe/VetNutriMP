package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.*
import kotlinx.serialization.Serializable

// Entités pour la base de données des aliments
@Serializable
@Entity(tableName = "FOOD")
data class FoodEntity(
        @PrimaryKey val UUID: String,
        val groupAlim: Int,
        val typeAlim: Int,
        val ingredients: String,
        val price: Double,
        val categPrice: String,
        val brand: String,
        val gamme: String,
        val unitPres: Int,
        val quantityPres: Float,
        val version: Int,
        val date: String,
        val nameDef: String,
        val consistent: Int,
        val deprecated: Int,
        val DataB: String,
        val RefRation: String? = null,
        val quantity: Float? = null,
        val RefAlimUnif: String? = null,
        val refTarget: Int? = null
)

@Serializable
@Entity(tableName = "NAMEFOOD")
data class NameFoodEntity(@PrimaryKey val refFood: String, val lang: String, val value: String)

@Serializable
@Entity(tableName = "ESPECE")
data class EspeceEntity(@PrimaryKey val refFood: String, val value: String)

@Serializable
@Entity(tableName = "VALUEAA")
data class ValueAAEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val kind: Int,
        val refFood: String,
        val version: Int,
        val value: Float,
        val date: String
)

@Serializable
@Entity(tableName = "VALUEBASE")
data class ValueBaseEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val kind: Int,
        val refFood: String,
        val version: Int,
        val value: Float,
        val date: String
)

@Serializable
@Entity(tableName = "VALUELIPID")
data class ValueLipidEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val kind: Int,
        val refFood: String,
        val version: Int,
        val value: Float,
        val date: String
)

@Serializable
@Entity(tableName = "VALUEMACRO")
data class ValueMacroEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val kind: Int,
        val refFood: String,
        val version: Int,
        val value: Float,
        val date: String
)

@Serializable
@Entity(tableName = "VALUEMIN")
data class ValueMinEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val kind: Int,
        val refFood: String,
        val version: Int,
        val value: Float,
        val date: String
)

@Serializable
@Entity(tableName = "VALUEVITAM")
data class ValueVitamEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val kind: Int,
        val refFood: String,
        val version: Int,
        val value: Float,
        val date: String
)

@Serializable
@Entity(tableName = "VALUEOTHER")
data class ValueOtherEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val kind: Int,
        val refFood: String,
        val version: Int,
        val value: Float,
        val date: String
)

@Serializable
@Entity(tableName = "INDICATION")
data class IndicationEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val refFood: String,
        val value: Int
)

@Serializable
@Entity(tableName = "dataDef")
data class DataDefEntity(@PrimaryKey val UUID: String, val sNAME: String, val compNAME: String)

// Entités pour la base de données des animaux
@Serializable
@Entity(tableName = "ANIMALS")
data class AnimalEntity(
        @PrimaryKey val UUID: String,
        val name: String,
        val dead: Int,
        val id: String,
        val sex: Int,
        val specie: String,
        val ownerName: String,
        val birthdate: String,
        val race: String,
        val summary: String
)

@Serializable
@Entity(tableName = "CONSULTATIONS")
data class ConsultationEntity(
        @PrimaryKey val UUID: String,
        val date: String,
        val subject: String?,
        val observation: String?,
        val cRendu: String,
        val weight: Float,
        val idealWeight: Float,
        val water: Float,
        val bodyFat: Float,
        val methodAnalysis: String?,
        val BCS: Int,
        val k1Id: String?,
        val k1Value: Float,
        val k2Id: String?,
        val k2Value: Float,
        val k3Id: String?,
        val k3Value: Float,
        val k4Id: String?,
        val k4Value: Float,
        val k5Id: String?,
        val k5Value: Float,
        val nLittle: Int,
        val pAdult: Float,
        val coefGes: Int,
        val coefLact: Int,
        val idAnim: String,
        val MCS: Int
)

@Serializable
@Entity(tableName = "Weight")
data class WeightEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val refAnimal: String,
        val date: String,
        val value: Float,
        val UUID: String
)

@Serializable
@Entity(tableName = "RATION")
data class RationEntity(
        @PrimaryKey val UUID: String,
        val idConsult: String?,
        val name: String,
        val coef: Float,
        val actual: Int,
        val number: Int,
        val espece: String?,
        val recette: Int,
        val description: String?
)

@Serializable
@Entity(tableName = "SupVar")
data class SupVarEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val idCons: String,
        val idVar: Int,
        val value: Float
)

@Serializable
@Entity(tableName = "ReferenceDisease")
data class ReferenceDiseaseEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val idCons: String,
        val refRef: String
)

@Serializable
@Entity(tableName = "Breed")
data class BreedEntity(@PrimaryKey val ID: String, val refSpecie: Int)

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
data class EspecesEntity(@PrimaryKey val ID: String, val category: String, val value: String)

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
        @PrimaryKey val UUID: String,
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
        @PrimaryKey val UUID: String,
        val fAuthor: String,
        val year: String,
        val fullRef: String,
        val comments: String,
        val consistent: Int
)

@Serializable
@Entity(tableName = "method")
data class MethodEntity(
        @PrimaryKey val UUID: String,
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
        val value: Float,
        val unit: Int,
        val percent: Float,
        val measure: Float
)

@Serializable
@Entity(tableName = "dataRef")
data class DataRefEntity(
        @PrimaryKey val UUID: String,
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
@Entity(tableName = "coef")
data class CoefEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val coefName: String,
        val value: Float,
        val groupUUID: Int,
        val refRef: String
)

@Serializable
@Entity(tableName = "speReqEq", primaryKeys = ["refEq", "refRef"])
data class SpeReqEqEntity(val refEq: String, val refRef: String)
