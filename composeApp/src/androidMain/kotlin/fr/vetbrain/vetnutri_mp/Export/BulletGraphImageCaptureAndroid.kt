package fr.vetbrain.vetnutri_mp.Export

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.asAndroidBitmap
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.graphics.Typeface
import java.io.ByteArrayOutputStream

/**
 * Implémentation Android pour ImageBitmap.toByteArray()
 */
actual fun ImageBitmap.toByteArray(): ByteArray {
    return try {
        // Convertir ImageBitmap en Bitmap Android
        val bitmap = this.asAndroidBitmap()
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.toByteArray()
    } catch (e: Exception) {
        ByteArray(0)
    }
}

/**
 * Implémentation Android pour drawTextOnCanvas
 */
actual fun drawTextOnCanvas(
    canvas: Canvas,
    text: String,
    x: Float,
    y: Float,
    textSize: Float,
    color: Color
) {
    try {
        val androidCanvas = canvas.nativeCanvas as AndroidCanvas
        val paint = AndroidPaint().apply {
            this.color = color.toArgb()
            this.textSize = textSize
            this.typeface = Typeface.DEFAULT
            this.isAntiAlias = true
        }
        androidCanvas.drawText(text, x, y, paint)
    } catch (e: Exception) {
    }
}