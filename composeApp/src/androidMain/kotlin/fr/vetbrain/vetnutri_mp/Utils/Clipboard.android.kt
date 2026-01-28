package fr.vetbrain.vetnutri_mp.Utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager

actual @Composable
fun copyToClipboardComposable(text: String) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(text))
}

actual @Composable
fun getClipboardTextComposable(): String? {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    return clipboardManager.getText()?.text
}
