package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import fr.vetbrain.vetnutri_mp.Localization.AndroidContext
import fr.vetbrain.vetnutri_mp.Data.ApiEnvelope
import fr.vetbrain.vetnutri_mp.Utils.createExportJson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.encodeToStream
import java.io.OutputStream

private suspend fun saveJson(defaultFileName: String, write: (OutputStream) -> Unit): Boolean {
    return try {
        val uri = JsonFileOperationsBridge.pickExport(defaultFileName.ifBlank { "vetnutri_export.json" }) ?: return false
        withContext(Dispatchers.IO) {
            val stream = AndroidContext.appContext.contentResolver.openOutputStream(uri, "wt")
                ?: error("Impossible d'ouvrir la destination")
            stream.use { write(it) }
        }
        true
    } catch (e: CancellationException) {
        throw e
    } catch (ignored: Exception) {
        ignored.rethrowIfCancellation()
        false
    }
}

actual suspend fun exportJsonToFile(content: String, defaultFileName: String): Boolean =
    saveJson(defaultFileName) { it.write(content.toByteArray(Charsets.UTF_8)) }

@OptIn(ExperimentalSerializationApi::class)
actual suspend fun exportApiEnvelopeToFile(envelope: ApiEnvelope, defaultFileName: String): Boolean =
    saveJson(defaultFileName) { createExportJson().encodeToStream(ApiEnvelope.serializer(), envelope, it) }

actual suspend fun openJsonFilesContent(): List<fr.vetbrain.vetnutri_mp.Data.FichierJson>? {
    val uris = JsonFileOperationsBridge.pickImportMultiple()
    if (uris.isEmpty()) return null
    return uris.map { uri ->
        val nom = nomAffiche(uri)
        val contenu = try {
            readFileContent(uri)
        } catch (e: Exception) {
            e.rethrowIfCancellation()
            fr.vetbrain.vetnutri_mp.Utils.AppLog.w("ExportImport", "Lecture impossible : $nom", e)
            ""
        }
        fr.vetbrain.vetnutri_mp.Data.FichierJson(nom, contenu)
    }
}

/** Nom du fichier tel que l'affiche le fournisseur de documents (repli : fin de l'URI). */
private suspend fun nomAffiche(uri: android.net.Uri): String = withContext(fr.vetbrain.vetnutri_mp.Utils.AppDispatchers.IO) {
    try {
        AndroidContext.appContext.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
    } catch (e: Exception) {
        e.rethrowIfCancellation()
        fr.vetbrain.vetnutri_mp.Utils.AppLog.d("ExportImport", "Nom de fichier indisponible : ${e.message}")
        null
    } ?: uri.lastPathSegment?.substringAfterLast('/') ?: "copie.json"
}

actual suspend fun openJsonFileContent(): String? {
    val uri = JsonFileOperationsBridge.pickImport() ?: return null
    return readFileContent(uri)
}
