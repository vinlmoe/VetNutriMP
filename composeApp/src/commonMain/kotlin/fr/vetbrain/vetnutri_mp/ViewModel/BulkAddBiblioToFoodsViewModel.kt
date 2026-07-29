package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseFoodRepository
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Utils.PlatformDispatcher
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel du module d'ajout en masse de bibliographie aux aliments (accessible depuis
 * Settings). Permet d'appliquer une ou plusieurs [BiblioRef] existantes à un lot d'aliments
 * sélectionnés en une seule opération.
 */
class BulkAddBiblioToFoodsViewModel(
        private val foodRepository: FoodRepository,
        private val biblioRefRepository: BiblioRefRepository,
        private val platformDispatcher: PlatformDispatcher = PlatformDispatcher(),
        private val coroutineContext: CoroutineContext = platformDispatcher.provideMainDispatcher()
) {
        private val scope = CoroutineScope(coroutineContext)

        private val _allFoods = MutableStateFlow<List<AlimentEv>>(emptyList())
        val allFoods: StateFlow<List<AlimentEv>> = _allFoods.asStateFlow()

        private val _availableBiblioRefs = MutableStateFlow<List<BiblioRef>>(emptyList())
        val availableBiblioRefs: StateFlow<List<BiblioRef>> = _availableBiblioRefs.asStateFlow()

        private val _selectedBiblioRefIds = MutableStateFlow<Set<String>>(emptySet())
        val selectedBiblioRefIds: StateFlow<Set<String>> = _selectedBiblioRefIds.asStateFlow()

        private val _loading = MutableStateFlow(false)
        val loading: StateFlow<Boolean> = _loading.asStateFlow()

        private val _error = MutableStateFlow("")
        val error: StateFlow<String> = _error.asStateFlow()

        private val _resultMessage = MutableStateFlow<String?>(null)
        val resultMessage: StateFlow<String?> = _resultMessage.asStateFlow()

        init {
                foodRepository.observeAllFoods().onEach { _allFoods.value = it }.launchIn(scope)
                loadAvailableBiblioRefs()
        }

        fun loadAvailableBiblioRefs() {
                scope.launch {
                        try {
                                biblioRefRepository.getAllBiblioRefs().collect { refs ->
                                        _availableBiblioRefs.value = refs
                                }
                        } catch (e: Exception) {
                                _error.value = "Erreur lors du chargement des bibliographies : ${e.message}"
                        }
                }
        }

        fun toggleBiblioRef(uuid: String) {
                _selectedBiblioRefIds.value =
                        if (_selectedBiblioRefIds.value.contains(uuid)) {
                                _selectedBiblioRefIds.value - uuid
                        } else {
                                _selectedBiblioRefIds.value + uuid
                        }
        }

        fun clearResultMessage() {
                _resultMessage.value = null
        }

        /**
         * Applique les [BiblioRef] sélectionnées aux aliments désignés par [foodUuids], en
         * conservant les bibliographies déjà attachées à chaque aliment (fusion, sans doublon).
         */
        fun applyToFoods(foodUuids: List<String>) {
                if (foodUuids.isEmpty() || _selectedBiblioRefIds.value.isEmpty()) return
                scope.launch {
                        _loading.value = true
                        _error.value = ""
                        try {
                                val selectedRefs =
                                        _availableBiblioRefs.value.filter {
                                                it.uuid in _selectedBiblioRefIds.value
                                        }
                                val isDatabaseRepo = foodRepository is DatabaseFoodRepository
                                if (isDatabaseRepo) (foodRepository as DatabaseFoodRepository).beginBatch()
                                var updatedCount = 0
                                try {
                                        foodUuids.forEach { uuid ->
                                                val existing = foodRepository.getFood(uuid) ?: return@forEach
                                                val mergedBiblioRefs =
                                                        (existing.biblioRefs + selectedRefs).distinctBy { it.uuid }
                                                foodRepository.updateFood(
                                                        existing.copy(biblioRefs = mergedBiblioRefs)
                                                )
                                                updatedCount++
                                        }
                                } finally {
                                        if (isDatabaseRepo) (foodRepository as DatabaseFoodRepository).endBatch()
                                }
                                _resultMessage.value =
                                        "Bibliographie ajoutée à $updatedCount aliment(s)."
                        } catch (e: Exception) {
                                _error.value = "Erreur lors de l'application : ${e.message ?: "Erreur inconnue"}"
                        } finally {
                                _loading.value = false
                        }
                }
        }
}
