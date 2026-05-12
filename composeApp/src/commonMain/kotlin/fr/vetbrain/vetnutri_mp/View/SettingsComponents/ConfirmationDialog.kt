package fr.vetbrain.vetnutri_mp.View.SettingsComponents

import androidx.compose.runtime.Composable
import fr.vetbrain.vetnutri_mp.Components.ConfirmDialog

/** Délègue à [ConfirmDialog] — conservé pour compatibilité avec les appelants existants. */
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
    ConfirmDialog(
            title = title,
            message = message,
            confirmText = confirmText,
            dismissText = dismissText,
            isDestructive = isDestructive,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
            onDismissRequest = onDismissRequest
    )
}

/** Dialogue spécialisé pour les suppressions de base de données. */
@Composable
fun DatabaseClearConfirmationDialog(
        entityName: String,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
) {
    ConfirmDialog(
            title = "Confirmation de suppression",
            message = "Êtes-vous sûr de vouloir supprimer TOUS les $entityName de la base de données ? Cette action est irréversible.",
            confirmText = "Oui, vider la base",
            dismissText = "Annuler",
            isDestructive = true,
            onConfirm = onConfirm,
            onDismiss = onDismiss
    )
}
