package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.Ration

/** Bibliothèque de recettes manipulée par le dialogue de recettes (fournie par AnimalDetailViewModel). */
interface RecipeLibrary {
    /** Recettes avec le détail de leurs aliments */
    suspend fun loadRecipes(): List<Ration>
    suspend fun cloneRecipe(recipeId: String)
    suspend fun deleteRecipe(recipeId: String)
    suspend fun createRecipe(name: String)
}
