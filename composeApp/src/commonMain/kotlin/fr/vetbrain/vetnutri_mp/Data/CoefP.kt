package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Utils.genUUID
import kotlin.uuid.*
import kotlinx.serialization.Serializable

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class CoefP(
        val uuid: String = genUUID(),
        var description: String?,
        var coef: Double?,
        var groupUUID: Int?
)
