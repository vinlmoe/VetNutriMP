package fr.vetbrain.vetnutri_mp.View.SettingsComponents

import androidx.compose.runtime.Composable
import fr.vetbrain.vetnutri_mp.Components.ConfirmDialog
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate

/** Délègue à [ConfirmDialog] — conservé pour compatibilité avec les appelants existants. */
@Composable
fun ConfirmationDialog(
        title: String,
        message: String,
        confirmText: String = translate(LocalizationKeys.General.CONFIRM),
        dismissText: String = translate(LocalizationKeys.General.CANCEL),
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
            title = translate(LocalizationKeys.Settings.CLEAR_CONFIRMATION_TITLE),
            message = translate(LocalizationKeys.Settings.CLEAR_CONFIRMATION_MESSAGE, entityName),
            confirmText = translate(LocalizationKeys.Settings.CLEAR_CONFIRMATION_CONFIRM),
            dismissText = translate(LocalizationKeys.General.CANCEL),
            isDestructive = true,
            onConfirm = onConfirm,
            onDismiss = onDismiss
    )
}
