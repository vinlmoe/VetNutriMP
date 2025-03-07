package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.*
import kotlinx.coroutines.flow.Flow
/*
@Dao
interface ReferenceDao {
    // Opérations pour EquationEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquation(equation: EquationEntity)

    @Update suspend fun updateEquation(equation: EquationEntity)

    @Delete suspend fun deleteEquation(equation: EquationEntity)

    @Query("SELECT * FROM equation WHERE UUID = :uuid")
    suspend fun getEquationById(uuid: String): EquationEntity?

    @Query("SELECT * FROM equation") fun getAllEquations(): Flow<List<EquationEntity>>

    // Opérations pour SupplementVariableEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplementVariable(supplementVariable: SupplementVariableEntity)

    @Query("SELECT * FROM SupplementVariable WHERE refEquation = :equationId")
    suspend fun getSupplementVariablesForEquation(
            equationId: String
    ): List<SupplementVariableEntity>

    // Opérations pour BiblioEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertBiblio(biblio: BiblioEntity)

    @Update suspend fun updateBiblio(biblio: BiblioEntity)

    @Delete suspend fun deleteBiblio(biblio: BiblioEntity)

    @Query("SELECT * FROM Biblio WHERE UUID = :uuid")
    suspend fun getBiblioById(uuid: String): BiblioEntity?

    @Query("SELECT * FROM Biblio") fun getAllBiblio(): Flow<List<BiblioEntity>>

    // Opérations pour MethodEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertMethod(method: MethodEntity)

    @Update suspend fun updateMethod(method: MethodEntity)

    @Delete suspend fun deleteMethod(method: MethodEntity)

    @Query("SELECT * FROM method WHERE UUID = :uuid")
    suspend fun getMethodById(uuid: String): MethodEntity?

    @Query("SELECT * FROM method") fun getAllMethods(): Flow<List<MethodEntity>>

    // Opérations pour TargetMethodEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTargetMethod(targetMethod: TargetMethodEntity)

    @Query("SELECT * FROM targetMethod WHERE refMethod = :methodId ORDER BY ord")
    suspend fun getTargetMethodsForMethod(methodId: String): List<TargetMethodEntity>

    // Opérations pour DataRefEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataRef(dataRef: DataRefEntity)

    @Update suspend fun updateDataRef(dataRef: DataRefEntity)

    @Delete suspend fun deleteDataRef(dataRef: DataRefEntity)

    @Query("SELECT * FROM dataRef WHERE UUID = :uuid")
    suspend fun getDataRefById(uuid: String): DataRefEntity?

    @Query("SELECT * FROM dataRef") fun getAllDataRefs(): Flow<List<DataRefEntity>>

    // Opérations pour CoefEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertCoef(coef: CoefEntity)

    @Query("SELECT * FROM coef WHERE refRef = :refId")
    suspend fun getCoefsForRef(refId: String): List<CoefEntity>

    // Opérations pour SpeReqEqEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeReqEq(speReqEq: SpeReqEqEntity)

    @Query("SELECT * FROM speReqEq WHERE refRef = :refId")
    suspend fun getSpeReqEqsForRef(refId: String): List<SpeReqEqEntity>

    @Query("SELECT * FROM speReqEq WHERE refEq = :equationId")
    suspend fun getSpeReqEqsForEquation(equationId: String): List<SpeReqEqEntity>
}*/
