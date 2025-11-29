package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Interface expect pour générer un QR code
 */
expect suspend fun generateQrCodeBitmap(text: String, size: Int = 256): ImageBitmap?

