package fr.vetbrain.vetnutri_mp.Utils

import platform.Foundation.NSRecursiveLock

actual class SynchronizedLock {
    private val lock = NSRecursiveLock()

    actual fun <T> withLock(block: () -> T): T {
        lock.lock()
        try {
            return block()
        } finally {
            lock.unlock()
        }
    }
}
