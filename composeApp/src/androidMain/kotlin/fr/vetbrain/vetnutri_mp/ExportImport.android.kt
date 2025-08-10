package fr.vetbrain.vetnutri_mp

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import fr.vetbrain.vetnutri_mp.Localization.AndroidContext

actual fun exportJsonToFile(content: String, defaultFileName: String): Boolean {
    val context = AndroidContext.appContext
    val nomFichier: String =
            if (defaultFileName.isBlank()) "vetnutri_export.json" else defaultFileName
    return try {
        val resolver = context.contentResolver
        val collection =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                else MediaStore.Files.getContentUri("external")
        val valeurs = ContentValues()
        valeurs.put(MediaStore.MediaColumns.DISPLAY_NAME, nomFichier)
        valeurs.put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                valeurs.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        val uri = resolver.insert(collection, valeurs) ?: return false
        resolver.openOutputStream(uri)?.use { flux ->
            flux.write(content.toByteArray(Charsets.UTF_8))
            flux.flush()
        }
                ?: return false
        true
    } catch (_: Throwable) {
        false
    }
}

actual fun openJsonFileContent(): String? {
    return null
}
