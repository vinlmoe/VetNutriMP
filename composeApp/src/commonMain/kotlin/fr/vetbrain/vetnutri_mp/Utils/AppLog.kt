package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.coroutines.TimeoutCancellationException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Journalisation unique de l'application.
 *
 * Remplace les `println` et `printStackTrace` dispersés : un seul point à brancher le
 * jour où un vrai backend de logs (logcat, os_log, fichier) est nécessaire.
 * Les messages de niveau debug ne sont émis qu'en build debug.
 */
object AppLog {
    fun d(tag: String, message: String) {
        if (debugEnabled) println("D/$tag: $message")
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        println("W/$tag: $message" + throwable.suffix())
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        println("E/$tag: $message" + throwable.suffix())
    }

    private val debugEnabled: Boolean by lazy {
        try {
            isDebugBuild()
        } catch (t: Throwable) {
            false
        }
    }

    private fun Throwable?.suffix(): String =
        if (this == null) "" else "\n" + stackTraceToString()
}

/**
 * À appeler en tête d'un `catch (e: Exception)` ou `catch (e: Throwable)` : relance
 * l'annulation de coroutine au lieu de l'avaler, sinon un écran fermé ou une tâche
 * annulée continue de tourner et peut écrire un état obsolète.
 * Les expirations de `withTimeout` ne sont pas relancées : elles restent gérées
 * localement comme avant.
 */
fun Throwable.rethrowIfCancellation() {
    if (this is CancellationException && this !is TimeoutCancellationException) throw this
}
