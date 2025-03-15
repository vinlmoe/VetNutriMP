package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalUuidApi::class)
class CreateAnimalViewModel(private val animalRepository: AnimalRepository) : ViewModel() {
    private val _animal = MutableStateFlow(AnimalEv(specieId = Espece.CHIEN.label))
    val animal: StateFlow<AnimalEv> = _animal

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    fun updateAnimal(animal: AnimalEv) {
        _animal.value = animal
    }

    fun saveAnimal() {
        viewModelScope.launch {
            try {
                _isSaving.value = true
                animalRepository.saveAnimal(_animal.value)
                _saveSuccess.value = true
            } catch (e: Exception) {
                // Gérer l'erreur ici
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun resetSaveStatus() {
        _saveSuccess.value = false
    }

    fun resetAnimal() {
        _animal.value = AnimalEv(specieId = Espece.CHIEN.label)
    }
}
