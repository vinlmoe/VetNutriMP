package fr.vetbrain.vetnutri_mp.Export

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.unit.dp
import javax.swing.JEditorPane
import javax.swing.JScrollPane

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
                SwingPanel(
                        modifier = Modifier.fillMaxWidth().height(400.dp),
                        factory = {
                            val editor = JEditorPane("text/html", html).apply { isEditable = false }
                            JScrollPane(editor)
                        }
                )
            },
            confirmButton = { Button(onClick = onConfirmExport) { Text("Exporter en PDF") } },
            dismissButton = { Button(onClick = onDismiss) { Text("Fermer") } }
    )
}
