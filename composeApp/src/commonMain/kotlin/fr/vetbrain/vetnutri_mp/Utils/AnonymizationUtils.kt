package fr.vetbrain.vetnutri_mp.Utils

import fr.vetbrain.vetnutri_mp.Data.ApiEnvelope
import fr.vetbrain.vetnutri_mp.Data.AnimalApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Anonymise un JSON d'export en remplaçant l'identifiant de l'animal et le nom du propriétaire par "anonyme"
 * @param jsonContent Le contenu JSON à anonymiser
 * @return Le JSON anonymisé
 */
fun anonymizeExportJson(jsonContent: String): String {
    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    try {
        val envelope = json.decodeFromString<ApiEnvelope>(jsonContent)
        val anonymizedAnimals = envelope.animals.map { animal ->
            animal.copy(
                externalId = "anonyme",
                ownerName = "anonyme"
            )
        }
        val anonymizedEnvelope = envelope.copy(animals = anonymizedAnimals)
        return json.encodeToString(anonymizedEnvelope)
    } catch (e: Exception) {
        e.printStackTrace()
        return jsonContent
    }
}

