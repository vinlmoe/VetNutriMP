package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.text.input.TextFieldValue
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Repository.RecipeRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * ViewModel pour l'édition des recettes
 */
class RecipeEditViewModel(
    private val recipeRepository: RecipeRepository,
    private val foodRepository: FoodRepository
) {
    
    // État des recettes
    private val _recipes = mutableStateListOf<Ration>()
    val recipes: SnapshotStateList<Ration> = _recipes
    
    // État de chargement
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    
    // État de la recette en cours d'édition
    private val _editingRecipe = mutableStateOf<Ration?>(null)
    val editingRecipe: State<Ration?> = _editingRecipe
    
    // État du mode d'édition
    private val _isEditMode = mutableStateOf(false)
    val isEditMode: State<Boolean> = _isEditMode
    
    // État des messages
    private val _message = mutableStateOf<String?>(null)
    val message: State<String?> = _message
    
    // État de la recherche
    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery
    
    // État de la recette en cours de création
    private val _newRecipeName = mutableStateOf("")
    val newRecipeName: State<String> = _newRecipeName
    
    // État des aliments sélectionnés pour la recette
    private val _selectedIngredients = mutableStateListOf<AlimentRation>()
    val selectedIngredients: SnapshotStateList<AlimentRation> = _selectedIngredients
    
    // État de l'aliment en cours d'ajout
    private val _alimentToAdd = mutableStateOf<AlimentEv?>(null)
    val alimentToAdd: State<AlimentEv?> = _alimentToAdd
    
    // État de la quantité pour l'aliment à ajouter
    private val _quantityToAdd = mutableStateOf("100")
    val quantityToAdd: State<String> = _quantityToAdd
    
    // État de la cible pour l'aliment à ajouter
    private val _targetToAdd = mutableStateOf(0)
    val targetToAdd: State<Int> = _targetToAdd
    
    // État de l'erreur de quantité
    private val _quantityError = mutableStateOf(false)
    val quantityError: State<Boolean> = _quantityError
    
    // État de la confirmation de suppression
    private val _showDeleteConfirmation = mutableStateOf<Ration?>(null)
    val showDeleteConfirmation: State<Ration?> = _showDeleteConfirmation
    
    // État de la confirmation de sauvegarde
    private val _showSaveConfirmation = mutableStateOf(false)
    val showSaveConfirmation: State<Boolean> = _showSaveConfirmation
    
    init {
        loadRecipes()
    }
    
    /**
     * Charge toutes les recettes depuis le repository
     */
    fun loadRecipes() {
        CoroutineScope(AppDispatchers.IO).launch {
            try {
                _isLoading.value = true
                println("🔍 RecipeEditViewModel: Chargement des recettes...")
                val loadedRecipes = recipeRepository.getAllRecipes()
                println("🔍 RecipeEditViewModel: ${loadedRecipes.size} recettes chargées")
                loadedRecipes.forEach { recipe ->
                    println("🔍 RecipeEditViewModel: Recette: ${recipe.name} (${recipe.alimentMutableList.size} aliments)")
                }
                _recipes.clear()
                _recipes.addAll(loadedRecipes)
                println("🔍 RecipeEditViewModel: ${_recipes.size} recettes dans la liste")
            } catch (e: Exception) {
                println("❌ RecipeEditViewModel: Erreur lors du chargement: ${e.message}")
                _message.value = "Erreur lors du chargement des recettes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Met à jour la requête de recherche
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * Filtre les recettes selon la recherche
     */
    fun getFilteredRecipes(): List<Ration> {
        val query = _searchQuery.value.lowercase()
        return if (query.isEmpty()) {
            _recipes.toList()
        } else {
            _recipes.filter { recipe ->
                recipe.name.lowercase().contains(query) ||
                recipe.alimentMutableList.any { aliment ->
                    aliment.aliment?.nom?.lowercase()?.contains(query) == true
                }
            }
        }
    }
    
    /**
     * Commence l'édition d'une recette
     */
    fun startEditingRecipe(recipe: Ration) {
        _editingRecipe.value = recipe.copy()
        _selectedIngredients.clear()
        _selectedIngredients.addAll(recipe.alimentMutableList.map { it.copy() })
        _isEditMode.value = true
        _newRecipeName.value = recipe.name
    }
    
    /**
     * Commence la création d'une nouvelle recette
     */
    fun startCreatingRecipe() {
        _editingRecipe.value = Ration(
            uuid = "temp_${UUID.randomUUID().toString()}", // UUID temporaire pour distinguer des nouvelles recettes
            name = "",
            espece = "CHIEN", // Espèce par défaut
            description = "",
            alimentMutableList = mutableListOf()
        )
        _selectedIngredients.clear()
        _isEditMode.value = true
        _newRecipeName.value = ""
    }
    
    /**
     * Annule l'édition
     */
    fun cancelEditing() {
        _editingRecipe.value = null
        _selectedIngredients.clear()
        _isEditMode.value = false
        _newRecipeName.value = ""
        _alimentToAdd.value = null
        _quantityToAdd.value = "100"
        _targetToAdd.value = 0
        _quantityError.value = false
    }
    
    /**
     * Met à jour le nom de la recette
     */
    fun updateRecipeName(name: String) {
        _newRecipeName.value = name
    }
    
    /**
     * Sélectionne un aliment à ajouter
     */
    fun selectAlimentToAdd(aliment: AlimentEv?) {
        _alimentToAdd.value = aliment
    }
    
    /**
     * Met à jour la quantité à ajouter
     */
    fun updateQuantityToAdd(quantity: String) {
        _quantityToAdd.value = quantity
        _quantityError.value = try {
            quantity.toDouble() <= 0
        } catch (e: NumberFormatException) {
            true
        }
    }
    
    /**
     * Met à jour la cible à ajouter
     */
    fun updateTargetToAdd(target: Int) {
        _targetToAdd.value = target
    }
    
    /**
     * Ajoute l'aliment sélectionné à la recette
     */
    fun addAlimentToRecipe() {
        val aliment = _alimentToAdd.value
        val quantity = _quantityToAdd.value
        val target = _targetToAdd.value
        
        if (aliment != null && !_quantityError.value) {
            try {
                val quantityValue = quantity.toDouble()
                if (quantityValue > 0) {
                    // Vérifier si l'aliment n'est pas déjà dans la liste
                    val existingAliment = _selectedIngredients.find { it.refAlimUnif == aliment.uuid }
                    if (existingAliment != null) {
                        println("🔍 DEBUG RecipeEditViewModel: Aliment ${aliment.nom} déjà présent dans la recette")
                        _message.value = "Cet aliment est déjà dans la recette"
                        return
                    }
                    
                    val newAlimentRation = AlimentRation(
                        uuid = UUID.randomUUID().toString(),
                        uuidUnif = aliment.uuid,
                        quantite = quantityValue,
                        proportion = 0.0,
                        aliment = aliment,
                        refAlimUnif = aliment.uuid,
                        refRation = null,
                        refTarget = target
                    )
                    
                    _selectedIngredients.add(newAlimentRation)
                    _alimentToAdd.value = null
                    _quantityToAdd.value = "100"
                    _targetToAdd.value = 0
                    println("🔍 DEBUG RecipeEditViewModel: Aliment ${aliment.nom} ajouté à la recette")
                }
            } catch (e: NumberFormatException) {
                _quantityError.value = true
            }
        }
    }
    
    /**
     * Supprime un aliment de la recette
     */
    fun removeAlimentFromRecipe(alimentRation: AlimentRation) {
        _selectedIngredients.remove(alimentRation)
    }
    
    /**
     * Met à jour la quantité d'un aliment dans la recette
     */
    fun updateAlimentQuantity(alimentRation: AlimentRation, newQuantity: String) {
        val index = _selectedIngredients.indexOf(alimentRation)
        if (index != -1) {
            try {
                val quantityValue = newQuantity.toDouble()
                if (quantityValue > 0) {
                    val updatedAliment = alimentRation.copy(quantite = quantityValue)
                    _selectedIngredients[index] = updatedAliment
                }
            } catch (e: NumberFormatException) {
                // Ignore les valeurs invalides
            }
        }
    }
    
    /**
     * Met à jour la cible d'un aliment dans la recette
     */
    fun updateAlimentTarget(alimentRation: AlimentRation, newTarget: Int) {
        val index = _selectedIngredients.indexOf(alimentRation)
        if (index != -1) {
            val updatedAliment = alimentRation.copy(refTarget = newTarget)
            _selectedIngredients[index] = updatedAliment
        }
    }
    
    /**
     * Sauvegarde la recette (création ou modification)
     */
    fun saveRecipe() {
        if (_newRecipeName.value.isBlank()) {
            _message.value = "Le nom de la recette ne peut pas être vide"
            return
        }
        
        if (_selectedIngredients.isEmpty()) {
            _message.value = "La recette doit contenir au moins un aliment"
            return
        }
        
        // Protection contre les appels multiples
        if (_isLoading.value) {
            println("🔍 DEBUG RecipeEditViewModel: Sauvegarde déjà en cours, ignoré")
            return
        }
        
        CoroutineScope(AppDispatchers.IO).launch {
            try {
                _isLoading.value = true
                
                val recipeToSave = _editingRecipe.value?.copy(
                    name = _newRecipeName.value,
                    alimentMutableList = _selectedIngredients.toMutableList()
                ) ?: Ration(
                    uuid = UUID.randomUUID().toString(),
                    name = _newRecipeName.value,
                    espece = "CHIEN",
                    description = "",
                    alimentMutableList = _selectedIngredients.toMutableList()
                )
                
                // Vérifier si c'est une nouvelle recette ou une modification
                val isNewRecipe = _editingRecipe.value?.uuid?.startsWith("temp_") == true || 
                                 _recipes.none { it.uuid == _editingRecipe.value?.uuid }
                
                if (!isNewRecipe && _editingRecipe.value?.uuid != null) {
                    // Modification d'une recette existante
                    recipeRepository.renameRecipe(recipeToSave.uuid, recipeToSave.name)
                    recipeRepository.replaceAliments(recipeToSave.uuid, recipeToSave.alimentMutableList)
                    _message.value = "Recette modifiée avec succès"
                } else {
                    // Création d'une nouvelle recette
                    val newRecipe = recipeRepository.createRecipe(
                        name = recipeToSave.name,
                        espece = recipeToSave.espece,
                        description = recipeToSave.description
                    )
                    recipeRepository.addAliments(newRecipe.uuid, recipeToSave.alimentMutableList)
                    _message.value = "Recette créée avec succès"
                }
                
                // Recharger les recettes et sortir du mode édition
                loadRecipes()
                cancelEditing()
                
            } catch (e: Exception) {
                _message.value = "Erreur lors de la sauvegarde: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Supprime une recette
     */
    fun deleteRecipe(recipe: Ration) {
        CoroutineScope(AppDispatchers.IO).launch {
            try {
                _isLoading.value = true
                recipeRepository.deleteRecipe(recipe.uuid)
                _message.value = "Recette supprimée avec succès"
                loadRecipes()
            } catch (e: Exception) {
                _message.value = "Erreur lors de la suppression: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Duplique une recette
     */
    fun duplicateRecipe(recipe: Ration) {
        CoroutineScope(AppDispatchers.IO).launch {
            try {
                _isLoading.value = true
                val duplicatedRecipe = recipeRepository.cloneRecipe(recipe.uuid)
                if (duplicatedRecipe != null) {
                    _message.value = "Recette dupliquée avec succès"
                } else {
                    _message.value = "Erreur lors de la duplication"
                }
                loadRecipes()
            } catch (e: Exception) {
                _message.value = "Erreur lors de la duplication: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Affiche la confirmation de suppression
     */
    fun showDeleteConfirmation(recipe: Ration) {
        _showDeleteConfirmation.value = recipe
    }
    
    /**
     * Cache la confirmation de suppression
     */
    fun hideDeleteConfirmation() {
        _showDeleteConfirmation.value = null
    }
    
    /**
     * Affiche la confirmation de sauvegarde
     */
    fun showSaveConfirmation() {
        _showSaveConfirmation.value = true
    }
    
    /**
     * Cache la confirmation de sauvegarde
     */
    fun hideSaveConfirmation() {
        _showSaveConfirmation.value = false
    }
    
    /**
     * Efface le message affiché
     */
    fun clearMessage() {
        _message.value = null
    }
    
    /**
     * Vérifie si la recette peut être sauvegardée
     */
    fun canSaveRecipe(): Boolean {
        return _newRecipeName.value.isNotBlank() && 
               _selectedIngredients.isNotEmpty() && 
               !_quantityError.value
    }
    
    /**
     * Vérifie si la recette a été modifiée
     */
    fun hasRecipeChanged(): Boolean {
        val originalRecipe = _editingRecipe.value
        if (originalRecipe == null) return true // Nouvelle recette
        
        return originalRecipe.name != _newRecipeName.value ||
               originalRecipe.alimentMutableList.size != _selectedIngredients.size ||
               originalRecipe.alimentMutableList.any { original ->
                   val matching = _selectedIngredients.find { it.uuidUnif == original.uuidUnif }
                   matching == null || 
                   matching.quantite != original.quantite ||
                   matching.refTarget != original.refTarget
               }
    }
}
