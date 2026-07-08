package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate

/**
 * Dialog pour demander si l'utilisateur souhaite anonymiser l'export
 */
@Composable
fun AnonymizationDialog(
    onConfirm: (shouldAnonymize: Boolean, shouldEncrypt: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var shouldAnonymize by remember { mutableStateOf(false) }
    var shouldEncrypt by remember { mutableStateOf(true) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Text(translate("anonymizationDialog.title"))
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    translate("anonymizationDialog.question"),
                    style = MaterialTheme.typography.body1
                )

                Text(
                    translate("anonymizationDialog.replacementIntro"),
                    style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    translate("anonymizationDialog.replacementDetails"),
                    style = MaterialTheme.typography.body2
                )

                Divider()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = shouldAnonymize,
                        onCheckedChange = { shouldAnonymize = it }
                    )
                    Text(
                        translate("anonymizationDialog.anonymizeData"),
                        style = MaterialTheme.typography.body2
                    )
                }

                Divider()

                Text(
                    translate("anonymizationDialog.encryptQuestion"),
                    style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = shouldEncrypt,
                        onCheckedChange = { shouldEncrypt = it }
                    )
                    Text(
                        translate("anonymizationDialog.encryptRecommended"),
                        style = MaterialTheme.typography.body2
                    )
                }

                if (!shouldEncrypt) {
                    Text(
                        translate("anonymizationDialog.noEncryptionWarning"),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(shouldAnonymize, shouldEncrypt) }
            ) {
                Text(translate("anonymizationDialog.continueAction"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(translate(LocalizationKeys.General.CANCEL))
            }
        }
    )
}
