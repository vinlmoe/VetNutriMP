package fr.vetbrain.vetnutri_mp.Export

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color

/**
 * Implémentation iOS pour ImageBitmap.toByteArray()
 */
actual fun ImageBitmap.toByteArray(): ByteArray {
    // TODO: Implémenter la conversion pour iOS
    return ByteArray(0)
}

/**
 * Implémentation iOS pour drawTextOnCanvas
 */
actual fun drawTextOnCanvas(
    canvas: Canvas,
    text: String,
    x: Float,
    y: Float,
    textSize: Float,
    color: Color
) {
    // TODO: Implémenter le dessin de texte pour iOS
}