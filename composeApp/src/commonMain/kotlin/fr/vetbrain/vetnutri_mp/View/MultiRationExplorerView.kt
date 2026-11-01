package fr.vetbrain.vetnutri_mp.View

import fr.vetbrain.vetnutri_mp.Components.AdaptiveTabRow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.CaseEquilibre
import fr.vetbrain.vetnutri_mp.Data.CibleExploration
import fr.vetbrain.vetnutri_mp.Data.FoodSearchFilters
import fr.vetbrain.vetnutri_mp.Data.ParametresExploration
import fr.vetbrain.vetnutri_mp.Data.ResultatExploration
import fr.vetbrain.vetnutri_mp.Data.RoleExploration
import fr.vetbrain.vetnutri_mp.Data.ScenarioExploration
import fr.vetbrain.vetnutri_mp.Data.StatutScenario
import fr.vetbrain.vetnutri_mp.Data.ZoneEquilibre
import fr.vetbrain.vetnutri_mp.Data.formaterQuantite
import fr.vetbrain.vetnutri_mp.Data.nomTraduitNutriment
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.ExcelPlatform.isCsvFileOperationsSupported
import fr.vetbrain.vetnutri_mp.ExcelPlatform.saveCsvFileForExport
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.View.Components.FoodSearchComponent
import fr.vetbrain.vetnutri_mp.View.Components.FoodSearchConfig
import fr.vetbrain.vetnutri_mp.View.Components.FoodSearchLayout
import fr.vetbrain.vetnutri_mp.ViewModel.MultiRationViewModel
import kotlinx.coroutines.launch
import fr.vetbrain.vetnutri_mp.Localization.translate

/** Nombre maximal de rations d'une case ouvertes ensemble dans une consultation. */
private const val MAX_RATIONS_PAR_CASE = 20

private val niveauxCible =
        listOf(
                Reflevel.OPTIMIN to translate("multiration.niveau.OPTIMIN"),
                Reflevel.MIN to translate("multiration.niveau.MIN"),
                Reflevel.OPTIMAX to translate("multiration.niveau.OPTIMAX"),
                Reflevel.MAX to translate("multiration.niveau.MAX")
        )

internal fun couleurZone(case: CaseEquilibre): Color =
        when (case.zone) {
            ZoneEquilibre.EQUILIBRABLE -> Color(0xFF0CA30C)
            ZoneEquilibre.SOUS_RESERVE -> Color(0xFF0CA30C)
            // Clair = 1 seuil manqué, foncé = 5 et plus
            ZoneEquilibre.SEUILS_NON_RESPECTES ->
                    listOf(Color(0xFFF4A6A0), Color(0xFFE77B72), Color(0xFFD03B3B), Color(0xFFA52626), Color(0xFF7A1717))[
                            ((case.minSeuilsManques ?: 5).coerceIn(1, 5)) - 1]
            ZoneEquilibre.ENERGIE_DEPASSEE -> Color(0xFFEC835A)
            ZoneEquilibre.NON_EVALUABLE -> Color(0xFFB5B5B0)
        }

private fun couleurStatut(statut: StatutScenario): Color =
        when (statut) {
            StatutScenario.CONFORME -> Color(0xFF0CA30C)
            StatutScenario.CONFORME_SOUS_RESERVE -> Color(0xFF0CA30C)
            StatutScenario.SEUILS_NON_RESPECTES -> Color(0xFFD03B3B)
            StatutScenario.ENERGIE_DEPASSEE -> Color(0xFFC0582E)
            else -> Color.Gray
        }

/**
 * Exploration multiration : toutes les rations d'une grille référentiel × poids × K, carte des
 * zones équilibrables et ouverture d'une ration (ou d'une case) dans l'analyse de ration.
 */
@Composable
fun MultiRationExplorerView(
        viewModel: MultiRationViewModel,
        onNavigateBack: () -> Unit,
        modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) { viewModel.chargerReferences() }
    val rationsOuvertes by viewModel.rationsOuvertes.collectAsState()
    if (rationsOuvertes.isNotEmpty()) {
        RationsExplorationView(viewModel, rationsOuvertes, modifier)
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        val courante by viewModel.explorationCourante.collectAsState()
        TopBarSimple(
                title = translate("multiration.titre") + (courante?.let { " — ${it.nom}" } ?: ""),
                onNavigateBack = onNavigateBack
        )
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 4.dp)) {
            PanneauConfiguration(viewModel, Modifier.width(420.dp).fillMaxHeight())
            Spacer(Modifier.width(12.dp))
            PanneauResultats(viewModel, Modifier.weight(1f).fillMaxHeight())
        }
    }
}

