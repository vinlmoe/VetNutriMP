package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Composant pour afficher une boîte de dialogue de confirmation avec deux actions.
 *
 * @param title Titre de la boîte de dialogue
 * @param message Message à afficher
 * @param onConfirm Fonction à appeler lors de la confirmation
 * @param onDismiss Fonction à appeler lors de l'annulation
 * @param confirmText Texte pour le bouton de confirmation
 * @param dismissText Texte pour le bouton d'annulation
 * @param modifier Modifier à appliquer à la boîte de dialogue
 */
@Composable
fun ConfirmDialog(
        title: String,
        message: String,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
        confirmText: String = "Confirmer",
        dismissText: String = "Annuler",
        modifier: Modifier = Modifier
) {
        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(title) },
                text = { Text(message) },
                confirmButton = { Button(onClick = onConfirm) { Text(confirmText) } },
                dismissButton = { OutlinedButton(onClick = onDismiss) { Text(dismissText) } },
                modifier = modifier
        )
}
