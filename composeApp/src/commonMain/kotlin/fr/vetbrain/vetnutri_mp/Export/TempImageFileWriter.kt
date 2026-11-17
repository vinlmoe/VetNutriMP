package fr.vetbrain.vetnutri_mp.Export

/**
 * Écrit une image temporaire sur la plateforme courante et retourne le chemin absolu.
 */
expect fun writeTempImageFile(fileName: String, bytes: ByteArray): String
