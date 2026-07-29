package fr.vetbrain.vetnutri_mp.View.Components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import qrscanner.CameraLens
import qrscanner.QrScanner

@Composable
actual fun QRCodeScannerView(
    onCodeScanned: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        QrScanner(
            modifier = Modifier.fillMaxSize(),
            flashlightOn = false,
            openImagePicker = false,
            cameraLens = CameraLens.Back,
            onCompletion = { result ->
                onCodeScanned(result)
            },
            onFailure = {
                onClose()
            },
            imagePickerHandler = {
                // Handle image picker state change if needed
            }
        )
    }
}
