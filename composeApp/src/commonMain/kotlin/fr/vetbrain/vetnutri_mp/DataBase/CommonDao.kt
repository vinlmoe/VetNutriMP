package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
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

    @Query("SELECT * FROM ALIMENTS WHERE refRation = :rationId")
    suspend fun getAlimentsForRation(rationId: String): List<AlimentRationEntity>

    @Insert suspend fun insertSupplementalVariable(supplementalVariable: SupplementalVariableEntity)

    @Insert suspend fun insertRation(ration: RationEntity)

    @Insert suspend fun insertAlimentRation(aliment: AlimentRationEntity)

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

    @Query("SELECT * FROM FOOD") suspend fun findAll(): List<FoodEntity>

    @Query("SELECT * FROM FOOD WHERE uuid = :id") suspend fun getFoodById(id: String): FoodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertFood(food: FoodEntity)

    @Update suspend fun updateFood(food: FoodEntity)

    @Query("DELETE FROM FOOD WHERE uuid = :uuid") suspend fun deleteFood(uuid: String)

    @Query("SELECT * FROM FOOD WHERE uuid = :uuid") suspend fun getFood(uuid: String): FoodEntity?

    @Query("SELECT * FROM FOOD") suspend fun getAllFoods(): List<FoodEntity>

    @Query("DELETE FROM FOOD") suspend fun deleteAllFoods()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIndications(indications: List<IndicationAlimentEntity>)

    @Query("DELETE FROM INDICATIONS_ALIMENTS WHERE refAliment = :alimentUuid")
    suspend fun deleteIndicationsForAliment(alimentUuid: String)

    @Query("SELECT * FROM INDICATIONS_ALIMENTS WHERE refAliment = :alimentUuid")
    suspend fun getIndicationsForAliment(alimentUuid: String): List<IndicationAlimentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEspeces(especes: List<EspeceAlimentEntity>)

    @Query("SELECT * FROM ESPECES_ALIMENTS WHERE refAliment = :alimentUuid")
    suspend fun getEspecesForAliment(alimentUuid: String): List<EspeceAlimentEntity>

    @Query("DELETE FROM ESPECES_ALIMENTS WHERE refAliment = :alimentUuid")
    suspend fun deleteEspecesForAliment(alimentUuid: String)
}

@Dao
interface NutrientValueDao {
    @Query("SELECT * FROM NUTRIENT_VALUES WHERE refAliment = :alimentUuid")
    suspend fun getNutrientValues(alimentUuid: String): List<NutrientValueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutrientValues(values: List<NutrientValueEntity>)

    @Delete suspend fun deleteNutrientValues(values: List<NutrientValueEntity>)

    @Query("DELETE FROM NUTRIENT_VALUES WHERE refAliment = :alimentUuid")
    suspend fun deleteAllNutrientValuesForAliment(alimentUuid: String)
}

/** DAO pour accéder aux références bibliographiques dans la base de données */
@Dao
interface BiblioRefDao {
    @Query("SELECT * FROM BIBLIO_REFS") suspend fun getAllBiblioRefs(): List<BiblioRefEntity>

    @Query("SELECT * FROM BIBLIO_REFS WHERE uuid = :uuid")
    suspend fun getBiblioRefById(uuid: String): BiblioRefEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBiblioRef(biblioRef: BiblioRefEntity)

    @Update suspend fun updateBiblioRef(biblioRef: BiblioRefEntity)

    @Delete suspend fun deleteBiblioRef(biblioRef: BiblioRefEntity)

    @Query("DELETE FROM BIBLIO_REFS") suspend fun deleteAllBiblioRefs()

    @Query(
            "SELECT * FROM BIBLIO_REFS WHERE firstAuthor LIKE '%' || :query || '%' OR completeRef LIKE '%' || :query || '%' OR comments LIKE '%' || :query || '%'"
    )
    suspend fun searchBiblioRefs(query: String): List<BiblioRefEntity>
}

/** DAO pour accéder aux équations dans la base de données */
@Dao
interface EquationDao {
    @Query("SELECT * FROM EQUATIONS") suspend fun getAllEquations(): List<EquationEntity>

    @Query("SELECT * FROM EQUATIONS WHERE uuid = :uuid")
    suspend fun getEquationById(uuid: String): EquationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquation(equation: EquationEntity)

    @Update suspend fun updateEquation(equation: EquationEntity)

    @Delete suspend fun deleteEquation(equation: EquationEntity)

    @Query("DELETE FROM EQUATIONS") suspend fun deleteAllEquations()

    @Query(
            "SELECT * FROM EQUATIONS WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'"
    )
    suspend fun searchEquations(query: String): List<EquationEntity>
}
