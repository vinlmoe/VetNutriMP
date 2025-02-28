package fr.vetbrain.vetnutri_mp.Theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

object AppTypography {
    val titleLarge = TextStyle(fontSize = AppSizes.fontSizeH4, fontWeight = FontWeight.Bold)
    val titleMedium = TextStyle(fontSize = AppSizes.fontSizeH6, fontWeight = FontWeight.Medium)
    val titleSmall =
            TextStyle(fontSize = AppSizes.fontSizeSubtitle1, fontWeight = FontWeight.Medium)
    val bodyLarge = TextStyle(fontSize = AppSizes.fontSizeBody1)
    val bodyMedium = TextStyle(fontSize = AppSizes.fontSizeBody2)
    val bodySmall = TextStyle(fontSize = AppSizes.fontSizeCaption)
    val labelLarge = TextStyle(fontSize = AppSizes.fontSizeBody1, fontWeight = FontWeight.Medium)
    val labelMedium = TextStyle(fontSize = AppSizes.fontSizeBody2, fontWeight = FontWeight.Medium)
    val labelSmall = TextStyle(fontSize = AppSizes.fontSizeCaption, fontWeight = FontWeight.Medium)
}
