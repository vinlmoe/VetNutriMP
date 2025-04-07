package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.coroutines.CoroutineDispatcher

expect class PlatformDispatcher() {
    fun provideMainDispatcher(): CoroutineDispatcher

    /** Fournit un dispatcher pour les opérations I/O */
    fun provideIODispatcher(): CoroutineDispatcher
}
