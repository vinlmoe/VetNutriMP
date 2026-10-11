package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import fr.vetbrain.vetnutri_mp.Data.especeDuGroupe
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
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
 * Panneau « Troupeau » de l'onglet Rations : paramètres de chaque type pour la consultation
 * (effectif, poids, référentiel, K, variables), répartition de la ration du groupe au prorata des
 * besoins énergétiques, et choix du type dont la ration par animal est analysée en dessous par
 * l'analyse de ration habituelle.
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
    val toutesReferences by viewModel.availableReferences.collectAsState()
    val types = animal?.typesTroupeau.orEmpty()
    val c = consultation ?: return
    // Référentiels généraux proposés à chaque groupe : ceux de l'espèce de ses individus
    val generales = remember(toutesReferences) { toutesReferences.filter { !it.maladie }.sortedBy { it.nom } }
    fun referencesDuGroupe(type: TypeAnimalTroupeau): List<fr.vetbrain.vetnutri_mp.Data.ReferenceEv> {
        val espece = animal?.especeDuGroupe(type)
        return generales.filter { it.espece == espece }.ifEmpty { generales }
    }
    var parametresOuverts by remember { mutableStateOf(true) }

    Card(modifier = modifier, elevation = 2.dp) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val effectif = types.sumOf { c.parametresType(it).nombre.coerceAtLeast(0) }
            Text("Troupeau — $effectif animaux", fontWeight = FontWeight.Bold, color = VetNutriColors.Primary)
            Text(
                    "Les rations sont saisies en quantités pour tout le troupeau. Elles sont réparties entre les groupes " +
                            "au prorata du besoin énergétique (un individu du groupe i reçoit quantité × Bᵢ / Σ nⱼ·Bⱼ, " +
                            "Bᵢ = besoin standard × K1…K5 × K du groupe). Choisir un groupe pour analyser la ration d'un de ses individus.",
                    fontSize = 11.sp,
                    color = Color.Gray
            )

            // Choix de la ration analysée : groupe ou un animal d'un type
            Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    // Défilement horizontal si les types sont nombreux
                    modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                BoutonTypeTroupeau("Ration du troupeau", typeAnalyse == null, enabled = true) { viewModel.selectTypeTroupeau(null) }
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
                                "${formaterNombreTroupeau((a?.facteur ?: 0.0) * 100.0)} % de la ration du troupeau par individu). " +
                                "Poids, référentiel et K affichés sont ceux du groupe (modifiables) ; la composition est en lecture seule.",
                        fontSize = 12.sp,
                        color = VetNutriColors.Primary
                )
            }

            // Paramètres de chaque type pour cette consultation
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { parametresOuverts = !parametresOuverts }
            ) {
                Text("Effectifs, poids et référentiels des groupes pour cette consultation", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Text(if (parametresOuverts) "▲" else "▼", color = Color.Gray)
            }
            if (parametresOuverts) {
                types.forEach { type ->
                    key(c.uuid, type.id) { LigneParametresType(viewModel, type, c.parametresType(type), referencesDuGroupe(type)) }
                }
                if (types.isEmpty())
                        Text("Aucun groupe d'individus : modifier la fiche pour décrire le troupeau.", fontSize = 12.sp, color = couleurAlerteTroupeau)
            }

            // Répartition et synthèse
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

@Composable
private fun LigneParametresType(
        viewModel: fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel,
        type: TypeAnimalTroupeau,
        p: fr.vetbrain.vetnutri_mp.Data.ParametresTypeTroupeau,
        references: List<fr.vetbrain.vetnutri_mp.Data.ReferenceEv>
) {
    val reference = references.firstOrNull { it.uuid == p.referenceId }
    Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
            Text(
                    type.nom.ifBlank { "Groupe sans nom" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.width(140.dp).padding(bottom = 10.dp)
            )
            ChampNombreTroupeau("Nombre", p.nombre.toDouble(), { viewModel.updateParametresTroupeau(p.copy(nombre = it.toInt())) },
                    Modifier.width(80.dp), entier = true)
            ChampNombreTroupeau("Poids (kg)", p.poids, { viewModel.updateParametresTroupeau(p.copy(poids = it)) }, Modifier.width(95.dp))
            fr.vetbrain.vetnutri_mp.Components.DropdownField(
                    label = "Référentiel",
                    selectedValue = reference,
                    options = references,
                    onValueChange = { viewModel.updateParametresTroupeau(p.copy(referenceId = it.uuid)) },
                    valueToString = { "${it.nom} — ${it.stadePhysio.label}" },
                    modifier = Modifier.weight(1f)
            )
            ChampNombreTroupeau("K", p.k, { viewModel.updateParametresTroupeau(p.copy(k = it)) }, Modifier.width(70.dp))
        }
        val variables = fr.vetbrain.vetnutri_mp.Data.variablesEquationsTroupeau(reference)
        if (variables.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(start = 146.dp)) {
                variables.forEach { v ->
                    ChampNombreTroupeau(
                            v,
                            p.variables[v] ?: 0.0,
                            { valeur -> viewModel.updateParametresTroupeau(p.copy(variables = p.variables + (v to valeur))) },
                            Modifier.width(90.dp)
                    )
                }
            }
        }
        if (p.referenceId != null && reference == null)
                Text("Référentiel introuvable : en choisir un autre.", fontSize = 11.sp, color = couleurAlerteTroupeau)
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
