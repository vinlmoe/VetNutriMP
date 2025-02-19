package fr.vetbrain.vetnutri_mp.DataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.vetbrain.vetnutri_mp.Model.Animal
import fr.vetbrain.vetnutri_mp.Model.Food

@Database(entities = [Animal::class, Food::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AndroidDatabase : RoomDatabase(), AppDatabase {
        abstract override fun animalDao(): CommonAnimalDao
        abstract override fun foodDao(): CommonFoodDao

        override fun clearAllTables() {
                // Room implémente automatiquement cette méthode
        }

        companion object {
                fun create(context: Context): AndroidDatabase {
                        return Room.databaseBuilder(
                                        context,
                                        AndroidDatabase::class.java,
                                        DatabaseFactory.DATABASE_NAME
                                )
                                .build()
                }
        }
}

class AndroidDatabaseImpl(private val db: AndroidDatabase) : DatabaseInterface {
        override suspend fun getAllAnimals(): List<AnimalEv> = db.animalDao().getAllAnimals()
        override suspend fun getAnimalById(id: String): AnimalEv? = db.animalDao().getAnimalById(id)
        override suspend fun insertAnimal(animal: AnimalEv) = db.animalDao().insertAnimal(animal)
        override suspend fun updateAnimal(animal: AnimalEv) = db.animalDao().updateAnimal(animal)
        override suspend fun deleteAnimal(animal: AnimalEv) = db.animalDao().deleteAnimal(animal)

        override suspend fun getAllConsultations(): List<ConsultationEv> =
                db.consultationDao().getAllConsultations()
        override suspend fun getConsultationById(id: String): ConsultationEv? =
                db.consultationDao().getConsultationById(id)
        override suspend fun insertConsultation(consultation: ConsultationEv) =
                db.consultationDao().insertConsultation(consultation)
        override suspend fun updateConsultation(consultation: ConsultationEv) =
                db.consultationDao().updateConsultation(consultation)
        override suspend fun deleteConsultation(consultation: ConsultationEv) =
                db.consultationDao().deleteConsultation(consultation)

        override suspend fun getAllRations(): List<Ration> = db.rationDao().getAllRations()
        override suspend fun getRationById(id: String): Ration? = db.rationDao().getRationById(id)
        override suspend fun insertRation(ration: Ration) = db.rationDao().insertRation(ration)
        override suspend fun updateRation(ration: Ration) = db.rationDao().updateRation(ration)
        override suspend fun deleteRation(ration: Ration) = db.rationDao().deleteRation(ration)
}
