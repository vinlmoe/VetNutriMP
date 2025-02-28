package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.AnimalEvJson
import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalUuidApi::class)
class AnimalListViewModel(private val animalRepository: AnimalRepository) : ViewModel() {
    private val _animals = MutableStateFlow<List<AnimalEv>>(emptyList())
    val animals: StateFlow<List<AnimalEv>> = _animals

    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult

    fun loadAnimals() {
        viewModelScope.launch { _animals.value = animalRepository.getAllAnimals() }
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
                val importedCount = animalRepository.importAnimals(animalsJson)
                _importResult.value = ImportResult.Success(importedCount)
                loadAnimals() // Refresh the list after import
            } catch (e: Exception) {
                _importResult.value = ImportResult.Error(e.message ?: "Erreur inconnue")
            }
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

    sealed class ImportResult {
        data class Success(val count: Int) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }
}
