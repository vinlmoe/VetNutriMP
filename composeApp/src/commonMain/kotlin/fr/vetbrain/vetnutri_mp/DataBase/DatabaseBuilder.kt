package fr.vetbrain.vetnutri_mp.DataBase

import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseAnimalRepository

object DatabaseBuilder {
    private var database: AppDatabase? = null
    private var animalRepository: AnimalRepository? = null

    fun initialize() {
        if (database == null) {
            database = createDatabase()
            animalRepository = DatabaseAnimalRepository(database!!.animalDao())
        }
    }

    fun getAnimalRepository(): AnimalRepository {
        checkNotNull(animalRepository) {
            "DatabaseBuilder n'a pas été initialisé. Appelez initialize() d'abord."
        }
        return animalRepository!!
    }

    fun getDatabase(): AppDatabase {
        checkNotNull(database) {
            "DatabaseBuilder n'a pas été initialisé. Appelez initialize() d'abord."
        }
        return database!!
    }
}

expect class PlatformDatabaseBuilder() : DatabaseBuilder

object DatabaseUtils {
    fun getDatabasePath(): String = AppDatabase.DATABASE_NAME

    fun getDatabaseDir(): String {
        return "database"
    }

    fun ensureDatabaseDirectoryExists() {
        // Implémenté spécifiquement pour chaque plateforme
    }
}
