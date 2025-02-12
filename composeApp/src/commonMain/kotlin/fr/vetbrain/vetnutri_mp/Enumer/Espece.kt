package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

enum class Espece(override val label: String) : Labelable {
    CHIEN("Chien"),
    CHAT("Chat"),
    CHEVAL("Cheval"),
    FURET("Furet"),
    CANIN("Canidé sauvage"),
    FELIN("Félin sauvage"),
    FOLIVORE("Folivore");

    companion object {
        fun getByLabel(label: String): Espece? = entries.find { it.label == label }

        fun valuesExcept(vararg exceptions: Espece): List<Espece> =
                entries.filter { it !in exceptions }
    }
}
