package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

enum class ContEnum(override val label: String, private val id: Int) : Labelable {
    NO("no", 0),
    GEL("gel", 9),
    CAN("can", 2),
    SACHET("sachet", 3),
    PRESSION("pressure", 4),
    ML("ml", 5),
    COMP("tablet", 6),
    BOUCH("spoon", 7),
    DOSETTE("dosette", 8);

    var conv: Double = 1.0

    fun nameToString() = label

    companion object {
        fun byId(id: Int) = values().find { it.id == id } ?: NO
        fun getByName(str: String) = values().find { it.name == str } ?: NO
        fun getByLabel(label: String): ContEnum = values().find { it.label.equals(label, ignoreCase = true) } ?: NO
    }
}
