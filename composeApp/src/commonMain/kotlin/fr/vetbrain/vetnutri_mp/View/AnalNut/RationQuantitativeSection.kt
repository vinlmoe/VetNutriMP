package fr.vetbrain.vetnutri_mp.View.AnalNut

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.CenteredMessage
import fr.vetbrain.vetnutri_mp.Components.TooltipArea
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.PreferencesApplication
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Data.ValeurNutritionnelle
import fr.vetbrain.vetnutri_mp.Data.analyserValeursNutritionnellesRation
import fr.vetbrain.vetnutri_mp.Data.analyserValeursNutritionnellesRationAvecEquations
import fr.vetbrain.vetnutri_mp.Enumer.AAEnum
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis
import fr.vetbrain.vetnutri_mp.Enumer.NutrientEnergy
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.NutrientOther
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.AnalNut
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private enum class RationNutrientQuantityMode(val labelKey: String) {
    PER_100G_RATION(AnalNut.QUANTITY_MODE_PER_100G_RATION),
    PER_100G_DM(AnalNut.QUANTITY_MODE_PER_100G_DM),
    PER_MCAL(AnalNut.QUANTITY_MODE_PER_MCAL),
    PER_MJ(AnalNut.QUANTITY_MODE_PER_MJ),
    ABSOLUTE(AnalNut.QUANTITY_MODE_ABSOLUTE)
}

