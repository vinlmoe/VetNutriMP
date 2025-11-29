package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.AnimalEvJson
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.FoodImportResult
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi

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
    // Instance statique de Json pour éviter la création redondante
    private val json = Json { ignoreUnknownKeys = true }

    private val _allAnimals = MutableStateFlow<List<AnimalEv>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedEspece = MutableStateFlow<Espece?>(null)
    val selectedEspece: StateFlow<Espece?> = _selectedEspece

    // Liste de toutes les espèces disponibles, avec une option "Toutes" (null)
    val availableEspeces: List<Espece?> = listOf(null) + Espece.entries.toList()

    /**
     * Flux d'animaux filtrés selon le terme de recherche et l'espèce sélectionnée. La recherche
     * s'effectue sur:
     * - Le nom de l'animal
     * - Le nom du propriétaire
     * - La race de l'animal Les résultats sont triés par ordre alphabétique du nom de l'animal.
     */
    val animals: StateFlow<List<AnimalEv>> =
            _searchQuery
                    .map { query ->
                        if (query.isBlank() && _selectedEspece.value == null) {
                            _allAnimals.value.sortedBy { it.nom }
                        } else {
                            _allAnimals.value
                                    .filter { animal ->
                                        val matchesQuery =
                                                query.isBlank() ||
                                                        animal.nom.contains(
                                                                query,
                                                                ignoreCase = true
                                                        ) ||
                                                        animal.ownerName.contains(
                                                                query,
                                                                ignoreCase = true
                                                        ) ||
                                                        animal.race.contains(
                                                                query,
                                                                ignoreCase = true
                                                        )

                                        val matchesEspece =
                                                _selectedEspece.value == null ||
                                                        animal.getEspece() == _selectedEspece.value

                                        matchesQuery && matchesEspece
                                    }
                                    .sortedBy { it.nom }
                        }
                    }
                    .let { MutableStateFlow(emptyList()) }

    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult

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
            updateFilteredAnimals()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        updateFilteredAnimals()
    }

    fun setSelectedEspece(espece: Espece?) {
        _selectedEspece.value = espece
        updateFilteredAnimals()
    }

    /**
     * Met à jour la liste des animaux filtrés en fonction du terme de recherche et de l'espèce
     * sélectionnée.
     */
    private fun updateFilteredAnimals() {
        val query = _searchQuery.value
        val espece = _selectedEspece.value

        (animals as MutableStateFlow).value =
                if (query.isBlank() && espece == null) {
                    _allAnimals.value.sortedBy { it.nom }
                } else {
                    _allAnimals.value
                            .filter { animal ->
                                val matchesQuery =
                                        query.isBlank() ||
                                                animal.nom.contains(query, ignoreCase = true) ||
                                                animal.ownerName.contains(
                                                        query,
                                                        ignoreCase = true
                                                ) ||
                                                animal.race.contains(query, ignoreCase = true)

                                val matchesEspece = espece == null || animal.getEspece() == espece

                                matchesQuery && matchesEspece
                            }
                            .sortedBy { it.nom }
                }
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

            // Créer le service de partage JSON
            val shareService = fr.vetbrain.vetnutri_mp.Service.createJsonShareService()

            // Extraire le binId depuis l'URL si nécessaire
            val binId = if (binIdOrUrl.contains("jsonbin.io")) {
                shareService.extractBinIdFromUrl(binIdOrUrl) ?: run {
                    finishApiImport()
                    return ImportResult.Error("Impossible d'extraire l'ID du bin depuis l'URL: $binIdOrUrl")
                }
            } else {
                binIdOrUrl
            }

            appendApiImportLog("📥 Téléchargement du bin: $binId")
            updateApiImportProgress(0.1)

            // Télécharger le JSON depuis jsonbin.io
            val downloadResult = shareService.downloadJson(binId)

            val jsonContent = downloadResult.getOrElse { error ->
                finishApiImport()
                return ImportResult.Error("Erreur lors du téléchargement depuis jsonbin.io: ${error.message}")
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
                count = totalCount,
                importedCount = importCounts.animals + importCounts.foods + importCounts.equations +
                              importCounts.references + importCounts.biblios + importCounts.rations +
                              importCounts.recipes + importCounts.conseils,
                updatedCount = 0, // L'importAll ne retourne pas les counts de mise à jour séparément
                deletedCount = 0,
                errorCount = 0,
                conseils = importCounts.conseils
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

    /**
     * Délègue l'importation des animaux à la fonction de plateforme spécifique Cela permet d'éviter
     * l'ambiguïté d'appel direct dans la vue
     */
    fun importAnimalsFromFileUI() {
        fr.vetbrain.vetnutri_mp.importAnimalsFromFile(this)
    }

    /**
     * Importe des animaux à partir d'une chaîne JSON
     *
     * @param jsonContent Le contenu JSON à désérialiser
     */
    fun importAnimalsFromJson(jsonContent: String) {
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
            val conseils: Int = 0
        ) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }
}