// --- Configuration ----------------------------------------------------------------------------

@Composable
private fun PanneauConfiguration(viewModel: MultiRationViewModel, modifier: Modifier) {
    val p by viewModel.parametres.collectAsState()
    val progression by viewModel.progression.collectAsState()
    var onglet by remember { mutableStateOf(0) }
    val problemes = viewModel.problemes(p)

    Card(modifier = modifier, elevation = 2.dp) {
        Column(Modifier.fillMaxSize()) {
            // Bloc de calcul toujours visible : ce qui sera calculé et ce qui le bloque
            Column(Modifier.fillMaxWidth().background(Color(0xFFF5F5F5)).padding(12.dp)) {
                Text(
                        translate("multiration.nbScenarios", viewModel.nombreScenarios(p).toString()),
                        fontWeight = FontWeight.Bold
                )
                Text(
                        translate("multiration.formuleScenarios"),
                        fontSize = 11.sp,
                        color = Color.Gray
                )
                Spacer(Modifier.height(4.dp))
                if (problemes.isEmpty()) {
                    Text(translate("multiration.pret"), color = Color(0xFF3C763D), fontSize = 12.sp)
                } else {
                    problemes.forEach { Text("• $it", color = Color(0xFFA94442), fontSize = 12.sp) }
                }
                Spacer(Modifier.height(8.dp))
                val enCours = progression
                if (enCours == null) {
                    Button(
                            onClick = { viewModel.lancerCalcul() },
                            enabled = problemes.isEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                    ButtonDefaults.buttonColors(
                                            backgroundColor = VetNutriColors.Primary,
                                            contentColor = VetNutriColors.OnPrimary
                                    )
                    ) { Text(translate("multiration.calculerTout")) }
                } else {
                    LinearProgressIndicator(
                            progress = if (enCours.total > 0) enCours.faits.toFloat() / enCours.total else 0f,
                            modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${enCours.faits} / ${enCours.total}", fontSize = 12.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = { viewModel.annulerCalcul() }) { Text(translate("multiration.annuler")) }
                    }
                }
            }
            AdaptiveTabRow(tabCount = 4, selectedTabIndex = onglet, backgroundColor = Color.White) {
                listOf(translate("multiration.onglet.animaux"), translate("multiration.onglet.ingredients"), translate("multiration.onglet.options"), translate("multiration.onglet.enregistrees")).forEachIndexed { i, titre ->
                    Tab(selected = onglet == i, onClick = { onglet = i }, text = { Text(titre, fontSize = 13.sp) })
                }
            }
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)) {
                when (onglet) {
                    0 -> OngletAnimaux(viewModel, p)
                    1 -> OngletIngredients(viewModel, p)
                    2 -> OngletOptions(viewModel, p)
                    else -> OngletEnregistrees(viewModel)
                }
            }
        }
    }
}

@Composable
private fun ChampNombre(label: String, valeur: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val invalide = MultiRationViewModel.nombre(valeur).isNaN()
    OutlinedTextField(
            value = valeur,
            onValueChange = onChange,
            label = { Text(label, fontSize = 11.sp) },
            singleLine = true,
            isError = invalide,
            modifier = modifier
    )
}

/** Champ décimal qui conserve la saisie en cours ; [cle] réinitialise le texte (ex. nutriment changé). */
@Composable
private fun ChampDecimal(label: String, valeur: Double, cle: Any?, onValide: (Double) -> Unit, modifier: Modifier = Modifier) {
    var texte by remember(cle) { mutableStateOf(formaterQuantite(valeur)) }
    val nombre = MultiRationViewModel.nombre(texte)
    OutlinedTextField(
            value = texte,
            onValueChange = {
                texte = it
                val v = MultiRationViewModel.nombre(it)
                if (!v.isNaN() && v >= 0.0) onValide(v)
            },
            label = { Text(label, fontSize = 11.sp) },
            singleLine = true,
            isError = nombre.isNaN() || nombre < 0.0,
            modifier = modifier
    )
}

