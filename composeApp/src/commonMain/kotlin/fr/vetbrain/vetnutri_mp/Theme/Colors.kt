package fr.vetbrain.vetnutri_mp.Theme

import androidx.compose.ui.graphics.Color

object AppColors {
    // Couleurs principales
    val Primary = Color(0xFF6200EE)
    val PrimaryVariant = Color(0xFF3700B3)
    val Secondary = Color(0xFF03DAC6)
    val SecondaryVariant = Color(0xFF018786)

    // Couleurs de fond
    val Background = Color(0xFFFFFFFF)
    val Surface = Color(0xFFFFFFFF)

    // Couleurs de texte
    val OnPrimary = Color(0xFFFFFFFF)
    val OnSecondary = Color(0xFF000000)
    val OnBackground = Color(0xFF000000)
    val OnSurface = Color(0xFF000000)

    // Couleurs d'état
    val Error = Color(0xFFB00020)
    val OnError = Color(0xFFFFFFFF)
    
    // Couleurs spécifiques pour le code
    object CodeColors {
        val Keyword = Color(0xFF0033B3)        // mots-clés comme class, fun, val, var
        val String = Color(0xFF008000)         // chaînes de caractères
        val Number = Color(0xFF0000FF)         // nombres
        val Comment = Color(0xFF808080)        // commentaires
        val Annotation = Color(0xFF808000)     // annotations comme @Composable
        val Function = Color(0xFF000080)       // noms de fonctions
        val Property = Color(0xFF660E7A)       // propriétés
        val Type = Color(0xFF20999D)          // types comme String, Int
    }
} 