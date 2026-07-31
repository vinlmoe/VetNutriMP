package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

fun getDatabasePath(): String {
    val testDatabaseDir = System.getenv("VETNUTRI_TEST_DATABASE_DIR")
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
    val dataDir = if (testDatabaseDir != null) {
        File(testDatabaseDir)
    } else {
        val userHome = System.getProperty("user.home")
        File(userHome, ".vetnutri_mp/data")
    }
    if (!dataDir.exists()) dataDir.mkdirs()
    return File(dataDir, AppDatabase.DATABASE_NAME).absolutePath
}

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    return Room.databaseBuilder<AppDatabase>(name = getDatabasePath())
}
