package fr.vetbrain.vetnutri_mp.Database

import androidx.room.*
import fr.vetbrain.vetnutri_mp.Data.*

@Dao
interface AnimalDao {
    @Query("SELECT * FROM animals") suspend fun getAllAnimals(): List<AnimalEv>

    @Query("SELECT * FROM animals WHERE uuid = :id")
    suspend fun getAnimalById(id: String): AnimalEv?

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAnimal(animal: AnimalEv)

    @Update suspend fun updateAnimal(animal: AnimalEv)

    @Delete suspend fun deleteAnimal(animal: AnimalEv)
}

@Dao
interface ConsultationDao {
    @Query("SELECT * FROM consultations") suspend fun getAllConsultations(): List<ConsultationEv>

    @Query("SELECT * FROM consultations WHERE uuid = :id")
    suspend fun getConsultationById(id: String): ConsultationEv?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsultation(consultation: ConsultationEv)

    @Update suspend fun updateConsultation(consultation: ConsultationEv)

    @Delete suspend fun deleteConsultation(consultation: ConsultationEv)
}

@Dao
interface RationDao {
    @Query("SELECT * FROM rations") suspend fun getAllRations(): List<Ration>

    @Query("SELECT * FROM rations WHERE uuid = :id") suspend fun getRationById(id: String): Ration?

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertRation(ration: Ration)

    @Update suspend fun updateRation(ration: Ration)

    @Delete suspend fun deleteRation(ration: Ration)
}
