package fr.vetbrain.vetnutri_mp.Localization

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import fr.vetbrain.vetnutri_mp.Utils.createPreferencesStorage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object LocalizationManager {
    private const val KEY_LOCALE = "selected_locale"
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var strings by mutableStateOf<LocalizedStrings?>(null)
    private var currentLocaleState by mutableStateOf("fr")
    val currentLocale: String
        get() = currentLocaleState
    private val json = Json { ignoreUnknownKeys = true }
    private var resourceReader = ResourceReader()

    internal fun setResourceReader(reader: ResourceReader) {
        resourceReader = reader
    }

    /**
     * Charge la langue sauvegardée au démarrage
     */
    suspend fun loadLocale() {
        println("LocalizationManager: Loading saved locale...")
        try {
            val storage = createPreferencesStorage()
            val savedLocale = storage.getString(KEY_LOCALE, "fr")
            println("LocalizationManager: Saved locale found: $savedLocale")
            initialize(savedLocale)
        } catch (e: Exception) {
            println("LocalizationManager: Error loading saved locale: ${e.message}. Defaulting to 'fr'")
            initialize("fr")
        }
    }

    fun initialize(locale: String = "fr") {
        if (strings != null && currentLocaleState == locale) {
            println("LocalizationManager: Already initialized for $locale, skipping")
            return
        }
        
        println("LocalizationManager: Initializing for $locale")
        currentLocaleState = locale
        loadStrings()
    }

    fun setLocale(locale: String) {
        println("LocalizationManager: Requesting locale change to $locale")
        if (currentLocaleState != locale) {
            currentLocaleState = locale
            loadStrings()
            
            // Persister le changement
            scope.launch {
                try {
                    val storage = createPreferencesStorage()
                    storage.saveString(KEY_LOCALE, locale)
                    println("LocalizationManager: Locale $locale persisted successfully")
                } catch (e: Exception) {
                    println("LocalizationManager: Error persisting locale: ${e.message}")
                }
            }
        } else {
            println("LocalizationManager: Locale is already $locale, skipping")
        }
    }

    private fun loadStrings() {
        val resourceName = "strings_$currentLocaleState.json"
        println("LocalizationManager: Loading strings from $resourceName")
        
        try {
            val jsonString = resourceReader.readResource(resourceName)
            strings = json.decodeFromString<LocalizedStrings>(jsonString)
            println("LocalizationManager: Successfully loaded strings for $currentLocaleState")
            
        } catch (e: Exception) {
            println("LocalizationManager: Error loading strings for $currentLocaleState: ${e.message}")
            if (currentLocaleState != "fr") {
                println("LocalizationManager: Falling back to 'fr'")
                currentLocaleState = "fr"
                loadStrings()
            } else {
                println("LocalizationManager: CRITICAL: Could not load fallback 'fr' strings")
            }
        }
    }

    fun translate(key: String): String {
        return strings?.translations?.get(key) ?: key
    }
}
