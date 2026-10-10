package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import fr.vetbrain.vetnutri_mp.Components.AppDatePicker
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.AnalyseTroupeau
import fr.vetbrain.vetnutri_mp.Data.AnalyseTypeTroupeau
import fr.vetbrain.vetnutri_mp.Data.ConformiteStatus
import fr.vetbrain.vetnutri_mp.Data.ConsultationTroupeau
import fr.vetbrain.vetnutri_mp.Data.FoodSearchFilters
import fr.vetbrain.vetnutri_mp.Data.LigneAnalyseTroupeau
import fr.vetbrain.vetnutri_mp.Data.Troupeau
import fr.vetbrain.vetnutri_mp.Data.TypeAnimalTroupeau
import fr.vetbrain.vetnutri_mp.Data.nomTraduitNutriment
import fr.vetbrain.vetnutri_mp.Data.variablesEquationsTroupeau
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.View.Components.FoodSearchComponent
import fr.vetbrain.vetnutri_mp.View.Components.FoodSearchConfig
import fr.vetbrain.vetnutri_mp.View.Components.FoodSearchLayout
import fr.vetbrain.vetnutri_mp.ViewModel.HerdViewModel
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

private val couleurConforme = Color(0xFF0CA30C)
private val couleurCritique = Color(0xFFD03B3B)
private val couleurOptimale = Color(0xFF2B6CB0)

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
private fun formaterGrammes(g: Double): String =
        if (g >= 1000.0) "${formaterNombreTroupeau(g / 1000.0, 2)} kg" else "${formaterNombreTroupeau(g, 1)} g"

private fun dateLocale(ms: Long): LocalDate =
        Instant.fromEpochMilliseconds(ms).toLocalDateTime(TimeZone.currentSystemDefault()).date

private fun formaterDate(ms: Long): String {
    val d = dateLocale(ms)
    return "${d.dayOfMonth.toString().padStart(2, '0')}/${d.monthNumber.toString().padStart(2, '0')}/${d.year}"
}

private fun nombreSaisi(texte: String): Double? = texte.trim().replace(',', '.').toDoubleOrNull()

/**
 * Champ numérique qui conserve la saisie en cours et se resynchronise si la valeur change ailleurs
 * (ex. ajustement énergétique de la ration).
 */
@Composable
private fun ChampNombreTroupeau(
        label: String,
        valeur: Double,
        onValide: (Double) -> Unit,
        modifier: Modifier = Modifier,
        entier: Boolean = false
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
            isError = nombre == null || nombre < 0.0 || (entier && nombre != round(nombre)),
            modifier = modifier
    )
}

@Composable
private fun BandeauTroupeau(texte: String, fond: Color, couleur: Color, onClose: () -> Unit) {
    Row(
            Modifier.fillMaxWidth().padding(vertical = 4.dp).background(fond, RoundedCornerShape(4.dp)).padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(texte, color = couleur, fontSize = 13.sp, modifier = Modifier.weight(1f))
        IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = couleur)
        }
    }
}

@Composable
private fun SectionTroupeau(titre: String, modifier: Modifier = Modifier, contenu: @Composable ColumnScope.() -> Unit) {
    Card(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = 2.dp) {
        Column(Modifier.padding(12.dp)) {
            Text(titre, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(6.dp))
            contenu()
        }
    }
}

/**
 * Mode troupeau : groupes d'animaux (types × effectifs), consultations de groupe avec un
 * référentiel par type, ration du groupe et apports de chaque type répartis au prorata des
 * besoins énergétiques.
 */
@Composable
fun HerdView(viewModel: HerdViewModel, onNavigateBack: () -> Unit, modifier: Modifier = Modifier) {
    LaunchedEffect(Unit) { viewModel.charger() }
    val troupeau by viewModel.troupeau.collectAsState()
    val erreur by viewModel.erreur.collectAsState()
    val message by viewModel.message.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        val courant = troupeau
        TopBarSimple(
                title = if (courant == null) "Troupeaux et groupes d'animaux" else "Troupeau — ${courant.nom}",
                onNavigateBack = { if (courant == null) onNavigateBack() else viewModel.fermerTroupeau() }
        )
        Column(Modifier.padding(horizontal = 12.dp)) {
            erreur?.let { BandeauTroupeau(it, Color(0xFFFDECEA), Color(0xFFA94442)) { viewModel.effacerErreur() } }
            message?.let { BandeauTroupeau(it, Color(0xFFE0F2F1), Color(0xFF00695C)) { viewModel.effacerMessage() } }
        }
        if (courant == null) ListeTroupeaux(viewModel, Modifier.fillMaxSize())
        else EcranTroupeau(viewModel, courant, Modifier.fillMaxSize())
    }
}

