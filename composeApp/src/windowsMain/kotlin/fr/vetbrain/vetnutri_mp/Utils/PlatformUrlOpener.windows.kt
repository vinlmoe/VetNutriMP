package fr.vetbrain.vetnutri_mp.Utils

import java.awt.Desktop
import java.net.URI

/**
 * Implémentation Windows pour PlatformUrlOpener
 * Fonctionnalité complète sur Windows
 */
actual object PlatformUrlOpener {
    actual fun openUrl(url: String) {
        try {
            val desktop = Desktop.getDesktop()
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(URI(url))
            } else {
                // Fallback pour les systèmes sans Desktop
                val os = System.getProperty("os.name").lowercase()
                val command = when {
                    os.contains("mac") -> "open"
                    os.contains("win") -> "start"
                    else -> "xdg-open"
                }
                Runtime.getRuntime().exec("$command $url")
            }
        } catch (e: Exception) {
            throw UpdateException("Impossible d'ouvrir l'URL: $url", e)
        }
    }
}
