package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.DataBase.AnimalDao
import fr.vetbrain.vetnutri_mp.DataBase.AnimalEntity
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalUuidApi::class)
class DatabaseAnimalRepository(private val animalDao: AnimalDao) : AnimalRepository {
        override suspend fun saveAnimal(animal: AnimalEv): Unit =
                withContext(Dispatchers.Default) {
                        val entity =
                                AnimalEntity(
                                        uuid = animal.uuid,
                                        nom = animal.nom,
                                        dead = animal.dead,
                                        id = animal.id,
                                        sexId = animal.sexId,
                                        specieId = animal.specieId,
                                        ownerName = animal.ownerName,
                                        birthdate = animal.birthdate.toString(),
                                        race = animal.race,
                                        summary = animal.summary
                                )
                        animalDao.insert(entity)
                }

        override suspend fun getAllAnimals(): List<AnimalEv> {
                return withContext(Dispatchers.Default) {
                        animalDao.getAllAnimals().map { entity ->
                                AnimalEv(
                                        uuid = entity.uuid ?: "",
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
}
