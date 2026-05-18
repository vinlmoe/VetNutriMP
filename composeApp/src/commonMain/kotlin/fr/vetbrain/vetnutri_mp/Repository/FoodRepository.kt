package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Data.AlimentEvLight
import kotlinx.coroutines.flow.Flow

/** Résultat de l'importation d'aliments contenant des statistiques détaillées */
data class FoodImportResult(
        val importedCount: Int,
        val updatedCount: Int,
        val deletedCount: Int,
        val errorCount: Int,
        val totalCount: Int,
        val nonResolvedNutrientsCount: Int,
        val nonResolvedNutrients: List<String> = emptyList()
)

/** Interface définissant les opérations de base pour la gestion des aliments. */
interface FoodRepository {
    suspend fun insert(food: AlimentEv)
    suspend fun update(food: AlimentEv)
    suspend fun delete(food: AlimentEv)
    suspend fun getAllFoods(): List<AlimentEv>
    suspend fun getFoodById(id: String): AlimentEv?
    fun observeAllFoods(): Flow<List<AlimentEv>>

    /**
     * Récupère une liste légère de tous les aliments sans les valeurs nutritionnelles. Cette
     * méthode est optimisée pour les performances lorsque seules les informations de base des
     * aliments sont nécessaires.
     *
     * @return Une liste d'objets AlimentEvLight contenant les informations de base des aliments
     */
    suspend fun getAllFoodsLight(): List<AlimentEvLight>

    /**
     * Importe une liste d'aliments et les insère dans la base de données
     * @param foods Liste des aliments à importer
     * @return Résultat détaillé de l'importation
     */
    suspend fun importFoods(
            foods: List<AlimentEvJson>,
            mergeNutrients: Boolean = false,
            importOnlyIfNewer: Boolean = false
    ): FoodImportResult

    /**
     * Insère un aliment avec ses propriétés associées (espèces, indications, valeurs de nutriments)
     * @param food Aliment à insérer
     */
    suspend fun insertFood(food: AlimentEv)

    /**
     * Récupère un aliment avec toutes ses propriétés associées par UUID
     * @param uuid UUID de l'aliment à récupérer
     * @return L'aliment complet ou null si non trouvé
     */
    suspend fun getFood(uuid: String): AlimentEv?

    /**
     * Supprime un aliment et toutes ses propriétés associées
     * @param uuid UUID de l'aliment à supprimer
     */
    suspend fun deleteFood(uuid: String)

    /**
     * Met à jour un aliment et toutes ses propriétés associées
     * @param food Aliment à mettre à jour
     */
    suspend fun updateFood(food: AlimentEv)

    /**
     * Supprime tous les aliments de la base de données
     * @return Le nombre d'aliments supprimés
     */
    suspend fun clearAllFoods(): Int

    /** Retourne le nombre d'aliments sans charger leur contenu. */
    suspend fun getFoodsCount(): Int

    /** Retourne les labels de nutriments distincts présents dans la base (sans charger les valeurs). */
    suspend fun getDistinctNutrientLabels(): List<String>
}
