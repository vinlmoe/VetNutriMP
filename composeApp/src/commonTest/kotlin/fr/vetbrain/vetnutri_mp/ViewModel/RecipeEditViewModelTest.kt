package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Repository.RecipeRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeEditViewModelTest {
    
    private lateinit var viewModel: RecipeEditViewModel
    private lateinit var mockRecipeRepository: RecipeRepository
    private lateinit var mockFoodRepository: FoodRepository
    private val testDispatcher = StandardTestDispatcher()
    
        fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRecipeRepository = mock()
        mockFoodRepository = mock()
        viewModel = RecipeEditViewModel(mockRecipeRepository, mockFoodRepository)
    }

    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    fun `test initial state`() = runTest {
        assertFalse(viewModel.isEditMode.value)
        assertNull(viewModel.editingRecipe.value)
        assertTrue(viewModel.recipes.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.message.value)
        assertEquals("", viewModel.searchQuery.value)
        assertEquals("", viewModel.newRecipeName.value)
        assertTrue(viewModel.selectedIngredients.isEmpty())
        assertNull(viewModel.alimentToAdd.value)
        assertEquals("100", viewModel.quantityToAdd.value)
        assertEquals(0, viewModel.targetToAdd.value)
        assertFalse(viewModel.quantityError.value)
        assertNull(viewModel.showDeleteConfirmation.value)
        assertFalse(viewModel.showSaveConfirmation.value)
    }
    
    fun `test startCreatingRecipe`() = runTest {
        viewModel.startCreatingRecipe()
        
        assertTrue(viewModel.isEditMode.value)
        assertNotNull(viewModel.editingRecipe.value)
        assertEquals("", viewModel.newRecipeName.value)
        assertTrue(viewModel.selectedIngredients.isEmpty())
        assertEquals("CHIEN", viewModel.editingRecipe.value?.espece)
    }
    
    fun `test startEditingRecipe`() = runTest {
        val testRecipe = Ration(
            uuid = "test-uuid",
            name = "Test Recipe",
            espece = "CHIEN",
            alimentMutableList = mutableListOf(
                AlimentRation(
                    uuid = "aliment-1",
                    uuidUnif = "food-1",
                    quantite = 100.0,
                    refTarget = 0
                )
            )
        )
        
        viewModel.startEditingRecipe(testRecipe)
        
        assertTrue(viewModel.isEditMode.value)
        assertEquals(testRecipe.name, viewModel.newRecipeName.value)
        assertEquals(1, viewModel.selectedIngredients.size)
        assertEquals(testRecipe.uuid, viewModel.editingRecipe.value?.uuid)
    }
    
    fun `test cancelEditing`() = runTest {
        viewModel.startCreatingRecipe()
        viewModel.updateRecipeName("Test Recipe")
        
        viewModel.cancelEditing()
        
        assertFalse(viewModel.isEditMode.value)
        assertNull(viewModel.editingRecipe.value)
        assertEquals("", viewModel.newRecipeName.value)
        assertTrue(viewModel.selectedIngredients.isEmpty())
        assertNull(viewModel.alimentToAdd.value)
        assertEquals("100", viewModel.quantityToAdd.value)
        assertEquals(0, viewModel.targetToAdd.value)
        assertFalse(viewModel.quantityError.value)
    }
    
    fun `test updateRecipeName`() = runTest {
        viewModel.updateRecipeName("New Recipe Name")
        assertEquals("New Recipe Name", viewModel.newRecipeName.value)
    }
    
    fun `test selectAlimentToAdd`() = runTest {
        val testAliment = AlimentEv(
            uuid = "food-1",
            nom = "Test Food",
            brand = "Test Brand"
        )
        
        viewModel.selectAlimentToAdd(testAliment)
        assertEquals(testAliment, viewModel.alimentToAdd.value)
    }
    
    fun `test selectAlimentTo Main`() = runTest {
        viewModel.selectAlimentToAdd(null)
        assertNull(viewModel.alimentToAdd.value)
    }
    
    fun `test updateQuantityToAdd with valid quantity`() = runTest {
        viewModel.updateQuantityToAdd("150.5")
        assertEquals("150.5", viewModel.quantityToAdd.value)
        assertFalse(viewModel.quantityError.value)
    }
    
    fun `test updateQuantityToAdd with invalid quantity`() = runTest {
        viewModel.updateQuantityToAdd("0")
        assertEquals("0", viewModel.quantityToAdd.value)
        assertTrue(viewModel.quantityError.value)
    }
    
    fun `test updateQuantityToAdd with negative quantity`() = runTest {
        viewModel.updateQuantityToAdd("-10")
        assertEquals("-10", viewModel.quantityToAdd.value)
        assertTrue(viewModel.quantityError.value)
    }
    
    fun `test updateQuantityToAdd with non-numeric`() = runTest {
        viewModel.updateQuantityToAdd("abc")
        assertEquals("abc", viewModel.quantityToAdd.value)
        assertTrue(viewModel.quantityError.value)
    }
    
    fun `test updateTargetToAdd`() = runTest {
        viewModel.updateTargetToAdd(5)
        assertEquals(5, viewModel.targetToAdd.value)
    }
    
    fun `test addAlimentToRecipe with valid data`() = runTest {
        val testAliment = AlimentEv(
            uuid = "food-1",
            nom = "Test Food"
        )
        
        viewModel.selectAlimentToAdd(testAliment)
        viewModel.updateQuantityToAdd("200.0")
        viewModel.updateTargetToAdd(3)
        
        viewModel.addAlimentToRecipe()
        
        assertEquals(1, viewModel.selectedIngredients.size)
        val addedIngredient = viewModel.selectedIngredients.first()
        assertEquals(testAliment.uuid, addedIngredient.uuidUnif)
        assertEquals(200.0, addedIngredient.quantite)
        assertEquals(3, addedIngredient.refTarget)
        assertEquals(testAliment, addedIngredient.aliment)
        
        // Vérifier que les champs sont réinitialisés
        assertNull(viewModel.alimentToAdd.value)
        assertEquals("100", viewModel.quantityToAdd.value)
        assertEquals(0, viewModel.targetToAdd.value)
    }
    
    fun `test addAlimentToRecipe with invalid quantity`() = runTest {
        val testAliment = AlimentEv(uuid = "food-1", nom = "Test Food")
        viewModel.selectAlimentToAdd(testAliment)
        viewModel.updateQuantityToAdd("0")
        
        viewModel.addAlimentToRecipe()
        
        assertTrue(viewModel.selectedIngredients.isEmpty())
        assertNotNull(viewModel.alimentToAdd.value) // L'aliment reste sélectionné
    }
    
    fun `test addAlimentToRecipe with no aliment selected`() = runTest {
        viewModel.updateQuantityToAdd("200.0")
        
        viewModel.addAlimentToRecipe()
        
        assertTrue(viewModel.selectedIngredients.isEmpty())
    }
    
    fun `test removeAlimentFromRecipe`() = runTest {
        val testIngredient = AlimentRation(
            uuid = "aliment-1",
            uuidUnif = "food-1",
            quantite = 100.0
        )
        
        viewModel.selectedIngredients.add(testIngredient)
        assertEquals(1, viewModel.selectedIngredients.size)
        
        viewModel.removeAlimentFromRecipe(testIngredient)
        
        assertTrue(viewModel.selectedIngredients.isEmpty())
    }
    
    fun `test updateAlimentQuantity`() = runTest {
        val testIngredient = AlimentRation(
            uuid = "aliment-1",
            uuidUnif = "food-1",
            quantite = 100.0
        )
        
        viewModel.selectedIngredients.add(testIngredient)
        
        viewModel.updateAlimentQuantity(testIngredient, "250.0")
        
        assertEquals(250.0, viewModel.selectedIngredients.first().quantite)
    }
    
    fun `test updateAlimentTarget`() = runTest {
        val testIngredient = AlimentRation(
            uuid = "aliment-1",
            uuidUnif = "food-1",
            quantite = 100.0,
            refTarget = 0
        )
        
        viewModel.selectedIngredients.add(testIngredient)
        
        viewModel.updateAlimentTarget(testIngredient, 7)
        
        assertEquals(7, viewModel.selectedIngredients.first().refTarget)
    }
    
    fun `test canSaveRecipe with valid data`() = runTest {
        viewModel.updateRecipeName("Test Recipe")
        val testAliment = AlimentEv(uuid = "food-1", nom = "Test Food")
        viewModel.selectAlimentToAdd(testAliment)
        viewModel.updateQuantityToAdd("100.0")
        viewModel.addAlimentToRecipe()
        
        assertTrue(viewModel.canSaveRecipe())
    }
    
    fun `test canSaveRecipe with empty name`() = runTest {
        viewModel.updateRecipeName("")
        val testAliment = AlimentEv(uuid = "food-1", nom = "Test Food")
        viewModel.selectAlimentToAdd(testAliment)
        viewModel.updateQuantityToAdd("100.0")
        viewModel.addAlimentToRecipe()
        
        assertFalse(viewModel.canSaveRecipe())
    }
    
    fun `test canSaveRecipe with no ingredients`() = runTest {
        viewModel.updateRecipeName("Test Recipe")
        
        assertFalse(viewModel.canSaveRecipe())
    }
    
    fun `test canSaveRecipe with quantity error`() = runTest {
        viewModel.updateRecipeName("Test Recipe")
        val testAliment = AlimentEv(uuid = "food-1", nom = "Test Food")
        viewModel.selectAlimentToAdd(testAliment)
        viewModel.updateQuantityToAdd("0")
        viewModel.addAlimentToRecipe()
        
        assertFalse(viewModel.canSaveRecipe())
    }
    
    fun `test hasRecipeChanged for new recipe`() = runTest {
        viewModel.startCreatingRecipe()
        assertTrue(viewModel.hasRecipeChanged())
    }
    
    fun `test hasRecipeChanged for existing recipe without changes`() = runTest {
        val testRecipe = Ration(
            uuid = "test-uuid",
            name = "Test Recipe",
            espece = "CHIEN",
            alimentMutableList = mutableListOf()
        )
        
        viewModel.startEditingRecipe(testRecipe)
        assertFalse(viewModel.hasRecipeChanged())
    }
    
    fun `test hasRecipeChanged for existing recipe with name change`() = runTest {
        val testRecipe = Ration(
            uuid = "test-uuid",
            name = "Test Recipe",
            espece = "CHIEN",
            alimentMutableList = mutableListOf()
        )
        
        viewModel.startEditingRecipe(testRecipe)
        viewModel.updateRecipeName("Modified Recipe")
        
        assertTrue(viewModel.hasRecipeChanged())
    }
    
    fun `test getFilteredRecipes with empty search`() = runTest {
        val testRecipes = listOf(
            Ration(uuid = "1", name = "Recipe 1"),
            Ration(uuid = "2", name = "Recipe 2")
        )
        
        viewModel.recipes.addAll(testRecipes)
        
        val filtered = viewModel.getFilteredRecipes()
        assertEquals(2, filtered.size)
    }
    
    fun `test getFilteredRecipes with search query`() = runTest {
        val testRecipes = listOf(
            Ration(uuid = "1", name = "Chicken Recipe"),
            Ration(uuid = "2", name = "Beef Recipe"),
            Ration(uuid = "3", name = "Fish Recipe")
        )
        
        viewModel.recipes.addAll(testRecipes)
        viewModel.updateSearchQuery("chicken")
        
        val filtered = viewModel.getFilteredRecipes()
        assertEquals(1, filtered.size)
        assertEquals("Chicken Recipe", filtered.first().name)
    }
    
    fun `test showDeleteConfirmation`() = runTest {
        val testRecipe = Ration(uuid = "test-uuid", name = "Test Recipe")
        
        viewModel.showDeleteConfirmation(testRecipe)
        
        assertEquals(testRecipe, viewModel.showDeleteConfirmation.value)
    }
    
    fun `test hideDeleteConfirmation`() = runTest {
        val testRecipe = Ration(uuid = "test-uuid", name = "Test Recipe")
        viewModel.showDeleteConfirmation(testRecipe)
        
        viewModel.hideDeleteConfirmation()
        
        assertNull(viewModel.showDeleteConfirmation.value)
    }
    
    fun `test showSaveConfirmation`() = runTest {
        viewModel.showSaveConfirmation()
        assertTrue(viewModel.showSaveConfirmation.value)
    }
    
    fun `test hideSaveConfirmation`() = runTest {
        viewModel.showSaveConfirmation()
        viewModel.hideSaveConfirmation()
        assertFalse(viewModel.showSaveConfirmation.value)
    }
    
    fun `test clearMessage`() = runTest {
        viewModel.message.value = "Test message"
        viewModel.clearMessage()
        assertNull(viewModel.message.value)
    }
}
