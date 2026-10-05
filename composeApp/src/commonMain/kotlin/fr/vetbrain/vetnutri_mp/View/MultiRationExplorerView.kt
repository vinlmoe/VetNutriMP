package fr.vetbrain.vetnutri_mp.View

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
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.CaseEquilibre
import fr.vetbrain.vetnutri_mp.Data.CibleExploration
import fr.vetbrain.vetnutri_mp.Data.FoodSearchFilters
import fr.vetbrain.vetnutri_mp.Data.ResultatExploration
import fr.vetbrain.vetnutri_mp.Data.RoleExploration
import fr.vetbrain.vetnutri_mp.Data.ScenarioExploration
import fr.vetbrain.vetnutri_mp.Data.StatutScenario
import fr.vetbrain.vetnutri_mp.Data.ZoneEquilibre
import fr.vetbrain.vetnutri_mp.Data.formaterQuantite
import fr.vetbrain.vetnutri_mp.Data.nomTraduitNutriment
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.ExcelPlatform.isCsvFileOperationsSupported
import fr.vetbrain.vetnutri_mp.ExcelPlatform.saveCsvFileForExport
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.View.Components.FoodSearchComponent
import fr.vetbrain.vetnutri_mp.View.Components.FoodSearchConfig
import fr.vetbrain.vetnutri_mp.View.Components.FoodSearchLayout
import fr.vetbrain.vetnutri_mp.ViewModel.MultiRationViewModel
import kotlinx.coroutines.launch

/** Nombre maximal de rations d'une case ouvertes ensemble dans une consultation. */
private const val MAX_RATIONS_PAR_CASE = 20

private val niveauxCible =
        listOf(
                Reflevel.OPTIMIN to "Optimum minimum",
                Reflevel.MIN to "Minimum",
                Reflevel.OPTIMAX to "Optimum maximum",
                Reflevel.MAX to "Maximum"
        )

