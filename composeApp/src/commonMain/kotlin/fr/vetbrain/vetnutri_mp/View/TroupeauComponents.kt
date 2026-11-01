package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Data.TypeAnimalTroupeau
import fr.vetbrain.vetnutri_mp.Data.apportsTroupeauEntier
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Data.kCommunTroupeau
import fr.vetbrain.vetnutri_mp.Data.parametresType
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round
import fr.vetbrain.vetnutri_mp.Localization.translate

/** Nombre à la française, décimales selon l'ordre de grandeur (ou imposées). */
internal fun formaterNombreTroupeau(v: Double?, decimalesImposees: Int? = null): String {
    if (v == null || !v.isFinite()) return "—"
    val decimales =
            decimalesImposees
                    ?: when {
                        abs(v) >= 100.0 -> 0
                        abs(v) >= 10.0 -> 1
                        abs(v) >= 1.0 -> 2
                        else -> 3
                    }
    val facteur = 10.0.pow(decimales)
    val n = round(abs(v) * facteur).toLong()
    val diviseur = facteur.toLong()
    // Virgule décimale en français, point dans les autres langues
    val separateur = if (fr.vetbrain.vetnutri_mp.Localization.LocalizationManager.currentLocale == "fr") "," else "."
    val texte = if (decimales == 0) "$n" else "${n / diviseur}$separateur" + (n % diviseur).toString().padStart(decimales, '0')
    return if (v < 0 && n != 0L) "-$texte" else texte
}

/** Quantité en g, avec l'équivalent en kg au-delà d'un kilo. */
internal fun formaterGrammesTroupeau(g: Double): String =
        if (g >= 1000.0) "${formaterNombreTroupeau(g / 1000.0, 2)} kg" else "${formaterNombreTroupeau(g, 1)} g"

private fun nombreSaisi(texte: String): Double? = texte.trim().replace(',', '.').toDoubleOrNull()

/**
 * Champ numérique qui conserve la saisie en cours et se resynchronise si la valeur change ailleurs.
 */
@Composable
internal fun ChampNombreTroupeau(
        label: String,
        valeur: Double,
        onValide: (Double) -> Unit,
        modifier: Modifier = Modifier,
        entier: Boolean = false,
        enabled: Boolean = true
) {
    // Saisie : jusqu'à 3 décimales, sans zéros inutiles (la valeur affichée est la valeur utilisée)
    fun formater(v: Double) =
            if (entier) v.toLong().toString()
            else formaterNombreTroupeau(v, 3).trimEnd('0').trimEnd(',')
    var texte by remember { mutableStateOf(formater(valeur)) }
    LaunchedEffect(valeur) { if (nombreSaisi(texte) != valeur) texte = formater(valeur) }
    val nombre = nombreSaisi(texte)
    OutlinedTextField(
            value = texte,
            onValueChange = {
                texte = it
                val v = nombreSaisi(it)
                if (v != null && v >= 0.0 && (!entier || v == round(v))) onValide(v)
            },
            label = { Text(label, fontSize = 11.sp) },
            singleLine = true,
            enabled = enabled,
            isError = nombre == null || nombre < 0.0 || (entier && nombre != round(nombre)),
            modifier = modifier
    )
}

/** Choix « Individu / Troupeau » à la création ou à la modification d'un animal. */
@Composable
fun SelecteurIndividuTroupeau(estTroupeau: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(translate("troupeau.dossier"), fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
        listOf(false to translate("troupeau.individu"), true to translate("troupeau.troupeauGroupe")).forEach { (valeur, libelle) ->
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onChange(valeur) }.padding(end = 16.dp)
            ) {
                RadioButton(
                        selected = estTroupeau == valeur,
                        onClick = { onChange(valeur) },
                        colors = RadioButtonDefaults.colors(selectedColor = VetNutriColors.Primary)
                )
                Text(libelle)
            }
        }
    }
}

/**
 * Groupes d'individus du troupeau : nom, nombre, poids moyen, espèce, race et sexe. Le troupeau
 * lui-même n'a ni espèce ni race : ce sont celles de ses individus. Nombre et poids sont les
 * valeurs par défaut des consultations ; référentiel et K se choisissent dans chaque consultation.
 */
