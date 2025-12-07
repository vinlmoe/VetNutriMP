package fr.vetbrain.vetnutri_mp.Utils

import androidx.compose.runtime.Composable
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
actual fun rememberShareLauncher(): (String) -> Unit {
    return { text ->
        // Sur desktop, on copie juste dans le presse-papier
        try {
            val selection = StringSelection(text)
            Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


















