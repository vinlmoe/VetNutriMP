package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Enumer.ContEnum
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.NumberUtils

/**
 * Calcule la quantité en unités (sachet, cuillère, etc.) pour un aliment
 * @param aliment L'aliment ration
 * @return Une chaîne de caractères représentant la quantité en unités ou null si non applicable
 */
fun calculerQuantiteEnUnites(aliment: AlimentRation): String? {
        val alim = aliment.aliment ?: return null
        val cont = alim.cont ?: return null
        val quantInt = alim.quantInt ?: return null

        // Vérifier que le cont n'est pas NO et que quantInt > 0
        if (cont == ContEnum.NO || quantInt <= 0) return null

        // Calculer le nombre d'unités
        val nombreUnites = aliment.quantite / quantInt

        // Formater le résultat
        val qte = NumberUtils.format(nombreUnites.toDouble(), 1)
        val plural = if (nombreUnites > 1) "s" else ""
        return when (cont) {
                ContEnum.SACHET ->
                        translate("alimentItem.unit.sachet", qte, plural, quantInt.toString())
                ContEnum.CAN ->
                        translate("alimentItem.unit.can", qte, plural, quantInt.toString())
                ContEnum.ML -> translate("alimentItem.unit.ml", qte, quantInt.toString())
                ContEnum.COMP ->
                        translate("alimentItem.unit.comp", qte, plural, quantInt.toString())
                ContEnum.BOUCH ->
                        translate("alimentItem.unit.bouch", qte, plural, quantInt.toString())
                ContEnum.DOSETTE ->
                        translate("alimentItem.unit.dosette", qte, plural, quantInt.toString())
                ContEnum.GEL -> translate("alimentItem.unit.gel", qte, quantInt.toString())
                ContEnum.PRESSION ->
                        translate("alimentItem.unit.pression", qte, plural, quantInt.toString())
                else -> null
        }
}

fun genererTexteRationPressePapier(ration: fr.vetbrain.vetnutri_mp.Data.Ration): String {
        val lignes = ration.alimentMutableList.map { aliment ->
                val marque = aliment.aliment?.brand?.takeIf { it.isNotBlank() }?.let { "$it, " } ?: ""
                val nom = aliment.aliment?.nom ?: translate("alimentItem.unnamedFood")
                val quantite = "${NumberUtils.format(aliment.quantite.coerceAtLeast(0.0), 1)} g"
                val unite = calculerQuantiteEnUnites(aliment)?.let { " ($it)" } ?: ""
                translate("alimentItem.clipboardLine", marque, nom, quantite, unite)
        }
        val titre = translate("alimentItem.clipboardTitle", ration.name)
        return (listOf(titre) + lignes).joinToString("\n")
}

/**
 * Composant pour afficher un aliment dans une liste
 *
 * @param aliment L'aliment à afficher
 * @param isEditing Indique si l'aliment est en cours d'édition
 * @param onStartEditing Action à exécuter pour commencer l'édition
 * @param onQuantityChange Action à exécuter lorsque la quantité change
 * @param onFinishEditing Action à exécuter lorsque l'édition est terminée
 * @param onDelete Action à exécuter pour supprimer l'aliment
 * @param modifier Modificateur optionnel
 */
