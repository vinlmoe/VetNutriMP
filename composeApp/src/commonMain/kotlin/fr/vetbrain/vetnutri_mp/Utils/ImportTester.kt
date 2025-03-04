package fr.vetbrain.vetnutri_mp.Utils

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Data.toData
import fr.vetbrain.vetnutri_mp.Enumer.Espece

/** Utilitaire pour tester l'importation et l'affichage des aliments */
object ImportTester {

    /**
     * Teste l'importation d'un fichier JSON d'aliments et analyse les espèces
     *
     * @param jsonContent Le contenu JSON à tester
     * @return Un rapport de test
     */
    fun testFoodImport(jsonContent: String): String {
        val report = StringBuilder()
        report.append("==== TEST D'IMPORT DES ALIMENTS ====\n\n")

        try {
            // Analyse des espèces
            report.append(ImportUtils.analyzeEspecesInFoodJson(jsonContent))

            // Tenter l'importation
            report.append("\n==== RÉSULTAT DE L'IMPORTATION ====\n")
            val foods = ImportUtils.importFoodsFromJson(jsonContent)

            report.append("Nombre d'aliments importés: ${foods.size}\n\n")

            // Vérifier que les espèces sont correctement converties
            val foodsByEspeces: Map<List<String>, List<AlimentEvJson>> =
                    foods.groupBy { it.Especes }

            report.append("Répartition par espèces après importation:\n")
            foodsByEspeces.forEach { (especes: List<String>, foodList: List<AlimentEvJson>) ->
                report.append("- ${especes.joinToString(", ")}: ${foodList.size} aliments\n")
            }

            // Extraire quelques exemples pour vérification
            if (foods.isNotEmpty()) {
                report.append("\nExemples d'aliments importés:\n")
                foods.take(5).forEach { food ->
                    report.append("- ${food.nom} (ID: ${food.UUID})\n")
                    report.append("  Espèces: ${food.Especes.joinToString(", ")}\n")
                    report.append("  Champ espece: ${food.espece}\n")

                    // Tester la conversion vers les objets de domaine
                    val domainFood: AlimentEv = food.toData()
                    report.append("  Après conversion vers AlimentEv:\n")
                    report.append("    Espèces: ${domainFood.especes.joinToString(", ")}\n")

                    // Vérifier la correspondance avec l'énumération Espece
                    domainFood.especes.forEach { especeStr: String ->
                        try {
                            val espece = Espece.valueOf(especeStr)
                            report.append("    - $especeStr = OK (ID: ${espece.id})\n")
                        } catch (e: Exception) {
                            try {
                                val espece = Espece.getByLabel(especeStr)
                                if (espece != null) {
                                    report.append(
                                            "    - $especeStr = OK via label (ID: ${espece.id})\n"
                                    )
                                } else {
                                    report.append("    - $especeStr = NON RECONNU\n")
                                }
                            } catch (e: Exception) {
                                report.append("    - $especeStr = ERREUR: ${e.message}\n")
                            }
                        }
                    }

                    report.append("\n")
                }
            }
        } catch (e: Exception) {
            report.append("ERREUR CRITIQUE: ${e.message}\n")
            e.printStackTrace()
        }

        return report.toString()
    }
}
