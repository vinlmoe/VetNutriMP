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

            // Vérifier si le contenu est un JSON valide
            if (!isValidJson(content)) {
                println("Erreur: Le contenu n'est pas un JSON valide")
                return ImportResult(emptyList(), emptyList())
            }

            // Vérifier si le contenu correspond à un fichier d'animaux
            if (!isAnimalJsonContent(content)) {
                println("Erreur: Le contenu JSON ne semble pas être un fichier d'animaux")
                println(
                        "Le fichier semble être un fichier d'aliments. Utilisez la commande import-food pour ce type de fichier."
                )
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

        // Vérifier si le contenu est un JSON valide
        if (!isValidJson(jsonContent)) {
            println("Erreur: Le contenu n'est pas un JSON valide")
            return emptyList()
        }

        // Vérifier si le contenu correspond à un fichier d'aliments
        if (isAnimalJsonContent(jsonContent)) {
            println("Erreur: Le contenu JSON ne semble pas être un fichier d'aliments")
            println(
                    "Le fichier semble être un fichier d'animaux. Utilisez la commande import-ani pour ce type de fichier."
            )
            return emptyList()
        }

        // Structure pour collecter des informations détaillées sur les erreurs
        val importErrors = mutableListOf<String>()

        // Collecter les nutriments non résolus
        val nonResolvedNutrients = mutableMapOf<String, Int>()

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
                    println("\n========== RAPPORT DES NUTRIMENTS NON RÉSOLUS ==========")
                    println("${nonResolvedNutrients.size} nutriments n'ont pas pu être résolus:")

                    // Trier et compter par occurrence
                    val countByNutrient =
                            nonResolvedNutrients.toList().sortedByDescending { it.second }

                    // Afficher par ordre de fréquence
                    countByNutrient.forEach { (nutrient, count) ->
                        println("  - $nutrient (présent dans $count aliments)")
                    }

                    println("\nSuggestions pour résoudre le problème:")
                    println(
                            "1. Vérifiez que les labels des nutriments dans le fichier d'importation correspondent exactement aux labels définis dans les énumérations."
                    )
                    println(
                            "2. Assurez-vous que les énumérations NutrientVitam, NutrientMain, etc. incluent tous les nutriments nécessaires."
                    )
                    println(
                            "3. Les labels de nutriments doivent correspondre aux valeurs de la propriété 'label' dans les énumérations."
                    )
                    println(
                            "4. Ajoutez des cas spéciaux dans NutrientResolver.AllNutrientResolver pour ces nutriments."
                    )
                    println("========================================================")
                } else {
                    println("\nTous les nutriments ont été résolus avec succès!")
                }

                // Stocker les erreurs pour consultation ultérieure
                if (importErrors.isNotEmpty()) {
                    saveImportErrors(importErrors, nonResolvedNutrients)
                }

                return foods
            } catch (e: Exception) {
                println("Erreur lors de l'importation comme liste: ${e.message}")
                importErrors.add("Erreur générale lors de l'importation: ${e.message}")

                try {
                    println("Tentative d'importation comme un seul aliment...")
                    val food = json.decodeFromString<AlimentEvJson>(preprocessedJsonString)
                    println("Importation réussie: 1 aliment trouvé")

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
                        println(
                                "\nNutriments non résolus dans le JSON (${nonResolvedNutrients.size}):"
                        )
                        nonResolvedNutrients.entries.sortedByDescending { it.value }.forEach {
                                (nutrientKey, count) ->
                            println("  - $nutrientKey (${count} occurrences)")
                        }
                    }

                    // Stocker les erreurs pour consultation ultérieure
                    if (importErrors.isNotEmpty()) {
                        saveImportErrors(importErrors, nonResolvedNutrients)
                    }

                    return listOf(food)
                } catch (e: Exception) {
                    println("Erreur lors de l'importation comme un seul aliment: ${e.message}")
                    importErrors.add(
                            "Erreur lors de l'importation comme aliment unique: ${e.message}"
                    )
                }
            }
        } catch (e: Exception) {
            println("Erreur lors du prétraitement du JSON: ${e.message}")
            importErrors.add("Erreur lors du prétraitement du JSON: ${e.message}")

            // Fallback à la méthode standard
            try {
                println("Tentative d'importation standard comme liste d'aliments...")
                val foods = json.decodeFromString<List<AlimentEvJson>>(jsonContent)
                println("Importation réussie: ${foods.size} aliments trouvés")

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
                    println("\nNutriments non résolus dans le JSON (${nonResolvedNutrients.size}):")
                    nonResolvedNutrients.entries.sortedByDescending { it.value }.forEach {
                            (nutrientKey, count) ->
                        println("  - $nutrientKey (${count} occurrences)")
                    }
                }

                // Stocker les erreurs pour consultation ultérieure
                if (importErrors.isNotEmpty()) {
                    saveImportErrors(importErrors, nonResolvedNutrients)
                }

                return foods
            } catch (e: Exception) {
                println("Erreur lors de l'importation standard comme liste: ${e.message}")
                importErrors.add("Erreur lors de l'importation standard comme liste: ${e.message}")

                try {
                    println("Tentative d'importation standard comme un seul aliment...")
                    val food = json.decodeFromString<AlimentEvJson>(jsonContent)
                    println("Importation réussie: 1 aliment trouvé")

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
                        println(
                                "\nNutriments non résolus dans le JSON (${nonResolvedNutrients.size}):"
                        )
                        nonResolvedNutrients.entries.sortedByDescending { it.value }.forEach {
                                (nutrientKey, count) ->
                            println("  - $nutrientKey (${count} occurrences)")
                        }
                    }

                    // Stocker les erreurs pour consultation ultérieure
                    if (importErrors.isNotEmpty()) {
                        saveImportErrors(importErrors, nonResolvedNutrients)
                    }

                    return listOf(food)
                } catch (e: Exception) {
                    println(
                            "Erreur lors de l'importation standard comme un seul aliment: ${e.message}"
                    )
                    importErrors.add(
                            "Erreur lors de l'importation standard comme un seul aliment: ${e.message}"
                    )
                }
            }
        }

        println("Échec de l'importation. Retour d'une liste vide.")

        // Même en cas d'échec, afficher les nutriments non résolus collectés lors de l'analyse
        if (nonResolvedNutrients.isNotEmpty()) {
            println("\nNutriments non résolus dans le JSON (${nonResolvedNutrients.size}):")
            nonResolvedNutrients.entries.sortedByDescending { it.value }.forEach {
                    (nutrientKey, count) ->
                println("  - $nutrientKey (${count} occurrences)")
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
            println("\n=== RAPPORT DÉTAILLÉ DES ERREURS D'IMPORTATION ===")
            println(errorReport.toString())
            println("=== FIN DU RAPPORT ===")

            // Note: L'écriture dans un fichier a été supprimée car elle n'est pas compatible
            // multiplateforme
            // Pour implémenter cette fonctionnalité, il faudrait utiliser expect/actual ou une
            // bibliothèque multiplateforme d'I/O
        } catch (e: Exception) {
            println("Erreur lors de la génération du rapport d'erreurs: ${e.message}")
        }
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
                if (nom is JsonPrimitive && nom.content.contains("purison", ignoreCase = true)) {
                    println("Traitement spécial pour l'aliment purison: ${nom.content}")
                }

                // Si on trouve un champ "valMap", traiter son contenu
                if (result.containsKey("valMap")) {
                    val valMap = result["valMap"]

                    // Détecter si valMap est null ou vide
                    if (valMap == null || (valMap is JsonObject && valMap.isEmpty())) {
                        println("ALERTE: valMap est null ou vide pour l'aliment: ${result["nom"]}")

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
                            println("Traitement du nutriment: $key -> $normalizedKey")

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
                                                println(
                                                        "Nutriment normalisé: $key ($nutrientKey) -> $normalizedKey ($normalizedNutrientKey)"
                                                )
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
                                        println(
                                                "Nutriment avec value uniquement: $key -> $normalizedKey"
                                        )
                                    }

                                    // Format 3: Valeur numérique directe (chaîne qui peut être
                                    // convertie en nombre)
                                    value is JsonPrimitive &&
                                            value.isString &&
                                            value.content.toFloatOrNull() != null -> {
                                        // Convertir en objet NutrientQuantity
                                        val nutritionValue =
                                                JsonObject(
                                                        mapOf(
                                                                "value" to
                                                                        JsonPrimitive(
                                                                                value.content
                                                                                        .toFloat()
                                                                        ),
                                                                "nut" to
                                                                        JsonPrimitive(normalizedKey)
                                                        )
                                                )
                                        newValMap[normalizedKey] = nutritionValue
                                        println(
                                                "Nutriment avec valeur numérique en chaîne: $key -> $normalizedKey (${value.content})"
                                        )
                                    }

                                    // Format 4: Valeur numérique directe
                                    value is JsonPrimitive &&
                                            value.content.toFloatOrNull() != null -> {
                                        // Convertir en objet NutrientQuantity
                                        val floatValue = value.content.toFloatOrNull() ?: 0f

                                        val nutritionValue =
                                                JsonObject(
                                                        mapOf(
                                                                "value" to
                                                                        JsonPrimitive(floatValue),
                                                                "nut" to
                                                                        JsonPrimitive(normalizedKey)
                                                        )
                                                )
                                        newValMap[normalizedKey] = nutritionValue
                                        println(
                                                "Nutriment avec valeur primitive: $key -> $normalizedKey ($floatValue)"
                                        )
                                    }

                                    // Format 5: Valeur null mais clé importante
                                    value == null && isEssentialNutrient(normalizedKey) -> {
                                        // Pour les nutriments essentiels, mettre une valeur par
                                        // défaut
                                        // de 0
                                        val nutritionValue =
                                                JsonObject(
                                                        mapOf(
                                                                "value" to JsonPrimitive(0f),
                                                                "nut" to
                                                                        JsonPrimitive(normalizedKey)
                                                        )
                                                )
                                        newValMap[normalizedKey] = nutritionValue
                                        println(
                                                "Nutriment essentiel avec valeur null: $key -> $normalizedKey (défaut: 0)"
                                        )
                                    }

                                    // Autres cas - format spécial pouvant inclure des structures
                                    // JSON imbriquées
                                    else -> {
                                        println(
                                                "Format non standard pour la valeur nutritionnelle $key: $value"
                                        )

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
                                                                    .toFloatOrNull()
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
                                                        println(
                                                                "Valeur extraite du chemin '$path': $key -> $normalizedKey ($extractedValue)"
                                                        )
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
                                                            match.value.toFloatOrNull()
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
                                                        println(
                                                                "Nutriment avec valeur extraite: $key -> $normalizedKey ($extractedNumber)"
                                                        )
                                                    } else {
                                                        // Si la conversion échoue, enregistrer la
                                                        // valeur originale
                                                        newValMap[normalizedKey] = value
                                                        println(
                                                                "Conservation de la valeur originale pour $normalizedKey après échec d'extraction"
                                                        )
                                                    }
                                                } else {
                                                    // Conserver tel quel si aucun nombre n'est
                                                    // trouvé
                                                    newValMap[normalizedKey] = value
                                                    println(
                                                            "Conservation de la valeur originale pour $normalizedKey (aucun nombre trouvé)"
                                                    )
                                                }
                                            } catch (e: Exception) {
                                                // Conserver tel quel en cas d'erreur
                                                newValMap[normalizedKey] = value
                                                println(
                                                        "Erreur lors du traitement du nutriment $key: ${e.message}"
                                                )
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                println(
                                        "Exception lors du traitement du nutriment $key: ${e.message}"
                                )
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
                        "FIBRE",
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
                        "PROTEINE" to 0f,
                        "LIPIDE" to 0f,
                        "HUMIDITE" to 0f,
                        "CENDRE" to 0f,
                        "ENA" to 0f,
                        "CELLULOSE" to 0f
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
                println("Ajout du nutriment essentiel manquant: $nutrient = $defaultValue")
            }
        }

        // Calcul de l'ENA si manquant mais qu'on a les autres valeurs
        if (valMap.containsKey("ENA")) {
            val enaValue =
                    try {
                        val enaObj = valMap["ENA"] as? JsonObject
                        (enaObj?.get("value") as? JsonPrimitive)?.content?.toFloatOrNull() ?: 0f
                    } catch (e: Exception) {
                        0f
                    }

            // Si ENA est 0, essayer de le calculer
            if (enaValue == 0f) {
                try {
                    val proteine =
                            (valMap["PROTEINE"] as? JsonObject)?.let {
                                (it["value"] as? JsonPrimitive)?.content?.toFloatOrNull()
                            }
                                    ?: 0f
                    val lipide =
                            (valMap["LIPIDE"] as? JsonObject)?.let {
                                (it["value"] as? JsonPrimitive)?.content?.toFloatOrNull()
                            }
                                    ?: 0f
                    val humidite =
                            (valMap["HUMIDITE"] as? JsonObject)?.let {
                                (it["value"] as? JsonPrimitive)?.content?.toFloatOrNull()
                            }
                                    ?: 0f
                    val cendre =
                            (valMap["CENDRE"] as? JsonObject)?.let {
                                (it["value"] as? JsonPrimitive)?.content?.toFloatOrNull()
                            }
                                    ?: 0f
                    val cellulose =
                            (valMap["CELLULOSE"] as? JsonObject)?.let {
                                (it["value"] as? JsonPrimitive)?.content?.toFloatOrNull()
                            }
                                    ?: 0f

                    // Formule pour calculer l'ENA
                    val calculatedEna = 100f - proteine - lipide - humidite - cendre - cellulose

                    // Ne mettre à jour que si le résultat est positif et que les valeurs semblent
                    // cohérentes
                    if (calculatedEna > 0 &&
                                    (proteine + lipide + humidite + cendre + cellulose) <= 100f
                    ) {
                        val nutritionValue =
                                JsonObject(
                                        mapOf(
                                                "value" to JsonPrimitive(calculatedEna),
                                                "nut" to JsonPrimitive("ENA")
                                        )
                                )
                        valMap["ENA"] = nutritionValue
                        println(
                                "ENA calculé: $calculatedEna à partir de PROTEINE=$proteine, LIPIDE=$lipide, HUMIDITE=$humidite, CENDRE=$cendre, CELLULOSE=$cellulose"
                        )
                    }
                } catch (e: Exception) {
                    println("Erreur lors du calcul de l'ENA: ${e.message}")
                }
            }
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
                                // Utiliser l'id de l'énumération comme valeur pour espece (au lieu
                                // de categorie)
                                result["espece"] = JsonPrimitive(especeEnum.id)
                                println(
                                        "Prétraitement animal: Conversion de l'espèce $especeValue en ${especeEnum.label} (id: ${especeEnum.id})"
                                )
                            } catch (e: Exception) {
                                // Conserver la valeur originale si la conversion échoue
                                println(
                                        "Prétraitement animal: Échec de conversion de l'espèce $especeValue: ${e.message}, utilisation de l'ID par défaut"
                                )
                                // Essayer de trouver une correspondance directe par categorie
                                val especeByCategorie =
                                        Espece.entries.find { it.categorie == especeValue }
                                if (especeByCategorie != null) {
                                    result["espece"] = JsonPrimitive(especeByCategorie.id)
                                    println(
                                            "Prétraitement animal: Espèce trouvée par catégorie: $especeValue -> ${especeByCategorie.label} (id: ${especeByCategorie.id})"
                                    )
                                } else {
                                    // En cas d'échec, définir une valeur par défaut pour éviter des
                                    // erreurs
                                    result["espece"] = JsonPrimitive("0") // Par défaut CHIEN (ID 0)
                                    println(
                                            "Prétraitement animal: Utilisation de l'ID par défaut CHIEN (0)"
                                    )
                                }
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
            println("Erreur lors de l'analyse du contenu JSON: ${e.message}")
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
            println("Le contenu n'est pas un JSON valide: ${e.message}")
            false
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
}

/** Classe contenant le résultat de l'importation */
data class ImportResult(val animals: List<AnimalEvJson>, val foods: List<AlimentEvJson>)
