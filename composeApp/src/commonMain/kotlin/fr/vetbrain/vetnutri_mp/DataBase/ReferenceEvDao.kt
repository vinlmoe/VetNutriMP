package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** DAO pour les opérations CRUD sur les références nutritionnelles. */
@Dao
interface ReferenceEvDao {
        // -------------------- Requêtes pour ReferenceEvEntity --------------------

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertReference(reference: ReferenceEvEntity): Long

        @Update suspend fun updateReference(reference: ReferenceEvEntity)

        @Delete suspend fun deleteReference(reference: ReferenceEvEntity)

        @Query("SELECT * FROM REFERENCE_EV ORDER BY nom ASC")
        fun getAllReferences(): Flow<List<ReferenceEvEntity>>

        @Query("SELECT * FROM REFERENCE_EV WHERE uuid = :referenceId")
        suspend fun getReferenceById(referenceId: String): ReferenceEvEntity?

        @Query("SELECT * FROM REFERENCE_EV WHERE espece = :espece AND stadePhysio = :stade")
        suspend fun getReferencesByEspeceAndStade(
                espece: String,
                stade: String
        ): List<ReferenceEvEntity>

        // -------------------- Requêtes pour NutrientReferenceEntity --------------------

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertNutrientReference(nutrientReference: NutrientReferenceEntity): Long

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertNutrientReferences(nutrientReferences: List<NutrientReferenceEntity>)

        @Update suspend fun updateNutrientReference(nutrientReference: NutrientReferenceEntity)

        @Delete suspend fun deleteNutrientReference(nutrientReference: NutrientReferenceEntity)

        @Query("DELETE FROM NUTRIENT_REFERENCES WHERE referenceId = :referenceId")
        suspend fun deleteAllNutrientReferencesForReference(referenceId: String)

        @Query("SELECT * FROM NUTRIENT_REFERENCES WHERE referenceId = :referenceId")
        suspend fun getNutrientReferencesForReference(
                referenceId: String
        ): List<NutrientReferenceEntity>

        @Query(
                "SELECT * FROM NUTRIENT_REFERENCES WHERE referenceId = :referenceId AND nutrient = :nutrientName AND niveauRef = :niveauRef"
        )
        suspend fun getNutrientReferenceByParams(
                referenceId: String,
                nutrientName: String,
                niveauRef: String
        ): NutrientReferenceEntity?

        // -------------------- Requêtes pour CoefficientEntity --------------------

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertCoefficient(coefficient: CoefficientEntity): Long

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertCoefficients(coefficients: List<CoefficientEntity>)

        @Update suspend fun updateCoefficient(coefficient: CoefficientEntity)

        @Delete suspend fun deleteCoefficient(coefficient: CoefficientEntity)

        @Query("DELETE FROM COEFFICIENTS WHERE referenceId = :referenceId")
        suspend fun deleteAllCoefficientsForReference(referenceId: String)

        @Query("SELECT * FROM COEFFICIENTS WHERE referenceId = :referenceId")
        suspend fun getCoefficientsForReference(referenceId: String): List<CoefficientEntity>

        @Query(
                "SELECT * FROM COEFFICIENTS WHERE referenceId = :referenceId AND groupUUID = :groupId"
        )
        suspend fun getCoefficientsForGroup(
                referenceId: String,
                groupId: Int
        ): List<CoefficientEntity>

        // -------------------- Transactions complexes --------------------

        /**
         * Supprime une référence et toutes ses entités associées (nutriments, coefficients) en une
         * seule transaction
         */
        @Transaction
        suspend fun deleteReferenceWithRelations(referenceId: String) {
                deleteAllNutrientReferencesForReference(referenceId)
                deleteAllCoefficientsForReference(referenceId)
                getReferenceById(referenceId)?.let { deleteReference(it) }
        }
}
