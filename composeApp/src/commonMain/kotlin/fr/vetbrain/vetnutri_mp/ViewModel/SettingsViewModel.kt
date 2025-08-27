package fr.vetbrain.vetnutri_mp.ViewModel

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
                null
) {
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
                val nonResolvedNutrients: Int = 0
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
        val alimentsJson =
                Json { ignoreUnknownKeys = true }.decodeFromString<List<AlimentEvJson>>(jsonContent)
        return foodRepository.importFoods(alimentsJson)
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
    suspend fun importFoodsFromList(foodsList: List<AlimentEvJson>): FoodImportResult {
        val result = foodRepository.importFoods(foodsList)
        // Mettre à jour le résultat de l'importation pour l'affichage dans l'interface
        _importResult.value =
                ImportResult.Success(
                        count = result.totalCount,
                        importedCount = result.importedCount,
                        updatedCount = result.updatedCount,
                        deletedCount = result.deletedCount,
                        errorCount = result.errorCount,
                        nonResolvedNutrients = result.nonResolvedNutrientsCount
                )
        return result
    }

    /**
     * Supprime toutes les références de la base de données
     * @return Le nombre de références supprimées
     */
    suspend fun clearAllReferences(): Int {
        println("DEBUG SettingsViewModel: clearAllReferences() appelée")

        return if (referenceEvRepository != null) {
            println("DEBUG SettingsViewModel: Repository non null, appel de clearAllReferences()")
            val result = referenceEvRepository.clearAllReferences()
            println("DEBUG SettingsViewModel: clearAllReferences() a retourné: $result")
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
        println("DEBUG SettingsViewModel: clearAllEquations() appelée")

        return if (equationRepository != null) {
            println("DEBUG SettingsViewModel: Repository non null, appel de clearAllEquations()")
            val result = equationRepository.clearAllEquations()
            println("DEBUG SettingsViewModel: clearAllEquations() a retourné: $result")
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
        println("DEBUG SettingsViewModel: clearAllBiblioRefs() appelée")

        return if (biblioRefRepository != null) {
            println("DEBUG SettingsViewModel: Repository non null, appel de clearAllBiblioRefs()")
            val result = biblioRefRepository.clearAllBiblioRefs()
            println("DEBUG SettingsViewModel: clearAllBiblioRefs() a retourné: $result")
            result
        } else {
            0
        }
    }

    /**
     * Relance l'import automatique des données initiales (aliments et références nutritionnelles)
     * @return Le résultat de l'importation
     */
    suspend fun relaunchAutomaticImport(): ImportResult {
        return try {
            // Créer l'ExportImportRepository avec tous les repositories nécessaires
            val exportImportRepo =
                    fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository(
                            animalRepository = animalRepository,
                            foodRepository = foodRepository,
                            equationRepository = equationRepository,
                            referenceRepository = referenceEvRepository,
                            biblioRepository = biblioRefRepository,
                            consultationRepository = consultationRepository,
                            recipeRepository = recipeRepository
                    )

            // Lire le fichier de ressources pour l'import automatique
            val json =
                    try {
                        // Essayer d'abord le chemin iOS (direct), puis le chemin Android/Desktop
                        // (data/)
                        try {
                            fr.vetbrain.vetnutri_mp.Localization.ResourceReader()
                                    .readResource("vetnutri_export_init.json")
                        } catch (e: Exception) {
                            fr.vetbrain.vetnutri_mp.Localization.ResourceReader()
                                    .readResource("data/vetnutri_export_init.json")
                        }
                    } catch (e: Exception) {
                        throw IllegalStateException(
                                "Fichier vetnutri_export_init.json introuvable: ${e.message}"
                        )
                    }

            if (json.isEmpty()) {
                throw IllegalStateException("Le fichier JSON d'import automatique est vide")
            }

            // Lancer l'import avec un listener de progression
            val importCounts =
                    exportImportRepo.importAll(
                            apiJson = json,
                            listener =
                                    fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository
                                            .ImportProgressListener(
                                                    onProgress = { progress ->
                                                        // Mettre à jour la progression si
                                                        // nécessaire
                                                        println(
                                                                "IMPORT AUTO: Progression: ${(progress * 100).toInt()}%"
                                                        )
                                                    },
                                                    onLog = { msg -> println("IMPORT AUTO: $msg") }
                                            )
                    )

            // Retourner le résultat de l'importation
            ImportResult.Success(
                    count =
                            importCounts.animals +
                                    importCounts.foods +
                                    importCounts.equations +
                                    importCounts.references,
                    importedCount =
                            importCounts.animals +
                                    importCounts.foods +
                                    importCounts.equations +
                                    importCounts.references
            )
        } catch (e: Exception) {
            ImportResult.Error("Erreur lors de l'import automatique: ${e.message}")
        }
    }
}
