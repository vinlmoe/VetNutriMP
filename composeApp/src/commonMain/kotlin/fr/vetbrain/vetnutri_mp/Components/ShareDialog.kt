package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Service.ShareLink
import fr.vetbrain.vetnutri_mp.Utils.copyToClipboardComposable
import fr.vetbrain.vetnutri_mp.Components.QRCodeView

/**
 * Dialog pour afficher le QR Code avec le BinID de l'export
 */
@Composable
fun ShareLinkDialog(
    shareLink: ShareLink,
    onDismiss: () -> Unit,
    onShare: (() -> Unit)? = null
) {
    var binIdCopied by remember { mutableStateOf(false) }
    var shouldCopy by remember { mutableStateOf(false) }
    
    // Effectuer la copie dans le contexte Composable
    if (shouldCopy) {
        copyToClipboardComposable(shareLink.binId)
        shouldCopy = false
        binIdCopied = true
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Text("Export généré avec succès")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Votre fichier JSON a été uploadé avec succès.",
                    style = MaterialTheme.typography.body2
                )
                
                Text(
                    "ID de l'export (BinID) :",
                    style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Bold
                )
                
                // Champ de texte avec le BinID (sélectionnable)
                SelectionContainer {
                    OutlinedTextField(
                        value = shareLink.binId,
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("BinID") },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            disabledTextColor = MaterialTheme.colors.onSurface,
                            disabledBorderColor = MaterialTheme.colors.primary
                        )
                    )
                }
                
                // QR Code avec le BinID
                Text(
                    "Scannez ce QR Code pour récupérer l'ID de l'export :",
                    style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Bold
                )
                
                QRCodeView(
                    text = shareLink.binId,
                    size = 256,
                    modifier = Modifier.padding(16.dp)
                )
                
                // Informations supplémentaires
                if (shareLink.expiresAt != null) {
                    val expiresIn = (shareLink.expiresAt - kotlinx.datetime.Clock.System.now().toEpochMilliseconds()) / (1000 * 60 * 60)
                    Text(
                        "⚠️ Cet export expirera dans ${expiresIn.toInt()} heures",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.secondary
                    )
                } else {
                    Text(
                        "ℹ️ Cet export ne expire pas automatiquement",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.secondary
                    )
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Bouton copier le BinID
                Button(
                    onClick = {
                        shouldCopy = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (binIdCopied) MaterialTheme.colors.primary.copy(alpha = 0.7f)
                        else MaterialTheme.colors.primary
                    )
                ) {
                    Icon(
                        if (binIdCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (binIdCopied) "Copié !" else "Copier BinID")
                }
                
                // Bouton partager (si onShare est fourni)
                onShare?.let {
                    Button(onClick = it) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Partager")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

