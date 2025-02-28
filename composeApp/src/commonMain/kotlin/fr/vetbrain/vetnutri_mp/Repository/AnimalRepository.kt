package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.DataBase.AnimalEntity

import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalUuidApi::class)
interface AnimalRepository {
    suspend fun saveAnimal(animal: AnimalEv)
    suspend fun getAllAnimals(): List<AnimalEv>
}
