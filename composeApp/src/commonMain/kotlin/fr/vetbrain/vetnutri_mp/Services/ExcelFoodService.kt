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
     */
    suspend fun importFoodsFromCsv(csvContent: String): ExcelImportResult = withContext(AppDispatchers.IO) {
        println("[EXCEL-SERVICE-INFO] === DÉBUT IMPORT EXCEL SERVICE ===")
        
        val parseResult = csvService.importFromCsv(csvContent)
        println("[EXCEL-SERVICE-INFO] ParseResult: ${parseResult.aliments.size} aliments, ${parseResult.errors.size} erreurs")
        
        if (parseResult.aliments.isEmpty()) {
            println("[EXCEL-SERVICE-ERROR] Aucun aliment valide trouvé dans le fichier CSV")
            return@withContext ExcelImportResult(
                success = false,
                message = "Aucun aliment valide trouvé dans le fichier CSV",
                importedCount = 0,
                errorCount = parseResult.errors.size,
                errors = parseResult.errors
            )
        }

        try {
            println("[EXCEL-SERVICE-INFO] Début de l'import dans le repository...")
            println("[EXCEL-SERVICE-INFO] Type de repository: ${foodRepository::class.simpleName}")
            
            // Utiliser le repository pour l'import avec persistance complète des nutriments
            val importResult = if (foodRepository is fr.vetbrain.vetnutri_mp.Repository.DatabaseFoodRepository) {
                println("[EXCEL-SERVICE-INFO] Utilisation de importFoodsDomain...")
                // Utiliser importFoodsDomain pour la persistance complète (aliments + nutriments)
                val result = foodRepository.importFoodsDomain(parseResult.aliments)
                println("[EXCEL-SERVICE-INFO] Résultat importFoodsDomain: imported=${result.importedCount}, updated=${result.updatedCount}, errors=${result.errorCount}")
                result
            } else {
                println("[EXCEL-SERVICE-INFO] Utilisation du fallback importFoods...")
                // Fallback pour les autres implémentations
                val alimentsJson = parseResult.aliments.map { aliment ->
                    aliment.toAlimentEvJson()
                }
                val result = foodRepository.importFoods(alimentsJson)
                println("[EXCEL-SERVICE-INFO] Résultat importFoods: imported=${result.importedCount}, updated=${result.updatedCount}, errors=${result.errorCount}")
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
            
            println("[EXCEL-SERVICE-INFO] === RÉSULTAT FINAL ===")
            println("[EXCEL-SERVICE-INFO] Success: ${finalResult.success}")
            println("[EXCEL-SERVICE-INFO] Imported: ${finalResult.importedCount}")
            println("[EXCEL-SERVICE-INFO] Updated: ${finalResult.updatedCount}")
            println("[EXCEL-SERVICE-INFO] Errors: ${finalResult.errorCount}")
            println("[EXCEL-SERVICE-INFO] Message: ${finalResult.message}")
            
            return@withContext finalResult
            
        } catch (e: Exception) {
            println("[EXCEL-SERVICE-ERROR] Exception lors de l'import: ${e.message}")
            println("[EXCEL-SERVICE-ERROR] Stack trace: ${e.stackTraceToString()}")
            
            val errorResult = ExcelImportResult(
                success = false,
                message = "Erreur lors de l'import: ${e.message}",
                importedCount = 0,
                errorCount = parseResult.errors.size + 1,
                errors = parseResult.errors + listOf("Erreur d'import: ${e.message}")
            )
            
            println("[EXCEL-SERVICE-ERROR] Résultat d'erreur: imported=${errorResult.importedCount}, errors=${errorResult.errorCount}")
            return@withContext errorResult
        }
    }

    /**
     * Prépare un import CSV pour prévisualisation (sans sauvegarder)
     */
    suspend fun previewCsvImport(csvContent: String): ExcelImportResult = withContext(AppDispatchers.IO) {
        val parseResult = csvService.importFromCsv(csvContent)
        
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
