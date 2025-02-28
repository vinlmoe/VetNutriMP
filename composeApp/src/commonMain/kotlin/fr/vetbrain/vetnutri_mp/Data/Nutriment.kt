package fr.vetbrain.vetnutri_mp.Data

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

@Serializable
@OptIn(ExperimentalUuidApi::class)
data class Nutriment(
        var uuid: String = Uuid.random().toString(),
        var name: String = "",
        var code: String = "",
        var unite: String = "",
        var description: String? = null
)
