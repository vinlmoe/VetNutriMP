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