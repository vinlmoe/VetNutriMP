package fr.vetbrain.vetnutri_mp.Utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp

/**
 * Implémentation Desktop du logo de l'application
 * Utilise l'icône applicative desktop
 */
@Composable
actual fun AppLogo(
    modifier: Modifier,
    size: Dp,
    tint: Color,
    contentDescription: String?
) {
    Image(
        painter = painterResource("icon.png"),
        contentDescription = contentDescription,
        modifier = modifier.size(size)
    )
}
