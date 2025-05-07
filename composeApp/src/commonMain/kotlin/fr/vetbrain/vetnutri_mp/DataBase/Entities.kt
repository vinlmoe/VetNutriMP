package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.*
import kotlinx.serialization.Serializable

/** Entité pour stocker les références nutritionnelles en base de données */
@Serializable
@Entity(tableName = "REFERENCE_EV")
data class ReferenceEvEntity(
        @PrimaryKey val uuid: String,
        val nom: String,
        val description: String,
        val maladie: Boolean = false,
        val nomMaladie: String = "",
        val nomEnergie: String = "",
        val consistent: Int = 0,
        val espece: String,
        val stadePhysio: String,

        // Noms des coefficients modificateurs
        val nomk1: String = "",
        val nomk2: String = "",
        val nomk3: String = "",
        val nomk4: String = "",
        val nomk5: String = "",

        // Références aux équations (UUIDs)
        val equationBW: String? = null,
        val equationBEE: String? = null,
        val equationDEcom: String? = null,
        val equationDEraw: String? = null,
        val equationME: String? = null
)

/** Entité pour stocker les coefficients modificateurs des références */
@Serializable
@Entity(
        tableName = "COEFFICIENTS",
        foreignKeys =
                [
                        ForeignKey(
                                entity = ReferenceEvEntity::class,
                                parentColumns = ["uuid"],
                                childColumns = ["referenceId"],
                                onDelete = ForeignKey.CASCADE
                        )],
        indices = [Index(value = ["referenceId", "groupUUID"], unique = false)]
)
data class CoefficientEntity(
        @PrimaryKey val uuid: String,
        val referenceId: String,
        val groupUUID: Int, // 0-4 pour les groupes modk1-modk5
        val description: String,
        val coef: Float
)

/** Entité pour stocker les valeurs nutritionnelles des références */
@Serializable
@Entity(
        tableName = "NUTRIENT_REFERENCES",
        foreignKeys =
                [
                        ForeignKey(
                                entity = ReferenceEvEntity::class,
                                parentColumns = ["uuid"],
                                childColumns = ["referenceId"],
                                onDelete = ForeignKey.CASCADE
                        ),
                        ForeignKey(
                                entity = BiblioRefEntity::class,
                                parentColumns = ["uuid"],
                                childColumns = ["biblioRefId"],
                                onDelete = ForeignKey.SET_NULL
                        )],
        indices =
                [
                        Index(value = ["referenceId", "nutrient", "niveauRef"], unique = true),
                        Index(value = ["biblioRefId"])]
)
data class NutrientReferenceEntity(
        @PrimaryKey val uuid: String,
        val referenceId: String,
        val nutrient: String, // Nom de l'énumération Nutrient
        val niveauRef: String, // Nom de l'énumération Reflevel
        val quantite: Float,
        val unite: String, // Nom de l'énumération UnitEnum
        val uniteReq: String, // Nom de l'énumération UnitReqEnum
        val biblioRefId: String?
)
