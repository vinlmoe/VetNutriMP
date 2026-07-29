package fr.vetbrain.vetnutri_mp.Utils

/**
 * Utilitaires pour la gestion du mode plein écran
 * Implémentation spécifique par plateforme
 */

/**
 * Active le mode plein écran
 * @param isFullscreen true pour activer, false pour désactiver
 */
expect fun setFullscreen(isFullscreen: Boolean)

/**
 * Vérifie si l'application est actuellement en mode plein écran
 * @return true si en mode plein écran, false sinon
 */
expect fun isFullscreen(): Boolean
