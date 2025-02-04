package fr.vetbrain.vetnutri_mp.Data

import kotlinx.serialization.Serializable

@Serializable
data class AnimP(
    val uuid: String,
    var name: String?,
    var breed: String?,
    var race: String?,
    var ownerName: String?,
    var id: String?,
    var specie: String?
) 