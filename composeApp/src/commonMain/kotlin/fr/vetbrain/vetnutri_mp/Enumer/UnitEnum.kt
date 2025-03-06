package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

enum class UnitEnum(
        private val name: String,
        private val id: Int,
        private val idFamily: Int,
        private val refId: Int,
        private val conv: Float,
        override val label: String
) : Labelable {
    BUg("g", 1, 1, 1, 1f, "BUg"),
    BUmg("mg", 2, 1, 1, 0.001f, "BUmg"),
    BUmu("µg", 3, 1, 1, 0.000001f, "BUmu"),
    AUui("UI", 4, 2, 4, 1f, "AUui"),
    AUmu("µg", 5, 2, 4, 3.33f, "AUmu"),
    DUui("UI", 6, 3, 6, 1f, "DUui"),
    DUmu("µg", 7, 3, 6, 40f, "DUmu"),
    EUui("UI", 8, 4, 6, 1f, "EUui"),
    EUmg("mg", 9, 4, 8, 1f, "EUmg"),
    NO("", 0, 5, 10, 0f, "NO");

    val displayName: String
        get() = name

    fun nameToString(): String {
        return name
    }

    fun getConv(): Float {
        return conv
    }

    fun getID(): Int {
        return id
    }

    fun getIDFamily(): Int {
        return idFamily
    }

    fun getName(): String {
        return name
    }

    fun getRefID(): Int {
        return refId
    }

    companion object {
        fun fromDisplayName(name: String): UnitEnum = values().find { it.displayName == name } ?: NO

        fun byId(id: Int): UnitEnum {
            for (e in values()) {
                if (e.id == id) {
                    return e
                }
            }
            return BUg
        }

        fun getByName(str: String, family: Int): UnitEnum {
            for (e in values()) {
                if (e.name == str && e.idFamily == family) {
                    return e
                }
            }
            return BUg
        }
    }
}
