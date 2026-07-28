package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.analyserCompositionAliment
import fr.vetbrain.vetnutri_mp.Data.grouperNutrimentsParCategorie
import fr.vetbrain.vetnutri_mp.Data.obtenirTitreCategorie
import fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.NumberUtils

private enum class CompositionMode { PER_100G, PER_1000_KCAL }

// Même ordre que HtmlDocumentBuilder.kt::buildNutrientTableBlock, pour rester cohérent avec
// l'export PDF.
private val ORDRE_CATEGORIES =
    listOf("BASE", "MACRO", "MIN", "VITAM", "LIPID", "AMA", "ANA", "OTHER", "ENERGY")

/**
 * Dialogue affichant la composition complète d'un aliment (pour 100g ou pour 1000 kcal) ainsi que
 * sa bibliographie. Déclenché depuis la loupe de `AlimentItem`.
 */
@Composable
fun AlimentDetailDialog(aliment: AlimentEv, onDismiss: () -> Unit) {
    var mode by remember { mutableStateOf(CompositionMode.PER_100G) }

    val valeurs = remember(aliment) { analyserCompositionAliment(aliment) }
    val groupes = remember(valeurs) { grouperNutrimentsParCategorie(valeurs) }
    val facteur1000Kcal = remember(valeurs) {
        valeurs[NutrientMain.ENERGIE.label]?.valeur?.takeIf { it > 0.0 }?.let { 1000.0 / it }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = aliment.nom ?: translate("alimentItem.noNameLabel"),
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold
                        )
                        if (!aliment.brand.isNullOrBlank()) {
                            Text(
                                text = aliment.brand!!,
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = translate("general.close")
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModeButton(
                        label = translate("alimentItem.detail.per100g"),
                        selected = mode == CompositionMode.PER_100G,
                        onClick = { mode = CompositionMode.PER_100G },
                        modifier = Modifier.weight(1f)
                    )
                    ModeButton(
                        label = translate("alimentItem.detail.per1000kcal"),
                        selected = mode == CompositionMode.PER_1000_KCAL,
                        enabled = facteur1000Kcal != null,
                        onClick = { mode = CompositionMode.PER_1000_KCAL },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (facteur1000Kcal == null) {
                    Text(
                        text = translate("alimentItem.detail.noEnergy"),
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                    )
                }

                Divider()

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (valeurs.isEmpty()) {
                        item {
                            Text(
                                text = translate("alimentItem.detail.noComposition"),
                                style = MaterialTheme.typography.body2,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        ORDRE_CATEGORIES.forEach { categorie ->
                            val nutriments = groupes[categorie]
                            if (!nutriments.isNullOrEmpty()) {
                                item(key = "cat-$categorie") {
                                    Text(
                                        text = obtenirTitreCategorie(categorie),
                                        style = MaterialTheme.typography.subtitle2,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Primary,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                                items(nutriments, key = { (nom, _) -> "$categorie-$nom" }) { (_, valeur) ->
                                    val isRatio = valeur.nutriment is NutrientAnalysis
                                    val valeurAffichee =
                                        if (mode == CompositionMode.PER_1000_KCAL && !isRatio && facteur1000Kcal != null) {
                                            valeur.valeur * facteur1000Kcal
                                        } else {
                                            valeur.valeur
                                        }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = valeur.nutriment.translateEnum(),
                                            style = MaterialTheme.typography.body2
                                        )
                                        Text(
                                            text = "${NumberUtils.format(valeurAffichee, 2)} ${valeur.unite.displayName}",
                                            style = MaterialTheme.typography.body2,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = translate("alimentItem.detail.bibliography"),
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold,
                            color = VetNutriColors.Primary
                        )
                    }

                    if (aliment.biblioRefs.isEmpty()) {
                        item {
                            Text(
                                text = translate("food_edit.biblio.empty"),
                                style = MaterialTheme.typography.body2,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(aliment.biblioRefs, key = { it.uuid }) { ref ->
                            BiblioRefReadOnlyCard(ref)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    if (selected) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = VetNutriColors.Primary,
                contentColor = VetNutriColors.OnPrimary
            )
        ) {
            Text(label, style = MaterialTheme.typography.caption)
        }
    } else {
        OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier) {
            Text(label, style = MaterialTheme.typography.caption)
        }
    }
}

@Composable
private fun BiblioRefReadOnlyCard(ref: BiblioRef) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text("${ref.firstAuthor} (${ref.year})", style = MaterialTheme.typography.subtitle2)
            if (ref.completeRef.isNotBlank()) {
                Text(ref.completeRef, style = MaterialTheme.typography.caption, color = Color.Gray)
            }
            if (ref.comments.isNotBlank()) {
                Text(ref.comments, style = MaterialTheme.typography.caption, color = Color.Gray)
            }
        }
    }
}
