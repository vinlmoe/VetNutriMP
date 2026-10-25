package fr.vetbrain.vetnutri_mp.DataBase

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabasePath(ctx: Context): String =
    ctx.applicationContext.getDatabasePath(AppDatabase.DATABASE_NAME).absolutePath

fun getDatabaseBuilder(ctx: Context): RoomDatabase.Builder<AppDatabase> {
    return Room.databaseBuilder(
        ctx.applicationContext,
        AppDatabase::class.java,
        getDatabasePath(ctx)
    )
}

/**
 * Instance unique de [AppDatabase] pour tout le processus Android.
 *
 * Évite de recréer (et de migrer) la base à chaque recréation de l'Activity (rotation, thème…)
 * sans jamais fermer l'instance précédente. [getOrCreate] est bloquant (ouverture + migrations) :
 * l'appeler hors du thread UI, par exemple depuis `Dispatchers.IO`.
 */
object AndroidDatabaseHolder {
    @Volatile
    private var instance: AppDatabase? = null

    /** Instance déjà ouverte, ou null si la base n'a pas encore été créée. */
    val current: AppDatabase?
        get() = instance

    /** Retourne l'instance existante ou crée (et ouvre) la base, une seule fois par processus. */
    fun getOrCreate(context: Context): AppDatabase {
        instance?.let { return it }
        synchronized(this) {
            instance?.let { return it }
            val database = getRoomDatabase(getDatabaseBuilder(context), getDatabasePath(context))
            instance = database
            return database
        }
    }
}
