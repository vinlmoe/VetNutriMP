package fr.vetbrain.vetnutri_mp

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
    } catch (_: Exception) {
        false
    }
}

actual suspend fun exportJsonToFile(content: String, defaultFileName: String): Boolean =
    saveJson(defaultFileName) { it.write(content.toByteArray(Charsets.UTF_8)) }

@OptIn(ExperimentalSerializationApi::class)
actual suspend fun exportApiEnvelopeToFile(envelope: ApiEnvelope, defaultFileName: String): Boolean =
    saveJson(defaultFileName) { createExportJson().encodeToStream(ApiEnvelope.serializer(), envelope, it) }

actual suspend fun openJsonFileContent(): String? {
    val uri = JsonFileOperationsBridge.pickImport() ?: return null
    return readFileContent(uri)
}
