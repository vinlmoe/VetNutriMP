package fr.vetbrain.vetnutri_mp.Export

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

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
                // Simplifié: afficher l'HTML brut (on pourrait intégrer une webview desktop si
                // besoin)
                Text(html.take(2000))
            },
            confirmButton = { Button(onClick = onConfirmExport) { Text("Exporter en PDF") } },
            dismissButton = { Button(onClick = onDismiss) { Text("Fermer") } }
    )
}
