package fr.vetbrain.vetnutri_mp.View.Components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun QRCodeScannerView(
    onCodeScanned: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
)
