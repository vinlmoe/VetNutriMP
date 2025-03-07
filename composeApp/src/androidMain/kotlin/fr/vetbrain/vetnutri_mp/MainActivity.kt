package fr.vetbrain.vetnutri_mp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import fr.vetbrain.vetnutri_mp.DataBase.getDatabaseBuilder
import fr.vetbrain.vetnutri_mp.DataBase.getRoomDatabase
import fr.vetbrain.vetnutri_mp.Localization.AndroidContext
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialisation du contexte Android
        AndroidContext.appContext = applicationContext

        // Initialisation de la localisation
        LocalizationManager.initialize()

        // Initialisation de la base de données
        val appDatabase = getRoomDatabase(getDatabaseBuilder(this))

        setContent { App(appDatabase) }
    }
}

@Composable
fun AppAndroidPreview() {
    // Note: Cette preview ne peut pas utiliser la base de données
    // App(appDatabase)
}
