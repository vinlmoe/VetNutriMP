package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Remplace `AlertDialog` (Material 2) quand le contenu défile (LazyColumn, verticalScroll).
 *
 * `AlertDialog` place son contenu d'après la première ligne de base du texte : quand une liste
 * défile, la première ligne visible change et la boîte de dialogue change de hauteur à chaque
 * défilement. Ici titre, contenu et boutons sont simplement empilés : la taille ne dépend que du
 * contenu, pas de la position de défilement. Mêmes paramètres qu'`AlertDialog`.
 */
@Composable
fun AppAlertDialog(
        onDismissRequest: () -> Unit,
        confirmButton: @Composable () -> Unit = {},
        modifier: Modifier = Modifier,
        dismissButton: @Composable (() -> Unit)? = null,
        title: @Composable (() -> Unit)? = null,
        text: @Composable (() -> Unit)? = null
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
                // Largeur d'AlertDialog par défaut ; un modifier explicite (plein écran…) l'emporte
                modifier = if (modifier == Modifier) Modifier.widthIn(min = 280.dp, max = 560.dp) else modifier,
                shape = MaterialTheme.shapes.medium,
                elevation = 24.dp
        ) {
            Column(modifier = Modifier.padding(top = 24.dp)) {
                title?.let {
                    Box(Modifier.padding(horizontal = 24.dp)) {
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                            ProvideTextStyle(MaterialTheme.typography.subtitle1, it)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
                text?.let {
                    // Le contenu prend la place restante quand la hauteur du dialogue est bornée
                    Box(Modifier.padding(horizontal = 24.dp).weight(1f, fill = false)) {
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            ProvideTextStyle(MaterialTheme.typography.body2, it)
                        }
                    }
                }
                Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
        }
    }
}
