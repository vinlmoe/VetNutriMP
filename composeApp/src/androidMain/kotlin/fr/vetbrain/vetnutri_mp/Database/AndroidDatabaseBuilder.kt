package fr.vetbrain.vetnutri_mp.DataBase

import android.content.Context
import androidx.room.Room

class AndroidDatabaseBuilder(private val context: Context) : DatabaseBuilder {
    override fun build(): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
                .build()
    }
}
