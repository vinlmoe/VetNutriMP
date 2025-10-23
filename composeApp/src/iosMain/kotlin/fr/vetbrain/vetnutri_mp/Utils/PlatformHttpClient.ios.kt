package fr.vetbrain.vetnutri_mp.Utils

/**
 * Implémentation iOS pour PlatformHttpClient
 * Désactivé sur iOS pour éviter les problèmes de compatibilité
 */
actual object PlatformHttpClient {
    actual suspend fun fetchXml(url: String): String {
        // Désactivé sur iOS - retourne une erreur pour indiquer que la fonctionnalité n'est pas disponible
        throw UpdateException("Vérification des mises à jour désactivée sur iOS")
    }
}
