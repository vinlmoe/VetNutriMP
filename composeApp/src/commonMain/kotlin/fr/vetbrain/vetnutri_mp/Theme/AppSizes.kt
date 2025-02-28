package fr.vetbrain.vetnutri_mp.Theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AppSizes {
    var sizeMultiplier by mutableStateOf(1f)

    // Espacements
    val paddingXXSmall
        get() = (4 * sizeMultiplier).dp
    val paddingXSmall
        get() = (6 * sizeMultiplier).dp
    val paddingSmall
        get() = (8 * sizeMultiplier).dp
    val paddingMedium
        get() = (16 * sizeMultiplier).dp
    val paddingLarge
        get() = (24 * sizeMultiplier).dp
    val paddingXLarge
        get() = (32 * sizeMultiplier).dp

    // Élévations
    val elevationSmall
        get() = (2 * sizeMultiplier).dp
    val elevationMedium
        get() = (4 * sizeMultiplier).dp
    val elevationLarge
        get() = (8 * sizeMultiplier).dp

    // Tailles d'icônes
    val iconSizeSmall
        get() = (16 * sizeMultiplier).dp
    val iconSizeMedium
        get() = (24 * sizeMultiplier).dp
    val iconSizeLarge
        get() = (32 * sizeMultiplier).dp

    // Tailles de texte
    val fontSizeCaption
        get() = (12 * sizeMultiplier).sp
    val fontSizeBody2
        get() = (14 * sizeMultiplier).sp
    val fontSizeBody1
        get() = (16 * sizeMultiplier).sp
    val fontSizeSubtitle1
        get() = (16 * sizeMultiplier).sp
    val fontSizeSubtitle2
        get() = (14 * sizeMultiplier).sp
    val fontSizeH6
        get() = (20 * sizeMultiplier).sp
    val fontSizeH5
        get() = (24 * sizeMultiplier).sp
    val fontSizeH4
        get() = (34 * sizeMultiplier).sp

    // Tailles de composants
    val buttonHeight
        get() = (36 * sizeMultiplier).dp
    val textFieldHeight
        get() = (56 * sizeMultiplier).dp
    val cardElevationNormal
        get() = (2 * sizeMultiplier).dp
    val cardElevationSelected
        get() = (8 * sizeMultiplier).dp
    val cardSpacing
        get() = (8 * sizeMultiplier).dp
    val drawerWidth
        get() = (300 * sizeMultiplier).dp

    // Breakpoints
    val breakpointWideScreen = 840.dp

    fun adjustSize(multiplier: Float) {
        sizeMultiplier = multiplier.coerceIn(0.5f, 2f)
    }
}
