package fr.vetbrain.vetnutri_mp.Localization

import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import fr.vetbrain.vetnutri_mp.Utils.AppLog
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import fr.vetbrain.vetnutri_mp.Utils.createPreferencesStorage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.CoroutineScope
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object LocalizationManager {
    private const val KEY_LOCALE = "selected_locale"
    private val scope = CoroutineScope(AppDispatchers.Main + SupervisorJob())
    private var strings by mutableStateOf<LocalizedStrings?>(null)
    // Langue de référence : une clé absente d'une autre langue s'affiche en français, pas brute
    private var stringsFrancais: LocalizedStrings? = null
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
        } catch (ignored: Exception) {
            ignored.rethrowIfCancellation()
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
                } catch (ignored: Exception) { ignored.rethrowIfCancellation(); AppLog.d("LocalizationManager", "Exception ignorée : ${ignored.message}") }
            }
        }
    }

    private fun loadStrings() {
        val resourceName = "strings_$currentLocaleState.json"
        try {
            val jsonString = resourceReader.readResource(resourceName)
            strings = json.decodeFromString<LocalizedStrings>(jsonString)
        } catch (ignored: Exception) {
            ignored.rethrowIfCancellation()
            if (currentLocaleState != "fr") {
                currentLocaleState = "fr"
                loadStrings()
            }
        }
    }

    fun translate(key: String): String {
        return strings?.translations?.get(key) ?: francais()?.translations?.get(key) ?: key
    }

    private fun francais(): LocalizedStrings? {
        if (currentLocaleState == "fr") return strings
        return stringsFrancais ?: try {
            json.decodeFromString<LocalizedStrings>(resourceReader.readResource("strings_fr.json"))
                    .also { stringsFrancais = it }
        } catch (e: Exception) {
            e.rethrowIfCancellation()
            AppLog.d("LocalizationManager", "Textes français illisibles : ${e.message}")
            null
        }
    }
}
