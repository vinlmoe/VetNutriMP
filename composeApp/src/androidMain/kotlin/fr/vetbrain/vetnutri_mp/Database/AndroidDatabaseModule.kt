package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.Room
import fr.vetbrain.vetnutri_mp.Localization.AndroidContext

class AndroidDatabaseModule : DatabaseModule {
    private var database: AndroidDatabase? = null

    override fun initialize() {
        if (database == null) {
            database =
                    Room.databaseBuilder(
                                    AndroidContext.appContext,
                                    AndroidDatabase::class.java,
                                    DatabaseFactory.DATABASE_NAME
                            )
                            .build()
        }
    }

    override fun getAnimalDao(): CommonAnimalDao {
        checkNotNull(database) { "La base de données doit être initialisée d'abord" }
        return database!!.animalDao()
    }

    override fun getFoodDao(): CommonFoodDao {
        checkNotNull(database) { "La base de données doit être initialisée d'abord" }
        return database!!.foodDao()
    }

    override fun getDatabasePath(): String {
        return AndroidContext.appContext.getDatabasePath(DatabaseFactory.DATABASE_NAME).absolutePath
    }
}

actual fun createPlatformDatabaseModule(): DatabaseModule = AndroidDatabaseModule()