// --- Liste des troupeaux ----------------------------------------------------------------------

@Composable
private fun ListeTroupeaux(viewModel: HerdViewModel, modifier: Modifier) {
    val troupeaux by viewModel.troupeaux.collectAsState()
    val references by viewModel.references.collectAsState()
    val especes = remember(references) { viewModel.especesDisponibles() }
    var nom by remember { mutableStateOf("") }
    var espece by remember { mutableStateOf<Espece?>(null) }
    var aSupprimer by remember { mutableStateOf<Troupeau?>(null) }

    Column(modifier.verticalScroll(rememberScrollState()).padding(12.dp)) {
        SectionTroupeau("Nouveau troupeau ou groupe") {
            Text(
                    "Décrire ensuite les types d'animaux (effectif, poids), puis créer des consultations : " +
                            "un référentiel par type, une ration pour tout le groupe.",
                    fontSize = 12.sp,
                    color = Color.Gray
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                OutlinedTextField(
                        value = nom,
                        onValueChange = { nom = it },
                        label = { Text("Nom", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                )
                DropdownField(
                        label = "Espèce",
                        selectedValue = espece ?: especes.firstOrNull(),
                        options = especes,
                        onValueChange = { espece = it },
                        valueToString = { it.translateEnum() },
                        modifier = Modifier.width(200.dp)
                )
                Button(
                        onClick = {
                            val choisie = espece ?: especes.firstOrNull() ?: Espece.CHIEN
                            viewModel.creerTroupeau(nom, choisie)
                            nom = ""
                        },
                        enabled = nom.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Primary, contentColor = VetNutriColors.OnPrimary)
                ) { Text("Créer") }
            }
        }

        SectionTroupeau("Troupeaux enregistrés") {
            if (troupeaux.isEmpty()) Text("Aucun troupeau enregistré.", fontSize = 12.sp, color = Color.Gray)
            troupeaux.forEach { t ->
                val especeTroupeau = Espece.entries.firstOrNull { it.name == t.espece }
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.ouvrirTroupeau(t.uuid) }.padding(vertical = 4.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(t.nom, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                                "${especeTroupeau?.translateEnum() ?: t.espece} — ${t.contenu.types.size} type(s), " +
                                        "${t.effectifParDefaut} animaux — ${t.contenu.consultations.size} consultation(s)",
                                fontSize = 12.sp,
                                color = Color.Gray
                        )
                    }
                    TextButton(onClick = { viewModel.ouvrirTroupeau(t.uuid) }) { Text("Ouvrir") }
                    IconButton(onClick = { aSupprimer = t }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Supprimer", modifier = Modifier.size(16.dp))
                    }
                }
                Divider()
            }
        }
    }

    aSupprimer?.let { t ->
        AlertDialog(
                onDismissRequest = { aSupprimer = null },
                title = { Text("Supprimer le troupeau ?") },
                text = { Text("« ${t.nom} », ses types d'animaux et ses ${t.contenu.consultations.size} consultation(s) seront supprimés.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.supprimerTroupeau(t.uuid)
                        aSupprimer = null
                    }) { Text("Supprimer") }
                },
                dismissButton = { TextButton(onClick = { aSupprimer = null }) { Text("Annuler") } }
        )
    }
}

// --- Troupeau ouvert --------------------------------------------------------------------------

