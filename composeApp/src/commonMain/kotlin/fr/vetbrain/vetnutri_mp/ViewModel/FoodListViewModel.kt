package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Repository.DatabaseFoodRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FoodListViewModel(private val foodRepository: DatabaseFoodRepository) {
    // Scope pour les coroutines du ViewModel
    private val viewModelScope = CoroutineScope(AppDispatchers.Main)

    // Liste des aliments
    private val _foods = MutableStateFlow<List<AlimentEv>>(emptyList())
    val foods: StateFlow<List<AlimentEv>> = _foods.asStateFlow()

    // Filtre de recherche
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtre par type d'aliment
    private val _selectedFoodType = MutableStateFlow<FoodKind?>(null)
    val selectedFoodType: StateFlow<FoodKind?> = _selectedFoodType.asStateFlow()

    // Filtre par groupe d'aliment
    private val _selectedFoodGroup = MutableStateFlow<GroupAlim?>(null)
    val selectedFoodGroup: StateFlow<GroupAlim?> = _selectedFoodGroup.asStateFlow()

    // Filtre par espèce
    private val _selectedEspece = MutableStateFlow<Espece?>(null)
    val selectedEspece: StateFlow<Espece?> = _selectedEspece.asStateFlow()

    // Liste des types d'aliments disponibles
    private val _availableFoodTypes = MutableStateFlow<List<FoodKind?>>(emptyList())
    val availableFoodTypes: StateFlow<List<FoodKind?>> = _availableFoodTypes.asStateFlow()

    // Liste des groupes d'aliments disponibles
    private val _availableFoodGroups = MutableStateFlow<List<GroupAlim?>>(emptyList())
    val availableFoodGroups: StateFlow<List<GroupAlim?>> = _availableFoodGroups.asStateFlow()

    // Liste des espèces disponibles
    val availableEspeces: List<Espece?> = listOf(null) + Espece.entries

    /** Charge la liste des aliments depuis le repository */
    suspend fun loadFoods() {
        val allFoods = foodRepository.getAllFoods()
        _foods.value = filterFoods(allFoods)

        // Charger les types d'aliments disponibles
        val foodTypes = allFoods.mapNotNull { it.typeAliment }.distinct().sorted()
        _availableFoodTypes.value = listOf(null) + foodTypes

        // Charger les groupes d'aliments disponibles
        val foodGroups = allFoods.mapNotNull { it.group }.distinct().sorted()
        _availableFoodGroups.value = listOf(null) + foodGroups
    }

    /** Définit le filtre de recherche */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        viewModelScope.launch { refreshFilteredFoods() }
    }

    /** Définit le filtre par type d'aliment */
    fun setSelectedFoodType(foodType: FoodKind?) {
        _selectedFoodType.value = foodType
        viewModelScope.launch { refreshFilteredFoods() }
    }

    /** Définit le filtre par groupe d'aliment */
    fun setSelectedFoodGroup(foodGroup: GroupAlim?) {
        _selectedFoodGroup.value = foodGroup
        viewModelScope.launch { refreshFilteredFoods() }
    }

    /** Définit le filtre par espèce */
    fun setSelectedEspece(espece: Espece?) {
        _selectedEspece.value = espece
        viewModelScope.launch { refreshFilteredFoods() }
    }

    /** Rafraîchit la liste filtrée des aliments */
    private suspend fun refreshFilteredFoods() {
        val allFoods = foodRepository.getAllFoods()
        _foods.value = filterFoods(allFoods)
    }

    /** Filtre les aliments selon les critères de recherche */
    private fun filterFoods(foods: List<AlimentEv>): List<AlimentEv> {
        return foods
                .filter { food ->
                    // Filtre par recherche textuelle
                    val matchesSearch =
                            searchQuery.value.isEmpty() ||
                                    food.nom?.contains(searchQuery.value, ignoreCase = true) ==
                                            true ||
                                    food.brand?.contains(searchQuery.value, ignoreCase = true) ==
                                            true ||
                                    food.ingredients?.contains(
                                            searchQuery.value,
                                            ignoreCase = true
                                    ) == true

                    // Filtre par type d'aliment
                    val matchesFoodType =
                            selectedFoodType.value == null ||
                                    food.typeAliment == selectedFoodType.value

                    // Filtre par groupe d'aliment
                    val matchesFoodGroup =
                            selectedFoodGroup.value == null || food.group == selectedFoodGroup.value

                    // Filtre par espèce
                    val matchesEspece =
                            selectedEspece.value == null ||
                                    food.especes.any { it == selectedEspece.value?.name }

                    matchesSearch && matchesFoodType && matchesFoodGroup && matchesEspece
                }
                .sortedBy { it.nom }
    }

    /** Supprime un aliment */
    suspend fun deleteFood(food: AlimentEv) {
        foodRepository.deleteFood(food.uuid)
        loadFoods()
    }
}
