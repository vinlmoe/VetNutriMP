package fr.vetbrain.vetnutri_mp.Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@Entity(tableName = "Food", foreignKeys = [
    ForeignKey(entity = Ration::class, parentColumns = ["uuid"], childColumns = ["refRation"], onDelete = ForeignKey.CASCADE)
])
@Serializable
data class AlimentRation @OptIn(ExperimentalUuidApi::class) constructor(
    @PrimaryKey val uuid: String =  Uuid.random().toString(), // Unique ID for each ration item
    @ColumnInfo(name = "refAlimUnif") val refAlimUnif: String, // Reference to the base AlimentEv
    @ColumnInfo(name = "refRation") var refRation: String? = null, // Foreign key to Ration
    val quantity: Float?,
    @ColumnInfo(name = "refTarget") val refTarget: Int?, // Using Int to store enum coef
    @Ignore  var alim: AlimentEv? = null // Transient and custom serialized for AlimentEv
)  {
    fun upUUID(rationUUID: String) {
        refRation = rationUUID
    }
}


