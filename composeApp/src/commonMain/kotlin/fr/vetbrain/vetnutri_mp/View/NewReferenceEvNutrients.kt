package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.UnitEnum
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.NewReferenceEvViewModel
import fr.vetbrain.vetnutri_mp.Utils.isIosPlatform

// Éditeur de référentiel : liste, cartes et édition des nutriments (valeurs par niveau)

/** Vue pour afficher une liste de nutriments avec leurs valeurs actuelles */
@Composable
fun <T : Nutrient> NutrientListView(
        nutrients: List<T>,
        currentReference: ReferenceEv,
        biblioRefs: List<BiblioRef>,
        onNutrientSelected: (Nutrient) -> Unit,
        viewModel: NewReferenceEvViewModel
) {
        LazyVerticalGrid(
                columns =
                        GridCells.Adaptive(
                                minSize = 280.dp
                        ), // Adaptation automatique avec largeur minimale de 280dp
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(4.dp)
        ) {
                items(nutrients.size) { index ->
                        val nutrient = nutrients[index]
                        NutrientCard(
                                nutrient = nutrient,
                                currentReference = currentReference,
                                onNutrientSelected = onNutrientSelected,
                                viewModel = viewModel
                        )
                }
        }
}

/**
 * Carte affichant les informations d'un nutriment et ses valeurs
 *
 * @param nutrient Le nutriment à afficher
 * @param currentReference La référence actuelle contenant les valeurs du nutriment
 * @param onNutrientSelected Callback appelé lorsque le nutriment est sélectionné pour édition
 * @param viewModel ViewModel pour écouter les mises à jour forcées
 */
