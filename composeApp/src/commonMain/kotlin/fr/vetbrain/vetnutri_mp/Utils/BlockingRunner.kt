package fr.vetbrain.vetnutri_mp.Utils

/**
 * Exécute un bloc suspendu en bloquant le thread appelant (`runBlocking` sur JVM, Android et iOS).
 * Réservé à l'initialisation, quand l'appelant n'est pas une coroutine.
 */
expect fun <T> runBlockingOnPlatform(block: suspend () -> T): T
