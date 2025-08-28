package fr.vetbrain.vetnutri_mp.Utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * Composant multiplateforme pour afficher le logo de l'application
 * Déclaration expect pour les implémentations spécifiques aux plateformes
 */
@Composable
expect fun AppLogo(
    modifier: Modifier,
    size: Dp,
    tint: Color,
    contentDescription: String?
) 