@Composable
fun <T : Nutrient> NutrientCard(
        nutrient: T,
        currentReference: ReferenceEv,
        onNutrientSelected: (Nutrient) -> Unit,
        viewModel: NewReferenceEvViewModel
) {
        // Utiliser des variables mutableStateOf pour la réactivité immédiate
        var hasMin by remember { mutableStateOf(false) }
        var hasMax by remember { mutableStateOf(false) }
        var hasOptMin by remember { mutableStateOf(false) }
        var hasOptMax by remember { mutableStateOf(false) }

        var minValue by remember { mutableStateOf(0.0) }
        var maxValue by remember { mutableStateOf(0.0) }
        var optMinValue by remember { mutableStateOf(0.0) }
        var optMaxValue by remember { mutableStateOf(0.0) }

        var minUnit by remember {
                mutableStateOf<fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum?>(null)
        }
        var maxUnit by remember {
                mutableStateOf<fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum?>(null)
        }
        var optMinUnit by remember {
                mutableStateOf<fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum?>(null)
        }
        var optMaxUnit by remember {
                mutableStateOf<fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum?>(null)
        }

        var minUnitEnum by remember { mutableStateOf<UnitEnum?>(null) }
        var maxUnitEnum by remember { mutableStateOf<UnitEnum?>(null) }
        var optMinUnitEnum by remember { mutableStateOf<UnitEnum?>(null) }
        var optMaxUnitEnum by remember { mutableStateOf<UnitEnum?>(null) }

        var minBiblio by remember { mutableStateOf<fr.vetbrain.vetnutri_mp.Data.BiblioRef?>(null) }
        var maxBiblio by remember { mutableStateOf<fr.vetbrain.vetnutri_mp.Data.BiblioRef?>(null) }
        var optMinBiblio by remember {
                mutableStateOf<fr.vetbrain.vetnutri_mp.Data.BiblioRef?>(null)
        }
        var optMaxBiblio by remember {
                mutableStateOf<fr.vetbrain.vetnutri_mp.Data.BiblioRef?>(null)
        }

        // Observer le forceUpdate pour déclencher les mises à jour
        val forceUpdate by viewModel.forceUpdate.collectAsState()

        // Vérifier si le nutriment appartient à la famille 5 (pas d'unité de besoin)
        val shouldShowUnitReq = nutrient.ue.getIDFamily() != 5

        // Effet pour mettre à jour les valeurs quand la référence change OU quand forceUpdate
        // change
        LaunchedEffect(currentReference, forceUpdate) {
                // Vérifier si des valeurs sont définies pour ce nutriment
                hasMin =
                        currentReference.contientNutriment(
                                nutrient,
                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                        )
                hasMax =
                        currentReference.contientNutriment(
                                nutrient,
                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                        )
                hasOptMin =
                        currentReference.contientNutriment(
                                nutrient,
                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                        )
                hasOptMax =
                        currentReference.contientNutriment(
                                nutrient,
                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                        )

                // Récupérer les valeurs si elles existent
                minValue =
                        if (hasMin)
                                currentReference.obtenirNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                )
                        else 0.0

                maxValue =
                        if (hasMax)
                                currentReference.obtenirNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                )
                        else 0.0

                optMinValue =
                        if (hasOptMin)
                                currentReference.obtenirNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                                )
                        else 0.0

                optMaxValue =
                        if (hasOptMax)
                                currentReference.obtenirNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                                )
                        else 0.0

                // Récupérer les unités si elles existent
                minUnit =
                        if (hasMin)
                                fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.getById(
                                        currentReference.obtenirUniteNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                        )
                                )
                        else null

                maxUnit =
                        if (hasMax)
                                fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.getById(
                                        currentReference.obtenirUniteNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                        )
                                )
                        else null

                optMinUnit =
                        if (hasOptMin)
                                fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.getById(
                                        currentReference.obtenirUniteNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                                        )
                                )
                        else null

                optMaxUnit =
                        if (hasOptMax)
                                fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.getById(
                                        currentReference.obtenirUniteNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                                        )
                                )
                        else null

                // Récupérer les UnitEnum si elles existent
                minUnitEnum =
                        if (hasMin)
                                currentReference.obtenirUnitEnumNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                )
                        else null

                maxUnitEnum =
                        if (hasMax)
                                currentReference.obtenirUnitEnumNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                )
                        else null

                optMinUnitEnum =
                        if (hasOptMin)
                                currentReference.obtenirUnitEnumNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                                )
                        else null

                optMaxUnitEnum =
                        if (hasOptMax)
                                currentReference.obtenirUnitEnumNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                                )
                        else null

                // Récupérer les bibliographies si elles existent
                minBiblio =
                        if (hasMin)
                                currentReference.obtenirBiblioNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                )
                        else null

                maxBiblio =
                        if (hasMax)
                                currentReference.obtenirBiblioNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                )
                        else null

                optMinBiblio =
                        if (hasOptMin)
                                currentReference.obtenirBiblioNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                                )
                        else null

                optMaxBiblio =
                        if (hasOptMax)
                                currentReference.obtenirBiblioNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                                )
                        else null
        }

        Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp,
                backgroundColor = MaterialTheme.colors.surface,
                shape = RoundedCornerShape(12.dp)
        ) {
                Column(modifier = Modifier.padding(12.dp)) {
                        // En-tête avec le nom du nutriment et bouton d'édition
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text = nutrient.label,
                                        style = MaterialTheme.typography.subtitle2,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Primary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                        onClick = { onNutrientSelected(nutrient) },
                                        modifier = Modifier.size(32.dp)
                                ) {
                                        Icon(
                                                Icons.Default.Edit,
                                                contentDescription = translate(LocalizationKeys.General.EDIT),
                                                tint = VetNutriColors.Primary,
                                                modifier = Modifier.size(18.dp)
                                        )
                                }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Afficher les valeurs avec leurs unités
                        if (hasMin || hasMax || hasOptMin || hasOptMax) {
                                // Si au moins une valeur est définie
                                Column(modifier = Modifier.fillMaxWidth()) {
                                        if (hasMin) {
                                                CompactNutrientValueRow(
                                                        label = translate("new_reference.level.min"),
                                                        value = minValue,
                                                        unit = minUnit?.label ?: "",
                                                        unitEnum = minUnitEnum?.displayName ?: "",
                                                        color = MaterialTheme.colors.primary,
                                                        shouldShowUnitReq = shouldShowUnitReq,
                                                        biblio = minBiblio?.toString() ?: ""
                                                )
                                        }
                                        if (hasOptMin) {
                                                CompactNutrientValueRow(
                                                        label = translate("new_reference.level.opt_min_compact"),
                                                        value = optMinValue,
                                                        unit = optMinUnit?.label ?: "",
                                                        unitEnum = optMinUnitEnum?.displayName
                                                                        ?: "",
                                                        color =
                                                                Color(
                                                                        0xFF4CAF50
                                                                ), // Vert pour optimum,
                                                        shouldShowUnitReq = shouldShowUnitReq,
                                                        biblio = optMinBiblio?.toString() ?: ""
                                                )
                                        }
                                        if (hasOptMax) {
                                                CompactNutrientValueRow(
                                                        label = translate("new_reference.level.opt_max_compact"),
                                                        value = optMaxValue,
                                                        unit = optMaxUnit?.label ?: "",
                                                        unitEnum = optMaxUnitEnum?.displayName
                                                                        ?: "",
                                                        color =
                                                                Color(
                                                                        0xFF4CAF50
                                                                ), // Vert pour optimum,
                                                        shouldShowUnitReq = shouldShowUnitReq,
                                                        biblio = optMaxBiblio?.toString() ?: ""
                                                )
                                        }
                                        if (hasMax) {
                                                CompactNutrientValueRow(
                                                        label = translate("new_reference.level.max"),
                                                        value = maxValue,
                                                        unit = maxUnit?.label ?: "",
                                                        unitEnum = maxUnitEnum?.displayName ?: "",
                                                        color = MaterialTheme.colors.secondary,
                                                        shouldShowUnitReq = shouldShowUnitReq,
                                                        biblio = maxBiblio?.toString() ?: ""
                                                )
                                        }
                                }
                        } else {
                                // Si aucune valeur n'est définie
                                Box(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                ) {
                                        Text(
                                                translate("new_reference.not_defined"),
                                                style = MaterialTheme.typography.caption,
                                                color =
                                                        MaterialTheme.colors.onSurface.copy(
                                                                alpha = 0.5f
                                                        )
                                        )
                                }
                        }
                }
        }
}