internal fun couleurZone(case: CaseEquilibre): Color =
        when (case.zone) {
            ZoneEquilibre.EQUILIBRABLE -> Color(0xFF0CA30C)
            ZoneEquilibre.SOUS_RESERVE -> Color(0xFFFAB219)
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
            StatutScenario.CONFORME_SOUS_RESERVE -> Color(0xFFB07A00)
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
        onOuvrirAnalyse: (AnimalEv, String) -> Unit,
        modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) { viewModel.chargerReferences() }

    Column(modifier = modifier.fillMaxSize()) {
        TopBarSimple(title = "Exploration multiration", onNavigateBack = onNavigateBack)
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 4.dp)) {
            PanneauConfiguration(viewModel, Modifier.width(420.dp).fillMaxHeight())
            Spacer(Modifier.width(12.dp))
            PanneauResultats(viewModel, onOuvrirAnalyse, Modifier.weight(1f).fillMaxHeight())
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
                        "${viewModel.nombreScenarios(p)} scénarios",
                        fontWeight = FontWeight.Bold
                )
                Text(
                        "= combinaisons × poids × K × référentiels",
                        fontSize = 11.sp,
                        color = Color.Gray
                )
                Spacer(Modifier.height(4.dp))
                if (problemes.isEmpty()) {
                    Text("✓ Prêt à calculer.", color = Color(0xFF3C763D), fontSize = 12.sp)
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
                    ) { Text("Calculer toutes les rations") }
                } else {
                    LinearProgressIndicator(
                            progress = if (enCours.total > 0) enCours.faits.toFloat() / enCours.total else 0f,
                            modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${enCours.faits} / ${enCours.total}", fontSize = 12.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = { viewModel.annulerCalcul() }) { Text("Annuler") }
                    }
                }
            }
            TabRow(selectedTabIndex = onglet, backgroundColor = Color.White) {
                listOf("Chiens", "Ingrédients", "Options").forEachIndexed { i, titre ->
                    Tab(selected = onglet == i, onClick = { onglet = i }, text = { Text(titre, fontSize = 13.sp) })
                }
            }
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)) {
                when (onglet) {
                    0 -> OngletChiens(viewModel, p)
                    1 -> OngletIngredients(viewModel, p)
                    else -> OngletOptions(viewModel, p)
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
private fun OngletChiens(viewModel: MultiRationViewModel, p: MultiRationViewModel.Parametres) {
    val references by viewModel.references.collectAsState()
    var filtre by remember { mutableStateOf("") }

    Text("Référentiels généraux", fontWeight = FontWeight.Bold)
    Text(
            "Chaque référentiel est croisé avec tous les poids : aucun profil physiologique n'est déduit du poids.",
            fontSize = 11.sp,
            color = Color.Gray
    )
    OutlinedTextField(
            value = filtre,
            onValueChange = { filtre = it },
            label = { Text("Filtrer", fontSize = 11.sp) },
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
                Text("${ref.nom} — ${ref.stadePhysio.label} (${ref.espece.name.lowercase()})", fontSize = 13.sp)
            }
        }
        if (references.isEmpty()) Text("Chargement des référentiels…", fontSize = 12.sp, color = Color.Gray)
    }

    Spacer(Modifier.height(12.dp))
    Text("Poids (kg)", fontWeight = FontWeight.Bold)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        ChampNombre("de", p.poidsDe, { v -> viewModel.modifierParametres { it.copy(poidsDe = v) } }, Modifier.weight(1f))
        ChampNombre("à", p.poidsA, { v -> viewModel.modifierParametres { it.copy(poidsA = v) } }, Modifier.weight(1f))
        ChampNombre("pas", p.poidsPas, { v -> viewModel.modifierParametres { it.copy(poidsPas = v) } }, Modifier.weight(1f))
    }
    Spacer(Modifier.height(8.dp))
    Text("Coefficient K", fontWeight = FontWeight.Bold)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        ChampNombre("de", p.kDe, { v -> viewModel.modifierParametres { it.copy(kDe = v) } }, Modifier.weight(1f))
        ChampNombre("à", p.kA, { v -> viewModel.modifierParametres { it.copy(kA = v) } }, Modifier.weight(1f))
        ChampNombre("pas", p.kPas, { v -> viewModel.modifierParametres { it.copy(kPas = v) } }, Modifier.weight(1f))
    }
    val (poids, k) = viewModel.grille(p)
    if (poids != null && k != null) {
        Text(
                "${poids.size} poids (${poids.take(6).joinToString { formaterQuantite(it) }}${if (poids.size > 6) ", …" else ""}) × " +
                        "${k.size} valeurs de K (${k.take(6).joinToString { formaterQuantite(it) }}${if (k.size > 6) ", …" else ""})",
                fontSize = 12.sp
        )
    }
    Text(
            "Besoin énergétique total = besoin standard × K (enregistré comme coefficient d'ajustement à l'ouverture d'une ration). " +
                    "Les seuils par 1000 kcal restent basés sur le besoin standard, comme dans l'analyse de ration.",
            fontSize = 11.sp,
            color = Color.Gray
    )
}

