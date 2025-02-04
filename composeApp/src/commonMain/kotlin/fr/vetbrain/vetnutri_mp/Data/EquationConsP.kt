package fr.vetbrain.vetnutri_mp.Data

import androidx.room.*
import kotlinx.serialization.Serializable

@Serializable
data class EquationConsP(
    @Ignore @Serializable(with = EquationSerializer::class) var equation: Equation? = null
) 