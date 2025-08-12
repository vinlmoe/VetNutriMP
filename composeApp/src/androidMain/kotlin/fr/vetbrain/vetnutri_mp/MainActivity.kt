package fr.vetbrain.vetnutri_mp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import fr.vetbrain.vetnutri_mp.DataBase.getDatabaseBuilder
import fr.vetbrain.vetnutri_mp.DataBase.getRoomDatabase
import fr.vetbrain.vetnutri_mp.Localization.AndroidContext
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?): Unit {
        super.onCreate(savedInstanceState)

        // Initialisation du contexte Android
        AndroidContext.appContext = applicationContext
        AndroidContext.setCurrentActivity(this)

        // Initialisation de la localisation
        LocalizationManager.initialize()

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
