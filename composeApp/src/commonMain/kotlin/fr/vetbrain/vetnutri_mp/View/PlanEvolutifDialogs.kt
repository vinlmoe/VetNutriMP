package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.LigneSynthese
import fr.vetbrain.vetnutri_mp.Data.PlanEvolutif
import fr.vetbrain.vetnutri_mp.Data.PositionReference
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.SupplementalvariableP
import fr.vetbrain.vetnutri_mp.Data.nomTraduitNutriment
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Evolutive
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import fr.vetbrain.vetnutri_mp.Utils.normalizeDecimalInput
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel

/** Parse une saisie décimale (',' ou '.') ; vide ou invalide → null. */
private fun parseDecimal(texte: String): Double? = texte.replace(',', '.').toDoubleOrNull()

private fun formatSaisie(valeur: Double?): String =
        valeur?.let { TextUtils.formatDecimal(it, 2).trimEnd('0').trimEnd(',', '.') } ?: ""

/**
 * Création ou édition d'une étape de plan évolutif : poids de l'étape (vide = poids réel de la
 * consultation) et variables requises par les équations d'énergie (vide = valeur de la
 * consultation). Le nom de l'étape en découle et n'est pas saisi.
 *
 * @param etape l'étape éditée, ou null pour une nouvelle étape
 */
