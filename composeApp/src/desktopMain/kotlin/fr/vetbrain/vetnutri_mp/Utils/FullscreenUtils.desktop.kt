package fr.vetbrain.vetnutri_mp.Utils

/**
 * Implémentation Desktop pour la gestion du mode plein écran
 * Sur desktop, le mode plein écran n'est pas applicable de la même manière
 */

/**
 * Active ou désactive le mode plein écran sur Desktop
 * @param isFullscreen true pour activer, false pour désactiver
 */
actual fun setFullscreen(isFullscreen: Boolean): Unit {
    // Sur desktop, le mode plein écran n'est pas implémenté
    // Cette fonction est un placeholder pour la compatibilité
}

/**
 * Vérifie si l'application est actuellement en mode plein écran sur Desktop
 * @return false car le mode plein écran n'est pas applicable sur desktop
 */
actual fun isFullscreen(): Boolean {
    // Sur desktop, le mode plein écran n'est pas applicable
    return false
}
