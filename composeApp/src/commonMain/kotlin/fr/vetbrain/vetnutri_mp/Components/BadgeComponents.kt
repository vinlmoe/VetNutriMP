package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import fr.vetbrain.vetnutri_mp.Theme.AppSizes

/**
 * Composant Badge permettant d'afficher une étiquette ou un badge avec une couleur d'arrière-plan
 * personnalisée
 *
 * @param text Texte principal du badge
 * @param subText Texte secondaire optionnel (affiché entre parenthèses)
 * @param id Identifiant optionnel à afficher
 * @param backgroundColor Couleur d'arrière-plan du badge
 * @param modifier Modificateur Compose optionnel
 */
@Composable
fun Badge(
        text: String,
        subText: String? = null,
        id: Any? = null,
        backgroundColor: Color,
        modifier: Modifier = Modifier
) {
        Surface(
                color = backgroundColor.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.small,
                modifier = modifier.padding(vertical = AppSizes.paddingXXSmall)
        ) {
                Column(
                        modifier =
                                Modifier.padding(
                                        horizontal = AppSizes.paddingSmall,
                                        vertical = AppSizes.paddingXSmall
                                ),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        Text(
                                text = text,
                                style =
                                        MaterialTheme.typography.body1.copy(
                                                fontSize = AppSizes.fontSizeBody1
                                        )
                        )
                        if (subText != null || id != null) {
                                Row(
                                        horizontalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingXSmall),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        if (id != null) {
                                                Text(
                                                        text = "ID: $id",
                                                        style =
                                                                MaterialTheme.typography.caption
                                                                        .copy(
                                                                                fontSize =
                                                                                        AppSizes.fontSizeCaption
                                                                        ),
                                                        color = Color.Gray
                                                )
                                        }
                                        if (subText != null) {
                                                Text(
                                                        text = "($subText)",
                                                        style =
                                                                MaterialTheme.typography.caption
                                                                        .copy(
                                                                                fontSize =
                                                                                        AppSizes.fontSizeCaption
                                                                        ),
                                                        color = Color.Gray
                                                )
                                        }
                                }
                        }
                }
        }
}
