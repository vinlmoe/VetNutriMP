package fr.vetbrain.vetnutri_mp.DataBase

import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseAnimalRepository
import okio.Path

actual object DatabaseModule {
    private lateinit var database: AppDatabase
    private lateinit var animalRepository: AnimalRepository

    actual fun initialize(databaseName: String) {
        ensureDatabaseDirectoryExists()
        database = DatabaseBuilder().build()
        animalRepository = DatabaseAnimalRepository(database.animalDao())
    }

    actual fun getAnimalRepository(): AnimalRepository {
        if (!::animalRepository.isInitialized) {
            throw IllegalStateException(
                    "DatabaseModule n'a pas été initialisé. Appelez initialize() d'abord."
            )
        }
        return animalRepository
    }

    actual fun getDatabasePath(): Path {
        return DatabaseBuilder.getDatabasePath()
    }
}
