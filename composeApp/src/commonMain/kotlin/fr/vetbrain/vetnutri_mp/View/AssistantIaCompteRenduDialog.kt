package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.vetbrain.vetnutri_mp.Export.CompteRenduIa
import fr.vetbrain.vetnutri_mp.Export.ReponseIaCompteRendu
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.AnimalDetail
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

private val FormeChamp = RoundedCornerShape(8.dp)

/**
 * Rédaction du compte rendu avec une IA externe, sans appel réseau : 1) copier le prompt
 * anonymisé, 2) coller la réponse JSON de l'IA, appliquée aux textes du compte rendu.
 */
@Composable
fun AssistantIaCompteRenduDialog(
        prompt: String,
        onAppliquer: (ReponseIaCompteRendu) -> Unit,
        onDismiss: () -> Unit
) {
    val presse = LocalClipboardManager.current
    var promptCopie by remember { mutableStateOf(false) }
    var reponse by remember { mutableStateOf("") }
    var reponseInvalide by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth().heightIn(max = 720.dp)
        ) {
            Column(
                    modifier = Modifier.padding(AppSizes.paddingMedium).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                            translate(AnimalDetail.AI_ASSISTANT_TITLE),
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = translate(General.CLOSE))
                    }
                }

                Text(translate(AnimalDetail.AI_ASSISTANT_STEP_PROMPT), style = MaterialTheme.typography.body2)
                OutlinedTextField(
                        value = prompt,
                        onValueChange = {},
                        readOnly = true,
                        textStyle = MaterialTheme.typography.caption.copy(fontFamily = FontFamily.Monospace),
                        maxLines = 10,
                        shape = FormeChamp,
                        modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                            onClick = {
                                presse.setText(AnnotatedString(prompt))
                                promptCopie = true
                            },
                            shape = FormeChamp
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(translate(AnimalDetail.AI_ASSISTANT_COPY_PROMPT))
                    }
                    if (promptCopie) {
                        Spacer(Modifier.width(AppSizes.paddingSmall))
                        Text(
                                translate(AnimalDetail.AI_ASSISTANT_PROMPT_COPIED),
                                style = MaterialTheme.typography.caption,
                                color = VetNutriColors.Primary
                        )
                    }
                }

                Spacer(Modifier.height(AppSizes.paddingSmall))
                Text(translate(AnimalDetail.AI_ASSISTANT_STEP_RESPONSE), style = MaterialTheme.typography.body2)
                OutlinedTextField(
                        value = reponse,
                        onValueChange = {
                            reponse = it
                            reponseInvalide = false
                        },
                        label = { Text(translate(AnimalDetail.AI_ASSISTANT_RESPONSE_LABEL)) },
                        isError = reponseInvalide,
                        minLines = 4,
                        maxLines = 10,
                        shape = FormeChamp,
                        modifier = Modifier.fillMaxWidth()
                )
                if (reponseInvalide) {
                    Text(
                            translate(AnimalDetail.AI_ASSISTANT_INVALID_RESPONSE),
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.error
                    )
                }
                Text(
                        translate(AnimalDetail.AI_ASSISTANT_REPLACE_WARNING),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) { Text(translate(General.CANCEL)) }
                    Button(
                            onClick = {
                                val lue = CompteRenduIa.lireReponse(reponse)
                                if (lue == null) {
                                    reponseInvalide = true
                                } else {
                                    onAppliquer(lue)
                                    onDismiss()
                                }
                            },
                            enabled = reponse.isNotBlank(),
                            shape = FormeChamp,
                            colors =
                                    ButtonDefaults.buttonColors(
                                            backgroundColor = VetNutriColors.Primary,
                                            contentColor = VetNutriColors.OnPrimary
                                    )
                    ) { Text(translate(AnimalDetail.AI_ASSISTANT_APPLY)) }
                }
            }
        }
    }
}
