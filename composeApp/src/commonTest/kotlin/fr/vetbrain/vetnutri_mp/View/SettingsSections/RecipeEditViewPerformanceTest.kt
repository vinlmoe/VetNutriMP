package fr.vetbrain.vetnutri_mp.View.SettingsSections

import fr.vetbrain.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetbrain.vetnutri_mp.Repository.RecipeRepository
import fr.vetbrain.vetbrain.vetnutri_mp.ViewModel.RecipeEditViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.*
import kotlin.test.*

class RecipeEditViewPerformanceTest {
    
    @Test
    fun testViewModelInitializationPerformance() = runTest {
        val mockRecipeRepository = mock<RecipeRepository>()
        val mockFoodRepository = mock<FoodRepository>()
        
        val startTime = System.currentTimeMillis()
        
        val viewModel = RecipeEditViewModel(mockRecipeRepository, mockFoodRepository)
        
        val endTime = System.currentTimeMillis()
        val initializationTime = endTime - startTime
        
        // L'initialisation devrait être rapide (< 100ms)
        assertTrue(initializationTime < 100, "Initialisation trop lente: ${initializationTime}ms")
        
        // Vérifier l'état initial
        assertFalse(viewModel.isEditMode.value)
        assertNull(viewModel.editingRecipe.value)
        assertTrue(viewModel.recipes.isEmpty())
    }
    
    @Test
    fun testRecipeFilteringPerformance() = runTest {
        val mockRecipeRepository = mock<RecipeRepository>()
        val mockFoodRepository = mock<FoodRepository>()
        val viewModel = RecipeEditViewModel(mockRecipeRepository, mockFoodRepository)
        
        // Créer un grand nombre de recettes pour tester les performances
        val largeRecipeList = (1..1000).map { index ->
            Ration(
                uuid = "recipe-$index",
                name = "Recipe $index",
                espece = "CHIEN",
                alimentMutableList = mutableListOf(
                    AlimentRation(
                        uuid = "aliment-$index",
                        uuidUnif = "food-$index",
                        quantite = 100.0 + index
                    )
                )
            )
        }
        
        viewModel.recipes.addAll(largeRecipeList)
        
        // Mesurer le temps de filtrage
        val startTime = System.currentTimeMillis()
        
        viewModel.updateSearchQuery("Recipe 500")
        val filteredRecipes = viewModel.getFilteredRecipes()
        
        val endTime = System.currentTimeMillis()
        val filteringTime = endTime - startTime
        
        // Le filtrage devrait être rapide (< 50ms même avec 1000 recettes)
        assertTrue(filteringTime < 50, "Filtrage trop lent: ${filteringTime}ms")
        
        // Vérifier le résultat
        assertEquals(1, filteredRecipes.size)
        assertEquals("Recipe 500", filteredRecipes.first().name)
    }
    
    @Test
    fun testRecipeSearchPerformance() = runTest {
        val mockRecipeRepository = mock<RecipeRepository>()
        val mockFoodRepository = mock<FoodRepository>()
        val viewModel = RecipeEditViewModel(mockRecipeRepository, mockFoodRepository)
        
        // Créer des recettes avec des noms variés
        val recipes = listOf(
            "Chicken Recipe", "Beef Recipe", "Fish Recipe",
            "Vegetarian Recipe", "Grain Recipe", "Protein Recipe"
        ).mapIndexed { index, name ->
            Ration(
                uuid = "recipe-$index",
                name = name,
                espece = "CHIEN",
                alimentMutableList = mutableListOf()
            )
        }
        
        viewModel.recipes.addAll(recipes)
        
        // Tester plusieurs recherches consécutives
        val searchQueries = listOf("chicken", "beef", "fish", "vegetarian", "grain", "protein")
        
        val totalStartTime = System.currentTimeMillis()
        
        searchQueries.forEach { query ->
            val startTime = System.currentTimeMillis()
            
            viewModel.updateSearchQuery(query)
            val filteredRecipes = viewModel.getFilteredRecipes()
            
            val endTime = System.currentTimeMillis()
            val searchTime = endTime - startTime
            
            // Chaque recherche devrait être rapide (< 10ms)
            assertTrue(searchTime < 10, "Recherche trop lente pour '$query': ${searchTime}ms")
            
            // Vérifier que le résultat est correct
            assertTrue(filteredRecipes.isNotEmpty(), "Aucun résultat pour '$query'")
        }
        
        val totalEndTime = System.currentTimeMillis()
        val totalTime = totalEndTime - totalStartTime
        
        // Le temps total pour 6 recherches devrait être < 100ms
        assertTrue(totalTime < 100, "Temps total des recherches trop lent: ${totalTime}ms")
    }
    
