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
            println("DEBUG: JSON récupéré: $jsonString")
            if (jsonString.isNotBlank() && jsonString != "{}") {
                // Essayer de parser le JSON manuellement
                _preferences = parsePreferencesFromJson(jsonString)
                println("DEBUG: Préférences chargées depuis le stockage")
            } else {
                _preferences = PreferencesApplication()
                println("DEBUG: Préférences par défaut utilisées")
            }
        } catch (e: Exception) {
            println("DEBUG: Erreur lors du chargement: ${e.message}")
            // En cas d'erreur, utiliser les préférences par défaut
            _preferences = PreferencesApplication()
        }
    }

    /** Sauvegarde les préférences dans le stockage */
    suspend fun savePreferences(preferences: PreferencesApplication) {
        try {
            val jsonString = serializePreferencesToJson(preferences)
            preferencesStorage.saveString(PREFERENCES_KEY, jsonString)
            _preferences = preferences
            println("DEBUG: Préférences sauvegardées dans le stockage")
        } catch (e: Exception) {
            println("DEBUG: Erreur lors de la sauvegarde: ${e.message}")
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
            // Utiliser directement l'ID de l'enum
            println(
                    "DEBUG SAVE: Sauvegarde $speciesName avec expressionId=${prefs.typeExpressionBesoinId} (${TypeExpressionBesoin.getById(prefs.typeExpressionBesoinId).displayName})"
            )
            speciesEntries.add(
                    "\"$speciesName\":{\"expressionId\":${prefs.typeExpressionBesoinId},\"nutrients\":$nutrientsJson}"
            )
        }
        sb.append(speciesEntries.joinToString(","))

        sb.append("}}")
        val result = sb.toString()
        println("DEBUG SAVE: JSON généré: $result")
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
                    // Chercher les données de cette espèce dans le JSON
                    val speciesPattern = "\"$speciesName\":\\{([^}]+)\\}"
                    val speciesMatch = Regex(speciesPattern).find(speciesJson)

                    if (speciesMatch != null) {
                        val speciesData = speciesMatch.groupValues[1]
                        println("DEBUG: Données trouvées pour $speciesName: $speciesData")

                        // Extraire l'expression
                        val expressionPattern = "\"expressionId\":([0-9]+)"
                        val expressionMatch = Regex(expressionPattern).find(speciesData)
                        val expressionId =
                                expressionMatch?.groupValues?.get(1)?.toIntOrNull()
                                        ?: TypeExpressionBesoin.DEFAULT.id

                        println(
                                "DEBUG LOAD: Expression ID trouvée pour $speciesName: $expressionId (${TypeExpressionBesoin.getById(expressionId).displayName})"
                        )

                        // Extraire les nutriments
                        val nutrients = parseNutrientsFromJson(speciesData)

                        println(
                                "DEBUG: ${nutrients.size} catégories de nutriments trouvées pour $speciesName"
                        )

                        preferencesMap[speciesName] =
                                fr.vetbrain.vetnutri_mp.Data.PreferencesEspece(
                                        espece = speciesName,
                                        typeExpressionBesoinId = expressionId,
                                        nutrimentsSelectionnes = nutrients
                                )
                        println(
                                "DEBUG LOAD: Préférences créées pour $speciesName avec expression ID: $expressionId"
                        )
                    } else {
                        println("DEBUG: Aucune donnée trouvée pour $speciesName dans le JSON")
                        println(
                                "DEBUG: Espèce $speciesName - utilisation des préférences par défaut"
                        )
                    }
                }
            }

            return PreferencesApplication(preferencesParEspece = preferencesMap)
        } catch (e: Exception) {
            println("DEBUG: Erreur de parsing JSON: ${e.message}")
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

            println("DEBUG: Nutriments parsés: ${result.mapValues { it.value.size }}")
        } catch (e: Exception) {
            println("DEBUG: Erreur parsing nutriments: ${e.message}")
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

    /** Exporte les préférences au format JSON */
    fun exporterPreferences(): String {
        return serializePreferencesToJson(_preferences)
    }

    /** Importe les préférences depuis un JSON */
    suspend fun importerPreferences(preferencesJson: String): Boolean {
        return try {
            val nouvellesPreferences = parsePreferencesFromJson(preferencesJson)
            savePreferences(nouvellesPreferences)
            println("DEBUG: Préférences importées avec succès")
            true
        } catch (e: Exception) {
            println("ERROR: Erreur lors de l'import des préférences: ${e.message}")
            false
        }
    }
}
