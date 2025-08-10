package fr.vetbrain.vetnutri_mp.Export

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun HtmlPreviewDialog(
        html: String,
        isVisible: Boolean,
        onConfirmExport: () -> Unit,
        onDismiss: () -> Unit
) {
    if (!isVisible) return
    val context = LocalContext.current
    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Prévisualisation") },
            text = {
                AndroidView(
                        modifier = Modifier.fillMaxWidth().height(400.dp),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = false
                                webViewClient = WebViewClient()
                                loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                            }
                        }
                )
            },
            confirmButton = { Button(onClick = onConfirmExport) { Text("Exporter en PDF") } },
            dismissButton = { Button(onClick = onDismiss) { Text("Fermer") } }
    )
}
