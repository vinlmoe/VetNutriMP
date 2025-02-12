package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimalDao {
    // Opérations CRUD pour AnimalEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAnimal(animal: AnimalEntity)

    @Update suspend fun updateAnimal(animal: AnimalEntity)

    @Delete suspend fun deleteAnimal(animal: AnimalEntity)

    @Query("SELECT * FROM ANIMALS WHERE UUID = :uuid")
    suspend fun getAnimalById(uuid: String): AnimalEntity?

    @Query("SELECT * FROM ANIMALS") fun getAllAnimals(): Flow<List<AnimalEntity>>

    // Opérations pour ConsultationEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsultation(consultation: ConsultationEntity)

    @Update suspend fun updateConsultation(consultation: ConsultationEntity)

    @Delete suspend fun deleteConsultation(consultation: ConsultationEntity)

    @Query("SELECT * FROM CONSULTATIONS WHERE UUID = :uuid")
    suspend fun getConsultationById(uuid: String): ConsultationEntity?

    @Query("SELECT * FROM CONSULTATIONS WHERE idAnim = :animalId")
    suspend fun getConsultationsForAnimal(animalId: String): List<ConsultationEntity>

    // Opérations pour WeightEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertWeight(weight: WeightEntity)

    @Update suspend fun updateWeight(weight: WeightEntity)

    @Delete suspend fun deleteWeight(weight: WeightEntity)

    @Query("SELECT * FROM Weight WHERE UUID = :uuid")
    suspend fun getWeightById(uuid: String): WeightEntity?

    @Query("SELECT * FROM Weight WHERE refAnimal = :animalId ORDER BY date DESC")
    suspend fun getWeightsForAnimal(animalId: String): List<WeightEntity>

    // Opérations pour RationEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertRation(ration: RationEntity)

    @Update suspend fun updateRation(ration: RationEntity)

    @Delete suspend fun deleteRation(ration: RationEntity)

    @Query("SELECT * FROM RATION WHERE UUID = :uuid")
    suspend fun getRationById(uuid: String): RationEntity?

    @Query("SELECT * FROM RATION WHERE idConsult = :consultationId")
    suspend fun getRationsForConsultation(consultationId: String): List<RationEntity>

    // Opérations pour SupVarEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertSupVar(supVar: SupVarEntity)

    @Query("SELECT * FROM SupVar WHERE idCons = :consultationId")
    suspend fun getSupVarsForConsultation(consultationId: String): List<SupVarEntity>

    // Opérations pour ReferenceDisease
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferenceDisease(referenceDisease: ReferenceDiseaseEntity)

    @Query("SELECT * FROM ReferenceDisease WHERE idCons = :consultationId")
    suspend fun getReferenceDiseaseForConsultation(
            consultationId: String
    ): List<ReferenceDiseaseEntity>

    // Opérations pour Breed
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertBreed(breed: BreedEntity)

    @Query("SELECT * FROM Breed WHERE ID = :id") suspend fun getBreedById(id: String): BreedEntity?

    // Opérations pour BreedName
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreedName(breedName: BreedNameEntity)

    @Query("SELECT * FROM breedName WHERE refBreed = :breedId AND lang = :lang")
    suspend fun getBreedName(breedId: String, lang: String): BreedNameEntity?

    // Opérations pour Especes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEspeces(especes: EspecesEntity)

    @Query("SELECT * FROM Especes WHERE ID = :id")
    suspend fun getEspecesById(id: String): EspecesEntity?

    // Opérations pour EspeceName
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEspeceName(especeName: EspeceNameEntity)

    @Query("SELECT * FROM EspeceName WHERE ref_espece = :id AND lang = :lang")
    suspend fun getEspeceName(id: String, lang: String): EspeceNameEntity?
}
