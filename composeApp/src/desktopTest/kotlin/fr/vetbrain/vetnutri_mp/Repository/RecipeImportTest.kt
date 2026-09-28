package fr.vetbrain.vetnutri_mp.Repository

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.DataBase.AppDatabase
import kotlin.test.*
import kotlinx.coroutines.test.runTest

class RecipeImportTest {
    @Test
    fun repeatedImportUpdatesRecipeAndReplacesIngredients() = runTest {
        val db = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver()).build()
        try {
            val recipes = RecipeRepository(db.recipeDao(), db.foodDao())
            val foods = DatabaseFoodRepository(
                foodDao = db.foodDao(), nutrientValueDao = db.nutrientValueDao(),
                customNutrientDao = db.customNutrientDao(),
                alimentBiblioRefDao = db.alimentBiblioRefDao(),
                biblioRefDao = db.biblioRefDao(), energyPerSpeciesDao = db.energyPerSpeciesDao()
            )
            foods.insertFood(AlimentEv(uuid = "food", nom = "Test"))
            val importer = ExportImportRepository(InMemoryAnimalRepository(), recipeRepository = recipes)
            val recipe = RecipeApi("recipe", "Original", aliments = listOf(
                RecipeIngredientApi("ingredient", "food", 50.0)
            ))
            fun envelope(value: RecipeApi) = ApiEnvelope("test", 0, emptyList(), recipes = listOf(value))
            repeat(2) {
                assertEquals(1, importer.importAll(envelope(recipe)).requireComplete().recipes)
                assertEquals(1, db.recipeDao().getAllRecipes().size)
                assertEquals(1, db.recipeDao().getAlimentsForRecipe("recipe").size)
            }
            val updated = recipe.copy(name = "Updated", number = 3, aliments = listOf(
                RecipeIngredientApi("ingredient", "food", 75.0, 2)
            ))
            importer.importAll(envelope(updated)).requireComplete()
            assertEquals("Updated", db.recipeDao().getRecipeById("recipe")?.name)
            assertEquals(3, db.recipeDao().getRecipeById("recipe")?.number)
            val ingredient = db.recipeDao().getAlimentsForRecipe("recipe").single()
            assertEquals(75.0, ingredient.quantity)
            assertEquals(2, ingredient.refTarget)

            // A failed ingredient write must restore both the recipe and its ingredients.
            val invalid = updated.copy(name = "Invalid", aliments = listOf(
                RecipeIngredientApi("missing", "unknown-food", 10.0)
            ))
            assertEquals(1, importer.importAll(envelope(invalid)).errorCount)
            assertEquals("Updated", db.recipeDao().getRecipeById("recipe")?.name)
            assertEquals(ingredient, db.recipeDao().getAlimentsForRecipe("recipe").single())

            importer.importAll(envelope(updated.copy(aliments = emptyList()))).requireComplete()
            assertTrue(db.recipeDao().getAlimentsForRecipe("recipe").isEmpty())
        } finally {
            db.close()
        }
    }
}
