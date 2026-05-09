package fr.vetbrain.vetnutri_mp.Utils

import androidx.compose.runtime.Composable
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

actual @Composable
fun copyToClipboardComposable(text: String) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val selection = StringSelection(text)
    clipboard.setContents(selection, null)
}

actual @Composable
fun getClipboardTextComposable(): String? {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    return try {
        clipboard.getData(DataFlavor.stringFlavor) as? String
    } catch (e: Exception) {
        null
    }
}
