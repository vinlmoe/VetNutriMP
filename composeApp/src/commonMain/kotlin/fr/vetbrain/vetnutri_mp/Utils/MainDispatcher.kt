package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.coroutines.CoroutineDispatcher

expect class PlatformDispatcher() {
    fun provideMainDispatcher(): CoroutineDispatcher
}
