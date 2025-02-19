package fr.vetbrain.vetnutri_mp.DataBase

interface DatabaseModule {
    fun initialize()
    fun getAnimalDao(): AnimalDao
    fun getFoodDao(): FoodDao
    fun getDatabasePath(): String
}

object DatabaseFactory {
    const val DATABASE_NAME = "vetnutri.db"
    const val DATABASE_VERSION = 1

    private var instance: DatabaseModule? = null

    fun initialize() {
        if (instance == null) {
            instance = createPlatformDatabaseModule()
            instance?.initialize()
        }
    }

    fun getInstance(): DatabaseModule {
        return instance ?: throw IllegalStateException("DatabaseFactory n'a pas été initialisé")
    }
}

expect fun createPlatformDatabaseModule(): DatabaseModule

// Les fonctions communes sont déplacées vers DatabaseUtils
