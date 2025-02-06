package fr.vetbrain.vetnutri_mp.Enumerise

import fr.vetbrain.vetnutri_mp.Data.Labelable

enum class Sex(
        val nom: String,
        val coef: Float,
         val id: Int,
         override val label: String ?
) :Labelable{
    MALEE("Mâle entier", 1f, 0, "MaleEnt"),
    MALEC("Mâle castré", 0.8f, 1, "MaleSpray"),
    FEMELLEE("Femelle entière", 1f, 2, "FemEnt"),
    FEMELLE("Femelle stérilisée", 0.8f, 3, "FemSpray");

   

    companion object {
        fun byId(id: Int) = entries.find { it.id == id } ?: MALEE
    }
}
