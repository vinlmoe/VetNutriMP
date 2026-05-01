package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Repository.DatabaseFoodRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Liste des aliments avec filtres réactifs.
 * - S'abonne au flow `observeAllFoods` et dérive les filtres en mémoire.
 * - N'étend pas `ViewModel` pour rester multiplateforme; utilise un scope dédié.
 */
class FoodListViewModel(private val foodRepository: DatabaseFoodRepository) {
        // Scope dédié (pas de ViewModel Android ici)
        private val job = SupervisorJob()
        private val viewModelScope = CoroutineScope(AppDispatchers.Main + job)

        // Index normalisé pour la recherche textuelle (champs lowercase pré-calculés)
        private data class NormalizedFood(
                val aliment: AlimentEv,
                val nomLower: String,
                val brandLower: String,
                val gammeLower: String,
                val ingredientsLower: String,
                val especeStrings: Set<String>  // valeurs lowercase préparées pour le match espèce
        )
        private var normalizedIndex: List<NormalizedFood> = emptyList()

        // Liste des aliments (non filtrée)
        private val _allFoods = MutableStateFlow<List<AlimentEv>>(emptyList())
        val allFoods: StateFlow<List<AlimentEv>> = _allFoods.asStateFlow()

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
                                _allFoods.value = allFoods
                                // Construire l'index normalisé une seule fois par chargement
                                normalizedIndex = buildNormalizedIndex(allFoods)
                                _foods.value = filterFoods(allFoods)
                                updateAvailableFilterValues(allFoods)
                        }
                        .catch { e ->
                                e.printStackTrace()
                                _allFoods.value = emptyList()
                                _foods.value = emptyList()
                        }
                        .launchIn(viewModelScope)

                // Debouncing sur la recherche textuelle : évite un refiltre à chaque frappe
                _searchQuery
                        .debounce(300)
                        .onEach { refreshFilteredFoods() }
                        .launchIn(viewModelScope)
        }

        /** Construit un index avec champs lowercase pré-calculés pour le filtrage rapide */
        private fun buildNormalizedIndex(foods: List<AlimentEv>): List<NormalizedFood> =
                foods.map { a ->
                        NormalizedFood(
                                aliment = a,
                                nomLower = a.nom?.lowercase() ?: "",
                                brandLower = a.brand?.lowercase() ?: "",
                                gammeLower = a.gamme?.lowercase() ?: "",
                                ingredientsLower = a.ingredients?.lowercase() ?: "",
                                especeStrings = a.especes.map { it.lowercase() }.toSet()
                        )
                }

        /** Charge la liste des aliments depuis le repository (maintenu pour compatibilité) */
        fun loadFoods() {
                viewModelScope.launch {
                        val allFoods = foodRepository.getAllFoodsLight().map { light ->
                                fr.vetbrain.vetnutri_mp.Data.AlimentEv(
                                        uuid = light.uuid,
                                        nom = light.nom,
                                        brand = light.brand,
                                        group = light.group,
                                        typeAliment = light.typeAliment,
                                        gamme = light.gamme,
                                        deprecated = light.deprecated,
                                        dataB = light.dataB,
                                        especes = light.especes.toMutableList(),
                                        indicat = light.indicat.toMutableList(),
                                        valMap = mutableMapOf()
                                )
                        }
                        _allFoods.value = allFoods
                        normalizedIndex = buildNormalizedIndex(allFoods)
                        _foods.value = filterFoods(allFoods)
                        updateAvailableFilterValues(allFoods)
                }
        }

        /** Force le rechargement des données depuis le repository */
        fun forceRefresh() = loadFoods()

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

        /** Définit le filtre de recherche — le debounce gère le refiltre */
        fun setSearchQuery(query: String) {
                _searchQuery.value = query
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

        /** Filtre les aliments selon les critères de recherche — utilise l'index normalisé */
        private fun filterFoods(foods: List<AlimentEv>): List<AlimentEv> {
                val query = searchQuery.value
                val searchWords = if (query.isBlank()) emptyList()
                else query.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }.map { it.lowercase() }

                val targetEspece = selectedEspece.value
                val filterEspece = targetEspece != null && targetEspece != Espece.CH
                // Pré-calculer les identifiants lowercase de l'espèce sélectionnée
                val especeKeys: Set<String> = if (filterEspece && targetEspece != null) {
                        setOfNotNull(
                                targetEspece.name.lowercase(),
                                targetEspece.label.lowercase(),
                                targetEspece.id.lowercase()
                        )
                } else emptySet()

                // Utiliser l'index normalisé si disponible, sinon la liste brute
                val source = if (normalizedIndex.size == foods.size) normalizedIndex
                             else buildNormalizedIndex(foods)

                return source
                        .filter { norm ->
                                val aliment = norm.aliment

                                // Filtre textuel sur champs pré-normalisés
                                val matchesSearch = searchWords.isEmpty() || searchWords.any { word ->
                                        norm.nomLower.contains(word) ||
                                        norm.brandLower.contains(word) ||
                                        norm.gammeLower.contains(word) ||
                                        norm.ingredientsLower.contains(word)
                                }

                                val matchesFoodType = selectedFoodType.value == null ||
                                        selectedFoodType.value == FoodKind.ALL ||
                                        aliment.typeAliment == selectedFoodType.value

                                val matchesFoodGroup = selectedFoodGroup.value == null ||
                                        selectedFoodGroup.value == GroupAlim.ALL ||
                                        aliment.group == selectedFoodGroup.value

                                // Filtre espèce sans try-catch : lookup dans les clés pré-calculées
                                val matchesEspece = !filterEspece || norm.especeStrings.any { s ->
                                        especeKeys.contains(s) || Espece.getFromString(s) == targetEspece
                                }

                                val matchesIndication = selectedIndication.value == null ||
                                        selectedIndication.value == AlimIndic.ALL ||
                                        aliment.indicat.any { it == selectedIndication.value }

                                val matchesDataB = selectedDataB.value.isNullOrEmpty() ||
                                        aliment.dataB?.trim() == selectedDataB.value?.trim()

                                matchesSearch && matchesFoodType && matchesFoodGroup &&
                                        matchesEspece && matchesIndication && matchesDataB
                        }
                        .map { it.aliment }
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

        /** Charge les nutriments spécifiés pour une liste d'aliments depuis la base de données */
        suspend fun loadNutrientsForFoods(
                foodUuids: List<String>,
                nutrients: List<fr.vetbrain.vetnutri_mp.Enumer.Nutrient>
        ): Map<String, Map<fr.vetbrain.vetnutri_mp.Enumer.Nutrient, Double>> {
                return foodRepository.loadNutrientsForFoods(foodUuids, nutrients)
        }

        fun clear() {
                viewModelScope.cancel()
        }
}
