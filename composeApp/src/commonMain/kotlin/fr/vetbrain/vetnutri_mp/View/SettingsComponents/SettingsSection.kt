package fr.vetbrain.vetnutri_mp.View.SettingsComponents

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

/**
 * Section de paramètres avec titre et contenu, avec animations
 * @param title Titre de la section
 * @param subtitle Sous-titre optionnel de la section
 * @param icon Icône optionnelle de la section
 * @param content Contenu de la section
 * @param modifier Modificateur appliqué au composant
 */
@Composable
fun SettingsSection(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "section_alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = tween(durationMillis = 300),
        label = "section_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .scale(scale),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = AppSizes.elevationSmall,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(AppSizes.paddingLarge)
        ) {
            // En-tête de la section avec animation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = AppSizes.paddingMedium)
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = VetNutriColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                }
                
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.h6,
                        color = VetNutriColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Ligne de séparation
            Divider(
                color = VetNutriColors.Primary.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = AppSizes.paddingSmall)
            )

            // Contenu de la section
            Box(
                modifier = Modifier.padding(top = AppSizes.paddingMedium)
            ) {
                content()
            }
        }
    }
}

/**
 * Section d'information avec style distinctif et animations
 * @param title Titre de la section
 * @param message Message d'information
 * @param modifier Modificateur appliqué au composant
 */
@Composable
fun InfoSection(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 100),
        label = "info_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha),
        backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1f),
        elevation = 1.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(AppSizes.paddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Information",
                tint = VetNutriColors.Primary,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Bold,
                    color = VetNutriColors.Primary
                )
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

/**
 * Section d'avertissement avec style distinctif et animations
 * @param title Titre de la section
 * @param message Message d'avertissement
 * @param modifier Modificateur appliqué au composant
 */
@Composable
fun WarningSection(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 200),
        label = "warning_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha),
        backgroundColor = VetNutriColors.Error.copy(alpha = 0.1f),
        elevation = 1.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(AppSizes.paddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Avertissement",
                tint = VetNutriColors.Error,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Bold,
                    color = VetNutriColors.Error
                )
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
