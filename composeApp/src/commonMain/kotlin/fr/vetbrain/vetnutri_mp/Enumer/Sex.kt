package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

enum class Sex(val id: Int, val displayName: String, override val label: String) : Labelable {
    MALE(1, "Mâle", "M"),
    FEMALE(2, "Femelle", "F");

    companion object {
        fun fromId(id: Int): Sex? = values().find { it.id == id }
    }
}
