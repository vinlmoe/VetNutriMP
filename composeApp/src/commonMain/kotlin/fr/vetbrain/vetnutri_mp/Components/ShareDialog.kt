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
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate

/**
 * Dialog pour afficher le QR Code avec le BinID de l'export
 */
@Composable
fun ShareLinkDialog(
    shareLink: ShareLink,
    onDismiss: () -> Unit,
    onShare: (() -> Unit)? = null
) {
    var qrDataCopied by remember { mutableStateOf(false) }
    var shouldCopyQrData by remember { mutableStateOf(false) }
    
    // Effectuer la copie dans le contexte Composable
    if (shouldCopyQrData) {
        shareLink.qrCodeData?.let { copyToClipboardComposable(it) }
        shouldCopyQrData = false
        qrDataCopied = true
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Text(translate("shareDialog.exportSuccessTitle"))
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
                    translate("shareDialog.uploadSuccess"),
                    style = MaterialTheme.typography.body2
                )

                // QR Code avec le BinID
                Text(
                    if (shareLink.qrCodeData != null)
                        translate("shareDialog.scanQrEncrypted")
                    else
                        translate("shareDialog.scanQrId"),
                    style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Bold
                )

                if (shareLink.qrCodeData != null && shareLink.qrCodeData.contains("\"key\"")) {
                    Text(
                        translate("shareDialog.decryptionKeyWarning"),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.error
                    )
                }

                QRCodeView(
                    text = shareLink.qrCodeData ?: shareLink.binId,
                    size = 256,
                    modifier = Modifier.padding(16.dp)
                )

                if (shareLink.qrCodeData != null) {
                    Text(
                        translate("shareDialog.qrDataLabel"),
                        style = MaterialTheme.typography.body2,
                        fontWeight = FontWeight.Bold
                    )
                    SelectionContainer {
                        OutlinedTextField(
                            value = shareLink.qrCodeData,
                            onValueChange = { },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(translate("shareDialog.qrJsonFieldLabel")) },
                            maxLines = 3,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                disabledTextColor = MaterialTheme.colors.onSurface,
                                disabledBorderColor = MaterialTheme.colors.primary
                            )
                        )
                    }
                }

                // Informations supplémentaires
                if (shareLink.expiresAt != null) {
                    val expiresIn = (shareLink.expiresAt - kotlinx.datetime.Clock.System.now().toEpochMilliseconds()) / (1000 * 60 * 60)
                    Text(
                        translate("shareDialog.expiresInHours", expiresIn.toInt().toString()),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.secondary
                    )
                } else {
                    Text(
                        translate("shareDialog.noAutoExpiration"),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.secondary
                    )
                }

                // Avertissements liés à l'absence de clé API
                shareLink.warnings.forEach { warning ->
                    Text(
                        translate("shareDialog.warningPrefix", warning),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.error
                    )
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (shareLink.qrCodeData != null) {
                    Button(
                        onClick = {
                            shouldCopyQrData = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (qrDataCopied) MaterialTheme.colors.primary.copy(alpha = 0.7f)
                            else MaterialTheme.colors.primary
                        )
                    ) {
                        Icon(
                            if (qrDataCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (qrDataCopied) translate("shareDialog.copiedConfirmation") else translate("shareDialog.copyQrJson"))
                    }
                }

                // Bouton partager (si onShare est fourni)
                onShare?.let {
                    Button(onClick = it) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(translate("shareDialog.share"))
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(translate(LocalizationKeys.General.CLOSE))
            }
        }
    )
}
