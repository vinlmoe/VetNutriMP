package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val userHome = System.getProperty("user.home")
    val dataDir = File(userHome, ".vetnutri_mp/data")
    if (!dataDir.exists()) {
        dataDir.mkdirs()
    }
    val dbFile = File(dataDir, AppDatabase.DATABASE_NAME)
    return Room.databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath,
    )
}
