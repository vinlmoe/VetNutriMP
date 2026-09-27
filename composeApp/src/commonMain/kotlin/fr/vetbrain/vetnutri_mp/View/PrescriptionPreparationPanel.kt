package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.PlanEvolutif
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.sortedForDisplay
import fr.vetbrain.vetnutri_mp.Export.HtmlSection
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.AnimalDetail
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.TextUtils

/** Couleur des rations actuelles, comme dans la liste des rations. */
private val CouleurRationActuelle = Color(0xFFFF9800)
private val FormeCarte = RoundedCornerShape(12.dp)
private val FormeLigne = RoundedCornerShape(8.dp)

/**
 * Préparation de l'ordonnance et du compte rendu : sélection des rations, conseils, sections
 * personnalisées et textes du compte rendu, avec une barre d'actions fixe en bas. Sans état : la
 * vue appelante garde l'état (persisté dans la consultation) et construit les documents.
 */
@Composable
fun PrescriptionPreparationPanel(
        consultation: ConsultationEv?,
        selectedRationIds: Set<String>,
        onSelectedRationIdsChange: (Set<String>) -> Unit,
        isExamMode: Boolean,
        selectedConseils: List<HtmlSection>,
        onRemoveConseil: (HtmlSection) -> Unit,
        onAddConseil: () -> Unit,
        localSections: List<HtmlSection>,
        onRemoveLocalSection: (HtmlSection) -> Unit,
        onOpenSectionEditor: () -> Unit,
        anamnese: String,
        onAnamneseChange: (String) -> Unit,
        examenClinique: String,
        onExamenCliniqueChange: (String) -> Unit,
        facteurNutritionnelClef: String,
        onFacteurNutritionnelClefChange: (String) -> Unit,
        additionalText: String,
        onAdditionalTextChange: (String) -> Unit,
        onCopyCompteRendu: () -> Unit,
        onPreviewCompteRendu: () -> Unit,
        onPreviewPrescription: () -> Unit,
        modifier: Modifier = Modifier
) {
    val rations = remember(consultation) {
        consultation?.let { PlanEvolutif.rationsPrincipales(it).sortedForDisplay() } ?: emptyList()
    }
    val nbRationsSelectionnees = rations.count { it.uuid in selectedRationIds }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val large = maxWidth >= 900.dp
        Column(modifier = Modifier.fillMaxSize()) {
            EnTetePanneau(consultation)

            val cartesRationsEtConseils: @Composable ColumnScope.() -> Unit = {
                CarteRations(consultation, rations, selectedRationIds, onSelectedRationIdsChange)
                CarteConseils(
                        isExamMode = isExamMode,
                        selectedConseils = selectedConseils,
                        onRemoveConseil = onRemoveConseil,
                        onAddConseil = onAddConseil,
                        localSections = localSections,
                        onRemoveLocalSection = onRemoveLocalSection,
                        onOpenSectionEditor = onOpenSectionEditor
                )
            }
            val carteCompteRendu: @Composable ColumnScope.() -> Unit = {
                CarteCompteRendu(
                        anamnese, onAnamneseChange,
                        examenClinique, onExamenCliniqueChange,
                        facteurNutritionnelClef, onFacteurNutritionnelClefChange,
                        additionalText, onAdditionalTextChange
                )
            }
            val espacement = Arrangement.spacedBy(AppSizes.paddingMedium)
            Box(
                    modifier =
                            Modifier.weight(1f)
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(AppSizes.paddingMedium)
            ) {
                if (large) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = espacement) {
                        Column(Modifier.weight(1f), verticalArrangement = espacement, content = cartesRationsEtConseils)
                        Column(Modifier.weight(1f), verticalArrangement = espacement, content = carteCompteRendu)
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = espacement) {
                        cartesRationsEtConseils()
                        carteCompteRendu()
                    }
                }
            }

            BarreActions(
                    large = large,
                    resume =
                            translate(
                                    AnimalDetail.PRESCRIPTION_SUMMARY,
                                    nbRationsSelectionnees.toString(),
                                    (selectedConseils.size + localSections.size).toString()
                            ),
                    ordonnanceActive = consultation != null,
                    onCopyCompteRendu = onCopyCompteRendu,
                    onPreviewCompteRendu = onPreviewCompteRendu,
                    onPreviewPrescription = onPreviewPrescription
            )
        }
    }
}

