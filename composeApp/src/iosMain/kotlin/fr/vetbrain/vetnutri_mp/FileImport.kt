@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.Data.ApiEnvelope
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.createExportJson
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.ImportViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import platform.Foundation.*
import platform.UIKit.*
import platform.UniformTypeIdentifiers.UTTypeJSON
import platform.UniformTypeIdentifiers.UTTypePlainText
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume

actual fun importAnimalsFromFile(viewModel: AnimalListViewModel, clearFoodsBeforeImport: Boolean) =
    importAnimalJsonFile(viewModel, clearFoodsBeforeImport)
actual fun importFoodsFromFile(viewModel: SettingsViewModel) = importFoodJsonFile(viewModel)
actual fun importApiFromFile(viewModel: SettingsViewModel) = importApiJsonFile(viewModel)
actual fun importNutritionalRequirementsFromFile(viewModel: ImportViewModel) = viewModel.pickNutritionalRequirementsFile()

private fun jsonPresenter(): UIViewController? {
    val window = UIApplication.sharedApplication.keyWindow
        ?: UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow
    var controller = window?.rootViewController ?: return null
    while (controller.presentedViewController != null) controller = controller.presentedViewController!!
    return controller
}

private class JsonPickerDelegate(val complete: (NSURL?) -> Unit) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        complete(didPickDocumentsAtURLs.firstOrNull() as? NSURL)
    }
    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) { complete(null) }
}

// UIKit's delegate is weak. Keep it alive until a result or coroutine cancellation.
private var jsonPickerDelegate: JsonPickerDelegate? = null

private suspend fun pickJsonUrl(): NSURL? = withContext(Dispatchers.Main) {
    check(jsonPickerDelegate == null) { "Une sélection de fichier est déjà ouverte" }
    val presenter = checkNotNull(jsonPresenter()) { "Sélecteur de fichier indisponible" }
    suspendCancellableCoroutine { continuation ->
        val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeJSON, UTTypePlainText))
        val delegate = JsonPickerDelegate { url ->
            jsonPickerDelegate = null
            if (continuation.isActive) continuation.resume(url)
        }
        jsonPickerDelegate = delegate
        picker.delegate = delegate
        continuation.invokeOnCancellation {
            dispatch_async(dispatch_get_main_queue()) {
                if (jsonPickerDelegate === delegate) {
                    picker.dismissViewControllerAnimated(true, null)
                    jsonPickerDelegate = null
                }
            }
        }
        presenter.presentViewController(picker, true, null)
    }
}

actual suspend fun openJsonFileContent(): String? {
    val url = pickJsonUrl() ?: return null
    return withContext(AppDispatchers.IO) {
        val access = url.startAccessingSecurityScopedResource()
        try {
            NSString.stringWithContentsOfURL(url, NSUTF8StringEncoding, null)
                ?: error("Impossible de lire le fichier sélectionné en UTF-8")
        } finally {
            if (access) url.stopAccessingSecurityScopedResource()
        }
    }
}

actual suspend fun exportJsonToFile(content: String, defaultFileName: String): Boolean {
    return try {
        val path = withContext(AppDispatchers.IO) {
            val directory = NSTemporaryDirectory() + NSUUID().UUIDString
            check(NSFileManager.defaultManager.createDirectoryAtPath(directory, true, null, null))
            val name = defaultFileName.substringAfterLast('/').substringAfterLast('\\').ifBlank { "vetnutri_export.json" }
            val filePath = "$directory/$name"
            check(NSString.create(string = content).writeToFile(filePath, true, NSUTF8StringEncoding, null))
            filePath
        }
        withContext(Dispatchers.Main) {
            val presenter = jsonPresenter() ?: return@withContext false
            val controller = UIActivityViewController(listOf(NSURL.fileURLWithPath(path)), null)
            controller.popoverPresentationController?.let {
                it.sourceView = presenter.view
                it.sourceRect = presenter.view.bounds
            }
            suspendCancellableCoroutine<Boolean> { continuation ->
                controller.completionWithItemsHandler = { _, completed, _, _ ->
                    if (continuation.isActive) continuation.resume(completed)
                }
                continuation.invokeOnCancellation {
                    dispatch_async(dispatch_get_main_queue()) { controller.dismissViewControllerAnimated(true, null) }
                }
                presenter.presentViewController(controller, true, null)
            }
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        false
    }
}

actual suspend fun exportApiEnvelopeToFile(envelope: ApiEnvelope, defaultFileName: String): Boolean {
    val content = withContext(AppDispatchers.IO) { createExportJson().encodeToString(ApiEnvelope.serializer(), envelope) }
    return exportJsonToFile(content, defaultFileName)
}
