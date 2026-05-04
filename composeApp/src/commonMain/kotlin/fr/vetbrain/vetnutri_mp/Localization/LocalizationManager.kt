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
        try {
            val storage = createPreferencesStorage()
            val savedLocale = storage.getString(KEY_LOCALE, "fr")
            initialize(savedLocale)
        } catch (_: Exception) {
            initialize("fr")
        }
    }

    fun initialize(locale: String = "fr") {
        if (strings != null && currentLocaleState == locale) return
        currentLocaleState = locale
        loadStrings()
    }

    fun setLocale(locale: String) {
        if (currentLocaleState != locale) {
            currentLocaleState = locale
            loadStrings()
            scope.launch {
                try {
                    val storage = createPreferencesStorage()
                    storage.saveString(KEY_LOCALE, locale)
                } catch (_: Exception) {}
            }
        }
    }

    private fun loadStrings() {
        val resourceName = "strings_$currentLocaleState.json"
        try {
            val jsonString = resourceReader.readResource(resourceName)
            strings = json.decodeFromString<LocalizedStrings>(jsonString)
        } catch (_: Exception) {
            if (currentLocaleState != "fr") {
                currentLocaleState = "fr"
                loadStrings()
            }
        }
    }

    fun translate(key: String): String {
        return strings?.translations?.get(key) ?: key
    }
}
