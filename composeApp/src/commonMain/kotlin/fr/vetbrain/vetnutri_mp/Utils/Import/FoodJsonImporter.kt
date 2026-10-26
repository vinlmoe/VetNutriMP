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

/** Importation JSON des aliments et prétraitement du JSON (extrait de ImportUtils). */
object FoodJsonImporter {
    /**
     * Importe une liste d'aliments à partir d'une chaîne JSON
     *
     * @param jsonContent Le contenu JSON à désérialiser
     * @return La liste des aliments importés
     */
    fun importFoodsFromJson(jsonContent: String): List<AlimentEvJson> {

        // Vérifier si le contenu est un JSON valide
        if (!ImportCommun.isValidJson(jsonContent)) {
            return emptyList()
        }

        // Vérifier si le contenu correspond à un fichier d'aliments
        if (ImportCommun.isAnimalJsonContent(jsonContent)) {
            return emptyList()
        }

        // Structure pour collecter des informations détaillées sur les erreurs
        val importErrors = mutableListOf<String>()

        // Collecter les nutriments non résolus
        val nonResolvedNutrients = mutableMapOf<String, Int>()

        try {
            // Diagnostic : Examiner les données d'espèces dans le JSON brut
            val rawJson = ImportCommun.json.parseToJsonElement(jsonContent)
            if (rawJson is kotlinx.serialization.json.JsonArray) {

                // Examiner jusqu'à 5 éléments
                val samplesToCheck = minOf(5, rawJson.size)
                for (i in 0 until samplesToCheck) {
                    val item = rawJson[i]
                    if (item is JsonObject) {
                        val nom = item["nom"]?.toString() ?: "inconnu"
                        val especeVal = item["espece"]
                        val especesVal = item["Especes"]

                        // Vérifier si Especes est un tableau vide dans le JSON
                        if (especesVal is kotlinx.serialization.json.JsonArray &&
                                        especesVal.isEmpty()
                        ) {}

                        // Examiner les nutriments
                        if (item.containsKey("valMap")) {
                            val valMap = item["valMap"]
                            if (valMap is JsonObject) {
                                // Collecter les clés de nutriments
                                for (key in valMap.keys) {
                                    // On stockera ces clés pour plus tard vérifier lesquelles n'ont
                                    // pas été résolues
                                    val nutrient =
                                            fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver
                                                    .AllNutrientResolver(key)
                                    if (nutrient == null) {
                                        nonResolvedNutrients[key] =
                                                (nonResolvedNutrients[key] ?: 0) + 1
                                        importErrors.add(
                                                "Nutriment non résolu dans l'aliment '$nom': $key"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tenter de traiter directement le JSON en cas de format complexe
            val jsonElement = ImportCommun.json.parseToJsonElement(jsonContent)
            val preprocessedJson = preprocessEspecesAndNutrientValues(jsonElement)
            val preprocessedJsonString =
                    ImportCommun.json.encodeToString(JsonElement.serializer(), preprocessedJson)

            try {
                val foods = ImportCommun.json.decodeFromString<List<AlimentEvJson>>(preprocessedJsonString)

                // Afficher des informations de débogage sur les espèces
                foods.forEach { food ->

                    // Vérifier les nutriments dans chaque aliment
                    food.valMap.forEach { (_, nutrientQuantity) ->
                        val nutrientKey = nutrientQuantity.nut
                        val nutrient =
                                fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(
                                        nutrientKey
                                )
                        if (nutrient == null) {
                            nonResolvedNutrients[nutrientKey] =
                                    (nonResolvedNutrients[nutrientKey] ?: 0) + 1
                            importErrors.add(
                                    "Nutriment non résolu dans l'aliment '${food.nom}': $nutrientKey"
                            )
                        }
                    }
                }

                // Afficher les nutriments non résolus
                if (nonResolvedNutrients.isNotEmpty()) {

                    // Trier et compter par occurrence
                    val countByNutrient =
                            nonResolvedNutrients.toList().sortedByDescending { it.second }

                    // Afficher par ordre de fréquence
                    countByNutrient.forEach { (nutrient, count) ->
                        
                    }
                } else {}

                // Stocker les erreurs pour consultation ultérieure
                if (importErrors.isNotEmpty()) {
                    saveImportErrors(importErrors, nonResolvedNutrients)
                }

                return foods
            } catch (e: Exception) {
                importErrors.add("Erreur générale lors de l'importation: ${e.message}")

                try {
                    val food = ImportCommun.json.decodeFromString<AlimentEvJson>(preprocessedJsonString)

                    // Vérifier les nutriments dans l'aliment unique
                    food.valMap.forEach { (_, nutrientQuantity) ->
                        val nutrientKey = nutrientQuantity.nut
                        val nutrient =
                                fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(
                                        nutrientKey
                                )
                        if (nutrient == null) {
                            nonResolvedNutrients[nutrientKey] =
                                    (nonResolvedNutrients[nutrientKey] ?: 0) + 1
                            importErrors.add(
                                    "Nutriment non résolu dans l'aliment '${food.nom}': $nutrientKey"
                            )
                        }
                    }

                    // Afficher les nutriments non résolus
                    if (nonResolvedNutrients.isNotEmpty()) {
                        nonResolvedNutrients.entries.sortedByDescending { it.value }.forEach {
                                (nutrientKey, count) ->
                            
                        }
                    }

                    // Stocker les erreurs pour consultation ultérieure
                    if (importErrors.isNotEmpty()) {
                        saveImportErrors(importErrors, nonResolvedNutrients)
                    }

                    return listOf(food)
                } catch (e: Exception) {
                    importErrors.add(
                            "Erreur lors de l'importation comme aliment unique: ${e.message}"
                    )
                }
            }
        } catch (e: Exception) {
            importErrors.add("Erreur lors du prétraitement du JSON: ${e.message}")

            // Fallback à la méthode standard
            try {
                val foods = ImportCommun.json.decodeFromString<List<AlimentEvJson>>(jsonContent)

                // Vérifier les nutriments dans chaque aliment
                foods.forEach { food ->
                    food.valMap.forEach { (_, nutrientQuantity) ->
                        val nutrientKey = nutrientQuantity.nut
                        val nutrient =
                                fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(
                                        nutrientKey
                                )
                        if (nutrient == null) {
                            nonResolvedNutrients[nutrientKey] =
                                    (nonResolvedNutrients[nutrientKey] ?: 0) + 1
                            importErrors.add(
                                    "Nutriment non résolu dans l'aliment '${food.nom}': $nutrientKey"
                            )
                        }
                    }
                }

                // Afficher les nutriments non résolus
                if (nonResolvedNutrients.isNotEmpty()) {
                    
                    nonResolvedNutrients.entries.sortedByDescending { it.value }.forEach {
                            (nutrientKey, count) ->
                        
                    }
                }

                // Stocker les erreurs pour consultation ultérieure
                if (importErrors.isNotEmpty()) {
                    saveImportErrors(importErrors, nonResolvedNutrients)
                }

                return foods
            } catch (e: Exception) {
                importErrors.add("Erreur lors de l'importation standard comme liste: ${e.message}")

                try {
                    val food = ImportCommun.json.decodeFromString<AlimentEvJson>(jsonContent)

                    // Vérifier les nutriments dans l'aliment unique
                    food.valMap.forEach { (_, nutrientQuantity) ->
                        val nutrientKey = nutrientQuantity.nut
                        val nutrient =
                                fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(
                                        nutrientKey
                                )
                        if (nutrient == null) {
                            nonResolvedNutrients[nutrientKey] =
                                    (nonResolvedNutrients[nutrientKey] ?: 0) + 1
                            importErrors.add(
                                    "Nutriment non résolu dans l'aliment '${food.nom}': $nutrientKey"
                            )
                        }
                    }

                    // Afficher les nutriments non résolus
                    if (nonResolvedNutrients.isNotEmpty()) {
                        nonResolvedNutrients.entries.sortedByDescending { it.value }.forEach {
                                (nutrientKey, count) ->
                            
                        }
                    }

                    // Stocker les erreurs pour consultation ultérieure
                    if (importErrors.isNotEmpty()) {
                        saveImportErrors(importErrors, nonResolvedNutrients)
                    }

                    return listOf(food)
                } catch (e: Exception) {
                    importErrors.add(
                            "Erreur lors de l'importation standard comme un seul aliment: ${e.message}"
                    )
                }
            }
        }

        // Même en cas d'échec, afficher les nutriments non résolus collectés lors de l'analyse
        if (nonResolvedNutrients.isNotEmpty()) {
            
            nonResolvedNutrients.entries.sortedByDescending { it.value }.forEach {
                    (nutrientKey, count) ->
                
            }
        }

        // Stocker les erreurs pour consultation ultérieure
        if (importErrors.isNotEmpty()) {
            saveImportErrors(importErrors, nonResolvedNutrients)
        }

        return emptyList()
    }

    /** Sauvegarde les erreurs d'importation dans un rapport. */
    private fun saveImportErrors(
            importErrors: List<String>,
            unresolvedNutrients: Map<String, Int>
    ) {
        if (importErrors.isEmpty() && unresolvedNutrients.isEmpty()) {
            return
        }

        try {
            val errorReport = StringBuilder("=== RAPPORT D'ERREURS D'IMPORTATION ===\n")

            // Ajouter les erreurs d'importation
            if (importErrors.isNotEmpty()) {
                errorReport.append("ERREURS D'IMPORTATION (${importErrors.size}):\n")

                // Regrouper les erreurs similaires
                val groupedErrors = importErrors.groupBy { it }.mapValues { it.value.size }

                // Trier par fréquence d'occurrence
                val sortedErrors = groupedErrors.entries.sortedByDescending { it.value }

                sortedErrors.forEach { (error, count) ->
                    if (count > 1) {
                        errorReport.append("[$count occurrences] $error\n")
                    } else {
                        errorReport.append("$error\n")
                    }
                }
                errorReport.append("\n")
            }

            // Ajouter les nutriments non résolus
            if (unresolvedNutrients.isNotEmpty()) {
                errorReport.append(
                        "NUTRIMENTS NON RÉSOLUS (${unresolvedNutrients.values.sum()} occurrences):\n"
                )

                // Trier par fréquence d'occurrence
                val sortedNutrients = unresolvedNutrients.entries.sortedByDescending { it.value }

                sortedNutrients.forEach { (nutrient, count) ->
                    errorReport.append("[$count occurrences] $nutrient\n")
                }
            }

            // Au lieu d'écrire dans un fichier, afficher les détails dans la console
            

            // Note: L'écriture dans un fichier a été supprimée car elle n'est pas compatible
            // multiplateforme
            // Pour implémenter cette fonctionnalité, il faudrait utiliser expect/actual ou une
            // bibliothèque multiplateforme d'I/O
        } catch (e: Exception) {}
    }

    /**
     * Normalise les labels des nutriments pour l'importation
     * @param label Le label à normaliser
     * @return Le label normalisé
     */
    private fun normalizeNutrientLabel(label: String): String {
        val trimmed = label.trim().replace("[", "").replace("]", "").replace("\"", "")

        // Normalisation simple: majuscules et suppression des caractères spéciaux
        return trimmed.uppercase()
                .replace("_", "")
                .replace("-", "")
                .replace(" ", "")
                .replace(".", "")
    }

    /** Prétraite les valeurs nutritionnelles et les espèces dans le JSON */
    private fun preprocessEspecesAndNutrientValues(jsonElement: JsonElement): JsonElement {
        when (jsonElement) {
            is JsonObject -> {
                val result = jsonElement.toMutableMap()

                // Traitement spécial pour les objets nommés "purison" ou contenant ce nom
                val nom = result["nom"]
                if (nom is JsonPrimitive && nom.content.contains("purison", ignoreCase = true)) {}

                // Si on trouve un champ "valMap", traiter son contenu
                if (result.containsKey("valMap")) {
                    val valMap = result["valMap"]

                    // Détecter si valMap est null ou vide
                    if (valMap == null || (valMap is JsonObject && valMap.isEmpty())) {

                        // Créer un valMap par défaut avec des nutriments essentiels
                        val defaultValMap = mutableMapOf<String, JsonElement>()
                        ensureEssentialNutrients(defaultValMap)
                        result["valMap"] = JsonObject(defaultValMap)
                    } else if (valMap is JsonObject) {
                        val newValMap = mutableMapOf<String, JsonElement>()

                        // Parcourir les entrées de valMap
                        for ((key, value) in valMap.entries) {
                            // Tentative de normalisation du label du nutriment
                            val normalizedKey = normalizeNutrientLabel(key)

                            try {
                                // Vérifier les différents formats possibles
                                when {
                                    // Format 1: Déjà un objet NutrientQuantity avec valeur et nut
                                    value is JsonObject &&
                                            value.contains("value") &&
                                            value.contains("nut") -> {
                                        // On vérifie si on peut résoudre ce nutriment
                                        val nutrientKey =
                                                value["nut"]?.let {
                                                    if (it is JsonPrimitive) it.content else null
                                                }
                                        if (nutrientKey != null) {
                                            val normalizedNutrientKey =
                                                    normalizeNutrientLabel(nutrientKey)
                                            if (normalizedNutrientKey != nutrientKey) {
                                                // Si la clé a été normalisée, créer un nouvel objet
                                                // avec la clé normalisée
                                                val nutritionValue =
                                                        JsonObject(
                                                                mapOf(
                                                                        "value" to value["value"]!!,
                                                                        "nut" to
                                                                                JsonPrimitive(
                                                                                        normalizedNutrientKey
                                                                                )
                                                                )
                                                        )
                                                newValMap[normalizedKey] = nutritionValue
                                            } else {
                                                // Sinon, conserver l'objet tel quel avec la clé
                                                // normalisée
                                                newValMap[normalizedKey] = value
                                            }
                                        } else {
                                            // Si pas de clé de nutriment, utiliser la clé
                                            // normalisée
                                            val nutritionValue =
                                                    JsonObject(
                                                            mapOf(
                                                                    "value" to value["value"]!!,
                                                                    "nut" to
                                                                            JsonPrimitive(
                                                                                    normalizedKey
                                                                            )
                                                            )
                                                    )
                                            newValMap[normalizedKey] = nutritionValue
                                        }
                                    }

                                    // Format 2: Objet avec seulement "value"
                                    value is JsonObject && value.contains("value") -> {
                                        // Créer un nouvel objet avec "nut" qui reprend la clé
                                        // normalisée
                                        val nutritionValue =
                                                JsonObject(
                                                        mapOf(
                                                                "value" to value["value"]!!,
                                                                "nut" to
                                                                        JsonPrimitive(normalizedKey)
                                                        )
                                                )
                                        newValMap[normalizedKey] = nutritionValue
                                    }

                                    // Format 3: Valeur numérique directe (chaîne qui peut être
                                    // convertie en nombre)
                                    value is JsonPrimitive &&
                                            value.isString &&
                                            value.content.toDoubleOrNull() != null -> {
                                        // Convertir en objet NutrientQuantity
                                        val nutritionValue =
                                                JsonObject(
                                                        mapOf(
                                                                "value" to
                                                                        JsonPrimitive(
                                                                                value.content
                                                                                        .toDouble()
                                                                        ),
                                                                "nut" to
                                                                        JsonPrimitive(normalizedKey)
                                                        )
                                                )
                                        newValMap[normalizedKey] = nutritionValue
                                    }

                                    // Format 4: Valeur numérique directe
                                    value is JsonPrimitive &&
                                            value.content.toDoubleOrNull() != null -> {
                                        // Convertir en objet NutrientQuantity
                                        val doubleValue = value.content.toDoubleOrNull() ?: 0.0

                                        val nutritionValue =
                                                JsonObject(
                                                        mapOf(
                                                                "value" to
                                                                        JsonPrimitive(doubleValue),
                                                                "nut" to
                                                                        JsonPrimitive(normalizedKey)
                                                        )
                                                )
                                        newValMap[normalizedKey] = nutritionValue
                                    }

                                    // Autres cas - format spécial pouvant inclure des structures
                                    // JSON imbriquées
                                    else -> {

                                        // Analyse plus poussée pour extraire des données imbriquées
                                        if (value is JsonObject) {
                                            // Essayer plusieurs chemins possibles pour trouver la
                                            // valeur
                                            val possibleValuePaths =
                                                    listOf("value", "val", "v", "valeur")
                                            for (path in possibleValuePaths) {
                                                if (value.containsKey(path) &&
                                                                value[path] is JsonPrimitive
                                                ) {
                                                    val extractedValue =
                                                            (value[path] as JsonPrimitive).content
                                                                    .toDoubleOrNull()
                                                    if (extractedValue != null) {
                                                        val nutritionValue =
                                                                JsonObject(
                                                                        mapOf(
                                                                                "value" to
                                                                                        JsonPrimitive(
                                                                                                extractedValue
                                                                                        ),
                                                                                "nut" to
                                                                                        JsonPrimitive(
                                                                                                normalizedKey
                                                                                        )
                                                                        )
                                                                )
                                                        newValMap[normalizedKey] = nutritionValue
                                                        break
                                                    }
                                                }
                                            }
                                        }

                                        // Si aucune extraction n'a réussi, essayer une approche
                                        // plus agressive
                                        if (!newValMap.containsKey(normalizedKey)) {
                                            try {
                                                // Tenter d'extraire une valeur numérique de la
                                                // représentation JSON
                                                val jsonString = value.toString()
                                                val numberPattern =
                                                        Regex(
                                                                """[-+]?[0-9]*\.?[0-9]+(?:[eE][-+]?[0-9]+)?"""
                                                        )
                                                val match = numberPattern.find(jsonString)
                                                if (match != null) {
                                                    val extractedNumber =
                                                            match.value.toDoubleOrNull()
                                                    if (extractedNumber != null) {
                                                        val nutritionValue =
                                                                JsonObject(
                                                                        mapOf(
                                                                                "value" to
                                                                                        JsonPrimitive(
                                                                                                extractedNumber
                                                                                        ),
                                                                                "nut" to
                                                                                        JsonPrimitive(
                                                                                                normalizedKey
                                                                                        )
                                                                        )
                                                                )
                                                        newValMap[normalizedKey] = nutritionValue
                                                    } else {
                                                        // Si la conversion échoue, enregistrer la
                                                        // valeur originale
                                                        newValMap[normalizedKey] = value
                                                    }
                                                } else {
                                                    // Conserver tel quel si aucun nombre n'est
                                                    // trouvé
                                                    newValMap[normalizedKey] = value
                                                }
                                            } catch (e: Exception) {
                                                // Conserver tel quel en cas d'erreur
                                                newValMap[normalizedKey] = value
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        // Remplacer valMap par sa version prétraitée
                        result["valMap"] = JsonObject(newValMap)

                        // Vérifier si les nutriments essentiels sont présents, sinon les ajouter
                        // avec des valeurs par défaut
                        ensureEssentialNutrients(newValMap)
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
                                } catch (e: Exception) {
                                    // Fallback à l'ancienne méthode si la conversion échoue
                                    result["Especes"] =
                                            kotlinx.serialization.json.JsonArray(
                                                    listOf(JsonPrimitive(especeValue.toString()))
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
                                    } catch (e: Exception) {
                                        // Conserver la valeur originale si la conversion échoue
                                        newEspecesList.add(especeItem)
                                    }
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
     * Vérifie si un nutriment est essentiel et devrait être présent dans tous les aliments
     * @param nutrientKey La clé du nutriment à vérifier
     * @return true si le nutriment est essentiel, false sinon
     */
    private fun isEssentialNutrient(nutrientKey: String): Boolean {
        val essentialNutrients =
                setOf(
                        "PROTEINE",
                        "LIPIDE",
                        "HUMIDITE",
                        "CENDRE",
                        "ENA",
                        "CELLULOSE",
                        "AMIDON",
                        "SUCRE"
                )
        return essentialNutrients.contains(nutrientKey)
    }

    /**
     * S'assure que les nutriments essentiels sont présents dans la valMap
     * @param valMap La map des valeurs nutritionnelles à compléter
     */
    private fun ensureEssentialNutrients(valMap: MutableMap<String, JsonElement>) {
        val essentialNutrients =
                mapOf(
                        "PROTEINE" to 0.0,
                        "LIPIDE" to 0.0,
                        "HUMIDITE" to 0.0,
                        "CENDRE" to 0.0,
                        "ENA" to 0.0,
                        "CELLULOSE" to 0.0
                )

        // Pour chaque nutriment essentiel
        for ((nutrient, defaultValue) in essentialNutrients) {
            // Si le nutriment n'est pas présent, l'ajouter avec sa valeur par défaut
            if (!valMap.containsKey(nutrient)) {
                val nutritionValue =
                        JsonObject(
                                mapOf(
                                        "value" to JsonPrimitive(defaultValue),
                                        "nut" to JsonPrimitive(nutrient)
                                )
                        )
                valMap[nutrient] = nutritionValue
            }
        }

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

    /**
     * Prétraite les valeurs nutritionnelles dans le JSON pour convertir les objets complexes en
     * valeurs simples
     *
     * @param jsonElement L'élément JSON à prétraiter
     * @return L'élément JSON prétraité
     */
    internal fun preprocessNutrientValues(jsonElement: JsonElement): JsonElement {
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
     * Extrait les sections foods et references d'un fichier vetnutri_export_init.json
     * @param jsonContent Le contenu JSON du fichier
     * @return Pair contenant la liste des aliments et la liste des références
     */
    fun extractSectionsFromVetNutriInit(jsonContent: String): Pair<List<AlimentEvJson>, String> {
        return try {
            val jsonElement = ImportCommun.json.parseToJsonElement(jsonContent)
            if (jsonElement is JsonObject) {
                val foods =
                        jsonElement["foods"]?.let { foodsElement ->
                            when (foodsElement) {
                                is kotlinx.serialization.json.JsonArray -> {
                                    // Utiliser le système de mapping existant avec
                                    // preprocessEspecesAndNutrientValues
                                    val foodsJsonString = foodsElement.toString()
                                    importFoodsFromJson(foodsJsonString)
                                }
                                else -> emptyList()
                            }
                        }
                                ?: emptyList()

                val referencesJson = jsonElement["references"]?.toString() ?: ""

                Pair(foods, referencesJson)
            } else {
                Pair(emptyList(), "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(emptyList(), "")
        }
    }

}
