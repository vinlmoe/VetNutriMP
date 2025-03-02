package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "my_room.db")
    return Room.databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath,
    )
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
