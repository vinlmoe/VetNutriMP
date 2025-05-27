package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime



fun  instantNow(): Instant{
    val clock: Clock = Clock.System
   return (clock.now())
}
fun today(): LocalDateTime {
    return instantNow().toLocalDateTime(TimeZone.currentSystemDefault())
}