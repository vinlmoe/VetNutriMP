package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.ui.graphics.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import network.chaintech.qrkit.QrCode

actual suspend fun generateQrCodeBitmap(text: String, size: Int): ImageBitmap? = withContext(Dispatchers.Default) {
    try {
        val qrCode = QrCode.encodeText(text, QrCode.Ecc.MEDIUM)
        val qrSize = qrCode.size
        val scale = size / qrSize
        val imageSize = qrSize * scale
        
        val imageBitmap = ImageBitmap(imageSize, imageSize)
        val canvas = Canvas(imageBitmap)
        
        // Dessiner le QR code
        for (y in 0 until qrSize) {
            for (x in 0 until qrSize) {
                val isBlack = qrCode.getModule(x, y)
                val color = if (isBlack) {
                    Color.Black
                } else {
                    Color.White
                }
                
                val paint = Paint().apply {
                    this.color = color
                    style = PaintingStyle.Fill
                }
                
                canvas.drawRect(
                    left = (x * scale).toFloat(),
                    top = (y * scale).toFloat(),
                    right = ((x + 1) * scale).toFloat(),
                    bottom = ((y + 1) * scale).toFloat(),
                    paint = paint
                )
            }
        }
        
        imageBitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

