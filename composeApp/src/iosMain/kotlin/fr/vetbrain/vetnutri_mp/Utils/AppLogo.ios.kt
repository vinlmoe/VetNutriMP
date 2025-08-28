package fr.vetbrain.vetnutri_mp.Utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

/**
 * Implémentation iOS du logo de l'application
 * Utilise l'icône native iOS
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun AppLogo(
    modifier: Modifier,
    size: Dp,
    tint: Color,
    contentDescription: String?
) {
    Image(
        painter = painterResource("icons/icon.png"),
        contentDescription = contentDescription,
        modifier = modifier.size(size)
    )
} 