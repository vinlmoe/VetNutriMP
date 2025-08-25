package fr.vetbrain.vetnutri_mp.View.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

/**
 * Exemple d'utilisation de la fonctionnalité d'analyse graphique
 * 
 * Ce composant démontre comment intégrer la nouvelle fonctionnalité d'analyse
 * graphique dans une interface existante.
 */
@Composable
fun FoodSearchComponentExample(
    modifier: Modifier = Modifier
) {
    var showAnalyseView by remember { mutableStateOf(false) }
    var selectedAliments by remember { mutableStateOf<List<AlimentEv>>(emptyList()) }
    
    // Données d'exemple
    val alimentsExemple = remember {
        listOf(
            AlimentEv(
                uuid = "ex1",
                nom = "Croquettes Premium Chien",
                gamme = "Premium",
                brand = "Marque A",
                valMap = mutableMapOf()
            ),
            AlimentEv(
                uuid = "ex2", 
                nom = "Pâtée Chat Senior",
                gamme = "Senior",
                brand = "Marque B",
                valMap = mutableMapOf()
            ),
            AlimentEv(
                uuid = "ex3",
                nom = "Biscuits Éducation",
                gamme = "Éducation", 
                brand = "Marque C",
                valMap = mutableMapOf()
            )
        )
    }

    Column(
        modifier = modifier.padding(AppSizes.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
    ) {
        // En-tête
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = AppSizes.elevationSmall
        ) {
            Column(
                modifier = Modifier.padding(AppSizes.paddingMedium)
            ) {
                Text(
                    text = "Exemple d'Analyse Graphique des Aliments",
                    style = MaterialTheme.typography.h5,
                    color = VetNutriColors.Primary
                )
                Text(
                    text = "Démonstration de la nouvelle fonctionnalité d'analyse graphique",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // Contrôles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Aliments sélectionnés : ${selectedAliments.size}",
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = "Cliquez sur les aliments pour les sélectionner",
                    style = MaterialTheme.typography.caption
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
            ) {
                Button(
                    onClick = { 
                        selectedAliments = alimentsExemple.take(2)
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = VetNutriColors.Secondary
                    )
                ) {
                    Text("Sélectionner 2 aliments")
                }
                
                Button(
                    onClick = { showAnalyseView = true },
                    enabled = selectedAliments.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = VetNutriColors.Primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Analyse graphique",
                        modifier = Modifier.width(16.dp).height(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Voir l'analyse graphique")
                }
            }
        }

        // Liste des aliments sélectionnés
        if (selectedAliments.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = AppSizes.elevationSmall
            ) {
                Column(
                    modifier = Modifier.padding(AppSizes.paddingMedium)
                ) {
                    Text(
                        text = "Aliments sélectionnés pour l'analyse",
                        style = MaterialTheme.typography.h6,
                        color = VetNutriColors.Primary
                    )
                    
                    selectedAliments.forEachIndexed { index, aliment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${index + 1}. ${aliment.nom ?: "Sans nom"}",
                                    style = MaterialTheme.typography.subtitle2
                                )
                                Text(
                                    text = "${aliment.gamme ?: ""} - ${aliment.brand ?: ""}",
                                    style = MaterialTheme.typography.caption
                                )
                            }
                        }
                        
                        if (index < selectedAliments.size - 1) {
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }

        // Affichage de la vue d'analyse si demandée
        if (showAnalyseView) {
            // TODO: Implémenter la vue d'analyse graphique
            // Pour l'instant, affichons un message
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = AppSizes.elevationSmall
            ) {
                Column(
                    modifier = Modifier.padding(AppSizes.paddingMedium)
                ) {
                    Text(
                        text = "Vue d'analyse graphique",
                        style = MaterialTheme.typography.h6,
                        color = VetNutriColors.Primary
                    )
                    Text(
                        text = "Cette fonctionnalité sera bientôt disponible",
                        style = MaterialTheme.typography.body2
                    )
                    Button(
                        onClick = { showAnalyseView = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Fermer")
                    }
                }
            }
        }
    }
} 