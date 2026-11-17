package fr.vetbrain.vetnutri_mp.ExcelPlatform

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Pont entre les expect fun `suspend` de ExcelFileOperations et les ActivityResultLauncher Android.
 * Les launchers doivent être enregistrés avant STARTED (donc dans MainActivity.onCreate, pas depuis
 * un composable) : voir ExcelFileOperationsBridge.register(this) dans MainActivity.kt.
 */
internal object ExcelFileOperationsBridge {
    private var importLauncher: ActivityResultLauncher<String>? = null
    private var exportLauncher: ActivityResultLauncher<String>? = null

    private var pendingImport: CompletableDeferred<Uri?>? = null
    private var pendingExport: CompletableDeferred<Uri?>? = null

    fun register(activity: ComponentActivity) {
        importLauncher = activity.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            pendingImport?.complete(uri)
            pendingImport = null
        }
        exportLauncher = activity.registerForActivityResult(
            ActivityResultContracts.CreateDocument("text/csv")
        ) { uri ->
            pendingExport?.complete(uri)
            pendingExport = null
        }
    }

    suspend fun pickImportUri(): Uri? {
        val launcher = importLauncher ?: return null
        val deferred = CompletableDeferred<Uri?>()
        pendingImport = deferred
        withContext(Dispatchers.Main) { launcher.launch("text/csv") }
        return deferred.await()
    }

    suspend fun pickExportUri(defaultFileName: String): Uri? {
        val launcher = exportLauncher ?: return null
        val deferred = CompletableDeferred<Uri?>()
        pendingExport = deferred
        withContext(Dispatchers.Main) { launcher.launch(defaultFileName) }
        return deferred.await()
    }
}
