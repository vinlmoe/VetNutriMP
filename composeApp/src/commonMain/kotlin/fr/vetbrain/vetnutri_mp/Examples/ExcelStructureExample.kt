package fr.vetbrain.vetnutri_mp.Examples

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Services.ExcelFoodService
import fr.vetbrain.vetnutri_mp.Utils.genUUID

/**
 * Exemple pour démontrer la nouvelle structure CSV avec unité dans l'en-tête
 */
class ExcelStructureExample {

    fun demonstrateNewCsvStructure() {
        // Créer un aliment exemple avec quelques nutriments
        val exempleAliment = AlimentEv(
            uuid = genUUID(),
            nom = "Croquettes Premium Chat",
            brand = "Royal Canin",
            gamme = "Sterilised",
            group = GroupAlim.FCOMP,
            typeAliment = FoodKind.COMPLET,
            cont = ContEnum.SACHET,
            price = 45.99,
            categPrice = "Premium",
            quantInt = 2.0,
            consistent = true,
            especes = mutableListOf("Chat"),
            indicat = mutableListOf(AlimIndic.NEUT, AlimIndic.SEN)
        ).apply {
            // Ajouter quelques nutriments avec leurs unités
            setNutrient(NutrientMain.PROTEINE, 30.0)  // 30 g/100g
            setNutrient(NutrientMain.LIPIDE, 12.0)    // 12 g/100g
            setNutrient(NutrientMain.ENERGIE, 350.0)  // 350 kcal/100g
            setNutrient(NutrientVitam.VITA, 15000.0)  // 15000 µg/100g
            setNutrient(NutrientMacro.CAL, 1.2)       // 1.2 g/100g
        }

        // Convertir en AlimentExcelRow
        val excelRow = fr.vetbrain.vetnutri_mp.Data.AlimentExcelRow.fromAlimentEv(exempleAliment)

        // Afficher la structure des données
        excelRow.nutriments.forEach { (nutrientLabel, valeur) ->
        }

        // Générer le CSV (simulation)
    }

    /**
     * Exemple de structure CSV attendue
     */
    fun showExpectedCsvStructure() {
    }
}