package fr.vetbrain.vetnutri_mp.ExcelPlatform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.stringWithContentsOfURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.popoverPresentationController
import platform.UniformTypeIdentifiers.UTTypeCommaSeparatedText
import platform.darwin.NSObject
import kotlin.coroutines.resume

/**
 * Implémentation iOS des opérations de fichiers CSV.
 * Import : UIDocumentPickerViewController (sélection puis lecture du contenu).
 * Export : UIActivityViewController (feuille de partage), même approche que exportJsonToFile
 * dans FileImport.kt.ios.kt pour rester cohérent avec le reste de l'app.
 */

private fun topViewController(): UIViewController? {
    val window = UIApplication.sharedApplication.keyWindow
        ?: UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow
    var top = window?.rootViewController ?: return null
    while (true) {
        top = top?.presentedViewController ?: break
    }
    return top
}

private class DocumentPickerDelegate(
    private val onResult: (NSURL?) -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        onResult(didPickDocumentsAtURLs.firstOrNull() as? NSURL)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onResult(null)
    }
}

// Référence forte conservée le temps de la présentation du picker : UIDocumentPickerViewController.delegate
// est une propriété faible côté UIKit, donc sans ceci le delegate Kotlin serait libéré avant le callback.
private var activeDocumentPickerDelegate: DocumentPickerDelegate? = null

@OptIn(ExperimentalForeignApi::class)
private suspend fun pickCsvDocumentUrl(): NSURL? = suspendCancellableCoroutine { continuation ->
    val presenter = topViewController()
    if (presenter == null) {
        continuation.resume(null)
        return@suspendCancellableCoroutine
    }

    val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeCommaSeparatedText))
    val delegate = DocumentPickerDelegate { url ->
        activeDocumentPickerDelegate = null
        if (continuation.isActive) continuation.resume(url)
    }
    activeDocumentPickerDelegate = delegate
    picker.delegate = delegate

    continuation.invokeOnCancellation {
        activeDocumentPickerDelegate = null
    }

    presenter.presentViewController(picker, animated = true, completion = null)
}

@OptIn(ExperimentalForeignApi::class)
private fun readCsvContent(url: NSURL): String? {
    val didStartAccess = url.startAccessingSecurityScopedResource()
    return try {
        NSString.stringWithContentsOfURL(url, NSUTF8StringEncoding, null)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        if (didStartAccess) url.stopAccessingSecurityScopedResource()
    }
}

actual suspend fun openCsvFileForImport(): String? {
    val url = pickCsvDocumentUrl() ?: return null
    return readCsvContent(url)
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun saveCsvFileForExport(csvContent: String, defaultFileName: String): Boolean {
    return try {
        val tempDirectory = NSTemporaryDirectory()
        val filePath = "$tempDirectory$defaultFileName"

        val nsString = NSString.create(string = csvContent)
        val data = nsString.dataUsingEncoding(NSUTF8StringEncoding) ?: return false

        val success = NSFileManager.defaultManager.createFileAtPath(filePath, data, null)
        if (!success) return false

        val url = NSURL.fileURLWithPath(filePath)
        val activityViewController = UIActivityViewController(listOf(url), null)

        val window = UIApplication.sharedApplication.keyWindow
            ?: UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow

        val popover = activityViewController.popoverPresentationController
        if (popover != null) {
            popover.sourceView = window
            popover.sourceRect = window?.bounds ?: CGRectMake(0.0, 0.0, 0.0, 0.0)
        }

        (window?.rootViewController ?: topViewController())?.presentViewController(
            activityViewController,
            true,
            null
        )
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

actual suspend fun openCsvFileWithPreview(): String? = openCsvFileForImport()

actual fun isCsvFileOperationsSupported(): Boolean = true
