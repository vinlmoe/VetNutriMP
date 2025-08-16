package fr.vetbrain.vetnutri_mp.Localization

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.json.Json

object LocalizationManager {
    private var strings by mutableStateOf<LocalizedStrings?>(null)
    private var currentLocale by mutableStateOf("fr")
    private var resourceReader = ResourceReader()

    internal fun setResourceReader(reader: ResourceReader) {
        resourceReader = reader
    }

    fun initialize(locale: String = "fr") {
        currentLocale = locale
        loadStrings()
    }

    fun setLocale(locale: String) {
        if (currentLocale != locale) {
            currentLocale = locale
            loadStrings()
        }
    }

    private fun loadStrings() {
        val resourceName = "strings_$currentLocale.json"
        println("🌍 DEBUG: Tentative de chargement de la ressource: $resourceName")
        try {
            val jsonString = resourceReader.readResource(resourceName)
            strings = Json.decodeFromString<LocalizedStrings>(jsonString)
            println(
                    "✅ DEBUG: Ressource $resourceName chargée avec succès (${strings?.translations?.size ?: 0} traductions)"
            )
        } catch (e: Exception) {
            println("❌ DEBUG: Erreur lors du chargement de $resourceName: ${e.message}")
            if (currentLocale != "fr") {
                println("🔄 DEBUG: Fallback vers le français...")
                currentLocale = "fr"
                loadStrings()
            } else {
                println(
                        "💥 DEBUG: Impossible de charger même le français, utilisation des clés par défaut"
                )
            }
        }
    }

    fun translate(key: String): String {
        return strings?.translations?.get(key) ?: key
    }
}
