package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestionnaire de l'acceptation des conditions générales d'utilisation Sauvegarde l'état
 * d'acceptation et le persiste entre les sessions
 */
class TermsAcceptanceStorage {

    companion object {
        private const val KEY_TERMS_ACCEPTED = "terms_accepted"
        private const val KEY_TERMS_VERSION = "terms_version"
        private const val CURRENT_TERMS_VERSION = "1.0"
    }

    private val _isTermsAccepted = MutableStateFlow(false)
    val isTermsAccepted: StateFlow<Boolean> = _isTermsAccepted.asStateFlow()

    /**
     * Vérifie si les conditions générales ont été acceptées
     * @return true si les CGU sont acceptées, false sinon
     */
    suspend fun checkTermsAcceptance(): Boolean {
        return try {
            // Vérifier la version des CGU
            val savedVersion = getStoredTermsVersion()
            val isAccepted = getStoredTermsAcceptance()

            // Si la version a changé, considérer que les CGU ne sont plus acceptées
            if (savedVersion != CURRENT_TERMS_VERSION) {
                setTermsAccepted(false)
                false
            } else {
                isAccepted
            }
        } catch (e: Exception) {
            // En cas d'erreur, considérer que les CGU ne sont pas acceptées
            false
        }
    }

    /** Marque les conditions générales comme acceptées */
    suspend fun acceptTerms() {
        try {
            setTermsAccepted(true)
            setTermsVersion(CURRENT_TERMS_VERSION)
            _isTermsAccepted.value = true
        } catch (e: Exception) {
            // Gérer l'erreur de sauvegarde
            e.printStackTrace()
        }
    }

    /** Marque les conditions générales comme non acceptées */
    suspend fun declineTerms() {
        try {
            setTermsAccepted(false)
            _isTermsAccepted.value = false
        } catch (e: Exception) {
            // Gérer l'erreur de sauvegarde
            e.printStackTrace()
        }
    }

    /** Réinitialise l'acceptation des CGU (utile pour les tests ou mises à jour) */
    suspend fun resetTermsAcceptance() {
        try {
            setTermsAccepted(false)
            setTermsVersion("")
            _isTermsAccepted.value = false
        } catch (e: Exception) {
            // Gérer l'erreur de sauvegarde
            e.printStackTrace()
        }
    }

    /** Vérifie si les CGU ont été acceptées dans la session actuelle */
    fun isTermsAcceptedInSession(): Boolean {
        return _isTermsAccepted.value
    }

    // Méthodes privées pour la persistance des données
    private suspend fun getStoredTermsAcceptance(): Boolean {
        val storage = createPreferencesStorage()
        val acceptedString = storage.getString(KEY_TERMS_ACCEPTED, "false")
        return acceptedString.toBoolean()
    }

    private suspend fun setTermsAccepted(accepted: Boolean) {
        val storage = createPreferencesStorage()
        storage.saveString(KEY_TERMS_ACCEPTED, accepted.toString())
    }

    private suspend fun getStoredTermsVersion(): String {
        val storage = createPreferencesStorage()
        return storage.getString(KEY_TERMS_VERSION, "")
    }

    private suspend fun setTermsVersion(version: String) {
        val storage = createPreferencesStorage()
        storage.saveString(KEY_TERMS_VERSION, version)
    }
}
