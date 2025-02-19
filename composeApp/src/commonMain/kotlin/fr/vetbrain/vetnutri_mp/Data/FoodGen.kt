package fr.vetbrain.vetnutri_mp.Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class AlimentRation (
    var uuid: String = Uuid.random().toString(),
    var refAlimUnif: String? = null,
    var refRation: String? = null,
    var quantity: Float? = null,
    var refTarget: Int? = null,
    var alim: AlimentEv? = null
) {
    fun upUUID(rationUUID: String) {
        refRation = rationUUID
    }
}


