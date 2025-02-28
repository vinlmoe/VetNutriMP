package fr.vetbrain.vetnutri_mp.Enumer

enum class Language(val code: String) {
    FRANCAIS("fr"),
    ENGLISH("en"),
    DEUTSCH("de"),
    ESPANOL("es"),
    ITALIANO("it"),
    JAPANESE("ja"),
    ROMANA("ro"),
    UKRAINIAN("uk");

    companion object {
        fun fromCode(code: String): Language {
            return values().find { it.code == code } ?: FRANCAIS
        }
    }
} 