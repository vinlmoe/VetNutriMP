package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim

/**
 * Version allégée de AlimentEv ne contenant que les informations nécessaires pour l'affichage et la
 * recherche. Cette classe est utilisée pour optimiser le chargement des aliments au démarrage de
 * l'application.
 */
data class AlimentEvLight(
        val uuid: String,
        val nom: String? = null,
        val brand: String? = null,
        val group: GroupAlim? = null,
        val typeAliment: FoodKind? = null,
        val gamme: String? = null,
        val especes: List<String> = emptyList(),
        val indicat: List<AlimIndic> = emptyList(),
        val deprecated: Boolean = false,
        val dataB: String? = null
)
