package fr.vetbrain.vetnutri_mp.Navigation

import androidx.compose.runtime.Composable

/** Pas de retour système global sur cette plateforme : aucune interception. */
@Composable
internal actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
}
