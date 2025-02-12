package fr.vetbrain.vetnutri_mp.DataBase

import android.content.Context
import androidx.room.Room
import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseAnimalRepository

actual object DatabaseModule {
    private lateinit var database: AppDatabase
    private lateinit var animalRepository: AnimalRepository
    private lateinit var applicationContext: Context

    fun initializeContext(context: Context) {
        applicationContext = context.applicationContext
    }

    actual fun initialize(databaseName: String) {
        if (!::applicationContext.isInitialized) {
            throw IllegalStateException("Context n'a pas été initialisé. Appelez initializeContext d'abord.")
        }

        database = Room.databaseBuilder(
            context = applicationContext,
            klass = AppDatabase::class.java,
            name = databaseName
        ).build()

        animalRepository = DatabaseAnimalRepository(database.animalDao())
    }

    actual fun getAnimalRepository(): AnimalRepository {
        if (!::animalRepository.isInitialized) {
            throw IllegalStateException("DatabaseModule n'a pas été initialisé")
        }
        return animalRepository
    }
} 