package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.selection.SelectionContainer
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.PlatformUrlOpener
import fr.vetbrain.vetnutri_mp.Utils.UpdateChecker
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import kotlinx.coroutines.launch

/**
 * Dialogue pour afficher les informations de mise à jour et proposer le téléchargement
 */
@Composable
fun UpdateDialog(
    updateResult: UpdateChecker.UpdateCheckResult,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = VetNutriColors.Primary
                )
                Text(
                    text = translate("update.app_available_title"),
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    color = VetNutriColors.Primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Informations sur les versions
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 2.dp,
                    backgroundColor = MaterialTheme.colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = translate("update.app_versions_available"),
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Bold,
                            color = VetNutriColors.Secondary
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = translate("update.app_current_version_label"),
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = updateResult.currentVersion,
                                    style = MaterialTheme.typography.body1,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            Column {
                                Text(
                                    text = translate("update.app_new_version_label"),
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = updateResult.newVersion ?: translate("update.app_version_na"),
                                    style = MaterialTheme.typography.body1,
                                    fontWeight = FontWeight.Bold,
                                    color = VetNutriColors.Primary
                                )
                            }
                        }
                    }
                }
                
                // Message d'information
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 1.dp,
                    backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = VetNutriColors.Primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = translate("update.app_info_message"),
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface
                        )
                    }
                }
                
                // Lien de téléchargement
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 2.dp,
                    backgroundColor = VetNutriColors.Primary.copy(alpha = 0.05f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = translate("update.app_download_title"),
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Bold,
                            color = VetNutriColors.Primary
                        )
                        
                        Text(
                            text = translate("update.app_download_instructions"),
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface
                        )
                        
                        // Affichage de l'URL de téléchargement
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = 1.dp,
                            backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.8f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = translate("update.app_download_url_label"),
                                    style = MaterialTheme.typography.caption,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                                SelectionContainer {
                                    Text(
                                        text = updateResult.downloadUrl,
                                        style = MaterialTheme.typography.body2,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        color = VetNutriColors.Primary,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                MaterialTheme.colors.background.copy(alpha = 0.5f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(8.dp)
                                    )
                                }
                            }
                        }
                        
                        Button(
                            onClick = {
                                // Afficher l'URL de téléchargement de manière claire
                                
                                // Essayer d'ouvrir l'URL dans le navigateur
                                try {
                                    PlatformUrlOpener.openUrl(updateResult.downloadUrl)
                                } catch (e: Exception) {
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = VetNutriColors.Primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = translate("update.app_open_download_page"),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = VetNutriColors.Secondary
                )
            ) {
                Text(translate(LocalizationKeys.General.CLOSE))
            }
        },
        modifier = modifier
    )
}

/**
 * Dialogue d'erreur pour les problèmes de vérification de mise à jour
 */
@Composable
fun UpdateErrorDialog(
    errorMessage: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = translate("update.app_check_error_title"),
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.error
            )
        },
        text = {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.body2
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.error
                )
            ) {
                Text(translate(LocalizationKeys.General.CLOSE))
            }
        },
        modifier = modifier
    )
}
