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

data class ConsistencyError(
        val refUuid: String,
        val nutrientLabel: String,
        val violation: String
)

val BULK_EDITABLE_CATEGORIES =
        listOf(
                MainNutrientEnum.MACRO,
                MainNutrientEnum.MIN,
                MainNutrientEnum.VITAM,
                MainNutrientEnum.LIPID,
                MainNutrientEnum.AMA,
                MainNutrientEnum.OTHER,
                MainNutrientEnum.ANA,
                MainNutrientEnum.BASE
        )

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

    // Incrémenté après chaque copyColumn() pour forcer la réinitialisation des états locaux
    private val _copyVersion = MutableStateFlow(0)
    val copyVersion: StateFlow<Int> = _copyVersion.asStateFlow()

    private val _consistencyErrors = MutableStateFlow<List<ConsistencyError>>(emptyList())
    val consistencyErrors: StateFlow<List<ConsistencyError>> = _consistencyErrors.asStateFlow()

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
                _consistencyErrors.value = emptyList()
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

    fun getCellValue(refUuid: String, nutrientLabel: String): String =
            getCellValueForLevel(refUuid, nutrientLabel, _selectedLevel.value)

    fun getCellValueForLevel(refUuid: String, nutrientLabel: String, level: Reflevel): String {
        val key = cellKey(refUuid, nutrientLabel, level)
        _editedValues.value[key]?.let { return it }
        val ref = _references.value.find { it.uuid == refUuid } ?: return ""
        val nutrient =
                findNutrientByLabel(nutrientLabel, _selectedCategory.value)
                        ?: findNutrientByLabelAllCategories(nutrientLabel) ?: return ""
        val v = ref.obtenirNutriment(nutrient, level)
        return if (v == -1.0) "" else v.toString()
    }

    /** Copie toutes les valeurs du niveau courant de [fromRefUuid] vers [toRefUuid]. */
    fun copyColumn(fromRefUuid: String, toRefUuid: String) {
        val category = _selectedCategory.value
        val level = _selectedLevel.value
        val newEdits = _editedValues.value.toMutableMap()
        getNutrientsForCategory(category).forEach { nutrient ->
            val value = getCellValueForLevel(fromRefUuid, nutrient.label, level)
            if (value.isNotEmpty()) {
                newEdits[cellKey(toRefUuid, nutrient.label, level)] = value
            }
        }
        _editedValues.value = newEdits
        _copyVersion.value++
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

    fun hasConsistencyError(refUuid: String, nutrientLabel: String): String? =
            _consistencyErrors.value
                    .firstOrNull { it.refUuid == refUuid && it.nutrientLabel == nutrientLabel }
                    ?.violation

    fun clearConsistencyErrors() {
        _consistencyErrors.value = emptyList()
    }

    fun validateConsistency() {
        val errors = mutableListOf<ConsistencyError>()
        _references.value.forEach { ref ->
            BULK_EDITABLE_CATEGORIES.forEach { category ->
                getNutrientsForCategory(category).forEach { nutrient ->
                    val min = getCellValueForLevel(ref.uuid, nutrient.label, Reflevel.MIN).toDoubleOrNull()
                    val max = getCellValueForLevel(ref.uuid, nutrient.label, Reflevel.MAX).toDoubleOrNull()
                    val optimin = getCellValueForLevel(ref.uuid, nutrient.label, Reflevel.OPTIMIN).toDoubleOrNull()
                    val optimax = getCellValueForLevel(ref.uuid, nutrient.label, Reflevel.OPTIMAX).toDoubleOrNull()

                    if (min != null && max != null && min > max)
                        errors.add(ConsistencyError(ref.uuid, nutrient.label, "MIN ($min) > MAX ($max)"))
                    if (min != null && optimin != null && min > optimin)
                        errors.add(ConsistencyError(ref.uuid, nutrient.label, "MIN ($min) > OPTIMIN ($optimin)"))
                    if (optimin != null && optimax != null && optimin > optimax)
                        errors.add(ConsistencyError(ref.uuid, nutrient.label, "OPTIMIN ($optimin) > OPTIMAX ($optimax)"))
                    if (optimax != null && max != null && optimax > max)
                        errors.add(ConsistencyError(ref.uuid, nutrient.label, "OPTIMAX ($optimax) > MAX ($max)"))
                }
            }
        }
        _consistencyErrors.value = errors
    }

    /**
     * Génère un CSV avec toutes les catégories, tous les niveaux et toutes les références.
     * Format : Catégorie;Nutriment;RefA - MIN;RefA - MAX;...;RefB - MIN;...
     */
    fun generateCsv(): String {
        val refs = _references.value
        val sb = StringBuilder()

        // En-tête
        sb.append("Catégorie;Nutriment")
        refs.forEach { ref ->
            Reflevel.values().forEach { level ->
                sb.append(";${ref.nom} - ${level.name}")
            }
        }
        sb.append("\n")

        // Lignes de données
        BULK_EDITABLE_CATEGORIES.forEach { category ->
            getNutrientsForCategory(category).forEach { nutrient ->
                sb.append("${category.label};${nutrient.label}")
                refs.forEach { ref ->
                    Reflevel.values().forEach { level ->
                        val v = getCellValueForLevel(ref.uuid, nutrient.label, level)
                        sb.append(";$v")
                    }
                }
                sb.append("\n")
            }
        }

        return sb.toString()
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