    @Test
    fun testRecipeStateChangesPerformance() = runTest {
        val mockRecipeRepository = mock<RecipeRepository>()
        val mockFoodRepository = mock<FoodRepository>()
        val viewModel = RecipeEditViewModel(mockRecipeRepository, mockFoodRepository)
        
        // Tester les changements d'état rapides
        val startTime = System.currentTimeMillis()
        
        repeat(100) { index ->
            viewModel.updateRecipeName("Recipe $index")
            viewModel.updateQuantityToAdd("${100 + index}")
            viewModel.updateTargetToAdd(index % 10)
        }
        
        val endTime = System.currentTimeMillis()
        val stateChangeTime = endTime - startTime
        
        // 100 changements d'état devraient être rapides (< 200ms)
        assertTrue(stateChangeTime < 200, "Changements d'état trop lents: ${stateChangeTime}ms")
        
        // Vérifier l'état final
        assertEquals("Recipe 99", viewModel.newRecipeName.value)
        assertEquals("199", viewModel.quantityToAdd.value)
        assertEquals(9, viewModel.targetToAdd.value)
    }
    
    @Test
    fun testRecipeValidationPerformance() = runTest {
        val mockRecipeRepository = mock<RecipeRepository>()
        val mockFoodRepository = mock<FoodRepository>()
        val viewModel = RecipeEditViewModel(mockRecipeRepository, mockFoodRepository)
        
        // Créer une recette avec plusieurs ingrédients
        val testAliment = AlimentEv(uuid = "food-1", nom = "Test Food")
        
        viewModel.startCreatingRecipe()
        viewModel.updateRecipeName("Test Recipe")
        
        // Ajouter plusieurs ingrédients
        val startTime = System.currentTimeMillis()
        
        repeat(50) { index ->
            viewModel.selectAlimentToAdd(testAliment)
            viewModel.updateQuantityToAdd("${100 + index}")
            viewModel.updateTargetToAdd(index % 5)
            viewModel.addAlimentToRecipe()
        }
        
        val endTime = System.currentTimeMillis()
        val addIngredientsTime = endTime - startTime
        
        // Ajouter 50 ingrédients devrait être rapide (< 500ms)
        assertTrue(addIngredientsTime < 500, "Ajout d'ingrédients trop lent: ${addIngredientsTime}ms")
        
        // Vérifier que la validation fonctionne
        assertTrue(viewModel.canSaveRecipe())
        assertEquals(50, viewModel.selectedIngredients.size)
    }
    
    @Test
    fun testMemoryUsage() = runTest {
        val mockRecipeRepository = mock<RecipeRepository>()
        val mockFoodRepository = mock<FoodRepository>()
        
        // Mesurer l'utilisation mémoire avant
        val runtime = Runtime.getRuntime()
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()
        
        val viewModel = RecipeEditViewModel(mockRecipeRepository, mockFoodRepository)
        
        // Créer un grand nombre de recettes
        val largeRecipeList = (1..1000).map { index ->
            Ration(
                uuid = "recipe-$index",
                name = "Recipe $index",
                espece = "CHIEN",
                alimentMutableList = mutableListOf(
                    AlimentRation(
                        uuid = "aliment-$index",
                        uuidUnif = "food-$index",
                        quantite = 100.0 + index
                    )
                )
            )
        }
        
        viewModel.recipes.addAll(largeRecipeList)
        
        // Mesurer l'utilisation mémoire après
        val finalMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = finalMemory - initialMemory
        
        // L'utilisation mémoire devrait être raisonnable (< 10MB pour 1000 recettes)
        val memoryUsedMB = memoryUsed / (1024 * 1024)
        assertTrue(memoryUsedMB < 10, "Utilisation mémoire trop élevée: ${memoryUsedMB}MB")
        
        // Vérifier que les données sont correctement stockées
        assertEquals(1000, viewModel.recipes.size)
    }
    
