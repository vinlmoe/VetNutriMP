package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel {
    private val _uiScale = MutableStateFlow(1f)
    val uiScale: StateFlow<Float> = _uiScale.asStateFlow()

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
}
