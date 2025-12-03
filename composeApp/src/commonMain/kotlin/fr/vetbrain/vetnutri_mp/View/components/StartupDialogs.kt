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
                text = "Mise à jour de la base de données",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "La base de données nécessite une mise à jour pour fonctionner correctement.",
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
                            text = "Mise à jour en cours...",
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
                Text("Mettre à jour")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isUpdatingDatabase
            ) {
                Text("Annuler")
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
                text = "Conditions générales d'utilisation",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "⚠️ IMPORTANT : Ces conditions doivent être acceptées à CHAQUE démarrage",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "En utilisant ce logiciel, vous acceptez les conditions suivantes :",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "• Ce logiciel est destiné aux professionnels de santé vétérinaire",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "• Les calculs et recommandations sont fournis à titre informatif",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "• La responsabilité de l'utilisateur reste entière dans l'application",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "• Les données saisies restent confidentielles et locales",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Développé par S. Lefebvre, Dr Vétérinaire, PhD, HDR, Maître de conférence en nutrition à VetAgro Sup.",
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
                Text("J'accepte les conditions")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Fermer")
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
                text = "Mise à jour des données JSON",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Une nouvelle version des données JSON est disponible.",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                currentJsonVersion?.let { current ->
                    Text(
                        text = "Version actuelle : $current",
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                
                newJsonVersion?.let { new ->
                    Text(
                        text = "Nouvelle version : $new",
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
                Text("Mettre à jour")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Plus tard")
            }
        },
        modifier = modifier
    )
}
