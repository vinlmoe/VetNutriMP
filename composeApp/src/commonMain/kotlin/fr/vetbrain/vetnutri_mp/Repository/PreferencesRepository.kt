package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.PreferencesApplication
import fr.vetbrain.vetnutri_mp.Data.PreferencesEspece
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import fr.vetbrain.vetnutri_mp.Enumer.UnitType
import fr.vetbrain.vetnutri_mp.Utils.PreferencesStorage

/** Repository pour la gestion des préférences de l'application */
class PreferencesRepository(private val preferencesStorage: PreferencesStorage) {

    companion object {
        private const val PREFERENCES_KEY = "app_preferences"
    }

    // Trouve la position de la '}' correspondante à l'accolade ouvrante à openIndex
    private fun findMatchingClosingBrace(text: String, openIndex: Int): Int {
        var depth = 0
        for (i in openIndex until text.length) {
            when (text[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return i
                }
            }
        }
        return -1
    }

    // État des préférences
    private var _preferences = PreferencesApplication()
    val preferences: PreferencesApplication
        get() = _preferences

    /** Obtient les unités valides pour l'expression des besoins (excluant UnitType.AUTRE) */
    fun getValidUnits(): List<UnitReqEnum> {
        return UnitReqEnum.values().filter { it.type != UnitType.AUTRE }
    }

    /** Convertit un UnitReqEnum vers le format d'affichage utilisé dans les préférences */
    fun unitToDisplayString(unit: UnitReqEnum): String {
        return when (unit) {
            UnitReqEnum.PERKG -> "Par kg"
            UnitReqEnum.PERMS -> "Par kg de poids métabolique"
            UnitReqEnum.PERKCAL -> "Par Mcal de BEE"
            UnitReqEnum.PERKJ -> "Par MJ de BEE"
            else -> unit.label
        }
    }

    /** Charge les préférences depuis le stockage */
    suspend fun loadPreferences() {
        try {
            val jsonString = preferencesStorage.getString(PREFERENCES_KEY, "{}")
            println("🔍 PERSISTANCE Repo: loadPreferences() brut=\n$jsonString")
            if (jsonString.isNotBlank() && jsonString != "{}") {
                // Essayer de parser le JSON manuellement
                _preferences = parsePreferencesFromJson(jsonString)
            } else {
                _preferences = PreferencesApplication()
            }
            println("🔍 PERSISTANCE Repo: loadPreferences() -> ${_preferences}")
        } catch (e: Exception) {
            // En cas d'erreur, utiliser les préférences par défaut
            _preferences = PreferencesApplication()
        }
    }

    /** Sauvegarde les préférences dans le stockage */
    suspend fun savePreferences(preferences: PreferencesApplication) {
        try {
            val jsonString = serializePreferencesToJson(preferences)
            println("🔍 PERSISTANCE Repo: savePreferences() json=\n$jsonString")
            preferencesStorage.saveString(PREFERENCES_KEY, jsonString)
            _preferences = preferences
        } catch (e: Exception) {
            // Même en cas d'erreur de sauvegarde, on met à jour en mémoire
            _preferences = preferences
        }
    }

    /** Obtient les préférences pour une espèce */
    suspend fun getPreferencesForSpecies(species: Espece): PreferencesEspece {
        return _preferences.getPreferencesEspece(species)
    }

    /** Met à jour les préférences pour une espèce */
    suspend fun updatePreferencesForSpecies(
            species: Espece,
            preferences: PreferencesEspece
    ): PreferencesApplication {
        val updatedPreferences = _preferences.updatePreferencesEspece(preferences)
        savePreferences(updatedPreferences)
        return updatedPreferences
    }

    /** Obtient toutes les espèces disponibles */
    fun getAvailableSpecies(): List<Espece> {
        return Espece.valuesExcept()
    }

    /** Obtient les noms d'affichage des espèces */
    fun getSpeciesDisplayNames(): List<String> {
        return getAvailableSpecies().map { it.label }
    }

    /** Sérialise les préférences en JSON simple */
    private fun serializePreferencesToJson(preferences: PreferencesApplication): String {
        val sb = StringBuilder()
        sb.append("{")
        sb.append("\"version\":${preferences.versionPreferences},")
        sb.append("\"species\":{")

        val speciesEntries = mutableListOf<String>()
        preferences.preferencesParEspece.forEach { (speciesName, prefs) ->
            val nutrientsJson = serializeNutrientsMap(prefs.nutrimentsSelectionnes)
            val equationsJson = serializeEquationsList(prefs.equationsComplementaires)
            // Utiliser directement l'ID de l'enum
            speciesEntries.add(
                    "\"$speciesName\":{\"expressionId\":${prefs.typeExpressionBesoinId},\"nutrients\":$nutrientsJson,\"equations\":$equationsJson}"
            )
        }
        sb.append(speciesEntries.joinToString(","))

        sb.append("}}")
        val result = sb.toString()
        return result
    }

    /** Sérialise la map des nutriments */
    private fun serializeNutrientsMap(nutrients: Map<String, List<Int>>): String {
        val sb = StringBuilder()
        sb.append("{")
        val entries = mutableListOf<String>()
        nutrients.forEach { (category, list) ->
            val listStr = list.joinToString(",", "[", "]")
            entries.add("\"$category\":$listStr")
        }
        sb.append(entries.joinToString(","))
        sb.append("}")
        return sb.toString()
    }

