package fr.vetbrain.vetnutri_mp.Data

/**
 * Modèle de domaine pour une recette
 */
data class Recette(
        val uuid: String,
        val name: String?,
        val number: Int = 0,
        val espece: String?,
        val description: String?,
        val aliments: MutableList<AlimentRecette> = mutableListOf()
)

/**
 * Modèle de domaine pour un ingrédient de recette
 */
data class AlimentRecette(
        val uuid: String,
        val refAlimUnif: String,
        val refRecipe: String,
        val quantity: Double = 0.0,
        val refTarget: Int = 0
)
