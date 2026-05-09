package fr.vetbrain.vetnutri_mp.Utils

/**
 * Interface multiplateforme pour ouvrir des URLs
 */
expect object PlatformUrlOpener {
    fun openUrl(url: String)
}
