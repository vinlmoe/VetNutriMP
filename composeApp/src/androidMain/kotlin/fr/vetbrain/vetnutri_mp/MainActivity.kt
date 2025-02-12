package fr.vetbrain.vetnutri_mp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import fr.vetbrain.vetnutri_mp.DataBase.DatabaseModule
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
        DatabaseModule.initialize()

        setContent { App() }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
