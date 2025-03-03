package fr.vetbrain.vetnutri_mp.Theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Objet contenant toutes les tailles utilisées dans l'application */
object AppSizes {
    var sizeMultiplier by mutableStateOf(1f)

    // Padding
    val paddingXXSmall
        get() = (2 * sizeMultiplier).dp
    val paddingXSmall
        get() = (4 * sizeMultiplier).dp
    val paddingSmall
        get() = (8 * sizeMultiplier).dp
    val paddingMedium
        get() = (16 * sizeMultiplier).dp
    val paddingLarge
        get() = (24 * sizeMultiplier).dp
    val paddingXLarge
        get() = (32 * sizeMultiplier).dp
    val paddingXXLarge
        get() = (48 * sizeMultiplier).dp

    // Élévation
    val elevationSmall
        get() = (2 * sizeMultiplier).dp
    val elevationMedium
        get() = (4 * sizeMultiplier).dp
    val elevationLarge
        get() = (8 * sizeMultiplier).dp
    val cardElevationNormal
        get() = (1 * sizeMultiplier).dp
    val cardElevationSelected
        get() = (4 * sizeMultiplier).dp

    // Tailles d'icônes
    val iconSizeXSmall
        get() = (16 * sizeMultiplier).dp
    val iconSizeSmall
        get() = (20 * sizeMultiplier).dp
    val iconSizeMedium
        get() = (24 * sizeMultiplier).dp
    val iconSizeLarge
        get() = (32 * sizeMultiplier).dp
    val iconSizeXLarge
        get() = (48 * sizeMultiplier).dp

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
    val fontSizeH3
        get() = (48 * sizeMultiplier).sp
    val fontSizeH2
        get() = (60 * sizeMultiplier).sp
    val fontSizeH1
        get() = (96 * sizeMultiplier).sp

    // Tailles de composants
    val buttonHeight
        get() = (48 * sizeMultiplier).dp
    val textFieldHeight
        get() = (56 * sizeMultiplier).dp
    val cardMinHeight
        get() = (72 * sizeMultiplier).dp
    val cardSpacing
        get() = (8 * sizeMultiplier).dp
    val dividerHeight
        get() = (1 * sizeMultiplier).dp
    val dividerWidth
        get() = (1 * sizeMultiplier).dp
    val borderWidth
        get() = (1 * sizeMultiplier).dp
    val cornerRadius
        get() = (4 * sizeMultiplier).dp
    val cornerRadiusLarge
        get() = (8 * sizeMultiplier).dp
    val cornerRadiusXLarge
        get() = (16 * sizeMultiplier).dp
    val inputMultilineHeight
        get() = (120 * sizeMultiplier).dp

    // Breakpoints
    val breakpointWideScreen = 840.dp

    fun adjustSize(multiplier: Float) {
        sizeMultiplier = multiplier.coerceIn(0.5f, 2f)
    }
}
