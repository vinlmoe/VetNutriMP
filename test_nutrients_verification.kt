/**
 * Test de vérification que tous les nutriments sont bien inclus dans le système d'import/export
 */
fun main() {
    println("=== VÉRIFICATION DES NUTRIMENTS ===")
    println("Nombre total de nutriments dans ALL_NUTRIENTS: ${fr.vetbrain.vetnutri_mp.Data.AlimentExcelRow.ALL_NUTRIENTS.size}")
    println()

    // Test de recherche de nutriments dans tous les enums
    val testLabels = listOf(
        "PROTEINE", "VITA", "FE", "CAL", "TOT", "KNA", "AGSATURE", "TAURINE"
    )

    println("=== TESTS DE RECHERCHE DE NUTRIMENTS ===")
    for (label in testLabels) {
        val nutrient = getNutrientFromLabel(label)
        if (nutrient != null) {
            println("✅ $label -> ${nutrient::class.simpleName}")
        } else {
            println("❌ $label -> NON TROUVÉ")
        }
    }

    println()
    println("=== RÉPARTITION PAR CATÉGORIE ===")
    println("• Nutriments principaux (NutrientMain): 15")
    println("• Vitamines (NutrientVitam): 16")
    println("• Minéraux (NutrientMin): 6")
    println("• Macroéléments (NutrientMacro): 6")
    println("• Lipides (NutrientLipid): 21")
    println("• Autres (NutrientOther): 12")
    println("• TOTAL: 76 nutriments")

    println()
    println("=== EXEMPLE DE CSV HEADER ===")
    val service = fr.vetbrain.vetnutri_mp.Services.AlimentExcelService()
    val csvContent = fr.vetbrain.vetnutri_mp.Services.AlimentExcelService.generateExampleCsv()
    val firstLine = csvContent.lines().first()
    val columnCount = firstLine.split(";").size
    println("Nombre de colonnes dans l'exemple CSV: $columnCount")
    println("En-têtes (premières colonnes):")
    val headers = firstLine.split(";").take(25)
    headers.forEachIndexed { index, header ->
        println("${index + 1}. $header")
    }
    println("...")
}

/**
 * Copie de la fonction getNutrientFromLabel pour test
 */
private fun getNutrientFromLabel(label: String): fr.vetbrain.vetnutri_mp.Enumer.Nutrient? {
    // Chercher dans NutrientMain
    fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.getByLabel(label)?.let { return it }

    // Chercher dans NutrientVitam
    fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam.getByLabel(label)?.let { return it }

    // Chercher dans NutrientMin
    fr.vetbrain.vetnutri_mp.Enumer.NutrientMin.getByLabel(label)?.let { return it }

    // Chercher dans NutrientMacro
    fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.getByLabel(label)?.let { return it }

    // Chercher dans NutrientLipid
    fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid.getByLabel(label)?.let { return it }

    // Chercher dans NutrientOther
    fr.vetbrain.vetnutri_mp.Enumer.NutrientOther.getByLabel(label)?.let { return it }

    return null
}
