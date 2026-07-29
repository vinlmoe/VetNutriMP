package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.ui.graphics.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import java.io.FileOutputStream
import qrcode.QRCode

actual suspend fun generateQrCodeBitmap(text: String, size: Int): ImageBitmap? = withContext(Dispatchers.Default) {
    try {
        // Utiliser QRCode-Kotlin avec l'API correcte
        val qrCode = QRCode.ofSquares()
            .withSize(10)
            .build(text)
        
        // Créer un fichier temporaire pour le rendu
        val tempFile = File.createTempFile("qrcode_", ".png")
        tempFile.deleteOnExit()
        
        // Rendre le QR code et l'écrire dans le fichier
        val rendered = qrCode.render()
        FileOutputStream(tempFile).use { outputStream ->
            rendered.writeImage(outputStream)
        }
        
        // Lire le fichier en BufferedImage
        val bufferedImage = ImageIO.read(tempFile)
        
        // Redimensionner si nécessaire
        val resizedImage = if (bufferedImage.width != size || bufferedImage.height != size) {
            val resized = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
            val graphics = resized.createGraphics()
            graphics.drawImage(bufferedImage, 0, 0, size, size, null)
            graphics.dispose()
            resized
        } else {
            bufferedImage
        }
        
        // Convertir BufferedImage en ImageBitmap en dessinant pixel par pixel
        val imageBitmap = ImageBitmap(size, size)
        val canvas = Canvas(imageBitmap)
        
        // Dessiner le BufferedImage sur le canvas Compose pixel par pixel
        for (y in 0 until size) {
            for (x in 0 until size) {
                val rgb = resizedImage.getRGB(x, y)
                val alpha = (rgb shr 24) and 0xFF
                val red = (rgb shr 16) and 0xFF
                val green = (rgb shr 8) and 0xFF
                val blue = rgb and 0xFF
                
                val color = Color(
                    red = red,
                    green = green,
                    blue = blue,
                    alpha = alpha
                )
                
                val paint = Paint().apply {
                    this.color = color
                    style = PaintingStyle.Fill
                }
                
                canvas.drawRect(
                    left = x.toFloat(),
                    top = y.toFloat(),
                    right = (x + 1).toFloat(),
                    bottom = (y + 1).toFloat(),
                    paint = paint
                )
            }
        }
        
        // Nettoyer le fichier temporaire
        tempFile.delete()
        
        imageBitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

