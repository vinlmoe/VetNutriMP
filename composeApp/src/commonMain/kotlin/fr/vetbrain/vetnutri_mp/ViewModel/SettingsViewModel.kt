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
        private val animalRepository: AnimalRepository,
        private val foodRepository: DatabaseFoodRepository
) {
    private val _uiScale = MutableStateFlow(1f)
    val uiScale: StateFlow<Float> = _uiScale.asStateFlow()

    // Résultat de l'importation
    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult.asStateFlow()

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
    fun setUiScale(scale: Float) {
        val newScale = scale.coerceIn(0.5f, 2f)
        _uiScale.value = newScale
        AppSizes.adjustSize(newScale)
    }

    /** Augmente l'échelle de l'interface utilisateur */
    fun incrementUiScale() {
        setUiScale(_uiScale.value + 0.1f)
    }

    /** Diminue l'échelle de l'interface utilisateur */
    fun decrementUiScale() {
        setUiScale(_uiScale.value - 0.1f)
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

    /**
     * Importe les aliments à partir d'un contenu JSON.
     * @param jsonContent Le contenu JSON à importer
     * @return Le résultat détaillé de l'importation
     */
    suspend fun importFoodsFromJson(jsonContent: String): FoodImportResult {
        // Utilisation de kotlinx.serialization pour parser le JSON
        val alimentsJson = Json.decodeFromString<List<AlimentEvJson>>(jsonContent)
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
        println(
                "Importation terminée. ${result.importedCount} aliments importés, ${result.updatedCount} mis à jour, ${result.deletedCount} supprimés."
        )
        return result
    }
}
