package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.AnimalEvJson
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
interface AnimalRepository {
    suspend fun saveAnimal(animal: AnimalEv)
    suspend fun getAllAnimals(): List<AnimalEv>
    suspend fun deleteAnimal(animal: AnimalEv)

    /**
     * Importe une liste d'animaux à partir de leurs représentations JSON
     *
     * @param animalsJson La liste des animaux au format JSON
     * @return Le nombre d'animaux importés avec succès
     */
    suspend fun importAnimals(animalsJson: List<AnimalEvJson>): Int
}
