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
                Text("Anonymisation de l'export")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Souhaitez-vous anonymiser les données avant l'export ?",
                    style = MaterialTheme.typography.body1
                )
                
                Text(
                    "L'anonymisation remplacera :",
                    style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    "• L'identifiant de l'animal par \"anonyme\"\n" +
                    "• Le nom du propriétaire par \"anonyme\"",
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
                        "Anonymiser les données",
                        style = MaterialTheme.typography.body2
                    )
                }

                Divider()

                Text(
                    "Souhaitez-vous chiffrer le JSON ?",
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
                        "Chiffrer le JSON (recommandé)",
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(shouldAnonymize, shouldEncrypt) }
            ) {
                Text("Continuer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
