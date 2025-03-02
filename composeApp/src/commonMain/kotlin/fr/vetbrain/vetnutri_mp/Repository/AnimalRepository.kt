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
     * Met à jour un animal existant dans la base de données
     *
     * @param animal L'animal à mettre à jour
     */
    suspend fun updateAnimal(animal: AnimalEv)

    /**
     * Récupère un animal par son identifiant
     *
     * @param id L'identifiant de l'animal à récupérer
     * @return L'animal correspondant à l'identifiant ou null s'il n'existe pas
     */
    suspend fun getAnimalById(id: String): AnimalEv?

    /**
     * Importe une liste d'animaux à partir de leurs représentations JSON
     *
     * @param animalsJson La liste des animaux au format JSON
     * @return Le nombre d'animaux importés avec succès
     */
    suspend fun importAnimals(animalsJson: List<AnimalEvJson>): Int
    
    /**
     * Récupère un animal avec toutes ses relations (consultations, rations, aliments)
     * 
     * @param id L'identifiant de l'animal à récupérer
     * @return L'animal complet avec toutes ses relations ou null si non trouvé
     */
    suspend fun getAnimalWithRelations(id: String): AnimalEv?
    
    /**
     * Récupère le repository des aliments
     *
     * @return Le repository des aliments ou null s'il n'est pas disponible
     */
    fun getFoodRepository(): FoodRepository?
}
