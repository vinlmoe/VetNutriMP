package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.Localization.AndroidContext
import fr.vetbrain.vetnutri_mp.Service.FileService

actual fun createFileService(): FileService {
    return AndroidContext.appContext?.let { FileService(it) }
            ?: throw IllegalStateException("Android context not initialized")
}
