package fr.vetbrain.vetnutri_mp.Theme

import androidx.compose.ui.graphics.Color

object VetNutriColors {
    val Primary = Color(0xFF008B8B) // Turquoise foncé
    val PrimaryVariant = Color(0xFF006666) // Turquoise très foncé
    val Secondary = Color(0xFF80A9A9) // Gris turquoise
    val Background = Color(0xFFF5FDFF) // Turquoise extrêmement clair
    val Surface = Color(0xFFF5FDFF) // Turquoise extrêmement clair
    val Error = Color(0xFFB00020) // Rouge erreur

    val OnPrimary = Color(0xFFFFFFFF) // Blanc
    val OnSecondary = Color(0xFF000000) // Noir
    val OnBackground = Color(0xFF000000) // Noir
    val OnSurface = Color(0xFF000000) // Noir
    val OnError = Color(0xFFFFFFFF) // Blanc

    // Couleurs spécifiques pour le code
    object CodeColors {
        val Keyword = Color(0xFF0033B3) // mots-clés comme class, fun, val, var
        val String = Color(0xFF008000) // chaînes de caractères
        val Number = Color(0xFF0000FF) // nombres
        val Comment = Color(0xFF808080) // commentaires
        val Annotation = Color(0xFF808000) // annotations comme @Composable
        val Function = Color(0xFF000080) // noms de fonctions
        val Property = Color(0xFF660E7A) // propriétés
        val Type = Color(0xFF20999D) // types comme String, Int
    }
}
