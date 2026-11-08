package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import fr.vetbrain.vetnutri_mp.Components.AdaptiveTabRow
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Components.FlowRow
import fr.vetbrain.vetnutri_mp.Components.PuceFiltre
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.CaseEquilibre
import fr.vetbrain.vetnutri_mp.Data.CibleExploration
import fr.vetbrain.vetnutri_mp.Data.FoodSearchFilters
import fr.vetbrain.vetnutri_mp.Data.ParametresExploration
import fr.vetbrain.vetnutri_mp.Data.ResultatExploration
import fr.vetbrain.vetnutri_mp.Data.RoleExploration
import fr.vetbrain.vetnutri_mp.Data.ScenarioExploration
import fr.vetbrain.vetnutri_mp.Data.StatutScenario
import fr.vetbrain.vetnutri_mp.Data.SyntheseCombinaison
import fr.vetbrain.vetnutri_mp.Data.SyntheseExploration
import fr.vetbrain.vetnutri_mp.Data.ZoneEquilibre
import fr.vetbrain.vetnutri_mp.Data.formaterQuantite
import fr.vetbrain.vetnutri_mp.Data.nomTraduitNutriment
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.ExcelPlatform.isCsvFileOperationsSupported
import fr.vetbrain.vetnutri_mp.ExcelPlatform.saveCsvFileForExport
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.Recherche
import fr.vetbrain.vetnutri_mp.View.Components.FoodSearchComponent
import fr.vetbrain.vetnutri_mp.View.Components.FoodSearchConfig
import fr.vetbrain.vetnutri_mp.View.Components.FoodSearchLayout
import fr.vetbrain.vetnutri_mp.ViewModel.MultiRationViewModel
import kotlinx.coroutines.launch

/** Nombre maximal de rations d'une case ouvertes ensemble dans la vue ration. */
private const val MAX_RATIONS_PAR_CASE = 20

/** Lignes affichées d'emblée dans les listes longues (scénarios, combinaisons). */
private const val LIGNES_PAR_PAGE = 30

/** Largeur du panneau de détail d'une case, à côté de la carte. */
private val LARGEUR_DETAIL = 400.dp

private val niveauxCible =
        listOf(Reflevel.OPTIMIN, Reflevel.MIN, Reflevel.OPTIMAX, Reflevel.MAX)

private fun Reflevel.libelleCible(): String = translate("multiration.niveau.$name")

// --- Couleurs -------------------------------------------------------------------------------------

private val VertConforme = Color(0xFF2E8B3D)
private val RougeSeuils = Color(0xFFC62828)
private val OrangeEnergie = Color(0xFFE07B39)
private val GrisNonEvaluable = Color(0xFFB0B7B7)
private val AmbreAvertissement = Color(0xFF8A6D1B)

/** Teinte d'une case : vert si équilibrable, rouge d'autant plus foncé que les seuils manqués sont nombreux. */
internal fun couleurZone(case: CaseEquilibre): Color =
        when (case.zone) {
            ZoneEquilibre.EQUILIBRABLE, ZoneEquilibre.SOUS_RESERVE -> VertConforme
            ZoneEquilibre.SEUILS_NON_RESPECTES -> nuanceSeuils(case.minSeuilsManques ?: 5)
            ZoneEquilibre.ENERGIE_DEPASSEE -> OrangeEnergie
            ZoneEquilibre.NON_EVALUABLE -> GrisNonEvaluable
        }

/** Clair = 1 seuil manqué, foncé = 5 et plus. */
private fun nuanceSeuils(n: Int): Color =
        listOf(Color(0xFFF2A9A3), Color(0xFFE57A70), Color(0xFFD04A42), Color(0xFFA82C27), Color(0xFF751A17))[n.coerceIn(1, 5) - 1]

private fun couleurStatut(statut: StatutScenario): Color =
        when (statut) {
            StatutScenario.CONFORME, StatutScenario.CONFORME_SOUS_RESERVE -> VertConforme
            StatutScenario.SEUILS_NON_RESPECTES -> RougeSeuils
            StatutScenario.ENERGIE_DEPASSEE -> OrangeEnergie
            else -> GrisNonEvaluable
        }

/** Texte lisible sur un fond donné. */
private fun encreSur(fond: Color): Color = if (fond.luminance() > 0.45f) Color(0xFF1B1B1B) else Color.White

@Composable
private fun couleurDiscrete(): Color = MaterialTheme.colors.onSurface.copy(alpha = 0.62f)

private val bordure: Color get() = Color(0x1F000000)

/**
 * Exploration multiration : toutes les rations d'une grille référentiel × poids × K, carte des
 * zones équilibrables, classement des combinaisons d'aliments et ouverture d'une ration (ou d'une
 * case) dans une vue ration autonome.
 *
 * Grand écran : configuration à gauche (repliable), résultats à droite. Sous
 * [AppSizes.breakpointCompact] : deux onglets, Configuration puis Résultats.
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
    val courante by viewModel.explorationCourante.collectAsState()
    val resultat by viewModel.resultat.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        TopBarSimple(
                title = translate("multiration.titre") + (courante?.let { " — ${it.nom}" } ?: ""),
                onNavigateBack = onNavigateBack
        )
        BoxWithConstraints(Modifier.fillMaxSize()) {
            if (maxWidth < AppSizes.breakpointCompact) {
                var onglet by remember { mutableStateOf(0) }
                // Un nouveau résultat s'affiche aussitôt
                LaunchedEffect(resultat) { if (resultat != null) onglet = 1 }
                Column(Modifier.fillMaxSize()) {
                    TabRow(selectedTabIndex = onglet, backgroundColor = MaterialTheme.colors.surface) {
                        Tab(selected = onglet == 0, onClick = { onglet = 0 },
                                text = { Text(translate("multiration.panneau.configuration"), fontSize = 13.sp) })
                        Tab(selected = onglet == 1, onClick = { onglet = 1 },
                                text = { Text(translate("multiration.panneau.resultats"), fontSize = 13.sp) })
                    }
                    if (onglet == 0) PanneauConfiguration(viewModel, Modifier.fillMaxSize(), onMasquer = null)
                    else PanneauResultats(viewModel, Modifier.fillMaxSize().padding(horizontal = 8.dp))
                }
            } else {
                var configurationVisible by remember { mutableStateOf(true) }
                val largeurConfiguration = if (maxWidth < 1100.dp) 360.dp else 410.dp
                Row(Modifier.fillMaxSize().padding(start = 12.dp, end = 12.dp, bottom = 8.dp)) {
                    if (configurationVisible) {
                        PanneauConfiguration(
                                viewModel,
                                Modifier.width(largeurConfiguration).fillMaxHeight(),
                                onMasquer = { configurationVisible = false }
                        )
                    } else {
                        ConfigurationRepliee(viewModel) { configurationVisible = true }
                    }
                    Spacer(Modifier.width(12.dp))
                    PanneauResultats(viewModel, Modifier.weight(1f).fillMaxHeight())
                }
            }
        }
    }
}

/** Barre étroite affichée quand la configuration est repliée : la rouvrir ou relancer le calcul. */
@Composable
private fun ConfigurationRepliee(viewModel: MultiRationViewModel, onAfficher: () -> Unit) {
    val p by viewModel.parametres.collectAsState()
    val progression by viewModel.progression.collectAsState()
    Card(Modifier.width(52.dp).fillMaxHeight(), elevation = 1.dp, border = BorderStroke(1.dp, bordure)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 8.dp)) {
            IconButton(onClick = onAfficher) {
                Icon(Icons.Default.Tune, contentDescription = translate("multiration.afficherConfiguration"), tint = VetNutriColors.Primary)
            }
            IconButton(
                    onClick = { viewModel.lancerCalcul() },
                    enabled = progression == null && viewModel.problemes(p).isEmpty()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = translate("multiration.calculerTout"))
            }
            if (progression != null) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        }
    }
}

