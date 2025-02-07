package fr.vetbrain.vetnutri_mp.Data

import androidx.room.*
import kotlinx.serialization.Serializable
import fr.vetbrain.vetnutri_mp.*

@Serializable
data class AlimP(
    val uuid: String,
    var nameDef: String?,
    var brand: String?,
    var gamme: String?,
    var price: Double?,
    @Ignore  var alimentEv: AlimentEv? = null
)