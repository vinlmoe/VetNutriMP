package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryAnimalRepository : AnimalRepository {
    private val animals = mutableListOf<AnimalEv>()
    private val animalsFlow = MutableStateFlow<List<AnimalEv>>(emptyList())

    override suspend fun saveAnimal(animal: AnimalEv) {
        val existingIndex = animals.indexOfFirst { it.uuid == animal.uuid }
        if (existingIndex >= 0) {
            animals[existingIndex] = animal
        } else {
            animals.add(animal)
        }
        updateFlow()
    }

    override suspend fun getAllAnimals(): List<AnimalEv> {
        return animals.toList()
    }

    override suspend fun deleteAnimal(animal: AnimalEv) {
        animals.removeIf { it.uuid == animal.uuid }
        updateFlow()
    }

    private fun updateFlow() {
        animalsFlow.value = animals.toList()
    }
}
