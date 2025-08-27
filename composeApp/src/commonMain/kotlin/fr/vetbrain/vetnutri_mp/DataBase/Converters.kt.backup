package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { dateStr ->
            if (dateStr.isBlank()) {
                null
            } else {
                try {
                    LocalDate.parse(dateStr)
                } catch (e: Exception) {
                    println(
                            "⚠️ WARNING: Impossible de parser la date '$dateStr' dans le convertisseur Room: ${e.message}"
                    )
                    null
                }
            }
        }
    }

    @TypeConverter
    fun fromBoolean(value: Boolean?): Int? {
        return value?.let { if (it) 1 else 0 }
    }

    @TypeConverter
    fun toBoolean(value: Int?): Boolean? {
        return value?.let { it == 1 }
    }

    @TypeConverter
    fun fromDouble(value: Double?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toDouble(value: String?): Double? {
        return value?.toDoubleOrNull()
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let { Json.decodeFromString<List<String>>(it) }
    }
}
