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
                errors.add("Erreur ligne ${index + 2}: ${e.message}")
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

        // Ajouter les colonnes de nutriments
        AlimentExcelRow.ALL_NUTRIENTS.forEach { nutrient ->
            headers.add("$nutrient (valeur)")
            headers.add("$nutrient (unité)")
        }

        return headers
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
            val nutrientData = row.nutriments[nutrient]
            values.add(nutrientData?.first?.toString() ?: "")
            values.add(escapeCsvValue(nutrientData?.second ?: ""))
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

        var currentIndex = 0

        // Informations de base
        val uuid = values[currentIndex++]
        val nom = values[currentIndex++].takeIf { it.isNotBlank() }
        val brand = values[currentIndex++].takeIf { it.isNotBlank() }
        val gamme = values[currentIndex++].takeIf { it.isNotBlank() }
        val ingredients = values[currentIndex++].takeIf { it.isNotBlank() }

        // Classification
        val groupAlim = values[currentIndex++].takeIf { it.isNotBlank() }
        val typeAliment = values[currentIndex++].takeIf { it.isNotBlank() }
        val contEnum = values[currentIndex++].takeIf { it.isNotBlank() }

        // Prix et quantité
        val price = values[currentIndex++].toDoubleOrNull()
        val categPrice = values[currentIndex++].takeIf { it.isNotBlank() }
        val quantInt = values[currentIndex++].toDoubleOrNull()

        // Statuts
        val consistent = values[currentIndex++].toBooleanStrictOrNull() ?: false
        val deprecated = values[currentIndex++].toBooleanStrictOrNull() ?: false
        val dataB = values[currentIndex++].takeIf { it.isNotBlank() }

        // Espèces et indications
        val especes = values[currentIndex++].takeIf { it.isNotBlank() }
        val indications = values[currentIndex++].takeIf { it.isNotBlank() }

        // Ration
        val rationUUID = values[currentIndex++].takeIf { it.isNotBlank() }

        // Nutriments
        val nutrimentsMap = mutableMapOf<String, Pair<Double?, String?>>()
        AlimentExcelRow.ALL_NUTRIENTS.forEach { nutrient ->
            val valeur = values[currentIndex++].toDoubleOrNull()
            val unite = values[currentIndex++].takeIf { it.isNotBlank() }

            if (valeur != null) {
                nutrimentsMap[nutrient] = Pair(valeur, unite)
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
