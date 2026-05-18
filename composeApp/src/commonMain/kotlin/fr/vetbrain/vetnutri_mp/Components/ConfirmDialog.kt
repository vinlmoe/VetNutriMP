package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

/**
 * Boîte de dialogue de confirmation réutilisable.
 *
 * @param isDestructive Si true, le bouton de confirmation est affiché en rouge
 */
@Composable
fun ConfirmDialog(
        title: String,
        message: String,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
        confirmText: String = "Confirmer",
        dismissText: String = "Annuler",
        isDestructive: Boolean = false,
        onDismissRequest: () -> Unit = onDismiss,
        modifier: Modifier = Modifier
) {
        AlertDialog(
                onDismissRequest = onDismissRequest,
                title = { Text(text = title, style = MaterialTheme.typography.h6) },
                text = { Text(text = message, style = MaterialTheme.typography.body2) },
                confirmButton = {
                        Button(
                                onClick = onConfirm,
                                colors = ButtonDefaults.buttonColors(
                                        backgroundColor = if (isDestructive) VetNutriColors.Error else VetNutriColors.Primary,
                                        contentColor = Color.White
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp)
                        ) { Text(confirmText) }
                },
                dismissButton = {
                        OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.padding(horizontal = 8.dp)
                        ) { Text(dismissText) }
                },
                backgroundColor = MaterialTheme.colors.surface,
                modifier = modifier
        )
}
