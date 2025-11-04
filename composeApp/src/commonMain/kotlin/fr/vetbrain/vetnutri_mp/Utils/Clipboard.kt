package fr.vetbrain.vetnutri_mp.Utils

import androidx.compose.runtime.Composable

/**
 * Copie du texte dans le presse-papiers multiplateforme
 * Pour Android, doit être appelé depuis un Composable
 */
@Composable
expect fun copyToClipboardComposable(text: String)

