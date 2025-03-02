package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
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
    fun fromFloat(value: Float?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toFloat(value: String?): Float? {
        return value?.toFloatOrNull()
    }

    @TypeConverter
    fun fromDouble(value: Double?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toDouble(value: String?): Double? {
        return value?.toDoubleOrNull()
    }
}
