package fr.vetbrain.vetnutri_mp.Localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun String.translate(): String {
    val strings by LocalizationManager.strings.collectAsState()
    return strings?.translations?.get(this) ?: this
}

fun String.translateNonComposable(): String {
    return LocalizationManager.translate(this)
}
