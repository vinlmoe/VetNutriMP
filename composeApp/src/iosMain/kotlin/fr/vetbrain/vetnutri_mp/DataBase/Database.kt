package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = documentDirectory() + "/vetnutri.db"
    return Room.databaseBuilder<AppDatabase>(
            name = dbFilePath,
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory =
            NSFileManager.defaultManager.URLForDirectory(
                    directory = NSDocumentDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = null,
                    create = false,
                    error = null,
            )

    actual object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
        private lateinit var instance: AppDatabase

        override fun initialize(): AppDatabase {
            if (!::instance.isInitialized) {
                throw IllegalStateException(
                        "La base de données n'a pas été initialisée. Assurez-vous d'appeler getRoomDatabase() d'abord."
                )
            }
            return instance
        }

        fun setInstance(database: AppDatabase) {
            instance = database
        }
    }
    return requireNotNull(documentDirectory?.path)
}

actual object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    private lateinit var instance: AppDatabase

    override fun initialize(): AppDatabase {
        if (!::instance.isInitialized) {
            throw IllegalStateException(
                    "La base de données n'a pas été initialisée. Assurez-vous d'appeler getRoomDatabase() d'abord."
            )
        }
        return instance
    }

    fun setInstance(database: AppDatabase) {
        instance = database
    }
}
