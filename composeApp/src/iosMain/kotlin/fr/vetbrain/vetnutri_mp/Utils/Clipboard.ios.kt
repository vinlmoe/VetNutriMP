package fr.vetbrain.vetnutri_mp.Utils

import androidx.compose.runtime.Composable
import platform.UIKit.UIPasteboard

actual @Composable
fun copyToClipboardComposable(text: String) {
    val pasteboard = UIPasteboard.generalPasteboard
    pasteboard.string = text
}

actual @Composable
fun getClipboardTextComposable(): String? {
    val pasteboard = UIPasteboard.generalPasteboard
    return pasteboard.string
}