// --- Configuration ----------------------------------------------------------------------------

@Composable
private fun PanneauConfiguration(viewModel: MultiRationViewModel, modifier: Modifier, onMasquer: (() -> Unit)?) {
    val p by viewModel.parametres.collectAsState()
    var onglet by remember { mutableStateOf(0) }
    val listesVides = viewModel.listesVides(p)
    val incomplets = listOf(viewModel.animauxIncomplets(p), listesVides.isNotEmpty(), false, false)

    Card(modifier = modifier, elevation = 1.dp, border = BorderStroke(1.dp, bordure)) {
        Column(Modifier.fillMaxSize()) {
            BlocCalcul(viewModel, p, onMasquer)
            AdaptiveTabRow(tabCount = 4, selectedTabIndex = onglet, backgroundColor = MaterialTheme.colors.surface, minTabWidth = 118.dp) {
                listOf("animaux", "ingredients", "options", "enregistrees").forEachIndexed { i, cle ->
                    Tab(
                            selected = onglet == i,
                            onClick = { onglet = i },
                            selectedContentColor = VetNutriColors.Primary,
                            unselectedContentColor = couleurDiscrete(),
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(translate("multiration.onglet.$cle"), fontSize = 13.sp, maxLines = 1, softWrap = false)
                                    if (incomplets[i]) {
                                        Spacer(Modifier.width(4.dp))
                                        Box(Modifier.size(7.dp).clip(CircleShape).background(MaterialTheme.colors.error))
                                    }
                                }
                            }
                    )
                }
            }
            Divider()
            Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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

/** En-tête toujours visible : taille du calcul, ce qui le bloque, bouton de calcul. */
@Composable
private fun BlocCalcul(viewModel: MultiRationViewModel, p: ParametresExploration, onMasquer: (() -> Unit)?) {
    val progression by viewModel.progression.collectAsState()
    val problemes = viewModel.problemes(p)
    val (poids, k) = viewModel.grille(p)

    Column(Modifier.fillMaxWidth().background(VetNutriColors.Primary.copy(alpha = 0.06f)).padding(12.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                            formaterEntier(viewModel.nombreScenarios(p)),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = VetNutriColors.PrimaryVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(translate("multiration.scenarios"), fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
                }
                // Décomposition lisible du nombre de scénarios
                FlowRow(mainAxisSpacing = 4.dp, crossAxisSpacing = 4.dp) {
                    Facteur(translate("multiration.facteur.referentiels", p.referenceIds.size.toString()))
                    Signe("×")
                    Facteur(translate("multiration.facteur.poids", (poids?.size ?: 0).toString()))
                    Signe("×")
                    Facteur(translate("multiration.facteur.k", (k?.size ?: 0).toString()))
                    Signe("×")
                    Facteur(translate("multiration.facteur.combinaisons", formaterEntier(viewModel.nombreCombinaisons(p))))
                }
            }
            if (onMasquer != null) {
                IconButton(onClick = onMasquer, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = translate("multiration.masquerConfiguration"))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (problemes.isEmpty()) {
            LigneEtat(Icons.Default.CheckCircle, translate("multiration.pret"), VertConforme)
        } else {
            problemes.forEach { LigneEtat(Icons.Default.Warning, it, MaterialTheme.colors.error) }
        }
        Spacer(Modifier.height(8.dp))
        val enCours = progression
        if (enCours == null) {
            Button(
                    onClick = { viewModel.lancerCalcul() },
                    enabled = problemes.isEmpty(),
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Primary, contentColor = VetNutriColors.OnPrimary)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(translate("multiration.calculerTout"))
            }
        } else {
            LinearProgressIndicator(
                    progress = if (enCours.total > 0) enCours.faits.toFloat() / enCours.total else 0f,
                    color = VetNutriColors.Primary,
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                        translate("multiration.progression", formaterEntier(enCours.faits.toLong()), formaterEntier(enCours.total.toLong())),
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { viewModel.annulerCalcul() }) { Text(translate("multiration.annuler")) }
            }
        }
    }
}

@Composable
private fun Facteur(texte: String) {
    Text(
            texte,
            fontSize = 11.sp,
            modifier = Modifier.background(MaterialTheme.colors.surface, RoundedCornerShape(10.dp))
                    .border(1.dp, bordure, RoundedCornerShape(10.dp))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
    )
}

@Composable
private fun Signe(texte: String) {
    Text(texte, fontSize = 11.sp, color = couleurDiscrete(), modifier = Modifier.padding(top = 2.dp))
}

@Composable
private fun LigneEtat(icone: ImageVector, texte: String, couleur: Color) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 1.dp)) {
        Icon(icone, contentDescription = null, tint = couleur, modifier = Modifier.size(15.dp).padding(top = 1.dp))
        Spacer(Modifier.width(6.dp))
        Text(texte, fontSize = 12.sp, color = couleur)
    }
}

/** Titre de section des onglets de configuration. */
@Composable
private fun TitreSection(texte: String, aide: String? = null, action: @Composable RowScope.() -> Unit = {}) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(texte, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VetNutriColors.PrimaryVariant, modifier = Modifier.weight(1f))
            action()
        }
        if (aide != null) Aide(aide)
    }
}

