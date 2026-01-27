package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Enumer.AAEnum
import fr.vetbrain.vetnutri_mp.Enumer.MainNutrientEnum
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.NutrientOther
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseFoodRepository
import fr.vetbrain.vetnutri_mp.Repository.FoodImportResult
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/** ViewModel pour la gestion des paramètres de l'application */
class SettingsViewModel(
        internal val animalRepository: AnimalRepository,
        internal val foodRepository: DatabaseFoodRepository,
        internal val recipeRepository: fr.vetbrain.vetnutri_mp.Repository.RecipeRepository? = null,
        internal val referenceEvRepository:
                fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository? =
                null,
        internal val equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository? =
                null,
        internal val biblioRefRepository: fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository? =
                null,
        internal val consultationRepository:
                fr.vetbrain.vetnutri_mp.Repository.ConsultationRepository? =
                null,
        internal val conseilRepository: fr.vetbrain.vetnutri_mp.Repository.ConseilRepository? = null
) : ViewModel() {
    // Instance statique de Json pour éviter la création redondante
    private val json = Json { ignoreUnknownKeys = true }

    // Options d'import des aliments (configurées depuis l'UI)
    var importMergeNutrients: Boolean = false
    var importOnlyIfNewer: Boolean = false
    
    private val _uiScale = MutableStateFlow(1.0)
    val uiScale: StateFlow<Double> = _uiScale.asStateFlow()

    // Résultat de l'importation
    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult.asStateFlow()

    // État import API (progression + logs)
    private val _isApiImporting = MutableStateFlow(false)
    val isApiImporting: StateFlow<Boolean> = _isApiImporting.asStateFlow()

    private val _apiImportProgress = MutableStateFlow(0.0)
    val apiImportProgress: StateFlow<Double> = _apiImportProgress.asStateFlow()

    private val _apiImportLogs = MutableStateFlow<List<String>>(emptyList())
    val apiImportLogs: StateFlow<List<String>> = _apiImportLogs.asStateFlow()

    // Message de résultat pour les références nutritionnelles
    private val _nutritionalRequirementMessage = MutableStateFlow<String?>(null)
    val nutritionalRequirementMessage: StateFlow<String?> =
            _nutritionalRequirementMessage.asStateFlow()

    // Menu latéral
    private val _isDrawerOpen = MutableStateFlow(false)
    val isDrawerOpen: StateFlow<Boolean> = _isDrawerOpen.asStateFlow()

    // Préférences des nutriments par catégorie
    private val _selectedMainNutrients =
            MutableStateFlow<List<NutrientMain>>(NutrientMain.entries.toList())
    val selectedMainNutrients: StateFlow<List<NutrientMain>> = _selectedMainNutrients.asStateFlow()

    private val _selectedMinerals =
            MutableStateFlow<List<NutrientMin>>(NutrientMin.entries.toList())
    val selectedMinerals: StateFlow<List<NutrientMin>> = _selectedMinerals.asStateFlow()

    private val _selectedVitamins =
            MutableStateFlow<List<NutrientVitam>>(NutrientVitam.entries.toList())
    val selectedVitamins: StateFlow<List<NutrientVitam>> = _selectedVitamins.asStateFlow()

    private val _selectedLipids =
            MutableStateFlow<List<NutrientLipid>>(NutrientLipid.entries.toList())
    val selectedLipids: StateFlow<List<NutrientLipid>> = _selectedLipids.asStateFlow()

    private val _selectedAminoAcids = MutableStateFlow<List<AAEnum>>(AAEnum.entries.toList())
    val selectedAminoAcids: StateFlow<List<AAEnum>> = _selectedAminoAcids.asStateFlow()

    private val _selectedOtherNutrients =
            MutableStateFlow<List<NutrientOther>>(NutrientOther.entries.toList())
    val selectedOtherNutrients: StateFlow<List<NutrientOther>> =
            _selectedOtherNutrients.asStateFlow()

    /** Résultat d'une importation */
    sealed class ImportResult {
        /**
         * Succès de l'importation
         * @param count Le nombre total d'éléments importés
         * @param importedCount Le nombre d'éléments nouvellement importés
         * @param updatedCount Le nombre d'éléments mis à jour
         * @param deletedCount Le nombre d'éléments supprimés
         * @param errorCount Le nombre d'erreurs rencontrées
         * @param nonResolvedNutrients Le nombre de nutriments non résolus
         */
        data class Success(
                val count: Int,
                val importedCount: Int = count,
                val updatedCount: Int = 0,
                val deletedCount: Int = 0,
                val errorCount: Int = 0,
                val nonResolvedNutrients: Int = 0,
                val conseils: Int = 0
        ) : ImportResult()

        /**
         * Erreur lors de l'importation
         * @param message Le message d'erreur
         */
        data class Error(val message: String) : ImportResult()
    }

    /** Définit l'échelle de l'interface utilisateur */
    fun setUiScale(scale: Double) {
        val newScale = scale.coerceIn(0.5, 2.0)
        _uiScale.value = newScale
        AppSizes.adjustSize(newScale)
    }

    /** Augmente l'échelle de l'interface utilisateur */
    fun incrementUiScale() {
        setUiScale(_uiScale.value + 0.1)
    }

    /** Diminue l'échelle de l'interface utilisateur */
    fun decrementUiScale() {
        setUiScale(_uiScale.value - 0.1)
    }

    /** Ouvre le menu latéral */
    fun openDrawer() {
        _isDrawerOpen.value = true
    }

    /** Ferme le menu latéral */
    fun closeDrawer() {
        _isDrawerOpen.value = false
    }

    /** Met à jour l'ordre et la sélection des nutriments principaux */
    fun updateMainNutrients(nutrients: List<NutrientMain>) {
        _selectedMainNutrients.value = nutrients
    }

    /** Met à jour l'ordre et la sélection des minéraux */
    fun updateMinerals(minerals: List<NutrientMin>) {
        _selectedMinerals.value = minerals
    }

    /** Met à jour l'ordre et la sélection des vitamines */
    fun updateVitamins(vitamins: List<NutrientVitam>) {
        _selectedVitamins.value = vitamins
    }

    /** Met à jour l'ordre et la sélection des lipides */
    fun updateLipids(lipids: List<NutrientLipid>) {
        _selectedLipids.value = lipids
    }

    /** Met à jour l'ordre et la sélection des acides aminés */
    fun updateAminoAcids(aminoAcids: List<AAEnum>) {
        _selectedAminoAcids.value = aminoAcids
    }

    /** Met à jour l'ordre et la sélection des autres nutriments */
    fun updateOtherNutrients(otherNutrients: List<NutrientOther>) {
        _selectedOtherNutrients.value = otherNutrients
    }

    /** Obtient tous les nutriments disponibles pour une catégorie spécifique */
    fun getAllNutrientsForCategory(category: MainNutrientEnum): List<Nutrient> {
        return when (category) {
            MainNutrientEnum.BASE -> NutrientMain.entries.toList()
            MainNutrientEnum.MIN -> NutrientMin.entries.toList()
            MainNutrientEnum.VITAM -> NutrientVitam.entries.toList()
            MainNutrientEnum.LIPID -> NutrientLipid.entries.toList()
            MainNutrientEnum.AMA -> AAEnum.entries.toList()
            MainNutrientEnum.OTHER -> NutrientOther.entries.toList()
            else -> emptyList()
        }
    }

    /** Réinitialise le résultat de l'importation */
    fun resetImportResult() {
        _importResult.value = null
    }

    /** Définit le résultat de l'importation */
    fun setImportResult(result: ImportResult) {
        _importResult.value = result
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

    /**
     * Importe les aliments à partir d'un contenu JSON.
     * @param jsonContent Le contenu JSON à importer
     * @return Le résultat détaillé de l'importation
     */
    suspend fun importFoodsFromJson(jsonContent: String): FoodImportResult {
        // Utilisation de kotlinx.serialization pour parser le JSON
        val alimentsJson = json.decodeFromString<List<AlimentEvJson>>(jsonContent)
        return foodRepository.importFoods(
                alimentsJson,
                mergeNutrients = importMergeNutrients,
                importOnlyIfNewer = importOnlyIfNewer
        )
    }

    /**
     * Interface pour importer des aliments depuis l'UI Cette méthode est un point d'entrée pour
     * l'importation via l'UI
     */
    fun importFoodsFromFileUI() {
        // Appeler la fonction d'importation spécifique à la plateforme
        fr.vetbrain.vetnutri_mp.importFoodsFromFile(this)
    }

    /** Interface pour importer via le nouveau format API depuis l'UI (Desktop prioritaire) */
    fun importApiFromFileUI() {
        fr.vetbrain.vetnutri_mp.importApiFromFile(this)
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
     * Interface pour importer des références nutritionnelles depuis l'UI Cette méthode est un point
     * d'entrée pour l'importation via l'UI
     */
    fun importNutritionalRequirementsFromFileUI() {
        try {
            // Lancer l'importation avec feedback dans SettingsViewModel
            _nutritionalRequirementMessage.value = "🔄 Sélection du fichier en cours..."

            // Créer un ImportViewModel temporaire avec les repositories nécessaires
            // Note: Nous devons utiliser l'ImportViewModel car il a les bons repositories
            // Cette fonction devrait plutôt être appelée depuis l'ImportViewModel
            _nutritionalRequirementMessage.value =
                    "❌ Cette fonction doit être appelée depuis l'ImportViewModel qui a accès aux repositories nécessaires."
        } catch (e: Exception) {
            _nutritionalRequirementMessage.value = "❌ Erreur: ${e.message}"
        }
    }

    /** Définit le message de résultat d'importation des références nutritionnelles */
    fun setNutritionalRequirementMessage(message: String) {
        _nutritionalRequirementMessage.value = message
    }

    /** Efface le message de résultat d'importation des références nutritionnelles */
    fun clearNutritionalRequirementMessage() {
        _nutritionalRequirementMessage.value = null
    }

    /**
     * Supprime tous les aliments de la base de données
     * @return Le nombre d'aliments supprimés
     */
    suspend fun clearAllFoods(): Int {
        return foodRepository.clearAllFoods()
    }

    /**
     * Vide entièrement la base de données des animaux
     *
     * @return Le nombre d'animaux supprimés
     */
    suspend fun clearAllAnimals(): Int {
        val animals = animalRepository.getAllAnimals()
        val count = animals.size

        animals.forEach { animal -> animalRepository.deleteAnimal(animal) }

        return count
    }

    /**
     * Importe les aliments à partir d'une liste.
     * @param foodsList La liste d'aliments à importer
     * @return Le résultat détaillé de l'importation
     */
    suspend fun importFoodsFromList(
            foodsList: List<AlimentEvJson>,
            mergeNutrients: Boolean = importMergeNutrients,
            importOnlyIfNewer: Boolean = this.importOnlyIfNewer
    ): FoodImportResult {
        val result =
                foodRepository.importFoods(
                        foodsList,
                        mergeNutrients = mergeNutrients,
                        importOnlyIfNewer = importOnlyIfNewer
                )
        // Mettre à jour le résultat de l'importation pour l'affichage dans l'interface
        _importResult.value =
                ImportResult.Success(
                        count = result.totalCount,
                        importedCount = result.importedCount,
                        updatedCount = result.updatedCount,
                        deletedCount = result.deletedCount,
                        errorCount = result.errorCount,
                        nonResolvedNutrients = result.nonResolvedNutrientsCount,
                        conseils = 0
                )
        return result
    }

    /**
     * Supprime toutes les références de la base de données
     * @return Le nombre de références supprimées
     */
    suspend fun clearAllReferences(): Int {

        return if (referenceEvRepository != null) {

            val result = referenceEvRepository.clearAllReferences()

            result
        } else {
            0
        }
    }

    /**
     * Supprime toutes les équations de la base de données
     * @return Le nombre d'équations supprimées
     */
    suspend fun clearAllEquations(): Int {

        return if (equationRepository != null) {

            val result = equationRepository.clearAllEquations()

            result
        } else {
            0
        }
    }

    /**
     * Supprime toutes les références bibliographiques de la base de données
     * @return Le nombre de références bibliographiques supprimées
     */
    suspend fun clearAllBiblioRefs(): Int {

        return if (biblioRefRepository != null) {

            val result = biblioRefRepository.clearAllBiblioRefs()

            result
        } else {
            0
        }
    }

    /**
     * Relance l'import automatique des données initiales (aliments et références nutritionnelles)
     * @param forceImport Si true, force l'import même si les versions sont identiques
     * @return Le résultat de l'importation
     */
    suspend fun relaunchAutomaticImport(forceImport: Boolean = false): ImportResult {
        val logMessages = mutableListOf<String>()
        fun log(message: String) {
            logMessages.add(message)
            println("[IMPORT] $message")
        }
        
        return try {
            log("=".repeat(60))
            log("🚀 DÉBUT DE L'IMPORT AUTOMATIQUE")
            log("Force import: $forceImport")
            log("=".repeat(60))
            
            // Créer l'ExportImportRepository avec tous les repositories nécessaires
            log("Création de l'ExportImportRepository...")
            val exportImportRepo =
                    fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository(
                            animalRepository = animalRepository,
                            foodRepository = foodRepository,
                            equationRepository = equationRepository,
                            referenceRepository = referenceEvRepository,
                            biblioRepository = biblioRefRepository,
                            consultationRepository = consultationRepository,
                            recipeRepository = recipeRepository,
                            conseilRepository = conseilRepository
                    )
            log("✓ ExportImportRepository créé")

            // Lire le fichier de ressources pour l'import automatique
            log("Lecture du fichier JSON de ressources...")
            val json =
                    try {
                        // Essayer d'abord le chemin iOS (direct), puis le chemin Android/Desktop
                        // (data/)
                        try {
                            log("Tentative de lecture: vetnutri_export_init.json")
                            val result = fr.vetbrain.vetnutri_mp.Localization.ResourceReader()
                                    .readResource("vetnutri_export_init.json")
                            log("✓ Fichier lu avec succès (${result.length} caractères)")
                            result
                        } catch (e: Exception) {
                            log("⚠ Chemin direct échoué, tentative: data/vetnutri_export_init.json")
                            val result = fr.vetbrain.vetnutri_mp.Localization.ResourceReader()
                                    .readResource("data/vetnutri_export_init.json")
                            log("✓ Fichier lu avec succès (${result.length} caractères)")
                            result
                        }
                    } catch (e: Exception) {
                        log("❌ ERREUR: Fichier introuvable - ${e.message}")
                        throw IllegalStateException(
                                "Fichier vetnutri_export_init.json introuvable: ${e.message}"
                        )
                    }

            if (json.isEmpty()) {
                log("❌ ERREUR: Le fichier JSON est vide")
                throw IllegalStateException("Le fichier JSON d'import automatique est vide")
            }

            // Vérifier si une mise à jour est nécessaire
            log("Vérification de la version de la base de données...")
            val databaseVersionManager = fr.vetbrain.vetnutri_mp.Utils.DatabaseVersionManager()
            val updateNeeded = databaseVersionManager.isJsonUpdateNeeded(json)
            log("Mise à jour nécessaire: $updateNeeded")

            // 🔧 CORRECTION : Vérifier spécifiquement si les aliments sont manquants
            log("Vérification de l'état actuel de la base de données...")
            val currentFoodCount = foodRepository.getAllFoods().size
            val currentReferenceCount = referenceEvRepository?.getAllReferenceEv()?.size ?: 0
            val foodsAreMissing = currentFoodCount == 0
            val databaseIsEmpty = currentFoodCount == 0 && currentReferenceCount == 0
            
            log("État actuel:")
            log("  - Aliments: $currentFoodCount")
            log("  - Références: $currentReferenceCount")
            log("  - Aliments manquants: $foodsAreMissing")
            log("  - Base vide: $databaseIsEmpty")

            // Si l'import n'est pas forcé, vérifier si une mise à jour est nécessaire
            if (!forceImport && !updateNeeded && !foodsAreMissing) {
                // Aucune mise à jour nécessaire et aliments présents
                val currentJsonVersion = databaseVersionManager.getStoredJsonVersion()
                log("ℹ️ Aucune mise à jour nécessaire")
                log("  Version JSON stockée: ${currentJsonVersion ?: "Aucune"}")
                log("✓ Import annulé (base déjà à jour)")
                log("=".repeat(60))
                return ImportResult.Success(
                        count = currentFoodCount + currentReferenceCount,
                        importedCount = 0,
                        conseils = 0
                )
            }

            // 🔧 CORRECTION : Forcer l'import si les aliments sont manquants ou si la base est vide
            if (foodsAreMissing || databaseIsEmpty) {
                log("⚠️ Import forcé: base de données incomplète ou vide")
            }

            // Lancer l'import avec un listener de progression
            log("Démarrage de l'import des données...")
            val importCounts =
                    exportImportRepo.importAll(
                            apiJson = json,
                            listener =
                                    fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository
                                            .ImportProgressListener(
                                                    onProgress = { progress ->
                                                        // Mettre à jour la progression si
                                                        // nécessaire
                                                        if ((progress * 100).toInt() % 10 == 0) {
                                                            log("📊 Progression: ${(progress * 100).toInt()}%")
                                                        }
                                                    },
                                                    onLog = { message ->
                                                        log("  → $message")
                                                    }
                                            )
                    )

            log("✓ Import terminé avec succès")
            log("Résultats de l'import:")
            log("  - Animaux: ${importCounts.animals}")
            log("  - Aliments: ${importCounts.foods}")
            log("  - Équations: ${importCounts.equations}")
            log("  - Références: ${importCounts.references}")
            log("  - Bibliographies: ${importCounts.biblios}")
            log("  - Rations: ${importCounts.rations}")
            log("  - Recettes: ${importCounts.recipes}")
            log("  - Conseils: ${importCounts.conseils}")

            // Mettre à jour la version JSON après import réussi
            log("Mise à jour de la version JSON stockée...")
            databaseVersionManager.updateJsonVersionAfterImport(json)
            val newStoredVersion = databaseVersionManager.getStoredJsonVersion()
            log("✓ Version JSON mise à jour: ${newStoredVersion ?: "Aucune"}")

            // Retourner le résultat de l'importation
            val totalCount = importCounts.animals + importCounts.foods + importCounts.equations + importCounts.references
            log("📊 Total importé: $totalCount éléments")
            log("=".repeat(60))
            log("✅ IMPORT TERMINÉ AVEC SUCCÈS")
            log("=".repeat(60))
            
            ImportResult.Success(
                    count = totalCount,
                    importedCount = totalCount,
                    conseils = importCounts.conseils
            )
        } catch (e: Exception) {
            log("=".repeat(60))
            log("❌ ERREUR LORS DE L'IMPORT")
            log("Type: ${e::class.simpleName}")
            log("Message: ${e.message}")
            log("Stack trace:")
            e.printStackTrace()
            log("=".repeat(60))
            ImportResult.Error("Erreur lors de l'import automatique: ${e.message}")
        }
    }

    /**
     * Lance l'import automatique en arrière-plan (survit à la destruction de la vue)
     * @param forceImport Si true, force l'import
     * @param onResult Callback appelé avec le résultat
     */
    fun launchAutomaticImport(
        forceImport: Boolean = false,
        onResult: (ImportResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = relaunchAutomaticImport(forceImport)
            onResult(result)
        }
    }
}
