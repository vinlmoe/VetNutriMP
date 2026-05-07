package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import qrgenerator.generateCode

actual suspend fun generateQrCodeBitmap(text: String, size: Int): ImageBitmap? = withContext(Dispatchers.Default) {
    try {
        val qrBitmap = generateCode(text)

        if (qrBitmap.width == size && qrBitmap.height == size) {
            qrBitmap
        } else {
            val resizedBitmap = ImageBitmap(size, size)
            val canvas = Canvas(resizedBitmap)
            val paint = Paint().apply {
                isAntiAlias = true
            }
            val scaleX = size.toFloat() / qrBitmap.width.toFloat()
            val scaleY = size.toFloat() / qrBitmap.height.toFloat()
            canvas.scale(scaleX, scaleY)
            canvas.drawImage(qrBitmap, Offset.Zero, paint)
            resizedBitmap
        }
    } catch (_: Throwable) {
        null
    }
}
