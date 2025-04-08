package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

/**
 * Énumération représentant les stades physiologiques d'un animal. Basée sur l'enum StadePhysio du
 * projet Java original.
 */
enum class StadePhysio(val categorie: Int, override val label: String) : Labelable {
    ADULTE(0, "Adulte"),
    CROISSANCE(1, "Croissance"),
    LACTATION(2, "Lactation"),
    GESTATION(3, "Gestation"),
    HOSPIT(4, "Hospit");

    companion object {
        /**
         * Récupère un stade physiologique à partir de son identifiant
         *
         * @param id L'identifiant à rechercher
         * @return Le stade physiologique correspondant ou ADULTE par défaut
         */
        fun getFromInt(id: Int): StadePhysio = entries.find { it.categorie == id } ?: ADULTE

        /**
         * Récupère un stade physiologique à partir de son nom
         *
         * @param name Le nom à rechercher
         * @return Le stade physiologique correspondant ou ADULTE par défaut
         */
        fun getFromString(name: String): StadePhysio = entries.find { it.label == name } ?: ADULTE

        /**
         * Récupère le nom d'un stade physiologique à partir de son identifiant
         *
         * @param id L'identifiant à rechercher
         * @return Le nom du stade physiologique correspondant ou "Adulte" par défaut
         */
        fun getStringFromInt(id: Int): String = getFromInt(id).label
    }

    /**
     * Convertit l'énumération en chaîne de caractères
     *
     * @return Le nom du stade physiologique
     */
    fun nameToString(): String = label

    override fun toString(): String = label
}