@Composable
private fun Aide(texte: String) {
    Text(texte, fontSize = 11.sp, color = couleurDiscrete(), lineHeight = 15.sp)
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
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
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
            label = { Text(label, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            singleLine = true,
            isError = nombre.isNaN() || nombre < 0.0,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
            modifier = modifier
    )
}

@Composable
private fun OngletAnimaux(viewModel: MultiRationViewModel, p: ParametresExploration) {
    val toutes by viewModel.references.collectAsState()
    var filtre by remember { mutableStateOf("") }
    val especes = remember(toutes) { viewModel.especesDisponibles() }
    val references = toutes.filter { it.espece == p.espece }

    TitreSection(translate("multiration.espece"))
    DropdownField(
            label = translate("multiration.espece"),
            selectedValue = p.espece,
            options = especes.ifEmpty { listOf(p.espece) },
            onValueChange = { viewModel.choisirEspece(it) },
            valueToString = { it.translateEnum() },
            modifier = Modifier.fillMaxWidth()
    )

    TitreSection(
            translate("multiration.referentielsGeneraux", p.espece.translateEnum()),
            aide = translate("multiration.referentielsAide")
    ) {
        Text(
                translate("multiration.nbSelectionnes", p.referenceIds.size.toString(), references.size.toString()),
                fontSize = 12.sp,
                color = couleurDiscrete()
        )
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
                value = filtre,
                onValueChange = { filtre = it },
                placeholder = { Text(translate("multiration.filtrer"), fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
        )
        TextButton(onClick = { viewModel.selectionnerToutesReferences(true) }, enabled = references.isNotEmpty()) {
            Text(translate("multiration.tout"), fontSize = 12.sp)
        }
        TextButton(onClick = { viewModel.selectionnerToutesReferences(false) }, enabled = p.referenceIds.isNotEmpty()) {
            Text(translate("multiration.aucun"), fontSize = 12.sp)
        }
    }
    val visibles = references.filter { filtre.isBlank() || Recherche.correspond(filtre, it.nom) || it.uuid in p.referenceIds }
    Column(
            Modifier.fillMaxWidth()
                    .heightIn(max = 280.dp)
                    .border(1.dp, bordure, RoundedCornerShape(6.dp))
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 2.dp)
    ) {
        visibles.forEach { ref ->
            val coche = ref.uuid in p.referenceIds
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                            .background(if (coche) VetNutriColors.Primary.copy(alpha = 0.07f) else Color.Transparent)
                            .clickable { viewModel.basculerReference(ref.uuid) }
                            .padding(end = 8.dp)
            ) {
                Checkbox(
                        checked = coche,
                        onCheckedChange = { viewModel.basculerReference(ref.uuid) },
                        colors = CheckboxDefaults.colors(checkedColor = VetNutriColors.Primary)
                )
                Column(Modifier.weight(1f).padding(vertical = 4.dp)) {
                    Text(ref.nom, fontSize = 13.sp)
                    Text(ref.stadePhysio.label, fontSize = 11.sp, color = couleurDiscrete())
                }
            }
        }
        val message = when {
            toutes.isEmpty() -> translate("multiration.chargementReferentiels")
            references.isEmpty() -> translate("multiration.aucunReferentiel")
            visibles.isEmpty() -> translate("multiration.aucunResultatFiltre")
            else -> null
        }
        message?.let { Text(it, fontSize = 12.sp, color = couleurDiscrete(), modifier = Modifier.padding(12.dp)) }
    }

    val (poids, k) = viewModel.grille(p)
    TitreSection(translate("multiration.grilleTitre"), aide = translate("multiration.besoinTotalAide"))
    LigneGrille(
            translate("multiration.poidsKg"), p.poidsDe, p.poidsA, p.poidsPas, poids, " kg",
            { v -> viewModel.modifierParametres { it.copy(poidsDe = v) } },
            { v -> viewModel.modifierParametres { it.copy(poidsA = v) } },
            { v -> viewModel.modifierParametres { it.copy(poidsPas = v) } },
            translate("multiration.probleme.poids")
    )
    LigneGrille(
            translate("multiration.coefficientK"), p.kDe, p.kA, p.kPas, k, "",
            { v -> viewModel.modifierParametres { it.copy(kDe = v) } },
            { v -> viewModel.modifierParametres { it.copy(kA = v) } },
            { v -> viewModel.modifierParametres { it.copy(kPas = v) } },
            translate("multiration.probleme.k")
    )
}

/** Une dimension de la grille (de, à, pas) et l'aperçu des valeurs obtenues. */
@Composable
private fun LigneGrille(
        titre: String,
        de: String,
        a: String,
        pas: String,
        valeurs: List<Double>?,
        unite: String,
        onDe: (String) -> Unit,
        onA: (String) -> Unit,
        onPas: (String) -> Unit,
        erreur: String
) {
    Column(Modifier.fillMaxWidth()) {
        Text(titre, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ChampNombre(translate("multiration.de"), de, onDe, Modifier.weight(1f))
            ChampNombre(translate("multiration.a"), a, onA, Modifier.weight(1f))
            ChampNombre(translate("multiration.pas"), pas, onPas, Modifier.weight(1f))
        }
        Spacer(Modifier.height(4.dp))
        if (valeurs == null) {
            Text(erreur, fontSize = 11.sp, color = MaterialTheme.colors.error)
        } else {
            FlowRow(mainAxisSpacing = 4.dp, crossAxisSpacing = 4.dp) {
                val affichees = if (valeurs.size > 10) valeurs.take(5) + listOf(Double.NaN) + valeurs.takeLast(3) else valeurs
                affichees.forEach { v ->
                    if (v.isNaN()) Signe("… (${valeurs.size})")
                    else Facteur(formaterQuantite(v) + unite)
                }
            }
        }
    }
}

@Composable
private fun OngletIngredients(viewModel: MultiRationViewModel, p: ParametresExploration) {
    var roleAjout by remember { mutableStateOf<RoleExploration?>(null) }
    var ouvert by remember { mutableStateOf<RoleExploration?>(RoleExploration.PROTEINES) }

    Aide(translate("multiration.combinaisonAide"))
    RoleExploration.ordonner(p.listes).forEachIndexed { i, role ->
        CarteRole(
                viewModel = viewModel,
                p = p,
                role = role,
                numero = i + 1,
                ouvert = ouvert == role,
                onBasculer = { ouvert = if (ouvert == role) null else role },
                onAjouter = { roleAjout = role }
        )
    }
    OutlinedButton(onClick = { ouvert = viewModel.ajouterListe() }, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(translate("multiration.ajouterListe"), fontSize = 12.sp)
    }
    roleAjout?.let { role -> DialogueAjoutAliments(viewModel, role, onDismiss = { roleAjout = null }) }
}

