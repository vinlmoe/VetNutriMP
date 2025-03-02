package fr.vetbrain.vetnutri_mp.Utils

import fr.vetbrain.vetnutri_mp.Data.AnimalEvJson
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/** Classe utilitaire pour l'importation de données */
object ImportUtils {
    /** Format JSON pour la désérialisation */
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true // Ajouter plus de tolérance pour le parsing
    }

    /**
     * Importe une liste d'animaux à partir d'une chaîne JSON
     *
     * @param jsonContent Le contenu JSON à désérialiser
     * @return La liste des animaux importés
     */
    fun importAnimalsFromJson(jsonContent: String): List<AnimalEvJson> {
        println("Début de l'importation JSON. Taille du contenu: ${jsonContent.length} caractères")

        return try {
            println("Tentative d'importation comme liste d'animaux...")
            val animals = json.decodeFromString<List<AnimalEvJson>>(jsonContent)
            println("Importation réussie: ${animals.size} animaux trouvés")
            animals
        } catch (e: Exception) {
            println("Erreur lors de l'importation comme liste: ${e.message}")

            // En cas d'erreur, essayer d'importer un seul animal
            try {
                println("Tentative d'importation comme un seul animal...")
                val animal = json.decodeFromString<AnimalEvJson>(jsonContent)
                println("Importation réussie: 1 animal trouvé")
                listOf(animal)
            } catch (e: Exception) {
                println("Erreur lors de l'importation comme un seul animal: ${e.message}")
                println("Échec de l'importation. Retour d'une liste vide.")
                emptyList()
            }
        }
    }
}
