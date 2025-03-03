package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseFoodRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class SettingsViewModel(
        private val animalRepository: AnimalRepository,
        private val foodRepository: DatabaseFoodRepository
) {
    private val _uiScale = MutableStateFlow(1f)
    val uiScale: StateFlow<Float> = _uiScale.asStateFlow()

    // Résultat de l'importation
    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult.asStateFlow()

    fun setUiScale(scale: Float) {
        val newScale = scale.coerceIn(0.5f, 2f)
        _uiScale.value = newScale
        AppSizes.adjustSize(newScale)
    }

    fun incrementUiScale() {
        setUiScale(_uiScale.value + 0.1f)
    }

    fun decrementUiScale() {
        setUiScale(_uiScale.value - 0.1f)
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
     * @return Le nombre d'aliments importés
     */
    suspend fun importFoods(jsonContent: String): Int {
        try {
            println(
                    "Début de l'importation des aliments. Taille du contenu: ${jsonContent.length} caractères"
            )

            // Tentative d'importation comme liste d'aliments
            val foodsJson = Json.decodeFromString<List<AlimentEvJson>>(jsonContent)
            println("Importation réussie: ${foodsJson.size} aliments trouvés")

            // Importer les aliments
            val importedCount = foodRepository.importFoods(foodsJson)
            println("${importedCount} aliments importés dans la base de données")

            // Mettre à jour le résultat de l'importation
            _importResult.value = ImportResult.Success(importedCount)

            return importedCount
        } catch (e: Exception) {
            println("Erreur lors de l'importation des aliments: ${e.message}")
            e.printStackTrace()

            // Mettre à jour le résultat de l'importation
            _importResult.value = ImportResult.Error(e.message ?: "Erreur inconnue")

            return 0
        }
    }

    /**
     * Importe les aliments à partir d'une liste d'AlimentEvJson déjà parsée.
     * @param foodsJson La liste d'aliments au format JSON
     * @return Le nombre d'aliments importés
     */
    suspend fun importFoodsFromList(foodsJson: List<AlimentEvJson>): Int {
        try {
            println("Importation de ${foodsJson.size} aliments...")

            // Importer les aliments
            val importedCount = foodRepository.importFoods(foodsJson)
            println("${importedCount} aliments importés dans la base de données")

            // Mettre à jour le résultat de l'importation
            _importResult.value = ImportResult.Success(importedCount)

            return importedCount
        } catch (e: Exception) {
            println("Erreur lors de l'importation des aliments: ${e.message}")
            e.printStackTrace()

            // Mettre à jour le résultat de l'importation
            _importResult.value = ImportResult.Error(e.message ?: "Erreur inconnue")

            return 0
        }
    }

    /** Classe scellée représentant le résultat de l'importation */
    sealed class ImportResult {
        data class Success(val count: Int) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }
}
