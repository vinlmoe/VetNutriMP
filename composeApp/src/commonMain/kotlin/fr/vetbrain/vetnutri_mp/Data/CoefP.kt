package fr.vetbrain.vetnutri_mp.Data

import kotlinx.serialization.Serializable
import kotlin.uuid.*
@OptIn(ExperimentalUuidApi::class)
@Serializable
data class CoefP(
    val uuid: String = Uuid.random().toString(),
    var description: String?,
    var coef: Float?,
    var groupUUID: Int?
) 