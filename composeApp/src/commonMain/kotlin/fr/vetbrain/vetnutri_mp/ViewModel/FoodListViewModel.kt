package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Repository.DatabaseFoodRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FoodListViewModel(private val foodRepository: DatabaseFoodRepository) {
        // Scope pour les coroutines du ViewModel
        private val viewModelScope = CoroutineScope(AppDispatchers.Main)

        // Liste des aliments (non filtrée)
        private val _allFoods = MutableStateFlow<List<AlimentEv>>(emptyList())

        // Liste des aliments filtrés
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

        // Filtre par indication
        private val _selectedIndication = MutableStateFlow<AlimIndic?>(null)
        val selectedIndication: StateFlow<AlimIndic?> = _selectedIndication.asStateFlow()

        // Filtre par base de données
        private val _selectedDataB = MutableStateFlow<String?>(null)
        val selectedDataB: StateFlow<String?> = _selectedDataB.asStateFlow()

        // Liste des types d'aliments disponibles
        private val _availableFoodTypes = MutableStateFlow<List<FoodKind?>>(emptyList())
        val availableFoodTypes: StateFlow<List<FoodKind?>> = _availableFoodTypes.asStateFlow()

        // Liste des groupes d'aliments disponibles
        private val _availableFoodGroups = MutableStateFlow<List<GroupAlim?>>(emptyList())
        val availableFoodGroups: StateFlow<List<GroupAlim?>> = _availableFoodGroups.asStateFlow()

        // Liste des indications disponibles
        private val _availableIndications = MutableStateFlow<List<AlimIndic?>>(emptyList())
        val availableIndications: StateFlow<List<AlimIndic?>> = _availableIndications.asStateFlow()

        // Liste des espèces disponibles
        val availableEspeces: List<Espece?> = listOf(null) + Espece.entries

        init {
                // S'abonner au Flow réactif des aliments pour des mises à jour automatiques
                foodRepository
                        .observeAllFoods()
                        .onEach { allFoods ->
                                // Stocker tous les aliments non filtrés
                                _allFoods.value = allFoods

                                // Filtrer les aliments selon les critères actuels
                                val filteredFoods = filterFoods(allFoods)

                                // Mettre à jour l'état filtré
                                _foods.value = filteredFoods

                                // Mettre à jour les listes de valeurs disponibles pour les filtres
                                updateAvailableFilterValues(allFoods)
                        }
                        .catch { e ->
                                e.printStackTrace()
                                _allFoods.value = emptyList()
                                _foods.value = emptyList()
                        }
                        .launchIn(viewModelScope)
        }

        /** Charge la liste des aliments depuis le repository (maintenu pour compatibilité) */
        fun loadFoods() {
                viewModelScope.launch {
                        val allFoodsLight = foodRepository.getAllFoodsLight()
                        val allFoods = allFoodsLight.map { light ->
                                fr.vetbrain.vetnutri_mp.Data.AlimentEv(
                                        uuid = light.uuid,
                                        nom = light.nom,
                                        brand = light.brand,
                                        group = light.group,
                                        typeAliment = light.typeAliment,
                                        gamme = light.gamme,
                                        deprecated = light.deprecated,
                                        especes = light.especes.toMutableList(),
                                        indicat = light.indicat.toMutableList(),
                                        valMap = mutableMapOf()
                                )
                        }

                        // Stocker tous les aliments non filtrés
                        _allFoods.value = allFoods

                        // Filtrer les aliments selon les critères actuels
                        val filteredFoods = filterFoods(allFoods)

                        // Mettre à jour l'état filtré
                        _foods.value = filteredFoods

                        // Mettre à jour les listes de valeurs disponibles pour les filtres
                        updateAvailableFilterValues(allFoods)
                }
        }

        /** Force le rechargement des données depuis le repository */
        fun forceRefresh() {
                viewModelScope.launch {
                        // Forcer le rechargement des données
                        val allFoods = foodRepository.getAllFoods()

                        // Stocker tous les aliments non filtrés
                        _allFoods.value = allFoods

                        // Filtrer les aliments selon les critères actuels
                        val filteredFoods = filterFoods(allFoods)

                        // Mettre à jour l'état filtré
                        _foods.value = filteredFoods

                        // Mettre à jour les listes de valeurs disponibles pour les filtres
                        updateAvailableFilterValues(allFoods)
                }
        }

        /** Met à jour les listes de valeurs disponibles pour les filtres */
        private fun updateAvailableFilterValues(allFoods: List<AlimentEv>) {
                // Charger les types d'aliments disponibles
                val foodTypes = allFoods.mapNotNull { it.typeAliment }.distinct().sorted()
                _availableFoodTypes.value = listOf(null) + foodTypes

                // Charger les groupes d'aliments disponibles
                val foodGroups = allFoods.mapNotNull { it.group }.distinct().sorted()
                _availableFoodGroups.value = listOf(null) + foodGroups

                // Charger les indications disponibles
                val indications = allFoods.flatMap { it.indicat }.distinct().sorted()
                _availableIndications.value = listOf(null) + indications
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

        /** Définit le filtre par indication */
        fun setSelectedIndication(indication: AlimIndic?) {
                _selectedIndication.value = indication
                viewModelScope.launch { refreshFilteredFoods() }
        }

        /** Définit le filtre par base de données */
        fun setSelectedDataB(dataB: String?) {
                _selectedDataB.value = dataB
                viewModelScope.launch { refreshFilteredFoods() }
        }

        /** Rafraîchit la liste filtrée des aliments */
        private suspend fun refreshFilteredFoods() {
                // Appliquer les filtres sur les données stockées localement
                _foods.value = filterFoods(_allFoods.value)
        }

        /** Filtre les aliments selon les critères de recherche */
        private fun filterFoods(foods: List<AlimentEv>): List<AlimentEv> {
                return foods
                        .filter { aliment ->
                                // Filtre par recherche textuelle avec recherche multi-mots (OR)
                                val matchesSearch =
                                        if (searchQuery.value.isEmpty()) true
                                        else {
                                                val searchWords =
                                                        searchQuery
                                                                .value
                                                                .trim()
                                                                .split("\\s+".toRegex())
                                                                .filter { it.isNotEmpty() }
                                                                .map { it.lowercase() }

                                                if (searchWords.isEmpty()) true
                                                else {
                                                        // Au moins un mot doit être trouvé dans au
                                                        // moins un des champs
                                                        searchWords.any { word ->
                                                                aliment.nom
                                                                        ?.lowercase()
                                                                        ?.contains(word) == true ||
                                                                        aliment.brand
                                                                                ?.lowercase()
                                                                                ?.contains(word) ==
                                                                                true ||
                                                                        aliment.gamme
                                                                                ?.lowercase()
                                                                                ?.contains(word) ==
                                                                                true ||
                                                                        aliment.ingredients
                                                                                ?.lowercase()
                                                                                ?.contains(word) ==
                                                                                true
                                                        }
                                                }
                                        }

                                // Filtre par type d'aliment
                                val matchesFoodType =
                                        selectedFoodType.value == null ||
                                                selectedFoodType.value == FoodKind.ALL ||
                                                aliment.typeAliment == selectedFoodType.value

                                // Filtre par groupe d'aliment
                                val matchesFoodGroup =
                                        selectedFoodGroup.value == null ||
                                                selectedFoodGroup.value == GroupAlim.ALL ||
                                                aliment.group == selectedFoodGroup.value

                                // Filtre par espèce avec traitement amélioré
                                val matchesEspece =
                                        if (selectedEspece.value == null ||
                                                        selectedEspece.value == Espece.CH
                                        ) {
                                                true // Aucun filtre d'espèce sélectionné ou toutes
                                                // les espèces acceptées
                                        } else {
                                                aliment.especes.any { especeStr ->
                                                        try {
                                                                // 1. Vérifier correspondance
                                                                // directe avec le nom de l'enum ou
                                                                // label
                                                                if (especeStr ==
                                                                                selectedEspece
                                                                                        .value
                                                                                        ?.name ||
                                                                                especeStr ==
                                                                                        selectedEspece
                                                                                                .value
                                                                                                ?.label ||
                                                                                especeStr ==
                                                                                        selectedEspece
                                                                                                .value
                                                                                                ?.id ||
                                                                                especeStr ==
                                                                                        selectedEspece
                                                                                                .value
                                                                                                ?.ordinal
                                                                                                .toString()
                                                                ) {
                                                                        return@any true
                                                                }

                                                                // 2. Vérifier correspondance
                                                                // insensible à la casse
                                                                if (especeStr.equals(
                                                                                selectedEspece
                                                                                        .value
                                                                                        ?.name,
                                                                                ignoreCase = true
                                                                        ) ||
                                                                                especeStr.equals(
                                                                                        selectedEspece
                                                                                                .value
                                                                                                ?.label,
                                                                                        ignoreCase =
                                                                                                true
                                                                                )
                                                                ) {
                                                                        return@any true
                                                                }

                                                                // 3. Utiliser la fonction
                                                                // centralisée pour obtenir
                                                                // l'énumération
                                                                val especeEnum =
                                                                        Espece.getFromString(
                                                                                especeStr
                                                                        )

                                                                // 4. Comparer l'énumération
                                                                // récupérée avec celle sélectionnée
                                                                if (especeEnum != null &&
                                                                                especeEnum ==
                                                                                        selectedEspece
                                                                                                .value
                                                                ) {
                                                                        return@any true
                                                                }

                                                                false // Aucune correspondance
                                                                // trouvée
                                                        } catch (e: Exception) {
                                                                // En cas d'erreur dans les
                                                                // conversions, on vérifie
                                                                // simplement l'égalité des chaînes
                                                                especeStr ==
                                                                        selectedEspece
                                                                                .value
                                                                                ?.name ||
                                                                        especeStr ==
                                                                                selectedEspece
                                                                                        .value
                                                                                        ?.label ||
                                                                        especeStr.equals(
                                                                                selectedEspece
                                                                                        .value
                                                                                        ?.name,
                                                                                ignoreCase = true
                                                                        ) ||
                                                                        especeStr.equals(
                                                                                selectedEspece
                                                                                        .value
                                                                                        ?.label,
                                                                                ignoreCase = true
                                                                        )
                                                        }
                                                }
                                        }

                                // Filtre par indication
                                val matchesIndication =
                                        if (selectedIndication.value == null ||
                                                        selectedIndication.value == AlimIndic.ALL
                                        ) {
                                                true // Aucun filtre d'indication sélectionné ou
                                                // toutes les indications acceptées
                                        } else {
                                                aliment.indicat.any { indication ->
                                                        indication == selectedIndication.value
                                                }
                                        }

                                // Filtre par base de données
                                val matchesDataB =
                                        if (selectedDataB.value == null || selectedDataB.value == ""
                                        ) {
                                                true // Aucun filtre de base de données sélectionné
                                        } else {
                                                aliment.dataB?.trim() == selectedDataB.value?.trim()
                                        }

                                matchesSearch &&
                                        matchesFoodType &&
                                        matchesFoodGroup &&
                                        matchesEspece &&
                                        matchesIndication &&
                                        matchesDataB
                        }
                        .sortedBy { it.nom }
        }

        /** Supprime un aliment */
        fun deleteFood(food: AlimentEv) {
                viewModelScope.launch {
                        foodRepository.deleteFood(food.uuid)
                        // Plus besoin d'appeler loadFoods() car le Flow se met à jour
                        // automatiquement
                }
        }
}
