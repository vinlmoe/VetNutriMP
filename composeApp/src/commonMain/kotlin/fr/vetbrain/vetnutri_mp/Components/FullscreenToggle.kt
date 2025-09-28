package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Utils.isFullscreen
import fr.vetbrain.vetnutri_mp.Utils.setFullscreen

/**
 * Composant pour basculer le mode plein écran
 * Disponible uniquement sur Android
 */
@Composable
fun FullscreenToggle(
    modifier: Modifier = Modifier
) {
    var isFullscreenState by remember { mutableStateOf(isFullscreen()) }
    
    LaunchedEffect(Unit) {
        isFullscreenState = isFullscreen()
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = { 
                val newState = !isFullscreenState
                setFullscreen(newState)
                isFullscreenState = newState
            }
        ) {
            Icon(
                imageVector = if (isFullscreenState) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                contentDescription = if (isFullscreenState) "Quitter le mode plein écran" else "Mode plein écran"
            )
        }
        
        Text(
            text = if (isFullscreenState) "Plein écran" else "Mode normal",
            style = MaterialTheme.typography.caption
        )
    }
}
