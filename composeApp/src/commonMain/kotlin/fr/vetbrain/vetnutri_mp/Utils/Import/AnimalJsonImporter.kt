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

/** Importation JSON des animaux (extrait de ImportUtils). */
object AnimalJsonImporter {
    /**
     * Importe une liste d'animaux à partir d'une chaîne JSON
     *
     * @param content Le contenu JSON à désérialiser
     * @return Un objet contenant la liste des animaux importés et la liste des aliments extraits
     * des rations
     */
    fun importAnimalsFromJson(content: String): ImportResult {

        try {
            // Étape 1: Valider le format JSON de base
            if (content.isBlank()) {
                return ImportResult(emptyList(), emptyList())
            }

            // Vérifier si le contenu est un JSON valide
            if (!ImportCommun.isValidJson(content)) {
                return ImportResult(emptyList(), emptyList())
            }

            // Vérifier si le contenu correspond à un fichier d'animaux
            if (!ImportCommun.isAnimalJsonContent(content)) {
                return ImportResult(emptyList(), emptyList())
            }

            // Prétraiter le JSON pour convertir les valeurs de nutriments complexes en valeurs
            // simples
            val jsonParser = Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
                explicitNulls = false
            }

            val originalJson = jsonParser.parseToJsonElement(content)

            val preprocessedJson = FoodJsonImporter.preprocessNutrientValues(originalJson)

            val preprocessedWithEspeces = preprocessAnimalEspeces(preprocessedJson)
            val preprocessedContent = jsonParser.encodeToString(preprocessedWithEspeces)

            val animals =
                    try {
                        val result =
                                jsonParser.decodeFromString<List<AnimalEvJson>>(preprocessedContent)
                        result
                    } catch (e: Exception) {

                        // Essayer d'importer comme un seul animal
                        try {
                            val animal =
                                    jsonParser.decodeFromString<AnimalEvJson>(preprocessedContent)
                            listOf(animal)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            emptyList()
                        }
                    }

            // Si aucun animal n'a été importé, retourner un résultat vide
            if (animals.isEmpty()) {
                return ImportResult(emptyList(), emptyList())
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

            return ImportResult(animals, allFoods.toList())
        } catch (e: Exception) {
            e.printStackTrace()
            return ImportResult(emptyList(), emptyList())
        }
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
            val preprocessedJson = FoodJsonImporter.preprocessNutrientValues(originalJson)
            val preprocessedContent = jsonParser.encodeToString(preprocessedJson)

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

            return allFoods.toList()
        } catch (e: Exception) {
            return emptyList()
        }
    }

    /**
     * Prétraite les espèces dans les données d'animaux pour convertir les identifiants numériques
     * en labels
     * @param jsonElement L'élément JSON à prétraiter
     * @return L'élément JSON prétraité
     */
    private fun preprocessAnimalEspeces(jsonElement: JsonElement): JsonElement {
        when (jsonElement) {
            is JsonObject -> {
                val result = jsonElement.toMutableMap()

                // Si on trouve un champ "espece", le convertir en utilisant l'énumération Espece
                if (result.containsKey("espece")) {
                    val espece = result["espece"]
                    if (espece is JsonPrimitive) {
                        val especeStr = espece.toString().trim('"')

                        // Traiter à la fois les cas où espece est un nombre ou une chaîne
                        val especeValue =
                                try {
                                    especeStr.toIntOrNull()
                                } catch (e: Exception) {
                                    null
                                }

                        if (especeValue != null) {
                            try {
                                // Tenter de convertir l'ID numérique en énumération Espece
                                val especeEnum = Espece.getEnumFromInt(especeValue)
                                // Utiliser l'id de l'énumération comme valeur pour espece (au lieu
                                // de categorie)
                                result["espece"] = JsonPrimitive(especeEnum.id)
                            } catch (e: Exception) {
                                // Conserver la valeur originale si la conversion échoue
                                // Essayer de trouver une correspondance directe par categorie
                                val especeByCategorie =
                                        Espece.entries.find { it.categorie == especeValue }
                                if (especeByCategorie != null) {
                                    result["espece"] = JsonPrimitive(especeByCategorie.id)
                                } else {
                                    // En cas d'échec, définir une valeur par défaut pour éviter des
                                    // erreurs
                                    result["espece"] = JsonPrimitive("0") // Par défaut CHIEN (ID 0)
                                }
                            }
                        } else {
                            // Si ce n'est pas un nombre, essayer de trouver par le label
                            try {
                                val especeEnum = Espece.getByLabel(especeStr)
                                if (especeEnum != null) {
                                    result["espece"] = JsonPrimitive(especeEnum.id.toString())
                                } else {
                                    // Si non trouvé par label, utiliser une valeur par défaut
                                    result["espece"] = JsonPrimitive("1") // Par défaut CHIEN (ID 1)
                                }
                            } catch (e: Exception) {
                                result["espece"] = JsonPrimitive("1") // Par défaut CHIEN (ID 1)
                            }
                        }
                    }
                }

                if (result.containsKey("race")) {
                    val raceValue = result["race"]
                    val especeValue = result["espece"]
                    if (raceValue is JsonPrimitive && especeValue is JsonPrimitive) {
                        val mapped =
                                RaceCodeMapper.resolveRaceCode(
                                        especeValue.content,
                                        raceValue.content
                                )
                        if (mapped != null) {
                            result["race"] = JsonPrimitive(mapped)
                        }
                    }
                }

                // Prétraiter récursivement tous les champs qui sont des objets ou des tableaux
                for ((key, value) in result.entries) {
                    result[key] = preprocessAnimalEspeces(value)
                }

                return JsonObject(result)
            }
            is kotlinx.serialization.json.JsonArray -> {
                // Prétraiter récursivement chaque élément du tableau
                return kotlinx.serialization.json.JsonArray(
                        jsonElement.map { preprocessAnimalEspeces(it) }
                )
            }
            else -> return jsonElement
        }
    }

    /**
     * Analyse et valide la structure d'un fichier JSON d'animaux
     * @param content Le contenu JSON à analyser
     * @return Un message de diagnostic
     */
    fun analyzeAnimalJsonStructure(content: String): String {
        if (content.isBlank()) {
            return "Erreur: Le contenu JSON est vide"
        }

        val diagnosticBuilder = StringBuilder()
        diagnosticBuilder.append("Analyse du fichier JSON d'animaux:\n")

        try {
            val jsonParser = Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            }

            // Vérifier si c'est un tableau ou un objet unique
            val jsonElement = jsonParser.parseToJsonElement(content)

            when (jsonElement) {
                is kotlinx.serialization.json.JsonArray -> {
                    diagnosticBuilder.append("Format: Tableau de ${jsonElement.size} éléments\n")

                    // Examiner le premier élément pour vérifier les champs
                    if (jsonElement.isNotEmpty()) {
                        val firstAnimal = jsonElement[0]
                        if (firstAnimal is JsonObject) {
                            diagnosticBuilder.append("Champs du premier animal:\n")
                            firstAnimal.keys.forEach { key ->
                                val value = firstAnimal[key]
                                diagnosticBuilder.append("- $key: ${value?.toString()?.take(50)}")
                                if (value.toString().length > 50) diagnosticBuilder.append("...")
                                diagnosticBuilder.append("\n")
                            }

                            // Vérifier spécifiquement le champ espece
                            if (firstAnimal.containsKey("espece")) {
                                val especeValue = firstAnimal["espece"]
                                diagnosticBuilder.append(
                                        "Type du champ espece: ${if (especeValue != null) especeValue::class.simpleName else "null"}\n"
                                )
                                diagnosticBuilder.append("Valeur du champ espece: $especeValue\n")
                            } else {
                                diagnosticBuilder.append("ATTENTION: Champ 'espece' manquant!\n")
                            }

                            // Vérifier les consultations
                            if (firstAnimal.containsKey("consultations")) {
                                val consultationsValue = firstAnimal["consultations"]
                                if (consultationsValue is kotlinx.serialization.json.JsonArray) {
                                    diagnosticBuilder.append(
                                            "Consultations: ${consultationsValue.size} trouvées\n"
                                    )
                                } else {
                                    diagnosticBuilder.append("Consultations: Format invalide\n")
                                }
                            } else if (firstAnimal.containsKey("list")) {
                                val listValue = firstAnimal["list"]
                                if (listValue is JsonObject &&
                                                listValue.containsKey("consultations")
                                ) {
                                    val consultationsInList = listValue["consultations"]
                                    if (consultationsInList is kotlinx.serialization.json.JsonArray
                                    ) {
                                        diagnosticBuilder.append(
                                                "Consultations (via list): ${consultationsInList.size} trouvées\n"
                                        )
                                    } else {
                                        diagnosticBuilder.append(
                                                "Consultations (via list): Format invalide\n"
                                        )
                                    }
                                } else {
                                    diagnosticBuilder.append(
                                            "list: Ne contient pas de consultations valides\n"
                                    )
                                }
                            } else {
                                diagnosticBuilder.append(
                                        "AVERTISSEMENT: Aucune consultation trouvée\n"
                                )
                            }
                        } else {
                            diagnosticBuilder.append(
                                    "ERREUR: Le premier élément n'est pas un objet JSON valide\n"
                            )
                        }
                    }
                }
                is JsonObject -> {
                    diagnosticBuilder.append("Format: Objet JSON unique (un seul animal)\n")
                    diagnosticBuilder.append("Champs de l'animal:\n")
                    jsonElement.keys.forEach { key ->
                        val value = jsonElement[key]
                        diagnosticBuilder.append("- $key: ${value?.toString()?.take(50)}")
                        if (value.toString().length > 50) diagnosticBuilder.append("...")
                        diagnosticBuilder.append("\n")
                    }

                    // Vérifier spécifiquement le champ espece
                    if (jsonElement.containsKey("espece")) {
                        val especeValue = jsonElement["espece"]
                        diagnosticBuilder.append(
                                "Type du champ espece: ${if (especeValue != null) especeValue::class.simpleName else "null"}\n"
                        )
                        diagnosticBuilder.append("Valeur du champ espece: $especeValue\n")
                    } else {
                        diagnosticBuilder.append("ATTENTION: Champ 'espece' manquant!\n")
                    }
                }
                else -> {
                    diagnosticBuilder.append(
                            "ERREUR: Format JSON non reconnu (ni tableau ni objet)\n"
                    )
                }
            }

            // Tentative de désérialisation pour détecter les problèmes
            try {
                val preprocessedJson = FoodJsonImporter.preprocessNutrientValues(jsonElement)
                val preprocessedWithEspeces = preprocessAnimalEspeces(preprocessedJson)
                val preprocessedContent = jsonParser.encodeToString(preprocessedWithEspeces)

                diagnosticBuilder.append("\nTest de désérialisation:\n")

                val animals =
                        try {
                            jsonParser.decodeFromString<List<AnimalEvJson>>(preprocessedContent)
                            diagnosticBuilder.append(
                                    "✓ Désérialisation comme liste d'animaux réussie\n"
                            )
                            true
                        } catch (e: Exception) {
                            diagnosticBuilder.append(
                                    "✗ Échec de désérialisation comme liste: ${e.message}\n"
                            )

                            try {
                                jsonParser.decodeFromString<AnimalEvJson>(preprocessedContent)
                                diagnosticBuilder.append(
                                        "✓ Désérialisation comme animal unique réussie\n"
                                )
                                true
                            } catch (e: Exception) {
                                diagnosticBuilder.append(
                                        "✗ Échec de désérialisation comme animal unique: ${e.message}\n"
                                )
                                false
                            }
                        }

                if (!animals) {
                    diagnosticBuilder.append("\nRECOMMANDATIONS:\n")
                    diagnosticBuilder.append("- Vérifier la structure du JSON\n")
                    diagnosticBuilder.append(
                            "- Assurez-vous que tous les champs obligatoires sont présents\n"
                    )
                    diagnosticBuilder.append("- Vérifiez le format des dates (YYYY-MM-DD)\n")
                    diagnosticBuilder.append(
                            "- Vérifiez que les valeurs numériques sont correctes\n"
                    )
                }
            } catch (e: Exception) {
                diagnosticBuilder.append("\nERREUR lors du prétraitement: ${e.message}\n")
            }
        } catch (e: Exception) {
            diagnosticBuilder.append("ERREUR lors de l'analyse JSON: ${e.message}\n")
        }

        return diagnosticBuilder.toString()
    }

}
