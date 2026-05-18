package fr.vetbrain.vetnutri_mp.Examples

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Service.ExcelFoodService
import fr.vetbrain.vetnutri_mp.Utils.genUUID

/**
 * Exemple d'utilisation du service Excel pour l'import/export des aliments
 */
class ExcelImportExportExample {
    
    /**
     * Exemple d'export d'aliments vers CSV
     */
    suspend fun exportExample(excelFoodService: ExcelFoodService) {
        try {
            // Exporter tous les aliments
            val csvContent = excelFoodService.exportAllFoodsToCsv()
            
            // Exporter une sélection d'aliments
            val selectedIds = setOf("uuid1", "uuid2", "uuid3")
            val selectedCsv = excelFoodService.exportSelectedFoodsToCsv(selectedIds)
            
        } catch (e: Exception) {
        }
    }
    
    /**
     * Exemple d'import d'aliments depuis CSV
     */
    suspend fun importExample(excelFoodService: ExcelFoodService, csvContent: String) {
        try {
            // Prévisualisation de l'import
            val previewResult = excelFoodService.previewCsvImport(csvContent)
            
            if (previewResult.success) {
                // Import réel
                val importResult = excelFoodService.importFoodsFromCsv(csvContent)
            }
            
        } catch (e: Exception) {
        }
    }
    
    /**
     * Exemple de génération d'un CSV d'exemple
     */
    fun generateExampleCsv(excelFoodService: ExcelFoodService): String {
        return excelFoodService.generateExampleCsv()
    }
    
    /**
     * Exemple de création d'un aliment pour test
     */
    fun createTestAliment(): AlimentEv {
        return AlimentEv(
            uuid = genUUID(),
            nom = "Croquettes Premium Test",
            brand = "Test Brand",
            gamme = "Test Range",
            ingredients = "Viande de poulet, riz, maïs...",
            group = GroupAlim.FCOMP,
            typeAliment = FoodKind.COMPLET,
            cont = ContEnum.SACHET,
            price = 25.99,
            categPrice = "Premium",
            quantInt = 1.5,
            consistent = true,
            especes = mutableListOf("Chat"),
            indicat = mutableListOf(AlimIndic.NEUT)
        ).apply {
            // Ajouter quelques nutriments
            setNutrient(NutrientMain.PROTEINE, 30.0)
            setNutrient(NutrientMain.LIPIDE, 12.0)
            setNutrient(NutrientMain.ENERGIE, 350.0)
            setNutrient(NutrientVitam.VITA, 15000.0)
            setNutrient(NutrientMacro.CAL, 1.2)
        }
    }
}
