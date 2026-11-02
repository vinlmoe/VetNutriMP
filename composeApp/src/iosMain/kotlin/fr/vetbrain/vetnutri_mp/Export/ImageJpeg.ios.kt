package fr.vetbrain.vetnutri_mp.Export

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.Color
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface
import fr.vetbrain.vetnutri_mp.jsonPresenter
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.AppLog
import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.*
import platform.UIKit.*
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume

actual fun encoderJpeg(image: ImageBitmap, qualite: Int): ByteArray {
    val source = image.asSkiaBitmap()
    val surface = Surface.makeRasterN32Premul(source.width, source.height)
    surface.canvas.clear(Color.WHITE)
    surface.canvas.drawImage(Image.makeFromBitmap(source), 0f, 0f)
    return surface.makeImageSnapshot().encodeToData(EncodedImageFormat.JPEG, qualite)?.bytes
            ?: error("Encodage JPEG impossible")
}

/** Archive ZIP proposée dans la feuille de partage (Fichiers, AirDrop, mail…), comme l'export JSON. */
@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual suspend fun exporterZip(contenu: ByteArray, nomFichier: String): Boolean {
    return try {
        val chemin = withContext(AppDispatchers.IO) {
            val dossier = NSTemporaryDirectory() + NSUUID().UUIDString
            check(NSFileManager.defaultManager.createDirectoryAtPath(dossier, true, null, null))
            val nom = nomFichier.substringAfterLast('/').ifBlank { "vetnutri_graphiques.zip" }
            val cheminFichier = "$dossier/$nom"
            FileSystem.SYSTEM.write(cheminFichier.toPath()) { write(contenu) }
            cheminFichier
        }
        withContext(Dispatchers.Main) {
            val presentateur = jsonPresenter() ?: return@withContext false
            val controleur = UIActivityViewController(listOf(NSURL.fileURLWithPath(chemin)), null)
            controleur.popoverPresentationController?.let {
                it.sourceView = presentateur.view
                it.sourceRect = presentateur.view.bounds
            }
            suspendCancellableCoroutine<Boolean> { continuation ->
                controleur.completionWithItemsHandler = { _, termine, _, _ ->
                    if (continuation.isActive) continuation.resume(termine)
                }
                continuation.invokeOnCancellation {
                    dispatch_async(dispatch_get_main_queue()) { controleur.dismissViewControllerAnimated(true, null) }
                }
                presentateur.presentViewController(controleur, true, null)
            }
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        e.rethrowIfCancellation()
        AppLog.e("exporterZip", e.message ?: "Erreur", e)
        false
    }
}
