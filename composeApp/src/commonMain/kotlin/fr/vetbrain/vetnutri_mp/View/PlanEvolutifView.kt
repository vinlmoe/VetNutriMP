package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.CenteredMessage
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.LigneSynthese
import fr.vetbrain.vetnutri_mp.Data.PlanEvolutif
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.SupplementalvariableP
import fr.vetbrain.vetnutri_mp.Data.VariablesEtape
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Evolutive
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Repository.RecipeRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import fr.vetbrain.vetnutri_mp.Utils.VariablesEnergie
import fr.vetbrain.vetnutri_mp.Utils.normalizeDecimalInput
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import kotlinx.coroutines.launch

/**
 * Plan évolutif d'une consultation : une ration par étape (croissance, gestation, lactation,
 * activité...). Chaque étape a son propre poids et ses propres variables d'énergie ; l'onglet
 * « Étape » réutilise [RationsView] (édition, ajustement, analyse des besoins au poids de
 * l'étape) et l'onglet « Synthèse » présente les masses des ingrédients pour toutes les étapes.
 */
@Composable
fun PlanEvolutifView(
        viewModel: AnimalDetailViewModel,
        showSnackbar: (String) -> Unit,
        equationRepository: EquationRepository,
        recipeRepository: RecipeRepository,
        isExamMode: Boolean = false,
        modifier: Modifier = Modifier
) {
    val consultationState by viewModel.selectedConsultation.collectAsState()
    val selectedRation by viewModel.selectedRation.collectAsState()
    val bilans by viewModel.bilansEtapes.collectAsState()
    val referenceUtilisee by viewModel.referenceUtilisee.collectAsState()
    val availableReferences by viewModel.availableReferences.collectAsState()

    // Messages affichés localement (l'écran parent peut ne pas avoir d'hôte de snackbar)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val notifier: (String) -> Unit = { message ->
        showSnackbar(message)
        coroutineScope.launch { snackbarHostState.showSnackbar(message) }
    }

    val consultation = consultationState
    if (consultation == null) {
        CenteredMessage(translate(LocalizationKeys.Graph.NO_CONSULTATION), modifier)
        return
    }
    if (!consultation.isEvolutive) {
        CenteredMessage(translate(Evolutive.NOT_EVOLUTIVE), modifier)
        return
    }

    LaunchedEffect(consultation.uuid) { viewModel.recalculerBilansEtapes() }

    val etapes = remember(consultation) { PlanEvolutif.etapesTriees(consultation) }
    if (etapes.isEmpty()) {
        CreationPlan(
                consultation = consultation,
                onCreer = { depart -> viewModel.creerPlanEvolutif(depart) },
                modifier = modifier
        )
        return
    }

    val distinctives = remember(consultation) { PlanEvolutif.variablesDistinctives(consultation) }
    val libelles =
            remember(consultation, distinctives) {
                etapes.associate { etape ->
                    val base = PlanEvolutif.libelleEtape(consultation, etape, distinctives)
                    etape.uuid to
                            if (PlanEvolutif.estPoidsReel(etape))
                                    translate(Evolutive.STEP_LABEL_REAL, base)
                            else base
                }
            }

    // Toujours travailler sur une étape du plan
    val etapeSelectionnee = etapes.firstOrNull { it.uuid == selectedRation?.uuid }
    LaunchedEffect(consultation.uuid, etapes.map { it.uuid }) {
        if (etapeSelectionnee == null) viewModel.selectRation(etapes.first())
    }

    val referencesComplementaires =
            remember(consultation.referencesMaladies, availableReferences) {
                consultation.referencesMaladies.mapNotNull { id ->
                    availableReferences.firstOrNull { it.uuid == id }
                }
            }
    val variablesRequises =
            remember(referenceUtilisee, referencesComplementaires) {
                VariablesEnergie.variablesEnergieRequises(
                        referenceUtilisee,
                        referencesComplementaires
                )
            }

    var onglet by remember { mutableStateOf(0) }
    var showAjoutEtape by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // En-tête : titre + actions
            Row(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .padding(horizontal = AppSizes.paddingMedium, vertical = AppSizes.paddingSmall),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text =
                                translate(
                                        Evolutive.PLAN_TITLE_FORMAT,
                                        consultation.profilEvolutif?.translateEnum() ?: ""
                                ),
                        style = MaterialTheme.typography.h6,
                        color = VetNutriColors.Primary,
                        modifier = Modifier.weight(1f)
                )
                IconButtonWithTooltip(
                        imageVector = Icons.Filled.Add,
                        contentDescription = translate(Evolutive.ADD_STEP),
                        tooltip = translate(Evolutive.ADD_STEP),
                        tint = VetNutriColors.Primary,
                        onClick = { showAjoutEtape = true }
                )
                IconButtonWithTooltip(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = translate(Evolutive.PROPAGATE),
                        tooltip = translate(Evolutive.PROPAGATE),
                        tint = VetNutriColors.Primary,
                        onClick = {
                            etapeSelectionnee?.let { source ->
                                val ajouts = viewModel.propagerAlimentsEtape(source)
                                notifier(translate(Evolutive.PROPAGATE_DONE, ajouts.toString()))
                            }
                        }
                )
            }

            // Étapes triées (poids croissant)
            Row(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = AppSizes.paddingMedium),
                    horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
            ) {
                etapes.forEach { etape ->
                    PuceEtape(
                            libelle = libelles[etape.uuid] ?: "",
                            selectionnee = etape.uuid == etapeSelectionnee?.uuid,
                            onClick = { viewModel.selectRation(etape) }
                    )
                }
            }

            // Poids et variables de l'étape sélectionnée
            etapeSelectionnee?.let { etape ->
                EditeurEtape(
                        consultation = consultation,
                        etape = etape,
                        variablesRequises = variablesRequises,
                        onAppliquer = { poids, variables ->
                            viewModel.mettreAJourEtape(etape, poids, variables)
                        },
                        onSupprimer = {
                            if (!viewModel.supprimerEtape(etape)) {
                                notifier(translate(Evolutive.LAST_REAL_STEP))
                            }
                        }
                )
            }

            TabRow(selectedTabIndex = onglet, backgroundColor = VetNutriColors.Surface) {
                Tab(
                        selected = onglet == 0,
                        onClick = { onglet = 0 },
                        text = { Text(translate(Evolutive.TAB_STEP)) }
                )
                Tab(
                        selected = onglet == 1,
                        onClick = { onglet = 1 },
                        text = { Text(translate(Evolutive.TAB_SUMMARY)) }
                )
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                when (onglet) {
                    0 ->
                            RationsView(
                                    viewModel = viewModel,
                                    showSnackbar = notifier,
                                    equationRepository = equationRepository,
                                    recipeRepository = recipeRepository,
                                    isExamMode = isExamMode
                            )
                    else ->
                            SynthesePlan(
                                    consultation = consultation,
                                    etapes = etapes,
                                    libelles = libelles,
                                    bilans = bilans,
                                    onQuantite = { etape, ref, quantite ->
                                        viewModel.mettreAJourQuantiteEtape(etape, ref, quantite)
                                    }
                            )
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    if (showAjoutEtape) {
        SaisiePoidsDialog(
                titre = translate(Evolutive.ADD_STEP),
                poidsReel = consultation.weight,
                onDismiss = { showAjoutEtape = false },
                onConfirm = { poids ->
                    showAjoutEtape = false
                    viewModel.ajouterEtape(poids)
                }
        )
    }
}

/** Création du plan : choix de la ration de départ (copiée pour la première étape). */
@Composable
private fun CreationPlan(
        consultation: ConsultationEv,
        onCreer: (Ration?) -> Unit,
        modifier: Modifier = Modifier
) {
    Column(
            modifier =
                    modifier.fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(AppSizes.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
    ) {
        Text(
                text = translate(Evolutive.NO_PLAN),
                style = MaterialTheme.typography.body1
        )
        consultation.rations.filter { !it.recette }.forEach { ration ->
            OutlinedButton(onClick = { onCreer(ration) }, modifier = Modifier.fillMaxWidth()) {
                Text(translate(Evolutive.CREATE_PLAN_FROM, ration.name.ifBlank { "—" }))
            }
        }
        TextButton(onClick = { onCreer(null) }) { Text(translate(Evolutive.CREATE_EMPTY_PLAN)) }
    }
}

@Composable
private fun PuceEtape(libelle: String, selectionnee: Boolean, onClick: () -> Unit) {
    Surface(
            shape = RoundedCornerShape(50),
            color = if (selectionnee) VetNutriColors.Primary else VetNutriColors.Surface,
            border = BorderStroke(1.dp, VetNutriColors.Primary),
            modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
                text = libelle,
                color = if (selectionnee) Color.White else VetNutriColors.Primary,
                style = MaterialTheme.typography.body2,
                fontWeight = if (selectionnee) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/** Parse une saisie décimale (',' ou '.') ; vide ou invalide → null. */
private fun parseDecimal(texte: String): Double? = texte.replace(',', '.').toDoubleOrNull()

private fun formatSaisie(valeur: Double?): String =
        valeur?.let { TextUtils.formatDecimal(it, 2).trimEnd('0').trimEnd(',', '.') } ?: ""

/**
 * Poids de l'étape (vide = poids réel) et variables requises par les équations d'énergie
 * (vide = valeur de la consultation).
 */
@Composable
private fun EditeurEtape(
        consultation: ConsultationEv,
        etape: Ration,
        variablesRequises: List<VariableKind>,
        onAppliquer: (Double?, List<SupplementalvariableP>) -> Unit,
        onSupprimer: () -> Unit
) {
    var poidsTexte by remember(etape.uuid, etape.poids) { mutableStateOf(formatSaisie(etape.poids)) }
    val valeursInitiales =
            remember(etape.uuid, etape.suppVarp) {
                variablesRequises.associateWith { kind ->
                    formatSaisie(etape.suppVarp.firstOrNull { it.variable == kind }?.varue)
                }
            }
    var variablesTexte by remember(etape.uuid, etape.suppVarp, variablesRequises) {
        mutableStateOf(valeursInitiales)
    }
    val poidsReelTexte = consultation.weight?.let { "${formatSaisie(it)} kg" } ?: "—"

    Card(
            modifier =
                    Modifier.fillMaxWidth()
                            .padding(horizontal = AppSizes.paddingMedium, vertical = AppSizes.paddingSmall),
            elevation = AppSizes.elevationSmall,
            backgroundColor = VetNutriColors.Surface
    ) {
        Row(
                modifier =
                        Modifier.horizontalScroll(rememberScrollState())
                                .padding(AppSizes.paddingSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
        ) {
            OutlinedTextField(
                    value = poidsTexte,
                    onValueChange = { poidsTexte = normalizeDecimalInput(it) },
                    label = { Text(translate(Evolutive.STEP_WEIGHT)) },
                    placeholder = { Text(translate(Evolutive.STEP_WEIGHT_HINT, poidsReelTexte)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.width(220.dp)
            )
            if (variablesRequises.isEmpty()) {
                Text(
                        text = translate(Evolutive.NO_STEP_VARIABLES),
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray,
                        modifier = Modifier.widthIn(max = 260.dp)
                )
            }
            variablesRequises.forEach { kind ->
                val valeurConsultation =
                        consultation.suppVarp.firstOrNull { it.variable == kind }?.varue
                OutlinedTextField(
                        value = variablesTexte[kind] ?: "",
                        onValueChange = { saisie ->
                            variablesTexte = variablesTexte + (kind to normalizeDecimalInput(saisie))
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
                        modifier = Modifier.width(160.dp)
                )
            }
            IconButtonWithTooltip(
                    imageVector = Icons.Filled.Check,
                    contentDescription = translate(Evolutive.APPLY_STEP),
                    tooltip = translate(Evolutive.APPLY_STEP),
                    tint = VetNutriColors.Primary,
                    onClick = {
                        val poids = parseDecimal(poidsTexte)?.takeIf { it > 0.0 }
                        // Les variables non requises déjà saisies sur l'étape sont conservées
                        val autres =
                                etape.suppVarp.filter { sv ->
                                    sv.variable != null && sv.variable !in variablesRequises
                                }
                        val saisies =
                                variablesRequises.mapNotNull { kind ->
                                    parseDecimal(variablesTexte[kind] ?: "")?.let {
                                        SupplementalvariableP(kind, it)
                                    }
                                }
                        onAppliquer(poids, autres + saisies)
                    }
            )
            IconButtonWithTooltip(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = translate(Evolutive.DELETE_STEP),
                    tooltip = translate(Evolutive.DELETE_STEP),
                    tint = VetNutriColors.Error,
                    onClick = onSupprimer
            )
        }
    }
}

/** Tableau ingrédients × étapes, masses éditables, et bilan énergétique par étape. */
@Composable
private fun SynthesePlan(
        consultation: ConsultationEv,
        etapes: List<Ration>,
        libelles: Map<String, String>,
        bilans: Map<String, AnimalDetailViewModel.BilanEtape>,
        onQuantite: (Ration, String, Double) -> Unit
) {
    val lignes = remember(consultation) { PlanEvolutif.matriceSynthese(consultation) }
    var edition by remember { mutableStateOf<Pair<Ration, LigneSynthese>?>(null) }
    val largeurNom = 200.dp
    val largeurCellule = 110.dp

    Column(
            modifier =
                    Modifier.fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                            .padding(AppSizes.paddingMedium)
    ) {
        // En-tête
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

        // Totaux et bilan énergétique
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

@Composable
private fun LigneBilan(
        titre: String,
        etapes: List<Ration>,
        largeurNom: androidx.compose.ui.unit.Dp,
        largeurCellule: androidx.compose.ui.unit.Dp,
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
        largeur: androidx.compose.ui.unit.Dp,
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

/** Saisie du poids d'une nouvelle étape (vide = poids réel de la consultation). */
@Composable
private fun SaisiePoidsDialog(
        titre: String,
        poidsReel: Double?,
        onDismiss: () -> Unit,
        onConfirm: (Double?) -> Unit
) {
    var texte by remember { mutableStateOf("") }
    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(titre) },
            text = {
                OutlinedTextField(
                        value = texte,
                        onValueChange = { texte = normalizeDecimalInput(it) },
                        label = { Text(translate(Evolutive.STEP_WEIGHT)) },
                        placeholder = {
                            Text(
                                    translate(
                                            Evolutive.STEP_WEIGHT_HINT,
                                            poidsReel?.let { "${formatSaisie(it)} kg" } ?: "—"
                                    )
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            },
            confirmButton = {
                TextButton(onClick = { onConfirm(parseDecimal(texte)?.takeIf { it > 0.0 }) }) {
                    Text(translate(LocalizationKeys.General.OK))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(translate(LocalizationKeys.General.CANCEL)) }
            }
    )
}

/** Saisie de la masse (g/j) d'un ingrédient pour une étape. */
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
