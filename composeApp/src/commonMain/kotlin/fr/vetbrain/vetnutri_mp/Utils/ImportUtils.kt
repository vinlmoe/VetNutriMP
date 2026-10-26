package fr.vetbrain.vetnutri_mp.Utils

import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Data.AnimalEvJson
import fr.vetbrain.vetnutri_mp.Service.ImportPersistence

/**
 * Classe utilitaire pour l'importation de données.
 *
 * Façade : chaque méthode publique délègue aux objets spécialisés
 * [AnimalJsonImporter], [FoodJsonImporter], [NutritionalRequirementImporter], [ImportCommun]
 * et [ImportPersistence] (signatures inchangées pour les appelants).
 */
object ImportUtils {
    /** Liste pour traquer les résolutions de nutriments problématiques */
    internal val resolutionsProblematiques = mutableListOf<String>()

    /** Fonction pour extraire tous les cas problématiques */
    fun getResolutionsProblematiques(): List<String> {
        return resolutionsProblematiques.toList()
    }

    /** Fonction pour vider la liste des résolutions problématiques */
    fun clearResolutionsProblematiques() {
        resolutionsProblematiques.clear()
    }

    /** Génère un rapport détaillé des résolutions problématiques */
    fun genererRapportResolutionsProblematiques(): String {
        val rapport = StringBuilder()
        rapport.append("🔍 RAPPORT DES RÉSOLUTIONS PROBLÉMATIQUES DE NUTRIMENTS\n")
        rapport.append("=".repeat(80) + "\n\n")

        if (resolutionsProblematiques.isEmpty()) {
            rapport.append("✅ Aucune résolution problématique détectée !\n")
            return rapport.toString()
        }

        // Grouper par type de problème
        val echecs = resolutionsProblematiques.filter { it.startsWith("ECHEC_TOTAL") }
        val resolutionsParVariante =
                resolutionsProblematiques.filter { it.startsWith("RESOLUTION_PAR_VARIANTE") }
        val echecsExacts =
                resolutionsProblematiques.filter { it.startsWith("ECHEC_RESOLUTION_EXACTE") }

        rapport.append("📊 STATISTIQUES:\n")
        rapport.append("  • Échecs totaux: ${echecs.size}\n")
        rapport.append("  • Résolutions par variante: ${resolutionsParVariante.size}\n")
        rapport.append("  • Échecs de résolution exacte: ${echecsExacts.size}\n")
        rapport.append("  • Total des problèmes: ${resolutionsProblematiques.size}\n\n")

        if (echecs.isNotEmpty()) {
            rapport.append("❌ ÉCHECS TOTAUX (${echecs.size}):\n")
            rapport.append("-".repeat(50) + "\n")
            echecs.forEach { probleme ->
                val nutrient = probleme.substringAfter("ECHEC_TOTAL: '").substringBefore("'")
                rapport.append("  • $nutrient\n")
            }
            rapport.append("\n")
        }

        if (resolutionsParVariante.isNotEmpty()) {
            rapport.append("⚠️ RÉSOLUTIONS PAR VARIANTE (${resolutionsParVariante.size}):\n")
            rapport.append("-".repeat(50) + "\n")
            resolutionsParVariante.forEach { probleme ->
                val details = probleme.substringAfter("RESOLUTION_PAR_VARIANTE: ")
                rapport.append("  • $details\n")
            }
            rapport.append("\n")
        }

        if (echecsExacts.isNotEmpty()) {
            rapport.append("🔶 ÉCHECS DE RÉSOLUTION EXACTE (${echecsExacts.size}):\n")
            rapport.append("-".repeat(50) + "\n")
            val nutrientsProblematiques = mutableMapOf<String, String>()
            echecsExacts.forEach { probleme ->
                val parts = probleme.substringAfter("ECHEC_RESOLUTION_EXACTE: ").split(" → ")
                if (parts.size == 2) {
                    val original = parts[0].trim('\'')
                    val normalise = parts[1].trim('\'')
                    nutrientsProblematiques[original] = normalise
                }
            }

            nutrientsProblematiques.forEach { (original, normalise) ->
                rapport.append("  • '$original' → '$normalise'\n")
            }
        }

        return rapport.toString()
    }

    // ----- Animaux -----

    /** Importe des animaux (et leurs aliments) depuis un contenu JSON */
    fun importAnimalsFromJson(content: String): ImportResult =
            AnimalJsonImporter.importAnimalsFromJson(content)

    /** Extrait les aliments d'un JSON d'animaux */
    fun extractFoodsFromAnimalJson(content: String): List<AlimentEvJson> =
            AnimalJsonImporter.extractFoodsFromAnimalJson(content)

