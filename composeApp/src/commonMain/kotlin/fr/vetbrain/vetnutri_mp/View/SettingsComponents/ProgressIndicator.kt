package fr.vetbrain.vetnutri_mp.View.SettingsComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

/**
 * Indicateur de progression pour les actions longues
 * @param isVisible Indique si l'indicateur doit être affiché
 * @param modifier Modificateur appliqué au composant
 */
@Composable
fun FullScreenProgressIndicator(isVisible: Boolean, modifier: Modifier = Modifier) {
    if (isVisible) {
        Box(
                modifier = modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = VetNutriColors.Primary) }
    }
}

/**
 * Indicateur de progression avec message
 * @param isVisible Indique si l'indicateur doit être affiché
 * @param message Message à afficher
 * @param modifier Modificateur appliqué au composant
 */
@Composable
fun ProgressIndicatorWithMessage(
        isVisible: Boolean,
        message: String,
        modifier: Modifier = Modifier
) {
    if (isVisible) {
        Box(
                modifier = modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = VetNutriColors.Primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = message, style = MaterialTheme.typography.body1, color = Color.White)
            }
        }
    }
}

/**
 * Indicateur de progression linéaire avec pourcentage
 * @param progress Progression entre 0.0 et 1.0
 * @param message Message optionnel à afficher
 * @param modifier Modificateur appliqué au composant
 */
@Composable
fun LinearProgressWithMessage(
        progress: Float,
        message: String? = null,
        modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                color = VetNutriColors.Primary
        )

        message?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, style = MaterialTheme.typography.caption, color = Color.Gray)
        }
    }
}
