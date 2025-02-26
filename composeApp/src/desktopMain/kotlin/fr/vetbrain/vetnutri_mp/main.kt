package fr.vetbrain.vetnutri_mp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import fr.vetbrain.vetnutri_mp.DataBase.getDatabaseBuilder
import fr.vetbrain.vetnutri_mp.DataBase.getRoomDatabase
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager

fun main() = application {
    // Initialisation de la localisation
    LocalizationManager.initialize()

    // Initialisation de la base de données
    val appDatabase = getRoomDatabase(getDatabaseBuilder())

    Window(
            onCloseRequest = ::exitApplication,
            title = "VetNutri MP",
    ) { App(appDatabase) }
}
