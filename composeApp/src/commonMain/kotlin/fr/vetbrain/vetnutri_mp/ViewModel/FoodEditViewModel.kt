package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.compose.runtime.mutableStateListOf
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalUuidApi::class)
class FoodEditViewModel(
        private val foodRepository: FoodRepository,
        private val alimentUuid: String? = null
) {
    private val coroutineScope = CoroutineScope(AppDispatchers.Main)

    // Créer un aliment par défaut avec un UUID aléatoire
    private val defaultAliment = AlimentEv(uuid = Uuid.random().toString(), rationUUID = null)

    private val _alimentState: MutableStateFlow<AlimentEv> = MutableStateFlow(defaultAliment)
    val alimentState: StateFlow<AlimentEv> = _alimentState.asStateFlow()

    // Liste des nutriments disponibles
    private val _allNutrients = mutableStateListOf<Nutrient>()

    init {
        // Charger tous les types de nutriments
        loadNutrients()

        // Si un UUID est fourni, charger l'aliment correspondant
        if (!alimentUuid.isNullOrBlank()) {
            loadAliment(alimentUuid)
        }
    }

    private fun loadNutrients() {
        // Charger tous les types de nutriments
        
        // Debug temporaire pour DM
        println("🔍 DEBUG DM dans FoodEditViewModel.loadNutrients():")
        println("  - NutrientMain.entries.size: ${NutrientMain.entries.size}")
        println("  - NutrientMain.entries: ${NutrientMain.entries.map { it.label }}")
        val dmNutrient = NutrientMain.entries.find { it.label == "DM" }
        if (dmNutrient != null) {
            println("  - DM trouvé: ${dmNutrient.label}")
        } else {
            println("  - DM NON trouvé dans NutrientMain.entries!")
        }

        // Nutriments principaux
        NutrientMain.entries.forEach { nutrient ->
            if (!_allNutrients.contains(nutrient)) {
                _allNutrients.add(nutrient)
            }
        }

        // Lipides
        NutrientLipid.entries.forEach { nutrient ->
            if (!_allNutrients.contains(nutrient)) {
                _allNutrients.add(nutrient)
            }
        }

        // Macronutriments
        NutrientMacro.entries.forEach { nutrient ->
            if (!_allNutrients.contains(nutrient)) {
                _allNutrients.add(nutrient)
            }
        }

        // Minéraux
        NutrientMin.entries.forEach { nutrient ->
            if (!_allNutrients.contains(nutrient)) {
                _allNutrients.add(nutrient)
            }
        }

        // Vitamines
        NutrientVitam.entries.forEach { nutrient ->
            if (!_allNutrients.contains(nutrient)) {
                _allNutrients.add(nutrient)
            }
        }

        // Acides aminés
        AAEnum.entries.forEach { nutrient ->
            if (!_allNutrients.contains(nutrient)) {
                _allNutrients.add(nutrient)
            }
        }

        // Autres nutriments
        NutrientOther.entries.forEach { nutrient ->
            if (!_allNutrients.contains(nutrient)) {
                _allNutrients.add(nutrient)
            }
        }
    }

    private fun loadAliment(uuid: String) {
        coroutineScope.launch {
            try {
                val aliment = foodRepository.getFood(uuid)
                if (aliment != null) {
                    _alimentState.value = aliment

                    // S'assurer que la liste des nutriments est chargée
                    if (_allNutrients.isEmpty()) {
                        loadNutrients()
                    }

                    // Collecter les labels existants pour éviter les doublons
                    val existingLabels = _allNutrients.map { it.label }.toSet()

                    // Ajouter uniquement les nutriments qui ne sont pas déjà dans la liste
                    aliment.valMap.keys.forEach { nutrient ->
                        if (nutrient.label !in existingLabels) {
                            _allNutrients.add(nutrient)
                        }
                    }
                } else {}
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getAllNutrients(): List<Nutrient> {
        // Debug temporaire pour DM
        println("🔍 DEBUG DM dans FoodEditViewModel.getAllNutrients():")
        println("  - _allNutrients.size: ${_allNutrients.size}")
        println("  - _allNutrients.isEmpty(): ${_allNutrients.isEmpty()}")
        if (_allNutrients.isEmpty()) {
            println("  - Retour des clés de l'aliment actuel: ${_alimentState.value.valMap.keys.size}")
            return _alimentState.value.valMap.keys.toList()
        }
        println("  - Retour de _allNutrients: ${_allNutrients.size}")
        return _allNutrients
    }

    suspend fun saveAliment(aliment: AlimentEv) {
        try {
            // Vérifier si c'est un nouvel aliment ou une mise à jour
            val existingAliment =
                    try {
                        foodRepository.getFoodById(aliment.uuid)
                    } catch (e: Exception) {
                        null
                    }

            // Sauvegarder l'aliment selon le cas
            if (existingAliment != null) {
                // Aliment existant : mise à jour
                foodRepository.updateFood(aliment)
            } else {
                // Nouvel aliment : insertion
                foodRepository.insertFood(aliment)
            }

            // Mettre à jour l'état local
            _alimentState.value = aliment
        } catch (e: Exception) {
            e.printStackTrace()
            throw e // Relancer l'exception pour que la vue puisse la gérer
        }
    }
}
