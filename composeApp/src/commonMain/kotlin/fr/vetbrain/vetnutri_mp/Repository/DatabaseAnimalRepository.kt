package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.DataBase.AnimalDao
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toEntity
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class DatabaseAnimalRepository(private val animalDao: AnimalDao) : AnimalRepository {
    override suspend fun saveAnimal(animal: AnimalEv) {
        withContext(AppDispatchers.Default) { animalDao.insert(animal.toEntity()) }
    }

    override suspend fun getAllAnimals(): List<AnimalEv> {
        return withContext(AppDispatchers.Default) {
            animalDao.getAllAnimals().map { entity ->
                AnimalEv(
                        uuid = entity.uuid,
                        nom = entity.nom ?: "",
                        dead = entity.dead ?: false,
                        id = entity.id,
                        sexId = entity.sexId ?: 0,
                        specieId = entity.specieId ?: "",
                        ownerName = entity.ownerName ?: "",
                        birthdate = entity.birthdate?.let { LocalDate.parse(it) },
                        race = entity.race ?: "",
                        summary = entity.summary ?: ""
                )
            }
        }
    }

    override suspend fun deleteAnimal(animal: AnimalEv) {
        withContext(AppDispatchers.Default) {
            animalDao.delete(animal.toEntity(includeRelations = false))
        }
    }
}
