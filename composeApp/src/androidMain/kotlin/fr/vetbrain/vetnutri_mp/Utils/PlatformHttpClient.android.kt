package fr.vetbrain.vetnutri_mp.Utils

/**
 * Implémentation Android pour PlatformHttpClient
 * Désactivé sur Android pour éviter les problèmes de compatibilité
 */
actual object PlatformHttpClient {
    actual suspend fun fetchXml(url: String): String {
        // Désactivé sur Android - retourne une erreur pour indiquer que la fonctionnalité n'est pas disponible
        throw UpdateException("Vérification des mises à jour désactivée sur Android")
    }
}