/** Ligne compacte affichant une valeur nutritionnelle pour les cartes */
@Composable
fun CompactNutrientValueRow(
        label: String,
        value: Double,
        unit: String,
        unitEnum: String,
        biblio: String,
        color: Color,
        shouldShowUnitReq: Boolean = true
) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                // Première ligne : Label et valeur avec unités
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        // Étiquette du niveau avec couleur
                        Text(
                                text = label,
                                style = MaterialTheme.typography.caption,
                                fontWeight = FontWeight.Bold,
                                color = color,
                                modifier = Modifier.width(35.dp)
                        )

                        // Valeur avec unités
                        val displayText =
                                if (shouldShowUnitReq) "$value $unitEnum $unit"
                                else "$value $unitEnum"
                        Text(
                                text = displayText,
                                style = MaterialTheme.typography.caption,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                        )
                }

                // Deuxième ligne : Référence bibliographique (si disponible)
                if (biblio.isNotEmpty()) {
                        Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 1.dp),
                                horizontalArrangement = Arrangement.End
                        ) {
                                Text(
                                        text = translate("reference.ref", biblio.toString()),
                                        style =
                                                MaterialTheme.typography.caption.copy(
                                                        fontSize =
                                                                MaterialTheme.typography
                                                                        .caption
                                                                        .fontSize * 0.85f
                                                ),
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                )
                        }
                }
        }
}