@Composable
private fun OngletIngredients(viewModel: MultiRationViewModel, p: MultiRationViewModel.Parametres) {
    var roleAjout by remember { mutableStateOf<RoleExploration?>(null) }
    var ouvert by remember { mutableStateOf(RoleExploration.PROTEINES) }

    Text(
            "Une combinaison = un aliment de chaque liste. Ajustements successifs dans cet ordre ; l'énergie est complétée en dernier.",
            fontSize = 11.sp,
            color = Color.Gray
    )
    Spacer(Modifier.height(6.dp))
    RoleExploration.entries.forEachIndexed { i, role ->
        val liste = p.listes[role].orEmpty()
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), elevation = 1.dp) {
            Column(Modifier.padding(8.dp)) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { ouvert = role }
                ) {
                    Text("${i + 1}. ${role.libelle}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(
                            if (liste.isEmpty()) "(vide)" else "(${liste.size} aliment${if (liste.size > 1) "s" else ""})",
                            color = if (liste.isEmpty()) Color(0xFFA94442) else Color.Gray,
                            fontSize = 12.sp
                    )
                    Icon(if (ouvert == role) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
                }
                if (ouvert == role) {
                    liste.forEach { aliment ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(aliment.nom ?: aliment.uuid, fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 2)
                            IconButton(onClick = { viewModel.retirerAliment(role, aliment.uuid) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Retirer", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    TextButton(onClick = { roleAjout = role }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(" Ajouter des aliments", fontSize = 12.sp)
                    }
                    if (!role.estEnergie) {
                        val cible = p.cibles[role] ?: CibleExploration(role.nutrimentParDefaut)
                        if (role == RoleExploration.FIBRES) {
                            DropdownField(
                                    label = "Nutriment des fibres",
                                    selectedValue = cible.nutriment,
                                    options = RoleExploration.nutrimentsFibres,
                                    onValueChange = { viewModel.modifierNutrimentCible(role, it) },
                                    valueToString = { nomTraduitNutriment(it) },
                                    modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                            DropdownField(
                                    label = "Cible (niveau du référentiel)",
                                    selectedValue = niveauxCible.firstOrNull { it.first == cible.niveau },
                                    options = niveauxCible,
                                    onValueChange = { viewModel.modifierNiveauCible(role, it.first) },
                                    valueToString = { it.second },
                                    modifier = Modifier.weight(1f)
                            )
                            ChampDecimal(
                                    "Facteur ×",
                                    cible.facteur,
                                    cle = cible.nutriment.label,
                                    onValide = { f -> viewModel.modifierFacteurCible(role, f) },
                                    modifier = Modifier.width(90.dp)
                            )
                        }
                        if (role == RoleExploration.FIBRES)
                                Text(
                                        "Pour la cellulose, l'ajustement vise 5 × le seuil, comme l'ajustement multi-nutriments. La conformité reste évaluée sur les seuils du référentiel.",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                )
                    } else {
                        Text(
                                "Quantité calculée en dernier pour compléter l'énergie. Un excédent énergétique est signalé, sans quantité négative.",
                                fontSize = 11.sp,
                                color = Color.Gray
                        )
                    }
                    if (p.arrondir) {
                        ChampDecimal(
                                "Dose minimale si utilisé (g)",
                                p.doseMinimale[role] ?: 5.0,
                                cle = role,
                                onValide = { d -> viewModel.modifierDoseMinimale(role, d) },
                                modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    roleAjout?.let { role -> DialogueAjoutAliments(viewModel, role, onDismiss = { roleAjout = null }) }
}

/** Choix des aliments d'une liste, avec le composant de recherche d'aliments de l'application. */
@Composable
private fun DialogueAjoutAliments(viewModel: MultiRationViewModel, role: RoleExploration, onDismiss: () -> Unit) {
    val aliments by viewModel.aliments.collectAsState(initial = emptyList())
    val p by viewModel.parametres.collectAsState()
    var filtres by remember { mutableStateOf(FoodSearchFilters()) }
    val dejaChoisis = p.listes[role].orEmpty()

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(modifier = Modifier.fillMaxWidth(0.85f).fillMaxHeight(0.85f), elevation = 8.dp) {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Text("Ajouter des aliments — ${role.libelle}", style = MaterialTheme.typography.h6)
                Text(
                        "Cliquer sur un aliment l'ajoute à la liste (${dejaChoisis.size} choisi${if (dejaChoisis.size > 1) "s" else ""}).",
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
                    Button(onClick = onDismiss) { Text("Fermer") }
                }
            }
        }
    }
}

@Composable
private fun OngletOptions(viewModel: MultiRationViewModel, p: MultiRationViewModel.Parametres) {
    @Composable
    fun Option(texte: String, coche: Boolean, onChange: (Boolean) -> Unit) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onChange(!coche) }) {
            Checkbox(checked = coche, onCheckedChange = onChange)
            Text(texte, fontSize = 13.sp)
        }
    }
    Option("Arrondir les quantités comme l'application", p.arrondir) { v -> viewModel.modifierParametres { it.copy(arrondir = v) } }
    Text(
            "Arrondi après chaque ajustement : ½ contenant (dosette, sachet, boîte), sinon 1 g sous 20 g, 5 g sous 200 g, 25 g au-delà. " +
                    "Sous la dose minimale de sa liste, un aliment n'est pas utilisé (moins de la moitié) ou est porté à cette dose. " +
                    "L'énergie est acceptée à ± ½ pas de l'aliment énergétique.",
            fontSize = 11.sp,
            color = Color.Gray
    )
    Option("Ignorer les OPTIMAX : seuls les MAX limitent les apports", p.ignorerOptimax) { v ->
        viewModel.modifierParametres { it.copy(ignorerOptimax = v) }
    }
    Text(
            "Les valeurs absentes d'un aliment comptent pour 0, comme dans l'analyse de ration. Un scénario dont seuls des nutriments " +
                    "non renseignés échouent est « conforme sous réserve ».",
            fontSize = 11.sp,
            color = Color.Gray
    )
    Spacer(Modifier.height(8.dp))
    ChampNombre("Nombre maximal de scénarios", p.maxScenarios, { v -> viewModel.modifierParametres { it.copy(maxScenarios = v) } }, Modifier.fillMaxWidth())
}

// --- Résultats --------------------------------------------------------------------------------

@Composable
private fun PanneauResultats(
        viewModel: MultiRationViewModel,
        onOuvrirAnalyse: (AnimalEv, String) -> Unit,
        modifier: Modifier
) {
    val resultat by viewModel.resultat.collectAsState()
    val p by viewModel.parametres.collectAsState()
    val parametresDuResultat by viewModel.parametresDuResultat.collectAsState()
    val erreur by viewModel.erreur.collectAsState()
    val caseSelectionnee by viewModel.caseSelectionnee.collectAsState()
    val scope = rememberCoroutineScope()

    Column(modifier.verticalScroll(rememberScrollState())) {
        erreur?.let {
            Bandeau(it, Color(0xFFF2DEDE), Color(0xFFA94442)) { viewModel.effacerErreur() }
        }
        val r = resultat
        if (r == null) {
            Bandeau(
                    "Choisir les référentiels, la grille et les six listes d'ingrédients à gauche, puis « Calculer toutes les rations ». " +
                            "Un clic sur une case de la carte affiche ses rations ; elles peuvent ensuite être ouvertes dans l'analyse de ration pour être analysées, éditées et exportées.",
                    Color(0xFFD9EDF7),
                    Color(0xFF31708F)
            )
            return@Column
        }
        if (parametresDuResultat != null && parametresDuResultat != p) {
            Bandeau(
                    "Paramètres modifiés depuis le dernier calcul : les résultats affichés ne les prennent pas encore en compte.",
                    Color(0xFFFCF8E3),
                    Color(0xFF8A6D3B)
            )
        }
        val conformes = r.scenarios.count { it.statut == StatutScenario.CONFORME }
        Text(
                "${r.scenarios.size} scénarios ; $conformes conformes à tous les seuils renseignés.",
                style = MaterialTheme.typography.h6
        )
        Text(
                "Cliquer sur une case pour voir ses rations. Libellé : n/N combinaisons conformes ; sinon nombre minimal de seuils renseignés non respectés.",
                fontSize = 12.sp,
                color = Color.Gray
        )
        Spacer(Modifier.height(8.dp))
        LegendeZones()
        r.cases.groupBy { it.reference.uuid }.values.forEach { cases ->
            CarteReference(cases, caseSelectionnee) { viewModel.selectionnerCase(it) }
        }
        Text(
                "Une case est équilibrable si au moins une combinaison respecte tous les seuils. L'ajustement est séquentiel : " +
                        "une case rouge signifie qu'aucune combinaison testée ne convient avec cette méthode, pas qu'aucune ration n'existe.",
                fontSize = 11.sp,
                color = Color.Gray
        )
        if (isCsvFileOperationsSupported()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                OutlinedButton(onClick = { scope.launch { saveCsvFileForExport(viewModel.csvScenarios(), "exploration-scenarios.csv") } }) {
                    Text("Exporter les scénarios (CSV)", fontSize = 12.sp)
                }
                OutlinedButton(onClick = { scope.launch { saveCsvFileForExport(viewModel.csvCases(), "exploration-carte.csv") } }) {
                    Text("Exporter la carte (CSV)", fontSize = 12.sp)
                }
            }
        }
        caseSelectionnee?.let { case -> DetailCase(viewModel, case, onOuvrirAnalyse) }
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
                    Icon(Icons.Default.Close, contentDescription = "Fermer", tint = couleur)
                }
    }
}

