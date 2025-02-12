package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.Animal
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class TestAnimalRepository : AnimalRepository {
    private val animals = mutableListOf<Animal>()
    var lastSavedAnimal: Animal? = null
        private set

    override suspend fun saveAnimal(animal: Animal) {
        lastSavedAnimal = animal
        val index = animals.indexOfFirst { it.uuid == animal.uuid }
        if (index != -1) {
            animals[index] = animal
        } else {
            animals.add(animal)
        }
    }

    override suspend fun getAllAnimals(): List<Animal> = animals.toList()

    fun clear() {
        animals.clear()
        lastSavedAnimal = null
    }
}
