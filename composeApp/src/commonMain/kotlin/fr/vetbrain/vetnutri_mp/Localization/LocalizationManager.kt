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
        
        try {
            val jsonString = resourceReader.readResource(resourceName)
            strings = Json.decodeFromString<LocalizedStrings>(jsonString)
            
        } catch (e: Exception) {
            
            if (currentLocale != "fr") {
                
                currentLocale = "fr"
                loadStrings()
            } else {
                
            }
        }
    }

    fun translate(key: String): String {
        return strings?.translations?.get(key) ?: key
    }
}
