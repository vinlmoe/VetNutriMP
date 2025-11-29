package fr.vetbrain.vetnutri_mp.View.Components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun QRCodeScannerView(
    onCodeScanned: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Scanner QR Code non disponible sur Desktop")
    }
}


