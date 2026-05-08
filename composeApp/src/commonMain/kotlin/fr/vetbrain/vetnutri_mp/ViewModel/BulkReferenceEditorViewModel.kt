package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.AAEnum
import fr.vetbrain.vetnutri_mp.Enumer.MainNutrientEnum
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.NutrientOther
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Utils.PlatformDispatcher
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BulkReferenceEditorViewModel(
        private val referenceEvRepository: DatabaseReferenceEvRepository,
        private val biblioRefRepository: BiblioRefRepository,
        private val platformDispatcher: PlatformDispatcher = PlatformDispatcher(),
        private val coroutineContext: CoroutineContext =
                platformDispatcher.provideMainDispatcher()
) {
    private val scope = CoroutineScope(coroutineContext)

    private val _references = MutableStateFlow<List<ReferenceEv>>(emptyList())
    val references: StateFlow<List<ReferenceEv>> = _references.asStateFlow()

    private val _selectedCategory = MutableStateFlow(MainNutrientEnum.MACRO)
    val selectedCategory: StateFlow<MainNutrientEnum> = _selectedCategory.asStateFlow()

    private val _selectedLevel = MutableStateFlow(Reflevel.MIN)
    val selectedLevel: StateFlow<Reflevel> = _selectedLevel.asStateFlow()

    // clé : "${refUuid}||${nutrientLabel}||${levelName}"
    private val _editedValues = MutableStateFlow<Map<String, String>>(emptyMap())
    val editedValues: StateFlow<Map<String, String>> = _editedValues.asStateFlow()

    private val _availableBiblioRefs = MutableStateFlow<List<BiblioRef>>(emptyList())
    val availableBiblioRefs: StateFlow<List<BiblioRef>> = _availableBiblioRefs.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error.asStateFlow()

    fun loadReferences(ids: List<String>) {
        scope.launch {
            _loading.value = true
            _error.value = ""
            try {
                val loaded = ids.mapNotNull { referenceEvRepository.getById(it) }
                _references.value = loaded
                _editedValues.value = emptyMap()
            } catch (e: Exception) {
                _error.value = "Erreur lors du chargement : ${e.message ?: "Erreur inconnue"}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun selectCategory(type: MainNutrientEnum) {
        _selectedCategory.value = type
    }

    fun selectLevel(level: Reflevel) {
        _selectedLevel.value = level
    }

    fun updateCell(refUuid: String, nutrientLabel: String, newValue: String) {
        val key = cellKey(refUuid, nutrientLabel, _selectedLevel.value)
        _editedValues.value = _editedValues.value + (key to newValue)
    }

    fun getCellValue(refUuid: String, nutrientLabel: String): String {
        val key = cellKey(refUuid, nutrientLabel, _selectedLevel.value)
        _editedValues.value[key]?.let { return it }
        val ref = _references.value.find { it.uuid == refUuid } ?: return ""
        val nutrient = findNutrientByLabel(nutrientLabel, _selectedCategory.value) ?: return ""
        val v = ref.obtenirNutriment(nutrient, _selectedLevel.value)
        return if (v == -1.0) "" else v.toString()
    }

    fun getNutrientsForCategory(category: MainNutrientEnum): List<Nutrient> {
        return when (category) {
            MainNutrientEnum.BASE -> NutrientMain.entries
            MainNutrientEnum.MACRO -> NutrientMacro.entries
            MainNutrientEnum.MIN -> NutrientMin.entries
            MainNutrientEnum.VITAM -> NutrientVitam.entries
            MainNutrientEnum.LIPID -> NutrientLipid.entries
            MainNutrientEnum.AMA -> AAEnum.entries
            MainNutrientEnum.OTHER -> NutrientOther.entries
            MainNutrientEnum.ANA -> NutrientAnalysis.entries
            MainNutrientEnum.ENERGIE ->
                    NutrientMain.entries.filter { it.label.contains("Energie") }
            else -> emptyList()
        }
    }

    fun saveAll() {
        scope.launch {
            _loading.value = true
            _error.value = ""
            try {
                val edits = _editedValues.value
                if (edits.isEmpty()) return@launch

                _references.value.forEach { reference ->
                    var modified = false
                    edits.forEach { (key, rawValue) ->
                        if (!key.startsWith("${reference.uuid}||")) return@forEach
                        val parts = key.split("||")
                        if (parts.size != 3) return@forEach
                        val nutrientLabel = parts[1]
                        val levelName = parts[2]
                        val level =
                                try {
                                    Reflevel.valueOf(levelName)
                                } catch (e: Exception) {
                                    return@forEach
                                }
                        val nutrient = findNutrientByLabelAllCategories(nutrientLabel)
                                ?: return@forEach
                        val doubleValue = rawValue.toDoubleOrNull() ?: return@forEach
                        val existing = reference.obtenirNutrimentRef(nutrient, level)
                        val unitReq = existing?.uniteReq ?: UnitReqEnum.PERKG
                        val biblio = existing?.biblio ?: BiblioRef()
                        reference.definirNutriment(doubleValue, nutrient, level, unitReq, biblio)
                        modified = true
                    }
                    if (modified) {
                        referenceEvRepository.updateReferenceEv(reference)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors de la sauvegarde : ${e.message ?: "Erreur inconnue"}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadAvailableBiblioRefs() {
        scope.launch {
            try {
                biblioRefRepository.getAllBiblioRefs().collect { refs ->
                    _availableBiblioRefs.value = refs
                }
            } catch (e: Exception) {
                _error.value =
                        "Erreur lors du chargement des bibliographies : ${e.message}"
            }
        }
    }

    private fun cellKey(refUuid: String, nutrientLabel: String, level: Reflevel): String =
            "${refUuid}||${nutrientLabel}||${level.name}"

    private fun findNutrientByLabel(label: String, category: MainNutrientEnum): Nutrient? =
            getNutrientsForCategory(category).find { it.label == label }

    private fun findNutrientByLabelAllCategories(label: String): Nutrient? {
        val searchOrder =
                listOf(
                        MainNutrientEnum.MACRO,
                        MainNutrientEnum.MIN,
                        MainNutrientEnum.VITAM,
                        MainNutrientEnum.LIPID,
                        MainNutrientEnum.AMA,
                        MainNutrientEnum.OTHER,
                        MainNutrientEnum.ANA,
                        MainNutrientEnum.BASE,
                        MainNutrientEnum.ENERGIE
                )
        searchOrder.forEach { cat ->
            getNutrientsForCategory(cat).find { it.label == label }?.let { return it }
        }
        return null
    }
}
