package fr.vetbrain.vetnutri_mp.Export

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.ByteArrayOutputStream

/**
 * Implémentation Desktop pour ImageBitmap.toByteArray()
 */
actual fun ImageBitmap.toByteArray(): ByteArray {
    return try {
        val awtImage = this.toAwtImage()
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(awtImage, "PNG", outputStream)
        outputStream.toByteArray()
    } catch (e: Exception) {
        ByteArray(0)
    }
}

/**
 * Implémentation Desktop pour drawTextOnCanvas
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
}
