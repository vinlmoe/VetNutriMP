package fr.vetbrain.vetnutri_mp.Navigation

import androidx.compose.runtime.Composable

/**
 * Intercepte le « retour » système de la plateforme (bouton/geste Android) tant que [enabled] est vrai.
 * Sans effet sur Desktop et iOS, qui n'ont pas de retour système global.
 */
@Composable
internal expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
