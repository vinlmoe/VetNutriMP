 ackage fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AlimentRationDao {
    @Insert suspend fun insert(alimentRation: AlimentRationEntity)

    @Update suspend fun update(alimentRation: AlimentRationEntity)

    @Delete suspend fun delete(alimentRation: AlimentRationEntity)

    @Query("SELECT * FROM aliment_ration WHERE refRation = :rationId")
    suspend fun getAlimentRationsForRation(rationId: String): List<AlimentRationEntity>

    @Query("SELECT * FROM aliment_ration WHERE uuid = :id")
    suspend fun getAlimentRationById(id: String): AlimentRationEntity?

    @Query("SELECT * FROM aliment_ration WHERE refAlimUnif = :alimentId")
    suspend fun getAlimentRationsForAliment(alimentId: String): List<AlimentRationEntity>
}

@Dao
interface AlimentDao {
    @Insert suspend fun insert(aliment: AlimentEntity)

    @Update suspend fun update(aliment: AlimentEntity)

    @Delete suspend fun delete(aliment: AlimentEntity)

    @Query("SELECT * FROM aliment") suspend fun getAllAliments(): List<AlimentEntity>

    @Query("SELECT * FROM aliment WHERE uuid = :id")
    suspend fun getAlimentById(id: String): AlimentEntity?

    @Query("SELECT * FROM aliment WHERE type_aliment = :type")
    suspend fun getAlimentsByType(type: String): List<AlimentEntity>
}

@Dao
interface NutrimentDao {
    @Insert suspend fun insert(nutriment: NutrimentEntity)

    @Update suspend fun update(nutriment: NutrimentEntity)

    @Delete suspend fun delete(nutriment: NutrimentEntity)

    @Query("SELECT * FROM nutriment") suspend fun getAllNutriments(): List<NutrimentEntity>

    @Query("SELECT * FROM nutriment WHERE uuid = :id")
    suspend fun getNutrimentById(id: String): NutrimentEntity?
}

@Dao
interface AlimentNutrimentDao {
    @Insert suspend fun insert(alimentNutriment: AlimentNutrimentEntity)

    @Update suspend fun update(alimentNutriment: AlimentNutrimentEntity)

    @Delete suspend fun delete(alimentNutriment: AlimentNutrimentEntity)

    @Query("SELECT * FROM aliment_nutriment WHERE ref_aliment = :alimentId")
    suspend fun getNutrimentsForAliment(alimentId: String): List<AlimentNutrimentEntity>

    @Query("SELECT * FROM aliment_nutriment WHERE ref_nutriment = :nutrimentId")
    suspend fun getAlimentsForNutriment(nutrimentId: String): List<AlimentNutrimentEntity>
}
