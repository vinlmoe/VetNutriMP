@file:OptIn(ExperimentalUuidApi::class)

package fr.vetbrain.vetnutri_mp.Data

import androidx.room.*
import fr.vetbrain.vetnutri_mp.Enumer.TargetAdjust
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import kotlinx.serialization.Serializable
import kotlin.uuid.*

@Entity(tableName = "TargetMethod")
@Serializable
data class TargetDefinitionEv(
    @PrimaryKey val uuid: String = Uuid.random().toString(),
    @ColumnInfo(name = "refMethod") var refMethod: String?,
    var ord: Int?,
    var kind: Int?,
    var varue: Float?,
    var unit: Int?,
    var percent: Float?,
    var measure: Float?,
    @Ignore var targ: TargetAdjust? = null,
    @Ignore var ure: UnitReqEnum? = null
) 