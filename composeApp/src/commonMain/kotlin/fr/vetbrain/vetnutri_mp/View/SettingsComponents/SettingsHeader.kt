package fr.vetbrain.vetnutri_mp.View.SettingsComponents

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
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
 * En-tête des paramètres avec bouton de retour et animations
 * @param onBack Callback appelé lors du clic sur le bouton retour
 * @param modifier Modificateur appliqué au composant
 */
@Composable
fun SettingsHeader(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "header_alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.9f,
        animationSpec = tween(durationMillis = 300),
        label = "header_scale"
    )

    Column(
        modifier = modifier
            .alpha(alpha)
            .scale(scale)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSizes.paddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bouton retour avec icône et animation
            var isBackPressed by remember { mutableStateOf(false) }
            val backScale by animateFloatAsState(
                targetValue = if (isBackPressed) 0.95f else 1f,
                animationSpec = tween(durationMillis = 100),
                label = "back_scale"
            )
            
            IconButton(
                onClick = {
                    isBackPressed = true
                    onBack()
                },
                modifier = Modifier
                    .size(AppSizes.iconSizeLarge)
                    .scale(backScale)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Retour",
                    modifier = Modifier.size(AppSizes.iconSizeMedium),
                    tint = VetNutriColors.Primary
                )
            }

            // Titre centré avec animation
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = VetNutriColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Paramètres",
                    style = MaterialTheme.typography.h5.copy(
                        fontSize = AppSizes.fontSizeH5
                    ),
                    color = VetNutriColors.Primary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Espace vide pour équilibrer la mise en page
            Spacer(modifier = Modifier.size(AppSizes.iconSizeLarge))
        }

        // Ligne de séparation avec animation
        Divider(
            color = VetNutriColors.Primary.copy(alpha = 0.2f),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = AppSizes.paddingMedium)
        )
    }
}
