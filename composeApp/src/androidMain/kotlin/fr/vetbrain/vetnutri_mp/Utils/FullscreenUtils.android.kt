package fr.vetbrain.vetnutri_mp.Utils

import fr.vetbrain.vetnutri_mp.Localization.AndroidContext

/**
 * Implémentation Android pour la gestion du mode plein écran
 */

/**
 * Active ou désactive le mode plein écran sur Android
 * @param isFullscreen true pour activer, false pour désactiver
 */
actual fun setFullscreen(isFullscreen: Boolean): Unit {
    val activity = AndroidContext.getCurrentActivityOrNull()
    activity?.let { 
        FullscreenManager.toggleFullscreen(it, isFullscreen)
    }
}

/**
 * Vérifie si l'application est actuellement en mode plein écran sur Android
 * @return true si en mode plein écran, false sinon
 */
actual fun isFullscreen(): Boolean {
    val activity = AndroidContext.getCurrentActivityOrNull()
    return activity?.let { FullscreenManager.isFullscreen(it) } ?: false
}
