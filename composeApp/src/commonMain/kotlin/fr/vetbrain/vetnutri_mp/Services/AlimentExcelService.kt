package fr.vetbrain.vetnutri_mp.Services

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentExcelRow
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service pour l'import/export des aliments vers/depuis Excel
 * Utilise le format CSV pour la compatibilité multiplateforme
 */
class AlimentExcelService {

    /**
     * Exporte une liste d'aliments vers un format CSV
     */
    fun exportToCsv(aliments: List<AlimentEv>): String {
        val csvLines = mutableListOf<String>()

        // En-têtes
        val headers = createCsvHeaders()
        csvLines.add(headers.joinToString(";"))

        // Données
        aliments.forEach { aliment ->
            val row = AlimentExcelRow.fromAlimentEv(aliment)
            val csvLine = createCsvLine(row)
            csvLines.add(csvLine)
        }

        return csvLines.joinToString("\n")
    }

    /**
     * Importe des aliments depuis un CSV avec logs détaillés pour le débogage
     */
    fun importFromCsv(csvContent: String): ImportResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        try {
            logInfo("=== DÉBUT IMPORT CSV ===")
            logInfo("Taille du contenu CSV: ${csvContent.length} caractères")
            
            val lines = csvContent.lines().filter { it.isNotBlank() }
            logInfo("Nombre de lignes non vides: ${lines.size}")
            
            if (lines.isEmpty()) {
                logError("Le fichier CSV est vide")
                return ImportResult(emptyList(), listOf("Le fichier CSV est vide"))
            }

            // Analyse des en-têtes
            val headers = lines.first().split(";").map { it.trim() }
            logInfo("En-têtes détectés (${headers.size}): ${headers.take(5).joinToString(", ")}${if (headers.size > 5) "..." else ""}")
            logInfo("TOUS LES HEADERS: ${headers.joinToString("|")}")
            
            // Vérification des en-têtes attendus
            val expectedHeaders = createCsvHeaders()
            logInfo("En-têtes attendus (${expectedHeaders.size}): ${expectedHeaders.take(5).joinToString(", ")}${if (expectedHeaders.size > 5) "..." else ""}")
            
            val dataLines = lines.drop(1)
            logInfo("Nombre de lignes de données: ${dataLines.size}")

            val aliments = mutableListOf<AlimentEv>()
            var successCount = 0
            var errorCount = 0

            dataLines.forEachIndexed { index, line ->
                try {
                    logInfo("--- Traitement ligne ${index + 2} ---")
                    logInfo("Contenu ligne: ${line.take(100)}${if (line.length > 100) "..." else ""}")
                    
                    val row = parseCsvLine(line, headers, index + 2)
                    logInfo("AlimentExcelRow créé: nom='${row.nom}', nutriments=${row.nutriments.size}")
                    
                    val aliment = AlimentExcelRow.toAlimentEv(row)
                    logInfo("AlimentEv créé: nom='${aliment.nom}', nutriments=${aliment.valMap.size}")
                    
                    aliments.add(aliment)
                    successCount++
                    logInfo("✅ Ligne ${index + 2} importée avec succès")
                    
                } catch (e: Exception) {
                    errorCount++
                    val errorMsg = "❌ Erreur ligne ${index + 2}: ${e.message}"
                    logError("$errorMsg - Exception: ${e::class.simpleName}")
                    logError("Stack trace: ${e.stackTraceToString()}")
                    errors.add(errorMsg)
                }
            }

            logInfo("=== RÉSULTAT IMPORT ===")
            logInfo("✅ Succès: $successCount lignes")
            logInfo("❌ Erreurs: $errorCount lignes")
            logInfo("⚠️ Avertissements: ${warnings.size}")
            logInfo("Total aliments importés: ${aliments.size}")

            return ImportResult(aliments, errors + warnings)
            
        } catch (e: Exception) {
            logError("Erreur critique lors de l'import CSV: ${e.message}")
            logError("Stack trace: ${e.stackTraceToString()}")
            return ImportResult(emptyList(), listOf("Erreur critique: ${e.message}"))
        }
    }

    /**
     * Crée les en-têtes du CSV
     */
    private fun createCsvHeaders(): List<String> {
        val headers = mutableListOf(
            "UUID",
            "Nom",
            "Marque",
            "Gamme",
            "Ingrédients",
            "Groupe Alimentaire",
            "Type Aliment",
            "Conditionnement",
            "Prix",
            "Catégorie Prix",
            "Quantité Interne",
            "Consistant",
            "Obsolète",
            "Données Base",
            "Espèces",
            "Indications",
            "UUID Ration"
        )

        // Ajouter les colonnes de nutriments avec unité dans l'en-tête
        AlimentExcelRow.ALL_NUTRIENTS.forEach { nutrient ->
            // Trouver le nutriment correspondant pour obtenir son unité
            val nutrientEnum = findNutrientByLabel(nutrient)
            val unitName = nutrientEnum?.ue?.displayName ?: ""
            headers.add("$nutrient ($unitName)")
        }

        return headers
    }

    /**
     * Trouve un nutriment par son label dans tous les enums
     */
    private fun findNutrientByLabel(label: String): Nutrient? {
        // Chercher dans NutrientMain
        NutrientMain.values().forEach { nutrient ->
            if (nutrient.label == label) return nutrient
        }
        
        // Chercher dans NutrientVitam
        NutrientVitam.values().forEach { nutrient ->
            if (nutrient.label == label) return nutrient
        }
        
        // Chercher dans NutrientMin
        NutrientMin.values().forEach { nutrient ->
            if (nutrient.label == label) return nutrient
        }
        
        // Chercher dans NutrientLipid
        NutrientLipid.values().forEach { nutrient ->
            if (nutrient.label == label) return nutrient
        }
        
        // Chercher dans AAEnum
        AAEnum.values().forEach { nutrient ->
            if (nutrient.label == label) return nutrient
        }
        
        return null
    }

    /**
     * Crée une ligne CSV à partir d'un AlimentExcelRow
     */
    private fun createCsvLine(row: AlimentExcelRow): String {
        val values = mutableListOf(
            escapeCsvValue(row.uuid),
            escapeCsvValue(row.nom ?: ""),
            escapeCsvValue(row.brand ?: ""),
            escapeCsvValue(row.gamme ?: ""),
            escapeCsvValue(row.ingredients ?: ""),
            escapeCsvValue(row.groupAlim ?: ""),
            escapeCsvValue(row.typeAliment ?: ""),
            escapeCsvValue(row.contEnum ?: ""),
            row.price?.toString() ?: "",
            escapeCsvValue(row.categPrice ?: ""),
            row.quantInt?.toString() ?: "",
            row.consistent.toString(),
            row.deprecated.toString(),
            escapeCsvValue(row.dataB ?: ""),
            escapeCsvValue(row.especes ?: ""),
            escapeCsvValue(row.indications ?: ""),
            escapeCsvValue(row.rationUUID ?: "")
        )

        // Ajouter les valeurs des nutriments
        AlimentExcelRow.ALL_NUTRIENTS.forEach { nutrient ->
            val valeur = row.nutriments[nutrient]
            values.add(valeur?.toString() ?: "")
        }

        return values.joinToString(";")
    }

    /**
     * Parse une ligne CSV en AlimentExcelRow avec logs détaillés
     */
    private fun parseCsvLine(line: String, headers: List<String>, lineNumber: Int): AlimentExcelRow {
        logInfo("Parsing ligne $lineNumber: ${line.take(50)}...")
        
        val values = parseCsvValues(line)
        logInfo("Valeurs parsées: ${values.size} colonnes")
        
        if (values.size != headers.size) {
            logError("Ligne $lineNumber: Nombre de colonnes incorrect - attendu ${headers.size}, trouvé ${values.size}")
            logError("Headers: ${headers.joinToString("|")}")
            logError("Values: ${values.joinToString("|")}")
            throw IllegalArgumentException("Nombre de colonnes incorrect: attendu ${headers.size}, trouvé ${values.size}")
        }

        // Créer une map header -> valeur pour un accès par nom
        val headerValueMap = headers.zip(values).toMap()
        logInfo("Map header->valeur créée avec ${headerValueMap.size} entrées")

        // Informations de base
        val uuid = headerValueMap["UUID"] ?: ""
        val nom = headerValueMap["Nom"]?.takeIf { it.isNotBlank() }
        val brand = headerValueMap["Marque"]?.takeIf { it.isNotBlank() }
        val gamme = headerValueMap["Gamme"]?.takeIf { it.isNotBlank() }
        val ingredients = headerValueMap["Ingrédients"]?.takeIf { it.isNotBlank() }
        
        logInfo("DEBUG - Première colonne: '${headers.first()}' = '${values.first()}'")
        logInfo("DEBUG - Mapping UUID: headerValueMap['UUID'] = '${headerValueMap["UUID"]}'")
        logInfo("Infos de base - UUID: '$uuid', Nom: '$nom', Marque: '$brand'")

        // Classification
        val groupAlim = headerValueMap["Groupe Alimentaire"]?.takeIf { it.isNotBlank() }
        val typeAliment = headerValueMap["Type Aliment"]?.takeIf { it.isNotBlank() }
        val contEnum = headerValueMap["Conditionnement"]?.takeIf { it.isNotBlank() }
        
        logInfo("Classification - Groupe: '$groupAlim', Type: '$typeAliment', Conditionnement: '$contEnum'")

        // Prix et quantité
        val price = headerValueMap["Prix"]?.toDoubleOrNull()
        val categPrice = headerValueMap["Catégorie Prix"]?.takeIf { it.isNotBlank() }
        val quantInt = headerValueMap["Quantité Interne"]?.toDoubleOrNull()
        
        logInfo("Prix/Quantité - Prix: $price, Catégorie: '$categPrice', Quantité: $quantInt")

        // Statuts
        val consistent = headerValueMap["Consistant"]?.toBooleanStrictOrNull() ?: false
        val deprecated = headerValueMap["Obsolète"]?.toBooleanStrictOrNull() ?: false
        val dataB = headerValueMap["Données Base"]?.takeIf { it.isNotBlank() }
        
        logInfo("Statuts - Consistant: $consistent, Obsolète: $deprecated, DataB: '$dataB'")

        // Espèces et indications
        val especes = headerValueMap["Espèces"]?.takeIf { it.isNotBlank() }
        val indications = headerValueMap["Indications"]?.takeIf { it.isNotBlank() }
        
        logInfo("Espèces/Indications - Espèces: '$especes', Indications: '$indications'")
        logInfo("Debug headerValueMap pour Espèces: ${headerValueMap.filterKeys { it.contains("Espèces") }}")
        logInfo("Debug headerValueMap pour Indications: ${headerValueMap.filterKeys { it.contains("Indications") }}")

        // Ration
        val rationUUID = headerValueMap["UUID Ration"]?.takeIf { it.isNotBlank() }
        
        logInfo("Ration UUID: '$rationUUID'")

        // Nutriments - correspondance robuste par résolution de label
        val nutrimentsMap = mutableMapOf<String, Double?>()
        val fixedHeaders = setOf(
                "UUID",
                "Nom",
                "Marque",
                "Gamme",
                "Ingrédients",
                "Groupe Alimentaire",
                "Type Aliment",
                "Conditionnement",
                "Prix",
                "Catégorie Prix",
                "Quantité Interne",
                "Consistant",
                "Obsolète",
                "Données Base",
                "Espèces",
                "Indications",
                "UUID Ration"
        )

        logInfo("Traitement des nutriments...")
        var nutrimentCount = 0
        var nutrimentErrorCount = 0

        headers.forEach { header ->
            if (header !in fixedHeaders) {
                val rawName = header.substringBefore("(").trim().removeSuffix("")
                val valueStr = headerValueMap[header]
                
                // Vérifier si c'est vraiment une colonne de nutriment
                if (isNutrientColumn(rawName)) {
                    val value = valueStr?.toDoubleOrNull()
                    
                    if (value != null) {
                        logInfo("Traitement nutriment: '$rawName' = $value")
                        
                        val resolved = fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(rawName)
                        if (resolved != null) {
                            nutrimentsMap[resolved.label] = value
                            nutrimentCount++
                            logInfo("✅ Nutriment résolu: '$rawName' -> '${resolved.label}'")
                        } else {
                            // Fallback: essayer aussi sans espaces/avec normalisation simple
                            val alt = rawName.replace("_", " ").replace("-", " ").trim()
                            val resolvedAlt = fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(alt)
                            if (resolvedAlt != null) {
                                nutrimentsMap[resolvedAlt.label] = value
                                nutrimentCount++
                                logInfo("✅ Nutriment résolu (fallback): '$rawName' -> '$alt' -> '${resolvedAlt.label}'")
                            } else {
                                nutrimentErrorCount++
                                logError("❌ Nutriment non résolu: '$rawName' (valeur: $value)")
                            }
                        }
                    } else if (valueStr?.isNotBlank() == true) {
                        logError("❌ Valeur nutriment non numérique: '$rawName' = '$valueStr'")
                    }
                } else {
                    logInfo("Colonne ignorée (non-nutriment): '$rawName' = '$valueStr'")
                }
            }
        }
        
        logInfo("Résultat nutriments: $nutrimentCount résolus, $nutrimentErrorCount erreurs")

        return AlimentExcelRow(
            uuid = uuid,
            nom = nom,
            brand = brand,
            gamme = gamme,
            ingredients = ingredients,
            groupAlim = groupAlim,
            typeAliment = typeAliment,
            contEnum = contEnum,
            price = price,
            categPrice = categPrice,
            quantInt = quantInt,
            consistent = consistent,
            deprecated = deprecated,
            dataB = dataB,
            especes = especes,
            indications = indications,
            rationUUID = rationUUID,
            nutriments = nutrimentsMap
        )
    }

    /**
     * Parse les valeurs CSV en tenant compte des guillemets et des points-virgules
     */
    private fun parseCsvValues(line: String): List<String> {
        val values = mutableListOf<String>()
        var currentValue = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val char = line[i]

            when {
                char == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        // Échapper les guillemets doubles
                        currentValue.append('"')
                        i++ // Sauter le prochain guillemet
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                char == ';' && !inQuotes -> {
                    values.add(currentValue.toString().trim())
                    currentValue = StringBuilder()
                }
                else -> {
                    currentValue.append(char)
                }
            }
            i++
        }

        // Ajouter la dernière valeur
        values.add(currentValue.toString().trim())

        return values
    }

    /**
     * Échappe les valeurs CSV qui contiennent des caractères spéciaux
     */
    private fun escapeCsvValue(value: String): String {
        return if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    /**
     * Vérifie si une colonne est vraiment un nutriment
     */
    private fun isNutrientColumn(columnName: String): Boolean {
        // Colonnes connues qui ne sont PAS des nutriments
        val nonNutrientColumns = setOf(
            "UUID", "ID", "REF", "REFERENCE", "CODE", "SKU",
            "VERSION", "DATE", "TIMESTAMP", "CREATED", "UPDATED",
            "USER", "AUTHOR", "SOURCE", "FILE", "PATH", "URL",
            "STATUS", "STATE", "FLAG", "TYPE", "CATEGORY",
            "DESCRIPTION", "COMMENT", "NOTE", "REMARK",
            "TAG", "LABEL", "NAME", "TITLE"
        )
        
        val normalizedName = columnName.uppercase().trim()
        
        // Si c'est une colonne connue non-nutriment, retourner false
        if (normalizedName in nonNutrientColumns) {
            return false
        }
        
        // Vérifier si le nom correspond à un nutriment connu
        val isKnownNutrient = AlimentExcelRow.ALL_NUTRIENTS.any { 
            it.uppercase() == normalizedName 
        }
        
        if (isKnownNutrient) {
            return true
        }
        
        // Vérifier avec le NutrientResolver
        val resolved = fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(columnName)
        return resolved != null
    }

    /**
     * Fonctions de logging pour le débogage
     */
    private fun logInfo(message: String) {
        println("[CSV-IMPORT-INFO] $message")
    }
    
    private fun logError(message: String) {
        println("[CSV-IMPORT-ERROR] $message")
    }
    
    private fun logWarning(message: String) {
        println("[CSV-IMPORT-WARNING] $message")
    }

    /**
     * Résultat de l'import
     */
    data class ImportResult(
        val aliments: List<AlimentEv>,
        val errors: List<String>
    )

    companion object {
        /**
         * Génère un exemple de fichier CSV avec toutes les colonnes
         */
        fun generateExampleCsv(): String {
            val service = AlimentExcelService()

            // Créer un aliment exemple
            val exempleAliment = AlimentEv(
                uuid = genUUID(),
                nom = "Croquettes Premium Chat",
                brand = "Royal Canin",
                gamme = "Sterilised",
                ingredients = "Viande de poulet, riz, maïs, gluten de blé...",
                group = GroupAlim.FCOMP,
                typeAliment = FoodKind.COMPLET,
                cont = ContEnum.SACHET,
                price = 45.99,
                categPrice = "Premium",
                quantInt = 2.0,
                consistent = true,
                especes = mutableListOf("Chat"),
                indicat = mutableListOf(AlimIndic.NEUT, AlimIndic.SEN)
            ).apply {
                // Ajouter quelques nutriments exemple
                setNutrient(NutrientMain.PROTEINE, 30.0)
                setNutrient(NutrientMain.LIPIDE, 12.0)
                setNutrient(NutrientMain.ENERGIE, 350.0)
                setNutrient(NutrientVitam.VITA, 15000.0)
            }

            return service.exportToCsv(listOf(exempleAliment))
        }
    }
}
