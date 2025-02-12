package fr.vetbrain.vetnutri_mp.DataBase

import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository

actual object DatabaseModule {
    private lateinit var database: AppDatabase
    private lateinit var animalRepository: AnimalRepository

    actual fun initialize(databaseName: String) {
        // TODO: Implémenter la création de la base de données pour iOS
        throw NotImplementedError("La base de données n'est pas encore implémentée pour iOS")
    }

    actual fun getAnimalRepository(): AnimalRepository {
        if (!::animalRepository.isInitialized) {
            throw IllegalStateException("DatabaseModule n'a pas été initialisé")
        }
        return animalRepository
    }
}
