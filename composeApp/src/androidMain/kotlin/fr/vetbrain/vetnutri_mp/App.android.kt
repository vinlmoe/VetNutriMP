package fr.vetbrain.vetnutri_mp

import android.content.Context
import fr.vetbrain.vetnutri_mp.Service.FileService

// Variables globales pour le contexte Android
var androidContext: Context? = null

actual fun createFileService(): FileService {
    return androidContext?.let { FileService(it) }
            ?: throw IllegalStateException("Android context not initialized")
}
