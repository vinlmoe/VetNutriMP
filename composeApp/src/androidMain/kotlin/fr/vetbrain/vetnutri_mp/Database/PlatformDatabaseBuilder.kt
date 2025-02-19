package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.Room
import fr.vetbrain.vetnutri_mp.Localization.AndroidContext

actual class PlatformDatabaseBuilder : DatabaseBuilder {
    override fun build(): AppDatabase {
        return Room.databaseBuilder(
            AndroidContext.appContext,
            AndroidDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }
} 