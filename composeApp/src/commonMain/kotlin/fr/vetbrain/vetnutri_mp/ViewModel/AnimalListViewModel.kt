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
class AnimalListViewModel(private val animalRepository: AnimalRepository) : ViewModel() {
    private val _animals = MutableStateFlow<List<Animal>>(emptyList())
    val animals: StateFlow<List<Animal>> = _animals

    fun loadAnimals() {
        viewModelScope.launch { _animals.value = animalRepository.getAllAnimals() }
    }
}
