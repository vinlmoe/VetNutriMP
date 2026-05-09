package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

/**
 * Composant pour afficher un QR code
 */
@Composable
fun QRCodeView(
    text: String,
    size: Int = 256,
    modifier: Modifier = Modifier
) {
    var qrBitmap by remember(text) { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(text) {
        isLoading = true
        qrBitmap = fr.vetbrain.vetnutri_mp.Components.generateQrCodeBitmap(text, size)
        isLoading = false
    }
    
    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            qrBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = "QR Code",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

