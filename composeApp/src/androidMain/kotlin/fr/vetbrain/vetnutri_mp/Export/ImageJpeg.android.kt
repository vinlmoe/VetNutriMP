package fr.vetbrain.vetnutri_mp.Export

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import fr.vetbrain.vetnutri_mp.JsonFileOperationsBridge
import fr.vetbrain.vetnutri_mp.Localization.AndroidContext
import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual fun encoderJpeg(image: ImageBitmap, qualite: Int): ByteArray {
    // Une capture peut être un bitmap matériel : copie logicielle avant de dessiner dessus
    val source = image.asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, false)
    val resultat = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    Canvas(resultat).apply {
        drawColor(android.graphics.Color.WHITE)
        drawBitmap(source, 0f, 0f, null)
    }
    return ByteArrayOutputStream().use { sortie ->
        resultat.compress(Bitmap.CompressFormat.JPEG, qualite, sortie)
        sortie.toByteArray()
    }
}

actual suspend fun exporterZip(contenu: ByteArray, nomFichier: String): Boolean {
    return try {
        val uri = JsonFileOperationsBridge.pickExportZip(nomFichier) ?: return false
        withContext(Dispatchers.IO) {
            val flux = AndroidContext.appContext.contentResolver.openOutputStream(uri, "wt")
                ?: error("Impossible d'ouvrir la destination")
            flux.use { it.write(contenu) }
        }
        true
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        e.rethrowIfCancellation()
        fr.vetbrain.vetnutri_mp.Utils.AppLog.e("exporterZip", e.message ?: "Erreur", e)
        false
    }
}
