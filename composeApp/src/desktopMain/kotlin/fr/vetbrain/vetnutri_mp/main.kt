package fr.vetbrain.vetnutri_mp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager

fun main() = application {
    // Initialisation de la localisation
    LocalizationManager.initialize()

    Window(
            onCloseRequest = ::exitApplication,
            title = "VetNutri MP",
    ) { App() }
}
