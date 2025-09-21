package fr.vetbrain.vetnutri_mp.Export

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * Implémentation Desktop (JVM) pour la conversion ImageBitmap vers ByteArray
 */
actual fun ImageBitmap.toByteArray(): ByteArray {
    return try {
        val awtImage = this.toAwtImage()
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(awtImage, "PNG", outputStream)
        val result = outputStream.toByteArray()
        println("DEBUG: ImageBitmap.toByteArray() - Taille générée: ${result.size} bytes")
        result
    } catch (e: Exception) {
        println("DEBUG: Erreur ImageBitmap.toByteArray(): ${e.message}")
        e.printStackTrace()
        ByteArray(0)
    }
}

/**
 * Implémentation Desktop (JVM) pour le dessin de texte sur Canvas
 */
actual fun drawTextOnCanvas(
    canvas: Canvas,
    text: String,
    x: Float,
    y: Float,
    textSize: Float,
    color: Color
) {
    // Note: Pour Desktop, le dessin de texte est géré directement dans drawBulletGraphOnCanvas
    // en créant un BufferedImage avec Graphics2D. Cette fonction est un placeholder.
    println("DEBUG: drawTextOnCanvas appelé pour Desktop - texte: '$text' à ($x, $y)")
}
