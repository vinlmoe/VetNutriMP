package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

enum class Sex(val id: Int, override val label: String, val displayName: String, val coef: Float) :
        Labelable {
    MALEE(0, "MaleEnt", "Mâle entier", 1.0f),
    MALEC(1, "MaleSpray", "Mâle castré", 0.8f),
    FEMELLEE(2, "FemEnt", "Femelle entière", 1.0f),
    FEMELLE(3, "FemSpray", "Femelle stérilisée", 0.8f);

    companion object {
        fun fromId(id: Int): Sex? = values().find { it.id == id }
    }
}
