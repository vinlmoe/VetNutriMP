package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.Room
import java.io.File

actual object DatabaseBuilder {
    actual fun build(): AppDatabase {
        val databasePath =
                File(System.getProperty("user.home"), ".vetnutri/${AppDatabase.DATABASE_NAME}")
        databasePath.parentFile?.mkdirs()

        return Room.databaseBuilder(AppDatabase::class.java, databasePath.absolutePath).build()
    }
}
