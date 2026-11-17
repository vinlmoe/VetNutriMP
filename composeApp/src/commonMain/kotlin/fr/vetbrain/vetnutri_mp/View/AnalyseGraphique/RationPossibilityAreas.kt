package fr.vetbrain.vetnutri_mp.View.AnalyseGraphique

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.RationAggregator
import fr.vetbrain.vetnutri_mp.Data.sortedForDisplay
import fr.vetbrain.vetnutri_mp.Enumer.RationAnalysisScope
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import io.github.koalaplot.core.xygraph.Point
import io.github.koalaplot.core.xygraph.XYGraphScope
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi

@Composable
internal fun consultationsPourGraphique(
    consultations: List<ConsultationEv>,
    selected: ConsultationEv?,
    currentOnly: Boolean,
    includeSums: Boolean
): List<ConsultationEv> = remember(consultations, selected, currentOnly, includeSums) {
    prepareGraphConsultations(consultations, selected, currentOnly, includeSums)
}

internal fun prepareGraphConsultations(
    consultations: List<ConsultationEv>,
    selected: ConsultationEv?,
    currentOnly: Boolean,
    includeSums: Boolean
): List<ConsultationEv> {
    val sources = if (currentOnly) listOfNotNull(selected) else
        consultations.map { if (it.uuid == selected?.uuid) selected else it }
    return sources.map { consultation ->
        if (!includeSums) consultation.copy(rations = consultation.rations.sortedForDisplay().toMutableList()) else {
            val sums = listOf(RationAnalysisScope.GROUPE_ACTUELLES, RationAnalysisScope.GROUPE_PROPOSEES)
                .mapNotNull { scope ->
                    RationAggregator.agreger(consultation, scope)?.let {
                        it.copy(uuid = "${it.uuid}-${consultation.uuid}", name = "Σ ${it.name}")
                    }
                }
            consultation.copy(rations = (consultation.rations + sums).sortedForDisplay().toMutableList())
        }
    }
}

/** Convex envelope, independent of ration order; collinear possibilities remain a segment. */
internal fun rationEnvelope(points: List<Pair<Float, Float>>): List<Pair<Float, Float>> {
    val sorted = points.filter { it.first.isFinite() && it.second.isFinite() }
        .distinct().sortedWith(compareBy({ it.first }, { it.second }))
    if (sorted.size <= 2) return sorted
    fun cross(o: Pair<Float, Float>, a: Pair<Float, Float>, b: Pair<Float, Float>): Double =
        (a.first.toDouble() - o.first) * (b.second.toDouble() - o.second) -
            (a.second.toDouble() - o.second) * (b.first.toDouble() - o.first)
    fun half(input: List<Pair<Float, Float>>): List<Pair<Float, Float>> {
        val result = mutableListOf<Pair<Float, Float>>()
        input.forEach { p ->
            while (result.size >= 2 && cross(result[result.lastIndex - 1], result.last(), p) <= 0.0)
                result.removeAt(result.lastIndex)
            result.add(p)
        }
        return result.dropLast(1)
    }
    return half(sorted) + half(sorted.reversed())
}

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
internal fun XYGraphScope<Float, Float>.RationPossibilityAreas(
    points: List<Point<Float, Float>>,
    groups: List<Pair<String, Boolean>>,
    sums: List<Boolean>
) {
    Canvas(Modifier.fillMaxSize()) {
        clipRect {
            points.indices.groupBy { groups[it] }.forEach { (group, indices) ->
                val color = if (group.second) Color(0xFFFF9800) else VetNutriColors.Primary
                val hull = rationEnvelope(indices.filter { !sums[it] }.map { points[it].x to points[it].y })
                val path = Path()
                hull.forEachIndexed { index, p ->
                    val offset = scale(Point(p.first, p.second), size)
                    if (index == 0) path.moveTo(offset.x, offset.y) else path.lineTo(offset.x, offset.y)
                }
                if (hull.size >= 3) {
                    path.close()
                    drawPath(path, color.copy(alpha = 0.16f))
                }
                drawPath(path, color.copy(alpha = 0.7f), style = Stroke(2f))
                indices.filter { sums[it] }.forEach { index ->
                    val point = points[index]
                    if (point.x.isFinite() && point.y.isFinite())
                        drawCircle(color, radius = 10f, center = scale(point, size), style = Stroke(3f))
                }
            }
        }
    }
}
