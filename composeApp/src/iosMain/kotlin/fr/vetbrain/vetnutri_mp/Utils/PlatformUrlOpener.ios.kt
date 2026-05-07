package fr.vetbrain.vetnutri_mp.Utils

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

/**
 * Implémentation iOS pour PlatformUrlOpener
 * Fonctionnalité complète sur iOS
 */
actual object PlatformUrlOpener {
    actual fun openUrl(url: String) {
        try {
            val nsUrl = NSURL.URLWithString(url)
                ?: throw UpdateException("URL invalide: $url")
            
            UIApplication.sharedApplication.openURL(nsUrl)
        } catch (e: Exception) {
            throw UpdateException("Impossible d'ouvrir l'URL: $url", e)
        }
    }
}