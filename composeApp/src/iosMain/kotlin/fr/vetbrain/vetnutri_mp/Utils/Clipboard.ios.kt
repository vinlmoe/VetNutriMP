package fr.vetbrain.vetnutri_mp.Utils

import androidx.compose.runtime.Composable
import platform.UIKit.UIPasteboard
import platform.UIKit.UIPasteboardGeneral

actual @Composable
fun copyToClipboardComposable(text: String) {
    val pasteboard = UIPasteboard.generalPasteboard
    pasteboard.string = text
}

