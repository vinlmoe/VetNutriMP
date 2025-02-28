package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

/** Énumération des sexes des animaux Correspond à l'enum Sex de Java */
enum class Sex(val id: Int, val displayName: String, override val label: String, val coef: Float) :
        Labelable {
    MALE_ENTIER(0, "Mâle entier", "MaleEnt", 1.0f),
    MALE_CASTRE(1, "Mâle castré", "MaleSpray", 0.8f),
    FEMELLE_ENTIERE(2, "Femelle entière", "FemEnt", 1.0f),
    FEMELLE_STERILISEE(3, "Femelle stérilisée", "FemSpray", 0.8f);

    companion object {
        fun fromId(id: Int): Sex = values().find { it.id == id } ?: MALE_ENTIER

        /** Retourne une version simplifiée du sexe (mâle ou femelle) */
        fun getSimpleSex(id: Int): String {
            return when (id) {
                0, 1 -> "Mâle"
                2, 3 -> "Femelle"
                else -> "Non spécifié"
            }
        }
    }
}
