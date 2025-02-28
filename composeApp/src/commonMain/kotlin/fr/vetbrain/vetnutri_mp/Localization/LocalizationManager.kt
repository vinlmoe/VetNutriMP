package fr.vetbrain.vetnutri_mp.Localization

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

object LocalizationManager {
    private val _strings = MutableStateFlow<LocalizedStrings?>(null)
    private val _currentLocale = MutableStateFlow("fr")
    private var resourceReader = ResourceReader()

    val currentLocale: StateFlow<String> = _currentLocale.asStateFlow()
    val strings: StateFlow<LocalizedStrings?> = _strings.asStateFlow()

    internal fun setResourceReader(reader: ResourceReader) {
        resourceReader = reader
    }

    fun initialize(locale: String = "fr") {
        _currentLocale.value = locale
        loadStrings()
    }

    fun setLocale(locale: String) {
        if (_currentLocale.value != locale) {
            _currentLocale.value = locale
            loadStrings()
            // Force une mise à jour des strings pour déclencher une recomposition
            _strings.value = _strings.value
        }
    }

    fun getCurrentLocale(): String {
        return _currentLocale.value
    }

    private fun loadStrings() {
        val resourceName = "strings_${_currentLocale.value}.json"
        try {
            val jsonString = resourceReader.readResource(resourceName)
            _strings.value = Json.decodeFromString<LocalizedStrings>(jsonString)
        } catch (e: Exception) {
            println("Error loading strings for locale ${_currentLocale.value}: ${e.message}")
            if (_currentLocale.value != "fr") {
                _currentLocale.value = "fr"
                loadStrings()
            }
        }
    }

    fun translate(key: String): String {
        return _strings.value?.translations?.get(key) ?: key
    }
}
