package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalUuidApi::class)
class AnimalListViewModel(private val animalRepository: AnimalRepository) : ViewModel() {
    private val _animals = MutableStateFlow<List<AnimalEv>>(emptyList())
    val animals: StateFlow<List<AnimalEv>> = _animals

    fun loadAnimals() {
        viewModelScope.launch { _animals.value = animalRepository.getAllAnimals() }
    }
}
