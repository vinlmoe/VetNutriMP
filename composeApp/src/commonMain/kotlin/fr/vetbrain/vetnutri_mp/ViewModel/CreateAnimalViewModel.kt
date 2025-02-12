package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.vetbrain.vetnutri_mp.Data.Animal
import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class CreateAnimalViewModel(private val animalRepository: AnimalRepository) : ViewModel() {
    private val _animal = MutableStateFlow(Animal(espece = Animal.Espece.CHIEN))
    val animal: StateFlow<Animal> = _animal

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    fun updateAnimal(animal: Animal) {
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
}
