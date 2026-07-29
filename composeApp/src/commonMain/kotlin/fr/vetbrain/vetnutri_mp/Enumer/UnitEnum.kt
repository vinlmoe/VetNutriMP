package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

enum class UnitEnum(
        private val unitName: String,
        private val id: Int,
        private val idFamily: Int,
        private val refId: Int,
        val conv: Double,
        override val label: String
) : Labelable {
    BUg("g", 1, 1, 1, 1.0, "BUg"),
    BUmg("mg", 2, 1, 1, 0.001, "BUmg"),
    BUmu("µg", 3, 1, 1, 0.000001, "BUmu"),
    AUui("UI", 4, 2, 4, 1.0, "AUui"),
    AUmu("µg", 5, 2, 4, 3.33, "AUmu"),
    DUui("UI", 6, 3, 6, 1.0, "DUui"),
    DUmu("µg", 7, 3, 6, 40.0, "DUmu"),
    EUui("UI", 8, 4, 6, 1.0, "EUui"),
    EUmg("mg", 9, 4, 8, 1.0, "EUmg"),
    KCAL("kcal", 10, 6, 10, 1.0, "KCAL"),
    NO("", 0, 5, 10, 0.0, "NO");

    val displayName: String
        get() = unitName

    fun nameToString(): String {
        return unitName
    }

    fun getID(): Int {
        return id
    }

    fun getIDFamily(): Int {
        return idFamily
    }

    fun getName(): String {
        return unitName
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
                if (e.unitName == str && e.idFamily == family) {
                    return e
                }
            }
            return BUg
        }
    }
}
