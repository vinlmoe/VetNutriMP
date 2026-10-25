package fr.vetbrain.vetnutri_mp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.DataBase.AndroidDatabaseHolder
import fr.vetbrain.vetnutri_mp.DataBase.AppDatabase
import fr.vetbrain.vetnutri_mp.ExcelPlatform.ExcelFileOperationsBridge
import fr.vetbrain.vetnutri_mp.Localization.AndroidContext
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager
import fr.vetbrain.vetnutri_mp.Utils.FullscreenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?): Unit {
        super.onCreate(savedInstanceState)

        // Configuration plein écran
        FullscreenManager.enableFullscreen(this)

        // Initialisation du contexte Android
        AndroidContext.appContext = applicationContext
        AndroidContext.setCurrentActivity(this)

        // Enregistrement des ActivityResultLauncher pour l'import/export CSV
        // (doit se faire avant STARTED, donc ici plutôt que depuis un composable)
        ExcelFileOperationsBridge.register(this)
        JsonFileOperationsBridge.register(this)

        // Initialisation de la localisation avec détection de la langue du système
        val systemLocale = resources.configuration.locales[0].language
        val primaryLanguage =
                systemLocale.split("-")[
                        0] // Extraire la langue principale (ex: "de" depuis "de-DE")
        
        LocalizationManager.initialize(primaryLanguage)

        // Initialisation de la base de données : instance unique par processus, ouverte (avec
        // ses migrations) hors du thread UI ; l'application s'affiche une fois la base prête.
        val appContext = applicationContext
        setContent {
            var appDatabase by remember { mutableStateOf<AppDatabase?>(AndroidDatabaseHolder.current) }
            LaunchedEffect(Unit) {
                if (appDatabase == null) {
                    appDatabase = withContext(Dispatchers.IO) { AndroidDatabaseHolder.getOrCreate(appContext) }
                }
            }
            val database = appDatabase
            if (database == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                App(database)
            }
        }
    }

    override fun onDestroy() {
        JsonFileOperationsBridge.unregister(isChangingConfigurations)
        super.onDestroy()
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
