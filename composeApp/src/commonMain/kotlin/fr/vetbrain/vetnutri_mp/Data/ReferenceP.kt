package fr.vetbrain.vetnutri_mp.Data

import androidx.room.*
import kotlinx.serialization.Serializable

@Serializable
data class ReferenceP(
    @Ignore @Serializable(with = ReferenceEvSerializer::class) var reference: ReferenceEv? = null
) 