package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Composant de carte générique pour les sélections multiples.
 *
 * @param T Type d'élément à sélectionner
 * @param titre Titre affiché en haut de la carte
 * @param elementsDisponibles Liste des éléments disponibles pour la sélection
 * @param elementsSelectionnes Liste mutable des éléments actuellement sélectionnés
 * @param onSelectionChange Callback appelé lorsque la sélection change
 * @param getLabel Fonction pour obtenir le libellé à afficher pour chaque élément
 * @param getIdentifiant Fonction pour obtenir l'identifiant unique de chaque élément
 * @param couleurArrierePlan Couleur d'arrière-plan des badges des éléments sélectionnés
 * @param modifier Modificateur Compose optionnel
 */
@Composable
fun <T> MultiSelectionCard(
        titre: String,
        elementsDisponibles: List<T>,
        elementsSelectionnes: MutableList<T>,
        onSelectionChange: (List<T>) -> Unit,
        getLabel: (T) -> String,
        getIdentifiant: (T) -> String,
        couleurArrierePlan: Color,
        modifier: Modifier = Modifier
) {
    var afficherDialogue by remember { mutableStateOf(false) }
    val elementsTriés by
            remember(elementsDisponibles) {
                mutableStateOf(elementsDisponibles.sortedBy { getLabel(it) })
            }

    Card(modifier = modifier.fillMaxWidth(), elevation = 4.dp) {
        Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = titre, style = MaterialTheme.typography.h6)

            OutlinedButton(
                    onClick = { afficherDialogue = true },
                    modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            if (elementsSelectionnes.isEmpty()) "Sélectionner..."
                            else "${elementsSelectionnes.size} élément(s) sélectionné(s)"
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            if (elementsSelectionnes.isNotEmpty()) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    elementsSelectionnes.take(5).forEach { element ->
                        Surface(
                                color = couleurArrierePlan.copy(alpha = 0.2f),
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = getLabel(element))
                                Text(
                                        text = "(${getIdentifiant(element)})",
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Gray
                                )
                            }
                        }
                    }
                    if (elementsSelectionnes.size > 5) {
                        Text("+ ${elementsSelectionnes.size - 5} autres")
                    }
                }
            }
        }
    }

    if (afficherDialogue) {
        Dialog(onDismissRequest = { afficherDialogue = false }) {
            Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colors.surface,
                    elevation = 8.dp,
                    modifier = Modifier.fillMaxWidth(0.9f).heightIn(max = 450.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                            text = titre,
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Column(
                                modifier =
                                        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        ) {
                            elementsTriés.forEach { element ->
                                key(getIdentifiant(element)) {
                                    Row(
                                            modifier =
                                                    Modifier.fillMaxWidth()
                                                            .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                                checked = elementsSelectionnes.contains(element),
                                                onCheckedChange = { checked ->
                                                    val nouvelleListe =
                                                            elementsSelectionnes.toMutableList()
                                                    if (checked) {
                                                        if (!nouvelleListe.contains(element)) {
                                                            nouvelleListe.add(element)
                                                        }
                                                    } else {
                                                        nouvelleListe.remove(element)
                                                    }
                                                    onSelectionChange(nouvelleListe)
                                                }
                                        )
                                        Column(modifier = Modifier.padding(start = 8.dp)) {
                                            Text(text = getLabel(element))
                                            Text(
                                                    text = "(${getIdentifiant(element)})",
                                                    style = MaterialTheme.typography.caption,
                                                    color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Button(
                            onClick = { afficherDialogue = false },
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) { Text("Fermer") }
                }
            }
        }
    }
}
