package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.foundation.layout.defaultMinSize

/**
 * Composant IconButton avec Tooltip intégré.
 * Simule le comportement de IconButton (taille min 48dp) avec gestion du tooltip.
 */
@Composable
fun IconButtonWithTooltip(
    imageVector: ImageVector,
    contentDescription: String?,
    tooltip: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    tint: Color = LocalContentColor.current,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier
) {
    IconWithTooltip(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tooltip = tooltip,
        onClick = onClick,
        enabled = enabled,
        tint = tint,
        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp).then(modifier),
        iconModifier = iconModifier
    )
}

/**
 * Composant Icon avec Tooltip intégré.
 * Remplace le composant Icon standard + clickable.
 * Gère le clic, le survol (tooltip) et l'appui long (tooltip).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IconWithTooltip(
    imageVector: ImageVector,
    contentDescription: String?,
    tooltip: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    tint: Color = LocalContentColor.current,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier
) {
    var isHovering by remember { mutableStateOf(false) }
    var showHoverTooltip by remember { mutableStateOf(false) }
    var showLongPressTooltip by remember { mutableStateOf(false) }

    // Gestion du survol avec délai pour éviter le clignotement
    LaunchedEffect(isHovering) {
        if (isHovering) {
            delay(500) // Augmentation du délai à 500ms
            showHoverTooltip = true
        } else {
            showHoverTooltip = false
        }
    }

    // Gestion de l'appui long avec fermeture automatique
    LaunchedEffect(showLongPressTooltip) {
        if (showLongPressTooltip) {
            delay(2000)
            showLongPressTooltip = false
        }
    }

    val showTooltip = showHoverTooltip || showLongPressTooltip

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .combinedClickable(
                enabled = enabled,
                onClick = onClick,
                onLongClick = {
                    if (enabled) {
                        showLongPressTooltip = true
                    }
                }
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Enter) {
                            isHovering = true
                        } else if (event.type == PointerEventType.Exit) {
                            isHovering = false
                        }
                    }
                }
            }
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint,
            modifier = iconModifier
        )

        if (showTooltip) {
            TooltipPopup(text = tooltip, onDismiss = { 
                showHoverTooltip = false
                showLongPressTooltip = false
            })
        }
    }
}


@Composable
fun TooltipArea(
    tooltip: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var showTooltip by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Enter) {
                            showTooltip = true
                        } else if (event.type == PointerEventType.Exit) {
                            showTooltip = false
                        }
                    }
                }
            }
    ) {
        content()
        if (showTooltip) {
            TooltipPopup(text = tooltip, onDismiss = { showTooltip = false })
        }
    }
}

@Composable
private fun TooltipPopup(
    text: String,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current
    val offsetY = with(density) { 40.dp.roundToPx() }
    
    Popup(
        alignment = Alignment.BottomCenter,
        offset = IntOffset(0, offsetY),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = false)
    ) {
        Box(
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(4.dp))
                .background(Color.DarkGray, RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.caption,
                color = Color.White
            )
        }
    }
}
