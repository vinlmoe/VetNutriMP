package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

@Composable
fun ConfirmDialog(title: String, message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                Button(
                        onClick = onConfirm,
                        colors =
                                ButtonDefaults.buttonColors(
                                        backgroundColor = VetNutriColors.Error,
                                        contentColor = VetNutriColors.OnError
                                )
                ) { Text(General.CONFIRM.translate()) }
            },
            dismissButton = {
                Button(
                        onClick = onDismiss,
                        colors =
                                ButtonDefaults.buttonColors(
                                        backgroundColor = VetNutriColors.Secondary,
                                        contentColor = VetNutriColors.OnSecondary
                                )
                ) { Text(General.CANCEL.translate()) }
            }
    )
}
