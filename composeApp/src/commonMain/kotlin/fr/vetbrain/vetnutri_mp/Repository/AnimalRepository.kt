package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
interface AnimalRepository {
    suspend fun saveAnimal(animal: AnimalEv)
    suspend fun getAllAnimals(): List<AnimalEv>
    suspend fun deleteAnimal(animal: AnimalEv)
}
