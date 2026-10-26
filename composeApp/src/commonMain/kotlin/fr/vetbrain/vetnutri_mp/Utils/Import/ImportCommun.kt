package fr.vetbrain.vetnutri_mp.Utils

import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Data.AnimalEvJson
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Utils.RaceCodeMapper
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** Helpers d'import partagés par les importateurs JSON (extraits de ImportUtils). */
object ImportCommun {
    /** Format JSON pour la désérialisation */
    internal val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true // Ajouter plus de tolérance pour le parsing
        prettyPrint = false
        explicitNulls = false
    }

    /**
     * Vérifie si le contenu JSON correspond à un fichier d'animaux ou d'aliments
     *
     * @param content Le contenu JSON à analyser
     * @return true si le contenu semble être un fichier d'animaux, false s'il semble être un
     * fichier d'aliments
     */
    fun isAnimalJsonContent(content: String): Boolean {
        try {
            if (content.isBlank()) {
                return false
            }

            val jsonParser = Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
                explicitNulls = false
            }

            val jsonElement = jsonParser.parseToJsonElement(content)

            // Vérifier si c'est un tableau ou un objet unique
            if (jsonElement is kotlinx.serialization.json.JsonArray) {
                // Examiner le premier élément du tableau
                if (jsonElement.isEmpty()) {
                    return false
                }

                val firstItem = jsonElement[0]
                return isAnimalJsonObject(firstItem)
            } else if (jsonElement is JsonObject) {
                // C'est un objet unique
                return isAnimalJsonObject(jsonElement)
            }

            return false
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Vérifie si un objet JSON correspond à un animal ou un aliment
     *
     * @param jsonObject L'objet JSON à analyser
     * @return true si l'objet semble être un animal, false s'il semble être un aliment
     */
    private fun isAnimalJsonObject(jsonObject: JsonElement): Boolean {
        if (jsonObject !is JsonObject) {
            return false
        }

        // Caractéristiques spécifiques aux animaux
        val animalSpecificKeys =
                listOf(
                        "listWeight",
                        "sex",
                        "race",
                        "dateNaiss",
                        "nomProprio",
                        "consultations",
                        "list"
                )

        // Caractéristiques spécifiques aux aliments
        val foodSpecificKeys =
                listOf("ingredients", "prix", "marque", "categoriePrix", "quantInt", "presentation")

        // Compter combien de caractéristiques d'animal sont présentes
        val animalKeysCount = animalSpecificKeys.count { key -> jsonObject.containsKey(key) }

        // Compter combien de caractéristiques d'aliment sont présentes
        val foodKeysCount = foodSpecificKeys.count { key -> jsonObject.containsKey(key) }

        // Si plus de caractéristiques d'animal sont présentes, c'est probablement un animal
        return animalKeysCount > foodKeysCount
    }

    /**
     * Vérifie si le contenu est un JSON valide
     *
     * @param content Le contenu à vérifier
     * @return true si le contenu est un JSON valide, false sinon
     */
    fun isValidJson(content: String): Boolean {
        return try {
            if (content.isBlank()) {
                return false
            }

            val jsonParser = Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
                explicitNulls = false
            }

            // Tente simplement de parser le JSON sans traiter son contenu
            jsonParser.parseToJsonElement(content)
            true
        } catch (e: Exception) {
            false
        }
    }

}
