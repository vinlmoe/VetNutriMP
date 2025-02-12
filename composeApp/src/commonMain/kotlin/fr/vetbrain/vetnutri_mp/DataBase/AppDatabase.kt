package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
        entities =
                [
                        // Entités Food
                        FoodEntity::class,
                        NameFoodEntity::class,
                        EspeceEntity::class,
                        ValueAAEntity::class,
                        ValueBaseEntity::class,
                        ValueLipidEntity::class,
                        ValueMacroEntity::class,
                        ValueMinEntity::class,
                        ValueVitamEntity::class,
                        ValueOtherEntity::class,
                        IndicationEntity::class,
                        DataDefEntity::class,

                        // Entités Animal
                        AnimalEntity::class,
                        ConsultationEntity::class,
                        WeightEntity::class,
                        RationEntity::class,
                        SupVarEntity::class,
                        ReferenceDiseaseEntity::class,
                        BreedEntity::class,
                        BreedNameEntity::class,
                        EspecesEntity::class,
                        EspeceNameEntity::class,

                        // Entités Reference
                        EquationEntity::class,
                        SupplementVariableEntity::class,
                        BiblioEntity::class,
                        MethodEntity::class,
                        TargetMethodEntity::class,
                        DataRefEntity::class,
                        CoefEntity::class,
                        SpeReqEqEntity::class],
        version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun animalDao(): AnimalDao
    abstract fun referenceDao(): ReferenceDao

    companion object {
        const val DATABASE_NAME = "vetnutri_database"
    }
}

expect object DatabaseBuilder {
    fun build(): AppDatabase
}
