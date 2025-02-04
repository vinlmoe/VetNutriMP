package fr.vetbrain.vetnutri_mp.Data

import androidx.room.*
import kotlinx.serialization.Serializable

@Serializable
data class AlimP(
    val uuid: String,
    var nameDef: String?,
    var brand: String?,
    var gamme: String?,
    var price: Float?,
    @Ignore @Serializable(with = AlimentEvSerializer::class) var alimentEv: AlimentEv? = null
) {
    constructor(alimentEv: AlimentEv) : this(
        alimentEv.uuid,
        alimentEv.nom,
        alimentEv.brand,
        alimentEv.gamme,
        alimentEv.price,
        alimentEv
    )
    constructor(uuid: String, nameDef: String?, brand: String?, gamme: String?, price: Double?) : this(
        uuid,
        nameDef,
        brand,
        gamme,
        price?.toFloat(),
        null
    )
} 