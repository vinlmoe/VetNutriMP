package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import fr.vetbrain.vetnutri_mp.Theme.AppSizes

/**
 * Composant Badge pour afficher un élément avec un arrière-plan coloré
 *
 * @param text Texte principal à afficher dans le badge
 * @param subText Texte secondaire à afficher en plus petit (facultatif)
 * @param id Identifiant associé au badge (facultatif)
 * @param backgroundColor Couleur d'arrière-plan du badge
 * @param contentColor Couleur du texte
 * @param modifier Modificateur optionnel
 */
@Composable
fun Badge(
        text: String,
        subText: String? = null,
        id: Any? = null,
        backgroundColor: Color,
        contentColor: Color = Color.White,
        modifier: Modifier = Modifier
) {
    Box(
            modifier =
                    modifier.clip(RoundedCornerShape(AppSizes.cornerRadius))
                            .background(backgroundColor)
                            .padding(
                                    horizontal = AppSizes.paddingSmall,
                                    vertical = AppSizes.paddingXXSmall
                            ),
            contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                    text = text,
                    style = MaterialTheme.typography.caption,
                    color = contentColor,
                    fontWeight = FontWeight.Medium
            )

            if (subText != null) {
                Text(
                        text = subText,
                        style =
                                MaterialTheme.typography.caption.copy(
                                        fontSize = AppSizes.fontSizeCaption * 0.8f
                                ),
                        color = contentColor.copy(alpha = 0.7f)
                )
            }

            if (id != null && id.toString() != text && id.toString() != subText) {
                Text(
                        text = id.toString(),
                        style =
                                MaterialTheme.typography.caption.copy(
                                        fontSize = AppSizes.fontSizeCaption * 0.7f
                                ),
                        color = contentColor.copy(alpha = 0.5f)
                )
            }
        }
    }
}
