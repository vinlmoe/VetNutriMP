package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Erreurs à montrer à l'utilisateur au lieu de faire planter l'application.
 *
 * `UserErrorDialog` (monté une fois dans `App.kt`) affiche le message courant. À utiliser quand
 * une donnée attendue manque au moment d'agir, typiquement une sélection qui a changé pendant
 * une attente asynchrone ou entre l'affichage d'un bouton et le clic.
 */
object UserErrors {
    private val _current = MutableStateFlow<String?>(null)
    val current: StateFlow<String?> = _current.asStateFlow()

    fun report(message: String, cause: Throwable? = null) {
        AppLog.w("UserErrors", message, cause)
        _current.value = message
    }

    fun dismiss() {
        _current.value = null
    }
}

/**
 * Renvoie la valeur, ou signale à l'utilisateur que l'action est impossible et renvoie null.
 * Usage : `val ration = selectedRation.orUserError("aucune ration n'est sélectionnée") ?: return@launch`
 *
 * @param raison fin de phrase après « Action impossible : »
 */
fun <T : Any> T?.orUserError(raison: String): T? {
    if (this == null) UserErrors.report("Action impossible : $raison.")
    return this
}