    /** Parse les préférences depuis JSON simple */
    private fun parsePreferencesFromJson(json: String): PreferencesApplication {
        try {
            // Parser JSON simple manuellement
            val preferencesMap =
                    mutableMapOf<String, fr.vetbrain.vetnutri_mp.Data.PreferencesEspece>()

            // Extraire la section species
            val speciesStart = json.indexOf("\"species\":{") + 11
            val speciesEnd = json.lastIndexOf("}}")
            if (speciesStart > 11 && speciesEnd > speciesStart) {
                val speciesJson = json.substring(speciesStart, speciesEnd)

                // Parser chaque espèce (utiliser automatiquement toutes les espèces disponibles
                // dans l'enum)
                val species = fr.vetbrain.vetnutri_mp.Enumer.Espece.valuesExcept().map { it.name }
                species.forEach { speciesName ->
                    // Extraire l'objet JSON complet pour l'espèce via comptage d'accolades
                    val key = "\"$speciesName\":{"
                    val objStart = speciesJson.indexOf(key)
                    if (objStart != -1) {
                        val braceOpenIndex = objStart + key.length - 1 // position de '{'
                        val objEnd = findMatchingClosingBrace(speciesJson, braceOpenIndex)
                        if (objEnd > braceOpenIndex) {
                            val speciesData = speciesJson.substring(braceOpenIndex + 1, objEnd)

                        // Extraire l'expression
                        val expressionPattern = "\"expressionId\":([0-9]+)"
                        val expressionMatch = Regex(expressionPattern).find(speciesData)
                        val expressionId =
                                expressionMatch?.groupValues?.get(1)?.toIntOrNull()
                                        ?: TypeExpressionBesoin.DEFAULT.id


                        // Extraire les nutriments
                        val nutrients = parseNutrientsFromJson(speciesData)

                        // Extraire les équations complémentaires
                        val equations = parseEquationsFromJson(speciesData)


                        preferencesMap[speciesName] =
                                fr.vetbrain.vetnutri_mp.Data.PreferencesEspece(
                                        espece = speciesName,
                                        typeExpressionBesoinId = expressionId,
                                        nutrimentsSelectionnes = nutrients,
                                        equationsComplementaires = equations
                                )
                        }
                    } else {
                    }
                }
            }

            return PreferencesApplication(preferencesParEspece = preferencesMap)
        } catch (e: Exception) {
            return PreferencesApplication()
        }
    }

    /** Parse les nutriments depuis JSON */
    private fun parseNutrientsFromJson(nutrientsJson: String): Map<String, List<Int>> {
        val result = mutableMapOf<String, List<Int>>()
        try {
            // Exemple: {"BASE":[1,2,3],"MACRO":[4,5,6]}
            val categories = listOf("BASE", "MACRO", "MIN", "VITAM", "LIPID", "AMA", "ANA", "OTHER")

            categories.forEach { category ->
                val pattern = "\"$category\":\\[([^\\]]+)\\]"
                val regex = Regex(pattern)
                val match = regex.find(nutrientsJson)
                if (match != null) {
                    val numbersStr = match.groupValues[1]
                    val numbers =
                            if (numbersStr.isNotBlank()) {
                                numbersStr.split(",").mapNotNull { it.trim().toIntOrNull() }
                            } else {
                                emptyList()
                            }
                    result[category] = numbers
                } else {
                    result[category] = emptyList()
                }
            }

        } catch (e: Exception) {
            // Retourner une map vide en cas d'erreur
            return mapOf(
                    "BASE" to emptyList(),
                    "MACRO" to emptyList(),
                    "MIN" to emptyList(),
                    "VITAM" to emptyList(),
                    "LIPID" to emptyList(),
                    "AMA" to emptyList(),
                    "ANA" to emptyList(),
                    "OTHER" to emptyList()
            )
        }
        return result
    }

    /** Sérialise la liste des UUID d'équations (on stocke une liste simple) */
    private fun serializeEquationsList(equations: Map<String, String>): String {
        val uuids = equations.keys.toList()
        val listStr = uuids.joinToString(separator = ",", prefix = "[", postfix = "]") { "\"$it\"" }
        return listStr
    }

    /** Parse la liste des UUID d'équations depuis JSON, renvoie une map uuid->uuid */
    private fun parseEquationsFromJson(speciesDataJson: String): Map<String, String> {
        return try {
            val pattern = "\"equations\":\\[([^]]*)\\]"
            val match = Regex(pattern).find(speciesDataJson)
            if (match != null) {
                val inside = match.groupValues[1]
                val items = if (inside.isBlank()) emptyList() else inside.split(",")
                val uuids = items.mapNotNull { raw ->
                    val t = raw.trim().trim('"')
                    if (t.isBlank()) null else t
                }
                uuids.associateWith { it }
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /** Exporte les préférences au format JSON */
    fun exporterPreferences(): String {
        return serializePreferencesToJson(_preferences)
    }

    /** Importe les préférences depuis un JSON */
    suspend fun importerPreferences(preferencesJson: String): Boolean {
        return try {
            val nouvellesPreferences = parsePreferencesFromJson(preferencesJson)
            savePreferences(nouvellesPreferences)
            true
        } catch (e: Exception) {
            false
        }
    }
}
 