@Composable
private fun CarteRole(
        viewModel: MultiRationViewModel,
        p: ParametresExploration,
        role: RoleExploration,
        numero: Int,
        ouvert: Boolean,
        onBasculer: () -> Unit,
        onAjouter: () -> Unit
) {
    val liste = p.listes[role].orEmpty()
    val cible = p.cibles[role] ?: CibleExploration(role.nutrimentParDefaut)
    val ignore = !role.estEnergie && cible.nutriment.label in p.besoinsIgnores
    val vide = liste.isEmpty() && !ignore
    val titre = if (role.supplementaire) nomTraduitNutriment(cible.nutriment) else role.libelleTraduit()

    Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 0.dp,
            border = BorderStroke(1.dp, if (vide) MaterialTheme.colors.error.copy(alpha = 0.5f) else bordure)
    ) {
        Column {
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onBasculer).padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(24.dp).clip(CircleShape)
                                .background(if (ignore) GrisNonEvaluable else VetNutriColors.Primary)
                ) { Text(numero.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(titre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                            when {
                                ignore -> translate("multiration.besoinIgnore")
                                liste.isEmpty() -> translate("multiration.vide")
                                else -> liste.joinToString(", ") { it.nom ?: it.uuid }
                            },
                            fontSize = 11.sp,
                            color = if (vide) MaterialTheme.colors.error else couleurDiscrete(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                    )
                }
                if (liste.isNotEmpty()) {
                    Text(
                            liste.size.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = VetNutriColors.PrimaryVariant,
                            modifier = Modifier.background(VetNutriColors.Primary.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Icon(if (ouvert) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
            }
            if (!ouvert) return@Column
            Divider()
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Aliments de la liste
                if (liste.isNotEmpty()) {
                    FlowRow(mainAxisSpacing = 6.dp, crossAxisSpacing = 6.dp) {
                        liste.forEach { aliment ->
                            PuceAliment(aliment.nom ?: aliment.uuid) { viewModel.retirerAliment(role, aliment.uuid) }
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = onAjouter, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(translate("multiration.ajouterAliments"), fontSize = 12.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    if (liste.isNotEmpty()) {
                        TextButton(onClick = { viewModel.viderListe(role) }) {
                            Text(translate("multiration.viderListe"), fontSize = 12.sp, color = couleurDiscrete())
                        }
                    }
                }

                // Cible
                if (!role.estEnergie) {
                    Text(translate("multiration.cibleTitre"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    if (role == RoleExploration.FIBRES || role.supplementaire) {
                        DropdownField(
                                label = translate("multiration.nutrimentCible"),
                                selectedValue = cible.nutriment,
                                options = if (role == RoleExploration.FIBRES) RoleExploration.nutrimentsFibres else
                                    NutrientResolver.getAllNutrientLabels()
                                            .mapNotNull { NutrientResolver.AllNutrientResolver(it) }
                                            .filter { it != NutrientMain.ENERGIE }
                                            .sortedBy { nomTraduitNutriment(it) },
                                onValueChange = { viewModel.modifierNutrimentCible(role, it) },
                                valueToString = { nomTraduitNutriment(it) },
                                modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                        DropdownField(
                                label = translate("multiration.cibleNiveau"),
                                selectedValue = cible.niveau,
                                options = niveauxCible,
                                onValueChange = { viewModel.modifierNiveauCible(role, it) },
                                valueToString = { it.libelleCible() },
                                height = 56.dp,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                        )
                        ChampDecimal(
                                translate("multiration.facteur"),
                                cible.facteur,
                                cle = cible.nutriment.label,
                                onValide = { f -> viewModel.modifierFacteurCible(role, f) },
                                modifier = Modifier.width(96.dp)
                        )
                    }
                    if (role == RoleExploration.FIBRES) Aide(translate("multiration.celluloseAide"))
                } else {
                    Aide(translate("multiration.energieAide"))
                }
                if (role == RoleExploration.SODIUM || role.supplementaire) Aide(translate("multiration.selAide"))

                // Arrondi propre à la liste
                if (p.arrondir) {
                    Text(translate("multiration.arrondiTitre"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ChampDecimal(
                                translate("multiration.pasManuel"),
                                p.pasArrondiManuel[role] ?: 0.0,
                                cle = "pas-${role.name}",
                                onValide = { pas -> viewModel.modifierPasArrondi(role, pas) },
                                modifier = Modifier.weight(1f)
                        )
                        ChampDecimal(
                                translate("multiration.doseMinimale"),
                                p.doseMinimale[role] ?: role.doseParDefaut,
                                cle = role,
                                onValide = { d -> viewModel.modifierDoseMinimale(role, d) },
                                modifier = Modifier.weight(1f)
                        )
                    }
                    Aide(translate("multiration.pasManuelAide"))
                }
                if (role.supplementaire) {
                    TextButton(onClick = { viewModel.retirerListe(role) }) {
                        Text(translate("multiration.supprimerListe"), color = MaterialTheme.colors.error, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PuceAliment(nom: String, onRetirer: () -> Unit) {
    Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.background(VetNutriColors.Primary.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                    .padding(start = 10.dp, end = 2.dp)
    ) {
        Text(nom, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 240.dp))
        IconButton(onClick = onRetirer, modifier = Modifier.size(26.dp)) {
            Icon(Icons.Default.Close, contentDescription = translate("multiration.retirer"), modifier = Modifier.size(14.dp))
        }
    }
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
        Card(modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.88f), elevation = 8.dp) {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                            translate("multiration.ajouterAlimentsRole", role.libelleTraduit()),
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = translate("multiration.fermer")) }
                }
                Aide(translate("multiration.cliquerAjoute", dejaChoisis.size.toString()))
                if (dejaChoisis.isNotEmpty()) {
                    Box(Modifier.heightIn(max = 96.dp).verticalScroll(rememberScrollState()).padding(vertical = 6.dp)) {
                        FlowRow(mainAxisSpacing = 6.dp, crossAxisSpacing = 6.dp) {
                            dejaChoisis.forEach { a -> PuceAliment(a.nom ?: a.uuid) { viewModel.retirerAliment(role, a.uuid) } }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                FoodSearchComponent(
                        foods = aliments,
                        filters = filtres,
                        onFiltersChange = { filtres = it },
                        config = FoodSearchConfig(
                                layout = FoodSearchLayout.VERTICAL,
                                onFoodSelected = { viewModel.ajouterAliment(role, it) },
                                isLoading = aliments.isEmpty()
                        ),
                        modifier = Modifier.weight(1f).fillMaxWidth()
                )
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                    Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Primary, contentColor = VetNutriColors.OnPrimary)
                    ) { Text(translate("multiration.terminer")) }
                }
            }
        }
    }
}

@Composable
private fun OngletOptions(viewModel: MultiRationViewModel, p: ParametresExploration) {
    @Composable
    fun Option(texte: String, coche: Boolean, onChange: (Boolean) -> Unit) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { onChange(!coche) }.padding(vertical = 2.dp)
        ) {
            Text(texte, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Switch(checked = coche, onCheckedChange = onChange, colors = SwitchDefaults.colors(checkedThumbColor = VetNutriColors.Primary))
        }
    }

    TitreSection(translate("multiration.arrondiTitre"))
    Option(translate("multiration.arrondir"), p.arrondir) { v -> viewModel.modifierParametres { it.copy(arrondir = v) } }
    if (p.arrondir) {
        Option(translate("multiration.arrondirSup"), p.arrondirAuSuperieur) { v ->
            viewModel.modifierParametres { it.copy(arrondirAuSuperieur = v) }
        }
    }
    Aide(
            translate("multiration.arrondiAide") + " " +
                    (if (p.arrondirAuSuperieur) translate("multiration.arrondiSupAide") else translate("multiration.arrondiProcheAide"))
    )
    Divider()

    TitreSection(translate("multiration.seuilsTitre"))
    Option(translate("multiration.ignorerOptimax"), p.ignorerOptimax) { v -> viewModel.modifierParametres { it.copy(ignorerOptimax = v) } }
    Aide(translate("multiration.valeursAbsentesAide"))
    Divider()

    val references by viewModel.references.collectAsState()
    val besoins = (references.filter { it.uuid in p.referenceIds }.flatMap {
        (it.getRefMapMin().keys + it.getRefMapOMin().keys + it.getRefMapMax().keys + it.getRefMapOMax().keys).toList()
    } + p.cibles.values.map { it.nutriment } + p.besoinsIgnores.mapNotNull { NutrientResolver.AllNutrientResolver(it) })
            .filter { it != NutrientMain.ENERGIE }
            .distinctBy { it.label }
            .sortedBy { nomTraduitNutriment(it) }
    TitreSection(translate("multiration.besoins"), aide = translate("multiration.besoinsAide")) {
        if (besoins.isNotEmpty())
            Text(
                    translate("multiration.nbSelectionnes", besoins.count { it.label !in p.besoinsIgnores }.toString(), besoins.size.toString()),
                    fontSize = 12.sp,
                    color = couleurDiscrete()
            )
    }
    if (besoins.isEmpty()) {
        Text(translate("multiration.besoinsSansReferentiel"), fontSize = 12.sp, color = couleurDiscrete())
    } else {
        FlowRow(mainAxisSpacing = 6.dp, crossAxisSpacing = 6.dp) {
            besoins.forEach { n ->
                val actif = n.label !in p.besoinsIgnores
                PuceBascule(nomTraduitNutriment(n), actif) {
                    viewModel.modifierParametres {
                        it.copy(besoinsIgnores = if (actif) it.besoinsIgnores + n.label else it.besoinsIgnores - n.label)
                    }
                }
            }
        }
    }
    Divider()

    TitreSection(translate("multiration.limiteTitre"))
    ChampNombre(
            translate("multiration.maxScenarios"),
            p.maxScenarios,
            { v -> viewModel.modifierParametres { it.copy(maxScenarios = v) } },
            Modifier.fillMaxWidth()
    )
}

/** Puce cliquable active / inactive (besoins pris en compte, filtres). */
@Composable
private fun PuceBascule(texte: String, actif: Boolean, onClick: () -> Unit) {
    val forme = RoundedCornerShape(14.dp)
    Text(
            texte,
            fontSize = 12.sp,
            color = if (actif) VetNutriColors.PrimaryVariant else couleurDiscrete(),
            textDecoration = if (actif) null else androidx.compose.ui.text.style.TextDecoration.LineThrough,
            modifier = Modifier.clip(forme)
                    .background(if (actif) VetNutriColors.Primary.copy(alpha = 0.12f) else Color.Transparent)
                    .border(1.dp, if (actif) VetNutriColors.Primary.copy(alpha = 0.4f) else bordure, forme)
                    .clickable(onClick = onClick)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

/** Configurations enregistrées en base : enregistrer, recharger, supprimer. */
@Composable
private fun OngletEnregistrees(viewModel: MultiRationViewModel) {
    val explorations by viewModel.explorations.collectAsState()
    val courante by viewModel.explorationCourante.collectAsState()
    var nom by remember(courante?.uuid) { mutableStateOf(courante?.nom ?: "") }
    var aSupprimer by remember { mutableStateOf<String?>(null) }

    TitreSection(translate("multiration.enregistrerConfig"), aide = translate("multiration.enregistrerAide"))
    OutlinedTextField(
            value = nom,
            onValueChange = { nom = it },
            label = { Text(translate("multiration.nom"), fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
    )
    FlowRow(mainAxisSpacing = 6.dp, crossAxisSpacing = 6.dp) {
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
    TextButton(onClick = { viewModel.nouvelleExploration() }) {
        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(translate("multiration.nouvelle"), fontSize = 12.sp)
    }

    Divider()
    TitreSection(translate("multiration.explorationsEnregistrees"))
    if (explorations.isEmpty()) Text(translate("multiration.aucuneExploration"), fontSize = 12.sp, color = couleurDiscrete())
    explorations.forEach { e ->
        val espece = Espece.entries.firstOrNull { it.name == e.espece }
        val active = e.uuid == courante?.uuid
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (active) VetNutriColors.Primary.copy(alpha = 0.1f) else Color.Transparent)
                        .border(1.dp, if (active) VetNutriColors.Primary.copy(alpha = 0.4f) else bordure, RoundedCornerShape(6.dp))
                        .clickable { viewModel.chargerExploration(e.uuid) }
                        .padding(start = 10.dp, top = 6.dp, bottom = 6.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(e.nom, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(espece?.translateEnum() ?: e.espece, fontSize = 11.sp, color = couleurDiscrete())
            }
            TextButton(onClick = { viewModel.chargerExploration(e.uuid) }) { Text(translate("multiration.charger"), fontSize = 12.sp) }
            IconButton(onClick = { aSupprimer = e.uuid }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Close, contentDescription = translate("multiration.supprimer"), tint = MaterialTheme.colors.error, modifier = Modifier.size(18.dp))
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
                    }) { Text(translate("multiration.supprimer"), color = MaterialTheme.colors.error) }
                },
                dismissButton = { TextButton(onClick = { aSupprimer = null }) { Text(translate("multiration.annuler")) } }
        )
    }
}

// --- Résultats --------------------------------------------------------------------------------

@Composable
private fun PanneauResultats(viewModel: MultiRationViewModel, modifier: Modifier) {
    val resultat by viewModel.resultat.collectAsState()
    val p by viewModel.parametres.collectAsState()
    val parametresDuResultat by viewModel.parametresDuResultat.collectAsState()
    val erreur by viewModel.erreur.collectAsState()
    val message by viewModel.message.collectAsState()
    val progression by viewModel.progression.collectAsState()
    var onglet by remember { mutableStateOf(0) }

    BoxWithConstraints(modifier) {
        val etroit = maxWidth < AppSizes.breakpointCompact
        val detailACote = maxWidth >= LARGEUR_DETAIL + 480.dp
        val r = resultat

        @Composable
        fun Bandeaux() {
            erreur?.let { Bandeau(it, MaterialTheme.colors.error, Icons.Default.Warning) { viewModel.effacerErreur() } }
            message?.let { Bandeau(it, VertConforme, Icons.Default.CheckCircle) { viewModel.effacerMessage() } }
            if (r != null && parametresDuResultat != null && parametresDuResultat != p) {
                Bandeau(translate("multiration.parametresModifies"), AmbreAvertissement, Icons.Default.Warning,
                        action = translate("multiration.recalculer") to { viewModel.lancerCalcul() },
                        actionActive = progression == null && viewModel.problemes(p).isEmpty())
            }
        }

        if (r == null) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Bandeaux()
                EtatInitial(progression != null)
            }
            return@BoxWithConstraints
        }

        @Composable
        fun Entete() {
            Bandeaux()
            EnteteResultats(viewModel, r)
            AdaptiveTabRow(tabCount = 3, selectedTabIndex = onglet, backgroundColor = MaterialTheme.colors.background, minTabWidth = 120.dp) {
                listOf("carte", "combinaisons", "courbes").forEachIndexed { i, cle ->
                    Tab(
                            selected = onglet == i,
                            onClick = { onglet = i },
                            selectedContentColor = VetNutriColors.Primary,
                            unselectedContentColor = couleurDiscrete(),
                            text = { Text(translate("multiration.resultats.$cle"), fontSize = 13.sp, maxLines = 1, softWrap = false) }
                    )
                }
            }
            Divider()
        }

        val voirSurCarte: (Int) -> Unit = { c ->
            viewModel.filtrerCombinaison(c)
            onglet = 0
        }

        if (etroit) {
            // Téléphone : une seule colonne qui défile
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Entete()
                Spacer(Modifier.height(8.dp))
                when (onglet) {
                    0 -> OngletCarte(viewModel, r, detailACote = false)
                    1 -> OngletCombinaisons(viewModel, voirSurCarte)
                    else -> CourbesMultiration(r)
                }
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                Entete()
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    if (onglet == 0 && detailACote) {
                        OngletCarteACote(viewModel, r)
                    } else {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 8.dp)) {
                            when (onglet) {
                                0 -> OngletCarte(viewModel, r, detailACote = false)
                                1 -> OngletCombinaisons(viewModel, voirSurCarte)
                                else -> CourbesMultiration(r)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Premier affichage, avant tout calcul : les trois étapes. */
@Composable
private fun EtatInitial(enCours: Boolean) {
    Card(Modifier.fillMaxWidth().padding(vertical = 8.dp), elevation = 0.dp, border = BorderStroke(1.dp, bordure)) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GridOn, contentDescription = null, tint = VetNutriColors.Primary, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(12.dp))
                Text(translate("multiration.vide.titre"), style = MaterialTheme.typography.h6)
            }
            listOf("etape1", "etape2", "etape3").forEachIndexed { i, cle ->
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(24.dp).clip(CircleShape).background(VetNutriColors.Primary.copy(alpha = 0.12f))
                    ) { Text((i + 1).toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VetNutriColors.PrimaryVariant) }
                    Spacer(Modifier.width(12.dp))
                    Text(translate("multiration.vide.$cle"), fontSize = 13.sp, modifier = Modifier.padding(top = 3.dp))
                }
            }
            if (enCours) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = VetNutriColors.Primary)
                    Spacer(Modifier.width(10.dp))
                    Text(translate("multiration.calculEnCours"), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun Bandeau(
        texte: String,
        couleur: Color,
        icone: ImageVector,
        action: Pair<String, () -> Unit>? = null,
        actionActive: Boolean = true,
        onClose: (() -> Unit)? = null
) {
    Row(
            Modifier.fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .background(couleur.copy(alpha = 0.09f), RoundedCornerShape(6.dp))
                    .border(1.dp, couleur.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icone, contentDescription = null, tint = couleur, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(texte, color = couleur, fontSize = 13.sp, modifier = Modifier.weight(1f))
        action?.let { (libelle, onAction) ->
            TextButton(onClick = onAction, enabled = actionActive) { Text(libelle, color = couleur, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
        }
        if (onClose != null)
            IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = translate("multiration.fermer"), tint = couleur, modifier = Modifier.size(16.dp))
            }
    }
}

/** Indicateurs clés du résultat et exports. */
@Composable
private fun EnteteResultats(viewModel: MultiRationViewModel, r: ResultatExploration) {
    val synthese by viewModel.synthese.collectAsState()
    val scope = rememberCoroutineScope()
    val total = r.scenarios.size
    val meilleure = synthese.combinaisons.firstOrNull()

    FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp, modifier = Modifier.padding(vertical = 6.dp)) {
        Indicateur(translate("multiration.kpi.scenarios"), formaterEntier(total.toLong()), null)
        Indicateur(
                translate("multiration.kpi.conformes"),
                formaterEntier(synthese.scenariosConformes.toLong()),
                pourcentage(synthese.scenariosConformes, total),
                couleur = if (synthese.scenariosConformes > 0) VertConforme else RougeSeuils
        )
        Indicateur(
                translate("multiration.kpi.cases"),
                "${synthese.casesEquilibrables} / ${synthese.cases}",
                pourcentage(synthese.casesEquilibrables, synthese.cases),
                couleur = if (synthese.casesEquilibrables > 0) VertConforme else RougeSeuils
        )
        if (meilleure != null) {
            Indicateur(
                    translate("multiration.kpi.meilleure"),
                    "${meilleure.casesConformes} / ${meilleure.cases}",
                    translate("multiration.kpi.meilleureDetail", meilleure.combinaison.toString())
            )
        }
    }
    if (r.configuration.besoinsIgnores.isNotEmpty()) {
        val noms = r.configuration.besoinsIgnores.map { label ->
            NutrientResolver.AllNutrientResolver(label)?.let { nomTraduitNutriment(it) } ?: label
        }
        Aide(translate("multiration.besoinsIgnores", noms.joinToString()))
    }
    if (isCsvFileOperationsSupported()) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = { scope.launch { saveCsvFileForExport(viewModel.csvScenarios(), "exploration-scenarios.csv") } }) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(translate("multiration.exporterScenarios"), fontSize = 12.sp)
            }
            TextButton(onClick = { scope.launch { saveCsvFileForExport(viewModel.csvCases(), "exploration-carte.csv") } }) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(translate("multiration.exporterCarte"), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun Indicateur(titre: String, valeur: String, detail: String?, couleur: Color = VetNutriColors.PrimaryVariant) {
    Column(
            Modifier.widthIn(min = 150.dp)
                    .background(MaterialTheme.colors.surface, RoundedCornerShape(8.dp))
                    .border(1.dp, bordure, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(titre, fontSize = 11.sp, color = couleurDiscrete())
        Row(verticalAlignment = Alignment.Bottom) {
            Text(valeur, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = couleur)
            if (detail != null) {
                Spacer(Modifier.width(6.dp))
                Text(detail, fontSize = 11.sp, color = couleurDiscrete(), modifier = Modifier.padding(bottom = 3.dp))
            }
        }
    }
}

/** Filtre de la carte par combinaison et légende. */
@Composable
private fun OutilsCarte(viewModel: MultiRationViewModel) {
    val synthese by viewModel.synthese.collectAsState()
    val filtre by viewModel.combinaisonFiltre.collectAsState()
    // -1 = toutes les combinaisons (une valeur nulle serait affichée comme « Sélectionner »)
    val parNumero = synthese.combinaisons.associateBy { it.combinaison }
    val options: List<Int> = listOf(-1) + synthese.combinaisons.map { it.combinaison }
    Row(verticalAlignment = Alignment.CenterVertically) {
        DropdownField(
                label = translate("multiration.carte.combinaisonAffichee"),
                selectedValue = filtre ?: -1,
                options = options,
                onValueChange = { viewModel.filtrerCombinaison(it.takeIf { n -> n >= 0 }) },
                valueToString = { n ->
                    val c = parNumero[n]
                    if (c == null) translate("multiration.carte.toutes")
                    else translate("multiration.carte.uneCombinaison", c.combinaison.toString(), c.aliments.joinToString(" + "),
                            c.casesConformes.toString(), c.cases.toString())
                },
                modifier = Modifier.weight(1f)
        )
        if (filtre != null) {
            IconButton(onClick = { viewModel.filtrerCombinaison(null) }) {
                Icon(Icons.Default.Close, contentDescription = translate("multiration.carte.toutes"))
            }
        }
    }
    Aide(if (filtre == null) translate("multiration.cliquerCase") else translate("multiration.carte.filtreAide"))
    LegendeZones()
}

/** Carte et détail empilés (fenêtre étroite) : le détail suit la carte du référentiel concerné. */
@Composable
private fun OngletCarte(viewModel: MultiRationViewModel, r: ResultatExploration, detailACote: Boolean) {
    val cases by viewModel.casesAffichees.collectAsState()
    val caseSelectionnee by viewModel.caseSelectionnee.collectAsState()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutilsCarte(viewModel)
        cases.groupBy { it.reference.uuid }.values.forEach { casesReference ->
            CarteReference(casesReference, caseSelectionnee) { viewModel.selectionnerCase(it) }
            val choisie = caseSelectionnee
            if (!detailACote && choisie != null && choisie.reference.uuid == casesReference.first().reference.uuid) {
                DetailCase(viewModel, choisie, onFermer = { viewModel.selectionnerCase(null) })
            }
        }
        Aide(translate("multiration.equilibrableAide"))
        if (r.cases.isEmpty()) Text(translate("multiration.courbes.aucuneDonnee"), fontSize = 12.sp)
    }
}

/** Grande fenêtre : carte à gauche, détail de la case à droite, chacun défile seul. */
@Composable
private fun OngletCarteACote(viewModel: MultiRationViewModel, r: ResultatExploration) {
    val caseSelectionnee by viewModel.caseSelectionnee.collectAsState()
    Row(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(top = 8.dp, end = 12.dp)) {
            OngletCarte(viewModel, r, detailACote = true)
        }
        Column(Modifier.width(LARGEUR_DETAIL).fillMaxHeight().verticalScroll(rememberScrollState()).padding(top = 8.dp)) {
            val choisie = caseSelectionnee
            if (choisie == null) {
                Card(Modifier.fillMaxWidth(), elevation = 0.dp, border = BorderStroke(1.dp, bordure)) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.GridOn, contentDescription = null, tint = couleurDiscrete(), modifier = Modifier.size(28.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(translate("multiration.detail.aucuneCase"), fontSize = 13.sp, color = couleurDiscrete(), textAlign = TextAlign.Center)
                    }
                }
            } else {
                DetailCase(viewModel, choisie, onFermer = { viewModel.selectionnerCase(null) })
            }
        }
    }
}

@Composable
private fun LegendeZones() {
    FlowRow(mainAxisSpacing = 14.dp, crossAxisSpacing = 4.dp, modifier = Modifier.padding(vertical = 4.dp)) {
        ElementLegende(VertConforme, translate("multiration.zone.EQUILIBRABLE"))
        Row(verticalAlignment = Alignment.CenterVertically) {
            (1..5).forEach { Box(Modifier.size(width = 8.dp, height = 12.dp).background(nuanceSeuils(it))) }
            Text(" " + translate("multiration.legende.seuils"), fontSize = 11.sp)
        }
        ElementLegende(OrangeEnergie, translate("multiration.legende.energie"))
        ElementLegende(GrisNonEvaluable, translate("multiration.legende.nonEvaluable"))
    }
}

@Composable
private fun ElementLegende(couleur: Color, texte: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(couleur))
        Text(" $texte", fontSize = 11.sp)
    }
}

/** Carte poids × K d'un référentiel : K en ordonnée (croissant vers le haut), poids en abscisse. */
@Composable
private fun CarteReference(cases: List<CaseEquilibre>, selection: CaseEquilibre?, onClick: (CaseEquilibre) -> Unit) {
    val poids = cases.map { it.poids }.distinct().sorted()
    val ks = cases.map { it.k }.distinct().sortedDescending()
    val parCle = cases.associateBy { it.poids to it.k }
    val equilibrables = cases.count { it.zone == ZoneEquilibre.EQUILIBRABLE }
    val largeurCase: Dp = 58.dp
    val largeurAxe: Dp = 52.dp

    Card(Modifier.fillMaxWidth(), elevation = 0.dp, border = BorderStroke(1.dp, bordure)) {
        Column(Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(cases.first().reference.nom, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text(
                        translate("multiration.carte.casesEquilibrables", equilibrables.toString(), cases.size.toString()),
                        fontSize = 12.sp,
                        color = if (equilibrables > 0) VertConforme else couleurDiscrete()
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                Column {
                    ks.forEach { k ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("K ${formaterQuantite(k)}", fontSize = 11.sp, color = couleurDiscrete(), modifier = Modifier.width(largeurAxe))
                            poids.forEach { w ->
                                val case = parCle[w to k]
                                val choisie = case != null && selection != null && case.reference.uuid == selection.reference.uuid &&
                                        case.poids == selection.poids && case.k == selection.k
                                val fond = case?.let { couleurZone(it) } ?: Color(0xFFEDEDED)
                                Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(1.5.dp)
                                                .size(width = largeurCase - 3.dp, height = 30.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(fond)
                                                .then(if (choisie) Modifier.border(BorderStroke(2.5.dp, Color(0xFF102A2A)), RoundedCornerShape(4.dp)) else Modifier)
                                                .clickable(enabled = case != null) { case?.let(onClick) }
                                ) {
                                    if (case != null) {
                                        val texte = when (case.zone) {
                                            ZoneEquilibre.EQUILIBRABLE, ZoneEquilibre.SOUS_RESERVE -> "${case.conformes + case.sousReserve}/${case.combinaisons}"
                                            else -> case.minSeuilsManques?.toString() ?: "–"
                                        }
                                        Text(texte, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = encreSur(fond), textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }
                    }
                    Row {
                        Spacer(Modifier.width(largeurAxe))
                        poids.forEach { w ->
                            Text(formaterQuantite(w), fontSize = 11.sp, color = couleurDiscrete(), textAlign = TextAlign.Center, modifier = Modifier.width(largeurCase))
                        }
                    }
                    Row {
                        Spacer(Modifier.width(largeurAxe))
                        Text(translate("multiration.carte.axePoids"), fontSize = 11.sp, color = couleurDiscrete(),
                                textAlign = TextAlign.Center, modifier = Modifier.width(largeurCase * poids.size))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCase(viewModel: MultiRationViewModel, case: CaseEquilibre, onFermer: () -> Unit) {
    val scenario by viewModel.scenarioSelectionne.collectAsState()
    var conformesSeulement by remember(case) { mutableStateOf(false) }
    var limite by remember(case, conformesSeulement) { mutableStateOf(LIGNES_PAR_PAGE) }
    val couleur = couleurZone(case)

    Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp, border = BorderStroke(1.dp, couleur.copy(alpha = 0.6f))) {
        Column {
            // En-tête coloré selon la zone
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().background(couleur.copy(alpha = 0.12f)).padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(case.reference.nom, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("${formaterQuantite(case.poids)} kg · K ${formaterQuantite(case.k)}", fontSize = 13.sp)
                }
                IconButton(onClick = onFermer) { Icon(Icons.Default.Close, contentDescription = translate("multiration.fermer")) }
            }
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(couleur))
                    Spacer(Modifier.width(6.dp))
                    Text(case.zone.libelleTraduit(), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Text(
                        translate("multiration.detailCase.conformes", "${case.conformes + case.sousReserve}", case.combinaisons.toString()),
                        fontSize = 12.sp
                )
                if (case.seuilsLimitants.isNotEmpty()) {
                    Text(translate("multiration.seuilsLimitants", case.seuilsLimitants), fontSize = 12.sp, color = couleurDiscrete())
                }
                FlowRow(mainAxisSpacing = 6.dp, crossAxisSpacing = 6.dp) {
                    Button(
                            onClick = { scenario?.let { viewModel.ouvrirDansAnalyse(listOf(it)) } },
                            enabled = scenario != null,
                            colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Primary, contentColor = VetNutriColors.OnPrimary)
                    ) { Text(translate("multiration.analyserEditer"), fontSize = 12.sp) }
                    OutlinedButton(
                            onClick = { viewModel.ouvrirDansAnalyse(case.scenarios.take(MAX_RATIONS_PAR_CASE)) },
                            enabled = case.scenarios.isNotEmpty()
                    ) {
                        Text(translate("multiration.ouvrirCase", minOf(case.scenarios.size, MAX_RATIONS_PAR_CASE).toString()), fontSize = 12.sp)
                    }
                }
                Aide(translate("multiration.ouvrirAide"))

                // Ration sélectionnée
                scenario?.takeIf { s -> case.scenarios.any { it.id == s.id } }?.let { DetailScenario(it) }

                Divider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(translate("multiration.combinaisonsCase"), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    PuceFiltre(translate("multiration.detail.conformesSeulement"), selectionnee = conformesSeulement, onClick = { conformesSeulement = !conformesSeulement })
                }
                val liste = if (conformesSeulement) case.scenarios.filter {
                    it.statut == StatutScenario.CONFORME || it.statut == StatutScenario.CONFORME_SOUS_RESERVE
                } else case.scenarios
                if (liste.isEmpty()) Text(translate("multiration.detail.aucunConforme"), fontSize = 12.sp, color = couleurDiscrete())
                liste.take(limite).forEach { s ->
                    LigneScenario(s, choisi = s.id == scenario?.id) { viewModel.selectionnerScenario(s) }
                }
                if (liste.size > limite) {
                    TextButton(onClick = { limite += LIGNES_PAR_PAGE }) {
                        Text(translate("multiration.afficherPlus", (liste.size - limite).toString()), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun LigneScenario(s: ScenarioExploration, choisi: Boolean, onClick: () -> Unit) {
    Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (choisi) VetNutriColors.Primary.copy(alpha = 0.12f) else Color.Transparent)
                    .clickable(onClick = onClick)
                    .padding(vertical = 5.dp, horizontal = 6.dp)
    ) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(couleurStatut(s.statut)))
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                    s.ration.alimentMutableList.filter { it.quantite > 0.0 }.joinToString(" + ") { it.aliment?.nom ?: "?" }.ifEmpty { s.message },
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
            )
            Text(
                    s.statut.libelleTraduit() + if (s.seuilsManques.isEmpty()) "" else " · " + translate("multiration.nbSeuils", s.seuilsManques.size.toString()),
                    fontSize = 11.sp,
                    color = couleurStatut(s.statut)
            )
        }
        Text("#${s.combinaison}", fontSize = 11.sp, color = couleurDiscrete())
    }
}

/** Composition de la ration choisie et écarts aux seuils. */
@Composable
private fun DetailScenario(s: ScenarioExploration) {
    Column(
            Modifier.fillMaxWidth()
                    .background(MaterialTheme.colors.onSurface.copy(alpha = 0.03f), RoundedCornerShape(6.dp))
                    .border(1.dp, bordure, RoundedCornerShape(6.dp))
                    .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(translate("multiration.detail", s.id), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text(s.statut.libelleTraduit(), fontSize = 11.sp, color = couleurStatut(s.statut), fontWeight = FontWeight.Medium)
        }
        s.ration.alimentMutableList.forEachIndexed { i, a ->
            val nul = a.quantite <= 0.0
            Row {
                Text(s.roles.getOrNull(i)?.libelleTraduit() ?: "", fontSize = 11.sp, color = couleurDiscrete(), modifier = Modifier.width(96.dp))
                Text(a.aliment?.nom ?: "?", fontSize = 12.sp, modifier = Modifier.weight(1f),
                        color = if (nul) couleurDiscrete() else MaterialTheme.colors.onSurface)
                Text("${formaterQuantite(a.quantite)} g", fontSize = 12.sp, fontWeight = if (nul) FontWeight.Normal else FontWeight.Bold,
                        modifier = Modifier.width(72.dp), textAlign = TextAlign.End)
            }
        }
        val energie = s.energie
        val besoin = s.besoinTotal
        if (energie != null && besoin != null && besoin > 0.0) {
            Spacer(Modifier.height(4.dp))
            Text(
                    translate("multiration.energieBilan", energie.toInt().toString(), besoin.toInt().toString(),
                            "${if (energie >= besoin) "+" else ""}${formaterQuantite(100.0 * (energie - besoin) / besoin)}"),
                    fontSize = 12.sp
            )
        }
        if (s.seuilsManques.isNotEmpty())
            Text(translate("multiration.seuilsNonRespectes", s.seuilsManques.joinToString()), fontSize = 12.sp, color = RougeSeuils)
        if (s.seuilsNonRenseignes.isNotEmpty())
            Text(translate("multiration.echecsDonnees", s.seuilsNonRenseignes.joinToString()), fontSize = 12.sp, color = AmbreAvertissement)
        if (s.message.isNotEmpty()) Text(s.message, fontSize = 12.sp, color = couleurDiscrete())
    }
}

// --- Classement des combinaisons ---------------------------------------------------------------

@Composable
private fun OngletCombinaisons(viewModel: MultiRationViewModel, onVoirSurCarte: (Int) -> Unit) {
    val synthese by viewModel.synthese.collectAsState()
    var limite by remember(synthese) { mutableStateOf(LIGNES_PAR_PAGE) }
    var conformesSeulement by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SeuilsFrequents(synthese)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(translate("multiration.combinaisons.titre"), fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    color = VetNutriColors.PrimaryVariant, modifier = Modifier.weight(1f))
            PuceFiltre(translate("multiration.combinaisons.conformesSeulement"), selectionnee = conformesSeulement, onClick = { conformesSeulement = !conformesSeulement })
        }
        Aide(translate("multiration.combinaisons.aide"))
        val liste = if (conformesSeulement) synthese.combinaisons.filter { it.casesConformes > 0 } else synthese.combinaisons
        if (liste.isEmpty()) Text(translate("multiration.combinaisons.aucune"), fontSize = 12.sp, color = couleurDiscrete())
        liste.take(limite).forEachIndexed { i, c -> CarteCombinaison(i + 1, c, onVoirSurCarte) }
        if (liste.size > limite) {
            TextButton(onClick = { limite += LIGNES_PAR_PAGE }) {
                Text(translate("multiration.afficherPlus", (liste.size - limite).toString()), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun CarteCombinaison(rang: Int, c: SyntheseCombinaison, onVoirSurCarte: (Int) -> Unit) {
    val couleur = when {
        c.casesConformes == c.cases && c.cases > 0 -> VertConforme
        c.casesConformes > 0 -> VetNutriColors.Primary
        else -> RougeSeuils
    }
    Card(Modifier.fillMaxWidth(), elevation = 0.dp, border = BorderStroke(1.dp, bordure)) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
            Text("$rang", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = couleurDiscrete(), modifier = Modifier.width(32.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FlowRow(mainAxisSpacing = 4.dp, crossAxisSpacing = 4.dp) {
                    c.aliments.forEachIndexed { i, nom ->
                        if (i > 0) Signe("+")
                        Text(
                                nom,
                                fontSize = 12.sp,
                                modifier = Modifier.background(MaterialTheme.colors.onSurface.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                            progress = c.tauxConformite.toFloat(),
                            color = couleur,
                            backgroundColor = couleur.copy(alpha = 0.15f),
                            modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                            translate("multiration.combinaisons.couverture", c.casesConformes.toString(), c.cases.toString(),
                                    pourcentage(c.casesConformes, c.cases) ?: "0 %"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                    )
                }
                if (c.poidsConformes.isNotEmpty()) {
                    Text(
                            translate("multiration.combinaisons.plages", plage(c.poidsConformes), plage(c.kConformes)),
                            fontSize = 11.sp,
                            color = couleurDiscrete()
                    )
                }
                if (c.casesSousReserve > 0) {
                    Text(translate("multiration.combinaisons.sousReserve", c.casesSousReserve.toString()), fontSize = 11.sp, color = AmbreAvertissement)
                }
                if (c.seuilsLimitants.isNotEmpty() && c.casesConformes < c.cases) {
                    Text(
                            translate("multiration.combinaisons.limitee", c.seuilsLimitants.joinToString { "${it.first} (${it.second})" }),
                            fontSize = 11.sp,
                            color = RougeSeuils
                    )
                }
            }
            TextButton(onClick = { onVoirSurCarte(c.combinaison) }) {
                Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(translate("multiration.combinaisons.voirCarte"), fontSize = 12.sp)
            }
        }
    }
}

/** Seuils qui font le plus souvent échouer les rations : ce qu'il faut corriger en priorité. */
@Composable
private fun SeuilsFrequents(synthese: SyntheseExploration) {
    if (synthese.seuilsFrequents.isEmpty()) return
    Card(Modifier.fillMaxWidth(), elevation = 0.dp, border = BorderStroke(1.dp, bordure)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(translate("multiration.seuilsFrequents.titre"), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VetNutriColors.PrimaryVariant)
            Aide(translate("multiration.seuilsFrequents.aide", formaterEntier(synthese.scenariosNonConformes.toLong())))
            val max = synthese.seuilsFrequents.first().scenarios.coerceAtLeast(1)
            synthese.seuilsFrequents.take(8).forEach { s ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(s.seuil, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(170.dp))
                    Box(Modifier.weight(1f).height(10.dp)) {
                        Box(
                                Modifier.fillMaxHeight()
                                        .fillMaxWidth(s.scenarios.toFloat() / max)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(RougeSeuils.copy(alpha = 0.75f))
                        )
                    }
                    Text(
                            "${s.scenarios} (${pourcentage(s.scenarios, synthese.scenariosNonConformes) ?: "0 %"})",
                            fontSize = 11.sp,
                            color = couleurDiscrete(),
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(90.dp)
                    )
                }
            }
        }
    }
}

// --- Formats ----------------------------------------------------------------------------------

private fun formaterEntier(n: Long): String =
        n.toString().reversed().chunked(3).joinToString(" ").reversed()

private fun pourcentage(n: Int, total: Int): String? =
        if (total <= 0) null else "${kotlin.math.round(100.0 * n / total).toInt()} %"

private fun plage(valeurs: List<Double>): String =
        when {
            valeurs.isEmpty() -> "—"
            valeurs.size == 1 -> formaterQuantite(valeurs.first())
            else -> "${formaterQuantite(valeurs.first())}–${formaterQuantite(valeurs.last())}"
        }

/** Libellés des énumérations du moteur, dans la langue de l'interface. */
private fun RoleExploration.libelleTraduit(): String =
        translate("multiration.role." + if (supplementaire) "SUPPLEMENT" else name)

private fun ZoneEquilibre.libelleTraduit(): String = translate("multiration.zone.$name")

private fun StatutScenario.libelleTraduit(): String = translate("multiration.statut.$name")
