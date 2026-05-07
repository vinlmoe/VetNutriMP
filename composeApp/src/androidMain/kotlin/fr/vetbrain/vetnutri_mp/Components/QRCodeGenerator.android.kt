package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import qrgenerator.generateCode

actual suspend fun generateQrCodeBitmap(text: String, size: Int): ImageBitmap? = withContext(Dispatchers.Default) {
    try {
        // Utiliser l'API publique generateCode de qrgenerator
        val qrBitmap = generateCode(text)
        
        // Redimensionner si nécessaire
        if (qrBitmap.width == size && qrBitmap.height == size) {
            qrBitmap
        } else {
            // Redimensionner l'image
            val resizedBitmap = ImageBitmap(size, size)
            val canvas = Canvas(resizedBitmap)
            val paint = Paint().apply {
                isAntiAlias = true
            }
            // Utiliser drawImage avec scale
            val scaleX = size.toFloat() / qrBitmap.width.toFloat()
            val scaleY = size.toFloat() / qrBitmap.height.toFloat()
            canvas.scale(scaleX, scaleY)
            canvas.drawImage(qrBitmap, Offset.Zero, paint)
            resizedBitmap
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
