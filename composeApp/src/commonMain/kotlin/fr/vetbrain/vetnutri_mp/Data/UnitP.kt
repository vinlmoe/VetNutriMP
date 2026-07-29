package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.UnitEnum

/**
 * Classe représentant une unité avec ses propriétés. Basée sur la classe UnitP du projet Java
 * original.
 */
class UnitP(private val unitEnum: UnitEnum) {
    // Nom de l'unité
    private val nom: String = unitEnum.displayName

    /**
     * Récupère l'unité enum
     *
     * @return L'énumération UnitEnum
     */
    fun getUnit(): UnitEnum = unitEnum

    /**
     * Récupère le nom de l'unité
     *
     * @return Le nom de l'unité
     */
    fun getNom(): String = nom

    /** Récupère le nom sous forme de String */
    fun getNomS(): String = nom
}
