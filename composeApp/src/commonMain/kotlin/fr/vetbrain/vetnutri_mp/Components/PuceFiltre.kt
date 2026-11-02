package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

/**
 * Puce de filtre aux couleurs de l'application : turquoise plein quand elle est choisie, contour
 * sinon. Le texte reste sur une ligne (une puce trop étroite coupait les mots).
 */
@Composable
fun PuceFiltre(texte: String, selectionnee: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (selectionnee) VetNutriColors.Primary else MaterialTheme.colors.surface,
            contentColor = if (selectionnee) VetNutriColors.OnPrimary else VetNutriColors.PrimaryVariant,
            border = BorderStroke(1.dp, if (selectionnee) VetNutriColors.Primary else VetNutriColors.Primary.copy(alpha = 0.5f)),
            modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(
                texte,
                style = MaterialTheme.typography.body2,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
