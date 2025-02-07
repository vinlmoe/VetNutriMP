package fr.vetbrain.vetnutri_mp.Enumerise

import fr.vetbrain.vetnutri_mp.Data.Labelable

enum class ContEnum(override val label: String, private val id: Int) :Labelable{
    NO("NO", 0),
    GEL("Gelule", 9),
    CAN("Can", 2),
    SACHET("Sachet", 3),
    PRESSION("Pression", 4),
    ML("mL", 5),
    COMP("comprim", 6),
    BOUCH("Bouch", 7),
    DOSETTE("Dosette", 8);

    var conv: Float = 1f

    fun nameToString() = label


    companion object {
        fun byId(id: Int) = values().find { it.id == id } ?: NO
        fun getByName(str: String) = values().find { it.name == str } ?: NO
    }
}
