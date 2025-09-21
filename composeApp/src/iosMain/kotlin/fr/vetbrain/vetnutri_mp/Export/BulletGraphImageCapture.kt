package fr.vetbrain.vetnutri_mp.Export

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toNSImage
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import platform.Foundation.NSData
import platform.Foundation.NSBitmapImageRep
import platform.Foundation.NSBitmapImageFileTypePNG

/**
 * Implémentation iOS pour la conversion ImageBitmap vers ByteArray
 */
actual fun ImageBitmap.toByteArray(): ByteArray {
    val nsImage = this.toNSImage()
    val nsBitmapRep = NSBitmapImageRep(nsImage)
    val nsData = nsBitmapRep.representationUsingType(NSBitmapImageFileTypePNG, properties = null)
    
    return if (nsData != null) {
        val byteArray = ByteArray(nsData.length.toInt())
        nsData.getBytes(byteArray.refTo(0), nsData.length)
        byteArray
    } else {
        ByteArray(0)
    }
}

/**
 * Implémentation iOS pour le dessin de texte sur Canvas
 */
actual fun drawTextOnCanvas(
    canvas: Canvas,
    text: String,
    x: Float,
    y: Float,
    textSize: Float,
    color: Color
) {
    // Note: Le dessin de texte sur iOS nécessite une implémentation plus complexe
    // Pour l'instant, on se contente d'un placeholder
    // TODO: Implémenter le dessin de texte pour iOS
}
