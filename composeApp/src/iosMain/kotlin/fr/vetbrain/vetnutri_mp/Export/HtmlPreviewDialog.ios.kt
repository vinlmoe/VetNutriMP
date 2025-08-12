package fr.vetbrain.vetnutri_mp.Export

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import platform.WebKit.WKWebView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun HtmlPreviewDialog(
        html: String,
        isVisible: Boolean,
        onConfirmExport: () -> Unit,
        onDismiss: () -> Unit
) {
        if (!isVisible) return
        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Prévisualisation") },
                text = {
                        UIKitView(
                                factory = {
                                        val webView = WKWebView()
                                        webView.loadHTMLString(html, baseURL = null)
                                        webView
                                },
                                modifier = Modifier.fillMaxWidth().height(400.dp)
                        )
                },
                confirmButton = { Button(onClick = onConfirmExport) { Text("Exporter en PDF") } },
                dismissButton = { Button(onClick = onDismiss) { Text("Fermer") } }
        )
}
