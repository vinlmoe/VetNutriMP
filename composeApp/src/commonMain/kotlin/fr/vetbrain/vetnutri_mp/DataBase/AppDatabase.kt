package fr.vetbrain.vetnutri_mp.DataBase

interface AppDatabase {
    fun animalDao(): AnimalDao
    fun foodDao(): FoodDao
    fun clearAllTables()

    companion object {
        const val DATABASE_NAME = "vetnutri.db"
    }
}

expect fun createDatabase(): AppDatabase
