package fr.vetbrain.vetnutri_mp.Theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val LightColorPalette =
        lightColors(
                primary = VetNutriColors.Primary,
                primaryVariant = VetNutriColors.PrimaryVariant,
                secondary = VetNutriColors.Secondary,
                background = VetNutriColors.Background,
                surface = VetNutriColors.Surface,
                error = VetNutriColors.Error,
                onPrimary = VetNutriColors.OnPrimary,
                onSecondary = VetNutriColors.OnSecondary,
                onBackground = VetNutriColors.OnBackground,
                onSurface = VetNutriColors.OnSurface,
                onError = VetNutriColors.OnError
        )

@Composable
fun VetNutriTheme(content: @Composable () -> Unit) {
    MaterialTheme(colors = LightColorPalette, typography = VetNutriTypography, content = content)
}
