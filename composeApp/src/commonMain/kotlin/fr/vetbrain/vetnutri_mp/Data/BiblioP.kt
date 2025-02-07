package fr.vetbrain.vetnutri_mp.Data

import androidx.room.*
import kotlinx.serialization.Serializable

@Serializable
data class BiblioP(
    var biblio: BiblioRef? = null
) 