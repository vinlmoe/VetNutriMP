package fr.vetbrain.vetnutri_mp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import fr.vetbrain.vetnutri_mp.DataBase.getDatabaseBuilder
import fr.vetbrain.vetnutri_mp.DataBase.getRoomDatabase
import fr.vetbrain.vetnutri_mp.Localization.AndroidContext
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager
import fr.vetbrain.vetnutri_mp.Utils.FullscreenManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?): Unit {
        super.onCreate(savedInstanceState)

        // Configuration plein écran
        FullscreenManager.enableFullscreen(this)

        // Initialisation du contexte Android
        AndroidContext.appContext = applicationContext
        AndroidContext.setCurrentActivity(this)

        // Initialisation de la localisation avec détection de la langue du système
        val systemLocale = resources.configuration.locales[0].language
        val primaryLanguage =
                systemLocale.split("-")[
                        0] // Extraire la langue principale (ex: "de" depuis "de-DE")
        
        LocalizationManager.initialize(primaryLanguage)

        // Initialisation de la base de données
        val appDatabase = getRoomDatabase(getDatabaseBuilder(this))

        setContent { App(appDatabase) }
    }

    override fun onResume(): Unit {
        super.onResume()
        AndroidContext.setCurrentActivity(this)
    }

    override fun onPause(): Unit {
        super.onPause()
        AndroidContext.clearCurrentActivity()
    }
}
