package fr.vetbrain.vetnutri_mp.Data

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class AlimentRation(
        val uuid: String = Uuid.random().toString(),
        val uuidUnif: String = "",
        val quantity: Float = 0f,
        val proportion: Float = 0f,
        var aliment: AlimentEv? = null,
        val weight: Float = 1f,
        val category: Int = 0,
        val density: Double = 0.0,
        val refAlimUnif: String? = null,
        var refRation: String? = null,
        val refTarget: Int? = null
)
