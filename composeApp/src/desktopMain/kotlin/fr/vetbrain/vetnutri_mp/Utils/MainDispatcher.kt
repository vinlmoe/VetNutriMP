package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual class PlatformDispatcher {
    actual fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Default

    actual fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO
}
