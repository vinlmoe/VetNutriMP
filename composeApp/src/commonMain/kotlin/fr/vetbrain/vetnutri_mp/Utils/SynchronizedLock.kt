package fr.vetbrain.vetnutri_mp.Utils

/**
 * Verrou réentrant, non suspendant, utilisable depuis n'importe quel dispatcher (Main compris)
 * pour protéger de courtes sections critiques sur des structures globales (caches LRU,
 * registre des nutriments personnalisés).
 *
 * `synchronized` n'existe pas en commonMain et un `Mutex` ne peut pas être pris hors coroutine ;
 * d'où ce petit `expect/actual` : `synchronized` sur JVM/Android, `NSRecursiveLock` sur iOS.
 * Ne jamais suspendre ni appeler de code long dans [withLock].
 */
expect class SynchronizedLock() {
    fun <T> withLock(block: () -> T): T
}
