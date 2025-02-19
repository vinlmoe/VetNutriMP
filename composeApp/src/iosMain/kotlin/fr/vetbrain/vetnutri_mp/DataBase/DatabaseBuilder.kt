package fr.vetbrain.vetnutri_mp.DataBase

import platform.SQLite.SQLITE_OPEN_CREATE
import platform.SQLite.SQLITE_OPEN_READWRITE
import platform.SQLite.sqlite3
import platform.SQLite.sqlite3_open_v2

actual class DatabaseBuilder {
    actual fun build(): AppDatabase {
        val dbPath = getDatabasePath()
        ensureDatabaseDirectoryExists()

        val database = sqlite3()
        val result =
                sqlite3_open_v2(
                        dbPath.toString(),
                        database,
                        SQLITE_OPEN_READWRITE or SQLITE_OPEN_CREATE,
                        null
                )

        if (result != 0) {
            throw IllegalStateException("Impossible d'ouvrir la base de données: $result")
        }

        return IosAppDatabase(database)
    }

    private class IosAppDatabase(private val sqliteDatabase: sqlite3) : AppDatabase() {
        override fun animalDao(): AnimalDao = IosAnimalDao(sqliteDatabase)
        override fun foodDao(): FoodDao = IosFoodDao(sqliteDatabase)
        override fun referenceDao(): ReferenceDao = IosReferenceDao(sqliteDatabase)

        init {
            // Création des tables si elles n'existent pas
            animalDao().createTablesIfNotExist()
            foodDao().createTablesIfNotExist()
            referenceDao().createTablesIfNotExist()
        }
    }
}
