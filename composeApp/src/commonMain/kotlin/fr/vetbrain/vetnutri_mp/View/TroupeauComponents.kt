package fr.vetbrain.vetnutri_mp.View

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
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

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
    val texte = if (decimales == 0) "$n" else "${n / diviseur}," + (n % diviseur).toString().padStart(decimales, '0')
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
        Text("Dossier :", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
        listOf(false to "Individu", true to "Troupeau / groupe d'animaux").forEach { (valeur, libelle) ->
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
                "Individus du troupeau — ${types.sumOf { it.nombre.coerceAtLeast(0) }} animaux",
                fontWeight = FontWeight.Bold
        )
        Text(
                "Décrire les individus par groupe (espèce, race, sexe, nombre, poids moyen). Le troupeau peut " +
                        "mêler plusieurs espèces. Le référentiel et K de chaque groupe se choisissent dans chaque consultation.",
                fontSize = 11.sp,
                color = Color.Gray
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
                                    label = { Text("Groupe (ex. vaches en lactation)", fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                            )
                            ChampNombreTroupeau(
                                    "Nombre",
                                    type.nombre.toDouble(),
                                    { v -> majGroupe(type.id) { t -> t.copy(nombre = v.toInt()) } },
                                    Modifier.width(90.dp),
                                    entier = true
                            )
                            ChampNombreTroupeau(
                                    "Poids moyen (kg)",
                                    type.poids,
                                    { v -> majGroupe(type.id) { t -> t.copy(poids = v) } },
                                    Modifier.width(130.dp)
                            )
                            IconButton(
                                    onClick = { onChange(types.filterNot { it.id == type.id }) },
                                    enabled = types.size > 1,
                                    modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Retirer le groupe", modifier = Modifier.size(16.dp))
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
                            DropdownField(
                                    label = "Espèce",
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
                                    label = { Text("Race", fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                            )
                            DropdownField(
                                    label = "Sexe",
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
                    onChange(types + TypeAnimalTroupeau(nom = "Groupe ${types.size + 1}", espece = types.lastOrNull()?.espece))
                }
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(" Ajouter un groupe d'individus")
        }
    }
}

/** Groupe par défaut d'un nouveau troupeau, de l'espèce choisie jusque-là pour la fiche. */
fun typesTroupeauParDefaut(espece: Espece? = null): MutableList<TypeAnimalTroupeau> =
        mutableListOf(TypeAnimalTroupeau(nom = "Adultes", nombre = 1, poids = 0.0, espece = espece?.name))

private val couleurConformeTroupeau = Color(0xFF0CA30C)
private val couleurAlerteTroupeau = Color(0xFFD03B3B)

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

    Card(modifier = modifier, elevation = 2.dp) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val effectif = types.sumOf { c.parametresType(it).nombre.coerceAtLeast(0) }
            Text("Troupeau — $effectif animaux", fontWeight = FontWeight.Bold, color = VetNutriColors.Primary)
            Text(
                    "Rations en quantités pour tout le troupeau, réparties entre les groupes au prorata du besoin " +
                            "énergétique (un individu du groupe i reçoit quantité × Bᵢ / Σ nⱼ·Bⱼ). L'état du troupeau " +
                            "(effectifs, poids, référentiels, K) se modifie dans la consultation.",
                    fontSize = 11.sp,
                    color = Color.Gray
            )

            // Ce qui est analysé : le troupeau entier ou un individu d'un groupe
            Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    // Défilement horizontal si les groupes sont nombreux
                    modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                Text("Analyser :", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                BoutonTypeTroupeau("Troupeau entier", typeAnalyse == null, enabled = true) { viewModel.selectTypeTroupeau(null) }
                types.forEach { type ->
                    val repartissable = analyse?.type(type.id)?.facteur?.let { it > 0.0 } == true
                    BoutonTypeTroupeau(
                            "${type.nom.ifBlank { "Groupe sans nom" }} (${c.parametresType(type).nombre})",
                            typeAnalyse == type.id,
                            enabled = repartissable || typeAnalyse == type.id
                    ) { viewModel.selectTypeTroupeau(type.id) }
                }
            }
            val typeChoisi = types.firstOrNull { it.id == typeAnalyse }
            if (typeChoisi != null) {
                val a = analyse?.type(typeChoisi.id)
                Text(
                        "Analyse ci-dessous : un individu du groupe « ${typeChoisi.nom} » (" +
                                "${formaterNombreTroupeau((a?.facteur ?: 0.0) * 100.0)} % de la ration du troupeau), " +
                                "avec le poids, le référentiel et le K du groupe. La composition est en lecture seule : " +
                                "revenir à « Troupeau entier » pour la modifier.",
                        fontSize = 12.sp,
                        color = VetNutriColors.Primary
                )
            } else {
                Text(
                        "Analyse ci-dessous : apports à l'ensemble du troupeau comparés à la somme des besoins de ses individus.",
                        fontSize = 12.sp,
                        color = VetNutriColors.Primary
                )
            }
            if (types.isEmpty())
                    Text("Aucun groupe d'individus : modifier la fiche pour décrire le troupeau.", fontSize = 12.sp, color = couleurAlerteTroupeau)

            // Répartition et synthèse par groupe
            val a = analyse
            if (a != null) {
                Divider()
                SyntheseTroupeau(a, typeAnalyse) { viewModel.selectTypeTroupeau(it) }
                if (typeAnalyse == null && !scope.estGroupe) {
                    val facteur = a.facteurAjustementEnergie
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                                onClick = {
                                    if (viewModel.ajusterRationTroupeauEnergie()) showSnackbar("Quantités ajustées au besoin énergétique du troupeau")
                                },
                                enabled = facteur != null && a.quantiteTotale > 0.0
                        ) { Text("Ajuster la ration au besoin énergétique du troupeau", fontSize = 12.sp) }
                        if (facteur != null) Text("(× ${formaterNombreTroupeau(facteur, 3)})", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
private fun BoutonTypeTroupeau(libelle: String, choisi: Boolean, enabled: Boolean, onClick: () -> Unit) {
    if (choisi) {
        Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Primary, contentColor = VetNutriColors.OnPrimary)
        ) { Text(libelle, fontSize = 12.sp) }
    } else {
        OutlinedButton(onClick = onClick, enabled = enabled) { Text(libelle, fontSize = 12.sp) }
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
            Text("État du troupeau — $effectif animaux", fontWeight = FontWeight.Bold, color = VetNutriColors.Primary)
            Text(
                    "Pour chaque groupe d'individus : effectif et poids moyen à cette date, état corporel, référentiel et K. " +
                            "Besoin d'un individu = besoin standard du référentiel × K du groupe.",
                    fontSize = 11.sp,
                    color = Color.Gray
            )
            if (types.isEmpty())
                    Text("Aucun groupe d'individus : modifier la fiche pour décrire le troupeau.", fontSize = 12.sp, color = couleurAlerteTroupeau)
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
                    type.nom.ifBlank { "Groupe sans nom" } + if (details.isNotEmpty()) " — $details" else "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                ChampNombreTroupeau("Effectif", p.nombre.toDouble(), { onChange(p.copy(nombre = it.toInt())) },
                        Modifier.width(90.dp), entier = true)
                ChampNombreTroupeau("Poids moyen (kg)", p.poids, { onChange(p.copy(poids = it)) }, Modifier.width(130.dp))
                ChampNombreTroupeau("NEC moyenne", p.bcs ?: 0.0, { onChange(p.copy(bcs = it.takeIf { v -> v > 0.0 })) },
                        Modifier.width(110.dp))
                ChampNombreTroupeau("K", p.k, { onChange(p.copy(k = it)) }, Modifier.width(70.dp))
            }
            DropdownField(
                    label = "Référentiel",
                    selectedValue = reference,
                    options = references,
                    onValueChange = { onChange(p.copy(referenceId = it.uuid)) },
                    valueToString = { "${it.nom} — ${it.stadePhysio.label}" },
                    modifier = Modifier.fillMaxWidth()
            )
            if (p.referenceId == null)
                    Text("Référentiel à choisir pour calculer les besoins de ce groupe.", fontSize = 11.sp, color = couleurAlerteTroupeau)
            else if (reference == null)
                    Text("Référentiel introuvable : en choisir un autre.", fontSize = 11.sp, color = couleurAlerteTroupeau)
            if (p.nombre > 0 && p.poids <= 0.0)
                    Text("Poids moyen à renseigner.", fontSize = 11.sp, color = couleurAlerteTroupeau)
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
                Text("Variables des équations du référentiel (valeur par défaut si non saisie).", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

/** Groupes de l'état du troupeau incomplets (référentiel ou poids manquant), pour la validation. */
fun groupesIncompletsTroupeau(
        types: List<TypeAnimalTroupeau>,
        consultation: fr.vetbrain.vetnutri_mp.Data.ConsultationEv
): List<String> =
        types.filter { type ->
            val p = consultation.parametresType(type)
            p.nombre > 0 && (p.referenceId == null || p.poids <= 0.0)
        }.map { it.nom.ifBlank { "Groupe sans nom" } }

/**
 * Analyse des apports au troupeau entier : apport total de la ration du troupeau et somme des
 * besoins de tous les individus, nutriment par nutriment, avec les groupes hors normes.
 */
@Composable
fun AnalyseApportsTroupeau(
        analyse: fr.vetbrain.vetnutri_mp.Data.AnalyseTroupeau?,
        modifier: Modifier = Modifier,
        // Défilement propre seulement si la hauteur est bornée (grande vue) : dans une colonne
        // déjà défilante, un second défilement vertical n'est pas mesurable
        defilementVertical: Boolean = false
) {
    var seulementHorsNormes by remember { mutableStateOf(false) }
    Card(modifier = modifier, elevation = 2.dp) {
        Column(
                Modifier.padding(12.dp).then(if (defilementVertical) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Apports au troupeau entier", fontWeight = FontWeight.Bold, color = VetNutriColors.Primary)
            val lignes = analyse?.apportsTroupeauEntier().orEmpty()
            if (analyse == null || lignes.isEmpty()) {
                Text(
                        "Analyse indisponible : compléter l'état du troupeau dans la consultation (référentiel et poids de chaque groupe) " +
                                "et ajouter des aliments à la ration.",
                        fontSize = 12.sp,
                        color = Color.Gray
                )
                analyse?.problemes?.forEach { Text("• $it", fontSize = 12.sp, color = couleurAlerteTroupeau) }
            } else {
                Text(
                        "Apport total par jour comparé à la somme des besoins des individus (Σ effectif × besoin d'un individu). " +
                                "Une somme conforme peut masquer la carence d'un groupe : voir la dernière colonne.",
                        fontSize = 11.sp,
                        color = Color.Gray
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { seulementHorsNormes = !seulementHorsNormes }) {
                    Checkbox(checked = seulementHorsNormes, onCheckedChange = { seulementHorsNormes = it })
                    Text("Seulement les nutriments hors normes", fontSize = 12.sp)
                }
                val affichees =
                        if (seulementHorsNormes) lignes.filter { it.statutTroupeau != null || it.groupesHorsNormes.isNotEmpty() }
                        else lignes
                Column(Modifier.horizontalScroll(rememberScrollState())) {
                    Row(Modifier.padding(vertical = 2.dp)) {
                        listOf("Nutriment" to 180, "Apport troupeau" to 130, "MIN" to 90, "OPTIMIN" to 90, "OPTIMAX" to 90, "MAX" to 90, "Troupeau" to 150, "Groupes hors normes" to 280)
                                .forEach { (titre, largeur) ->
                                    Text(titre, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(largeur.dp))
                                }
                    }
                    affichees.forEach { l ->
                        val unite = if (l.nutriment == fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.ENERGIE) "kcal" else l.unite.displayName
                        Row(Modifier.padding(vertical = 1.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(fr.vetbrain.vetnutri_mp.Data.nomTraduitNutriment(l.nutriment), fontSize = 12.sp, maxLines = 1, modifier = Modifier.width(180.dp))
                            Text(
                                    "${formaterNombreTroupeau(l.apport)} $unite" + if (l.complet) "" else " *",
                                    fontSize = 12.sp,
                                    modifier = Modifier.width(130.dp)
                            )
                            listOf(Reflevel.MIN, Reflevel.OPTIMIN, Reflevel.OPTIMAX, Reflevel.MAX).forEach { niveau ->
                                Text(l.seuils[niveau]?.let { formaterNombreTroupeau(it) } ?: "", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.width(90.dp))
                            }
                            val statut = l.statutTroupeau
                            Text(
                                    when {
                                        l.ratio -> "Ratio (voir groupes)"
                                        statut == null -> "Conforme"
                                        statut == fr.vetbrain.vetnutri_mp.Data.ConformiteStatus.CARENCE -> if (l.critique) "Carence" else "Sous l'optimum"
                                        else -> if (l.critique) "Excès" else "Au-dessus de l'optimum"
                                    },
                                    fontSize = 12.sp,
                                    color = if (l.ratio) Color.Gray else if (statut == null) couleurConformeTroupeau else couleurAlerteTroupeau,
                                    modifier = Modifier.width(150.dp)
                            )
                            Text(
                                    l.groupesHorsNormes.joinToString(" · ") { (nom, c) ->
                                        val sens = if (c.status == fr.vetbrain.vetnutri_mp.Data.ConformiteStatus.CARENCE ||
                                                        c.status == fr.vetbrain.vetnutri_mp.Data.ConformiteStatus.CARENCE_MALADIE) "carence" else "excès"
                                        "$nom ($sens${if (c.isCritical) "" else ", optimum"})"
                                    }.ifEmpty { "—" },
                                    fontSize = 12.sp,
                                    color = if (l.groupesHorsNormes.isEmpty()) Color.Gray else couleurAlerteTroupeau,
                                    modifier = Modifier.width(280.dp)
                            )
                        }
                    }
                    Text("* valeur incomplète : composition absente pour au moins un aliment.", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun SyntheseTroupeau(
        a: fr.vetbrain.vetnutri_mp.Data.AnalyseTroupeau,
        typeAnalyse: String?,
        onAnalyser: (String) -> Unit
) {
    val couverture =
            if (a.besoinGroupe != null && a.energieGroupe != null && a.besoinGroupe > 0.0) a.energieGroupe / a.besoinGroupe * 100.0
            else null
    Text("Répartition de la ration du troupeau", fontWeight = FontWeight.Bold, fontSize = 13.sp)
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        IndicateurTroupeau("Ration du troupeau", formaterGrammesTroupeau(a.quantiteTotale) + " / j")
        IndicateurTroupeau("Besoin du troupeau", a.besoinGroupe?.let { "${formaterNombreTroupeau(it, 0)} kcal" } ?: "—")
        IndicateurTroupeau("Énergie apportée", a.energieGroupe?.let { "${formaterNombreTroupeau(it, 0)} kcal" } ?: "—")
        IndicateurTroupeau(
                "Couverture",
                couverture?.let { "${formaterNombreTroupeau(it, 0)} %" } ?: "—",
                couverture?.let { if (it in 90.0..110.0) couleurConformeTroupeau else couleurAlerteTroupeau }
        )
    }
    a.problemes.forEach { Text("• $it", fontSize = 12.sp, color = couleurAlerteTroupeau) }

    // En-tête du tableau
    Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
        listOf("Groupe" to 1.4f, "Part" to 0.6f, "Besoin / individu" to 0.9f, "Énergie / individu" to 0.9f, "Couverture" to 0.7f, "Hors normes" to 0.8f, "Ration d'un individu" to 2.2f, "" to 0.8f)
                .forEach { (titre, poids) -> Text(titre, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(poids)) }
    }
    a.types.forEach { t ->
        key(t.type.id) {
            val choisi = t.type.id == typeAnalyse
            Row(
                    Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        "${t.type.nom.ifBlank { "Groupe sans nom" }} (${t.parametres.nombre})",
                        fontSize = 12.sp,
                        fontWeight = if (choisi) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1.4f)
                )
                Text(t.part?.let { "${formaterNombreTroupeau(it * 100.0, 1)} %" } ?: "—", fontSize = 12.sp, modifier = Modifier.weight(0.6f))
                Text(t.besoinEnergetique?.let { "${formaterNombreTroupeau(it, 0)} kcal" } ?: "—", fontSize = 12.sp, modifier = Modifier.weight(0.9f))
                Text(t.energieParAnimal?.let { "${formaterNombreTroupeau(it, 0)} kcal" } ?: "—", fontSize = 12.sp, modifier = Modifier.weight(0.9f))
                val cv = t.couvertureEnergie
                Text(
                        cv?.let { "${formaterNombreTroupeau(it, 0)} %" } ?: "—",
                        fontSize = 12.sp,
                        color = cv?.let { if (it in 90.0..110.0) couleurConformeTroupeau else couleurAlerteTroupeau } ?: Color.Unspecified,
                        modifier = Modifier.weight(0.7f)
                )
                val horsNormes = t.nonConformes
                Text(
                        if (t.rationParAnimal == null) "—" else if (horsNormes.isEmpty()) "Aucun" else horsNormes.size.toString(),
                        fontSize = 12.sp,
                        color = if (t.rationParAnimal == null) Color.Unspecified else if (horsNormes.isEmpty()) couleurConformeTroupeau else couleurAlerteTroupeau,
                        modifier = Modifier.weight(0.8f)
                )
                Text(
                        t.rationParAnimal?.alimentMutableList?.filter { it.quantite > 0.0 }?.joinToString(" · ") {
                            "${it.aliment?.nom ?: "?"} ${formaterGrammesTroupeau(it.quantite)}"
                        } ?: (t.message ?: "—"),
                        fontSize = 11.sp,
                        maxLines = 2,
                        modifier = Modifier.weight(2.2f)
                )
                Box(Modifier.weight(0.8f)) {
                    if (!choisi && (t.facteur ?: 0.0) > 0.0)
                            TextButton(onClick = { onAnalyser(t.type.id) }) { Text("Analyser", fontSize = 11.sp) }
                }
            }
        }
    }
}

@Composable
private fun IndicateurTroupeau(libelle: String, valeur: String, couleur: Color? = null) {
    Column {
        Text(libelle, fontSize = 11.sp, color = Color.Gray)
        Text(valeur, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = couleur ?: Color.Unspecified)
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Consultation du troupeau", style = MaterialTheme.typography.h6, modifier = Modifier.weight(1f))
            TextButton(onClick = onEdit) { Text("Modifier") }
            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Fermer") }
        }
        Divider()
        Text("Date : ${consultation.date?.toString() ?: "non précisée"}")
        if (consultation.objectConsult.isNotBlank()) Text("Objectif : ${consultation.objectConsult}")
        if (consultation.observation.isNotBlank()) Text("Observations : ${consultation.observation}")

        val effectif = types.sumOf { consultation.parametresType(it).nombre.coerceAtLeast(0) }
        Text("État du troupeau — $effectif animaux", fontWeight = FontWeight.Bold, color = VetNutriColors.Primary)
        var besoinTotal = 0.0
        var besoinComplet = true
        types.forEach { type ->
            val p = consultation.parametresType(type)
            val reference = p.referenceId?.let { parId[it] }
            val besoin = fr.vetbrain.vetnutri_mp.Data.besoinAnimalTroupeau(p, reference, kCommun)
            if (p.nombre > 0) {
                if (besoin == null) besoinComplet = false else besoinTotal += p.nombre * besoin
            }
            Card(Modifier.fillMaxWidth(), elevation = 1.dp, backgroundColor = Color(0xFFFAFAFA)) {
                Column(Modifier.padding(8.dp)) {
                    Text(type.nom.ifBlank { "Groupe sans nom" }, fontWeight = FontWeight.Bold)
                    val details =
                            listOfNotNull(
                                    type.especeEnum()?.translateEnum(),
                                    type.race.takeIf { it.isNotBlank() },
                                    type.sexe()?.translateEnum()
                            ).joinToString(", ")
                    if (details.isNotEmpty()) Text(details, fontSize = 12.sp, color = Color.Gray)
                    Text(
                            "${p.nombre} individus × ${formaterNombreTroupeau(p.poids)} kg" +
                                    (p.bcs?.let { " — NEC ${formaterNombreTroupeau(it)}" } ?: ""),
                            fontSize = 13.sp
                    )
                    Text(
                            "Référentiel : ${reference?.nom ?: if (p.referenceId == null) "à choisir" else "introuvable"} — K ${formaterNombreTroupeau(p.k)}",
                            fontSize = 13.sp,
                            color = if (reference == null) couleurAlerteTroupeau else Color.Unspecified
                    )
                    if (p.variables.isNotEmpty())
                            Text(p.variables.entries.joinToString { "${it.key} = ${formaterNombreTroupeau(it.value)}" }, fontSize = 12.sp, color = Color.Gray)
                    Text(
                            "Besoin énergétique d'un individu : ${besoin?.let { "${formaterNombreTroupeau(it, 0)} kcal" } ?: "—"}",
                            fontSize = 13.sp
                    )
                }
            }
        }
        Text(
                "Besoin énergétique du troupeau : " +
                        if (besoinComplet && types.isNotEmpty()) "${formaterNombreTroupeau(besoinTotal, 0)} kcal / j"
                        else "incomplet (référentiel ou poids manquant)",
                fontWeight = FontWeight.Bold
        )
    }
}
