package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.DataBase.AlimentRecetteEntity
import fr.vetbrain.vetnutri_mp.DataBase.FoodDao
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toAlimentEv
import fr.vetbrain.vetnutri_mp.DataBase.RecetteEntity
import fr.vetbrain.vetnutri_mp.DataBase.RecipeDao
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.withContext

/**
 * Repository pour gérer les recettes persistantes (listes d'aliments réutilisables). Une recette
 * est analogue à une ration mais indépendante de toute consultation, et sans flags.
 */
class RecipeRepository(private val recipeDao: RecipeDao, private val foodDao: FoodDao) {
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
                    coef = 1.0,
                    actual = false,
                    number = 1,
                    espece = espece,
                    recette = true,
                    description = description ?: "",
                    alimentMutableList = mutableListOf()
            )
        }
    }

    suspend fun createRecipeWithUuid(
            uuid: String,
            name: String,
            espece: String?,
            description: String?,
            number: Int = 1
    ): Ration {
        return withContext(AppDispatchers.IO) {
            val entity: RecetteEntity =
                    RecetteEntity(
                            uuid = uuid,
                            name = name,
                            number = number,
                            espece = espece,
                            description = description
                    )
            recipeDao.insertRecipe(entity)
            Ration(
                    uuid = uuid,
                    idConsult = "",
                    name = name,
                    coef = 1.0,
                    actual = false,
                    number = number,
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

            val result =
                    recipes.map { recipe ->
                        val aliments: List<AlimentRecetteEntity> =
                                recipeDao.getAlimentsForRecipe(recipe.uuid)

                        Ration(
                                uuid = recipe.uuid,
                                idConsult = "",
                                name = recipe.name ?: "",
                                coef = 1.0,
                                actual = false,
                                number = recipe.number,
                                espece = recipe.espece,
                                recette = true,
                                description = recipe.description ?: "",
                                alimentMutableList =
                                        aliments
                                                .map { it.toDataForRecipeWithDetails() }
                                                .toMutableList()
                        )
                    }
            result
        }
    }

    suspend fun getAllRecipesAsRecette(): List<fr.vetbrain.vetnutri_mp.Data.Recette> {
        return withContext(AppDispatchers.IO) {
            println("🔍 DEBUG RecipeRepository: getAllRecipesAsRecette() appelé")
            val recipes: List<RecetteEntity> = recipeDao.getAllRecipes()
            println("🔍 DEBUG RecipeRepository: ${recipes.size} recettes trouvées en base")

            val result =
                    recipes.map { recipe ->
                        val aliments: List<AlimentRecetteEntity> =
                                recipeDao.getAlimentsForRecipe(recipe.uuid)
                        println(
                                "🔍 DEBUG RecipeRepository: Recette ${recipe.name} (${recipe.uuid}) a ${aliments.size} ingrédients"
                        )

                        fr.vetbrain.vetnutri_mp.Data.Recette(
                                uuid = recipe.uuid,
                                name = recipe.name,
                                number = recipe.number,
                                espece = recipe.espece,
                                description = recipe.description,
                                aliments = aliments.map { it.toDataForRecette() }.toMutableList()
                        )
                    }
            println(
                    "🔍 DEBUG RecipeRepository: ${result.size} recettes converties en format Recette"
            )
            result
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
                    coef = 1.0,
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
                                uuid =
                                        fr.vetbrain.vetnutri_mp.Utils
                                                .genUUID(), // Générer un nouvel UUID pour l'entrée
                                // de recette
                                refAlimUnif = al.refAlimUnif
                                                ?: al.uuidUnif, // Référence vers l'aliment original
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
        // Pour les recettes, on crée un AlimentRation temporaire qui sera régénéré lors de
        // l'application
        return AlimentRation(
                uuid =
                        fr.vetbrain.vetnutri_mp.Utils
                                .genUUID(), // UUID temporaire qui ne sera pas utilisé
                uuidUnif = this.refAlimUnif, // L'important : référence vers l'aliment
                quantite = this.quantity,
                proportion = 0.0,
                aliment = null, // Sera chargé lors de l'application
                refAlimUnif = this.refAlimUnif, // Référence vers l'aliment
                refRation = null, // Pas de ration associée dans une recette
                refTarget = this.refTarget
        )
    }

    private suspend fun AlimentRecetteEntity.toDataForRecipeWithDetails(): AlimentRation {
        // Charger les détails complets de l'aliment depuis la base de données
        val foodEntity = foodDao.getFoodById(this.refAlimUnif)
        val alimentDetails = foodEntity?.toAlimentEv()

        return AlimentRation(
                uuid = fr.vetbrain.vetnutri_mp.Utils.genUUID(), // UUID temporaire
                uuidUnif = this.refAlimUnif,
                quantite = this.quantity,
                proportion = 0.0,
                aliment = alimentDetails, // Aliment complet avec tous les détails
                refAlimUnif = this.refAlimUnif,
                refRation = null,
                refTarget = this.refTarget
        )
    }

    private fun AlimentRecetteEntity.toDataForRecette():
            fr.vetbrain.vetnutri_mp.Data.AlimentRecette {
        // Convertir en AlimentRecette pour l'export
        return fr.vetbrain.vetnutri_mp.Data.AlimentRecette(
                uuid = this.uuid,
                refAlimUnif = this.refAlimUnif,
                refRecipe = this.refRecipe,
                quantity = this.quantity,
                refTarget = this.refTarget
        )
    }
}