@Composable
private fun EnTetePanneau(consultation: ConsultationEv?) {
    Column(
            modifier =
                    Modifier.fillMaxWidth()
                            .padding(
                                    start = AppSizes.paddingMedium,
                                    end = AppSizes.paddingMedium,
                                    top = AppSizes.paddingMedium
                            )
    ) {
        Text(
                translate(AnimalDetail.EXPORT_DOCUMENTS_TITLE),
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.SemiBold,
                color = VetNutriColors.Primary
        )
        val sousTitre =
                when {
                    consultation == null -> translate(AnimalDetail.NO_CONSULTATION_FOR_PRESCRIPTION)
                    consultation.date != null ->
                            translate(AnimalDetail.PRESCRIPTION_CONSULTATION_OF, formatDate(consultation.date!!))
                    else -> null
                }
        sousTitre?.let {
            Text(
                    it,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

private fun formatDate(date: kotlinx.datetime.LocalDate): String =
        "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"

/** Carte de section : icône dans une pastille, titre, action facultative, contenu. */
@Composable
private fun CarteSection(
        icone: ImageVector,
        titre: String,
        action: (@Composable () -> Unit)? = null,
        contenu: @Composable ColumnScope.() -> Unit
) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = FormeCarte,
            elevation = 0.dp,
            border = BorderStroke(1.dp, VetNutriColors.Primary.copy(alpha = 0.15f)),
            backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
                modifier = Modifier.padding(AppSizes.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                        modifier =
                                Modifier.size(32.dp)
                                        .clip(CircleShape)
                                        .background(VetNutriColors.Primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                ) {
                    Icon(icone, contentDescription = null, tint = VetNutriColors.Primary, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(AppSizes.paddingSmall))
                Text(
                        titre,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                )
                action?.invoke()
            }
            contenu()
        }
    }
}

@Composable
private fun TexteDiscret(texte: String) {
    Text(
            texte,
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
    )
}

/** Pastille de statut (ration actuelle/proposée, plan évolutif). */
@Composable
private fun Pastille(texte: String, couleur: Color) {
    Text(
            texte,
            style = MaterialTheme.typography.caption,
            fontWeight = FontWeight.Medium,
            color = couleur,
            maxLines = 1,
            modifier =
                    Modifier.clip(RoundedCornerShape(50))
                            .background(couleur.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
private fun CarteRations(
        consultation: ConsultationEv?,
        rations: List<Ration>,
        selectedRationIds: Set<String>,
        onSelectedRationIdsChange: (Set<String>) -> Unit
) {
    val toutesSelectionnees = rations.isNotEmpty() && rations.all { it.uuid in selectedRationIds }
    CarteSection(
            icone = Icons.Filled.Restaurant,
            titre = translate(AnimalDetail.PRESCRIPTION_RATIONS_TITLE),
            action =
                    if (rations.size > 1) {
                        {
                            TextButton(
                                    onClick = {
                                        val ids = rations.map { it.uuid }.toSet()
                                        onSelectedRationIdsChange(
                                                if (toutesSelectionnees) selectedRationIds - ids
                                                else selectedRationIds + ids
                                        )
                                    }
                            ) {
                                Text(
                                        translate(
                                                if (toutesSelectionnees) AnimalDetail.PRESCRIPTION_DESELECT_ALL
                                                else AnimalDetail.PRESCRIPTION_SELECT_ALL
                                        )
                                )
                            }
                        }
                    } else null
    ) {
        when {
            consultation == null -> TexteDiscret(translate(AnimalDetail.NO_CONSULTATION_FOR_PRESCRIPTION))
            rations.isEmpty() -> TexteDiscret(translate(AnimalDetail.NO_RATION_AVAILABLE))
            else ->
                    rations.forEach { ration ->
                        val selectionnee = ration.uuid in selectedRationIds
                        LigneRation(
                                ration = ration,
                                nbEtapes = PlanEvolutif.etapesDe(consultation, ration).size,
                                selectionnee = selectionnee,
                                onToggle = {
                                    onSelectedRationIdsChange(
                                            if (selectionnee) selectedRationIds - ration.uuid
                                            else selectedRationIds + ration.uuid
                                    )
                                }
                        )
                    }
        }
    }
}

@Composable
private fun LigneRation(ration: Ration, nbEtapes: Int, selectionnee: Boolean, onToggle: () -> Unit) {
    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .clip(FormeLigne)
                            .background(
                                    if (selectionnee) VetNutriColors.Primary.copy(alpha = 0.07f)
                                    else Color.Transparent
                            )
                            .border(
                                    1.dp,
                                    if (selectionnee) VetNutriColors.Primary.copy(alpha = 0.45f)
                                    else Color.Gray.copy(alpha = 0.2f),
                                    FormeLigne
                            )
                            .clickable(onClick = onToggle)
                            .padding(end = AppSizes.paddingMedium, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
                checked = selectionnee,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = VetNutriColors.Primary)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                    ration.name.ifBlank { "—" },
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (ration.actual) {
                    Pastille(translate(AnimalDetail.RATION_CURRENT), CouleurRationActuelle)
                } else {
                    Pastille(translate(AnimalDetail.RATION_PROPOSED), VetNutriColors.Primary)
                }
                if (nbEtapes > 0) {
                    Pastille(
                            translate(AnimalDetail.PRESCRIPTION_PLAN_STEPS, (nbEtapes + 1).toString()),
                            VetNutriColors.PrimaryVariant
                    )
                }
            }
        }
        Text(
                "${TextUtils.formatDecimal(ration.getQuantiteTotale(), 0)} g/j",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun CarteConseils(
        isExamMode: Boolean,
        selectedConseils: List<HtmlSection>,
        onRemoveConseil: (HtmlSection) -> Unit,
        onAddConseil: () -> Unit,
        localSections: List<HtmlSection>,
        onRemoveLocalSection: (HtmlSection) -> Unit,
        onOpenSectionEditor: () -> Unit
) {
    CarteSection(icone = Icons.Filled.Lightbulb, titre = translate(AnimalDetail.PRESCRIPTION_ADVICE_TITLE)) {
        if (isExamMode) {
            TexteDiscret(translate(AnimalDetail.CUSTOM_ADVICE_UNAVAILABLE_EXAM_MODE))
            return@CarteSection
        }
        if (selectedConseils.isEmpty() && localSections.isEmpty()) {
            TexteDiscret(translate(AnimalDetail.PRESCRIPTION_NO_ADVICE))
        }
        selectedConseils.forEach { conseil ->
            LigneElement(
                    titre = conseil.title,
                    detail = conseil.category.name,
                    onRemove = { onRemoveConseil(conseil) }
            )
        }
        if (localSections.isNotEmpty()) {
            Text(
                    translate(AnimalDetail.PRESCRIPTION_CUSTOM_SECTIONS, localSections.size.toString()),
                    style = MaterialTheme.typography.overline,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = AppSizes.paddingSmall)
            )
            localSections.forEach { section ->
                LigneElement(
                        titre = section.title,
                        detail = translate(AnimalDetail.BLOCKS_COUNT, section.content.blocks.size.toString()),
                        onRemove = { onRemoveLocalSection(section) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)) {
            OutlinedButton(onClick = onAddConseil, shape = FormeLigne) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(translate(AnimalDetail.ADD_ADVICE))
            }
            TextButton(onClick = onOpenSectionEditor) {
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(translate(AnimalDetail.PRESCRIPTION_NEW_SECTION))
            }
        }
    }
}

/** Conseil ou section ajouté(e) : titre, détail discret, bouton de retrait. */
@Composable
private fun LigneElement(titre: String, detail: String, onRemove: () -> Unit) {
    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .clip(FormeLigne)
                            .background(VetNutriColors.Primary.copy(alpha = 0.05f))
                            .padding(start = AppSizes.paddingMedium, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                    titre,
                    style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
            )
            Text(detail, style = MaterialTheme.typography.caption, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
        }
        IconButton(onClick = onRemove) {
            Icon(
                    Icons.Filled.Close,
                    contentDescription = translate(General.DELETE),
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun CarteCompteRendu(
        anamnese: String,
        onAnamneseChange: (String) -> Unit,
        examenClinique: String,
        onExamenCliniqueChange: (String) -> Unit,
        facteurNutritionnelClef: String,
        onFacteurNutritionnelClefChange: (String) -> Unit,
        additionalText: String,
        onAdditionalTextChange: (String) -> Unit
) {
    CarteSection(icone = Icons.Filled.Description, titre = translate(AnimalDetail.PRESCRIPTION_REPORT_TITLE)) {
        ChampTexte(anamnese, onAnamneseChange, translate(AnimalDetail.CR_SECTION_ANAMNESE), 6)
        ChampTexte(examenClinique, onExamenCliniqueChange, translate(AnimalDetail.CR_EXAM_CLINIQUE), 6)
        ChampTexte(
                facteurNutritionnelClef,
                onFacteurNutritionnelClefChange,
                translate(AnimalDetail.CR_KEY_NUTRITIONAL_FACTOR),
                4
        )
        ChampTexte(additionalText, onAdditionalTextChange, translate(AnimalDetail.ADDITIONAL_TEXT_LABEL), 6)
    }
}

@Composable
private fun ChampTexte(valeur: String, onChange: (String) -> Unit, libelle: String, maxLignes: Int) {
    OutlinedTextField(
            value = valeur,
            onValueChange = onChange,
            label = { Text(libelle) },
            minLines = 2,
            maxLines = maxLignes,
            shape = FormeLigne,
            colors =
                    TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = VetNutriColors.Primary,
                            focusedLabelColor = VetNutriColors.Primary,
                            cursorColor = VetNutriColors.Primary
                    ),
            modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun BarreActions(
        large: Boolean,
        resume: String,
        ordonnanceActive: Boolean,
        onCopyCompteRendu: () -> Unit,
        onPreviewCompteRendu: () -> Unit,
        onPreviewPrescription: () -> Unit
) {
    Surface(elevation = 8.dp, color = MaterialTheme.colors.surface) {
        val boutons: @Composable RowScope.(Modifier) -> Unit = { mod ->
            OutlinedButton(onClick = onCopyCompteRendu, shape = FormeLigne, modifier = mod) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(translate(AnimalDetail.PRESCRIPTION_COPY_REPORT), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(onClick = onPreviewCompteRendu, shape = FormeLigne, modifier = mod) {
                Icon(Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(translate(AnimalDetail.PRESCRIPTION_PREVIEW_REPORT), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Button(
                    onClick = onPreviewPrescription,
                    enabled = ordonnanceActive,
                    shape = FormeLigne,
                    colors =
                            ButtonDefaults.buttonColors(
                                    backgroundColor = VetNutriColors.Primary,
                                    contentColor = VetNutriColors.OnPrimary
                            ),
                    modifier = mod
            ) {
                Icon(Icons.Filled.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(translate(AnimalDetail.PREVIEW_PRESCRIPTION), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (large) {
            Row(
                    modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingMedium),
                    horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        resume,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f)
                )
                boutons(Modifier)
            }
        } else {
            Column(
                    modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingSmall),
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)
            ) {
                Text(
                        resume,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)) {
                    boutons(Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Choix des conseils à ajouter : recherche par titre ou catégorie, un clic ajoute le conseil ;
 * les conseils déjà présents sont cochés.
 */
@Composable
fun ConseilPickerDialog(
        availableConseils: List<HtmlSection>,
        selectedConseils: List<HtmlSection>,
        onAdd: (HtmlSection) -> Unit,
        onDismiss: () -> Unit
) {
    var recherche by remember { mutableStateOf("") }
    val idsSelectionnes = selectedConseils.map { it.id }.toSet()
    val filtres =
            availableConseils.filter {
                it.title.contains(recherche, ignoreCase = true) ||
                        it.category.name.contains(recherche, ignoreCase = true)
            }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth().heightIn(max = 600.dp)
        ) {
            Column(
                    modifier = Modifier.padding(AppSizes.paddingMedium),
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                            translate(AnimalDetail.ADD_ADVICE),
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = translate(General.CLOSE))
                    }
                }
                OutlinedTextField(
                        value = recherche,
                        onValueChange = { recherche = it },
                        placeholder = { Text(translate(AnimalDetail.SEARCH_ADVICE_HINT)) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon =
                                if (recherche.isNotEmpty()) {
                                    {
                                        IconButton(onClick = { recherche = "" }) {
                                            Icon(Icons.Filled.Close, contentDescription = null)
                                        }
                                    }
                                } else null,
                        singleLine = true,
                        shape = FormeLigne,
                        modifier = Modifier.fillMaxWidth()
                )
                if (filtres.isEmpty()) {
                    TexteDiscret(translate(AnimalDetail.PRESCRIPTION_NO_ADVICE_FOUND))
                }
                LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filtres, key = { it.id }) { conseil ->
                        val dejaAjoute = conseil.id in idsSelectionnes
                        Row(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .clip(FormeLigne)
                                                .background(
                                                        if (dejaAjoute) VetNutriColors.Primary.copy(alpha = 0.08f)
                                                        else Color.Transparent
                                                )
                                                .border(1.dp, Color.Gray.copy(alpha = 0.2f), FormeLigne)
                                                .clickable(enabled = !dejaAjoute) { onAdd(conseil) }
                                                .padding(horizontal = AppSizes.paddingMedium, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(conseil.title, style = MaterialTheme.typography.body1, fontWeight = FontWeight.Medium)
                                Text(
                                        conseil.category.name,
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Icon(
                                    if (dejaAjoute) Icons.Filled.Check else Icons.Filled.Add,
                                    contentDescription =
                                            if (dejaAjoute) translate(AnimalDetail.SELECTED) else translate(General.ADD),
                                    tint = VetNutriColors.Primary
                            )
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(translate(General.CLOSE)) }
                }
            }
        }
    }
}