/** Ligne affichant une valeur nutritionnelle avec son unité et sa référence bibliographique */
@Composable
fun NutrientValueRow(
        label: String,
        value: Double,
        unit: String,
        unitEnum: String,
        biblio: String,
        color: Color,
        shouldShowUnitReq: Boolean = true
) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
                // Étiquette du niveau
                Text(
                        text = label,
                        style = MaterialTheme.typography.body2,
                        fontWeight = FontWeight.Medium,
                        color = color
                )

                // Valeur avec unité physique et optionnellement unité de besoin
                val displayText =
                        if (shouldShowUnitReq) "$value $unitEnum $unit" else "$value $unitEnum"
                Text(
                        text = displayText,
                        style = MaterialTheme.typography.body2,
                        fontWeight = FontWeight.Bold
                )

                // Référence bibliographique (truncate si trop longue)
                val shortBiblio = if (biblio.length > 15) biblio.take(12) + "..." else biblio
                Text(
                        text = shortBiblio,
                        style = MaterialTheme.typography.caption,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(80.dp)
                )
        }
}

/** Retourne le nom d'affichage d'un nutriment. */
private fun getNutrientDisplayNameLocal(nutrient: Nutrient): String {
        return nutrient.label
}

/**
 * Boîte de dialogue pour éditer les valeurs d'un nutriment
 *
 * @param nutrient Le nutriment à éditer
 * @param currentReference La référence en cours d'édition
 * @param biblioRefs Liste des références bibliographiques disponibles
 * @param viewModel ViewModel pour les opérations sur les nutriments
 * @param onDismiss Callback appelé lorsque la boîte de dialogue est fermée
 */
