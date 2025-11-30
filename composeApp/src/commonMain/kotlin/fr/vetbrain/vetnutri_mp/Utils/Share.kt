package fr.vetbrain.vetnutri_mp.Utils

import androidx.compose.runtime.Composable

/**
 * Retourne une fonction permettant de lancer le partage natif de texte
 */
@Composable
expect fun rememberShareLauncher(): (String) -> Unit








