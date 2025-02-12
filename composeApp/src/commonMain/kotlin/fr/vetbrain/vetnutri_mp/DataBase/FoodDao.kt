package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    // Opérations CRUD pour FoodEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertFood(food: FoodEntity)

    @Update suspend fun updateFood(food: FoodEntity)

    @Delete suspend fun deleteFood(food: FoodEntity)

    @Query("SELECT * FROM FOOD WHERE UUID = :uuid")
    suspend fun getFoodById(uuid: String): FoodEntity?

    @Query("SELECT * FROM FOOD") fun getAllFood(): Flow<List<FoodEntity>>

    // Opérations pour NameFoodEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodName(foodName: NameFoodEntity)

    @Query("SELECT * FROM NAMEFOOD WHERE refFood = :refFood AND lang = :lang")
    suspend fun getFoodName(refFood: String, lang: String): NameFoodEntity?

    // Opérations pour EspeceEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertEspece(espece: EspeceEntity)

    @Query("SELECT * FROM ESPECE WHERE refFood = :refFood")
    suspend fun getEspecesByFood(refFood: String): List<EspeceEntity>

    // Opérations pour les valeurs nutritionnelles
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertValueAA(value: ValueAAEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertValueBase(value: ValueBaseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertValueLipid(value: ValueLipidEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertValueMacro(value: ValueMacroEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertValueMin(value: ValueMinEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertValueVitam(value: ValueVitamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertValueOther(value: ValueOtherEntity)

    // Requêtes pour récupérer les valeurs nutritionnelles
    @Query("SELECT * FROM VALUEAA WHERE refFood = :refFood AND version = :version")
    suspend fun getAAValues(refFood: String, version: Int): List<ValueAAEntity>

    @Query("SELECT * FROM VALUEBASE WHERE refFood = :refFood AND version = :version")
    suspend fun getBaseValues(refFood: String, version: Int): List<ValueBaseEntity>

    @Query("SELECT * FROM VALUELIPID WHERE refFood = :refFood AND version = :version")
    suspend fun getLipidValues(refFood: String, version: Int): List<ValueLipidEntity>

    @Query("SELECT * FROM VALUEMACRO WHERE refFood = :refFood AND version = :version")
    suspend fun getMacroValues(refFood: String, version: Int): List<ValueMacroEntity>

    @Query("SELECT * FROM VALUEMIN WHERE refFood = :refFood AND version = :version")
    suspend fun getMinValues(refFood: String, version: Int): List<ValueMinEntity>

    @Query("SELECT * FROM VALUEVITAM WHERE refFood = :refFood AND version = :version")
    suspend fun getVitamValues(refFood: String, version: Int): List<ValueVitamEntity>

    @Query("SELECT * FROM VALUEOTHER WHERE refFood = :refFood AND version = :version")
    suspend fun getOtherValues(refFood: String, version: Int): List<ValueOtherEntity>

    // Opérations pour les indications
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIndication(indication: IndicationEntity)

    @Query("SELECT * FROM INDICATION WHERE refFood = :refFood")
    suspend fun getIndications(refFood: String): List<IndicationEntity>

    // Opérations pour DataDef
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataDef(dataDef: DataDefEntity)

    @Query("SELECT * FROM dataDef WHERE UUID = :uuid")
    suspend fun getDataDefById(uuid: String): DataDefEntity?
}