@Composable
fun EtapeEditDialog(
        titre: String,
        consultation: ConsultationEv,
        etape: Ration?,
        variablesRequises: List<VariableKind>,
        onDismiss: () -> Unit,
        onSave: (poids: Double?, variables: List<SupplementalvariableP>) -> Unit
) {
    var poidsTexte by remember(etape?.uuid) { mutableStateOf(formatSaisie(etape?.poids)) }
    var variablesTexte by
            remember(etape?.uuid, variablesRequises) {
                mutableStateOf(
                        variablesRequises.associateWith { kind ->
                            formatSaisie(etape?.suppVarp?.firstOrNull { it.variable == kind }?.varue)
                        }
                )
            }
    val poidsReelTexte = consultation.weight?.let { "${formatSaisie(it)} kg" } ?: "—"

    // Aperçu du nom fixe de l'étape
    fun saisie(): Pair<Double?, List<SupplementalvariableP>> {
        val poids = parseDecimal(poidsTexte)?.takeIf { it > 0.0 }
        // Les variables non requises déjà présentes sur l'étape sont conservées
        val autres =
                etape?.suppVarp?.filter { sv ->
                    sv.variable != null && sv.variable !in variablesRequises
                } ?: emptyList()
        val saisies =
                variablesRequises.mapNotNull { kind ->
                    parseDecimal(variablesTexte[kind] ?: "")?.let { SupplementalvariableP(kind, it) }
                }
        return poids to (autres + saisies)
    }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(titre) },
            text = {
                Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                    val (poidsApercu, varsApercu) = saisie()
                    Text(
                            text = PlanEvolutif.nomAutomatique(poidsApercu, varsApercu),
                            style = MaterialTheme.typography.subtitle2,
                            color = VetNutriColors.Primary
                    )
                    OutlinedTextField(
                            value = poidsTexte,
                            onValueChange = { poidsTexte = normalizeDecimalInput(it) },
                            label = { Text(translate(Evolutive.STEP_WEIGHT)) },
                            placeholder = { Text(translate(Evolutive.STEP_WEIGHT_HINT, poidsReelTexte)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                    )
                    if (variablesRequises.isEmpty()) {
                        Text(
                                text = translate(Evolutive.NO_STEP_VARIABLES),
                                style = MaterialTheme.typography.caption,
                                color = Color.Gray
                        )
                    } else {
                        Text(
                                text = translate(Evolutive.STEP_VARIABLES),
                                style = MaterialTheme.typography.subtitle2
                        )
                    }
                    variablesRequises.forEach { kind ->
                        val valeurConsultation =
                                consultation.suppVarp.firstOrNull { it.variable == kind }?.varue
                        OutlinedTextField(
                                value = variablesTexte[kind] ?: "",
                                onValueChange = { texte ->
                                    variablesTexte = variablesTexte + (kind to normalizeDecimalInput(texte))
                                },
                                label = { Text(kind.label) },
                                placeholder = {
                                    Text(
                                            translate(
                                                    Evolutive.STEP_VARIABLE_HINT,
                                                    formatSaisie(valeurConsultation).ifBlank { "—" }
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                        onClick = {
                            val (poids, variables) = saisie()
                            onSave(poids, variables)
                        }
                ) { Text(translate(LocalizationKeys.General.OK)) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(translate(LocalizationKeys.General.CANCEL)) }
            }
    )
}

/**
 * Synthèse du plan rangé sous [parent] : ingrédients en lignes, rations du plan en colonnes
 * (triées par poids), masses modifiables, besoin, apport et couverture par colonne, puis
 * nutriments non couverts (% du minimum) et maximums dépassés (% du maximum).
 */
@Composable
fun SynthesePlanDialog(
        consultation: ConsultationEv,
        parent: Ration,
        bilans: Map<String, AnimalDetailViewModel.BilanEtape>,
        onQuantite: (Ration, String, Double) -> Unit,
        onPropager: () -> Unit,
        onDismiss: () -> Unit
) {
    val etapes = remember(consultation, parent.uuid) { PlanEvolutif.planTrie(consultation, parent) }
    val distinctives =
            remember(consultation, parent.uuid) {
                PlanEvolutif.variablesDistinctives(consultation, parent)
            }
    val libelles =
            remember(consultation, parent.uuid) {
                etapes.associate { etape ->
                    val base = PlanEvolutif.libelleEtape(consultation, etape, distinctives)
                    etape.uuid to
                            if (PlanEvolutif.estRationParente(etape))
                                    translate(Evolutive.STEP_LABEL_REAL, base)
                            else base
                }
            }
    val lignes =
            remember(consultation, parent.uuid) { PlanEvolutif.matriceSynthese(consultation, parent) }
    var edition by remember { mutableStateOf<Pair<Ration, LigneSynthese>?>(null) }
    val largeurNom = 200.dp
    val largeurCellule = 110.dp

    Dialog(onDismissRequest = onDismiss) {
        Surface(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                Text(
                        text = translate(Evolutive.SUMMARY_TITLE_FORMAT, parent.name),
                        style = MaterialTheme.typography.h6,
                        color = VetNutriColors.Primary
                )
                Spacer(Modifier.height(AppSizes.paddingSmall))
                Column(
                        modifier =
                                Modifier.weight(1f)
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState())
                                        .horizontalScroll(rememberScrollState())
                ) {
                    Row {
                        CelluleTexte(translate(Evolutive.INGREDIENT), largeurNom, gras = true, alignDebut = true)
                        etapes.forEach { etape ->
                            CelluleTexte(libelles[etape.uuid] ?: "", largeurCellule, gras = true)
                        }
                    }
                    Divider(color = VetNutriColors.Primary.copy(alpha = 0.4f))
                    lignes.forEach { ligne ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CelluleTexte(ligne.nom, largeurNom, alignDebut = true)
                            etapes.forEachIndexed { index, etape ->
                                val quantite = ligne.quantites[index]
                                CelluleTexte(
                                        texte = quantite?.let { "${TextUtils.formatDecimal(it, 1)} g" } ?: "—",
                                        largeur = largeurCellule,
                                        couleur = VetNutriColors.Primary,
                                        onClick =
                                                if (quantite != null) {
                                                    { edition = etape to ligne }
                                                } else null
                                )
                            }
                        }
                        Divider(color = Color.LightGray.copy(alpha = 0.5f))
                    }
                    Row {
                        CelluleTexte(translate(Evolutive.TOTAL_ROW), largeurNom, gras = true, alignDebut = true)
                        etapes.forEach { etape ->
                            CelluleTexte(
                                    "${TextUtils.formatDecimal(etape.getQuantiteTotale(), 1)} g",
                                    largeurCellule,
                                    gras = true
                            )
                        }
                    }
                    LigneBilan(translate(Evolutive.NEED_ROW), etapes, largeurNom, largeurCellule) { etape ->
                        bilans[etape.uuid]?.besoinTotal?.let { TextUtils.formatDecimal(it, 0) } ?: "—"
                    }
                    LigneBilan(translate(Evolutive.ENERGY_ROW), etapes, largeurNom, largeurCellule) { etape ->
                        bilans[etape.uuid]?.energieApportee?.let { TextUtils.formatDecimal(it, 0) } ?: "—"
                    }
                    LigneBilan(translate(Evolutive.COVERAGE_ROW), etapes, largeurNom, largeurCellule) { etape ->
                        val bilan = bilans[etape.uuid]
                        val besoin = bilan?.besoinTotal
                        if (bilan != null && besoin != null && besoin > 0.0)
                                "${TextUtils.formatDecimal(bilan.energieApportee / besoin * 100.0, 0)} %"
                        else "—"
                    }
                    // Nutriments dont le minimum n'est pas couvert dans au moins une étape
                    SectionPositions(
                            titre = translate(Evolutive.MIN_NOT_COVERED_SECTION),
                            etapes = etapes,
                            positions = { bilans[it.uuid]?.couverturesMin },
                            horsNorme = { it < 100.0 },
                            largeurNom = largeurNom,
                            largeurCellule = largeurCellule
                    )
                    // Nutriments dont le maximum est dépassé dans au moins une étape
                    SectionPositions(
                            titre = translate(Evolutive.MAX_EXCEEDED_SECTION),
                            etapes = etapes,
                            positions = { bilans[it.uuid]?.positionsMax },
                            horsNorme = { it > 100.0 },
                            largeurNom = largeurNom,
                            largeurCellule = largeurCellule
                    )
                }
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onPropager) { Text(translate(Evolutive.PROPAGATE)) }
                    TextButton(onClick = onDismiss) { Text(translate(LocalizationKeys.General.CLOSE)) }
                }
            }
        }
    }

    edition?.let { (etape, ligne) ->
        val index = etapes.indexOfFirst { it.uuid == etape.uuid }
        SaisieQuantiteDialog(
                titre = "${ligne.nom} — ${libelles[etape.uuid] ?: ""}",
                valeurInitiale = ligne.quantites.getOrNull(index),
                onDismiss = { edition = null },
                onConfirm = { quantite ->
                    edition = null
                    onQuantite(etape, ligne.refAlimUnif, quantite)
                }
        )
    }
}

