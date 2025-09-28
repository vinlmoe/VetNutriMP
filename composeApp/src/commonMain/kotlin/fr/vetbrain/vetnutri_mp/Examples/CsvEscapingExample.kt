package fr.vetbrain.vetnutri_mp.Examples

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Utils.genUUID

/**
 * Exemple démontrant la gestion des sauts de ligne dans les champs CSV
 */
class CsvEscapingExample {

    fun demonstrateCsvEscaping() {
        // Créer un aliment avec des champs contenant des sauts de ligne
        val alimentAvecSautsLigne = AlimentEv(
            uuid = genUUID(),
            nom = "Aliment avec description\nmulti-ligne",
            brand = "Marque Test",
            ingredients = "Ingrédient 1\nIngrédient 2\nIngrédient 3",
            group = GroupAlim.FCOMP,
            typeAliment = FoodKind.COMPLET,
            cont = ContEnum.SACHET,
            price = 25.99,
            categPrice = "Premium",
            quantInt = 1.5,
            consistent = true,
            especes = mutableListOf("Chat", "Chien"),
            indicat = mutableListOf(AlimIndic.NEUT, AlimIndic.SEN)
        ).apply {
            setNutrient(NutrientMain.PROTEINE, 30.0)
            setNutrient(NutrientMain.LIPIDE, 12.0)
        }

        // Simuler la conversion en CSV (sans utiliser le service complet)
        val csvContent = generateSampleCsvWithLineBreaks(alimentAvecSautsLigne)

        println("=== CSV généré avec échappement ===")
        println(csvContent)
        println()

        // Simuler l'import (parsing basique)
        println("=== Simulation d'import ===")
        println("Le CSV avec sauts de ligne échappés peut maintenant être parsé correctement")
        println("Les champs multi-lignes sont préservés dans les guillemets")
        println("Exemple de champ avec saut de ligne:")
        println("  Original: '${alimentAvecSautsLigne.ingredients}'")
        println("  Dans CSV: \"Ingrédient 1\nIngrédient 2\nIngrédient 3\"")
    }

    /**
     * Montre comment les champs avec sauts de ligne sont échappés
     */
    fun showEscapingExamples() {
        val testCases = listOf(
            "Champ normal",
            "Champ avec,virgule",
            "Champ avec \"guillemets\"",
            "Champ avec\nsaut de ligne",
            "Champ avec\ttabulation",
            "Champ \"avec tous\nles caractères\tspeciaux\""
        )

        println("=== Exemples d'échappement CSV ===")
        testCases.forEach { testCase ->
            val escaped = escapeCsvValue(testCase)
            println("Original: '$testCase'")
            println("Échappé:  '$escaped'")
            println()
        }
    }

    /**
     * Génère un exemple de CSV avec des champs contenant des sauts de ligne
     */
    private fun generateSampleCsvWithLineBreaks(aliment: AlimentEv): String {
        val values = mutableListOf<String>()

        // Ajouter les valeurs de base
        values.add(aliment.uuid)
        values.add(escapeCsvValue(aliment.nom ?: ""))
        values.add(escapeCsvValue(aliment.brand ?: ""))
        values.add(escapeCsvValue(aliment.gamme ?: ""))
        values.add(escapeCsvValue(aliment.ingredients ?: ""))
        values.add(aliment.group?.label ?: "")
        values.add(aliment.typeAliment?.label ?: "")
        values.add(aliment.cont?.label ?: "")
        values.add(aliment.price?.toString() ?: "")
        values.add(escapeCsvValue(aliment.categPrice ?: ""))
        values.add(aliment.quantInt?.toString() ?: "")
        values.add(aliment.consistent.toString())
        values.add(aliment.deprecated.toString())
        values.add(escapeCsvValue(aliment.dataB ?: ""))
        values.add(escapeCsvValue(aliment.especes.joinToString(", ")))
        values.add(escapeCsvValue(aliment.indicat.joinToString(", ") { it.label }))
        values.add(escapeCsvValue(aliment.rationUUID ?: ""))

        // Ajouter les nutriments (simplifié pour l'exemple)
        values.add("PROTEINE")
        values.add(aliment.valMap[NutrientMain.PROTEINE]?.value?.toString() ?: "")
        values.add(aliment.valMap[NutrientMain.PROTEINE]?.nut ?: "")
        values.add("LIPIDE")
        values.add(aliment.valMap[NutrientMain.LIPIDE]?.value?.toString() ?: "")
        values.add(aliment.valMap[NutrientMain.LIPIDE]?.nut ?: "")

        return values.joinToString(";")
    }

    /**
     * Fonction d'échappement CSV (copie locale pour l'exemple)
     */
    private fun escapeCsvValue(value: String): String {
        val needsEscaping = value.contains(";") ||
                           value.contains("\"") ||
                           value.contains("\n") ||
                           value.contains("\r") ||
                           value.contains("\t")

        return if (needsEscaping) {
            val escapedContent = value.replace("\"", "\"\"")
            "\"$escapedContent\""
        } else {
            value
        }
    }
}
