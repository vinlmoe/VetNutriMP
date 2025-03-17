package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

/**
 * Composant pour afficher un message centré dans un conteneur
 *
 * @param message Le message à afficher
 * @param modifier Le modificateur à appliquer au composant
 * @param textColor La couleur du texte (gris par défaut)
 */
@Composable
fun CenteredMessage(message: String, modifier: Modifier = Modifier, textColor: Color = Color.Gray) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
                text = message,
                style = MaterialTheme.typography.body1,
                color = textColor,
                textAlign = TextAlign.Center
        )
    }
}
