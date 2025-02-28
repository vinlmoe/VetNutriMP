package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Base de données Room pour KMP. Cette classe définit la structure de la base de données et ses
 * DAOs. Elle est utilisée à la fois sur Android et iOS.
 */
@Database(
<<<<<<< HEAD
        entities =
                [
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
                        AlimentReferenceEntity::class,
                        NutrientValueEntity::class],
        version = 2,
=======
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
            AlimentReferenceEntity::class],
        version = 1,
>>>>>>> parent of f5d4378 (Houra cela marche reste des petit truc et es fonctionalité)
        exportSchema = true
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun animalDao(): AnimalDao
<<<<<<< HEAD
    abstract fun consultationDao(): ConsultationDao
    abstract fun rationDao(): RationDao
    abstract fun alimentBaseDao(): AlimentBaseDao
    abstract fun nutrientValueDao(): NutrientValueDao
=======

>>>>>>> parent of f5d4378 (Houra cela marche reste des petit truc et es fonctionalité)

    companion object {
        const val DATABASE_NAME = "vetnutri.db"
    }
}


/**
 * Fonction de création de la base de données. L'implémentation spécifique à la plateforme est
 * fournie dans les modules Android et iOS.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}


fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
<<<<<<< HEAD
            // .addMigrations(MIGRATIONS)
            /*.fallbackToDestructiveMigrationOnDowngrade(
                dropAllTables = true
            )*/
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
}
}
=======
       // .addMigrations(MIGRATIONS)
        /*.fallbackToDestructiveMigrationOnDowngrade(
            dropAllTables = true
        )*/
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
>>>>>>> parent of f5d4378 (Houra cela marche reste des petit truc et es fonctionalité)
