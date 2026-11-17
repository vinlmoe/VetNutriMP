package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Utils.AppLog
import fr.vetbrain.vetnutri_mp.Utils.Recherche
import fr.vetbrain.vetnutri_mp.Utils.isIosPlatform
import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation

/**
 * Menu de choix d'un nutriment avec un champ de recherche en tête (insensible à la casse et aux
 * accents, nom traduit ou libellé français), pour les longues listes de [NUTRIMENT_OPTIONS].
 */
@Composable
internal fun MenuNutriments(
        expanded: Boolean,
        onDismiss: () -> Unit,
        options: List<NutrimentOption>,
        onChoisi: (NutrimentOption) -> Unit
) {
    var requete by remember(expanded) { mutableStateOf("") }
    // Index calculé une fois par liste : nom affiché (langue de l'interface) et libellé français
    val index = remember(options) { options.map { it to Recherche.indexer(it.displayName, it.libelle) } }
    val mots = Recherche.mots(requete)
    val visibles = if (mots.isEmpty()) options else index.filter { (_, texte) -> Recherche.correspond(mots, texte) }.map { it.first }
    val focus = remember { FocusRequester() }

    DropdownMenu(
            expanded = expanded,
            onDismissRequest = { if (!isIosPlatform) onDismiss() },
            modifier = Modifier.heightIn(max = 420.dp).width(320.dp)
    ) {
        OutlinedTextField(
                value = requete,
                onValueChange = { requete = it },
                singleLine = true,
                placeholder = { Text(translate("graphAliments.searchNutrient")) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = AppSizes.paddingSmall)
                        .focusRequester(focus)
        )
        if (visibles.isEmpty()) {
            Text(
                    translate("graphAliments.noNutrientFound"),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(AppSizes.paddingMedium)
            )
        }
        visibles.forEach { option ->
            DropdownMenuItem(onClick = {
                onChoisi(option)
                onDismiss()
            }) {
                Text(
                        if (option.key.isEmpty()) option.displayName else "${option.displayName} (${option.unit})",
                        style = MaterialTheme.typography.body2
                )
            }
        }
    }
    LaunchedEffect(expanded) {
        if (expanded) {
            try {
                focus.requestFocus()
            } catch (e: IllegalStateException) {
                e.rethrowIfCancellation()
                AppLog.d("MenuNutriments", "Champ de recherche pas encore affiché : ${e.message}")
            }
        }
    }
}
