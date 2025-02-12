package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.Animal
import fr.vetbrain.vetnutri_mp.DataBase.AnimalDao
import fr.vetbrain.vetnutri_mp.DataBase.AnimalEntity
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalUuidApi::class)
class DatabaseAnimalRepository(private val animalDao: AnimalDao) : AnimalRepository {
        override suspend fun saveAnimal(animal: Animal): Unit =
                withContext(Dispatchers.Default) {
                        val entity =
                                AnimalEntity(
                                        UUID = animal.uuid,
                                        name = animal.nom,
                                        dead = if (animal.mort) 1 else 0,
                                        id = animal.id.toString(),
                                        sex = animal.sexe.ordinal,
                                        specie = animal.espece.name,
                                        ownerName = animal.nomProprio,
                                        birthdate = animal.dateNaissance?.toString() ?: "",
                                        race = animal.race,
                                        summary = animal.resume
                                )
                        animalDao.insertAnimal(entity)
                }

        override suspend fun getAllAnimals(): List<Animal> =
                withContext(Dispatchers.Default) {
                        animalDao.getAllAnimals().first().map { entity ->
                                Animal(
                                        uuid = entity.UUID,
                                        id = entity.id.toLongOrNull() ?: 0L,
                                        nom = entity.name,
                                        espece = Animal.Espece.valueOf(entity.specie),
                                        sexe = Animal.Sexe.values()[entity.sex],
                                        race = entity.race,
                                        dateNaissance =
                                                if (entity.birthdate.isNotEmpty())
                                                        LocalDate.parse(entity.birthdate)
                                                else null,
                                        nomProprio = entity.ownerName,
                                        resume = entity.summary,
                                        mort = entity.dead == 1
                                )
                        }
                }
}
