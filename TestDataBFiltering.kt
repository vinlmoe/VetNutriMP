// Test simple pour vérifier le filtrage dataB
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.View.components.FoodSearchFilters

fun main() {
    // Créer des aliments de test avec différentes valeurs dataB
    val testFoods = listOf(
        AlimentEv(uuid = "1", nom = "Aliment VF24", dataB = "VF24"),
        AlimentEv(uuid = "2", nom = "Aliment CIQUAL", dataB = "0"),
        AlimentEv(uuid = "3", nom = "Aliment FCEN", dataB = "1"),
        AlimentEv(uuid = "4", nom = "Aliment Barf", dataB = "5"),
        AlimentEv(uuid = "5", nom = "Aliment Générique", dataB = "4"),
        AlimentEv(uuid = "6", nom = "Aliment Sans DataB", dataB = null),
        AlimentEv(uuid = "7", nom = "Aliment VF24 2", dataB = "VF24")
    )

    println("=== Test du filtrage dataB ===")
    println("Aliments disponibles:")
    testFoods.forEach { aliment ->
        println("  - ${aliment.nom}: dataB='${aliment.dataB}'")
    }

    // Tester différents filtres
    val testFilters = listOf(
        "VF24" to "VetFood 2024",
        "0" to "CIQUAL",
        "1" to "FCEN",
        "5" to "Aliment Barf",
        "4" to "Générique",
        null to "Aucun filtre",
        "" to "Filtre vide"
    )

    testFilters.forEach { (filterValue, filterName) ->
        val filters = FoodSearchFilters(dataB = filterValue)

        val filteredFoods = testFoods.filter { aliment ->
            // Même logique que dans FoodSearchComponent
            val matchesDataB = when (val dataBFilter = filters.dataB) {
                null -> true
                "" -> true
                else -> aliment.dataB?.trim() == dataBFilter.trim()
            }
            matchesDataB
        }

        println("\n=== Test avec filtre '$filterName' (valeur: '$filterValue') ===")
        println("Nombre d'aliments filtrés: ${filteredFoods.size}")
        filteredFoods.forEach { aliment ->
            println("  ✓ ${aliment.nom} (dataB: '${aliment.dataB}')")
        }
    }
}