/**
 * Section de la synthèse : une ligne par nutriment hors norme dans au moins une étape, avec
 * l'apport en % de la borne pour chaque étape (hors norme en rouge, violet si réf. maladie).
 */
@Composable
private fun SectionPositions(
        titre: String,
        etapes: List<Ration>,
        positions: (Ration) -> Map<String, PositionReference>?,
        horsNorme: (Double) -> Boolean,
        largeurNom: Dp,
        largeurCellule: Dp
) {
    val parEtape = etapes.map { positions(it).orEmpty() }
    // Ordre des nutriments de l'analyse, dédoublonné sur l'ensemble des étapes
    val labels =
            parEtape.flatMap { carte -> carte.filterValues { horsNorme(it.pourcentage) }.keys }
                    .distinct()

    Divider(color = VetNutriColors.Primary.copy(alpha = 0.4f))
    Row {
        CelluleTexte(titre, largeurNom, gras = true, alignDebut = true)
        if (labels.isEmpty()) {
            CelluleTexte(translate(Evolutive.NONE_OUT_OF_RANGE), largeurCellule, alignDebut = true)
        }
    }
    labels.forEach { label ->
        val nutriment = parEtape.firstNotNullOf { it[label] }.nutriment
        Row {
            CelluleTexte(nomTraduitNutriment(nutriment), largeurNom, alignDebut = true)
            parEtape.forEach { carte ->
                val position = carte[label]
                CelluleTexte(
                        texte =
                                position?.let { "${TextUtils.formatDecimal(it.pourcentage, 0)} %" }
                                        ?: "—",
                        largeur = largeurCellule,
                        gras = position != null && horsNorme(position.pourcentage),
                        couleur =
                                when {
                                    position == null || !horsNorme(position.pourcentage) ->
                                            Color.Unspecified
                                    position.maladie -> COULEUR_MALADIE
                                    else -> VetNutriColors.Error
                                }
                )
            }
        }
    }
}

/** Couleur des bornes issues d'une référence maladie (comme l'écran d'analyse). */
private val COULEUR_MALADIE = Color(0xFF9C27B0)

@Composable
private fun LigneBilan(
        titre: String,
        etapes: List<Ration>,
        largeurNom: Dp,
        largeurCellule: Dp,
        valeur: (Ration) -> String
) {
    Row {
        CelluleTexte(titre, largeurNom, alignDebut = true)
        etapes.forEach { etape -> CelluleTexte(valeur(etape), largeurCellule) }
    }
}

@Composable
private fun CelluleTexte(
        texte: String,
        largeur: Dp,
        gras: Boolean = false,
        alignDebut: Boolean = false,
        couleur: Color = Color.Unspecified,
        onClick: (() -> Unit)? = null
) {
    Text(
            text = texte,
            style = MaterialTheme.typography.body2,
            fontWeight = if (gras) FontWeight.Bold else FontWeight.Normal,
            color = couleur,
            textAlign = if (alignDebut) TextAlign.Start else TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier =
                    Modifier.width(largeur)
                            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                            .padding(horizontal = 6.dp, vertical = 8.dp)
    )
}

/** Saisie de la masse (g/j) d'un ingrédient pour une ration du plan. */
@Composable
private fun SaisieQuantiteDialog(
        titre: String,
        valeurInitiale: Double?,
        onDismiss: () -> Unit,
        onConfirm: (Double) -> Unit
) {
    var texte by remember { mutableStateOf(formatSaisie(valeurInitiale)) }
    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(titre) },
            text = {
                OutlinedTextField(
                        value = texte,
                        onValueChange = { texte = normalizeDecimalInput(it) },
                        label = { Text("g/j") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            },
            confirmButton = {
                TextButton(
                        onClick = { parseDecimal(texte)?.takeIf { it >= 0.0 }?.let(onConfirm) }
                ) { Text(translate(LocalizationKeys.General.OK)) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(translate(LocalizationKeys.General.CANCEL)) }
            }
    )
}
