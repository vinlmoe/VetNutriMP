package fr.vetbrain.vetnutri_mp.Localization

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.json.Json

object LocalizationManager {
    private var strings by mutableStateOf<LocalizedStrings?>(null)
    private var currentLocale by mutableStateOf("fr")

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
            val jsonString = readResource(resourceName)
            strings = Json.decodeFromString<LocalizedStrings>(jsonString)
        } catch (e: Exception) {
            println("Error loading strings for locale $currentLocale: ${e.message}")
            if (currentLocale != "fr") {
                currentLocale = "fr"
                loadStrings()
            }
        }
    }

    private fun readResource(name: String): String {
        return LocalizationManager::class.java.classLoader?.getResource(name)?.readText()
                ?: throw IllegalStateException("Resource $name not found")
    }

    fun translate(key: String): String {
        return strings?.translations?.get(key) ?: key
    }
}
