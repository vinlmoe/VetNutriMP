package fr.vetbrain.vetnutri_mp.Guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import fr.vetbrain.vetnutri_mp.App
import fr.vetbrain.vetnutri_mp.DataBase.getDatabaseBuilder
import fr.vetbrain.vetnutri_mp.DataBase.getDatabasePath
import fr.vetbrain.vetnutri_mp.DataBase.getRoomDatabase
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

/**
 * Pilote de captures d'écran pour le guide utilisateur (docs/guide_utilisateur).
 *
 * Inactif par défaut : ne s'exécute que si VETNUTRI_GUIDE_SCRIPT pointe vers un script de pas.
 * Lancer avec VETNUTRI_TEST_DATABASE_DIR et VETNUTRI_GUIDE_HOME pointant vers des dossiers
 * jetables pour ne jamais toucher à la base ni aux préférences de l'utilisateur.
 *
 * VETNUTRI_GUIDE_SIZE=<largeur>x<hauteur> change la taille de rendu (1400x900 par défaut),
 * par exemple 400x800 pour vérifier la mise en page sur un téléphone.
 *
 * Commandes du script (une par ligne, `#` = commentaire) :
 *   wait <texte>        attend qu'un nœud contienne ce texte (120 s max)
 *   click <texte>       clique le premier nœud au texte exact
 *   clicksub <texte>    clique le premier nœud contenant ce texte
 *   clickdesc <texte>   clique le premier nœud de cette contentDescription
 *   clickn <n> <texte>  clique le n-ième nœud (0 = premier) au texte exact
 *   type <champ>|<val>  saisit une valeur dans le champ portant ce libellé
 *   shot <nom>          enregistre une capture <nom>.png
 *   dump <nom>          enregistre les textes visibles dans <nom>.txt
 *   sleep <ms>          laisse tourner l'horloge
 */
@OptIn(ExperimentalTestApi::class)
class GuideScreenshotDriver {

    @Test
    fun captures() {
        val scriptPath = System.getenv("VETNUTRI_GUIDE_SCRIPT") ?: return
        System.getenv("VETNUTRI_GUIDE_HOME")?.let { System.setProperty("user.home", it) }
        val outDir = File(System.getenv("VETNUTRI_GUIDE_OUT") ?: "build/guide").apply { mkdirs() }
        val lines = File(scriptPath).readLines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }

        val db = getRoomDatabase(getDatabaseBuilder(), getDatabasePath())

        val (width, height) = (System.getenv("VETNUTRI_GUIDE_SIZE") ?: "1400x900")
            .split('x').map { it.trim().toInt() }

