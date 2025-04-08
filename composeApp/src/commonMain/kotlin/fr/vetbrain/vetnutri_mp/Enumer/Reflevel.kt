package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

/**
 * Énumération représentant les niveaux relatifs des références de nutriments Basée sur
 * l'énumération Reflevel du projet Java original
 */
enum class Reflevel(val uuid: Int, override val label: String) : Labelable {
    MIN(0, "minimum"),
    MAX(1, "maximum"),
    OPTIMIN(2, "optimumMin"),
    OPTIMAX(3, "optimumMax");

    companion object {
        /**
         * Récupère un niveau relatif par son identifiant
         *
         * @param id L'identifiant à rechercher
         * @return Le niveau relatif correspondant ou MIN par défaut
         */
        fun getById(id: Int): Reflevel = entries.find { it.uuid == id } ?: MIN
    }

    override fun toString(): String = label
}
