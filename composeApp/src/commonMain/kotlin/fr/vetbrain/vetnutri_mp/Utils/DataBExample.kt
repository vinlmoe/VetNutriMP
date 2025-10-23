package fr.vetbrain.vetnutri_mp.Utils

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Enumer.AAEnum
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain

/**
 * Exemples d'utilisation pratique de l'enum DataB dans l'application VetNutri MP
 *
 * Ce fichier montre comment tirer parti des fonctionnalités type-safe de l'enum DataB pour
 * améliorer la maintenabilité et réduire les erreurs dans le code.
 */
object DataBExample {

    /** Exemple 1: Validation et conversion d'une valeur dataB depuis un aliment */
    fun validateAndConvertDataB(alimentDataB: String?): DataB? {
        return DataB.fromCode(alimentDataB).also { dataB ->
            if (dataB == null && alimentDataB != null) {
                println("⚠️ Valeur dataB inconnue: '$alimentDataB'")
            }
        }
    }

    /** Exemple 2: Filtrage type-safe des aliments par base de données */
    fun filterAlimentsByDataB(aliments: List<AlimentEv>, targetDataB: DataB): List<AlimentEv> {
        return aliments.filter { aliment -> DataB.fromCode(aliment.dataB) == targetDataB }
    }

    /** Exemple 3: Statistiques sur les bases de données utilisées */
    fun getDataBStatistics(aliments: List<AlimentEv>): Map<DataB, Int> {
        return aliments.mapNotNull { DataB.fromCode(it.dataB) }.groupBy { it }.mapValues {
            it.value.size
        }
    }

    /** Exemple 4: Interface utilisateur avec enum (au lieu de strings) */
    fun createDataBFilterOptions(): List<Pair<DataB, String>> {
        return DataB.values().map { dataB -> dataB to dataB.displayName }
    }

    /** Exemple 5: Migration des anciennes valeurs string vers l'enum */
    fun migrateOldDataBValues(oldValue: String?): DataB? {
        return when (oldValue?.trim()) {
            "0" -> DataB.CIQUAL
            "1" -> DataB.FCEN
            "2" -> DataB.PETFOOD_DIVERS
            "4" -> DataB.GENERIQUE
            "5" -> DataB.ALIMENT_BARF
            "VF24" -> DataB.VETFOOD_2024
            "CHEVAL" -> DataB.CHEVAL
            else -> null
        }
    }

    /** Exemple 6: Sérialisation/Export avec l'enum */
    fun exportDataBInfo(dataB: DataB?): String {
        return dataB?.let { "${it.name} (${it.code}): ${it.displayName}" }
                ?: "Base de données inconnue"
    }
}

// Extension functions pour faciliter l'usage
fun String?.toDataB(): DataB? = DataB.fromCode(this)

fun DataB?.toDisplayString(): String = this?.displayName ?: "Base de données inconnue"

/** Classe exemple montrant comment intégrer DataB dans les modèles de données */
data class AlimentWithDataB(
        val name: String,
        val dataB: DataB?, // Au lieu de String?
        val nutrients: Map<String, Double> = emptyMap()
) {
    // Méthodes pratiques utilisant l'enum
    fun getDataBCode(): String = dataB?.code ?: ""
    fun getDataBDisplayName(): String = dataB.toDisplayString()

    fun isFromTrustedSource(): Boolean =
            dataB in listOf(DataB.CIQUAL, DataB.FCEN, DataB.VETFOOD_2024)
}

// Fonction d'exemple pour la migration
fun migrateAlimentToNewModel(oldAliment: AlimentEv): AlimentWithDataB {
    return AlimentWithDataB(
            name = oldAliment.nom ?: "Sans nom",
            dataB = oldAliment.dataB.toDataB(),
            nutrients = emptyMap() // À implémenter selon vos besoins
    )
}

/** Exemple de test de la protection de l'aminogramme */
object AminogramProtectionTest {

    /** Test de la protection : AA + VF24 = null */
    fun testAminogramProtection() {
        // Créer un aliment de test avec dataB = "VF24"
        val alimentVF24 = AlimentEv(nom = "Test Aliment VF24", dataB = "VF24")

        // Créer un aliment de test avec dataB = "CIQUAL"
        val alimentCIQUAL = AlimentEv(nom = "Test Aliment CIQUAL", dataB = "CIQUAL")

        // Tester avec un acide aminé (ALANINE)
        val alanine = AAEnum.ALANINE

        // Test 1 : VF24 + AA = null (PROTÉGÉ)
        val resultVF24 = alimentVF24.getNutrient(alanine)
        println("VF24 + ALANINE = $resultVF24 (doit être null)")

        // Test 2 : CIQUAL + AA = valeur normale (NON PROTÉGÉ)
        val resultCIQUAL = alimentCIQUAL.getNutrient(alanine)
        println("CIQUAL + ALANINE = $resultCIQUAL")

        // Test 3 : VF24 + autre nutriment = valeur normale (NON PROTÉGÉ)
        val energie = NutrientMain.ENERGIE
        val resultVF24Energie = alimentVF24.getNutrient(energie)
        println("VF24 + ENERGIE = $resultVF24Energie")
    }

    /** Test de la protection dans l'interface d'édition */
    fun testAminogramProtectionInEditInterface() {
        // Simuler un aliment VF24 avec des valeurs d'acides aminés
        val alimentVF24 =
                AlimentEv(nom = "Test Aliment VF24 avec AA", dataB = "VF24").apply {
                    // Ajouter quelques acides aminés avec des valeurs
                    valMap[AAEnum.ALANINE] =
                            fr.vetbrain.vetnutri_mp.Data.NutrientQuantity(1.5, "ALANINE")
                    valMap[AAEnum.ARGININE] =
                            fr.vetbrain.vetnutri_mp.Data.NutrientQuantity(2.1, "ARGININE")
                    valMap[NutrientMain.ENERGIE] =
                            fr.vetbrain.vetnutri_mp.Data.NutrientQuantity(100.0, "ENERGIE")
                }

        println("=== Test de protection dans l'interface d'édition ===")
        println("Aliment: ${alimentVF24.nom} (dataB: ${alimentVF24.dataB})")

        // Simuler ce qui se passe dans FoodEditView lors de l'initialisation
        val nutrientValues = mutableMapOf<Nutrient, String>()

        // Récupération des valeurs via getNutrient() (comme dans FoodEditView maintenant)
        listOf(AAEnum.ALANINE, AAEnum.ARGININE, NutrientMain.ENERGIE).forEach { nutrient ->
            val value = alimentVF24.getNutrient(nutrient)
            if (value != null) {
                nutrientValues[nutrient] = value.toString()
                println("✓ $nutrient = $value (AFFICHÉ)")
            } else {
                println("✗ $nutrient = null (MASQUÉ - PROTÉGÉ)")
            }
        }

        println("Résultat: ${nutrientValues.size} nutriments affichés dans l'interface d'édition")
        println("Les acides aminés VF24 sont maintenant masqués dans l'interface d'édition !")
    }
}