        runDesktopComposeUiTest(width = width, height = height) {
            mainClock.autoAdvance = true
            setContent {
                Box(Modifier.fillMaxSize().background(Color.White)) { App(db) }
            }
            for (line in lines) {
                val cmd = line.substringBefore(' ')
                val arg = line.substringAfter(' ', "")
                println("GUIDE> $line")
                try {
                    when (cmd) {
                        "wait" -> waitUntil(timeoutMillis = 120_000) {
                            onAllNodesWithText(arg, substring = true, useUnmergedTree = true)
                                .fetchSemanticsNodes().isNotEmpty()
                        }
                        "click" -> onAllNodesWithText(arg, useUnmergedTree = true)[0].performClick()
                        "clicksub" -> onAllNodesWithText(arg, substring = true, useUnmergedTree = true)[0].performClick()
                        "scrollto" -> onAllNodesWithText(arg, substring = true, useUnmergedTree = true)[0].performScrollTo()
                        "toggle" -> onAllNodes(isToggleable())[arg.ifEmpty { "0" }.toInt()].performClick()
                        "clickleft" ->onAllNodesWithText(arg, useUnmergedTree = true)[0]
                            .performTouchInput { click(androidx.compose.ui.geometry.Offset(-30f, height / 2f)) }
                        "scrolltodesc" ->onAllNodesWithContentDescription(arg, useUnmergedTree = true)[0].performScrollTo()
                        "scrolldown" ->onAllNodes(hasScrollAction())[arg.ifEmpty { "0" }.toInt()]
                            .performTouchInput { swipeUp(startY = bottom - 10f, endY = top + 10f) }
                        // Défile la dernière zone défilante (celle du dialogue ouvert, le cas échéant)
                        "scrolllast" -> onAllNodes(hasScrollAction()).let { zones ->
                            zones[zones.fetchSemanticsNodes().size - 1]
                                .performTouchInput { swipeUp(startY = bottom - 10f, endY = top + 10f) }
                        }
                        "clickdesc" ->onAllNodesWithContentDescription(arg, useUnmergedTree = true)[0].performClick()
                        "clickdescn" -> {
                            val n = arg.substringBefore(' ').toInt()
                            onAllNodesWithContentDescription(arg.substringAfter(' '), useUnmergedTree = true)[n].performClick()
                        }
                        "clickn" -> {
                            val n = arg.substringBefore(' ').toInt()
                            onAllNodesWithText(arg.substringAfter(' '), useUnmergedTree = true)[n].performClick()
                        }
                        "type" -> {
                            val (label, value) = arg.split('|', limit = 2)
                            val id = onAllNodes(hasSetTextAction() and hasText(label, substring = true))
                                .fetchSemanticsNodes()[0].id
                            val field = onNode(SemanticsMatcher("id=$id") { it.id == id })
                            field.performTextClearance()
                            field.performTextInput(value)
                        }
                        "shot" -> {
                            mainClock.advanceTimeBy(2_000)
                            waitForIdle()
                            val image = captureToImage().toAwtImage()
                            ImageIO.write(image, "png", File(outDir, "$arg.png"))
                        }
                        "dump" -> File(outDir, "$arg.txt").writeText(
                            onAllNodes(isRoot(), useUnmergedTree = true).fetchSemanticsNodes().flatMap { it.descendants() }
                                .mapNotNull { it.describe() }.joinToString("\n")
                        )
                        "sleep" -> {
                            Thread.sleep(arg.toLong())
                            mainClock.advanceTimeBy(arg.toLong())
                            waitForIdle()
                        }
                        else -> error("Commande inconnue : $cmd")
                    }
                } catch (e: Throwable) {
                    println("GUIDE! échec sur « $line » : ${e.message?.lineSequence()?.firstOrNull()}")
                    File(outDir, "_echec.txt").writeText("$line\n${e.message}")
                    ImageIO.write(captureToImage().toAwtImage(), "png", File(outDir, "_echec.png"))
                    File(outDir, "_echec_textes.txt").writeText(
                        onAllNodes(isRoot(), useUnmergedTree = true).fetchSemanticsNodes().flatMap { it.descendants() }
                            .mapNotNull { it.describe() }.joinToString("\n")
                    )
                    break
                }
            }
        }
    }

    private fun SemanticsNode.descendants(): List<SemanticsNode> =
        children.flatMap { listOf(it) + it.descendants() }

    private fun SemanticsNode.describe(): String? {
        val texts = config.getOrNull(SemanticsProperties.Text)?.joinToString(" ") { it.text }
        val desc = config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString(" ")
        val editable = config.getOrNull(SemanticsProperties.EditableText)?.text
        val clickable = config.contains(androidx.compose.ui.semantics.SemanticsActions.OnClick)
        if (texts == null && desc == null && editable == null) return null
        return buildString {
            if (clickable) append("[clic] ")
            texts?.let { append("texte=\"$it\" ") }
            desc?.let { append("desc=\"$it\" ") }
            editable?.let { append("saisie=\"$it\" ") }
            append("@${boundsInRoot.left.toInt()},${boundsInRoot.top.toInt()}")
        }
    }
}
