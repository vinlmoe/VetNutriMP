package fr.vetbrain.vetnutri_mp.DataBase

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(ctx: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = ctx.applicationContext
    val dbFile = appContext.getDatabasePath("vetnutri.db")
    return Room.databaseBuilder(appContext, AppDatabase::class.java, dbFile.absolutePath)
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

// Extension function pour configurer l'instance
fun AppDatabase.configureConstructor() {
    AppDatabaseConstructor.setInstance(this)
}
