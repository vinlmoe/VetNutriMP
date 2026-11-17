package fr.vetbrain.vetnutri_mp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.DataBase.AppDatabase
import fr.vetbrain.vetnutri_mp.Theme.VetNutriTheme
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val ouvertureBase = Mutex()
private var baseOuverte: AppDatabase? = null

/**
 * Ouvre la base une seule fois par processus : une Activity Android recréée (rotation) reprend
 * la même instance au lieu de rouvrir et resauvegarder le fichier.
 */
suspend fun openAppDatabaseOnce(open: suspend () -> AppDatabase): AppDatabase =
        ouvertureBase.withLock { baseOuverte ?: open().also { baseOuverte = it } }

/**
 * Point d'entrée Android et iOS : la base (sauvegarde .bak, migrations) s'ouvre hors du thread de
 * l'interface, un indicateur s'affiche pendant ce temps.
 */
@Composable
fun AppWithDatabase(open: suspend () -> AppDatabase) {
    val database by produceState<AppDatabase?>(null) { value = openAppDatabaseOnce(open) }
    val ouverte = database
    if (ouverte != null) {
        App(ouverte)
    } else {
        VetNutriTheme {
            Box(
                Modifier.fillMaxSize().background(MaterialTheme.colors.background),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        }
    }
}
