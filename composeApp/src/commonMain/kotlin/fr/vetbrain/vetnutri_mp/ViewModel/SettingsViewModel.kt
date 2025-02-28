package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Enumer.Language
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel {
    private val _uiScale = MutableStateFlow(1f)
    val uiScale: StateFlow<Float> = _uiScale.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    private val _currentLanguage =
            MutableStateFlow(Language.fromCode(LocalizationManager.getCurrentLocale()))
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()

    fun showSettings() {
        _showSettings.value = true
    }

    fun hideSettings() {
        _showSettings.value = false
    }

    fun setUiScale(scale: Float) {
        val newScale = scale.coerceIn(0.5f, 2f)
        _uiScale.value = newScale
        AppSizes.adjustSize(newScale)
    }

    fun incrementUiScale() {
        setUiScale(_uiScale.value + 0.1f)
    }

    fun decrementUiScale() {
        setUiScale(_uiScale.value - 0.1f)
    }

    fun setLanguage(language: Language) {
        _currentLanguage.value = language
        LocalizationManager.setLocale(language.code)
    }
}
