package fr.vetbrain.vetnutri_mp.View.SettingsComponents

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import kotlinx.coroutines.delay

/**
 * Composant de feedback utilisateur avec animations
 * @param message Message à afficher
 * @param type Type de feedback (SUCCESS, ERROR, WARNING, INFO)
 * @param onDismiss Callback appelé lors de la fermeture
 * @param autoDismiss Délai avant fermeture automatique (en ms, null pour pas de fermeture auto)
 * @param modifier Modificateur appliqué au composant
 */
@Composable
fun UserFeedback(
    message: String,
    type: FeedbackType,
    onDismiss: () -> Unit,
    autoDismiss: Long? = 3000L,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    var isDismissing by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
        autoDismiss?.let { delay ->
            delay(delay)
            isDismissing = true
            delay(300) // Animation de sortie
            onDismiss()
        }
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible && !isDismissing) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "feedback_alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isVisible && !isDismissing) 1f else 0.8f,
        animationSpec = tween(durationMillis = 300),
        label = "feedback_scale"
    )
    
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible && !isDismissing) 0f else -50f,
        animationSpec = tween(durationMillis = 300),
        label = "feedback_slide"
    )

    if (alpha > 0f) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .alpha(alpha)
                .scale(scale),
            backgroundColor = type.backgroundColor,
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSizes.paddingMedium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Icône et message
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                    Icon(
                        imageVector = type.icon,
                        contentDescription = type.description,
                        tint = type.iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Text(
                        text = message,
                        style = MaterialTheme.typography.body1,
                        color = type.textColor,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Bouton de fermeture
                IconButton(
                    onClick = {
                        isDismissing = true
                        onDismiss()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer",
                        tint = type.textColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Types de feedback disponibles
 */
enum class FeedbackType(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val backgroundColor: Color,
    val textColor: Color,
    val iconColor: Color,
    val description: String
) {
    SUCCESS(
        icon = Icons.Default.CheckCircle,
        backgroundColor = Color(0xFFE8F5E8),
        textColor = Color(0xFF2E7D32),
        iconColor = Color(0xFF4CAF50),
        description = "Succès"
    ),
    ERROR(
        icon = Icons.Default.Error,
        backgroundColor = Color(0xFFFFEBEE),
        textColor = Color(0xFFC62828),
        iconColor = Color(0xFFF44336),
        description = "Erreur"
    ),
    WARNING(
        icon = Icons.Default.Warning,
        backgroundColor = Color(0xFFFFF8E1),
        textColor = Color(0xFFEF6C00),
        iconColor = Color(0xFFFF9800),
        description = "Avertissement"
    ),
    INFO(
        icon = Icons.Default.Info,
        backgroundColor = Color(0xFFE3F2FD),
        textColor = Color(0xFF1565C0),
        iconColor = Color(0xFF2196F3),
        description = "Information"
    )
}

/**
 * Indicateur de progression avec animation
 * @param isVisible Si l'indicateur doit être visible
 * @param message Message à afficher
 * @param modifier Modificateur appliqué au composant
 */
@Composable
fun ProgressIndicator(
    isVisible: Boolean,
    message: String,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "progress_alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.9f,
        animationSpec = tween(durationMillis = 300),
        label = "progress_scale"
    )
    
    if (alpha > 0f) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .alpha(alpha)
                .scale(scale),
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 8.dp,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(AppSizes.paddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = VetNutriColors.Primary,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(AppSizes.paddingMedium))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
