package fr.vetbrain.vetnutri_mp.Utils

/**
 * Implémentation iOS pour la gestion du mode plein écran
 * Sur iOS, le mode plein écran n'est pas applicable de la même manière
 */

/**
 * Active ou désactive le mode plein écran sur iOS
 * @param isFullscreen true pour activer, false pour désactiver
 */
actual fun setFullscreen(isFullscreen: Boolean): Unit {
    // Sur iOS, le mode plein écran n'est pas implémenté
    // Cette fonction est un placeholder pour la compatibilité
}

/**
 * Vérifie si l'application est actuellement en mode plein écran sur iOS
 * @return false car le mode plein écran n'est pas applicable sur iOS
 */
actual fun isFullscreen(): Boolean {
    // Sur iOS, le mode plein écran n'est pas applicable
    return false
}
