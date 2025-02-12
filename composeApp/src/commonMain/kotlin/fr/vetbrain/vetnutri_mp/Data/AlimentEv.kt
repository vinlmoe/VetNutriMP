package fr.vetbrain.vetnutri_mp.Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(tableName = "Food")
@Serializable
data class AlimentEv
@OptIn(ExperimentalUuidApi::class)
constructor(
        @PrimaryKey val uuid: String = Uuid.random().toString(),
        @ColumnInfo(name = "groupAlim") val group: GroupAlim?,
        @ColumnInfo(name = "typeAlim") val typeAliment: FoodKind?,
        val ingredients: String?,
        val price: Double?,
        val categPrice: String?,
        val brand: String?,
        val gamme: String?,
        @ColumnInfo(name = "nameDef") val nom: String?,
        val consistent: Boolean, // Consider Consistent enum if applicable
        @ColumnInfo(name = "unitPres") val cont: Int?, // Consider UnitPres enum if applicable
        @ColumnInfo(name = "quantityPres") val quantInt: Float?,
        val deprecated: Int?,
        @ColumnInfo(name = "DataB") val dataB: String?,
        @Ignore var especes: MutableList<String>, // Transient, loaded separately
        @Ignore var indicat: MutableList<AlimIndic> // Transient, loaded separately
)