@Composable
private fun OngletAnimaux(viewModel: MultiRationViewModel, p: ParametresExploration) {
    val toutes by viewModel.references.collectAsState()
    var filtre by remember { mutableStateOf("") }
    val especes = remember(toutes) { viewModel.especesDisponibles() }
    val references = toutes.filter { it.espece == p.espece }

    DropdownField(
            label = translate("multiration.espece"),
            selectedValue = p.espece,
            options = especes.ifEmpty { listOf(p.espece) },
            onValueChange = { viewModel.choisirEspece(it) },
            valueToString = { it.translateEnum() },
            modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    Text(translate("multiration.referentielsGeneraux", p.espece.translateEnum()), fontWeight = FontWeight.Bold)
    Text(
            translate("multiration.referentielsAide"),
            fontSize = 11.sp,
            color = Color.Gray
    )
    OutlinedTextField(
            value = filtre,
            onValueChange = { filtre = it },
            label = { Text(translate("multiration.filtrer"), fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
    )
    val visibles = references.filter { filtre.isBlank() || it.nom.contains(filtre, ignoreCase = true) || it.uuid in p.referenceIds }
    Column(Modifier.fillMaxWidth().heightIn(max = 260.dp).verticalScroll(rememberScrollState())) {
        visibles.forEach { ref ->
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.basculerReference(ref.uuid) }
            ) {
                Checkbox(checked = ref.uuid in p.referenceIds, onCheckedChange = { viewModel.basculerReference(ref.uuid) })
                Text("${ref.nom} — ${ref.stadePhysio.label}", fontSize = 13.sp)
            }
        }
        if (toutes.isEmpty()) Text(translate("multiration.chargementReferentiels"), fontSize = 12.sp, color = Color.Gray)
        else if (references.isEmpty()) Text(translate("multiration.aucunReferentiel"), fontSize = 12.sp, color = Color.Gray)
    }

    Spacer(Modifier.height(12.dp))
    Text(translate("multiration.poidsKg"), fontWeight = FontWeight.Bold)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        ChampNombre(translate("multiration.de"), p.poidsDe, { v -> viewModel.modifierParametres { it.copy(poidsDe = v) } }, Modifier.weight(1f))
        ChampNombre(translate("multiration.a"), p.poidsA, { v -> viewModel.modifierParametres { it.copy(poidsA = v) } }, Modifier.weight(1f))
        ChampNombre(translate("multiration.pas"), p.poidsPas, { v -> viewModel.modifierParametres { it.copy(poidsPas = v) } }, Modifier.weight(1f))
    }
    Spacer(Modifier.height(8.dp))
    Text(translate("multiration.coefficientK"), fontWeight = FontWeight.Bold)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        ChampNombre(translate("multiration.de"), p.kDe, { v -> viewModel.modifierParametres { it.copy(kDe = v) } }, Modifier.weight(1f))
        ChampNombre(translate("multiration.a"), p.kA, { v -> viewModel.modifierParametres { it.copy(kA = v) } }, Modifier.weight(1f))
        ChampNombre(translate("multiration.pas"), p.kPas, { v -> viewModel.modifierParametres { it.copy(kPas = v) } }, Modifier.weight(1f))
    }
    val (poids, k) = viewModel.grille(p)
    if (poids != null && k != null) {
        Text(
                translate("multiration.grille", poids.size.toString(), "${poids.take(6).joinToString { formaterQuantite(it) }}${if (poids.size > 6) ", …" else ""}", k.size.toString(), "${k.take(6).joinToString { formaterQuantite(it) }}${if (k.size > 6) ", …" else ""}"),
                fontSize = 12.sp
        )
    }
    Text(
            translate("multiration.besoinTotalAide"),
            fontSize = 11.sp,
            color = Color.Gray
    )
}

