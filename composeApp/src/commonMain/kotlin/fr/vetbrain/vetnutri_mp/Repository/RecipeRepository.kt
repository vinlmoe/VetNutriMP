package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.DataBase.AlimentRecetteEntity
import fr.vetbrain.vetnutri_mp.DataBase.RecetteEntity
import fr.vetbrain.vetnutri_mp.DataBase.RecipeDao
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.withContext

/**
 * Repository pour gérer les recettes persistantes (listes d'aliments réutilisables). Une recette
 * est analogue à une ration mais indépendante de toute consultation, et sans flags.
 */
class RecipeRepository(private val recipeDao: RecipeDao) {
    suspend fun createRecipe(name: String, espece: String?, description: String?): Ration {
        return withContext(AppDispatchers.IO) {
            val uuid: String = fr.vetbrain.vetnutri_mp.Utils.genUUID()
            val entity: RecetteEntity =
                    RecetteEntity(
                            uuid = uuid,
                            name = name,
                            number = 1,
                            espece = espece,
                            description = description
                    )
            recipeDao.insertRecipe(entity)
            Ration(
                    uuid = uuid,
                    idConsult = "",
                    name = name,
                    coef = 1f,
                    actual = false,
                    number = 1,
                    espece = espece,
                    recette = true,
                    description = description ?: "",
                    alimentMutableList = mutableListOf()
            )
        }
    }

    suspend fun getAllRecipes(): List<Ration> {
        return withContext(AppDispatchers.IO) {
            val recipes: List<RecetteEntity> = recipeDao.getAllRecipes()
            recipes.map { recipe ->
                val aliments: List<AlimentRecetteEntity> =
                        recipeDao.getAlimentsForRecipe(recipe.uuid)
                val ration: Ration =
                        Ration(
                                uuid = recipe.uuid,
                                idConsult = "",
                                name = recipe.name ?: "",
                                coef = 1f,
                                actual = false,
                                number = recipe.number,
                                espece = recipe.espece,
                                recette = true,
                                description = recipe.description ?: "",
                                alimentMutableList =
                                        aliments.map { it.toDataForRecipe() }.toMutableList()
                        )
                ration
            }
        }
    }

    suspend fun getRecipeById(uuid: String): Ration? {
        return withContext(AppDispatchers.IO) {
            val entity: RecetteEntity = recipeDao.getRecipeById(uuid) ?: return@withContext null
            val aliments: List<AlimentRecetteEntity> = recipeDao.getAlimentsForRecipe(uuid)
            Ration(
                    uuid = entity.uuid,
                    idConsult = "",
                    name = entity.name ?: "",
                    coef = 1f,
                    actual = false,
                    number = entity.number,
                    espece = entity.espece,
                    recette = true,
                    description = entity.description ?: "",
                    alimentMutableList = aliments.map { it.toDataForRecipe() }.toMutableList()
            )
        }
    }

    suspend fun addAliments(recipeId: String, aliments: List<AlimentRation>) {
        return withContext(AppDispatchers.IO) {
            aliments.forEach { al ->
                val entity: AlimentRecetteEntity =
                        AlimentRecetteEntity(
                                uuid = al.uuid,
                                refAlimUnif = al.refAlimUnif ?: al.uuidUnif,
                                refRecipe = recipeId,
                                quantity = al.quantite,
                                refTarget = al.refTarget ?: 0
                        )
                recipeDao.insertAlimentRecette(entity)
            }
        }
    }

    suspend fun replaceAliments(recipeId: String, aliments: List<AlimentRation>) {
        return withContext(AppDispatchers.IO) {
            recipeDao.deleteAlimentsForRecipe(recipeId)
            addAliments(recipeId, aliments)
        }
    }

    suspend fun renameRecipe(recipeId: String, newName: String) {
        return withContext(AppDispatchers.IO) {
            val entity: RecetteEntity = recipeDao.getRecipeById(recipeId) ?: return@withContext
            recipeDao.updateRecipe(entity.copy(name = newName))
        }
    }

    suspend fun cloneRecipe(recipeId: String): Ration? {
        return withContext(AppDispatchers.IO) {
            val src: RecetteEntity = recipeDao.getRecipeById(recipeId) ?: return@withContext null
            val srcAliments: List<AlimentRecetteEntity> = recipeDao.getAlimentsForRecipe(recipeId)
            val newId: String = fr.vetbrain.vetnutri_mp.Utils.genUUID()
            val copy: RecetteEntity = src.copy(uuid = newId, name = (src.name ?: "") + " bis")
            recipeDao.insertRecipe(copy)
            srcAliments.forEach { a ->
                recipeDao.insertAlimentRecette(
                        a.copy(uuid = fr.vetbrain.vetnutri_mp.Utils.genUUID(), refRecipe = newId)
                )
            }
            getRecipeById(newId)
        }
    }

    suspend fun deleteRecipe(recipeId: String) {
        return withContext(AppDispatchers.IO) {
            val entity: RecetteEntity = recipeDao.getRecipeById(recipeId) ?: return@withContext
            recipeDao.deleteRecipe(entity)
        }
    }

    private fun AlimentRecetteEntity.toDataForRecipe(): AlimentRation {
        return AlimentRation(
                uuid = this.uuid,
                uuidUnif = this.refAlimUnif,
                quantite = this.quantity,
                proportion = 0f,
                aliment = null,
                refAlimUnif = this.refAlimUnif,
                refRation = null,
                refTarget = this.refTarget
        )
    }
}
