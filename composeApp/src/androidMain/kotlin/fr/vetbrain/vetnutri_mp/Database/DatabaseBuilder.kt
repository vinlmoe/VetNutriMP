package fr.vetbrain.vetnutri_mp.DataBase

import android.content.Context
import androidx.room.Room
import okio.Path

actual class DatabaseBuilder {
    companion object {
        private lateinit var applicationContext: Context

        fun initializeContext(context: Context) {
            applicationContext = context.applicationContext
        }
    }

    actual fun build(): AppDatabase {
        if (!::applicationContext.isInitialized) {
            throw IllegalStateException("Context n'a pas été initialisé. Appelez initializeContext d'abord.")
        }

        return Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }
}
