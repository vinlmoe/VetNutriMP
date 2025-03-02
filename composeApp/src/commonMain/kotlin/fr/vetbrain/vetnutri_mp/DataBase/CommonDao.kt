package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AnimalDao {
    @Insert suspend fun insert(animal: AnimalEntity)

    @Update suspend fun update(animal: AnimalEntity)

    @Delete suspend fun delete(animal: AnimalEntity)

    @Query("SELECT * FROM animals") suspend fun getAllAnimals(): List<AnimalEntity>

    @Query("SELECT * FROM animals WHERE uuid = :id")
    suspend fun getAnimalById(id: String): AnimalEntity?

    @Insert suspend fun insertWeight(weight: WeightEntity)

    @Insert suspend fun insertConsultation(consultation: ConsultationEntity)

    @Insert suspend fun insertRation(ration: RationEntity)

    @Insert suspend fun insertAlimentRation(aliment: AlimentRationEntity)

    @Insert suspend fun insertSupplementalVariable(supplementalVariable: SupplementalVariableEntity)

    @Query("SELECT * FROM WEIGHT WHERE refAnimal = :animalId")
    suspend fun getWeightsForAnimal(animalId: String): List<WeightEntity>

    @Query("DELETE FROM WEIGHT WHERE refAnimal = :animalId")
    suspend fun deleteWeightsForAnimal(animalId: String)

    @Query("SELECT * FROM CONSULTATIONS WHERE idAnim = :animalId")
    suspend fun getConsultationsForAnimal(animalId: String): List<ConsultationEntity>

    @Delete suspend fun deleteConsultation(consultation: ConsultationEntity)

    @Query("DELETE FROM SUPPLEMENTAL_VARIABLES WHERE idConsult = :consultationId")
    suspend fun deleteSupplementalVariablesForConsultation(consultationId: String)

    @Query("DELETE FROM RATIONS WHERE idConsult = :consultationId")
    suspend fun deleteRationsForConsultation(consultationId: String)

    @Query("SELECT * FROM ALIMENTS WHERE refRation = :rationId")
    suspend fun getAlimentRationsForRation(rationId: String): List<AlimentRationEntity>
}

@Dao
interface ConsultationDao {
    @Insert suspend fun insert(consultation: ConsultationEntity)

    @Update suspend fun update(consultation: ConsultationEntity)

    @Delete suspend fun delete(consultation: ConsultationEntity)

    @Query("SELECT * FROM CONSULTATIONS WHERE idAnim = :animalId")
    suspend fun getConsultationsForAnimal(animalId: String): List<ConsultationEntity>

    @Query("SELECT * FROM CONSULTATIONS WHERE uuid = :id")
    suspend fun getConsultationById(id: String): ConsultationEntity?

    @Query("SELECT * FROM SUPPLEMENTAL_VARIABLES WHERE idConsult = :consultationId")
    suspend fun getSupplementalVariablesForConsultation(
            consultationId: String
    ): List<SupplementalVariableEntity>

    @Query("SELECT * FROM RATIONS WHERE idConsult = :consultationId")
    suspend fun getRationsForConsultation(consultationId: String): List<RationEntity>

    @Insert suspend fun insertSupplementalVariable(supplementalVariable: SupplementalVariableEntity)

    @Insert suspend fun insertRation(ration: RationEntity)

    @Query("DELETE FROM RATIONS WHERE idConsult = :consultationId")
    suspend fun deleteRationsForConsultation(consultationId: String)

    @Query("DELETE FROM SUPPLEMENTAL_VARIABLES WHERE idConsult = :consultationId")
    suspend fun deleteSupplementalVariablesForConsultation(consultationId: String)
}

@Dao
interface FoodDao {
    @Insert suspend fun insert(food: FoodEntity)

    @Update suspend fun update(food: FoodEntity)

    @Delete suspend fun delete(food: FoodEntity)

    @Query("SELECT * FROM FOOD") suspend fun getAllFoods(): List<FoodEntity>

    @Query("SELECT * FROM FOOD WHERE UUID = :id") suspend fun getFoodById(id: String): FoodEntity?
}
