package fr.vetbrain.vetnutri_mp.View.SettingsComponents

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

/**
 * Dialogue de confirmation pour les actions dangereuses
 * @param title Titre du dialogue
 * @param message Message de confirmation
 * @param confirmText Texte du bouton de confirmation
 * @param dismissText Texte du bouton d'annulation
 * @param isDestructive Indique si l'action est destructive (change la couleur du bouton)
 * @param onConfirm Callback appelé lors de la confirmation
 * @param onDismiss Callback appelé lors de l'annulation
 * @param onDismissRequest Callback appelé lors de la fermeture du dialogue
 */
@Composable
fun ConfirmationDialog(
        title: String,
        message: String,
        confirmText: String = "Confirmer",
        dismissText: String = "Annuler",
        isDestructive: Boolean = true,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
        onDismissRequest: () -> Unit = onDismiss
) {
    AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(text = title, style = MaterialTheme.typography.h6) },
            text = { Text(text = message, style = MaterialTheme.typography.body2) },
            confirmButton = {
                Button(
                        onClick = onConfirm,
                        colors =
                                ButtonDefaults.buttonColors(
                                        backgroundColor =
                                                if (isDestructive) {
                                                    VetNutriColors.Error
                                                } else {
                                                    VetNutriColors.Primary
                                                },
                                        contentColor = Color.White
                                ),
                        modifier = Modifier.padding(horizontal = 8.dp)
                ) { Text(confirmText) }
            },
            dismissButton = {
                Button(
                        onClick = onDismiss,
                        colors =
                                ButtonDefaults.buttonColors(
                                        backgroundColor = VetNutriColors.Secondary,
                                        contentColor = VetNutriColors.OnSecondary
                                ),
                        modifier = Modifier.padding(horizontal = 8.dp)
                ) { Text(dismissText) }
            },
            backgroundColor = MaterialTheme.colors.surface
    )
}

/**
 * Dialogue de confirmation spécialisé pour les actions de suppression de base de données
 * @param entityName Nom de l'entité à supprimer (ex: "aliments", "animaux")
 * @param onConfirm Callback appelé lors de la confirmation
 * @param onDismiss Callback appelé lors de l'annulation
 */
@Composable
fun DatabaseClearConfirmationDialog(
        entityName: String,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
) {
    ConfirmationDialog(
            title = "Confirmation de suppression",
            message =
                    "Êtes-vous sûr de vouloir supprimer TOUS les $entityName de la base de données ? Cette action est irréversible.",
            confirmText = "Oui, vider la base",
            dismissText = "Annuler",
            isDestructive = true,
            onConfirm = onConfirm,
            onDismiss = onDismiss
    )
}
