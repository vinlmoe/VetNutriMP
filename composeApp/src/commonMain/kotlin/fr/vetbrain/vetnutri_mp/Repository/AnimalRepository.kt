package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.Animal
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
interface AnimalRepository {
    suspend fun saveAnimal(animal: Animal)
    suspend fun getAllAnimals(): List<Animal>
}