@Composable
fun AnalyseQuantitativeRationSection(
    ration: Ration,
    referenceUtilisee: ReferenceEv?,
    equationRepository: EquationRepository,
    preferencesApplication: PreferencesApplication?,
    animal: AnimalEv?,
    nutrimentsSelectionnes: List<String>,
    energieTotaleKcal: Double,
    isLargeView: Boolean,
    onContextBadgeClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val compositionAccent = Color(0xFF0F766E)
    var mode by remember { mutableStateOf(RationNutrientQuantityMode.PER_100G_RATION) }
    var showModeMenu by remember { mutableStateOf(false) }
    var afficherTous by remember { mutableStateOf(false) }
    var valeursNutritionnelles by remember { mutableStateOf<Map<String, ValeurNutritionnelle>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }

    val rationChangeKey = remember(ration) { ration.alimentMutableList.map { "${it.uuid}:${it.quantite}" } }

    LaunchedEffect(ration.uuid, rationChangeKey, referenceUtilisee, preferencesApplication, animal, equationRepository) {
        loading = true
        valeursNutritionnelles = withContext(Dispatchers.Default) {
            val preferencesEspece =
                if (animal != null && preferencesApplication != null) {
                    preferencesApplication.getPreferencesEspece(animal.getEspece())
                } else {
                    null
                }

            if (referenceUtilisee != null && preferencesEspece != null) {
                analyserValeursNutritionnellesRationAvecEquations(
                    ration = ration,
                    preferencesEspece = preferencesEspece,
                    equationRepository = equationRepository,
                    referenceEv = referenceUtilisee
                )
            } else {
                analyserValeursNutritionnellesRation(ration)
            }
        }
        loading = false
    }

    val quantiteTotaleRation = ration.getQuantiteTotale()
    val humiditeTotale = valeursNutritionnelles["HUMIDITE"]?.valeur ?: 0.0
    val matiereSecheTotale = (quantiteTotaleRation - humiditeTotale).coerceAtLeast(0.0)

    val nutrimentsAffiches =
        remember(valeursNutritionnelles, afficherTous, nutrimentsSelectionnes, mode) {
            valeursNutritionnelles
                .toList()
                .filter { (nom, valeur) ->
                    val hideHumidityInDryMatter =
                        mode == RationNutrientQuantityMode.PER_100G_DM &&
                            (nom == NutrientMain.HUMIDITE.label || valeur.nutriment == NutrientMain.HUMIDITE)
                    !hideHumidityInDryMatter
                }
                .filter { (_, valeur) ->
                    val isNutrientRatio = valeur.nutriment is NutrientAnalysis
                    if (isNutrientRatio) true else valeur.valeur > 0.0
                }
                .filter { (nom, _) ->
                    if (afficherTous) true else nutrimentsSelectionnes.contains(nom)
                }
                .sortedBy { (_, valeur) -> ordreNutrimentAnalyseRation(valeur.nutriment) }
        }

    val nutrimentsGroupes =
        remember(nutrimentsAffiches) {
            nutrimentsAffiches.groupBy { (_, valeur) -> categorieNutrimentAnalyseRation(valeur.nutriment) }
        }

    Card(
        modifier = modifier,
        elevation = AppSizes.elevationMedium,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingSmall),
            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)
                ) {
                    TooltipArea(tooltip = translate(AnalNut.TOOLTIP_SWITCH_TO_INTAKES)) {
                        Surface(
                            modifier =
                                if (onContextBadgeClick != null) {
                                    Modifier.clickable { onContextBadgeClick.invoke() }
                                } else {
                                    Modifier
                                },
                            color = compositionAccent.copy(alpha = 0.15f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = translate(AnalNut.COMPOSITION),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.overline,
                                fontWeight = FontWeight.Bold,
                                color = compositionAccent
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)
                ) {
                    Box {
                        TextButton(
                            onClick = { showModeMenu = true },
                            contentPadding = PaddingValues(horizontal = AppSizes.paddingSmall, vertical = 0.dp)
                        ) {
                            Text(
                                text = translate(mode.labelKey),
                                style = MaterialTheme.typography.caption,
                                color = compositionAccent
                            )
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                tint = compositionAccent
                            )
                        }
                        DropdownMenu(
                            expanded = showModeMenu,
                            onDismissRequest = { showModeMenu = false }
                        ) {
                            RationNutrientQuantityMode.entries.forEach { option ->
                                DropdownMenuItem(
                                    onClick = {
                                        mode = option
                                        showModeMenu = false
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)
                                    ) {
                                        Text(
                                            translate(option.labelKey),
                                            style = MaterialTheme.typography.body2
                                        )
                                        if (option == mode) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = null,
                                                tint = compositionAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = if (afficherTous) translate(AnalNut.ALL) else translate(AnalNut.SELECTED),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                    IconButton(
                        onClick = { afficherTous = !afficherTous },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (afficherTous) Icons.Filled.ToggleOn else Icons.Filled.ToggleOff,
                            contentDescription = null,
                            tint =
                                if (afficherTous) compositionAccent
                                else MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            if (loading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingSmall),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (nutrimentsAffiches.isEmpty()) {
                CenteredMessage(
                    message = translate(AnalNut.NO_VALUE_AVAILABLE),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                val ordreCategories =
                    listOf("BASE", "MACRO", "MIN", "VITAM", "LIPID", "AMA", "ANA", "OTHER", "ENERGY")

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                    modifier =
                        if (isLargeView) {
                            Modifier.fillMaxWidth().fillMaxHeight()
                        } else {
                            Modifier.fillMaxWidth().height(400.dp)
                        }
                ) {
                    ordreCategories.forEach { categorie ->
                        val nutrimentsCategorie =
                            nutrimentsGroupes[categorie]
                                ?.sortedBy { (_, valeur) -> ordreNutrimentAnalyseRation(valeur.nutriment) }
                                ?: emptyList()

                        if (nutrimentsCategorie.isNotEmpty()) {
                            item {
                                TitreSectionAnalyseRationCard(
                                    titre = titreCategorieAnalyseRation(categorie),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)
                                ) {
                                    nutrimentsCategorie.chunked(3).forEach { rangee ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)
                                        ) {
                                            rangee.forEach { (nom, valeur) ->
                                                QuantitativeNutrimentCard(
                                                    nom = nom,
                                                    valeurNutritionnelle = valeur,
                                                    mode = mode,
                                                    quantiteTotaleRation = quantiteTotaleRation,
                                                    matiereSecheTotale = matiereSecheTotale,
                                                    energieTotaleKcal = energieTotaleKcal,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            repeat(3 - rangee.size) { Spacer(modifier = Modifier.weight(1f)) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuantitativeNutrimentCard(
    nom: String,
    valeurNutritionnelle: ValeurNutritionnelle,
    mode: RationNutrientQuantityMode,
    quantiteTotaleRation: Double,
    matiereSecheTotale: Double,
    energieTotaleKcal: Double,
    modifier: Modifier = Modifier
) {
    val isNutrientRatio = valeurNutritionnelle.nutriment is NutrientAnalysis
    val factor =
        facteurConversionQuantite(
            mode = mode,
            quantiteTotaleRation = quantiteTotaleRation,
            matiereSecheTotale = matiereSecheTotale,
            energieTotaleKcal = energieTotaleKcal
        )
    val valeurAffichee =
        if (isNutrientRatio || factor == null) {
            valeurNutritionnelle.valeur
        } else {
            valeurNutritionnelle.valeur * factor
        }
    val uniteAffichee =
        if (isNutrientRatio) {
            valeurNutritionnelle.unite.displayName
        } else {
            construireUniteAffichage(uniteBase = valeurNutritionnelle.unite.displayName, mode = mode)
        }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = AppSizes.elevationSmall,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier.padding(AppSizes.paddingXSmall),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = traduireNomNutrimentPourRation(nom, valeurNutritionnelle),
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Bold,
                color = VetNutriColors.Primary,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${TextUtils.formatDecimal(valeurAffichee, 2)} $uniteAffichee",
                style = MaterialTheme.typography.overline,
                color = MaterialTheme.colors.onSurface,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun TitreSectionAnalyseRationCard(titre: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = 2.dp,
        backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(
                modifier = Modifier.weight(1f).height(1.dp),
                color = VetNutriColors.Primary.copy(alpha = 0.3f)
            )
            Text(
                text = titre,
                style = MaterialTheme.typography.subtitle2,
                fontWeight = FontWeight.Bold,
                color = VetNutriColors.Primary,
                modifier = Modifier.padding(horizontal = AppSizes.paddingSmall)
            )
            Divider(
                modifier = Modifier.weight(1f).height(1.dp),
                color = VetNutriColors.Primary.copy(alpha = 0.3f)
            )
        }
    }
}

private fun facteurConversionQuantite(
    mode: RationNutrientQuantityMode,
    quantiteTotaleRation: Double,
    matiereSecheTotale: Double,
    energieTotaleKcal: Double
): Double? {
    return when (mode) {
        RationNutrientQuantityMode.ABSOLUTE -> 1.0
        RationNutrientQuantityMode.PER_100G_RATION ->
            if (quantiteTotaleRation > 0.0) 100.0 / quantiteTotaleRation else null
        RationNutrientQuantityMode.PER_100G_DM ->
            if (matiereSecheTotale > 0.0) 100.0 / matiereSecheTotale else null
        RationNutrientQuantityMode.PER_MCAL ->
            if (energieTotaleKcal > 0.0) 1000.0 / energieTotaleKcal else null
        RationNutrientQuantityMode.PER_MJ ->
            if (energieTotaleKcal > 0.0) 239.005736 / energieTotaleKcal else null
    }
}

private fun construireUniteAffichage(uniteBase: String, mode: RationNutrientQuantityMode): String {
    val suffixe =
        when (mode) {
            RationNutrientQuantityMode.ABSOLUTE -> ""
            else -> translate(mode.labelKey)
        }
    return if (uniteBase.isBlank()) suffixe else "$uniteBase $suffixe".trim()
}

private fun traduireNomNutrimentPourRation(nom: String, valeur: ValeurNutritionnelle): String {
    return when (val nutriment = valeur.nutriment) {
        is NutrientLipid -> nutriment.translateEnum()
        is NutrientMacro -> nutriment.translateEnum()
        is NutrientMain -> nutriment.translateEnum()
        is NutrientMin -> nutriment.translateEnum()
        is NutrientOther -> nutriment.translateEnum()
        is NutrientVitam -> nutriment.translateEnum()
        is AAEnum -> nutriment.translateEnum()
        is NutrientAnalysis -> nutriment.translateEnum()
        else -> nom
    }
}

private fun ordreNutrimentAnalyseRation(nutriment: Nutrient): Int {
    val categorieOffset =
        when (nutriment) {
            is NutrientMain -> 0
            is NutrientMacro -> 1000
            is NutrientMin -> 2000
            is NutrientVitam -> 3000
            is NutrientLipid -> 4000
            is AAEnum -> 5000
            is NutrientAnalysis -> 6000
            is NutrientOther -> 7000
            is NutrientEnergy -> 8000
            else -> 9000
        }

    val index =
        when (nutriment) {
            is NutrientMain -> nutriment.ordinal
            is NutrientMacro -> nutriment.ordinal
            is NutrientMin -> nutriment.ordinal
            is NutrientVitam -> nutriment.ordinal
            is NutrientLipid -> nutriment.ordinal
            is AAEnum -> nutriment.ordinal
            is NutrientAnalysis -> nutriment.ordinal
            is NutrientOther -> nutriment.ordinal
            is NutrientEnergy -> nutriment.ordinal
            else -> 999
        }

    return categorieOffset + index
}

private fun categorieNutrimentAnalyseRation(nutriment: Nutrient): String {
    return when (nutriment) {
        is NutrientMain -> "BASE"
        is NutrientMacro -> "MACRO"
        is NutrientMin -> "MIN"
        is NutrientVitam -> "VITAM"
        is NutrientLipid -> "LIPID"
        is AAEnum -> "AMA"
        is NutrientAnalysis -> "ANA"
        is NutrientOther -> "OTHER"
        is NutrientEnergy -> "ENERGY"
        else -> "OTHER"
    }
}

private fun titreCategorieAnalyseRation(categorie: String): String {
    return when (categorie) {
        "BASE" -> translate(LocalizationKeys.NutrientCategory.BASE_NAME)
        "MACRO" -> translate(LocalizationKeys.NutrientCategory.MACRO_NAME)
        "MIN" -> translate(LocalizationKeys.NutrientCategory.MIN_NAME)
        "VITAM" -> translate(LocalizationKeys.NutrientCategory.VITAM_NAME)
        "LIPID" -> translate(LocalizationKeys.NutrientCategory.LIPID_NAME)
        "AMA" -> translate(LocalizationKeys.NutrientCategory.AMA_NAME)
        "ANA" -> translate(LocalizationKeys.NutrientCategory.ANA_NAME)
        "OTHER" -> translate(LocalizationKeys.NutrientCategory.OTHER_NAME)
        "ENERGY" -> translate(LocalizationKeys.NutrientCategory.ENERGIE_NAME)
        else -> categorie
    }
}
