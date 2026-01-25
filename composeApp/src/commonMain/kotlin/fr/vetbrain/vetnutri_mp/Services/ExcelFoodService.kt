package fr.vetbrain.vetnutri_mp.Services

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Data.AlimentExcelRow
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Repository.FoodImportResult
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.ExcelPlatform.*
import kotlinx.coroutines.withContext

/**
 * Service intégré pour l'import/export Excel des aliments
 * Utilise le FoodRepository pour la persistance des données
 */
class ExcelFoodService(
    private val foodRepository: FoodRepository
) {
    private val csvService = AlimentExcelService()

    /**
     * Exporte tous les aliments vers un fichier CSV
     */
    suspend fun exportAllFoodsToCsv(): String = withContext(AppDispatchers.IO) {
        val aliments = foodRepository.getAllFoods()
        csvService.exportToCsv(aliments)
    }

    /**
     * Exporte une sélection d'aliments vers un fichier CSV
     */
    suspend fun exportSelectedFoodsToCsv(foodIds: Set<String>): String = withContext(AppDispatchers.IO) {
        val allFoods = foodRepository.getAllFoods()
        val selectedFoods = allFoods.filter { it.uuid in foodIds }
        csvService.exportToCsv(selectedFoods)
    }

    /**
     * Importe des aliments depuis un fichier CSV
     * @param csvContent Le contenu du fichier CSV
     * @param dataB La base de données à utiliser (prioritaire sur celle du CSV, null pour utiliser celle du CSV)
     */
    suspend fun importFoodsFromCsv(csvContent: String, dataB: String? = null): ExcelImportResult = withContext(AppDispatchers.IO) {
        
        val parseResult = csvService.importFromCsv(csvContent, dataB)
        
        if (parseResult.aliments.isEmpty()) {
            return@withContext ExcelImportResult(
                success = false,
                message = "Aucun aliment valide trouvé dans le fichier CSV",
                importedCount = 0,
                errorCount = parseResult.errors.size,
                errors = parseResult.errors
            )
        }

        try {
            
            // Utiliser le repository pour l'import avec persistance complète des nutriments
            val importResult = if (foodRepository is fr.vetbrain.vetnutri_mp.Repository.DatabaseFoodRepository) {
                // Utiliser importFoodsDomain pour la persistance complète (aliments + nutriments)
                val result = foodRepository.importFoodsDomain(parseResult.aliments)
                result
            } else {
                // Fallback pour les autres implémentations
                val alimentsJson = parseResult.aliments.map { aliment ->
                    aliment.toAlimentEvJson()
                }
                val result = foodRepository.importFoods(alimentsJson)
                result
            }

            val finalResult = ExcelImportResult(
                success = true,
                message = "Import réussi: ${importResult.importedCount} aliments importés, ${importResult.updatedCount} mis à jour",
                importedCount = importResult.importedCount,
                updatedCount = importResult.updatedCount,
                errorCount = importResult.errorCount + parseResult.errors.size,
                errors = parseResult.errors + importResult.nonResolvedNutrients
            )
            
            
            return@withContext finalResult
            
        } catch (e: Exception) {
            
            val errorResult = ExcelImportResult(
                success = false,
                message = "Erreur lors de l'import: ${e.message}",
                importedCount = 0,
                errorCount = parseResult.errors.size + 1,
                errors = parseResult.errors + listOf("Erreur d'import: ${e.message}")
            )
            
            return@withContext errorResult
        }
    }

    /**
     * Prépare un import CSV pour prévisualisation (sans sauvegarder)
     * @param csvContent Le contenu du fichier CSV
     * @param dataB La base de données à utiliser (prioritaire sur celle du CSV, null pour utiliser celle du CSV)
     */
    suspend fun previewCsvImport(csvContent: String, dataB: String? = null): ExcelImportResult = withContext(AppDispatchers.IO) {
        val parseResult = csvService.importFromCsv(csvContent, dataB)
        
        ExcelImportResult(
            success = parseResult.aliments.isNotEmpty(),
            message = if (parseResult.aliments.isNotEmpty()) 
                "Prévisualisation: ${parseResult.aliments.size} aliments valides trouvés" 
            else "Aucun aliment valide trouvé",
            importedCount = parseResult.aliments.size,
            errorCount = parseResult.errors.size,
            errors = parseResult.errors,
            previewMode = true
        )
    }

    /**
     * Génère un fichier CSV d'exemple avec la structure complète
     */
    fun generateExampleCsv(): String {
        return AlimentExcelService.generateExampleCsv()
    }

    /**
     * Résultat d'import Excel
     */
    data class ExcelImportResult(
        val success: Boolean,
        val message: String,
        val importedCount: Int = 0,
        val updatedCount: Int = 0,
        val errorCount: Int = 0,
        val errors: List<String> = emptyList(),
        val previewMode: Boolean = false
    )
}

/**
 * Extension pour convertir AlimentEv en AlimentEvJson
 */
private fun AlimentEv.toAlimentEvJson(): fr.vetbrain.vetnutri_mp.Data.AlimentEvJson {
    return fr.vetbrain.vetnutri_mp.Data.AlimentEvJson(
        UUID = uuid,
        nom = nom ?: "",
        group = group?.name ?: "",
        foodKind = typeAliment?.name ?: "",
        ingredients = ingredients ?: "",
        prix = price ?: 0.0,
        categoriePrix = categPrice ?: "i",
        marque = brand ?: "",
        indication = indicat.map { it.name },
        espece = 0, // TODO: Déterminer l'espèce principale
        Especes = especes,
        gamme = gamme ?: "",
        dateMaj = lastUpdateDate ?: "",
        imageRef = imageRef ?: "",
        presentation = cont?.name ?: "",
        quantInt = quantInt ?: 0.0,
        cont = cont?.name ?: "NO",
        deprecated = deprecated,
        DataB = dataB ?: "6",
        valMap = valMap.mapKeys { it.key.label }.mapValues { (_, quantity) ->
            fr.vetbrain.vetnutri_mp.Data.NutrientQuantity(
                value = quantity.value,
                nut = quantity.unit
            )
        }
    )
}
