package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.AnimalEvJson
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
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

    override suspend fun getAnimalWithRelations(id: String): AnimalEv? {
        // Pour l'implémentation en mémoire, on retourne simplement l'animal
        // car les relations sont déjà chargées en mémoire
        return getAnimalById(id)
    }

    override suspend fun importAnimals(animalsJson: List<AnimalEvJson>): Int {
        var importedCount = 0

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

                // Ajouter les consultations - gérer les deux formats possibles
                val consultations =
                        when {
                            // Format 1: consultations directement dans l'objet animal
                            animalJson.consultations != null -> animalJson.consultations

                            // Format 2: consultations dans list.consultations
                            animalJson.list != null -> animalJson.list.consultations

                            // Aucune consultation
                            else -> emptyList()
                        }

                animal.consultations =
                        consultations
                                .map { consultJson ->
                                    ConsultationEv(
                                            uuid = consultJson.UUID,
                                            idAnim = animalJson.UUID,
                                            date = consultJson.date,
                                            objectConsult = consultJson.objet ?: "",
                                            observation = consultJson.observation ?: "",
                                            cRendu = consultJson.CRendu,
                                            weight = consultJson.Poids,
                                            idealWeight = consultJson.PoidsIdeal,
                                            water = consultJson.Boisson,
                                            bodyFat = consultJson.TauxMG
                                    )
                                }
                                .toMutableList()

                saveAnimal(animal)
                importedCount++
            } catch (e: Exception) {
                // Ignorer les erreurs d'importation pour un animal spécifique
                println("Erreur lors de l'importation de l'animal: ${e.message}")
            }
        }

        return importedCount
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
