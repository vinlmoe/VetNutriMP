package fr.vetbrain.vetnutri_mp.Export

import androidx.compose.runtime.Composable

/** Affiche une boîte de dialogue de prévisualisation HTML avec actions. */
@Composable
expect fun HtmlPreviewDialog(
        html: String,
        isVisible: Boolean,
        onConfirmExport: () -> Unit,
        onDismiss: () -> Unit
)
