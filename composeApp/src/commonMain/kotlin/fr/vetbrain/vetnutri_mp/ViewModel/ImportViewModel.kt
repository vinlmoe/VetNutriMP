package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ImportViewModel(
        private val animalRepository: AnimalRepository,
        private val coroutineScope: CoroutineScope
) {
    // Flag pour indiquer si les aliments doivent être supprimés avant l'importation
    var shouldClearFoodsBeforeImport: Boolean = false

    // Créer une instance d'AnimalListViewModel pour utiliser ses fonctions d'importation
    private val animalListViewModel = AnimalListViewModel(animalRepository)

    // État de l'importation
    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    // Résultat de l'importation
    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult.asStateFlow()

    init {
        // Observer les résultats d'importation d'AnimalListViewModel
        coroutineScope.launch {
            animalListViewModel.importResult.collectLatest { result ->
                result?.let {
                    when (it) {
                        is AnimalListViewModel.ImportResult.Success -> {
                            _importResult.value =
                                    ImportResult.Success(
                                            animalCount = it.count,
                                            foodCount =
                                                    0 // On ne peut pas savoir combien d'aliments
                                            // ont été importés
                                            )
                        }
                        is AnimalListViewModel.ImportResult.Error -> {
                            _importResult.value = ImportResult.Error(it.message)
                        }
                    }
                }
            }
        }
    }

    /**
     * Importe des animaux à partir d'une chaîne JSON
     *
     * @param jsonContent Le contenu JSON à désérialiser
     */
    fun importAnimalsFromJson(jsonContent: String) {
        _isImporting.value = true
        coroutineScope.launch(AppDispatchers.IO) {
            try {
                // Vider la base d'aliments si demandé
                if (shouldClearFoodsBeforeImport) {
                    println("Suppression de tous les aliments existants...")
                    val foodRepository = animalRepository.getFoodRepository()
                    if (foodRepository != null) {
                        val deletedCount = foodRepository.clearAllFoods()
                        println("$deletedCount aliments ont été supprimés")
                    } else {
                        println("Le repository des aliments n'est pas disponible.")
                    }

                    // Réinitialiser le flag après utilisation
                    shouldClearFoodsBeforeImport = false
                }

                // Utiliser la méthode existante d'AnimalListViewModel
                animalListViewModel.importAnimalsFromJson(jsonContent)
            } catch (e: Exception) {
                _importResult.value = ImportResult.Error(e.message ?: "Erreur inconnue")
                _isImporting.value = false
            }
        }
    }

    /** Réinitialise le résultat de l'importation */
    fun resetImportResult() {
        _importResult.value = null
        animalListViewModel.resetImportResult()
    }

    /**
     * Définit une erreur d'importation
     *
     * @param message Le message d'erreur à afficher
     */
    fun setImportError(message: String) {
        _importResult.value = ImportResult.Error(message)
    }

    /**
     * Délègue l'importation des animaux à la fonction de plateforme spécifique Cela permet d'éviter
     * l'ambiguïté d'appel direct dans la vue
     */
    fun importAnimalsFromFileUI() {
        fr.vetbrain.vetnutri_mp.importAnimalsFromFile(animalListViewModel)
    }

    /** Classe scellée représentant le résultat de l'importation */
    sealed class ImportResult {
        data class Success(val animalCount: Int, val foodCount: Int = 0) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }
}
