package fr.vetbrain.vetnutri_mp.Services

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentExcelRow
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Utils.genUUID

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
     * Importe des aliments depuis un CSV
     */
    fun importFromCsv(csvContent: String): ImportResult {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        
        if (lines.isEmpty()) {
            return ImportResult(emptyList(), listOf("Le fichier CSV est vide"))
        }

        val headers = lines.first().split(";").map { it.trim() }
        val dataLines = lines.drop(1)

        val aliments = mutableListOf<AlimentEv>()
        val errors = mutableListOf<String>()

        dataLines.forEachIndexed { index, line ->
            try {
                val row = parseCsvLine(line, headers)
                val aliment = AlimentExcelRow.toAlimentEv(row)
                aliments.add(aliment)
            } catch (e: Exception) {
                val errorMsg = "Erreur ligne ${index + 2}: ${e.message}"
                errors.add(errorMsg)
            }
        }


        return ImportResult(aliments, errors)
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
     * Parse une ligne CSV en AlimentExcelRow
     */
    private fun parseCsvLine(line: String, headers: List<String>): AlimentExcelRow {
        val values = parseCsvValues(line)
        if (values.size != headers.size) {
            throw IllegalArgumentException("Nombre de colonnes incorrect: attendu ${headers.size}, trouvé ${values.size}")
        }

        // Créer une map header -> valeur pour un accès par nom
        val headerValueMap = headers.zip(values).toMap()

        // Informations de base
        val uuid = headerValueMap["UUID"] ?: ""
        val nom = headerValueMap["Nom"]?.takeIf { it.isNotBlank() }
        val brand = headerValueMap["Marque"]?.takeIf { it.isNotBlank() }
        val gamme = headerValueMap["Gamme"]?.takeIf { it.isNotBlank() }
        val ingredients = headerValueMap["Ingrédients"]?.takeIf { it.isNotBlank() }

        // Classification
        val groupAlim = headerValueMap["Groupe Alimentaire"]?.takeIf { it.isNotBlank() }
        val typeAliment = headerValueMap["Type Aliment"]?.takeIf { it.isNotBlank() }
        val contEnum = headerValueMap["Conditionnement"]?.takeIf { it.isNotBlank() }

        // Prix et quantité
        val price = headerValueMap["Prix"]?.toDoubleOrNull()
        val categPrice = headerValueMap["Catégorie Prix"]?.takeIf { it.isNotBlank() }
        val quantInt = headerValueMap["Quantité Interne"]?.toDoubleOrNull()

        // Statuts
        val consistent = headerValueMap["Consistant"]?.toBooleanStrictOrNull() ?: false
        val deprecated = headerValueMap["Obsolète"]?.toBooleanStrictOrNull() ?: false
        val dataB = headerValueMap["Données Base"]?.takeIf { it.isNotBlank() }

        // Espèces et indications
        val especes = headerValueMap["Espèces"]?.takeIf { it.isNotBlank() }
        val indications = headerValueMap["Indications"]?.takeIf { it.isNotBlank() }

        // Ration
        val rationUUID = headerValueMap["UUID Ration"]?.takeIf { it.isNotBlank() }

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

        headers.forEach { header ->
            if (header !in fixedHeaders) {
                val rawName = header.substringBefore("(").trim().removeSuffix("")
                val valueStr = headerValueMap[header]
                val value = valueStr?.toDoubleOrNull()
                if (value != null) {
                    val resolved = fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(rawName)
                    if (resolved != null) {
                        nutrimentsMap[resolved.label] = value
                    } else {
                        // Fallback: essayer aussi sans espaces/avec normalisation simple
                        val alt = rawName.replace("_", " ").replace("-", " ").trim()
                        val resolvedAlt = fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(alt)
                        if (resolvedAlt != null) {
                            nutrimentsMap[resolvedAlt.label] = value
                        }
                    }
                }
            }
        }

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
