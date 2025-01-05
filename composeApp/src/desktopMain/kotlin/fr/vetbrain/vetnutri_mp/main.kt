package fr.vetbrain.vetnutri_mp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "VetNutri MP",
    ) {
        App()
    }
}