@Composable
private fun LegendeZones() {
    val exemples =
            listOf(
                    ZoneEquilibre.EQUILIBRABLE to Color(0xFF0CA30C),
                    ZoneEquilibre.SOUS_RESERVE to Color(0xFFFAB219),
                    ZoneEquilibre.SEUILS_NON_RESPECTES to Color(0xFFD03B3B),
                    ZoneEquilibre.ENERGIE_DEPASSEE to Color(0xFFEC835A),
                    ZoneEquilibre.NON_EVALUABLE to Color(0xFFB5B5B0)
            )
    Column(Modifier.padding(bottom = 8.dp)) {
        exemples.forEach { (zone, couleur) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).background(couleur))
                Text(
                        " ${zone.libelle}${if (zone == ZoneEquilibre.SEUILS_NON_RESPECTES) " (clair = 1 seuil, foncé = 5 et plus)" else ""}",
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
                                            ZoneEquilibre.EQUILIBRABLE -> "${case.conformes}/${case.combinaisons}"
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
private fun DetailCase(viewModel: MultiRationViewModel, case: CaseEquilibre, onOuvrirAnalyse: (AnimalEv, String) -> Unit) {
    val scenario by viewModel.scenarioSelectionne.collectAsState()
    val ouverture by viewModel.ouvertureEnCours.collectAsState()

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), elevation = 2.dp) {
        Column(Modifier.padding(12.dp)) {
            Text(
                    "${case.reference.nom} — ${formaterQuantite(case.poids)} kg — K ${formaterQuantite(case.k)}",
                    style = MaterialTheme.typography.h6
            )
            Text("${case.zone.libelle} — ${case.conformes}/${case.combinaisons} combinaisons conformes", fontSize = 13.sp)
            if (case.seuilsLimitants.isNotEmpty()) Text("Seuils limitants : ${case.seuilsLimitants}", fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                        onClick = { scenario?.let { viewModel.ouvrirDansAnalyse(case, listOf(it), onOuvrirAnalyse) } },
                        enabled = scenario != null && !ouverture,
                        colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Primary, contentColor = VetNutriColors.OnPrimary)
                ) { Text("Analyser et éditer cette ration") }
                OutlinedButton(
                        onClick = { viewModel.ouvrirDansAnalyse(case, case.scenarios.take(MAX_RATIONS_PAR_CASE), onOuvrirAnalyse) },
                        enabled = !ouverture
                ) {
                    Text("Ouvrir la case (${minOf(case.scenarios.size, MAX_RATIONS_PAR_CASE)} rations)")
                }
            }
            Text(
                    "Les rations sont enregistrées dans une consultation de l'animal de travail « Exploration multiration » " +
                            "(référentiel, poids et K de la case), puis ouvertes dans l'analyse de ration : analyse détaillée, " +
                            "ajustement multi-nutriments, édition et export habituels.",
                    fontSize = 11.sp,
                    color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            Text("Combinaisons de la case (la meilleure d'abord)", fontWeight = FontWeight.Bold)
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
        Text(s.statut.libelle, fontSize = 12.sp, color = couleurStatut(s.statut), modifier = Modifier.width(170.dp))
        Text(
                s.composition.ifEmpty { s.message },
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
        )
        Text(
                if (s.seuilsManques.isEmpty()) "" else "${s.seuilsManques.size} seuil(s)",
                fontSize = 12.sp,
                modifier = Modifier.width(80.dp),
                textAlign = TextAlign.End
        )
    }
}

