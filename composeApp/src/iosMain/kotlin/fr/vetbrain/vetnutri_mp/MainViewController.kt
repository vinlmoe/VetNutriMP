package fr.vetbrain.vetnutri_mp

import androidx.compose.ui.window.ComposeUIViewController
import fr.vetbrain.vetnutri_mp.DataBase.getDatabaseBuilder
import fr.vetbrain.vetnutri_mp.DataBase.getRoomDatabase
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager

fun MainViewController() = ComposeUIViewController {
    // Initialisation de la localisation
    LocalizationManager.initialize()

    // Initialisation de la base de données
    val appDatabase = getRoomDatabase(getDatabaseBuilder())

    App(appDatabase)
}