@Composable
fun NutrientEditDialog(
        nutrient: Nutrient,
        currentReference: ReferenceEv,
        biblioRefs: List<BiblioRef>,
        viewModel: NewReferenceEvViewModel,
        onDismiss: () -> Unit
) {
        // États pour les valeurs
        var minValue by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                )
                        )
                                currentReference
                                        .obtenirNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                        )
                                        .toString()
                        else ""
                )
        }
        var maxValue by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                )
                        )
                                currentReference
                                        .obtenirNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                        )
                                        .toString()
                        else ""
                )
        }
        var optMinValue by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                                )
                        )
                                currentReference
                                        .obtenirNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                                        )
                                        .toString()
                        else ""
                )
        }
        var optMaxValue by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                                )
                        )
                                currentReference
                                        .obtenirNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                                        )
                                        .toString()
                        else ""
                )
        }

        // États pour les unités UnitReqEnum
        val unitOptions = fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.values()

        var selectedUnitMin by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                )
                        )
                                fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.getById(
                                        currentReference.obtenirUniteNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                        )
                                )
                        else fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.PERKG
                )
        }

        var selectedUnitMax by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                )
                        )
                                fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.getById(
                                        currentReference.obtenirUniteNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                        )
                                )
                        else fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.PERKG
                )
        }

        var selectedUnitOptMin by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                                )
                        )
                                fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.getById(
                                        currentReference.obtenirUniteNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                                        )
                                )
                        else fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.PERKG
                )
        }

        var selectedUnitOptMax by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                                )
                        )
                                fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.getById(
                                        currentReference.obtenirUniteNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                                        )
                                )
                        else fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.PERKG
                )
        }

        // États pour les UnitEnum avec filtrage par idFamily
        val defaultUnitEnum = nutrient.ue
        val availableUnitEnums =
                UnitEnum.values().filter { it.getIDFamily() == defaultUnitEnum.getIDFamily() }

        // Vérifier si le nutriment appartient à la famille 5 (pas d'unité de besoin)
        val shouldShowUnitReq = defaultUnitEnum.getIDFamily() != 5

        var selectedUnitEnumMin by remember { mutableStateOf(defaultUnitEnum) }
        var selectedUnitEnumMax by remember { mutableStateOf(defaultUnitEnum) }
        var selectedUnitEnumOptMin by remember { mutableStateOf(defaultUnitEnum) }
        var selectedUnitEnumOptMax by remember { mutableStateOf(defaultUnitEnum) }

        // États pour les références bibliographiques
        var selectedBiblioMin by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                )
                        )
                                currentReference.obtenirBiblioNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                )
                        else if (biblioRefs.isNotEmpty()) biblioRefs[0] else BiblioRef()
                )
        }

        var selectedBiblioMax by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                )
                        )
                                currentReference.obtenirBiblioNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                )
                        else if (biblioRefs.isNotEmpty()) biblioRefs[0] else BiblioRef()
                )
        }

        var selectedBiblioOptMin by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                                )
                        )
                                currentReference.obtenirBiblioNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                                )
                        else if (biblioRefs.isNotEmpty()) biblioRefs[0] else BiblioRef()
                )
        }

        var selectedBiblioOptMax by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                                )
                        )
                                currentReference.obtenirBiblioNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                                )
                        else if (biblioRefs.isNotEmpty()) biblioRefs[0] else BiblioRef()
                )
        }

        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(text = translate("reference.editionNutriment", getNutrientDisplayNameLocal(nutrient))) },
                text = {
                        Column(
                                modifier = Modifier.width(800.dp).height(400.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                                // En-tête avec info sur les unités
                                Text(
                                        text =
                                                "Unités physiques disponibles: ${defaultUnitEnum.getIDFamily()}",
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.primary
                                )

                                // Layout en grille pour les 4 niveaux
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Minimum
                                        NutrientLevelRow(
                                                label = translate("new_reference.level.min"),
                                                value = minValue,
                                                onValueChange = { minValue = it },
                                                selectedUnitEnum = selectedUnitEnumMin,
                                                onUnitEnumChange = { selectedUnitEnumMin = it },
                                                selectedUnitReq = selectedUnitMin,
                                                onUnitReqChange = { selectedUnitMin = it },
                                                selectedBiblio = selectedBiblioMin,
                                                onBiblioChange = { selectedBiblioMin = it },
                                                availableUnitEnums = availableUnitEnums,
                                                unitReqOptions = unitOptions,
                                                biblioRefs = biblioRefs,
                                                shouldShowUnitReq = shouldShowUnitReq
                                        )

                                        // Maximum
                                        NutrientLevelRow(
                                                label = translate("new_reference.level.max"),
                                                value = maxValue,
                                                onValueChange = { maxValue = it },
                                                selectedUnitEnum = selectedUnitEnumMax,
                                                onUnitEnumChange = { selectedUnitEnumMax = it },
                                                selectedUnitReq = selectedUnitMax,
                                                onUnitReqChange = { selectedUnitMax = it },
                                                selectedBiblio = selectedBiblioMax,
                                                onBiblioChange = { selectedBiblioMax = it },
                                                availableUnitEnums = availableUnitEnums,
                                                unitReqOptions = unitOptions,
                                                biblioRefs = biblioRefs,
                                                shouldShowUnitReq = shouldShowUnitReq
                                        )

                                        // Optimum Min
                                        NutrientLevelRow(
                                                label = translate("auto.view.newreferenceeveditview.opt_min"),
                                                value = optMinValue,
                                                onValueChange = { optMinValue = it },
                                                selectedUnitEnum = selectedUnitEnumOptMin,
                                                onUnitEnumChange = { selectedUnitEnumOptMin = it },
                                                selectedUnitReq = selectedUnitOptMin,
                                                onUnitReqChange = { selectedUnitOptMin = it },
                                                selectedBiblio = selectedBiblioOptMin,
                                                onBiblioChange = { selectedBiblioOptMin = it },
                                                availableUnitEnums = availableUnitEnums,
                                                unitReqOptions = unitOptions,
                                                biblioRefs = biblioRefs,
                                                shouldShowUnitReq = shouldShowUnitReq
                                        )

                                        // Optimum Max
                                        NutrientLevelRow(
                                                label = translate(LocalizationKeys.RefLevels.OPTIMAX),
                                                value = optMaxValue,
                                                onValueChange = { optMaxValue = it },
                                                selectedUnitEnum = selectedUnitEnumOptMax,
                                                onUnitEnumChange = { selectedUnitEnumOptMax = it },
                                                selectedUnitReq = selectedUnitOptMax,
                                                onUnitReqChange = { selectedUnitOptMax = it },
                                                selectedBiblio = selectedBiblioOptMax,
                                                onBiblioChange = { selectedBiblioOptMax = it },
                                                availableUnitEnums = availableUnitEnums,
                                                unitReqOptions = unitOptions,
                                                biblioRefs = biblioRefs,
                                                shouldShowUnitReq = shouldShowUnitReq
                                        )
                                }
                        }
                },
                confirmButton = {
                        TextButton(
                                onClick = {
                                        // Traiter les valeurs pour chaque niveau
                                        // Si la valeur est vide ou null, supprimer la référence
                                        // Sinon, sauvegarder la nouvelle valeur

                                        // Minimum
                                        if (minValue.isBlank()) {
                                                viewModel.removeNutrientValue(
                                                        nutrient,
                                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                                )
                                        } else {
                                                val minDouble = minValue.toDoubleOrNull()
                                                if (minDouble != null && minDouble >= 0) {
                                                        viewModel.updateNutrientValue(
                                                                nutrient = nutrient,
                                                                value = minDouble,
                                                                level =
                                                                        fr.vetbrain.vetnutri_mp
                                                                                .Enumer.Reflevel
                                                                                .MIN,
                                                                unit = selectedUnitMin,
                                                                biblioRef = selectedBiblioMin,
                                                                unitEnum = selectedUnitEnumMin
                                                        )
                                                }
                                        }

                                        // Maximum
                                        if (maxValue.isBlank()) {
                                                viewModel.removeNutrientValue(
                                                        nutrient,
                                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                                )
                                        } else {
                                                val maxDouble = maxValue.toDoubleOrNull()
                                                if (maxDouble != null && maxDouble >= 0) {
                                                        viewModel.updateNutrientValue(
                                                                nutrient = nutrient,
                                                                value = maxDouble,
                                                                level =
                                                                        fr.vetbrain.vetnutri_mp
                                                                                .Enumer.Reflevel
                                                                                .MAX,
                                                                unit = selectedUnitMax,
                                                                biblioRef = selectedBiblioMax,
                                                                unitEnum = selectedUnitEnumMax
                                                        )
                                                }
                                        }

                                        // Optimum Min
                                        if (optMinValue.isBlank()) {
                                                viewModel.removeNutrientValue(
                                                        nutrient,
                                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel
                                                                .OPTIMIN
                                                )
                                        } else {
                                                val optMinDouble = optMinValue.toDoubleOrNull()
                                                if (optMinDouble != null && optMinDouble >= 0) {
                                                        viewModel.updateNutrientValue(
                                                                nutrient = nutrient,
                                                                value = optMinDouble,
                                                                level =
                                                                        fr.vetbrain.vetnutri_mp
                                                                                .Enumer.Reflevel
                                                                                .OPTIMIN,
                                                                unit = selectedUnitOptMin,
                                                                biblioRef = selectedBiblioOptMin,
                                                                unitEnum = selectedUnitEnumOptMin
                                                        )
                                                }
                                        }

                                        // Optimum Max
                                        if (optMaxValue.isBlank()) {
                                                viewModel.removeNutrientValue(
                                                        nutrient,
                                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel
                                                                .OPTIMAX
                                                )
                                        } else {
                                                val optMaxDouble = optMaxValue.toDoubleOrNull()
                                                if (optMaxDouble != null && optMaxDouble >= 0) {
                                                        viewModel.updateNutrientValue(
                                                                nutrient = nutrient,
                                                                value = optMaxDouble,
                                                                level =
                                                                        fr.vetbrain.vetnutri_mp
                                                                                .Enumer.Reflevel
                                                                                .OPTIMAX,
                                                                unit = selectedUnitOptMax,
                                                                biblioRef = selectedBiblioOptMax,
                                                                unitEnum = selectedUnitEnumOptMax
                                                        )
                                                }
                                        }

                                        onDismiss()
                                }
                        ) { Text(translate(LocalizationKeys.General.SAVE)) }
                },
                dismissButton = { TextButton(onClick = onDismiss) { Text(translate(LocalizationKeys.General.CANCEL)) } }
        )
}

