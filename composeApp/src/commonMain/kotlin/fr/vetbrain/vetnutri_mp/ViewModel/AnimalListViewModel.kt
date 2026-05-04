package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.AnimalEvJson
import fr.vetbrain.vetnutri_mp.Data.ConsultationKeyword
import fr.vetbrain.vetnutri_mp.Data.ApiEnvelope
import fr.vetbrain.vetnutri_mp.Data.ExamSession
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.FoodImportResult
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * ViewModel liste animaux.
 * - Maintient une liste complète et expose un flux filtré (recherche + espèce) via combine/stateIn.
 * - Gère les imports (local/API) et les logs de progression.
 * - Certains repositories sont optionnels et uniquement requis pour l'import complet API.
 */
@OptIn(ExperimentalUuidApi::class)
class AnimalListViewModel(
    private val animalRepository: AnimalRepository,
    internal val foodRepository: fr.vetbrain.vetnutri_mp.Repository.DatabaseFoodRepository? = null,
    internal val recipeRepository: fr.vetbrain.vetnutri_mp.Repository.RecipeRepository? = null,
    internal val referenceEvRepository: fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository? = null,
    internal val equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository? = null,
    internal val biblioRefRepository: fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository? = null,
    internal val consultationRepository: fr.vetbrain.vetnutri_mp.Repository.ConsultationRepository? = null,
    internal val conseilRepository: fr.vetbrain.vetnutri_mp.Repository.ConseilRepository? = null
) : ViewModel() {
    enum class AnimalSortOrder {
        LAST_CONSULTATION,
        NAME_ASC,
        AGE
    }

    // Instance statique de Json pour éviter la création redondante
    private val json = Json { ignoreUnknownKeys = true }

    private val _allAnimals = MutableStateFlow<List<AnimalEv>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedEspece = MutableStateFlow<Espece?>(null)
    val selectedEspece: StateFlow<Espece?> = _selectedEspece
    private val _sortOrder = MutableStateFlow(AnimalSortOrder.NAME_ASC)
    val sortOrder: StateFlow<AnimalSortOrder> = _sortOrder.asStateFlow()

    private val _keywordIncludeIds = MutableStateFlow<Set<String>>(emptySet())
    val keywordIncludeIds: StateFlow<Set<String>> = _keywordIncludeIds.asStateFlow()

    private val _keywordExcludeIds = MutableStateFlow<Set<String>>(emptySet())
    val keywordExcludeIds: StateFlow<Set<String>> = _keywordExcludeIds.asStateFlow()

    private val _availableKeywords = MutableStateFlow<List<ConsultationKeyword>>(emptyList())
    val availableKeywords: StateFlow<List<ConsultationKeyword>> = _availableKeywords.asStateFlow()

    private val _animalKeywordIds = MutableStateFlow<Map<String, Set<String>>>(emptyMap())

    private val _examSession = MutableStateFlow<ExamSession?>(null)
    val examSession: StateFlow<ExamSession?> = _examSession.asStateFlow()

    // Liste de toutes les espèces disponibles, avec une option "Toutes" (null)
    val availableEspeces: List<Espece?> = listOf(null) + Espece.entries.toList()

    /**
     * Flux d'animaux filtrés selon le terme de recherche et l'espèce sélectionnée. La recherche
     * s'effectue sur:
     * - Le nom de l'animal
     * - Le nom du propriétaire
     * - La race de l'animal Les résultats sont triés par ordre alphabétique du nom de l'animal.
     */
    private data class AnimalFilterState(
            val animals: List<AnimalEv>,
            val query: String,
            val espece: Espece?,
            val keywordMap: Map<String, Set<String>>,
            val includeIds: Set<String>,
            val examSession: ExamSession?,
            val sortOrder: AnimalSortOrder
    )

    val animals: StateFlow<List<AnimalEv>> =
            combine(
                            _allAnimals,
                            _searchQuery,
                            _selectedEspece,
                            _animalKeywordIds,
                            _keywordIncludeIds
                    ) { all, query, espece, keywordMap, includeIds ->
                        AnimalFilterState(all, query, espece, keywordMap, includeIds, null, AnimalSortOrder.NAME_ASC)
                    }
                    .combine(_sortOrder) { state, sortOrder ->
                        state.copy(sortOrder = sortOrder)
                    }
                    .combine(_examSession) { state, examSession ->
                        state.copy(examSession = examSession)
                    }
                    .combine(_keywordExcludeIds) { state, excludeIds ->
                        filterAnimals(
                                state.animals,
                                state.query,
                                state.espece,
                                state.keywordMap,
                                state.includeIds,
                                excludeIds,
                                state.examSession,
                                state.sortOrder
                        )
                    }
                    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult

    private val _isImportingAnimals = MutableStateFlow(false)
    val isImportingAnimals: StateFlow<Boolean> = _isImportingAnimals.asStateFlow()

    // État import API (progression + logs)
    private val _isApiImporting = MutableStateFlow(false)
    val isApiImporting: StateFlow<Boolean> = _isApiImporting.asStateFlow()

    private val _apiImportProgress = MutableStateFlow(0.0)
    val apiImportProgress: StateFlow<Double> = _apiImportProgress.asStateFlow()

    private val _apiImportLogs = MutableStateFlow<List<String>>(emptyList())
    val apiImportLogs: StateFlow<List<String>> = _apiImportLogs.asStateFlow()

    fun loadAnimals() {
        viewModelScope.launch {
            _allAnimals.value = animalRepository.getAllAnimals()
            refreshKeywordData()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedEspece(espece: Espece?) {
        _selectedEspece.value = espece
    }

    fun setSortOrder(sortOrder: AnimalSortOrder) {
        _sortOrder.value = sortOrder
    }

    fun setExamSession(session: ExamSession?) {
        _examSession.value = session
    }

    private fun filterAnimals(
            animals: List<AnimalEv>,
            query: String,
            espece: Espece?,
            keywordMap: Map<String, Set<String>>,
            includeIds: Set<String>,
            excludeIds: Set<String>,
            examSession: ExamSession?,
            sortOrder: AnimalSortOrder
    ): List<AnimalEv> {
        val trimmedQuery = query.trim()

        val filteredAnimals =
                animals
                .filter { animal ->
                    val matchesExam =
                            if (examSession == null) {
                                // Hors mode examen, masquer les animaux d'examen de la recherche standard.
                                animal.examExerciseId.isNullOrBlank()
                            } else {
                                !animal.examExerciseId.isNullOrBlank() &&
                                    (examSession.studentId.isBlank() ||
                                        animal.examStudentId == examSession.studentId) &&
                                    (examSession.studentNumber.isBlank() ||
                                        animal.examStudentNumber == examSession.studentNumber)
                            }

                    val matchesQuery =
                            trimmedQuery.isBlank() ||
                                    animal.id.orEmpty().contains(trimmedQuery, ignoreCase = true) ||
                                    animal.nom.contains(trimmedQuery, ignoreCase = true) ||
                                    animal.ownerName.contains(trimmedQuery, ignoreCase = true) ||
                                    animal.race.contains(trimmedQuery, ignoreCase = true)

                    val matchesEspece = espece == null || animal.getEspece() == espece
                    val animalKeywords = keywordMap[animal.uuid] ?: emptySet()
                    val matchesKeywordInclude =
                            includeIds.isEmpty() ||
                                    includeIds.all { animalKeywords.contains(it) }
                    val matchesKeywordExclude =
                            excludeIds.isEmpty() ||
                                    excludeIds.none { animalKeywords.contains(it) }

                    matchesExam && matchesQuery && matchesEspece && matchesKeywordInclude && matchesKeywordExclude
                }

        return when (sortOrder) {
            AnimalSortOrder.LAST_CONSULTATION ->
                filteredAnimals.sortedWith(
                    compareByDescending<AnimalEv> {
                        it.consultations.mapNotNull { consultation -> consultation.date }.maxOrNull()
                    }.thenBy { it.nom.lowercase() }
                )
            AnimalSortOrder.AGE -> {
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                filteredAnimals.sortedWith(
                    compareByDescending<AnimalEv> { animal ->
                        animal.birthdate?.let { birthdate -> today.year - birthdate.year }
                    }.thenBy { it.nom.lowercase() }
                )
            }
            AnimalSortOrder.NAME_ASC -> filteredAnimals.sortedBy { it.nom.lowercase() }
        }
    }

    suspend fun exportExamAnimalsToJsonBin(session: ExamSession): Result<fr.vetbrain.vetnutri_mp.Service.ShareLink> {
        return withContext(AppDispatchers.IO) {
            try {
                val allAnimals = animalRepository.getAllAnimals()
                val examAnimals =
                        allAnimals.filter { animal ->
                            !animal.examExerciseId.isNullOrBlank() &&
                                    (session.studentId.isBlank() || animal.examStudentId == session.studentId) &&
                                    (session.studentNumber.isBlank() || animal.examStudentNumber == session.studentNumber)
                        }

                if (examAnimals.isEmpty()) {
                    return@withContext Result.failure(Exception("Aucun animal d'examen à exporter."))
                }

                if (consultationRepository == null) {
                    return@withContext Result.failure(Exception("Références manquantes pour exporter les consultations."))
                }

                val exportImportRepo = fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository(
                    animalRepository = animalRepository,
                    foodRepository = foodRepository,
                    equationRepository = equationRepository,
                    referenceRepository = referenceEvRepository,
                    biblioRepository = biblioRefRepository,
                    consultationRepository = consultationRepository,
                    recipeRepository = recipeRepository,
                    conseilRepository = conseilRepository
                )

                val jsonContent = exportImportRepo.exportWithSelection(
                    fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository.ExportSelectionOptions(
                        includeAnimals = true,
                        includeFoods = false,
                        includeRations = true,
                        includeRecipes = false,
                        includeEquations = false,
                        includeConseils = false,
                        includeLinkedFromAnimals = true,
                        animalIds = examAnimals.map { it.uuid }.toSet()
                    )
                )

                val shareService = fr.vetbrain.vetnutri_mp.Service.createJsonShareService()
                val shareOptions =
                        fr.vetbrain.vetnutri_mp.Service.ShareOptions(
                            binName = "exam_${session.studentId}",
                            encryptJson = false,
                            isPrivate = false
                        )

                shareService.uploadJson(jsonContent, shareOptions)
            } catch (e: Exception) {
                Result.failure(Exception("Erreur lors de l'export examen: ${e.message}", e))
            }
        }
    }

    fun setKeywordFilters(includeIds: Set<String>, excludeIds: Set<String>) {
        _keywordIncludeIds.value = includeIds
        _keywordExcludeIds.value = excludeIds
    }

    fun clearKeywordFilters() {
        _keywordIncludeIds.value = emptySet()
        _keywordExcludeIds.value = emptySet()
    }

    private suspend fun refreshKeywordData() {
        if (consultationRepository == null) {
            _availableKeywords.value = emptyList()
            _animalKeywordIds.value = emptyMap()
            return
        }

        try {
            _availableKeywords.value = consultationRepository.getAllKeywords()
        } catch (_: Exception) {
            _availableKeywords.value = emptyList()
        }

        val animalIds = _allAnimals.value.map { it.uuid }
        val keywordMap = mutableMapOf<String, Set<String>>()

        for (animalId in animalIds) {
            try {
                val consultations = consultationRepository.getConsultationsForAnimal(animalId)
                val keywordIds =
                        consultations
                                .flatMap { it.keywordIds }
                                .filter { it.isNotBlank() }
                                .toSet()
                keywordMap[animalId] = keywordIds
            } catch (_: Exception) {
                keywordMap[animalId] = emptySet()
            }
        }
        _animalKeywordIds.value = keywordMap
    }

    fun deleteAnimal(animal: AnimalEv) {
        viewModelScope.launch {
            animalRepository.deleteAnimal(animal)
            loadAnimals() // Refresh the list after deletion
        }
    }

    fun importAnimals(animalsJson: List<AnimalEvJson>) {
        viewModelScope.launch {
            try {
                val importResult = animalRepository.importAnimals(animalsJson)
                _importResult.value =
                        ImportResult.Success(importResult.importedCount + importResult.updatedCount)
                loadAnimals() // Refresh the list after import
            } catch (e: Exception) {
                _importResult.value = ImportResult.Error(e.message ?: "Erreur inconnue")
            }
        }
    }

    // Contrats de suivi pour import API
    fun startApiImport() {
        _isApiImporting.value = true
        _apiImportProgress.value = 0.0
        _apiImportLogs.value = emptyList()
    }

    fun updateApiImportProgress(progress: Double) {
        _apiImportProgress.value = progress.coerceIn(0.0, 1.0)
    }

    fun appendApiImportLog(message: String) {
        val newLogs = (_apiImportLogs.value + message).takeLast(200)
        _apiImportLogs.value = newLogs
    }

    fun finishApiImport() {
        _isApiImporting.value = false
    }

    fun setImportResult(result: ImportResult) {
        _importResult.value = result
    }

    /**
     * Importe des données depuis jsonbin.io en utilisant un binId ou une URL
     * @param binIdOrUrl L'ID du bin ou l'URL complète jsonbin.io
     * @return Le résultat de l'importation
     */
    suspend fun importFromJsonBin(binIdOrUrl: String): ImportResult {
        return try {
            startApiImport()
            appendApiImportLog("🔄 Début de l'import depuis jsonbin.io...")
            val preImportIds = try {
                animalRepository.getAllAnimals().map { it.uuid }.toSet()
            } catch (_: Exception) {
                emptySet()
            }

            // Créer le service de partage JSON
            val shareService = fr.vetbrain.vetnutri_mp.Service.createJsonShareService()

            // Support QR JSON chiffré {binId, key, iv}
            val qrPayload = shareService.parseQrPayload(binIdOrUrl)
            val binId = when {
                qrPayload != null -> qrPayload.binId
                binIdOrUrl.contains("jsonbin.io") -> {
                    shareService.extractBinIdFromUrl(binIdOrUrl) ?: run {
                        finishApiImport()
                        return ImportResult.Error("Impossible d'extraire l'ID du bin depuis l'URL: $binIdOrUrl")
                    }
                }
                else -> binIdOrUrl
            }
            val keyBase64 = qrPayload?.key
            val ivBase64 = qrPayload?.iv
            if (qrPayload != null) {
                appendApiImportLog("🔐 QR chiffré détecté (bin: $binId)")
            }

            appendApiImportLog("📥 Téléchargement du bin: $binId")
            updateApiImportProgress(0.1)

            // Télécharger le JSON depuis jsonbin.io
            val downloadResult = shareService.downloadJson(binId, keyBase64, ivBase64)

            val jsonContent = downloadResult.getOrElse { error ->
                finishApiImport()
                return ImportResult.Error("Erreur lors du téléchargement depuis jsonbin.io: ${error.message}")
            }

            val importedAnimalIds: List<String> =
                try {
                    val root = json.parseToJsonElement(jsonContent)
                    val animalsArray = root.jsonObject["animals"]?.jsonArray ?: JsonArray(emptyList())
                    val ids =
                        animalsArray.mapNotNull { element ->
                            val obj = element as? JsonObject ?: return@mapNotNull null
                            obj["uuid"]?.jsonPrimitive?.content
                        }
                    ids
                } catch (_: Exception) {
                    emptyList()
                }

            appendApiImportLog("✅ JSON téléchargé (${jsonContent.length} caractères)")
            updateApiImportProgress(0.3)

            // Vérifier que tous les repositories sont disponibles
            if (foodRepository == null || recipeRepository == null || referenceEvRepository == null || 
                equationRepository == null || biblioRefRepository == null || consultationRepository == null || 
                conseilRepository == null) {
                finishApiImport()
                return ImportResult.Error("Erreur interne: repositories manquants pour l'import complet")
            }

            // Créer l'ExportImportRepository avec tous les repositories nécessaires
            val exportImportRepo = fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository(
                animalRepository = animalRepository,
                foodRepository = foodRepository,
                equationRepository = equationRepository,
                referenceRepository = referenceEvRepository,
                biblioRepository = biblioRefRepository,
                consultationRepository = consultationRepository,
                recipeRepository = recipeRepository,
                conseilRepository = conseilRepository
            )

            appendApiImportLog("🔄 Parsing et import des données...")
            updateApiImportProgress(0.4)

            // Importer les données avec un listener de progression
            val importCounts = exportImportRepo.importAll(
                apiJson = jsonContent,
                listener = fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository.ImportProgressListener(
                    onProgress = { progress ->
                        // Mapper la progression de 0.4 à 0.9 (car on commence à 0.4)
                        val mappedProgress = 0.4 + (progress * 0.5)
                        updateApiImportProgress(mappedProgress)
                    },
                    onLog = { message ->
                        appendApiImportLog(message)
                    }
                )
            )
            val postImportIds = try {
                animalRepository.getAllAnimals().map { it.uuid }.toSet()
            } catch (_: Exception) {
                emptySet()
            }
            val newIds = (postImportIds - preImportIds).toList()
            val resolvedAnimalIds =
                if (importedAnimalIds.isNotEmpty()) importedAnimalIds else newIds

            updateApiImportProgress(1.0)

            val totalCount = importCounts.animals + importCounts.foods + importCounts.equations +
                           importCounts.references + importCounts.biblios + importCounts.rations +
                           importCounts.recipes + importCounts.conseils

            appendApiImportLog("✅ Import terminé avec succès!")
            appendApiImportLog("📊 Résultat: $totalCount éléments importés")

            finishApiImport()
            
            // Rafraîchir la liste des animaux
            loadAnimals()

            ImportResult.Success(
                count = importCounts.animals,
                importedCount = importCounts.animals,
                updatedCount = 0, // L'importAll ne retourne pas les counts de mise à jour séparément
                deletedCount = 0,
                errorCount = 0,
                conseils = importCounts.conseils,
                animalIds = resolvedAnimalIds
            )
        } catch (e: Exception) {
            finishApiImport()
            appendApiImportLog("❌ Erreur: ${e.message}")
            ImportResult.Error("Erreur lors de l'import depuis jsonbin.io: ${e.message}")
        }
    }

    /**
     * Définit une erreur d'importation
     *
     * @param message Le message d'erreur à afficher
     */
    fun setImportError(message: String) {
        _importResult.value = ImportResult.Error(message)
    }

    fun resetImportResult() {
        _importResult.value = null
    }

    suspend fun getAnimalById(uuid: String): AnimalEv? {
        return withContext(AppDispatchers.IO) {
            animalRepository.getAnimalById(uuid)
        }
    }

    fun getFoodRepository() = animalRepository.getFoodRepository()

    /**
     * Délègue l'importation des animaux à la fonction de plateforme spécifique Cela permet d'éviter
     * l'ambiguïté d'appel direct dans la vue
     */
    fun importAnimalsFromFileUI() {
        fr.vetbrain.vetnutri_mp.importAnimalsFromFile(this, clearFoodsBeforeImport = false)
    }

    /**
     * Importe des animaux à partir d'une chaîne JSON
     *
     * @param jsonContent Le contenu JSON à désérialiser
     */
    fun importAnimalsFromJson(jsonContent: String) {
        _isImportingAnimals.value = true
        viewModelScope.launch {
            try {
                val importResult =
                        fr.vetbrain.vetnutri_mp.Utils.ImportUtils.importAnimalsFromJson(jsonContent)

                if (importResult.animals.isNotEmpty()) {
                    var foodsImported = false
                    var foodImportResult: FoodImportResult? = null

                    // Importer d'abord les aliments extraits des rations s'il y en a
                    if (importResult.foods.isNotEmpty()) {
                        try {
                            // Obtenir le repository des aliments
                            val foodRepository = animalRepository.getFoodRepository()
                            if (foodRepository != null) {
                                try {
                                    foodImportResult =
                                            foodRepository.importFoods(importResult.foods)
                                    foodsImported =
                                            foodImportResult.importedCount +
                                                    foodImportResult.updatedCount > 0
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    // Continuer l'importation des animaux même si l'importation des
                                    // aliments échoue
                                }
                            } else {
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    try {
                        val importResult = animalRepository.importAnimals(importResult.animals)

                        if (importResult.importedCount + importResult.updatedCount > 0) {
                            if (foodsImported) {
                                _importResult.value =
                                        ImportResult.Success(
                                                importResult.importedCount +
                                                        importResult.updatedCount
                                        )
                            } else {
                                _importResult.value =
                                        ImportResult.Success(
                                                importResult.importedCount +
                                                        importResult.updatedCount
                                        )
                            }
                            loadAnimals() // Actualiser la liste après l'importation
                        } else {
                            _importResult.value =
                                    ImportResult.Error("Échec de l'importation des animaux")
                        }
                    } catch (e: Exception) {
                        _importResult.value =
                                ImportResult.Error(
                                        "Erreur lors de l'importation des animaux: ${e.message}"
                                )
                        e.printStackTrace()
                    }
                } else {
                    _importResult.value =
                            ImportResult.Error("Aucun animal trouvé dans le fichier JSON")
                }
            } catch (e: Exception) {
                _importResult.value = ImportResult.Error(e.message ?: "Erreur inconnue")
                e.printStackTrace()
            } finally {
                _isImportingAnimals.value = false
            }
        }
    }

    sealed class ImportResult {
        data class Success(
            val count: Int,
            val importedCount: Int = count,
            val updatedCount: Int = 0,
            val deletedCount: Int = 0,
            val errorCount: Int = 0,
            val nonResolvedNutrients: Int = 0,
            val conseils: Int = 0,
            val animalIds: List<String> = emptyList()
        ) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }
}
