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

@Dao
interface RationDao {
    @Insert suspend fun insert(ration: RationEntity)

    @Update suspend fun update(ration: RationEntity)

    @Delete suspend fun delete(ration: RationEntity)

    @Query("SELECT * FROM RATIONS WHERE idConsult = :consultationId")
    suspend fun getRationsForConsultation(consultationId: String): List<RationEntity>

    @Query("SELECT * FROM RATIONS WHERE uuid = :id")
    suspend fun getRationById(id: String): RationEntity?

    @Query("SELECT * FROM RATIONS") suspend fun getAllRations(): List<RationEntity>
}

@Dao
interface AlimentBaseDao {
    @Insert suspend fun insert(aliment: AlimentEntity)

    @Update suspend fun update(aliment: AlimentEntity)

    @Delete suspend fun delete(aliment: AlimentEntity)

    @Query("SELECT * FROM ALIMENTS_BASE") suspend fun getAllAliments(): List<AlimentEntity>

    @Query("SELECT * FROM ALIMENTS_BASE WHERE uuid = :id")
    suspend fun getAlimentById(id: String): AlimentEntity?

    @Query("SELECT * FROM ALIMENTS_BASE WHERE typeAliment = :typeAliment")
    suspend fun getAlimentsByType(typeAliment: Int): List<AlimentEntity>

    @Query("SELECT * FROM ALIMENTS_BASE WHERE group = :groupId")
    suspend fun getAlimentsByGroup(groupId: Int): List<AlimentEntity>

    @Query("SELECT * FROM ALIMENTS_BASE WHERE deprecated = 0")
    suspend fun getNonDeprecatedAliments(): List<AlimentEntity>

    @Insert suspend fun insertEspeceAliment(especeAliment: EspeceAlimentEntity)

    @Insert suspend fun insertIndicationAliment(indicationAliment: IndicationAlimentEntity)

    @Query("SELECT * FROM ESPECES_ALIMENTS WHERE refAliment = :alimentId")
    suspend fun getEspecesForAliment(alimentId: String): List<EspeceAlimentEntity>

    @Query("SELECT * FROM INDICATIONS_ALIMENTS WHERE refAliment = :alimentId")
    suspend fun getIndicationsForAliment(alimentId: String): List<IndicationAlimentEntity>

    @Query("DELETE FROM ESPECES_ALIMENTS WHERE refAliment = :alimentId")
    suspend fun deleteEspecesForAliment(alimentId: String)

    @Query("DELETE FROM INDICATIONS_ALIMENTS WHERE refAliment = :alimentId")
    suspend fun deleteIndicationsForAliment(alimentId: String)
}

@Dao
interface NutrientValueDao {
    @Insert suspend fun insert(nutrientValue: NutrientValueEntity)

    @Insert suspend fun insertAll(nutrientValues: List<NutrientValueEntity>)

    @Update suspend fun update(nutrientValue: NutrientValueEntity)

    @Delete suspend fun delete(nutrientValue: NutrientValueEntity)

    @Query("SELECT * FROM NUTRIENT_VALUES WHERE alimentId = :alimentId")
    suspend fun getNutrientValuesForAliment(alimentId: String): List<NutrientValueEntity>

    @Query(
            "SELECT * FROM NUTRIENT_VALUES WHERE alimentId = :alimentId AND nutrientType = :nutrientType"
    )
    suspend fun getNutrientValuesForAlimentByType(
            alimentId: String,
            nutrientType: Int
    ): List<NutrientValueEntity>

    @Query("DELETE FROM NUTRIENT_VALUES WHERE alimentId = :alimentId")
    suspend fun deleteNutrientValuesForAliment(alimentId: String)
}

@Dao
interface AlimentRationDao {
    @Insert suspend fun insert(alimentRation: AlimentRationEntity)

    @Update suspend fun update(alimentRation: AlimentRationEntity)

    @Delete suspend fun delete(alimentRation: AlimentRationEntity)

    @Query("SELECT * FROM ALIMENTS WHERE refRation = :rationId")
    suspend fun getAlimentRationsForRation(rationId: String): List<AlimentRationEntity>

    @Query("SELECT * FROM ALIMENTS WHERE uuid = :id")
    suspend fun getAlimentRationById(id: String): AlimentRationEntity?
}
