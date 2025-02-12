package fr.vetbrain.vetnutri_mp.Data

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class WeightDate(
        val uuid: String =Uuid.random().toString(),
        var refAnimal: String,
        var date: LocalDate,
        var value: Float
)
