package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Utils.genUUID
import kotlinx.serialization.Serializable
import kotlin.uuid.*
@OptIn(ExperimentalUuidApi::class)
@Serializable
data class CoefP(
    val uuid: String = genUUID(),
    var description: String?,
    var coef: Float?,
    var groupUUID: Int?
) 