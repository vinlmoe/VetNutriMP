package fr.vetbrain.vetnutri_mp.Export

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter
import org.jetbrains.skia.Color
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface

actual fun encoderJpeg(image: ImageBitmap, qualite: Int): ByteArray {
    val source = image.asSkiaBitmap()
    val surface = Surface.makeRasterN32Premul(source.width, source.height)
    surface.canvas.clear(Color.WHITE)
    surface.canvas.drawImage(Image.makeFromBitmap(source), 0f, 0f)
    return surface.makeImageSnapshot().encodeToData(EncodedImageFormat.JPEG, qualite)?.bytes
            ?: error("Encodage JPEG impossible")
}

actual suspend fun exporterZip(contenu: ByteArray, nomFichier: String): Boolean {
    // Tests automatisés (pilote de captures) : enregistrement sans dialogue
    System.getenv("VETNUTRI_TEST_EXPORT_DIR")?.takeIf { it.isNotBlank() }?.let { dossier ->
        File(dossier).mkdirs()
        File(dossier, nomFichier).writeBytes(contenu)
        return true
    }
    var resultat = false
    val dialogue = {
        val selecteur = JFileChooser().apply {
            dialogTitle = nomFichier
            selectedFile = File(nomFichier)
            fileFilter = FileNameExtensionFilter("ZIP (*.zip)", "zip")
        }
        if (selecteur.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            val choisi = selecteur.selectedFile
            val fichier = if (choisi.name.endsWith(".zip", ignoreCase = true)) choisi else File(choisi.path + ".zip")
            resultat = try {
                fichier.writeBytes(contenu)
                true
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                fr.vetbrain.vetnutri_mp.Utils.AppLog.e("exporterZip", e.message ?: "Erreur", e)
                false
            }
        }
    }
    // Le dialogue Swing s'ouvre sur l'EDT, jamais depuis la coroutine Compose appelante (runSwingDialog)
    fr.vetbrain.vetnutri_mp.ExcelPlatform.runSwingDialog(dialogue)
    return resultat
}
