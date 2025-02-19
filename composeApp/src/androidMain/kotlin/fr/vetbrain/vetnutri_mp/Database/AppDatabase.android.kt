package fr.vetbrain.vetnutri_mp.DataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.vetbrain.vetnutri_mp.Localization.AndroidContext

@Database(
    entities = [
        AnimalEntity::class,
        ConsultationEntity::class,
        WeightEntity::class,
        RationEntity::class,
        AlimentRationEntity::class,
        AlimentEntity::class,
        EspeceAlimentEntity::class,
        IndicationAlimentEntity::class,
        SupplementalVariableEntity::class,
        FoodEntity::class,
        AlimentReferenceEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AndroidRoomDatabase : RoomDatabase() {
    abstract fun animalDao(): AnimalDao
    abstract fun foodDao(): FoodDao
}

actual fun createDatabase(): AppDatabase {
    return Room.databaseBuilder(
        AndroidContext.appContext,
        AndroidRoomDatabase::class.java,
        AppDatabase.DATABASE_NAME
    ).build() as AppDatabase
}
