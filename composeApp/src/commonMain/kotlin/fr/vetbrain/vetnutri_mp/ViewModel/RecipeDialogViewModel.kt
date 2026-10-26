package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Repository.RecipeRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.withContext

/**
 * Opérations du dialogue des recettes (View/components/RecipeDialog.kt) et de l'enregistrement
 * d'une ration comme recette (RationsView) : liste, clonage, suppression, création.
 * Les Composables n'accèdent plus à RecipeRepository / FoodRepository.
 */
class RecipeDialogViewModel(
        private val recipeRepository: RecipeRepository,
        private val foodRepository: FoodRepository
) : BaseViewModel() {

    /** Recettes avec le détail des aliments (AlimentEv) résolu depuis le FoodRepository. */
    suspend fun loadRecipesWithFoodDetails(): List<Ration> =
            withContext(AppDispatchers.IO) {
                recipeRepository.getAllRecipes().map { recipe ->
                    val alimentsWithDetails =
                            recipe.alimentMutableList.map { aliment ->
                                val alimentDetails = aliment.refAlimUnif?.let { foodRepository.getFoodById(it) }
                                aliment.copy(aliment = alimentDetails)
                            }
                    recipe.copy(alimentMutableList = alimentsWithDetails.toMutableList())
                }
            }

    suspend fun cloneRecipe(recipeId: String): Ration? =
            withContext(AppDispatchers.IO) { recipeRepository.cloneRecipe(recipeId) }

    suspend fun deleteRecipe(recipeId: String) =
            withContext(AppDispatchers.IO) { recipeRepository.deleteRecipe(recipeId) }

    suspend fun createRecipe(name: String, espece: String? = null, description: String? = null): Ration =
            withContext(AppDispatchers.IO) { recipeRepository.createRecipe(name, espece, description) }

    /** Enregistre une ration comme nouvelle recette (nom + aliments). */
    suspend fun saveRationAsRecipe(name: String, espece: String?, aliments: List<AlimentRation>): Ration =
            withContext(AppDispatchers.IO) {
                val recette = recipeRepository.createRecipe(name = name, espece = espece, description = null)
                recipeRepository.replaceAliments(recette.uuid, aliments)
                recette
            }
}