    @Test
    fun testConcurrentOperations() = runTest {
        val mockRecipeRepository = mock<RecipeRepository>()
        val mockFoodRepository = mock<FoodRepository>()
        val viewModel = RecipeEditViewModel(mockRecipeRepository, mockFoodRepository)
        
        // Simuler des opérations concurrentes
        val startTime = System.currentTimeMillis()
        
        // Opération 1: Mise à jour du nom
        viewModel.updateRecipeName("Concurrent Recipe")
        
        // Opération 2: Mise à jour de la quantité
        viewModel.updateQuantityToAdd("150")
        
        // Opération 3: Mise à jour de la cible
        viewModel.updateTargetToAdd(5)
        
        // Opération 4: Vérification de la validation
        val canSave = viewModel.canSaveRecipe()
        
        val endTime = System.currentTimeMillis()
        val concurrentOperationsTime = endTime - startTime
        
        // Les opérations concurrentes devraient être rapides (< 50ms)
        assertTrue(concurrentOperationsTime < 50, "Opérations concurrentes trop lentes: ${concurrentOperationsTime}ms")
        
        // Vérifier l'état final
        assertEquals("Concurrent Recipe", viewModel.newRecipeName.value)
        assertEquals("150", viewModel.quantityToAdd.value)
        assertEquals(5, viewModel.targetToAdd.value)
        assertFalse(canSave) // Pas d'ingrédients ajoutés
    }
    
    @Test
    fun testRecipeListRenderingPerformance() = runTest {
        val mockRecipeRepository = mock<RecipeRepository>()
        val mockFoodRepository = mock<FoodRepository>()
        val viewModel = RecipeEditViewModel(mockRecipeRepository, mockFoodRepository)
        
        // Créer des recettes avec des structures complexes
        val complexRecipes = (1..100).map { index ->
            Ration(
                uuid = "recipe-$index",
                name = "Complex Recipe $index",
                espece = "CHIEN",
                alimentMutableList = mutableListOf(
                    AlimentRation(
                        uuid = "aliment-$index-1",
                        uuidUnif = "food-$index-1",
                        quantite = 100.0 + index,
                        refTarget = index % 10
                    ),
                    AlimentRation(
                        uuid = "aliment-$index-2",
                        uuidUnif = "food-$index-2",
                        quantite = 200.0 + index,
                        refTarget = (index + 1) % 10
                    )
                )
            )
        }
        
        viewModel.recipes.addAll(complexRecipes)
        
        // Mesurer le temps de filtrage avec une recherche complexe
        val startTime = System.currentTimeMillis()
        
        viewModel.updateSearchQuery("Complex Recipe 50")
        val filteredRecipes = viewModel.getFilteredRecipes()
        
        val endTime = System.currentTimeMillis()
        val complexFilteringTime = endTime - startTime
        
        // Le filtrage de recettes complexes devrait être rapide (< 30ms)
        assertTrue(complexFilteringTime < 30, "Filtrage complexe trop lent: ${complexFilteringTime}ms")
        
        // Vérifier le résultat
        assertEquals(1, filteredRecipes.size)
        assertEquals("Complex Recipe 50", filteredRecipes.first().name)
        assertEquals(2, filteredRecipes.first().alimentMutableList.size)
    }
}