/** Composant pour une ligne compacte d'édition d'un niveau de nutriment */
@Composable
private fun NutrientLevelRow(
        label: String,
        value: String,
        onValueChange: (String) -> Unit,
        selectedUnitEnum: UnitEnum,
        onUnitEnumChange: (UnitEnum) -> Unit,
        selectedUnitReq: fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum,
        onUnitReqChange: (fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum) -> Unit,
        selectedBiblio: BiblioRef,
        onBiblioChange: (BiblioRef) -> Unit,
        availableUnitEnums: List<UnitEnum>,
        unitReqOptions: Array<fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum>,
        biblioRefs: List<BiblioRef>,
        shouldShowUnitReq: Boolean
) {
        var unitEnumExpanded by remember { mutableStateOf(false) }
        var unitReqExpanded by remember { mutableStateOf(false) }
        var biblioExpanded by remember { mutableStateOf(false) }

        Card(elevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        // Label
                        Text(
                                text = label,
                                style = MaterialTheme.typography.body2,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(60.dp)
                        )

                        // Champ valeur
                        OutlinedTextField(
                                value = value,
                                onValueChange = onValueChange,
                                keyboardOptions =
                                        KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.width(80.dp),
                                singleLine = true
                        )

                        // UnitEnum
                        Box(modifier = Modifier.width(80.dp)) {
                                OutlinedButton(
                                        onClick = { unitEnumExpanded = true },
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                        Text(
                                                selectedUnitEnum.displayName,
                                                style = MaterialTheme.typography.caption,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                        )
                                }

                                DropdownMenu(
                                        expanded = unitEnumExpanded,
                                        onDismissRequest = {
                                            if (!isIosPlatform) {
                                                unitEnumExpanded = false 
                                            }
                                        }
                                ) {
                                        availableUnitEnums.forEach { unitEnum ->
                                                DropdownMenuItem(
                                                        onClick = {
                                                                onUnitEnumChange(unitEnum)
                                                                unitEnumExpanded = false
                                                        }
                                                ) { Text(unitEnum.displayName) }
                                        }
                                }
                        }

                        // UnitReq (affiché seulement si shouldShowUnitReq est true)
                        if (shouldShowUnitReq) {
                                Box(modifier = Modifier.width(80.dp)) {
                                        OutlinedButton(
                                                onClick = { unitReqExpanded = true },
                                                modifier = Modifier.fillMaxWidth()
                                        ) {
                                                Text(
                                                        selectedUnitReq.label,
                                                        style = MaterialTheme.typography.caption,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                )
                                        }

                                        DropdownMenu(
                                                expanded = unitReqExpanded,
                                                onDismissRequest = {
                                                    if (!isIosPlatform) {
                                                        unitReqExpanded = false 
                                                    }
                                                }
                                        ) {
                                                unitReqOptions.forEach { unit ->
                                                        DropdownMenuItem(
                                                                onClick = {
                                                                        onUnitReqChange(unit)
                                                                        unitReqExpanded = false
                                                                }
                                                        ) { Text(unit.label) }
                                                }
                                        }
                                }
                        }

                        // Biblio
                        Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                        onClick = { biblioExpanded = true },
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                        Text(
                                                if (selectedBiblio.uuid.isNotEmpty())
                                                        "${selectedBiblio.firstAuthor} (${selectedBiblio.year})"
                                                else translate(LocalizationKeys.NewReference.BIBLIO_PLACEHOLDER),
                                                style = MaterialTheme.typography.caption,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                        )
                                }

                                DropdownMenu(
                                        expanded = biblioExpanded,
                                        onDismissRequest = {
                                            if (!isIosPlatform) {
                                                biblioExpanded = false 
                                            }
                                        }
                                ) {
                                        biblioRefs.forEach { biblio ->
                                                DropdownMenuItem(
                                                        onClick = {
                                                                onBiblioChange(biblio)
                                                                biblioExpanded = false
                                                        }
                                                ) { Text("${biblio.firstAuthor} (${biblio.year})") }
                                        }
                                }
                        }
                }
        }
}
