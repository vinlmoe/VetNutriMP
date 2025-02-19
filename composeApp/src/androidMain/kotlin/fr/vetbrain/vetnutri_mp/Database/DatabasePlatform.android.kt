package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.Room
import fr.vetbrain.vetnutri_mp.Localization.AndroidContext

actual fun createDatabase(): AppDatabase {
    return Room.databaseBuilder(
                    AndroidContext.appContext,
                    AndroidRoomDatabase::class.java,
                    AppDatabase.DATABASE_NAME
            )
            .build() as
            AppDatabase
}
