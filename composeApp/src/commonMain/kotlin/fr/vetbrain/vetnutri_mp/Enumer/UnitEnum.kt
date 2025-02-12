package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

enum class UnitEnum(val displayName: String, override val label: String) : Labelable {
    NO("", "NO"),
    BUg("g", "BUg"),
    BUmg("mg", "BUmg"),
    BUmu("µg", "BUmu"),
    AUui("UI", "AUui"),
    DUui("UI", "DUui"),
    EUui("UI", "EUui");

    companion object {
        fun fromDisplayName(name: String): UnitEnum = values().find { it.displayName == name } ?: NO
    }
}