@Composable
fun EditeurTypesTroupeau(
        types: List<TypeAnimalTroupeau>,
        onChange: (List<TypeAnimalTroupeau>) -> Unit,
        modifier: Modifier = Modifier
) {
    fun majGroupe(id: String, transformation: (TypeAnimalTroupeau) -> TypeAnimalTroupeau) =
            onChange(types.map { t -> if (t.id == id) transformation(t) else t })

    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
                translate("troupeau.individusDuTroupeau", types.sumOf { it.nombre.coerceAtLeast(0) }.toString()),
                fontWeight = FontWeight.Bold
        )
        types.forEach { type ->
            key(type.id) {
                Card(Modifier.fillMaxWidth(), elevation = 1.dp, backgroundColor = Color(0xFFFAFAFA)) {
                    Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            var nom by remember { mutableStateOf(type.nom) }
                            OutlinedTextField(
                                    value = nom,
                                    onValueChange = {
                                        nom = it
                                        majGroupe(type.id) { t -> t.copy(nom = it) }
                                    },
                                    label = { Text(translate("troupeau.nomGroupe"), fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                            )
                            ChampNombreTroupeau(
                                    translate("troupeau.nombre"),
                                    type.nombre.toDouble(),
                                    { v -> majGroupe(type.id) { t -> t.copy(nombre = v.toInt()) } },
                                    Modifier.width(90.dp),
                                    entier = true
                            )
                            ChampNombreTroupeau(
                                    translate("troupeau.poidsMoyen"),
                                    type.poids,
                                    { v -> majGroupe(type.id) { t -> t.copy(poids = v) } },
                                    Modifier.width(130.dp)
                            )
                            IconButton(
                                    onClick = { onChange(types.filterNot { it.id == type.id }) },
                                    enabled = types.size > 1,
                                    modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = translate("troupeau.retirerGroupe"), modifier = Modifier.size(16.dp))
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
                            DropdownField(
                                    label = translate("troupeau.espece"),
                                    selectedValue = type.especeEnum(),
                                    options = Espece.valuesExcept(Espece.CH).toList(),
                                    onValueChange = { e -> majGroupe(type.id) { t -> t.copy(espece = e.name) } },
                                    valueToString = { it.translateEnum() },
                                    modifier = Modifier.weight(1f)
                            )
                            var race by remember { mutableStateOf(type.race) }
                            OutlinedTextField(
                                    value = race,
                                    onValueChange = {
                                        race = it
                                        majGroupe(type.id) { t -> t.copy(race = it) }
                                    },
                                    label = { Text(translate("troupeau.race"), fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                            )
                            DropdownField(
                                    label = translate("troupeau.sexe"),
                                    selectedValue = type.sexe(),
                                    options = Sex.entries.toList(),
                                    onValueChange = { sx -> majGroupe(type.id) { t -> t.copy(sexId = sx.id) } },
                                    valueToString = { it.translateEnum() },
                                    modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
        TextButton(
                onClick = {
                    // Nouveau groupe : même espèce que le dernier groupe
                    onChange(types + TypeAnimalTroupeau(nom = translate("troupeau.groupeNumero", (types.size + 1).toString()), espece = types.lastOrNull()?.espece))
                }
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(" " + translate("troupeau.ajouterGroupe"))
        }
    }
}

/** Groupe par défaut d'un nouveau troupeau, de l'espèce choisie jusque-là pour la fiche. */
fun typesTroupeauParDefaut(espece: Espece? = null): MutableList<TypeAnimalTroupeau> =
        mutableListOf(TypeAnimalTroupeau(nom = translate("troupeau.adultes"), nombre = 1, poids = 0.0, espece = espece?.name))

private val couleurConformeTroupeau = Color(0xFF2E7D32)
private val couleurAvertissementTroupeau = Color(0xFFE08A00)
private val couleurAlerteTroupeau = Color(0xFFC62828)
private val couleurBordureTroupeau = Color(0xFFD5E4E4)
private val couleurEnteteTroupeau = Color(0xFFE6F2F2)
private val couleurLigneAlterneeTroupeau = Color(0xFFF4F9F9)

/**
 * Panneau « Troupeau » de l'onglet Rations : choix de ce qui est analysé (le troupeau entier, ou un
 * individu d'un groupe) et répartition de la ration du troupeau au prorata des besoins
 * énergétiques. L'état du troupeau (effectifs, poids, référentiels, K, variables) se définit dans
 * la consultation, pas ici.
 */
@Composable
fun PanneauTroupeauConsultation(
        viewModel: fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel,
        showSnackbar: (String) -> Unit,
        modifier: Modifier = Modifier
) {
    val animal by viewModel.animal.collectAsState()
    val consultation by viewModel.selectedConsultation.collectAsState()
    val typeAnalyse by viewModel.typeTroupeauAnalyse.collectAsState()
    val analyse by viewModel.analyseTroupeau.collectAsState()
    val scope by viewModel.rationAnalysisScope.collectAsState()
    val types = animal?.typesTroupeau.orEmpty()
    val c = consultation ?: return
    Card(modifier = modifier, elevation = 2.dp, shape = RoundedCornerShape(10.dp)) {
      BoxWithConstraints {
        val etroit = maxWidth < AppSizes.breakpointCompact
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // En-tête : effectif et ajustement de la ration (sur sa propre ligne en écran étroit)
            val effectif = types.sumOf { c.parametresType(it).nombre.coerceAtLeast(0) }
            val a = analyse
            val ajustement: (@Composable () -> Unit)? =
                    if (a != null && typeAnalyse == null && !scope.estGroupe) {
                        {
                            val facteur = a.facteurAjustementEnergie
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (facteur != null) Text("× ${formaterNombreTroupeau(facteur, 3)}", fontSize = 12.sp, color = Color.Gray)
                                OutlinedButton(
                                        onClick = {
                                            if (viewModel.ajusterRationTroupeauEnergie()) showSnackbar(translate("troupeau.quantitesAjustees"))
                                            else showSnackbar(translate("troupeau.analyseEnCours"))
                                        },
                                        enabled = facteur != null && a.quantiteTotale > 0.0,
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) { Text(translate("troupeau.ajusterRation"), fontSize = 12.sp) }
                            }
                        }
                    } else null
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                        translate("troupeau.troupeauEffectif", effectif.toString()),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = VetNutriColors.Primary,
                        modifier = Modifier.weight(1f)
                )
                if (!etroit) ajustement?.invoke()
            }
            if (etroit) ajustement?.invoke()

            // Ce qui est analysé : le troupeau entier ou un individu d'un groupe
            Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    // Défilement horizontal si les groupes sont nombreux
                    modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                Text(translate("troupeau.analyser"), fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Color.DarkGray)
                PuceTroupeau(translate("troupeau.troupeauEntier"), typeAnalyse == null, enabled = true) { viewModel.selectTypeTroupeau(null) }
                types.forEach { type ->
                    val repartissable = analyse?.type(type.id)?.facteur?.let { it > 0.0 } == true
                    PuceTroupeau(
                            "${type.nom.ifBlank { translate("troupeau.groupeSansNom") }} · ${c.parametresType(type).nombre}",
                            typeAnalyse == type.id,
                            enabled = repartissable || typeAnalyse == type.id
                    ) { viewModel.selectTypeTroupeau(type.id) }
                }
            }
            if (types.isEmpty()) Text(translate("troupeau.aucunGroupe"), fontSize = 12.sp, color = couleurAlerteTroupeau)

            // Répartition et synthèse par groupe
            analyse?.let { SyntheseTroupeau(it, typeAnalyse, etroit) { id -> viewModel.selectTypeTroupeau(if (id == typeAnalyse) null else id) } }
        }
      }
    }
}

/** Puce de choix de ce qui est analysé (troupeau entier ou groupe). */
@Composable
private fun PuceTroupeau(libelle: String, choisi: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val forme = RoundedCornerShape(16.dp)
    Surface(
            shape = forme,
            color = if (choisi) VetNutriColors.Primary else Color.Transparent,
            border = BorderStroke(1.dp, if (choisi) VetNutriColors.Primary else couleurBordureTroupeau),
            modifier = Modifier.clip(forme).clickable(enabled = enabled && !choisi, onClick = onClick)
    ) {
        Text(
                libelle,
                fontSize = 12.sp,
                fontWeight = if (choisi) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    choisi -> VetNutriColors.OnPrimary
                    enabled -> VetNutriColors.PrimaryVariant
                    else -> Color.Gray
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/** Petite étiquette colorée (statut, couverture, nombre d'écarts). */
@Composable
private fun PastilleTroupeau(texte: String, couleur: Color) {
    Surface(shape = RoundedCornerShape(10.dp), color = couleur.copy(alpha = 0.12f)) {
        Text(
                texte,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = couleur,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

/**
 * État du troupeau dans le formulaire de consultation : pour chaque groupe d'individus, effectif,
 * poids moyen, note d'état corporel, référentiel (de l'espèce du groupe), K et variables des
 * équations du référentiel. Remplace les champs de l'individu (poids, NEC, référentiel...).
 */
@Composable
fun EditeurEtatTroupeau(
        types: List<TypeAnimalTroupeau>,
        consultation: fr.vetbrain.vetnutri_mp.Data.ConsultationEv,
        referencesDuGroupe: (TypeAnimalTroupeau) -> List<fr.vetbrain.vetnutri_mp.Data.ReferenceEv>,
        onChange: (fr.vetbrain.vetnutri_mp.Data.ParametresTypeTroupeau) -> Unit,
        modifier: Modifier = Modifier
) {
    Card(modifier = modifier, elevation = 2.dp) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val effectif = types.sumOf { consultation.parametresType(it).nombre.coerceAtLeast(0) }
            Text(translate("troupeau.etatTroupeau", effectif.toString()), fontWeight = FontWeight.Bold, color = VetNutriColors.Primary)
            if (types.isEmpty())
                    Text(translate("troupeau.aucunGroupe"), fontSize = 12.sp, color = couleurAlerteTroupeau)
            types.forEach { type ->
                key(consultation.uuid, type.id) {
                    LigneEtatGroupe(type, consultation.parametresType(type), referencesDuGroupe(type), onChange)
                }
            }
        }
    }
}

@Composable
private fun LigneEtatGroupe(
        type: TypeAnimalTroupeau,
        p: fr.vetbrain.vetnutri_mp.Data.ParametresTypeTroupeau,
        references: List<fr.vetbrain.vetnutri_mp.Data.ReferenceEv>,
        onChange: (fr.vetbrain.vetnutri_mp.Data.ParametresTypeTroupeau) -> Unit
) {
    val reference = references.firstOrNull { it.uuid == p.referenceId }
    Card(Modifier.fillMaxWidth(), elevation = 1.dp, backgroundColor = Color(0xFFFAFAFA)) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val details =
                    listOfNotNull(
                            type.especeEnum()?.translateEnum(),
                            type.race.takeIf { it.isNotBlank() },
                            type.sexe()?.translateEnum()
                    ).joinToString(", ")
            Text(
                    type.nom.ifBlank { translate("troupeau.groupeSansNom") } + if (details.isNotEmpty()) " — $details" else "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                ChampNombreTroupeau(translate("troupeau.effectif"), p.nombre.toDouble(), { onChange(p.copy(nombre = it.toInt())) },
                        Modifier.width(90.dp), entier = true)
                ChampNombreTroupeau(translate("troupeau.poidsMoyen"), p.poids, { onChange(p.copy(poids = it)) }, Modifier.width(130.dp))
                ChampNombreTroupeau(translate("troupeau.necMoyenne"), p.bcs ?: 0.0, { onChange(p.copy(bcs = it.takeIf { v -> v > 0.0 })) },
                        Modifier.width(110.dp))
                ChampNombreTroupeau("K", p.k, { onChange(p.copy(k = it)) }, Modifier.width(70.dp))
            }
            DropdownField(
                    label = translate("troupeau.referentiel"),
                    selectedValue = reference,
                    options = references,
                    onValueChange = { onChange(p.copy(referenceId = it.uuid)) },
                    valueToString = { "${it.nom} — ${it.stadePhysio.label}" },
                    modifier = Modifier.fillMaxWidth()
            )
            if (p.referenceId == null)
                    Text(translate("troupeau.referentielAChoisir"), fontSize = 11.sp, color = couleurAlerteTroupeau)
            else if (reference == null)
                    Text(translate("troupeau.referentielIntrouvable"), fontSize = 11.sp, color = couleurAlerteTroupeau)
            if (p.nombre > 0 && p.poids <= 0.0)
                    Text(translate("troupeau.poidsARenseigner"), fontSize = 11.sp, color = couleurAlerteTroupeau)
            val variables = fr.vetbrain.vetnutri_mp.Data.variablesEquationsTroupeau(reference)
            if (variables.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    variables.forEach { v ->
                        ChampNombreTroupeau(
                                v,
                                p.variables[v] ?: 0.0,
                                { valeur -> onChange(p.copy(variables = p.variables + (v to valeur))) },
                                Modifier.width(90.dp)
                        )
                    }
                }
                Text(translate("troupeau.variables"), fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

/**
 * Groupes de l'état du troupeau incomplets (référentiel manquant ou introuvable, poids nul, K non
 * positif), pour la validation : mêmes conditions que celles qui bloquent la répartition.
 * [referencesConnues] null = référentiels pas encore chargés, existence non vérifiée.
 */
fun groupesIncompletsTroupeau(
        types: List<TypeAnimalTroupeau>,
        consultation: fr.vetbrain.vetnutri_mp.Data.ConsultationEv,
        referencesConnues: Set<String>? = null
): List<String> =
        types.filter { type ->
            val p = consultation.parametresType(type)
            p.nombre > 0 &&
                    (p.referenceId == null ||
                            (referencesConnues != null && p.referenceId !in referencesConnues) ||
                            p.poids <= 0.0 ||
                            p.k <= 0.0)
        }.map { it.nom.ifBlank { translate("troupeau.groupeSansNom") } }

/**
 * Analyse des apports au troupeau entier : apport total de la ration du troupeau et somme des
 * besoins de tous les individus, nutriment par nutriment, avec les groupes hors normes.
 */
@Composable
fun AnalyseApportsTroupeau(
        analyse: fr.vetbrain.vetnutri_mp.Data.AnalyseTroupeau?,
        /** Apports au troupeau entier, calculés par le ViewModel (apportsTroupeauEntier). */
        lignes: List<fr.vetbrain.vetnutri_mp.Data.LigneApportTroupeau>,
        modifier: Modifier = Modifier,
        // Défilement propre seulement si la hauteur est bornée (grande vue) : dans une colonne
        // déjà défilante, un second défilement vertical n'est pas mesurable
        defilementVertical: Boolean = false
) {
    var seulementHorsNormes by remember { mutableStateOf(false) }
    Card(modifier = modifier, elevation = 2.dp, shape = RoundedCornerShape(10.dp)) {
        Column(
                Modifier.padding(14.dp).then(if (defilementVertical) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(translate("troupeau.apportsTroupeauEntier"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = VetNutriColors.Primary)
                Spacer(Modifier.weight(1f))
                if (analyse != null && lignes.isNotEmpty()) {
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable { seulementHorsNormes = !seulementHorsNormes }
                    ) {
                        Checkbox(checked = seulementHorsNormes, onCheckedChange = { seulementHorsNormes = it })
                        Text(translate("troupeau.seulementHorsNormes"), fontSize = 12.sp, modifier = Modifier.padding(end = 6.dp))
                    }
                }
            }
            if (analyse == null || lignes.isEmpty()) {
                Text(translate("troupeau.analyseIndisponible"), fontSize = 12.sp, color = Color.Gray)
                analyse?.problemes?.forEach { Text("• $it", fontSize = 12.sp, color = couleurAlerteTroupeau) }
            } else {
                // Ordre stable : énergie, puis nutriments hors normes, puis ordre alphabétique
                val affichees =
                        (if (seulementHorsNormes) lignes.filter { it.statutTroupeau != null || it.groupesHorsNormes.isNotEmpty() }
                        else lignes).sortedWith(
                                compareBy<fr.vetbrain.vetnutri_mp.Data.LigneApportTroupeau>(
                                        { it.nutriment != fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.ENERGIE },
                                        { it.statutTroupeau == null && it.groupesHorsNormes.isEmpty() },
                                        { fr.vetbrain.vetnutri_mp.Data.nomTraduitNutriment(it.nutriment) }
                                )
                        )
                val niveaux = listOf(Reflevel.MIN, Reflevel.OPTIMIN, Reflevel.OPTIMAX, Reflevel.MAX)
                Column(Modifier.horizontalScroll(rememberScrollState())) {
                    Row(
                            Modifier.background(couleurEnteteTroupeau, RoundedCornerShape(6.dp)).padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        CelluleEntete(translate("troupeau.colNutriment"), 170.dp, alignerADroite = false)
                        CelluleEntete(translate("troupeau.colApport"), 110.dp)
                        niveaux.forEach { CelluleEntete(it.name, 72.dp) }
                        Spacer(Modifier.width(12.dp))
                        CelluleEntete(translate("troupeau.colTroupeau"), 140.dp, alignerADroite = false)
                        CelluleEntete(translate("troupeau.colGroupesHorsNormes"), 260.dp, alignerADroite = false)
                    }
                    affichees.forEachIndexed { i, l ->
                        val unite = if (l.nutriment == fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.ENERGIE) "kcal" else l.unite.displayName
                        Row(
                                Modifier.background(if (i % 2 == 1) couleurLigneAlterneeTroupeau else Color.Transparent)
                                        .heightIn(min = 28.dp).padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                    fr.vetbrain.vetnutri_mp.Data.nomTraduitNutriment(l.nutriment),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    modifier = Modifier.width(170.dp).padding(start = 8.dp)
                            )
                            Text(
                                    "${formaterNombreTroupeau(l.apport)} $unite" + if (l.complet) "" else " *",
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.width(110.dp)
                            )
                            niveaux.forEach { niveau ->
                                Text(
                                        l.seuils[niveau]?.let { formaterNombreTroupeau(it) } ?: "",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.width(72.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            val statut = l.statutTroupeau
                            Box(Modifier.width(140.dp)) {
                                when {
                                    l.ratio -> Text(translate("troupeau.ratioVoirGroupes"), fontSize = 11.sp, color = Color.Gray)
                                    statut == null -> PastilleTroupeau(translate("troupeau.conforme"), couleurConformeTroupeau)
                                    else -> PastilleTroupeau(
                                            if (statut == fr.vetbrain.vetnutri_mp.Data.ConformiteStatus.CARENCE)
                                                    (if (l.critique) translate("troupeau.carence") else translate("troupeau.sousOptimum"))
                                            else (if (l.critique) translate("troupeau.exces") else translate("troupeau.dessusOptimum")),
                                            if (l.critique) couleurAlerteTroupeau else couleurAvertissementTroupeau
                                    )
                                }
                            }
                            Text(
                                    l.groupesHorsNormes.joinToString(" · ") { (nom, c) ->
                                        val sens = if (c.status == fr.vetbrain.vetnutri_mp.Data.ConformiteStatus.CARENCE ||
                                                        c.status == fr.vetbrain.vetnutri_mp.Data.ConformiteStatus.CARENCE_MALADIE) translate("troupeau.carenceMin") else translate("troupeau.excesMin")
                                        "$nom ($sens${if (c.isCritical) "" else ", optimum"})"
                                    }.ifEmpty { "—" },
                                    fontSize = 12.sp,
                                    color = if (l.groupesHorsNormes.isEmpty()) Color.Gray else couleurAlerteTroupeau,
                                    modifier = Modifier.width(260.dp)
                            )
                        }
                    }
                    Text(translate("troupeau.valeurIncomplete"), fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun CelluleEntete(texte: String, largeur: androidx.compose.ui.unit.Dp, alignerADroite: Boolean = true) {
    Text(
            texte,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = VetNutriColors.PrimaryVariant,
            textAlign = if (alignerADroite) TextAlign.End else TextAlign.Start,
            maxLines = 1,
            modifier = Modifier.width(largeur).padding(start = if (alignerADroite) 0.dp else 8.dp)
    )
}

@Composable
private fun SyntheseTroupeau(
        a: fr.vetbrain.vetnutri_mp.Data.AnalyseTroupeau,
        typeAnalyse: String?,
        etroit: Boolean,
        onAnalyser: (String) -> Unit
) {
    val couverture =
            if (a.besoinGroupe != null && a.energieGroupe != null && a.besoinGroupe > 0.0) a.energieGroupe / a.besoinGroupe * 100.0
            else null
    val tuiles: List<@Composable (Modifier) -> Unit> = listOf(
            { m -> TuileTroupeau(translate("troupeau.rationTroupeau"), formaterGrammesTroupeau(a.quantiteTotale) + translate("troupeau.parJour"), m) },
            { m -> TuileTroupeau(translate("troupeau.besoinTroupeau"), a.besoinGroupe?.let { "${formaterNombreTroupeau(it, 0)} kcal" } ?: "—", m) },
            { m -> TuileTroupeau(translate("troupeau.energieApportee"), a.energieGroupe?.let { "${formaterNombreTroupeau(it, 0)} kcal" } ?: "—", m) },
            { m ->
                TuileTroupeau(
                        translate("troupeau.couverture"),
                        couverture?.let { "${formaterNombreTroupeau(it, 0)} %" } ?: "—",
                        m,
                        couleur = couverture?.let { couleurCouverture(it) },
                        progression = couverture?.let { (it / 100.0).toFloat() }
                )
            }
    )
    // Une rangée de quatre tuiles, ou une grille 2 × 2 en écran étroit
    tuiles.chunked(if (etroit) 2 else 4).forEach { rangee ->
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            rangee.forEach { tuile -> tuile(Modifier.weight(1f)) }
        }
    }
    a.problemes.forEach { Text("• $it", fontSize = 12.sp, color = couleurAlerteTroupeau) }
    if (etroit) {
        a.types.forEach { t ->
            key(t.type.id) { CarteGroupeTroupeau(t, t.type.id == typeAnalyse, onAnalyser) }
        }
        return
    }

    // Tableau des groupes : une ligne cliquable par groupe (analyse d'un individu du groupe)
    Column(Modifier.fillMaxWidth().border(1.dp, couleurBordureTroupeau, RoundedCornerShape(8.dp)).clip(RoundedCornerShape(8.dp))) {
        Row(Modifier.fillMaxWidth().background(couleurEnteteTroupeau).padding(horizontal = 10.dp, vertical = 6.dp)) {
            listOf(
                    translate("troupeau.colGroupe") to 1.3f, translate("troupeau.colPart") to 0.6f,
                    translate("troupeau.colBesoinIndividu") to 0.9f, translate("troupeau.colEnergieIndividu") to 0.9f,
                    translate("troupeau.couverture") to 0.7f, translate("troupeau.colHorsNormes") to 0.8f,
                    translate("troupeau.colRationIndividu") to 2.2f
            ).forEach { (titre, poids) ->
                Text(titre, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VetNutriColors.PrimaryVariant, maxLines = 1, modifier = Modifier.weight(poids))
            }
        }
        a.types.forEachIndexed { i, t ->
            key(t.type.id) {
                val choisi = t.type.id == typeAnalyse
                val analysable = (t.facteur ?: 0.0) > 0.0
                if (i > 0) Divider(color = couleurBordureTroupeau)
                Row(
                        Modifier.fillMaxWidth()
                                .background(if (choisi) VetNutriColors.Primary.copy(alpha = 0.10f) else Color.Transparent)
                                .clickable(enabled = analysable || choisi) { onAnalyser(t.type.id) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(Modifier.weight(1.3f), verticalAlignment = Alignment.CenterVertically) {
                        if (choisi) Box(Modifier.size(width = 3.dp, height = 16.dp).background(VetNutriColors.Primary))
                        Text(
                                "${t.type.nom.ifBlank { translate("troupeau.groupeSansNom") }} · ${t.parametres.nombre}",
                                fontSize = 12.sp,
                                fontWeight = if (choisi) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                modifier = Modifier.padding(start = if (choisi) 6.dp else 0.dp)
                        )
                    }
                    Text(t.part?.let { "${formaterNombreTroupeau(it * 100.0, 1)} %" } ?: "—", fontSize = 12.sp, modifier = Modifier.weight(0.6f))
                    Text(t.besoinEnergetique?.let { "${formaterNombreTroupeau(it, 0)} kcal" } ?: "—", fontSize = 12.sp, modifier = Modifier.weight(0.9f))
                    Text(t.energieParAnimal?.let { "${formaterNombreTroupeau(it, 0)} kcal" } ?: "—", fontSize = 12.sp, modifier = Modifier.weight(0.9f))
                    Box(Modifier.weight(0.7f)) {
                        val cv = t.couvertureEnergie
                        if (cv != null) PastilleTroupeau("${formaterNombreTroupeau(cv, 0)} %", couleurCouverture(cv)) else Text("—", fontSize = 12.sp)
                    }
                    Box(Modifier.weight(0.8f)) {
                        val horsNormes = t.nonConformes
                        when {
                            t.rationParAnimal == null -> Text("—", fontSize = 12.sp)
                            horsNormes.isEmpty() -> PastilleTroupeau(translate("troupeau.aucun"), couleurConformeTroupeau)
                            else -> PastilleTroupeau(translate("troupeau.nbHorsNormes", horsNormes.size.toString()), couleurAlerteTroupeau)
                        }
                    }
                    Text(
                            t.rationParAnimal?.alimentMutableList?.filter { it.quantite > 0.0 }?.joinToString(" · ") {
                                "${it.aliment?.nom ?: "?"} ${formaterGrammesTroupeau(it.quantite)}"
                            } ?: (t.message ?: "—"),
                            fontSize = 12.sp,
                            maxLines = 2,
                            modifier = Modifier.weight(2.2f)
                    )
                }
            }
        }
    }
}

/** Groupe du troupeau en écran étroit : une carte cliquable au lieu d'une ligne de tableau. */
@Composable
private fun CarteGroupeTroupeau(
        t: fr.vetbrain.vetnutri_mp.Data.AnalyseTypeTroupeau,
        choisi: Boolean,
        onAnalyser: (String) -> Unit
) {
    val analysable = (t.facteur ?: 0.0) > 0.0
    Column(
            Modifier.fillMaxWidth()
                    .border(1.dp, if (choisi) VetNutriColors.Primary else couleurBordureTroupeau, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (choisi) VetNutriColors.Primary.copy(alpha = 0.08f) else Color.White)
                    .clickable(enabled = analysable || choisi) { onAnalyser(t.type.id) }
                    .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                    "${t.type.nom.ifBlank { translate("troupeau.groupeSansNom") }} · ${t.parametres.nombre}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
            )
            t.couvertureEnergie?.let { PastilleTroupeau("${formaterNombreTroupeau(it, 0)} %", couleurCouverture(it)) }
            val horsNormes = t.nonConformes
            if (t.rationParAnimal != null)
                    if (horsNormes.isEmpty()) PastilleTroupeau(translate("troupeau.aucun"), couleurConformeTroupeau)
                    else PastilleTroupeau(translate("troupeau.nbHorsNormes", horsNormes.size.toString()), couleurAlerteTroupeau)
        }
        Text(
                listOfNotNull(
                        t.part?.let { "${translate("troupeau.colPart")} ${formaterNombreTroupeau(it * 100.0, 1)} %" },
                        t.besoinEnergetique?.let { "${translate("troupeau.colBesoinIndividu")} ${formaterNombreTroupeau(it, 0)} kcal" }
                ).joinToString(" · "),
                fontSize = 12.sp,
                color = Color.DarkGray
        )
        Text(
                t.rationParAnimal?.alimentMutableList?.filter { it.quantite > 0.0 }?.joinToString(" · ") {
                    "${it.aliment?.nom ?: "?"} ${formaterGrammesTroupeau(it.quantite)}"
                } ?: (t.message ?: "—"),
                fontSize = 12.sp
        )
    }
}

/** Couverture énergétique : vert entre 90 et 110 %, orange jusqu'à 80 / 120 %, rouge au-delà. */
private fun couleurCouverture(pourcentage: Double): Color = when {
    pourcentage in 90.0..110.0 -> couleurConformeTroupeau
    pourcentage in 80.0..120.0 -> couleurAvertissementTroupeau
    else -> couleurAlerteTroupeau
}

@Composable
private fun TuileTroupeau(
        libelle: String,
        valeur: String,
        modifier: Modifier = Modifier,
        couleur: Color? = null,
        progression: Float? = null
) {
    Column(
            modifier.border(1.dp, couleurBordureTroupeau, RoundedCornerShape(8.dp))
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(libelle, fontSize = 11.sp, color = Color.Gray, maxLines = 1)
        Text(valeur, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = couleur ?: VetNutriColors.OnSurface, maxLines = 1)
        if (progression != null) {
            LinearProgressIndicator(
                    progress = progression.coerceIn(0f, 1f),
                    color = couleur ?: VetNutriColors.Primary,
                    backgroundColor = couleurBordureTroupeau,
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
            )
        }
    }
}

/**
 * Détail d'une consultation de troupeau : date, objectif, observations et état du troupeau
 * (effectif, poids moyen, NEC, référentiel, K et besoin énergétique de chaque groupe).
 */
@Composable
fun DetailConsultationTroupeau(
        consultation: fr.vetbrain.vetnutri_mp.Data.ConsultationEv,
        types: List<TypeAnimalTroupeau>,
        references: List<fr.vetbrain.vetnutri_mp.Data.ReferenceEv>,
        onEdit: () -> Unit,
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier
) {
    val parId = remember(references) { references.associateBy { it.uuid } }
    val kCommun = consultation.kCommunTroupeau()
    Column(
            modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(translate("troupeau.detailTitre"), style = MaterialTheme.typography.h6, modifier = Modifier.weight(1f))
            TextButton(onClick = onEdit) { Text(translate("troupeau.modifier")) }
            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = translate("troupeau.fermer")) }
        }
        Divider()
        LigneInfoTroupeau(translate("troupeau.date"), consultation.date?.toString() ?: translate("troupeau.nonPrecisee"))
        if (consultation.objectConsult.isNotBlank()) LigneInfoTroupeau(translate("troupeau.objectif"), consultation.objectConsult)
        if (consultation.observation.isNotBlank()) LigneInfoTroupeau(translate("troupeau.observations"), consultation.observation)

        val effectif = types.sumOf { consultation.parametresType(it).nombre.coerceAtLeast(0) }
        Text(translate("troupeau.etatTroupeau", effectif.toString()), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = VetNutriColors.Primary)
        var besoinTotal = 0.0
        var besoinComplet = true
        types.forEach { type ->
            val p = consultation.parametresType(type)
            val reference = p.referenceId?.let { parId[it] }
            val besoin = fr.vetbrain.vetnutri_mp.Data.besoinAnimalTroupeau(p, reference, kCommun)
            if (p.nombre > 0) {
                if (besoin == null) besoinComplet = false else besoinTotal += p.nombre * besoin
            }
            Column(
                    Modifier.fillMaxWidth().border(1.dp, couleurBordureTroupeau, RoundedCornerShape(8.dp))
                            .background(Color.White, RoundedCornerShape(8.dp)).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(type.nom.ifBlank { translate("troupeau.groupeSansNom") }, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(
                            besoin?.let { translate("troupeau.besoinParIndividu", formaterNombreTroupeau(it, 0)) } ?: "—",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = VetNutriColors.PrimaryVariant
                    )
                }
                val details =
                        listOfNotNull(
                                type.especeEnum()?.translateEnum(),
                                type.race.takeIf { it.isNotBlank() },
                                type.sexe()?.translateEnum()
                        ).joinToString(", ")
                if (details.isNotEmpty()) Text(details, fontSize = 12.sp, color = Color.Gray)
                Text(
                        translate("troupeau.individusPoids", p.nombre.toString(), formaterNombreTroupeau(p.poids)) +
                                (p.bcs?.let { " — " + translate("troupeau.nec", formaterNombreTroupeau(it)) } ?: ""),
                        fontSize = 13.sp
                )
                Text(
                        translate(
                                "troupeau.referentielK",
                                reference?.nom ?: if (p.referenceId == null) translate("troupeau.aChoisir") else translate("troupeau.introuvable"),
                                formaterNombreTroupeau(p.k)
                        ),
                        fontSize = 13.sp,
                        color = if (reference == null) couleurAlerteTroupeau else Color.Unspecified
                )
                if (p.variables.isNotEmpty())
                        Text(p.variables.entries.joinToString { "${it.key} = ${formaterNombreTroupeau(it.value)}" }, fontSize = 12.sp, color = Color.Gray)
            }
        }
        Row(
                Modifier.fillMaxWidth().background(couleurEnteteTroupeau, RoundedCornerShape(8.dp)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text(translate("troupeau.besoinEnergetiqueTroupeau"), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(
                    if (besoinComplet && types.isNotEmpty()) "${formaterNombreTroupeau(besoinTotal, 0)} kcal" + translate("troupeau.parJour")
                    else translate("troupeau.besoinIncomplet"),
                    fontWeight = FontWeight.Bold,
                    color = if (besoinComplet && types.isNotEmpty()) VetNutriColors.PrimaryVariant else couleurAlerteTroupeau
            )
        }
    }
}

@Composable
private fun LigneInfoTroupeau(libelle: String, valeur: String) {
    Row {
        Text(libelle, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.width(120.dp))
        Text(valeur, fontSize = 13.sp)
    }
}
