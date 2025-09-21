package fr.vetbrain.vetnutri_mp.Export

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

/**
 * Implémentation Android pour la conversion ImageBitmap vers ByteArray
 */
actual fun ImageBitmap.toByteArray(): ByteArray {
    val androidBitmap = this.asAndroidBitmap()
    val outputStream = ByteArrayOutputStream()
    androidBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    return outputStream.toByteArray()
}

/**
 * Implémentation Android pour le dessin de texte sur Canvas
 */
actual fun drawTextOnCanvas(
    canvas: Canvas,
    text: String,
    x: Float,
    y: Float,
    textSize: Float,
    color: Color
) {
    // Note: Le dessin de texte sur Canvas nécessite une implémentation plus complexe
    // Pour l'instant, on se contente d'un placeholder
    // TODO: Implémenter le dessin de texte pour Android
}
