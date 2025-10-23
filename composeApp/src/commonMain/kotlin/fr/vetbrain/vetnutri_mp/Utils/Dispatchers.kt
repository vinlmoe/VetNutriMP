package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

object AppDispatchers {
    private val platformDispatcher = PlatformDispatcher()
    private var _io: CoroutineDispatcher = Dispatchers.IO
    private var _default: CoroutineDispatcher = Dispatchers.Default
    private var _main: CoroutineDispatcher = try {
        platformDispatcher.provideMainDispatcher()
    } catch (e: Exception) {
        println("⚠️ Erreur lors de l'initialisation du dispatcher principal: ${e.message}")
        println("⚠️ Utilisation du fallback Dispatchers.Main")
        // Fallback pour éviter les erreurs Android sur desktop
        Dispatchers.Main
    }

    // Pour les opérations de base de données et I/O
    val IO: CoroutineDispatcher
        get() = _io

    // Pour les opérations CPU-intensives
    val Default: CoroutineDispatcher
        get() = _default

    // Pour les opérations UI
    val Main: CoroutineDispatcher
        get() {
            println("🔍 AppDispatchers.Main utilisé: ${_main::class.simpleName}")
            return _main
        }

    // Fonction pour injecter des dispatchers pour les tests
    internal fun setDispatchers(
            io: CoroutineDispatcher = Dispatchers.IO,
            default: CoroutineDispatcher = Dispatchers.Default,
            main: CoroutineDispatcher
    ) {
        _io = io
        _default = default
        _main = main
    }

    // Fonction pour réinitialiser les dispatchers par défaut
    internal fun resetDispatchers() {
        _io = Dispatchers.IO
        _default = Dispatchers.Default
        _main = try {
            platformDispatcher.provideMainDispatcher()
        } catch (e: Exception) {
            // Fallback pour éviter les erreurs Android sur desktop
            Dispatchers.Main
        }
    }
}
