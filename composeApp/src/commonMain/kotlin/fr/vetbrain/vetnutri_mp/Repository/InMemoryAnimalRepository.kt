package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.AnimalEvJson
import fr.vetbrain.vetnutri_mp.Data.WeightDate
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
        animals.removeAll { it.uuid == animal.uuid }
        updateFlow()
    }

    override suspend fun updateAnimal(animal: AnimalEv) {
        val existingIndex = animals.indexOfFirst { it.uuid == animal.uuid }
        if (existingIndex >= 0) {
            animals[existingIndex] = animal
            updateFlow()
        }
    }

    override suspend fun getAnimalById(id: String): AnimalEv? {
        return animals.find { it.uuid == id }
    }

    override suspend fun importAnimals(animalsJson: List<AnimalEvJson>): AnimalImportResult {
        var importedCount = 0
        var updatedCount = 0
        var errorCount = 0

        animalsJson.forEach { animalJson ->
            try {
                // Convertir manuellement AnimalEvJson en AnimalEv
                val animal =
                        AnimalEv(
                                uuid = animalJson.UUID,
                                nom = animalJson.nom,
                                dead = animalJson.dead,
                                id = animalJson.id,
                                sexId = animalJson.sex,
                                specieId = animalJson.espece,
                                ownerName = animalJson.nomProprio,
                                birthdate = animalJson.dateNaiss,
                                race = animalJson.race,
                                summary = animalJson.resume
                        )

                // Ajouter les poids à l'historique
                animal.weightHistory =
                        animalJson
                                .listWeight
                                .map { weightJson ->
                                    WeightDate(
                                            uuid = weightJson.UUID,
                                            refAnimal = animalJson.UUID,
                                            date = weightJson.date,
                                            value = weightJson.value
                                    )
                                }
                                .toMutableList()

                // Vérifier si l'animal existe déjà
                val existingIndex = animals.indexOfFirst { it.uuid == animal.uuid }
                if (existingIndex >= 0) {
                    // Mettre à jour l'animal existant
                    animals[existingIndex] = animal
                    updatedCount++
                } else {
                    // Ajouter le nouvel animal
                    animals.add(animal)
                    importedCount++
                }
            } catch (e: Exception) {
                errorCount++
            }
        }

        updateFlow()

        return AnimalImportResult(
                importedCount = importedCount,
                updatedCount = updatedCount,
                errorCount = errorCount,
                totalCount = animals.size,
                foodsImportedCount = 0
        )
    }

    private fun updateFlow() {
        animalsFlow.value = animals.toList()
    }

    /**
     * Récupère le repository des aliments
     *
     * @return Le repository des aliments ou null s'il n'est pas disponible
     */
    override fun getFoodRepository(): FoodRepository? {
        return null
    }
}
