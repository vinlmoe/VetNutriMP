package fr.vetbrain.vetnutri_mp.DataBase

import android.content.Context
import androidx.room.Room

actual object DatabaseBuilder {
    private lateinit var applicationContext: Context

    fun initializeContext(context: Context) {
        applicationContext = context.applicationContext
    }

    actual fun build(): AppDatabase {
        if (!::applicationContext.isInitialized) {
            throw IllegalStateException(
                    "Context n'a pas été initialisé. Appelez initializeContext d'abord."
            )
        }

        return Room.databaseBuilder(
                        context = applicationContext,
                        klass = AppDatabase::class.java,
                        name = AppDatabase.DATABASE_NAME
                )
                .build()
    }
}