@Composable
private fun OngletIngredients(viewModel: MultiRationViewModel, p: ParametresExploration) {
    var roleAjout by remember { mutableStateOf<RoleExploration?>(null) }
    var ouvert by remember { mutableStateOf(RoleExploration.PROTEINES) }

    Text(
            translate("multiration.combinaisonAide"),
            fontSize = 11.sp,
            color = Color.Gray
    )
    Spacer(Modifier.height(6.dp))
    RoleExploration.ordonner(p.listes).forEachIndexed { i, role ->
        val liste = p.listes[role].orEmpty()
        val ignore = !role.estEnergie && (p.cibles[role]?.nutriment ?: role.nutrimentParDefaut).label in p.besoinsIgnores
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), elevation = 1.dp) {
            Column(Modifier.padding(8.dp)) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { ouvert = role }
                ) {
                    Text("${i + 1}. ${if (role.supplementaire) nomTraduitNutriment((p.cibles[role] ?: CibleExploration(role.nutrimentParDefaut)).nutriment) else role.libelleTraduit()}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(
                            if (ignore) translate("multiration.besoinIgnore") else if (liste.isEmpty()) translate("multiration.vide") else translate("multiration.nbAliments", liste.size.toString()),
                            color = if (liste.isEmpty() && !ignore) Color(0xFFA94442) else Color.Gray,
                            fontSize = 12.sp
                    )
                    Icon(if (ouvert == role) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
                }
                if (ouvert == role) {
                    liste.forEach { aliment ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(aliment.nom ?: aliment.uuid, fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 2)
                            IconButton(onClick = { viewModel.retirerAliment(role, aliment.uuid) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, contentDescription = translate("multiration.retirer"), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    TextButton(onClick = { roleAjout = role }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(" " + translate("multiration.ajouterAliments"), fontSize = 12.sp)
                    }
                    if (!role.estEnergie) {
                        val cible = p.cibles[role] ?: CibleExploration(role.nutrimentParDefaut)
                        if (role == RoleExploration.FIBRES || role.supplementaire) {
                            DropdownField(
                                    label = translate("multiration.nutrimentCible"),
                                    selectedValue = cible.nutriment,
                                    options = if (role == RoleExploration.FIBRES) RoleExploration.nutrimentsFibres else
                                            fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.getAllNutrientLabels()
                                                    .mapNotNull { fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(it) }
                                                    .filter { it != fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.ENERGIE }
                                                    .sortedBy { nomTraduitNutriment(it) },
                                    onValueChange = { viewModel.modifierNutrimentCible(role, it) },
                                    valueToString = { nomTraduitNutriment(it) },
                                    modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                            DropdownField(
                                    label = translate("multiration.cibleNiveau"),
                                    selectedValue = niveauxCible.firstOrNull { it.first == cible.niveau },
                                    options = niveauxCible,
                                    onValueChange = { viewModel.modifierNiveauCible(role, it.first) },
                                    valueToString = { it.second },
                                    modifier = Modifier.weight(1f)
                            )
                            ChampDecimal(
                                    translate("multiration.facteur"),
                                    cible.facteur,
                                    cle = cible.nutriment.label,
                                    onValide = { f -> viewModel.modifierFacteurCible(role, f) },
                                    modifier = Modifier.width(90.dp)
                            )
                        }
                        if (role == RoleExploration.FIBRES)
                                Text(
                                        translate("multiration.celluloseAide"),
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                )
                    } else {
                        Text(
                                translate("multiration.energieAide"),
                                fontSize = 11.sp,
                                color = Color.Gray
                        )
                    }
                    if (role == RoleExploration.SODIUM || role.supplementaire) {
                        Text(translate("multiration.selAide"), fontSize = 11.sp, color = Color.Gray)
                    }
                    if (role.supplementaire) {
                        TextButton(onClick = { viewModel.retirerListe(role) }) { Text(translate("multiration.supprimerListe")) }
                    }
                    if (p.arrondir) {
                        ChampDecimal(
                                translate("multiration.pasManuel"),
                                p.pasArrondiManuel[role] ?: 0.0,
                                cle = "pas-${role.name}",
                                onValide = { pas -> viewModel.modifierPasArrondi(role, pas) },
                                modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                                translate("multiration.pasManuelAide"),
                                fontSize = 11.sp,
                                color = Color.Gray
                        )
                        ChampDecimal(
                                translate("multiration.doseMinimale"),
                                p.doseMinimale[role] ?: role.doseParDefaut,
                                cle = role,
                                onValide = { d -> viewModel.modifierDoseMinimale(role, d) },
                                modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    TextButton(onClick = { ouvert = viewModel.ajouterListe() }) {
        Icon(Icons.Default.Add, contentDescription = null)
        Text(translate("multiration.ajouterListe"))
    }
    roleAjout?.let { role -> DialogueAjoutAliments(viewModel, role, onDismiss = { roleAjout = null }) }
}

/** Choix des aliments d'une liste, avec le composant de recherche d'aliments de l'application. */
@Composable
private fun DialogueAjoutAliments(viewModel: MultiRationViewModel, role: RoleExploration, onDismiss: () -> Unit) {
    val aliments by viewModel.aliments.collectAsState(initial = emptyList())
    val p by viewModel.parametres.collectAsState()
    // Aliments de l'espèce explorée par défaut (modifiable dans les filtres du composant)
    var filtres by remember { mutableStateOf(FoodSearchFilters(selectedEspece = p.espece)) }
    val dejaChoisis = p.listes[role].orEmpty()

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(modifier = Modifier.fillMaxWidth(0.85f).fillMaxHeight(0.85f), elevation = 8.dp) {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Text(translate("multiration.ajouterAlimentsRole", role.libelleTraduit()), style = MaterialTheme.typography.h6)
                Text(
                        translate("multiration.cliquerAjoute", dejaChoisis.size.toString()),
                        fontSize = 12.sp,
                        color = Color.Gray
                )
                if (dejaChoisis.isNotEmpty())
                        Text(
                                dejaChoisis.joinToString(" · ") { it.nom ?: it.uuid },
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                        )
                Spacer(Modifier.height(8.dp))
                FoodSearchComponent(
                        foods = aliments,
                        filters = filtres,
                        onFiltersChange = { filtres = it },
                        config =
                                FoodSearchConfig(
                                        layout = FoodSearchLayout.VERTICAL,
                                        onFoodSelected = { viewModel.ajouterAliment(role, it) },
                                        isLoading = aliments.isEmpty()
                                ),
                        modifier = Modifier.weight(1f).fillMaxWidth()
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss) { Text(translate("multiration.fermer")) }
                }
            }
        }
    }
}

@Composable
private fun OngletOptions(viewModel: MultiRationViewModel, p: ParametresExploration) {
    @Composable
    fun Option(texte: String, coche: Boolean, onChange: (Boolean) -> Unit) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onChange(!coche) }) {
            Checkbox(checked = coche, onCheckedChange = onChange)
            Text(texte, fontSize = 13.sp)
        }
    }
    Option(translate("multiration.arrondir"), p.arrondir) { v -> viewModel.modifierParametres { it.copy(arrondir = v) } }
    if (p.arrondir) {
        Option(translate("multiration.arrondirSup"), p.arrondirAuSuperieur) { v ->
            viewModel.modifierParametres { it.copy(arrondirAuSuperieur = v) }
        }
    }
    Text(
            translate("multiration.arrondiAide") + " " +
                    (if (p.arrondirAuSuperieur) translate("multiration.arrondiSupAide")
                    else translate("multiration.arrondiProcheAide")),
            fontSize = 11.sp,
            color = Color.Gray
    )
    Option(translate("multiration.ignorerOptimax"), p.ignorerOptimax) { v ->
        viewModel.modifierParametres { it.copy(ignorerOptimax = v) }
    }
    Text(
            translate("multiration.valeursAbsentesAide"),
            fontSize = 11.sp,
            color = Color.Gray
    )
    Spacer(Modifier.height(8.dp))
    Text(translate("multiration.besoins"), fontWeight = FontWeight.Bold)
    Text(translate("multiration.besoinsAide"), fontSize = 11.sp, color = Color.Gray)
    val references by viewModel.references.collectAsState()
    val besoins = (references.filter { it.uuid in p.referenceIds }.flatMap {
        (it.getRefMapMin().keys + it.getRefMapOMin().keys + it.getRefMapMax().keys + it.getRefMapOMax().keys).toList()
    } + p.cibles.values.map { it.nutriment } +
            p.besoinsIgnores.mapNotNull { fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(it) })
            .filter { it != fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.ENERGIE }
            .distinctBy { it.label }.sortedBy { nomTraduitNutriment(it) }
    besoins.forEach { n ->
        Option(nomTraduitNutriment(n), n.label !in p.besoinsIgnores) { actif ->
            viewModel.modifierParametres {
                it.copy(besoinsIgnores = if (actif) it.besoinsIgnores - n.label else it.besoinsIgnores + n.label)
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    ChampNombre(translate("multiration.maxScenarios"), p.maxScenarios, { v -> viewModel.modifierParametres { it.copy(maxScenarios = v) } }, Modifier.fillMaxWidth())
}

/** Configurations enregistrées en base : enregistrer, recharger, supprimer. */
@Composable
private fun OngletEnregistrees(viewModel: MultiRationViewModel) {
    val explorations by viewModel.explorations.collectAsState()
    val courante by viewModel.explorationCourante.collectAsState()
    var nom by remember(courante?.uuid) { mutableStateOf(courante?.nom ?: "") }
    var aSupprimer by remember { mutableStateOf<String?>(null) }

    Text(translate("multiration.enregistrerConfig"), fontWeight = FontWeight.Bold)
    Text(
            translate("multiration.enregistrerAide"),
            fontSize = 11.sp,
            color = Color.Gray
    )
    OutlinedTextField(
            value = nom,
            onValueChange = { nom = it },
            label = { Text(translate("multiration.nom"), fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
    )
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
        Button(
                onClick = { viewModel.enregistrerExploration(nom) },
                enabled = nom.isNotBlank(),
                colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Primary, contentColor = VetNutriColors.OnPrimary)
        ) { Text(if (courante != null) translate("multiration.mettreAJour") else translate("multiration.enregistrer"), fontSize = 12.sp) }
        if (courante != null) {
            OutlinedButton(onClick = { viewModel.enregistrerExploration(nom, commeNouvelle = true) }, enabled = nom.isNotBlank()) {
                Text(translate("multiration.enregistrerNouvelle"), fontSize = 12.sp)
            }
        }
    }
    TextButton(onClick = { viewModel.nouvelleExploration() }) { Text(translate("multiration.nouvelle"), fontSize = 12.sp) }

    Divider(Modifier.padding(vertical = 8.dp))
    Text(translate("multiration.explorationsEnregistrees"), fontWeight = FontWeight.Bold)
    if (explorations.isEmpty()) Text(translate("multiration.aucuneExploration"), fontSize = 12.sp, color = Color.Gray)
    explorations.forEach { e ->
        val espece = Espece.entries.firstOrNull { it.name == e.espece }
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                        Modifier.fillMaxWidth()
                                .background(if (e.uuid == courante?.uuid) Color(0xFFE0F2F1) else Color.Transparent)
                                .padding(vertical = 2.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(e.nom, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(espece?.translateEnum() ?: e.espece, fontSize = 11.sp, color = Color.Gray)
            }
            TextButton(onClick = { viewModel.chargerExploration(e.uuid) }) { Text(translate("multiration.charger"), fontSize = 12.sp) }
            IconButton(onClick = { aSupprimer = e.uuid }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = translate("multiration.supprimer"), modifier = Modifier.size(16.dp))
            }
        }
    }

    aSupprimer?.let { uuid ->
        val e = explorations.firstOrNull { it.uuid == uuid }
        AlertDialog(
                onDismissRequest = { aSupprimer = null },
                title = { Text(translate("multiration.supprimerTitre")) },
                text = { Text(translate("multiration.supprimerTexte", e?.nom ?: "")) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.supprimerExploration(uuid)
                        aSupprimer = null
                    }) { Text(translate("multiration.supprimer")) }
                },
                dismissButton = { TextButton(onClick = { aSupprimer = null }) { Text(translate("multiration.annuler")) } }
        )
    }
}

// --- Résultats --------------------------------------------------------------------------------

@Composable
private fun PanneauResultats(
        viewModel: MultiRationViewModel,
        modifier: Modifier
) {
    val resultat by viewModel.resultat.collectAsState()
    val p by viewModel.parametres.collectAsState()
    val parametresDuResultat by viewModel.parametresDuResultat.collectAsState()
    val erreur by viewModel.erreur.collectAsState()
    val message by viewModel.message.collectAsState()
    val caseSelectionnee by viewModel.caseSelectionnee.collectAsState()
    val scope = rememberCoroutineScope()

    Column(modifier.verticalScroll(rememberScrollState())) {
        erreur?.let {
            Bandeau(it, Color(0xFFF2DEDE), Color(0xFFA94442)) { viewModel.effacerErreur() }
        }
        message?.let {
            Bandeau(it, Color(0xFFDFF0D8), Color(0xFF3C763D)) { viewModel.effacerMessage() }
        }
        val r = resultat
        if (r == null) {
            Bandeau(
                    translate("multiration.introResultats"),
                    Color(0xFFD9EDF7),
                    Color(0xFF31708F)
            )
            return@Column
        }
        if (parametresDuResultat != null && parametresDuResultat != p) {
            Bandeau(
                    translate("multiration.parametresModifies"),
                    Color(0xFFFCF8E3),
                    Color(0xFF8A6D3B)
            )
        }
        if (r.configuration.besoinsIgnores.isNotEmpty()) {
            val noms = r.configuration.besoinsIgnores.map { label ->
                fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(label)?.let { nomTraduitNutriment(it) } ?: label
            }
            Text(translate("multiration.besoinsIgnores", noms.joinToString()), fontSize = 12.sp)
        }
        val conformes = r.scenarios.count {
            it.statut == StatutScenario.CONFORME || it.statut == StatutScenario.CONFORME_SOUS_RESERVE
        }
        Text(
                translate("multiration.bilanScenarios", r.scenarios.size.toString(), conformes.toString()),
                style = MaterialTheme.typography.h6
        )
        Text(
                translate("multiration.cliquerCase"),
                fontSize = 12.sp,
                color = Color.Gray
        )
        Spacer(Modifier.height(8.dp))
        LegendeZones()
        r.cases.groupBy { it.reference.uuid }.values.forEach { cases ->
            CarteReference(cases, caseSelectionnee) { viewModel.selectionnerCase(it) }
        }
        Text(
                translate("multiration.equilibrableAide"),
                fontSize = 11.sp,
                color = Color.Gray
        )
        if (isCsvFileOperationsSupported()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                OutlinedButton(onClick = { scope.launch { saveCsvFileForExport(viewModel.csvScenarios(), "exploration-scenarios.csv") } }) {
                    Text(translate("multiration.exporterScenarios"), fontSize = 12.sp)
                }
                OutlinedButton(onClick = { scope.launch { saveCsvFileForExport(viewModel.csvCases(), "exploration-carte.csv") } }) {
                    Text(translate("multiration.exporterCarte"), fontSize = 12.sp)
                }
            }
        }
        CourbesMultiration(r)
        caseSelectionnee?.let { case -> DetailCase(viewModel, case) }
    }
}

