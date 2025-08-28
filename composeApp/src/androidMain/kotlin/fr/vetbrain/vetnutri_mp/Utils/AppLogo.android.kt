package fr.vetbrain.vetnutri_mp.Utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import fr.vetbrain.vetnutri_mp.R

/**
 * Implémentation Android du logo de l'application
 * Utilise l'icône native Android
 */
@Composable
actual fun AppLogo(
    modifier: Modifier,
    size: Dp,
    tint: Color,
    contentDescription: String?
) {
    Image(
        painter = painterResource(R.drawable.ic_launcher),
        contentDescription = contentDescription,
        modifier = modifier.size(size)
    )
} 