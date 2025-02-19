package fr.vetbrain.vetnutri_mp.DataBase

import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteConnection

actual open class DatabaseBuilder {
    actual fun build(): AppDatabase {
        val dbPath = getDatabasePath()
        ensureDatabaseDirectoryExists()

        val config =
                SQLiteConfig().apply {
                    setSharedCache(true)
                    enableLoadExtension(true)
                }

        val connection =
                SQLiteConnection.createConnection("jdbc:sqlite:${dbPath}", config.toProperties())

        val database = createDatabase(connection)
        initializeDatabase(database)

        return database
    }

    private fun createDatabase(connection: SQLiteConnection): AppDatabase {
        // Création de la base de données avec les DAOs nécessaires
        return object : AppDatabase() {
            override fun animalDao(): AnimalDao = DesktopAnimalDao(connection)
            override fun foodDao(): FoodDao = DesktopFoodDao(connection)
            override fun referenceDao(): ReferenceDao = DesktopReferenceDao(connection)
        }
    }

    private fun initializeDatabase(database: AppDatabase) {
        // Création des tables si elles n'existent pas
        database.animalDao().createTablesIfNotExist()
        database.foodDao().createTablesIfNotExist()
        database.referenceDao().createTablesIfNotExist()
    }
}
