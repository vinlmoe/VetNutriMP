package fr.vetbrain.vetnutri_mp.DataBase

import platform.SQLite.*

actual class PlatformDatabaseBuilder : DatabaseBuilder {
    override fun build(): AppDatabase {
        val database = sqlite3()
        val result = sqlite3_open_v2(
            AppDatabase.DATABASE_NAME,
            database,
            SQLITE_OPEN_READWRITE or SQLITE_OPEN_CREATE,
            null
        )

        if (result != SQLITE_OK) {
            throw IllegalStateException("Impossible d'ouvrir la base de données: $result")
        }

        return IosDatabase(database)
    }
} 