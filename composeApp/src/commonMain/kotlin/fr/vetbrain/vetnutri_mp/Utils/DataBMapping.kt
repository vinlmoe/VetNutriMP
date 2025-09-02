package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Utilitaire pour la gestion des correspondances des valeurs dataB
 * Permet de convertir les codes dataB en noms lisibles
 */
object DataBMapping {
    private var mappings: Map<String, String> = emptyMap()
    private var defaultValue: String = "Base de données inconnue"

    init {
        loadMappings()
    }

    /**
     * Charge les correspondances depuis le fichier de configuration
     */
    private fun loadMappings() {
        try {
            // Essayer de charger depuis le fichier de ressources
            val resourcePath = "data/datab_mapping.json"
            val jsonContent = this::class.java.classLoader
                ?.getResourceAsStream(resourcePath)
                ?.bufferedReader()
                ?.readText()

            if (jsonContent != null) {
                val json = Json.parseToJsonElement(jsonContent) as JsonObject

                // Charger les mappings
                val mappingsJson = json["mappings"]?.jsonObject
                if (mappingsJson != null) {
                    mappings = mappingsJson.entries.associate { (key, value) ->
                        key to value.jsonPrimitive.content
                    }
                }

                // Charger la valeur par défaut
                json["default"]?.jsonPrimitive?.content?.let {
                    defaultValue = it
                }
            } else {
                // Fallback vers les mappings codés en dur
                loadDefaultMappings()
            }
        } catch (e: Exception) {
            // En cas d'erreur, utiliser les mappings par défaut
            loadDefaultMappings()
        }
    }

    /**
     * Charge les mappings par défaut (en cas d'absence du fichier)
     */
    private fun loadDefaultMappings() {
        mappings = mapOf(
            "0" to "CIQUAL",
            "1" to "FCEN",
            "2" to "PetFood Divers",
            "4" to "Générique",
            "5" to "Aliment Barf",
            "VF24" to "VetFood 2024"
        )
        defaultValue = "Base de données inconnue"
    }

    /**
     * Obtient le nom lisible correspondant à une valeur dataB
     *
     * @param dataBValue La valeur dataB brute
     * @return Le nom lisible correspondant ou la valeur par défaut
     */
    fun getDisplayName(dataBValue: String?): String {
        if (dataBValue.isNullOrBlank()) {
            return defaultValue
        }

        return mappings[dataBValue.trim()] ?: dataBValue
    }

    /**
     * Obtient toutes les correspondances disponibles
     *
     * @return Map des correspondances dataB -> nom lisible
     */
    fun getAllMappings(): Map<String, String> = mappings.toMap()

    /**
     * Vérifie si une valeur dataB a une correspondance connue
     *
     * @param dataBValue La valeur dataB à vérifier
     * @return true si une correspondance existe, false sinon
     */
    fun hasMapping(dataBValue: String?): Boolean {
        return dataBValue != null && mappings.containsKey(dataBValue.trim())
    }

    /**
     * Recharge les correspondances (utile après modification du fichier)
     */
    fun reloadMappings() {
        loadMappings()
    }
}
