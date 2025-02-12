package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.Room
import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseAnimalRepository
import java.io.File

actual object DatabaseModule {
    private lateinit var database: AppDatabase
    private lateinit var animalRepository: AnimalRepository

    actual fun initialize(databaseName: String) {
        val databasePath = File(System.getProperty("user.home"), ".vetnutri/$databaseName")
        databasePath.parentFile?.mkdirs()

        database = Room.databaseBuilder(AppDatabase::class.java, databasePath.absolutePath).build()

        animalRepository = DatabaseAnimalRepository(database.animalDao())
    }

    actual fun getAnimalRepository(): AnimalRepository {
        if (!::animalRepository.isInitialized) {
            throw IllegalStateException("DatabaseModule n'a pas été initialisé")
        }
        return animalRepository
    }
}
