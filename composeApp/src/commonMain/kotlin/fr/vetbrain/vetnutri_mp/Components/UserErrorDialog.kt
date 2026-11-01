package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import fr.vetbrain.vetnutri_mp.Utils.UserErrors
import fr.vetbrain.vetnutri_mp.Localization.translate

/** Affiche l'erreur courante de [UserErrors] ; à monter une seule fois, au niveau de l'application. */
@Composable
fun UserErrorDialog() {
    val message by UserErrors.current.collectAsState()
    val texte = message ?: return
    AlertDialog(
        onDismissRequest = { UserErrors.dismiss() },
        title = { Text(translate("erreur.titre"), style = MaterialTheme.typography.h6) },
        text = { Text(texte) },
        confirmButton = { TextButton(onClick = { UserErrors.dismiss() }) { Text("OK") } }
    )
}