@Composable
private fun DetailScenario(s: ScenarioExploration) {
    Divider(Modifier.padding(vertical = 8.dp))
    Text("Détail ${s.id}", fontWeight = FontWeight.Bold)
    s.ration.alimentMutableList.forEachIndexed { i, a ->
        Row {
            Text(s.roles.getOrNull(i)?.libelle ?: "", fontSize = 12.sp, modifier = Modifier.width(130.dp))
            Text(a.aliment?.nom ?: "?", fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text("${formaterQuantite(a.quantite)} g", fontSize = 12.sp, modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
        }
    }
    val energie = s.energie
    val besoin = s.besoinTotal
    if (energie != null && besoin != null && besoin > 0.0) {
        Text(
                "Énergie ${energie.toInt()} / ${besoin.toInt()} kcal (${if (energie >= besoin) "+" else ""}${formaterQuantite(100.0 * (energie - besoin) / besoin)} %)",
                fontSize = 12.sp
        )
    }
    if (s.seuilsManques.isNotEmpty()) Text("Seuils non respectés : ${s.seuilsManques.joinToString()}", fontSize = 12.sp, color = Color(0xFFA94442))
    if (s.seuilsNonRenseignes.isNotEmpty())
            Text("Échecs sur données absentes : ${s.seuilsNonRenseignes.joinToString()}", fontSize = 12.sp, color = Color(0xFF8A6D3B))
    if (s.message.isNotEmpty()) Text(s.message, fontSize = 12.sp, color = Color.Gray)
}
