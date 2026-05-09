package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.compose.runtime.mutableStateListOf
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalUuidApi::class)
class FoodEditViewModel(
        private val foodRepository: FoodRepository,
        private val alimentUuid: String? = null,
        private val biblioRefRepository: BiblioRefRepository? = null
) {
    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(AppDispatchers.Main + job)

    // Créer un aliment par défaut avec un UUID aléatoire
    private val defaultAliment = AlimentEv(uuid = Uuid.random().toString(), rationUUID = null)

    private val _alimentState: MutableStateFlow<AlimentEv> = MutableStateFlow(defaultAliment)
    val alimentState: StateFlow<AlimentEv> = _alimentState.asStateFlow()

    // Références bibliographiques
    private val _selectedBiblioRefs = MutableStateFlow<List<BiblioRef>>(emptyList())
    val selectedBiblioRefs: StateFlow<List<BiblioRef>> = _selectedBiblioRefs.asStateFlow()

    private val _availableBiblioRefs = MutableStateFlow<List<BiblioRef>>(emptyList())
    val availableBiblioRefs: StateFlow<List<BiblioRef>> = _availableBiblioRefs.asStateFlow()

    // Liste des nutriments disponibles
    private val _allNutrients = mutableStateListOf<Nutrient>()

    init {
        // Charger tous les types de nutriments
        loadNutrients()
        preloadCustomNutrientsFromRepository()
        loadAvailableBiblioRefs()

        // Si un UUID est fourni, charger l'aliment correspondant
        if (!alimentUuid.isNullOrBlank()) {
            loadAliment(alimentUuid)
        }
    }

    private fun loadNutrients() {
        // Charger tous les types de nutriments
        
        // Debug temporaire pour DM
        val dmNutrient = NutrientMain.entries.find { it.label == "DM" }
        if (dmNutrient != null) {
        } else {
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

        // Nutriments personnalisés déjà connus dans la session
        CustomNutrientRegistry.all().forEach { nutrient ->
            if (!_allNutrients.any { it.label == nutrient.label }) {
                _allNutrients.add(nutrient)
            }
        }
    }

    private fun loadAvailableBiblioRefs() {
        coroutineScope.launch {
            try {
                biblioRefRepository?.getAllBiblioRefs()?.collect { refs ->
                    _availableBiblioRefs.value = refs
                }
            } catch (_: Exception) {}
        }
    }

    private fun loadAliment(uuid: String) {
        coroutineScope.launch {
            try {
                val aliment = foodRepository.getFood(uuid)
                if (aliment != null) {
                    _alimentState.value = aliment
                    _selectedBiblioRefs.value = aliment.biblioRefs

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
                        if (nutrient is CustomNutrient) {
                            CustomNutrientRegistry.register(nutrient)
                        }
                    }
                } else {}
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun preloadCustomNutrientsFromRepository() {
        coroutineScope.launch {
            try {
                val foods = foodRepository.getAllFoods()
                foods.forEach { food ->
                    food.valMap.keys.forEach { nutrient ->
                        if (nutrient is CustomNutrient) {
                            CustomNutrientRegistry.register(nutrient)
                        } else if (
                            NutrientMain.entries.none { it.label.equals(nutrient.label, ignoreCase = true) } &&
                            NutrientMacro.entries.none { it.label.equals(nutrient.label, ignoreCase = true) } &&
                            NutrientMin.entries.none { it.label.equals(nutrient.label, ignoreCase = true) } &&
                            NutrientLipid.entries.none { it.label.equals(nutrient.label, ignoreCase = true) } &&
                            NutrientVitam.entries.none { it.label.equals(nutrient.label, ignoreCase = true) } &&
                            NutrientOther.entries.none { it.label.equals(nutrient.label, ignoreCase = true) } &&
                            AAEnum.entries.none { it.label.equals(nutrient.label, ignoreCase = true) } &&
                            NutrientEnergy.entries.none { it.label.equals(nutrient.label, ignoreCase = true) } &&
                            NutrientAnalysis.entries.none { it.label.equals(nutrient.label, ignoreCase = true) }
                        ) {
                            CustomNutrientRegistry.register(CustomNutrient.fromLabel(nutrient.label))
                        }
                    }
                }
                CustomNutrientRegistry.all().forEach { nutrient ->
                    if (!_allNutrients.any { it.label == nutrient.label }) {
                        _allNutrients.add(nutrient)
                    }
                }
            } catch (_: Exception) {
                // Ne bloque pas l'édition si le préchargement échoue
            }
        }
    }

    fun getAllNutrients(): List<Nutrient> {
        CustomNutrientRegistry.all().forEach { nutrient ->
            if (!_allNutrients.any { it.label == nutrient.label }) {
                _allNutrients.add(nutrient)
            }
        }
        if (_allNutrients.isEmpty()) {
            return _alimentState.value.valMap.keys.toList()
        }
        return _allNutrients
    }

    fun addBiblioRef(ref: BiblioRef) {
        if (_selectedBiblioRefs.value.none { it.uuid == ref.uuid }) {
            _selectedBiblioRefs.value = _selectedBiblioRefs.value + ref
        }
    }

    fun removeBiblioRef(ref: BiblioRef) {
        _selectedBiblioRefs.value = _selectedBiblioRefs.value.filter { it.uuid != ref.uuid }
    }

    fun addOrGetCustomNutrient(name: String, unit: String = "g"): Nutrient? {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return null
        val nutrient = CustomNutrientRegistry.registerFromRaw(trimmedName, unit)
        if (!_allNutrients.any { it.label == nutrient.label }) {
            _allNutrients.add(nutrient)
        }
        return nutrient
    }

    suspend fun saveAliment(aliment: AlimentEv) {
        try {
            val todayIso = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
            val alimentWithDate = aliment.copy(
                lastUpdateDate = todayIso,
                biblioRefs = _selectedBiblioRefs.value
            )

            // Vérifier si c'est un nouvel aliment ou une mise à jour
            val existingAliment =
                    try {
                        foodRepository.getFoodById(alimentWithDate.uuid)
                    } catch (e: Exception) {
                        null
                    }

            // Sauvegarder l'aliment selon le cas
            if (existingAliment != null) {
                // Aliment existant : mise à jour
                foodRepository.updateFood(alimentWithDate)
            } else {
                // Nouvel aliment : insertion
                foodRepository.insertFood(alimentWithDate)
            }

            // Mettre à jour l'état local
            _alimentState.value = alimentWithDate
        } catch (e: Exception) {
            e.printStackTrace()
            throw e // Relancer l'exception pour que la vue puisse la gérer
        }
    }

    fun clear() {
        job.cancel()
    }
}
