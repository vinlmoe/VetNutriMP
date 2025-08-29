package fr.vetbrain.vetnutri_mp.Utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.material.Text

/**
 * Implémentation iOS du logo de l'application
 * Utilise un logo temporaire simple
 */
@Composable
actual fun AppLogo(
    modifier: Modifier,
    size: Dp,
    tint: Color,
    contentDescription: String?
) {
    Box(
        modifier = modifier
            .size(size)
            .background(color = tint, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "V",
            color = Color.White,
            style = androidx.compose.material.MaterialTheme.typography.h6
        )
    }
} 