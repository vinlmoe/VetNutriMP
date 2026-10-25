package fr.vetbrain.vetnutri_mp.Utils

actual class SynchronizedLock {
    private val monitor = Any()

    actual fun <T> withLock(block: () -> T): T = synchronized(monitor) { block() }
}
