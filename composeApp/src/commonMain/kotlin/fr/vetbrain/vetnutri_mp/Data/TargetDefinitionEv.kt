package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.TargetAdjust
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class TargetDefinitionEv(
        val uuid: String = Uuid.random().toString(),
        var refMethod: String?,
        var ord: Int?,
        var kind: Int?,
        var varue: Float?,
        var unit: Int?,
        var percent: Float?,
        var measure: Float?,
        var targ: TargetAdjust? = null,
        var ure: UnitReqEnum? = null
)
