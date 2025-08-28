package fr.vetbrain.vetnutri_mp.Utils

import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * Implémentation Desktop du logo de l'application
 * Utilise une icône simple pour éviter les problèmes de ressources
 */
@Composable
actual fun AppLogo(
    modifier: Modifier,
    size: Dp,
    tint: Color,
    contentDescription: String?
) {
    Icon(
        imageVector = Icons.Default.Storage,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint
    )
} 