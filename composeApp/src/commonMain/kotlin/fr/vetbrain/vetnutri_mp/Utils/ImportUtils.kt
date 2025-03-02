package fr.vetbrain.vetnutri_mp.Utils

import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Data.AnimalEvJson
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

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
     * @param content Le contenu JSON à désérialiser
     * @return Un objet contenant la liste des animaux importés et la liste des aliments extraits
     * des rations
     */
    fun importAnimalsFromJson(content: String): ImportResult {
        println("Début de l'importation JSON. Taille du contenu: ${content.length} caractères")

        try {
            // Prétraiter le JSON pour convertir les valeurs de nutriments complexes en valeurs
            // simples
            val jsonParser = Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
            val originalJson = jsonParser.parseToJsonElement(content)
            val preprocessedJson = preprocessNutrientValues(originalJson)
            val preprocessedContent = jsonParser.encodeToString(preprocessedJson)

            println("Tentative d'importation comme liste d'animaux...")
            val animals =
                    try {
                        jsonParser.decodeFromString<List<AnimalEvJson>>(preprocessedContent)
                    } catch (e: Exception) {
                        println("Erreur lors de l'importation comme liste: ${e.message}")

                        // Essayer d'importer comme un seul animal
                        println("Tentative d'importation comme un seul animal...")
                        try {
                            listOf(jsonParser.decodeFromString<AnimalEvJson>(preprocessedContent))
                        } catch (e: Exception) {
                            println(
                                    "Erreur lors de l'importation comme animal unique: ${e.message}"
                            )
                            emptyList()
                        }
                    }

            println("Importation réussie: ${animals.size} animaux trouvés")

            // Extraire tous les aliments des rations
            val allFoods = mutableSetOf<AlimentEvJson>()
            animals.forEach { animal ->
                // Vérifier les consultations directes
                animal.consultations?.forEach { consultation ->
                    // Vérifier les rations dans la consultation
                    consultation.rationList.forEach { (_, ration) ->
                        ration.alimentList.forEach { alimentRation ->
                            alimentRation.alime.let { aliment -> allFoods.add(aliment) }
                        }
                    }
                }

                // Vérifier également les consultations dans list
                animal.list?.consultations?.forEach { consultation ->
                    consultation.rationList.forEach { (_, ration) ->
                        ration.alimentList.forEach { alimentRation ->
                            alimentRation.alime.let { aliment -> allFoods.add(aliment) }
                        }
                    }
                }
            }

            println("Extraction terminée: ${allFoods.size} aliments extraits des rations")

            return ImportResult(animals, allFoods.toList())
        } catch (e: Exception) {
            println("Erreur lors de l'importation JSON: ${e.message}")
            e.printStackTrace()
            return ImportResult(emptyList(), emptyList())
        }
    }

    /**
     * Importe une liste d'aliments à partir d'une chaîne JSON
     *
     * @param jsonContent Le contenu JSON à désérialiser
     * @return La liste des aliments importés
     */
    fun importFoodsFromJson(jsonContent: String): List<AlimentEvJson> {
        println(
                "Début de l'importation JSON des aliments. Taille du contenu: ${jsonContent.length} caractères"
        )

        try {
            // Tenter de traiter directement le JSON en cas de format complexe
            val jsonElement = json.parseToJsonElement(jsonContent)
            val preprocessedJson = preprocessNutrientValues(jsonElement)
            val preprocessedJsonString =
                    json.encodeToString(JsonElement.serializer(), preprocessedJson)

            try {
                println("Tentative d'importation comme liste d'aliments...")
                val foods = json.decodeFromString<List<AlimentEvJson>>(preprocessedJsonString)
                println("Importation réussie: ${foods.size} aliments trouvés")
                return foods
            } catch (e: Exception) {
                println("Erreur lors de l'importation comme liste: ${e.message}")

                try {
                    println("Tentative d'importation comme un seul aliment...")
                    val food = json.decodeFromString<AlimentEvJson>(preprocessedJsonString)
                    println("Importation réussie: 1 aliment trouvé")
                    return listOf(food)
                } catch (e: Exception) {
                    println("Erreur lors de l'importation comme un seul aliment: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("Erreur lors du prétraitement du JSON: ${e.message}")

            // Fallback à la méthode standard
            try {
                println("Tentative d'importation standard comme liste d'aliments...")
                val foods = json.decodeFromString<List<AlimentEvJson>>(jsonContent)
                println("Importation réussie: ${foods.size} aliments trouvés")
                return foods
            } catch (e: Exception) {
                println("Erreur lors de l'importation standard comme liste: ${e.message}")

                try {
                    println("Tentative d'importation standard comme un seul aliment...")
                    val food = json.decodeFromString<AlimentEvJson>(jsonContent)
                    println("Importation réussie: 1 aliment trouvé")
                    return listOf(food)
                } catch (e: Exception) {
                    println(
                            "Erreur lors de l'importation standard comme un seul aliment: ${e.message}"
                    )
                }
            }
        }

        println("Échec de l'importation. Retour d'une liste vide.")
        return emptyList()
    }

    /**
     * Prétraite les valeurs nutritionnelles dans le JSON pour convertir les objets complexes en
     * valeurs simples
     *
     * @param jsonElement L'élément JSON à prétraiter
     * @return L'élément JSON prétraité
     */
    private fun preprocessNutrientValues(jsonElement: JsonElement): JsonElement {
        when (jsonElement) {
            is JsonObject -> {
                val result = jsonElement.toMutableMap()

                // Si on trouve un champ "valMap", traiter son contenu
                if (result.containsKey("valMap")) {
                    val valMap = result["valMap"]
                    if (valMap is JsonObject) {
                        val newValMap = valMap.toMutableMap()

                        // Parcourir les entrées de valMap
                        for ((key, value) in valMap.entries) {
                            if (value is JsonObject && value.contains("value")) {
                                // Si la valeur est un objet avec un champ "value", extraire ce
                                // champ
                                val valueField = value["value"]
                                if (valueField is JsonPrimitive) {
                                    // Remplacer l'objet par la valeur directe
                                    newValMap[key] = valueField
                                }
                            }
                        }

                        // Remplacer valMap par sa version prétraitée
                        result["valMap"] = JsonObject(newValMap)
                    }
                }

                // Prétraiter récursivement tous les champs qui sont des objets ou des tableaux
                for ((key, value) in result.entries) {
                    result[key] = preprocessNutrientValues(value)
                }

                return JsonObject(result)
            }
            is kotlinx.serialization.json.JsonArray -> {
                // Prétraiter récursivement chaque élément du tableau
                return kotlinx.serialization.json.JsonArray(
                        jsonElement.map { preprocessNutrientValues(it) }
                )
            }
            else -> return jsonElement
        }
    }

    /**
     * Détermine si un aliment doit être remplacé par une version plus complète.
     * @param existingFood L'aliment existant
     * @param newFood Le nouvel aliment
     * @return true si le nouvel aliment est plus complet et doit remplacer l'existant
     */
    private fun shouldReplaceFood(existingFood: AlimentEvJson, newFood: AlimentEvJson): Boolean {
        var existingScore = 0
        var newScore = 0

        // Attribuer des points pour chaque attribut non vide
        if (existingFood.nom.isNotBlank()) existingScore += 1
        if (newFood.nom.isNotBlank()) newScore += 1

        if (existingFood.ingredients.isNotBlank()) existingScore += 1
        if (newFood.ingredients.isNotBlank()) newScore += 1

        if (existingFood.marque.isNotBlank()) existingScore += 1
        if (newFood.marque.isNotBlank()) newScore += 1

        if (existingFood.gamme.isNotBlank()) existingScore += 1
        if (newFood.gamme.isNotBlank()) newScore += 1

        if (existingFood.valMap.isNotEmpty()) existingScore += existingFood.valMap.size
        if (newFood.valMap.isNotEmpty()) newScore += newFood.valMap.size

        // Si le nouveau score est meilleur, remplacer
        return newScore > existingScore
    }

    /**
     * Extrait les aliments d'un JSON contenant des animaux et leurs rations
     *
     * @param content Le contenu JSON à analyser
     * @return La liste des aliments extraits
     */
    fun extractFoodsFromAnimalJson(content: String): List<AlimentEvJson> {
        try {
            // Prétraiter le JSON pour convertir les valeurs de nutriments complexes en valeurs
            // simples
            val jsonParser = Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
            val originalJson = jsonParser.parseToJsonElement(content)
            val preprocessedJson = preprocessNutrientValues(originalJson)
            val preprocessedContent = jsonParser.encodeToString(preprocessedJson)

            println("Extraction des aliments à partir du JSON d'animaux...")

            // D'abord essayer de parser comme liste d'animaux
            val animals =
                    try {
                        jsonParser.decodeFromString<List<AnimalEvJson>>(preprocessedContent)
                    } catch (e: Exception) {
                        // Essayer d'importer comme un seul animal
                        try {
                            listOf(jsonParser.decodeFromString<AnimalEvJson>(preprocessedContent))
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }

            // Extraire tous les aliments des rations
            val allFoods = mutableSetOf<AlimentEvJson>()
            animals.forEach { animal ->
                // Vérifier les consultations directes
                animal.consultations?.forEach { consultation ->
                    // Vérifier les rations dans la consultation
                    consultation.rationList.forEach { (_, ration) ->
                        ration.alimentList.forEach { alimentRation ->
                            alimentRation.alime.let { aliment -> allFoods.add(aliment) }
                        }
                    }
                }

                // Vérifier également les consultations dans list
                animal.list?.consultations?.forEach { consultation ->
                    consultation.rationList.forEach { (_, ration) ->
                        ration.alimentList.forEach { alimentRation ->
                            alimentRation.alime.let { aliment -> allFoods.add(aliment) }
                        }
                    }
                }
            }

            println("Extraction terminée: ${allFoods.size} aliments extraits des rations")
            return allFoods.toList()
        } catch (e: Exception) {
            println("Erreur lors de l'extraction des aliments du JSON: ${e.message}")
            return emptyList()
        }
    }
}

/** Classe contenant le résultat de l'importation */
data class ImportResult(val animals: List<AnimalEvJson>, val foods: List<AlimentEvJson>)
