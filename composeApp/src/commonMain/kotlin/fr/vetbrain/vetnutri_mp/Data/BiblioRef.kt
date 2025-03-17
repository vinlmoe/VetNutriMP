package fr.vetbrain.vetnutri_mp.Data

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

/** Classe représentant une référence bibliographique */
@OptIn(ExperimentalUuidApi::class)
@Serializable
data class BiblioRef(
        val uuid: String = Uuid.random().toString(),
        var firstAuthor: String? = null,
        var year: String? = null,
        var completeRef: String? = null,
        var comments: String? = null,
        var consistent: Int? = null
)
