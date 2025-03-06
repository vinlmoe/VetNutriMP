package fr.vetbrain.vetnutri_mp.Utils

import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Data.AnimalEvJson
import fr.vetbrain.vetnutri_mp.Enumer.Espece
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
        prettyPrint = false
        explicitNulls = false
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
            // Étape 1: Valider le format JSON de base
            if (content.isBlank()) {
                println("Erreur: Le contenu JSON est vide")
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

            println("Analyse de la structure JSON...")
            val originalJson = jsonParser.parseToJsonElement(content)

            println("Prétraitement des valeurs nutritionnelles...")
            val preprocessedJson = preprocessNutrientValues(originalJson)

            println("Prétraitement des espèces des animaux...")
            val preprocessedWithEspeces = preprocessAnimalEspeces(preprocessedJson)
            val preprocessedContent = jsonParser.encodeToString(preprocessedWithEspeces)

            println("Tentative d'importation comme liste d'animaux...")
            val animals =
                    try {
                        val result =
                                jsonParser.decodeFromString<List<AnimalEvJson>>(preprocessedContent)
                        println("Importation réussie comme liste: ${result.size} animaux")
                        result
                    } catch (e: Exception) {
                        println("Erreur lors de l'importation comme liste: ${e.message}")
                        println("Cause: ${e.cause}")

                        // Essayer d'importer comme un seul animal
                        println("Tentative d'importation comme un seul animal...")
                        try {
                            val animal =
                                    jsonParser.decodeFromString<AnimalEvJson>(preprocessedContent)
                            println("Importation réussie comme animal unique")
                            listOf(animal)
                        } catch (e: Exception) {
                            println(
                                    "Erreur lors de l'importation comme animal unique: ${e.message}"
                            )
                            println("Cause: ${e.cause}")
                            e.printStackTrace()
                            emptyList()
                        }
                    }

            println("Importation terminée: ${animals.size} animaux trouvés")

            // Si aucun animal n'a été importé, retourner un résultat vide
            if (animals.isEmpty()) {
                println("Aucun animal importé")
                return ImportResult(emptyList(), emptyList())
            }

            // Extraire tous les aliments des rations
            val allFoods = mutableSetOf<AlimentEvJson>()

            println("Extraction des aliments depuis les rations...")
            animals.forEach { animal ->
                println("Traitement de l'animal: ${animal.nom}")

                // Vérifier les consultations directes
                animal.consultations?.forEach { consultation ->
                    println("Traitement de la consultation du ${consultation.date}")
                    // Vérifier les rations dans la consultation
                    consultation.rationList.forEach { (_, ration) ->
                        println("Traitement de la ration: ${ration.Nom}")
                        ration.alimentList.forEach { alimentRation ->
                            println("Ajout de l'aliment: ${alimentRation.alime.nom}")
                            alimentRation.alime.let { aliment -> allFoods.add(aliment) }
                        }
                    }
                }

                // Vérifier également les consultations dans list
                animal.list?.consultations?.forEach { consultation ->
                    println("Traitement de la consultation (via list) du ${consultation.date}")
                    consultation.rationList.forEach { (_, ration) ->
                        println("Traitement de la ration: ${ration.Nom}")
                        ration.alimentList.forEach { alimentRation ->
                            println("Ajout de l'aliment: ${alimentRation.alime.nom}")
                            alimentRation.alime.let { aliment -> allFoods.add(aliment) }
                        }
                    }
                }
            }

            println("Extraction terminée: ${allFoods.size} aliments extraits des rations")

            return ImportResult(animals, allFoods.toList())
        } catch (e: Exception) {
            println("Erreur critique lors de l'importation JSON: ${e.message}")
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
            // Diagnostic : Examiner les données d'espèces dans le JSON brut
            val rawJson = json.parseToJsonElement(jsonContent)
            if (rawJson is kotlinx.serialization.json.JsonArray) {
                println("Format JSON : tableau de ${rawJson.size} éléments")

                // Examiner jusqu'à 5 éléments
                val samplesToCheck = minOf(5, rawJson.size)
                for (i in 0 until samplesToCheck) {
                    val item = rawJson[i]
                    if (item is JsonObject) {
                        val nom = item["nom"]?.toString() ?: "inconnu"
                        val especeVal = item["espece"]
                        val especesVal = item["Especes"]

                        println("Aliment brut $nom - espece: $especeVal, Especes: $especesVal")

                        // Vérifier si Especes est un tableau vide dans le JSON
                        if (especesVal is kotlinx.serialization.json.JsonArray &&
                                        especesVal.isEmpty()
                        ) {
                            println("  => Especes est un tableau vide pour $nom")
                        }
                    }
                }
            }

            // Tenter de traiter directement le JSON en cas de format complexe
            val jsonElement = json.parseToJsonElement(jsonContent)
            val preprocessedJson = preprocessEspecesAndNutrientValues(jsonElement)
            val preprocessedJsonString =
                    json.encodeToString(JsonElement.serializer(), preprocessedJson)

            try {
                println("Tentative d'importation comme liste d'aliments...")
                val foods = json.decodeFromString<List<AlimentEvJson>>(preprocessedJsonString)
                println("Importation réussie: ${foods.size} aliments trouvés")

                // Afficher des informations de débogage sur les espèces
                foods.forEach { food ->
                    println(
                            "Aliment: ${food.nom}, Especes: ${food.Especes}, espece: ${food.espece}"
                    )
                }

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

    /** Prétraite les valeurs nutritionnelles et les espèces dans le JSON */
    private fun preprocessEspecesAndNutrientValues(jsonElement: JsonElement): JsonElement {
        when (jsonElement) {
            is JsonObject -> {
                val result = jsonElement.toMutableMap()

                // Si on trouve un champ "valMap", traiter son contenu
                if (result.containsKey("valMap")) {
                    val valMap = result["valMap"]
                    if (valMap is JsonObject) {
                        val newValMap = mutableMapOf<String, JsonElement>()

                        // Parcourir les entrées de valMap
                        for ((key, value) in valMap.entries) {
                            // Vérifier les différents formats possibles
                            when {
                                // Format 1: Déjà un objet NutrientQuantity
                                value is JsonObject &&
                                        value.contains("value") &&
                                        value.contains("nut") -> {
                                    // Déjà dans le bon format, conserver tel quel
                                    newValMap[key] = value
                                }

                                // Format 2: Objet avec seulement "value"
                                value is JsonObject && value.contains("value") -> {
                                    // Créer un nouvel objet avec "nut" qui reprend la clé
                                    val nutritionValue =
                                            JsonObject(
                                                    mapOf(
                                                            "value" to value["value"]!!,
                                                            "nut" to JsonPrimitive(key)
                                                    )
                                            )
                                    newValMap[key] = nutritionValue
                                }

                                // Format 3: Valeur numérique directe
                                value is JsonPrimitive &&
                                        value.isString &&
                                        value.content.toFloatOrNull() != null -> {
                                    // Convertir en objet NutrientQuantity
                                    val nutritionValue =
                                            JsonObject(
                                                    mapOf(
                                                            "value" to
                                                                    JsonPrimitive(
                                                                            value.content.toFloat()
                                                                    ),
                                                            "nut" to JsonPrimitive(key)
                                                    )
                                            )
                                    newValMap[key] = nutritionValue
                                }
                                value is JsonPrimitive &&
                                        (value.isString ||
                                                value.content.toFloatOrNull() != null) -> {
                                    // Convertir en objet NutrientQuantity
                                    val floatValue =
                                            if (value.isString) {
                                                value.content.toFloatOrNull() ?: 0f
                                            } else {
                                                value.content.toFloatOrNull() ?: 0f
                                            }

                                    val nutritionValue =
                                            JsonObject(
                                                    mapOf(
                                                            "value" to JsonPrimitive(floatValue),
                                                            "nut" to JsonPrimitive(key)
                                                    )
                                            )
                                    newValMap[key] = nutritionValue
                                }

                                // Autres cas
                                else -> {
                                    println(
                                            "Format non reconnu pour la valeur nutritionnelle $key: $value"
                                    )
                                    // Essayer de convertir en flottant si possible
                                    val floatValue = value.toString().toFloatOrNull()
                                    if (floatValue != null) {
                                        val nutritionValue =
                                                JsonObject(
                                                        mapOf(
                                                                "value" to
                                                                        JsonPrimitive(floatValue),
                                                                "nut" to JsonPrimitive(key)
                                                        )
                                                )
                                        newValMap[key] = nutritionValue
                                    } else {
                                        // Conserver tel quel si la conversion échoue
                                        newValMap[key] = value
                                    }
                                }
                            }
                        }

                        // Remplacer valMap par sa version prétraitée
                        result["valMap"] = JsonObject(newValMap)
                    }
                }

                // Vérifier et traiter les espèces
                if (result.containsKey("Especes")) {
                    val especes = result["Especes"]
                    // Si Especes est un tableau vide et espece est présent avec une valeur > 0,
                    // convertir espece en élément de Especes
                    if (especes is kotlinx.serialization.json.JsonArray &&
                                    especes.isEmpty() &&
                                    result.containsKey("espece")
                    ) {
                        val espece = result["espece"]
                        if (espece is JsonPrimitive) {
                            val especeValue = espece.toString().trim('"').toIntOrNull()
                            if (especeValue != null && especeValue > 0) {
                                try {
                                    // Tenter de convertir l'ID numérique en énumération Espece
                                    val especeEnum = Espece.getEnumFromInt(especeValue)
                                    // Utiliser le label de l'énumération comme valeur pour Especes
                                    result["Especes"] =
                                            kotlinx.serialization.json.JsonArray(
                                                    listOf(JsonPrimitive(especeEnum.label))
                                            )
                                    println(
                                            "Prétraitement: Conversion de l'espèce $especeValue en ${especeEnum.label}"
                                    )
                                } catch (e: Exception) {
                                    // Fallback à l'ancienne méthode si la conversion échoue
                                    result["Especes"] =
                                            kotlinx.serialization.json.JsonArray(
                                                    listOf(JsonPrimitive(especeValue.toString()))
                                            )
                                    println(
                                            "Prétraitement: Fallback - Ajout de l'espèce $especeValue à Especes pour l'aliment ${result["nom"]}"
                                    )
                                }
                            }
                        }
                    } else if (especes is kotlinx.serialization.json.JsonArray && !especes.isEmpty()
                    ) {
                        // Convertir les identifiants numériques en labels d'énumération Espece si
                        // possible
                        val newEspecesList = mutableListOf<JsonPrimitive>()
                        for (especeItem in especes) {
                            if (especeItem is JsonPrimitive) {
                                val especeStr = especeItem.toString().trim('"')
                                val especeValue = especeStr.toIntOrNull()
                                if (especeValue != null) {
                                    try {
                                        // Tenter de convertir l'ID en énumération Espece
                                        val especeEnum = Espece.getEnumFromInt(especeValue)
                                        newEspecesList.add(JsonPrimitive(especeEnum.label))
                                        println(
                                                "Prétraitement: Conversion de l'espèce $especeValue en ${especeEnum.label}"
                                        )
                                    } catch (e: Exception) {
                                        // Conserver la valeur originale si la conversion échoue
                                        newEspecesList.add(especeItem)
                                    }
                                } else {
                                    // Si ce n'est pas un nombre, conserver tel quel
                                    newEspecesList.add(especeItem)
                                }
                            }
                        }
                        if (newEspecesList.isNotEmpty()) {
                            result["Especes"] = kotlinx.serialization.json.JsonArray(newEspecesList)
                        }
                    }
                }

                // Prétraiter récursivement tous les champs qui sont des objets ou des tableaux
                for ((key, value) in result.entries) {
                    result[key] = preprocessEspecesAndNutrientValues(value)
                }

                return JsonObject(result)
            }
            is kotlinx.serialization.json.JsonArray -> {
                // Prétraiter récursivement chaque élément du tableau
                return kotlinx.serialization.json.JsonArray(
                        jsonElement.map { preprocessEspecesAndNutrientValues(it) }
                )
            }
            else -> return jsonElement
        }
    }

    /**
     * Prétraite les valeurs nutritionnelles dans le JSON pour convertir les objets complexes en
     * valeurs simples
     *
     * @param jsonElement L'élément JSON à prétraiter
     * @return L'élément JSON prétraité
     */
    private fun preprocessNutrientValues(jsonElement: JsonElement): JsonElement {
        // Renvoyer au prétraitement complet qui gère aussi les espèces
        return preprocessEspecesAndNutrientValues(jsonElement)
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
                                // Utiliser le label de l'énumération comme valeur pour espece
                                result["espece"] = JsonPrimitive(especeEnum.id.toString())
                                println(
                                        "Prétraitement animal: Conversion de l'espèce $especeValue en ${especeEnum.label} (id: ${especeEnum.id})"
                                )
                            } catch (e: Exception) {
                                // Conserver la valeur originale si la conversion échoue
                                println(
                                        "Prétraitement animal: Échec de conversion de l'espèce $especeValue: ${e.message}, utilisation de l'ID par défaut"
                                )
                                // En cas d'échec, définir une valeur par défaut pour éviter des
                                // erreurs
                                result["espece"] = JsonPrimitive("1") // Par défaut CHIEN (ID 1)
                            }
                        } else {
                            // Si ce n'est pas un nombre, essayer de trouver par le label
                            try {
                                val especeEnum = Espece.getByLabel(especeStr)
                                if (especeEnum != null) {
                                    result["espece"] = JsonPrimitive(especeEnum.id.toString())
                                    println(
                                            "Prétraitement animal: Conversion du label d'espèce $especeStr en ID ${especeEnum.id}"
                                    )
                                } else {
                                    // Si non trouvé par label, utiliser une valeur par défaut
                                    println(
                                            "Prétraitement animal: Espèce non reconnue '$especeStr', utilisation de l'ID par défaut"
                                    )
                                    result["espece"] = JsonPrimitive("1") // Par défaut CHIEN (ID 1)
                                }
                            } catch (e: Exception) {
                                println(
                                        "Prétraitement animal: Erreur lors de la conversion de l'espèce '$especeStr': ${e.message}"
                                )
                                result["espece"] = JsonPrimitive("1") // Par défaut CHIEN (ID 1)
                            }
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
                val preprocessedJson = preprocessNutrientValues(jsonElement)
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

    /**
     * Analyse les espèces des aliments dans un fichier JSON
     * @param content Le contenu JSON à analyser
     * @return Un rapport détaillé sur les espèces trouvées
     */
    fun analyzeEspecesInFoodJson(content: String): String {
        if (content.isBlank()) {
            return "Erreur: Le contenu JSON est vide"
        }

        val diagnosticBuilder = StringBuilder()
        diagnosticBuilder.append("Analyse des espèces des aliments:\n")

        try {
            val jsonParser = Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            }

            // Prétraiter le JSON
            val jsonElement = jsonParser.parseToJsonElement(content)
            val preprocessedJson = preprocessEspecesAndNutrientValues(jsonElement)
            val preprocessedContent =
                    jsonParser.encodeToString(JsonElement.serializer(), preprocessedJson)

            // Désérialiser les aliments
            val foods =
                    try {
                        jsonParser.decodeFromString<List<AlimentEvJson>>(preprocessedContent)
                    } catch (e: Exception) {
                        try {
                            listOf(jsonParser.decodeFromString<AlimentEvJson>(preprocessedContent))
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }

            if (foods.isEmpty()) {
                diagnosticBuilder.append("Aucun aliment trouvé dans le JSON\n")
                return diagnosticBuilder.toString()
            }

            diagnosticBuilder.append("Nombre d'aliments trouvés: ${foods.size}\n\n")

            // Analyser les espèces pour chaque aliment
            val alimWithEspeces = foods.filter { it.Especes.isNotEmpty() }
            val alimWithoutEspeces = foods.filter { it.Especes.isEmpty() }

            diagnosticBuilder.append("Aliments avec espèces: ${alimWithEspeces.size}\n")
            diagnosticBuilder.append("Aliments sans espèces: ${alimWithoutEspeces.size}\n\n")

            // Statistiques sur les espèces
            val allEspecesLabels = mutableMapOf<String, Int>()

            alimWithEspeces.forEach { food ->
                food.Especes.forEach { espece ->
                    allEspecesLabels[espece] = (allEspecesLabels[espece] ?: 0) + 1
                }
            }

            if (allEspecesLabels.isNotEmpty()) {
                diagnosticBuilder.append("Distribution des espèces:\n")
                allEspecesLabels.entries.sortedByDescending { it.value }.forEach { (label, count) ->
                    val especeEnum =
                            try {
                                // Essayer de trouver l'énumération correspondante
                                val espece = Espece.getByLabel(label) ?: Espece.valueOf(label)
                                "(ID: ${espece.id}, catégorie: ${espece.categorie})"
                            } catch (e: Exception) {
                                "(non reconnue dans l'énumération)"
                            }

                    diagnosticBuilder.append("- $label $especeEnum: $count aliments\n")
                }
            }

            // Détails des aliments sans espèces
            if (alimWithoutEspeces.isNotEmpty()) {
                diagnosticBuilder.append("\nDétails des aliments sans espèces spécifiées:\n")
                alimWithoutEspeces.take(10).forEach { food ->
                    diagnosticBuilder.append(
                            "- ${food.nom} (ID: ${food.UUID}, espece: ${food.espece})\n"
                    )
                }

                if (alimWithoutEspeces.size > 10) {
                    diagnosticBuilder.append("  ... et ${alimWithoutEspeces.size - 10} autres\n")
                }
            }

            // Détails de quelques aliments avec espèces
            if (alimWithEspeces.isNotEmpty()) {
                diagnosticBuilder.append("\nExemples d'aliments avec espèces spécifiées:\n")
                alimWithEspeces.take(10).forEach { food ->
                    diagnosticBuilder.append("- ${food.nom} (ID: ${food.UUID})\n")
                    diagnosticBuilder.append("  Espèces: ${food.Especes.joinToString(", ")}\n")
                    diagnosticBuilder.append("  Champ espece: ${food.espece}\n")
                }
            }
        } catch (e: Exception) {
            diagnosticBuilder.append("\nErreur lors de l'analyse: ${e.message}\n")
            e.printStackTrace()
        }

        return diagnosticBuilder.toString()
    }
}

/** Classe contenant le résultat de l'importation */
data class ImportResult(val animals: List<AnimalEvJson>, val foods: List<AlimentEvJson>)
