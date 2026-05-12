package fr.vetbrain.vetnutri_mp.Service

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentExcelRow
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Utils.DataB
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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
     * @param csvContent Le contenu du fichier CSV
     * @param dataB La base de données à utiliser (prioritaire sur celle du CSV, null pour utiliser celle du CSV)
     */
    fun importFromCsv(csvContent: String, dataB: String? = null): ImportResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        try {
            logInfo("=== DÉBUT IMPORT CSV ===")
            logInfo("Taille du contenu CSV: ${csvContent.length} caractères")
            val sanitizedCsvContent = csvContent.removePrefix("\uFEFF")
            
            val rows = splitCsvRows(sanitizedCsvContent).filter { it.isNotBlank() }
            logInfo("Nombre de lignes CSV non vides (quote-aware): ${rows.size}")
            
            if (rows.isEmpty()) {
                logError("Le fichier CSV est vide")
                return ImportResult(emptyList(), listOf("Le fichier CSV est vide"))
            }

            // Analyse des en-têtes
            val headers = parseCsvValues(rows.first()).map { it.removePrefix("\uFEFF").trim() }
            logInfo("En-têtes détectés (${headers.size}): ${headers.take(5).joinToString(", ")}${if (headers.size > 5) "..." else ""}")
            logInfo("TOUS LES HEADERS: ${headers.joinToString("|")}")
            
            // Vérification des en-têtes attendus
            val expectedHeaders = createCsvHeaders()
            logInfo("En-têtes attendus (${expectedHeaders.size}): ${expectedHeaders.take(5).joinToString(", ")}${if (expectedHeaders.size > 5) "..." else ""}")
            
            val dataLines = rows.drop(1)
            logInfo("Nombre de lignes de données: ${dataLines.size}")

            val aliments = mutableListOf<AlimentEv>()
            var successCount = 0
            var errorCount = 0

            dataLines.forEachIndexed { index, line ->
                try {
                    logInfo("--- Traitement ligne ${index + 2} ---")
                    logInfo("Contenu ligne: ${line.take(100)}${if (line.length > 100) "..." else ""}")
                    
                    val row = parseCsvLine(line, headers, index + 2, dataB)
                    logInfo("AlimentExcelRow créé: nom='${row.nom}', nutriments=${row.nutriments.size}, dataB='${row.dataB}'")
                    
                    val aliment = AlimentExcelRow.toAlimentEv(row)
                    logInfo("AlimentEv créé: nom='${aliment.nom}', nutriments=${aliment.valMap.size}, dataB='${aliment.dataB}'")
                    
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
            "Date dernière mise à jour",
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
        val exportDate = row.lastUpdateDate
            ?: Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
                .toString()
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
            escapeCsvValue(exportDate),
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
     * @param line La ligne CSV à parser
     * @param headers Les en-têtes de colonnes
     * @param lineNumber Le numéro de ligne (pour les logs)
     * @param dataBPriority La base de données à utiliser en priorité (null pour utiliser celle du CSV)
     */
    private fun parseCsvLine(line: String, headers: List<String>, lineNumber: Int, dataBPriority: String? = null): AlimentExcelRow {
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
        val uuidRaw = headerValueMap.trouverValeurColonne(
            listOf(
                "UUID",
                "\uFEFFUUID",
                "ï»¿UUID"
            )
        )?.trim()
        val uuid = uuidRaw?.takeIf { it.isNotBlank() } ?: genUUID().also {
            logWarning("Ligne $lineNumber: UUID manquant, UUID généré automatiquement: $it")
        }
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

        // Prix et quantité (recherche tolérante pour gérer les problèmes d'encodage d'accents)
        val price = headerValueMap["Prix"]?.toDoubleOrNullWithComma()
        val categPrice = headerValueMap["Catégorie Prix"]?.takeIf { it.isNotBlank() }
        val quantIntRaw = headerValueMap.trouverValeurColonne(
            listOf(
                "Quantité Interne",
                "Quantite Interne",
                "Quantit� Interne"
            )
        )
        val quantInt = quantIntRaw?.toDoubleOrNullWithComma()
        
        logInfo("Prix/Quantité - Prix: $price, Catégorie: '$categPrice', Quantité: $quantInt")

        // Statuts
        val consistent = headerValueMap["Consistant"]?.toBooleanStrictOrNull() ?: false
        val deprecated = headerValueMap["Obsolète"]?.toBooleanStrictOrNull() ?: false
        val lastUpdateDateRaw = headerValueMap.trouverValeurColonne(
            listOf(
                "Date dernière mise à jour",
                "Date derniere mise a jour",
                "Date mise a jour",
                "Date mise à jour",
                "Date MAJ",
                "DateMaj"
            )
        )
        val lastUpdateDate = lastUpdateDateRaw?.takeIf { it.isNotBlank() }
            ?: Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
                .toString()
        val dataBFromCsvRaw = headerValueMap.trouverValeurColonne(
            listOf(
                "Données Base",
                "Donnees Base",
                "Donn�es Base",
                "DataB",
                "Data B",
                "Base de données",
                "Base de donnees"
            )
        )?.takeIf { it.isNotBlank() }
        val dataBFromCsv = dataBFromCsvRaw?.let { normalizeDataBValue(it) }
        val dataBPriorityNormalized = dataBPriority?.takeIf { it.isNotBlank() }?.let { normalizeDataBValue(it) }
        // Utiliser la valeur prioritaire si fournie, sinon celle du CSV
        val dataB = dataBPriorityNormalized ?: dataBFromCsv
        
        logInfo("Statuts - Consistant: $consistent, Obsolète: $deprecated, Date MAJ: '$lastUpdateDate', DataB (CSV brute): '$dataBFromCsvRaw', DataB (CSV normalisée): '$dataBFromCsv', DataB (prioritaire): '$dataBPriority', DataB (final): '$dataB'")

        // Espèces et indications (recherche tolérante)
        val especesRaw = headerValueMap.trouverValeurColonne(
            listOf(
                "Espèces",
                "Especes",
                "Esp�ces"
            )
        )
        val especes = especesRaw?.takeIf { it.isNotBlank() }
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
                "Date dernière mise à jour",
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
                
                // Extraire l'unité depuis l'en-tête (entre parenthèses)
                val unitInHeader = extractUnitFromHeader(header)
                
                // Vérifier si c'est vraiment une colonne de nutriment
                if (isNutrientColumn(rawName)) {
                    val rawValue = valueStr?.toDoubleOrNullWithComma()
                    
                    if (rawValue != null) {
                        // Convertir la valeur selon l'unité détectée
                        val convertedValue = convertNutrientValue(rawValue, unitInHeader, rawName)
                        
                        logInfo("Traitement nutriment: '$rawName' = $rawValue (unité: '$unitInHeader' -> converti: $convertedValue)")
                        
                        val resolved = fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(rawName)
                        if (resolved != null) {
                            nutrimentsMap[resolved.label] = convertedValue
                            nutrimentCount++
                            logInfo("✅ Nutriment résolu: '$rawName' -> '${resolved.label}' (valeur: $convertedValue)")
                        } else {
                            // Fallback: essayer aussi sans espaces/avec normalisation simple
                            val alt = rawName.replace("_", " ").replace("-", " ").trim()
                            val resolvedAlt = fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(alt)
                            if (resolvedAlt != null) {
                                nutrimentsMap[resolvedAlt.label] = convertedValue
                                nutrimentCount++
                                logInfo("✅ Nutriment résolu (fallback): '$rawName' -> '$alt' -> '${resolvedAlt.label}' (valeur: $convertedValue)")
                            } else {
                                nutrimentErrorCount++
                                logError("❌ Nutriment non résolu: '$rawName' (valeur: $rawValue)")
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
            dataB = dataB, // Sera remplacé par la valeur prioritaire si fournie
            lastUpdateDate = lastUpdateDate,
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
     * Découpe le contenu CSV en lignes en respectant les retours à la ligne
     * présents dans des cellules entre guillemets.
     */
    private fun splitCsvRows(csvContent: String): List<String> {
        val rows = mutableListOf<String>()
        val currentRow = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < csvContent.length) {
            val char = csvContent[i]

            when {
                char == '"' -> {
                    if (inQuotes && i + 1 < csvContent.length && csvContent[i + 1] == '"') {
                        // Guillemet échappé
                        currentRow.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                        currentRow.append(char)
                    }
                }
                (char == '\n' || char == '\r') && !inQuotes -> {
                    if (char == '\r' && i + 1 < csvContent.length && csvContent[i + 1] == '\n') {
                        i++ // Consommer le LF de CRLF
                    }
                    rows.add(currentRow.toString())
                    currentRow.clear()
                }
                else -> {
                    currentRow.append(char)
                }
            }
            i++
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow.toString())
        }

        return rows
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
     * Convertit une chaîne en Double en gérant les séparateurs décimaux européens (virgule) et anglo-saxons (point)
     */
    private fun String.toDoubleOrNullWithComma(): Double? {
        if (this.isBlank()) return null
        // Remplacer la virgule par un point pour le format européen
        val normalized = this.replace(",", ".")
        return normalized.toDoubleOrNull()
    }

    /**
     * Extrait l'unité depuis l'en-tête de colonne (entre parenthèses)
     * Exemples: "LIPIDE (%)" -> "%", "CA (g/100g)" -> "g/100g", "P (g/kg)" -> "g/kg"
     * @return L'unité extraite ou null si aucune parenthèse n'est trouvée
     */
    private fun extractUnitFromHeader(header: String): String? {
        val startIndex = header.indexOf("(")
        val endIndex = header.indexOf(")")
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return header.substring(startIndex + 1, endIndex).trim()
        }
        return null
    }

    /**
     * Convertit une valeur de nutriment selon l'unité détectée dans l'en-tête
     * - Si l'unité est "/kg" ou "g/kg": convertit en "/100g" (divise par 10)
     * - Si l'unité est "%": pas de conversion (déjà en pourcentage)
     * - Si l'unité est "/100g" ou "g/100g" ou rien: pas de conversion (déjà la bonne unité)
     * 
     * @param value La valeur brute à convertir
     * @param unit L'unité extraite de l'en-tête (peut être null)
     * @param nutrientName Le nom du nutriment (pour les cas spéciaux)
     * @return La valeur convertie
     */
    private fun convertNutrientValue(value: Double, unit: String?, nutrientName: String): Double {
        if (unit == null) {
            // Pas d'unité dans les parenthèses, supposer /100g (pas de conversion)
            return value
        }

        val unitLower = unit.lowercase()
        
        // Détecter si c'est en /kg (g/kg, mg/kg, UI/kg, etc.)
        if (unitLower.contains("/kg") || unitLower.endsWith("/kg")) {
            // Convertir de /kg vers /100g: diviser par 10
            // Exemple: 100 g/kg = 10 g/100g (car 100g = 0.1kg, donc 100/10 = 10)
            return value / 10.0
        }
        
        // Si c'est en %, pas de conversion (déjà en pourcentage)
        if (unitLower == "%" || unitLower.contains("%")) {
            return value
        }
        
        // Si c'est en /100g ou g/100g, pas de conversion
        if (unitLower.contains("/100g") || unitLower.endsWith("/100g")) {
            return value
        }
        
        // Pour les autres unités (mg/100g, UI/kg, etc.), on ne convertit pas
        // car elles sont déjà dans la bonne unité ou nécessitent une conversion plus complexe
        return value
    }

    /**
     * Trouve la valeur d'une colonne en testant plusieurs noms possibles
     * et en tolérant les différences d'accents / encodage.
     */
    private fun Map<String, String>.trouverValeurColonne(nomsPossibles: List<String>): String? {
        if (this.isEmpty()) return null
        val clesNormalisees = this.keys.associateBy { it.normaliserNomColonne() }
        val nomTrouve = nomsPossibles.firstNotNullOfOrNull { nom ->
            val cleNormalisee = nom.normaliserNomColonne()
            clesNormalisees[cleNormalisee]
        }
        return nomTrouve?.let { this[it] }
    }

    /**
     * Normalise un nom de colonne pour comparaison robuste
     * (majuscules, suppression des espaces et remplacement d'accents / caractères invalides).
     */
    private fun String.normaliserNomColonne(): String {
        val sansAccents = this.uppercase()
            .replace("�", "E")
            .replace("É", "E")
            .replace("È", "E")
            .replace("Ê", "E")
            .replace("Ë", "E")
            .replace("À", "A")
            .replace("Â", "A")
            .replace("Ä", "A")
            .replace("Ô", "O")
            .replace("Ö", "O")
            .replace("Û", "U")
            .replace("Ü", "U")
            .replace("Î", "I")
            .replace("Ï", "I")
        return sansAccents
            .replace(" ", "")
            .replace("_", "")
    }

    /**
     * Normalise la valeur DataB importée:
     * - conserve les codes connus (0, 1, 2, 4, 5, VF24, CHEVAL)
     * - convertit un libellé lisible (ex: "CIQUAL") vers son code
     * - sinon retourne la valeur d'origine trimmée
     */
    private fun normalizeDataBValue(rawValue: String): String {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) return trimmed

        DataB.fromCode(trimmed)?.let { return it.code }

        val byDisplayName = DataB.values().firstOrNull {
            it.displayName.equals(trimmed, ignoreCase = true)
        }
        if (byDisplayName != null) {
            return byDisplayName.code
        }

        return trimmed
    }

    /**
     * Fonctions de logging pour le débogage
     */
    private fun logInfo(@Suppress("UNUSED_PARAMETER") message: String) = Unit
    private fun logError(@Suppress("UNUSED_PARAMETER") message: String) = Unit
    private fun logWarning(@Suppress("UNUSED_PARAMETER") message: String) = Unit

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
                lastUpdateDate = "2024-01-01",
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