@Composable
private fun EcranTroupeau(viewModel: HerdViewModel, troupeau: Troupeau, modifier: Modifier) {
    val consultationId by viewModel.consultationId.collectAsState()
    val consultation = troupeau.contenu.consultations.firstOrNull { it.id == consultationId }

    Row(modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
        Column(Modifier.width(400.dp).fillMaxHeight().verticalScroll(rememberScrollState())) {
            SectionDefinition(viewModel, troupeau)
            SectionTypes(viewModel, troupeau)
            SectionConsultations(viewModel, troupeau, consultationId)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())) {
            if (consultation == null) {
                SectionTroupeau("Consultation") {
                    Text(
                            "Créer une consultation pour choisir le référentiel de chaque type, saisir la ration du groupe et analyser les apports.",
                            fontSize = 13.sp,
                            color = Color.Gray
                    )
                    Button(onClick = { viewModel.nouvelleConsultation() }) { Text("Nouvelle consultation") }
                }
            } else {
                SectionEnteteConsultation(viewModel, consultation)
                SectionAnimauxConsultation(viewModel, troupeau, consultation)
                SectionRation(viewModel, consultation)
                SectionAnalyse(viewModel)
            }
        }
    }
}

@Composable
private fun SectionDefinition(viewModel: HerdViewModel, troupeau: Troupeau) {
    val references by viewModel.references.collectAsState()
    val especes = remember(references) { viewModel.especesDisponibles() }
    var nom by remember(troupeau.uuid) { mutableStateOf(troupeau.nom) }
    SectionTroupeau("Troupeau") {
        OutlinedTextField(
                value = nom,
                onValueChange = {
                    nom = it
                    if (it.isNotBlank()) viewModel.renommer(it.trim())
                },
                label = { Text("Nom", fontSize = 11.sp) },
                singleLine = true,
                isError = nom.isBlank(),
                modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        DropdownField(
                label = "Espèce (filtre les référentiels et les aliments)",
                selectedValue = viewModel.espece(troupeau),
                options = especes,
                onValueChange = { viewModel.choisirEspece(it) },
                valueToString = { it.translateEnum() },
                modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SectionTypes(viewModel: HerdViewModel, troupeau: Troupeau) {
    var aRetirer by remember { mutableStateOf<TypeAnimalTroupeau?>(null) }
    SectionTroupeau("Types d'animaux — ${troupeau.effectifParDefaut} animaux") {
        Text(
                "Effectif et poids moyen par type : valeurs reprises par les nouvelles consultations (modifiables dans chaque consultation).",
                fontSize = 11.sp,
                color = Color.Gray
        )
        troupeau.contenu.types.forEach { type -> key(type.id) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                var nom by remember(type.id) { mutableStateOf(type.nom) }
                OutlinedTextField(
                        value = nom,
                        onValueChange = {
                            nom = it
                            viewModel.modifierType(type.copy(nom = it))
                        },
                        label = { Text("Type", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                )
                ChampNombreTroupeau("Nombre", type.nombre.toDouble(), { viewModel.modifierType(type.copy(nombre = it.toInt())) },
                        Modifier.width(80.dp), entier = true)
                ChampNombreTroupeau("Poids (kg)", type.poids, { viewModel.modifierType(type.copy(poids = it)) }, Modifier.width(90.dp))
                IconButton(onClick = { aRetirer = type }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Retirer le type", modifier = Modifier.size(16.dp))
                }
            }
        } }
        TextButton(onClick = { viewModel.ajouterType() }) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(" Ajouter un type d'animaux", fontSize = 12.sp)
        }
    }
    aRetirer?.let { type ->
        AlertDialog(
                onDismissRequest = { aRetirer = null },
                title = { Text("Retirer le type ?") },
                text = { Text("« ${type.nom} » sera retiré du troupeau et de toutes ses consultations.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.retirerType(type.id)
                        aRetirer = null
                    }) { Text("Retirer") }
                },
                dismissButton = { TextButton(onClick = { aRetirer = null }) { Text("Annuler") } }
        )
    }
}

@Composable
private fun SectionConsultations(viewModel: HerdViewModel, troupeau: Troupeau, selection: String?) {
    var aSupprimer by remember { mutableStateOf<ConsultationTroupeau?>(null) }
    SectionTroupeau("Consultations") {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(
                    onClick = { viewModel.nouvelleConsultation() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Primary, contentColor = VetNutriColors.OnPrimary)
            ) { Text("Nouvelle", fontSize = 12.sp) }
            if (selection != null)
                    OutlinedButton(onClick = { viewModel.dupliquerConsultation(selection) }) { Text("Dupliquer", fontSize = 12.sp) }
        }
        Text(
                "Une nouvelle consultation reprend les référentiels, K et la ration de la plus récente.",
                fontSize = 11.sp,
                color = Color.Gray
        )
        if (troupeau.contenu.consultations.isEmpty()) Text("Aucune consultation.", fontSize = 12.sp, color = Color.Gray)
        troupeau.contenu.consultations.sortedByDescending { it.date }.forEach { c ->
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                            Modifier.fillMaxWidth()
                                    .background(if (c.id == selection) Color(0xFFE0F2F1) else Color.Transparent)
                                    .clickable { viewModel.selectionnerConsultation(c.id) }
                                    .padding(4.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(c.titre.ifBlank { "Sans titre" }, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("${formaterDate(c.date)} — ${c.ration.size} aliment(s)", fontSize = 11.sp, color = Color.Gray)
                }
                IconButton(onClick = { aSupprimer = c }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Supprimer", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
    aSupprimer?.let { c ->
        AlertDialog(
                onDismissRequest = { aSupprimer = null },
                title = { Text("Supprimer la consultation ?") },
                text = { Text("« ${c.titre} » du ${formaterDate(c.date)} sera supprimée.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.supprimerConsultation(c.id)
                        aSupprimer = null
                    }) { Text("Supprimer") }
                },
                dismissButton = { TextButton(onClick = { aSupprimer = null }) { Text("Annuler") } }
        )
    }
}

// --- Consultation -----------------------------------------------------------------------------

@Composable
private fun SectionEnteteConsultation(viewModel: HerdViewModel, consultation: ConsultationTroupeau) {
    var titre by remember(consultation.id) { mutableStateOf(consultation.titre) }
    var notes by remember(consultation.id) { mutableStateOf(consultation.notes) }
    SectionTroupeau("Consultation") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                    value = titre,
                    onValueChange = {
                        titre = it
                        viewModel.modifierConsultation { c -> c.copy(titre = it) }
                    },
                    label = { Text("Titre", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
            )
            AppDatePicker(
                    selectedDate = dateLocale(consultation.date),
                    onDateSelected = { d ->
                        val ms = d.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                        viewModel.modifierConsultation { c -> c.copy(date = ms) }
                    },
                    label = "Date",
                    modifier = Modifier.width(200.dp)
            )
        }
        OutlinedTextField(
                value = notes,
                onValueChange = {
                    notes = it
                    viewModel.modifierConsultation { c -> c.copy(notes = it) }
                },
                label = { Text("Notes", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp)
        )
    }
}

@Composable
private fun SectionAnimauxConsultation(viewModel: HerdViewModel, troupeau: Troupeau, consultation: ConsultationTroupeau) {
    val toutes by viewModel.references.collectAsState()
    val references = remember(toutes, troupeau.espece) { viewModel.referencesEspece() }
    val effectif = troupeau.contenu.types.sumOf { consultation.parametres(it).nombre }
    SectionTroupeau("Animaux et référentiels — $effectif animaux") {
        Text(
                "Pour chaque type : effectif, poids moyen, référentiel et K (besoin énergétique = besoin standard × K).",
                fontSize = 11.sp,
                color = Color.Gray
        )
        if (references.isEmpty())
                Text(
                        "Aucun référentiel général pour ${viewModel.espece(troupeau).translateEnum()}.",
                        fontSize = 12.sp,
                        color = Color(0xFFA94442)
                )
        troupeau.contenu.types.forEach { type -> key(consultation.id, type.id) {
            val p = consultation.parametres(type)
            val reference = references.firstOrNull { it.uuid == p.referenceId }
            Card(Modifier.fillMaxWidth().padding(vertical = 3.dp), elevation = 1.dp, backgroundColor = Color(0xFFFAFAFA)) {
                Column(Modifier.padding(8.dp)) {
                    Text(type.nom.ifBlank { "Type sans nom" }, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                        ChampNombreTroupeau("Nombre", p.nombre.toDouble(), { viewModel.modifierParametres(p.copy(nombre = it.toInt())) },
                                Modifier.width(80.dp), entier = true)
                        ChampNombreTroupeau("Poids (kg)", p.poids, { viewModel.modifierParametres(p.copy(poids = it)) }, Modifier.width(95.dp))
                        DropdownField(
                                label = "Référentiel",
                                selectedValue = reference,
                                options = references,
                                onValueChange = { viewModel.modifierParametres(p.copy(referenceId = it.uuid)) },
                                valueToString = { "${it.nom} — ${it.stadePhysio.label}" },
                                modifier = Modifier.weight(1f)
                        )
                        ChampNombreTroupeau("K", p.k, { viewModel.modifierParametres(p.copy(k = it)) }, Modifier.width(70.dp))
                    }
                    val variables = variablesEquationsTroupeau(reference)
                    if (variables.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                            variables.forEach { v ->
                                ChampNombreTroupeau(
                                        v,
                                        p.variables[v] ?: 0.0,
                                        { valeur -> viewModel.modifierParametres(p.copy(variables = p.variables + (v to valeur))) },
                                        Modifier.width(90.dp)
                                )
                            }
                        }
                        Text("Variables des équations du référentiel (valeur par défaut si non saisie).", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        } }
    }
}

@Composable
private fun SectionRation(viewModel: HerdViewModel, consultation: ConsultationTroupeau) {
    val analyse by viewModel.analyse.collectAsState()
    var ajout by remember { mutableStateOf(false) }
    val total = consultation.ration.sumOf { it.quantite }
    SectionTroupeau("Ration du groupe (quantités par jour pour l'ensemble des animaux)") {
        if (consultation.ration.isEmpty()) Text("Aucun aliment.", fontSize = 12.sp, color = Color.Gray)
        consultation.ration.forEach { ligne -> key(ligne.id) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(ligne.nom.ifBlank { ligne.alimentId }, fontSize = 13.sp, modifier = Modifier.weight(1f), maxLines = 2)
                ChampNombreTroupeau("Quantité (g)", ligne.quantite, { viewModel.modifierQuantite(ligne.id, it) }, Modifier.width(130.dp))
                Text(formaterGrammes(ligne.quantite), fontSize = 12.sp, color = Color.Gray, modifier = Modifier.width(80.dp))
                IconButton(onClick = { viewModel.retirerLigne(ligne.id) }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Retirer", modifier = Modifier.size(16.dp))
                }
            }
        } }
        Text("Total : ${formaterGrammes(total)} / jour", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { ajout = true }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(" Ajouter des aliments", fontSize = 12.sp)
            }
            val facteur = analyse?.facteurAjustementEnergie
            OutlinedButton(onClick = { viewModel.ajusterRationEnergie() }, enabled = facteur != null && consultation.ration.isNotEmpty()) {
                Text("Ajuster au besoin énergétique du groupe", fontSize = 12.sp)
            }
            if (facteur != null) Text("(× ${formaterNombreTroupeau(facteur, 3)})", fontSize = 12.sp, color = Color.Gray)
        }
        Text(
                "L'ajustement multiplie toutes les quantités par le même facteur : les proportions entre aliments sont conservées.",
                fontSize = 11.sp,
                color = Color.Gray
        )
    }
    if (ajout) DialogueAjoutAlimentsTroupeau(viewModel, consultation, onDismiss = { ajout = false })
}

@Composable
private fun DialogueAjoutAlimentsTroupeau(viewModel: HerdViewModel, consultation: ConsultationTroupeau, onDismiss: () -> Unit) {
    val aliments by viewModel.aliments.collectAsState(initial = emptyList())
    var filtres by remember { mutableStateOf(FoodSearchFilters(selectedEspece = viewModel.espece())) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(modifier = Modifier.fillMaxWidth(0.85f).fillMaxHeight(0.85f), elevation = 8.dp) {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Text("Ajouter des aliments à la ration du groupe", style = MaterialTheme.typography.h6)
                Text("Cliquer sur un aliment l'ajoute à la ration (quantité à saisir ensuite).", fontSize = 12.sp, color = Color.Gray)
                if (consultation.ration.isNotEmpty())
                        Text(
                                consultation.ration.joinToString(" · ") { it.nom.ifBlank { it.alimentId } },
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
                                        onFoodSelected = { viewModel.ajouterAliment(it) },
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

// --- Analyse ----------------------------------------------------------------------------------

@Composable
private fun SectionAnalyse(viewModel: HerdViewModel) {
    val analyse by viewModel.analyse.collectAsState()
    val calcul by viewModel.calculEnCours.collectAsState()
    var seulementHorsNormes by remember { mutableStateOf(false) }
    SectionTroupeau("Apports par type d'animaux") {
        Text(
                "La ration du groupe est répartie au prorata du besoin énergétique : un animal du type i reçoit " +
                        "quantité du groupe × Bᵢ / Σ (nⱼ × Bⱼ), puis ses apports sont comparés à son référentiel.",
                fontSize = 11.sp,
                color = Color.Gray
        )
        if (calcul) LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
        val a = analyse
        if (a == null) {
            if (!calcul) Text("Analyse en attente.", fontSize = 12.sp, color = Color.Gray)
        } else {
            ResumeGroupe(a)
            a.problemes.forEach { Text("• $it", fontSize = 12.sp, color = Color(0xFFA94442)) }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { seulementHorsNormes = !seulementHorsNormes }) {
                Checkbox(checked = seulementHorsNormes, onCheckedChange = { seulementHorsNormes = it })
                Text("Afficher seulement les nutriments hors normes", fontSize = 12.sp)
            }
            a.types.forEach { key(it.type.id) { CarteTypeAnalyse(it, seulementHorsNormes) } }
        }
    }
}

@Composable
private fun ResumeGroupe(a: AnalyseTroupeau) {
    val couverture =
            if (a.besoinGroupe != null && a.energieGroupe != null && a.besoinGroupe > 0.0) a.energieGroupe / a.besoinGroupe * 100.0
            else null
    Row(
            Modifier.fillMaxWidth().background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp)).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Indicateur("Effectif", "${a.effectif} animaux")
        Indicateur("Ration du groupe", formaterGrammes(a.quantiteTotale) + " / j")
        Indicateur("Besoin du groupe", a.besoinGroupe?.let { "${formaterNombreTroupeau(it, 0)} kcal" } ?: "—")
        Indicateur("Énergie apportée", a.energieGroupe?.let { "${formaterNombreTroupeau(it, 0)} kcal" } ?: "—")
        Indicateur(
                "Couverture",
                couverture?.let { "${formaterNombreTroupeau(it, 0)} %" } ?: "—",
                couleur = couverture?.let { if (it in 90.0..110.0) couleurConforme else couleurCritique }
        )
    }
}

@Composable
private fun Indicateur(libelle: String, valeur: String, couleur: Color? = null) {
    Column {
        Text(libelle, fontSize = 11.sp, color = Color.Gray)
        Text(valeur, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = couleur ?: Color.Unspecified)
    }
}

@Composable
private fun CarteTypeAnalyse(t: AnalyseTypeTroupeau, seulementHorsNormes: Boolean) {
    var ouvert by remember(t.type.id) { mutableStateOf(true) }
    val horsNormes = t.nonConformes.size
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = 1.dp) {
        Column(Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { ouvert = !ouvert }) {
                Column(Modifier.weight(1f)) {
                    Text(
                            "${t.type.nom.ifBlank { "Type sans nom" }} — ${t.parametres.nombre} animal(aux) de ${formaterNombreTroupeau(t.parametres.poids)} kg",
                            fontWeight = FontWeight.Bold
                    )
                    Text(t.reference?.nom ?: "Référentiel non choisi", fontSize = 12.sp, color = Color.Gray)
                }
                if (t.rationParAnimal != null)
                        Text(
                                if (horsNormes == 0) "Conforme" else "$horsNormes hors norme(s)",
                                color = if (horsNormes == 0) couleurConforme else couleurCritique,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                        )
                Icon(if (ouvert) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                Indicateur("Besoin standard / animal", t.besoinStandard?.let { "${formaterNombreTroupeau(it, 0)} kcal" } ?: "—")
                Indicateur("Besoin (× K = ${formaterNombreTroupeau(t.parametres.k)})", t.besoinEnergetique?.let { "${formaterNombreTroupeau(it, 0)} kcal" } ?: "—")
                Indicateur("Part de la ration", t.part?.let { "${formaterNombreTroupeau(it * 100.0, 1)} %" } ?: "—")
                Indicateur("Énergie / animal", t.energieParAnimal?.let { "${formaterNombreTroupeau(it, 0)} kcal" } ?: "—")
                Indicateur(
                        "Couverture",
                        t.couvertureEnergie?.let { "${formaterNombreTroupeau(it, 0)} %" } ?: "—",
                        couleur = t.couvertureEnergie?.let { if (it in 90.0..110.0) couleurConforme else couleurCritique }
                )
            }
            t.message?.let { Text(it, fontSize = 12.sp, color = Color(0xFFA94442)) }
            if (ouvert) DetailTypeAnalyse(t, seulementHorsNormes)
        }
    }
}

@Composable
private fun DetailTypeAnalyse(t: AnalyseTypeTroupeau, seulementHorsNormes: Boolean) {
    Column(Modifier.fillMaxWidth()) {
        val ration = t.rationParAnimal
        if (ration != null && ration.alimentMutableList.isNotEmpty()) {
            Text("Ration", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
            Row(Modifier.fillMaxWidth()) {
                Text("Aliment", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                Text("Par animal", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(110.dp))
                Text("Pour le type (× ${t.parametres.nombre})", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(150.dp))
            }
            ration.alimentMutableList.forEach { ligne ->
                Row(Modifier.fillMaxWidth()) {
                    Text(ligne.aliment?.nom ?: "?", fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(formaterGrammes(ligne.quantite), fontSize = 12.sp, modifier = Modifier.width(110.dp))
                    Text(formaterGrammes(ligne.quantite * t.parametres.nombre), fontSize = 12.sp, modifier = Modifier.width(150.dp))
                }
            }
        }
        val lignes = if (seulementHorsNormes) t.lignes.filter { it.conformite != null } else t.lignes
        if (lignes.isNotEmpty()) {
            Text("Apports d'un animal", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
            TableauNutriments(lignes)
        } else if (seulementHorsNormes && t.lignes.isNotEmpty()) {
            Text("Tous les seuils du référentiel sont respectés.", fontSize = 12.sp, color = couleurConforme)
        }
    }
}

@Composable
private fun TableauNutriments(lignes: List<LigneAnalyseTroupeau>) {
    val niveaux = listOf(Reflevel.MIN to "MIN", Reflevel.OPTIMIN to "OPTIMIN", Reflevel.OPTIMAX to "OPTIMAX", Reflevel.MAX to "MAX")
    Column(Modifier.horizontalScroll(rememberScrollState())) {
        Row(Modifier.background(Color(0xFFEEEEEE)).padding(vertical = 2.dp)) {
            Text("Nutriment", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(200.dp))
            Text("Apport", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp))
            niveaux.forEach { (_, libelle) -> Text(libelle, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp)) }
            Text("Statut", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(230.dp))
        }
        lignes.forEach { l ->
            val c = l.conformite
            val couleur =
                    when {
                        c == null -> couleurConforme
                        c.isCritical -> couleurCritique
                        else -> couleurOptimale
                    }
            val unite = if (l.nutriment == NutrientMain.ENERGIE) "kcal" else l.valeur.unite.displayName
            Row(Modifier.padding(vertical = 1.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(nomTraduitNutriment(l.nutriment), fontSize = 12.sp, modifier = Modifier.width(200.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                        "${formaterNombreTroupeau(l.valeur.valeur)} $unite" + if (l.valeur.complete) "" else " *",
                        fontSize = 12.sp,
                        modifier = Modifier.width(110.dp)
                )
                niveaux.forEach { (niveau, _) ->
                    Text(l.seuils[niveau]?.let { formaterNombreTroupeau(it) } ?: "", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.width(80.dp))
                }
                Text(
                        when (c?.status) {
                            null -> "Conforme"
                            ConformiteStatus.CARENCE, ConformiteStatus.CARENCE_MALADIE -> if (c.isCritical) "Carence (< MIN)" else "Sous l'optimum (< OPTIMIN)"
                            else -> if (c.isCritical) "Excès (> MAX)" else "Au-dessus de l'optimum (> OPTIMAX)"
                        },
                        fontSize = 12.sp,
                        color = couleur,
                        modifier = Modifier.width(230.dp)
                )
            }
        }
        Text("* valeur incomplète : composition absente pour au moins un aliment.", fontSize = 10.sp, color = Color.Gray)
    }
}
