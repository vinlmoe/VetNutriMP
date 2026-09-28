package fr.vetbrain.vetnutri_mp

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Registered before STARTED; the system picker grants access on Android 9 as well. */
internal object JsonFileOperationsBridge {
    private var importLauncher: ActivityResultLauncher<Array<String>>? = null
    private var exportLauncher: ActivityResultLauncher<String>? = null
    private var pendingImport: CompletableDeferred<Uri?>? = null
    private var pendingExport: CompletableDeferred<Uri?>? = null

    fun register(activity: ComponentActivity) {
        importLauncher = activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            pendingImport?.complete(uri)
            pendingImport = null
        }
        exportLauncher = activity.registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            pendingExport?.complete(uri)
            pendingExport = null
        }
    }

    fun unregister(changingConfigurations: Boolean) {
        importLauncher = null
        exportLauncher = null
        if (!changingConfigurations) {
            pendingImport?.cancel()
            pendingExport?.cancel()
            pendingImport = null
            pendingExport = null
        }
    }

    suspend fun pickImport(): Uri? = withContext(Dispatchers.Main) {
        check(pendingImport == null) { "Une sélection de fichier est déjà ouverte" }
        val launcher = checkNotNull(importLauncher) { "Sélecteur de fichier indisponible" }
        val result = CompletableDeferred<Uri?>()
        pendingImport = result
        try {
            // Some providers label .json files as text/plain or application/octet-stream.
            launcher.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
        } catch (e: Exception) {
            pendingImport = null
            throw e
        }
        result.await()
    }

    suspend fun pickExport(name: String): Uri? = withContext(Dispatchers.Main) {
        check(pendingExport == null) { "Une sélection de destination est déjà ouverte" }
        val launcher = checkNotNull(exportLauncher) { "Sélecteur de fichier indisponible" }
        val result = CompletableDeferred<Uri?>()
        pendingExport = result
        try {
            launcher.launch(name)
        } catch (e: Exception) {
            pendingExport = null
            throw e
        }
        result.await()
    }
}
