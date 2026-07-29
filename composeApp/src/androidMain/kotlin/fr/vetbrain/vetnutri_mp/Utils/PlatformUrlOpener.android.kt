package fr.vetbrain.vetnutri_mp.Utils

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable

/**
 * Implémentation Android pour PlatformUrlOpener
 * Fonctionnalité complète sur Android
 */
actual object PlatformUrlOpener {
    actual fun openUrl(url: String) {
        // Pour Android, nous devons utiliser le contexte
        // Cette implémentation sera appelée depuis un Composable
        throw UpdateException("PlatformUrlOpener.openUrl() doit être appelé depuis un Composable sur Android")
    }
    
    @Composable
    fun openUrlComposable(url: String) {
        val context = LocalContext.current
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            throw UpdateException("Impossible d'ouvrir l'URL: $url", e)
        }
    }
}