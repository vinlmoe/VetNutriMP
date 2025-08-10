package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing

actual class PlatformDispatcher {
    actual fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Swing

    actual fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO
}
