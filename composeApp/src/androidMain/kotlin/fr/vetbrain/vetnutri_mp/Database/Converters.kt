package fr.vetbrain.vetnutri_mp.Database

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun fromList(value: List<*>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toList(value: String): List<*> {
        return Json.decodeFromString(value)
    }
}
