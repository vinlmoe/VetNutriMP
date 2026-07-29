package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Composant Badge pour afficher un texte court avec un arrière-plan coloré
 *
 * @param text Texte à afficher dans le badge
 * @param backgroundColor Couleur d'arrière-plan du badge
 * @param textColor Couleur du texte du badge
 * @param modifier Modificateur pour personnaliser l'apparence du badge
 */
@Composable
fun Badge(
        text: String,
        backgroundColor: Color,
        textColor: Color = Color.White,
        modifier: Modifier = Modifier
) {
    Box(
            modifier =
                    modifier.clip(RoundedCornerShape(12.dp))
                            .background(backgroundColor)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) { Text(text = text, color = textColor, style = MaterialTheme.typography.caption) }
}
