package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.RationGroupItem
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Theme.AppSizes

// Onglet Rations : lignes d'information et groupes de rations (plans évolutifs)

// Fonction locale InfoRow pour éviter les problèmes d'import
@Composable
internal fun LocalInfoRow(label: String, value: String) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = AppSizes.paddingXSmall),
                horizontalArrangement = Arrangement.Start
        ) {
                Text(
                        text = "$label :",
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.width(120.dp)
                )
                Text(text = value, style = MaterialTheme.typography.body1)
        }
}

/**
 * Entrées de sélection des périmètres d'analyse groupée (toutes les rations actuelles d'une part,
 * toutes les proposées d'autre part).
 *
 * Les groupes ne sont proposés que lorsque la consultation compte au moins deux rations : en deçà,
 * l'analyse groupée se confondrait avec l'analyse de la ration unique.
 *
 * @param rationsActuelles Rations actuelles de la consultation
 * @param rationsProposees Rations proposées de la consultation
 * @param scopeCourant Périmètre d'analyse actuellement actif
 * @param onSelectGroupe Callback de sélection d'un périmètre groupé
 */
@Composable
internal fun GroupesRationsItems(
        rationsActuelles: List<Ration>,
        rationsProposees: List<Ration>,
        scopeCourant: RationAnalysisScope,
        onSelectGroupe: (RationAnalysisScope) -> Unit
) {
        if (rationsActuelles.size + rationsProposees.size < 2) return

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (rationsActuelles.isNotEmpty()) {
                        RationGroupItem(
                                scope = RationAnalysisScope.GROUPE_ACTUELLES,
                                rations = rationsActuelles,
                                isSelected = scopeCourant == RationAnalysisScope.GROUPE_ACTUELLES,
                                onClick = { onSelectGroupe(RationAnalysisScope.GROUPE_ACTUELLES) }
                        )
                }
                if (rationsProposees.isNotEmpty()) {
                        RationGroupItem(
                                scope = RationAnalysisScope.GROUPE_PROPOSEES,
                                rations = rationsProposees,
                                isSelected = scopeCourant == RationAnalysisScope.GROUPE_PROPOSEES,
                                onClick = { onSelectGroupe(RationAnalysisScope.GROUPE_PROPOSEES) }
                        )
                }
        }
}
