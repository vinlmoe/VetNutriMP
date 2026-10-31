package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.TabRow
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Barre d'onglets qui se répartit sur toute la largeur quand la place suffit et devient défilante
 * sinon : sur un téléphone, les libellés restent sur une ligne au lieu d'être coupés en milieu de
 * mot.
 *
 * @param tabCount nombre d'onglets émis par [tabs]
 * @param minTabWidth largeur en dessous de laquelle un onglet réparti deviendrait illisible
 */
@Composable
fun AdaptiveTabRow(
        selectedTabIndex: Int,
        tabCount: Int,
        modifier: Modifier = Modifier,
        backgroundColor: Color = MaterialTheme.colors.surface,
        contentColor: Color = contentColorFor(backgroundColor),
        minTabWidth: Dp = 110.dp,
        tabs: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        if (maxWidth < minTabWidth * tabCount) {
            ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    backgroundColor = backgroundColor,
                    contentColor = contentColor,
                    edgePadding = 0.dp,
                    tabs = tabs
            )
        } else {
            TabRow(
                    selectedTabIndex = selectedTabIndex,
                    backgroundColor = backgroundColor,
                    contentColor = contentColor,
                    tabs = tabs
            )
        }
    }
}
