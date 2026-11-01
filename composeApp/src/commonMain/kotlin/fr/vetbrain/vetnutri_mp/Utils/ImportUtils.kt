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

    /** Liste pour traquer les résolutions de nutriments problématiques */
    private val resolutionsProblematiques = mutableListOf<String>()

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
            if (!isValidJson(content)) {
                return ImportResult(emptyList(), emptyList())
            }

            // Vérifier si le contenu correspond à un fichier d'animaux
            if (!isAnimalJsonContent(content)) {
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

            val preprocessedJson = preprocessNutrientValues(originalJson)

            val preprocessedWithEspeces = preprocessAnimalEspeces(preprocessedJson)
            val preprocessedContent = jsonParser.encodeToString(preprocessedWithEspeces)

            val animals =
                    try {
                        val result =
                                jsonParser.decodeFromString<List<AnimalEvJson>>(preprocessedContent)
                        result
                    } catch (e: Exception) {
                        e.rethrowIfCancellation()

                        // Essayer d'importer comme un seul animal
                        try {
                            val animal =
                                    jsonParser.decodeFromString<AnimalEvJson>(preprocessedContent)
                            listOf(animal)
                        } catch (e: Exception) {
                            e.rethrowIfCancellation()
                            AppLog.e("ImportUtils", e.message ?: "Erreur", e)
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
            e.rethrowIfCancellation()
            AppLog.e("ImportUtils", e.message ?: "Erreur", e)
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

        // Vérifier si le contenu est un JSON valide
        if (!isValidJson(jsonContent)) {
            return emptyList()
        }

        // Vérifier si le contenu correspond à un fichier d'aliments
        if (isAnimalJsonContent(jsonContent)) {
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
            val jsonElement = json.parseToJsonElement(jsonContent)
            val preprocessedJson = preprocessEspecesAndNutrientValues(jsonElement)
            val preprocessedJsonString =
                    json.encodeToString(JsonElement.serializer(), preprocessedJson)

            try {
                val foods = json.decodeFromString<List<AlimentEvJson>>(preprocessedJsonString)

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
                e.rethrowIfCancellation()
                importErrors.add("Erreur générale lors de l'importation: ${e.message}")

                try {
                    val food = json.decodeFromString<AlimentEvJson>(preprocessedJsonString)

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
                    e.rethrowIfCancellation()
                    importErrors.add(
                            "Erreur lors de l'importation comme aliment unique: ${e.message}"
                    )
                }
            }
        } catch (e: Exception) {
            e.rethrowIfCancellation()
            importErrors.add("Erreur lors du prétraitement du JSON: ${e.message}")

            // Fallback à la méthode standard
            try {
                val foods = json.decodeFromString<List<AlimentEvJson>>(jsonContent)

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
                e.rethrowIfCancellation()
                importErrors.add("Erreur lors de l'importation standard comme liste: ${e.message}")

                try {
                    val food = json.decodeFromString<AlimentEvJson>(jsonContent)

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
                    e.rethrowIfCancellation()
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
        } catch (e: Exception) { e.rethrowIfCancellation(); AppLog.d("ImportUtils", "Exception ignorée : ${e.message}") }
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
                                                e.rethrowIfCancellation()
                                                // Conserver tel quel en cas d'erreur
                                                newValMap[normalizedKey] = value
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e.rethrowIfCancellation()
                                AppLog.e("ImportUtils", e.message ?: "Erreur", e)
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
                                    e.rethrowIfCancellation()
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
                                        e.rethrowIfCancellation()
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

            // D'abord essayer de parser comme liste d'animaux
            val animals =
                    try {
                        jsonParser.decodeFromString<List<AnimalEvJson>>(preprocessedContent)
                    } catch (e: Exception) {
                        e.rethrowIfCancellation()
                        // Essayer d'importer comme un seul animal
                        try {
                            listOf(jsonParser.decodeFromString<AnimalEvJson>(preprocessedContent))
                        } catch (e: Exception) {
                            e.rethrowIfCancellation()
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
            e.rethrowIfCancellation()
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
                                    e.rethrowIfCancellation()
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
                                e.rethrowIfCancellation()
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
                                e.rethrowIfCancellation()
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
            e.rethrowIfCancellation()
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
            e.rethrowIfCancellation()
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
     * Importe une liste de références nutritionnelles à partir d'une chaîne JSON Format attendu:
     * .vbnr.json (VetBrain Nutritional Requirements) Structure: Array de NutritionalRequirementData
     * avec reference, allEquations, allBibliographicReferences, nutrientRequirements
     *
     * @param jsonContent Le contenu JSON à désérialiser (.vbnr.json format)
     * @return La liste des références nutritionnelles importées
     */
    internal fun lireReferencesNutritionnelles(
            jsonContent: String
    ): List<fr.vetbrain.vetnutri_mp.Data.ReferenceEv> {

        try {
            // Vérifier si le contenu est un JSON valide
            if (!isValidJson(jsonContent)) {
                return emptyList()
            }

            // Vérifier si le contenu correspond à un fichier de références nutritionnelles
            if (!isNutritionalRequirementJsonContent(jsonContent)) {
                return emptyList()
            }

            val references = mutableListOf<fr.vetbrain.vetnutri_mp.Data.ReferenceEv>()
            val jsonElement = json.parseToJsonElement(jsonContent)

            when (jsonElement) {
                is kotlinx.serialization.json.JsonArray -> {

                    jsonElement.forEachIndexed { index, element ->
                        if (element is kotlinx.serialization.json.JsonObject) {
                            val ref = creerReferenceDepuisNutritionalRequirementData(element, index)
                            if (ref != null) {
                                references.add(ref)
                            } else {}
                        }
                    }
                }
                is kotlinx.serialization.json.JsonObject -> {
                    val ref = creerReferenceDepuisNutritionalRequirementData(jsonElement, 0)
                    if (ref != null) {
                        references.add(ref)
                    } else {}
                }
                else -> {
                    return emptyList()
                }
            }

            return references
        } catch (e: Exception) {
            e.rethrowIfCancellation()
            AppLog.e("ImportUtils", e.message ?: "Erreur", e)
            return emptyList()
        }
    }

    /**
     * Crée une ReferenceEv à partir d'un objet NutritionalRequirementData JSON Structure attendue:
     * { reference: {...}, allEquations: [...], allBibliographicReferences: [...],
     * nutrientRequirements: [...] }
     */
    private fun creerReferenceDepuisNutritionalRequirementData(
            dataObj: kotlinx.serialization.json.JsonObject,
            index: Int
    ): fr.vetbrain.vetnutri_mp.Data.ReferenceEv? {
        return try {
            // Extraire l'objet reference du NutritionalRequirementData
            val referenceObj = dataObj["reference"] as? kotlinx.serialization.json.JsonObject

            if (referenceObj == null) {
                return null
            }

            // Extraire les propriétés de base de la référence
            val uuid =
                    extraireStringDepuisJson(referenceObj, "UUID")
                            ?: fr.vetbrain.vetnutri_mp.Utils.genUUID()

            val nom =
                    extraireStringDepuisJson(referenceObj, "nom")
                            ?: "Référence importée ${index + 1}"

            val description =
                    extraireStringDepuisJson(referenceObj, "description")
                            ?: "Référence nutritionnelle importée automatiquement"

            val disease = extraireBooleanDepuisJson(referenceObj, "disease") ?: false
            val nameDisease = extraireStringDepuisJson(referenceObj, "nameDisease") ?: ""
            val nameEnergy =
                    extraireStringDepuisJson(referenceObj, "nameEnergy") ?: "Énergie métabolisable"
            val consistent = extraireIntDepuisJson(referenceObj, "consistent") ?: 1

            // Convertir les espèces et stades physiologiques
            val speciesStr = extraireStringDepuisJson(referenceObj, "species") ?: "CHIEN"
            val sPhysioStr = extraireStringDepuisJson(referenceObj, "sPhysio") ?: "ADULTE"

            val espece = convertirStringVersEspece(speciesStr)

            // Utiliser la détection intelligente pour le stade physiologique
            val stadePhysio = detecterStadePhysioDepuisNom(nom, sPhysioStr)

            // Utiliser la détection intelligente pour les maladies
            val (estMaladie, nomMaladieDetecte) = detecterMaladie(nom, disease, nameDisease)

            // Créer la référence de base
            val reference =
                    fr.vetbrain.vetnutri_mp.Data.ReferenceEv(
                            uuid = uuid,
                            nom = nom,
                            description = description,
                            maladie = estMaladie,
                            nomMaladie = nomMaladieDetecte,
                            nomEnergie = nameEnergy,
                            consistent = consistent,
                            espece = espece,
                            stadePhysio = stadePhysio
                    )

            // Traiter les équations si présentes
            val allEquations = dataObj["allEquations"] as? kotlinx.serialization.json.JsonArray
            if (allEquations != null) {
                val equations = traiterEquations(allEquations)

                // Assigner les équations selon leur type
                equations.forEach { equation ->
                    when (equation.kind) {
                        fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERGYNEED -> {
                            if (equation.name.contains("BEE", ignoreCase = true) ||
                                            equation.name.contains("energy", ignoreCase = true)
                            ) {
                                reference.equationBEE = equation
                            } else {
                                reference.equationsNut.add(equation)
                            }
                        }
                        fr.vetbrain.vetnutri_mp.Enumer.EquationKind.MW -> {
                            reference.equationBW = equation
                        }
                        fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERGYDENSITY -> {
                            if (equation.name.contains("commercial", ignoreCase = true)) {
                                reference.equationDEcom = equation
                            } else {
                                reference.equationDEraw = equation
                            }
                        }
                        fr.vetbrain.vetnutri_mp.Enumer.EquationKind.NEED -> {
                            reference.equationsNut.add(equation)
                        }
                        else -> {
                            reference.equationsNut.add(equation)
                        }
                    }
                }

                // Vérification finale des équations assignées
                reference.equationsNut.forEach { eq ->
                    
                }
            } else {}

            // Traiter les références bibliographiques
            val allBibliographicReferences =
                    dataObj["allBibliographicReferences"] as? kotlinx.serialization.json.JsonArray
            if (allBibliographicReferences != null) {
                // Les références biblio sont utilisées dans les nutriments, pas stockées
                // directement dans la référence
            }

            // Traiter les besoins nutritionnels
            val nutrientRequirements =
                    dataObj["nutrientRequirements"] as? kotlinx.serialization.json.JsonArray
            if (nutrientRequirements != null) {
                traiterBesoinsNutritionnels(
                        reference,
                        nutrientRequirements,
                        allBibliographicReferences
                )
            }

            // Traiter les coefficients de modification si présents
            traiterCoefficientsModification(reference, referenceObj)

            

            return reference
        } catch (e: Exception) {
            e.rethrowIfCancellation()
            AppLog.e("ImportUtils", e.message ?: "Erreur", e)
            return null
        }
    }

    /** Traite les équations d'un tableau JSON et les convertit en objets Equation */
    private fun traiterEquations(
            equationsArray: kotlinx.serialization.json.JsonArray
    ): List<fr.vetbrain.vetnutri_mp.Data.Equation> {
        val equations = mutableListOf<fr.vetbrain.vetnutri_mp.Data.Equation>()

        equationsArray.forEach { element ->
            if (element is kotlinx.serialization.json.JsonObject) {
                try {
                    val equation = creerEquationDepuisJson(element)
                    if (equation != null) {
                        equations.add(equation)
                    }
                } catch (e: Exception) { e.rethrowIfCancellation(); AppLog.d("ImportUtils", "Exception ignorée : ${e.message}") }
            }
        }

        return equations
    }

    /** Crée un objet Equation à partir d'un objet JSON */
    private fun creerEquationDepuisJson(
            equationObj: kotlinx.serialization.json.JsonObject
    ): fr.vetbrain.vetnutri_mp.Data.Equation? {
        try {
            val uuid =
                    extraireStringDepuisJson(equationObj, "UUID")
                            ?: fr.vetbrain.vetnutri_mp.Utils.genUUID()
            val name = extraireStringDepuisJson(equationObj, "name") ?: "Équation importée"
            val description =
                    extraireStringDepuisJson(equationObj, "Description")
                            ?: extraireStringDepuisJson(equationObj, "description") ?: ""
            val equationScript = extraireStringDepuisJson(equationObj, "equationScript") ?: "0"

            // kind : identifiant V2 (0..6) ou nom de l'énumération (« ENERGYNEED », « ENERCOMP »…)
            val kind =
                    (equationObj["kind"] as? kotlinx.serialization.json.JsonPrimitive)
                            ?.content
                            ?.trim()
                            ?.let { brut ->
                                brut.toIntOrNull()?.let { LegacyV2Mappings.equationKindNames[it] }
                                        ?: brut.uppercase()
                            }
                            ?.let { nom ->
                                fr.vetbrain.vetnutri_mp.Enumer.EquationKind.entries.firstOrNull {
                                    it.name == nom
                                }
                            }
                            ?: fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERGYNEED
            val speciesStr =
                    extraireStringDepuisJson(equationObj, "Specie")
                            ?: extraireStringDepuisJson(equationObj, "specie") ?: "CHIEN"
            val consistent = extraireBooleanDepuisJson(equationObj, "consistent") ?: true

            val espece = convertirStringVersEspece(speciesStr)

            // Traiter la référence bibliographique si présente
            val bibRef =
                    equationObj["bib"]?.let { bibElement ->
                        if (bibElement is kotlinx.serialization.json.JsonObject) {
                            creerBiblioRefDepuisJsonComplet(bibElement)
                        } else null
                    }
                            ?: creerBiblioRefParDefaut(
                                    "Aucune référence bibliographique fournie pour l'équation"
                            )

            val equation =
                    fr.vetbrain.vetnutri_mp.Data.Equation(
                            uuid = uuid,
                            name = name,
                            description = description,
                            equationScript = equationScript,
                            kind = kind,
                            specie = espece,
                            bib = bibRef,
                            consistent = consistent
                    )

            
            return equation
        } catch (e: Exception) {
            e.rethrowIfCancellation()
            AppLog.e("ImportUtils", e.message ?: "Erreur", e)
            return null
        }
    }

    /**
     * Crée un objet BiblioRef à partir d'un objet JSON complet (avec firstAuthor, year,
     * completeRef)
     */
    private fun creerBiblioRefDepuisJsonComplet(
            biblioObj: kotlinx.serialization.json.JsonObject
    ): fr.vetbrain.vetnutri_mp.Data.BiblioRef? {
        try {
            val uuid = extraireStringDepuisJson(biblioObj, "UUID")
            val firstAuthor = extraireStringDepuisJson(biblioObj, "firstAuthor") ?: ""
            val year = extraireIntDepuisJson(biblioObj, "year") ?: 0
            val completeRef = extraireStringDepuisJson(biblioObj, "completeRef") ?: ""
            val comment = extraireStringDepuisJson(biblioObj, "comment") ?: ""
            val consistent = extraireIntDepuisJson(biblioObj, "consistent") ?: 1

            // Vérifier si les données sont valides et utilisables
            if (uuid.isNullOrBlank() || firstAuthor.isBlank() || year < 1900 || consistent == 0) {

                return creerBiblioRefParDefaut("Données incomplètes dans JSON")
            }

            val biblioRef =
                    fr.vetbrain.vetnutri_mp.Data.BiblioRef(
                            uuid = uuid,
                            firstAuthor = firstAuthor,
                            year = year,
                            completeRef = completeRef,
                            comments = comment,
                            consistent = consistent
                    )

            
            return biblioRef
        } catch (e: Exception) {
            e.rethrowIfCancellation()
            AppLog.e("ImportUtils", e.message ?: "Erreur", e)
            return creerBiblioRefParDefaut("Erreur lors de la création")
        }
    }

    /** Crée une référence bibliographique par défaut pour éviter les erreurs de clé étrangère */
    private fun creerBiblioRefParDefaut(motif: String): fr.vetbrain.vetnutri_mp.Data.BiblioRef {
        // Utiliser un UUID fixe pour éviter les duplicatas et garantir la cohérence
        val defaultUuid = "default-biblio"

        return fr.vetbrain.vetnutri_mp.Data.BiblioRef(
                uuid = defaultUuid,
                firstAuthor = "Système VetNutri",
                year = 2024,
                completeRef = "Référence par défaut générée automatiquement lors de l'import",
                comments = "Créée automatiquement - $motif",
                consistent = 1
        )
    }

    /** Crée un objet BiblioRef à partir d'un objet JSON (ancienne version pour compatibilité) */
    private fun creerBiblioRefDepuisJson(
            biblioObj: kotlinx.serialization.json.JsonObject
    ): fr.vetbrain.vetnutri_mp.Data.BiblioRef? {
        // Essayer d'abord la version complète
        val completeVersion = creerBiblioRefDepuisJsonComplet(biblioObj)
        if (completeVersion != null && completeVersion.firstAuthor.isNotBlank()) {
            return completeVersion
        }

        // Sinon, essayer l'ancienne méthode
        try {
            val uuid =
                    extraireStringDepuisJson(biblioObj, "UUID")
                            ?: fr.vetbrain.vetnutri_mp.Utils.genUUID()
            val author = extraireStringDepuisJson(biblioObj, "author") ?: ""
            val title = extraireStringDepuisJson(biblioObj, "title") ?: ""
            val year = extraireIntDepuisJson(biblioObj, "year") ?: 0
            val completeRef = "$author. $title. $year"

            return fr.vetbrain.vetnutri_mp.Data.BiblioRef(
                    uuid = uuid,
                    firstAuthor = author,
                    year = year,
                    completeRef = completeRef
            )
        } catch (e: Exception) {
            e.rethrowIfCancellation()
            return null
        }
    }

    /** Traite les besoins nutritionnels et les ajoute aux maps de la référence */
    private fun traiterBesoinsNutritionnels(
            reference: fr.vetbrain.vetnutri_mp.Data.ReferenceEv,
            nutrientRequirements: kotlinx.serialization.json.JsonArray,
            allBibliographicReferences: kotlinx.serialization.json.JsonArray?
    ) {
        // Créer un index des références bibliographiques pour la recherche rapide
        val biblioIndex = mutableMapOf<String, fr.vetbrain.vetnutri_mp.Data.BiblioRef>()
        allBibliographicReferences?.forEach { element ->
            if (element is kotlinx.serialization.json.JsonObject) {
                val biblioRef = creerBiblioRefDepuisJsonComplet(element)
                if (biblioRef != null && biblioRef.uuid.isNotBlank()) {
                    biblioIndex[biblioRef.uuid] = biblioRef
                }
            }
        }

        nutrientRequirements.forEach { element ->
            if (element is kotlinx.serialization.json.JsonObject) {
                try {
                    val nutrientInfo = creerNutrientRequirementInfoDepuisJson(element, biblioIndex)
                    if (nutrientInfo != null) {
                        ajouterNutrientALaReference(reference, nutrientInfo)
                    }
                } catch (e: Exception) { e.rethrowIfCancellation(); AppLog.d("ImportUtils", "Exception ignorée : ${e.message}") }
            }
        }
    }

    /** Crée un objet représentant un besoin nutritionnel à partir du JSON */
    private fun creerNutrientRequirementInfoDepuisJson(
            nutrientObj: kotlinx.serialization.json.JsonObject,
            biblioIndex: Map<String, fr.vetbrain.vetnutri_mp.Data.BiblioRef>
    ): NutrientRequirementInfo? {
        try {
            // Extraire le nutriment - le JSON a directement "nutrient": "K"
            val nutrientString = extraireStringDepuisJson(nutrientObj, "nutrient")
            if (nutrientString == null) {
                return null
            }

            // Normaliser le nom du nutriment pour la résolution
            val normalizedNutrientString = normaliserNomNutrient(nutrientString)
            
            // 🔍 LOG DIAGNOSTIC : Tracer la résolution des nutriments
            

            var finalNutrient = resoudreNutrimentExact(normalizedNutrientString)

            if (finalNutrient != null) {
                // 🔍 LOG DIAGNOSTIC : Nutriment résolu avec succès
                
            } else {
                // 🔍 LOG DIAGNOSTIC : Échec de la résolution
                
                resolutionsProblematiques.add(
                        "ECHEC_RESOLUTION_EXACTE: '$nutrientString' → '$normalizedNutrientString'"
                )

                // Essayer avec d'autres variantes courantes
                val variantes = genererVariantesNutrient(nutrientString)
                for (variante in variantes) {
                    finalNutrient = resoudreNutrimentExact(variante)
                    if (finalNutrient != null) {
                        resolutionsProblematiques.add(
                                "RESOLUTION_PAR_VARIANTE: '$nutrientString' → '$variante' (${finalNutrient::class.simpleName})"
                        )
                        break
                    }
                }

                if (finalNutrient == null) {
                    resolutionsProblematiques.add(
                            "ECHEC_TOTAL: '$nutrientString' - Aucune correspondance trouvée"
                    )
                    return null
                }
            }

            // Extraire le niveau de référence
            val referenceLevelStr = extraireStringDepuisJson(nutrientObj, "referenceLevel") ?: "MIN"
            val referenceLevel = convertirStringVersReflevel(referenceLevelStr)

            // Extraire la quantité et les unités
            val quantity = extraireDoubleDepuisJson(nutrientObj, "quantity") ?: 0.0
            val unitStr = extraireStringDepuisJson(nutrientObj, "unit") ?: "BUg"
            val unitRequirementStr =
                    extraireStringDepuisJson(nutrientObj, "unitRequirement") ?: "MCAL"

            val unit = convertirStringVersUnitEnum(unitStr)
            val unitRequirement = convertirStringVersUnitReqEnum(unitRequirementStr)

            // Extraire la référence bibliographique
            val biblioRefBrut = nutrientObj["bibliographicReference"]
            val biblioRefElement = biblioRefBrut as? kotlinx.serialization.json.JsonObject
            val biblioRef =
                    if (biblioRefBrut is kotlinx.serialization.json.JsonPrimitive && biblioRefBrut.isString) {
                        // Référence par identifiant vers allBibliographicReferences
                        biblioIndex[biblioRefBrut.content]
                    } else if (biblioRefElement != null) {
                        // Chercher d'abord par UUID si présent
                        val biblioUuid = extraireStringDepuisJson(biblioRefElement, "UUID")
                        if (biblioUuid != null && biblioIndex.containsKey(biblioUuid)) {
                            biblioIndex[biblioUuid]
                        } else {
                            // Sinon créer directement depuis le JSON
                            creerBiblioRefDepuisJson(biblioRefElement)
                        }
                    } else null

            return NutrientRequirementInfo(
                    nutrient = finalNutrient,
                    referenceLevel = referenceLevel,
                    quantity = quantity,
                    unit = unit,
                    unitRequirement = unitRequirement,
                    bibliographicReference = biblioRef
            )
        } catch (e: Exception) {
            e.rethrowIfCancellation()
            AppLog.e("ImportUtils", e.message ?: "Erreur", e)
            return null
        }
    }

    /** Libellés exacts des nutriments standard (et libellés alternatifs des vitamines). */
    private val libellesNutrimentsStandards: Set<String> by lazy {
        buildSet {
            listOf<List<fr.vetbrain.vetnutri_mp.Enumer.Nutrient>>(
                            fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.entries,
                            fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.entries,
                            fr.vetbrain.vetnutri_mp.Enumer.NutrientMin.entries,
                            fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid.entries,
                            fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam.entries,
                            fr.vetbrain.vetnutri_mp.Enumer.NutrientOther.entries,
                            fr.vetbrain.vetnutri_mp.Enumer.AAEnum.entries,
                            fr.vetbrain.vetnutri_mp.Enumer.NutrientEnergy.entries,
                            fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis.entries
                    )
                    .flatten()
                    .forEach { add(it.label.uppercase()) }
            fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam.entries.forEach { vitamine ->
                vitamine.altLabels.forEach { add(it.uppercase()) }
            }
            // Alias traités explicitement par NutrientResolver (cholestérol, fibres)
            addAll(listOf("CHOL", "FIBRETOT", "FIBRESOL"))
        }
    }

    /**
     * Résolution sans approximation pour un besoin de référentiel. `AllNutrientResolver` ne renvoie
     * jamais null : un libellé inconnu ou mal orthographié y est rapproché du nutriment le plus
     * ressemblant, ou de celui dont le libellé contient le sien. Le besoin serait alors rangé
     * sous un autre nutriment et en écraserait la valeur. Seuls un libellé standard exact ou un
     * nutriment personnalisé déjà déclaré sont acceptés ; le reste est écarté et signalé.
     */
    private fun resoudreNutrimentExact(libelle: String): fr.vetbrain.vetnutri_mp.Enumer.Nutrient? {
        val normalise = fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.normalizeLabel(libelle).uppercase()
        val connu =
                normalise in libellesNutrimentsStandards ||
                        fr.vetbrain.vetnutri_mp.Enumer.CustomNutrientRegistry.getByLabel(normalise) != null
        return if (connu) fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(libelle) else null
    }

    /** Normalise le nom d'un nutriment pour une meilleure résolution */
    private fun normaliserNomNutrient(nutrient: String): String {
        return when (nutrient.uppercase().trim()) {
            // Corrections des minéraux essentiels - Utiliser les labels EXACTS des énumérations
            "MG",
            "MAGNESIUM" -> "MG" // NutrientMacro.MG a le label "MG"
            "CA", "CALCIUM" -> "CA" // NutrientMacro.CA a le label "CA" (pas "CAL")
            "P", "PHOSPHORE", "PHOSPHORUS" -> "P" // NutrientMacro.P a le label "P" (pas "PHOS")
            "NA", "SODIUM" -> "NA" // NutrientMacro.NA a le label "NA"
            "K", "POTASSIUM" -> "K" // NutrientMacro.K a le label "K"
            "CL", "CHLORE", "CHLORIDE" -> "CL" // NutrientMacro.CL a le label "CL" (pas "CHL")

            // Corrections des oligo-éléments
            "FE",
            "IRON",
            "FER" -> "FE"
            "ZN", "ZINC" -> "ZN"
            "CU", "COPPER", "CUIVRE" -> "CU"
            "MN", "MANGANESE" -> "MN"
            "I", "IODE", "IODINE" -> "I"
            "SE", "SELENIUM" -> "SE"

            // Corrections d'acides aminés - Utiliser les vrais labels de AAEnum
            "METHCYS",
            "METH+CYS",
            "MET+CYS",
            "METHIONINE_CYSTEINE" -> "METHCYS" // NutrientAnalysis.MethCys a le label "METHCYS"
            "PHENTYR", "PHEN+TYR", "PHE+TYR", "PHENYLALANINE_TYROSINE" ->
                    "PHENTYR" // NutrientAnalysis.PhenTyr a le label "PHENTYR"
            "TRYPTOPHAN", "TRYPTOPHANE", "TRY" ->
                    "TRYPTOPHANE" // AAEnum.TRYPTOPHANE a le label "TRYPTOPHANE"
            "LYSINE", "LYS" -> "LYSINE"
            "METHIONINE", "MET" -> "METHIONINE"
            "CYSTEINE", "CYS" -> "CYSTEINE"
            "PHENYLALANINE", "PHE" -> "PHENYLALANINE"
            "TYROSINE", "TYR" -> "TYROSINE"
            "LEUCINE", "LEU" -> "LEUCINE"
            "ISOLEUCINE", "ILE" -> "ISOLEUCINE"
            "VALINE", "VAL" -> "VALINE"
            "THREONINE", "THR" -> "THREONINE"
            "HISTIDINE", "HIS" -> "HISTIDINE"
            "ARGININE", "ARG" -> "ARGININE"

            // Corrections de vitamines
            "VITB1",
            "THIAMINE",
            "THIAMIN" -> "VITB1"
            "VITB2", "RIBOFLAVINE", "RIBOFLAVIN" -> "VITB2"
            "VITB3", "NIACINE", "NIACIN" -> "VITB3"
            "VITB5", "PANTOTHENIC", "PANTOTHENIC_ACID" -> "VITB5"
            "VITB6", "PYRIDOXINE", "PYRIDOXIN" -> "VITB6"
            "VITB9", "FOLATE", "FOLIC_ACID" -> "VITB9"
            "VITB12", "COBALAMIN", "CYANOCOBALAMIN" -> "VITB12" // Correction importante !
            "VITA", "RETINOL", "VITAMIN_A" -> "VITA"
            "VITD", "CHOLECALCIFEROL", "VITAMIN_D" -> "VITD"
            "VITE", "TOCOPHEROL", "VITAMIN_E" -> "VITE"
            "VITK", "PHYLLOQUINONE", "VITAMIN_K" -> "VITK"
            "VITC", "ASCORBIC", "VITAMIN_C" -> "VITC"
            "CHOLINE" -> "CHOLINE"

            // Corrections de ratios et analyses - Utiliser les vrais labels de NutrientAnalysis
            "PCA",
            "P/CA",
            "PHOSPHOCALCIUM",
            "PCa" -> "CAP" // NutrientAnalysis.PCa a le label "CAP" !
            "O6O3", "O6/O3", "OMEGA6OMEGA3" -> "O6O3"
            "ZNCU", "ZN/CU", "ZINCCOPPER", "ZnCu" -> "ZNCU"

            // Corrections d'acides gras
            "AG204",
            "DHA" -> "AG204"
            "AG183", "ALA" -> "AG183"
            "AG205", "EPA" -> "AG205"
            "AG182", "LINOLEIC", "LINOLEIQUE" -> "AG182"
            "O3", "OMEGA3" -> "O3"
            "O6", "OMEGA6" -> "O6"
            "EPADHA", "EPA+DHA" -> "EPADHA"

            // Corrections des macronutriments
            "PROTEINE",
            "PROTEIN" -> "PROTEINE"
            "LIPIDE", "LIPIDES", "FAT" -> "LIPIDE"
            "GLUCIDE", "GLUCIDES", "CARBOHYDRATE" -> "GLUCIDE"
            "FIBRE", "FIBRES", "FIBER" -> "CELLULOSE"
            "CELLULOSE" -> "CELLULOSE"
            "AMIDON", "STARCH" -> "AMIDON"
            "SUCRE", "SUGAR" -> "SUCRE"
            "HUMIDITE", "MOISTURE" -> "HUMIDITE"
            "CENDRE", "ASH" -> "CENDRE"
            "ENA", "NFE" -> "ENA"

            // Garder tel quel pour les autres
            else -> nutrient.uppercase().trim()
        }
    }

    /** Génère des variantes possibles pour un nom de nutriment */
    private fun genererVariantesNutrient(nutrient: String): List<String> {
        val base = nutrient.uppercase().trim()
        val variantes = mutableListOf<String>()

        // Ajouter la forme normalisée en premier
        variantes.add(normaliserNomNutrient(base))

        // Ajouter la forme originale
        variantes.add(base)

        // Variantes spécifiques pour les minéraux problématiques
        when (base) {
            "MG" -> variantes.addAll(listOf("MG", "MAGNESIUM"))
            "CA" -> variantes.addAll(listOf("CA", "CALCIUM"))
            "P" -> variantes.addAll(listOf("P", "PHOSPHORE", "PHOSPHORUS"))
            "NA" -> variantes.addAll(listOf("NA", "SODIUM"))
            "K" -> variantes.addAll(listOf("K", "POTASSIUM"))
            "FE" -> variantes.addAll(listOf("FE", "IRON", "FER"))
            "ZN" -> variantes.addAll(listOf("ZN", "ZINC"))
            "CU" -> variantes.addAll(listOf("CU", "COPPER", "CUIVRE"))
            "MN" -> variantes.addAll(listOf("MN", "MANGANESE"))
            "I" -> variantes.addAll(listOf("I", "IODE", "IODINE"))
            "SE" -> variantes.addAll(listOf("SE", "SELENIUM"))
        }

        // Variantes pour les acides aminés
        when (base) {
            "TRYPTOPHAN", "TRYPTOPHANE" ->
                    variantes.addAll(listOf("TRY", "TRYPTOPHAN", "TRYPTOPHANE"))
            "METHIONINE_CYSTEINE", "METH+CYS" ->
                    variantes.addAll(listOf("METH_CYS", "METHCYS", "MET+CYS"))
            "PHENYLALANINE_TYROSINE", "PHEN+TYR" ->
                    variantes.addAll(listOf("PHEN_TYR", "PHENTYR", "PHE+TYR"))
            "LYSINE" -> variantes.addAll(listOf("LYSINE", "LYS"))
            "METHIONINE" -> variantes.addAll(listOf("METHIONINE", "MET"))
            "LEUCINE" -> variantes.addAll(listOf("LEUCINE", "LEU"))
            "ISOLEUCINE" -> variantes.addAll(listOf("ISOLEUCINE", "ILE"))
            "VALINE" -> variantes.addAll(listOf("VALINE", "VAL"))
            "THREONINE" -> variantes.addAll(listOf("THREONINE", "THR"))
            "HISTIDINE" -> variantes.addAll(listOf("HISTIDINE", "HIS"))
            "ARGININE" -> variantes.addAll(listOf("ARGININE", "ARG"))
            "PHENYLALANINE" -> variantes.addAll(listOf("PHENYLALANINE", "PHE"))
            "TYROSINE" -> variantes.addAll(listOf("TYROSINE", "TYR"))
            "CYSTEINE" -> variantes.addAll(listOf("CYSTEINE", "CYS"))
        }

        // Variantes pour les vitamines
        when (base) {
            "CYANOCOBALAMIN", "COBALAMIN" ->
                    variantes.addAll(listOf("VITB12", "B12", "CYANOCOBALAMIN"))
            "THIAMINE", "THIAMIN" -> variantes.addAll(listOf("VITB1", "B1", "THIAMIN"))
            "RIBOFLAVIN", "RIBOFLAVINE" -> variantes.addAll(listOf("VITB2", "B2", "RIBOFLAVIN"))
            "NIACIN", "NIACINE" -> variantes.addAll(listOf("VITB3", "B3", "NIACIN"))
            "PANTOTHENIC_ACID", "PANTOTHENIC" -> variantes.addAll(listOf("VITB5", "B5"))
            "PYRIDOXIN", "PYRIDOXINE" -> variantes.addAll(listOf("VITB6", "B6", "PYRIDOXIN"))
            "FOLIC_ACID", "FOLATE" -> variantes.addAll(listOf("VITB9", "B9", "FOLIC_ACID"))
            "VITAMIN_A", "RETINOL" -> variantes.addAll(listOf("VITA", "VITAMIN_A"))
            "VITAMIN_D", "CHOLECALCIFEROL" -> variantes.addAll(listOf("VITD", "VITAMIN_D"))
            "VITAMIN_E", "TOCOPHEROL" -> variantes.addAll(listOf("VITE", "VITAMIN_E"))
            "VITAMIN_K", "PHYLLOQUINONE" -> variantes.addAll(listOf("VITK", "VITAMIN_K"))
            "CHOLINE" -> variantes.addAll(listOf("CHOLINE"))
        }

        // Variantes pour les ratios
        when (base) {
            "PCA", "P/CA", "PHOSPHOCALCIUM" -> variantes.addAll(listOf("PCa", "PCA", "P/CA"))
            "ZNCU", "ZN/CU", "ZINCCOPPER" -> variantes.addAll(listOf("ZnCu", "ZNCU", "ZN/CU"))
            "O6O3", "O6/O3", "OMEGA6OMEGA3" -> variantes.addAll(listOf("o6o3", "O6O3", "O6/O3"))
        }

        // Ajouter des variantes avec/sans préfixes pour les vitamines
        if (base.startsWith("VIT")) {
            variantes.add(base.removePrefix("VIT"))
            variantes.add("VITAMIN_" + base.removePrefix("VIT"))
        }

        return variantes.distinct()
    }

    /** Ajoute un nutriment à la référence dans la map appropriée selon le niveau de référence */
    private fun ajouterNutrientALaReference(
            reference: fr.vetbrain.vetnutri_mp.Data.ReferenceEv,
            nutrientInfo: NutrientRequirementInfo
    ) {
        try {
            // 🔍 LOG DIAGNOSTIC : Tentative d'ajout du nutriment
            
            
            // Créer la référence bibliographique si elle n'existe pas
            val biblio =
                    nutrientInfo.bibliographicReference ?: fr.vetbrain.vetnutri_mp.Data.BiblioRef()

            // Utiliser la méthode publique pour définir le nutriment
            // Quantité ramenée à l'unité du nutriment (50 mg de K → 0,05 g) : le reste de
            // l'application lit la valeur dans nutrient.ue
            val uniteNutriment = nutrientInfo.nutrient.ue
            val uniteSource = nutrientInfo.unit
            val quantite =
                    if (uniteSource != null && uniteSource != uniteNutriment &&
                                    uniteSource.getIDFamily() == uniteNutriment.getIDFamily() &&
                                    uniteNutriment.conv > 0.0
                    ) {
                        nutrientInfo.quantity * uniteSource.conv / uniteNutriment.conv
                    } else {
                        nutrientInfo.quantity
                    }
            reference.definirNutriment(
                    valeur = quantite,
                    nutrient = nutrientInfo.nutrient,
                    niveauRef = nutrientInfo.referenceLevel,
                    uniteReq = nutrientInfo.unitRequirement,
                    biblio = biblio
            )
            
            // 🔍 LOG DIAGNOSTIC : Vérification après ajout
            
            
        } catch (e: Exception) { e.rethrowIfCancellation(); AppLog.d("ImportUtils", "Exception ignorée : ${e.message}") }
    }

    /** Traite les coefficients de modification (modk1-5) si présents */
    private fun traiterCoefficientsModification(
            reference: fr.vetbrain.vetnutri_mp.Data.ReferenceEv,
            referenceObj: kotlinx.serialization.json.JsonObject
    ) {
        try {
            // Traiter les noms des coefficients
            reference.nomk1 = extraireStringDepuisJson(referenceObj, "namek1") ?: ""
            reference.nomk2 = extraireStringDepuisJson(referenceObj, "namek2") ?: ""
            reference.nomk3 = extraireStringDepuisJson(referenceObj, "namek3") ?: ""
            reference.nomk4 = extraireStringDepuisJson(referenceObj, "namek4") ?: ""
            reference.nomk5 = extraireStringDepuisJson(referenceObj, "namek5") ?: ""

            // Traiter les tableaux de coefficients
            val coefArrays = listOf("modk1", "modk2", "modk3", "modk4", "modk5")
            val referenceLists =
                    listOf(
                            reference.modk1,
                            reference.modk2,
                            reference.modk3,
                            reference.modk4,
                            reference.modk5
                    )

            coefArrays.forEachIndexed { index, arrayName ->
                val coefArray = referenceObj[arrayName] as? kotlinx.serialization.json.JsonArray
                if (coefArray != null) {
                    val coefList = referenceLists[index]
                    coefArray.forEach { element ->
                        if (element is kotlinx.serialization.json.JsonObject) {
                            val coefP = creerCoefPDepuisJson(element, index)
                            if (coefP != null) {
                                coefList.add(coefP)
                            }
                        }
                    }
                }
            }

            // Déduplication des coefficients par groupe (même nom et même valeur)
            reference.deduplicateCoefficients()
        } catch (e: Exception) { e.rethrowIfCancellation(); AppLog.d("ImportUtils", "Exception ignorée : ${e.message}") }
    }

    /** Crée un objet CoefP à partir d'un objet JSON */
    private fun creerCoefPDepuisJson(
            coefObj: kotlinx.serialization.json.JsonObject,
            groupUUID: Int
    ): fr.vetbrain.vetnutri_mp.Data.CoefP? {
        try {
            val description =
                    extraireStringDepuisJson(coefObj, "description")
                            ?: extraireStringDepuisJson(coefObj, "name") ?: ""
            val coef =
                    extraireDoubleDepuisJson(coefObj, "coef")
                            ?: extraireDoubleDepuisJson(coefObj, "value") ?: 0.0

            return fr.vetbrain.vetnutri_mp.Data.CoefP(
                    description = description,
                    coef = coef,
                    groupUUID = groupUUID
            )
        } catch (e: Exception) {
            e.rethrowIfCancellation()
            return null
        }
    }

    /** Extrait une chaîne d'un objet JSON */
    private fun extraireStringDepuisJson(
            obj: kotlinx.serialization.json.JsonObject,
            key: String
    ): String? {
        return (obj[key] as? kotlinx.serialization.json.JsonPrimitive)?.content
    }

    /** Extrait un booléen d'un objet JSON */
    private fun extraireBooleanDepuisJson(
            obj: kotlinx.serialization.json.JsonObject,
            key: String
    ): Boolean? {
        return (obj[key] as? kotlinx.serialization.json.JsonPrimitive)?.content?.toBoolean()
    }

    /** Extrait un entier d'un objet JSON */
    private fun extraireIntDepuisJson(
            obj: kotlinx.serialization.json.JsonObject,
            key: String
    ): Int? {
        return (obj[key] as? kotlinx.serialization.json.JsonPrimitive)?.content?.toIntOrNull()
    }

    /** Convertit une chaîne en énumération Espece */
    private fun convertirStringVersEspece(species: String): fr.vetbrain.vetnutri_mp.Enumer.Espece {
        return when (species.uppercase()) {
            "CHIEN", "DOG", "CANINE" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.CHIEN
            "CHAT", "CAT", "FELINE" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.CHAT
            "LAPIN", "RABBIT", "LAPINE" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.LAPIN
            "PRIMATE", "PRIMATES", "MONKEY", "APE" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.PRIMATE
            "RAT", "RATS" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.RAT
            "SOURIS", "MOUSE", "MICE" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.SOURIS
            "FURET", "FERRET", "FURETS" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.FURET
            "CHEVAL", "HORSE", "EQUINE", "CHEVAUX" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.CHEVAL
            "FELIN", "FELINS", "FELINES" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.FELIN
            "CANIN", "CANINS", "CANINES" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.CANIN
            "HERBIVORE", "HERBIVORES" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.HERBIVORE
            "FOLIVORE", "FOLIVORES" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.FOLIVORE
            "ALL", "CH", "TOUTES", "TOUS" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.CH
            else -> {
                // Essayer d'utiliser la méthode getFromString qui gère de nombreux cas
                val especeDetectee = fr.vetbrain.vetnutri_mp.Enumer.Espece.getFromString(species)
                if (especeDetectee != null) {
                    especeDetectee
                } else {
                    fr.vetbrain.vetnutri_mp.Enumer.Espece.CHIEN
                }
            }
        }
    }

    /**
     * Convertit une chaîne en énumération StadePhysio Inclut une logique intelligente pour détecter
     * le stade selon le contexte
     */
    private fun convertirStringVersStadePhysio(
            sPhysio: String
    ): fr.vetbrain.vetnutri_mp.Enumer.StadePhysio {
        return when (sPhysio.uppercase()) {
            "ADULTE", "ADULT" -> fr.vetbrain.vetnutri_mp.Enumer.StadePhysio.ADULTE
            "CROISSANCE", "GROWTH" -> fr.vetbrain.vetnutri_mp.Enumer.StadePhysio.CROISSANCE
            "GESTATION", "GESTANTE", "PREGNANT" ->
                    fr.vetbrain.vetnutri_mp.Enumer.StadePhysio.GESTATION
            "LACTATION", "LACTANTE", "LACTATING" ->
                    fr.vetbrain.vetnutri_mp.Enumer.StadePhysio.LACTATION
            else -> {
                fr.vetbrain.vetnutri_mp.Enumer.StadePhysio.ADULTE
            }
        }
    }

    /** Convertit une chaîne en énumération Reflevel */
    private fun convertirStringVersReflevel(
            reflevel: String
    ): fr.vetbrain.vetnutri_mp.Enumer.Reflevel {
        reflevel.trim().toIntOrNull()?.let { id ->
            LegacyV2Mappings.referenceLevelNames[id]?.let {
                return fr.vetbrain.vetnutri_mp.Enumer.Reflevel.valueOf(it)
            }
        }
        return when (reflevel.trim().uppercase()) {
            "MAX", "MAXIMUM" -> fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
            "OPTIMIN", "OPTIMUMMIN" -> fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
            "OPTIMAX", "OPTIMUMMAX" -> fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
            else -> fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
        }
    }

    /** Convertit une chaîne en énumération UnitEnum - amélioration pour les unités JSON réelles */
    /** Unité de la quantité ; null quand le fichier n'en précise pas d'exploitable (« NO », « % »). */
    private fun convertirStringVersUnitEnum(unit: String): fr.vetbrain.vetnutri_mp.Enumer.UnitEnum? {
        return when (unit.trim().uppercase()) {
            "G", "BUG" -> fr.vetbrain.vetnutri_mp.Enumer.UnitEnum.BUg
            "MG", "BUMG" -> fr.vetbrain.vetnutri_mp.Enumer.UnitEnum.BUmg
            "UG", "µG", "BUMU" -> fr.vetbrain.vetnutri_mp.Enumer.UnitEnum.BUmu
            "IU", "UI", "AUUI" -> fr.vetbrain.vetnutri_mp.Enumer.UnitEnum.AUui
            "DUI", "DUUI" -> fr.vetbrain.vetnutri_mp.Enumer.UnitEnum.DUui
            "KCAL" -> fr.vetbrain.vetnutri_mp.Enumer.UnitEnum.KCAL
            else -> null
        }
    }

    /**
     * Convertit une chaîne en énumération UnitReqEnum - amélioration pour les unités de besoin JSON
     * réelles
     */
    private fun convertirStringVersUnitReqEnum(
            unitReq: String
    ): fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum {
        // Codes V2 (MCAL, KGBW, KGMW, NO, PERC) : même correspondance que LegacyV2Mappings
        return when (unitReq.trim().uppercase()) {
            "KGBW", "PERKG" -> fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.PERKG
            "KGMW", "PERMS", "DM" -> fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.PERMS
            "MCAL", "PERKCAL" -> fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.PERKCAL
            "PERKJ" -> fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.PERKJ
            "PERC", "PERCENT", "%", "RATIO" -> fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.RATIO
            else -> fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.ABSOLUTE
        }
    }

    /**
     * Détecte le stade physiologique basé sur le nom de la référence si le champ sPhysio n'est pas
     * informatif
     */
    private fun detecterStadePhysioDepuisNom(
            nom: String,
            sPhysio: String
    ): fr.vetbrain.vetnutri_mp.Enumer.StadePhysio {
        val nomLower = nom.lowercase()

        // Si sPhysio est déjà informatif, l'utiliser
        if (sPhysio.uppercase() != "ADULTE") {
            return convertirStringVersStadePhysio(sPhysio)
        }

        // Sinon, détecter basé sur le nom
        return when {
            nomLower.contains("croissance") ||
                    nomLower.contains("growth") ||
                    nomLower.contains("chiot") ||
                    nomLower.contains("chaton") ||
                    nomLower.contains("jeune") ->
                    fr.vetbrain.vetnutri_mp.Enumer.StadePhysio.CROISSANCE
            nomLower.contains("gestation") ||
                    nomLower.contains("gestante") ||
                    nomLower.contains("pregnant") ->
                    fr.vetbrain.vetnutri_mp.Enumer.StadePhysio.GESTATION
            nomLower.contains("lactation") ||
                    nomLower.contains("lactante") ||
                    nomLower.contains("lactating") ||
                    nomLower.contains("allaitement") ->
                    fr.vetbrain.vetnutri_mp.Enumer.StadePhysio.LACTATION
            else -> fr.vetbrain.vetnutri_mp.Enumer.StadePhysio.ADULTE
        }
    }

    /**
     * Détecte si une référence correspond à une maladie basé sur son nom et d'autres indicateurs
     */
    private fun detecterMaladie(
            nom: String,
            disease: Boolean,
            nameDisease: String
    ): Pair<Boolean, String> {
        val nomLower = nom.lowercase()

        // Si disease est explicitement défini et vrai
        if (disease && nameDisease.isNotBlank()) {
            return Pair(true, nameDisease)
        }

        // Si disease est défini mais nameDisease est vide, utiliser le nom comme nom de maladie
        if (disease) {
            return Pair(true, nom)
        }

        // Détection basée sur le nom
        val indicateursMaladie =
                listOf(
                        "cardio",
                        "cardiac",
                        "heart",
                        "cardiaque",
                        "renal",
                        "kidney",
                        "rénal",
                        "rein",
                        "hepatic",
                        "liver",
                        "hépatique",
                        "foie",
                        "diabete",
                        "diabetes",
                        "diabétique",
                        "obese",
                        "obesity",
                        "obésité",
                        "cancer",
                        "oncol",
                        "tumeur",
                        "allergi",
                        "allergy",
                        "allergique",
                        "gastro",
                        "digestif",
                        "intestinal",
                        "arthro",
                        "joint",
                        "articulaire"
                )

        val estMaladie = indicateursMaladie.any { nomLower.contains(it) }
        val nomMaladie = if (estMaladie) nom else ""

        return Pair(estMaladie, nomMaladie)
    }

    /** Vérifie si le contenu correspond à un fichier de références nutritionnelles (.vbnr.json) */
    private fun isNutritionalRequirementJsonContent(content: String): Boolean {
        return try {
            val jsonElement = json.parseToJsonElement(content)

            // Vérifier si c'est un tableau d'objets avec les clés requises
            if (jsonElement is kotlinx.serialization.json.JsonArray && jsonElement.isNotEmpty()) {
                val firstElement = jsonElement.first()
                if (firstElement is kotlinx.serialization.json.JsonObject) {
                    val requiredKeys =
                            listOf(
                                    "reference",
                                    "allEquations",
                                    "allBibliographicReferences",
                                    "nutrientRequirements"
                            )
                    val hasAllRequiredKeys =
                            requiredKeys.all { key -> firstElement.containsKey(key) }

                    if (hasAllRequiredKeys) {
                        
                        return true
                    }

                    // Fallback: vérifier au moins 3 des 4 clés requises
                    val keyCount = requiredKeys.count { key -> firstElement.containsKey(key) }
                    if (keyCount >= 3) {
                        return true
                    }
                }
            } else if (jsonElement is kotlinx.serialization.json.JsonObject) {
                // Vérifier si c'est un objet unique avec la structure NutritionalRequirementData
                val requiredKeys =
                        listOf(
                                "reference",
                                "allEquations",
                                "allBibliographicReferences",
                                "nutrientRequirements"
                        )
                val hasAllRequiredKeys = requiredKeys.all { key -> jsonElement.containsKey(key) }

                if (hasAllRequiredKeys) {
                    
                    return true
                }

                // Fallback: vérifier au moins 3 des 4 clés requises
                val keyCount = requiredKeys.count { key -> jsonElement.containsKey(key) }
                if (keyCount >= 3) {
                    return true
                }
            }

            // Fallback final: chercher des mots-clés caractéristiques dans le contenu
            val contentLower = content.lowercase()
            val nutritionalKeywords =
                    listOf(
                            "reference",
                            "nutrient",
                            "equation",
                            "bibliographic",
                            "species",
                            "sphysio",
                            "disease",
                            "allequations",
                            "nutritionalrequirement",
                            "reflevel"
                    )
            val keywordCount = nutritionalKeywords.count { contentLower.contains(it) }

            if (keywordCount >= 4) {
                return true
            }

            return false
        } catch (e: Exception) {
            e.rethrowIfCancellation()
            return false
        }
    }

    /** Sauvegarde automatiquement tous les éléments importés dans la base de données */
    suspend fun sauvegarderDonneesImportees(
            referencesImportees: List<fr.vetbrain.vetnutri_mp.Data.ReferenceEv>,
            databaseReferenceEvRepository:
                    fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository?,
            equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository?,
            biblioRefRepository: fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository?
    ): SauvegardeResult {
        var referencesAjoutees = 0
        var referencesMisesAJour = 0
        var equationsAjoutees = 0
        var equationsMisesAJour = 0
        var bibliographiesAjoutees = 0
        var bibliographiesMisesAJour = 0
        val erreurs = mutableListOf<String>()

        try {
            // 1. Sauvegarder toutes les bibliographies d'abord (références)
            if (biblioRefRepository != null) {
                val toutesLesBibliographies = mutableSetOf<fr.vetbrain.vetnutri_mp.Data.BiblioRef>()

                for (reference in referencesImportees) {
                    // Collecter toutes les bibliographies de la référence
                    reference.getAllBiblioRefs().forEach {
                            biblio: fr.vetbrain.vetnutri_mp.Data.BiblioRef ->
                        if (biblio.uuid.isNotBlank() && biblio.firstAuthor.isNotBlank()) {
                            toutesLesBibliographies.add(biblio)
                        }
                    }

                    // Collecter les bibliographies des équations assignées aux propriétés
                    // individuelles
                    reference.equationBEE?.bib?.let { biblio ->
                        if (biblio.uuid.isNotBlank() && biblio.firstAuthor.isNotBlank()) {
                            toutesLesBibliographies.add(biblio)
                        }
                    }

                    reference.equationBW?.bib?.let { biblio ->
                        if (biblio.uuid.isNotBlank() && biblio.firstAuthor.isNotBlank()) {
                            toutesLesBibliographies.add(biblio)
                        }
                    }

                    reference.equationDEraw?.bib?.let { biblio ->
                        if (biblio.uuid.isNotBlank() && biblio.firstAuthor.isNotBlank()) {
                            toutesLesBibliographies.add(biblio)
                        }
                    }

                    reference.equationDEcom?.bib?.let { biblio ->
                        if (biblio.uuid.isNotBlank() && biblio.firstAuthor.isNotBlank()) {
                            toutesLesBibliographies.add(biblio)
                        }
                    }

                    // Bibliographies des équations nutritionnelles
                    reference.equationsNut.forEach { equation ->
                        equation.bib?.let { biblio ->
                            if (biblio.uuid.isNotBlank() && biblio.firstAuthor.isNotBlank()) {
                                toutesLesBibliographies.add(biblio)
                            }
                        }
                    }
                }

                for (bibliographie in toutesLesBibliographies) {
                    try {
                        val existante = biblioRefRepository.getBiblioRefById(bibliographie.uuid)
                        if (existante != null) {
                            if (existante != bibliographie) {
                                biblioRefRepository.updateBiblioRef(bibliographie)
                                bibliographiesMisesAJour++
                            }
                        } else {
                            biblioRefRepository.insertBiblioRef(bibliographie)
                            bibliographiesAjoutees++
                        }
                    } catch (e: Exception) {
                        e.rethrowIfCancellation()
                        erreurs.add(
                                "Erreur bibliographie ${bibliographie.firstAuthor}: ${e.message}"
                        )
                    }
                }
            }

            // 2. Sauvegarder toutes les équations ensuite
            if (equationRepository != null) {
                val toutesLesEquations = mutableSetOf<fr.vetbrain.vetnutri_mp.Data.Equation>()

                for (reference in referencesImportees) {
                    // Collecter toutes les équations de la référence
                    // Équations des listes existantes
                    reference.getAllEquations().forEach {
                            equation: fr.vetbrain.vetnutri_mp.Data.Equation ->
                        if (equation.uuid.isNotBlank() && equation.name.isNotBlank()) {
                            toutesLesEquations.add(equation)
                        }
                    }

                    // Équations assignées aux propriétés individuelles (nouvellement importées)
                    reference.equationBEE?.let { equation ->
                        if (equation.uuid.isNotBlank() && equation.name.isNotBlank()) {
                            toutesLesEquations.add(equation)
                        }
                    }

                    reference.equationBW?.let { equation ->
                        if (equation.uuid.isNotBlank() && equation.name.isNotBlank()) {
                            toutesLesEquations.add(equation)
                        }
                    }

                    reference.equationDEraw?.let { equation ->
                        if (equation.uuid.isNotBlank() && equation.name.isNotBlank()) {
                            toutesLesEquations.add(equation)
                        }
                    }

                    reference.equationDEcom?.let { equation ->
                        if (equation.uuid.isNotBlank() && equation.name.isNotBlank()) {
                            toutesLesEquations.add(equation)
                        }
                    }

                    // Équations dans la liste equationsNut
                    reference.equationsNut.forEach { equation ->
                        if (equation.uuid.isNotBlank() && equation.name.isNotBlank()) {
                            toutesLesEquations.add(equation)
                        }
                    }
                }

                for (equation in toutesLesEquations) {
                    try {
                        val existante = equationRepository.getEquationById(equation.uuid)
                        if (existante != null) {
                            if (existante != equation) {
                                equationRepository.updateEquation(equation)
                                equationsMisesAJour++
                            }
                        } else {
                            equationRepository.saveEquation(equation)
                            equationsAjoutees++
                        }
                    } catch (e: Exception) {
                        e.rethrowIfCancellation()
                        erreurs.add("Erreur équation ${equation.name}: ${e.message}")
                    }
                }
            }

            // 3. Sauvegarder les références nutritionnelles enfin
            if (databaseReferenceEvRepository != null) {
                for (reference in referencesImportees) {
                    try {
                        val existante =
                                databaseReferenceEvRepository.getReferenceEvById(reference.uuid)
                        if (existante != null) {
                            if (existante != reference) {
                                databaseReferenceEvRepository.updateReferenceEv(reference)
                                referencesMisesAJour++
                            }
                        } else {
                            databaseReferenceEvRepository.saveReferenceEv(reference)
                            referencesAjoutees++
                            
                        }
                    } catch (e: Exception) {
                        e.rethrowIfCancellation()
                        erreurs.add("Erreur référence ${reference.nom}: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            e.rethrowIfCancellation()
            erreurs.add("Erreur générale de sauvegarde: ${e.message}")
        }

        return SauvegardeResult(
                referencesAjoutees = referencesAjoutees,
                referencesMisesAJour = referencesMisesAJour,
                equationsAjoutees = equationsAjoutees,
                equationsMisesAJour = equationsMisesAJour,
                bibliographiesAjoutees = bibliographiesAjoutees,
                bibliographiesMisesAJour = bibliographiesMisesAJour,
                erreurs = erreurs
        )
    }

    /**
     * Importe des références nutritionnelles depuis un fichier JSON .vbnr avec sauvegarde
     * automatique en base de données
     *
     * @param jsonContent Le contenu JSON du fichier .vbnr.json à importer
     * @param databaseReferenceEvRepository Repository pour sauvegarder les références (optionnel)
     * @param equationRepository Repository pour sauvegarder les équations (optionnel)
     * @param biblioRefRepository Repository pour sauvegarder les bibliographies (optionnel)
     * @param sauvegarderEnBase Si true, sauvegarde automatiquement en base de données
     * @return La liste des références nutritionnelles importées
     */
    suspend fun importNutritionalRequirementsFromJson(
            jsonContent: String,
            databaseReferenceEvRepository:
                    fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository? =
                    null,
            equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository? = null,
            biblioRefRepository: fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository? = null,
            sauvegarderEnBase: Boolean = true
    ): List<fr.vetbrain.vetnutri_mp.Data.ReferenceEv> {
        val references = lireReferencesNutritionnelles(jsonContent)
        if (sauvegarderEnBase && references.isNotEmpty()) {
            val resultat =
                    sauvegarderDonneesImportees(
                            references,
                            databaseReferenceEvRepository,
                            equationRepository,
                            biblioRefRepository
                    )
            resultat.erreurs.forEach { AppLog.w("ImportUtils", it) }
        }
        return references
    }

    /**
     * Extrait les sections foods et references d'un fichier vetnutri_export_init.json
     * @param jsonContent Le contenu JSON du fichier
     * @return Pair contenant la liste des aliments et la liste des références
     */
    fun extractSectionsFromVetNutriInit(jsonContent: String): Pair<List<AlimentEvJson>, String> {
        return try {
            val jsonElement = json.parseToJsonElement(jsonContent)
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
            e.rethrowIfCancellation()
            AppLog.e("ImportUtils", e.message ?: "Erreur", e)
            Pair(emptyList(), "")
        }
    }

    /** Extrait un double d'un objet JSON */
    private fun extraireDoubleDepuisJson(
            obj: kotlinx.serialization.json.JsonObject,
            key: String
    ): Double? {
        return (obj[key] as? kotlinx.serialization.json.JsonPrimitive)?.content?.toDoubleOrNull()
    }
}

/** Classe contenant le résultat de l'importation */
data class ImportResult(val animals: List<AnimalEvJson>, val foods: List<AlimentEvJson>)

/** Classe de données pour représenter un besoin nutritionnel lors de l'importation */
data class NutrientRequirementInfo(
        val nutrient: fr.vetbrain.vetnutri_mp.Enumer.Nutrient,
        val referenceLevel: fr.vetbrain.vetnutri_mp.Enumer.Reflevel,
        val quantity: Double,
        val unit: fr.vetbrain.vetnutri_mp.Enumer.UnitEnum?,
        val unitRequirement: fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum,
        val bibliographicReference: fr.vetbrain.vetnutri_mp.Data.BiblioRef?
)

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
