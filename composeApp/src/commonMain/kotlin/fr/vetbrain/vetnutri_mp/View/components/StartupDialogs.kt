package fr.vetbrain.vetnutri_mp.View.Components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.DatabaseStatus
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate

/**
 * Dialog optimisé pour la mise à jour de la base de données
 */
@Composable
fun UpdateDialog(
    databaseStatus: DatabaseStatus?,
    isUpdatingDatabase: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = translate(LocalizationKeys.Database.UPDATE_TITLE),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = translate(LocalizationKeys.Database.UPDATE_MSG),
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                databaseStatus?.let { status ->
                    Text(
                        text = "État actuel :",
                        style = MaterialTheme.typography.body2,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "• Aliments : ${status.foodCount}",
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Text(
                        text = "• Références : ${status.referenceCount}",
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                if (isUpdatingDatabase) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = VetNutriColors.Primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = translate(LocalizationKeys.Database.UPDATING),
                            style = MaterialTheme.typography.body2,
                            color = VetNutriColors.Secondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isUpdatingDatabase,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = VetNutriColors.Primary
                )
            ) {
                Text(translate(LocalizationKeys.Update.UPDATE_BUTTON))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isUpdatingDatabase
            ) {
                Text(translate(LocalizationKeys.General.CANCEL))
            }
        },
        modifier = modifier
    )
}

/**
 * Dialog optimisé pour les conditions générales
 */
@Composable
fun TermsDialog(
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = translate(LocalizationKeys.Terms.TITLE),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = translate(LocalizationKeys.Terms.WARNING),
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = translate(LocalizationKeys.Terms.ACCEPT_HEADER),
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = translate(LocalizationKeys.Terms.CONDITION_PRO),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = translate(LocalizationKeys.Terms.CONDITION_INFO),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = translate(LocalizationKeys.Terms.CONDITION_RESPONSIBILITY),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = translate(LocalizationKeys.Terms.CONDITION_PRIVACY),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = translate(LocalizationKeys.Terms.CREDITS),
                    style = MaterialTheme.typography.caption,
                    color = VetNutriColors.Secondary,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = VetNutriColors.Primary
                )
            ) {
                Text(translate(LocalizationKeys.Terms.ACCEPT_BUTTON))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(translate(LocalizationKeys.AnalNut.CLOSE))
            }
        }
    )
}

/**
 * Dialog optimisé pour les mises à jour JSON
 */
@Composable
fun JsonUpdateDialog(
    currentJsonVersion: String?,
    newJsonVersion: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = translate(LocalizationKeys.Update.JSON_TITLE),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = translate(LocalizationKeys.Update.JSON_MSG),
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                currentJsonVersion?.let { current ->
                    Text(
                        text = translate(LocalizationKeys.Update.JSON_CURRENT, current),
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                
                newJsonVersion?.let { new ->
                    Text(
                        text = translate(LocalizationKeys.Update.JSON_NEW, new),
                        style = MaterialTheme.typography.body2,
                        fontWeight = FontWeight.Medium,
                        color = VetNutriColors.Primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = VetNutriColors.Primary
                )
            ) {
                Text(translate(LocalizationKeys.Update.UPDATE_BUTTON))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(translate(LocalizationKeys.Update.LATER_BUTTON))
            }
        },
        modifier = modifier
    )
}
