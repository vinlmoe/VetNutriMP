package fr.vetbrain.vetnutri_mp.DataBase

import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository

expect object DatabaseModule {
    fun initialize(databaseName: String = AppDatabase.DATABASE_NAME)
    fun getAnimalRepository(): AnimalRepository
}
