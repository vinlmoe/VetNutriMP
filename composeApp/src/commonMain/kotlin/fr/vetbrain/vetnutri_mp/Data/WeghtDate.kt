package fr.vetbrain.vetnutri_mp.Data


import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@ExperimentalUuidApi
@Serializable
data class WeightDate(
    val UUID: String = Uuid.random().toString(), // Initialisation de l'UUID par défaut
    var date: LocalDate,
    var value: Float,
    var variation: Float = 0f,
    var variationp: Float = 0f
) {
    constructor(d: LocalDate, v: Float) : this(date = d, value = v)
    constructor(uuid: String, d: LocalDate, v: Float) : this(UUID = uuid, date = d, value = v)
}