    /** Analyse la structure d'un JSON d'animaux (rapport texte) */
    fun analyzeAnimalJsonStructure(content: String): String =
            AnimalJsonImporter.analyzeAnimalJsonStructure(content)

    // ----- Aliments -----

    /** Importe des aliments depuis un contenu JSON */
    fun importFoodsFromJson(jsonContent: String): List<AlimentEvJson> =
            FoodJsonImporter.importFoodsFromJson(jsonContent)

    /** Analyse les espèces présentes dans un JSON d'aliments (rapport texte) */
    fun analyzeEspecesInFoodJson(content: String): String =
            FoodJsonImporter.analyzeEspecesInFoodJson(content)

    /** Extrait les sections foods et references d'un fichier vetnutri_export_init.json */
    fun extractSectionsFromVetNutriInit(jsonContent: String): Pair<List<AlimentEvJson>, String> =
            FoodJsonImporter.extractSectionsFromVetNutriInit(jsonContent)

    // ----- Détection de contenu -----

    /** Vérifie si le contenu JSON correspond à des animaux */
    fun isAnimalJsonContent(content: String): Boolean = ImportCommun.isAnimalJsonContent(content)

    /** Vérifie si le contenu est un JSON valide */
    fun isValidJson(content: String): Boolean = ImportCommun.isValidJson(content)

    // ----- Références nutritionnelles -----

    /** Importe des références nutritionnelles (.vbnr.json) sans sauvegarde en base */
    fun importNutritionalRequirementsFromJson(
            jsonContent: String
    ): List<fr.vetbrain.vetnutri_mp.Data.ReferenceEv> =
            NutritionalRequirementImporter.importNutritionalRequirementsFromJson(jsonContent)

    /** Importe des références nutritionnelles (.vbnr.json) avec sauvegarde en base */
    suspend fun importNutritionalRequirementsFromJson(
            jsonContent: String,
            databaseReferenceEvRepository:
                    fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository? =
                    null,
            equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository? = null,
            biblioRefRepository: fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository? = null,
            sauvegarderEnBase: Boolean = true
    ): List<fr.vetbrain.vetnutri_mp.Data.ReferenceEv> =
            ImportPersistence.importNutritionalRequirementsFromJson(
                    jsonContent,
                    databaseReferenceEvRepository,
                    equationRepository,
                    biblioRefRepository,
                    sauvegarderEnBase
            )

    /** Sauvegarde les références importées (et leurs équations / bibliographies) en base */
    suspend fun sauvegarderDonneesImportees(
            referencesImportees: List<fr.vetbrain.vetnutri_mp.Data.ReferenceEv>,
            databaseReferenceEvRepository:
                    fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository?,
            equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository?,
            biblioRefRepository: fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository?
    ): SauvegardeResult =
            ImportPersistence.sauvegarderDonneesImportees(
                    referencesImportees,
                    databaseReferenceEvRepository,
                    equationRepository,
                    biblioRefRepository
            )
}

/** Classe contenant le résultat de l'importation */
data class ImportResult(val animals: List<AnimalEvJson>, val foods: List<AlimentEvJson>)

/** Classe de données pour représenter un besoin nutritionnel lors de l'importation */
data class NutrientRequirementInfo(
        val nutrient: fr.vetbrain.vetnutri_mp.Enumer.Nutrient,
        val referenceLevel: fr.vetbrain.vetnutri_mp.Enumer.Reflevel,
        val quantity: Double,
        val unit: fr.vetbrain.vetnutri_mp.Enumer.UnitEnum,
        val unitRequirement: fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum,
        val bibliographicReference: fr.vetbrain.vetnutri_mp.Data.BiblioRef?
)

/** Extension pour répéter une chaîne (comme Python * operator) */
operator fun String.times(n: Int): String = repeat(n)

/** Résultat d'une opération de sauvegarde automatique */
data class SauvegardeResult(
        val referencesAjoutees: Int,
        val referencesMisesAJour: Int,
        val equationsAjoutees: Int,
        val equationsMisesAJour: Int,
        val bibliographiesAjoutees: Int,
        val bibliographiesMisesAJour: Int,
        val erreurs: List<String>
) {
    val totalReferences: Int
        get() = referencesAjoutees + referencesMisesAJour
    val totalEquations: Int
        get() = equationsAjoutees + equationsMisesAJour
    val totalBibliographies: Int
        get() = bibliographiesAjoutees + bibliographiesMisesAJour
    val aDesErreurs: Boolean
        get() = erreurs.isNotEmpty()
    val succesTotal: Boolean
        get() = !aDesErreurs
}
