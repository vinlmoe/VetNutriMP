package fr.vetbrain.vetnutri_mp.Data

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalUuidApi::class)
data class WeightDate(
        var uuid: String = Uuid.random().toString(),
        var refAnimal: String,
        var date: LocalDate,
        var value: Double
)
