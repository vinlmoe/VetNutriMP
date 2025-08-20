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

    suspend fun getAllRecipes(): List<Ration> {
        return withContext(AppDispatchers.IO) {
            println("🔍 DEBUG RecipeRepository: getAllRecipes() appelé")
            val recipes: List<RecetteEntity> = recipeDao.getAllRecipes()
            println("🔍 DEBUG RecipeRepository: ${recipes.size} recettes trouvées en base")

            val result =
                    recipes.map { recipe ->
                        println(
                                "🔍 DEBUG RecipeRepository: Traitement recette: ${recipe.name} (${recipe.uuid})"
                        )
                        val aliments: List<AlimentRecetteEntity> =
                                recipeDao.getAlimentsForRecipe(recipe.uuid)
                        println(
                                "🔍 DEBUG RecipeRepository: ${aliments.size} aliments trouvés pour la recette ${recipe.name}"
                        )

                        val ration: Ration =
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
                                                        .map { it.toDataForRecipe() }
                                                        .toMutableList()
                                )
                        println(
                                "🔍 DEBUG RecipeRepository: Ration créée avec ${ration.alimentMutableList.size} aliments"
                        )
                        ration
                    }
            println(
                    "🔍 DEBUG RecipeRepository: getAllRecipes() terminé, ${result.size} rations retournées"
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
            println(
                    "🔍 DEBUG RecipeRepository: addAliments appelé pour recette $recipeId avec ${aliments.size} aliments"
            )
            aliments.forEach { al ->
                println(
                        "🔍 DEBUG RecipeRepository: Ajout aliment - refAlimUnif: ${al.refAlimUnif}, quantite: ${al.quantite}"
                )
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
                println(
                        "🔍 DEBUG RecipeRepository: Entity créé - UUID: ${entity.uuid}, refAlimUnif: ${entity.refAlimUnif}"
                )
                recipeDao.insertAlimentRecette(entity)
                println("✅ DEBUG RecipeRepository: Aliment ajouté à la recette")
            }
            println("🔍 DEBUG RecipeRepository: addAliments terminé")
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
        println("🔍 DEBUG RecipeRepository: Conversion AlimentRecetteEntity vers AlimentRation")
        println("🔍 DEBUG RecipeRepository: - UUID entité recette: ${this.uuid}")
        println("🔍 DEBUG RecipeRepository: - refAlimUnif (aliment): ${this.refAlimUnif}")
        println("🔍 DEBUG RecipeRepository: - refRecipe: ${this.refRecipe}")
        println("🔍 DEBUG RecipeRepository: - quantity: ${this.quantity}")
        println("🔍 DEBUG RecipeRepository: - refTarget: ${this.refTarget}")

        // Pour les recettes, on crée un AlimentRation temporaire qui sera régénéré lors de
        // l'application
        val result =
                AlimentRation(
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

        println("🔍 DEBUG RecipeRepository: AlimentRation créé:")
        println("🔍 DEBUG RecipeRepository: - UUID temporaire: ${result.uuid}")
        println("🔍 DEBUG RecipeRepository: - refAlimUnif: ${result.refAlimUnif}")
        println("🔍 DEBUG RecipeRepository: - quantite: ${result.quantite}")
        println("🔍 DEBUG RecipeRepository: - refRation: ${result.refRation}")

        return result
    }
}
