package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** DAO pour les opérations CRUD sur les références bibliographiques. */
@Dao
interface BiblioRefDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBiblioRef(biblioRef: BiblioRefEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBiblioRefs(biblioRefs: List<BiblioRefEntity>)

    @Update suspend fun updateBiblioRef(biblioRef: BiblioRefEntity)

    @Delete suspend fun deleteBiblioRef(biblioRef: BiblioRefEntity)

    @Query("SELECT * FROM BIBLIO_REFS ORDER BY firstAuthor ASC")
    fun getAllBiblioRefs(): Flow<List<BiblioRefEntity>>

    @Query("SELECT * FROM BIBLIO_REFS WHERE uuid = :biblioRefId")
    suspend fun getBiblioRefById(biblioRefId: String): BiblioRefEntity?

    /** Récupère les références bibliographiques utilisées par une référence nutritionnelle */
    @Query(
            """
        SELECT DISTINCT br.* FROM BIBLIO_REFS br
        INNER JOIN NUTRIENT_REFERENCES nr ON br.uuid = nr.biblioRefId
        WHERE nr.referenceId = :referenceId
    """
    )
    suspend fun getBiblioRefsForReference(referenceId: String): List<BiblioRefEntity>
}