@Composable
private fun Bandeau(texte: String, fond: Color, couleur: Color, onClose: (() -> Unit)? = null) {
    Row(
            Modifier.fillMaxWidth().padding(vertical = 4.dp).background(fond, RoundedCornerShape(4.dp)).padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(texte, color = couleur, fontSize = 13.sp, modifier = Modifier.weight(1f))
        if (onClose != null)
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = translate("multiration.fermer"), tint = couleur)
                }
    }
}

@Composable
private fun LegendeZones() {
    val exemples =
            listOf(
                    ZoneEquilibre.EQUILIBRABLE to Color(0xFF0CA30C),
                    ZoneEquilibre.SOUS_RESERVE to Color(0xFF0CA30C),
                    ZoneEquilibre.SEUILS_NON_RESPECTES to Color(0xFFD03B3B),
                    ZoneEquilibre.ENERGIE_DEPASSEE to Color(0xFFEC835A),
                    ZoneEquilibre.NON_EVALUABLE to Color(0xFFB5B5B0)
            )
    Column(Modifier.padding(bottom = 8.dp)) {
        exemples.forEach { (zone, couleur) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).background(couleur))
                Text(
                        " ${if (zone == ZoneEquilibre.SOUS_RESERVE) translate("multiration.legendeSousReserve") else zone.libelleTraduit()}${if (zone == ZoneEquilibre.SEUILS_NON_RESPECTES) " " + translate("multiration.legendeNuances") else ""}",
                        fontSize = 11.sp
                )
            }
        }
    }
}

