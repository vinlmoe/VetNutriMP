package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.ui.graphics.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun generateQrCodeBitmap(text: String, size: Int): ImageBitmap? = withContext(Dispatchers.Default) {
    // TODO: Implémenter la génération de QR code pour iOS
    // Pour l'instant, retourner null pour éviter les erreurs de compilation
    // L'implémentation complète nécessitera l'utilisation de CoreImage avec les bonnes API
    null
}
