package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Composant pour disposer des éléments en rangées avec retour à la ligne automatique Ce composant
 * organise les éléments enfants horizontalement jusqu'à ce qu'ils atteignent la largeur maximale,
 * puis continue sur la ligne suivante.
 *
 * @param modifier Modificateur à appliquer au layout
 * @param mainAxisSpacing Espacement horizontal entre les éléments
 * @param crossAxisSpacing Espacement vertical entre les lignes
 * @param content Contenu à afficher dans le FlowRow
 */
@Composable
fun FlowRow(
        modifier: Modifier = Modifier,
        mainAxisSpacing: Dp = 0.dp,
        crossAxisSpacing: Dp = 0.dp,
        content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val rows = mutableListOf<MutableList<Placeable>>()
        val rowConstraints = constraints.copy(minWidth = 0)

        var rowMainAxisSize = 0
        var rowMainAxisOffset = 0

        var rowCrossAxisOffset = 0

        val currentRow = mutableListOf<Placeable>()

        measurables.forEach { measurable ->
            val placeable = measurable.measure(rowConstraints)

            if (rowMainAxisSize + mainAxisSpacing.roundToPx() + placeable.width >
                            constraints.maxWidth
            ) {
                rows.add(currentRow.toMutableList())
                rowMainAxisSize = 0
                rowMainAxisOffset = 0
                rowCrossAxisOffset += currentRow.maxByOrNull { it.height }?.height ?: 0
                rowCrossAxisOffset += crossAxisSpacing.roundToPx()
                currentRow.clear()
            }

            rowMainAxisSize += placeable.width + mainAxisSpacing.roundToPx()
            currentRow.add(placeable)
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        val height =
                rows.sumOf { row -> row.maxByOrNull { it.height }?.height ?: 0 } +
                        (rows.size - 1) * crossAxisSpacing.roundToPx()

        val width = constraints.maxWidth

        layout(width, height) {
            var yPosition = 0

            rows.forEach { row ->
                var xPosition = 0

                row.forEach { placeable ->
                    placeable.placeRelative(xPosition, yPosition)
                    xPosition += placeable.width + mainAxisSpacing.roundToPx()
                }

                yPosition += row.maxByOrNull { it.height }?.height ?: 0
                yPosition += crossAxisSpacing.roundToPx()
            }
        }
    }
}
 