/** Carte poids × K d'un référentiel : K en ordonnée (croissant vers le haut), poids en abscisse. */
@Composable
private fun CarteReference(cases: List<CaseEquilibre>, selection: CaseEquilibre?, onClick: (CaseEquilibre) -> Unit) {
    val poids = cases.map { it.poids }.distinct().sorted()
    val ks = cases.map { it.k }.distinct().sortedDescending()
    val parCle = cases.associateBy { it.poids to it.k }
    Text(cases.first().reference.nom, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
    Row(Modifier.horizontalScroll(rememberScrollState())) {
        Column {
            ks.forEach { k ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("K ${formaterQuantite(k)}", fontSize = 11.sp, modifier = Modifier.width(52.dp))
                    poids.forEach { w ->
                        val case = parCle[w to k]
                        val choisie = case != null && selection != null && case.reference.uuid == selection.reference.uuid &&
                                case.poids == selection.poids && case.k == selection.k
                        Box(
                                contentAlignment = Alignment.Center,
                                modifier =
                                        Modifier.size(width = 60.dp, height = 34.dp)
                                                .padding(1.dp)
                                                .background(case?.let { couleurZone(it) } ?: Color.LightGray)
                                                .then(if (choisie) Modifier.border(BorderStroke(3.dp, Color.Black)) else Modifier)
                                                .clickable(enabled = case != null) { case?.let(onClick) }
                        ) {
                            if (case != null) {
                                val texte =
                                        when (case.zone) {
                                            ZoneEquilibre.EQUILIBRABLE -> "${case.conformes + case.sousReserve}/${case.combinaisons}"
                                            ZoneEquilibre.SOUS_RESERVE -> "${case.sousReserve}/${case.combinaisons}"
                                            else -> case.minSeuilsManques?.toString() ?: "–"
                                        }
                                Text(texte, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
            Row {
                Spacer(Modifier.width(52.dp))
                poids.forEach { w ->
                    Text("${formaterQuantite(w)} kg", fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.width(60.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailCase(viewModel: MultiRationViewModel, case: CaseEquilibre) {
    val scenario by viewModel.scenarioSelectionne.collectAsState()

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), elevation = 2.dp) {
        Column(Modifier.padding(12.dp)) {
            Text(
                    "${case.reference.nom} — ${formaterQuantite(case.poids)} kg — K ${formaterQuantite(case.k)}",
                    style = MaterialTheme.typography.h6
            )
            Text(translate("multiration.detailCase", case.zone.libelleTraduit(), "${case.conformes + case.sousReserve}", case.combinaisons.toString()), fontSize = 13.sp)
            if (case.seuilsLimitants.isNotEmpty()) Text(translate("multiration.seuilsLimitants", case.seuilsLimitants.toString()), fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                        onClick = { scenario?.let { viewModel.ouvrirDansAnalyse(listOf(it)) } },
                        enabled = scenario != null,
                        colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Primary, contentColor = VetNutriColors.OnPrimary)
                ) { Text(translate("multiration.analyserEditer")) }
                OutlinedButton(
                        onClick = { viewModel.ouvrirDansAnalyse(case.scenarios.take(MAX_RATIONS_PAR_CASE)) },
                        enabled = case.scenarios.isNotEmpty()
                ) {
                    Text(translate("multiration.ouvrirCase", minOf(case.scenarios.size, MAX_RATIONS_PAR_CASE).toString()))
                }
            }
            Text(
                    translate("multiration.ouvrirAide"),
                    fontSize = 11.sp,
                    color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            Text(translate("multiration.combinaisonsCase"), fontWeight = FontWeight.Bold)
            case.scenarios.forEach { s ->
                LigneScenario(s, choisi = s.id == scenario?.id) { viewModel.selectionnerScenario(s) }
            }
            scenario?.takeIf { s -> case.scenarios.any { it.id == s.id } }?.let { DetailScenario(it) }
        }
    }
}

@Composable
private fun LigneScenario(s: ScenarioExploration, choisi: Boolean, onClick: () -> Unit) {
    Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                    Modifier.fillMaxWidth()
                            .background(if (choisi) Color(0xFFE0F2F1) else Color.Transparent)
                            .clickable(onClick = onClick)
                            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        Text(s.id, fontSize = 12.sp, modifier = Modifier.width(72.dp))
        Text(s.statut.libelleTraduit(), fontSize = 12.sp, color = couleurStatut(s.statut), modifier = Modifier.width(170.dp))
        Text(
                s.composition.ifEmpty { s.message },
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
        )
        Text(
                if (s.seuilsManques.isEmpty()) "" else translate("multiration.nbSeuils", s.seuilsManques.size.toString()),
                fontSize = 12.sp,
                modifier = Modifier.width(80.dp),
                textAlign = TextAlign.End
        )
    }
}

@Composable
private fun DetailScenario(s: ScenarioExploration) {
    Divider(Modifier.padding(vertical = 8.dp))
    Text(translate("multiration.detail", s.id.toString()), fontWeight = FontWeight.Bold)
    s.ration.alimentMutableList.forEachIndexed { i, a ->
        Row {
            Text(s.roles.getOrNull(i)?.libelleTraduit() ?: "", fontSize = 12.sp, modifier = Modifier.width(130.dp))
            Text(a.aliment?.nom ?: "?", fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text("${formaterQuantite(a.quantite)} g", fontSize = 12.sp, modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
        }
    }
    val energie = s.energie
    val besoin = s.besoinTotal
    if (energie != null && besoin != null && besoin > 0.0) {
        Text(
                translate("multiration.energieBilan", energie.toInt().toString(), besoin.toInt().toString(), "${if (energie >= besoin) "+" else ""}${formaterQuantite(100.0 * (energie - besoin) / besoin)}"),
                fontSize = 12.sp
        )
    }
    if (s.seuilsManques.isNotEmpty()) Text(translate("multiration.seuilsNonRespectes", s.seuilsManques.joinToString()), fontSize = 12.sp, color = Color(0xFFA94442))
    if (s.seuilsNonRenseignes.isNotEmpty())
            Text(translate("multiration.echecsDonnees", s.seuilsNonRenseignes.joinToString()), fontSize = 12.sp, color = Color(0xFF8A6D3B))
    if (s.message.isNotEmpty()) Text(s.message, fontSize = 12.sp, color = Color.Gray)
}

/** Libellés des énumérations du moteur, dans la langue de l'interface. */
private fun RoleExploration.libelleTraduit(): String =
        translate("multiration.role." + if (supplementaire) "SUPPLEMENT" else name)

private fun ZoneEquilibre.libelleTraduit(): String = translate("multiration.zone.$name")

private fun StatutScenario.libelleTraduit(): String = translate("multiration.statut.$name")
