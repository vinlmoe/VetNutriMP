package fr.vetbrain.vetnutri_mp.Data

import androidx.room.*
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import kotlinx.serialization.Serializable

@Serializable
data class SupplementalvariableP(
    @Ignore var variable: VariableKind? = null,
    var varue: Float?
) 