@Composable
fun AlimentItem(
        aliment: AlimentRation,
        isEditing: Boolean,
        onStartEditing: () -> Unit,
        onQuantityChange: (Double) -> Unit,
        onFinishEditing: () -> Unit,
        onDelete: () -> Unit,
        feedColor: Color? = null,
        modifier: Modifier = Modifier
) {
        // État local pour la quantité en cours d'édition
        var quantityText by
                remember(aliment.uuid, aliment.quantite) { mutableStateOf(aliment.quantite.toString()) }

        var showDetailDialog by remember { mutableStateOf(false) }

        // FocusRequester pour gérer le focus automatique
        val focusRequester = remember { FocusRequester() }
        
        // Synchroniser quantityText avec aliment.quantite quand on n'est pas en mode édition
        LaunchedEffect(aliment.uuid, aliment.quantite, isEditing) {
                if (!isEditing) {
                        quantityText = aliment.quantite.coerceAtLeast(0.0).toString()
                }
        }
        
        // Demander le focus automatiquement quand on passe en mode édition
        LaunchedEffect(isEditing) {
                if (isEditing) {
                        // Petit délai pour s'assurer que le composant est bien rendu
                        kotlinx.coroutines.delay(50)
                        focusRequester.requestFocus()
                }
        }

        Card(
                modifier = modifier.fillMaxWidth(),
                elevation = AppSizes.elevationSmall,
                backgroundColor = MaterialTheme.colors.surface
        ) {
                Column(modifier = Modifier.padding(AppSizes.paddingSmall)) {
                        // En-tête avec nom et boutons d'action
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                feedColor?.let { color ->
                                        Box(
                                                modifier = Modifier
                                                        .size(10.dp)
                                                        .background(color, MaterialTheme.shapes.small)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                text = aliment.aliment?.nom ?: translate("alimentItem.noNameLabel"),
                                                style =
                                                        MaterialTheme.typography
                                                                .body2, // taille réduite
                                                fontWeight = FontWeight.Medium
                                        )

                                        // Informations supplémentaires sous le nom
                                        aliment.aliment?.let { alim ->
                                                val typeAliment = alim.typeAliment
                                                val marque = alim.brand
                                                val humidite =
                                                        alim.getNutrient(NutrientMain.HUMIDITE)

                                                Row(
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(
                                                                        AppSizes.paddingXSmall
                                                                )
                                                ) {
                                                        // Afficher la marque si c'est un aliment
                                                        // complet ou
                                                        // complémentaire
                                                        if ((typeAliment == FoodKind.COMPLET ||
                                                                        typeAliment ==
                                                                                FoodKind.COMPLEMENTAIRE) &&
                                                                        !marque.isNullOrBlank()
                                                        ) {
                                                                Text(
                                                                        text = marque,
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .caption,
                                                                        color =
                                                                                MaterialTheme.colors
                                                                                        .onSurface
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.7f
                                                                                        )
                                                                )
                                                        }

                                                        // Afficher l'état humide/sec basé sur
                                                        // l'humidité
                                                        humidite?.let { hum ->
                                                                val etatHumidite =
                                                                        if (hum > 15.0) translate("alimentItem.moist")
                                                                        else translate("alimentItem.dry")
                                                                Text(
                                                                        text = etatHumidite,
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .caption,
                                                                        color =
                                                                                if (hum > 15.0)
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                                else
                                                                                        VetNutriColors
                                                                                                .Secondary
                                                                )
                                                        }
                                                }
                                        }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (aliment.aliment != null) {
                                                IconButton(
                                                        onClick = { showDetailDialog = true },
                                                        modifier = Modifier.size(16.dp) // icône plus petite
                                                ) {
                                                        Icon(
                                                                imageVector = Icons.Filled.Search,
                                                                contentDescription = translate("alimentItem.viewDetails"),
                                                                tint = VetNutriColors.Primary,
                                                                modifier = Modifier.size(16.dp)
                                                        )
                                                }
                                        }

                                        IconButton(
                                                onClick = onStartEditing,
                                                modifier = Modifier.size(16.dp) // icône plus petite
                                        ) {
                                                Icon(
                                                        imageVector = Icons.Filled.Edit,
                                                        contentDescription = translate("alimentItem.editQuantity"),
                                                        tint = VetNutriColors.Secondary,
                                                        modifier =
                                                                Modifier.size(
                                                                        16.dp
                                                                ) // icône plus petite
                                                )
                                        }

                                        IconButton(
                                                onClick = onDelete,
                                                modifier = Modifier.size(16.dp) // icône plus petite
                                        ) {
                                                Icon(
                                                        imageVector = Icons.Filled.Delete,
                                                        contentDescription = translate(LocalizationKeys.General.DELETE),
                                                        tint = VetNutriColors.Error,
                                                        modifier =
                                                                Modifier.size(
                                                                        16.dp
                                                                ) // icône plus petite
                                                )
                                        }
                                }
                        }

                        // Informations sur l'aliment
                        Column(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(top = AppSizes.paddingXSmall)
                        ) {
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                text = translate("alimentItem.quantityLabel"),
                                                style = MaterialTheme.typography.caption
                                        ) // texte réduit

                                        if (isEditing) {
                                                val onValidate = {
                                                        // Normaliser la virgule en point pour la conversion
                                                        val texteNormalise: String =
                                                                quantityText.replace(
                                                                        ',',
                                                                        '.'
                                                                )
                                                        val nouvelleQuantite: Double =
                                                                texteNormalise.toDoubleOrNull()
                                                                        ?: aliment.quantite
                                                        onQuantityChange(nouvelleQuantite.coerceAtLeast(0.0))
                                                        onFinishEditing()
                                                }

                                                // Mode édition avec le composant
                                                // BasicNumberTextField
                                                BasicNumberTextField(
                                                        value = quantityText,
                                                        onValueChange = { newValue ->
                                                                // Filtrer pour n'accepter que les
                                                                // nombres
                                                                // et décimaux (point ou virgule)
                                                                val texteFiltre =
                                                                        newValue.filter { char ->
                                                                                char.isDigit() ||
                                                                                        char == '.' ||
                                                                                        char == ','
                                                                        }
                                                                // S'assurer qu'il n'y a qu'un seul séparateur décimal
                                                                val pointCount =
                                                                        texteFiltre.count { it == '.' }
                                                                val virguleCount =
                                                                        texteFiltre.count { it == ',' }
                                                                if (pointCount <= 1 &&
                                                                                virguleCount <= 1 &&
                                                                                pointCount +
                                                                                        virguleCount <=
                                                                                        1
                                                                ) {
                                                                        quantityText = texteFiltre
                                                                }
                                                        },
                                                        placeholder = "",
                                                        modifier =
                                                                Modifier.weight(1f).height(40.dp)
                                                                        .onPreviewKeyEvent {
                                                                                if (it.key == Key.Enter && it.type == KeyEventType.KeyDown) {
                                                                                        onValidate()
                                                                                        true
                                                                                } else {
                                                                                        false
                                                                                }
                                                                        },
                                                        singleLine = true,
                                                        focusRequester = focusRequester,
                                                        selectAllOnFocus = true
                                                )

                                                Button(
                                                        onClick = onValidate,
                                                        colors =
                                                                ButtonDefaults.buttonColors(
                                                                        backgroundColor =
                                                                                VetNutriColors
                                                                                        .Primary,
                                                                        contentColor =
                                                                                VetNutriColors
                                                                                        .OnPrimary
                                                                ),
                                                        modifier =
                                                                Modifier.padding(
                                                                        start =
                                                                                AppSizes.paddingSmall
                                                                )
                                                ) {
                                                        Text(
                                                                translate(LocalizationKeys.General.OK),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption
                                                        )
                                                }
                                        } else {
                                                // Mode affichage
                                                Text(
                                                        text = "${NumberUtils.format(aliment.quantite.coerceAtLeast(0.0), 1)} g",
                                                        style =
                                                                MaterialTheme.typography
                                                                        .caption, // texte réduit
                                                        fontWeight = FontWeight.Medium,
                                                        modifier = Modifier.clickable { onStartEditing() }
                                                )
                                        }
                                }

                                // Affichage de la quantité en unités si applicable
                                calculerQuantiteEnUnites(aliment)?.let { quantiteUnites ->
                                        Text(
                                                text = quantiteUnites,
                                                style = MaterialTheme.typography.caption,
                                                color =
                                                        MaterialTheme.colors.onSurface.copy(
                                                                alpha = 0.7f
                                                        ),
                                                modifier = Modifier.padding(top = 2.dp)
                                        )
                                }
                        }
                }
        }

        if (showDetailDialog) {
                aliment.aliment?.let { alim ->
                        AlimentDetailDialog(aliment = alim, onDismiss = { showDetailDialog = false })
                }
        }
}
